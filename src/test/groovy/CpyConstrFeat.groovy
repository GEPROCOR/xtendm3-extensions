/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT013MI.CpyConstrFeat
 * Description : Copy records to the EXT013 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class CpyConstrFeat extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction

  public CpyConstrFeat(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    DBAction query = database.table("EXT013").index("00").selection("EXZDES").build()
    DBContainer EXT013 = query.getContainer()
    EXT013.set("EXCONO", currentCompany)
    EXT013.set("EXZCFE", mi.in.get("ZCFE"))
    if(query.read(EXT013)){
      EXT013.set("EXZCFE", mi.in.get("CZCF"))
      if (!query.read(EXT013)) {
        EXT013.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT013.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        EXT013.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT013.setInt("EXCHNO", 1)
        EXT013.set("EXCHID", program.getUser())
        query.insert(EXT013)
      } else {
        mi.error("L'enregistrement existe déjà")
      }
    } else {
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
}
