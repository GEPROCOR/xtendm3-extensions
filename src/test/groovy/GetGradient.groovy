/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT014MI.GetGradient
 * Description : Retrieve records from the EXT014 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class GetGradient extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program

  public GetGradient(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi;
    this.database = database
    this.logger = logger
    this.program = program
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    DBAction query = database.table("EXT014").index("00").selection("EXZMAR", "EXZGRA", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT014 = query.getContainer()
    EXT014.set("EXCONO", currentCompany)
    EXT014.set("EXCSCD", mi.in.get("CSCD"))
    EXT014.set("EXZMIR", mi.in.get("ZMIR"))
    if(!query.readAll(EXT014, 3, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }

  Closure<?> outData = { DBContainer EXT014 ->
    String maximumRate = EXT014.get("EXZMAR")
    String gradient = EXT014.get("EXZGRA")
    String entryDate = EXT014.get("EXRGDT")
    String entryTime = EXT014.get("EXRGTM")
    String changeDate = EXT014.get("EXLMDT")
    String changeNumber = EXT014.get("EXCHNO")
    String changedBy = EXT014.get("EXCHID")
    mi.outData.put("ZMAR", maximumRate)
    mi.outData.put("ZGRA", gradient)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
