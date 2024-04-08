/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT041MI.GetSimpl
 * Description : The GetSimpl transaction get records to the EXT041 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX12 - Margin management
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class GetSimpl extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public GetSimpl(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
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
    DBAction query = database.table("EXT041").index("00").selection("EXCONO", "EXCUNO", "EXBOBE", "EXBOHE","EXBOBM" ,"EXBOHM" , "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build();
    DBContainer EXT041 = query.getContainer();
    EXT041.set("EXCONO", currentCompany);
    EXT041.set("EXCUNO", cuno);
    EXT041.set("EXTYPE", type);
    //Get Simple in EXT041
    if(!query.readAll(EXT041, 3, outData)){
      mi.error("L'enregistrement existe déjà");
      return;
    }
  }
  //Get Simple in EXT041
  Closure<?> outData = { DBContainer EXT041 ->
    String cono = EXT041.get("EXCONO");
    String cuno = EXT041.get("EXCUNO");
    String type = EXT041.get("EXTYPE");
    String bobe = EXT041.get("EXBOBE");
    String bohe = EXT041.get("EXBOHE");
    String bobm = EXT041.get("EXBOBM");
    String bohm = EXT041.get("EXBOHM");
    String entryDate = EXT041.get("EXRGDT");
    String entryTime = EXT041.get("EXRGTM");
    String changeDate = EXT041.get("EXLMDT");
    String changeNumber = EXT041.get("EXCHNO");
    String changedBy = EXT041.get("EXCHID");

    mi.outData.put("CONO", cono);
    mi.outData.put("CUNO", cuno);
    mi.outData.put("TYPE", type);
    mi.outData.put("BOBE", bobe);
    mi.outData.put("BOHE", bohe);
    mi.outData.put("BOBM", bobm);
    mi.outData.put("BOHM", bohm);
    mi.outData.put("RGDT", entryDate);
    mi.outData.put("RGTM", entryTime);
    mi.outData.put("LMDT", changeDate);
    mi.outData.put("CHNO", changeNumber);
    mi.outData.put("CHID", changedBy);
    mi.write();
  }
}
