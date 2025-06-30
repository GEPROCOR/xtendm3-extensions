/**
 * README
 * This extension is used by Interface
 *
 * Name : EXT180MI.LstDeactItem
 * Description : List records from EXT180 table.
 * Date         Changed By   Description
 * 20220620     RENARN       REAX30 - Deactivate EAN13
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class LstDeactItem extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program

  public LstDeactItem(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi
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
    if (mi.in.get("ITNO") == null) {
      DBAction query = database.table("EXT180").index("00").selection("EXITNO", "EXSUNO", "EXZINJ", "EXSTAT", "EXZTRT", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT180 = query.getContainer()
      EXT180.set("EXCONO", currentCompany)
      if(!query.readAll(EXT180, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    } else {
      String itemNumber = mi.in.get("ITNO")
      ExpressionFactory expression = database.getExpressionFactory("EXT180")
      expression = expression.ge("EXITNO", itemNumber)
      DBAction query = database.table("EXT180").index("00").matching(expression).selection("EXITNO", "EXSUNO", "EXZINJ", "EXSTAT", "EXZTRT", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT180 = query.getContainer()
      EXT180.set("EXCONO", currentCompany)
      if(!query.readAll(EXT180, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }
  // Retrieve EXT180
  Closure<?> outData = { DBContainer EXT180 ->
    String itemNumber = EXT180.get("EXITNO")
    String supplier = EXT180.get("EXSUNO")
    String injectionDate = EXT180.get("EXZINJ")
    String status = EXT180.get("EXSTAT")
    String processingDate = EXT180.get("EXZTRT")
    String entryDate = EXT180.get("EXRGDT")
    String entryTime = EXT180.get("EXRGTM")
    String changeDate = EXT180.get("EXLMDT")
    String changeNumber = EXT180.get("EXCHNO")
    String changedBy = EXT180.get("EXCHID")
    mi.outData.put("ITNO", itemNumber)
    mi.outData.put("SUNO", supplier)
    mi.outData.put("ZINJ", injectionDate)
    mi.outData.put("STAT", status)
    mi.outData.put("ZTRT", processingDate)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
