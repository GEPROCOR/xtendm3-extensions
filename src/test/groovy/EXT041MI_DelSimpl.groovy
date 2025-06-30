/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT041MI.DelSimpl
 * Description : The DelSimpl transaction delete records to the EXT041 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX12 - Margin management
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class DelSimpl extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public DelSimpl(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi;
    this.database = database;
    this.logger = logger;
    this.program = program;
    this.utility = utility;
  }

  public void main() {
    Integer currentCompany;
    String cuno = "";
    String type = "";
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
    }
    if(mi.in.get("TYPE") != null){
      type = mi.in.get("TYPE");
    }else{
      mi.error("Type est obligatoire");
      return;
    }
    if(mi.in.get("CUNO") != null){
      DBAction countryQuery = database.table("OCUSMA").index("00").build();
      DBContainer OCUSMA = countryQuery.getContainer();
      OCUSMA.set("OKCONO",currentCompany);
      OCUSMA.set("OKCUNO",mi.in.get("CUNO"));
      if (!countryQuery.read(OCUSMA)) {
        mi.error("Code Client " + mi.in.get("CUNO") + " n'existe pas");
        return;
      }
      cuno = mi.in.get("CUNO");
    }

    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT041").index("00").build()
    DBContainer EXT041 = query.getContainer();
    EXT041.set("EXCONO", currentCompany);
    EXT041.set("EXCUNO", cuno);
    EXT041.set("EXTYPE", type);
    //Delete Simple in EXT041
    if(!query.readLock(EXT041, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  //Delete Simple in EXT041
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }

}
