/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT075MI.UpdRateSales
 * Description : The UpdRateSales transaction update records to the EXT075 table.
 * Date         Changed By   Description
 * 20210510     CDUV         TARX16 - Calcul du tarif
 * 20220204     CDUV         Method commend added, logger and semicolon removed
 * 20220519     CDUV         lowerCamelCase has been fixed
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdRateSales extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private Integer currentCompany
  private String iASCD
  private String iFVDT
  private String iVFDT
  private String iCUNO
  private String iITNO
  private String iPRRF
  private String iCUCD
  private String iOBV1
  private String iOBV2
  private String iOBV3
  private String iITTY
  private String iMMO2
  private String iHIE3
  private String iHIE2
  private String iPOPN
  private String iMOY4
  private String iSAP4
  private String iMOY3
  private String iSAP3
  private String iTUT2
  private String iTUT1
  private String iTUM2
  private String iTUM1
  private String iMOY2
  private String iSAP2
  private String iMOY1
  private String iSAP1
  private String iMOY0
  private String iREM0
  private String iSAP0
  private String iMFIN
  private String iSAPR
  private String iZUPA
  private String iMDIV
  private String iMCUN
  private String iMOBJ
  private String iNEPR
  private String iPUPR
  private String iFLAG
  private String iFPSY
  private String iZIPL
  private String iTEDL

  private String iAGNB
  private String iSUNO
  private String obv1
  private String obv2
  private String obv3
  private String vfdt

  public UpdRateSales(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
  }

  public void main() {
    InitValue()

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
    }
    if(mi.in.get("ITNO") == null || mi.in.get("ITNO") == ""){
      mi.error("Code Article est obligatoire")
      return
    }

    if(mi.in.get("FVDT") == null || mi.in.get("FVDT") == ""){
      mi.error("Date Date Début Validité est obligatoire")
      return
    }else {
      if(mi.in.get("FVDT") != ""){
        iFVDT = mi.in.get("FVDT")
        if (!utility.call("DateUtil", "isDateValid", mi.in.get("FVDT"), "yyyyMMdd")) {
          mi.error("Date de début de validité est invalide")
          return
        }
      }
    }
    if(mi.in.get("VFDT") == null || mi.in.get("VFDT") == ""){
      mi.error("Date Date Fin Validité est obligatoire")
      return
    }else {
      iVFDT="0"
      if(mi.in.get("VFDT") != "0" && mi.in.get("VFDT") != ""){
        iVFDT = mi.in.get("VFDT")
        if (!utility.call("DateUtil", "isDateValid", mi.in.get("VFDT"), "yyyyMMdd")) {
          mi.error("Date de fin de validité est invalide")
          return
        }
      }
    }
    String iOBV1= ""
    if(mi.in.get("OBV1")!= null)iOBV1=mi.in.get("OBV1")
    String iOBV2=  ""
    if(mi.in.get("OBV2")!= null)iOBV2=mi.in.get("OBV2")
    String iOBV3=  ""
    if(mi.in.get("OBV3")!= null)iOBV3=mi.in.get("OBV3")
    String iOBV4=  ""
    String iOBV5=  ""
    DBAction TarifQuery = database.table("OPRBAS").index("00").build()
    DBContainer OPRBAS = TarifQuery.getContainer()
    OPRBAS.set("ODCONO",currentCompany)
    OPRBAS.set("ODPRRF",mi.in.get("PRRF"))
    OPRBAS.set("ODCUCD",mi.in.get("CUCD"))
    OPRBAS.set("ODCUNO",iCUNO)
    OPRBAS.set("ODFVDT",iFVDT as Integer )
    OPRBAS.set("ODITNO",mi.in.get("ITNO"))
    if (!TarifQuery.readAll(OPRBAS, 6, outData_OPRBAS_2)) {
      OPRBAS.set("ODCONO",currentCompany)
      OPRBAS.set("ODPRRF",mi.in.get("PRRF"))
      OPRBAS.set("ODCUCD",mi.in.get("CUCD"))
      String iCUNO_Blank=""
      OPRBAS.set("ODCUNO",iCUNO_Blank)
      OPRBAS.set("ODFVDT",iFVDT as Integer )
      OPRBAS.set("ODITNO",mi.in.get("ITNO"))
      if (!TarifQuery.readAll(OPRBAS, 6, outData_OPRBAS_2)) {
        mi.error("Ligne Tarif n'existe pas f")
        return
      }
    }
    if(obv1== null)obv1=""
    if(obv2== null)obv2=""
    if(obv3== null)obv3=""
    if(vfdt== null)vfdt=""
    if(iOBV1.trim()!=obv1.trim() || iOBV2.trim()!=obv2.trim() || iOBV3.trim()!=obv3.trim()||iVFDT.trim()!=vfdt.trim()){
      mi.error("Ligne Tarif n'existe pas f")
      return
    }
    if(mi.in.get("FPSY") != "" && mi.in.get("FPSY") != null){
      iFPSY = mi.in.get("FPSY")
      if(!utility.call("NumberUtil","isValidNumber",iFPSY,".")){
        mi.error("Format numérique Flag Psycho incorrect")
        return
      }
    }
    if(mi.in.get("FLAG") != "" && mi.in.get("FLAG") != null){
      iFLAG = mi.in.get("FLAG")
      if(!utility.call("NumberUtil","isValidNumber",iFLAG,".")){
        mi.error("Format numérique Flag 80/20 incorrect")
        return
      }
    }
    if(mi.in.get("PUPR") != "" && mi.in.get("PUPR") != null){
      iPUPR = mi.in.get("PUPR")
      if(!utility.call("NumberUtil","isValidNumber",iPUPR,".")){
        mi.error("Format numérique PANGeprocor incorrect")
        return
      }
    }
    if(mi.in.get("NEPR") != "" && mi.in.get("NEPR") != null){
      iNEPR = mi.in.get("NEPR")
      if(!utility.call("NumberUtil","isValidNumber",iNEPR,".")){
        mi.error("Format numérique PABGeprocor incorrect")
        return
      }
    }
    if(mi.in.get("MOBJ") != "" && mi.in.get("MOBJ") != null){
      iMOBJ = mi.in.get("MOBJ")
      if(!utility.call("NumberUtil","isValidNumber",iMOBJ,".")){
        mi.error("Format numérique Margeobj incorrect")
        return
      }
    }
    if(mi.in.get("MCUN") != "" && mi.in.get("MCUN") != null){
      iMCUN = mi.in.get("MCUN")
      if(!utility.call("NumberUtil","isValidNumber",iMCUN,".")){
        mi.error("Format numérique MargeplancherClient incorrect")
        return
      }
    }
    if(mi.in.get("MDIV") != "" && mi.in.get("MDIV") != null){
      iMDIV = mi.in.get("MDIV")
      if(!utility.call("NumberUtil","isValidNumber",iMDIV,".")){
        mi.error("Format numérique MargeplancherSté incorrect")
        return
      }
    }
    if(mi.in.get("ZUPA") != "" && mi.in.get("ZUPA") != null){
      iZUPA = mi.in.get("ZUPA")
      if(!utility.call("NumberUtil","isValidNumber",iZUPA,".")){
        mi.error("Format numérique Rempalette incorrect")
        return
      }
    }
    logger.debug("value SAPR= {$iSAPR}")
    if(mi.in.get("SAPR") != "" && mi.in.get("SAPR") != null){
      iSAPR = mi.in.get("SAPR")
      if(!utility.call("NumberUtil","isValidNumber",iSAPR,".")){
        mi.error("Format numérique Tarifinal incorrect")
        return
      }
    }
    logger.debug("value SAPR2= {$iSAPR}")
    if(mi.in.get("MFIN") != "" && mi.in.get("MFIN") != null){
      iMFIN = mi.in.get("MFIN")
      if(!utility.call("NumberUtil","isValidNumber",iMFIN,".")){
        mi.error("Format numérique Mfinale incorrect")
        return
      }
    }
    if(mi.in.get("SAP0") != "" && mi.in.get("SAP0") != null){
      iSAP0 = mi.in.get("SAP0")
      if(!utility.call("NumberUtil","isValidNumber",iSAP0,".")){
        mi.error("Format numérique T0 incorrect")
        return
      }
    }
    if(mi.in.get("REM0") != "" && mi.in.get("REM0") != null){
      iREM0 = mi.in.get("REM0")
      if(!utility.call("NumberUtil","isValidNumber",iREM0,".")){
        mi.error("Format numérique RemT0 incorrect")
        return
      }
    }
    if(mi.in.get("MOY0") != "" && mi.in.get("MOY0") != null){
      iMOY0 = mi.in.get("MOY0")
      if(!utility.call("NumberUtil","isValidNumber",iMOY0,".")){
        mi.error("Format numérique M0 incorrect")
        return
      }
    }
    if(mi.in.get("SAP1") != "" && mi.in.get("SAP1") != null){
      iSAP1 = mi.in.get("SAP1")
      if(!utility.call("NumberUtil","isValidNumber",iSAP1,".")){
        mi.error("Format numérique T1 incorrect")
        return
      }
    }
    if(mi.in.get("MOY1") != "" && mi.in.get("MOY1") != null){
      iMOY1 = mi.in.get("MOY1")
      if(!utility.call("NumberUtil","isValidNumber",iMOY1,".")){
        mi.error("Format numérique M1 incorrect")
        return
      }
    }
    if(mi.in.get("SAP2") != "" && mi.in.get("SAP2") != null){
      iSAP2 = mi.in.get("SAP2")
      if(!utility.call("NumberUtil","isValidNumber",iSAP2,".")){
        mi.error("Format numérique T2 incorrect")
        return
      }
    }
    if(mi.in.get("MOY2") != "" && mi.in.get("MOY2") != null){
      iMOY2 = mi.in.get("MOY2")
      if(!utility.call("NumberUtil","isValidNumber",iMOY2,".")){
        mi.error("Format numérique M2 incorrect")
        return
      }
    }
    if(mi.in.get("SAP3") != "" && mi.in.get("SAP3") != null){
      iSAP3 = mi.in.get("SAP3")
      if(!utility.call("NumberUtil","isValidNumber",iSAP3,".")){
        mi.error("Format numérique T3 incorrect")
        return
      }
    }
    logger.debug("value SAPR4= {$iSAPR}")
    if(mi.in.get("MOY3") != "" && mi.in.get("MOY3") != null){
      iMOY3 = mi.in.get("MOY3")
      if(!utility.call("NumberUtil","isValidNumber",iMOY3,".")){
        mi.error("Format numérique M3 incorrect")
        return
      }
    }
    if(mi.in.get("SAP4") != "" && mi.in.get("SAP4") != null){
      iSAP4 = mi.in.get("SAP4")
      if(!utility.call("NumberUtil","isValidNumber",iSAP4,".")){
        mi.error("Format numérique T4 incorrect")
        return
      }
    }
    if(mi.in.get("MOY4") != "" && mi.in.get("MOY4") != null){
      iMOY4 = mi.in.get("MOY4")
      if(!utility.call("NumberUtil","isValidNumber",iMOY4,".")){
        mi.error("Format numérique M4 incorrect")
        return
      }
    }
    if(mi.in.get("MMO2") != "" && mi.in.get("MMO2") != null){
      iMMO2 = mi.in.get("MMO2")
      if(!utility.call("NumberUtil","isValidNumber",iMMO2,".")){
        mi.error("Format numérique Moyenne M2 incorrect")
        return
      }
    }
    iTEDL = mi.in.get("TEDL")
    iZIPL = mi.in.get("ZIPL")
    iPOPN = mi.in.get("POPN")
    iHIE2 = mi.in.get("HIE2")
    iHIE3 = mi.in.get("HIE3")
    iITTY = mi.in.get("ITTY")
    iASCD = mi.in.get("ASCD")
    iAGNB = mi.in.get("AGNB")
    iSUNO = mi.in.get("SUNO")

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction TarifCompQuery = database.table("EXT075").index("00").build()
    DBContainer EXT075 = TarifCompQuery.getContainer()
    EXT075.set("EXCONO",currentCompany)
    EXT075.set("EXPRRF",mi.in.get("PRRF"))
    EXT075.set("EXCUCD",mi.in.get("CUCD"))
    EXT075.set("EXCUNO",iCUNO)
    EXT075.set("EXFVDT",iFVDT as Integer)
    EXT075.set("EXITNO",mi.in.get("ITNO"))
    EXT075.set("EXOBV1",iOBV1.trim())
    EXT075.set("EXOBV2",iOBV2.trim())
    EXT075.set("EXOBV3",iOBV3.trim())
    EXT075.set("EXVFDT",iVFDT as Integer)
    if(!TarifCompQuery.readLock(EXT075, updateCallBack)){
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
  // outDataEXT080 : Retrieve EXT080 CUNO
  Closure<?> outDataEXT080 = { DBContainer EXT080 ->
    iCUNO = EXT080.get("EXCUNO")
  }
  // outData_OPRBAS_2 : Retrieve OPRBAS
  Closure<?> outData_OPRBAS_2 = { DBContainer OPRBAS ->
    obv1 = OPRBAS.get("ODOBV1")
    obv2 = OPRBAS.get("ODOBV2")
    obv3 = OPRBAS.get("ODOBV3")
    vfdt = OPRBAS.get("ODVFDT")
  }
  // updateCallBack : Update EXT075
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")

    if(mi.in.get("ASCD") != "" && mi.in.get("ASCD") != null) lockedResult.set("EXASCD",iASCD)
    if(mi.in.get("ITTY") != "" && mi.in.get("ITTY") != null) lockedResult.set("EXITTY",iITTY)
    if(mi.in.get("MMO2") != "" && mi.in.get("MMO2") != null) lockedResult.set("EXMMO2",iMMO2 as double)
    if(mi.in.get("HIE3") != "" && mi.in.get("HIE3") != null) lockedResult.set("EXHIE3",iHIE3)
    if(mi.in.get("HIE2") != "" && mi.in.get("HIE2") != null) lockedResult.set("EXHIE2",iHIE2)
    if(mi.in.get("POPN") != "" && mi.in.get("POPN") != null) lockedResult.set("EXPOPN",iPOPN)
    if(mi.in.get("MOY4") != "" && mi.in.get("MOY4") != null) lockedResult.set("EXMOY4",iMOY4 as double)
    if(mi.in.get("SAP4") != "" && mi.in.get("SAP4") != null) lockedResult.set("EXSAP4",iSAP4 as double)
    if(mi.in.get("MOY3") != "" && mi.in.get("MOY3") != null) lockedResult.set("EXMOY3",iMOY3 as double)
    if(mi.in.get("SAP3") != "" && mi.in.get("SAP3") != null) lockedResult.set("EXSAP3",iSAP3 as double)
    if(mi.in.get("TUT2") != "" && mi.in.get("TUT2") != null) lockedResult.set("EXTUT2",iTUT2 as double)
    if(mi.in.get("TUT1") != "" && mi.in.get("TUT1") != null) lockedResult.set("EXTUT1",iTUT1 as double)
    if(mi.in.get("TUM2") != "" && mi.in.get("TUM2") != null) lockedResult.set("EXTUM2",iTUM2 as double)
    if(mi.in.get("TUM1") != "" && mi.in.get("TUM1") != null) lockedResult.set("EXTUM1",iTUM1 as double)
    if(mi.in.get("MOY2") != "" && mi.in.get("MOY2") != null) lockedResult.set("EXMOY2",iMOY2 as double)
    if(mi.in.get("SAP2") != "" && mi.in.get("SAP2") != null) lockedResult.set("EXSAP2",iSAP2 as double)
    if(mi.in.get("MOY1") != "" && mi.in.get("MOY1") != null) lockedResult.set("EXMOY1",iMOY1 as double)
    if(mi.in.get("SAP1") != "" && mi.in.get("SAP1") != null) lockedResult.set("EXSAP1",iSAP1 as double)
    if(mi.in.get("MOY0") != "" && mi.in.get("MOY0") != null) lockedResult.set("EXMOY0",iMOY0 as double)
    if(mi.in.get("REM0") != "" && mi.in.get("REM0") != null) lockedResult.set("EXREM0",iREM0 as double)
    if(mi.in.get("SAP0") != "" && mi.in.get("SAP0") != null) lockedResult.set("EXSAP0",iSAP0 as double)
    if(mi.in.get("MFIN") != "" && mi.in.get("MFIN") != null) lockedResult.set("EXMFIN",iMFIN as double)
    if(mi.in.get("SAPR") != "" && mi.in.get("SAPR") != null) lockedResult.set("EXSAPR",iSAPR as double)
    if(mi.in.get("ZUPA") != "" && mi.in.get("ZUPA") != null) lockedResult.set("EXZUPA",iZUPA as double)
    if(mi.in.get("MDIV") != "" && mi.in.get("MDIV") != null) lockedResult.set("EXMDIV",iMDIV as double)
    if(mi.in.get("MCUN") != "" && mi.in.get("MCUN") != null) lockedResult.set("EXMCUN",iMCUN as double)
    if(mi.in.get("MOBJ") != "" && mi.in.get("MOBJ") != null) lockedResult.set("EXMOBJ",iMOBJ as double)
    if(mi.in.get("NEPR") != "" && mi.in.get("NEPR") != null) lockedResult.set("EXNEPR",iNEPR as double)
    if(mi.in.get("PUPR") != "" && mi.in.get("PUPR") != null) lockedResult.set("EXPUPR",iPUPR as double)
    if(mi.in.get("FLAG") != "" && mi.in.get("FLAG") != null) lockedResult.set("EXFLAG",iFLAG as Integer)
    if(mi.in.get("FPSY") != "" && mi.in.get("FPSY") != null) lockedResult.set("EXFPSY",iFPSY as Integer)
    if(mi.in.get("ZIPL") != "" && mi.in.get("ZIPL") != null) lockedResult.set("EXZIPL",iZIPL)
    if(mi.in.get("TEDL") != "" && mi.in.get("TEDL") != null) lockedResult.set("EXTEDL",iTEDL)
    if(mi.in.get("AGNB") != "" && mi.in.get("AGNB") != null) lockedResult.set("EXAGNB",iAGNB)
    if(mi.in.get("SUNO") != "" && mi.in.get("SUNO") != null) lockedResult.set("EXSUNO",iSUNO)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // Init value
  private InitValue(){
    iFVDT =""
    iVFDT =""
    iCUNO =""
    iITNO =""
    iPRRF =""
    iCUCD =""
    iOBV1 =""
    iOBV2 =""
    iOBV3 =""
    iITTY =""
    iMMO2 =""
    iHIE3 =""
    iHIE2 =""
    iPOPN =""
    iMOY4 =""
    iSAP4 =""
    iMOY3 =""
    iSAP3 =""
    iTUT2 =""
    iTUT1 =""
    iTUM2 =""
    iTUM1 =""
    iMOY2 =""
    iSAP2 =""
    iMOY1 =""
    iSAP1 =""
    iMOY0 =""
    iREM0 =""
    iSAP0 =""
    iMFIN =""
    iSAPR =""
    iZUPA =""
    iMDIV =""
    iMCUN =""
    iMOBJ =""
    iNEPR =""
    iPUPR =""
    iFLAG =""
    iFPSY =""
    iZIPL =""
    iTEDL =""
    iASCD =""
    iSUNO =""
    iAGNB =""
  }
}

