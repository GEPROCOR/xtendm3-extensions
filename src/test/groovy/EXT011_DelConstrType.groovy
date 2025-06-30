/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT011MI.DelConstrType
 * Description : Delete records from the EXT011 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class DelConstrType extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final ProgramAPI program

  public DelConstrType(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    DBAction query = database.table("EXT011").index("00").build()
    DBContainer EXT011 = query.getContainer()
    EXT011.set("EXCONO", currentCompany)
    EXT011.set("EXZCTY", mi.in.get("ZCTY"))
    if(!query.readLock(EXT011, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
