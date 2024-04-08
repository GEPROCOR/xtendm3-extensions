/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT040MI.CopyObjective
 * Description : The CopyObjective transaction copy records to the EXT040/EXT041/EXT042 table.
 * Date         Changed By   Description
 * 20210730     APACE        TARX12 - Margin management
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class CopyObjective extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;
  public Integer currentCompany;
  public String cuno = "";
  public String cunoCopy = "";
  public String cunm = "";
  public String ascd = "";
  public String fdat = "";
  public String tdat = "";
  public String NBNR = "";
  public String cunmCopy = "";
  public CopyObjective(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi;
    this.database = database;
    this.logger = logger;
    this.program = program;
    this.utility = utility;
    this.miCaller = miCaller;
  }

  public void main() {

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
    }
    if(mi.in.get("ZCUN") != null){
      DBAction countryQuery = database.table("OCUSMA").index("00").selection("OKCUNM").build();
      DBContainer OCUSMA = countryQuery.getContainer();
      OCUSMA.set("OKCONO",currentCompany);
      OCUSMA.set("OKCUNO",mi.in.get("ZCUN"));
      if (!countryQuery.read(OCUSMA)) {
        mi.error("Code Client Copy " + mi.in.get("ZCUN") + " n'existe pas");
        return;
      }
      cunm = OCUSMA.get("OKCUNM");
      cunoCopy = mi.in.get("ZCUN");
    }
    if(mi.in.get("FDAT") == null){
      mi.error("Date de Début est obligatoire");
      return;
    }else{
      fdat = mi.in.get("FDAT");
      if(!utility.call("DateUtil","isDateValid",fdat,"yyyyMMdd")){
        mi.error("Format Date de début incorrect : "+fdat);
        return;
      }
    }

    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT040").index("00").selection("EXTDAT","EXMARG").build();
    DBContainer EXT040 = query.getContainer();
    EXT040.set("EXCONO", currentCompany);
    EXT040.set("EXCUNO", cunoCopy);
    EXT040.set("EXASCD", ascd);
    EXT040.setInt("EXFDAT", fdat as Integer);
    if (!query.read(EXT040)) {
      EXT040.set("EXCONO", currentCompany);
      EXT040.set("EXCUNO", cuno);
      EXT040.set("EXASCD", ascd);
      EXT040.setInt("EXFDAT", fdat as Integer);
      if (query.read(EXT040)) {
        tdat = EXT040.get("EXTDAT");
        EXT040.set("EXCUNO", cunoCopy);
        EXT040.set("EXCUNM", cunm);
        EXT040.setDouble("EXMARG", EXT040.get("EXMARG") as Double);
        EXT040.setInt("EXTDAT", Integer.parseInt(tdat));
        EXT040.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
        EXT040.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer);
        EXT040.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
        EXT040.setInt("EXCHNO", 1);
        EXT040.set("EXCHID", program.getUser());
        if(mi.in.get("CUNO") != null){
          DBAction countryQuery = database.table("OCUSMA").index("00").selection("OKCUNM").build();
          DBContainer OCUSMA = countryQuery.getContainer();
          OCUSMA.set("OKCONO",currentCompany);
          OCUSMA.set("OKCUNO",cunoCopy);
          if (!countryQuery.read(OCUSMA)) {
          }
          cunmCopy = OCUSMA.get("OKCUNM");
        }
        //Copy Objective in EXT040
        query.insert(EXT040);
        //Copy TOC in EXT042
        copyEXT042();
        //Copy Simpl MARG in EXT041
        copyEXT041MARG();
        //Copy Simpl T0T3 in EXT041
        copyEXT041T0T3();
      } else {
        mi.error("L'enregistrement copier n'existe pas");
        return;
      }
    } else {
      mi.error("L'enregistrement existe déjà");
      return;
    }
  }
  //Copy Simpl T0T3 in EXT041
  public void copyEXT041T0T3(){
    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT041").index("00").selection("EXCONO", "EXCUNO","EXCUNM", "EXBOBE", "EXBOHE","EXBOBM" ,"EXBOHM" , "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build();
    DBContainer EXT041 = query.getContainer();
    EXT041.set("EXCONO",currentCompany);
    EXT041.set("EXTYPE","T0T3");
    EXT041.set("EXCUNO",cuno);
    if(!query.readAll(EXT041, 3, outDataEXT041T0T3)){
    }
  }
  //Copy Simpl MARG in EXT041
  public void copyEXT041MARG(){
    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT041").index("00").selection("EXCONO", "EXCUNO","EXCUNM", "EXBOBE", "EXBOHE","EXBOBM" ,"EXBOHM" , "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build();
    DBContainer EXT041 = query.getContainer();
    EXT041.set("EXCONO",currentCompany);
    EXT041.set("EXTYPE","MARG");
    EXT041.set("EXCUNO",cuno);
    if(!query.readAll(EXT041, 3, outDataEXT041MARG)){
    }
  }
  //Copy Toc in EXT042
  public void copyEXT042(){
    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT042").index("10").selection("EXCONO","EXCLEF","EXCUNO","EXCUNM","EXHIE1","EXHIE2","EXHIE3","EXHIE4","EXHIE5",
      "EXCFI5","EXPOPN","EXBUAR","EXCFI1","EXTX15","EXADJT","EXFVDT","EXLVDT").build();
    DBContainer EXT042 = query.getContainer();
    EXT042.set("EXCONO",currentCompany);
    EXT042.set("EXCUNO",cuno);
    if(!query.readAll(EXT042, 2, outDataEXT042)){
    }
  }
  //Copy Simpl MARG in EXT041
  Closure<?> outDataEXT041MARG = { DBContainer EXT041Read ->
    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT041").index("00").build();
    DBContainer EXT041 = query.getContainer();
    EXT041.set("EXCONO",currentCompany);
    EXT041.set("EXTYPE","MARG");
    EXT041.set("EXCUNO",cunoCopy);
    if (!query.read(EXT041)) {
      EXT041.setDouble("EXBOBE", EXT041Read.get("EXBOBE").toString() as Double);
      EXT041.setDouble("EXBOHE", EXT041Read.get("EXBOHE").toString() as Double);
      EXT041.setDouble("EXBOBM", EXT041Read.get("EXBOBM").toString() as Double);
      EXT041.setDouble("EXBOHM", EXT041Read.get("EXBOHM").toString() as Double);
      EXT041.set("EXCUNM", cunmCopy);
      EXT041.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
      EXT041.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer);
      EXT041.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
      EXT041.setInt("EXCHNO", 1);
      EXT041.set("EXCHID", program.getUser());
      query.insert(EXT041);
    }
  }
  //Copy Simpl T0T3 in EXT041
  Closure<?> outDataEXT041T0T3 = { DBContainer EXT041Read ->
    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT041").index("00").build();
    DBContainer EXT041 = query.getContainer();
    EXT041.set("EXCONO",currentCompany);
    EXT041.set("EXTYPE","T0T3");
    EXT041.set("EXCUNO",cunoCopy);
    if (!query.read(EXT041)) {
      EXT041.setDouble("EXBOBE", EXT041Read.get("EXBOBE").toString() as Double);
      EXT041.setDouble("EXBOHE", EXT041Read.get("EXBOHE").toString() as Double);
      EXT041.setDouble("EXBOBM", EXT041Read.get("EXBOBM").toString() as Double);
      EXT041.setDouble("EXBOHM", EXT041Read.get("EXBOHM").toString() as Double);
      EXT041.set("EXCUNM", cunmCopy);
      EXT041.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
      EXT041.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer);
      EXT041.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
      EXT041.setInt("EXCHNO", 1);
      EXT041.set("EXCHID", program.getUser());
      query.insert(EXT041);
    }
  }
  //Copy Toc in EXT042
  Closure<?> outDataEXT042 = { DBContainer EXT042Read ->
    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT042").index("00").build();
    DBContainer EXT042 = query.getContainer();
    EXT042.set("EXCONO", currentCompany);
    EXT042.set("EXCUNO", cunoCopy);
    executeCRS165MIRtvNextNumber("ZB", "A")
    EXT042.setLong("EXCLEF", NBNR as Long);

    EXT042.set("EXPOPN", EXT042Read.get("EXPOPN").toString());
    EXT042.set("EXBUAR", EXT042Read.get("EXBUAR").toString());
    EXT042.set("EXCFI5", EXT042Read.get("EXCFI5").toString());
    EXT042.set("EXTX15", EXT042Read.get("EXTX15").toString());
    EXT042.set("EXHIE2", EXT042Read.get("EXHIE2").toString());
    EXT042.set("EXHIE3", EXT042Read.get("EXHIE3").toString());
    EXT042.set("EXHIE4", EXT042Read.get("EXHIE4").toString());
    EXT042.set("EXHIE5", EXT042Read.get("EXHIE5").toString());
    EXT042.set("EXCFI1", EXT042Read.get("EXCFI1").toString());
    EXT042.set("EXHIE1", EXT042Read.get("EXHIE1").toString());

    EXT042.setDouble("EXADJT", EXT042Read.get("EXADJT").toString() as Double);
    EXT042.setInt("EXFVDT", EXT042Read.get("EXFVDT").toString() as Integer);
    EXT042.setInt("EXLVDT", EXT042Read.get("EXLVDT").toString() as Integer);

    EXT042.set("EXCUNM", cunmCopy);

    EXT042.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
    EXT042.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer);
    EXT042.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
    EXT042.setInt("EXCHNO", 1);
    EXT042.set("EXCHID", program.getUser());
    query.insert(EXT042);
  }
  //Get ID Number with CRS615
  private executeCRS165MIRtvNextNumber(String NBTY, String NBID){
    def parameters = ["NBTY": NBTY, "NBID": NBID]
    Closure<?> handler = { Map<String, String> response ->
      NBNR = response.NBNR.trim()
      if (response.error != null) {
        return mi.error("Failed CRS165MI.RtvNextNumber: "+ response.errorMessage)
      }
    }
    miCaller.call("CRS165MI", "RtvNextNumber", parameters, handler)
  }
}
