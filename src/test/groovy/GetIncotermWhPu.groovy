import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT033MI.AddIncotermWhPu
 * Description : The AddIncotermWhPu transaction add records to the EXT033 table. 
 * Date         Changed By   Description
 * 20211213     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 */

public class GetIncotermWhPu extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program

  public GetIncotermWhPu(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
  }
  public void main() {
    // Check Warehouse
    if(mi.in.get("WHLO") == null || mi.in.get("WHLO") == ""){
      mi.error("Depot est obligatoire")
      return
    }

    // Check incoterm place purchase
    if(mi.in.get("ZIPP") == null || mi.in.get("ZIPP") == ""){
      mi.error("Incoterm lieu achat est obligatoire")
      return
    }
    DBAction query = database.table("EXT033").index("00").selection("EXPRIO", "EXZCOM", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT033 = query.getContainer()
    EXT033.set("EXCONO", mi.in.get("CONO"))
    EXT033.set("EXWHLO", mi.in.get("WHLO"))
    EXT033.set("EXZIPP", mi.in.get("ZIPP"))
    if(!query.readAll(EXT033, 3, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Retrieve EXT033
  Closure<?> outData = { DBContainer EXT033 ->
    String compatible = EXT033.get("EXZCOM")
    String entryDate = EXT033.get("EXRGDT")
    String entryTime = EXT033.get("EXRGTM")
    String changeDate = EXT033.get("EXLMDT")
    String changeNumber = EXT033.get("EXCHNO")
    String changedBy = EXT033.get("EXCHID")
    String PRIO = EXT033.get("EXPRIO")
    mi.outData.put("PRIO", PRIO)
    mi.outData.put("ZCOM", compatible)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
