/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT014MI.DelGradient
 * Description : Delete records from the EXT014 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
public class DelGradient extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final ProgramAPI program

  public DelGradient(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    DBAction query = database.table("EXT014").index("00").build()
    DBContainer EXT014 = query.getContainer()
    EXT014.set("EXCONO", currentCompany)
    EXT014.set("EXCSCD", mi.in.get("CSCD"))
    EXT014.set("EXZMIR", mi.in.get("ZMIR"))
    if(!query.readLock(EXT014, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
