/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT258MI.MoveDelLn
 * Description : The MoveDelLn transaction change of orderlines and Indexe
 * Date         Changed By   Description
 * 20220404     YVOYOU       CMDX27 - Mass change of orderlines for indexe
 * 20220425     RENARN       Check on status added
 * 20231113     YVOYOU       CMDX27 - Mass change of orderlines for indexe - Add control DLIX
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

public class MoveDelLn extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility
  private final MICallerAPI miCaller
  private Integer currentCompany
  private String currentDivision
  private final IonAPI ion
  private Integer iPONR
  private Integer iPOSX
  private Integer iRORC
  private String iDLIX
  private String iORNO
  private String iTLIX


  public MoveDelLn(MIAPI mi, IonAPI ion, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.ion = ion
    this.utility = utility
    this.miCaller = miCaller
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
    if (mi.in.get("PONR") == null) {
      mi.error("Numéro ligne commande est obligatoire")
      return
    } else {
      iPONR = mi.in.get("PONR")
    }
    if (mi.in.get("POSX") == null) {
      mi.error("Numéro suffixe de ligne de commande est obligatoire")
      return
    } else {
      iPOSX = mi.in.get("POSX")
    }
    iRORC = 3
    if (mi.in.get("DLIX") == null) {
      iDLIX = ""
    } else {
      iDLIX = mi.in.get("DLIX")
    }
    if (mi.in.get("TLIX") == null) {
      iTLIX = ""
    } else {
      iTLIX = mi.in.get("TLIX")
    }
    iORNO = mi.in.get("ORNO")
    if (iDLIX == "" || iDLIX == null) {
      //Retreive indexe
      executeMWS411MILstDelLnByOrd(iRORC, iORNO, iPONR, iPOSX)
    }
    //logger.debug("ESSAI TLIX = " + iTLIX)
    //logger.debug("ESSAI DLIX = " + iDLIX)
    if (iTLIX.trim().replace(".0", "") != iDLIX.trim()) {
      //Change indexe
      executeMWS411MIMoveDelLn(iDLIX, iRORC, iORNO, iPONR, iPOSX, iTLIX)
      if (iTLIX == "" || iTLIX == null) {
        mi.error("Numéro indexe invalide")
        return
      }
    }
    // Update line info
    currentDivision = (String) program.getLDAZD().DIVI
    ExpressionFactory expression = database.getExpressionFactory("OOLINE")
    expression = expression.lt("OBORST", "77")
    DBAction query_OOLINE = database.table("OOLINE").index("00").matching(expression).selection("OBUCA5", "OBJDCD").build()
    DBContainer OOLINE = query_OOLINE.getContainer()
    OOLINE.set("OBCONO", currentCompany)
    OOLINE.set("OBORNO", mi.in.get("ORNO"))
    OOLINE.set("OBPONR", mi.in.get("PONR"))
    OOLINE.set("OBPOSX", mi.in.get("POSX"))
    if (!query_OOLINE.readAllLock(OOLINE, 4, updateCallBack_OOLINE)) {
    }
    // Update indexe line info
    DBAction query_MHDISL = database.table("MHDISL").index("10").selection("URJDCD").build()
    DBContainer MHDISL = query_MHDISL.getContainer()
    MHDISL.set("URCONO", currentCompany)
    MHDISL.set("URRORC", iRORC)
    MHDISL.set("URRIDN", mi.in.get("ORNO"))
    MHDISL.set("URRIDL", mi.in.get("PONR"))
    MHDISL.set("URRIDX", mi.in.get("POSX"))
    if (!query_MHDISL.readAllLock(MHDISL, 5, updateCallBack_MHDISL)) {
    }
    // Update indexe info
    ExpressionFactory expression2 = database.getExpressionFactory("MHDISH")
    expression2 = expression2.lt("OQPGRS", "90")
    DBAction query_MHDISH = database.table("MHDISH").index("00").matching(expression2).selection("OQDCC1").build()
    DBContainer MHDISH = query_MHDISH.getContainer()
    MHDISH.set("OQCONO", currentCompany)
    MHDISH.set("OQINOU", 1)
    MHDISH.set("OQDLIX", Integer.parseInt(iTLIX.trim().replace(".0", "")))
    if (!query_MHDISH.readAllLock(MHDISH, 3, updateCallBack_MHDISH)) {
    }
    mi.outData.put("TLIX", iTLIX)
    mi.write()
  }
  // Update OOLINE
  Closure<?> updateCallBack_OOLINE = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("OBCHNO")
    if(mi.in.get("JDCD") != null)
      lockedResult.set("OBJDCD", mi.in.get("JDCD"))
    lockedResult.setInt("OBLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("OBCHNO", changeNumber + 1)
    lockedResult.set("OBCHID", program.getUser())
    lockedResult.update()
  }
  // Update MHDISL
  Closure<?> updateCallBack_MHDISL = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("URCHNO")
    if(mi.in.get("JDCD") != null)
      lockedResult.set("URJDCD", mi.in.get("JDCD"))
    lockedResult.setInt("URLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("URCHNO", changeNumber + 1)
    lockedResult.set("URCHID", program.getUser())
    lockedResult.update()
  }
  // Update MHDISH
  Closure<?> updateCallBack_MHDISH = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("OQCHNO")
    if(mi.in.get("JDCD") != null)
      lockedResult.set("OQDCC1", mi.in.get("JDCD"))
    lockedResult.setInt("OQLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("OQCHNO", changeNumber + 1)
    lockedResult.set("OQCHID", program.getUser())
    lockedResult.update()
  }
  // Execute MWS411MI.MoveDelLn
  private executeMWS411MIMoveDelLn(String DLIX, Integer RORC, String ORNO, Integer PONR, Integer POSX, String TLIX){
    logger.debug("DLIX = " + DLIX)
    logger.debug("RORC = " + RORC+"")
    logger.debug("ORNO = " + ORNO)
    logger.debug("PONR = " + PONR+"")
    logger.debug("POSX = " + POSX+"")
    logger.debug("TLIX = " + TLIX)
    def parameters = ["DLIX": DLIX+"", "RORC": RORC+"", "RIDN": ORNO, "RIDL": PONR+"", "RIDX": POSX+"", "TLIX": TLIX]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed MWS411MI.MoveDelLn: "+ response.errorMessage)
      }
      logger.debug("TLIX = " + response.TLIX)
      iTLIX = response.TLIX
    }
    miCaller.call("MWS411MI", "MoveDelLn", parameters, handler)
  }
  // Execute MWS411MI.LstDelLnByOrd
  private executeMWS411MILstDelLnByOrd(Integer RORC, String ORNO, Integer PONR, Integer POSX){
    def parameters = ["RORC": RORC+"", "RIDN": ORNO, "RIDL": PONR+"", "RIDX": POSX+""]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed MWS411MI.LstDelLnByOrd: "+ response.errorMessage)
      }
      logger.debug("DLIX = " + response.DLIX)
      iDLIX = Integer.parseInt(response.DLIX.trim())
    }
    miCaller.call("MWS411MI", "LstDelLnByOrd", parameters, handler)
  }
}

