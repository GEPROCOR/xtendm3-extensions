/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT033MI.UpdIncotermWhPu
 * Description : The UpdIncotermWhPu transaction Upd records to the EXT033 table.
 * Date         Changed By   Description
 * 20211213     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdIncotermWhPu extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private Integer currentCompany
  private Integer priority


  public UpdIncotermWhPu(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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
    }

    // Check Warehouse
    String whlo = ""
    if(mi.in.get("WHLO") == null || mi.in.get("ZIPP") == ""){
      mi.error("Warehouse est obligatoire")
      return
    }
    whlo = mi.in.get("WHLO")

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
    DBAction query = database.table("EXT033").index("00").selection("EXCHNO").build()
    DBContainer EXT033 = query.getContainer()
    EXT033.set("EXCONO", currentCompany)
    EXT033.set("EXWHLO",  whlo)
    EXT033.set("EXZIPP",  mi.in.get("ZIPP"))
    if(!query.readLock(EXT033, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Update EXT033
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.setInt("EXPRIO", priority)
    if(mi.in.get("ZCOM") != null)
      lockedResult.set("EXZCOM", mi.in.get("ZCOM"))
    if(mi.in.get("PRIO") != null)
      lockedResult.set("EXPRIO", priority)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
}
