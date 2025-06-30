/**
 * README
 * This extension is used by mashups
 *
 * Name : EXT815MI.AddCustomMsg
 * Description : Add custom message
 * Date         Changed By   Description
 * 20250205     PILHUG       EXTMSG - Custom error message
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddCustomMsg extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final SessionAPI session
  private final MICallerAPI miCaller
  private final UtilityAPI utility
  private final ProgramAPI program
  public Integer currentCompany

  public AddCustomMsg(MIAPI mi, DatabaseAPI database, SessionAPI session, MICallerAPI miCaller, UtilityAPI utility, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.session = session
    this.miCaller = miCaller
    this.utility = utility
    this.program = program
  }

  public void main() {
    LocalDateTime timeOfCreation = LocalDateTime.now()

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
    if (query.read(EXTMSG)){
      mi.error("L'enregistrement existe déjà")
      return
    }

    else {
      EXTMSG.set("EXLMSG",  mi.in.get("LMSG"))
      EXTMSG.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXTMSG.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXTMSG.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXTMSG.set("EXCHID", program.getUser())
      query.insert(EXTMSG)
    }
  }
}
