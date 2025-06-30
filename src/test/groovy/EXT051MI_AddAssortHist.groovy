/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT051MI.AddAssortHist
 * Description : The AddAssortHist transaction adds records to the EXT051 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX02 - Add assortment
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class AddAssortHist extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public AddAssortHist(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
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
    String type ="";
    String data ="";
    int chb1 =0;
    String desi ="";
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
    if(mi.in.get("TYPE") == null){
      mi.error("Le type est obligatoire");
      return;
    }else{
      type = mi.in.get("TYPE");
    }

    if(mi.in.get("CHB1") == null){
      chb1 = 0;
    }else{
      if(mi.in.get("CHB1") == 1){
        chb1 = 1;
      }else{
        chb1 = 0;
      }
    }
    if(mi.in.get("DATA") != null){
      data = mi.in.get("DATA");
    }
    if(mi.in.get("DESI") != null){
      desi = mi.in.get("DESI");
    }

    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT050").index("00").build()
    DBContainer EXT050 = query.getContainer();
    EXT050.set("EXCONO", currentCompany);
    EXT050.set("EXCUNO", cuno);
    EXT050.set("EXASCD", ascd);
    EXT050.setInt("EXDAT1", dat1 as Integer);
    if (query.read(EXT050)) {
      DBAction query2 = database.table("EXT051").index("00").build()
      DBContainer EXT051 = query2.getContainer();
      EXT051.set("EXCONO", currentCompany);
      EXT051.set("EXCUNO", cuno);
      EXT051.set("EXASCD", ascd);
      EXT051.setInt("EXDAT1", dat1 as Integer);
      EXT051.set("EXTYPE", type);
      EXT051.set("EXDATA", data);
      if (!query2.read(EXT051)) {
        EXT051.setInt("EXCHB1", chb1);
        EXT051.set("EXDESI", desi);
        EXT051.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
        EXT051.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer);
        EXT051.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
        EXT051.setInt("EXCHNO", 1);
        EXT051.set("EXCHID", program.getUser());
        query2.insert(EXT051);
      } else {
        mi.error("L'enregistrement existe déjà");
        return;
      }
    } else {
      mi.error("Entête sélection n'existe pas")
      return;
    }
  }
}
