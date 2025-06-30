/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT150MI.UpdSalePointSap
 * Description : Update records to the EXT150 table.
 * Date         Changed By   Description
 * 20240408     YVOYOU       CMD56 - Sale popint SAP vs XOMI
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdSalePointSap extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction

  public UpdSalePointSap(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    if (mi.in.get("PVSA") == null) {
      mi.error("Le point de vente SAP est obligatoire")
      return
    }

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT150").index("00").build()
    DBContainer EXT150 = query.getContainer()
    EXT150.set("EXCONO", currentCompany)
    EXT150.set("EXNPVT",  mi.in.get("NPVT"))
    if(!query.readLock(EXT150, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Update EXT150
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.set("EXPVSA", mi.in.get("PVSA"))
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
}
