/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT040MI.GetObjective
 * Description : The GetObjective transaction get records to the EXT040 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX12 - Margin management
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GetObjective extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public GetObjective(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi;
    this.database = database;
    this.logger = logger;
    this.program = program;
    this.utility = utility;
  }

  public void main() {
    //Load Current Company
    Integer currentCompany;
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
    }

    //Load Parameters
    String cuno = '';
    if(mi.in.get("CUNO")!=null){
      DBAction countryQuery = database.table("OCUSMA").index("00").build();
      DBContainer OCUSMA = countryQuery.getContainer();
      OCUSMA.set("OKCONO",currentCompany);
      OCUSMA.set("OKCUNO",mi.in.get("CUNO"));
      if (!countryQuery.read(OCUSMA)) {
        mi.error("Code Client " + mi.in.get("CUNO") + " n'existe pas");
        return;
      }
      cuno=mi.in.get("CUNO");
    }else{
      mi.error("Code Client obligatoire");
      return;
    }
    String ascd = '';
    if(mi.in.get("ASCD")!=null){
      DBAction countryQuery = database.table("CSYTAB").index("00").build();
      DBContainer CSYTAB = countryQuery.getContainer();
      CSYTAB.set("CTCONO",currentCompany);
      CSYTAB.set("CTSTCO",  "ASCD");
      CSYTAB.set("CTSTKY", mi.in.get("ASCD"));
      if (!countryQuery.read(CSYTAB)) {
        mi.error("Code Assortiment  " + mi.in.get("ASCD") + " n'existe pas");
        return;
      }
      ascd=mi.in.get("ASCD");
    }

    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT040").index("00").selection("EXCONO", "EXCUNO", "EXASCD", "EXMARG","EXFDAT" ,"EXTDAT" , "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build();
    DBContainer EXT040 = query.getContainer();
    EXT040.set("EXCONO", currentCompany);
    EXT040.set("EXCUNO", cuno);

    EXT040.set("EXASCD", ascd);
    if(!query.readAll(EXT040, 3, outData)){
      mi.error("L'enregistrement existe déjà");
      return;
    }
  }
  //Get Objective
  Closure<?> outData = { DBContainer EXT040 ->
    String cono = EXT040.get("EXCONO");
    String cuno = EXT040.get("EXCUNO");
    String ascd = EXT040.get("EXASCD");
    String marg = EXT040.get("EXMARG");
    String fdat = EXT040.get("EXFDAT");
    String tdat = EXT040.get("EXTDAT");
    String entryDate = EXT040.get("EXRGDT");
    String entryTime = EXT040.get("EXRGTM");
    String changeDate = EXT040.get("EXLMDT");
    String changeNumber = EXT040.get("EXCHNO");
    String changedBy = EXT040.get("EXCHID");

    mi.outData.put("CONO", cono);
    mi.outData.put("CUNO", cuno);
    mi.outData.put("ASCD", ascd);
    mi.outData.put("MARG", marg);
    mi.outData.put("FDAT", fdat);
    mi.outData.put("TDAT", tdat);
    mi.outData.put("RGDT", entryDate);
    mi.outData.put("RGTM", entryTime);
    mi.outData.put("LMDT", changeDate);
    mi.outData.put("CHNO", changeNumber);
    mi.outData.put("CHID", changedBy);
    mi.write();
  }
}
