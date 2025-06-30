/**
 * README
 * This extension is used by IEC
 *
 * Name : EXT258MI.ChgIndexeInfo
 * Description : The ChgIndexeInfo transaction change of orderlines.
 * Date         Changed By   Description
 * 20220328     YVOYOU       CMDX27 - Mass change of orderlines for indexe
 * 20220425     RENARN       Check on status added
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

public class ChgIndexeInfo extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility
  private Integer currentCompany
  private String currentDivision
  private final IonAPI ion
  private Integer iRORC

  public ChgIndexeInfo(MIAPI mi, IonAPI ion, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility) {
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
    if (mi.in.get("PONR") == null) {
      mi.error("Numéro ligne commande est obligatoire")
      return
    }
    if (mi.in.get("POSX") == null) {
      mi.error("Numéro suffixe de ligne de commande est obligatoire")
      return
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
    iRORC = 3
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
  }
  // Update OOLINE
  Closure<?> updateCallBack_OOLINE = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("OBCHNO")
    if(mi.in.get("JDCD") != null)
      lockedResult.set("OBJDCD", mi.in.get("JDCD"))
    if(mi.in.get("UCA5") != null)
      lockedResult.set("OBUCA5", mi.in.get("UCA5"))
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
}

