/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT030MI.LstIncPlSalPur
 * Description : The LstIncPlSalPur transaction list records to the EXT030 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 * 20211216     CDUV         Priority added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class LstIncPlSalPur extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program

  public LstIncPlSalPur(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
  }

  public void main() {
    int currentCompany = (Integer)program.getLDAZD().CONO
    if (mi.in.get("ZIPS") == null) {
      DBAction query = database.table("EXT030").index("00").selection("EXPRIO","EXZIPS", "EXZIPP", "EXZCOM", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT030 = query.getContainer()
      EXT030.set("EXCONO", currentCompany)
      if(!query.readAll(EXT030, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    } else {
      String incotermPlaceSale = mi.in.get("ZIPS")
      ExpressionFactory expression = database.getExpressionFactory("EXT030")
      expression = expression.ge("EXZIPS", incotermPlaceSale)
      if (mi.in.get("ZIPP") != null) {
        String incotermPlacePurchase = mi.in.get("ZIPP")
        expression = expression.and(expression.ge("EXZIPP", incotermPlacePurchase))
      }
      DBAction query = database.table("EXT030").index("00").matching(expression).selection("EXPRIO","EXZIPS", "EXZIPP", "EXZCOM", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT030 = query.getContainer()
      EXT030.set("EXCONO", currentCompany)
      if(!query.readAll(EXT030, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }
  // Retrieve EXT030
  Closure<?> outData = { DBContainer EXT030 ->
    int currentCompany = (Integer)program.getLDAZD().CONO
    String incotermPlaceSale = EXT030.get("EXZIPS")
    String incotermPlacePurchase = EXT030.get("EXZIPP")
    String compatible = EXT030.get("EXZCOM")
    String priority = EXT030.get("EXPRIO")
    String entryDate = EXT030.get("EXRGDT")
    String entryTime = EXT030.get("EXRGTM")
    String changeDate = EXT030.get("EXLMDT")
    String changeNumber = EXT030.get("EXCHNO")
    String changedBy = EXT030.get("EXCHID")
    mi.outData.put("ZIPS", incotermPlaceSale)
    mi.outData.put("ZIPP", incotermPlacePurchase)
    mi.outData.put("ZCOM", compatible)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.outData.put("PRIO", priority)
    mi.write()
  }
}
