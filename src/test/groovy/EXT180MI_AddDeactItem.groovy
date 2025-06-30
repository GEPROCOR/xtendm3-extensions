/**
 * README
 * This extension is used by Interface
 *
 * Name : EXT180MI.AddDeactItem
 * Description : Add records to the EXT180 table.
 * Date         Changed By   Description
 * 20220620     RENARN       REAX30 - Deactivate EAN13
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddDeactItem extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction

  public AddDeactItem(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    // Check item
    if(mi.in.get("ITNO") != null && mi.in.get("ITNO") != ""){
      DBAction Query = database.table("MITMAS").index("00").build()
      DBContainer MITMAS = Query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO",  mi.in.get("ITNO"))
      if (!Query.read(MITMAS)) {
        mi.error("Code article " + mi.in.get("ITNO") + " n'existe pas")
        return
      }
    }
    // Check supplier
    if(mi.in.get("SUNO") != null && mi.in.get("SUNO") != ""){
      DBAction query = database.table("CIDMAS").index("00").build()
      DBContainer CIDMAS = query.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO",  mi.in.get("SUNO"))
      if (!query.read(CIDMAS)) {
        mi.error("Fournisseur " + mi.in.get("SUNO") + " n'existe pas")
        return
      }
    }
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT180").index("00").build()
    DBContainer EXT180 = query.getContainer()
    EXT180.set("EXCONO", currentCompany)
    EXT180.set("EXITNO",  mi.in.get("ITNO"))
    EXT180.set("EXSUNO",  mi.in.get("SUNO"))
    EXT180.set("EXZINJ",  mi.in.get("ZINJ") as Integer)
    if (!query.read(EXT180)) {
      EXT180.set("EXSTAT", "05")
      EXT180.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT180.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT180.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT180.setInt("EXCHNO", 1)
      EXT180.set("EXCHID", program.getUser())
      query.insert(EXT180)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
}
