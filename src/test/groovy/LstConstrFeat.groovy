/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT013MI.LstConstrFeat
 * Description : List records from the EXT013 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 * 20220211     RENARN       ZRGP added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class LstConstrFeat extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program

  public LstConstrFeat(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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
    if (mi.in.get("ZCFE") == null) {
      DBAction query = database.table("EXT013").index("00").selection("EXZCFE", "EXZDES", "EXZRGP", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT013 = query.getContainer()
      EXT013.set("EXCONO", currentCompany)
      if(!query.readAll(EXT013, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    } else {
      String constraintType = mi.in.get("ZCFE")
      ExpressionFactory expression = database.getExpressionFactory("EXT013")
      expression = expression.ge("EXZCFE", constraintType)
      DBAction query = database.table("EXT013").index("00").matching(expression).selection("EXZCFE", "EXZDES", "EXZRGP", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT013 = query.getContainer()
      EXT013.set("EXCONO", currentCompany)
      if(!query.readAll(EXT013, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }
  // Get EXT013
  Closure<?> outData = { DBContainer EXT013 ->
    String constraintType = EXT013.get("EXZCFE")
    String description = EXT013.get("EXZDES")
    String groupingCode = EXT013.get("EXZRGP")
    String entryDate = EXT013.get("EXRGDT")
    String entryTime = EXT013.get("EXRGTM")
    String changeDate = EXT013.get("EXLMDT")
    String changeNumber = EXT013.get("EXCHNO")
    String changedBy = EXT013.get("EXCHID")
    mi.outData.put("ZCFE", constraintType)
    mi.outData.put("ZDES", description)
    mi.outData.put("ZRGP", groupingCode)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
