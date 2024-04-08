/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT040MI.DelObjective
 * Description : The DelObjective transaction delete records to the EXT040 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX12 - Margin management
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DelObjective extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public DelObjective(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi;
    this.database = database;
    this.logger = logger;
    this.program = program;
    this.utility = utility;
  }

  public void main() {
    Integer currentCompany;
    String cuno = "";
    String ascd = "";
    String fdat = "";

    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
    }

    if(mi.in.get("CUNO") != null){
      cuno = mi.in.get("CUNO");
    }

    if(mi.in.get("ASCD") != null){
      ascd = mi.in.get("ASCD");
    }

    if(mi.in.get("FDAT") == null){
      mi.error("Date de Début est obligatoire");
      return;
    }else {
      fdat = mi.in.get("FDAT");
      if (!utility.call("DateUtil", "isDateValid", fdat, "yyyyMMdd")) {
        mi.error("Format Date de début incorrect");
        return;
      }
    }

    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT040").index("00").build()
    DBContainer EXT040 = query.getContainer();
    EXT040.set("EXCONO", currentCompany);
    EXT040.set("EXCUNO", cuno);
    EXT040.set("EXASCD", ascd);
    EXT040.setInt("EXFDAT", fdat as Integer);
    if(!query.readLock(EXT040, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  //Delete Objective in EXT040
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
