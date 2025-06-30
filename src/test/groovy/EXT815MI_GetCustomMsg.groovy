import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class GetCustomMsg extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final SessionAPI session
  private final MICallerAPI miCaller
  private final UtilityAPI utility
  private final ProgramAPI program
  public Integer currentCompany

  public GetCustomMsg(MIAPI mi, DatabaseAPI database, SessionAPI session, MICallerAPI miCaller, UtilityAPI utility, ProgramAPI program) {
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

    DBAction query = database.table("EXTMSG").index("00").selection("EXLNCD","EXMSID","EXLMSG","EXRGDT","EXRGTM","EXLMDT","EXCHNO","EXCHID")build()
    DBContainer EXTMSG = query.getContainer()
    EXTMSG.set("EXCONO", currentCompany)
    EXTMSG.set("EXLNCD", mi.in.get("LNCD"))
    EXTMSG.set("EXMSID", mi.in.get("MSID"))
    if (!query.read(EXTMSG)){
      mi.error("Pas de message trouvé")
      return
    }
    else {
      String lncd = EXTMSG.get("EXLNCD")
      String msid = EXTMSG.get("EXMSID")
      String lmsg = EXTMSG.get("EXLMSG")
      String rgdt = EXTMSG.get("EXRGDT")
      String rgtm = EXTMSG.get("EXRGTM")
      String lmdt = EXTMSG.get("EXLMDT")
      String chno = EXTMSG.get("EXCHNO")
      String chid = EXTMSG.get("EXCHID")
      mi.outData.put("CONO", Integer.toString(currentCompany))
      mi.outData.put("LNCD", lncd)
      mi.outData.put("MSID", msid)
      mi.outData.put("LMSG", lmsg)
      mi.outData.put("RGDT", rgdt)
      mi.outData.put("RGTM", rgtm)
      mi.outData.put("LMDT", lmdt)
      mi.outData.put("CHNO", chno)
      mi.outData.put("CHID", chid)
      mi.write()
    }

  }
}
