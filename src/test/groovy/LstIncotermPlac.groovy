/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT031MI.LstIncotermPlac
 * Description : The LstIncotermPlac transaction list records to the EXT031 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class LstIncotermPlac extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program

  public LstIncotermPlac(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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
    if (mi.in.get("SQRY") == null) {
      if (mi.in.get("ZIPL") == null) {
        DBAction query = database.table("EXT031").index("00").selection("EXTEDL", "EXZPLA", "EXCONM", "EXZIPL", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
        DBContainer EXT031 = query.getContainer()
        EXT031.set("EXCONO", currentCompany)
        if(!query.readAll(EXT031, 1, outData)){
          mi.error("L'enregistrement n'existe pas")
          return
        }
      } else {
        String constraintType = mi.in.get("ZIPL")
        ExpressionFactory expression = database.getExpressionFactory("EXT031")
        expression = expression.ge("EXZIPL", constraintType)
        DBAction query = database.table("EXT031").index("10").matching(expression).selection("EXTEDL", "EXZPLA", "EXCONM", "EXZIPL", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
        DBContainer EXT031 = query.getContainer()
        EXT031.set("EXCONO", currentCompany)
        if(!query.readAll(EXT031, 1, outData)){
          mi.error("L'enregistrement n'existe pas")
          return
        }
      }
    } else {
      String sqry = mi.in.get("SQRY")
      ExpressionFactory expression = database.getExpressionFactory("EXT031")
      sqry = "%"+sqry+"%"
      expression = expression.like("EXTEDL", sqry)
      expression = expression.or(expression.like("EXZPLA", sqry))
      expression = expression.or(expression.like("EXCONM", sqry))
      expression = expression.or(expression.like("EXZIPL", sqry))
      DBAction query = database.table("EXT031").index("00").matching(expression).selection("EXTEDL", "EXZPLA", "EXCONM", "EXZIPL", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT031 = query.getContainer()
      EXT031.set("EXCONO", currentCompany)
      if(!query.readAll(EXT031, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }

  Closure<?> outData = { DBContainer EXT031 ->
    int currentCompany = (Integer)program.getLDAZD().CONO
    String sqry = EXT031.get("EXTEDL")
    String place = EXT031.get("EXZPLA")
    String placeDescription = EXT031.get("EXCONM")
    String sqryPlace = EXT031.get("EXZIPL")
    String entryDate = EXT031.get("EXRGDT")
    String entryTime = EXT031.get("EXRGTM")
    String changeDate = EXT031.get("EXLMDT")
    String changeNumber = EXT031.get("EXCHNO")
    String changedBy = EXT031.get("EXCHID")
    mi.outData.put("TEDL", sqry)
    mi.outData.put("ZPLA", place)
    mi.outData.put("CONM", placeDescription)
    mi.outData.put("ZIPL", sqryPlace)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
