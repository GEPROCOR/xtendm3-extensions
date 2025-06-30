/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT100MI.UpdAlias
 * Description : Update alias number in MMS025.
 * Date         Changed By   Description
 * 20211026     RENARN       REAX02 Recherche ITM8-EAN13
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdAlias extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private final UtilityAPI utility
  private final TextFilesAPI textFiles
  private Integer currentCompany
  private Integer alwt
  private String alwq
  private String itno
  private String popn
  private String e0pa
  private double cnqt
  private String alun
  private String remk
  private String unms = ""
  private String logFileName
  private boolean IN60

  public UpdAlias(MIAPI mi, DatabaseAPI database, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility, TextFilesAPI textFiles) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.miCaller = miCaller
    this.utility = utility
    this.textFiles = textFiles
  }

  public void main() {
    currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer) program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    if (mi.in.get("ALWT") != "" && mi.in.get("ALWT") != null) {
      alwt = mi.in.get("ALWT") as Integer
      if(alwt < 1 || alwt > 9) {
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Catégorie référence complémentaire " + alwt + " est invalide" + ";" + mi.in.get("ALWT") + ";" + mi.in.get("ALWQ") + ";" + mi.in.get("ITNO")+ ";" + mi.in.get("POPN") + ";" + mi.in.get("E0PA") + ";" + mi.in.get("CNQT") + ";" + mi.in.get("ALUN") + ";" + mi.in.get("REMK"))
        } else {
          mi.error("Catégorie référence complémentaire " + alwt + " est invalide")
          return
        }
      }
    }

    if (mi.in.get("ALWQ") != "" && mi.in.get("ALWQ") != null) {
      alwq = mi.in.get("ALWQ")
      if(mi.in.get("ALWQ") != "EA08" && mi.in.get("ALWQ") != "EA13" && mi.in.get("ALWQ") != "DU14" && mi.in.get("ALWQ") != "GTIN" && mi.in.get("ALWQ") != "UPC" && mi.in.get("ALWQ") != "ITM8") {
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Déterminateur référence complémentaire " + mi.in.get("ALWQ") + " est invalide" + ";" + mi.in.get("ALWT") + ";" + mi.in.get("ALWQ") + ";" + mi.in.get("ITNO")+ ";" + mi.in.get("POPN") + ";" + mi.in.get("E0PA") + ";" + mi.in.get("CNQT") + ";" + mi.in.get("ALUN") + ";" + mi.in.get("REMK"))
        } else {
          mi.error("Déterminateur référence complémentaire " + mi.in.get("ALWQ") + " est invalide")
          return
        }
      }
    }

    if (mi.in.get("ITNO") != "" && mi.in.get("ITNO") != null){
      itno = mi.in.get("ITNO")
      DBAction Query = database.table("MITMAS").index("00").selection("MMUNMS").build()
      DBContainer MITMAS = Query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO",  mi.in.get("ITNO"))
      if (!Query.read(MITMAS)) {
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Code article " + mi.in.get("ITNO") + " n'existe pas" + ";" + mi.in.get("ALWT") + ";" + mi.in.get("ALWQ") + ";" + mi.in.get("ITNO")+ ";" + mi.in.get("POPN") + ";" + mi.in.get("E0PA") + ";" + mi.in.get("CNQT") + ";" + mi.in.get("ALUN") + ";" + mi.in.get("REMK"))
        } else {
          mi.error("Code article " + mi.in.get("ITNO") + " n'existe pas")
          return
        }
      } else {
        unms = MITMAS.get("MMUNMS")
      }
    }

    if (mi.in.get("POPN") != "" && mi.in.get("POPN") != null)
      popn = mi.in.get("POPN")

    if (mi.in.get("E0PA") != "" && mi.in.get("E0PA") != null)
      e0pa = mi.in.get("E0PA")

    if (mi.in.get("CNQT") != "" && mi.in.get("CNQT") != null)
      cnqt = mi.in.get("CNQT") as double

    if (mi.in.get("ALUN") != "" && mi.in.get("ALUN") != null) {
      alun = mi.in.get("ALUN")
      if(alun.trim() != unms.trim()){
        DBAction query = database.table("MITAUN").index("00").build()
        DBContainer MITAUN = query.getContainer()
        MITAUN.set("MUCONO", currentCompany)
        MITAUN.set("MUITNO", itno)
        MITAUN.set("MUAUTP", 1)
        MITAUN.set("MUALUN", alun)
        if (!query.read(MITAUN)) {
          if(mi.in.get("FPNM") == "EVS101"){
            logMessage("Unité alternative " + mi.in.get("ALUN") + " n'existe pas" + ";" + mi.in.get("ALWT") + ";" + mi.in.get("ALWQ") + ";" + mi.in.get("ITNO")+ ";" + mi.in.get("POPN") + ";" + mi.in.get("E0PA") + ";" + mi.in.get("CNQT") + ";" + mi.in.get("ALUN") + ";" + mi.in.get("REMK"))
          } else {
            mi.error("Unité alternative " + mi.in.get("ALUN") + " n'existe pas")
            return
          }
        }
      }
    }

    if (mi.in.get("REMK") != "" && mi.in.get("REMK") != null)
      remk = mi.in.get("REMK")

    if(mi.in.get("FPNM") == "EVS101" && IN60)
      return;

    DBAction query = database.table("MITPOP").index("00").selection("MPSEA1", "MPVFDT").build()
    DBContainer MITPOP = query.getContainer()
    MITPOP.set("MPCONO", currentCompany)
    MITPOP.set("MPALWT", alwt)
    MITPOP.set("MPALWQ", alwq)
    MITPOP.set("MPITNO", itno)
    MITPOP.set("MPPOPN", popn)
    MITPOP.set("MPE0PA", e0pa)
    if (!query.readAll(MITPOP, 6, outData)) {
      if(mi.in.get("FPNM") == "EVS101"){
        logMessage("Référence complémentaire n'existe pas" + ";" + mi.in.get("ALWT") + ";" + mi.in.get("ALWQ") + ";" + mi.in.get("ITNO")+ ";" + mi.in.get("POPN") + ";" + mi.in.get("E0PA") + ";" + mi.in.get("CNQT") + ";" + mi.in.get("ALUN") + ";" + mi.in.get("REMK"))
      } else {
        mi.error("Référence complémentaire n'existe pas")
        return
      }
    }
  }
  void init() {
    IN60 = false
  }
  Closure<?> outData = { DBContainer MITPOP ->
    DBAction query = database.table("MITPOP").index("00").selection("MPCHNO", "MPCNQT", "MPALUN", "MPREMK").build()
    DBContainer MITPOP2 = query.getContainer()
    MITPOP2.set("MPCONO", currentCompany)
    MITPOP2.set("MPALWT", alwt)
    MITPOP2.set("MPALWQ", alwq)
    MITPOP2.set("MPITNO", itno)
    MITPOP2.set("MPPOPN", popn)
    MITPOP2.set("MPE0PA", e0pa)
    MITPOP2.set("MPSEA1", MITPOP.get("MPSEA1"))
    MITPOP2.set("MPVFDT", MITPOP.get("MPVFDT"))
    if(!query.readLock(MITPOP2, updateCallBack)){

    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("MPCHNO")
    if (mi.in.get("CNQT") != null)
      lockedResult.set("MPCNQT", cnqt)
    if (mi.in.get("ALUN") != null)
      lockedResult.set("MPALUN", alun)
    if (mi.in.get("REMK") != null)
      lockedResult.set("MPREMK", remk)
    lockedResult.setInt("MPLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("MPCHNO", changeNumber + 1)
    lockedResult.set("MPCHID", program.getUser())
    lockedResult.update()
  }
  void logMessage(String message) {
    textFiles.open("FileImport")
    logFileName = "MSG_" + program.getProgramName() + "." + "AddItemPack" + ".csv"
    if(!textFiles.exists(logFileName)) {
      log("MSG;"+"ITNO;"+"PACT;"+"PAMU")
      log(message);
    }
  }
  void log(String message) {
    IN60 = true
    //logger.info(message)
    message = LocalDateTime.now().toString() + ";" + message
    Closure<?> consumer = { PrintWriter printWriter ->
      printWriter.println(message)
    }
    textFiles.write(logFileName, "UTF-8", true, consumer)
  }
}
