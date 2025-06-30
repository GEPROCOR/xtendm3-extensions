/**
 * README
 * This extension is used by extensions
 *
 * Name : EXT800MI.AddParam
 * Description : Add general settings by extension
 * Date         Changed By   Description
 * 20210817     RENARN       APPX25 - Management of transport costs
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddParam extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction

  public AddParam(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    DBAction query = database.table("EXT800").index("00").build()
    DBContainer EXT800 = query.getContainer()
    EXT800.set("EXCONO", currentCompany)
    EXT800.set("EXEXNM",  mi.in.get("EXNM"))
    if (!query.read(EXT800)) {
      EXT800.set("EXP001", mi.in.get("P001"))
      EXT800.set("EXP002", mi.in.get("P002"))
      EXT800.set("EXP003", mi.in.get("P003"))
      EXT800.set("EXP004", mi.in.get("P004"))
      EXT800.set("EXP005", mi.in.get("P005"))
      EXT800.set("EXP006", mi.in.get("P006"))
      EXT800.set("EXP007", mi.in.get("P007"))
      EXT800.set("EXP008", mi.in.get("P008"))
      EXT800.set("EXP009", mi.in.get("P009"))
      EXT800.set("EXP010", mi.in.get("P010"))
      EXT800.set("EXP011", mi.in.get("P011"))
      EXT800.set("EXP012", mi.in.get("P012"))
      EXT800.set("EXP013", mi.in.get("P013"))
      EXT800.set("EXP014", mi.in.get("P014"))
      EXT800.set("EXP015", mi.in.get("P015"))
      EXT800.set("EXP016", mi.in.get("P016"))
      EXT800.set("EXP017", mi.in.get("P017"))
      EXT800.set("EXP018", mi.in.get("P018"))
      EXT800.set("EXP019", mi.in.get("P019"))
      EXT800.set("EXP020", mi.in.get("P020"))
      EXT800.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT800.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT800.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT800.setInt("EXCHNO", 1)
      EXT800.set("EXCHID", program.getUser())
      query.insert(EXT800)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
}
