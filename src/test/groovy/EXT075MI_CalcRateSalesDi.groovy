/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT075MI.CalcRateSalesDi
 * Description : The CalcRateSalesDi transaction launch calcul to the EXT075 table on differences since last launch. 
 * Date         Changed By   Description
 * 20210510     CDUV         TARX16 - Calcul du tarif
 * 20211207     CDUV         New check, database access and algorithm
 * 20220519     CDUV         lowerCamelCase has been fixed
 * 20221208     YYOU         update_CUGEX1() - Ajout motif erreur
 * 20230201     YYOU         update_CUGEX1() - Reset motif erreur
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class CalcRateSalesDi extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany
  private String currentDivision
  private final UtilityAPI utility
  private final MICallerAPI miCaller

  private String iCUNO
  private String iPRRF
  private String iFVDT
  private String iCUCD
  private String iOBV1
  private String iOBV2
  private String iOBV3
  private String iVFDT


  private String iTEDL
  private String iZIPL
  private Integer iFPSY
  private Integer iFLAG

  private double iPUPR
  private double iNEPR
  private double iMOBJ
  private double iMCUN
  private double iMDIV
  private double iZUPA
  private double iSAPR
  private double iMFIN
  private double iSAP0
  private double iREM0
  private double iMOY0
  private double iSAP1
  private double iMOY1
  private double iSAP2
  private double iMOY2
  private double iSAP3
  private double iMOY3
  private double iSAP4
  private double iMOY4
  private double iTUM1
  private double iTUM2
  private double iTUT1
  private double iTUT2
  private String iPOPN
  private double iMMO2
  private String iITTY
  private String iASCD
  private String iITNO
  private String iPLTB
  private String iSUNO
  private Integer dateJour
  private double iMarge
  private double iAjustement
  private String iHIE1
  private String iHIE2
  private String iHIE3
  private String iHIE4
  private String iHIE5
  private String iCFI1
  private String iITGR
  private String iBUAR
  private String iWHLO
  private String iPIDE
  private String iPDCC
  private String iCFI5
  private String iVAGN
  private String svAGNB
  private String svSUNO
  private String svDIVI

  private String szOBV1
  private String szOBV2
  private String szOBV3
  private String szOBV4
  private String szFVDT

  private boolean IN60
  private long iDTID
  private Integer numCount = 0
  private String count = 0
  private Integer compteurCugex1 = 0
  private boolean Ano = false
  private String status
  private double SZSAPR = 0d
  private double SZMMO2 = 0d
  private Integer SZCPTL = 0
  private String iCUNO_Blank
  private String motif

  public CalcRateSalesDi(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
    this.miCaller = miCaller
  }

  public void main() {
    IN60=false
    LocalDateTime timeOfCreation = LocalDateTime.now()
    dateJour=timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    iCUNO_Blank=""
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
    iCUNO = mi.in.get("CUNO")
    if(iCUNO== null ||  iCUNO == ""){
      DBAction queryEXT080 = database.table("EXT080").index("20").selection("EXCUNO").build()
      DBContainer EXT080 = queryEXT080.getContainer()
      EXT080.set("EXCONO", currentCompany)
      EXT080.set("EXPRRF", mi.in.get("PRRF"))
      EXT080.set("EXCUCD", mi.in.get("CUCD"))
      EXT080.set("EXFVDT", mi.in.get("FVDT") as Integer)
      if(!queryEXT080.readAll(EXT080, 4, outDataEXT080)){
        mi.error("L'enregistrement EXT080 n'existe pas donc pas de client")
        return
      }
      if(iCUNO==null || iCUNO==""){
        mi.error("Code Client est obligatoire")
        return
      }
    }
    logger.debug("value iCUNO= {$iCUNO}")

    if(mi.in.get("FVDT") == null || mi.in.get("FVDT") == ""){
      mi.error("Date Date Début Validité est obligatoire")
      return
    }else {
      iFVDT = mi.in.get("FVDT")
      if (!utility.call("DateUtil", "isDateValid", iFVDT, "yyyyMMdd")) {
        mi.error("Format Date de Validité incorrect")
        return
      }
    }
    iPRRF = mi.in.get("PRRF")

    iCUCD = mi.in.get("CUCD")

    // Recherche existence tarif
    iWHLO=""
    DBAction TarifQuery = database.table("OPRICH").index("00").build()
    DBContainer OPRICH = TarifQuery.getContainer()
    OPRICH.set("OJCONO",currentCompany)
    OPRICH.set("OJPRRF",iPRRF)
    OPRICH.set("OJCUCD",iCUCD)
    OPRICH.set("OJCUNO",iCUNO)
    OPRICH.set("OJFVDT",iFVDT as Integer)
    if (!TarifQuery.readAll(OPRICH, 5, outData_OPRICH)) {
      OPRICH.set("OJCONO",currentCompany)
      OPRICH.set("OJPRRF",iPRRF)
      OPRICH.set("OJCUCD",iCUCD)
      OPRICH.set("OJCUNO",iCUNO_Blank)
      OPRICH.set("OJFVDT",iFVDT as Integer)
      if (!TarifQuery.readAll(OPRICH, 5, outData_OPRICH)) {
        mi.error("Entete Tarif n'existe pas")
        return
      }
    }
    //logger.debug("Entete Tarif OK")

    update_CUGEX1("95", count, "?")

    // Suppression table travail EXT077
    DBAction TarifCompQueryC = database.table("EXT077").index("00").build()
    DBContainer EXT077 = TarifCompQueryC.getContainer()
    EXT077.set("EXCONO",currentCompany)
    EXT077.set("EXPRRF",mi.in.get("PRRF"))
    EXT077.set("EXCUCD",mi.in.get("CUCD"))
    EXT077.set("EXCUNO",iCUNO)
    EXT077.set("EXFVDT",mi.in.get("FVDT") as Integer)
    if(!TarifCompQueryC.readAllLock(EXT077, 5 ,Delete_EXT077)){
    }
    // Recherche info EXT080
    iZIPL=""
    iPIDE=""
    DBAction IncotermQuery = database.table("EXT080").index("00").selection("EXPIDE","EXWHLO","EXZIPL").build()
    DBContainer EXT080 = IncotermQuery.getContainer()
    EXT080.set("EXCONO",currentCompany)
    EXT080.set("EXPRRF",iPRRF)
    EXT080.set("EXCUCD",iCUCD)
    EXT080.set("EXCUNO",iCUNO)
    EXT080.set("EXFVDT",iFVDT as Integer)
    if (!IncotermQuery.readAll(EXT080, 5, outData_EXT080)) {
      update_CUGEX1("97", count, "Erreur Injection Tarif MEA ou pré-requis")
      //logger.debug("Paramétrage EXT080 n'existe pas")
      mi.error("Paramétrage EXT080 n'existe pas")
      return
    }
    // Recherche EXT041 marge
    //logger.debug("Incoterm Vente OK")
    //logger.debug("value iZIPL= {$iZIPL}")
    // Table Simplification ou tunnel d'ajustement EXT041
    iTUM1 = 0
    iTUM2 = 0
    iTUT1 = 0
    iTUT2 = 0
    DBAction TableSimpQuery = database.table("EXT041").index("00").selection("EXBOBE","EXBOHE","EXBOBM","EXBOHM").build()
    DBContainer EXT041 = TableSimpQuery.getContainer()
    EXT041.set("EXCONO",currentCompany)
    EXT041.set("EXTYPE","MARG")
    EXT041.set("EXCUNO",iCUNO)
    if (!TableSimpQuery.readAll(EXT041, 3, outData_EXT041_MARG)){
      //logger.debug("value EXT041 nok")
      EXT041.set("EXCONO",currentCompany)

      EXT041.set("EXTYPE","MARG")
      EXT041.set("EXCUNO"," ")
      if (!TableSimpQuery.readAll(EXT041, 3, outData_EXT041_MARG)){
        //logger.debug("value EXT041 bis nok")
      }
    }

    TableSimpQuery = database.table("EXT041").index("00").selection("EXBOBE","EXBOHE","EXBOBM","EXBOHM").build()
    EXT041 = TableSimpQuery.getContainer()
    EXT041.set("EXCONO",currentCompany)
    EXT041.set("EXTYPE","T0T3")
    EXT041.set("EXCUNO",iCUNO)
    if (!TableSimpQuery.readAll(EXT041, 3, outData_EXT041_T0T3)){
      //logger.debug("value EXT041 nok")
      EXT041.set("EXCONO",currentCompany)

      EXT041.set("EXTYPE","T0T3")
      EXT041.set("EXCUNO"," ")
      if (!TableSimpQuery.readAll(EXT041, 3, outData_EXT041_T0T3)){
        //logger.debug("value EXT041 bis nok")
      }
    }
    //logger.debug("value iTUM1= {$iTUM1}") //marge
    //logger.debug("value iTUM2= {$iTUM2}") //marge
    //logger.debug("value iTUT1= {$iTUT1}") //T0T3
    //logger.debug("value iTUT2= {$iTUT2}") //T0T3

    // Traitement 1
    iASCD = ""
    ExpressionFactory expressionTableCompTarif = database.getExpressionFactory("EXT075")
    expressionTableCompTarif = expressionTableCompTarif.eq("EXSAPR", "0")
    DBAction TableCompTarif = database.table("EXT075").index("00").matching(expressionTableCompTarif).selection("EXCUNO","EXASCD","EXITNO","EXOBV1","EXOBV2","EXOBV3","EXVFDT").build()

    DBContainer EXT075 = TableCompTarif.getContainer()
    EXT075.set("EXCONO",currentCompany)
    EXT075.set("EXPRRF",iPRRF)
    EXT075.set("EXCUCD",iCUCD)
    EXT075.set("EXCUNO",iCUNO)
    EXT075.set("EXFVDT",iFVDT as Integer)
    if (!TableCompTarif.readAll(EXT075, 5, outData_EXT075)) {
      update_CUGEX1("97", count, "Erreur Pré-requis (vérifier compte P, Incoterm, Devise…)")
      //logger.debug("Aucune ligne Tarif déja créée")
      mi.error("Aucune ligne Tarif déja créée")
      return
    }
    //logger.debug("Ligne Tarif comp OK")
    if(IN60){
      return
    }
    majEXT076()

    majEXT075_traitement2()

    count = numCount
    update_CUGEX1("99", count, "?")

    Update_EXT080("90")
  }
  // outDataEXT080 : Retrieve EXT080
  Closure<?> outDataEXT080 = { DBContainer EXT080 ->
    iCUNO = EXT080.get("EXCUNO")
  }
  // outData_EXT051 : Retrieve EXT051
  Closure<?> outData_EXT051 = { DBContainer EXT051 ->
    iPOPN = EXT051.get("EXDATA")
    //logger.debug("value iPOPN EXT051 = {$iPOPN}")
  }
  // outData_EXT081 : Retrieve EXT081
  Closure<?> outDataEXT081 = { DBContainer EXT081 ->
    String oFDAT = EXT081.get("EXFDAT")
    //logger.debug("value FDAT EXT051 = {$oFDAT}")
    DBAction queryEXT051 = database.table("EXT051").index("00").selection("EXDATA").build()
    DBContainer EXT051 = queryEXT051.getContainer()
    EXT051.set("EXCONO",currentCompany)
    EXT051.set("EXASCD",iASCD)
    EXT051.set("EXCUNO",iCUNO)
    EXT051.set("EXDAT1",oFDAT as Integer)
    EXT051.set("EXTYPE","POPN")
    if (!queryEXT051.readAll(EXT051, 5, outData_EXT051)){

    }
  }
  // outData_EXT075 : Retrieve EXT075 and process 1
  Closure<?> outData_EXT075= { DBContainer EXT075 ->
    iASCD = EXT075.get("EXASCD")
    iITNO = EXT075.get("EXITNO")
    iOBV1 = EXT075.get("EXOBV1")
    iOBV2 = EXT075.get("EXOBV2")
    iOBV3 = EXT075.get("EXOBV3")
    iVFDT = EXT075.get("EXVFDT")
    iCUNO = EXT075.get("EXCUNO")

    SZSAPR = 0d
    // OPRBAS prix=0.001
    DBAction OprbasQuery = database.table("OPRBAS").index("00").selection("ODSAPR").build()
    DBContainer OPRBAS = OprbasQuery.getContainer()
    OPRBAS.set("ODCONO",currentCompany)
    OPRBAS.set("ODPRRF",iPRRF)
    OPRBAS.set("ODCUCD",iCUCD)
    OPRBAS.set("ODCUNO",iCUNO_Blank)
    OPRBAS.set("ODFVDT",iFVDT as Integer)
    OPRBAS.set("ODITNO",iITNO)
    if (!OprbasQuery.readAll(OPRBAS, 6, outData_OPRBAS)) {
      update_CUGEX1("97", count, "L'article "+iITNO+" n'existe pas dans le tarif")
      //logger.debug("Tarif n'existe pas")
      mi.error("Tarif n'existe pas")
      return
    }
    //logger.debug("OPRBAS OK")
    logger.debug("value iITNO= {$iITNO}")
    logger.debug("value SZSAPR= {$SZSAPR}")
    if(SZSAPR==0.001){

      // Flag PSYCHO
      iFPSY = 0
      DBAction PsychoQuery = database.table("CUGEX1").index("00").selection("F1CHB1").build()
      DBContainer CUGEX1 = PsychoQuery.getContainer()
      CUGEX1.set("F1CONO",currentCompany)
      CUGEX1.set("F1FILE","OCUSIT")
      CUGEX1.set("F1PK01",iCUNO)
      CUGEX1.set("F1PK02",iITNO)
      if (!PsychoQuery.readAll(CUGEX1, 4, outData_CUGEX1)){

      }
      //logger.debug("value iFPSY= {$iFPSY}")
      // Recherche Info Article
      iITGR=""
      iBUAR=""
      iITTY=""
      iHIE1=""
      iHIE2=""
      iHIE3=""
      iHIE4=""
      iHIE5=""
      iCFI1=""
      iCFI5=""
      iDTID = 0
      DBAction ArticleQuery = database.table("MITMAS").index("00").selection("MMDTID","MMCFI5","MMITGR","MMBUAR","MMITTY","MMHIE1","MMHIE2","MMHIE3","MMHIE4","MMHIE5","MMCFI1").build()
      DBContainer MITMAS = ArticleQuery.getContainer()
      MITMAS.set("MMCONO",currentCompany)
      MITMAS.set("MMITNO",iITNO)
      if (!ArticleQuery.readAll(MITMAS, 2, outData_MITMAS)){
        update_CUGEX1("97", count, "L'article "+iITNO+" n'existe pas dans la base article (MMS001)")
        //logger.debug("Article n'existe pas")
        mi.error("Article n'existe pas")
        return
      }
      logger.debug("MITMAS OK")

      // Recherche contrat
      iSUNO = ""
      iVAGN=""
      iPUPR = RecherchePrixBrut()
      //logger.debug("value iPUPR= {$iPUPR}")
      iNEPR =  RechercheNEPR(iPUPR)
      //logger.debug("value iNEPR= {$iNEPR}")
      iSAP0 = rechercheT0()
      //logger.debug("value iSAP0= {$iSAP0}")
      if(iSAP0!=0){
        iMOY0=(iSAP0-iNEPR)/iSAP0
      }
      // Recherche Enseigne
      iPOPN=""
      DBAction EnseigneQuery = database.table("MITPOP").index("00").selection("MPPOPN","MPVFDT","MPLVDT").build()
      DBContainer MITPOP = EnseigneQuery.getContainer()
      MITPOP.set("MPCONO",currentCompany)
      MITPOP.set("MPALWT","3" as Integer)
      MITPOP.set("MPALWQ","ENS")
      MITPOP.set("MPITNO",iITNO)
      if (!EnseigneQuery.readAll(MITPOP, 4, outData_MITPOP)){
      }

      DBAction queryEXT081 = database.table("EXT081").index("00").selection("EXFDAT").build()
      DBContainer EXT081 = queryEXT081.getContainer()
      EXT081.set("EXCONO", currentCompany)
      EXT081.set("EXPRRF", iPRRF)
      EXT081.set("EXCUCD", iCUCD)
      EXT081.set("EXCUNO", iCUNO)
      EXT081.set("EXFVDT", iFVDT as Integer)
      EXT081.set("EXASCD", iASCD)
      if(!queryEXT081.readAll(EXT081, 6, outDataEXT081)){

      }

      //logger.debug("value iPOPN= {$iPOPN}")
      // Marge objectif et Marge Plancher EXT040
      iMOBJ = 0
      iMCUN = 0
      iMDIV = 0
      rechercheMarge()

      //logger.debug("value iMOBJ= {$iMOBJ}")
      //logger.debug("value iMCUN= {$iMCUN}")
      //logger.debug("value iMDIV= {$iMDIV}")
      if(IN60){
        return
      }
      // Flag 80/20
      iFLAG=rechercheFlag8020()
      //logger.debug("value iFLAG= {$iFLAG}")
      // TOC EXT042
      iAjustement=0
      rechercheToc()
      //logger.debug("value iAjustement= {$iAjustement}")
      iMOBJ = iMOBJ +iAjustement
      //logger.debug("value iMOBJ= {$iMOBJ}")

      // Conversion Devise
      // ???

      iSAP1=0
      iSAP2=0
      traitement1()
      //logger.debug("value iNEPR= {$iNEPR}")
      //logger.debug("value iSAP1= {$iSAP1}")
      //logger.debug("value iSAP2= {$iSAP2}")
      iMOY2=0
      if(iSAP2!=0){
        iMOY2=1-(iNEPR/iSAP2)
      }
      //logger.debug("value iMOY2= {$iMOY2}")
      //logger.debug("value iHIE2= {$iHIE2}")
      // Alim EXT075
      majEXT075()

      // Cumul EXT076
      if(iFPSY==0){
        // Tables Axes simplificateurs pour MMO2
        LocalDateTime timeOfCreation = LocalDateTime.now()
        DBAction AxesSimpQuery = database.table("EXT076").index("00").selection("EXCPTL","EXMMO2","EXCHNO").build()
        DBContainer EXT076 = AxesSimpQuery.getContainer()
        EXT076.set("EXCONO",currentCompany)
        EXT076.set("EXPRRF",iPRRF)
        EXT076.set("EXCUCD",iCUCD)
        EXT076.set("EXCUNO",iCUNO)
        EXT076.set("EXFVDT",iFVDT as Integer)
        EXT076.set("EXPOPN",iPOPN)
        EXT076.set("EXITTY",iITTY)
        EXT076.set("EXHIE2",iHIE2)
        EXT076.set("EXHIE3",iHIE3)
        if (!AxesSimpQuery.readLock(EXT076,updateCallBack_EXT076)) {
          EXT076.set("EXMMO2", iMOY2)
          EXT076.set("EXCPTL", 1)
          EXT076.set("EXASCD", iASCD)
          EXT076.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
          EXT076.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
          EXT076.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
          EXT076.setInt("EXCHNO", 1)
          EXT076.set("EXCHID", program.getUser())
          AxesSimpQuery.insert(EXT076)

          // ecriture EXT077
          DBAction AxesSimpQueryC = database.table("EXT077").index("00").selection("EXCPTL","EXMMO2","EXCHNO").build()
          DBContainer EXT077 = AxesSimpQueryC.getContainer()
          EXT077.set("EXCONO",currentCompany)
          EXT077.set("EXPRRF",iPRRF)
          EXT077.set("EXCUCD",iCUCD)
          EXT077.set("EXCUNO",iCUNO)
          EXT077.set("EXFVDT",iFVDT as Integer)
          EXT077.set("EXPOPN",iPOPN)
          EXT077.set("EXITTY",iITTY)
          EXT077.set("EXHIE2",iHIE2)
          EXT077.set("EXHIE3",iHIE3)
          if (!AxesSimpQueryC.readLock(EXT077,updateCallBack_EXT077)) {
            EXT077.set("EXMMO2", iMOY2)
            EXT077.set("EXCPTL", 1)
            EXT077.set("EXASCD", iASCD)
            EXT077.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            EXT077.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
            EXT077.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            EXT077.setInt("EXCHNO", 1)
            EXT077.set("EXCHID", program.getUser())
            AxesSimpQueryC.insert(EXT077)
          }
        }
      }
    }
    iSAPR=0
  }
  // updateCallBack_CUGEX1 : update CUGEX1
  Closure<?> updateCallBack_CUGEX1 = { LockedResult  lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("F1CHNO")
    double NbLignes = lockedResult.get("F1N196") as double
    NbLignes = NbLignes + count as double
    lockedResult.set("F1A030", status)
    lockedResult.set("F1N196", NbLignes)
    lockedResult.set("F1A121", motif)
    lockedResult.setInt("F1LMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("F1CHNO", changeNumber + 1)
    lockedResult.set("F1CHID", program.getUser())
    lockedResult.update()
  }
  // Delete_EXT077 Delete EXT077
  Closure<?> Delete_EXT077 = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  // updateCallBack_EXT077 : Update EXT077
  Closure<?> updateCallBack_EXT077 = { LockedResult  lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    int CompteurLigne = lockedResult.get("EXCPTL")
    double MMO2_lu = lockedResult.get("EXMMO2")
    MMO2_lu=MMO2_lu+iMOY2
    lockedResult.set("EXMMO2", MMO2_lu)
    lockedResult.setInt("EXCPTL", CompteurLigne + 1)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // updateCallBack_EXT077 : Update EXT077 to agregate line
  Closure<?> updateCallBack_EXT076 = { LockedResult  lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    int CompteurLigne = lockedResult.get("EXCPTL")
    double MMO2_lu = lockedResult.get("EXMMO2")
    // ecriture EXT077
    DBAction AxesSimpQueryC = database.table("EXT077").index("00").selection("EXCPTL","EXMMO2","EXCHNO").build()
    DBContainer EXT077 = AxesSimpQueryC.getContainer()
    EXT077.set("EXCONO",currentCompany)
    EXT077.set("EXPRRF",iPRRF)
    EXT077.set("EXCUCD",iCUCD)
    EXT077.set("EXCUNO",iCUNO)
    EXT077.set("EXFVDT",iFVDT as Integer)
    EXT077.set("EXPOPN",iPOPN)
    EXT077.set("EXITTY",iITTY)
    EXT077.set("EXHIE2",iHIE2)
    EXT077.set("EXHIE3",iHIE3)
    if (AxesSimpQueryC.readLock(EXT077,updateCallBack_EXT077)) {

      MMO2_lu=MMO2_lu+iMOY2
      lockedResult.set("EXMMO2", MMO2_lu)
      lockedResult.setInt("EXCPTL", CompteurLigne + 1)
      lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      lockedResult.setInt("EXCHNO", changeNumber + 1)
      lockedResult.set("EXCHID", program.getUser())
      lockedResult.update()
    }
  }
  // outData_EXT076 : Retrieve EXT076
  Closure<?> outData_EXT076 = { DBContainer EXT076 ->
    String oPOPN = EXT076.get("EXPOPN")
    String oITTY = EXT076.get("EXITTY")
    String oHIE2 = EXT076.get("EXHIE2")
    String oHIE3 = EXT076.get("EXHIE3")
    DBAction AxesSimpQuery_MAJ = database.table("EXT076").index("00").selection("EXCPTL","EXMMO2","EXCHNO").build()
    DBContainer EXT076_MAJ = AxesSimpQuery_MAJ.getContainer()
    EXT076_MAJ.set("EXCONO",currentCompany)
    EXT076_MAJ.set("EXPRRF",iPRRF)
    EXT076_MAJ.set("EXCUCD",iCUCD)
    EXT076_MAJ.set("EXCUNO",iCUNO)
    EXT076_MAJ.set("EXFVDT",iFVDT as Integer)
    EXT076_MAJ.set("EXPOPN",oPOPN)
    EXT076_MAJ.set("EXITTY",oITTY)
    EXT076_MAJ.set("EXHIE2",oHIE2)
    EXT076_MAJ.set("EXHIE3",oHIE3)
    if (!AxesSimpQuery_MAJ.readLock(EXT076_MAJ,updateCallBack_EXT076_MAJ)) {
    }
  }
  // outData_EXT077 : Retrieve EXT077
  Closure<?> outData_EXT077 = { DBContainer EXT077 ->
    String oPOPN = EXT077.get("EXPOPN")
    String oITTY = EXT077.get("EXITTY")
    String oHIE2 = EXT077.get("EXHIE2")
    String oHIE3 = EXT077.get("EXHIE3")
    DBAction AxesSimpQuery_MAJ = database.table("EXT077").index("00").selection("EXCPTL","EXMMO2","EXCHNO").build()
    DBContainer EXT077_MAJ = AxesSimpQuery_MAJ.getContainer()
    EXT077_MAJ.set("EXCONO",currentCompany)
    EXT077_MAJ.set("EXPRRF",iPRRF)
    EXT077_MAJ.set("EXCUCD",iCUCD)
    EXT077_MAJ.set("EXCUNO",iCUNO)
    EXT077_MAJ.set("EXFVDT",iFVDT as Integer)
    EXT077_MAJ.set("EXPOPN",oPOPN)
    EXT077_MAJ.set("EXITTY",oITTY)
    EXT077_MAJ.set("EXHIE2",oHIE2)
    EXT077_MAJ.set("EXHIE3",oHIE3)
    if (!AxesSimpQuery_MAJ.readLock(EXT077_MAJ,updateCallBack_EXT077_MAJ)) {
    }
  }
  // outData_EXT077_Maj76 : Retrieve EXT077 to update EXT076
  Closure<?> outData_EXT077_Maj76 = { DBContainer EXT077 ->
    String oPOPN = EXT077.get("EXPOPN")
    String oITTY = EXT077.get("EXITTY")
    String oHIE2 = EXT077.get("EXHIE2")
    String oHIE3 = EXT077.get("EXHIE3")
    SZMMO2 = EXT077.get("EXMMO2") as double
    SZCPTL = EXT077.get("EXCPTL") as Integer

    //logger.debug("MAJ EXT076") //T0T3
    //logger.debug("value oPOPN= {$oPOPN}") //T0T3
    //logger.debug("value oITTY= {$oITTY}") //T0T3
    //logger.debug("value oHIE2= {$oHIE2}") //T0T3
    //logger.debug("value SZMMO2= {$SZMMO2}") //T0T3
    //logger.debug("value SZCPTL= {$SZCPTL}") //T0T3

    DBAction AxesSimpQuery_MAJ = database.table("EXT076").index("00").selection("EXCHNO").build()
    DBContainer EXT076_MAJ = AxesSimpQuery_MAJ.getContainer()
    EXT076_MAJ.set("EXCONO",currentCompany)
    EXT076_MAJ.set("EXPRRF",iPRRF)
    EXT076_MAJ.set("EXCUCD",iCUCD)
    EXT076_MAJ.set("EXCUNO",iCUNO)
    EXT076_MAJ.set("EXFVDT",iFVDT as Integer)
    EXT076_MAJ.set("EXPOPN",oPOPN)
    EXT076_MAJ.set("EXITTY",oITTY)
    EXT076_MAJ.set("EXHIE2",oHIE2)
    EXT076_MAJ.set("EXHIE3",oHIE3)
    if (!AxesSimpQuery_MAJ.readLock(EXT076_MAJ,updateCallBack_EXT076_MAJ2)) {

    }
  }
  // updateCallBack_EXT077_MAJ :update EXT077 to calculate MMO2
  Closure<?> updateCallBack_EXT077_MAJ= { LockedResult  lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    int CompteurLigne = lockedResult.get("EXCPTL")
    double MMO2_lu = lockedResult.get("EXMMO2")
    MMO2_lu=MMO2_lu/CompteurLigne
    lockedResult.set("EXMMO2", MMO2_lu)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  //outData_OPRBAS : Retrieve OPRBAS
  Closure<?> outData_OPRBAS = { DBContainer EXT076 ->
    SZSAPR = EXT076.get("ODSAPR") as double
  }
  // updateCallBack_EXT076_MAJ : update to calculate MMO2
  Closure<?> updateCallBack_EXT076_MAJ= { LockedResult  lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    int CompteurLigne = lockedResult.get("EXCPTL")
    double MMO2_lu = lockedResult.get("EXMMO2")
    MMO2_lu=MMO2_lu/CompteurLigne
    lockedResult.set("EXMMO2", MMO2_lu)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // updateCallBack_EXT076_MAJ2 : update EXT076
  Closure<?> updateCallBack_EXT076_MAJ2= { LockedResult  lockedResult ->
    //logger.debug("MAJ updateCallBack_EXT076_MAJ2") //T0T3
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.set("EXMMO2", SZMMO2)
    lockedResult.set("EXCPTL", SZCPTL)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // outData_EXT075_T2 : Retrieve EXT075 in process 2
  Closure<?> outData_EXT075_T2 = { DBContainer EXT075 ->
    iFPSY = EXT075.get("EXFPSY")
    iPOPN = EXT075.get("EXPOPN")
    iITTY = EXT075.get("EXITTY")
    iHIE2 = EXT075.get("EXHIE2")
    iHIE3 = EXT075.get("EXHIE3")
    iITNO = EXT075.get("EXITNO")
    iMOY2 = EXT075.get("EXMOY2")
    iTUM1 = EXT075.get("EXTUM1")
    iTUM2 = EXT075.get("EXTUM2")
    iTUT1 = EXT075.get("EXTUT1")
    iTUT2 = EXT075.get("EXTUT2")
    iNEPR = EXT075.get("EXNEPR")
    iMCUN = EXT075.get("EXMCUN")
    iSAPR = EXT075.get("EXSAPR")
    iSAP2 = EXT075.get("EXSAP2")
    iPUPR = EXT075.get("EXPUPR")
    iSAP0 = EXT075.get("EXSAP0")
    SZSAPR = 0d
    // OPRBAS prix=0.001
    DBAction OprbasQuery = database.table("OPRBAS").index("00").selection("ODSAPR").build()
    DBContainer OPRBAS = OprbasQuery.getContainer()
    OPRBAS.set("ODCONO",currentCompany)
    OPRBAS.set("ODPRRF",iPRRF)
    OPRBAS.set("ODCUCD",iCUCD)
    OPRBAS.set("ODCUNO",iCUNO_Blank)
    OPRBAS.set("ODFVDT",iFVDT as Integer)
    OPRBAS.set("ODITNO",iITNO)
    if (!OprbasQuery.readAll(OPRBAS, 6, outData_OPRBAS)) {
      update_CUGEX1("97", count, "L'article "+iITNO+" n'existe pas dans le tarif "+ iPRRF +" (OIS017)")
      //logger.debug("Tarif n'existe pas")
      mi.error("Tarif n'existe pas")
      return
    }
    if(SZSAPR==0.001){
      //logger.debug("value iSAPR_BeforeT2 {$iSAPR }")
      if(iFPSY==0){
        iMMO2=0
        DBAction AxesSimpQuery = database.table("EXT076").index("00").selection("EXCPTL","EXMMO2","EXCHNO").build()
        DBContainer EXT076 = AxesSimpQuery.getContainer()
        EXT076.set("EXCONO",currentCompany)
        EXT076.set("EXPRRF",iPRRF)
        EXT076.set("EXCUCD",iCUCD)
        EXT076.set("EXCUNO",iCUNO)
        EXT076.set("EXFVDT",iFVDT as Integer)
        EXT076.set("EXPOPN",iPOPN)
        EXT076.set("EXITTY",iITTY)
        EXT076.set("EXHIE2",iHIE2)
        EXT076.set("EXHIE3",iHIE3)
        if (!AxesSimpQuery.readLock(EXT076,outData_EXT076_T2)) {
        }
        iSAPR=0
        iMFIN=0
        iMOY4=0
        iMOY3=0
        iSAP4=0
        iSAP3=0
        traitement2()
        DBAction EXT075Query_MAJ = database.table("EXT075").index("00").selection("EXCHNO").build()
        DBContainer EXT075_MAJ = EXT075Query_MAJ.getContainer()
        EXT075_MAJ.set("EXCONO",currentCompany)
        EXT075_MAJ.set("EXPRRF",iPRRF)
        EXT075_MAJ.set("EXCUCD",iCUCD)
        EXT075_MAJ.set("EXCUNO",iCUNO)
        EXT075_MAJ.set("EXFVDT",iFVDT as Integer)
        EXT075_MAJ.set("EXITNO",iITNO)
        EXT075_MAJ.set("EXOBV1",iOBV1)
        EXT075_MAJ.set("EXOBV2",iOBV2)
        EXT075_MAJ.set("EXOBV3",iOBV3)
        EXT075_MAJ.set("EXVFDT",iVFDT as Integer)
        if (!EXT075Query_MAJ.readLock(EXT075_MAJ,updateCallBack_EXT075_MAJ)) {
        }
      }
      iPDCC = ""
      String iDIVI =""
      DBAction DeviseQuery = database.table("CSYTAB").index("00").selection("CTPARM").build()
      DBContainer CSYTAB = DeviseQuery.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTDIVI",  iDIVI)
      CSYTAB.set("CTSTCO",  "CUCD")
      CSYTAB.set("CTSTKY", iCUCD)
      if (!DeviseQuery.readAll(CSYTAB, 4, outData_CSYTAB)){
        update_CUGEX1("97", count, "La devise "+iCUCD+" n'existe pas (Sélection de la devise dans la liste du Mashup)")
        //logger.debug("Devise n'existe pas")
        mi.error("Devise n'existe pas")
        return
      }
      //logger.debug("value iPDCC= {$iPDCC}")
      //logger.debug("value iSAPR= {$iSAPR}")
      //logger.debug("value iPUPR= {$iPUPR}")
      if(iPUPR<=0.001)iSAPR=0
      String oCONO = currentCompany
      String oPRIX = ""
      if(iPDCC=="0") oPRIX = (double)Math.round(iSAPR)
      if(iPDCC=="1") oPRIX = (double)Math.round(iSAPR*10)/10
      if(iPDCC=="2") oPRIX = (double)Math.round(iSAPR*100)/100
      if(iPDCC=="3") oPRIX = (double)Math.round(iSAPR*1000)/1000
      if(iPDCC=="4") oPRIX = (double)Math.round(iSAPR*10000)/10000
      if(iPDCC=="5") oPRIX = (double)Math.round(iSAPR*100000)/100000
      if(iPDCC=="6") oPRIX = (double)Math.round(iSAPR*1000000)/1000000

      //logger.debug("value oPRIX = {$oPRIX}")
      def paramOIS017MI = ["CONO": oCONO, "PRRF": iPRRF, "CUCD": iCUCD, "CUNO": iCUNO_Blank, "FVDT": iFVDT, "ITNO": iITNO, "OBV1": iOBV1, "OBV2": iOBV2, "OBV3": iOBV3, "SAPR": oPRIX]
      Closure<?> closure = {Map<String, String> response ->
        if(response.error != null){
          //logger.debug("OIS017MI"+response.errorMessage)
          update_CUGEX1("97", count, "Echec mise à jour prix de l'article "+iITNO+":"+oPRIX+"-"+response.errorMessage)
          return mi.error(response.errorMessage)
        }
      }
      miCaller.call("OIS017MI", "UpdBasePrice", paramOIS017MI, closure)
      numCount++
    }
  }
  //outData_EXT076_T2 : Retrieve EXT076
  Closure<?> outData_EXT076_T2 = { DBContainer EXT076 ->
    iMMO2 = EXT076.get("EXMMO2")  as double
    if(iMMO2>0.99)iMMO2=0.99
  }
  // outData_EXT080: Retrieve EXT080
  Closure<?> outData_EXT080= { DBContainer EXT080 ->
    iZIPL = EXT080.get("EXZIPL")
    iWHLO = EXT080.get("EXWHLO")
    iPIDE = EXT080.get("EXPIDE")
  }
  // outData_EXT041_MARG : Retrieve EXT041
  Closure<?> outData_EXT041_MARG= { DBContainer EXT041 ->
    iTUM1 = EXT041.get("EXBOBM") as double
    iTUM2 = EXT041.get("EXBOHM") as double
    iTUM1 = iTUM1/100
    iTUM2 = iTUM2/100
  }
  //outData_EXT041_T0T3 : Retrieve EXT041
  Closure<?> outData_EXT041_T0T3= { DBContainer EXT041 ->
    iTUT1 = EXT041.get("EXBOBE") as double
    iTUT2 = EXT041.get("EXBOHE") as double
    iTUT1 = iTUT1/100
    iTUT2 = iTUT2/100
  }
  //outData_EXT042 :: Retrieve EXT042
  Closure<?> outData_EXT042= { DBContainer EXT042 ->
    Integer oFVDT_NUM
    Integer oLVDT_NUM
    oFVDT_NUM =  EXT042.get("EXFVDT") as Integer
    oLVDT_NUM =  EXT042.get("EXLVDT") as Integer
    String POPN_lu=  EXT042.get("EXPOPN")
    String BUAR_lu=  EXT042.get("EXBUAR")
    String HIE3_lu=  EXT042.get("EXHIE3")
    String HIE4_lu=  EXT042.get("EXHIE4")
    String HIE5_lu=  EXT042.get("EXHIE5")
    String CFI1_lu=  EXT042.get("EXCFI1")
    String CFI5_lu=  EXT042.get("EXCFI5")
    String VAGN_lu=  EXT042.get("EXTX15")
    double iAjustement_lu = EXT042.get("EXADJT") as double
    if(oLVDT_NUM==0)oLVDT_NUM=99999999
    if(dateJour>= oFVDT_NUM && dateJour <=oLVDT_NUM){
      if((POPN_lu.trim().equals(iPOPN.trim())||POPN_lu.trim()=="")
        &&(VAGN_lu.trim().equals(iVAGN.trim())||VAGN_lu.trim()=="")
        &&(BUAR_lu.trim().equals(iBUAR.trim())||BUAR_lu.trim()=="")
        &&(HIE3_lu.trim().equals(iHIE3.trim())||HIE3_lu.trim()=="")
        &&(HIE4_lu.trim().equals(iHIE4.trim())||HIE4_lu.trim()=="")
        &&(HIE5_lu.trim().equals(iHIE5.trim())||HIE5_lu.trim()=="")
        &&(CFI1_lu.trim().equals(iCFI1.trim())||CFI1_lu.trim()=="")
        &&(CFI5_lu.trim().equals(iCFI5.trim())||CFI5_lu.trim()=="")){

        iAjustement = iAjustement_lu/100
        //logger.debug("value iAjustement={$iAjustement}")
      }
    }
  }
// outData_OPRICH: Retrieve OPRICH
  Closure<?> outData_OPRICH = { DBContainer OPRICH ->
  }
  // outData_CUGEX1 :  Retrieve CUGEX1
  Closure<?> outData_CUGEX1 = { DBContainer CUGEX1 ->
    iFPSY = CUGEX1.get("F1CHB1") as Integer
  }
  // outData_MITMAS :  Retrieve MITMAS in process 1
  Closure<?> outData_MITMAS = { DBContainer MITMAS ->
    iHIE1 = MITMAS.get("MMHIE1")
    iHIE2 = MITMAS.get("MMHIE2")
    iHIE3 = MITMAS.get("MMHIE3")
    iHIE4 = MITMAS.get("MMHIE4")
    iHIE5 = MITMAS.get("MMHIE5")
    iCFI1 = MITMAS.get("MMCFI1")
    iITTY = MITMAS.get("MMITTY")
    iITGR = MITMAS.get("MMITGR")
    iBUAR = MITMAS.get("MMBUAR")
    iCFI5 = MITMAS.get("MMCFI5")
    iDTID = MITMAS.get("MMDTID") as long
  }
  // outData_MITMAS2 :  Retrieve MITMAS in process 2
  Closure<?> outData_MITMAS2 = { DBContainer MITMAS ->
    iPDCC = MITMAS.get("MMPDCC")
  }
  // outData_CSYTAB :  Retrieve CSYTAB
  Closure<?> outData_CSYTAB = { DBContainer CSYTAB ->
    String oPDCC=CSYTAB.get("CTPARM")
    //logger.debug("value oPDCC= {$oPDCC}")
    if(oPDCC.trim().length()>=8){
      iPDCC = oPDCC.trim().substring(7,8)
    }
    //logger.debug("value iPDCC= {$iPDCC}")
  }
  // outData_MITPOP : Retrieve MITPOP
  Closure<?> outData_MITPOP = { DBContainer MITPOP ->
    Integer oVFDT_NUM
    Integer oLVDT_NUM
    oVFDT_NUM =  MITPOP.get("MPVFDT") as Integer
    oLVDT_NUM =  MITPOP.get("MPLVDT") as Integer
    if(oLVDT_NUM==0)oLVDT_NUM=99999999
    if(dateJour>= oVFDT_NUM && dateJour <=oLVDT_NUM && iPOPN==""){
      iPOPN = MITPOP.get("MPPOPN")
    }
  }
  // outData_EXT040 : Retrieve EXT040
  Closure<?> outData_EXT040= { DBContainer EXT040 ->
    Integer oFDAT = EXT040.get("EXFDAT")  as Integer
    Integer oTDAT = EXT040.get("EXTDAT")  as Integer
    if(oTDAT==0)oTDAT=99999999
    double oAGTP = EXT040.get("EXMARG") as double
    if(dateJour>= oFDAT && dateJour <=oTDAT){
      iMarge = EXT040.get("EXMARG") as double
      iMarge = iMarge/100
    }
  }
  // outData_EXT070 : Retrieve EXT070
  Closure<?> outData_EXT070 = { DBContainer EXT070 ->
    iFLAG = EXT070.get("EXFLAG") as Integer
  }
  Closure<?> updateCallBack_EXT075 = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    //logger.debug("value majT1= {$iSAP1}")
    //logger.debug("value majT1= {$iSAP2}")
    //logger.debug("value majT1= {$iSAPR}")
    if(iITTY != "") lockedResult.set("EXITTY",iITTY)
    if(iMMO2 != "") lockedResult.set("EXMMO2",iMMO2)
    if(iHIE3 != "") lockedResult.set("EXHIE3",iHIE3)
    if(iHIE2 != "") lockedResult.set("EXHIE2",iHIE2)
    if(iPOPN != "") lockedResult.set("EXPOPN",iPOPN)
    if(iMOY4 != "") lockedResult.set("EXMOY4",iMOY4)
    if(iSAP4 != "") lockedResult.set("EXSAP4",iSAP4)
    if(iMOY3 != "") lockedResult.set("EXMOY3",iMOY3)
    if(iSAP3 != "") lockedResult.set("EXSAP3",iSAP3)
    if(iTUT2 != "") lockedResult.set("EXTUT2",iTUT2)
    if(iTUT1 != "") lockedResult.set("EXTUT1",iTUT1)
    if(iTUM2 != "") lockedResult.set("EXTUM2",iTUM2)
    if(iTUM1 != "") lockedResult.set("EXTUM1",iTUM1)
    if(iMOY2 != "") lockedResult.set("EXMOY2",iMOY2)
    if(iSAP2 != "") lockedResult.set("EXSAP2",iSAP2)
    if(iMOY1 != "") lockedResult.set("EXMOY1",iMOY1)
    if(iSAP1 != "") lockedResult.set("EXSAP1",iSAP1)
    if(iMOY0 != "") lockedResult.set("EXMOY0",iMOY0)
    if(iREM0 != "") lockedResult.set("EXREM0",iREM0)
    if(iSAP0 != "") lockedResult.set("EXSAP0",iSAP0)
    if(iMFIN != "") lockedResult.set("EXMFIN",iMFIN)
    lockedResult.set("EXSAPR",iSAPR)
    if(iZUPA != "") lockedResult.set("EXZUPA",iZUPA)
    if(iMDIV != "") lockedResult.set("EXMDIV",iMDIV)
    if(iMCUN != "") lockedResult.set("EXMCUN",iMCUN)
    if(iMOBJ != "") lockedResult.set("EXMOBJ",iMOBJ)
    if(iNEPR != "") lockedResult.set("EXNEPR",iNEPR)
    if(iPUPR != "") lockedResult.set("EXPUPR",iPUPR)
    if(iFLAG != "") lockedResult.set("EXFLAG",iFLAG)
    if(iFPSY != "") lockedResult.set("EXFPSY",iFPSY)
    if(iZIPL != "") lockedResult.set("EXZIPL",iZIPL)
    if(iTEDL != "") lockedResult.set("EXTEDL",iTEDL)
    if(svAGNB != "") lockedResult.set("EXAGNB",svAGNB)
    if(svSUNO != "") lockedResult.set("EXSUNO",svSUNO)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // updateCallBack_EXT075_MAJ : update EXT075 in process 2
  Closure<?> updateCallBack_EXT075_MAJ = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    if(iMMO2 != "") lockedResult.set("EXMMO2",iMMO2)
    if(iMOY4 != "") lockedResult.set("EXMOY4",iMOY4)
    if(iSAP4 != "") lockedResult.set("EXSAP4",iSAP4)
    if(iMOY3 != "") lockedResult.set("EXMOY3",iMOY3)
    if(iSAP3 != "") lockedResult.set("EXSAP3",iSAP3)
    if(iMFIN != "") lockedResult.set("EXMFIN",iMFIN)
    if(iSAPR != "") lockedResult.set("EXSAPR",iSAPR)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  /**
   * Research Purchase Price in agreement
   *
   */
  private double RecherchePrixBrut(){
    logger.debug("RecherchePrixBrut  = {$iITNO} ")
    svAGNB=""
    svSUNO=""
    svDIVI=""
    szOBV1=""
    szOBV2=""
    szOBV3=""
    szOBV4=""
    szFVDT=""
    boolean Retour=false
    double oPUPR_Num=0
    Integer SVAGPT= 0
    String oVAGN="00000"
    String SVPRIO=""
    String oPRIO=""
    Integer SaveFVDT=0
    Integer iFVDT_int=iFVDT as Integer
    logger.debug("value iFVDT_int= {$iFVDT_int}")

    def TableArticleDepot = database.table("MITBAL").index("00").selection("MBSUNO","MBDIVI").build()
    def MITBAL = TableArticleDepot.getContainer()
    MITBAL.set("MBCONO", currentCompany)
    MITBAL.set("MBITNO", iITNO)
    MITBAL.set("MBWHLO", iWHLO)
    TableArticleDepot.readAll(MITBAL,3,{DBContainer recordMITBAL->
      svSUNO = recordMITBAL.get("MBSUNO")
      svDIVI = recordMITBAL.get("MBDIVI")
    })
    logger.debug("value svSUNO A = {$svSUNO}")
    def TablePromo = database.table("OPROMH").index("00").selection("FZTX15").build()
    def OPROMH = TablePromo.getContainer()
    OPROMH.set("FZCONO", currentCompany)
    OPROMH.set("FZDIVI", svDIVI)
    OPROMH.set("FZPIDE", iPIDE)
    TablePromo.readAll(OPROMH,3,{DBContainer recordOPROMH->
      oVAGN = recordOPROMH.get("FZTX15")
      logger.debug("value oVAGN A = {$oVAGN}")
      if(oVAGN=="" ||oVAGN==null){
        oVAGN="00000"
      }
    })
    logger.debug("value svSUNO= {$svSUNO}")
    logger.debug("value svDIVI= {$svDIVI}")
    logger.debug("value iWHLO= {$iWHLO}")
    logger.debug("value iPIDE= {$iPIDE}")
    logger.debug("value iFVDT_int= {$iFVDT_int}")
    logger.debug("value oVAGN= {$oVAGN}")

    def TableEXT030 = database.table("EXT030").index("10").selection("EXZIPP","EXPRIO").build()
    def EXT030 = TableEXT030.getContainer()
    EXT030.set("EXCONO", currentCompany)
    EXT030.set("EXZIPS", iZIPL)
    TableEXT030.readAll(EXT030,2,{DBContainer recordEXT030->
      String oZIPP = recordEXT030.get("EXZIPP")
      oPRIO = recordEXT030.get("EXPRIO")

      logger.debug("value oZIPP= {$oZIPP}")
      logger.debug("value oPRIO= {$oPRIO}")
      def TableCompContrat = database.table("EXT032").index("30").selection("EXAGNB").build()
      def EXT032_Liste = TableCompContrat.getContainer()
      EXT032_Liste.set("EXCONO", currentCompany)
      EXT032_Liste.set("EXZIPP", oZIPP)
      EXT032_Liste.set("EXSUNO", svSUNO)
      TableCompContrat.readAll(EXT032_Liste,3,{DBContainer recordEXT032->
        String oAGNB = recordEXT032.get("EXAGNB")
        logger.debug("value oAGNB= {$oAGNB}")
        def TableContrat = database.table("MPAGRH").index("00").selection("AHVAGN","AHAGTP","AHPAST","AHFVDT","AHUVDT").build()
        def MPAGRH = TableContrat.getContainer()
        MPAGRH.set("AHCONO", currentCompany)
        MPAGRH.set("AHSUNO", svSUNO)
        MPAGRH.set("AHAGNB", oAGNB)
        TableContrat.readAll(MPAGRH,3,{DBContainer recordMPAGRH ->
          String oPAST = recordMPAGRH.get("AHPAST")
          Integer oFVDT = recordMPAGRH.get("AHFVDT")  as Integer
          Integer oUVDT = recordMPAGRH.get("AHUVDT")  as Integer
          if(oUVDT==0)oUVDT=99999999
          logger.debug("value oPAST= {$oPAST}")
          logger.debug("value oFVDT= {$oFVDT}")
          logger.debug("value oUVDT= {$oUVDT}")
          String svVAGN =recordMPAGRH.get("AHVAGN")
          logger.debug("value svVAGN= {$svVAGN}")
          if(iFVDT_int>= oFVDT && iFVDT_int <=oUVDT && oPAST=="40"){
            if(recordMPAGRH.get("AHVAGN")==oVAGN ){
              ExpressionFactory expressionMPAGRL = database.getExpressionFactory("MPAGRL")
              expressionMPAGRL = expressionMPAGRL.eq("AIOBV1", iITNO)
              logger.debug("MPAGRH  ok")
              def TableLigneContrat = database.table("MPAGRL").index("00").matching(expressionMPAGRL).selection("AIAGPT","AIFVDT","AIUVDT","AIOBV1","AIOBV2","AIOBV3","AIOBV4").build()
              def MPAGRL = TableLigneContrat.getContainer()
              MPAGRL.set("AICONO", currentCompany)
              MPAGRL.set("AISUNO", svSUNO)
              MPAGRL.set("AIAGNB", oAGNB)
              MPAGRL.set("AIGRPI", "30" as Integer)
              MPAGRL.set("AIOBV1", iITNO)
              TableLigneContrat.readAll(MPAGRL,5,{DBContainer recordMPAGRL ->
                Integer oFVDT_MPAGRL = recordMPAGRL.get("AIFVDT") as Integer
                Integer oUVDT_MPAGRL = recordMPAGRL.get("AIUVDT") as Integer
                Integer oAGTP = recordMPAGRL.get("AIAGPT") as Integer
                if(oUVDT_MPAGRL==0)oUVDT=99999999
                def TableLigneCompContrat = database.table("MPAGRP").index("00").selection("AJPUPR").build()
                def MPAGRP = TableLigneCompContrat.getContainer()
                MPAGRP.set("AJCONO", currentCompany)
                MPAGRP.set("AJSUNO", svSUNO)
                MPAGRP.set("AJAGNB", oAGNB)
                MPAGRP.set("AJGRPI", "30" as Integer)
                MPAGRP.set("AJOBV1", iITNO)
                TableLigneCompContrat.readAll(MPAGRP,5,{DBContainer recordMPAGRP ->
                  logger.debug("MPAGRP pas miroir ok")
                  String oPUPR = recordMPAGRP.get("AJPUPR")
                  logger.debug("value oPUPR= {$oPUPR}")
                  logger.debug("value SVAGPT= {$SVAGPT}")
                  logger.debug("value oAGTP= {$oAGTP}")
                  if((SVAGPT==0 || SVAGPT >= oAGTP) && iFVDT_int>= oFVDT_MPAGRL && iFVDT_int <=oUVDT_MPAGRL){
                    if(SVPRIO=="" || oPRIO==SVPRIO){
                      if(SaveFVDT==0||SaveFVDT>oFVDT_MPAGRL){
                        logger.debug("prix brut pas  miroir ok")
                        SVAGPT = oAGTP
                        //Retour=true
                        oPUPR_Num=oPUPR as double
                        iSUNO = svSUNO
                        svAGNB = oAGNB
                        szOBV1 = recordMPAGRL.get("AIOBV1")
                        szOBV2 = recordMPAGRL.get("AIOBV2")
                        szOBV3 = recordMPAGRL.get("AIOBV3")
                        szOBV4 = recordMPAGRL.get("AIOBV4")
                        szFVDT = oFVDT_MPAGRL
                        SVPRIO=oPRIO
                        SaveFVDT=oFVDT_MPAGRL
                      }
                    }
                  }
                })
              })
            }
          }
        })
      })
    })
    return oPUPR_Num
  }
  /**
   * Research Calcul NEPR
   **/

  private double RechercheNEPR(double iPUPR){
    double oNEPR=iPUPR
    double oTaxeSp = 0
    double oFraisFSA = 0
    double oTauxRFA = 0
    double oRFA = 0
    String decimalSeparator = ","
    DBAction CUGEX1_query = database.table("CUGEX1").index("00").selection("F1N096","F1N196","F1N296","F1A030","F1A130","F1A230","F1A330").build()
    DBContainer CUGEX1 = CUGEX1_query.getContainer()
    CUGEX1.set("F1CONO", currentCompany)
    CUGEX1.set("F1FILE", "MPAGRL")
    CUGEX1.set("F1PK01", svSUNO)
    CUGEX1.set("F1PK02", svAGNB)
    CUGEX1.set("F1PK03", "30")
    CUGEX1.set("F1PK04", szOBV1)
    CUGEX1.set("F1PK05", szOBV2)
    CUGEX1.set("F1PK06", szOBV3)
    CUGEX1.set("F1PK07", szOBV4)
    CUGEX1.set("F1PK08", szFVDT)
    CUGEX1_query.readAll(CUGEX1,10,{DBContainer recordCUGEX1 ->
      String oA330 = recordCUGEX1.get("F1A330")
      double oN096 = recordCUGEX1.get("F1N096") as double
      double oN196 = recordCUGEX1.get("F1N196") as double
      double oN296 = recordCUGEX1.get("F1N296") as double
      oTaxeSp = oN096
      oFraisFSA = oN196
      oRFA = oN296
      DBAction MPCOVE_TauxRFA = database.table("MPCOVE").index("00").selection("IJOVHE").build()
      DBContainer MPCOVE = MPCOVE_TauxRFA.getContainer()
      MPCOVE.set("IJCONO",currentCompany)
      MPCOVE.set("IJCEID", "RFAFRS")
      MPCOVE.set("IJOVK1", oA330)
      MPCOVE_TauxRFA.readAll(MPCOVE,3,{DBContainer recordMPCOVE ->
        //logger.debug("Recherche TaxeSpé")
        String oOVHE = recordMPCOVE.get("IJOVHE")
        oTauxRFA = oOVHE as double
        //logger.debug("value oTauxRFA= {$oTauxRFA}")
      })
    })
    if(oRFA!=0){
      oNEPR =  oRFA*(1-oTauxRFA/100)+oTaxeSp + oFraisFSA
    }else{
      oNEPR =  iPUPR*(1-oTauxRFA/100)+oTaxeSp + oFraisFSA
    }
    return oNEPR
  }
  /**
   * Research Sales Price T0
   *
   */
  private double rechercheT0(){
    double oSAPR_Num = 0
    Integer SVDatePrec = 0
    Integer SVDateOPRBAS = iFVDT as Integer
    ExpressionFactory expressionOPRBAS = database.getExpressionFactory("OPRBAS")
    expressionOPRBAS = expressionOPRBAS.eq("ODITNO", iITNO)

    DBAction TarifLigneQuery = database.table("OPRBAS").index("00").matching(expressionOPRBAS).selection("ODSAPR","ODFVDT","ODLVDT").build()
    DBContainer OPRBAS = TarifLigneQuery.getContainer()
    OPRBAS.set("ODCONO",currentCompany)
    OPRBAS.set("ODPRRF",iPRRF)
    OPRBAS.set("ODCUCD",iCUCD)
    OPRBAS.set("ODCUNO",iCUNO_Blank)
    TarifLigneQuery.readAll(OPRBAS,4,{DBContainer recordOPRBAS ->
      //logger.debug("Recherche T0")
      String oSAPR = recordOPRBAS.get("ODSAPR")
      Integer oFVDT = recordOPRBAS.get("ODFVDT")  as Integer
      Integer oLVDT = recordOPRBAS.get("ODLVDT")  as Integer
      if(SVDateOPRBAS> oFVDT && (SVDatePrec==0 || SVDatePrec > oLVDT)){
        oSAPR_Num = oSAPR as double
        SVDatePrec = oLVDT
        //logger.debug("value T0 oSAPR_Num= {$oSAPR_Num}")
      }
    })
    return oSAPR_Num
  }
  /**
   * Research margin in Table Extend EXT040
   *
   */
  private void rechercheMarge(){

    // Recherche Sans Assortiment et sans Client
    iMarge = 0
    DBAction query = database.table("EXT040").index("00").selection("EXMARG","EXFDAT","EXTDAT").build()
    DBContainer EXT040 = query.getContainer()
    EXT040.set("EXCONO", currentCompany)
    EXT040.set("EXCUNO", " ")
    EXT040.set("EXASCD", " ")
    if(!query.readAll(EXT040, 3, outData_EXT040)){
      IN60=true
      update_CUGEX1("97", count, "L'enregistrement Marge niveau société n'existe pas")
      //logger.debug("L'enregistrement Marge niveau société n'existe pas")
      mi.error("L'enregistrement Marge niveau société n'existe pas")
      return
    }
    iMDIV=iMarge

    // Recherche Client sans assortiment
    iMarge = 0
    query = database.table("EXT040").index("00").selection("EXMARG","EXFDAT","EXTDAT").build()
    EXT040 = query.getContainer()
    EXT040.set("EXCONO", currentCompany)
    EXT040.set("EXCUNO", iCUNO)
    EXT040.set("EXASCD", " ")
    if(!query.readAll(EXT040, 3, outData_EXT040)){
      IN60=true
      //logger.debug("L'enregistrement Marge niveau Client n'existe pas")
      update_CUGEX1("97", count, "L'enregistrement Marge n'existe pas pour le client "+iCUNO)
      mi.error("L'enregistrement Marge niveau Client n'existe pas")
      return
    }
    iMCUN=iMarge

    // Recherche Client / assortiment
    iMarge = 0
    query = database.table("EXT040").index("10").selection("EXMARG","EXFDAT","EXTDAT").build()
    EXT040 = query.getContainer()
    //EXT040.set("EXCONO", currentCompany)
    EXT040.set("EXCUNO", iCUNO)
    EXT040.set("EXASCD", iASCD)
    if(!query.readAll(EXT040, 2, outData_EXT040)){
      IN60=true
      //logger.debug("L'enregistrement Marge Objet n'existe pas"+iASCD)
      update_CUGEX1("97", count, "L'enregistrement Marge n'existe pas pour l'assortiment "+iASCD)
      mi.error("L'enregistrement Marge Objet n'existe pas")
      return
    }
    iMOBJ=iMarge
  }
  /**
   * Research Flag in Table Extend 80/20 EXT070
   */
  private Integer rechercheFlag8020(){
    iFLAG=0
    DBAction query = database.table("EXT070").index("00").selection("EXCONO", "EXCUNO", "EXITNO", "EXSAAM", "EXSAAC", "EXTAUX","EXFLAG", "EXSAAT").build()
    DBContainer EXT070 = query.getContainer()
    EXT070.set("EXCONO", currentCompany)
    EXT070.set("EXCUNO", iCUNO)
    EXT070.set("EXITNO", iITNO)
    if(!query.readAll(EXT070, 3, outData_EXT070)){

    }
    return iFLAG
  }
  /**
   * Research in Table Extend TOC EXT042
   */
  private void rechercheToc(){
    DBAction query = database.table("EXT042").index("10").selection("EXCONO","EXCLEF","EXCUNO","EXHIE1","EXHIE2","EXHIE3","EXHIE4","EXHIE5",
      "EXCFI5","EXPOPN","EXBUAR","EXCFI1","EXTX15","EXADJT","EXFVDT","EXLVDT").build()
    DBContainer EXT042 = query.createContainer()
    EXT042.set("EXCONO", currentCompany)
    EXT042.set("EXCUNO", iCUNO)
    if(!query.readAll(EXT042, 2, outData_EXT042)) {

    }
  }
  /**
   * Update EXT075 before process 2
   *
   */
  private void majEXT075(){
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction TarifCompMajQuery = database.table("EXT075").index("00").selection("EXCHNO").build()
    DBContainer EXT075_Maj = TarifCompMajQuery.getContainer()
    EXT075_Maj.set("EXCONO",currentCompany)
    EXT075_Maj.set("EXPRRF",iPRRF)
    EXT075_Maj.set("EXCUCD",iCUCD)
    EXT075_Maj.set("EXCUNO",iCUNO)
    EXT075_Maj.set("EXFVDT",iFVDT as Integer)
    EXT075_Maj.set("EXITNO",iITNO)
    EXT075_Maj.set("EXOBV1",iOBV1)
    EXT075_Maj.set("EXOBV2",iOBV2)
    EXT075_Maj.set("EXOBV3",iOBV3)
    EXT075_Maj.set("EXVFDT",iVFDT as Integer)
    if(!TarifCompMajQuery.readLock(EXT075_Maj, updateCallBack_EXT075)){
      /*mi.error("L'enregistrement n'existe pas")
      return*/
    }
  }
  /**
   * Update EXT076 before Process 1
   *
   */
  private void majEXT076(){
    // Maj EXT077
    DBAction AxesSimpQueryMajQuery = database.table("EXT077").index("00").selection("EXPOPN","EXITTY","EXHIE2","EXHIE3","EXCHNO","EXMMO2","EXCPTL").build()
    DBContainer EXT077 = AxesSimpQueryMajQuery.getContainer()
    EXT077.set("EXCONO",currentCompany)
    EXT077.set("EXPRRF",iPRRF)
    EXT077.set("EXCUCD",iCUCD)
    EXT077.set("EXCUNO",iCUNO)
    EXT077.set("EXFVDT",iFVDT as Integer)
    if(!AxesSimpQueryMajQuery.readAll(EXT077, 5, outData_EXT077)){
      /*mi.error("L'enregistrement existe déjà")
      return*/
    }
    // Maj EXT076
    DBAction AxesSimpQueryMajQueryB = database.table("EXT077").index("00").selection("EXPOPN","EXITTY","EXHIE2","EXHIE3","EXCHNO","EXMMO2","EXCPTL").build()
    DBContainer EXT077B = AxesSimpQueryMajQueryB.getContainer()
    EXT077B.set("EXCONO",currentCompany)
    EXT077B.set("EXPRRF",iPRRF)
    EXT077B.set("EXCUCD",iCUCD)
    EXT077B.set("EXCUNO",iCUNO)
    EXT077B.set("EXFVDT",iFVDT as Integer)
    if(!AxesSimpQueryMajQueryB.readAll(EXT077B, 5, outData_EXT077_Maj76)){
      /*mi.error("L'enregistrement existe déjà")
      return*/
    }

  }
  /**
   * Update EXT075 before Process 1
   *
   */
  private void majEXT075_traitement2(){
    ExpressionFactory expressionTableCompTarif = database.getExpressionFactory("EXT075")
    expressionTableCompTarif = expressionTableCompTarif.eq("EXSAPR", "0")
    DBAction TarifCompMajQuery = database.table("EXT075").index("00").matching(expressionTableCompTarif).selection("EXSAP0","EXSAP2","EXSAPR","EXFPSY","EXMCUN","EXPOPN","EXITTY","EXHIE2","EXHIE3","EXITNO","EXOBV1","EXOBV2","EXOBV3","EXVFDT","EXOBV2","EXTUT1","EXPUPR","EXNEPR",
      "EXMOY2","EXTUM1","EXTUM2","EXTUT2").build()
    DBContainer EXT075_Maj = TarifCompMajQuery.getContainer()
    EXT075_Maj.set("EXCONO",currentCompany)
    EXT075_Maj.set("EXPRRF",iPRRF)
    EXT075_Maj.set("EXCUCD",iCUCD)
    EXT075_Maj.set("EXCUNO",iCUNO)
    EXT075_Maj.set("EXFVDT",iFVDT as Integer)
    if(!TarifCompMajQuery.readAll(EXT075_Maj, 5, outData_EXT075_T2)){
      /*mi.error("L'enregistrement n'existe pas")
      return*/
    }
    String oCONO = currentCompany
    String oPRLP = "0"
    def paramOIS017MI_ChgPriceList = ["CONO": oCONO, "PRRF": iPRRF, "CUCD": iCUCD, "CUNO": iCUNO_Blank, "FVDT": iFVDT, "PRLP": oPRLP]
    Closure<?> closure = {Map<String, String> response ->
      if(response.error != null){
        update_CUGEX1("97", count, "Echec mise à jour tarif "+iPRRF+"-"+response.errorMessage)
        //logger.debug("OIS017MI"+response.errorMessage)
        return mi.error(response.errorMessage)
      }
    }
    miCaller.call("OIS017MI", "ChgPriceList", paramOIS017MI_ChgPriceList, closure)
  }
  /**
   * Process 1 before data in Table Extend EXT076
   *
   */
  public void traitement1(){
    //logger.debug("traitement1")
    //logger.debug("value iFPSY = {$iFPSY}")
    //logger.debug("value iSAP0 = {$iSAP0}")
    //logger.debug("value iMCUN = {$iMCUN}")
    //logger.debug("value iMDIV = {$iMDIV}")
    //logger.debug("value iMOBJ = {$iMOBJ}")
    if(iFPSY==1){
      if(iMCUN>=iMDIV){
        double iCompareMCUN = 1-iMCUN
        if(iCompareMCUN!=0){
          iSAPR=iNEPR/(1-iMCUN)
        }
      }else{
        double iCompareMDIV = 1-iMDIV
        if(iCompareMDIV!=0){
          iSAPR=iNEPR/(1-iMDIV)
        }
      }
      if(iSAPR!=0){
        iMFIN=1-(iNEPR/iSAPR)
      }
    }else{
      iSAP1=(iNEPR/(1-iMOBJ))
      iMOY1 = iMOBJ
      if(iSAP0!=0){
        if(iSAP1==iSAP0){
          iSAP2=iSAP1
        }else{
          if(iFLAG==0){
            if(iSAP1>=iSAP0){
              iSAP2=iSAP1
            }else{
              if(iSAP0!=0){
                if(((iSAP1/iSAP0) - 1)< -0.2){
                  //logger.debug("value CDUV iSAP1 = {$iSAP1}")
                  //logger.debug("value CDUV iSAP0 = {$iSAP0}")
                  iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/150)
                  //logger.debug("value CDUV iSAP2 = {$iSAP2}")
                }else{
                  if(((iSAP1/iSAP0) - 1)>= -0.2 && ((iSAP1/iSAP0) - 1)< -0.1){
                    iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/80)
                  }else{
                    if(((iSAP1/iSAP0) - 1)>= -0.1 && ((iSAP1/iSAP0) - 1)< -0.05){
                      iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/25)
                    }else{
                      if(((iSAP1/iSAP0) - 1)>= -0.5 && ((iSAP1/iSAP0) - 1)< -0.025){
                        iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/15)
                      }else{
                        iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/10)
                      }
                    }
                  }
                }
              }
            }
          }
          if(iFLAG==1){
            if(iSAP0!=0){
              if(((iSAP1/iSAP0) - 1)< 0){
                if(((iSAP1/iSAP0) - 1)< -0.2){
                  iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/2.5)
                }else{
                  if(((iSAP1/iSAP0) - 1)>= -0.2 && ((iSAP1/iSAP0) - 1)< -0.1){
                    iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/2.1)
                  }else{
                    if(((iSAP1/iSAP0) - 1)>= -0.1 && ((iSAP1/iSAP0) - 1)< -0.05){
                      iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/2)
                    }else{
                      if(((iSAP1/iSAP0) - 1)>= -0.05 && ((iSAP1/iSAP0) - 1)< -0.025){
                        iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/1.36)
                      }else{
                        iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/1.25)
                      }
                    }
                  }
                }
              }else{
                if(((iSAP1/iSAP0) - 1) > 0.2){
                  iSAP2 = iSAP1
                }else{
                  if(((iSAP1/iSAP0) - 1)<= 0.2 && ((iSAP1/iSAP0) - 1)> 0.1){
                    iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/10)
                  }else{
                    if(((iSAP1/iSAP0) - 1)<=0.1 && ((iSAP1/iSAP0) - 1)>0.05){
                      iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/4.3)
                    }else{
                      if(((iSAP1/iSAP0) - 1)<= 0.05 && ((iSAP1/iSAP0) - 1)> 0.025){
                        iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/4.5)
                      }else{
                        iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/5)

                      }
                    }
                  }
                }
              }
            }
          }
          if(iFLAG==2){
            if(iSAP0!=0){
              if(((iSAP1/iSAP0) - 1)< 0){
                if(((iSAP1/iSAP0) - 1)< -0.2){
                  iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/5)
                }else{
                  if(((iSAP1/iSAP0) - 1)>= -0.2 && ((iSAP1/iSAP0) - 1)< -0.1){
                    iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/4)
                  }else{
                    if(((iSAP1/iSAP0) - 1)>= -0.1 && ((iSAP1/iSAP0) - 1)< -0.05){
                      iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/3)
                    }else{
                      if(((iSAP1/iSAP0) - 1)>= -0.05 && ((iSAP1/iSAP0) - 1)< -0.025){
                        iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/2.38)
                      }else{
                        iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/2)
                      }
                    }
                  }
                }
              }else{
                if(((iSAP1/iSAP0) - 1) > 0.2){
                  iSAP2 = iSAP1 *0.99
                }else{
                  if(((iSAP1/iSAP0) - 1)<= 0.2 && ((iSAP1/iSAP0) - 1)> 0.1){
                    iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/4)
                  }else{
                    if(((iSAP1/iSAP0) - 1)<=0.1 && ((iSAP1/iSAP0) - 1)>0.05){
                      iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/2)
                    }else{



                      if(((iSAP1/iSAP0) - 1)<= 0.05 && ((iSAP1/iSAP0) - 1)> 0.025){
                        iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/1.75)
                      }else{
                        iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/1.5)
                      }

                    }
                  }
                }
              }
            }
          }
        }
      }else{
        iSAP2=iSAP1
      }
    }
  }
  /**
   * Process 2 with data in Table Extend EXT076
   *
   */
  public void traitement2(){
    //logger.debug("traitement2")
    //logger.debug("value iITNO = {$iITNO}")
    //logger.debug("value iSAP0 = {$iSAP0}")
    //logger.debug("value iMOY2 = {$iMOY2}")
    //logger.debug("value iMMO2 = {$iMMO2}")
    //logger.debug("value iTUM2 = {$iTUM2}")
    //logger.debug("value iTUM1 = {$iTUM1}")
    //logger.debug("value iTUT2 = {$iTUT2}")
    //logger.debug("value iTUT1 = {$iTUT1}")
    //logger.debug("value iMCUN = {$iMCUN}")
    //logger.debug("value iSAP2 = {$iSAP2}")
    if(iMOY2<=(iMMO2+iTUM2) && iMOY2>=(iMMO2+iTUM1)){
      iMOY3=iMMO2
      //logger.debug("value iMOY3 = {$iMOY3}")
      iSAP3= (iNEPR/(1-iMOY3))
      //logger.debug("value iSAP3 = {$iSAP3}")
    }else{
      //logger.debug("value A2")
      iMOY3=iMOY2
      iSAP3=iSAP2
    }
    if(iSAP0!=0){
      if((iSAP3/iSAP0)-1<=iTUT2 && (iSAP3/iSAP0)-1>=iTUT1){
        //logger.debug("value B1")
        iSAP4=iSAP0
        iMOY4=1-(iNEPR/iSAP4)
      }else{
        //logger.debug("value B2")
        iMOY4=iMOY3
        iSAP4=iSAP3
      }
    }else{
      //logger.debug("value B3")
      iMOY4=iMOY3
      iSAP4=iSAP3
    }
    if(iMOY4<iMCUN){
      //logger.debug("value C1")
      iMFIN=iMCUN
      iSAPR=iNEPR/(1-iMFIN)
    }else{//aj algo
      //logger.debug("value C2")
      iMFIN=iMOY4//aj algo
      iSAPR=iSAP4//aj algo
    }
  }
  public void Update_EXT080(String istatus){
    status = istatus
    DBAction query = database.table("EXT080").index("00").build()
    DBContainer EXT080 = query.getContainer()
    EXT080.set("EXCONO", currentCompany)
    EXT080.set("EXPRRF", iPRRF)
    EXT080.set("EXCUCD", iCUCD)
    EXT080.set("EXCUNO", iCUNO)
    EXT080.set("EXFVDT", iFVDT as Integer)
    if(!query.readLock(EXT080, updateCallBack_EXT080)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack_EXT080 = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.set("EXSTAT", status)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }

  public void update_CUGEX1(String istatus, String icount, String imotif){
    if(!Ano){
      compteurCugex1++
      iCUNO_Blank=""
      if(compteurCugex1<=2){
        DBAction CUGEX1_query = database.table("CUGEX1").index("00").build()
        DBContainer CUGEX1 = CUGEX1_query.getContainer()
        CUGEX1.set("F1CONO", currentCompany)
        CUGEX1.set("F1FILE", "OPRICH")
        CUGEX1.set("F1PK01", iPRRF)
        CUGEX1.set("F1PK02", iCUCD)
        CUGEX1.set("F1PK03", iCUNO_Blank)
        CUGEX1.set("F1PK04", iFVDT)
        if (!CUGEX1_query.read(CUGEX1)) {
          executeCUSEXTMIAddFieldValue("OPRICH", iPRRF, iCUCD, iCUNO_Blank, iFVDT, "", "", "", "", istatus, icount, imotif)
        } else {
          count = icount
          status = istatus
          motif = imotif
          DBAction TarifCompCUGEX1 = database.table("CUGEX1").index("00").selection("F1N196","F1CHNO").build()
          DBContainer CUGEX1_B = TarifCompCUGEX1.getContainer()
          CUGEX1_B.set("F1CONO", currentCompany)
          CUGEX1_B.set("F1FILE", "OPRICH")
          CUGEX1_B.set("F1PK01", iPRRF)
          CUGEX1_B.set("F1PK02", iCUCD)
          CUGEX1_B.set("F1PK03", iCUNO_Blank)
          CUGEX1_B.set("F1PK04", iFVDT)
          if (!TarifCompCUGEX1.readLock(CUGEX1_B,updateCallBack_CUGEX1)) {

          }
        }
      }
    }

  }
  /**
   * Add CUGEX1 to set status (N196)
   */
  private executeCUSEXTMIAddFieldValue(String FILE, String PK01, String PK02, String PK03, String PK04, String PK05, String PK06, String PK07, String PK08, String A030, String N196, String A121){
    def parameters = ["FILE": FILE, "PK01": PK01, "PK02": PK02, "PK03": PK03, "PK04": PK04, "PK05": PK05, "PK06": PK06, "PK07": PK07, "PK08": PK08, "A030": A030, "N196": N196, "A121": A121]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed CUSEXTMI.AddFieldValue: "+ response.errorMessage)
      } else {
      }
    }
    miCaller.call("CUSEXTMI", "AddFieldValue", parameters, handler)
  }
  /**
   * Update CUGEX1 to set status (N196)
   */
  private executeCUSEXTMIChgFieldValue(String FILE, String PK01, String PK02, String PK03, String PK04, String PK05, String PK06, String PK07, String PK08, String A030, String N196, String A121){
    def parameters = ["FILE": FILE, "PK01": PK01, "PK02": PK02, "PK03": PK03, "PK04": PK04, "PK05": PK05, "PK06": PK06, "PK07": PK07, "PK08": PK08, "A030": A030, "N196": N196, "A121": A121]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed CUSEXTMI.ChgFieldValue: "+ response.errorMessage)
      } else {
      }
    }
    miCaller.call("CUSEXTMI", "ChgFieldValue", parameters, handler)
  }
}
