/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT040MI.UpdObjective
 * Description : The UpdObjective transaction update records to the EXT040 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX12 - Margin management
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UpdObjective extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public UpdObjective(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi;
    this.database = database;
    this.logger = logger;
    this.program = program;
    this.utility = utility;
  }

  public void main() {
    //Load Current Company
    Integer currentCompany;
    String cuno = "";
    String ascd = "";
    String fdat ="";
    String tdat ="99999999";
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
    }
    if(mi.in.get("MARG") == null){;
      mi.error("Marge est obligatoire");
      return
    }else{
      int maximumRate = 0;
      maximumRate = mi.in.get("MARG");
      if(maximumRate < -100 || maximumRate > 100){
        mi.error("Marge doit être comprise entre -100% et 100%");
        return;
      }
    }

    if(mi.in.get("FDAT") == null){
      mi.error("Date de Début est obligatoire");
      return;
    }else{
      fdat = mi.in.get("FDAT");
      if(!utility.call("DateUtil","isDateValid",fdat,"yyyyMMdd")){
        mi.error("Format Date de début incorrect");
        return;
      }
      if(mi.in.get("TDAT") != null) {
        tdat = mi.in.get("TDAT");
        if(!utility.call("DateUtil","isDateValid",fdat,"yyyyMMdd")){
          mi.error("Format Fin de début incorrect");
          return;
        }
        BigDecimal fdatNumber = new BigDecimal(fdat.toString())
        BigDecimal tdatNumber = new BigDecimal(tdat.toString())
        if(fdatNumber>tdatNumber){
          mi.error("Date de début doit être inférieure a la date de fin");
          return;
        }
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
  //Update Objective in EXT040
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now();
    int changeNumber = lockedResult.get("EXCHNO")
    String tdat = "99999999";
    if(mi.in.get("TDAT") != null) {
      tdat = mi.in.get("TDAT");
    }
    lockedResult.setDouble("EXMARG", mi.in.get("MARG") as Double);
    lockedResult.setInt("EXTDAT", Integer.parseInt(tdat));
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
    lockedResult.setInt("EXCHNO", changeNumber + 1);
    lockedResult.set("EXCHID", program.getUser());
    lockedResult.update()
  }
}
