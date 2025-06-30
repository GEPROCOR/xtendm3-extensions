/**
 * README
 * This extension is launched by Eventhub
 *
 * Name : EXT083MI.MngPrcLstAuto
 * Description : Execute EXT082MI.MngPrcLst for price list corresponding to the "Automatic update" received as a parameter
 * Date         Changed By   Description
 * 20210630     RENARN       TARX03 - Add price list
 * 20210922     RENARN       The extension has been completely rewritten
 * 20220214     RENARN       EXT116MI.ImpFileInterf has been replaced by EXT820MI.SubmitBatch
 * 20221013	  	YOUYVO	  	 Add Business Chain Management
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class MngPrcLstAuto extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private int currentCompany
  private String cuno
  private String itno
  private String prrf
  private String cucd
  private String fvdt
  private String ascd
  private String fdat
  private String minm
  private String trnm
  private String usid
  private String OIS040_CUNO = ""


  public MngPrcLstAuto(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
    this.miCaller = miCaller
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer) program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    // Check automatic update
    int zupd = 0
    if(mi.in.get("ZUPD") != null){
      zupd = mi.in.get("ZUPD")
      if(zupd != 0 && zupd != 1 && zupd != 2 && zupd != 3){
        mi.error("Mise à jour auto doit être 0, 1, 2 ou 3")
        return
      }
    }

    // Update status to 35 for the target price list
    LocalDateTime timeOfCreation = LocalDateTime.now()
    ExpressionFactory EXT080_expression = database.getExpressionFactory("EXT080")
    EXT080_expression = EXT080_expression.le("EXFVDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
    EXT080_expression = EXT080_expression.and(EXT080_expression.ge("EXLVDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd"))))
    DBAction EXT080_query = database.table("EXT080").index("10").matching(EXT080_expression).selection("EXCONO", "EXPRRF", "EXCUCD", "EXCUNO", "EXFVDT").build()
    DBContainer EXT080 = EXT080_query.getContainer()
    EXT080.setInt("EXCONO", currentCompany)
    EXT080.setInt("EXZUPD", zupd)
    if(!EXT080_query.readAllLock(EXT080, 2, updateCallBack_EXT080)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
    // Add item to the price list
    ExpressionFactory EXT080_expression2 = database.getExpressionFactory("EXT080")
    EXT080_expression2 = EXT080_expression2.le("EXFVDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
    EXT080_expression2 = EXT080_expression2.and(EXT080_expression2.ge("EXLVDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd"))))
    DBAction EXT080_query2 = database.table("EXT080").index("10").matching(EXT080_expression2).selection("EXCONO", "EXPRRF", "EXCUCD", "EXCUNO", "EXFVDT").build()
    DBContainer EXT080_2 = EXT080_query2.getContainer()
    EXT080_2.setInt("EXCONO", currentCompany)
    EXT080_2.setInt("EXZUPD", zupd)
    if(!EXT080_query2.readAll(EXT080_2, 2, EXT080_outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Retrieve EXT080
  Closure<?> EXT080_outData = { DBContainer EXT080 ->
    prrf = EXT080.get("EXPRRF")
    cucd = EXT080.get("EXCUCD")
    cuno = EXT080.get("EXCUNO")
    fvdt = EXT080.get("EXFVDT")
    // Update related EXT081 with status 10
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT081").index("00").build()
    DBContainer EXT081 = query.getContainer()
    EXT081.set("EXCONO", currentCompany)
    EXT081.set("EXPRRF", prrf)
    EXT081.set("EXCUCD", cucd)
    EXT081.set("EXCUNO", cuno)
    EXT081.set("EXFVDT", fvdt as Integer)
    if(!query.readAllLock(EXT081, 5, updateCallBack)){}
    // If the item matches the selection, it is added to the price list
    //executeEXT082MIMngPrcLst(prrf, cucd, cuno, fvdt, "2", itno)
  }
  // Update EXT081
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    ascd = lockedResult.get("EXASCD")
    fdat = lockedResult.get("EXFDAT")
    // Update status to 35
    lockedResult.set("EXSTAT", "35")
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()

    //Update each assortment by calling EXT052
    //executeEXT820MISubmitBatch(currentCompany as String, "EXT052", ascd, cuno, fdat, "", "2", prrf, cucd, fvdt)
    OIS040_CUNO = cuno
    executeEXT820MISubmitBatch(currentCompany as String, "EXT052", ascd, OIS040_CUNO, fdat, "", "2", prrf, cucd, fvdt, cuno)
  }
  //Search Business Chain
  private executeOIS040MILstBusChainCust(String chct, String nftr) {
    def parameters = ["CHCT": chct, "NFTR": nftr]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return
      }
      if (response.CUNO != null) {
        OIS040_CUNO = response.CUNO.trim()
      }else{
        OIS040_CUNO = cuno
      }
    }
    miCaller.call("OIS040MI", "LstBusChainCust", parameters, handler)
  }
  // Update EXT080
  Closure<?> updateCallBack_EXT080 = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    // Update status to 35
    lockedResult.set("EXSTAT", "35")
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // Execute EXT082MI.MngPrcLst
  private executeEXT082MIMngPrcLst(String PRRF, String CUCD, String CUNO, String FVDT, String OPT2, String ITNO){
    def parameters = ["PRRF": PRRF,"CUCD": CUCD, "CUNO": CUNO, "FVDT": FVDT, "OPT2": OPT2, "ITNO": ITNO]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
      } else {
      }
    }
    miCaller.call("EXT082MI", "MngPrcLst", parameters, handler)
  }
  // Execute EXT820MI.SubmitBatch
  private executeEXT820MISubmitBatch(String CONO, String JOID, String P001, String P002, String P003, String P004, String P005, String P006, String P007, String P008, String P009){
    def parameters = ["CONO": CONO, "JOID": JOID, "P001": P001, "P002": P002, "P003": P003, "P004": P004, "P005": P005, "P006": P006, "P007": P007, "P008": P008, "P009": P009]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
      } else {
      }
    }
    miCaller.call("EXT820MI", "SubmitBatch", parameters, handler)
  }
}
