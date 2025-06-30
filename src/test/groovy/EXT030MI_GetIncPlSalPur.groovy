/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT030MI.GetIncPlSalPur
 * Description : The GetIncPlSalPur transaction gets records to the EXT030 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 * 20211216     CDUV         Priority added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class GetIncPlSalPur extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program

  public GetIncPlSalPur(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
  }

  public void main() {
    // Check incoterm place sale
    if(mi.in.get("ZIPS") == null || mi.in.get("ZIPS") == ""){
      mi.error("Incoterm lieu vente est obligatoire")
      return
    }

    // Check incoterm place purchase
    if(mi.in.get("ZIPP") == null || mi.in.get("ZIPP") == ""){
      mi.error("Incoterm lieu achat est obligatoire")
      return
    }
    DBAction query = database.table("EXT030").index("00").selection("EXPRIO", "EXZCOM", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT030 = query.getContainer()
    EXT030.set("EXCONO", mi.in.get("CONO"))
    EXT030.set("EXZIPS", mi.in.get("ZIPS"))
    EXT030.set("EXZIPP", mi.in.get("ZIPP"))
    if(!query.readAll(EXT030, 3, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Retrieve EXT030
  Closure<?> outData = { DBContainer EXT030 ->
    String compatible = EXT030.get("EXZCOM")
    String entryDate = EXT030.get("EXRGDT")
    String entryTime = EXT030.get("EXRGTM")
    String changeDate = EXT030.get("EXLMDT")
    String changeNumber = EXT030.get("EXCHNO")
    String changedBy = EXT030.get("EXCHID")
    String PRIO = EXT030.get("EXPRIO")
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
