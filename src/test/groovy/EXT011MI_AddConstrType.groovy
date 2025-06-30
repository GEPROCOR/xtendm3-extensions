/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT011MI.AddConstrType
 * Description : Add records to the EXT011 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddConstrType extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction

  public AddConstrType(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi;
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
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT011").index("00").build()
    DBContainer EXT011 = query.getContainer()
    EXT011.set("EXCONO", currentCompany)
    EXT011.set("EXZCTY",  mi.in.get("ZCTY"))
    if (!query.read(EXT011)) {
      EXT011.set("EXTX40", mi.in.get("TX40"))
      EXT011.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT011.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT011.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT011.setInt("EXCHNO", 1)
      EXT011.set("EXCHID", program.getUser())
      query.insert(EXT011)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
}
