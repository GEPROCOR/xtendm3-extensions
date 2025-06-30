/**
 * README
 * This extension is used by MASHUP
 *
 * Name : EXT258MI.UpdLine
 * Description : The UpdLine transaction calls OIS258 for mass change of orderlines.
 * Date         Changed By   Description
 * 20220322     RENARN       CMDX27 - Mass change of orderlines
 * 20230119     YOUYVO       CMDX27 - Mass change of orderlines - jump OIS260
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

public class UpdLine extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility
  private Integer currentCompany
  private String currentDivision
  private final IonAPI ion
  private String date
  private String iROUT
  private String iPLDT
  private String iELNO
  private Integer iFPON
  private Integer iTPON
  private String iFUA5
  private String iTUA5

  public UpdLine(MIAPI mi, IonAPI ion, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility) {
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

    // Check order number
    if (mi.in.get("ORNO") == null || mi.in.get("ORNO") == "") {
      mi.error("Numéro commande est obligatoire")
      return
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
    // Check planned date
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
    // Save joint delivery
    iELNO = ""
    if(mi.in.get("ELNO") != null){
      iELNO = mi.in.get("ELNO")
    }
    iFUA5 = ""
    if(mi.in.get("FUA5") != null){
      iFUA5 = mi.in.get("FUA5")
    }
    iTUA5 = ""
    if(mi.in.get("TUA5") != null){
      iTUA5 = mi.in.get("TUA5")
    }
    if ((mi.in.get("ROUT") == null || mi.in.get("ROUT") == "")
      && (mi.in.get("PLDT") == null || mi.in.get("PLDT") == "")) {
      mi.error("Veuillez renseigner au moins un champ ROUT/PLDT")
      return
    }
    iFPON = 0
    if(mi.in.get("FPON") != null){
      iFPON = mi.in.get("FPON")
    }
    iTPON = 0
    if(mi.in.get("TPON") != null){
      iTPON = mi.in.get("TPON")
    }
    // Update ROUT and PLDT
    currentDivision = (String) program.getLDAZD().DIVI
    String iORNO = mi.in.get("ORNO")
    String iROUX
    String iPLDX
    String iELNX
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
    if (iELNO != "") {
      iELNX = "1"
    } else {
      iELNX = ""
    }
    logger.debug("iPLDT = " + iPLDT)
    logger.debug("iPLDX = " + iPLDX)
    logger.debug("iROUT = " + iROUT)
    logger.debug("iROUX = " + iROUX)
    def endpoint = "/M3/ips/service/OIS260"
    def headers = ["Accept": "application/xml", "Content-Type": "application/xml"]
    def queryParameters = (Map) null
    def formParameters = (Map) null // post URL encoded parameters
    def body = ""
    if(iROUT.trim() != "") {
      if (iROUT == "??") {
        iROUT = ""
      }
      if (iELNO == "??") {
        iELNO = ""
      }
      body = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cred=\"http://lawson.com/ws/credentials\" xmlns:lan=\"http://schemas.infor.com/ips/OIS260/UpdLine\">" +
        "<soapenv:Header><cred:lws><cred:company>" + currentCompany + "</cred:company><cred:division>" + currentDivision + "</cred:division></cred:lws>" +
        "</soapenv:Header>" +
        "<soapenv:Body>" +
        "<lan:UpdLine><lan:OIS260><lan:WFORNO>" + iORNO + "</lan:WFORNO><lan:WTORNO>" + iORNO + "</lan:WTORNO><lan:WFULA5>" + iFUA5 + "</lan:WFULA5><lan:WTULA5>" + iTUA5 + "</lan:WTULA5><lan:W1ROUT>" + iROUT + "</lan:W1ROUT><lan:W1ROUX>" + iROUX + "</lan:W1ROUX><lan:W1ELNO>" + iELNO + "</lan:W1ELNO><lan:W1ELNX>" + iELNX + "</lan:W1ELNX><lan:WFPONR>" + iFPON + "</lan:WFPONR><lan:WTPONR>" + iTPON + "</lan:WTPONR></lan:OIS260></lan:UpdLine></soapenv:Body></soapenv:Envelope>"
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
      if (iELNO == "??") {
        iELNO = ""
      }
      body = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cred=\"http://lawson.com/ws/credentials\" xmlns:lan=\"http://schemas.infor.com/ips/OIS260/UpdLine\">" +
        "<soapenv:Header><cred:lws><cred:company>" + currentCompany + "</cred:company><cred:division>" + currentDivision + "</cred:division></cred:lws>" +
        "</soapenv:Header>" +
        "<soapenv:Body>" +
        "<lan:UpdLine><lan:OIS260><lan:WFORNO>" + iORNO + "</lan:WFORNO><lan:WTORNO>" + iORNO + "</lan:WTORNO><lan:WFULA5>" + iFUA5 + "</lan:WFULA5><lan:WTULA5>" + iTUA5 + "</lan:WTULA5><lan:W1PLDT>" + iPLDT + "</lan:W1PLDT><lan:W1PLDX>" + iPLDX + "</lan:W1PLDX><lan:W1ELNO>" + iELNO + "</lan:W1ELNO><lan:W1ELNX>" + iELNX + "</lan:W1ELNX><lan:WFPONR>" + iFPON + "</lan:WFPONR><lan:WTPONR>" + iTPON + "</lan:WTPONR></lan:OIS260></lan:UpdLine></soapenv:Body></soapenv:Envelope>"
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
}
