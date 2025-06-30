/**
 * README
 * This extension is used by extensions
 *
 * Name : EXT805MI.Get
 * Description : Get general settings by log
 * Date         Changed By   Description
 * 20201215     YOUYVO       INTI99 Interface log management
 * 20220426     RENARN       Description has been added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class Get extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program

  public Get(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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
    DBAction query = database.table("EXT805").index("10").selection("EXZMMN", "EXZREP", "EXUSID").build()
    DBContainer EXT805 = query.getContainer()
    EXT805.set("EXCONO", currentCompany)
    EXT805.set("EXCOID",  mi.in.get("COID"))
    if(!query.readAll(EXT805, 2, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Retrieve EXT805
  Closure<?> outData = { DBContainer EXT805 ->
    String zmmn = EXT805.get("EXZMMN")
    String zrep = EXT805.get("EXZREP")
    String usid = EXT805.get("EXUSID")
    mi.outData.put("ZMMN", zmmn)
    mi.outData.put("ZREP", zrep)
    mi.outData.put("USID", usid)
  }
}
