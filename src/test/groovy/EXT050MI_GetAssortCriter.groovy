/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT050MI.GetAssortCriter
 * Description : The GetAssortCriter transaction get records to the EXT050 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX02 - Add assortment
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class GetAssortCriter extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public GetAssortCriter(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
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
    String dat1 ="";
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
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
    }else{
      mi.error("Code Client est obligatoire");
      return;
    }

    if(mi.in.get("ASCD") != null){
      DBAction countryQuery = database.table("CSYTAB").index("00").build();
      DBContainer CSYTAB = countryQuery.getContainer();
      CSYTAB.set("CTCONO",currentCompany);
      CSYTAB.set("CTSTCO",  "ASCD");
      CSYTAB.set("CTSTKY", mi.in.get("ASCD"));
      if (!countryQuery.read(CSYTAB)) {
        mi.error("Code Assortiment  " + mi.in.get("ASCD") + " n'existe pas");
        return;
      }
      ascd = mi.in.get("ASCD");
    }else{
      mi.error("Code Assortiment  " + mi.in.get("ASCD") + " n'existe pas");
      return;
    }

    if(mi.in.get("DAT1") != null){
      dat1 = mi.in.get("DAT1");
      if (!utility.call("DateUtil", "isDateValid", dat1, "yyyyMMdd")) {
        mi.error("Format Date de Validité incorrect");
        return;
      }
    }else{
      mi.error("Date de Validité est obligatoire");
      return;
    }
    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT050").index("00").build()
    DBContainer EXT050 = query.getContainer();
    EXT050.set("EXCONO", currentCompany);
    EXT050.set("EXCUNO", cuno);
    EXT050.set("EXASCD", ascd);
    EXT050.setInt("EXDAT1", dat1 as Integer);
    if(!query.readAll(EXT050, 4, outData)){
      mi.error("L'enregistrement existe déjà");
      return;
    }
  }

  Closure<?> outData = { DBContainer EXT050 ->
    String cono = EXT050.get("EXCONO");
    String cuno = EXT050.get("EXCUNO");
    String ascd = EXT050.get("EXASCD");
    String dat1 = EXT050.get("EXDAT1");
    String entryDate = EXT050.get("EXRGDT");
    String entryTime = EXT050.get("EXRGTM");
    String changeDate = EXT050.get("EXLMDT");
    String changeNumber = EXT050.get("EXCHNO");
    String changedBy = EXT050.get("EXCHID");

    mi.outData.put("CONO", cono);
    mi.outData.put("CUNO", cuno);
    mi.outData.put("ASCD", ascd);
    mi.outData.put("DAT1", dat1);
    mi.outData.put("RGDT", entryDate);
    mi.outData.put("RGTM", entryTime);
    mi.outData.put("LMDT", changeDate);
    mi.outData.put("CHNO", changeNumber);
    mi.outData.put("CHID", changedBy);
    mi.write();
  }
}
