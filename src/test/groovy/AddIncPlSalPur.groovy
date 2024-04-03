/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT030MI.AddIncPlSalPur
 * Description : The AddIncPlSalPur transaction add records to the EXT030 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 * 20211216     CDUV         Priority added
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddIncPlSalPur extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private Integer currentCompany
  private Integer priority

  public AddIncPlSalPur(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.program = program
    this.logger = logger
    this.miCaller = miCaller
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Check incoterm place sale
    if(mi.in.get("ZIPS") == null || mi.in.get("ZIPS") == ""){
      mi.error("Incoterm lieu vente est obligatoire")
      return
    } else {
      DBAction incotermPlaceQuery = database.table("EXT031").index("10").build()
      DBContainer EXT031 = incotermPlaceQuery.getContainer()
      EXT031.set("EXCONO", currentCompany)
      EXT031.set("EXZIPL", mi.in.get("ZIPS"))
      if(!incotermPlaceQuery.readAll(EXT031, 2, closure)){
        mi.error("Incoterm lieu vente " + mi.in.get("ZIPS") + " n'existe pas")
        return
      }
    }

    // Check incoterm place purchase
    if(mi.in.get("ZIPP") == null || mi.in.get("ZIPP") == ""){
      mi.error("Incoterm lieu achat est obligatoire")
      return
    } else {
      DBAction incotermPlaceQuery = database.table("EXT031").index("10").build()
      DBContainer EXT031 = incotermPlaceQuery.getContainer()
      EXT031.set("EXCONO", currentCompany)
      EXT031.set("EXZIPL", mi.in.get("ZIPP"))
      if(!incotermPlaceQuery.readAll(EXT031, 2, closure)){
        mi.error("Incoterm lieu achat " + mi.in.get("ZIPP") + " n'existe pas")
        return
      }
    }

    // Check compatible
    if(mi.in.get("ZCOM") != 0 && mi.in.get("ZCOM") != 1){
      Integer compatible = mi.in.get("ZCOM")
      mi.error("Compatible " + compatible + " est invalide")
      return
    }
    // Check priority
    if(mi.in.get("PRIO") != "" && mi.in.get("PRIO") != null){
      priority = mi.in.get("PRIO") as Integer
      if(priority<0 || priority>100){
        mi.error("Priority " + priority + " est invalide")
        return
      }
    }


    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT030").index("00").build()
    DBContainer EXT030 = query.getContainer()
    EXT030.set("EXCONO", currentCompany)
    EXT030.set("EXZIPS",  mi.in.get("ZIPS"))
    EXT030.set("EXZIPP",  mi.in.get("ZIPP"))
    if (!query.read(EXT030)) {
      EXT030.set("EXZCOM", mi.in.get("ZCOM"))
      EXT030.set("EXPRIO", priority)
      EXT030.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT030.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT030.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT030.setInt("EXCHNO", 1)
      EXT030.set("EXCHID", program.getUser())
      query.insert(EXT030)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
  Closure<?> closure = { DBContainer EXT030 ->
  }
}
