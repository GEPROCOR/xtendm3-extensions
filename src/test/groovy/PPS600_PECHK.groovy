/**
 * README
 *
 * Name : PPS600_PECHK
 * Description : Transport costs: distribution of the amount on PO lines and generation of a dedicated PO
 * Date         Changed By   Description
 * 20210818     RENARN       APPX25 - Management of transport costs
 * 20211005     RENARN       Change of the process according to pusl. Changed ceva and rasn checks.
 * 20220310     RENARN       DWDT is now retrieved from MPLINE
 * 20221028     RENARN       BUYE and PURC are now handled (PPS370MI.AddHead)
 * 20230601     RENARN       Added : DWDT when calling PPS370MI.AddLine, W1PUNO and WWCPPL hnadling
 * 20240527     YVOYOU       CMD61 : Add sale point control
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class PPS600_PECHK extends ExtendM3Trigger {
  private final InteractiveAPI interactive
  private final ProgramAPI program
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final DatabaseAPI database
  private final ExtensionAPI extension
  private boolean IN60
  private String msgn
  private String ceva
  private String faci
  private String whlo
  private String rasn
  private String dwdt
  private String orty
  private String newPuno
  private String PNLI
  private String EXT800_CDSE
  private String EXT800_EXTY
  private String EXT800_CEID
  private String EXT800_ORTY_1
  private String EXT800_ORTY_2
  private String EXT800_ITNO
  private String EXT800_BAOR
  private String error
  private String pusl
  private String newTransport
  private double pupr
  private String buye
  private String purc
  private String suno
  private int maxRecord

  public PPS600_PECHK(InteractiveAPI interactive, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller, DatabaseAPI database, ExtensionAPI extension) {
    this.program = program
    this.interactive = interactive
    this.logger = logger
    this.miCaller = miCaller
    this.database = database
    this.extension = extension
  }

  public void main() {
    // Get general settings
    executeEXT800MIGetParam("PPS600_PECHK")

    String puno
    String WFPUNO = ""
    String WTPUNO = ""
    String W1PUNO = ""
    String WWCPPL = ""
    WFPUNO = interactive.display.fields.WFPUNO
    WTPUNO = interactive.display.fields.WTPUNO
    W1PUNO = interactive.display.fields.W1PUNO
    WWCPPL = interactive.display.fields.WWCPPL
    IN60 = false

    // Processing enabled only if a single PO is selected
    puno = ""
    if (WFPUNO.trim() != "" && WTPUNO.trim() != "" && WFPUNO == WTPUNO) {
      puno = WFPUNO
    } else {
      puno = W1PUNO
    }

    ceva = ""
    faci = ""
    whlo = ""
    rasn = ""
    dwdt = ""
    buye = ""
    purc = ""
    maxRecord = 1000
    logger.debug("Step 1")
    logger.debug("puno = " + puno)
    logger.debug("WWCPPL = " + WWCPPL)
    //Sale point control
    DBAction MPHEAD_query0 = database.table("MPHEAD").index("00").selection("IAFACI", "IAWHLO", "IARASN", "IAORTY", "IAPUSL", "IABUYE", "IAPURC", "IASUNO").build()
    DBContainer MPHEAD0 = MPHEAD_query0.getContainer()
    MPHEAD0.set("IACONO", (Integer)program.getLDAZD().CONO)
    MPHEAD0.set("IAPUNO", puno)
    if (!MPHEAD_query0.readAll(MPHEAD0, 2, maxRecord, outData_MPHEAD)) {
      interactive.showOkDialog("L'OA n'existe pas")
    } else {
      if (suno.startsWith("BA")) {
        //first PO line number control
        ExpressionFactory expression_MPLINE = database.getExpressionFactory("MPLINE")
        expression_MPLINE = expression_MPLINE.le("IBELNO", " ")
        DBAction MPLINE_query = database.table("MPLINE").index("00").matching(expression_MPLINE).selection("IBPNLI", "IBDWDT").build()
        DBContainer MPLINE = MPLINE_query.getContainer()
        MPLINE.set("IBCONO", (Integer)program.getLDAZD().CONO)
        MPLINE.set("IBPUNO", puno)
        if (MPLINE_query.readAll(MPLINE, 2, 1, outData_MPLINE)) {
          IN60 = false
          interactive.showCustomError("WFPUNO", "L'OA ne peut pas être imprimé, il manque l'affectation point de vente")
          return
        }
      }
    }

    // If PO is found and not in copy mode
    if(puno.trim() != "" && (WWCPPL.trim() == "" || WWCPPL.trim() == "0")){
      logger.debug("Step 2")
      orty = ""
      DBAction MPHEAD_query1 = database.table("MPHEAD").index("00").selection("IAFACI", "IAWHLO", "IARASN", "IAORTY", "IAPUSL", "IABUYE", "IAPURC", "IASUNO").build()
      DBContainer MPHEAD1 = MPHEAD_query1.getContainer()
      MPHEAD1.set("IACONO", (Integer)program.getLDAZD().CONO)
      MPHEAD1.set("IAPUNO", puno)
      if (!MPHEAD_query1.readAll(MPHEAD1, 2, maxRecord, outData_MPHEAD)) {
        interactive.showOkDialog("L'OA n'existe pas")
      } else {
        if(orty != EXT800_ORTY_2)
          return
      }
      logger.debug("Step 3")
      // Check status
      if(pusl > "20"){
        interactive.showOkDialog("Statut bas OA = " + pusl + ". Il doit être inférieur ou égal à 20")
        return
      }
      logger.debug("Step 4")
      if(pusl < "20"){
        logger.debug("Step 5")
        // Get first PO line number
        DBAction MPLINE_query = database.table("MPLINE").index("00").selection("IBPNLI", "IBDWDT").build()
        DBContainer MPLINE = MPLINE_query.getContainer()
        MPLINE.set("IBCONO", (Integer)program.getLDAZD().CONO)
        MPLINE.set("IBPUNO", puno)
        if (!MPLINE_query.readAll(MPLINE, 2, 1, outData_MPLINE)) {}

        // Get costing element amount from the first line of the PO
        executePPS215MIGetPOCharge(puno, PNLI, EXT800_CDSE, EXT800_EXTY, EXT800_CEID)
        if(IN60)
          interactive.showOkDialog(error)
        logger.debug("rasn = " + rasn)
        logger.debug("ceva = " + ceva)
        // If costing element amount is found
        if (rasn.trim() != "" && ceva as double != 0){
          // Update PO charge
          executePPS215MIUpdPOCharge(puno, EXT800_CDSE, EXT800_EXTY, EXT800_CEID, ceva)
          if(IN60)
            interactive.showOkDialog(error)
          // Get rail station from the PO
          //logger.debug("PPS600_PECHK rasn = " + rasn)
          LocalDateTime timeOfCreation = LocalDateTime.now()
          String currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
          // Start entry
          executePPS370MIStartEntry(EXT800_BAOR)
          if(IN60)
            interactive.showOkDialog(error)
          // Add new PO
          executePPS370MIAddHead(msgn, faci, whlo, rasn, EXT800_ORTY_1, dwdt, puno, buye, purc)
          if(IN60)
            interactive.showOkDialog(error)
          // Add new PO line
          executePPS370MIAddLine(msgn, newPuno, EXT800_ITNO, "1", ceva, dwdt)
          if(IN60)
            interactive.showOkDialog(error)
          // Finish entry
          executePPS370MIFinishEntry(msgn)
          if(IN60)
            interactive.showOkDialog(error)
        } else {
          if (rasn.trim() == "" && ceva as double != 0) {
            interactive.showCustomError("RASN", "Code transporteur non trouvé")
          }
        }
      } else {
        // If status equal to 20
        newTransport = ""
        ExpressionFactory expression = database.getExpressionFactory("MPHEAD")
        expression = expression.eq("IAYRE1", puno)
        DBAction MPHEAD_query_2 = database.table("MPHEAD").index("30").matching(expression).selection("IAPUNO").build()
        DBContainer MPHEAD_2 = MPHEAD_query_2.getContainer()
        MPHEAD_2.set("IACONO", (Integer) program.getLDAZD().CONO)
        MPHEAD_2.set("IAFACI", faci)
        MPHEAD_2.set("IAORTY", EXT800_ORTY_1)
        if (!MPHEAD_query_2.readAll(MPHEAD_2, 3, maxRecord, outData_MPHEAD_2)) {
        }
        //logger.debug("newTransport = " + newTransport)
        if (newTransport.trim() != "") {
          pupr = 0
          // Get first PO line number
          DBAction MPLINE_query = database.table("MPLINE").index("00").selection("IBPNLI", "IBPUPR").build()
          DBContainer MPLINE = MPLINE_query.getContainer()
          MPLINE.set("IBCONO", (Integer) program.getLDAZD().CONO)
          MPLINE.set("IBPUNO", newTransport)
          if (!MPLINE_query.readAll(MPLINE, 2, 1, outData_MPLINE_2)) {
          }
          //logger.debug("pupr = " + pupr)
          if (pupr != 0) {
            // Update PO charge
            executePPS215MIUpdPOCharge(puno, EXT800_CDSE, EXT800_EXTY, EXT800_CEID, pupr as String)
          }
        }
      }
    }
  }

  // Execute EXT800MI GetParam to retrieve general settings
  private executeEXT800MIGetParam(String EXNM){
    Map<String, String> parameters = ["EXNM": EXNM]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        IN60 = true
        return
      }
      //if (response.P001 != null)
      //EXT800_PNLI = response.P001.trim()
      if (response.P002 != null)
        EXT800_CDSE = response.P002.trim()
      if (response.P003 != null)
        EXT800_EXTY = response.P003.trim()
      if (response.P004 != null)
        EXT800_CEID = response.P004.trim()
      if (response.P005 != null)
        EXT800_ORTY_1 = response.P005.trim()
      if (response.P006 != null)
        EXT800_ITNO = response.P006.trim()
      if (response.P007 != null)
        EXT800_BAOR = response.P007.trim()
      if (response.P008 != null)
        EXT800_ORTY_2 = response.P008.trim()
      //logger.debug("PPS600_PECHK executeEXT800MIGetParam EXT800_CDSE = " + EXT800_CDSE)
      //logger.debug("PPS600_PECHK executeEXT800MIGetParam EXT800_EXTY = " + EXT800_EXTY)
      //logger.debug("PPS600_PECHK executeEXT800MIGetParam EXT800_CEID = " + EXT800_CEID)
      //logger.debug("PPS600_PECHK executeEXT800MIGetParam EXT800_ORTY_1 = " + EXT800_ORTY_1)
      //logger.debug("PPS600_PECHK executeEXT800MIGetParam EXT800_ITNO = " + EXT800_ITNO)
      //logger.debug("PPS600_PECHK executeEXT800MIGetParam EXT800_BAOR = " + EXT800_BAOR)
      //logger.debug("PPS600_PECHK executeEXT800MIGetParam EXT800_ORTY_2 = " + EXT800_ORTY_2)
      //logger.debug("PPS600_PECHK executeEXT800MIGetParam IN60 = " + IN60)
    }
    miCaller.call("EXT800MI", "GetParam", parameters, handler)
  }
  // Execute PPS215MI GetPOCharge to retrieve PO charge
  private executePPS215MIGetPOCharge(String PUNO, String PNLI, String CDSE, String EXTY, String CEID){
    Map<String, String> parameters = ["PUNO": PUNO, "PNLI": PNLI, "CDSE": CDSE, "EXTY": EXTY, "CEID": CEID]
    Closure<?> handler = { Map<String, String> response ->
      IN60 = false
      error = ""
      if (response.error != null) {
        IN60 = true
        error = "executePPS215MIGetPOCharge - " + response.errorMessage
        return
      }
      if (response.CEVA != null)
        ceva = response.CEVA.trim()
      //logger.debug("PPS600_PECHK executePPS215MIGetPOCharge ceva = " + ceva)
      //logger.debug("PPS600_PECHK executePPS215MIGetPOCharge IN60 = " + IN60)
    }
    miCaller.call("PPS215MI", "GetPOCharge", parameters, handler)
  }
  // Execute PPS215MI UpdPOCharge to update PO charge
  private executePPS215MIUpdPOCharge(String PUNO, String CDSE, String EXTY, String CEID, String OVHE){
    Map<String, String> parameters = ["PUNO": PUNO, "CDSE": CDSE, "EXTY": EXTY, "CEID": CEID, "OVHE": OVHE]
    Closure<?> handler = { Map<String, String> response ->
      IN60 = false
      error = ""
      if (response.error != null) {
        IN60 = true
        error = "executePPS215MIUpdPOCharge - " + response.errorMessage
        return
      }
      //logger.debug("PPS600_PECHK executePPS215MIUpdPOCharge IN60 = " + IN60)
    }
    miCaller.call("PPS215MI", "UpdPOCharge", parameters, handler)
  }
  // Execute PPS370MI AddHead to add PO header
  private executePPS370MIAddHead(String MSGN, String FACI, String WHLO, String SUNO, String ORTY, String DWDT, String YRE1, String BUYE, String PURC){
    Map<String, String> parameters = ["MSGN": MSGN, "FACI": FACI, "WHLO": WHLO, "SUNO": SUNO, "ORTY": ORTY, "DWDT": DWDT, "YRE1": YRE1, "BUYE": BUYE, "PURC": PURC]
    Closure<?> handler = { Map<String, String> response ->
      IN60 = false
      error = ""
      if (response.error != null) {
        IN60 = true
        error = "executePPS370MIAddHead - " + response.errorMessage
        return
      }
      if (response.PUNO != null)
        newPuno = response.PUNO.trim()
      //logger.debug("PPS600_PECHK executePPS370MIAddHead newPuno = " + newPuno)
      //logger.debug("PPS600_PECHK executePPS370MIAddHead IN60 = " + IN60)
    }
    miCaller.call("PPS370MI", "AddHead", parameters, handler)
  }
  // Execute PPS370MI AddLine to add PO line
  private executePPS370MIAddLine(String MSGN, String PUNO, String ITNO, String ORQA, String PUPR, String DWDT){
    Map<String, String> parameters = ["MSGN": MSGN, "PUNO": PUNO, "ITNO": ITNO, "ORQA": ORQA, "PUPR": PUPR, "DWDT": DWDT]
    Closure<?> handler = { Map<String, String> response ->
      IN60 = false
      error = ""
      if (response.error != null) {
        IN60 = true
        error = "executePPS370MIAddLine - " + response.errorMessage
        return
      }
      //logger.debug("PPS600_PECHK executePPS370MIAddLine IN60 = " + IN60)
    }
    miCaller.call("PPS370MI", "AddLine", parameters, handler)
  }
  // Execute PPS370MI StartEntry to start the entry of the PO
  private executePPS370MIStartEntry(String BAOR){
    Map<String, String> parameters = ["BAOR": BAOR]
    Closure<?> handler = { Map<String, String> response ->
      IN60 = false
      error = ""
      if (response.error != null) {
        IN60 = true
        error = "executePPS370MIStartEntry - " + response.errorMessage
        return
      }
      if (response.MSGN != null)
        msgn = response.MSGN.trim()
      //logger.debug("PPS600_PECHK executePPS370MIStartEntry msgn = " + msgn)
      //logger.debug("PPS600_PECHK executePPS370MIStartEntry IN60 = " + IN60)
    }
    miCaller.call("PPS370MI", "StartEntry", parameters, handler)
  }
  // Execute PPS370MI FinishEntry to finish the entry of the PO
  private executePPS370MIFinishEntry(String MSGN){
    Map<String, String> parameters = ["MSGN": MSGN]
    Closure<?> handler = { Map<String, String> response ->
      IN60 = false
      error = ""
      if (response.error != null) {
        IN60 = true
        error = "executePPS370MIFinishEntry - " + response.errorMessage
        return
      }
      //logger.debug("PPS600_PECHK executePPS370MIFinishEntry IN60 = " + IN60)
    }
    miCaller.call("PPS370MI", "FinishEntry", parameters, handler)
  }
  // Get MPHEAD
  Closure<?> outData_MPHEAD = { DBContainer MPHEAD ->
    faci = MPHEAD.get("IAFACI")
    whlo = MPHEAD.get("IAWHLO")
    rasn = MPHEAD.get("IARASN")
    orty = MPHEAD.get("IAORTY")
    pusl = MPHEAD.get("IAPUSL")
    buye = MPHEAD.get("IABUYE")
    purc = MPHEAD.get("IAPURC")
    suno = MPHEAD.get("IASUNO")
  }
  // Get MPHEAD
  Closure<?> outData_MPHEAD_2 = { DBContainer MPHEAD ->
    newTransport = MPHEAD.get("IAPUNO")
  }
  // Get MPLINE
  Closure<?> outData_MPLINE = { DBContainer MPLINE ->
    PNLI = MPLINE.get("IBPNLI")
    dwdt = MPLINE.get("IBDWDT")
  }
  // Get MPLINE
  Closure<?> outData_MPLINE_2 = { DBContainer MPLINE ->
    pupr = MPLINE.get("IBPUPR")
  }
}
