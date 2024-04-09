/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT060
 * Description : The UpdLineInfo transaction update records to the OXLINE or OOLINE table (EXT060MI.UpdLineInfo conversion)
 * Date         Changed By   Description
 * 20211220     RENARN         CMDX04 - Recherche article
 */
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import groovy.json.JsonException
import groovy.json.JsonSlurper
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat

public class EXT060 extends ExtendM3Batch {
  private final LoggerAPI logger
  private final DatabaseAPI database;
  private final ProgramAPI program
  private final BatchAPI batch
  private final MICallerAPI miCaller
  private final TextFilesAPI textFiles
  private final UtilityAPI utility
  private Integer currentCompany
  private String rawData
  private int rawDataLength
  private int beginIndex
  private int endIndex
  private String logFileName
  private boolean IN60
  private String jobNumber
  private String currentDivision
  private final IonAPI ion
  private Integer fpnr
  private Integer tpnr
  private Integer ponr
  private String date
  private String iROUT
  private String saved_iROUT
  private String iPLDT
  private String inORNO
  private Integer inFPNR = 0
  private Integer inTPNR = 0
  private String inREPI
  private String inROUT
  private String inPLDT

  public EXT060(LoggerAPI logger, DatabaseAPI database, ProgramAPI program, BatchAPI batch, MICallerAPI miCaller, TextFilesAPI textFiles, UtilityAPI utility) {
    this.logger = logger;
    this.database = database;
    this.program = program
    this.batch = batch;
    this.miCaller = miCaller
    this.textFiles = textFiles
    this.utility = utility
  }

