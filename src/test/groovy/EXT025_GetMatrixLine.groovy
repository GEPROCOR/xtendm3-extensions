/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT025MI.GetMatrixLine
 * Description : Get record to the EXT025 table.
 * Date         Changed By   Description
 * 20240820     ALLNIC          CMD40 - Get a Preparation date assortment matrix line
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class GetMatrixLine extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private Integer currentCompany
  private Integer phase
  private Integer weekDayNumber

  public GetMatrixLine(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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
    if(mi.in.get("DLSP") == null || mi.in.get("DLSP") == ""){
      mi.error("Point de vente est obligatoire!")
      return
    }
    // Check warehouse
    if(mi.in.get("WHLO") == null || mi.in.get("WHLO") == ""){
      mi.error("Code dépôt est obligatoire!")
      return
    } else {
      DBAction Query = database.table("MITWHL").index("00").build()
      DBContainer MITWHL = Query.getContainer()
      MITWHL.set("MWCONO", currentCompany)
      MITWHL.set("MWWHLO",  mi.in.get("WHLO"))
      if (!Query.read(MITWHL)) {
        mi.error("Code dépôt " + mi.in.get("WHLO") + " n'existe pas!")
        return
      }
    }
    // Check customer order type
    if(mi.in.get("ORTP") == null || mi.in.get("ORTP") == ""){
      mi.error("Type commande de vente est obligatoire!")
      return
    } else {
      DBAction Query = database.table("OOTYPE").index("00").build()
      DBContainer OOTYPE = Query.getContainer()
      OOTYPE.set("OOCONO", currentCompany)
      OOTYPE.set("OOORTP",  mi.in.get("ORTP"))
      if (!Query.read(OOTYPE)) {
        mi.error("Code type commande de vente " + mi.in.get("ORTP") + " n'existe pas!")
        return
      }
    }
    // Check Phase
    if(mi.in.get("PHAS") == null || mi.in.get("PHAS") == ""){
      mi.error("Code phase est obligatoire!")
      return
    } else {
      phase = mi.in.get("PHAS") as Integer
      if(phase!=1 && phase!=2){
        mi.error("Code phase " + phase + " est invalide")
        return
      }
    }
    // Check Week day number
    if(mi.in.get("WDNU") == null || mi.in.get("WDNU") == ""){
      mi.error("Numero de jour est obligatoire!")
      return
    } else {
      weekDayNumber = mi.in.get("WDNU") as Integer
      if(weekDayNumber<1 || weekDayNumber>7){
        mi.error("Numero de jour " + weekDayNumber + " est invalide")
        return
      }
    }
    // Check record
    DBAction query = database.table("EXT025").index("00").selection("EXCONO", "EXDLSP", "EXWHLO", "EXORTP", "EXPHAS", "EXWDNU", "EXDELY", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT025 = query.getContainer()
    EXT025.set("EXCONO", currentCompany)
    EXT025.set("EXDLSP", mi.in.get("DLSP"))
    EXT025.set("EXWHLO", mi.in.get("WHLO"))
    EXT025.set("EXORTP", mi.in.get("ORTP"))
    EXT025.set("EXPHAS", mi.in.get("PHAS") as Integer)
    EXT025.set("EXWDNU", mi.in.get("WDNU") as Integer)
    if(!query.readAll(EXT025, 6, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Closure
  Closure<?> outData = { DBContainer EXT025 ->
    String company = EXT025.get("EXCONO")
    String salesPoint = EXT025.get("EXDLSP")
    String warehouse = EXT025.get("EXWHLO")
    String customerOrderType = EXT025.get("EXORTP")
    String phase = EXT025.get("EXPHAS")
    String weekDayNumber = EXT025.get("EXWDNU")
    String delay = EXT025.get("EXDELY")
    String entryDate = EXT025.get("EXRGDT")
    String entryTime = EXT025.get("EXRGTM")
    String changeDate = EXT025.get("EXLMDT")
    String changeNumber = EXT025.get("EXCHNO")
    String changedBy = EXT025.get("EXCHID")
    mi.outData.put("CONO", company)
    mi.outData.put("DLSP", salesPoint)
    mi.outData.put("WHLO", warehouse)
    mi.outData.put("ORTP", customerOrderType)
    mi.outData.put("PHAS", phase)
    mi.outData.put("WDNU", weekDayNumber)
    mi.outData.put("DELY", delay)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
