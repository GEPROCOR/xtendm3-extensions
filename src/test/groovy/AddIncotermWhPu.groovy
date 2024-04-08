/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT033MI.AddIncotermWhPu
 * Description : The AddIncotermWhPu transaction add records to the EXT033 table. 
 * Date         Changed By   Description
 * 20211213     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddIncotermWhPu extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private Integer currentCompany
  private Integer priority


  public AddIncotermWhPu(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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

    // Check Warehouse
    String whlo = ""
    if(mi.in.get("WHLO") != null) {
      DBAction query_whlo = database.table("MITWHL").index("00").build()
      DBContainer MITWHL = query_whlo.getContainer()
      MITWHL.set("MWCONO", currentCompany)
      MITWHL.set("MWWHLO", mi.in.get("WHLO"))
      if (!query_whlo.read(MITWHL)) {
        mi.error("Dépôt " + mi.in.get("WHLO") + " n'existe pas")
        return
      }
      whlo = mi.in.get("WHLO")
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
    DBAction query = database.table("EXT033").index("00").build()
    DBContainer EXT033 = query.getContainer()
    EXT033.set("EXCONO", currentCompany)
    EXT033.set("EXWHLO",  whlo)
    EXT033.set("EXZIPP",  mi.in.get("ZIPP"))
    if (!query.read(EXT033)) {
      EXT033.set("EXZCOM", mi.in.get("ZCOM"))
      EXT033.set("EXPRIO", priority)
      EXT033.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT033.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT033.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT033.setInt("EXCHNO", 1)
      EXT033.set("EXCHID", program.getUser())
      query.insert(EXT033)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
  Closure<?> closure = { DBContainer EXT031 ->
  }
}
