/**
 * README
 * This extension is used by EVS100
 *
 * Name : EXT880MI.AddTblSelFnc
 * Description : AddTblSelFnc general settings by extension
 * Date         Changed By   Description
 * 20240913     YVOYOU       create
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddTblSelFnc extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction

  public AddTblSelFnc(LoggerAPI logger, MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.logger = logger
  }

  public void main() {
    Integer fest = 0
    if (mi.in.get("FEST") == null) {
      fest = 0
    }else{
      fest = mi.in.get("FEST")
    }
    String resp = ""
    if (mi.in.get("RESP") == null) {
      resp = ""
    }else{
      resp = mi.in.get("RESP")
    }
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
    if (!query.read(CSYFTT)) {
      CSYFTT.set("CTRESP", resp)
      CSYFTT.setInt("CTFEST", fest)
      CSYFTT.setInt("CTRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      CSYFTT.setInt("CTRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      CSYFTT.setInt("CTLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      CSYFTT.setInt("CTCHNO", 1)
      CSYFTT.set("CTCHID", program.getUser())
      logger.debug("Etape4")
      query.insert(CSYFTT)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
}
