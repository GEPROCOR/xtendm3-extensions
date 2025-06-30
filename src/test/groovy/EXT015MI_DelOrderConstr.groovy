/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT015MI.DelOrderConstr
 * Description : Delete records from the EXT015 table.
 * Date         Changed By   Description
 * 20210311     RENARN       QUAX07 - Constraints engine
 */
public class DelOrderConstr extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final ProgramAPI program

  public DelOrderConstr(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    DBAction query = database.table("EXT015").index("00").build()
    DBContainer EXT015 = query.getContainer()
    EXT015.set("EXCONO", currentCompany)
    EXT015.set("EXORNO",  mi.in.get("ORNO"))
    EXT015.set("EXPONR",  mi.in.get("PONR"))
    EXT015.set("EXPOSX",  mi.in.get("POSX"))
    EXT015.set("EXZCSL",  mi.in.get("ZCSL"))
    if(!query.readLock(EXT015, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
