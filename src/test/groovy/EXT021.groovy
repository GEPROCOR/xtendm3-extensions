/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT021
 * Description : Manage final sales point (EXT021MI.MngFinalSalesPt conversion)
 * Date         Changed By   Description
 * 20210824     RENARN       CMDX06 - Gestion des points de vente
 */

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat

public class EXT021 extends ExtendM3Batch {
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
  private String jobNumber
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

  public EXT021(LoggerAPI logger, DatabaseAPI database, ProgramAPI program, BatchAPI batch, MICallerAPI miCaller, TextFilesAPI textFiles, UtilityAPI utility) {
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
    String inTYPE = getFirstParameter()
    String inFPVT = getNextParameter()
    String inTPVT = getNextParameter()
    String inDATE = getNextParameter()
    String inLFRS = getNextParameter()

    logger.debug("inTYPE = " + inTYPE)
    logger.debug("inFPVT = " + inFPVT)
    logger.debug("inTPVT = " + inTPVT)
    logger.debug("inDATE = " + inDATE)
    logger.debug("inLFRS = " + inLFRS)

    currentCompany = (Integer)program.getLDAZD().CONO

    // Perform Job
    // Get general settings
    executeEXT800MIGetParam("EXT021MI_MngFinalSalesPt")

    type = ""
    if (inTYPE == null || inTYPE == "") {
      String header = "MSG;"+"TYPE"
      String message = "Type point de vente est obligatoire" + ";" + inTYPE
      logMessage(header, message)
      return
    } else {
      type = inTYPE
    }
    fpvt = ""
    if (inFPVT == null || inFPVT == "") {
      String header = "MSG;"+"FPVT"
      String message = "Point de vente début obligatoire" + ";" + inFPVT
      logMessage(header, message)
      return
    } else {
      fpvt = inFPVT
    }
    tpvt = ""
    if (inTPVT == null || inTPVT == "") {
      String header = "MSG;"+"TPVT"
      String message = "Point de vente fin obligatoire" + ";" + inTPVT
      logMessage(header, message)
      return
    } else {
      tpvt = inTPVT
    }
    date = ""
    if (inDATE == null || inDATE == "") {
      String header = "MSG;"+"DATE"
      String message = "Date de référence est obligatoire" + ";" + inDATE
      logMessage(header, message)
      return;
    } else {
      date = inDATE;
      if (!utility.call("DateUtil", "isDateValid", date, "yyyyMMdd")) {
        String header = "MSG;"+"DATE"
        String message = "Date de référence est invalide" + ";" + inDATE
        logMessage(header, message)
        return;
      }
    }
    if (inLFRS == null || inLFRS == "") {
      String header = "MSG;"+"LFRS"
      String message = "Liste fournisseurs obligatoire" + ";" + inLFRS
      logMessage(header, message)
      return
    } else {
      lfrs = inLFRS
    }

    // Calculating the target date range for creating EXT021
    // = DATE received in parameter +/- number of days configured in EXT800
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
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
            EXT021_2.setInt("EXFDAT", target_fdat);
            EXT021_2.setInt("EXTDAT", target_tdat);
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
  // Delete EXTJOB
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
  // Execute EXT800MI Get param
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
