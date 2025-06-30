/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT031MI.AddIncotermPlac
 * Description : The AddIncotermPlac transaction add records to the EXT031 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class AddIncotermPlac extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private final MICallerAPI miCaller;
  private Integer currentCompany

  public AddIncotermPlac(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.logger = logger
    this.miCaller = miCaller;
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Check incoterm
    if(mi.in.get("TEDL") == null || mi.in.get("TEDL") == ""){
      mi.error("Incoterm est obligatoire")
      return
    } else {
      DBAction incotermQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = incotermQuery.getContainer()
      CSYTAB.set("CTCONO", currentCompany)
      CSYTAB.set("CTSTCO",  "TEDL")
      CSYTAB.set("CTSTKY", mi.in.get("TEDL"))
      if (!incotermQuery.read(CSYTAB)) {
        mi.error("Incoterm " + mi.in.get("TEDL") + " n'existe pas")
        return
      }
    }
    // Check place
    String placeDescription = ""
    if(mi.in.get("ZPLA") == null || mi.in.get("ZPLA") == ""){
      mi.error("Lieu est obligatoire")
      return
    } else {
      DBAction placeQuery = database.table("CIADDR").index("00").selection("OACONM").build()
      DBContainer CIADDR = placeQuery.getContainer()
      CIADDR.set("OACONO", currentCompany)
      CIADDR.set("OAADTH",  4)
      CIADDR.set("OAADK1", mi.in.get("ZPLA"))
      if (!placeQuery.read(CIADDR)) {
        mi.error("Lieu " + mi.in.get("ZPLA") + " n'existe pas")
        return
      } else {
        // Get place description
        placeDescription = CIADDR.get("OACONM")
      }
    }

    String incoterm = mi.in.get("TEDL")
    String place = mi.in.get("ZPLA")
    String incotermPlace = incoterm + place
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT031").index("00").build()
    DBContainer EXT031 = query.getContainer()
    EXT031.set("EXCONO", currentCompany)
    EXT031.set("EXTEDL",  mi.in.get("TEDL"))
    EXT031.set("EXZPLA",  mi.in.get("ZPLA"))
    if (!query.read(EXT031)) {
      EXT031.set("EXCONM", placeDescription)
      EXT031.set("EXZIPL", incotermPlace)
      EXT031.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT031.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT031.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT031.setInt("EXCHNO", 1)
      EXT031.set("EXCHID", program.getUser())
      query.insert(EXT031)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
}