  public void main() {
    // Get job number
    LocalDateTime timeOfCreation = LocalDateTime.now()
    jobNumber = program.getJobNumber() + timeOfCreation.format(DateTimeFormatter.ofPattern("yyMMdd")) + timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss"))

    logger.debug("Début" + program.getProgramName())
    //logger.debug("referenceId = " + batch.getReferenceId().get())
    if(batch.getReferenceId().isPresent()){
      Optional<String> data = getJobData(batch.getReferenceId().get())
      //logger.debug("data = " + data)
      performActualJob(data)
    } else {
      // No job data found
      logger.debug("Job data for job ${batch.getJobId()} is missing")
    }
  }
  // Get job data
  private Optional<String> getJobData(String referenceId){
    def query = database.table("EXTJOB").index("00").selection("EXDATA").build()
    def container = query.createContainer()
    container.set("EXRFID", referenceId)
    if (query.read(container)){
      logger.debug("EXDATA = " + container.getString("EXDATA"))
      return Optional.of(container.getString("EXDATA"))
    } else {
      logger.debug("EXTJOB not found")
    }
    return Optional.empty()
  }
  // Perform actual job
  private performActualJob(Optional<String> data){
    if(!data.isPresent()){
      logger.debug("Job reference Id ${batch.getReferenceId().get()} is passed but data was not found")
      return;
    }
    rawData = data.get()
    logger.debug("Début performActualJob")
    inORNO = getFirstParameter()
    inFPNR = getNextParameter() as Integer
    inTPNR = getNextParameter() as Integer
    inREPI = getNextParameter()
    inROUT = getNextParameter()
    inPLDT = getNextParameter()

    logger.debug("inORNO = " + inORNO)
    logger.debug("inFPNR = " + inFPNR)
    logger.debug("inTPNR = " + inTPNR)
    logger.debug("inREPI = " + inREPI)
    logger.debug("inROUT = " + inROUT)
    logger.debug("inPLDT = " + inPLDT)

    currentCompany = (Integer)program.getLDAZD().CONO

    // Perform Job

    if (inORNO == null || inORNO == "") {
      String header = "MSG;"+"ORNO"
      String message = "Numéro commande est obligatoire" + ";" + inORNO
      logMessage(header, message)
      return
    }

    if (inFPNR == 0) {
      String header = "MSG;"+"FPNR"
      String message = "Numéro ligne commande début est obligatoire" + ";" + inFPNR as String
      logMessage(header, message)
      return
    } else {
      fpnr = inFPNR
    }
    if (inTPNR == 0) {
      String header = "MSG;"+"TPNR"
      String message = "Numéro ligne commande fin est obligatoire" + ";" + inTPNR as String
      logMessage(header, message)
      return
    } else {
      tpnr = inTPNR
    }
    // Check replaced item
    if (inREPI != null && inREPI != "") {
      DBAction Query = database.table("MITMAS").index("00").build()
      DBContainer MITMAS = Query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO", inREPI)
      if (!Query.read(MITMAS)) {
        String header = "MSG;"+"REPI"
        String message = "Code article n'existe pas" + ";" + inREPI
        logMessage(header, message)
        return
      }
    }
    // Chect route
    iROUT = ""
    if (inROUT != null && inROUT != "") {
      iROUT = inROUT
      if (inROUT != "??") {
        DBAction Query = database.table("DROUTE").index("00").build()
        DBContainer DROUTE = Query.getContainer()
        DROUTE.set("DRCONO", currentCompany)
        DROUTE.set("DRROUT", inROUT)
        if (!Query.read(DROUTE)) {
          String header = "MSG;"+"ROUT"
          String message = "Tournée n'existe pas" + ";" + inROUT
          logMessage(header, message)
          return
        }
      }
    }
    iPLDT = ""
    if (inPLDT != null && inPLDT != "") {
      date = inPLDT;
      if (!utility.call("DateUtil", "isDateValid", date, "yyyyMMdd")) {
        String header = "MSG;"+"PLDT"
        String message = "Date planifiée est invalide" + ";" + inPLDT
        logMessage(header, message)
        return;
      }
      // Convert planing date
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
      LocalDate DATE = LocalDate.parse(date, formatter)
      iPLDT = DATE.format(DateTimeFormatter.ofPattern("ddMMyy"))
    }

    if ((inREPI == null || inREPI == "")
      && (inROUT == null || inROUT == "")
      && (inPLDT == null || inPLDT == "")) {
      String header = "MSG;"+"REPI;"+"ROUT;"+"PLDT"
      String message = "Veuillez renseigner au moins un champ REPI/ROUT/PLDT" + ";" + inREPI + ";" + inROUT + ";" + inPLDT
      logMessage(header, message)
      return
    }

    // Update OXLINE
    ExpressionFactory expression = database.getExpressionFactory("OXLINE")
    expression = expression.ge("OBPONR", fpnr as String)
    expression = expression.and(expression.le("OBPONR", tpnr as String))
    DBAction query_OXLINE = database.table("OXLINE").index("00").matching(expression).selection("OBREPI", "OBROUT")build()
    DBContainer OXLINE = query_OXLINE.getContainer()
    OXLINE.set("OBCONO", currentCompany)
    OXLINE.set("OBORNO", inORNO)
    if (!query_OXLINE.readAllLock(OXLINE, 2, updateCallBack_OXLINE)){
    }
    // Update OOLINE
    if (inREPI != null || inREPI != "") {
      ExpressionFactory expression2 = database.getExpressionFactory("OOLINE")
      expression2 = expression2.ge("OBPONR", fpnr as String)
      expression2 = expression2.and(expression2.le("OBPONR", tpnr as String))
      DBAction query_OOLINE = database.table("OOLINE").index("00").matching(expression2).selection("OBREPI").build()
      DBContainer OOLINE = query_OOLINE.getContainer()
      OOLINE.set("OBCONO", currentCompany)
      OOLINE.set("OBORNO", inORNO)
      if (!query_OOLINE.readAllLock(OOLINE, 2, updateCallBack_OOLINE)) {
      }
    }
    // Update ROUT and PLDT
    currentDivision = (String) program.getLDAZD().DIVI;
    String iORNO = inORNO
    String iROUX
    String iPLDX
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
      body = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cred=\"http://lawson.com/ws/credentials\" xmlns:lan=\"http://schemas.infor.com/ips/OIS260/Lancement\">" +
        "<soapenv:Header><cred:lws><cred:company>" + currentCompany + "</cred:company><cred:division>" + currentDivision + "</cred:division></cred:lws>" +
        "</soapenv:Header>" +
        "<soapenv:Body>" +
        "<lan:Lancement><lan:OIS260><lan:WFORNO>" + iORNO + "</lan:WFORNO><lan:WFPONR>" + fpnr + "</lan:WFPONR><lan:WTORNO>" + iORNO + "</lan:WTORNO><lan:WTPONR>" + tpnr + "</lan:WTPONR><lan:W1ROUT>" + iROUT + "</lan:W1ROUT><lan:W1ROUX>" + iROUX + "</lan:W1ROUX></lan:OIS260></lan:Lancement></soapenv:Body></soapenv:Envelope>"
      logger.debug("Step 2 endpoint = " + endpoint)
      logger.debug("Step 2 headers = " + headers)
      logger.debug("Step 2 queryParameters = " + queryParameters)
      logger.debug("Step 2 body = " + body)
      IonResponse response = ion.post(endpoint, headers, queryParameters, body);
      //logger.debug("CDUVmessage: ${body}")
      if (response.getError()) {
        logger.debug("Failed calling ION API, detailed error message: ${response.getErrorMessage()}")
        logger.debug("Failed calling ION API, detailed error message: ${body}")
        String header = "MSG webservice"
        String message = "Failed calling ION API, detailed error message: ${response.getErrorMessage()}"
        logMessage(header, message)
        return
      }
      if (response.getStatusCode() != 200) {
        logger.debug("Expected status 200 but got ${response.getStatusCode()} instead ${response.getContent()} ")
        logger.debug("Failed calling ION API, detailed error message: ${body}")
        String header = "MSG webservice"
        String message = "Expected status 200 but got ${response.getStatusCode()} instead ${response.getContent()} "
        logMessage(header, message)
        return
      }
    }
    if(iPLDT.trim() != "") {
      body = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cred=\"http://lawson.com/ws/credentials\" xmlns:lan=\"http://schemas.infor.com/ips/OIS260/Lancement\">" +
        "<soapenv:Header><cred:lws><cred:company>" + currentCompany + "</cred:company><cred:division>" + currentDivision + "</cred:division></cred:lws>" +
        "</soapenv:Header>" +
        "<soapenv:Body>" +
        "<lan:Lancement><lan:OIS260><lan:WFORNO>" + iORNO + "</lan:WFORNO><lan:WFPONR>" + fpnr + "</lan:WFPONR><lan:WTORNO>" + iORNO + "</lan:WTORNO><lan:WTPONR>" + tpnr + "</lan:WTPONR><lan:W1PLDT>" + iPLDT + "</lan:W1PLDT><lan:W1PLDX>" + iPLDX + "</lan:W1PLDX></lan:OIS260></lan:Lancement></soapenv:Body></soapenv:Envelope>"
      logger.debug("Step 1 body = " + body)
      IonResponse response = ion.post(endpoint, headers, queryParameters, body);
      //logger.debug("CDUVmessage: ${body}")
      if (response.getError()) {
        logger.debug("Failed calling ION API, detailed error message: ${response.getErrorMessage()}")
        logger.debug("Failed calling ION API, detailed error message: ${body}")
        String header = "MSG webservice"
        String message = "Failed calling ION API, detailed error message: ${response.getErrorMessage()}"
        logMessage(header, message)
        return
      }
      if (response.getStatusCode() != 200) {
        logger.debug("Expected status 200 but got ${response.getStatusCode()} instead ${response.getContent()} ")
        logger.debug("Failed calling ION API, detailed error message: ${body}")
        String header = "MSG webservice"
        String message = "Expected status 200 but got ${response.getStatusCode()} instead ${response.getContent()} "
        logMessage(header, message)
        return
      }
    }

