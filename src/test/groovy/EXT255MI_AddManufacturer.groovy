/**
 * README
 * This extension is used by extensions
 *
 * Name : EXT255MI.AddManufacturer
 * Description : Get general settings by log
 * Date         Changed By   Description
 * 20220211     YOUYVO       REAX28-X AddManufacturer specific GEPROCOR
 * 20220216     YOUYVO       REAX28-X AddManufacturer specific GEPROCOR - complement next back Infor
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.format.DateTimeParseException

public class AddManufacturer extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private Integer nFDAT = 0
  private String iPRIO = "0"
  private Integer nPRIO = 0
  private String iOBV1 = ""
  private String iOBV2 = ""
  private String iAncOBV1 = ""
  private String iAncOBV2 = ""
  private String iAncPROD = ""
  private Integer iCPT = 0
  private String iPROD = ""



  public AddManufacturer(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.miCaller = miCaller
    this.utility = utility
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer) program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    if (mi.in.get("OBV1") == null) {
      mi.error("Code OBV1 est obligatoire")
      return
    } else {
      //Control OBV1
      DBAction query = database.table("CIDMAS").index("00").build()
      DBContainer CIDMAS = query.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO", mi.in.get("OBV1"))
      if(!query.read(CIDMAS)){
        mi.error("Code fournisseur " + mi.in.get("OBV1") + " n'existe pas")
        return
      }
    }
    if (mi.in.get("OBV2") == null) {
      mi.error("Code OBV2 est obligatoire")
      return
    } else {
      //Control OBV2
      DBAction query = database.table("MITMAS").index("00").build()
      DBContainer MITMAS = query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO", mi.in.get("OBV2"))
      if(!query.read(MITMAS)){
        mi.error("Code article " + mi.in.get("OBV2") + " n'existe pas")
        return
      }
    }
    if (mi.in.get("PROD") == null || mi.in.get("PROD") == "") {
      mi.error("Fabricant est obligatoire")
      return
    } else {
      //Control PROD
      DBAction query = database.table("CIDMAS").index("00").build()
      DBContainer CIDMAS_PROD = query.getContainer()
      CIDMAS_PROD.set("IDCONO", currentCompany)
      CIDMAS_PROD.set("IDSUNO", mi.in.get("PROD"))
      if(!query.read(CIDMAS_PROD)){
        mi.error("Fabricant " + mi.in.get("PROD") + " n'existe pas")
        return
      }
    }
    iPRIO = "5"
    nPRIO = 5
    nFDAT = 0
    iCPT = 1
    iPROD = mi.in.get("PROD")
    iOBV1 = mi.in.get("OBV1")
    iOBV2 = mi.in.get("OBV2")
    DBAction queryMPAPMA = database.table("MPAPMA").index("00").selection("AMCONO", "AMPRIO", "AMOBV1", "AMOBV2", "AMOBV3", "AMOBV4", "AMOBV5", "AMFDAT", "AMTDAT", "AMMFRS", "AMPROD", "AMMNFP", "AMLMTS").build()
    DBContainer MPAPMA = queryMPAPMA.getContainer()
    MPAPMA.set("AMCONO", currentCompany)
    MPAPMA.set("AMPRIO", nPRIO)
    MPAPMA.set("AMOBV1", mi.in.get("OBV1"))
    MPAPMA.set("AMOBV2", mi.in.get("OBV2"))
    MPAPMA.set("AMOBV3", "")
    MPAPMA.set("AMOBV4", "")
    MPAPMA.set("AMOBV5", "")
    MPAPMA.set("AMFDAT", nFDAT)
    if (queryMPAPMA.readAll(MPAPMA, 8, updateCallBackMPAPMA)) {
    } else {
      //logger.debug("Insert 1")
      executePPS042MIAddManufacturer(iPRIO, iOBV1, iOBV2, "", "", "", "", iPROD, iPRIO, "", "20")
    }
  }

  //Method to insert records in the MPAPMA file managed in PPS042
  private executePPS042MIAddManufacturer(String PRIO, String OBV1, String OBV2, String OBV3, String OBV4, String OBV5, String FDAT, String PROD, String MNFP, String TDAT, String MFRS){
    def parameters = ["PRIO": PRIO, "OBV1": OBV1, "OBV2": OBV2, "OBV3": OBV3, "OBV4": OBV4, "OBV5": OBV5, "FDAT": FDAT, "PROD": PROD, "MNFP": MNFP, "TDAT": TDAT, "MFRS": MFRS]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed PPS042MI.AddManufacturer: "+ response.errorMessage)
      } else {
      }
    }
    miCaller.call("PPS042MI", "AddManufacturer", parameters, handler)
  }

  //Method to delete records in the MPAPMA file managed in PPS042
  private executePPS042MIDltManufacturer(String PRIO, String OBV1, String OBV2, String OBV3, String OBV4, String OBV5, String FDAT, String PROD){
    def parameters = ["PRIO": PRIO, "OBV1": OBV1, "OBV2": OBV2, "OBV3": OBV3, "OBV4": OBV4, "OBV5": OBV5, "FDAT": FDAT, "PROD": PROD]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed PPS042MI.DltManufacturer: "+ response.errorMessage)
      } else {
      }
    }
    miCaller.call("PPS042MI", "DltManufacturer", parameters, handler)
  }

  // If the combination PRIO, OBV1 to 5, FDAT already exists, the manufacturer is replaced
  Closure<?> updateCallBackMPAPMA = { DBContainer MPAPMA_1 ->
    //logger.debug("MPAPMA existe")
    iAncPROD = MPAPMA_1.get("AMPROD")
    iAncOBV1 = ""
    iAncOBV2 = ""
    if (iPROD.trim() != iAncPROD.trim() && iCPT==1) {
      iCPT += 1
      //logger.debug("MPAPMA PROD different:"+MPAPMA_1.get("AMPROD"))
      DBAction queryNewMPAPMA = database.table("MPAPMA").index("00").selection("AMCONO", "AMPRIO", "AMOBV1", "AMOBV2", "AMOBV3", "AMOBV4", "AMOBV5", "AMFDAT", "AMTDAT", "AMMFRS", "AMPROD", "AMMNFP", "AMLMTS").build()
      DBContainer MPAPMA_New = queryNewMPAPMA.getContainer()
      MPAPMA_New.set("AMCONO", MPAPMA_1.get("AMCONO"))
      MPAPMA_New.set("AMPRIO", MPAPMA_1.get("AMPRIO"))
      MPAPMA_New.set("AMOBV1", MPAPMA_1.get("AMOBV1"))
      MPAPMA_New.set("AMOBV2", MPAPMA_1.get("AMOBV2"))
      MPAPMA_New.set("AMOBV3", MPAPMA_1.get("AMOBV3"))
      MPAPMA_New.set("AMOBV4", MPAPMA_1.get("AMOBV4"))
      MPAPMA_New.set("AMOBV5", MPAPMA_1.get("AMOBV5"))
      MPAPMA_New.set("AMFDAT", MPAPMA_1.get("AMFDAT"))
      MPAPMA_New.set("AMPROD", MPAPMA_1.get("AMPROD"))
      if (queryNewMPAPMA.read(MPAPMA_New)) {
        //logger.debug("MPAPMA lecture ok pour insert")
        MPAPMA_New.set("AMPROD", iPROD)
        if (!queryNewMPAPMA.read(MPAPMA_New)) {
          //logger.debug("MPAPMA delete: "+MPAPMA_1.get("AMPROD"))
          iAncOBV1 = MPAPMA_1.get("AMOBV1")
          iAncOBV2 = MPAPMA_1.get("AMOBV2")
          executePPS042MIDltManufacturer(iPRIO, iAncOBV1, iAncOBV2, "", "", "", "", iAncPROD)
          iAncOBV1 = MPAPMA_1.get("AMOBV1")
          iAncOBV2 = MPAPMA_1.get("AMOBV2")
          executePPS042MIAddManufacturer(iPRIO, iAncOBV1, iAncOBV2, "", "", "", "", iPROD, iPRIO, "", "20")
        }
      }
    }
  }
}
