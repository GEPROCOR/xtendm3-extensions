/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT013MI.DelConstrFeat
 * Description : Delete records from the EXT013 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
public class DelConstrFeat extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final ProgramAPI program

  public DelConstrFeat(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    DBAction query = database.table("EXT013").index("00").build()
    DBContainer EXT013 = query.getContainer()
    EXT013.set("EXCONO", currentCompany)
    EXT013.set("EXZCFE", mi.in.get("ZCFE"))
    if(!query.readLock(EXT013, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
