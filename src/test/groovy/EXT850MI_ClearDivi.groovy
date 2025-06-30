//Clear DIVI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.text.DecimalFormat

public class ClearDivi extends ExtendM3Transaction {

  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private Integer fromCompany
  private Integer nbrLigne
  private String fromDivi
  private String fromfile
  private String clearValeur
  private final TextFilesAPI textFiles
  private String jobNumber
  private String currentDate
  private String logFileName
  private String fileName = ""
  private String directory = ""

  public ClearDivi(MIAPI mi, DatabaseAPI database, ProgramAPI program, MICallerAPI miCaller, TextFilesAPI textFiles) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.miCaller = miCaller
    this.textFiles = textFiles
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      fromCompany = (Integer)program.getLDAZD().CONO
    } else {
      fromCompany = (Integer)mi.in.get("CONO")
    }
    if (mi.in.get("DIVI") == null) {
      mi.error("Societe est obligatoire")
      return
    } else {
      fromDivi = mi.in.get("DIVI")
    }
    if (mi.in.get("FILE") == null) {
      mi.error("Table est obligatoire")
      return
    } else {
      fromfile = mi.in.get("FILE")
    }
    if (mi.in.get("NCON") == null) {
      mi.error("Nom champs CONO est obligatoire")
      return
    }
    if (mi.in.get("NDIV") == null) {
      mi.error("Nom champs DIVI est obligatoire")
      return
    }
    if (mi.in.get("ACTN") == null) {
      mi.error("Nom champs ACTN est obligatoire")
      return
    }
    directory = "EXT850MI"
    // Get job number
    // Get current date
    LocalDateTime timeOfCreation = LocalDateTime.now()
    currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    jobNumber = program.getJobNumber() + timeOfCreation.format(DateTimeFormatter.ofPattern("yyMMdd")) + timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss"))
    //Affectation clearValeur
    clearValeur = "DEL_"+ fromDivi
    //Clear DIVI
    nbrLigne = 0
    def tableName = fromfile
    String fieldNameCONO = mi.in.get("NCON")
    String fieldNameDIVI = mi.in.get("NDIV")
    if (mi.in.get("ACTN") == "COUNT") {
      DBAction query1_Table = database.table(tableName).index("00").build()
      DBContainer TABLE_Container1 = query1_Table.getContainer()
      TABLE_Container1.set(fieldNameCONO, fromCompany)
      TABLE_Container1.set(fieldNameDIVI, fromDivi)
      if(!query1_Table.readAll(TABLE_Container1, 2, outData)){
      }
    }else{
      if (mi.in.get("ACTN") == clearValeur)  {
        DBAction query2_Table = database.table(tableName).index("00").build()
        DBContainer TABLE_Container = query2_Table.getContainer()
        TABLE_Container.set(fieldNameCONO, fromCompany)
        TABLE_Container.set(fieldNameDIVI, fromDivi)
        if(!query2_Table.readAllLock(TABLE_Container, 2, updateCallBack)){
        }
      }
    }
    mi.outData.put("COUN", nbrLigne+"")
    logMessage(fromfile + ";" + nbrLigne+"")
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    nbrLigne +=1
    lockedResult.delete()
  }
  Closure<?> outData = { DBContainer TABLE_Container1 ->
    nbrLigne +=1
  }
  // Log message
  void logMessage(String message) {
    textFiles.open(directory)
    if(fileName == ""){
      logFileName = "EXT850MI_" + fromfile + currentDate + jobNumber + ".csv"
    } else {
      logFileName = fileName
    }
    if(!textFiles.exists(logFileName)) {
      log("NomTable;" + "Count")
      log(message)
    } else {
      log(message)
    }
  }
  // Log
  void log(String message) {
    //logger.info(message)
    message = message
    Closure<?> consumer = { PrintWriter printWriter ->
      printWriter.println(message)
    }
    textFiles.write(logFileName, "UTF-8", true, consumer)
  }
}
