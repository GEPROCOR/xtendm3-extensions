/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT050MI.DelAssortCriter
 * Description : The DelAssortCriter transaction delete records to the EXT050 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX02 - Add assortment
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class DelAssortCriter extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public DelAssortCriter(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
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
      /**
       DBAction countryQuery = database.table("OCUSMA").index("00").build();
       DBContainer OCUSMA = countryQuery.getContainer();
       OCUSMA.set("OKCONO",currentCompany);
       OCUSMA.set("OKCUNO",mi.in.get("CUNO"));
       if (!countryQuery.read(OCUSMA)) {
       mi.error("Code Client " + mi.in.get("CUNO") + " n'existe pas");
       return;
       }
       **/
      cuno = mi.in.get("CUNO");
    }

    if(mi.in.get("ASCD") != null){
      /**
       DBAction countryQuery = database.table("CSYTAB").index("00").build();
       DBContainer CSYTAB = countryQuery.getContainer();
       CSYTAB.set("CTCONO",currentCompany);
       CSYTAB.set("CTSTCO",  "ASCD");
       CSYTAB.set("CTSTKY", mi.in.get("ASCD"));
       if (!countryQuery.read(CSYTAB)) {
       mi.error("Code Assortiment  " + mi.in.get("ASCD") + " n'existe pas");
       return;
       }
       **/
      ascd = mi.in.get("ASCD");
    }

    if(mi.in.get("DAT1") != null){
      dat1 = mi.in.get("DAT1");
      /**
       if (!utility.call("DateUtil", "isDateValid", dat1, "yyyyMMdd")) {
       mi.error("Format Date de Validité incorrect");
       return;
       }
       **/

    }
    // Delete EXT050
    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT050").index("00").build()
    DBContainer EXT050 = query.getContainer();
    EXT050.set("EXCONO", currentCompany);
    EXT050.set("EXCUNO", cuno);
    EXT050.set("EXASCD", ascd);
    EXT050.setInt("EXDAT1", dat1 as Integer);
    if(!query.readLock(EXT050, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
    // Delete EXT051
    DBAction EXT051_query = database.table("EXT051").index("00").build()
    DBContainer EXT051 = EXT051_query.getContainer();
    EXT051.set("EXCONO", currentCompany);
    EXT051.set("EXCUNO", cuno);
    EXT051.set("EXASCD", ascd);
    EXT051.setInt("EXDAT1", dat1 as Integer);
    if(!EXT051_query.readAllLock(EXT051, 4, updateCallBack)){
    }
    // Delete EXT052
    DBAction EXT052_query = database.table("EXT052").index("00").build()
    DBContainer EXT052 = EXT052_query.getContainer();
    EXT052.set("EXCONO", currentCompany);
    EXT052.set("EXASCD", ascd);
    EXT052.set("EXCUNO", cuno);
    EXT052.set("EXFDAT", dat1 as Integer);
    if(!EXT052_query.readAllLock(EXT052, 4, updateCallBack)){
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
