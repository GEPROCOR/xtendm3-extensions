/**
 * README
 * This extension is used by extensions
 *
 * Name : EXT806MI.GetFileLog
 * Description : Get file log
 * Date         Changed By   Description
 * 20230223     RENARN       INTI99 Interface log management
 * 20230228     RENARN       maxRecord has been added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class GetFileLog extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program

  public GetFileLog(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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
    DBAction query = database.table("EXT806").index("00").selection("EXMINM", "EXTRNM", "EXMSID", "EXMSGD", "EXZLIN", "EXZIFN", "EXZIFL", "EXUSID", "EXCOID", "EXSTAT", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT806 = query.getContainer()
    EXT806.set("EXCONO", currentCompany)
    EXT806.set("EXZMMN",  mi.in.get("ZMMN"))
    EXT806.set("EXLMTS",  mi.in.get("LMTS"))
    if(!query.readAll(EXT806, 3, 1, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }

  Closure<?> outData = { DBContainer EXT806 ->
    String MINM = EXT806.get("EXMINM")
    String TRNM = EXT806.get("EXTRNM")
    String MSID = EXT806.get("EXMSID")
    String MSGD = EXT806.get("EXMSGD")
    String ZLIN = EXT806.get("EXZLIN")
    String ZIFN = EXT806.get("EXZIFN")
    String ZIFL = EXT806.get("EXZIFL")
    String USID = EXT806.get("EXUSID")
    String COID = EXT806.get("EXCOID")
    String STAT = EXT806.get("EXSTAT")
    String entryDate = EXT806.get("EXRGDT")
    String entryTime = EXT806.get("EXRGTM")
    String changeDate = EXT806.get("EXLMDT")
    String changeNumber = EXT806.get("EXCHNO")
    String changedBy = EXT806.get("EXCHID")
    mi.outData.put("MINM", MINM)
    mi.outData.put("TRNM", TRNM)
    mi.outData.put("MSID", MSID)
    mi.outData.put("MSGD", MSGD)
    mi.outData.put("ZLIN", ZLIN)
    mi.outData.put("ZIFN", ZIFN)
    mi.outData.put("ZIFL", ZIFL)
    mi.outData.put("USID", USID)
    mi.outData.put("COID", COID)
    mi.outData.put("STAT", STAT)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
