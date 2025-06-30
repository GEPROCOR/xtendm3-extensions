/**
 * README
 * This extension is used by MEC
 *
 * Name : EXT805MI.ImpFileLog
 * Description : Interface log management
 * Date         Changed By  Description
 * 20211026     RENARN      INTI99 Interface log management
 * 20211125     YOUYVO      ZMMN length has been increased
 * 20211215     YOUYVO      coID added
 * 20220419     RENARN      ZBDD and write into EXT806 added
 * 20230223     RENARN      STAT handling has been added
 * 20230228     RENARN      EXT806.set("EXLMTS has been removed
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.text.DecimalFormat
import java.time.ZoneOffset

public class ImpFileLog extends ExtendM3Transaction {
  private final MIAPI mi
  private final ProgramAPI program
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final UtilityAPI utility
  private final MICallerAPI miCaller
  private final TextFilesAPI textFiles
  private String logFileName
  private boolean IN60
  private Integer currentCompany
  private Long lmts
  private String currentDate
  private String jobNumber
  private String MECMappingName = ""
  private String programName = ""
  private String transactionName = ""
  private String messageID = ""
  private String message = ""
  private String dateTime = ""
  private String line = ""
  private String fileName = ""
  private String directory = ""
  private String userID = ""
  private String coID = "0"
  private String interfaceFileName = ""
  private String interfaceFileLine = ""
  private String dataBase = ""

  public ImpFileLog(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility, MICallerAPI miCaller, TextFilesAPI textFiles) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
    this.miCaller = miCaller
    this.textFiles = textFiles
  }
  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Get MEC mapping name
    if(mi.in.get("ZMMN") != "" && mi.in.get("ZMMN") != null)
      MECMappingName = mi.in.get("ZMMN")

    // Get program name
    if(mi.in.get("MINM") != "" && mi.in.get("MINM") != null)
      programName = mi.in.get("MINM")

    // Get transaction name
    if(mi.in.get("TRNM") != "" && mi.in.get("TRNM") != null)
      transactionName = mi.in.get("TRNM")

    // Get message ID
    if(mi.in.get("MSID") != "" && mi.in.get("MSID") != null)
      messageID = mi.in.get("MSID")

    // Get message
    if(mi.in.get("MSGD") != "" && mi.in.get("MSGD") != null)
      message = mi.in.get("MSGD")

    // Get date & time
    if(mi.in.get("ZDTH") != "" && mi.in.get("ZDTH") != null)
      dateTime = mi.in.get("ZDTH")

    // Get line
    if(mi.in.get("ZLIN") != "" && mi.in.get("ZLIN") != null)
      line = mi.in.get("ZLIN")

    // Get file name
    if(mi.in.get("ZFNM") != "" && mi.in.get("ZFNM") != null)
      fileName = mi.in.get("ZFNM")

    // Get interface file name
    if(mi.in.get("ZIFN") != "" && mi.in.get("ZIFN") != null)
      interfaceFileName = mi.in.get("ZIFN")

    // Get interface file line
    if(mi.in.get("ZIFL") != "" && mi.in.get("ZIFL") != null)
      interfaceFileLine = mi.in.get("ZIFL")

    // Message in database
    if(mi.in.get("ZBDD") != "" && mi.in.get("ZBDD") != null)
      dataBase = mi.in.get("ZBDD")

    // Get current date
    LocalDateTime timeOfCreation = LocalDateTime.now()
    currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

    // Get job number
    jobNumber = program.getJobNumber() + timeOfCreation.format(DateTimeFormatter.ofPattern("yyMMdd")) + timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss"))

    LocalDateTime timeOfCreationStamp = LocalDateTime.now()
    lmts = timeOfCreationStamp.toInstant(ZoneOffset.UTC).toEpochMilli()

    // Get directory and user ID
    DBAction query = database.table("EXT805").index("00").selection("EXZREP", "EXUSID", "EXCOID").build()
    DBContainer EXT805 = query.getContainer()
    EXT805.set("EXCONO", currentCompany)
    EXT805.set("EXZMMN",  MECMappingName)
    if(query.read(EXT805)){
      directory =  EXT805.get("EXZREP")
      userID =  EXT805.get("EXUSID")
      coID =  EXT805.get("EXCOID")
    }
    if(dataBase == "1"){
      writeEXT806()
    } else {
      logMessage(MECMappingName + ";" + programName + ";" + transactionName + ";" + messageID + ";" + message + ";" + dateTime + ";" + line + ";" + interfaceFileName + ";" + interfaceFileLine)
    }
    mi.outData.put("ZFNM", logFileName)
    mi.outData.put("USID", userID)
    mi.outData.put("COID", coID)
    mi.write()
  }
  // Log message
  void logMessage(String message) {
    textFiles.open(directory)
    if(fileName == ""){
      logFileName = "LOG_" + currentDate + MECMappingName + jobNumber + ".csv"
    } else {
      logFileName = fileName
    }
    if(!textFiles.exists(logFileName)) {
      log("NomMappingMEC;" + "API;" + "Transaction;" + "CodeMessage;" + "Message;" + "Date/heure;" + "DetailAPI;" + "NomFichierInterface;" + "NoLigne")
      log(message)
    } else {
      log(message)
    }
  }
  // Log
  void log(String message) {
    IN60 = true
    //logger.info(message)
    message = message
    Closure<?> consumer = { PrintWriter printWriter ->
      printWriter.println(message)
    }
    textFiles.write(logFileName, "UTF-8", true, consumer)
  }
  // Write message in EXT806
  void writeEXT806() {
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT806").index("00").build()
    DBContainer EXT806 = query.getContainer()
    EXT806.set("EXCONO", currentCompany)
    EXT806.set("EXLMTS", lmts)
    EXT806.set("EXZMMN",  MECMappingName)
    EXT806.set("EXMINM",  programName)
    EXT806.set("EXTRNM",  transactionName)
    EXT806.set("EXMSID",  messageID)
    EXT806.set("EXMSGD",  message)
    EXT806.set("EXZLIN",  line)
    EXT806.set("EXZIFN",  interfaceFileName)
    EXT806.set("EXZIFL",  interfaceFileLine)
    EXT806.set("EXUSID",  userID)
    EXT806.set("EXCOID",  coID as Integer)
    EXT806.set("EXSTAT",  "20")
    EXT806.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    EXT806.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
    EXT806.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    EXT806.setInt("EXCHNO", 1)
    EXT806.set("EXCHID", program.getUser())
    query.insert(EXT806)
  }
}
