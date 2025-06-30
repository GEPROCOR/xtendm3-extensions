public class DltCustomMsg extends ExtendM3Transaction {
  private final MIAPI mi
  private final MICallerAPI miCaller
  private final DatabaseAPI database
  private final ProgramAPI program

  public DltCustomMsg(MIAPI mi, MICallerAPI miCaller, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi;
    this.miCaller = miCaller
    this.database = database
    this.program = program
  }

  public void main() {

    Integer currentCompany
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
    if (!query.readLock(EXTMSG, updateCallBack)) {
      mi.error("L'enregistrement n'existe pas")
      return
    }

  }

  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
