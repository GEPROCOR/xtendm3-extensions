/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT010MI.LstByItem
 * Description : List records from the EXT010 table.
 * Date         Changed By   Description
 * 20210219     RENARN       QUAX01 - Constraints matrix
 * 20211102     RENARN       Parameters HAZI and ZPDA have been added
 * 20220228     RENARN       ZTPS has been added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class LstByItem extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany
  private String constraintID
  private String itemNumber
  private String constrainingFeature
  private String constraintLevel
  private String supplierNumber
  private String customerNumber
  private String manufacturer
  private String CNUF
  private String country
  private String HScode
  private String IFLSfield
  private String mainIngredient
  private String contains
  //private String GLNcode
  private String PNM
  private String brand
  private String controlCode
  private String standardLifetime
  private String percentageOfMandatoryLifetime
  private String origin
  private String orderType
  private String itemType
  private String dangerIndicator
  private String potentiallyDangerous
  private String dangerClass
  private String documentID1
  private String documentID2
  private String documentID3
  private String documentID4
  private String documentID5
  private String documentID6
  private String documentID7
  private String documentID8
  private String documentID9
  private String documentID10
  private String documentID11
  private String documentID12
  private String documentID13
  private String documentID14
  private String documentID15
  private String documentID16
  private String documentID17
  private String documentID18
  private String documentID19
  private String documentID20
  private String txid
  private Integer LastConstraintNumber
  private String lastConstraintNumber
  private Integer maxRecord
  private String ztps
  private String warehouse

  public LstByItem(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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
    // Check item
    if(mi.in.get("ITNO") != null && mi.in.get("ITNO") != "*"){
      DBAction Query = database.table("MITMAS").index("00").build()
      DBContainer MITMAS = Query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO",  mi.in.get("ITNO"))
      if (!Query.read(MITMAS)) {
        mi.error("Code article " + mi.in.get("ITNO") + " n'existe pas")
        return
      }
    }
    // Check constraining feature
    if(mi.in.get("ZCFE") != null && mi.in.get("ZCFE") != "*"){
      DBAction query = database.table("EXT013").index("00").build()
      DBContainer EXT013 = query.getContainer()
      EXT013.set("EXCONO", currentCompany)
      EXT013.set("EXZCFE",  mi.in.get("ZCFE"))
      if (!query.read(EXT013)) {
        mi.error("Caractéristique contraignante " + mi.in.get("ZCFE") + " n'existe pas")
        return
      }
    }
    // Check constraint level
    if(mi.in.get("ZCLV") != null && mi.in.get("ZCLV") != "*"){
      DBAction query = database.table("EXT012").index("00").build()
      DBContainer EXT012 = query.getContainer()
      EXT012.set("EXCONO", currentCompany)
      EXT012.set("EXZCLV",  mi.in.get("ZCLV"))
      if (!query.read(EXT012)) {
        mi.error("Niveau de contrainte " + mi.in.get("ZCLV") + " n'existe pas")
        return
      }
    }
    // Check supplier
    if(mi.in.get("SUNO") != null && !(mi.in.get("SUNO") as String).contains("*")){
      ExpressionFactory expression = database.getExpressionFactory("CIDVEN")
      expression = expression.eq("IISUCL", "100")
      DBAction query = database.table("CIDVEN").index("00").matching(expression).build()
      DBContainer CIDVEN = query.getContainer()
      CIDVEN.set("IICONO", currentCompany)
      CIDVEN.set("IISUNO",  mi.in.get("SUNO"))
      if (!query.read(CIDVEN)) {
        mi.error("Fournisseur " + mi.in.get("SUNO") + " n'existe pas")
        return
      }
    }
    // Check customer
    if(mi.in.get("CUNO") != null && mi.in.get("CUNO") != "*"){
      DBAction query = database.table("OCUSMA").index("00").build()
      DBContainer OCUSMA = query.getContainer()
      OCUSMA.set("OKCONO", currentCompany)
      OCUSMA.set("OKCUNO",  mi.in.get("CUNO"))
      if (!query.read(OCUSMA)) {
        mi.error("Client " + mi.in.get("CUNO") + " n'existe pas")
        return
      }
    }
    // Check manufacturer
    if(mi.in.get("SUN1") != null && !(mi.in.get("SUN1") as String).contains("*")){
      ExpressionFactory expression = database.getExpressionFactory("CIDVEN")
      expression = expression.eq("IISUCL", "500")
      DBAction query = database.table("CIDVEN").index("00").matching(expression).build()
      DBContainer CIDVEN = query.getContainer()
      CIDVEN.set("IICONO", currentCompany)
      CIDVEN.set("IISUNO",  mi.in.get("SUN1"))
      if (!query.read(CIDVEN)) {
        mi.error("Fabricant " + mi.in.get("SUN1") + " n'existe pas")
        return
      }
    }
    // Check CNUF
    if(mi.in.get("SUN3") != null && !(mi.in.get("SUN3") as String).contains("*")){
      ExpressionFactory expression = database.getExpressionFactory("CIDVEN")
      expression = expression.eq("IISUCL", "200")
      DBAction query = database.table("CIDVEN").index("00").matching(expression).build()
      DBContainer CIDVEN = query.getContainer()
      CIDVEN.set("IICONO", currentCompany)
      CIDVEN.set("IISUNO",  mi.in.get("SUN3"))
      if (!query.read(CIDVEN)) {
        mi.error("CNUF " + mi.in.get("SUN3") + " n'existe pas")
        return
      }
    }
    // Check country
    if(mi.in.get("CSCD") != null && mi.in.get("CSCD") != "*"){
      DBAction countryQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = countryQuery.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CSCD")
      CSYTAB.set("CTSTKY", mi.in.get("CSCD"))
      if (!countryQuery.read(CSYTAB)) {
        mi.error("Pays " + mi.in.get("CSCD") + " n'existe pas")
        return
      }
    }
    // Check brand
    if(mi.in.get("CFI1") != null && mi.in.get("CFI1") != "*"){
      DBAction countryQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = countryQuery.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CFI1")
      CSYTAB.set("CTSTKY", mi.in.get("CFI1"))
      if (!countryQuery.read(CSYTAB)) {
        mi.error("Marque " + mi.in.get("CFI1") + " n'existe pas")
        return
      }
    }
    // Check control code
    if(mi.in.get("CFI4") != null && mi.in.get("CFI4") != "*"){
      DBAction countryQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = countryQuery.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CFI4")
      CSYTAB.set("CTSTKY", mi.in.get("CFI4"))
      if (!countryQuery.read(CSYTAB)) {
        mi.error("Code régie " + mi.in.get("CFI4") + " n'existe pas")
        return
      }
    }
    // Check PNM
    if(mi.in.get("CFI5") != null && mi.in.get("CFI5") != "*"){
      DBAction countryQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = countryQuery.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CFI5")
      CSYTAB.set("CTSTKY", mi.in.get("CFI5"))
      if (!countryQuery.read(CSYTAB)) {
        mi.error("PNM " + mi.in.get("CFI5") + " n'existe pas")
        return
      }
    }
    // Check percentage of mandatory lifetime
    int zplt = 0
    if(mi.in.get("ZPLT") != null){
      zplt = mi.in.get("ZPLT")
      if(zplt < 0 || zplt > 100){
        mi.error("Pourcentage de durée de vie obligatoire doit être entre 0 and 100%")
        return
      }
    }
    // Check origin
    if(mi.in.get("CSC1") != null && mi.in.get("CSC1") != "*"){
      DBAction countryQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = countryQuery.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CSCD")
      CSYTAB.set("CTSTKY", mi.in.get("CSC1"))
      if (!countryQuery.read(CSYTAB)) {
        mi.error("Origine " + mi.in.get("CSC1") + " n'existe pas")
        return
      }
    }
    // Check order type
    if(mi.in.get("ORTP") != null && mi.in.get("ORTP") != "*"){
      logger.debug("inORTP = " + mi.in.get("ORTP"))
      DBAction countryQuery = database.table("OOTYPE").index("00").build()
      DBContainer OOTYPE = countryQuery.getContainer()
      OOTYPE.set("OOCONO",currentCompany)
      OOTYPE.set("OOORTP", mi.in.get("ORTP"))
      if (!countryQuery.read(OOTYPE)) {
        mi.error("Type commande " + mi.in.get("ORTP") + " n'existe pas")
        return
      }
    }
    // Check item type
    if(mi.in.get("ITTY") != null && mi.in.get("ITTY") != "*"){
      logger.debug("inITTY = " + mi.in.get("ITTY"))
      DBAction countryQuery = database.table("MITTTY").index("00").build()
      DBContainer MITTTY = countryQuery.getContainer()
      MITTTY.set("TYCONO",currentCompany)
      MITTTY.set("TYITTY", mi.in.get("ITTY"))
      if (!countryQuery.read(MITTTY)) {
        mi.error("Type article " + mi.in.get("ITTY") + " n'existe pas")
        return
      }
    }
    // Check danger indicator
    if(mi.in.get("HAZI") != null && mi.in.get("HAZI") != 0 && mi.in.get("HAZI") != 1){
      int hazi = mi.in.get("HAZI")
      mi.error("Indicateur danger " + hazi + " est invalide")
      return
    }
    // Check potentially dangerous
    if(mi.in.get("ZPDA") != null && mi.in.get("ZPDA") != 0 && mi.in.get("ZPDA") != 1){
      int zpda = mi.in.get("ZPDA")
      mi.error("Potentiellement dangereux " + zpda + " est invalide")
      return
    }
    // Check warehouse
    if(mi.in.get("WHLO") != null && mi.in.get("WHLO") != "*"){
      logger.debug("inWHLO = " + mi.in.get("WHLO"))
      DBAction countryQuery = database.table("MITWHL").index("00").build()
      DBContainer MITWHL = countryQuery.getContainer()
      MITWHL.set("MWCONO",currentCompany)
      MITWHL.set("MWWHLO", mi.in.get("WHLO"))
      if (!countryQuery.read(MITWHL)) {
        mi.error("Dépôt " + mi.in.get("WHLO") + " n'existe pas")
        return
      }
    }
    // Check document ID 1
    if(mi.in.get("DO01") != null && mi.in.get("DO01") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO01"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 1 " + mi.in.get("DO01") + " n'existe pas")
        return
      }
    }
    // Check document ID 2
    if(mi.in.get("DO02") != null && mi.in.get("DO02") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO02"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 2 " + mi.in.get("DO02") + " n'existe pas")
        return
      }
    }
    // Check document ID 3
    if(mi.in.get("DO03") != null && mi.in.get("DO03") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO03"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 3 " + mi.in.get("DO03") + " n'existe pas")
        return
      }
    }
    // Check document ID 4
    if(mi.in.get("DO04") != null && mi.in.get("DO04") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO04"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 4 " + mi.in.get("DO04") + " n'existe pas")
        return
      }
    }
    // Check document ID 5
    if(mi.in.get("DO05") != null && mi.in.get("DO05") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO05"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 5 " + mi.in.get("DO05") + " n'existe pas")
        return
      }
    }
    // Check document ID 6
    if(mi.in.get("DO06") != null && mi.in.get("DO06") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO06"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 6 " + mi.in.get("DO06") + " n'existe pas")
        return
      }
    }
    // Check document ID 7
    if(mi.in.get("DO07") != null && mi.in.get("DO07") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO07"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 7 " + mi.in.get("DO07") + " n'existe pas")
        return
      }
    }
    // Check document ID 8
    if(mi.in.get("DO08") != null && mi.in.get("DO08") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO08"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 8 " + mi.in.get("DO08") + " n'existe pas")
        return
      }
    }
    // Check document ID 9
    if(mi.in.get("DO09") != null && mi.in.get("DO09") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO09"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 9 " + mi.in.get("DO09") + " n'existe pas")
        return
      }
    }
    // Check document ID 10
    if(mi.in.get("DO10") != null && mi.in.get("DO10") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO10"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 10 " + mi.in.get("DO10") + " n'existe pas")
        return
      }
    }
    // Check document ID 11
    if(mi.in.get("DO11") != null && mi.in.get("DO11") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO11"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 11 " + mi.in.get("DO11") + " n'existe pas")
        return
      }
    }
    // Check document ID 12
    if(mi.in.get("DO12") != null && mi.in.get("DO12") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO12"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 12 " + mi.in.get("DO12") + " n'existe pas")
        return
      }
    }
    // Check document ID 13
    if(mi.in.get("DO13") != null && mi.in.get("DO13") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO13"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 13 " + mi.in.get("DO13") + " n'existe pas")
        return
      }
    }
    // Check document ID 14
    if(mi.in.get("DO14") != null && mi.in.get("DO14") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO14"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 14 " + mi.in.get("DO14") + " n'existe pas")
        return
      }
    }
    // Check document ID 15
    if(mi.in.get("DO15") != null && mi.in.get("DO15") != "*"){
      DBAction Query = database.table("MPDDOC").index("00").build()
      DBContainer MPDDOC = Query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID", mi.in.get("DO15"))
      if (!Query.read(MPDDOC)) {
        mi.error("Identité document 15 " + mi.in.get("DO15") + " n'existe pas")
        return
      }
    }
    if (mi.in.get("ZCID") == null && mi.in.get("ITNO") == null && mi.in.get("ZCFE") == null && mi.in.get("ZCLV") == null && mi.in.get("SUNO") == null && mi.in.get("CUNO") == null && mi.in.get("SUN1") == null && mi.in.get("SUN3") == null && mi.in.get("CSCD") == null && mi.in.get("CSNO") == null && mi.in.get("HIE0") == null && mi.in.get("SPE1") == null && mi.in.get("SPE2") == null /**&& mi.in.get("SUCM") == null**/ && mi.in.get("CFI1") == null && mi.in.get("CFI4") == null && mi.in.get("CFI5") == null && mi.in.get("ZSLT") == null && mi.in.get("ZPLT") == null && mi.in.get("CSC1") == null && mi.in.get("ORTP") == null && mi.in.get("ITTY") == null && mi.in.get("ZONU") == null && mi.in.get("HAZI") == null && mi.in.get("ZPDA") == null && mi.in.get("WHLO") == null && mi.in.get("DO01") == null && mi.in.get("DO02") == null && mi.in.get("DO03") == null && mi.in.get("DO04") == null && mi.in.get("DO05") == null && mi.in.get("DO06") == null && mi.in.get("DO07") == null && mi.in.get("DO08") == null && mi.in.get("DO09") == null && mi.in.get("DO10") == null && mi.in.get("DO11") == null && mi.in.get("DO12") == null && mi.in.get("DO13") == null && mi.in.get("DO14") == null && mi.in.get("DO15") == null && mi.in.get("DO16") == null && mi.in.get("DO17") == null && mi.in.get("DO18") == null && mi.in.get("DO19") == null && mi.in.get("DO20") == null) {
      // Get max records
      if (mi.in.get("MXRE") != null) {
        maxRecord = mi.in.get("MXRE") as Integer
      } else {
        maxRecord = 20
      }
      // Get last constraint number
      LastConstraintNumber = 0
      DBAction query_CSYNBR = database.table("CSYNBR").index("00").selection("CNNBNR").build()
      DBContainer CSYNBR = query_CSYNBR.getContainer()
      CSYNBR.set("CNCONO", currentCompany)
      CSYNBR.set("CNNBTY",  "ZA")
      CSYNBR.set("CNNBID",  "A")
      query_CSYNBR.readAll(CSYNBR, 4, outData_CSYNBR)

      ExpressionFactory expression = database.getExpressionFactory("EXT010")
      expression = expression.gt("EXZCID", lastConstraintNumber)
      DBAction query = database.table("EXT010").index("20").matching(expression).selection("EXZCID", "EXITNO", "EXZCFE", "EXZCLV", "EXSUNO", "EXCUNO", "EXSUN1", "EXSUN3", "EXCSCD", "EXCSNO", "EXHIE0", "EXSPE1", "EXSPE2",/**"EXSUCM",**/ "EXCFI1", "EXCFI4", "EXCFI5", "EXZSLT", "EXZPLT", "EXCSC1", "EXORTP", "EXITTY", "EXZONU", "EXHAZI", "EXZPDA", "EXZTPS", "EXWHLO", "EXDO01", "EXDO02", "EXDO03", "EXDO04", "EXDO05", "EXDO06", "EXDO07", "EXDO08", "EXDO09", "EXDO10", "EXDO11", "EXDO12", "EXDO13", "EXDO14", "EXDO15", "EXTXID", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT010 = query.getContainer()
      EXT010.set("EXCONO", currentCompany)
      if(!query.readAll(EXT010, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    } else {
      constraintID = mi.in.get("ZCID") as Integer
      itemNumber = mi.in.get("ITNO")
      constrainingFeature = mi.in.get("ZCFE")
      constraintLevel = mi.in.get("ZCLV")
      supplierNumber = mi.in.get("SUNO")
      customerNumber = mi.in.get("CUNO")
      manufacturer = mi.in.get("SUN1")
      CNUF = mi.in.get("SUN3")
      country = mi.in.get("CSCD")
      HScode = mi.in.get("CSNO")
      IFLSfield = mi.in.get("HIE0")
      mainIngredient = mi.in.get("SPE1")
      contains = mi.in.get("SPE2")
      //GLNcode = mi.in.get("SUCM")
      brand = mi.in.get("CFI1")
      controlCode = mi.in.get("CFI4")
      PNM = mi.in.get("CFI5")
      standardLifetime = mi.in.get("ZSLT")
      percentageOfMandatoryLifetime = mi.in.get("ZPLT") as Integer
      dangerClass = mi.in.get("ZONU")
      origin = mi.in.get("CSC1")
      orderType = mi.in.get("ORTP")
      itemType = mi.in.get("ITTY")
      dangerIndicator = mi.in.get("HAZI") as Integer
      potentiallyDangerous = mi.in.get("ZPDA") as Integer
      warehouse = mi.in.get("WHLO")
      documentID1 = mi.in.get("DO01")
      documentID2 = mi.in.get("DO02")
      documentID3 = mi.in.get("DO03")
      documentID4 = mi.in.get("DO04")
      documentID5 = mi.in.get("DO05")
      documentID6 = mi.in.get("DO06")
      documentID7 = mi.in.get("DO07")
      documentID8 = mi.in.get("DO08")
      documentID9 = mi.in.get("DO09")
      documentID10 = mi.in.get("DO10")
      documentID11 = mi.in.get("DO11")
      documentID12 = mi.in.get("DO12")
      documentID13 = mi.in.get("DO13")
      documentID14 = mi.in.get("DO14")
      documentID15 = mi.in.get("DO15")
      documentID16 = mi.in.get("DO16")
      documentID17 = mi.in.get("DO17")
      documentID18 = mi.in.get("DO18")
      documentID19 = mi.in.get("DO19")
      documentID20 = mi.in.get("DO20")
      buildExpression()
    }
  }
  // Get CSYNBR
  Closure<?> outData_CSYNBR = { DBContainer CSYNBR ->
    LastConstraintNumber = CSYNBR.get("CNNBNR")
    LastConstraintNumber = LastConstraintNumber - maxRecord
    lastConstraintNumber = LastConstraintNumber
  }
  // Get EXT010
  Closure<?> outData = { DBContainer EXT010 ->
    constraintID = EXT010.get("EXZCID")
    itemNumber = EXT010.get("EXITNO")
    // Get item description
    String itemNumber_description = ""
    if(EXT010.get("EXITNO") != ""){
      DBAction query = database.table("MITMAS").index("00").selection("MMITDS").build()
      DBContainer MITMAS = query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO",  EXT010.get("EXITNO"))
      if (query.read(MITMAS)) {
        itemNumber_description = MITMAS.get("MMITDS")
      }
    }
    constrainingFeature = EXT010.get("EXZCFE")
    // Get constraining feature description
    String constrainingFeature_description = ""
    if(EXT010.get("EXZCFE") != ""){
      DBAction query = database.table("EXT013").index("00").selection("EXZDES").build()
      DBContainer EXT013 = query.getContainer()
      EXT013.set("EXCONO", currentCompany)
      EXT013.set("EXZCFE",  EXT010.get("EXZCFE"))
      if (query.read(EXT013)) {
        constrainingFeature_description = EXT013.get("EXZDES")
      }
    }
    constraintLevel = EXT010.get("EXZCLV")
    // Get constraint level description
    String constraintLevel_description = ""
    if(EXT010.get("EXZCLV") != ""){
      DBAction query = database.table("EXT012").index("00").selection("EXZDES").build()
      DBContainer EXT012 = query.getContainer()
      EXT012.set("EXCONO", currentCompany)
      EXT012.set("EXZCLV",  EXT010.get("EXZCLV"))
      if (query.read(EXT012)) {
        constraintLevel_description = EXT012.get("EXZDES")
      }
    }
    supplierNumber = EXT010.get("EXSUNO")
    // Get supplier description
    String supplierNumber_description = ""
    if(EXT010.get("EXSUNO") != ""){
      DBAction query = database.table("CIDMAS").index("00").selection("IDSUNM").build()
      DBContainer CIDMAS = query.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO",  EXT010.get("EXSUNO"))
      if (query.read(CIDMAS)) {
        supplierNumber_description = CIDMAS.get("IDSUNM")
      }
    }
    customerNumber = EXT010.get("EXCUNO")
    // Get customer description
    String customerNumber_description = ""
    if(EXT010.get("EXCUNO") != ""){
      DBAction query = database.table("OCUSMA").index("00").selection("OKCUNM").build()
      DBContainer OCUSMA = query.getContainer()
      OCUSMA.set("OKCONO", currentCompany)
      OCUSMA.set("OKCUNO",  EXT010.get("EXCUNO"))
      if (query.read(OCUSMA)) {
        customerNumber_description = OCUSMA.get("OKCUNM")
      }
    }
    manufacturer = EXT010.get("EXSUN1")
    // Get manufacturer description
    String manufacturer_description = ""
    if(EXT010.get("EXSUN1") != ""){
      DBAction query = database.table("CIDMAS").index("00").selection("IDSUNM").build()
      DBContainer CIDMAS = query.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO",  EXT010.get("EXSUN1"))
      if (query.read(CIDMAS)) {
        manufacturer_description = CIDMAS.get("IDSUNM")
      }
    }
    CNUF = EXT010.get("EXSUN3")
    // Get CNUF description
    String CNUF_description = ""
    if(EXT010.get("EXSUN3") != ""){
      DBAction query = database.table("CIDMAS").index("00").selection("IDSUNM").build()
      DBContainer CIDMAS = query.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO",  EXT010.get("EXSUN3"))
      if (query.read(CIDMAS)) {
        CNUF_description = CIDMAS.get("IDSUNM")
      }
    }
    country = EXT010.get("EXCSCD")
    // Get country description
    String country_description = ""
    if(EXT010.get("EXCSCD") != ""){
      DBAction query = database.table("CSYTAB").index("00").selection("CTTX15").build()
      DBContainer CSYTAB = query.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CSCD")
      CSYTAB.set("CTSTKY", EXT010.get("EXCSCD"))
      if (query.read(CSYTAB)) {
        country_description = CSYTAB.get("CTTX15")
      }
    }
    HScode = EXT010.get("EXCSNO")
    // Get HS code description
    String HScode_description = ""
    if(EXT010.get("EXCSNO") != ""){
      DBAction query = database.table("CSYCSN").index("00").selection("CKTX15").build()
      DBContainer CSYCSN = query.getContainer()
      CSYCSN.set("CKCONO",currentCompany)
      CSYCSN.set("CKCSNO", EXT010.get("EXCSNO"))
      if (query.read(CSYCSN)) {
        HScode_description = CSYCSN.get("CKTX15")
      }
    }
    IFLSfield = EXT010.get("EXHIE0")
    // Get IFLS field description
    String IFLSfield_description = ""
    if(EXT010.get("EXHIE0") != ""){
      DBAction query = database.table("MITHRY").index("00").selection("HITX15").build()
      DBContainer MITHRY = query.getContainer()
      MITHRY.set("HICONO",currentCompany)
      MITHRY.set("HIHLVL",  5)
      MITHRY.set("HIHIE0", EXT010.get("EXHIE0"))
      if (query.read(MITHRY)) {
        IFLSfield_description = MITHRY.get("HITX15")
      }
    }
    mainIngredient = EXT010.get("EXSPE1")
    contains = EXT010.get("EXSPE2")
    //GLNcode = EXT010.get("EXSUCM")
    brand = EXT010.get("EXCFI1")
    // Get brand description
    String brand_description = ""
    if(EXT010.get("EXCFI1") != ""){
      DBAction query = database.table("CSYTAB").index("00").selection("CTTX15").build()
      DBContainer CSYTAB = query.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CFI1")
      CSYTAB.set("CTSTKY", EXT010.get("EXCFI1"))
      if (query.read(CSYTAB)) {
        brand_description = CSYTAB.get("CTTX15")
      }
    }
    controlCode = EXT010.get("EXCFI4")
    // Get controlCode description
    String controlCode_description = ""
    if(EXT010.get("EXCFI4") != ""){
      DBAction query = database.table("CSYTAB").index("00").selection("CTTX15").build()
      DBContainer CSYTAB = query.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CFI4")
      CSYTAB.set("CTSTKY", EXT010.get("EXCFI4"))
      if (query.read(CSYTAB)) {
        controlCode_description = CSYTAB.get("CTTX15")
      }
    }
    PNM = EXT010.get("EXCFI5")
    // Get PNM description
    String PNM_description = ""
    if(EXT010.get("EXCFI5") != ""){
      DBAction query = database.table("CSYTAB").index("00").selection("CTTX15").build()
      DBContainer CSYTAB = query.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CFI5")
      CSYTAB.set("CTSTKY", EXT010.get("EXCFI5"))
      if (query.read(CSYTAB)) {
        PNM_description = CSYTAB.get("CTTX15")
      }
    }
    standardLifetime = EXT010.get("EXZSLT")
    percentageOfMandatoryLifetime = EXT010.get("EXZPLT")
    origin = EXT010.get("EXCSC1")
    // Get origin description
    String origin_description = ""
    if(EXT010.get("EXCSC1") != ""){
      DBAction query = database.table("CSYTAB").index("00").selection("CTTX15").build()
      DBContainer CSYTAB = query.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CSCD")
      CSYTAB.set("CTSTKY", EXT010.get("EXCSC1"))
      if (query.read(CSYTAB)) {
        origin_description = CSYTAB.get("CTTX15")
      }
    }
    orderType = EXT010.get("EXORTP")
    // Get order type description
    String orderType_description = ""
    if(EXT010.get("EXORTP") != ""){
      DBAction query = database.table("OOTYPE").index("00").selection("OOTX15").build()
      DBContainer OOTYPE = query.getContainer()
      OOTYPE.set("OOCONO",currentCompany)
      OOTYPE.set("OOORTP", EXT010.get("EXORTP"))
      if (query.read(OOTYPE)) {
        orderType_description = OOTYPE.get("OOTX15")
      }
    }
    itemType = EXT010.get("EXITTY")
    // Get item type description
    String itemType_description = ""
    if(EXT010.get("EXITTY") != ""){
      DBAction query = database.table("MITTTY").index("00").selection("TYTX15").build()
      DBContainer MITTTY = query.getContainer()
      MITTTY.set("TYCONO",currentCompany)
      MITTTY.set("TYITTY", EXT010.get("EXITTY"))
      if (query.read(MITTTY)) {
        itemType_description = MITTTY.get("TYTX15")
      }
    }
    dangerClass = EXT010.get("EXZONU")
    dangerIndicator = EXT010.get("EXHAZI")
    potentiallyDangerous = EXT010.get("EXZPDA")
    warehouse = EXT010.get("EXWHLO")
    // Get warehouse description
    String warehouse_description = ""
    if(EXT010.get("EXWHLO") != ""){
      DBAction query = database.table("MITWHL").index("00").selection("MWWHNM").build()
      DBContainer MITWHL = query.getContainer()
      MITWHL.set("MWCONO",currentCompany)
      MITWHL.set("MWWHLO", EXT010.get("EXWHLO"))
      if (query.read(MITWHL)) {
        warehouse_description = MITWHL.get("MWWHNM")
      }
    }
    String documentID1 = EXT010.get("EXDO01")
    // Get document ID 1 description
    String documentID1_description = ""
    if(EXT010.get("EXDO01") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO01"))
      if (query.read(MPDDOC)) {
        documentID1_description = MPDDOC.get("DODODE")
      }
    }
    String documentID2 = EXT010.get("EXDO02")
    // Get document ID 2 description
    String documentID2_description = ""
    if(EXT010.get("EXDO02") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO02"))
      if (query.read(MPDDOC)) {
        documentID2_description = MPDDOC.get("DODODE")
      }
    }
    String documentID3 = EXT010.get("EXDO03")
    // Get document ID 3 description
    String documentID3_description = ""
    if(EXT010.get("EXDO03") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO03"))
      if (query.read(MPDDOC)) {
        documentID3_description = MPDDOC.get("DODODE")
      }
    }
    String documentID4 = EXT010.get("EXDO04")
    // Get document ID 4 description
    String documentID4_description = ""
    if(EXT010.get("EXDO04") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO04"))
      if (query.read(MPDDOC)) {
        documentID4_description = MPDDOC.get("DODODE")
      }
    }
    String documentID5 = EXT010.get("EXDO05")
    // Get document ID 5 description
    String documentID5_description = ""
    if(EXT010.get("EXDO05") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO05"))
      if (query.read(MPDDOC)) {
        documentID5_description = MPDDOC.get("DODODE")
      }
    }
    String documentID6 = EXT010.get("EXDO06")
    // Get document ID 6 description
    String documentID6_description = ""
    if(EXT010.get("EXDO06") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO06"))
      if (query.read(MPDDOC)) {
        documentID6_description = MPDDOC.get("DODODE")
      }
    }
    String documentID7 = EXT010.get("EXDO07")
    // Get document ID 7 description
    String documentID7_description = ""
    if(EXT010.get("EXDO07") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO07"))
      if (query.read(MPDDOC)) {
        documentID7_description = MPDDOC.get("DODODE")
      }
    }
    String documentID8 = EXT010.get("EXDO08")
    // Get document ID 8 description
    String documentID8_description = ""
    if(EXT010.get("EXDO08") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO08"))
      if (query.read(MPDDOC)) {
        documentID8_description = MPDDOC.get("DODODE")
      }
    }
    String documentID9 = EXT010.get("EXDO09")
    // Get document ID 9 description
    String documentID9_description = ""
    if(EXT010.get("EXDO09") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO09"))
      if (query.read(MPDDOC)) {
        documentID9_description = MPDDOC.get("DODODE")
      }
    }
    String documentID10 = EXT010.get("EXDO10")
    // Get document ID 10 description
    String documentID10_description = ""
    if(EXT010.get("EXDO10") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO10"))
      if (query.read(MPDDOC)) {
        documentID10_description = MPDDOC.get("DODODE")
      }
    }
    String documentID11 = EXT010.get("EXDO11")
    // Get document ID 11 description
    String documentID11_description = ""
    if(EXT010.get("EXDO11") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO11"))
      if (query.read(MPDDOC)) {
        documentID11_description = MPDDOC.get("DODODE")
      }
    }
    String documentID12 = EXT010.get("EXDO12")
    // Get document ID 12 description
    String documentID12_description = ""
    if(EXT010.get("EXDO12") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO12"))
      if (query.read(MPDDOC)) {
        documentID12_description = MPDDOC.get("DODODE")
      }
    }
    String documentID13 = EXT010.get("EXDO13")
    // Get document ID 13 description
    String documentID13_description = ""
    if(EXT010.get("EXDO13") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO13"))
      if (query.read(MPDDOC)) {
        documentID13_description = MPDDOC.get("DODODE")
      }
    }
    String documentID14 = EXT010.get("EXDO14")
    // Get document ID 14 description
    String documentID14_description = ""
    if(EXT010.get("EXDO14") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO14"))
      if (query.read(MPDDOC)) {
        documentID14_description = MPDDOC.get("DODODE")
      }
    }
    String documentID15 = EXT010.get("EXDO15")
    // Get document ID 15 description
    String documentID15_description = ""
    if(EXT010.get("EXDO15") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO15"))
      if (query.read(MPDDOC)) {
        documentID15_description = MPDDOC.get("DODODE")
      }
    }
    txid = EXT010.get("EXTXID")
    ztps = EXT010.get("EXZTPS")
    String entryDate = EXT010.get("EXRGDT")
    String entryTime = EXT010.get("EXRGTM")
    String changeDate = EXT010.get("EXLMDT")
    String changeNumber = EXT010.get("EXCHNO")
    String changedBy = EXT010.get("EXCHID")
    mi.outData.put("ZCID", constraintID)
    mi.outData.put("ITNO", itemNumber)
    mi.outData.put("ITDS", itemNumber_description)
    mi.outData.put("ZCFE", constrainingFeature)
    mi.outData.put("ZDE1", constrainingFeature_description)
    mi.outData.put("ZCLV", constraintLevel)
    mi.outData.put("ZDE2", constraintLevel_description)
    mi.outData.put("SUNO", supplierNumber)
    mi.outData.put("SUNM", supplierNumber_description)
    mi.outData.put("CUNO", customerNumber)
    mi.outData.put("CUNM", customerNumber_description)
    mi.outData.put("SUN1", manufacturer)
    mi.outData.put("SNM1", manufacturer_description)
    mi.outData.put("SUN3", CNUF)
    mi.outData.put("SNM3", CNUF_description)
    mi.outData.put("CSCD", country)
    mi.outData.put("ZCSC", country_description)
    mi.outData.put("CSNO", HScode)
    mi.outData.put("ZCSN", HScode_description)
    mi.outData.put("HIE0", IFLSfield)
    mi.outData.put("ZHIE", IFLSfield_description)
    mi.outData.put("SPE1", mainIngredient)
    mi.outData.put("SPE2", contains)
    //mi.outData.put("SUCM", GLNcode)
    mi.outData.put("CFI1", brand)
    mi.outData.put("ZCF1", brand_description)
    mi.outData.put("CFI4", controlCode)
    mi.outData.put("ZCF4", controlCode_description)
    mi.outData.put("CFI5", PNM)
    mi.outData.put("ZCF5", PNM_description)
    mi.outData.put("ZSLT", standardLifetime)
    mi.outData.put("ZPLT", percentageOfMandatoryLifetime)
    mi.outData.put("CSC1", origin)
    mi.outData.put("ZCS1", origin_description)
    mi.outData.put("ORTP", orderType)
    mi.outData.put("ZORT", orderType_description)
    mi.outData.put("ITTY", itemType)
    mi.outData.put("ZITT", itemType_description)
    mi.outData.put("ZONU", dangerClass)
    mi.outData.put("HAZI", dangerIndicator)
    mi.outData.put("ZPDA", potentiallyDangerous)
    mi.outData.put("WHLO", warehouse)
    mi.outData.put("WHNM", warehouse_description)
    mi.outData.put("DO01", documentID1)
    mi.outData.put("DD01", documentID1_description)
    mi.outData.put("DO02", documentID2)
    mi.outData.put("DD02", documentID2_description)
    mi.outData.put("DO03", documentID3)
    mi.outData.put("DD03", documentID3_description)
    mi.outData.put("DO04", documentID4)
    mi.outData.put("DD04", documentID4_description)
    mi.outData.put("DO05", documentID5)
    mi.outData.put("DD05", documentID5_description)
    mi.outData.put("DO06", documentID6)
    mi.outData.put("DD06", documentID6_description)
    mi.outData.put("DO07", documentID7)
    mi.outData.put("DD07", documentID7_description)
    mi.outData.put("DO08", documentID8)
    mi.outData.put("DD08", documentID8_description)
    mi.outData.put("DO09", documentID9)
    mi.outData.put("DD09", documentID9_description)
    mi.outData.put("DO10", documentID10)
    mi.outData.put("DD10", documentID10_description)
    mi.outData.put("DO11", documentID11)
    mi.outData.put("DD11", documentID11_description)
    mi.outData.put("DO12", documentID12)
    mi.outData.put("DD12", documentID12_description)
    mi.outData.put("DO13", documentID13)
    mi.outData.put("DD13", documentID13_description)
    mi.outData.put("DO14", documentID14)
    mi.outData.put("DD14", documentID14_description)
    mi.outData.put("DO15", documentID15)
    mi.outData.put("DD15", documentID15_description)
    mi.outData.put("TXID", txid)
    mi.outData.put("ZTPS", ztps)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
  // Build expression
  public void buildExpression(){
    ExpressionFactory expression = database.getExpressionFactory("EXT010")
    if(constraintID != null){
      expression = expression.eq("EXZCID", constraintID)
    } else {
      if(itemNumber != null){
        expression = expression.eq("EXITNO", itemNumber)
      }
      if(constrainingFeature != null){
        if(itemNumber == null){
          expression = expression.eq("EXZCFE", constrainingFeature)
        } else {
          expression = expression.and(expression.eq("EXZCFE", constrainingFeature))
        }
      }
      if(constraintLevel != null){
        if(itemNumber == null && constrainingFeature == null){
          expression = expression.eq("EXZCLV", constraintLevel)
        } else {
          expression = expression.and(expression.eq("EXZCLV", constraintLevel))
        }
      }
      if(supplierNumber != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null){
          expression = expression.like("EXSUNO", supplierNumber)
        } else {
          expression = expression.and(expression.like("EXSUNO", supplierNumber))
        }
      }
      if(customerNumber != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null){
          expression = expression.eq("EXCUNO", customerNumber)
        } else {
          expression = expression.and(expression.eq("EXCUNO", customerNumber))
        }
      }
      if(manufacturer != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null){
          expression = expression.like("EXSUN1", manufacturer)
        } else {
          expression = expression.and(expression.like("EXSUN1", manufacturer))
        }
      }
      if(CNUF != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null){
          expression = expression.like("EXSUN3", CNUF)
        } else {
          expression = expression.and(expression.like("EXSUN3", CNUF))
        }
      }
      if(country != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null){
          expression = expression.eq("EXCSCD", country)
        } else {
          expression = expression.and(expression.eq("EXCSCD", country))
        }
      }
      if(HScode != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null){
          expression = expression.like("EXCSNO", HScode)
        } else {
          expression = expression.and(expression.like("EXCSNO", HScode))
        }
      }
      if(IFLSfield != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null){
          expression = expression.like("EXHIE0", IFLSfield)
        } else {
          expression = expression.and(expression.like("EXHIE0", IFLSfield))
        }
      }
      if(mainIngredient != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null){
          expression = expression.eq("EXSPE1", mainIngredient)
        } else {
          expression = expression.and(expression.eq("EXSPE1", mainIngredient))
        }
      }
      /**
       if(GLNcode != null){
       if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null){
       expression = expression.eq("EXSUCM", GLNcode)
       } else {
       expression = expression.and(expression.eq("EXSUCM", GLNcode))
       }
       }
       **/
      if(PNM != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/){
          expression = expression.eq("EXCFI5", PNM)
        } else {
          expression = expression.and(expression.eq("EXCFI5", PNM))
        }
      }
      if(brand != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null){
          expression = expression.eq("EXCFI1", brand)
        } else {
          expression = expression.and(expression.eq("EXCFI1", brand))
        }
      }
      if(standardLifetime != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null){
          expression = expression.eq("EXZSLT", standardLifetime)
        } else {
          expression = expression.and(expression.eq("EXZSLT", standardLifetime))
        }
      }
      if(percentageOfMandatoryLifetime != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null){
          expression = expression.eq("EXZPLT", percentageOfMandatoryLifetime)
        } else {
          expression = expression.and(expression.eq("EXZPLT", percentageOfMandatoryLifetime))
        }
      }
      if(origin != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null){
          expression = expression.eq("EXCSC1", origin)
        } else {
          expression = expression.and(expression.eq("EXCSC1", origin))
        }
      }
      if(documentID1 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null){
          expression = expression.eq("EXDO01", documentID1)
        } else {
          expression = expression.and(expression.eq("EXDO01", documentID1))
        }
      }
      if(documentID2 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null){
          expression = expression.eq("EXDO02", documentID2)
        } else {
          expression = expression.and(expression.eq("EXDO02", documentID2))
        }
      }
      if(documentID3 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null){
          expression = expression.eq("EXDO03", documentID3)
        } else {
          expression = expression.and(expression.eq("EXDO03", documentID3))
        }
      }
      if(documentID4 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null){
          expression = expression.eq("EXDO04", documentID4)
        } else {
          expression = expression.and(expression.eq("EXDO04", documentID4))
        }
      }
      if(documentID5 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null){
          expression = expression.eq("EXDO05", documentID5)
        } else {
          expression = expression.and(expression.eq("EXDO05", documentID5))
        }
      }
      if(documentID6 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null){
          expression = expression.eq("EXDO06", documentID6)
        } else {
          expression = expression.and(expression.eq("EXDO06", documentID6))
        }
      }
      if(documentID7 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null){
          expression = expression.eq("EXDO07", documentID7)
        } else {
          expression = expression.and(expression.eq("EXDO07", documentID7))
        }
      }
      if(documentID8 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null){
          expression = expression.eq("EXDO08", documentID8)
        } else {
          expression = expression.and(expression.eq("EXDO08", documentID8))
        }
      }
      if(documentID9 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null){
          expression = expression.eq("EXDO09", documentID9)
        } else {
          expression = expression.and(expression.eq("EXDO09", documentID9))
        }
      }
      if(documentID10 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null){
          expression = expression.eq("EXDO10", documentID10)
        } else {
          expression = expression.and(expression.eq("EXDO10", documentID10))
        }
      }
      if(documentID11 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null){
          expression = expression.eq("EXDO11", documentID11)
        } else {
          expression = expression.and(expression.eq("EXDO11", documentID11))
        }
      }
      if(documentID12 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null){
          expression = expression.eq("EXDO12", documentID12)
        } else {
          expression = expression.and(expression.eq("EXDO12", documentID12))
        }
      }
      if(documentID13 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null){
          expression = expression.eq("EXDO13", documentID13)
        } else {
          expression = expression.and(expression.eq("EXDO13", documentID13))
        }
      }
      if(documentID14 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null){
          expression = expression.eq("EXDO14", documentID14)
        } else {
          expression = expression.and(expression.eq("EXDO14", documentID14))
        }
      }

      if(documentID15 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null){
          expression = expression.eq("EXDO15", documentID15)
        } else {
          expression = expression.and(expression.eq("EXDO15", documentID15))
        }
      }
      if(documentID16 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null/**&& documentID15 == null**/){
          expression = expression.eq("EXDO16", documentID16)
        } else {
          expression = expression.and(expression.eq("EXDO16", documentID16))
        }
      }
      if(documentID17 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null/**&& documentID15 == null**/ && documentID16 == null){
          expression = expression.eq("EXDO17", documentID17)
        } else {
          expression = expression.and(expression.eq("EXDO17", documentID17))
        }
      }
      if(documentID18 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null/**&& documentID15 == null**/ && documentID16 == null && documentID17 == null){
          expression = expression.eq("EXDO18", documentID18)
        } else {
          expression = expression.and(expression.eq("EXDO18", documentID18))
        }
      }
      if(documentID19 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null/**&& documentID15 == null**/ && documentID16 == null && documentID17 == null && documentID18 == null){
          expression = expression.eq("EXDO19", documentID19)
        } else {
          expression = expression.and(expression.eq("EXDO19", documentID19))
        }
      }
      if(documentID20 != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null/**&& documentID15 == null**/ && documentID16 == null && documentID17 == null && documentID18 == null && documentID19 == null){
          expression = expression.eq("EXDO20", documentID20)
        } else {
          expression = expression.and(expression.eq("EXDO20", documentID20))
        }
      }
      if(controlCode != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null/**&& documentID15 == null**/ && documentID16 == null && documentID17 == null && documentID18 == null && documentID19 == null && documentID20 == null){
          expression = expression.eq("EXCFI4", controlCode)
        } else {
          expression = expression.and(expression.eq("EXCFI4", controlCode))
        }
      }
      if(dangerClass != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null/**&& documentID15 == null**/ && documentID16 == null && documentID17 == null && documentID18 == null && documentID19 == null && documentID20 == null && controlCode == null){
          expression = expression.eq("EXZONU", dangerClass)
        } else {
          expression = expression.and(expression.eq("EXZONU", dangerClass))
        }
      }
      if(orderType != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null/**&& documentID15 == null**/ && documentID16 == null && documentID17 == null && documentID18 == null && documentID19 == null && documentID20 == null && controlCode == null && dangerClass == null){
          expression = expression.eq("EXORTP", orderType)
        } else {
          expression = expression.and(expression.eq("EXORTP", orderType))
        }
      }
      if(itemType != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null/**&& documentID15 == null**/ && documentID16 == null && documentID17 == null && documentID18 == null && documentID19 == null && documentID20 == null && controlCode == null && dangerClass == null && orderType == null){
          expression = expression.eq("EXITTY", itemType)
        } else {
          expression = expression.and(expression.eq("EXITTY", itemType))
        }
      }
      if(contains != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null/**&& documentID15 == null**/ && documentID16 == null && documentID17 == null && documentID18 == null && documentID19 == null && documentID20 == null && controlCode == null && dangerClass == null && orderType == null && itemType == null){
          expression = expression.eq("EXSPE2", contains)
        } else {
          expression = expression.and(expression.eq("EXSPE2", contains))
        }
      }
      if(dangerIndicator != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null/**&& documentID15 == null**/ && documentID16 == null && documentID17 == null && documentID18 == null && documentID19 == null && documentID20 == null && controlCode == null && dangerClass == null && orderType == null && itemType == null && contains == null){
          expression = expression.eq("EXHAZI", dangerIndicator)
        } else {
          expression = expression.and(expression.eq("EXHAZI", dangerIndicator))
        }
      }
      if(potentiallyDangerous != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null/**&& documentID15 == null**/ && documentID16 == null && documentID17 == null && documentID18 == null && documentID19 == null && documentID20 == null && controlCode == null && dangerClass == null && orderType == null && itemType == null && contains == null && dangerIndicator == null){
          expression = expression.eq("EXZPDA", potentiallyDangerous)
        } else {
          expression = expression.and(expression.eq("EXZPDA", potentiallyDangerous))
        }
      }
      if(warehouse != null){
        if(itemNumber == null && constrainingFeature == null && constraintLevel == null && supplierNumber == null && customerNumber == null && manufacturer == null && CNUF == null && country == null && HScode == null && IFLSfield == null && mainIngredient == null /**&& GLNcode == null**/ && PNM == null && brand == null && standardLifetime == null && percentageOfMandatoryLifetime == null && origin == null && documentID1 == null && documentID2 == null && documentID3 == null && documentID4 == null && documentID5 == null && documentID6 == null && documentID7 == null && documentID8 == null && documentID9 == null && documentID10 == null && documentID11 == null && documentID12 == null && documentID13 == null && documentID14 == null/**&& documentID15 == null**/ && documentID16 == null && documentID17 == null && documentID18 == null && documentID19 == null && documentID20 == null && controlCode == null && dangerClass == null && orderType == null && itemType == null && contains == null && dangerIndicator == null && potentiallyDangerous == null){
          expression = expression.eq("EXWHLO", warehouse)
        } else {
          expression = expression.and(expression.eq("EXWHLO", warehouse))
        }
      }
    }
    DBAction query = database.table("EXT010").index("10").matching(expression).selection("EXZCID", "EXITNO", "EXZCFE", "EXZCLV", "EXSUNO", "EXCUNO", "EXSUN1", "EXSUN3", "EXCSCD", "EXCSNO", "EXHIE0", "EXSPE1", "EXSPE2", /**"EXSUCM",**/ "EXCFI1", "EXCFI4", "EXCFI5", "EXZSLT", "EXZPLT", "EXCSC1", "EXORTP", "EXITTY", "EXZONU", "EXHAZI", "EXZPDA", "EXZTPS", "EXWHLO", "EXDO01", "EXDO02", "EXDO03", "EXDO04", "EXDO05", "EXDO06", "EXDO07", "EXDO08", "EXDO09", "EXDO10", "EXDO11", "EXDO12", "EXDO13", "EXDO14", "EXDO15", "EXTXID", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT010 = query.getContainer()
    EXT010.set("EXCONO", currentCompany)
    if(query.readAll(EXT010, 1, outData)){
    } else {
      mi.error("Record does not exist")
      return
    }
  }
}
