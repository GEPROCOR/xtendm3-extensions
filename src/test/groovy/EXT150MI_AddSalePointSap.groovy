/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT150MI.AddSalePointSap
 * Description : Add records to the EXT150 table.
 * Date         Changed By   Description
 * 20240408     YVOYOU       CMD56 - Sale popint SAP vs XOMI
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddSalePointSap extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction

  public AddSalePointSap(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    if (!query.read(EXT150)) {
      EXT150.set("EXNPVT", mi.in.get("NPVT"))
      EXT150.set("EXPVSA", mi.in.get("PVSA"))
      EXT150.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT150.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT150.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT150.setInt("EXCHNO", 1)
      EXT150.set("EXCHID", program.getUser())
      query.insert(EXT150)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
}
