/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT150MI.GetSalePointSap
 * Description : Get records to the EXT150 table.
 * Date         Changed By   Description
 * 20240408     YVOYOU       CMD56 - Sale popint SAP vs XOMI
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class GetSalePointSap extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction

  public GetSalePointSap(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
      mi.error("Le point de vente est obligatoire")
      return
    }
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT150").index("00").selection("EXNPVT", "EXPVSA", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT150 = query.getContainer()
    EXT150.set("EXCONO", currentCompany)
    EXT150.set("EXNPVT",  mi.in.get("NPVT"))
    if(!query.readAll(EXT150, 2, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
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
