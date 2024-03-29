/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT010MI.AddConstraint
 * Description : Add records to the EXT010 table.
 * Date         Changed By   Description
 * 20210219     RENARN       QUAX01 - Constraints matrix
 * 20211102     RENARN       Input parameters HAZI and ZPDA have been added
 * 20220228     RENARN       ZTPS has been added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddConstraint extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private final UtilityAPI utility
  private String NBNR
  private String ZGKY
  private Integer zblc
  private Integer ztps
  private test

  public AddConstraint(MIAPI mi, DatabaseAPI database, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.miCaller = miCaller
    this.utility = utility
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
    }
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

    // Check text id
    int txid = 0
    if(mi.in.get("TXID") != null){
      txid = mi.in.get("TXID")
      if(txid < 0){
        mi.error("Text ID " + mi.in.get("TXID") + " est invalide")
        return
      }
    }

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT010").index("30").build()
    DBContainer EXT010 = query.getContainer()
    EXT010.set("EXCONO", currentCompany)
    String itemNumber = ""
    if(mi.in.get("ITNO") != null){
      itemNumber = mi.in.get("ITNO")
    }
    EXT010.set("EXITNO", itemNumber)
    String constrainingFeature = ""
    if(mi.in.get("ZCFE") != null){
      constrainingFeature = mi.in.get("ZCFE")
    }
    EXT010.set("EXZCFE", constrainingFeature)
    String constraintLevel = ""
    if(mi.in.get("ZCLV") != null){
      constraintLevel = mi.in.get("ZCLV")
    }
    EXT010.set("EXZCLV", constraintLevel)
    String supplierNumber = ""
    if(mi.in.get("SUNO") != null){
      supplierNumber = mi.in.get("SUNO")
    }
    EXT010.set("EXSUNO", supplierNumber)
    String customerNumber = ""
    if(mi.in.get("CUNO") != null){
      customerNumber = mi.in.get("CUNO")
    }
    EXT010.set("EXCUNO", customerNumber)
    String manufacturer = ""
    if(mi.in.get("SUN1") != null){
      manufacturer = mi.in.get("SUN1")
    }
    EXT010.set("EXSUN1", manufacturer)
    String CNUF = ""
    if(mi.in.get("SUN3") != null){
      CNUF = mi.in.get("SUN3")
    }
    EXT010.set("EXSUN3", CNUF)
    String country = ""
    if(mi.in.get("CSCD") != null){
      country = mi.in.get("CSCD")
    }
    EXT010.set("EXCSCD", country)
    String HScode = ""
    if(mi.in.get("CSNO") != null){
      HScode = mi.in.get("CSNO")
    }
    EXT010.set("EXCSNO", HScode)
    String IFLSfield = ""
    if(mi.in.get("HIE0") != null){
      IFLSfield = mi.in.get("HIE0")
    }
    EXT010.set("EXHIE0", IFLSfield)
    String mainIngredient = ""
    if(mi.in.get("SPE1") != null){
      mainIngredient = mi.in.get("SPE1")
    }
    EXT010.set("EXSPE1", mainIngredient)
    String contains = ""
    if(mi.in.get("SPE2") != null){
      contains = mi.in.get("SPE2")
    }
    EXT010.set("EXSPE2", contains)
    /**
     String GLNcode = ""
     if(mi.in.get("SUCM") != null){
     GLNcode = mi.in.get("SUCM")
     }
     EXT010.set("EXSUCM", GLNcode)
     **/
    String brand = ""
    if(mi.in.get("CFI1") != null){
      brand = mi.in.get("CFI1")
    }
    EXT010.set("EXCFI1", brand)
    String controlCode = ""
    if(mi.in.get("CFI4") != null){
      controlCode = mi.in.get("CFI4")
    }
    EXT010.set("EXCFI4", controlCode)
    String PNM = ""
    if(mi.in.get("CFI5") != null){
      PNM = mi.in.get("CFI5")
    }
    EXT010.set("EXCFI5", PNM)
    int standardLifetime = 0
    if(mi.in.get("ZSLT") != null){
      standardLifetime = mi.in.get("ZSLT")
    }
    EXT010.set("EXZSLT", standardLifetime)
    percentageOfMandatoryLifetime = 0
    if(mi.in.get("ZPLT") != null){
      percentageOfMandatoryLifetime = mi.in.get("ZPLT")
    }
    EXT010.set("EXZPLT", percentageOfMandatoryLifetime)
    String origin = ""
    if(mi.in.get("CSC1") != null){
      origin = mi.in.get("CSC1")
    }
    EXT010.set("EXCSC1", origin)
    String dangerClass = ""
    if(mi.in.get("ZONU") != null){
      dangerClass = mi.in.get("ZONU")
    }
    EXT010.set("EXZONU", dangerClass)
    String orderType = ""
    if(mi.in.get("ORTP") != null){
      orderType = mi.in.get("ORTP")
    }
    EXT010.set("EXORTP", orderType)
    String itemType = ""
    if(mi.in.get("ITTY") != null){
      itemType = mi.in.get("ITTY")
    }
    EXT010.set("EXITTY", itemType)
    Integer dangerIndicator = 0
    if(mi.in.get("HAZI") != null){
      dangerIndicator = mi.in.get("HAZI") as Integer
    }
    EXT010.set("EXHAZI", dangerIndicator)
    Integer potentiallyDangerous = 0
    if(mi.in.get("ZPDA") != null){
      potentiallyDangerous = mi.in.get("ZPDA") as Integer
    }
    EXT010.set("EXZPDA", potentiallyDangerous)
    String warehouse = ""
    if(mi.in.get("WHLO") != null){
      warehouse = mi.in.get("WHLO")
    }
    EXT010.set("EXWHLO", warehouse)
    String documentID1 = ""
    if(mi.in.get("DO01") != null){
      documentID1 = mi.in.get("DO01")
    }
    EXT010.set("EXDO01", documentID1)
    String documentID2 = ""
    if(mi.in.get("DO02") != null){
      documentID2 = mi.in.get("DO02")
    }
    EXT010.set("EXDO02", documentID2)
    String documentID3 = ""
    if(mi.in.get("DO03") != null){
      documentID3 = mi.in.get("DO03")
    }
    EXT010.set("EXDO03", documentID3)
    String documentID4 = ""
    if(mi.in.get("DO04") != null){
      documentID4 = mi.in.get("DO04")
    }
    EXT010.set("EXDO04", documentID4)
    String documentID5 = ""
    if(mi.in.get("DO05") != null){
      documentID5 = mi.in.get("DO05")
    }
    EXT010.set("EXDO05", documentID5)
    String documentID6 = ""
    if(mi.in.get("DO06") != null){
      documentID6 = mi.in.get("DO06")
    }
    EXT010.set("EXDO06", documentID6)
    String documentID7 = ""
    if(mi.in.get("DO07") != null){
      documentID7 = mi.in.get("DO07")
    }
    EXT010.set("EXDO07", documentID7)
    String documentID8 = ""
    if(mi.in.get("DO08") != null){
      documentID8 = mi.in.get("DO08")
    }
    EXT010.set("EXDO08", documentID8)
    String documentID9 = ""
    if(mi.in.get("DO09") != null){
      documentID9 = mi.in.get("DO09")
    }
    EXT010.set("EXDO09", documentID9)
    String documentID10 = ""
    if(mi.in.get("DO10") != null){
      documentID10 = mi.in.get("DO10")
    }
    EXT010.set("EXDO10", documentID10)
    String documentID11 = ""
    if(mi.in.get("DO11") != null){
      documentID11 = mi.in.get("DO11")
    }
    EXT010.set("EXDO11", documentID11)
    String documentID12 = ""
    if(mi.in.get("DO12") != null){
      documentID12 = mi.in.get("DO12")
    }
    EXT010.set("EXDO12", documentID12)
    String documentID13 = ""
    if(mi.in.get("DO13") != null){
      documentID13 = mi.in.get("DO13")
    }
    EXT010.set("EXDO13", documentID13)
    String documentID14 = ""
    if(mi.in.get("DO14") != null){
      documentID14 = mi.in.get("DO14")
    }
    EXT010.set("EXDO14", documentID14)
    String documentID15 = ""
    if(mi.in.get("DO15") != null){
      documentID15 = mi.in.get("DO15")
    }
    EXT010.set("EXDO15", documentID15)
    txid = 0
    if(mi.in.get("TXID") != null){
      txid = mi.in.get("TXID")
    }
    ztps = 0
    if(mi.in.get("ZTPS") != null){
      ztps = mi.in.get("ZTPS")
    }
    EXT010.set("EXZBLC", zblc)
    EXT010.set("EXTXID", txid)
    EXT010.set("EXZTPS", ztps)
    EXT010.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    EXT010.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
    EXT010.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    EXT010.setInt("EXCHNO", 1)
    EXT010.set("EXCHID", program.getUser())
    ZGKY = itemNumber + constrainingFeature + constraintLevel + supplierNumber + customerNumber + manufacturer + CNUF + country + HScode + IFLSfield + mainIngredient + brand + controlCode + PNM + origin + dangerClass + contains + orderType + itemType + (dangerIndicator as String) + (potentiallyDangerous as String) + warehouse
    EXT010.set("EXZGKY", ZGKY)
    if(!query.readAll(EXT010, 2, outData)){
      // Retrieve constraint ID
      executeCRS165MIRtvNextNumber("ZA", "A")
      EXT010.set("EXZCID",  NBNR as Integer)
      String constraintID = EXT010.get("EXZCID")
      query.insert(EXT010)
      mi.outData.put("ZCID", constraintID)
      mi.write()
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }

  private executeCRS165MIRtvNextNumber(String NBTY, String NBID){
    def parameters = ["NBTY": NBTY, "NBID": NBID]
    Closure<?> handler = { Map<String, String> response ->
      NBNR = response.NBNR.trim()

      if (response.error != null) {
        return mi.error("Failed CRS165MI.RtvNextNumber: "+ response.errorMessage)
      }
    }
    miCaller.call("CRS165MI", "RtvNextNumber", parameters, handler)
  }

  Closure<?> outData = { DBContainer EXT012 ->
  }
}
