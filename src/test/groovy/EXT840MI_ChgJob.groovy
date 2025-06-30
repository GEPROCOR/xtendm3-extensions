/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT840MI.ChgJob
 * Description : Update records from the CJBCTL table.
 * Date         Changed By   Description
 * 20220405     RENARN       INTX97 - Job routing management
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class ChgJob extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final MICallerAPI miCaller
  private String jobName
  private String selection
  private String STAT
  private Integer JBPR
  private Integer RGDT
  private Integer RGTM
  private String BJNO
  private String QCMD
  private boolean jobOK

  public ChgJob(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.miCaller = miCaller
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Check from job queue
    if(mi.in.get("ZFJQ") != null){
      DBAction query = database.table("CJBQUE").index("00").build()
      DBContainer CJBQUE = query.getContainer()
      CJBQUE.set("J2JOBQ",  mi.in.get("ZFJQ"))
      if (!query.read(CJBQUE)) {
        mi.error("File d'attente source " + mi.in.get("ZFJQ") + " n'existe pas")
        return
      }
    } else {
      mi.error("File d'attente source est obligatoire")
      return
    }
    // Check job name
    if(mi.in.get("ZJNA") == null){
      mi.error("Nom job est obligatoire")
      return
    } else {
      jobName = mi.in.get("ZJNA")
    }
    // Check changed by
    if(mi.in.get("CHID") != null){
      DBAction query = database.table("CMNUSR").index("00").build()
      DBContainer CMNUSR = query.getContainer()
      CMNUSR.set("JUUSID", mi.in.get("CHID"))
      if (!query.read(CMNUSR)) {
        mi.error("Changé par " + mi.in.get("CHID") + " n'existe pas")
        return
      }
    } else {
      mi.error("Changé par est obligatoire")
      return
    }
    // Check to job queue
    if(mi.in.get("ZTJQ") != null){
      DBAction query = database.table("CJBQUE").index("00").build()
      DBContainer CJBQUE = query.getContainer()
      CJBQUE.set("J2JOBQ",  mi.in.get("ZTJQ"))
      if (!query.read(CJBQUE)) {
        mi.error("File d'attente cible " + mi.in.get("ZTJQ") + " n'existe pas")
        return
      }
    } else {
      mi.error("File d'attente cible est obligatoire")
      return
    }
    // Save selection
    selection = ""
    if(mi.in.get("ZSLT") != null){
      selection = mi.in.get("ZSLT")
    }
    ExpressionFactory expression = database.getExpressionFactory("CJBCTL")
    expression = expression.eq("J4JNA", jobName)
    DBAction query = database.table("CJBCTL").index("20").matching(expression).selection("J4STAT", "J4JBPR", "J4RGDT", "J4RGTM", "J4BJNO", "J4JNA", "J4CHID", "J4JOBQ").build()
    DBContainer CJBCTL = query.getContainer()
    CJBCTL.set("J4STAT", "00")
    CJBCTL.set("J4CHID", mi.in.get("CHID"))
    CJBCTL.set("J4JOBQ", mi.in.get("ZFJQ"))
    if(!query.readAll(CJBCTL, 3, outData_CJBCTL)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Retrieve CJBCTL
  Closure<?> outData_CJBCTL = { DBContainer CJBCTL ->
    logger.debug("JNA = " + CJBCTL.get("J4JNA"))
    logger.debug("CHID = " + CJBCTL.get("J4CHID"))
    logger.debug("JOBQ = " + CJBCTL.get("J4JOBQ"))
    logger.debug("BJNO = " + CJBCTL.get("J4BJNO"))
    STAT = CJBCTL.get("J4STAT")
    JBPR = CJBCTL.get("J4JBPR")
    RGDT = CJBCTL.get("J4RGDT")
    RGTM = CJBCTL.get("J4RGTM")
    BJNO = CJBCTL.get("J4BJNO")

    if(selection.trim() != ""){
      logger.debug("Update 1")
      jobOK = false
      DBAction query = database.table("CJBCMD").index("00").selection("CMQCMD").build()
      DBContainer CJBCMD = query.getContainer()
      CJBCMD.set("CMBJNO", BJNO)
      if(!query.readAll(CJBCMD, 1, outData_CJBCMD)){
      }
      if(jobOK){
        logger.debug("Update 11")
        DBAction query_CJBCTL = database.table("CJBCTL").index("00").selection("J4JOBQ").build()
        DBContainer CJBCTL2 = query_CJBCTL.getContainer()
        CJBCTL2.set("J4STAT", STAT)
        CJBCTL2.set("J4JBPR", JBPR)
        CJBCTL2.set("J4RGDT", RGDT)
        CJBCTL2.set("J4RGTM", RGTM)
        CJBCTL2.set("J4BJNO", BJNO)
        if (!query_CJBCTL.readLock(CJBCTL2, updateCallBack)) {
          mi.error("L'enregistrement n'existe pas")
          return
        }
      }
    } else {
      logger.debug("Update 2")
      DBAction query = database.table("CJBCTL").index("00").selection("J4JOBQ").build()
      DBContainer CJBCTL2 = query.getContainer()
      CJBCTL2.set("J4STAT", STAT)
      CJBCTL2.set("J4JBPR", JBPR)
      CJBCTL2.set("J4RGDT", RGDT)
      CJBCTL2.set("J4RGTM", RGTM)
      CJBCTL2.set("J4BJNO", BJNO)
      if (!query.readLock(CJBCTL2, updateCallBack)) {
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }
  // Retrieve CJBCMD
  Closure<?> outData_CJBCMD = { DBContainer CJBCMD ->
    QCMD = CJBCMD.get("CMQCMD")
    if(QCMD.contains(selection)){
      jobOK = true
    }
  }
  // Update CJBCTL
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("J4CHNO")
    if (mi.in.get("ZTJQ") != null)
      lockedResult.set("J4JOBQ", mi.in.get("ZTJQ"))
    lockedResult.setInt("J4LMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("J4CHNO", changeNumber + 1)
    lockedResult.set("J4CHID", program.getUser())
    lockedResult.update()
  }
}
