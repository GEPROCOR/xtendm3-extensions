/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT075MI.LstRateSales
 * Description : The LstRateSales transaction list records to the EXT075 table.
 * Date         Changed By   Description
 * 20210510     CDUV         TARX16 - Calcul du tarif
 * 20220204     CDUV         Unused code removed
 * 20220519     CDUV         lowerCamelCase has been fixed
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class LstRateSales extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany
  private String currentDivision
  private final UtilityAPI utility
  private String iCUNO

  public LstRateSales(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
  }
  public void main() {
    String fvdt=""
    String vfdt=""
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    if(mi.in.get("PRRF") == null || mi.in.get("PRRF") == "" ){
      mi.error("Code Tarif est obligatoire")
      return
    }
    if(mi.in.get("CUCD") == null ||  mi.in.get("CUCD") == ""){
      mi.error("Devise est obligatoire")
      return
    }
    if(mi.in.get("FVDT") == null ||  mi.in.get("FVDT") == ""){
      mi.error("Date de début Validité est obligatoire")
      return
    }
    if(mi.in.get("FVDT") != ""){
      fvdt = mi.in.get("FVDT")
      if (!utility.call("DateUtil", "isDateValid", fvdt, "yyyyMMdd")) {
        mi.error("Format Date de début Validité incorrect")
        return
      }
    }
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT075").index("00").selection("EXPRRF","EXCUCD","EXCUNO","EXFVDT","EXFVDT","EXITNO","EXOBV1","EXOBV2","EXOBV3","EXITTY","EXMMO2","EXHIE3","EXHIE2","EXPOPN","EXMOY4","EXSAP4",
      "EXMOY3","EXSAP3","EXTUT2","EXTUT1","EXTUM2","EXTUM1","EXMOY2","EXSAP2","EXMOY1","EXSAP1","EXMOY0","EXREM0","EXSAP0","EXMFIN","EXSAPR","EXZUPA",
      "EXMDIV","EXMCUN","EXMOBJ","EXNEPR","EXPUPR","EXFLAG","EXFPSY","EXZIPL","EXTEDL", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID","EXSUNO","EXAGNB").build()
    DBContainer EXT075 = query.getContainer()
    EXT075.set("EXCONO", currentCompany)
    EXT075.set("EXPRRF", mi.in.get("PRRF"))
    EXT075.set("EXCUCD", mi.in.get("CUCD"))
    EXT075.set("EXCUNO", mi.in.get("CUNO"))
    EXT075.set("EXFVDT", mi.in.get("FVDT") as Integer)

    iCUNO=mi.in.get("CUNO")
    if(iCUNO==null || iCUNO==""){
      DBAction queryEXT080 = database.table("EXT080").index("20").selection("EXCUNO").build()
      DBContainer EXT080 = queryEXT080.getContainer()
      EXT080.set("EXCONO", currentCompany)
      EXT080.set("EXPRRF", mi.in.get("PRRF"))
      EXT080.set("EXCUCD", mi.in.get("CUCD"))
      EXT080.set("EXFVDT", mi.in.get("FVDT") as Integer)
      if(!queryEXT080.readAll(EXT080, 4, outDataEXT080)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
      if(iCUNO==null || iCUNO==""){
        mi.error("Client n'existe pas")
        return
      }
      EXT075.set("EXCUNO", iCUNO)
    }
    String iITNO=mi.in.get("ITNO")
    if(iITNO==null)iITNO=""
    if( iITNO!= ""){
      if(mi.in.get("VFDT") == null || mi.in.get("VFDT") == ""){
        mi.error("Date Date fin Validité est obligatoire")
        return
      }else {
        vfdt = mi.in.get("VFDT")
        if(vfdt!="0"){
          if (!utility.call("DateUtil", "isDateValid", mi.in.get("VFDT"), "yyyyMMdd")) {
            mi.error("Date de début de validité est invalide")
            return
          }
        }
      }

      EXT075.set("EXITNO", mi.in.get("ITNO"))
      EXT075.set("EXOBV1", mi.in.get("OBV1"))
      EXT075.set("EXOBV2", mi.in.get("OBV2"))
      EXT075.set("EXOBV3", mi.in.get("OBV3"))
      EXT075.set("EXVFDT", vfdt as Integer)
      if(!query.readAll(EXT075, 10, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }else{
      if(!query.readAll(EXT075, 5, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }

  }
  // Retrieve outDataEXT080 : EXT080 CUNO
  Closure<?> outDataEXT080 = { DBContainer EXT080 ->
    iCUNO = EXT080.get("EXCUNO")
  }
  // outData : Retrieve EXT075
  Closure<?> outData = { DBContainer EXT075 ->
    String oPRRF = EXT075.get("EXPRRF")
    String oCUCD = EXT075.get("EXCUCD")
    String oCUNO = EXT075.get("EXCUNO")
    String oFVDT = EXT075.get("EXFVDT")
    String oVFDT = EXT075.get("EXFVDT")
    String oITNO = EXT075.get("EXITNO")
    String oOBV1 = EXT075.get("EXOBV1")
    String oOBV2 = EXT075.get("EXOBV2")
    String oOBV3 = EXT075.get("EXOBV3")
    String oASCD = EXT075.get("EXASCD")
    String oITTY = EXT075.get("EXITTY")
    String oMMO2 = EXT075.get("EXMMO2")
    String oHIE3 = EXT075.get("EXHIE3")
    String oHIE2 = EXT075.get("EXHIE2")
    String oPOPN = EXT075.get("EXPOPN")
    String oMOY4 = EXT075.get("EXMOY4")
    String oSAP4 = EXT075.get("EXSAP4")
    String oMOY3 = EXT075.get("EXMOY3")
    String oSAP3 = EXT075.get("EXSAP3")
    String oTUT2 = EXT075.get("EXTUT2")
    String oTUT1 = EXT075.get("EXTUT1")
    String oTUM2 = EXT075.get("EXTUM2")
    String oTUM1 = EXT075.get("EXTUM1")
    String oMOY2 = EXT075.get("EXMOY2")
    String oSAP2 = EXT075.get("EXSAP2")
    String oMOY1 = EXT075.get("EXMOY1")
    String oSAP1 = EXT075.get("EXSAP1")
    String oMOY0 = EXT075.get("EXMOY0")
    String oREM0 = EXT075.get("EXREM0")
    String oSAP0 = EXT075.get("EXSAP0")
    String oMFIN = EXT075.get("EXMFIN")
    String oSAPR = EXT075.get("EXSAPR")
    String oZUPA = EXT075.get("EXZUPA")
    String oMDIV = EXT075.get("EXMDIV")
    String oMCUN = EXT075.get("EXMCUN")
    String oMOBJ = EXT075.get("EXMOBJ")
    String oNEPR = EXT075.get("EXNEPR")
    String oPUPR = EXT075.get("EXPUPR")
    String oFLAG = EXT075.get("EXFLAG")
    String oFPSY = EXT075.get("EXFPSY")
    String oZIPL = EXT075.get("EXZIPL")
    String oTEDL = EXT075.get("EXTEDL")
    String entryDate = EXT075.get("EXRGDT")
    String entryTime = EXT075.get("EXRGTM")
    String changeDate = EXT075.get("EXLMDT")
    String changeNumber = EXT075.get("EXCHNO")
    String changedBy = EXT075.get("EXCHID")
    String oAGNB = EXT075.get("EXAGNB")
    String oSUNO = EXT075.get("EXSUNO")

    mi.outData.put("PRRF", oPRRF)
    mi.outData.put("CUCD", oCUCD)
    mi.outData.put("CUNO", oCUNO)
    mi.outData.put("FVDT", oFVDT)
    mi.outData.put("VFDT", oVFDT)
    mi.outData.put("ITNO", oITNO)
    mi.outData.put("OBV1", oOBV1)
    mi.outData.put("OBV2", oOBV2)
    mi.outData.put("OBV3", oOBV3)
    mi.outData.put("ASCD", oASCD)
    mi.outData.put("ITTY", oITTY)
    mi.outData.put("MMO2", oMMO2)
    mi.outData.put("HIE3", oHIE3)
    mi.outData.put("HIE2", oHIE2)
    mi.outData.put("POPN", oPOPN)
    mi.outData.put("MOY4", oMOY4)
    mi.outData.put("SAP4", oSAP4)
    mi.outData.put("MOY3", oMOY3)
    mi.outData.put("SAP3", oSAP3)
    mi.outData.put("TUT2", oTUT2)
    mi.outData.put("TUT1", oTUT1)
    mi.outData.put("TUM2", oTUM2)
    mi.outData.put("TUM1", oTUM1)
    mi.outData.put("MOY2", oMOY2)
    mi.outData.put("SAP2", oSAP2)
    mi.outData.put("MOY1", oMOY1)
    mi.outData.put("SAP1", oSAP1)
    mi.outData.put("MOY0", oMOY0)
    mi.outData.put("REM0", oREM0)
    mi.outData.put("SAP0", oSAP0)
    mi.outData.put("MFIN", oMFIN)
    mi.outData.put("SAPR", oSAPR)
    mi.outData.put("ZUPA", oZUPA)
    mi.outData.put("MDIV", oMDIV)
    mi.outData.put("MCUN", oMCUN)
    mi.outData.put("MOBJ", oMOBJ)
    mi.outData.put("NEPR", oNEPR)
    mi.outData.put("PUPR", oPUPR)
    mi.outData.put("FLAG", oFLAG)
    mi.outData.put("FPSY", oFPSY)
    mi.outData.put("ZIPL", oZIPL)
    mi.outData.put("TEDL", oTEDL)
    mi.outData.put("SUNO", oSUNO)
    mi.outData.put("AGNB", oAGNB)
    mi.write()
  }
}
