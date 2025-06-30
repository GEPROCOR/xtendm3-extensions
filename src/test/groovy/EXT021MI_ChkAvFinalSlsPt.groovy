/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT021MI.ChkAvFinalSlsPt
 * Description : Check available final sales point.
 * Date         Changed By   Description
 * 20220317     RENARN       CMDX06 - Gestion des points de vente
 * 20220425     RENARN       Added call of executeEXT800MIGetParam
 */
import java.time.LocalDate
import java.time.format.DateTimeFormatter

public class ChkAvFinalSlsPt extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final TextFilesAPI textFiles
  private final ExtensionAPI extension
  private Integer npvt
  private String bfrs
  private String date
  private Integer target_fdat
  private Integer target_tdat
  private Integer numberOfDays = 0
  private boolean IN60
  private Integer availableSlsPt

  public ChkAvFinalSlsPt(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility, TextFilesAPI textFiles, MICallerAPI miCaller, ExtensionAPI extension) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
    this.textFiles = textFiles
    this.miCaller = miCaller
    this.extension = extension
  }

  public void main() {
    // Get general settings
    executeEXT800MIGetParam("EXT021MI_ChkAvFinalSlsPt")

    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    npvt = 0
    if (mi.in.get("NPVT") == null) {
      mi.error("Numéro point de vente est obligatoire")
      return
    } else {
      npvt = mi.in.get("NPVT")
    }
    bfrs = ""
    if (mi.in.get("BFRS") == null) {
      mi.error("Base fournisseur est obligatoire")
      return
    } else {
      bfrs = mi.in.get("BFRS")
    }
    date = ""
    if (mi.in.get("DATE") == null) {
      mi.error("Date de référence est obligatoire")
      return
    } else {
      date = mi.in.get("DATE")
      if (!utility.call("DateUtil", "isDateValid", date, "yyyyMMdd")) {
        mi.error("Date de référence est incorrecte")
        return
      }
    }

    // Calculating the target date range to check the availability of the point of sale
    // = DATE received in parameter +/- number of days configured in EXT800
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    LocalDate DATE = LocalDate.parse(date, formatter)
    DATE = DATE.minusDays(numberOfDays)
    target_fdat = DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    DATE = LocalDate.parse(date, formatter)
    DATE = DATE.plusDays(numberOfDays)
    target_tdat = DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer

    logger.debug("target_fdat = " + target_fdat)
    logger.debug("target_tdat = " + target_tdat)

    availableSlsPt = 1
    DBAction query = database.table("EXT021").index("10").build()
    DBContainer EXT021 = query.getContainer()
    EXT021.set("EXCONO", currentCompany)
    EXT021.set("EXNPVT", npvt)
    EXT021.set("EXBFRS", bfrs)
    if (!query.readAll(EXT021, 3, outData_EXT021)) {
      //logger.debug("EXT021MI_MngFinalSalesPt non trouvé")
    }
    mi.outData.put("ZAVL", availableSlsPt as String)
    mi.write()
  }
  // Retrieve EXT021
  Closure<?> outData_EXT021 = { DBContainer EXT021 ->
    //logger.debug("EXT021MI_MngFinalSalesPt trouvé")
    Integer EXT021_fdat = EXT021.get("EXFDAT")
    Integer EXT021_tdat = EXT021.get("EXTDAT")
    // If the dates of EXT021 overlap the target dates for creation, the point of sale is not available
    if (EXT021_fdat >= target_fdat && EXT021_fdat <= target_tdat ||
      EXT021_tdat >= target_fdat && EXT021_tdat <= target_tdat ||
      target_fdat >= EXT021_fdat && target_fdat <= EXT021_tdat ||
      target_tdat >= EXT021_fdat && target_tdat <= EXT021_tdat) {
      availableSlsPt = 0
    }
  }
  // Execute EXT800MI.GetParam
  private executeEXT800MIGetParam(String EXNM){
    def parameters = ["EXNM": EXNM]
    Closure<?> handler = { Map<String, String> response ->
      if (response.P001 != null)
        numberOfDays = response.P001.trim() as Integer
      if (response.error != null) {
        IN60 = true
      }
      //logger.debug(EXNM + " executeEXT800MIGetParam numberOfDays = " + numberOfDays)
      //logger.debug(EXNM + " executeEXT800MIGetParam IN60 = " + IN60)
    }
    miCaller.call("EXT800MI", "GetParam", parameters, handler)
  }
}
