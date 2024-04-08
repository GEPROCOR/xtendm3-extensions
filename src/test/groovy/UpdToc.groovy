/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT042MI.UpdToc
 * Description : The UpdToc transaction update records to the EXT042 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX12 - Margin management
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class UpdToc extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public UpdToc(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi;
    this.database = database;
    this.logger = logger;
    this.program = program;
    this.utility = utility;
  }

  public void main() {
    Integer currentCompany;
    Long clef;
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
    }

    if (mi.in.get("CLEF") != null) {
      clef = (Long) mi.in.get("CLEF");
    } else {
      mi.error("Clef Auto Incrément est obligatoire");
      return;
    }

    if(mi.in.get("LVDT") != null) {
      Integer lvdt = mi.in.get("LVDT");
      if(!utility.call("DateUtil","isDateValid",lvdt,"yyyyMMdd")){
        mi.error("Format Date de début incorrect");
        return;
      }
    }

    DBAction query = database.table("EXT042").index("00").build()
    DBContainer EXT042 = query.getContainer();
    EXT042.set("EXCONO", currentCompany);
    EXT042.setLong("EXCLEF", clef);
    //Update Toc in EXT041
    if(!query.readLock(EXT042, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  //Update Toc in EXT041
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now();
    int changeNumber = lockedResult.get("EXCHNO")
    if(mi.in.get("LVDT") != null) {
      Integer tdat = mi.in.get("LVDT");
      lockedResult.setInt("EXLVDT", tdat);
    }
    if(mi.in.get("ADJT") != null) {
      Double adjt = mi.in.get("ADJT");
      lockedResult.setDouble("EXADJT", adjt);
    }
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
    lockedResult.setInt("EXCHNO", changeNumber + 1);
    lockedResult.set("EXCHID", program.getUser());
    lockedResult.update();
  }
}
