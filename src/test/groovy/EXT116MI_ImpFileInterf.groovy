/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT116MI.ImpFileInterf
 * Description : Generates a file to import and launches EVS100MI (Import file interface).
 * Date         Changed By   Description
 * 20210817     RENARN       TARX02 - Add assortment
 */

import sun.rmi.log.ReliableLog

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class ImpFileInterf extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;
  private final TextFilesAPI textFiles
  private String logFileName
  private boolean IN60
  private String bjno
  private String minm
  private String trnm
  private String usid
  private String zhed
  private String zlin


  public ImpFileInterf(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility, TextFilesAPI textFiles, MICallerAPI miCaller) {
    this.mi = mi;
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility;
    this.textFiles = textFiles
    this.miCaller = miCaller
  }

  public void main() {
    // Save job number
    LocalDateTime timeOfCreation = LocalDateTime.now()
    bjno = timeOfCreation.format(DateTimeFormatter.ofPattern("yyMMdd")) + timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) + program.getJobNumber()

    // Check program name
    if(mi.in.get("MINM") == null){
      mi.error("Nom API est obligatoire")
      return
    } else {
      minm = mi.in.get("MINM")
    }
    // Check transaction name
    if(mi.in.get("TRNM") == null){
      mi.error("Nom transaction est obligatoire")
      return
    } else {
      trnm = mi.in.get("TRNM")
    }
    // Check user
    if(mi.in.get("USID") == null){
      mi.error("Utilisateur est obligatoire")
      return
    } else {
      usid = mi.in.get("USID")
    }
    // Check header
    if(mi.in.get("ZHED") == null){
      mi.error("Entête est obligatoire")
      return
    } else {
      zhed = mi.in.get("ZHED")
    }
    // Check line
    if(mi.in.get("ZLIN") == null){
      mi.error("Ligne est obligatoire")
      return
    } else {
      zlin = mi.in.get("ZLIN")
    }
    createFile()
    executeEVS100MIImportFile(logFileName)
  }
  void createFile() {
    IN60 = false
    textFiles.open("FileImport")
    logFileName = usid + bjno + "-" + minm + "." + trnm + ".csv"
    if(!textFiles.exists(logFileName)){
      log(zhed)
      log(zlin)
    }
  }
  void log(String message) {
    IN60 = true
    //logger.info(message)
    //message = LocalDateTime.now().toString() + ";" + message
    Closure<?> consumer = { PrintWriter printWriter ->
      printWriter.println(message)
    }
    textFiles.write(logFileName, "UTF-8", true, consumer)
  }
  private executeEVS100MIImportFile(String FNAM){
    def parameters = ["FNAM": FNAM]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        IN60 = true
      }
      logger.debug("EXT116MI executeEVS100MIImportFile IN60 = " + IN60)
    }
    miCaller.call("EVS100MI", "ImportFile", parameters, handler)
  }
}
