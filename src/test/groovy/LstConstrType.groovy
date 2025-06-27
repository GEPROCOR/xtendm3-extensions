/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT011MI.LstConstrType
 * Description : List records from the EXT011 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class LstConstrType extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program

  public LstConstrType(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi;
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
    if (mi.in.get("ZCTY") == null) {
      DBAction query = database.table("EXT011").index("00").selection("EXZCTY", "EXTX40", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT011 = query.getContainer()
      EXT011.set("EXCONO", currentCompany)
      if(!query.readAll(EXT011, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    } else {
      String constraintType = mi.in.get("ZCTY")
      ExpressionFactory expression = database.getExpressionFactory("EXT011")
      expression = expression.ge("EXZCTY", constraintType)
      DBAction query = database.table("EXT011").index("00").matching(expression).selection("EXZCTY", "EXTX40", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT011 = query.getContainer()
      EXT011.set("EXCONO", currentCompany)
      if(!query.readAll(EXT011, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }

  Closure<?> outData = { DBContainer EXT011 ->
    String constraintType = EXT011.get("EXZCTY")
    String description = EXT011.get("EXTX40")
    String entryDate = EXT011.get("EXRGDT")
    String entryTime = EXT011.get("EXRGTM")
    String changeDate = EXT011.get("EXLMDT")
    String changeNumber = EXT011.get("EXCHNO")
    String changedBy = EXT011.get("EXCHID")
    mi.outData.put("ZCTY", constraintType)
    mi.outData.put("TX40", description)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
