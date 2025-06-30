/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT080MI.LstPrcLstCriter
 * Description : List records from the EXT080 table.
 * Date         Changed By   Description
 * 20210408     RENARN       TARX03 - Add price list
 * 20211015     RENARN       New output parameter : business area
 * 20211206     RENARN       New output parameter : warehouse
 * 20231103     RENARN       New output parameter : flag coût logistique
 * 20240625		  RENARN		   Snake case has been fixed
 * 20240331		  YVOYOU		   Change IPDE string 10
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class LstPrcLstCriter extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany
  private String currentDivision
  private String prrf
  private String cucd
  private String cuno
  private String fvdt
  private String lvdt
  private String zupa
  private String zipl
  private String pide
  private String pideDescription
  private String zupd
  private String stat
  private String buar
  private String whlo
  private String flag
  private Integer nbMaxRecord = 10000

  public LstPrcLstCriter(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
  }

  public void main() {
    currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    currentDivision = program.getLDAZD().DIVI
    if (mi.in.get("PRRF") == null) {
      DBAction query = database.table("EXT080").index("00").selection("EXPRRF", "EXCUCD", "EXCUNO", "EXFVDT", "EXLVDT", "EXZUPA", "EXZIPL", "EXPIDE", "EXZUPD", "EXSTAT", "EXBUAR", "EXWHLO", "EXFLAG", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT080 = query.getContainer()
      EXT080.set("EXCONO", currentCompany)
      if(!query.readAll(EXT080, 1, nbMaxRecord, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    } else {
      String prrf = mi.in.get("PRRF")
      ExpressionFactory expression = database.getExpressionFactory("EXT080")
      expression = expression.ge("EXPRRF", prrf)
      DBAction query = database.table("EXT080").index("00").matching(expression).selection("EXPRRF", "EXCUCD", "EXCUNO", "EXFVDT", "EXLVDT", "EXZUPA", "EXZIPL", "EXPIDE", "EXZUPD", "EXSTAT", "EXBUAR", "EXWHLO", "EXFLAG", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT080 = query.getContainer()
      EXT080.set("EXCONO", currentCompany)
      if(!query.readAll(EXT080, 1, nbMaxRecord, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }
  // Retrieve EXT080
  Closure<?> outData = { DBContainer EXT080 ->
    DBAction promotionQuery = database.table("OPROMH").index("00").selection("FZPIDE", "FZTX15").build()
    DBContainer OPROMH = promotionQuery.getContainer()
    pideDescription = ""
    OPROMH.set("FZCONO", currentCompany)
    OPROMH.set("FZDIVI", currentDivision)
    OPROMH.set("FZPIDE", EXT080.get("EXPIDE"))
    if (promotionQuery.readAll(OPROMH, 3, nbMaxRecord, outDataOPROMH)) {
    }
    prrf = EXT080.get("EXPRRF")
    cucd = EXT080.get("EXCUCD")
    cuno = EXT080.get("EXCUNO")
    fvdt = EXT080.get("EXFVDT")
    lvdt = EXT080.get("EXLVDT")
    zupa = EXT080.get("EXZUPA")
    zipl = EXT080.get("EXZIPL")
    pide = EXT080.get("EXPIDE")
    zupd = EXT080.get("EXZUPD") as Integer
    stat = EXT080.get("EXSTAT")
    buar = EXT080.get("EXBUAR")
    whlo = EXT080.get("EXWHLO")
    flag = EXT080.get("EXFLAG") as Integer
    String entryDate = EXT080.get("EXRGDT")
    String entryTime = EXT080.get("EXRGTM")
    String changeDate = EXT080.get("EXLMDT")
    String changeNumber = EXT080.get("EXCHNO")
    String changedBy = EXT080.get("EXCHID")
    mi.outData.put("PRRF", prrf)
    mi.outData.put("CUCD", cucd)
    mi.outData.put("CUNO", cuno)
    mi.outData.put("FVDT", fvdt)
    mi.outData.put("LVDT", lvdt)
    mi.outData.put("ZUPA", zupa)
    mi.outData.put("ZIPL", zipl)
    mi.outData.put("PIDE", pide)
    mi.outData.put("TX15", pideDescription)
    mi.outData.put("ZUPD", zupd)
    mi.outData.put("STAT", stat)
    mi.outData.put("BUAR", buar)
    mi.outData.put("WHLO", whlo)
    mi.outData.put("FLAG", flag)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
  // Retrieve OPROMH
  Closure<?> outDataOPROMH = { DBContainer OPROMH ->
    pideDescription = OPROMH.get("FZTX15")
  }
}
