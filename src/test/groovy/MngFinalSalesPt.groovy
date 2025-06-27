/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT021MI.MngFinalSalesPt
 * Description : Manage final sales point
 * Date         Changed By   Description
 * 20210824     RENARN       CMDX06 - Gestion des points de vente
 * 20211027     RENARN       Init variable with 0
 */

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.GregorianCalendar
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
public class MngFinalSalesPt extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final TextFilesAPI textFiles
  private final ExtensionAPI extension
  private String type
  private String fpvt
  private String tpvt
  private Integer FPVT
  private Integer TPVT
  private String date
  private String lfrs
  private String fdat
  private String tdat
  private Integer target_fdat
  private Integer target_tdat
  private Integer npvt
  private Integer retrievedNPVT = 0
  private Integer numberOfDays = 0
  private boolean IN60
  private boolean npvtNOK = true

  public MngFinalSalesPt(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility, TextFilesAPI textFiles, MICallerAPI miCaller, ExtensionAPI extension) {
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
    executeEXT800MIGetParam("EXT021MI_MngFinalSalesPt")

    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    type = ""
    if (mi.in.get("TYPE") == null) {
      mi.error("Type point de vente est obligatoire")
      return
    } else {
      type = mi.in.get("TYPE")
    }
    fpvt = ""
    if (mi.in.get("FPVT") == null) {
      mi.error("Point de vente début obligatoire")
      return
    } else {
      fpvt = mi.in.get("FPVT")
    }
    tpvt = ""
    if (mi.in.get("TPVT") == null) {
      mi.error("Point de vente fin obligatoire")
      return
    } else {
      tpvt = mi.in.get("TPVT")
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
    if (mi.in.get("LFRS") == null) {
      mi.error("Liste fournisseurs obligatoire")
      return
    } else {
      lfrs = mi.in.get("LFRS")
    }

    // Calculating the target date range for creating EXT021
    // = DATE received in parameter +/- number of days configured in EXT800
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    LocalDate DATE = LocalDate.parse(date, formatter)
    DATE = DATE.minusDays(numberOfDays)
    target_fdat = DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    DATE = LocalDate.parse(date, formatter)
    DATE = DATE.plusDays(numberOfDays)
    target_tdat = DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer

    //logger.debug("Step 1")
    FPVT = fpvt as Integer
    TPVT = tpvt as Integer
    int i
    for (i = (FPVT); i <= TPVT && npvtNOK; i++) {
      //logger.debug("Step 2")
      npvt = i
      npvtNOK = false
      DBAction query = database.table("EXT021").index("00").build()
      DBContainer EXT021 = query.getContainer()
      EXT021.set("EXCONO", currentCompany)
      EXT021.set("EXTYPE", type)
      EXT021.set("EXNPVT", npvt)
      if (!query.readAll(EXT021, 3, outData_EXT021)) {
        //logger.debug("EXT021MI_MngFinalSalesPt npvt non trouvé =" + npvt)
      }
      //logger.debug("EXT021MI_MngFinalSalesPt npvtNOK =" + npvtNOK)
      if(!npvtNOK){
        //logger.debug("Step 3")
        //logger.debug("EXT021MI_MngFinalSalesPt Création EXT021 avec npvt = " + npvt)
        Integer elementLength = lfrs.indexOf("|")
        Integer totalLength = lfrs.length()
        logger.debug("elementLength = " + elementLength)
        logger.debug("totalLength = " + totalLength)
        int beginIndex = 0
        int endIndex = lfrs.indexOf("|")
        //int i
        logger.debug("(totalLength) / (elementLength + 1) = " + (totalLength) / (elementLength + 1))
        for (i = 0; i < (totalLength) / (elementLength + 1); i++) {
          logger.debug("beginIndex/endIndex = " + beginIndex + "/" + endIndex)
          logger.debug("Fournisseur = " + lfrs.substring(beginIndex, endIndex))
          // Check supplier
          DBAction query_CIDMAS = database.table("CIDMAS").index("00").build()
          DBContainer CIDMAS = query_CIDMAS.getContainer()
          CIDMAS.set("IDCONO", currentCompany)
          CIDMAS.set("IDSUNO",  lfrs.substring(beginIndex, endIndex))
          if (query_CIDMAS.read(CIDMAS)) {
            LocalDateTime timeOfCreation = LocalDateTime.now()
            DBAction query_2 = database.table("EXT021").index("00").build()
            DBContainer EXT021_2 = query_2.getContainer()
            EXT021_2.set("EXCONO", currentCompany)
            EXT021_2.set("EXTYPE", type)
            EXT021_2.set("EXNPVT", npvt)
            EXT021_2.set("EXBFRS", lfrs.substring(beginIndex, endIndex))
            EXT021_2.setInt("EXFDAT", target_fdat)
            EXT021_2.setInt("EXTDAT", target_tdat)
            if (!query_2.read(EXT021_2)) {
              EXT021_2.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
              EXT021_2.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
              EXT021_2.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
              EXT021_2.setInt("EXCHNO", 1)
              EXT021_2.set("EXCHID", program.getUser())
              query_2.insert(EXT021_2)
            }
          }
          beginIndex = endIndex + 1
          endIndex = beginIndex + lfrs.indexOf("|")
        }
        //logger.debug("EXT021MI_MngFinalSalesPt FIN")
        retrievedNPVT = npvt
      }
    }

    mi.outData.put("NPVT", retrievedNPVT as String)
    mi.write()
  }
  // Retrieve EXT021
  Closure<?> outData_EXT021 = { DBContainer EXT021 ->
    //logger.debug("EXT021MI_MngFinalSalesPt npvt trouvé =" + npvt)
    String bfrs = EXT021.get("EXBFRS")
    bfrs = bfrs.trim()
    Integer EXT021_fdat = EXT021.get("EXFDAT")
    Integer EXT021_tdat = EXT021.get("EXTDAT")
    // If the supplier of EXT021 is included in the list of suppliers received as a parameter (LFRS) and
    // the dates of EXT021 overlap the target dates for creation, the point of sale is excluded
    //logger.debug("EXT021MI_MngFinalSalesPt Création EXT021_fdat/EXT021_tdat = " + EXT021_fdat + "/" + EXT021_tdat)
    //logger.debug("EXT021MI_MngFinalSalesPt Création target_fdat/target_tdat = " + target_fdat + "/" + target_tdat)
    //logger.debug("EXT021MI_MngFinalSalesPt Création EXT021_fdat >= target_fdat && EXT021_fdat <= target_tdat = " + (EXT021_fdat >= target_fdat && EXT021_fdat <= target_tdat))
    //logger.debug("EXT021MI_MngFinalSalesPt Création EXT021_tdat >= target_fdat && EXT021_tdat <= target_tdat = " + (EXT021_tdat >= target_fdat && EXT021_tdat <= target_tdat))
    //logger.debug("EXT021MI_MngFinalSalesPt Création target_fdat >= EXT021_fdat && target_fdat <= EXT021_tdat = " + (target_fdat >= EXT021_fdat && target_fdat <= EXT021_tdat))
    //logger.debug("EXT021MI_MngFinalSalesPt Création target_tdat >= EXT021_fdat && target_tdat <= EXT021_tdat = " + (target_tdat >= EXT021_fdat && target_tdat <= EXT021_tdat))
    if (lfrs.contains(bfrs) &&
      (EXT021_fdat >= target_fdat && EXT021_fdat <= target_tdat ||
        EXT021_tdat >= target_fdat && EXT021_tdat <= target_tdat ||
        target_fdat >= EXT021_fdat && target_fdat <= EXT021_tdat ||
        target_tdat >= EXT021_fdat && target_tdat <= EXT021_tdat))
      npvtNOK = true
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
