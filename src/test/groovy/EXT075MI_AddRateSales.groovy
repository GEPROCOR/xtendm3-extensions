/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT075MI.AddRateSales
 * Description : The AddRateSales transaction add records to the EXT075 table.
 * Date         Changed By   Description
 * 20210510     CDUV         TARX16 - Calcul du tarif
 * 20211103     YYOU         logMessage has been fixed
 * 20220519     CDUV         lowerCamelCase has been fixed
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddRateSales extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private final UtilityAPI utility
  private final TextFilesAPI textFiles
  private int currentCompany
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
  private String logFileName
  private boolean IN60

  public AddRateSales(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller, UtilityAPI utility, TextFilesAPI textFiles) {
    this.mi = mi
    this.database = database
    this.program = program
    this.logger = logger
    this.miCaller = miCaller
    this.utility = utility
    this.textFiles = textFiles
  }

  public void main() {
    InitValue()

    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    if(mi.in.get("PRRF") == null || mi.in.get("PRRF") == "" ){
      if(mi.in.get("FPNM") == "EVS101"){
        logMessage( "Code Tarif est obligatoire"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
        return
      } else {
        mi.error("Code Tarif est obligatoire")
        return
      }
    }
    if(mi.in.get("CUCD") == null ||  mi.in.get("CUCD") == ""){
      if(mi.in.get("FPNM") == "EVS101"){
        logMessage("Devise est obligatoire"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
        return
      } else {
        mi.error("Devise est obligatoire")
        return
      }
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

    if(mi.in.get("ITNO") == null ||  mi.in.get("ITNO") == ""){
      if(mi.in.get("FPNM") == "EVS101"){
        logMessage("Code Article est obligatoire"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
        return
      } else {
        mi.error("Code Article est obligatoire")
        return
      }
    }

    if(mi.in.get("FVDT") == null || mi.in.get("FVDT") == ""){
      if(mi.in.get("FPNM") == "EVS101"){
        logMessage("Date Date Début Validité est obligatoire")
        return
      } else {
        mi.error("Date Date Début Validité est obligatoire")
        return
      }
    }else {
      if(mi.in.get("FVDT") != ""){
        iFVDT = mi.in.get("FVDT")
        if (!utility.call("DateUtil", "isDateValid", iFVDT, "yyyyMMdd")) {
          mi.error("Format Date de début Validité incorrect")
          return
        }
      }
    }
    if(mi.in.get("VFDT") == null || mi.in.get("VFDT") == ""){
      if(mi.in.get("FPNM") == "EVS101"){
        logMessage("Date Date Fin Validité est obligatoire")
        return
      } else {
        mi.error("Date Date Fin Validité est obligatoire")
        return
      }
    }else {
      iVFDT=0
      if(mi.in.get("VFDT") != "0" && mi.in.get("VFDT") != ""){
        iVFDT = mi.in.get("VFDT")
        if (!utility.call("DateUtil", "isDateValid", iVFDT, "yyyyMMdd")) {
          mi.error("Format Date de fin Validité incorrect")
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
    OPRBAS.set("ODCUNO",mi.in.get("CUNO"))
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
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Ligne Tarif n'existe pas"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Ligne Tarif n'existe pas")
          return
        }
      }
    }
    if(obv1== null)obv1=""
    if(obv2== null)obv2=""
    if(obv3== null)obv3=""
    if(vfdt== null)vfdt=""
    if(iOBV1.trim()!=obv1.trim() || iOBV2.trim()!=obv2.trim() || iOBV3.trim()!=obv3.trim()||iVFDT.trim()!=vfdt.trim()){
      if(mi.in.get("FPNM") == "EVS101"){
        logMessage("Ligne Tarif n'existe pas"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
        return
      } else {
        mi.error("Ligne Tarif n'existe pas")
        return
      }
    }
    if(mi.in.get("FPSY") != "" && mi.in.get("FPSY") != null){
      iFPSY = mi.in.get("FPSY")
      if(!utility.call("NumberUtil","isValidNumber",iFPSY,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique Flag Psycho incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique Flag Psycho incorrect")
          return
        }
      }
    }
    if(mi.in.get("FLAG") != "" && mi.in.get("FLAG") != null){
      iFLAG = mi.in.get("FLAG")
      if(!utility.call("NumberUtil","isValidNumber",iFLAG,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique Flag 80/20 incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique Flag 80/20 incorrect")
          return
        }
      }
    }
    if(mi.in.get("PUPR") != "" && mi.in.get("PUPR") != null){
      iPUPR = mi.in.get("PUPR")
      if(!utility.call("NumberUtil","isValidNumber",iPUPR,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique PANGeprocor incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique PANGeprocor incorrect")
          return
        }
      }
    }
    if(mi.in.get("NEPR") != "" && mi.in.get("NEPR") != null){
      iNEPR = mi.in.get("NEPR")
      if(!utility.call("NumberUtil","isValidNumber",iNEPR,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique PABGeprocor incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique PABGeprocor incorrect")
          return
        }
      }
    }
    if(mi.in.get("MOBJ") != "" && mi.in.get("MOBJ") != null){
      iMOBJ = mi.in.get("MOBJ")
      if(!utility.call("NumberUtil","isValidNumber",iMOBJ,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique Margeobj incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique Margeobj incorrect")
          return
        }
      }
    }
    if(mi.in.get("MCUN") != "" && mi.in.get("MCUN") != null){
      iMCUN = mi.in.get("MCUN")
      if(!utility.call("NumberUtil","isValidNumber",iMOBJ,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique MargeplancherClient incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique MargeplancherClient incorrect")
          return
        }
      }
    }
    if(mi.in.get("MDIV") != "" && mi.in.get("MDIV") != null){
      iMDIV = mi.in.get("MDIV")
      if(!utility.call("NumberUtil","isValidNumber",iMDIV,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique MargeplancherSté incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique MargeplancherSté incorrect")
          return
        }
      }
    }
    if(mi.in.get("ZUPA") != "" && mi.in.get("ZUPA") != null){
      iZUPA = mi.in.get("ZUPA")
      if(!utility.call("NumberUtil","isValidNumber",iZUPA,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique Rempalette incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique Rempalette incorrect")
          return
        }
      }
    }
    logger.debug("value SAPR= {$iSAPR}")
    if(mi.in.get("SAPR") != "" && mi.in.get("SAPR") != null){
      iSAPR = mi.in.get("SAPR")
      if(!utility.call("NumberUtil","isValidNumber",iSAPR,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique Tarifinal incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique Tarifinal incorrect")
          return
        }
      }
    }
    logger.debug("value SAPR2= {$iSAPR}")
    if(mi.in.get("MFIN") != "" && mi.in.get("MFIN") != null){
      iMFIN = mi.in.get("MFIN")
      if(!utility.call("NumberUtil","isValidNumber",iMFIN,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique Mfinale incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique Mfinale incorrect")
          return
        }
      }
    }
    if(mi.in.get("SAP0") != "" && mi.in.get("SAP0") != null){
      iSAP0 = mi.in.get("SAP0")
      if(!utility.call("NumberUtil","isValidNumber",iSAP0,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique T0 incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique T0 incorrect")
          return
        }
      }
    }
    if(mi.in.get("REM0") != "" && mi.in.get("REM0") != null){
      iREM0 = mi.in.get("REM0")
      if(!utility.call("NumberUtil","isValidNumber",iREM0,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique RemT0 incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique RemT0 incorrect")
          return
        }
      }
    }
    if(mi.in.get("MOY0") != "" && mi.in.get("MOY0") != null){
      iMOY0 = mi.in.get("MOY0")
      if(!utility.call("NumberUtil","isValidNumber",iMOY0,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique M0 incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique M0 incorrect")
          return
        }
      }
    }
    if(mi.in.get("SAP1") != "" && mi.in.get("SAP1") != null){
      iSAP1 = mi.in.get("SAP1")
      if(!utility.call("NumberUtil","isValidNumber",iSAP1,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique T1 incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique T1 incorrect")
          return
        }
      }
    }
    if(mi.in.get("MOY1") != "" && mi.in.get("MOY1") != null){
      iMOY1 = mi.in.get("MOY1")
      if(!utility.call("NumberUtil","isValidNumber",iMOY1,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique M1 incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique M1 incorrect")
          return
        }
      }
    }
    if(mi.in.get("SAP2") != "" && mi.in.get("SAP2") != null){
      iSAP2 = mi.in.get("SAP2")
      if(!utility.call("NumberUtil","isValidNumber",iSAP2,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique T2 incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique T2 incorrect")
          return
        }
      }
    }
    if(mi.in.get("MOY2") != "" && mi.in.get("MOY2") != null){
      iMOY2 = mi.in.get("MOY2")
      if(!utility.call("NumberUtil","isValidNumber",iMOY2,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique M2 incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique M2 incorrect")
          return
        }
      }
    }
    if(mi.in.get("SAP3") != "" && mi.in.get("SAP3") != null){
      iSAP3 = mi.in.get("SAP3")
      if(!utility.call("NumberUtil","isValidNumber",iSAP3,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique T3 incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique T3 incorrect")
          return
        }
      }
    }
    logger.debug("value SAPR4= {$iSAPR}")
    if(mi.in.get("MOY3") != "" && mi.in.get("MOY3") != null){
      iMOY3 = mi.in.get("MOY3")
      if(!utility.call("NumberUtil","isValidNumber",iMOY3,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique M3 incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique M3 incorrect")
          return
        }
      }
    }
    if(mi.in.get("SAP4") != "" && mi.in.get("SAP4") != null){
      iSAP4 = mi.in.get("SAP4")
      if(!utility.call("NumberUtil","isValidNumber",iSAP4,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique T4 incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique T4 incorrect")
          return
        }
      }
    }
    if(mi.in.get("MOY4") != "" && mi.in.get("MOY4") != null){
      iMOY4 = mi.in.get("MOY4")
      if(!utility.call("NumberUtil","isValidNumber",iMOY4,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique M4 incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique M4 incorrect")
          return
        }
      }
    }
    if(mi.in.get("MMO2") != "" && mi.in.get("MMO2") != null){
      iMMO2 = mi.in.get("MMO2")
      if(!utility.call("NumberUtil","isValidNumber",iMMO2,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique Moyenne M2 incorrect"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
          return
        } else {
          mi.error("Format numérique Moyenne M2 incorrect")
          return
        }
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
    if (!TarifCompQuery.read(EXT075)) {
      if(mi.in.get("ASCD") != "" && mi.in.get("ASCD") != null) EXT075.set("EXASCD",iASCD)
      if(mi.in.get("ITTY") != "" && mi.in.get("ITTY") != null) EXT075.set("EXITTY",iITTY)
      if(mi.in.get("MMO2") != "" && mi.in.get("MMO2") != null) EXT075.set("EXMMO2",iMMO2 as double)
      if(mi.in.get("HIE3") != "" && mi.in.get("HIE3") != null) EXT075.set("EXHIE3",iHIE3)
      if(mi.in.get("HIE2") != "" && mi.in.get("HIE2") != null) EXT075.set("EXHIE2",iHIE2)
      if(mi.in.get("POPN") != "" && mi.in.get("POPN") != null) EXT075.set("EXPOPN",iPOPN)
      if(mi.in.get("MOY4") != "" && mi.in.get("MOY4") != null) EXT075.set("EXMOY4",iMOY4 as double)
      if(mi.in.get("SAP4") != "" && mi.in.get("SAP4") != null) EXT075.set("EXSAP4",iSAP4 as double)
      if(mi.in.get("MOY3") != "" && mi.in.get("MOY3") != null) EXT075.set("EXMOY3",iMOY3 as double)
      if(mi.in.get("SAP3") != "" && mi.in.get("SAP3") != null) EXT075.set("EXSAP3",iSAP3 as double)
      if(mi.in.get("TUT2") != "" && mi.in.get("TUT2") != null) EXT075.set("EXTUT2",iTUT2 as double)
      if(mi.in.get("TUT1") != "" && mi.in.get("TUT1") != null) EXT075.set("EXTUT1",iTUT1 as double)
      if(mi.in.get("TUM2") != "" && mi.in.get("TUM2") != null) EXT075.set("EXTUM2",iTUM2 as double)
      if(mi.in.get("TUM1") != "" && mi.in.get("TUM1") != null) EXT075.set("EXTUM1",iTUM1 as double)
      if(mi.in.get("MOY2") != "" && mi.in.get("MOY2") != null) EXT075.set("EXMOY2",iMOY2 as double)
      if(mi.in.get("SAP2") != "" && mi.in.get("SAP2") != null) EXT075.set("EXSAP2",iSAP2 as double)
      if(mi.in.get("MOY1") != "" && mi.in.get("MOY1") != null) EXT075.set("EXMOY1",iMOY1 as double)
      if(mi.in.get("SAP1") != "" && mi.in.get("SAP1") != null) EXT075.set("EXSAP1",iSAP1 as double)
      if(mi.in.get("MOY0") != "" && mi.in.get("MOY0") != null) EXT075.set("EXMOY0",iMOY0 as double)
      if(mi.in.get("REM0") != "" && mi.in.get("REM0") != null) EXT075.set("EXREM0",iREM0 as double)
      if(mi.in.get("SAP0") != "" && mi.in.get("SAP0") != null) EXT075.set("EXSAP0",iSAP0 as double)
      if(mi.in.get("MFIN") != "" && mi.in.get("MFIN") != null) EXT075.set("EXMFIN",iMFIN as double)
      if(mi.in.get("SAPR") != "" && mi.in.get("SAPR") != null) EXT075.set("EXSAPR",iSAPR as double)
      if(mi.in.get("ZUPA") != "" && mi.in.get("ZUPA") != null) EXT075.set("EXZUPA",iZUPA as double)
      if(mi.in.get("MDIV") != "" && mi.in.get("MDIV") != null) EXT075.set("EXMDIV",iMDIV as double)
      if(mi.in.get("MCUN") != "" && mi.in.get("MCUN") != null) EXT075.set("EXMCUN",iMCUN as double)
      if(mi.in.get("MOBJ") != "" && mi.in.get("MOBJ") != null) EXT075.set("EXMOBJ",iMOBJ as double)
      if(mi.in.get("NEPR") != "" && mi.in.get("NEPR") != null) EXT075.set("EXNEPR",iNEPR as double)
      if(mi.in.get("PUPR") != "" && mi.in.get("PUPR") != null) EXT075.set("EXPUPR",iPUPR as double)
      if(mi.in.get("FLAG") != "" && mi.in.get("FLAG") != null) EXT075.set("EXFLAG",iFLAG as Integer)
      if(mi.in.get("FPSY") != "" && mi.in.get("FPSY") != null) EXT075.set("EXFPSY",iFPSY as Integer)
      if(mi.in.get("ZIPL") != "" && mi.in.get("ZIPL") != null) EXT075.set("EXZIPL",iZIPL)
      if(mi.in.get("TEDL") != "" && mi.in.get("TEDL") != null) EXT075.set("EXTEDL",iTEDL)
      if(mi.in.get("AGNB") != "" && mi.in.get("AGNB") != null) EXT075.set("EXAGNB",iAGNB)
      if(mi.in.get("SUNO") != "" && mi.in.get("SUNO") != null) EXT075.set("EXSUNO",iSUNO)
      EXT075.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT075.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT075.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT075.setInt("EXCHNO", 1)
      EXT075.set("EXCHID", program.getUser())
      TarifCompQuery.insert(EXT075)
    } else {
      if(mi.in.get("FPNM") == "EVS101"){
        logMessage("L'enregistrement existe déjà1"+";"+mi.in.get("ITNO")+";"+mi.in.get("PRRF")+";"+mi.in.get("CUCD")+";"+mi.in.get("CUNO")+";"+mi.in.get("FVDT")+";"+mi.in.get("CONO"))
        return
      } else {
        mi.error("L'enregistrement existe déjà")
        return
      }
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
  // InitValue : Init Value
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
    iAGNB =""
    iSUNO =""
  }
  // Log message
  void logMessage(String message) {
    textFiles.open("FileImport")
    logFileName = "MSG_" + program.getProgramName() + "." + "AddRateSales" + ".csv"
    if(!textFiles.exists(logFileName)) {
      log("MSG;"+"ITNO;"+"PRRF;"+"CUCD;"+"CUNO;"+"FVDT;"+"CONO")
      log(message);
    }
  }
  // Log
  void log(String message) {
    IN60 = true
    message = LocalDateTime.now().toString() + ";" + message
    Closure<?> consumer = { PrintWriter printWriter ->
      printWriter.println(message)
    }
    textFiles.write(logFileName, "UTF-8", true, consumer)
  }
}
