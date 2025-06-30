import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class LstCustomMsg extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final SessionAPI session
  private final MICallerAPI miCaller
  private final UtilityAPI utility
  private final ProgramAPI program
  public Integer currentCompany


  public LstCustomMsg(MIAPI mi, DatabaseAPI database, SessionAPI session, MICallerAPI miCaller, UtilityAPI utility, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.session = session
    this.miCaller = miCaller
    this.utility = utility
    this.program = program
  }

  public void main() {

    if (mi.in.get("CONO") == null)
      currentCompany = (Integer) program.getLDAZD().CONO
    else
      currentCompany = mi.in.get("CONO")

    String lncd = mi.in.get("LNCD")
    String msid = mi.in.get("MSID")

    // Construction de l'expression en fonction des champs renseignés, ITNO ou WHLO ou les deux ou aucun
    ExpressionFactory expression = database.getExpressionFactory("EXTMSG")
    if (mi.in.get("LNCD") != null && mi.in.get("MSID") != null)
      expression = expression.eq("LNCD", lncd).and(expression.eq("MSID", msid))
    else if (mi.in.get("LNCD") == null && mi.in.get("MSID") != null)
      expression = expression.eq("MSID", msid)
    else if (mi.in.get("LNCD") != null && mi.in.get("MSID") == null)
      expression = expression.eq("LNCD", lncd)

    DBAction query = database.table("EXTMSG").index("00").matching(expression).selection("EXCONO", "EXLNCD", "EXMSID", "EXLMSG", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXTMSG = query.createContainer()
    EXTMSG.set("EXCONO", currentCompany)

    if (!query.readAll(EXTMSG, 1, outData)) {
      mi.error("Aucun enregistrement trouvé")
      return;
    }
  }

  Closure<?> outData = { DBContainer EXT068 ->
    String lncd = EXT068.get("EXLNCD")
    String msid = EXT068.get("EXMSID")
    String lmsg = EXT068.get("EXLMSG")
    String rgdt = EXT068.get("EXRGDT")
    String rgtm = EXT068.get("EXRGTM")
    String lmdt = EXT068.get("EXLMDT")
    String chno = EXT068.get("EXCHNO")
    String chid = EXT068.get("EXCHID")

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
