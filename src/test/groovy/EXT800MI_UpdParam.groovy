/**
 * README
 * This extension is used by extensions
 *
 * Name : EXT800MI.UpdParam
 * Description : Update general settings by extension
 * Date         Changed By   Description
 * 20210817     RENARN       APPX25 - Management of transport costs
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdParam extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction

  public UpdParam(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    if(!query.readLock(EXT800, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    if (mi.in.get("P001") != null)
      lockedResult.set("EXP001", mi.in.get("P001"))
    if (mi.in.get("P002") != null)
      lockedResult.set("EXP002", mi.in.get("P002"))
    if (mi.in.get("P003") != null)
      lockedResult.set("EXP003", mi.in.get("P003"))
    if (mi.in.get("P004") != null)
      lockedResult.set("EXP004", mi.in.get("P004"))
    if (mi.in.get("P005") != null)
      lockedResult.set("EXP005", mi.in.get("P005"))
    if (mi.in.get("P006") != null)
      lockedResult.set("EXP006", mi.in.get("P006"))
    if (mi.in.get("P007") != null)
      lockedResult.set("EXP007", mi.in.get("P007"))
    if (mi.in.get("P008") != null)
      lockedResult.set("EXP008", mi.in.get("P008"))
    if (mi.in.get("P009") != null)
      lockedResult.set("EXP009", mi.in.get("P009"))
    if (mi.in.get("P010") != null)
      lockedResult.set("EXP010", mi.in.get("P010"))
    if (mi.in.get("P011") != null)
      lockedResult.set("EXP011", mi.in.get("P011"))
    if (mi.in.get("P012") != null)
      lockedResult.set("EXP012", mi.in.get("P012"))
    if (mi.in.get("P013") != null)
      lockedResult.set("EXP013", mi.in.get("P013"))
    if (mi.in.get("P014") != null)
      lockedResult.set("EXP014", mi.in.get("P014"))
    if (mi.in.get("P015") != null)
      lockedResult.set("EXP015", mi.in.get("P015"))
    if (mi.in.get("P016") != null)
      lockedResult.set("EXP016", mi.in.get("P016"))
    if (mi.in.get("P017") != null)
      lockedResult.set("EXP017", mi.in.get("P017"))
    if (mi.in.get("P018") != null)
      lockedResult.set("EXP018", mi.in.get("P018"))
    if (mi.in.get("P019") != null)
      lockedResult.set("EXP019", mi.in.get("P019"))
    if (mi.in.get("P020") != null)
      lockedResult.set("EXP020", mi.in.get("P020"))
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
}
