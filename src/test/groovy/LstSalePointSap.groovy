/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT150MI.LstSalePointSap
 * Description : Get records to the EXT150 table.
 * Date         Changed By   Description
 * 20240408     YVOYOU       CMD56 - Sale popint SAP vs XOMI
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class LstSalePointSap extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction

  public LstSalePointSap(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    if (mi.in.get("NPVT") == null) {
      DBAction query = database.table("EXT150").index("00").selection("EXNPVT", "EXPVSA", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT150 = query.getContainer()
      EXT150.set("EXCONO", currentCompany)
      if(!query.readAll(EXT150, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    } else {
      String salePoint = mi.in.get("NPVT")
      ExpressionFactory expression = database.getExpressionFactory("EXT150")
      expression = expression.ge("EXNPVT", salePoint)
      DBAction query = database.table("EXT150").index("00").matching(expression).selection("EXNPVT", "EXPVSA", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT150 = query.getContainer()
      EXT150.set("EXCONO", currentCompany)
      if(!query.readAll(EXT150, 1, 10000, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }
  // Retrieve EXT150
  Closure<?> outData = { DBContainer EXT150 ->
    String company = EXT150.get("EXCONO")
    String salePoint = EXT150.get("EXNPVT")
    String salePointSap = EXT150.get("EXPVSA")
    String entryDate = EXT150.get("EXRGDT")
    String entryTime = EXT150.get("EXRGTM")
    String changeDate = EXT150.get("EXLMDT")
    String changeNumber = EXT150.get("EXCHNO")
    String changedBy = EXT150.get("EXCHID")
    mi.outData.put("CONO", company)
    mi.outData.put("NPVT", salePoint)
    mi.outData.put("PVSA", salePointSap)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
