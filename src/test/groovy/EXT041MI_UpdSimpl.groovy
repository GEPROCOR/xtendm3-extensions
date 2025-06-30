/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT041MI.UpdSimpl
 * Description : The UpdSimpl transaction update records to the EXT041 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX12 - Margin management
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdSimpl extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public UpdSimpl(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
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

    if(mi.in.get("BOBE") != null){
      int maximumRate = 0;
      maximumRate = mi.in.get("BOBE");
      if(maximumRate < -100 || maximumRate > 100){
        mi.error("Borne basse écart doit être comprise entre -100% et 100%");
        return;
      }
    }

    if(mi.in.get("BOHE") != null){
      int maximumRate = 0;
      maximumRate = mi.in.get("BOHE");
      if(maximumRate < -100 || maximumRate > 100){
        mi.error("Borne haute écart doit être comprise entre -100% et 100%");
        return;
      }
    }

    if(mi.in.get("BOBM") != null){
      int maximumRate = 0;
      maximumRate = mi.in.get("BOBM");
      if(maximumRate < -100 || maximumRate > 100){
        mi.error("Borne basse marge moyenne doit être comprise entre -100% et 100%");
        return;
      }
    }

    if(mi.in.get("BOHM") != null){;
      int maximumRate = 0;
      maximumRate = mi.in.get("BOHM");
      if(maximumRate < -100 || maximumRate > 100){
        mi.error("Borne haute marge moyenne doit être comprise entre -100% et 100%");
        return;
      }
    }

    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT041").index("00").build()
    DBContainer EXT041 = query.getContainer();
    EXT041.set("EXCONO", currentCompany);
    EXT041.set("EXCUNO", cuno);
    EXT041.set("EXTYPE", type);
    //Update Simple in EXT041
    if(!query.readLock(EXT041, updateCallBack)){
      mi.error("L'enregistrement n'existe pas");
      return
    }
  }
  //Update Simple in EXT041
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now();
    int changeNumber = lockedResult.get("EXCHNO")
    if(mi.in.get("BOBE")!=null){
      lockedResult.setDouble("EXBOBE", mi.in.get("BOBE") as Double);
    }
    if(mi.in.get("BOHE")!=null) {
      lockedResult.setDouble("EXBOHE", mi.in.get("BOHE") as Double);
    }
    if(mi.in.get("BOBM")!=null) {
      lockedResult.setDouble("EXBOBM", mi.in.get("BOBM") as Double);
    }
    if(mi.in.get("BOHM")!=null) {
      lockedResult.setDouble("EXBOHM", mi.in.get("BOHM") as Double);
    }
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
    lockedResult.setInt("EXCHNO", changeNumber + 1);
    lockedResult.set("EXCHID", program.getUser());
    lockedResult.update();
  }
}
