import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdCustomMsg extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final SessionAPI session
  private final MICallerAPI miCaller
  private final UtilityAPI utility
  private final ProgramAPI program
  public Integer currentCompany

  public UpdCustomMsg(MIAPI mi, DatabaseAPI database, SessionAPI session, MICallerAPI miCaller, UtilityAPI utility, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.session = session
    this.miCaller = miCaller
    this.utility = utility
    this.program = program
  }

  public void main() {

    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer) program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    DBAction query = database.table("EXTMSG").index("00").build()
    DBContainer EXTMSG = query.getContainer()
    EXTMSG.set("EXCONO", currentCompany)
    EXTMSG.set("EXLNCD", mi.in.get("LNCD"))
    EXTMSG.set("EXMSID", mi.in.get("MSID"))
    if (!query.readLock(EXTMSG, updateCallBack)){
      mi.error("Le message n'existe pas")
      return
    }

  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    String lmsg = mi.in.get("LMSG")

    lockedResult.set("EXLMSG", lmsg)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }

}
