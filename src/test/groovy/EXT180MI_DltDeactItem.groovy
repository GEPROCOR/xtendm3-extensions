/**
 * README
 * This extension is used by Interface
 *
 * Name : EXT180MI.DltDeactItem
 * Description : Delete records from the EXT180 table.
 * Date         Changed By   Description
 * 20220620     RENARN       REAX30 - Deactivate EAN13
 */
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class DltDeactItem extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final ProgramAPI program

  public DltDeactItem(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    DBAction query = database.table("EXT180").index("00").build()
    DBContainer EXT180 = query.getContainer()
    EXT180.set("EXCONO", currentCompany)
    EXT180.set("EXITNO",  mi.in.get("ITNO"))
    EXT180.set("EXSUNO",  mi.in.get("SUNO"))
    EXT180.set("EXZINJ",  mi.in.get("ZINJ") as Integer)
    if(!query.readLock(EXT180, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Delete EXT180
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
