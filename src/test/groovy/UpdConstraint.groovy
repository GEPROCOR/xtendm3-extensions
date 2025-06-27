/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT010MI.UpdConstraint
 * Description : Update records from the EXT010 table.
 * Date         Changed By   Description
 * 20210219     RENARN       QUAX01 - Constraints matrix
 * 20211102     RENARN       Input parameters HAZI and ZPDA have been added
 * 20220228     RENARN       ZTPS, WHLO has been added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdConstraint extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private Integer zblc
  private String itemNumber = ""
  private String constrainingFeature = ""
  private String constraintLevel = ""
  private String supplierNumber = ""
  private String customerNumber = ""
  private String manufacturer = ""
  private String CNUF = ""
  private String country = ""
  private String HScode = ""
  private String IFLSfield = ""
  private String mainIngredient = ""
  private String contains = ""
  private String brand = ""
  private String controlCode = ""
  private String PNM = ""
  private String origin = ""
  private String dangerClass = ""
  private String orderType = ""
  private String itemType = ""
  private String dangerIndicator = ""
  private String potentiallyDangerous = ""
  private String ZGKY = ""
  private String warehouse = ""

  public UpdConstraint(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
    this.mi = mi
    this.database = database
    this.program = program
    this.logger = logger
  }

  public void main() {
    Integer currentCompany
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
      itemNumber = mi.in.get("ITNO").toString().trim()
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
      constrainingFeature = mi.in.get("ZCFE").toString().trim()
    }
    // Check constraint level
    zblc = 0
    if(mi.in.get("ZCLV") != null && mi.in.get("ZCLV") != "*"){
      DBAction query = database.table("EXT012").index("00").selection("EXZBLC").build()
      DBContainer EXT012 = query.getContainer()
      EXT012.set("EXCONO", currentCompany)
      EXT012.set("EXZCLV",  mi.in.get("ZCLV"))
      if (!query.read(EXT012)) {
        mi.error("Niveau de contrainte " + mi.in.get("ZCLV") + " n'existe pas")
        return
      } else {
        zblc = EXT012.get("EXZBLC")
      }
      constraintLevel = mi.in.get("ZCLV").toString().trim()
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
      supplierNumber = mi.in.get("SUNO").toString().trim()
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
      customerNumber = mi.in.get("CUNO").toString().trim()
    }
    // Check manufacturer
    if(mi.in.get("SUN1") != null && !(mi.in.get("SUN1") as String).contains("*")){
      ExpressionFactory expression = database.getExpressionFactory("CIDVEN")
      expression = expression.eq("IISUCL", "200")
      DBAction query = database.table("CIDVEN").index("00").matching(expression).build()
      DBContainer CIDVEN = query.getContainer()
      CIDVEN.set("IICONO", currentCompany)
      CIDVEN.set("IISUNO",  mi.in.get("SUN1"))
      if (!query.read(CIDVEN)) {
        ExpressionFactory expression2 = database.getExpressionFactory("CIDVEN")
        expression2 = expression2.eq("IISUCL", "500")
        DBAction query2 = database.table("CIDVEN").index("00").matching(expression2).build()
        DBContainer CIDVEN2 = query2.getContainer()
        CIDVEN2.set("IICONO", currentCompany)
        CIDVEN2.set("IISUNO",  mi.in.get("SUN1"))
        if (!query2.read(CIDVEN2)) {
          mi.error("Fabricant " + mi.in.get("SUN1") + " n'existe pas")
          return
        }
      }
      manufacturer = mi.in.get("SUN1").toString().trim()
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
      CNUF = mi.in.get("SUN3").toString().trim()
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
      country = mi.in.get("CSCD").toString().trim()
    }
    if(mi.in.get("CSNO") != null)
      HScode = mi.in.get("CSNO").toString().trim()
    if(mi.in.get("HIE0") != null)
      IFLSfield = mi.in.get("HIE0").toString().trim()
    if(mi.in.get("SPE1") != null)
      mainIngredient = mi.in.get("SPE1").toString().trim()
    if(mi.in.get("SPE2") != null)
      contains = mi.in.get("SPE2").toString().trim()
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
      brand = mi.in.get("CFI1").toString().trim()
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
      controlCode = mi.in.get("CFI4").toString().trim()
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
      PNM = mi.in.get("CFI5").toString().trim()
    }
    // Check percentage of mandatory lifetime
    int percentageOfMandatoryLifetime = 0
    if(mi.in.get("ZPLT") != null){
      percentageOfMandatoryLifetime = mi.in.get("ZPLT")
      if(percentageOfMandatoryLifetime < 0 || percentageOfMandatoryLifetime > 100){
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
      origin = mi.in.get("CSC1").toString().trim()
    }
    if(mi.in.get("ZONU") != null)
      dangerClass = mi.in.get("ZONU").toString().trim()
    // Check order type
    if(mi.in.get("ORTP") != null && mi.in.get("ORTP") != "*"){
      DBAction countryQuery = database.table("OOTYPE").index("00").build()
      DBContainer OOTYPE = countryQuery.getContainer()
      OOTYPE.set("OOCONO",currentCompany)
      OOTYPE.set("OOORTP", mi.in.get("ORTP"))
      if (!countryQuery.read(OOTYPE)) {
        mi.error("Type commande " + mi.in.get("ORTP") + " n'existe pas")
        return
      }
      orderType = mi.in.get("ORTP").toString().trim()
    }
    // Check item type
    if(mi.in.get("ITTY") != null && mi.in.get("ITTY") != "*"){
      DBAction countryQuery = database.table("MITTTY").index("00").build()
      DBContainer MITTTY = countryQuery.getContainer()
      MITTTY.set("TYCONO",currentCompany)
      MITTTY.set("TYITTY", mi.in.get("ITTY"))
      if (!countryQuery.read(MITTTY)) {
        mi.error("Type article " + mi.in.get("ITTY") + " n'existe pas")
        return
      }
      itemType = mi.in.get("ITTY").toString().trim()
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
    if(mi.in.get("WHLO") != null && mi.in.get("WHLO") != "*") {
      DBAction query_whlo = database.table("MITWHL").index("00").build()
      DBContainer MITWHL = query_whlo.getContainer()
      MITWHL.set("MWCONO", currentCompany)
      MITWHL.set("MWWHLO", mi.in.get("WHLO"))
      if (!query_whlo.read(MITWHL)) {
        mi.error("Dépôt " + mi.in.get("WHLO") + " n'existe pas")
        return
      }
      warehouse = mi.in.get("WHLO").toString().trim()
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
    DBAction query = database.table("EXT010").index("00").selection("EXITNO", "EXZCFE", "EXZCLV", "EXSUNO", "EXCUNO", "EXSUN1", "EXSUN3", "EXCSCD", "EXCSNO", "EXHIE0", "EXSPE1", "EXSPE2", /**"EXSUCM",**/ "EXCFI1", "EXCFI4", "EXCFI5", "EXZSLT", "EXZPLT", "EXCSC1", "EXORTP", "EXITTY", "EXZONU", "EXHAZI", "EXZPDA", "EXZTPS", "EXWHLO", "EXDO01", "EXDO02", "EXDO03", "EXDO04", "EXDO05", "EXDO06", "EXDO07", "EXDO08", "EXDO09", "EXDO10", "EXDO11", "EXDO12", "EXDO13", "EXDO14", "EXDO15", "EXTXID", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT010 = query.getContainer()
    EXT010.set("EXCONO", currentCompany)
    EXT010.set("EXZCID",  mi.in.get("ZCID"))
    if(!query.readAll(EXT010, 2, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
    EXT010.set("EXCONO", currentCompany)
    EXT010.set("EXZCID", mi.in.get("ZCID"))
    if(!query.readLock(EXT010, updateCallBack)){
    }

    // Check text id
    int txid = 0
    if(mi.in.get("TXID") != null){
      txid = mi.in.get("TXID")
      if(txid < 0){
        mi.error("Text ID " + mi.in.get("TXID") + " est invalide")
        return
      }
    }
  }
  // Update EXT010
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    if(mi.in.get("ITNO") != null)
      lockedResult.set("EXITNO", mi.in.get("ITNO"))
    if(mi.in.get("ZCFE") != null)
      lockedResult.set("EXZCFE", mi.in.get("ZCFE"))
    if(mi.in.get("ZCLV") != null){
      lockedResult.set("EXZCLV", mi.in.get("ZCLV"))
      lockedResult.set("EXZBLC", zblc)
    }
    if(mi.in.get("SUNO") != null)
      lockedResult.set("EXSUNO", mi.in.get("SUNO"))
    if(mi.in.get("CUNO") != null)
      lockedResult.set("EXCUNO", mi.in.get("CUNO"))
    if(mi.in.get("SUN1") != null)
      lockedResult.set("EXSUN1", mi.in.get("SUN1"))
    if(mi.in.get("SUN3") != null)
      lockedResult.set("EXSUN3", mi.in.get("SUN3"))
    if(mi.in.get("CSCD") != null)
      lockedResult.set("EXCSCD", mi.in.get("CSCD"))
    if(mi.in.get("CSNO") != null)
      lockedResult.set("EXCSNO", mi.in.get("CSNO"))
    if(mi.in.get("HIE0") != null)
      lockedResult.set("EXHIE0", mi.in.get("HIE0"))
    if(mi.in.get("SPE1") != null)
      lockedResult.set("EXSPE1", mi.in.get("SPE1"))
    if(mi.in.get("SPE2") != null)
      lockedResult.set("EXSPE2", mi.in.get("SPE2"))
    //if(mi.in.get("SUCM") != null)
    //lockedResult.set("EXSUCM", mi.in.get("SUCM"))
    if(mi.in.get("CFI1") != null)
      lockedResult.set("EXCFI1", mi.in.get("CFI1"))
    if(mi.in.get("CFI4") != null)
      lockedResult.set("EXCFI4", mi.in.get("CFI4"))
    if(mi.in.get("CFI5") != null)
      lockedResult.set("EXCFI5", mi.in.get("CFI5"))
    if(mi.in.get("ZSLT") != null)
      lockedResult.set("EXZSLT", mi.in.get("ZSLT"))
    if(mi.in.get("ZPLT") != null)
      lockedResult.set("EXZPLT", mi.in.get("ZPLT"))
    if(mi.in.get("CSC1") != null)
      lockedResult.set("EXCSC1", mi.in.get("CSC1"))
    if(mi.in.get("ORTP") != null)
      lockedResult.set("EXORTP", mi.in.get("ORTP"))
    if(mi.in.get("ITTY") != null)
      lockedResult.set("EXITTY", mi.in.get("ITTY"))
    if(mi.in.get("HAZI") != null)
      lockedResult.set("EXHAZI", mi.in.get("HAZI"))
    if(mi.in.get("ZPDA") != null)
      lockedResult.set("EXZPDA", mi.in.get("ZPDA"))
    if(mi.in.get("ZONU") != null)
      lockedResult.set("EXZONU", mi.in.get("ZONU"))
    if(mi.in.get("WHLO") != null)
      lockedResult.set("EXWHLO", mi.in.get("WHLO"))
    if(mi.in.get("DO01") != null)
      lockedResult.set("EXDO01", mi.in.get("DO01"))
    if(mi.in.get("DO02") != null)
      lockedResult.set("EXDO02", mi.in.get("DO02"))
    if(mi.in.get("DO03") != null)
      lockedResult.set("EXDO03", mi.in.get("DO03"))
    if(mi.in.get("DO04") != null)
      lockedResult.set("EXDO04", mi.in.get("DO04"))
    if(mi.in.get("DO05") != null)
      lockedResult.set("EXDO05", mi.in.get("DO05"))
    if(mi.in.get("DO06") != null)
      lockedResult.set("EXDO06", mi.in.get("DO06"))
    if(mi.in.get("DO07") != null)
      lockedResult.set("EXDO07", mi.in.get("DO07"))
    if(mi.in.get("DO08") != null)
      lockedResult.set("EXDO08", mi.in.get("DO08"))
    if(mi.in.get("DO09") != null)
      lockedResult.set("EXDO09", mi.in.get("DO09"))
    if(mi.in.get("DO10") != null)
      lockedResult.set("EXDO10", mi.in.get("DO10"))
    if(mi.in.get("DO11") != null)
      lockedResult.set("EXDO11", mi.in.get("DO11"))
    if(mi.in.get("DO12") != null)
      lockedResult.set("EXDO12", mi.in.get("DO12"))
    if(mi.in.get("DO13") != null)
      lockedResult.set("EXDO13", mi.in.get("DO13"))
    if(mi.in.get("DO14") != null)
      lockedResult.set("EXDO14", mi.in.get("DO14"))
    if(mi.in.get("DO15") != null)
      lockedResult.set("EXDO15", mi.in.get("DO15"))
    if(mi.in.get("TXID") != null)
      lockedResult.set("EXTXID", mi.in.get("TXID"))
    if(mi.in.get("ZTPS") != null)
      lockedResult.set("EXZTPS", mi.in.get("ZTPS"))
    lockedResult.set("EXZGKY", ZGKY)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // Get EXT010
  Closure<?> outData = { DBContainer EXT010 ->
    if(itemNumber == ""){
      itemNumber = EXT010.get("EXITNO").toString().trim()
    }
    if(constrainingFeature == ""){
      constrainingFeature = EXT010.get("EXZCFE").toString().trim()
    }
    if(constraintLevel == ""){
      constraintLevel = EXT010.get("EXZCLV").toString().trim()
    }
    if(supplierNumber == ""){
      supplierNumber = EXT010.get("EXSUNO").toString().trim()
    }
    if(customerNumber == ""){
      customerNumber = EXT010.get("EXCUNO").toString().trim()
    }
    if(manufacturer == ""){
      manufacturer = EXT010.get("EXSUN1").toString().trim()
    }
    if(CNUF == ""){
      CNUF = EXT010.get("EXSUN3").toString().trim()
    }
    if(country == ""){
      country = EXT010.get("EXCSCD").toString().trim()
    }
    if(HScode == ""){
      HScode = EXT010.get("EXCSNO").toString().trim()
    }
    if(IFLSfield.trim() == ""){
      IFLSfield = EXT010.get("EXHIE0").toString().trim()
    }
    if(mainIngredient == ""){
      mainIngredient = EXT010.get("EXSPE1").toString().trim()
    }
    if(contains == ""){
      contains = EXT010.get("EXSPE2").toString().trim()
    }
    if(brand == ""){
      brand = EXT010.get("EXCFI1").toString().trim()
    }
    if(controlCode == ""){
      controlCode = EXT010.get("EXCFI4").toString().trim()
    }
    if(PNM == ""){
      PNM = EXT010.get("EXCFI5").toString().trim()
    }
    if(origin == ""){
      origin = EXT010.get("EXCSC1").toString().trim()
    }
    if(dangerClass == ""){
      dangerClass = EXT010.get("EXZONU").toString().trim()
    }
    if(orderType == ""){
      orderType = EXT010.get("EXORTP").toString().trim()
    }
    if(itemType == ""){
      itemType = EXT010.get("EXITTY").toString().trim()
    }
    if(dangerIndicator == ""){
      dangerIndicator = EXT010.get("EXHAZI").toString().trim()
    }
    if(potentiallyDangerous == ""){
      potentiallyDangerous = EXT010.get("EXZPDA").toString().trim()
    }
    if(warehouse == ""){
      warehouse = EXT010.get("EXWHLO").toString().trim()
    }
    ZGKY = itemNumber + constrainingFeature + constraintLevel + supplierNumber + customerNumber + manufacturer + CNUF + country + HScode + IFLSfield + mainIngredient + brand + controlCode + PNM + origin + dangerClass + contains + orderType + itemType + dangerIndicator + potentiallyDangerous + warehouse
    logger.debug("ZGKY" + ZGKY)
  }
}
