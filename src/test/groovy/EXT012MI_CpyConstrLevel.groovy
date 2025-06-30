/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT012MI.CpyConstrLevel
 * Description : Copy records to the EXT012 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class CpyConstrLevel extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction

  public CpyConstrLevel(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    DBAction query = database.table("EXT012").index("00").selection("EXZDES", "EXZCTY", "EXUSID", "EXZBLC").build()
    DBContainer EXT012 = query.getContainer()
    EXT012.set("EXCONO", currentCompany)
    EXT012.set("EXZCLV", mi.in.get("ZCLV"))
    if(query.read(EXT012)){
      EXT012.set("EXZCLV", mi.in.get("CZCL"))
      if (!query.read(EXT012)) {
        EXT012.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT012.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        EXT012.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT012.setInt("EXCHNO", 1)
        EXT012.set("EXCHID", program.getUser())
        query.insert(EXT012)
      } else {
        mi.error("L'enregistrement existe déjà")
      }
    } else {
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
}
