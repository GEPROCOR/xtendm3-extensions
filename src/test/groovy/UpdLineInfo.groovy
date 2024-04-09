/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT060MI.UpdLineInfo
 * Description : The UpdLineInfo transaction update records to the OXLINE or OOLINE table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX04 - Recherche article
 */

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import groovy.json.JsonException
import groovy.json.JsonSlurper
import java.util.GregorianCalendar
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat

public class UpdLineInfo extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility
  private Integer currentCompany
  private String currentDivision
  private final IonAPI ion
  private Integer fpnr
  private Integer tpnr
  private Integer ponr
  private String date
  private String iROUT
  private String saved_iROUT
  private String iPLDT
  private String iJDCD

  public UpdLineInfo(MIAPI mi, IonAPI ion, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.ion = ion
    this.utility = utility
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer) program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    if (mi.in.get("ORNO") == null || mi.in.get("ORNO") == "") {
      mi.error("Numéro commande est obligatoire")
      return
    }

    if (mi.in.get("FPNR") == null || mi.in.get("FPNR") == "") {
      mi.error("Numéro ligne commande début est obligatoire")
      return
    } else {
      fpnr = mi.in.get("FPNR")
    }
    if (mi.in.get("TPNR") == null || mi.in.get("TPNR") == "") {
      mi.error("Numéro ligne commande fin est obligatoire")
      return
    } else {
      tpnr = mi.in.get("TPNR")
    }
    // Check replaced item
    if (mi.in.get("REPI") != null && mi.in.get("REPI") != "") {
      DBAction Query = database.table("MITMAS").index("00").build()
      DBContainer MITMAS = Query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO", mi.in.get("REPI"))
      if (!Query.read(MITMAS)) {
        mi.error("Code article " + mi.in.get("REPI") + " n'existe pas")
        return
      }
    }
    // Chect route
    iROUT = ""
    if (mi.in.get("ROUT") != null && mi.in.get("ROUT") != "") {
      iROUT = mi.in.get("ROUT")
      if (mi.in.get("ROUT") != "??") {
        DBAction Query = database.table("DROUTE").index("00").build()
        DBContainer DROUTE = Query.getContainer()
        DROUTE.set("DRCONO", currentCompany)
        DROUTE.set("DRROUT", mi.in.get("ROUT"))
        if (!Query.read(DROUTE)) {
          mi.error("Tournée " + mi.in.get("ROUT") + " n'existe pas")
          return
        }
      }
    }
    iPLDT = ""
    if (mi.in.get("PLDT") != null && mi.in.get("PLDT") != "") {
      date = mi.in.get("PLDT")
      if (!utility.call("DateUtil", "isDateValid", date, "yyyyMMdd")) {
        mi.error("Date planifiée est incorrecte")
        return
      }
      // Convert planing date
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
      LocalDate DATE = LocalDate.parse(date, formatter)
      iPLDT = DATE.format(DateTimeFormatter.ofPattern("ddMMyy"))
    }
    iJDCD = ""
    if(mi.in.get("JDCD") != null){
      iJDCD = mi.in.get("JDCD")
    }

    if ((mi.in.get("REPI") == null || mi.in.get("REPI") == "")
      && (mi.in.get("ROUT") == null || mi.in.get("ROUT") == "")
      && (mi.in.get("PLDT") == null || mi.in.get("PLDT") == "")) {
      mi.error("Veuillez renseigner au moins un champ REPI/ROUT/PLDT")
      return
    }

    // Update OXLINE
    ExpressionFactory expression = database.getExpressionFactory("OXLINE")
    expression = expression.ge("OBPONR", fpnr as String)
    expression = expression.and(expression.le("OBPONR", tpnr as String))
    DBAction query_OXLINE = database.table("OXLINE").index("00").matching(expression).selection("OBREPI", "OBROUT")build()
    DBContainer OXLINE = query_OXLINE.getContainer()
    OXLINE.set("OBCONO", currentCompany)
    OXLINE.set("OBORNO", mi.in.get("ORNO"))
    if (!query_OXLINE.readAllLock(OXLINE, 2, updateCallBack_OXLINE)){
    }
    // Update OOLINE
    if (mi.in.get("REPI") != null || mi.in.get("REPI") != "") {
      ExpressionFactory expression2 = database.getExpressionFactory("OOLINE")
      expression2 = expression2.ge("OBPONR", fpnr as String)
      expression2 = expression2.and(expression2.le("OBPONR", tpnr as String))
      DBAction query_OOLINE = database.table("OOLINE").index("00").matching(expression2).selection("OBREPI").build()
      DBContainer OOLINE = query_OOLINE.getContainer()
      OOLINE.set("OBCONO", currentCompany)
      OOLINE.set("OBORNO", mi.in.get("ORNO"))
      if (!query_OOLINE.readAllLock(OOLINE, 2, updateCallBack_OOLINE)) {
      }
    }
    // Update ROUT and PLDT
    currentDivision = (String) program.getLDAZD().DIVI
    String iORNO = mi.in.get("ORNO")
    String iROUX
    String iPLDX
    String iJDCX
    if (iROUT != "") {
      iROUX = "1"
    } else {
      iROUX = ""
    }
    if (iPLDT != "") {
      iPLDX = "1"
    } else {
      iPLDX = ""
    }
    if (iJDCD != "") {
      iJDCX = "1"
    } else {
      iJDCX = ""
    }
    logger.debug("iPLDT = " + iPLDT)
    logger.debug("iPLDX = " + iPLDX)
    logger.debug("iROUT = " + iROUT)
    logger.debug("iROUX = " + iROUX)
    def endpoint = "/M3/ips/service/OIS260"//"""/M3/ips/service/CRS005"
    def headers = ["Accept": "application/xml", "Content-Type": "application/xml"]
    def queryParameters = (Map) null
    // ["PickingSkill": "TO1", "Option": "1" , "Name": "ESSA153", "Description": "ESSA153"]//["company": "750", "division": "BBB" ] //(Map)null // // define as map if there are any query parameters e.g. ["name1": "value1", "name2": "value2"]
    def formParameters = (Map) null // post URL encoded parameters
    def body = ""
    if(iROUT.trim() != "") {
      if (iROUT == "??") {
        iROUT = ""
      }
      if (iJDCD == "??") {
        iJDCD = ""
      }
      body = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cred=\"http://lawson.com/ws/credentials\" xmlns:lan=\"http://schemas.infor.com/ips/OIS260/Lancement\">" +
        "<soapenv:Header><cred:lws><cred:company>" + currentCompany + "</cred:company><cred:division>" + currentDivision + "</cred:division></cred:lws>" +
        "</soapenv:Header>" +
        "<soapenv:Body>" +
        "<lan:Lancement><lan:OIS260><lan:WFORNO>" + iORNO + "</lan:WFORNO><lan:WFPONR>" + fpnr + "</lan:WFPONR><lan:WTORNO>" + iORNO + "</lan:WTORNO><lan:WTPONR>" + tpnr + "</lan:WTPONR><lan:W1ROUT>" + iROUT + "</lan:W1ROUT><lan:W1ROUX>" + iROUX + "</lan:W1ROUX><lan:W1JDCD>" + iJDCD + "</lan:W1JDCD><lan:W1JDCX>" + iJDCX + "</lan:W1JDCX></lan:OIS260></lan:Lancement></soapenv:Body></soapenv:Envelope>"
      logger.debug("Step 2 endpoint = " + endpoint)
      logger.debug("Step 2 headers = " + headers)
      logger.debug("Step 2 queryParameters = " + queryParameters)
      logger.debug("Step 2 body = " + body)
      IonResponse response = ion.post(endpoint, headers, queryParameters, body)
      //logger.debug("CDUVmessage: ${body}")
      if (response.getError()) {
        logger.debug("Failed calling ION API, detailed error message: ${response.getErrorMessage()}")
        logger.debug("Failed calling ION API, detailed error message: ${body}")
        mi.error("Failed calling ION API, detailed error message: ${response.getErrorMessage()}")
        return
      }
      if (response.getStatusCode() != 200) {
        logger.debug("Expected status 200 but got ${response.getStatusCode()} instead ${response.getContent()} ")
        logger.debug("Failed calling ION API, detailed error message: ${body}")
        mi.error("Expected status 200 but got ${response.getStatusCode()} instead ${response.getContent()} ")
        return
      }
    }
    if(iPLDT.trim() != "") {
      if (iJDCD == "??") {
        iJDCD = ""
      }
      body = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cred=\"http://lawson.com/ws/credentials\" xmlns:lan=\"http://schemas.infor.com/ips/OIS260/Lancement\">" +
        "<soapenv:Header><cred:lws><cred:company>" + currentCompany + "</cred:company><cred:division>" + currentDivision + "</cred:division></cred:lws>" +
        "</soapenv:Header>" +
        "<soapenv:Body>" +
        "<lan:Lancement><lan:OIS260><lan:WFORNO>" + iORNO + "</lan:WFORNO><lan:WFPONR>" + fpnr + "</lan:WFPONR><lan:WTORNO>" + iORNO + "</lan:WTORNO><lan:WTPONR>" + tpnr + "</lan:WTPONR><lan:W1PLDT>" + iPLDT + "</lan:W1PLDT><lan:W1PLDX>" + iPLDX + "</lan:W1PLDX><lan:W1JDCD>" + iJDCD + "</lan:W1JDCD><lan:W1JDCX>" + iJDCX + "</lan:W1JDCX></lan:OIS260></lan:Lancement></soapenv:Body></soapenv:Envelope>"
      logger.debug("Step 1 body = " + body)
      IonResponse response = ion.post(endpoint, headers, queryParameters, body)
      //logger.debug("CDUVmessage: ${body}")
      if (response.getError()) {
        logger.debug("Failed calling ION API, detailed error message: ${response.getErrorMessage()}")
        logger.debug("Failed calling ION API, detailed error message: ${body}")
        mi.error("Failed calling ION API, detailed error message: ${response.getErrorMessage()}")
        return
      }
      if (response.getStatusCode() != 200) {
        logger.debug("Expected status 200 but got ${response.getStatusCode()} instead ${response.getContent()} ")
        logger.debug("Failed calling ION API, detailed error message: ${body}")
        mi.error("Expected status 200 but got ${response.getStatusCode()} instead ${response.getContent()} ")
        return
      }
    }
  }
  // Update OOLINE
  Closure<?> updateCallBack_OOLINE = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("OBCHNO")
    if(mi.in.get("REPI") != null)
      lockedResult.set("OBREPI", mi.in.get("REPI"))
    lockedResult.setInt("OBLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("OBCHNO", changeNumber + 1)
    lockedResult.set("OBCHID", program.getUser())
    lockedResult.update()
  }
  // Update OXLINE
  Closure<?> updateCallBack_OXLINE = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("OBCHNO")
    if(mi.in.get("REPI") != null)
      lockedResult.set("OBREPI", mi.in.get("REPI"))
    if(iROUT.trim()!=""){
      saved_iROUT = iROUT
      if(iROUT.trim() == "??"){
        iROUT = ""
      }
      lockedResult.set("OBROUT", iROUT)
      iROUT = saved_iROUT
    }
    if(mi.in.get("PLDT") != null)
      lockedResult.set("OBPLDT", mi.in.get("PLDT"))
    lockedResult.setInt("OBLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("OBCHNO", changeNumber + 1)
    lockedResult.set("OBCHID", program.getUser())
    lockedResult.update()
  }
}
