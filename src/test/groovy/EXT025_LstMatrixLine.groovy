/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT025MI.LstMatrixLine
 * Description : Get record to the EXT025 table.
 * Date         Changed By   Description
 * 20240820     ALLNIC          CMD40 - List a Preparation date assortment matrix line
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class LstMatrixLine extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility;
  private Integer currentCompany
  private Integer phase
  private Integer weekDayNumber
  private String salesPoint
  private String warehouse
  private String customerOrderType
  private String sPhase
  private String sWeekDayNumber


  public LstMatrixLine(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Check sales point
    if(mi.in.get("DLSP") != "" && mi.in.get("DLSP") != null){
      salesPoint = mi.in.get("DLSP");
    }
    // Check warehouse
    if(mi.in.get("WHLO") != "" && mi.in.get("WHLO") != null){
      DBAction Query = database.table("MITWHL").index("00").build()
      DBContainer MITWHL = Query.getContainer()
      MITWHL.set("MWCONO", currentCompany)
      MITWHL.set("MWWHLO",  mi.in.get("WHLO"))
      if (!Query.read(MITWHL)) {
        mi.error("Code dépôt " + mi.in.get("WHLO") + " n'existe pas!")
        return
      }
      warehouse = mi.in.get("WHLO");
    }
    // Check customer order type
    if(mi.in.get("ORTP") != "" && mi.in.get("ORTP") != null){
      DBAction Query = database.table("OOTYPE").index("00").build()
      DBContainer OOTYPE = Query.getContainer()
      OOTYPE.set("OOCONO", currentCompany)
      OOTYPE.set("OOORTP",  mi.in.get("ORTP"))
      if (!Query.read(OOTYPE)) {
        mi.error("Code type commande de vente " + mi.in.get("ORTP") + " n'existe pas!")
        return
      }
      customerOrderType = mi.in.get("ORTP");
    }
    // Check Phase
    if(mi.in.get("PHAS") != "" && mi.in.get("PHAS") != null){
      phase = mi.in.get("PHAS") as Integer
      if(phase!=1 && phase!=2){
        mi.error("Code phase " + phase + " est invalide")
        return
      }
      sPhase = mi.in.get("PHAS") as Integer;
    }
    // Check Week day number
    if(mi.in.get("WDNU") != "" && mi.in.get("WDNU") != null){
      weekDayNumber = mi.in.get("WDNU") as Integer
      if(weekDayNumber<1 || weekDayNumber>7){
        mi.error("Numero de jour " + weekDayNumber + " est invalide")
        return
      }
      sWeekDayNumber = mi.in.get("WDNU") as Integer;
    }
    //Create Expression
    ExpressionFactory expression = database.getExpressionFactory("EXT025")
    Boolean expressionFind = false;
    expression = expression.eq("EXCONO", currentCompany.toString())
    if(salesPoint!="" && salesPoint!= null){
      expression = expression.and(expression.ge("EXWHLO", warehouse))
    }
    if(warehouse!="" && warehouse!= null) {
      expression = expression.and(expression.ge("EXWHLO", warehouse))
    }
    if(customerOrderType!="" && customerOrderType!= null) {
      expression = expression.and(expression.ge("EXORTP", customerOrderType))
    }
    if(sPhase!="" && sPhase!= null) {
      expression = expression.and(expression.ge("EXPHAS", sPhase))
    }
    if(sWeekDayNumber!="" && sWeekDayNumber!= null) {
      expression = expression.and(expression.ge("EXWDNU", sWeekDayNumber))
    }
    //Run Select
    DBAction query = database.table("EXT025").index("00").matching(expression).selection("EXCONO", "EXDLSP", "EXWHLO", "EXORTP", "EXPHAS", "EXWDNU", "EXDELY", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT025 = query.getContainer()
    EXT025.setInt("EXCONO",currentCompany)
    if(!query.readAll(EXT025, 1, outData)){
      mi.error("L'enregistrement n'existe pas")
      return;
    }
  }
  // Closure
  Closure<?> outData = { DBContainer EXT025 ->
    String cono = EXT025.get("EXCONO")
    String dlsp = EXT025.get("EXDLSP")
    String whlo = EXT025.get("EXWHLO")
    String ortp = EXT025.get("EXORTP")
    String phas = EXT025.get("EXPHAS")
    String wdnu = EXT025.get("EXWDNU")
    String dely = EXT025.get("EXDELY")
    String entryDate = EXT025.get("EXRGDT")
    String entryTime = EXT025.get("EXRGTM")
    String changeDate = EXT025.get("EXLMDT")
    String changeNumber = EXT025.get("EXCHNO")
    String changedBy = EXT025.get("EXCHID")
    mi.outData.put("CONO", cono)
    mi.outData.put("DLSP", dlsp)
    mi.outData.put("WHLO", whlo)
    mi.outData.put("ORTP", ortp)
    mi.outData.put("PHAS", phas)
    mi.outData.put("WDNU", wdnu)
    mi.outData.put("DELY", dely)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write();
  }
}

   