    // Delete file EXTJOB
    deleteEXTJOB()
  }
  // Get first parameter
  private String getFirstParameter(){
    logger.debug("rawData = " + rawData)
    rawDataLength = rawData.length()
    beginIndex = 0
    endIndex = rawData.indexOf(";")
    // Get parameter
    String parameter = rawData.substring(beginIndex, endIndex)
    logger.debug("parameter = " + parameter)
    return parameter
  }
  // Get next parameter
  private String getNextParameter(){
    beginIndex = endIndex + 1
    endIndex = rawDataLength - rawData.indexOf(";") - 1
    rawData = rawData.substring(beginIndex, rawDataLength)
    rawDataLength = rawData.length()
    beginIndex = 0
    endIndex = rawData.indexOf(";")
    // Get parameter
    String parameter = rawData.substring(beginIndex, endIndex)
    logger.debug("parameter = " + parameter)
    return parameter
  }
  // Delete records related to the current job from EXTJOB table
  public void deleteEXTJOB(){
    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXTJOB").index("00").build()
    DBContainer EXTJOB = query.getContainer();
    EXTJOB.set("EXRFID", batch.getReferenceId().get())
    if(!query.readAllLock(EXTJOB, 1, updateCallBack_EXTJOB)){
    }
  }
  // Update EXTJOB
  Closure<?> updateCallBack_EXTJOB = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  // Log message
  void logMessage(String header, String message) {
    textFiles.open("FileImport")
    logFileName = "MSG_" + program.getProgramName() + "." + "batch" + "." + jobNumber + ".csv"
    if(!textFiles.exists(logFileName)) {
      log(header)
      log(message);
    }
  }
  // Log
  void log(String message) {
    IN60 = true
    //logger.debug(message)
    message = LocalDateTime.now().toString() + ";" + message
    Closure<?> consumer = { PrintWriter printWriter ->
      printWriter.println(message)
    }
    textFiles.write(logFileName, "UTF-8", true, consumer)
  }
  // Update OOLINE
  Closure<?> updateCallBack_OOLINE = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("OBCHNO")
    if(inREPI != null)
      lockedResult.set("OBREPI", inREPI)
    lockedResult.setInt("OBLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("OBCHNO", changeNumber + 1)
    lockedResult.set("OBCHID", program.getUser())
    lockedResult.update()
  }
  // Update OXLINE
  Closure<?> updateCallBack_OXLINE = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("OBCHNO")
    if(inREPI != null)
      lockedResult.set("OBREPI", inREPI)
    if(iROUT.trim()!=""){
      saved_iROUT = iROUT
      if(iROUT.trim() == "??"){
        iROUT = ""
      }
      lockedResult.set("OBROUT", iROUT)
      iROUT = saved_iROUT
    }
    if(inPLDT != null)
      lockedResult.set("OBPLDT", inPLDT)
    lockedResult.setInt("OBLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("OBCHNO", changeNumber + 1)
    lockedResult.set("OBCHID", program.getUser())
    lockedResult.update()
  }
}
