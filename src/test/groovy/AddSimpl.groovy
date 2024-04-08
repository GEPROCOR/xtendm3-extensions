/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT041MI.AddSimpl
 * Description : The AddSimpl transaction adds records to the EXT041 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX12 - Margin management
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class AddSimpl extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public AddSimpl(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
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
    String cunm = "";
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
      DBAction countryQuery = database.table("OCUSMA").selection("OKCUNM").index("00").build();
      DBContainer OCUSMA = countryQuery.getContainer();
      OCUSMA.set("OKCONO",currentCompany);
      OCUSMA.set("OKCUNO",mi.in.get("CUNO"));
      if (!countryQuery.read(OCUSMA)) {
        mi.error("Code Client " + mi.in.get("CUNO") + " n'existe pas");
        return;
      }
      cunm = OCUSMA.get("OKCUNM").toString();
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
        mi.error("Borne haute écart doit être comprise entre -100%% et 100%");
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

    if(mi.in.get("BOHM") != null){
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
    if (!query.read(EXT041)) {
      if(mi.in.get("BOBE") != null){
        EXT041.setDouble("EXBOBE", mi.in.get("BOBE") as Double);
      }
      if(mi.in.get("BOHE") != null){
        EXT041.setDouble("EXBOHE", mi.in.get("BOHE") as Double);
      }
      if(mi.in.get("BOBM") != null){
        EXT041.setDouble("EXBOBM", mi.in.get("BOBM") as Double);
      }
      if(mi.in.get("BOHM") != null){
        EXT041.setDouble("EXBOHM", mi.in.get("BOHM") as Double);
      }
      if(cunm!=''){
        EXT041.set("EXCUNM", cunm);
      }
      EXT041.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
      EXT041.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer);
      EXT041.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
      EXT041.setInt("EXCHNO", 1);
      EXT041.set("EXCHID", program.getUser());
      //Add Simpl in EXT041
      query.insert(EXT041);
    } else {
      mi.error("L'enregistrement existe déjà");
      return;
    }
  }
}
