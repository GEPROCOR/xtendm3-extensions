/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT015MI.UpdOrderConstr
 * Description : Update records from the EXT015 table.
 * Date         Changed By   Description
 * 20210311     RENARN       QUAX07 - Constraints engine
 * 20220310     RENARN       ZTPS added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdOrderConstr extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction

  public UpdOrderConstr(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.program = program
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Check status
    if(mi.in.get("STAT") != null && mi.in.get("STAT") != "20" && mi.in.get("STAT") != "30" && mi.in.get("STAT") != "40" && mi.in.get("STAT") != "45" && mi.in.get("STAT") != "50" && mi.in.get("STAT") != "55" && mi.in.get("STAT") != "60" && mi.in.get("STAT") != "70" && mi.in.get("STAT") != "80" && mi.in.get("STAT") != "90"){
      mi.error("Statut " + mi.in.get("STAT") + " est invalide")
      return
    }
    DBAction query = database.table("EXT015").index("00").selection("EXCONO", "EXORNO", "EXPONR", "EXPOSX", "EXZCSL", "EXZCTR").build()
    DBContainer EXT015 = query.getContainer()
    String orderNumber = mi.in.get("ORNO")
    Integer orderNumberLine = mi.in.get("PONR")
    Integer orderLineSuffix = mi.in.get("POSX")
    Integer constraintLine = mi.in.get("ZCSL")
    EXT015.set("EXCONO", currentCompany)
    EXT015.set("EXORNO", orderNumber)
    EXT015.set("EXPONR", orderNumberLine)
    EXT015.set("EXPOSX", orderLineSuffix)
    EXT015.set("EXZCSL", constraintLine)
    if(!query.readLock(EXT015, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Update EXT015
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    if (mi.in.get("ZCTR") != null)
      lockedResult.set("EXZCTR", mi.in.get("ZCTR"))
    if (mi.in.get("TXI2") != null)
      lockedResult.set("EXTXI2", mi.in.get("TXI2"))
    if (mi.in.get("STAT") != null)
      lockedResult.set("EXSTAT", mi.in.get("STAT"))
    if (mi.in.get("ZTPS") != null)
      lockedResult.set("EXZTPS", mi.in.get("ZTPS"))
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
}
