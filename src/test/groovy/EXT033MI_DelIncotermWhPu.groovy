/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT033MI.DelIncotermWhPu
 * Description : The DelIncotermWhPu transaction del records to the EXT033 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 */
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class DelIncotermWhPu extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final ProgramAPI program
  private Integer currentCompany

  public DelIncotermWhPu(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.program = program
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Check incoterm place purchase
    if(mi.in.get("ZIPP") == null || mi.in.get("ZIPP") == ""){
      mi.error("Incoterm lieu achat est obligatoire")
      return
    }
    // Check Warehouse
    if(mi.in.get("WHLO") == null || mi.in.get("WHLO") == ""){
      mi.error("Depot est obligatoire")
      return
    }

    DBAction query = database.table("EXT033").index("00").build()
    DBContainer EXT033 = query.getContainer()
    EXT033.set("EXCONO", currentCompany)
    EXT033.set("EXWHLO", mi.in.get("WHLO"))
    EXT033.set("EXZIPP", mi.in.get("ZIPP"))
    if(!query.readLock(EXT033, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Delete EXT033
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
