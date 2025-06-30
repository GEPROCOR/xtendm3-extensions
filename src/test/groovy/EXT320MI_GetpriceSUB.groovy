/**
 * README
 * This extension is used by MEC
 *
 * Name : EXT320MI.GetPriceSUB
 * Description : Retrieve price SUB.
 * Date         Changed By   Description
 * 20240717     YVOYOU       TAR19 - Get price SUB
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class GetPriceSUB extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final MICallerAPI miCaller
  private Integer currentCompany
  private String currentDivi
  private String inCUNO
  private String inITNO
  private String inWHLO
  private String inORTP
  private String inPRRF
  private String inSUNO
  private String inAGNB
  private String outSAPR
  //private int coef
  private double numSAPR
  private boolean IN60

  public GetPriceSUB(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.miCaller = miCaller
  }

  public void main() {
    currentCompany = (Integer)program.getLDAZD().CONO
    currentDivi = program.getLDAZD().DIVI

    IN60 = false
    outSAPR = ""

    if (mi.in.get("PUNO") == null) {
      mi.error("Numéro OA est obligatoire")
      return
    }

    if(mi.in.get("PNLI") == null){
      mi.error("Numéro ligne OA est obligatoire")
      return
    }

    if (mi.in.get("PNLS") == null) {
      mi.error("Numéro sous ligne OA est obligatoire")
      return
    }

    if (mi.in.get("ORTP") == null) {
      mi.error("Type de commande est obligatoire")
      return
    } else {
      inORTP = mi.in.get("ORTP")
      DBAction query = database.table("OOTYPE").index("00").build()
      DBContainer OOTYPE = query.getContainer()
      OOTYPE.set("OOCONO", currentCompany)
      OOTYPE.set("OOORTP", mi.in.get("ORTP"))
      if(!query.read(OOTYPE)){
        mi.error("type de commande " + mi.in.get("ORTP") + " n'existe pas")
        return
      }
    }

    if (mi.in.get("WHLO") == null) {
      mi.error("Code dépot est obligatoire")
      return
    }else{
      inWHLO = mi.in.get("WHLO")
    }

    if (mi.in.get("CUNO") == null) {
      mi.error("Code client est obligatoire")
      return
    } else {
      inCUNO = mi.in.get("CUNO")
      DBAction query = database.table("OCUSMA").index("00").build()
      DBContainer OCUSMA = query.getContainer()
      OCUSMA.set("OKCONO", currentCompany)
      OCUSMA.set("OKCUNO", mi.in.get("CUNO"))
      if(!query.read(OCUSMA)){
        mi.error("Code cleint " + mi.in.get("CUNO") + " n'existe pas")
        return
      }
    }

    if (mi.in.get("PRRF") == null) {
      mi.error("Code tarif est obligatoire")
      return
    }else{
      inPRRF = mi.in.get("PRRF")
    }

    if (mi.in.get("ITNO") == null) {
      mi.error("Code article est obligatoire")
      return
    } else {
      inITNO = mi.in.get("ITNO")
      DBAction query = database.table("MITMAS").index("00").build()
      DBContainer MITMAS = query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO", mi.in.get("ITNO"))
      if(!query.read(MITMAS)){
        mi.error("Code article " + mi.in.get("ITNO") + " n'existe pas")
        return
      }
    }

    if (mi.in.get("AGNB") == null) {
      mi.error("Numéro de contrat est obligatoire")
      return
    }else{
      inAGNB = 	mi.in.get("AGNB")
    }

    executeOIS320MIGetPriceLine(inCUNO, inITNO, inWHLO, inORTP, inPRRF)
    if (IN60==true || outSAPR=="") {
      logger.debug("Step0")
      //Seach SUNO
      IN60 = false
      DBAction query0 = database.table("MPHEAD").index("00").selection("IASUNO").build()
      DBContainer MPHEAD = query0.getContainer()
      MPHEAD.set("IACONO", currentCompany)
      MPHEAD.set("IAPUNO", mi.in.get("PUNO"))
      if(!query0.readAll(MPHEAD, 2, 1, outDataMPHEAD)){
      }
      executePPS295MILstPurCostSim(inWHLO, inITNO, inSUNO, inAGNB)
      if (IN60==false) {
        logger.debug("Step1")
        //Seach COEF
        DBAction query1 = database.table("CUGEX1").index("00").selection("F1N096").build()
        DBContainer CUGEX1 = query1.getContainer()
        CUGEX1.set("F1CONO", currentCompany)
        CUGEX1.set("F1FILE", "CMNDIV")
        CUGEX1.set("F1PK01", currentDivi)
        if(!query1.readAll(CUGEX1, 3, 1, outDataCUGEX1)){
        }
      }
    }
    logger.debug("Step3")
    mi.outData.put("SAPR", outSAPR)
    mi.write()
  }
  // Retrieve MPHEAD
  Closure<?> outDataMPHEAD = { DBContainer MPHEAD ->
    // Get item criteria value
    inSUNO = MPHEAD.get("IASUNO")
  }
  // Retrieve CUGEX1
  Closure<?> outDataCUGEX1 = { DBContainer CUGEX1 ->
    // Get item criteria value
    logger.debug("Step2")
    double coefDouble = CUGEX1.get("F1N096")
    logger.debug("Step2Bis")

    logger.debug("Step2Ter")
    logger.debug("outSAPR:"+outSAPR.trim())
    logger.debug("coef:"+coefDouble)
    if (outSAPR.trim() !="" && coefDouble !=0d) {
      logger.debug("Step2Qua")
      numSAPR = outSAPR.toDouble()
      logger.debug("Step2Cin")
      numSAPR = numSAPR * coefDouble
      outSAPR = numSAPR.toString()
    }
  }

  // Execute OIS320MI GetPriceLine
  private executeOIS320MIGetPriceLine(String CUNO, String ITNO, String WHLO, String ORTP, String PRRF){
    Map<String, String> parameters = ["CUNO": CUNO, "ITNO": ITNO, "WHLO": WHLO, "ORTP": ORTP, "PRRF": PRRF]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        IN60 = true
        return
      }
      if (response.SAPR != null)
        outSAPR = response.SAPR.trim()
      logger.debug("executeOIS320MIGetPriceLine = " + outSAPR)
    }
    miCaller.call("OIS320MI", "GetPriceLine", parameters, handler)
  }
  // Execute PPS295MI LstPurCostSim
  private executePPS295MILstPurCostSim(String WHLO, String ITNO, String SUNO, String AGNB){
    Map<String, String> parameters = ["ITNO": ITNO, "WHLO": WHLO, "SUNO": SUNO, "AGNB": AGNB]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        IN60 = true
        return
      }
      if (response.PUPR != null)
        outSAPR = response.PUPR.trim()
      logger.debug("executePPS295MILstPurCostSim = " + outSAPR)
    }
    miCaller.call("PPS295MI", "LstPurCostSim", parameters, handler)
  }

}
