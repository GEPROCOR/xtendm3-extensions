/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT016MI.RtvConstrByOrd
 * Description : Retrieve constraints by order line and add records to the EXT015 table.
 * Date         Changed By   Description
 * 20210317     RENARN       QUAX07 - Constraints engine
 * 20211208     RENARN       New criterias and check added
 * 20220228     RENARN       ZTPS and ROUT has been added
 * 20230904     MAXLEC       Control suclIndicator instead of sucl
 * 20230904     MAXLEC      NEW FIELD OREF IN EXT015
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class RtvConstrByOrd extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final MICallerAPI miCaller
  private Integer currentCompany
  private String orderNumber
  private Integer orderLineNumber
  private Integer lineSuffix
  private Integer constraintLine = 0
  private String orderCategory
  private String customerNumber = ""
  private String itemNumber =""
  private String supplierNumber = ""
  private String supplierNumber_MPAPMA
  private String country = ""
  private String manufacturer = ""
  private String hsCode = ""
  private String iflsField = ""
  private String brand = ""
  private String pnm = ""
  private String itemType = ""
  private String orderType = ""
  private String oref = ""
  private String standardLifetime = 0
  private String dangerIndicator = 0
  private String potentiallyDangerous = "0"
  private String unit
  private Double orderQuantity
  private Double lineAmount
  private String lineStatus
  private String constrainingType
  private String constrainingFeature
  private String constraintLevel
  private String userID
  private String cnuf = ""
  private String poPpoProd = ""
  private Double savedMnfp
  private Double mpapmaMnfp
  private String controlCode = ""
  private String dangerClass = ""
  private int chb9 = 0
  private String currentDate
  private String ingredient1 = ""
  private String ingredient2 = ""
  private String ingredient3 = ""
  private String ingredient4 = ""
  private String ingredient5 = ""
  private String ingredient6 = ""
  private String ingredient7 = ""
  private String ingredient8 = ""
  private String ingredient9 = ""
  private String ingredient10 = ""
  private boolean ingredientFound
  private boolean ingredient1IsOK
  private String spe1
  private String suno
  private String sun1
  private String sun3
  private boolean supplierNumberIsOk
  private boolean manufacturerIsOk
  private boolean cnufIsOk
  private String status
  private Integer zcid
  private Integer existingZCSL
  private String listZCID
  private boolean constraintFound
  private String productOrigin = ""
  private String sucl
  private String warehouse
  private String hie3
  private String ext015Zcid
  private String savedZcid
  private boolean statusGreaterThan44
  private String route
  private boolean suclIndicator

  public RtvConstrByOrd(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.miCaller = miCaller
  }

  public void main() {
    currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    if (mi.in.get("PONR") != null) {
      DBAction oolineQuery = database.table("OOLINE").index("00").selection("OBORNO", "OBPONR", "OBPOSX", "OBCUNO", "OBITNO", "OBADID", "OBFACI", "OBWHLO", "OBORQT", "OBLNAM", "OBORST", "OBROUT", "OBCHID").build()
      DBContainer OOLINE = oolineQuery.getContainer()
      OOLINE.set("OBCONO", currentCompany)
      OOLINE.set("OBORNO", mi.in.get("ORNO"))
      OOLINE.set("OBPONR", mi.in.get("PONR"))
      OOLINE.set("OBPOSX", mi.in.get("POSX"))
      if (!oolineQuery.readAll(OOLINE, 4, outDataOiline)) {
        mi.error("L'enregistrement n'existe pas")
        return
      }
      if(statusGreaterThan44){
        mi.error("Ligne avec statut supérieur à 44 non prise en compte")
        return
      }
    } else {
      DBAction oolineQuery = database.table("OOLINE").index("00").selection("OBORNO", "OBPONR", "OBPOSX", "OBCUNO", "OBITNO", "OBADID", "OBFACI", "OBWHLO", "OBORQT", "OBLNAM", "OBORST", "OBROUT", "OBCHID").build()
      DBContainer OOLINE = oolineQuery.getContainer()
      OOLINE.set("OBCONO", currentCompany)
      OOLINE.set("OBORNO", mi.in.get("ORNO"))
      if (!oolineQuery.readAll(OOLINE, 2, outDataOiline)) {
        mi.error("L'enregistrement n'existe pas")
        return
      }
      if(statusGreaterThan44){
        mi.error("Ligne avec statut supérieur à 44 non prise en compte")
        return
      }
    }
  }
  Closure<?> outDataOiline = { DBContainer OOLINE ->
    orderNumber = OOLINE.get("OBORNO")
    orderLineNumber = OOLINE.get("OBPONR")
    lineSuffix = OOLINE.get("OBPOSX")
    orderCategory = "3"
    customerNumber = OOLINE.get("OBCUNO")
    itemNumber = OOLINE.get("OBITNO")
    orderQuantity = OOLINE.get("OBORQT")
    lineAmount = OOLINE.get("OBLNAM")
    lineStatus = OOLINE.get("OBORST")
    warehouse = OOLINE.get("OBWHLO")
    userID = OOLINE.get("OBCHID")
    route = OOLINE.get("OBROUT")
    constraintFound = false

    if(lineStatus > "44"){
      statusGreaterThan44 = true
      return
    }

    country = ""
    // Retrieve country
    executeOIS100MIGetAddress(orderNumber, "1")

    // Retrieve next constraint line available
    constraintLine = 0
    DBAction query = database.table("EXT015").index("00").reverse().build()
    DBContainer EXT015 = query.getContainer()
    EXT015.set("EXCONO", currentCompany)
    EXT015.set("EXORNO", orderNumber)
    EXT015.set("EXPONR", orderLineNumber)
    EXT015.set("EXPOSX", lineSuffix)
    if (!query.readAll(EXT015, 4, 1, outDataExt015)) {
    }
    //logger.debug("constraintLine = " + constraintLine)

    supplierNumber = ""
    poPpoProd = ""
    logger.debug("OrderNumber = " + orderNumber)
    logger.debug("OrderLineNumber = " + orderLineNumber)
    logger.debug("LineSuffix = " + lineSuffix)
    DBAction supplierNumberQuery = database.table("MPOPLP").index("90").selection("PORORN", "PORORL", "POSUNO", "POPROD").build()
    DBContainer MPOPLP = supplierNumberQuery.getContainer()
    MPOPLP.set("POCONO", currentCompany)
    MPOPLP.set("PORORN", orderNumber)
    MPOPLP.set("PORORL", orderLineNumber)
    MPOPLP.set("PORORX", lineSuffix)
    MPOPLP.set("PORORC", 3)
    if (supplierNumberQuery.readAll(MPOPLP, 5, outDataMpoplp)) {
    }
    //logger.debug("Après lecture MPOPLP supplierNumber = " + supplierNumber)

    if (supplierNumber.trim() == "") {
      //logger.debug("Recherche MPLINE avec orderNumber = " + orderNumber)
      //logger.debug("Recherche MPLINE avec orderLineNumber = " + orderLineNumber)
      //logger.debug("Recherche MPLINE avec lineSuffix = " + lineSuffix)
      DBAction supplierNumberQuery2 = database.table("MPLINE").index("20").selection("IBRORN", "IBRORL", "IBSUNO", "IBPROD").build()
      DBContainer MPLINE = supplierNumberQuery2.getContainer()
      MPLINE.set("IBCONO", currentCompany)
      MPLINE.set("IBRORN", orderNumber)
      MPLINE.set("IBRORL", orderLineNumber)
      MPLINE.set("IBRORX", lineSuffix)
      MPLINE.set("IBRORC", 3)
      if (supplierNumberQuery2.readAll(MPLINE, 5, outDataMpline)) {
      }
      //logger.debug("Après lecture MPLINE supplierNumber = " + supplierNumber)
    }

    retrieveInformations()

    hsCode = ""
    DBAction hsCodeQuery = database.table("MITFAC").index("00").selection("M9CSNO").build()
    DBContainer MITFAC = hsCodeQuery.getContainer()
    MITFAC.set("M9CONO", currentCompany)
    MITFAC.set("M9FACI", OOLINE.get("OBFACI"))
    MITFAC.set("M9ITNO", OOLINE.get("OBITNO"))
    if(hsCodeQuery.read(MITFAC)){
      hsCode = MITFAC.get("M9CSNO")
      logger.debug("hsCode 1 = " + hsCode)
    }

    iflsField = ""
    brand = ""
    pnm = ""
    controlCode = ""
    itemType = ""
    dangerIndicator = 0
    hie3 = ""
    DBAction mitmasQuery = database.table("MITMAS").index("00").selection("MMHIE5", "MMCFI1", "MMCFI4", "MMCFI5", "MMSPE1", "MMUNMS", "MMITTY", "MMHAZI", "MMHIE3").build()
    DBContainer MITMAS = mitmasQuery.getContainer()
    MITMAS.set("MMCONO", currentCompany)
    MITMAS.set("MMITNO", OOLINE.get("OBITNO"))
    if(mitmasQuery.read(MITMAS)){
      iflsField = MITMAS.get("MMHIE5")
      brand = MITMAS.get("MMCFI1")
      controlCode = MITMAS.get("MMCFI4")
      pnm = MITMAS.get("MMCFI5")
      unit = MITMAS.get("MMUNMS")
      itemType = MITMAS.get("MMITTY")
      dangerIndicator = MITMAS.get("MMHAZI")
      hie3 = MITMAS.get("MMHIE3")
    }

    if (dangerIndicator == "0") {
      chb9 = 0
      DBAction cugex1MitmasQuery = database.table("CUGEX1").index("00").selection("F1CHB9", "F1A330", "F1A530").build()
      DBContainer cugex1Mitmas = cugex1MitmasQuery.getContainer()
      cugex1Mitmas.set("F1CONO", currentCompany)
      cugex1Mitmas.set("F1FILE", "MITMAS")
      cugex1Mitmas.set("F1PK01", OOLINE.get("OBITNO"))
      cugex1Mitmas.set("F1PK02", "")
      cugex1Mitmas.set("F1PK03", "")
      cugex1Mitmas.set("F1PK04", "")
      cugex1Mitmas.set("F1PK05", "")
      cugex1Mitmas.set("F1PK06", "")
      cugex1Mitmas.set("F1PK07", "")
      cugex1Mitmas.set("F1PK08", "")
      if (cugex1MitmasQuery.read(cugex1Mitmas)) {
        chb9 = cugex1Mitmas.get("F1CHB9")
      }
    }
    //logger.debug("dangerIndicator = " + dangerIndicator)
    //logger.debug("chb9 = " + chb9)
    //logger.debug("hie3 = " + hie3)
    if (dangerIndicator == "0" && chb9 == 0) {
      DBAction cugex1MithryQuery = database.table("CUGEX1").index("00").selection("F1CHB9", "F1A330", "F1A530").build()
      DBContainer cugex1Mithry = cugex1MithryQuery.getContainer()
      cugex1Mithry.set("F1CONO", currentCompany)
      cugex1Mithry.set("F1FILE", "MITHRY")
      cugex1Mithry.set("F1PK01", "3")
      cugex1Mithry.set("F1PK02", hie3)
      cugex1Mithry.set("F1PK03", "")
      cugex1Mithry.set("F1PK04", "")
      cugex1Mithry.set("F1PK05", "")
      cugex1Mithry.set("F1PK06", "")
      cugex1Mithry.set("F1PK07", "")
      cugex1Mithry.set("F1PK08", "")
      if (cugex1MithryQuery.read(cugex1Mithry)) {
        //logger.debug("CUGEX1 MITHRY found")
        if (cugex1Mithry.get("F1A330").toString().trim() == "OUI") {
          potentiallyDangerous = "1"
          //logger.debug("potentiallyDangerous MITHRY")
        }
      }
    }
    if(dangerIndicator == "1" || chb9 == 1)
      potentiallyDangerous = "0"
    //logger.debug("potentiallyDangerous = " + potentiallyDangerous)
    orderType = ""
    oref = ""
    DBAction ooheadQuery = database.table("OOHEAD").index("00").selection("OAORTP").build()
    DBContainer OOHEAD = ooheadQuery.getContainer()
    OOHEAD.set("OACONO", currentCompany)
    OOHEAD.set("OAORNO", OOLINE.get("OBORNO"))
    if(ooheadQuery.read(OOHEAD)){
      orderType = OOHEAD.get("OAORTP")
      oref = OOHEAD.get("OAORTP")
    }
    //logger.debug("itemType = " + itemType)
    //logger.debug("orderType = " + orderType)

    dangerClass = ""
    DBAction cugex1MitvenQuery = database.table("CUGEX1").index("00").selection("F1CHB9", "F1A330", "F1A530").build()
    DBContainer cugex1Mitven = cugex1MitvenQuery.getContainer()
    cugex1Mitven.set("F1CONO", currentCompany)
    cugex1Mitven.set("F1FILE",  "MITVEN")
    cugex1Mitven.set("F1PK01",  OOLINE.get("OBITNO"))
    cugex1Mitven.set("F1PK02",  "")
    cugex1Mitven.set("F1PK03",  "")
    cugex1Mitven.set("F1PK04",  cnuf)
    cugex1Mitven.set("F1PK05",  "")
    cugex1Mitven.set("F1PK06",  "")
    cugex1Mitven.set("F1PK07",  "")
    cugex1Mitven.set("F1PK08",  "")
    if(cugex1MitvenQuery.read(cugex1Mitven)){
      if(cugex1Mitven.get("F1A530").toString().trim() != null && cugex1Mitven.get("F1A530").toString().trim() != "")
        dangerClass = cugex1Mitven.get("F1A530").toString().trim()
    }
    /**
     GLNcode = ""
     DBAction glnCodeQuery = database.table("CIDMAS").index("00").selection("IDSUCM").build()
     DBContainer CIDMAS = glnCodeQuery.getContainer()
     CIDMAS.set("IDCONO", currentCompany)
     CIDMAS.set("IDSUNO", cnuf)
     if(glnCodeQuery.read(CIDMAS)){
     GLNcode = CIDMAS.get("IDSUCM")
     }
     **/
    //logger.debug("EXT016MI: currentDate = " + currentDate)
    //logger.debug("EXT016MI: orderNumber = " + orderNumber)
    //logger.debug("EXT016MI: orderLineNumber = " + orderLineNumber)
    //logger.debug("EXT016MI: lineSuffix = " + lineSuffix)
    //logger.debug("EXT016MI: customerNumber = " + customerNumber)
    //logger.debug("EXT016MI: itemNumber = " + itemNumber)
    //logger.debug("EXT016MI: country = " + country)
    //logger.debug("EXT016MI: supplierNumber = " + supplierNumber)
    //logger.debug("EXT016MI: manufacturer = " + manufacturer)

    //logger.debug("EXT016MI: hsCode = " + hsCode)
    //logger.debug("EXT016MI: hsCode.substring(0,1) = " + hsCode.substring(0,1))
    //logger.debug("EXT016MI: hsCode.substring(0,2) = " + hsCode.substring(0,2))
    //logger.debug("EXT016MI: hsCode.substring(0,3) = " + hsCode.substring(0,3))
    //logger.debug("EXT016MI: hsCode.substring(0,4) = " + hsCode.substring(0,4))
    //logger.debug("EXT016MI: hsCode.substring(0,5) = " + hsCode.substring(0,5))

    //logger.debug("EXT016MI: iflsField = " + iflsField)
    //logger.debug("EXT016MI: brand = " + brand)
    //logger.debug("EXT016MI: pnm = " + pnm)
    //logger.debug("EXT016MI: standardLifetime = " + standardLifetime)
    //logger.debug("EXT016MI: controlCode = " + controlCode)
    //logger.debug("EXT016MI: dangerClass = " + dangerClass)
    //logger.debug("EXT016MI: supplierNumber = " + supplierNumber)
    //logger.debug("EXT016MI: manufacturer = " + manufacturer)
    //logger.debug("EXT016MI: cnuf = " + cnuf)
    ExpressionFactory expressionExt010 = database.getExpressionFactory("EXT010")
    //if(itemNumber != null){
    expressionExt010 = (expressionExt010.eq("EXITNO", itemNumber)).or(expressionExt010.eq("EXITNO", "*"))
    //}
    //if(supplierNumber != null){
    if(itemNumber == null){
      expressionExt010 = expressionExt010.ne("EXSUNO", "")
    } else {
      expressionExt010 = expressionExt010.and(expressionExt010.ne("EXSUNO", ""))
    }
    //}

    //if(customerNumber != null){
    if(itemNumber == null && supplierNumber == null){
      expressionExt010 = (expressionExt010.eq("EXCUNO", customerNumber)).or(expressionExt010.eq("EXCUNO", "*"))
    } else {
      expressionExt010 = expressionExt010.and((expressionExt010.eq("EXCUNO", customerNumber)).or(expressionExt010.eq("EXCUNO", "*")))
    }
    //}
    //if(manufacturer != null){
    if(itemNumber == null && supplierNumber == null && customerNumber == null){
      expressionExt010 = expressionExt010.ne("EXSUN1", "")
    } else {
      expressionExt010 = expressionExt010.and(expressionExt010.ne("EXSUN1", ""))
    }
    //}

    //if(country != null){
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null){
      expressionExt010 = (expressionExt010.eq("EXCSCD", country)).or(expressionExt010.eq("EXCSCD", "*"))
    } else {
      expressionExt010 = expressionExt010.and((expressionExt010.eq("EXCSCD", country)).or(expressionExt010.eq("EXCSCD", "*")))
    }
    //}
    logger.debug("hsCode 2 = " + hsCode)
    if(hsCode != null && hsCode.trim() != ""){
      logger.debug("hsCode 21 = " + hsCode)
      if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null){
        expressionExt010 = (expressionExt010.eq("EXCSNO", hsCode)).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,1)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,2)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,3)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,4)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,5)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,6)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,7)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,8)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,9)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,10)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,11)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,12)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,13)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,14)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,15)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,16)+"*")).or(expressionExt010.eq("EXCSNO", "*"))
      } else {
        expressionExt010 = expressionExt010.and((expressionExt010.eq("EXCSNO", hsCode)).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,1)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,2)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,3)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,4)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,5)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,6)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,7)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,8)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,9)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,10)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,11)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,12)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,13)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,14)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,15)+"*")).or(expressionExt010.eq("EXCSNO", hsCode.substring(0,16)+"*")).or(expressionExt010.eq("EXCSNO", "*")))
      }
    } else {
      logger.debug("hsCode 22 = " + hsCode)
      if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null){
        expressionExt010 = (expressionExt010.eq("EXCSNO", hsCode)).or(expressionExt010.eq("EXCSNO", "*"))
      } else {
        expressionExt010 = expressionExt010.and((expressionExt010.eq("EXCSNO", hsCode)).or(expressionExt010.eq("EXCSNO", "*")))
      }
    }

    if(iflsField != null && iflsField.trim() != ""){
      if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null){
        expressionExt010 = (expressionExt010.eq("EXHIE0", iflsField)).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,1)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,2)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,3)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,4)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,5)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,6)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,7)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,8)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,9)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,10)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,11)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,12)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,13)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,14)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,15)+"*")).or(expressionExt010.eq("EXHIE0", "*"))
      } else {
        expressionExt010 = expressionExt010.and((expressionExt010.eq("EXHIE0", iflsField)).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,1)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,2)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,3)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,4)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,5)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,6)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,7)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,8)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,9)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,10)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,11)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,12)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,13)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,14)+"*")).or(expressionExt010.eq("EXHIE0", iflsField.substring(0,15)+"*")).or(expressionExt010.eq("EXHIE0", "*")))
      }
    } else {
      if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null&& hsCode == null){
        expressionExt010 = (expressionExt010.eq("EXHIE0", iflsField)).or(expressionExt010.eq("EXHIE0", "*"))
      } else {
        expressionExt010 = expressionExt010.and((expressionExt010.eq("EXHIE0", iflsField)).or(expressionExt010.eq("EXHIE0", "*")))
      }
    }
    //if(ingredient1 != null){
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null){
      expressionExt010 = expressionExt010.ne("EXSPE1", "")
    } else {
      expressionExt010 = expressionExt010.and(expressionExt010.ne("EXSPE1", ""))
    }
    //}
    //if(ingredientFound){
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null){
      expressionExt010 = expressionExt010.eq("EXSPE2", ingredient2).or(expressionExt010.eq("EXSPE2", ingredient3)).or(expressionExt010.eq("EXSPE2", ingredient4)).or(expressionExt010.eq("EXSPE2", ingredient5)).or(expressionExt010.eq("EXSPE2", ingredient6)).or(expressionExt010.eq("EXSPE2", ingredient7)).or(expressionExt010.eq("EXSPE2", ingredient8)).or(expressionExt010.eq("EXSPE2", ingredient9)).or(expressionExt010.eq("EXSPE2", ingredient10)).or(expressionExt010.eq("EXSPE2", "*"))
    } else {
      expressionExt010 = expressionExt010.and((expressionExt010.eq("EXSPE2", ingredient2)).or(expressionExt010.eq("EXSPE2", ingredient3)).or(expressionExt010.eq("EXSPE2", ingredient4)).or(expressionExt010.eq("EXSPE2", ingredient5)).or(expressionExt010.eq("EXSPE2", ingredient6)).or(expressionExt010.eq("EXSPE2", ingredient7)).or(expressionExt010.eq("EXSPE2", ingredient8)).or(expressionExt010.eq("EXSPE2", ingredient9)).or(expressionExt010.eq("EXSPE2", ingredient10)).or(expressionExt010.eq("EXSPE2", "*")))
    }
    //}
    /**
     if(GLNcode != null){
     if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null && !ingredientFound){
     expressionExt010 = (expressionExt010.eq("EXSUCM", GLNcode)).or(expressionExt010.eq("EXSUCM", "*"))
     } else {
     expressionExt010 = expressionExt010.and((expressionExt010.eq("EXSUCM", GLNcode)).or(expressionExt010.eq("EXSUCM", "*")))
     }
     }
     **/

    //if(pnm != null){
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null && !ingredientFound/** && GLNcode == null**/){
      expressionExt010 = (expressionExt010.eq("EXCFI5", pnm)).or(expressionExt010.eq("EXCFI5", "*"))
    } else {
      expressionExt010 = expressionExt010.and((expressionExt010.eq("EXCFI5", pnm)).or(expressionExt010.eq("EXCFI5", "*")))
    }
    //}

    //if(brand != null){
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null && !ingredientFound/** && GLNcode == null**/ && pnm == null){
      expressionExt010 = (expressionExt010.eq("EXCFI1", brand)).or(expressionExt010.eq("EXCFI1", "*"))
    } else {
      expressionExt010 = expressionExt010.and((expressionExt010.eq("EXCFI1", brand)).or(expressionExt010.eq("EXCFI1", "*")))
    }
    //}
    //if(cnuf != null){
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null && !ingredientFound/** && GLNcode == null**/ && pnm == null && brand == null){
      expressionExt010 = expressionExt010.ne("EXSUN3", "")
    } else {
      expressionExt010 = expressionExt010.and(expressionExt010.ne("EXSUN3", ""))
    }
    //}
    //if(controlCode != null){
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null && !ingredientFound/** && GLNcode == null**/ && pnm == null && brand == null && cnuf == null){
      expressionExt010 = (expressionExt010.eq("EXCFI4", controlCode)).or(expressionExt010.eq("EXCFI4", "*"))
    } else {
      expressionExt010 = expressionExt010.and((expressionExt010.eq("EXCFI4", controlCode)).or(expressionExt010.eq("EXCFI4", "*")))
    }
    //}
    //if(dangerClass != null){
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null && !ingredientFound/** && GLNcode == null**/ && pnm == null && brand == null && cnuf == null && controlCode == null){
      expressionExt010 = expressionExt010.eq("EXZONU", dangerClass).or(expressionExt010.eq("EXZONU", "*"))
    } else {
      expressionExt010 = expressionExt010.and(expressionExt010.eq("EXZONU", dangerClass).or(expressionExt010.eq("EXZONU", "*")))
    }
    //}
    //if(productOrigin != null){
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null && !ingredientFound/** && GLNcode == null**/ && pnm == null && brand == null && cnuf == null && controlCode == null && dangerClass == null){
      expressionExt010 = (expressionExt010.eq("EXCSC1", productOrigin)).or(expressionExt010.eq("EXCSC1", "*"))
    } else {
      expressionExt010 = expressionExt010.and((expressionExt010.eq("EXCSC1", productOrigin)).or(expressionExt010.eq("EXCSC1", "*")))
    }
    //}
    //if(orderType != null){
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null && !ingredientFound/** && GLNcode == null**/ && pnm == null && brand == null && cnuf == null && controlCode == null && dangerClass == null && productOrigin == null){
      expressionExt010 = (expressionExt010.eq("EXORTP", orderType)).or(expressionExt010.eq("EXORTP", "*"))
    } else {
      expressionExt010 = expressionExt010.and((expressionExt010.eq("EXORTP", orderType)).or(expressionExt010.eq("EXORTP", "*")))
    }
    //}
    //if(itemType != null){
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null && !ingredientFound/** && GLNcode == null**/ && pnm == null && brand == null && cnuf == null && controlCode == null && dangerClass == null && productOrigin == null && orderType == null){
      expressionExt010 = (expressionExt010.eq("EXITTY", itemType)).or(expressionExt010.eq("EXITTY", "*"))
    } else {
      expressionExt010 = expressionExt010.and((expressionExt010.eq("EXITTY", itemType)).or(expressionExt010.eq("EXITTY", "*")))
    }
    //}
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null && !ingredientFound/** && GLNcode == null**/ && pnm == null && brand == null && cnuf == null && controlCode == null && dangerClass == null && productOrigin == null && orderType == null && itemType == null){
      expressionExt010 = (expressionExt010.gt("EXZSLT", standardLifetime as String)).or(expressionExt010.eq("EXZSLT", "0"))
    } else {
      expressionExt010 = expressionExt010.and((expressionExt010.gt("EXZSLT", standardLifetime as String)).or(expressionExt010.eq("EXZSLT", "0")))
    }
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null && !ingredientFound/** && GLNcode == null**/ && pnm == null && brand == null && cnuf == null && controlCode == null && dangerClass == null && productOrigin == null && orderType == null && itemType == null && standardLifetime == 0){
      expressionExt010 = expressionExt010.eq("EXHAZI", dangerIndicator as String).or(expressionExt010.eq("EXHAZI", "0"))
    } else {
      expressionExt010 = expressionExt010.and(expressionExt010.eq("EXHAZI", dangerIndicator as String).or(expressionExt010.eq("EXHAZI", "0")))
    }
    if(itemNumber == null && supplierNumber == null && customerNumber == null && manufacturer == null && country == null && hsCode == null && iflsField == null && !ingredientFound/** && GLNcode == null**/ && pnm == null && brand == null && cnuf == null && controlCode == null && dangerClass == null && productOrigin == null && orderType == null && itemType == null && standardLifetime == 0 && dangerIndicator == "0" ){
      expressionExt010 = expressionExt010.eq("EXZPDA", potentiallyDangerous as String).or(expressionExt010.eq("EXZPDA", "0"))
    } else {
      expressionExt010 = expressionExt010.and(expressionExt010.eq("EXZPDA", potentiallyDangerous as String).or(expressionExt010.eq("EXZPDA", "0")))
    }
    listZCID = ""
    DBAction ext010Query = database.table("EXT010").index("00").matching(expressionExt010).selection("EXZCID", "EXITNO", "EXZCFE", "EXZCLV", "EXSUNO", "EXCUNO", "EXSUN1", "EXSUN3", "EXCSCD", "EXCSNO", "EXHIE0", "EXSPE1", "EXSPE2",/**"EXSUCM",**/ "EXCFI1", "EXCFI4", "EXCFI5", "EXSUN3", "EXZPLT", "EXCSC1", "EXZONU", "EXORTP", "EXITTY", "EXHAZI", "EXZPDA", "EXDO01", "EXDO02", "EXDO03", "EXDO04", "EXDO05", "EXDO06", "EXDO07", "EXDO08", "EXDO09", "EXDO10", "EXDO11", "EXDO12", "EXDO13", "EXDO14", "EXDO15", "EXTXID", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT010 = ext010Query.getContainer()
    EXT010.set("EXCONO", currentCompany)
    if(!ext010Query.readAll(EXT010, 1, outDataExt010)){
      // If no constraint was found, the order line is still added in EXT015
      addEmptyRecord()
    }
    logger.debug("listZCID = " + listZCID)
    EXT015.set("EXCONO", currentCompany)
    EXT015.set("EXORNO", orderNumber)
    EXT015.set("EXPONR", orderLineNumber)
    EXT015.set("EXPOSX", lineSuffix)
    if(!query.readAllLock(EXT015, 4, updateCallBack2)){}
  }
  Closure<?> outDataMpoplp = { DBContainer MPOPLP ->
    supplierNumber = MPOPLP.get("POSUNO")
    poPpoProd = MPOPLP.get("POPROD")
  }
  Closure<?> outDataMpline = { DBContainer MPLINE ->
    supplierNumber = MPLINE.get("IBSUNO")
    poPpoProd = MPLINE.get("IBPROD")
  }
  Closure<?> outData_MPAPMA1 = { DBContainer MPAPMA1 ->
    //logger.debug("MPAPMA1 trouvé PRIO = " + MPAPMA1.get("AMPRIO"))
    //logger.debug("MPAPMA1 trouvé OBV1 = " + MPAPMA1.get("AMOBV1"))
    //logger.debug("MPAPMA1 trouvé OBV2 = " + MPAPMA1.get("AMOBV2"))
    //logger.debug("MPAPMA1 trouvé OBV3 = " + MPAPMA1.get("AMOBV3"))
    mpapmaMnfp = MPAPMA1.get("AMMNFP")
    if(mpapmaMnfp < savedMnfp){
      savedMnfp = MPAPMA1.get("AMMNFP")
      cnuf = MPAPMA1.get("AMPROD")
    }
  }
  Closure<?> outDataMpapma2 = { DBContainer MPAPMA2 ->
    //logger.debug("MPAPMA2 trouvé PRIO = " + MPAPMA2.get("AMPRIO"))
    //logger.debug("MPAPMA2 trouvé OBV1 = " + MPAPMA2.get("AMOBV1"))
    //logger.debug("MPAPMA2 trouvé OBV2 = " + MPAPMA2.get("AMOBV2"))
    //logger.debug("MPAPMA2 trouvé OBV3 = " + MPAPMA2.get("AMOBV3"))
    mpapmaMnfp = MPAPMA2.get("AMMNFP")
    if(mpapmaMnfp < savedMnfp){
      savedMnfp = MPAPMA2.get("AMMNFP")
      manufacturer = MPAPMA2.get("AMPROD")
    }
    String PRIO = MPAPMA2.get("AMPRIO")
    String FDAT = MPAPMA2.get("AMFDAT")
    DBAction query = database.table("CUGEX1").index("00").selection("F1A030", "F1A130", "F1A230", "F1A330", "F1A430", "F1A530", "F1A630", "F1A730", "F1A830", "F1A930").build()
    DBContainer CUGEX1 = query.getContainer()
    CUGEX1.set("F1CONO", currentCompany)
    CUGEX1.set("F1FILE",  "MITVEN")
    CUGEX1.set("F1PK01",  itemNumber)
    CUGEX1.set("F1PK02",  "")
    CUGEX1.set("F1PK03",  "")
    CUGEX1.set("F1PK04",  MPAPMA2.get("AMPROD"))
    CUGEX1.set("F1PK05",  "")
    CUGEX1.set("F1PK06",  "")
    CUGEX1.set("F1PK07",  "")
    CUGEX1.set("F1PK08",  "")
    if(query.read(CUGEX1)){
      ingredient1 = CUGEX1.get("F1A030")
      ingredient2 = CUGEX1.get("F1A130")
      ingredient3 = CUGEX1.get("F1A230")
      ingredient4 = CUGEX1.get("F1A330")
      ingredient5 = CUGEX1.get("F1A430")
      ingredient6 = CUGEX1.get("F1A530")
      ingredient7 = CUGEX1.get("F1A630")
      ingredient8 = CUGEX1.get("F1A730")
      ingredient9 = CUGEX1.get("F1A830")
      ingredient10 = CUGEX1.get("F1A930")
      if(ingredient2.trim() != "" || ingredient3.trim() != "" || ingredient4.trim() != "" || ingredient5.trim() != "" || ingredient6.trim() != "" || ingredient7.trim() != "" || ingredient8.trim() != "" || ingredient9.trim() != "" || ingredient10.trim() != "") {
        ingredientFound = true
      }
    }
  }
  Closure<?> outDataMitven = { DBContainer MITVEN ->
    //logger.debug("MITVEN trouvé")
    productOrigin = MITVEN.get("IFORCO")
    //logger.debug("productOrigin = " + productOrigin)
    DBAction query = database.table("CUGEX1").index("00").selection("F1N196").build()
    DBContainer CUGEX1 = query.getContainer()
    CUGEX1.set("F1CONO", currentCompany)
    CUGEX1.set("F1FILE",  "MITVEN")
    CUGEX1.set("F1PK01",  MITVEN.get("IFITNO"))
    CUGEX1.set("F1PK02",  "")
    CUGEX1.set("F1PK03",  "")
    CUGEX1.set("F1PK04",  MITVEN.get("IFSUNO"))
    CUGEX1.set("F1PK05",  "")
    CUGEX1.set("F1PK06",  "")
    CUGEX1.set("F1PK07",  "")
    CUGEX1.set("F1PK08",  "")
    if(query.read(CUGEX1)){
      standardLifetime = CUGEX1.get("F1N196") as Integer
    }
  }
  Closure<?> outDataExt010 = { DBContainer EXT010 ->
    logger.debug("Contrainte trouvée = " + EXT010.get("EXZCID"))
    //logger.debug("checkSupplierNumber")
    //if(supplierNumber != null) {
    suno = EXT010.get("EXSUNO")
    //logger.debug("suno = " + suno)
    checkSupplierNumber()
    if(!supplierNumberIsOk)
      return // Constraint is ignored
    //}
    //logger.debug("checkSupplierNumber is OK")
    //logger.debug("checkManufacturer")
    //if(manufacturer != null) {
    sun1 = EXT010.get("EXSUN1")
    //logger.debug("sun1 = " + sun1)
    checkManufacturer()
    if(!manufacturerIsOk)
      return // Constraint is ignored
    //}
    //logger.debug("checkManufacturer is OK")
    //logger.debug("checkcnuf" + cnuf)
    //logger.debug("checkcnuf EXT010/SUN3" + EXT010.get("EXSUN3"))
    //if(cnuf != null) {
    sun3 = EXT010.get("EXSUN3")
    //logger.debug("sun3 = " + sun3)
    checkcnuf()
    if(!cnufIsOk)
      return // Constraint is ignored
    //}
    //logger.debug("checkcnuf is OK")
    //logger.debug("checkIngredient1")
    //if(ingredient1 != null) {
    spe1 = EXT010.get("EXSPE1")
    checkIngredient1()
    if(!ingredient1IsOK)
      return // Constraint is ignored
    //}
    //logger.debug("checkIngredient1 is OK")
    //logger.debug("spe1 = " + spe1)
    constraintFound = true
    //logger.debug("Contrainte ajoutée = " + EXT010.get("EXZCID"))
    zcid = EXT010.get("EXZCID")
    if(listZCID == ""){
      listZCID = "("+zcid+")"
    } else {
      listZCID = listZCID + "|" + "("+zcid+")"
    }
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer) program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    checkExistingConstraint()

    // The constraint is created in EXT015 if and only if it does not exist or only exists in status 90
    // Otherwise it is updated
    if(status == "" || status == "90"){
      LocalDateTime timeOfCreation = LocalDateTime.now()
      DBAction query = database.table("EXT015").index("00").build()
      constraintLine = constraintLine + 1
      DBContainer EXT015 = query.getContainer()
      EXT015.set("EXCONO", currentCompany)
      EXT015.set("EXORNO", orderNumber)
      EXT015.set("EXPONR", orderLineNumber)
      EXT015.set("EXPOSX", lineSuffix)
      EXT015.set("EXZCSL", constraintLine)
      if (!query.read(EXT015)) {
        EXT015.set("EXCUNO", customerNumber)
        EXT015.set("EXITNO", itemNumber)
        EXT015.set("EXORQT", orderQuantity)
        EXT015.set("EXUNMS", unit)
        EXT015.set("EXLNAM", lineAmount)
        EXT015.set("EXORST", lineStatus)
        Integer constraintID = EXT010.get("EXZCID")
        EXT015.set("EXZCID", constraintID)

        constrainingType = ""
        DBAction EXT012_query = database.table("EXT012").index("00").selection("EXZCTY").build()
        DBContainer EXT012 = EXT012_query.getContainer()
        EXT012.set("EXCONO", currentCompany)
        EXT012.set("EXZCLV", EXT010.get("EXZCLV"))
        if (EXT012_query.read(EXT012)) {
          constrainingType = EXT012.get("EXZCTY")
        }
        EXT015.set("EXZCTY", constrainingType)

        constrainingFeature = EXT010.get("EXZCFE")
        EXT015.set("EXZCFE", constrainingFeature)
        constraintLevel = EXT010.get("EXZCLV")
        EXT015.set("EXZCLV", constraintLevel)
        status = "20"
        EXT015.set("EXSTAT", status)
        String documentID1 = EXT010.get("EXDO01")
        EXT015.set("EXDO01", documentID1)
        String documentID2 = EXT010.get("EXDO02")
        EXT015.set("EXDO02", documentID2)
        String documentID3 = EXT010.get("EXDO03")
        EXT015.set("EXDO03", documentID3)
        String documentID4 = EXT010.get("EXDO04")
        EXT015.set("EXDO04", documentID4)
        String documentID5 = EXT010.get("EXDO05")
        EXT015.set("EXDO05", documentID5)
        String documentID6 = EXT010.get("EXDO06")
        EXT015.set("EXDO06", documentID6)
        String documentID7 = EXT010.get("EXDO07")
        EXT015.set("EXDO07", documentID7)
        String documentID8 = EXT010.get("EXDO08")
        EXT015.set("EXDO08", documentID8)
        String documentID9 = EXT010.get("EXDO09")
        EXT015.set("EXDO09", documentID9)
        String documentID10 = EXT010.get("EXDO10")
        EXT015.set("EXDO10", documentID10)
        String documentID11 = EXT010.get("EXDO11")
        EXT015.set("EXDO11", documentID11)
        String documentID12 = EXT010.get("EXDO12")
        EXT015.set("EXDO12", documentID12)
        String documentID13 = EXT010.get("EXDO13")
        EXT015.set("EXDO13", documentID13)
        String documentID14 = EXT010.get("EXDO14")
        EXT015.set("EXDO14", documentID14)
        String documentID15 = EXT010.get("EXDO15")
        EXT015.set("EXDO15", documentID15)
        Integer textID1 = EXT010.get("EXTXID")
        EXT015.set("EXTXI1", textID1)
        EXT015.set("EXROUT", route)
        EXT015.set("EXOREF", oref)
        EXT015.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT015.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        EXT015.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT015.setInt("EXCHNO", 1)
        EXT015.set("EXCHID", userID)
        query.insert(EXT015)
        //logger.debug("Contrainte créée = " + constraintID)
      }
    } else {
      DBAction query = database.table("EXT015").index("00").build()
      DBContainer EXT015 = query.getContainer()
      EXT015.set("EXCONO", currentCompany)
      EXT015.set("EXORNO", orderNumber)
      EXT015.set("EXPONR", orderLineNumber)
      EXT015.set("EXPOSX", lineSuffix)
      EXT015.set("EXZCSL", existingZCSL)
      if(!query.readLock(EXT015, updateCallBack)){}
    }
  }
  private executeOIS100MIGetAddress(String ORNO, String ADRT){
    Map parameters = ["ORNO": ORNO, "ADRT": ADRT]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
      } else {
        country = response.CSCD.trim()
      }
    }
    miCaller.call("OIS100MI", "GetAddress", parameters, handler)
  }

  // Adds a record with empty values
  public void addEmptyRecord(){
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT015").index("00").build()
    zcid = 0
    checkExistingConstraint()
    // The constraint is created in EXT015 if and only if it does not exist or only exists in status 90
    if(status == "" || status == "90") {
      constraintLine = constraintLine + 1
      status = "20"
      DBContainer EXT015 = query.getContainer()
      EXT015.set("EXCONO", currentCompany)
      EXT015.set("EXORNO", orderNumber)
      EXT015.set("EXPONR", orderLineNumber)
      EXT015.set("EXPOSX", lineSuffix)
      EXT015.set("EXZCSL", constraintLine)
      if (!query.read(EXT015)) {
        EXT015.set("EXCUNO", customerNumber)
        EXT015.set("EXITNO", itemNumber)
        EXT015.set("EXORQT", orderQuantity)
        EXT015.set("EXUNMS", unit)
        EXT015.set("EXLNAM", lineAmount)
        EXT015.set("EXORST", lineStatus)
        EXT015.set("EXSTAT", status)
        EXT015.set("EXROUT", route)
        EXT015.set("EXOREF", oref)
        EXT015.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT015.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        EXT015.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT015.setInt("EXCHNO", 1)
        EXT015.set("EXCHID", userID)
        query.insert(EXT015)
      }
    }
  }
  Closure<?> outDataExt015 = { DBContainer EXT015 ->
    constraintLine = EXT015.get("EXZCSL")
  }
  // Checks if the ingredient is valid for selection
  public void checkIngredient1(){
    //logger.debug("spe1 = " + spe1)
    //logger.debug("ingredient1 = " + ingredient1)
    if(ingredient1.trim() == spe1.trim() ){
      //logger.debug("ingredient1 = spe1 is OK")
      ingredient1IsOK = true
      return
    }
    if(spe1.trim() == "*"){
      //logger.debug("* is OK")
      ingredient1IsOK = true
      return
    }
    if(ingredient1.trim()  == "" || ingredient1.trim()  == null){
      //logger.debug("ingredient1 not found is KO")
      ingredient1IsOK = false
      return
    }
    Integer countStars = spe1.count("*")
    if(countStars > 0)
      spe1 = spe1.substring(1, spe1.length())
    ingredient1IsOK = false
    //logger.debug("countStars = " + countStars)
    String String1 = ""
    String String2 = ""
    String String3 = ""
    if(countStars >= 2){
      String1 = spe1.substring(0, spe1.indexOf("*"))
      //logger.debug("String1 = " + String1)
      if(ingredient1.contains(String1)) {
        ingredient1IsOK = true
        //logger.debug("String1 is OK")
      } else {
        ingredient1IsOK = false
        //logger.debug("String1 is KO")
      }
    }
    if(countStars >= 3){
      spe1 = spe1.substring(spe1.indexOf("*")+1, spe1.length())
      String2 = spe1.substring(0, spe1.indexOf("*"))
      //logger.debug("String2 = " + String2)
      if(ingredient1.contains(String1) && ingredient1.contains(String2)) {
        ingredient1IsOK = true
        //logger.debug("String2 is OK")
      } else {
        ingredient1IsOK = false
        //logger.debug("String2 is KO")
      }
    }
    if(countStars >= 4){
      spe1 = spe1.substring(spe1.indexOf("*")+1, spe1.length())
      String3 = spe1.substring(0, spe1.indexOf("*"))
      //logger.debug("String3 = " + String3)
      if(ingredient1.contains(String1) && ingredient1.contains(String2) && ingredient1.contains(String3)) {
        ingredient1IsOK = true
        //logger.debug("String3 is OK")
      } else {
        ingredient1IsOK = false
        //logger.debug("String3 is KO")
      }
    }
    //logger.debug("ingredient1IsOK = " + ingredient1IsOK)
  }

  // Checks if the supplier is valid for selection
  public void checkSupplierNumber(){
    supplierNumberIsOk = false
    //logger.debug("Check supplierNumber suno = " + suno)
    //logger.debug("Check supplierNumber supplierNumber = " + supplierNumber)
    if(suno != "" && suno.trim() != "*" && supplierNumber != "" && supplierNumber != null) {
      //logger.debug("Check supplierNumber Etape 1")
      if (supplierNumber == suno) {
        //logger.debug("Check supplierNumber Etape 2")
        supplierNumberIsOk = true
      } else {
        //logger.debug("Check supplierNumber Etape 3")
        if(supplierNumber != "") {
          int i
          for (i = 1; i <= suno.length() && !supplierNumberIsOk; i++) {
            if ((supplierNumber.substring(0, i) + "*") == suno.trim())
              supplierNumberIsOk = true
          }
          //logger.debug("Check supplierNumber Etape 4 supplierNumberIsOk = " + supplierNumberIsOk)
        } else {
          supplierNumberIsOk = false
        }
      }
    } else {
      //logger.debug("Check supplierNumber Etape 5")
      if(suno.trim() == "")
        supplierNumberIsOk = false
      if(suno.trim() == "*")
        supplierNumberIsOk = true
      //logger.debug("Check supplierNumber Etape 6 supplierNumberIsOk = " + supplierNumberIsOk)
    }
  }
  // Checks if the manufacturer is valid for selection
  public void checkManufacturer(){
    manufacturerIsOk = false
    //logger.debug("Check manufacturer sun1 = " + sun1)
    //logger.debug("Check manufacturer manufacturer = " + manufacturer)
    if(sun1 != "" && sun1.trim() != "*" && manufacturer != "" && manufacturer != null) {
      //logger.debug("Check manufacturer Etape 1")
      if (manufacturer == sun1) {
        //logger.debug("Check manufacturer Etape 2")
        manufacturerIsOk = true
      } else {
        //logger.debug("Check manufacturer Etape 3")
        if(manufacturer != "") {
          int i
          for (i = 1; i <= sun1.length() && !manufacturerIsOk; i++) {
            if ((manufacturer.substring(0, i) + "*") == sun1.trim())
              manufacturerIsOk = true
          }
          //logger.debug("Check manufacturer Etape 4 manufacturerIsOk = " + manufacturerIsOk)
        } else {
          manufacturerIsOk = false
        }
      }
    } else {
      //logger.debug("Check manufacturer Etape 5")
      if(sun1.trim() == "")
        manufacturerIsOk = false
      if(sun1.trim() == "*")
        manufacturerIsOk = true
      //logger.debug("Check manufacturer Etape 6 manufacturerIsOk = " + manufacturerIsOk)
    }
  }
  // Checks if the cnuf is valid for selection
  public void checkcnuf(){
    cnufIsOk = false
    //logger.debug("Check cnuf sun3 = " + sun3)
    //logger.debug("Check cnuf cnuf = " + cnuf)
    if(sun3 != "" && sun3.trim() != "*" && cnuf != "" && cnuf != null) {
      //logger.debug("Check cnuf Etape 1")
      if (cnuf == sun3) {
        //logger.debug("Check cnuf Etape 2")
        cnufIsOk = true
      } else {
        //logger.debug("Check cnuf Etape 3")
        if(cnuf != "") {
          int i
          for (i = 1; i <= sun3.length() && !cnufIsOk; i++) {
            if ((cnuf.substring(0, i) + "*") == sun3.trim())
              cnufIsOk = true
          }
          //logger.debug("Check cnuf Etape 4 cnufIsOk = " + cnufIsOk)
        } else {
          cnufIsOk = false
        }
      }
    } else {
      //logger.debug("Check cnuf Etape 5")
      if(sun3.trim() == "")
        cnufIsOk = false
      if(sun3.trim() == "*")
        cnufIsOk = true
      //logger.debug("Check cnuf Etape 6 cnufIsOk = " + cnufIsOk)
    }
  }
  // Checks existing constraints
  public void checkExistingConstraint(){
    status = ""
    existingZCSL = 0
    DBAction query = database.table("EXT015").index("40").build()
    DBContainer EXT015 = query.getContainer()
    EXT015.set("EXCONO", currentCompany)
    EXT015.set("EXORNO", orderNumber)
    EXT015.set("EXPONR", orderLineNumber)
    EXT015.set("EXPOSX", lineSuffix)
    EXT015.set("EXZCID", zcid)
    EXT015.set("EXSTAT", "20")
    if (!query.readAll(EXT015, 6, outDataExt0152)) {}
    if(status == "") {
      EXT015.set("EXSTAT", "30")
      if (!query.readAll(EXT015, 6, outDataExt0152)) {}
    }
    if(status == "") {
      EXT015.set("EXSTAT", "40")
      if (!query.readAll(EXT015, 6, outDataExt0152)) {
      }
    }
    if(status == "") {
      EXT015.set("EXSTAT", "50")
      if (!query.readAll(EXT015, 6, outDataExt0152)) {
      }
    }
    if(status == "") {
      EXT015.set("EXSTAT", "60")
      if (!query.readAll(EXT015, 6, outDataExt0152)) {
      }
    }
    if(status == "") {
      EXT015.set("EXSTAT", "70")
      if (!query.readAll(EXT015, 6, outDataExt0152)) {
      }
    }
    if(status == "") {
      EXT015.set("EXSTAT", "80")
      if (!query.readAll(EXT015, 6, outDataExt0152)) {
      }
    }
    if(status == "") {
      EXT015.set("EXSTAT", "90")
      if (!query.readAll(EXT015, 6, outDataExt0152)) {
      }
    }
  }
  Closure<?> outDataExt0152 = { DBContainer EXT015 ->
    status = EXT015.get("EXSTAT")
    existingZCSL = EXT015.get("EXZCSL")
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.set("EXCUNO", customerNumber)
    lockedResult.set("EXITNO", itemNumber)
    lockedResult.set("EXORQT", orderQuantity)
    lockedResult.set("EXUNMS", unit)
    lockedResult.set("EXLNAM", lineAmount)
    lockedResult.set("EXORST", lineStatus)
    lockedResult.set("EXROUT", route)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  Closure<?> updateCallBack2 = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    ext015Zcid = lockedResult.get("EXZCID")
    savedZcid = "("+ext015Zcid.trim()+")"
    logger.debug("EXZCID trouvée = " + savedZcid)
    //logger.debug("constraintFound = " + constraintFound)
    //logger.debug("test = " + (lockedResult.get("EXZCID") != 0 && !listZCID.contains(lockedResult.get("EXZCID") as String) || lockedResult.get("EXZCID") == 0 && constraintFound))
    if (lockedResult.get("EXZCID") != 0 && !listZCID.contains(savedZcid) || lockedResult.get("EXZCID") == 0 && constraintFound) {
      //logger.debug("Màj stt 90")
      logger.debug("EXZCID désactivée = " + lockedResult.get("EXZCID"))
      lockedResult.set("EXSTAT", "90")
      lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      lockedResult.setInt("EXCHNO", changeNumber + 1)
      lockedResult.set("EXCHID", program.getUser())
    }
    lockedResult.update()
  }
  // Retrieve supplier, cnuf, manufacturer and ingredients
  public void retrieveInformations(){
    cnuf = ""
    manufacturer = ""
    ingredient1 = ""
    ingredient2 = ""
    ingredient3 = ""
    ingredient4 = ""
    ingredient5 = ""
    ingredient6 = ""
    ingredient7 = ""
    ingredient8 = ""
    ingredient9 = ""
    ingredient10 = ""
    ingredientFound = false
    productOrigin = ""
    standardLifetime  = 0
    //logger.debug("retrieveInformations")
    // Supplier has been retrieved from purchase order or planned purchase order
    sucl = ""
    suclIndicator = false
    if(supplierNumber != ""){
      DBAction cidvenQuery = database.table("CIDVEN").index("00").selection("IISUCL").build()
      DBContainer CIDVEN = cidvenQuery.getContainer()
      CIDVEN.set("IICONO", currentCompany)
      CIDVEN.set("IISUNO", supplierNumber)
      if (cidvenQuery.read(CIDVEN)) {
        sucl = CIDVEN.get("IISUCL")
        checkPoPpoSupplier()
      }
    } else {
      // Supplier was not found in PO/PPO, it must be retrieved from item warehouse
      DBAction mitbalQuery = database.table("MITBAL").index("00").selection("MBSUNO").build()
      DBContainer MITBAL = mitbalQuery.getContainer()
      MITBAL.set("MBCONO", currentCompany)
      MITBAL.set("MBWHLO", warehouse)
      MITBAL.set("MBITNO", itemNumber)
      if (mitbalQuery.read(MITBAL)) {
        supplierNumber = MITBAL.get("MBSUNO")
        DBAction cidvenQuery = database.table("CIDVEN").index("00").selection("IISUCL").build()
        DBContainer CIDVEN = cidvenQuery.getContainer()
        CIDVEN.set("IICONO", currentCompany)
        CIDVEN.set("IISUNO", supplierNumber)
        if (cidvenQuery.read(CIDVEN)) {
          sucl = CIDVEN.get("IISUCL")
          checkItemWarehouseSupplier()
        }
      }
    }
  }
  // Retrieve informations from supplier that has been retrieved from purchase order or planned purchase order
  public void checkPoPpoSupplier(){
    //logger.debug("checkPoPpoSupplier")
    //logger.debug("checkItemWarehouseSupplier - sucl = " + sucl)
    LocalDateTime timeOfCreation = LocalDateTime.now()
    currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    suclIndicator = getSUCLIndicator(sucl)
    // Supplier base
    if(!suclIndicator){ //SUCL=100
      cnuf = poPpoProd
      //logger.debug("checkPoPpoSupplier - cnuf is PROD = " + cnuf)
      // Retrieve manufacturer and ingredients
      savedMnfp = 10
      ExpressionFactory expressionMpapma2 = database.getExpressionFactory("MPAPMA")
      expressionMpapma2 = expressionMpapma2.eq("AMMFRS", "20")
      expressionMpapma2 = expressionMpapma2.and(expressionMpapma2.le("AMFDAT", currentDate))
      expressionMpapma2 = expressionMpapma2.and((expressionMpapma2.ge("AMTDAT", currentDate)).or(expressionMpapma2.eq("AMTDAT", "0")))
      expressionMpapma2 = expressionMpapma2.and(expressionMpapma2.eq("AMOBV3", supplierNumber))
      expressionMpapma2 = expressionMpapma2.and(expressionMpapma2.eq("AMOBV4", cnuf))
      DBAction manufacturerQuery = database.table("MPAPMA").index("00").matching(expressionMpapma2).selection("AMPRIO", "OBV1", "OBV2", "OBV3", "OBV4", "OBV5", "AMFDAT", "AMPROD", "AMMNFP").build()
      DBContainer MPAPMA2 = manufacturerQuery.getContainer()
      MPAPMA2.set("AMCONO", currentCompany)
      MPAPMA2.set("AMPRIO", 2)
      MPAPMA2.set("AMOBV1", itemNumber)
      if(manufacturerQuery.readAll(MPAPMA2, 3, outDataMpapma2)){
      }
      //logger.debug("checkPoPpoSupplier - manufacturer = " + manufacturer)
      //logger.debug("checkPoPpoSupplier - ingredient1 = " + ingredient1)
      //logger.debug("checkPoPpoSupplier - ingredient2 = " + ingredient2)
      //logger.debug("checkPoPpoSupplier - ingredient3 = " + ingredient3)
      //logger.debug("checkPoPpoSupplier - ingredient4 = " + ingredient4)
      //logger.debug("checkPoPpoSupplier - ingredient5 = " + ingredient5)
      //logger.debug("checkPoPpoSupplier - ingredient6 = " + ingredient6)
      //logger.debug("checkPoPpoSupplier - ingredient7 = " + ingredient7)
      //logger.debug("checkPoPpoSupplier - ingredient8 = " + ingredient8)
      //logger.debug("checkPoPpoSupplier - ingredient9 = " + ingredient9)
      //logger.debug("checkPoPpoSupplier - ingredient10 = " + ingredient10)
      //logger.debug("checkItemWarehouseSupplier - ingredientFound = " + ingredientFound)
      // Retrieve product origin
      DBAction mitvenQuery = database.table("MITVEN").index("10").selection("IFORCO").build()
      DBContainer MITVEN = mitvenQuery.getContainer()
      MITVEN.set("IFCONO", currentCompany)
      MITVEN.set("IFSUNO", cnuf)
      MITVEN.set("IFITNO", itemNumber)
      if(mitvenQuery.readAll(MITVEN, 3, outDataMitven)){
      }
      //logger.debug("checkPoPpoSupplier - productOrigin = " + productOrigin)
      //logger.debug("checkPoPpoSupplier - standardLifetime = " + standardLifetime)
      potentiallyDangerous = "0"
      DBAction cidmasQuery = database.table("CIDMAS").index("00").selection("IDCFI3").build()
      DBContainer CIDMAS = cidmasQuery.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO", poPpoProd)
      if(cidmasQuery.read(CIDMAS)){
        //logger.debug("CIDMAS checkPoPpoSupplier/SUCL = 100 found")
        if (CIDMAS.get("IDCFI3").toString().trim() == "OUI"){
          potentiallyDangerous = "1"
          //logger.debug("potentiallyDangerous checkPoPpoSupplier/SUCL = 100")
        }
      }
    }
    // cnuf
    if(suclIndicator){ //SUCL=200
      cnuf = ""   // In this case, supplierNumber is the cnuf
      //logger.debug("checkPoPpoSupplier - cnuf is blank = " + cnuf)
      // Retrieve manufacturer and ingredients
      savedMnfp = 10
      ExpressionFactory expressionMpapma2 = database.getExpressionFactory("MPAPMA")
      expressionMpapma2 = expressionMpapma2.eq("AMMFRS", "20")
      expressionMpapma2 = expressionMpapma2.and(expressionMpapma2.le("AMFDAT", currentDate))
      expressionMpapma2 = expressionMpapma2.and((expressionMpapma2.ge("AMTDAT", currentDate)).or(expressionMpapma2.eq("AMTDAT", "0")))
      expressionMpapma2 = expressionMpapma2.and(expressionMpapma2.eq("AMOBV4", supplierNumber))
      DBAction manufacturerQuery = database.table("MPAPMA").index("00").matching(expressionMpapma2).selection("AMPRIO", "OBV1", "OBV2", "OBV3", "OBV4", "OBV5", "AMFDAT", "AMPROD", "AMMNFP").build()
      DBContainer MPAPMA2 = manufacturerQuery.getContainer()
      MPAPMA2.set("AMCONO", currentCompany)
      MPAPMA2.set("AMPRIO", 2)
      MPAPMA2.set("AMOBV1", itemNumber)
      if(manufacturerQuery.readAll(MPAPMA2, 3, outDataMpapma2)){
      }
      //logger.debug("checkPoPpoSupplier - manufacturer = " + manufacturer)
      //logger.debug("checkPoPpoSupplier - ingredient1 = " + ingredient1)
      //logger.debug("checkPoPpoSupplier - ingredient2 = " + ingredient2)
      //logger.debug("checkPoPpoSupplier - ingredient3 = " + ingredient3)
      //logger.debug("checkPoPpoSupplier - ingredient4 = " + ingredient4)
      //logger.debug("checkPoPpoSupplier - ingredient5 = " + ingredient5)
      //logger.debug("checkPoPpoSupplier - ingredient6 = " + ingredient6)
      //logger.debug("checkPoPpoSupplier - ingredient7 = " + ingredient7)
      //logger.debug("checkPoPpoSupplier - ingredient8 = " + ingredient8)
      //logger.debug("checkPoPpoSupplier - ingredient9 = " + ingredient9)
      //logger.debug("checkPoPpoSupplier - ingredient10 = " + ingredient10)
      //logger.debug("checkItemWarehouseSupplier - ingredientFound = " + ingredientFound)
      // Retrieve product origin
      DBAction mitvenQuery = database.table("MITVEN").index("10").selection("IFORCO").build()
      DBContainer MITVEN = mitvenQuery.getContainer()
      MITVEN.set("IFCONO", currentCompany)
      MITVEN.set("IFSUNO", supplierNumber)
      MITVEN.set("IFITNO", itemNumber)
      if(mitvenQuery.readAll(MITVEN, 3, outDataMitven)){
      }
      //logger.debug("checkPoPpoSupplier - productOrigin = " + productOrigin)
      //logger.debug("checkPoPpoSupplier - standardLifetime = " + standardLifetime)
      potentiallyDangerous = "0"
      DBAction cidmasQuery = database.table("CIDMAS").index("00").selection("IDCFI3").build()
      DBContainer CIDMAS = cidmasQuery.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO", supplierNumber)
      if(cidmasQuery.read(CIDMAS)){
        //logger.debug("CIDMAS checkPoPpoSupplier/SUCL = 200 found")
        if (CIDMAS.get("IDCFI3").toString().trim() == "OUI"){
          potentiallyDangerous = "1"
          //logger.debug("potentiallyDangerous checkPoPpoSupplier/SUCL = 200")
        }
      }
    }
  }
  // Retrieve informations from supplier that has been retrieved from item warehouse
  public void checkItemWarehouseSupplier(){
    //logger.debug("checkItemWarehouseSupplier")
    //logger.debug("checkItemWarehouseSupplier - sucl = " + sucl)
    LocalDateTime timeOfCreation = LocalDateTime.now()
    currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    suclIndicator = getSUCLIndicator(sucl)
    // Supplier base
    if(!suclIndicator){ //SUCL=100
      // Retrieve cnuf
      savedMnfp = 10
      ExpressionFactory expressionMpapma1 = database.getExpressionFactory("MPAPMA")
      expressionMpapma1 = expressionMpapma1.eq("AMMFRS", "20")
      expressionMpapma1 = expressionMpapma1.and(expressionMpapma1.le("AMFDAT", currentDate))
      expressionMpapma1 = expressionMpapma1.and((expressionMpapma1.ge("AMTDAT", currentDate)).or(expressionMpapma1.eq("AMTDAT", "0")))
      DBAction cnufQuery = database.table("MPAPMA").index("00").matching(expressionMpapma1).selection("AMPRIO", "OBV1", "OBV2", "OBV3", "OBV4", "OBV5", "AMFDAT", "AMPROD", "AMMNFP").build()
      DBContainer MPAPMA1 = cnufQuery.getContainer()
      MPAPMA1.set("AMCONO", currentCompany)
      MPAPMA1.set("AMPRIO", 5)
      MPAPMA1.set("AMOBV1", supplierNumber)
      MPAPMA1.set("AMOBV2", itemNumber)
      if(cnufQuery.readAll(MPAPMA1, 4, outData_MPAPMA1)){
      }
      //logger.debug("checkItemWarehouseSupplier - cnuf from MPAPMA = " + cnuf)
      // Retrieve manufacturer and ingredients
      savedMnfp = 10
      ExpressionFactory expressionMpapma2 = database.getExpressionFactory("MPAPMA")
      expressionMpapma2 = expressionMpapma2.eq("AMMFRS", "20")
      expressionMpapma2 = expressionMpapma2.and(expressionMpapma2.le("AMFDAT", currentDate))
      expressionMpapma2 = expressionMpapma2.and((expressionMpapma2.ge("AMTDAT", currentDate)).or(expressionMpapma2.eq("AMTDAT", "0")))
      expressionMpapma2 = expressionMpapma2.and(expressionMpapma2.eq("AMOBV4", cnuf))
      DBAction manufacturerQuery = database.table("MPAPMA").index("00").matching(expressionMpapma2).selection("AMPRIO", "OBV1", "OBV2", "OBV3", "OBV4", "OBV5", "AMFDAT", "AMPROD", "AMMNFP").build()
      DBContainer MPAPMA2 = manufacturerQuery.getContainer()
      MPAPMA2.set("AMCONO", currentCompany)
      MPAPMA2.set("AMPRIO", 2)
      MPAPMA2.set("AMOBV1", itemNumber)
      MPAPMA2.set("AMOBV2", supplierNumber)
      if(manufacturerQuery.readAll(MPAPMA2, 4, outDataMpapma2)){
      }
      //logger.debug("checkItemWarehouseSupplier - manufacturer = " + manufacturer)
      //logger.debug("checkItemWarehouseSupplier - ingredient1 = " + ingredient1)
      //logger.debug("checkItemWarehouseSupplier - ingredient2 = " + ingredient2)
      //logger.debug("checkItemWarehouseSupplier - ingredient3 = " + ingredient3)
      //logger.debug("checkItemWarehouseSupplier - ingredient4 = " + ingredient4)
      //logger.debug("checkItemWarehouseSupplier - ingredient5 = " + ingredient5)
      //logger.debug("checkItemWarehouseSupplier - ingredient6 = " + ingredient6)
      //logger.debug("checkItemWarehouseSupplier - ingredient7 = " + ingredient7)
      //logger.debug("checkItemWarehouseSupplier - ingredient8 = " + ingredient8)
      //logger.debug("checkItemWarehouseSupplier - ingredient9 = " + ingredient9)
      //logger.debug("checkItemWarehouseSupplier - ingredient10 = " + ingredient10)
      //logger.debug("checkItemWarehouseSupplier - ingredientFound = " + ingredientFound)
      // Retrieve product origin
      DBAction mitvenQuery = database.table("MITVEN").index("10").selection("IFORCO").build()
      DBContainer MITVEN = mitvenQuery.getContainer()
      MITVEN.set("IFCONO", currentCompany)
      MITVEN.set("IFSUNO", cnuf)
      MITVEN.set("IFITNO", itemNumber)
      if(mitvenQuery.readAll(MITVEN, 3, outDataMitven)){
      }
      //logger.debug("checkItemWarehouseSupplier - productOrigin = " + productOrigin)
      //logger.debug("checkItemWarehouseSupplier - standardLifetime = " + standardLifetime)
      potentiallyDangerous = "0"
      DBAction cidmasQuery = database.table("CIDMAS").index("00").selection("IDCFI3").build()
      DBContainer CIDMAS = cidmasQuery.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO", cnuf)
      if(cidmasQuery.read(CIDMAS)){
        //logger.debug("CIDMAS checkItemWarehouseSupplier/SUCL = 100 found")
        if (CIDMAS.get("IDCFI3").toString().trim() == "OUI"){
          potentiallyDangerous = "1"
          //logger.debug("potentiallyDangerous checkItemWarehouseSupplier/SUCL = 100")
        }
      }
    }
    //cnuf
    if(suclIndicator){ //SUCL=200
      cnuf = ""   // In this case, supplierNumber is the cnuf
      //logger.debug("checkItemWarehouseSupplier - cnuf is blank = " + cnuf)
      // Retrieve manufacturer and ingredients
      savedMnfp = 10
      ExpressionFactory expressionMpapma2 = database.getExpressionFactory("MPAPMA")
      expressionMpapma2 = expressionMpapma2.eq("AMMFRS", "20")
      expressionMpapma2 = expressionMpapma2.and(expressionMpapma2.le("AMFDAT", currentDate))
      expressionMpapma2 = expressionMpapma2.and((expressionMpapma2.ge("AMTDAT", currentDate)).or(expressionMpapma2.eq("AMTDAT", "0")))
      expressionMpapma2 = expressionMpapma2.and(expressionMpapma2.eq("AMOBV4", supplierNumber))
      DBAction manufacturerQuery = database.table("MPAPMA").index("00").matching(expressionMpapma2).selection("AMPRIO", "OBV1", "OBV2", "OBV3", "OBV4", "OBV5", "AMFDAT", "AMPROD", "AMMNFP").build()
      DBContainer MPAPMA2 = manufacturerQuery.getContainer()
      MPAPMA2.set("AMCONO", currentCompany)
      MPAPMA2.set("AMPRIO", 2)
      MPAPMA2.set("AMOBV1", itemNumber)
      if(manufacturerQuery.readAll(MPAPMA2, 3, outDataMpapma2)){
      }
      //logger.debug("checkItemWarehouseSupplier - manufacturer = " + manufacturer)
      //logger.debug("checkItemWarehouseSupplier - ingredient1 = " + ingredient1)
      //logger.debug("checkItemWarehouseSupplier - ingredient2 = " + ingredient2)
      //logger.debug("checkItemWarehouseSupplier - ingredient3 = " + ingredient3)
      //logger.debug("checkItemWarehouseSupplier - ingredient4 = " + ingredient4)
      //logger.debug("checkItemWarehouseSupplier - ingredient5 = " + ingredient5)
      //logger.debug("checkItemWarehouseSupplier - ingredient6 = " + ingredient6)
      //logger.debug("checkItemWarehouseSupplier - ingredient7 = " + ingredient7)
      //logger.debug("checkItemWarehouseSupplier - ingredient8 = " + ingredient8)
      //logger.debug("checkItemWarehouseSupplier - ingredient9 = " + ingredient9)
      //logger.debug("checkItemWarehouseSupplier - ingredient10 = " + ingredient10)
      //logger.debug("checkItemWarehouseSupplier - ingredientFound = " + ingredientFound)
      // Retrieve product origin
      DBAction mitvenQuery = database.table("MITVEN").index("10").selection("IFORCO").build()
      DBContainer MITVEN = mitvenQuery.getContainer()
      MITVEN.set("IFCONO", currentCompany)
      MITVEN.set("IFSUNO", supplierNumber)
      MITVEN.set("IFITNO", itemNumber)
      if(mitvenQuery.readAll(MITVEN, 3, outDataMitven)){
      }
      //logger.debug("checkItemWarehouseSupplier - productOrigin = " + productOrigin)
      //logger.debug("checkItemWarehouseSupplier - standardLifetime = " + standardLifetime)
      potentiallyDangerous = "0"
      DBAction cidmasQuery = database.table("CIDMAS").index("00").selection("IDCFI3").build()
      DBContainer CIDMAS = cidmasQuery.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO", supplierNumber)
      if(cidmasQuery.read(CIDMAS)){
        //logger.debug("CIDMAS checkItemWarehouseSupplier/SUCL = 200 found")
        if (CIDMAS.get("IDCFI3").toString().trim() == "OUI"){
          potentiallyDangerous = "1"
          //logger.debug("potentiallyDangerous checkItemWarehouseSupplier/SUCL = 200")
        }
      }
    }
  }

  public boolean getSUCLIndicator(String pSUCL) {
    DBAction cugex1CsytabQuery = database.table("CUGEX1").index("00").selection("F1CHB1").build()
    DBContainer cugex1Csytab = cugex1CsytabQuery.getContainer()
    cugex1Csytab.set("F1CONO", currentCompany)
    cugex1Csytab.set("F1FILE", "CSYTAB")
    cugex1Csytab.set("F1PK01", "")
    cugex1Csytab.set("F1PK02", "SUCL")
    cugex1Csytab.set("F1PK03", pSUCL)
    cugex1Csytab.set("F1PK04", "")
    cugex1Csytab.set("F1PK05", "")
    cugex1Csytab.set("F1PK06", "")
    cugex1Csytab.set("F1PK07", "")
    cugex1Csytab.set("F1PK08", "")
    if (cugex1CsytabQuery.read(cugex1Csytab)) {
      return (boolean)cugex1Csytab.get("F1CHB1")
    }
    return false;
  }

}
