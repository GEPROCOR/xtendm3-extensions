/**
 * README
 * This extension is used by batch EXT052
 *
 * Name : EXT053
 * Description : Read EXT052 table and call "CRS105MI/AddAssmItem" for each item (EXT053MI.AddAssortItems conversion)
 * Date         Changed By   Description
 * 20220420     RENARN       TARX02 - Add assortment
 * 20220620     RENARN       Logger.info have been removed, method comment added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class EXT053 extends ExtendM3Batch {
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
  private String ascd = ""
  private String cuno = ""
  private String fdat = ""
  private String itno = ""

  public EXT053(LoggerAPI logger, DatabaseAPI database, ProgramAPI program, BatchAPI batch, MICallerAPI miCaller, TextFilesAPI textFiles, UtilityAPI utility) {
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
  // Perform job
  private performActualJob(Optional<String> data){
    if(!data.isPresent()){
      logger.debug("Job reference Id ${batch.getReferenceId().get()} is passed but data was not found")
      return
    }
    rawData = data.get()
    logger.debug("Début performActualJob")
    String inASCD = getFirstParameter()
    String inCUNO = getNextParameter()
    String inFDAT = getNextParameter()

    currentCompany = (Integer)program.getLDAZD().CONO

    // Perform Job
    ascd = inASCD
    cuno = inCUNO

    fdat =""
    if (inFDAT == null){
      String header = "MSG"
      String message = "Date de début est obligatoire"
      logMessage(header, message)
      return
    } else {
      fdat = inFDAT
      if (!utility.call("DateUtil", "isDateValid", fdat, "yyyyMMdd")) {
        String header = "MSG"
        String message = "Date de début est invalide"
        logMessage(header, message)
        return
      }
    }
    logger.debug("EXT053 fdat = " + fdat)

    // Check selection header
    DBAction EXT050_query = database.table("EXT050").index("00").build()
    DBContainer EXT050 = EXT050_query.getContainer()
    EXT050.set("EXCONO", currentCompany)
    EXT050.set("EXASCD", ascd)
    EXT050.set("EXCUNO", cuno)
    EXT050.setInt("EXDAT1", fdat as Integer)
    if(!EXT050_query.readAll(EXT050, 4, EXT050_outData)){
      String header = "MSG"
      String message = "Entête sélection n'existe pas"
      logMessage(header, message)
      return
    }

    DBAction EXT052_query = database.table("EXT052").index("00").selection("EXITNO").build()
    DBContainer EXT052 = EXT052_query.getContainer()
    EXT052.set("EXCONO", currentCompany)
    EXT052.set("EXASCD", ascd)
    EXT052.set("EXCUNO", cuno)
    EXT052.set("EXFDAT", fdat as Integer)
    if (!EXT052_query.readAll(EXT052, 4, EXT052_outData)) {
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
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXTJOB").index("00").build()
    DBContainer EXTJOB = query.getContainer()
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
      log(message)
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
  // Retrieve EXT050
  Closure<?> EXT050_outData = { DBContainer EXT050 ->
  }
  // Retrieve EXT052
  Closure<?> EXT052_outData = { DBContainer EXT052 ->
    itno = EXT052.get("EXITNO")
    logger.debug("executeCRS105MIAddAssmItem : ascd = " + ascd)
    logger.debug("executeCRS105MIAddAssmItem : itno = " + itno)
    logger.debug("executeCRS105MIAddAssmItem : fdat = " + fdat)
    executeCRS105MIAddAssmItem(ascd, itno, fdat)
  }
  // Execute CRS105MI AddAssItem
  private executeCRS105MIAddAssmItem(String ASCD, String ITNO, String FDAT){
    def parameters = ["ASCD": ASCD, "ITNO": ITNO, "FDAT": FDAT]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
      } else {
      }
    }
    miCaller.call("CRS105MI", "AddAssmItem", parameters, handler)
  }
}
