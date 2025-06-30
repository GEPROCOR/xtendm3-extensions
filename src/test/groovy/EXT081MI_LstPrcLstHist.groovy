/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT081MI.LstPrcLstHist
 * Description : List records from the EXT081 table.
 * Date         Changed By   Description
 * 20210408     RENARN       TARX03 - Add price list
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class LstPrcLstHist extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany
  private String currentDivision
  private String prrf
  private String cucd
  private String cuno
  private String fvdt
  private String ascd
  private String fdat


  public LstPrcLstHist(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi;
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
      DBAction query = database.table("EXT081").index("00").selection("EXPRRF", "EXCUCD", "EXCUNO", "EXFVDT", "EXASCD", "EXFDAT", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT081 = query.getContainer()
      EXT081.set("EXCONO", currentCompany)
      if(!query.readAll(EXT081, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    } else {
      DBAction query = database.table("EXT081").index("00").selection("EXPRRF", "EXCUCD", "EXCUNO", "EXFVDT", "EXASCD", "EXFDAT", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT081 = query.getContainer()
      EXT081.set("EXCONO", currentCompany)
      EXT081.set("EXPRRF", mi.in.get("PRRF"))
      EXT081.set("EXCUCD", mi.in.get("CUCD"))
      EXT081.set("EXCUNO", mi.in.get("CUNO"))
      EXT081.set("EXFVDT", mi.in.get("FVDT") as Integer)
      if(!query.readAll(EXT081, 5, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }

  Closure<?> outData = { DBContainer EXT081 ->
    prrf = EXT081.get("EXPRRF")
    cucd = EXT081.get("EXCUCD")
    cuno = EXT081.get("EXCUNO")
    fvdt = EXT081.get("EXFVDT")
    ascd = EXT081.get("EXASCD")
    fdat = EXT081.get("EXFDAT")
    String entryDate = EXT081.get("EXRGDT")
    String entryTime = EXT081.get("EXRGTM")
    String changeDate = EXT081.get("EXLMDT")
    String changeNumber = EXT081.get("EXCHNO")
    String changedBy = EXT081.get("EXCHID")
    mi.outData.put("PRRF", prrf)
    mi.outData.put("CUCD", cucd)
    mi.outData.put("CUNO", cuno)
    mi.outData.put("FVDT", fvdt)
    mi.outData.put("ASCD", ascd)
    mi.outData.put("FDAT", fdat)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
