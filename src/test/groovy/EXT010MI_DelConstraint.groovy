/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT010MI.DelConstraint
 * Description : Delete records from the EXT010 table.
 * Date         Changed By   Description
 * 20210219     RENARN       QUAX01 - Constraints matrix
 */
public class DelConstraint extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final ProgramAPI program

  public DelConstraint(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    DBAction query = database.table("EXT010").index("00").build()
    DBContainer EXT010 = query.getContainer()
    EXT010.set("EXCONO", currentCompany)
    EXT010.set("EXZCID", mi.in.get("ZCID"))
    if(!query.readLock(EXT010, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
