/**
 * README
 * This extension is used by extensions
 *
 * Name : EXT806MI.DelFileLog
 * Description : Delete file log
 * Date         Changed By   Description
 * 20230223     RENARN       INTI99 Interface log management
 */
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class DelFileLog extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final ProgramAPI program

  public DelFileLog(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    lockedResult.delete()
  }
}
