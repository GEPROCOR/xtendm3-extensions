/**
 * README
 * This extension is used by extensions
 *
 * Name : EXT806MI.UpdFileLog
 * Description : Update file log
 * Date         Changed By   Description
 * 20230223     RENARN       INTI99 Interface log management
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdFileLog extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction

  public UpdFileLog(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    if(mi.in.get("USID") != null){
      DBAction CMNUSR_query = database.table("CMNUSR").index("00").build()
      DBContainer CMNUSR = CMNUSR_query.getContainer()
      CMNUSR.set("JUCONO",currentCompany)
      CMNUSR.set("JUUSID",mi.in.get("USID"))
      if (!CMNUSR_query.read(CMNUSR)) {
        mi.error("Utilisateur " + mi.in.get("USID") + " n'existe pas")
        return
      }
    }
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT806").index("00").build()
    DBContainer EXT806 = query.getContainer()
    EXT806.set("EXCONO", currentCompany)
    EXT806.set("EXZMMN",  mi.in.get("ZMMN"))
    EXT806.set("EXLMTS",  mi.in.get("LMTS"))
    if(!query.readLock(EXT806, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    if (mi.in.get("MINM") != null)
      lockedResult.set("EXMINM", mi.in.get("MINM"))
    if (mi.in.get("TRNM") != null)
      lockedResult.set("EXTRNM", mi.in.get("TRNM"))
    if (mi.in.get("MSID") != null)
      lockedResult.set("EXMSID", mi.in.get("MSID"))
    if (mi.in.get("MSGD") != null)
      lockedResult.set("EXMSGD", mi.in.get("MSGD"))
    if (mi.in.get("ZLIN") != null)
      lockedResult.set("EXZLIN", mi.in.get("ZLIN"))
    if (mi.in.get("ZIFN") != null)
      lockedResult.set("EXZIFN", mi.in.get("ZIFN"))
    if (mi.in.get("ZIFL") != null)
      lockedResult.set("EXZIFL", mi.in.get("ZIFL"))
    if (mi.in.get("USID") != null)
      lockedResult.set("EXUSID", mi.in.get("USID"))
    if (mi.in.get("COID") != null)
      lockedResult.set("EXCOID", mi.in.get("COID"))
    if (mi.in.get("STAT") != null)
      lockedResult.set("EXSTAT", mi.in.get("STAT"))
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
}
