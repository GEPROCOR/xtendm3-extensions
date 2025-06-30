/**
 * README
 * This extension is used by extensions
 *
 * Name : EXT800MI.DelParam
 * Description : Delete general settings by extension
 * Date         Changed By   Description
 * 20210817     RENARN       APPX25 - Management of transport costs
 */
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class DelParam extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final ProgramAPI program

  public DelParam(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi;
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
    DBAction query = database.table("EXT800").index("00").build()
    DBContainer EXT800 = query.getContainer()
    EXT800.set("EXCONO", currentCompany)
    EXT800.set("EXEXNM", mi.in.get("EXNM"))
    if(!query.readLock(EXT800, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
