/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT280
 * Description : batch template
 * Date         Changed By   Description
 * 20231208     RENARN       CMDX32 - Extraction of simulated prices
 * 20240115     RENARN       Ajout NETP
 * 20240617     RENARN       BJNO added in the primary key EXT28000, deletion of EXT280 records removed
 * 20240906     YVOYOU       Add EXT281 for history price, replace EXT280 for last price
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class EXT280 extends ExtendM3Batch {
  private final LoggerAPI logger
  private final DatabaseAPI database
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
  private Integer currentDate
  private String inCUNO
  private String inPRRF
  private String inORTP
  private String inWHLO
  private String inWHL2
  private String inWHL3
  private String inWHL4
  private String ocusmaCUCD
  private String sapr
  private String netp
  private String oprbasITNO

  public EXT280(LoggerAPI logger, DatabaseAPI database, ProgramAPI program, BatchAPI batch, MICallerAPI miCaller, TextFilesAPI textFiles, UtilityAPI utility) {
    this.logger = logger
    this.database = database
    this.program = program
    this.batch = batch
    this.miCaller = miCaller
    this.textFiles = textFiles
    this.utility = utility
  }

  public void main() {
    // Get job number
    LocalDateTime timeOfCreation = LocalDateTime.now()
    jobNumber = program.getJobNumber() + timeOfCreation.format(DateTimeFormatter.ofPattern("yyMMdd")) + timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss"))

    logger.debug("Début" + program.getProgramName())
    logger.debug("referenceId = " + batch.getReferenceId().get())
    if (batch.getReferenceId().isPresent()) {
      Optional<String> data = getJobData(batch.getReferenceId().get())
      logger.debug("data = " + data)
      performActualJob(data)
    } else {
      // No job data found
      logger.debug("Job data for job ${batch.getJobId()} is missing")
    }
  }
  // Get job data
  private Optional<String> getJobData(String referenceId) {
    DBAction query = database.table("EXTJOB").index("00").selection("EXDATA").build()
    DBContainer container = query.createContainer()
    container.set("EXRFID", referenceId)
    if (query.read(container)) {
      logger.debug("EXDATA = " + container.getString("EXDATA"))
      return Optional.of(container.getString("EXDATA"))
    } else {
      logger.debug("EXTJOB not found")
    }
    return Optional.empty()
  }
  // Perform actual job
  private performActualJob(Optional<String> data) {
    if (!data.isPresent()) {
      logger.debug("Job reference Id ${batch.getReferenceId().get()} is passed but data was not found")
      return
    }
    rawData = data.get()
    logger.debug("Début performActualJob")
    inCUNO = getFirstParameter()
    inPRRF = getNextParameter()
    inORTP = getNextParameter()
    inWHLO = getNextParameter()
    inWHL2 = getNextParameter()
    inWHL3 = getNextParameter()
    inWHL4 = getNextParameter()

    currentCompany = (Integer) program.getLDAZD().CONO

    LocalDateTime timeOfCreation = LocalDateTime.now()
    currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer

    // Perform Job

    // Customer is mandatory
    if (inCUNO.trim() == "") {
      logger.debug("Customer missing")
      return
    }

    // Order type is mandatory
    if (inORTP.trim() == "") {
      logger.debug("Order type missing")
      return
    }

    // Warehouse is mandatory
    if (inWHLO.trim() == "") {
      logger.debug("Warehouse missing")
      return
    }

    // Check customer
    ocusmaCUCD = ""
    DBAction queryOCUSMA = database.table("OCUSMA").index("00").selection("OKCUCD").build()
    DBContainer OCUSMA = queryOCUSMA.getContainer()
    OCUSMA.set("OKCONO", currentCompany)
    OCUSMA.set("OKCUNO", inCUNO)
    if (!queryOCUSMA.read(OCUSMA)) {
      logger.debug("Customer not found")
      return
    } else {
      ocusmaCUCD = OCUSMA.get("OKCUCD")
    }

    ExpressionFactory expression = database.getExpressionFactory("OPRICH")
    expression = expression.le("OJFVDT", currentDate as String).and(expression.ge("OJLVDT", currentDate as String))
    DBAction queryOPRICH = database.table("OPRICH").index("10").matching(expression).selection("OJPRRF", "OJCUCD", "OJCUNO", "OJFVDT").reverse().build()
    DBContainer OPRICH = queryOPRICH.getContainer()
    if (inPRRF.trim() == "") {
      logger.debug("No inPRRF")
      OPRICH.set("OJCONO", currentCompany)
      OPRICH.set("OJCUCD", ocusmaCUCD)
      OPRICH.set("OJCUNO", "")
      if (!queryOPRICH.readAll(OPRICH, 3, outDataOPRICH)) {
      }
    } else {
      logger.debug("inPRRF = " + inPRRF)
      OPRICH.set("OJCONO", currentCompany)
      OPRICH.set("OJCUCD", ocusmaCUCD)
      OPRICH.set("OJCUNO", "")
      OPRICH.set("OJPRRF", inPRRF)
      if (!queryOPRICH.readAll(OPRICH, 4, outDataOPRICH)) {
      }
    }

    // Delete file EXTJOB
    deleteEXTJOB()
  }
  // Get first parameter
  private String getFirstParameter() {
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
  private String getNextParameter() {
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
  public void deleteEXTJOB() {
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXTJOB").index("00").build()
    DBContainer EXTJOB = query.getContainer()
    EXTJOB.set("EXRFID", batch.getReferenceId().get())
    if (!query.readAllLock(EXTJOB, 1, updateCallBackEXTJOB)) {
    }
  }
  // Delete EXTJOB
  Closure<?> updateCallBackEXTJOB = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  // Log message
  void logMessage(String header, String message) {
    textFiles.open("FileImport")
    logFileName = "MSG_" + program.getProgramName() + "." + "batch" + "." + jobNumber + ".csv"
    if (!textFiles.exists(logFileName)) {
      log(header)
      log(message)
    }
  }
  // Log
  void log(String message) {
    IN60 = true
    logger.debug(message)
    message = LocalDateTime.now().toString() + ";" + message
    Closure<?> consumer = { PrintWriter printWriter ->
      printWriter.println(message)
    }
    textFiles.write(logFileName, "UTF-8", true, consumer)
  }
  // Retrieve OPRICH
  Closure<?> outDataOPRICH = { DBContainer OPRICH ->
    logger.debug("OPRICH found - PRRF = " + OPRICH.get("OJPRRF"))
    DBAction queryOPRBAS = database.table("OPRBAS").index("00").selection("ODPRRF", "ODCUCD", "ODCUNO", "ODFVDT", "ODITNO").reverse().build()
    DBContainer OPRBAS = queryOPRBAS.getContainer()
    OPRBAS.set("ODCONO", currentCompany)
    OPRBAS.set("ODPRRF", OPRICH.get("OJPRRF"))
    OPRBAS.set("ODCUCD", OPRICH.get("OJCUCD"))
    OPRBAS.set("ODCUNO", OPRICH.get("OJCUNO"))
    OPRBAS.set("ODFVDT", OPRICH.get("OJFVDT"))
    if (!queryOPRBAS.readAll(OPRBAS, 5, outDataOPRBAS)) {
    }
  }
  // Retrieve OPRBAS
  Closure<?> outDataOPRBAS = { DBContainer OPRBAS ->
    logger.debug("OPRBAS found - ITNO = " + OPRBAS.get("ODITNO"))
    oprbasITNO = OPRBAS.get("ODITNO")

    if(inWHLO != "") {
      getPrice(inWHLO)
    }
    if(inWHL2 != "") {
      getPrice(inWHL2)
    }
    if(inWHL3 != "") {
      getPrice(inWHL3)
    }
    if(inWHL4 != "") {
      getPrice(inWHL4)
    }
  }
  // Get price per item, Warehouse, price list
  private getPrice(String whlo){
    sapr = ""
    netp = ""
    executeOIS320MIGetPriceLine(inCUNO, inPRRF, inORTP, whlo, oprbasITNO)

    logger.debug("sapr = " + sapr)
    logger.debug("netp = " + netp)
    if(sapr.trim() != "" || netp.trim() != "") {
      LocalDateTime timeOfCreation = LocalDateTime.now()
      // Log of last price calculation
      DBAction query = database.table("EXT280").index("00").build()
      DBContainer EXT280 = query.getContainer()
      EXT280.set("EXCONO", currentCompany)
      EXT280.set("EXCUNO", inCUNO)
      EXT280.set("EXPRRF", inPRRF)
      EXT280.set("EXITNO", oprbasITNO)
      EXT280.set("EXWHLO", whlo)
      if(!query.readLock(EXT280, updateEXT280)){
        logger.debug("write EXT280")
        if(sapr.trim() != "") {
          EXT280.setDouble("EXSAPR", sapr as Double)
        }
        if(netp.trim() != "") {
          EXT280.setDouble("EXNETP", netp as Double)
        }
        EXT280.set("EXBJNO", jobNumber)
        EXT280.setString("EXORTP", inORTP)
        EXT280.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT280.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        EXT280.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT280.setInt("EXCHNO", 1)
        EXT280.set("EXCHID", program.getUser())
        query.insert(EXT280)
      }
      DBAction query1 = database.table("EXT281").index("00").build()
      DBContainer EXT281 = query1.getContainer()
      EXT281.set("EXCONO", currentCompany)
      EXT281.set("EXCUNO", inCUNO)
      EXT281.set("EXPRRF", inPRRF)
      EXT281.set("EXITNO", oprbasITNO)
      EXT281.set("EXWHLO", whlo)
      EXT281.set("EXBJNO", jobNumber)
      if (!query1.read(EXT281)) {
        logger.debug("write EXT281")
        if(sapr.trim() != "") {
          EXT281.setDouble("EXSAPR", sapr as Double)
        }
        if(netp.trim() != "") {
          EXT281.setDouble("EXNETP", netp as Double)
        }
        EXT281.set("EXBJNO", jobNumber)
        EXT281.setString("EXORTP", inORTP)
        EXT281.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT281.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        EXT281.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT281.setInt("EXCHNO", 1)
        EXT281.set("EXCHID", program.getUser())
        query1.insert(EXT281)
      }
    }
  }
  //updateEXT280 : Update EXT280
  Closure<?> updateEXT280 = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    if(sapr.trim() != "") {
      lockedResult.set("EXSAPR", sapr as double)
    }
    if(netp.trim() != "") {
      lockedResult.set("EXNETP", netp as double)
    }
    lockedResult.set("EXORTP", inORTP)
    lockedResult.set("EXBJNO", jobNumber)
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // Execute OIS320MI GetPriceLine
  private executeOIS320MIGetPriceLine(String CUNO, String PRRF, String ORTP, String WHLO, String ITNO){
    Map<String, String> parameters = ["CUNO": CUNO, "PRRF": PRRF, "ORTP": ORTP, "WHLO": WHLO, "ITNO": ITNO]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
      } else {
        sapr = response.SAPR.trim()
        netp = response.NETP.trim()
      }
    }
    miCaller.call("OIS320MI", "GetPriceLine", parameters, handler)
  }
}
