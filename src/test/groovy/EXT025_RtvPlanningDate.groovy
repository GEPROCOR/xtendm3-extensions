/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT025MI.RtvPlanningDate
 * Description : Retrieve the planning date to the EXT025 table.
 * Date         Changed By   Description
 * 20240822     ALLNIC          CMD40 - Retrieve the planning date in Preparation date assortment matrix
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class RtvPlanningDate extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private Integer currentCompany
  private String currentDivision
  private Integer phase
  private String weekDayNumber
  private String delay
  private Integer planningDate
  private String preparationDate
  private String adjustedDate

  public RtvPlanningDate(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.miCaller = miCaller
    this.utility = utility
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    currentDivision = (String)program.getLDAZD().DIVI
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
    // Check the preparation date
    if(mi.in.get("CUDT") == null || mi.in.get("CUDT") == ""){
      mi.error("Date de préparation est obligatoire");
      return;
    }else{
      preparationDate = mi.in.get("CUDT") as Integer;
      if (!utility.call("DateUtil", "isDateValid", preparationDate, "yyyyMMdd")) {
        mi.error("Date de préparation " + preparationDate +" invalide")
        return
      }
    }
    //Retrieve the week day number of the date
    DBAction queryCSYCAL = database.table("CSYCAL").index("00").selection("CDCONO", "CDDIVI", "CDYMD8", "CDYWD5").build()
    DBContainer CSYCAL = queryCSYCAL.getContainer()
    CSYCAL.set("CDCONO", currentCompany)
    CSYCAL.set("CDDIVI",  currentDivision)
    CSYCAL.set("CDYMD8", preparationDate as Integer)
    if (!queryCSYCAL.readAll(CSYCAL, 3, outDataCSYCAL)) {
      mi.error("Date " + preparationDate + " n'existe pas dans le calendrier!")
      return
    }
    // Retrieve the delay
    DBAction queryEXT025 = database.table("EXT025").index("00").selection("EXCONO", "EXDLSP", "EXWHLO", "EXORTP", "EXPHAS", "EXWDNU", "EXDELY").build()
    DBContainer EXT025 = queryEXT025.getContainer()
    EXT025.set("EXCONO", currentCompany)
    EXT025.set("EXDLSP", mi.in.get("DLSP"))
    EXT025.set("EXWHLO", mi.in.get("WHLO"))
    EXT025.set("EXORTP", mi.in.get("ORTP"))
    EXT025.set("EXPHAS", mi.in.get("PHAS") as Integer)
    EXT025.set("EXWDNU", weekDayNumber as Integer)
    if(!queryEXT025.readAll(EXT025, 6, outDataEXT025)){
      delay = "0"
    }
    executeCRS900MIAddWorkingDays(preparationDate, delay)
    mi.outData.put("PLDT", adjustedDate.trim())
    mi.write()
  }
  //Closure
  Closure<?> outDataCSYCAL = { DBContainer CSYCAL ->
    String cdywd5 = CSYCAL.get("CDYWD5")
    if(cdywd5 != null && cdywd5 != "") {
      weekDayNumber = cdywd5.substring(4,5)
    }
  }
  Closure<?> outDataEXT025 = { DBContainer EXT025 ->
    delay = EXT025.get("EXDELY") as Integer
    delay = "-" + delay
  }



  // Execute CRS900MI.AddWorkingDays
  private executeCRS900MIAddWorkingDays(String FRDT, String DAYS) {
    LinkedHashMap<String, String> parameters = ["FRDT": FRDT, "DAYS": DAYS]
    Closure<?> handler = { Map<String, String> response ->
      adjustedDate = response.TODT.trim()
      if (response.error != null) {
        return mi.error("Failed CRS900MI.AddWorkingDays: " + response.errorMessage)
      }
    }
    miCaller.call("CRS900MI", "AddWorkingDays", parameters, handler)
  }
}
