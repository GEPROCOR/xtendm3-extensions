/**
 * README
 * This extension is used by EVS100
 *
 * Name : EXT880MI.DelTblSelFnc
 * Description : Add general settings by extension
 * Date         Changed By   Description
 * 20240913     YVOYOU       create
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class DelTblSelFnc extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction

  public DelTblSelFnc(LoggerAPI logger, MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.logger = logger
  }

  public void main() {
    logger.debug("Etape1")
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("CSYFTT").index("00").build()
    DBContainer CSYFTT = query.getContainer()
    logger.debug("Etape2")
    CSYFTT.setInt("CTFESE", mi.in.get("FESE") as Integer)
    CSYFTT.setInt("CTPRIO", mi.in.get("PRIO") as Integer)
    CSYFTT.set("CTOBV1", mi.in.get("OBV1"))
    CSYFTT.set("CTOBV2", mi.in.get("OBV2"))
    CSYFTT.set("CTOBV3", mi.in.get("OBV3"))
    logger.debug("Etape3")
    if(!query.readLock(CSYFTT, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
