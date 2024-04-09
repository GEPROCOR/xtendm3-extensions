/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT050MI.AddAssortCriter
 * Description : The AddAssortCriter transaction adds records to the EXT050 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX02 - Add assortment
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class AddAssortCriter extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public AddAssortCriter(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
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
      mi.error("Code Assortiment est obligatoire");
      return;
    }
    if(mi.in.get("DAT1") == null){
      mi.error("Date de Validité est obligatoire");
      return;
    }else {
      dat1 = mi.in.get("DAT1");
      if (!utility.call("DateUtil", "isDateValid", dat1, "yyyyMMdd")) {
        mi.error("Format Date de Validité incorrect");
        return;
      }
    }
    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT050").index("00").build()
    DBContainer EXT050 = query.getContainer();
    EXT050.set("EXCONO", currentCompany);
    EXT050.set("EXCUNO", cuno);
    EXT050.set("EXASCD", ascd);
    EXT050.setInt("EXDAT1", dat1 as Integer);
    if (!query.read(EXT050)) {
      EXT050.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
      EXT050.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer);
      EXT050.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
      EXT050.setInt("EXCHNO", 1);
      EXT050.set("EXCHID", program.getUser());
      query.insert(EXT050);
    } else {
      mi.error("L'enregistrement existe déjà");
      return;
    }
  }
}
