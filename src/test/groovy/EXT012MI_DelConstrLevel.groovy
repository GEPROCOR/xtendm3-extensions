/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT012MI.DelConstrLevel
 * Description : Delete records from the EXT012 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
public class DelConstrLevel extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final ProgramAPI program

  public DelConstrLevel(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    DBAction query = database.table("EXT012").index("00").build()
    DBContainer EXT012 = query.getContainer()
    EXT012.set("EXCONO", currentCompany)
    EXT012.set("EXZCLV", mi.in.get("ZCLV"))
    if(!query.readLock(EXT012, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
