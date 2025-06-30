/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT030MI.UpdIncPlSalPur
 * Description : The UpdIncPlSalPur transaction update records to the EXT030 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 * 20211216     CDUV         Priority added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdIncPlSalPur extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private Integer priority

  public UpdIncPlSalPur(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.program = program
    this.logger = logger
    this.miCaller = miCaller
  }

  public void main() {
    // Check incoterm place sale
    if(mi.in.get("ZIPS") == null || mi.in.get("ZIPS") == ""){
      mi.error("Incoterm lieu vente est obligatoire")
      return
    }

    // Check incoterm place purchase
    if(mi.in.get("ZIPP") == null || mi.in.get("ZIPP") == ""){
      mi.error("Incoterm lieu achat est obligatoire")
      return
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

    DBAction query = database.table("EXT030").index("00").selection("EXCHNO").build()
    DBContainer EXT030 = query.getContainer()
    EXT030.set("EXCONO", mi.in.get("CONO"))
    EXT030.set("EXZIPS",  mi.in.get("ZIPS"))
    EXT030.set("EXZIPP",  mi.in.get("ZIPP"))
    if(!query.readLock(EXT030, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Update EXT030
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.setInt("EXPRIO", priority)
    if(mi.in.get("ZCOM") != null)
      lockedResult.set("EXZCOM", mi.in.get("ZCOM"))
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
}
