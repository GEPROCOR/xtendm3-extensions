/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT075
 * Description : The CalcRateSales transaction launch calcul to the EXT075 table (EXT075MI.CalcRateSales conversion).
 * Date         Changed By   Description
 * 20210510     CDUV         TARX16 - Calcul du tarif
 * 20220519     CDUV         lowerCamelCase has been fixed
 * 20221125     YYOU         recherchePrixBrut() - Suppression ctrl date validité contrat
 * 20221208     YYOU         updateCUGEX1() - Ajout motif erreur
 * 20230201     YYOU         updateCUGEX1() - Reset motif erreur
 * 20231206     RENARN       Ticket 0071775 SAP0 initialization problem
 * 20240124     SLAISNE      fix SAP0 initialization in function rechercheT0, ticket 0071775
 * 20240220     ARENARD      lowerCamelCase has been fixed, Map has been changed, EXLMDT handling has been fixed
 * 20240226     ARENARD      lowerCamelCase has been fixed, unused/commented out code removed
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class EXT075 extends ExtendM3Batch {
  private final LoggerAPI logger
  private final DatabaseAPI database
  private final ProgramAPI program
  private final BatchAPI batch
  private final MICallerAPI miCaller
  private final TextFilesAPI textFiles
  private Integer currentCompany
  private String rawData
  private int rawDataLength
  private int beginIndex
  private int endIndex
  private String logFileName
  private boolean IN60
  private String jobNumber

  private String currentDivision
  private final UtilityAPI utility
  private String iCUNO
  private String iPRRF
  private String iFVDT
  private String iCUCD
  private String iOBV1
  private String iOBV2
  private String iOBV3
  private String iVFDT


  //private String iTEDL
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

  private long iDTID
  private Integer numCount = 0
  private String count = 0
  private Integer compteurCugex1 = 0
  private boolean ano = false
  private String status
  private String motif
  private String iCunoBlank

  public EXT075(LoggerAPI logger, UtilityAPI utility,DatabaseAPI database, ProgramAPI program, BatchAPI batch, MICallerAPI miCaller, TextFilesAPI textFiles) {
    this.logger = logger
    this.database = database
    this.program = program
    this.batch = batch
    this.miCaller = miCaller
    this.textFiles = textFiles
    this.utility = utility
  }

  public void main() {
    // Get job number
    LocalDateTime timeOfCreation = LocalDateTime.now()
    jobNumber = program.getJobNumber() + timeOfCreation.format(DateTimeFormatter.ofPattern("yyMMdd")) + timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss"))

    logger.debug("Début" + program.getProgramName())
    logger.debug("referenceId = " + batch.getReferenceId().get())
    if(batch.getReferenceId().isPresent()){
      Optional<String> data = getJobData(batch.getReferenceId().get())
      logger.debug("data = " + data)
      performActualJob(data)
    } else {
      // No job data found
      logger.debug("Job data for job ${batch.getJobId()} is missing")
    }
  }
  // Get job data
  private Optional<String> getJobData(String referenceId){
    DBAction query = database.table("EXTJOB").index("00").selection("EXDATA").build()
    DBContainer container = query.createContainer()
    container.set("EXRFID", referenceId)
    if (query.read(container)){
      logger.debug("EXDATA = " + container.getString("EXDATA"))
      return Optional.of(container.getString("EXDATA"))
    } else {
      logger.debug("EXTJOB not found")
    }
    return Optional.empty()
  }
  // Perform actual job
  private performActualJob(Optional<String> data){
    if(!data.isPresent()){
      logger.debug("Job reference Id ${batch.getReferenceId().get()} is passed but data was not found")
      return
    }
    rawData = data.get()
    logger.debug("Début performActualJob")
    String inPRRF = getFirstParameter()
    String inCUCD = getNextParameter()
    String inCUNO = getNextParameter()
    String inFVDT = getNextParameter()

    currentCompany = (Integer)program.getLDAZD().CONO

    // Perform Job
    IN60=false
    LocalDateTime timeOfCreation = LocalDateTime.now()
    dateJour=timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    iCunoBlank=""
    currentCompany = (Integer)program.getLDAZD().CONO

    if(inPRRF == null || inPRRF == "" ){
      logger.debug("Code Tarif est obligatoire")
      return
    }
    if(inCUCD == null ||  inCUCD == ""){
      logger.debug("Devise est obligatoire")
      return
    }
    iCUNO = inCUNO
    if(iCUNO== null ||  iCUNO == ""){
      DBAction queryEXT080 = database.table("EXT080").index("20").selection("EXCUNO").build()
      DBContainer EXT080 = queryEXT080.getContainer()
      EXT080.set("EXCONO", currentCompany)
      EXT080.set("EXPRRF", inPRRF)
      EXT080.set("EXCUCD", inCUCD)
      EXT080.set("EXFVDT", inFVDT as Integer)
      if(!queryEXT080.readAll(EXT080, 4, outDataEXT0801)){
        logger.debug("L'enregistrement EXT080 n'existe pas donc pas de client")
        return
      }
      if(iCUNO==null || iCUNO==""){
        logger.debug("Code Client est obligatoire")
        return
      }
    }

    if(inFVDT == null || inFVDT == ""){
      logger.debug("Date Date Début Validité est obligatoire")
      return
    }else {
      logger.debug("value inFVDT= {$inFVDT}")
      iFVDT = inFVDT
      if (!utility.call("DateUtil", "isDateValid", iFVDT, "yyyyMMdd")) {
        logger.debug("Format Date de Validité incorrect")
        return
      }
    }
    iPRRF = inPRRF
    iCUCD = inCUCD
    iWHLO=""
    // Check Sales Price
    DBAction tarifQuery = database.table("OPRICH").index("00").build()
    DBContainer OPRICH = tarifQuery.getContainer()
    OPRICH.set("OJCONO",currentCompany)
    OPRICH.set("OJPRRF",iPRRF)
    OPRICH.set("OJCUCD",iCUCD)
    OPRICH.set("OJCUNO",iCUNO)
    OPRICH.set("OJFVDT",iFVDT as Integer)
    if (!tarifQuery.readAll(OPRICH, 5, outDataOPRICH)) {
      OPRICH.set("OJCONO",currentCompany)
      OPRICH.set("OJPRRF",iPRRF)
      OPRICH.set("OJCUCD",iCUCD)
      OPRICH.set("OJCUNO",iCunoBlank)
      OPRICH.set("OJFVDT",iFVDT as Integer)
      if (!tarifQuery.readAll(OPRICH, 5, outDataOPRICH)) {
        logger.debug("Entete Tarif n'existe pas")
        return
      }
    }
    logger.debug("Entete Tarif OK")

    updateCUGEX1("95", count, "?")

    // Delete data if exists in EXT076
    DBAction tarifCompQueryB = database.table("EXT076").index("00").build()
    DBContainer EXT076 = tarifCompQueryB.getContainer()
    EXT076.set("EXCONO",currentCompany)
    EXT076.set("EXPRRF",inPRRF)
    EXT076.set("EXCUCD",inCUCD)
    EXT076.set("EXCUNO",iCUNO)
    EXT076.set("EXFVDT",inFVDT as Integer)
    if(!tarifCompQueryB.readAllLock(EXT076, 5 ,deleteEXT076)){
    }

    // Retrieve EXT080
    iZIPL=""
    iPIDE=""
    DBAction incotermQuery = database.table("EXT080").index("00").selection("EXPIDE","EXWHLO","EXZIPL").build()
    DBContainer EXT080 = incotermQuery.getContainer()
    EXT080.set("EXCONO",currentCompany)
    EXT080.set("EXPRRF",iPRRF)
    EXT080.set("EXCUCD",iCUCD)
    EXT080.set("EXCUNO",iCUNO)
    EXT080.set("EXFVDT",iFVDT as Integer)
    if (!incotermQuery.readAll(EXT080, 5, outDataEXT080)) {
      updateCUGEX1("97", count, "Erreur Injection Tarif MEA ou pré-requis")
      logger.debug("Paramétrage EXT080 n'existe pas")
      logger.debug("Paramétrage EXT080 n'existe pas")
      return
    }

    logger.debug("Incoterm Vente OK")
    logger.debug("value iZIPL= {$iZIPL}")
    // table Simplification ou tunnel d'ajustement EXT041
    iTUM1 = 0
    iTUM2 = 0
    iTUT1 = 0
    iTUT2 = 0
    DBAction tableSimpQuery = database.table("EXT041").index("00").selection("EXBOBE","EXBOHE","EXBOBM","EXBOHM").build()
    DBContainer EXT041 = tableSimpQuery.getContainer()
    EXT041.set("EXCONO",currentCompany)
    EXT041.set("EXTYPE","MARG")
    EXT041.set("EXCUNO",iCUNO)
    if (!tableSimpQuery.readAll(EXT041, 3, outDataEXT041Marg)){
      logger.debug("value EXT041 nok")
      EXT041.set("EXCONO",currentCompany)

      EXT041.set("EXTYPE","MARG")
      EXT041.set("EXCUNO"," ")
      if (!tableSimpQuery.readAll(EXT041, 3, outDataEXT041Marg)){
        logger.debug("value EXT041 bis nok")
      }
    }

    tableSimpQuery = database.table("EXT041").index("00").selection("EXBOBE","EXBOHE","EXBOBM","EXBOHM").build()
    EXT041 = tableSimpQuery.getContainer()
    EXT041.set("EXCONO",currentCompany)
    EXT041.set("EXTYPE","T0T3")
    EXT041.set("EXCUNO",iCUNO)
    if (!tableSimpQuery.readAll(EXT041, 3, outDataEXT041T0T3)){
      logger.debug("value EXT041 nok")
      EXT041.set("EXCONO",currentCompany)

      EXT041.set("EXTYPE","T0T3")
      EXT041.set("EXCUNO"," ")
      if (!tableSimpQuery.readAll(EXT041, 3, outDataEXT041T0T3)){
        logger.debug("value EXT041 bis nok")
      }
    }
    logger.debug("value iTUM1= {$iTUM1}") //marge
    logger.debug("value iTUM2= {$iTUM2}") //marge
    logger.debug("value iTUT1= {$iTUT1}") //T0T3
    logger.debug("value iTUT2= {$iTUT2}") //T0T3

    // Read record EXT075, Process and Update EXT075/EXT076
    iASCD = ""
    DBAction tableCompTarif = database.table("EXT075").index("00").selection("EXCUNO","EXASCD","EXITNO","EXOBV1","EXOBV2","EXOBV3","EXVFDT").build()
    DBContainer EXT075 = tableCompTarif.getContainer()
    EXT075.set("EXCONO",currentCompany)
    EXT075.set("EXPRRF",iPRRF)
    EXT075.set("EXCUCD",iCUCD)
    EXT075.set("EXCUNO",iCUNO)
    EXT075.set("EXFVDT",iFVDT as Integer)
    if (!tableCompTarif.readAll(EXT075, 5, outDataEXT075)) {
      updateCUGEX1("97", count, "Erreur Pré-requis (vérifier compte P, Incoterm, Devise…)")
      logger.debug("Aucune ligne Tarif déja créée")
      logger.debug("Aucune ligne Tarif déja créée")
      return
    }
    logger.debug("Ligne Tarif comp OK")
    if(IN60){
      return
    }

    // Update EXT076
    majEXT076()

    // Update EXT076
    majEXT075Traitement2()

    count = numCount
    updateCUGEX1("99", count, "?")

    // Delete file EXTJOB
    deleteEXTJOB()
  }
  // Get first parameter
  private String getFirstParameter(){
    logger.debug("rawData = " + rawData)
    rawDataLength = rawData.length()
    beginIndex = 0
    endIndex = rawData.indexOf(";")
    // Get parameter
    String parameter = rawData.substring(beginIndex, endIndex)
    logger.debug("parameter = " + parameter)
    return parameter
  }
  // Get next parameter
  private String getNextParameter(){
    beginIndex = endIndex + 1
    endIndex = rawDataLength - rawData.indexOf(";") - 1
    rawData = rawData.substring(beginIndex, rawDataLength)
    rawDataLength = rawData.length()
    beginIndex = 0
    endIndex = rawData.indexOf(";")
    // Get parameter
    String parameter = rawData.substring(beginIndex, endIndex)
    logger.debug("parameter = " + parameter)
    return parameter
  }
  /**
   * Delete records related to the current job from EXTJOB table
   */
  public void deleteEXTJOB(){
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXTJOB").index("00").build()
    DBContainer EXTJOB = query.getContainer()
    EXTJOB.set("EXRFID", batch.getReferenceId().get())
    if(!query.readAllLock(EXTJOB, 1, updateCallBackEXTJOB)){
    }
  }
  // updateCallBackEXTJOB :: Delete EXTJOB
  Closure<?> updateCallBackEXTJOB = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  // Log message
  void logMessage(String header, String message) {
    textFiles.open("FileImport")
    logFileName = "MSG_" + program.getProgramName() + "." + "batch" + "." + jobNumber + ".csv"
    if(!textFiles.exists(logFileName)) {
      log(header)
      log(message)
    }
  }
  // Log
  void log(String message) {
    IN60 = true
    logger.debug(message)
    message = LocalDateTime.now().toString() + ";" + message
    Closure<?> consumer = { PrintWriter printWriter ->
      printWriter.println(message)
    }
    textFiles.write(logFileName, "UTF-8", true, consumer)
  }
  // outDataEXT0801 :: Retrieve EXT080
  Closure<?> outDataEXT0801 = { DBContainer EXT080 ->
    iCUNO = EXT080.get("EXCUNO")
  }
  // outDataEXT051 :: Retrieve EXT051
  Closure<?> outDataEXT051 = { DBContainer EXT051 ->
    iPOPN = EXT051.get("EXDATA")
    logger.debug("value iPOPN EXT051 = {$iPOPN}")
  }
  // outDataEXT081 :: Retrieve EXT081
  Closure<?> outDataEXT081 = { DBContainer EXT081 ->
    String oFDAT = EXT081.get("EXFDAT")
    logger.debug("value FDAT EXT051 = {$oFDAT}")
    DBAction queryEXT051 = database.table("EXT051").index("00").selection("EXDATA").build()
    DBContainer EXT051 = queryEXT051.getContainer()
    EXT051.set("EXCONO",currentCompany)
    EXT051.set("EXASCD",iASCD)
    EXT051.set("EXCUNO",iCUNO)
    EXT051.set("EXDAT1",oFDAT as Integer)
    EXT051.set("EXTYPE","POPN")
    if (!queryEXT051.readAll(EXT051, 5, outDataEXT051)){

    }
  }
  // outDataEXT075 :: Read EXT075 and Process1
  Closure<?> outDataEXT075= { DBContainer EXT075 ->
    iASCD = EXT075.get("EXASCD")
    iITNO = EXT075.get("EXITNO")
    iOBV1 = EXT075.get("EXOBV1")
    iOBV2 = EXT075.get("EXOBV2")
    iOBV3 = EXT075.get("EXOBV3")
    iVFDT = EXT075.get("EXVFDT")
    iCUNO = EXT075.get("EXCUNO")
    // Flag PSYCHO
    iFPSY = 0
    DBAction psychoQuery = database.table("CUGEX1").index("00").selection("F1CHB1").build()
    DBContainer CUGEX1 = psychoQuery.getContainer()
    CUGEX1.set("F1CONO",currentCompany)
    CUGEX1.set("F1FILE","OCUSIT")
    CUGEX1.set("F1PK01",iCUNO)
    CUGEX1.set("F1PK02",iITNO)
    if (!psychoQuery.readAll(CUGEX1, 4, outDataCUGEX1)){

    }
    logger.debug("value iFPSY= {$iFPSY}")
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
    DBAction articleQuery = database.table("MITMAS").index("00").selection("MMDTID","MMCFI5","MMITGR","MMBUAR","MMITTY","MMHIE1","MMHIE2","MMHIE3","MMHIE4","MMHIE5","MMCFI1").build()
    DBContainer MITMAS = articleQuery.getContainer()
    MITMAS.set("MMCONO",currentCompany)
    MITMAS.set("MMITNO",iITNO)
    if (!articleQuery.readAll(MITMAS, 2, outDataMITMAS)){
      updateCUGEX1("97", count, "L'article "+iITNO+" n'existe pas dans la base article (MMS001)")
      logger.debug("Article n'existe pas")
      logger.debug("Article n'existe pas")
      return
    }
    logger.debug("MITMAS OK")

    // Recherche contrat
    iSUNO = ""
    iVAGN=""
    iPUPR = recherchePrixBrut()
    logger.debug("value iPUPR= {$iPUPR}")
    iNEPR =  rechercheNEPR(iPUPR)
    logger.debug("value iNEPR= {$iNEPR}")
    iSAP0 = rechercheT0()
    logger.debug("value iSAP0= {$iSAP0}")
    if(iSAP0!=0){
      iMOY0=(iSAP0-iNEPR)/iSAP0
    }
    // Recherche Enseigne
    iPOPN=""
    DBAction enseigneQuery = database.table("MITPOP").index("00").selection("MPPOPN","MPVFDT","MPLVDT").build()
    DBContainer MITPOP = enseigneQuery.getContainer()
    MITPOP.set("MPCONO",currentCompany)
    MITPOP.set("MPALWT","3" as Integer)
    MITPOP.set("MPALWQ","ENS")
    MITPOP.set("MPITNO",iITNO)
    if (!enseigneQuery.readAll(MITPOP, 4, outDataMITPOP)){
    }
    // outDataEXT075 :: Retrieve EXT075
    DBAction queryEXT081 = database.table("EXT081").index("00").selection("EXFDAT").build()
    DBContainer EXT081 = queryEXT081.getContainer()
    EXT081.set("EXCONO", currentCompany)
    EXT081.set("EXPRRF", iPRRF)
    EXT081.set("EXCUCD", iCUCD)
    EXT081.set("EXCUNO", iCUNO)
    EXT081.set("EXFVDT", iFVDT  as Integer)
    EXT081.set("EXASCD", iASCD)
    if(!queryEXT081.readAll(EXT081, 6, outDataEXT081)){

    }

    logger.debug("value iPOPN= {$iPOPN}")
    // Marge objectif et Marge Plancher EXT040
    iMOBJ = 0
    iMCUN = 0
    iMDIV = 0
    rechercheMarge()

    logger.debug("value iMOBJ= {$iMOBJ}")
    logger.debug("value iMCUN= {$iMCUN}")
    logger.debug("value iMDIV= {$iMDIV}")
    if(IN60){
      return
    }
    // Flag 80/20
    iFLAG=rechercheFlag8020()
    logger.debug("value iFLAG= {$iFLAG}")
    // TOC EXT042
    iAjustement=0
    rechercheToc()
    logger.debug("value iAjustement= {$iAjustement}")
    //iMOBJ = iMOBJ*(1+iAjustement)
    iMOBJ = iMOBJ +iAjustement
    logger.debug("value iMOBJ= {$iMOBJ}")

    iSAP1=0
    iSAP2=0
    traitement1()
    logger.debug("value iNEPR= {$iNEPR}")
    logger.debug("value iSAP1= {$iSAP1}")
    logger.debug("value iSAP2= {$iSAP2}")
    iMOY2=0
    if(iSAP2!=0){
      iMOY2=1-(iNEPR/iSAP2)
    }
    logger.debug("value iMOY2= {$iMOY2}")
    logger.debug("value iHIE2= {$iHIE2}")
    // Alim EXT075
    majEXT075()

    // Cumul EXT076
    if(iFPSY==0){
      // tables Axes simplificateurs pour MMO2
      LocalDateTime timeOfCreation = LocalDateTime.now()
      DBAction axesSimpQuery = database.table("EXT076").index("00").selection("EXCPTL","EXMMO2","EXCHNO").build()
      DBContainer EXT076 = axesSimpQuery.getContainer()
      EXT076.set("EXCONO",currentCompany)
      EXT076.set("EXPRRF",iPRRF)
      EXT076.set("EXCUCD",iCUCD)
      EXT076.set("EXCUNO",iCUNO)
      EXT076.set("EXFVDT",iFVDT as Integer)
      EXT076.set("EXPOPN",iPOPN)
      EXT076.set("EXITTY",iITTY)
      EXT076.set("EXHIE2",iHIE2)
      EXT076.set("EXHIE3",iHIE3)
      if (!axesSimpQuery.readLock(EXT076,updateCallBackEXT076)) {
        EXT076.set("EXMMO2", iMOY2)
        EXT076.set("EXCPTL", 1)
        EXT076.set("EXASCD", iASCD)
        EXT076.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT076.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        EXT076.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT076.setInt("EXCHNO", 1)
        EXT076.set("EXCHID", program.getUser())
        axesSimpQuery.insert(EXT076)
      }
    }
    iSAPR=0
  }
  // updateCallBackCUGEX1 :: Update CUGEX1
  Closure<?> updateCallBackCUGEX1 = { LockedResult  lockedResult ->
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
  // deleteEXT076 :: Delete EXT076
  Closure<?> deleteEXT076 = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  // updateCallBackEXT076 :: Update EXT076
  Closure<?> updateCallBackEXT076 = { LockedResult  lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    int compteurLigne = lockedResult.get("EXCPTL")
    double luMMO2 = lockedResult.get("EXMMO2")
    luMMO2=luMMO2+iMOY2
    lockedResult.set("EXMMO2", luMMO2)
    lockedResult.setInt("EXCPTL", compteurLigne + 1)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // outDataEXT076 retreive EXT076
  Closure<?> outDataEXT076 = { DBContainer EXT076 ->
    String oPOPN = EXT076.get("EXPOPN")
    String oITTY = EXT076.get("EXITTY")
    String oHIE2 = EXT076.get("EXHIE2")
    String oHIE3 = EXT076.get("EXHIE3")
    DBAction axesSimpQueryUpd = database.table("EXT076").index("00").selection("EXCPTL","EXMMO2","EXCHNO").build()
    DBContainer updEXT076 = axesSimpQueryUpd.getContainer()
    updEXT076.set("EXCONO",currentCompany)
    updEXT076.set("EXPRRF",iPRRF)
    updEXT076.set("EXCUCD",iCUCD)
    updEXT076.set("EXCUNO",iCUNO)
    updEXT076.set("EXFVDT",iFVDT as Integer)
    updEXT076.set("EXPOPN",oPOPN)
    updEXT076.set("EXITTY",oITTY)
    updEXT076.set("EXHIE2",oHIE2)
    updEXT076.set("EXHIE3",oHIE3)
    if (!axesSimpQueryUpd.readLock(updEXT076,updateCallBackUpdEXT076)) {
    }
  }
  //updateCallBackUpdEXT076 : Update EXT076 with calculation
  Closure<?> updateCallBackUpdEXT076= { LockedResult  lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    int compteurLigne = lockedResult.get("EXCPTL")
    double luMMO2 = lockedResult.get("EXMMO2")
    luMMO2=luMMO2/compteurLigne
    lockedResult.set("EXMMO2", luMMO2)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // outDataEXT075T2 : Retrieve EXT075 and Process 2                                                                                                                                                                     075T2 :: Retrieve EXT075 with
  Closure<?> outDataEXT075T2 = { DBContainer EXT075 ->
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

    logger.debug("value iSAPRBeforeT2 {$iSAPR }")
    if(iFPSY==0){
      iMMO2=0
      DBAction axesSimpQuery = database.table("EXT076").index("00").selection("EXCPTL","EXMMO2","EXCHNO").build()
      DBContainer EXT076 = axesSimpQuery.getContainer()
      EXT076.set("EXCONO",currentCompany)
      EXT076.set("EXPRRF",iPRRF)
      EXT076.set("EXCUCD",iCUCD)
      EXT076.set("EXCUNO",iCUNO)
      EXT076.set("EXFVDT",iFVDT as Integer)
      EXT076.set("EXPOPN",iPOPN)
      EXT076.set("EXITTY",iITTY)
      EXT076.set("EXHIE2",iHIE2)
      EXT076.set("EXHIE3",iHIE3)
      if (!axesSimpQuery.readLock(EXT076,outDataEXT076T2)) {
      }

      iSAPR=0
      iMFIN=0
      iMOY4=0
      iMOY3=0
      iSAP4=0
      iSAP3=0
      traitement2()

      DBAction majQueryEXT075 = database.table("EXT075").index("00").selection("EXCHNO").build()
      DBContainer updEXT075 = majQueryEXT075.getContainer()
      updEXT075.set("EXCONO",currentCompany)
      updEXT075.set("EXPRRF",iPRRF)
      updEXT075.set("EXCUCD",iCUCD)
      updEXT075.set("EXCUNO",iCUNO)
      updEXT075.set("EXFVDT",iFVDT as Integer)
      updEXT075.set("EXITNO",iITNO)
      updEXT075.set("EXOBV1",iOBV1)
      updEXT075.set("EXOBV2",iOBV2)
      updEXT075.set("EXOBV3",iOBV3)
      updEXT075.set("EXVFDT",iVFDT as Integer)
      if (!majQueryEXT075.readLock(updEXT075,updateCallBackUpdEXT075)) {
        /*logger.debug("Enreg n'existe pas")
        return */
      }
    }
    iPDCC = ""

    String iDIVI =""
    DBAction deviseQuery = database.table("CSYTAB").index("00").selection("CTPARM").build()
    DBContainer CSYTAB = deviseQuery.getContainer()
    CSYTAB.set("CTCONO",currentCompany)
    CSYTAB.set("CTDIVI",  iDIVI)
    CSYTAB.set("CTSTCO",  "CUCD")
    CSYTAB.set("CTSTKY", iCUCD)
    if (!deviseQuery.readAll(CSYTAB, 4, outDataCSYTAB)){
      updateCUGEX1("97", count, "La devise "+iCUCD+" n'existe pas (Sélection de la devise dans la liste du Mashup)")
      logger.debug("Devise n'existe pas")
      logger.debug("Devise n'existe pas")
      return
    }
    logger.debug("value iPDCC= {$iPDCC}")
    logger.debug("value iSAPR= {$iSAPR}")
    logger.debug("value iPUPR= {$iPUPR}")
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

    logger.debug("value oPRIX = {$oPRIX}")
    Map<String, String> paramOIS017MI = ["CONO": oCONO, "PRRF": iPRRF, "CUCD": iCUCD, "CUNO": iCunoBlank, "FVDT": iFVDT, "ITNO": iITNO, "OBV1": iOBV1, "OBV2": iOBV2, "OBV3": iOBV3, "SAPR": oPRIX]
    Closure<?> closure = {Map<String, String> response ->
      if(response.error != null){
        logger.debug("OIS017MI"+response.errorMessage)
        updateCUGEX1("97", count, "Echec mise à jour prix de l'article "+iITNO+":"+oPRIX+"-"+response.errorMessage)
        return logger.debug(response.errorMessage)
      }
    }
    miCaller.call("OIS017MI", "UpdBasePrice", paramOIS017MI, closure)
    numCount++
  }
  // outDataEXT076T2 : Retrieve EXT076
  Closure<?> outDataEXT076T2 = { DBContainer EXT076 ->
    iMMO2 = EXT076.get("EXMMO2") as double
    if(iMMO2>0.99)iMMO2=0.99
  }
  // outDataEXT080 : Retrieve EXT080
  Closure<?> outDataEXT080= { DBContainer EXT080 ->
    iZIPL = EXT080.get("EXZIPL")
    iWHLO = EXT080.get("EXWHLO")
    iPIDE = EXT080.get("EXPIDE")
  }
  // outDataEXT041Marg : Retrieve EXT041 Marge
  Closure<?> outDataEXT041Marg= { DBContainer EXT041 ->
    iTUM1 = EXT041.get("EXBOBM") as double
    iTUM2 = EXT041.get("EXBOHM") as double
    iTUM1 = iTUM1/100
    iTUM2 = iTUM2/100
  }
  // outDataEXT041T0T3 : Retrieve 41 Marge
  Closure<?> outDataEXT041T0T3= { DBContainer EXT041 ->
    iTUT1 = EXT041.get("EXBOBE") as double
    iTUT2 = EXT041.get("EXBOHE") as double
    iTUT1 = iTUT1/100
    iTUT2 = iTUT2/100
  }
  // outDataEXT042 : Retrieve EXT042 TOC
  Closure<?> outDataEXT042= { DBContainer EXT042 ->
    Integer oNumFVDT
    Integer oNumLVDT
    oNumFVDT =  EXT042.get("EXFVDT") as Integer
    oNumLVDT =  EXT042.get("EXLVDT") as Integer
    String luPOPN=  EXT042.get("EXPOPN")
    String luBUAR=  EXT042.get("EXBUAR")
    String luHIE3=  EXT042.get("EXHIE3")
    String luHIE4=  EXT042.get("EXHIE4")
    String luHIE5=  EXT042.get("EXHIE5")
    String luCFI1=  EXT042.get("EXCFI1")
    String luCFI5=  EXT042.get("EXCFI5")
    String luVAGN=  EXT042.get("EXTX15")
    double iAjustementLu = EXT042.get("EXADJT") as double
    if(oNumLVDT==0)oNumLVDT=99999999
    if(dateJour>= oNumFVDT && dateJour <=oNumLVDT){
      if((luPOPN.trim().equals(iPOPN.trim())||luPOPN.trim()=="")
        &&(luVAGN.trim().equals(iVAGN.trim())||luVAGN.trim()=="")
        &&(luBUAR.trim().equals(iBUAR.trim())||luBUAR.trim()=="")
        &&(luHIE3.trim().equals(iHIE3.trim())||luHIE3.trim()=="")
        &&(luHIE4.trim().equals(iHIE4.trim())||luHIE4.trim()=="")
        &&(luHIE5.trim().equals(iHIE5.trim())||luHIE5.trim()=="")
        &&(luCFI1.trim().equals(iCFI1.trim())||luCFI1.trim()=="")
        &&(luCFI5.trim().equals(iCFI5.trim())||luCFI5.trim()=="")){

        iAjustement = iAjustementLu/100
        logger.debug("value iAjustement={$iAjustement}")
      }
    }
  }
  // outDataOPRICH : Retrieve OPRICH
  Closure<?> outDataOPRICH = { DBContainer OPRICH ->
  }
  // outDataCUGEX1 : Retrieve CUGEX1
  Closure<?> outDataCUGEX1 = { DBContainer CUGEX1 ->
    iFPSY = CUGEX1.get("F1CHB1") as Integer
  }
  // outDataMITMAS : Retrieve MITMAS for Research in Process1
  Closure<?> outDataMITMAS = { DBContainer MITMAS ->
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
  // outDataMITMAS : Retrieve MITMAS for Research in Process2
  Closure<?> outDataMITMAS2 = { DBContainer MITMAS ->
    iPDCC = MITMAS.get("MMPDCC")
  }
  //outDataCSYTAB : Retrieve CSYTAB
  Closure<?> outDataCSYTAB = { DBContainer CSYTAB ->
    String oPDCC=CSYTAB.get("CTPARM")
    logger.debug("value oPDCC= {$oPDCC}")
    if(oPDCC.trim().length()>=8){
      iPDCC = oPDCC.trim().substring(7,8)
    }
    logger.debug("value iPDCC= {$iPDCC}")
  }
  //outDataMITPOP : Retrieve MITPOP
  Closure<?> outDataMITPOP = { DBContainer MITPOP ->
    Integer oNumVFDT
    Integer oNumLVDT
    oNumVFDT =  MITPOP.get("MPVFDT") as Integer
    oNumLVDT =  MITPOP.get("MPLVDT") as Integer
    if(oNumLVDT==0)oNumLVDT=99999999
    if(dateJour>= oNumVFDT && dateJour <=oNumLVDT && iPOPN==""){
      iPOPN = MITPOP.get("MPPOPN")
    }
  }
  //outDataEXT040 : Retrieve EXT040
  Closure<?> outDataEXT040= { DBContainer EXT040 ->
    Integer oFDAT = EXT040.get("EXFDAT")  as Integer
    Integer oTDAT = EXT040.get("EXTDAT")  as Integer
    if(oTDAT==0)oTDAT=99999999
    double oAGTP = EXT040.get("EXMARG") as double
    if(dateJour>= oFDAT && dateJour <=oTDAT){
      iMarge = EXT040.get("EXMARG") as double
      iMarge = iMarge/100
    }
  }
  //outDataEXT070 : Retrieve EXT070
  Closure<?> outDataEXT070 = { DBContainer EXT070 ->
    iFLAG = EXT070.get("EXFLAG") as Integer
  }
  //updateCallBackEXT075 : update EXT075 before process 1
  Closure<?> updateCallBackEXT075 = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    logger.debug("value majT1= {$iSAP1}")
    logger.debug("value majT1= {$iSAP2}")
    logger.debug("value majT1= {$iSAPR}")
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
    //if(iTEDL != "") lockedResult.set("EXTEDL",iTEDL)
    if(svAGNB != "") lockedResult.set("EXAGNB",svAGNB)
    if(svSUNO != "") lockedResult.set("EXSUNO",svSUNO)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)          // A REACTIVER
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // updateCallBackUpdEXT075 :  update EXT075 before process 2
  Closure<?> updateCallBackUpdEXT075 = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")

    if(iMMO2 != "") lockedResult.set("EXMMO2",iMMO2)
    if(iMOY4 != "") lockedResult.set("EXMOY4",iMOY4)
    if(iSAP4 != "") lockedResult.set("EXSAP4",iSAP4)
    if(iMOY3 != "") lockedResult.set("EXMOY3",iMOY3)
    if(iSAP3 != "") lockedResult.set("EXSAP3",iSAP3)
    if(iMFIN != "") lockedResult.set("EXMFIN",iMFIN)
    if(iSAPR != "") lockedResult.set("EXSAPR",iSAPR)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)   // A REACTIVER
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }

  /**
   * Research Purchase Price in agreement
   *
   */
  private double recherchePrixBrut(){
    svAGNB=""
    svSUNO=""
    svDIVI=""
    szOBV1=""
    szOBV2=""
    szOBV3=""
    szOBV4=""
    szFVDT=""
    boolean Retour=false
    double oNumPUPR=0
    Integer svAGPT= 0
    String oVAGN="00000"
    String svPRIO=""
    String oPRIO=""
    Integer saveFVDT=0
    Integer iIntFVDT=iFVDT as Integer

    DBAction tableArticleDepot = database.table("MITBAL").index("00").selection("MBSUNO","MBDIVI").build()
    DBContainer MITBAL = tableArticleDepot.getContainer()
    MITBAL.set("MBCONO", currentCompany)
    MITBAL.set("MBITNO", iITNO)
    MITBAL.set("MBWHLO", iWHLO)
    tableArticleDepot.readAll(MITBAL,3,{DBContainer recordMITBAL->
      svSUNO = recordMITBAL.get("MBSUNO")
      svDIVI = recordMITBAL.get("MBDIVI")
    })

    DBAction tablePromo = database.table("OPROMH").index("00").selection("FZTX15").build()
    DBContainer OPROMH = tablePromo.getContainer()
    OPROMH.set("FZCONO", currentCompany)
    OPROMH.set("FZDIVI", svDIVI)
    OPROMH.set("FZPIDE", iPIDE)
    tablePromo.readAll(OPROMH,3,{DBContainer recordOPROMH->
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
    logger.debug("value iIntFVDT= {$iIntFVDT}")
    logger.debug("value oVAGN= {$oVAGN}")

    DBAction tableEXT030 = database.table("EXT030").index("10").selection("EXZIPP","EXPRIO").build()
    DBContainer EXT030 = tableEXT030.getContainer()
    EXT030.set("EXCONO", currentCompany)
    EXT030.set("EXZIPS", iZIPL)
    tableEXT030.readAll(EXT030,2,{DBContainer recordEXT030->
      String oZIPP = recordEXT030.get("EXZIPP")
      oPRIO = recordEXT030.get("EXPRIO")

      logger.debug("value oZIPP= {$oZIPP}")
      logger.debug("value oPRIO= {$oPRIO}")
      DBAction tableCompContrat = database.table("EXT032").index("30").selection("EXAGNB").build()
      DBContainer EXT032Liste = tableCompContrat.getContainer()
      EXT032Liste.set("EXCONO", currentCompany)
      EXT032Liste.set("EXZIPP", oZIPP)
      EXT032Liste.set("EXSUNO", svSUNO)
      tableCompContrat.readAll(EXT032Liste,3,{DBContainer recordEXT032->
        String oAGNB = recordEXT032.get("EXAGNB")
        logger.debug("value oAGNB= {$oAGNB}")
        DBAction tableContrat = database.table("MPAGRH").index("00").selection("AHVAGN","AHAGTP","AHPAST","AHFVDT","AHUVDT").build()
        DBContainer MPAGRH = tableContrat.getContainer()
        MPAGRH.set("AHCONO", currentCompany)
        MPAGRH.set("AHSUNO", svSUNO)
        MPAGRH.set("AHAGNB", oAGNB)
        tableContrat.readAll(MPAGRH,3,{DBContainer recordMPAGRH ->
          String oPAST = recordMPAGRH.get("AHPAST")
          Integer oFVDT = recordMPAGRH.get("AHFVDT")  as Integer
          Integer oUVDT = recordMPAGRH.get("AHUVDT")  as Integer
          if(oUVDT==0)oUVDT=99999999
          logger.debug("value oPAST= {$oPAST}")
          logger.debug("value oFVDT= {$oFVDT}")
          logger.debug("value oUVDT= {$oUVDT}")
          String svVAGN =recordMPAGRH.get("AHVAGN")
          logger.debug("value svVAGN= {$svVAGN}")
          logger.debug("Get MPAGRH VAGN={$svVAGN} / iFVDT={$iIntFVDT} : PAST={$oPAST}, FVDT={$oFVDT}, UVDT={$oUVDT}")
          if(iIntFVDT>= oFVDT && iIntFVDT <=oUVDT && oPAST=="40"){
            if(recordMPAGRH.get("AHVAGN")==oVAGN ){
              ExpressionFactory expressionMPAGRL = database.getExpressionFactory("MPAGRL")
              expressionMPAGRL = expressionMPAGRL.eq("AIOBV1", iITNO)
              logger.debug("MPAGRH  ok")
              DBAction tableLigneContrat = database.table("MPAGRL").index("00").matching(expressionMPAGRL).selection("AIAGPT","AIFVDT","AIUVDT","AIOBV1","AIOBV2","AIOBV3","AIOBV4").build()
              DBContainer MPAGRL = tableLigneContrat.getContainer()
              MPAGRL.set("AICONO", currentCompany)
              MPAGRL.set("AISUNO", svSUNO)
              MPAGRL.set("AIAGNB", oAGNB)
              MPAGRL.set("AIGRPI", "30" as Integer)
              MPAGRL.set("AIOBV1", iITNO)
              tableLigneContrat.readAll(MPAGRL,5,{DBContainer recordMPAGRL ->
                Integer oMpagrlFVDT = recordMPAGRL.get("AIFVDT") as Integer
                Integer oMpagrlUVDT = recordMPAGRL.get("AIUVDT") as Integer
                Integer oAGTP = recordMPAGRL.get("AIAGPT") as Integer
                if(oMpagrlUVDT==0)oUVDT=99999999
                logger.debug("MPAGRL ok")

                DBAction tableLigneCompContrat = database.table("MPAGRP").index("00").selection("AJPUPR").build()
                DBContainer MPAGRP = tableLigneCompContrat.getContainer()
                MPAGRP.set("AJCONO", currentCompany)
                MPAGRP.set("AJSUNO", svSUNO)
                MPAGRP.set("AJAGNB", oAGNB)
                MPAGRP.set("AJGRPI", "30" as Integer)
                MPAGRP.set("AJOBV1", iITNO)
                tableLigneCompContrat.readAll(MPAGRP,5,{DBContainer recordMPAGRP ->
                  logger.debug("MPAGRP pas miroir ok")
                  String oPUPR = recordMPAGRP.get("AJPUPR")
                  logger.debug("value oPUPR= {$oPUPR}")
                  logger.debug("value svAGPT= {$svAGPT}")
                  logger.debug("value oAGTP= {$oAGTP}")
                  logger.debug("Get MPAGRL ITNO={$iITNO} : AGPT={$oAGTP}({$svAGPT}), FVDT={$oMpagrlFVDT}, UVDT={$oMpagrlUVDT}")
                  if((svAGPT==0 || svAGPT >= oAGTP) && iIntFVDT>= oMpagrlFVDT && iIntFVDT <=oMpagrlUVDT){
                    if(svPRIO=="" || oPRIO==svPRIO){
                      //if(saveFVDT==0||saveFVDT>oMpagrlFVDT){
                      logger.debug("prix brut pas  miroir ok")
                      svAGPT = oAGTP
                      //Retour=true
                      oNumPUPR=oPUPR as double
                      iSUNO = svSUNO
                      //iVAGN= recordMPAGRH.get("AHVAGN")
                      svAGNB = oAGNB
                      szOBV1 = recordMPAGRL.get("AIOBV1")
                      szOBV2 = recordMPAGRL.get("AIOBV2")
                      szOBV3 = recordMPAGRL.get("AIOBV3")
                      szOBV4 = recordMPAGRL.get("AIOBV4")
                      szFVDT = oMpagrlFVDT
                      svPRIO=oPRIO
                      saveFVDT=oMpagrlFVDT
                      //}
                    }
                  }
                })
              })
            }
          }
        })
      })
    })
    return oNumPUPR
  }
  /**
   * Research Calcul NEPR
   **/
  private double rechercheNEPR(double iPUPR){
    double oNEPR=iPUPR
    double oTaxeSp = 0
    double oFraisFSA = 0
    double oTauxRFA = 0
    double oRFA = 0
    String decimalSeparator = ","
    DBAction CUGEX1query = database.table("CUGEX1").index("00").selection("F1N096","F1N196","F1N296","F1A030","F1A130","F1A230","F1A330").build()
    DBContainer CUGEX1 = CUGEX1query.getContainer()
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
    CUGEX1query.readAll(CUGEX1,10,{DBContainer recordCUGEX1 ->
      String oA330 = recordCUGEX1.get("F1A330")
      double oN096 = recordCUGEX1.get("F1N096") as double
      double oN196 = recordCUGEX1.get("F1N196") as double
      double oN296 = recordCUGEX1.get("F1N296") as double
      oTaxeSp = oN096
      oFraisFSA = oN196
      oRFA= oN296
      DBAction tauxRfaMPCOVE = database.table("MPCOVE").index("00").selection("IJOVHE").build()
      DBContainer MPCOVE = tauxRfaMPCOVE.getContainer()
      MPCOVE.set("IJCONO",currentCompany)
      MPCOVE.set("IJCEID", "RFAFRS")
      MPCOVE.set("IJOVK1", oA330)
      tauxRfaMPCOVE.readAll(MPCOVE,3,{DBContainer recordMPCOVE ->
        logger.debug("Recherche TaxeSpé")
        String oOVHE = recordMPCOVE.get("IJOVHE")
        oTauxRFA = oOVHE as double
        logger.debug("value oTauxRFA= {$oTauxRFA}")
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
    double oNumSAPR = 0
    ExpressionFactory expressionOPRBAS = database.getExpressionFactory("OPRBAS")
    expressionOPRBAS = expressionOPRBAS.eq("ODITNO", iITNO)
    DBAction queryOPRBAS = database.table("OPRBAS").index("00").matching(expressionOPRBAS).selection("ODSAPR","ODFVDT","ODLVDT").reverse().build()
    DBContainer OPRBAS = queryOPRBAS.getContainer()
    OPRBAS.set("ODCONO", currentCompany)
    OPRBAS.set("ODPRRF", iPRRF)
    OPRBAS.set("ODCUCD", iCUCD)
    OPRBAS.set("ODCUNO", "")
    queryOPRBAS.readAll(OPRBAS, 4, { DBContainer recordOPRBAS ->
      logger.debug("Recherche T0")
      String oSAPR = recordOPRBAS.get("ODSAPR")
      Integer fvdtOPRBAS = recordOPRBAS.get("ODFVDT") as Integer
      Integer lvdtOPRBAS = recordOPRBAS.get("ODLVDT") as Integer
      if (oNumSAPR == 0 && fvdtOPRBAS < (iFVDT as Integer) && lvdtOPRBAS <= (iFVDT as Integer)) {
        oNumSAPR = oSAPR as double
        return;
      }
    })
    return oNumSAPR
  }
  /**
   * Research margin in table Extend EXT040
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
    if(!query.readAll(EXT040, 3, outDataEXT040)){
      IN60=true
      updateCUGEX1("97", count, "L'enregistrement Marge Plancher niveau société n'existe pas")
      logger.debug("L'enregistrement Marge niveau société n'existe pas")
      logger.debug("L'enregistrement Marge niveau société n'existe pas")
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
    if(!query.readAll(EXT040, 3, outDataEXT040)){
      IN60=true
      logger.debug("L'enregistrement Marge niveau Client n'existe pas")
      updateCUGEX1("97", count, "L'enregistrement Marge Plancher n'existe pas pour le client "+iCUNO)
      logger.debug("L'enregistrement Marge niveau Client n'existe pas")
      return
    }
    iMCUN=iMarge

    // Recherche Client / assortiment
    iMarge = 0
    query = database.table("EXT040").index("10").selection("EXMARG","EXFDAT","EXTDAT").build()
    EXT040 = query.getContainer()
    EXT040.set("EXCONO", currentCompany)
    //EXT040.set("EXCUNO", iCUNO)
    EXT040.set("EXASCD", iASCD)
    if(!query.readAll(EXT040, 2, outDataEXT040)){
      IN60=true
      logger.debug("L'enregistrement Marge Objet n'existe pas"+iASCD)
      updateCUGEX1("97", count, "L'enregistrement Marge Plancher n'existe pas pour l'assortiment "+iASCD)
      logger.debug("L'enregistrement Marge Objet n'existe pas")
      return
    }
    iMOBJ=iMarge
  }
  /**
   * Research Flag in table Extend 80/20 EXT070
   */
  private Integer rechercheFlag8020(){
    iFLAG=0
    DBAction query = database.table("EXT070").index("00").selection("EXCONO", "EXCUNO", "EXITNO", "EXSAAM", "EXSAAC", "EXTAUX","EXFLAG", "EXSAAT").build()
    DBContainer EXT070 = query.getContainer()
    EXT070.set("EXCONO", currentCompany)
    EXT070.set("EXCUNO", iCUNO)
    EXT070.set("EXITNO", iITNO)
    if(!query.readAll(EXT070, 3, outDataEXT070)){

    }
    return iFLAG
  }
  /**
   * Research in table Extend TOC EXT042
   */
  private void rechercheToc(){
    DBAction query = database.table("EXT042").index("10").selection("EXCONO","EXCLEF","EXCUNO","EXHIE1","EXHIE2","EXHIE3","EXHIE4","EXHIE5",
      "EXCFI5","EXPOPN","EXBUAR","EXCFI1","EXTX15","EXADJT","EXFVDT","EXLVDT").build()
    DBContainer EXT042 = query.createContainer()
    EXT042.set("EXCONO", currentCompany)
    EXT042.set("EXCUNO", iCUNO)
    if(!query.readAll(EXT042, 2, outDataEXT042)) {

    }
  }
  /**
   * Update EXT075 before process 2
   *
   */
  private void majEXT075(){
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction tarifCompMajQuery = database.table("EXT075").index("00").selection("EXCHNO").build()
    DBContainer updEXT0752 = tarifCompMajQuery.getContainer()
    updEXT0752.set("EXCONO",currentCompany)
    updEXT0752.set("EXPRRF",iPRRF)
    updEXT0752.set("EXCUCD",iCUCD)
    updEXT0752.set("EXCUNO",iCUNO)
    updEXT0752.set("EXFVDT",iFVDT as Integer)
    updEXT0752.set("EXITNO",iITNO)
    updEXT0752.set("EXOBV1",iOBV1)
    updEXT0752.set("EXOBV2",iOBV2)
    updEXT0752.set("EXOBV3",iOBV3)
    updEXT0752.set("EXVFDT",iVFDT as Integer)
    if(!tarifCompMajQuery.readLock(updEXT0752, updateCallBackEXT075)){
    }
  }
  /**
   * Update EXT076 before Process 1
   *
   */
  private void majEXT076(){
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction axesSimpQueryMajQuery = database.table("EXT076").index("00").selection("EXPOPN","EXITTY","EXHIE2","EXHIE3","EXCHNO","EXMMO2","EXCPTL").build()
    DBContainer updEXT0762 = axesSimpQueryMajQuery.getContainer()
    updEXT0762.set("EXCONO",currentCompany)
    updEXT0762.set("EXPRRF",iPRRF)
    updEXT0762.set("EXCUCD",iCUCD)
    updEXT0762.set("EXCUNO",iCUNO)
    updEXT0762.set("EXFVDT",iFVDT as Integer)
    if(!axesSimpQueryMajQuery.readAll(updEXT0762, 5, outDataEXT076)){
    }
  }
  /**
   * Update EXT075 before Process 1
   *
   */
  private void majEXT075Traitement2(){
    DBAction tarifCompMajQuery = database.table("EXT075").index("00").selection("EXSAP0","EXSAP2","EXSAPR","EXFPSY","EXMCUN","EXPOPN","EXITTY","EXHIE2","EXHIE3","EXITNO","EXOBV1","EXOBV2","EXOBV3","EXVFDT","EXOBV2","EXTUT1","EXPUPR","EXNEPR",
      "EXMOY2","EXTUM1","EXTUM2","EXTUT2").build()
    DBContainer updEXT0752 = tarifCompMajQuery.getContainer()
    updEXT0752.set("EXCONO",currentCompany)
    updEXT0752.set("EXPRRF",iPRRF)
    updEXT0752.set("EXCUCD",iCUCD)
    updEXT0752.set("EXCUNO",iCUNO)
    updEXT0752.set("EXFVDT",iFVDT as Integer)
    if(!tarifCompMajQuery.readAll(updEXT0752, 5, outDataEXT075T2)){
    }
    String oCONO = currentCompany
    String oPRLP = "0"
    Map<String, String> paramOIS017MIChgPriceList = ["CONO": oCONO, "PRRF": iPRRF, "CUCD": iCUCD, "CUNO": iCunoBlank, "FVDT": iFVDT, "PRLP": oPRLP]
    Closure<?> closure = {Map<String, String> response ->
      if(response.error != null){
        updateCUGEX1("97", count, "Echec mise à jour tarif"+iPRRF+"-"+response.errorMessage)
        logger.debug("OIS017MI"+response.errorMessage)
        return logger.debug(response.errorMessage)
      }
    }
    miCaller.call("OIS017MI", "ChgPriceList", paramOIS017MIChgPriceList, closure)
  }
  /**
   * Process 1 before data in table Extend EXT076
   *
   */
  public void traitement1(){
    logger.debug("traitement1")
    logger.debug("value iFPSY = {$iFPSY}")
    logger.debug("value iSAP0 = {$iSAP0}")
    logger.debug("value iMCUN = {$iMCUN}")
    logger.debug("value iMDIV = {$iMDIV}")
    logger.debug("value iMOBJ = {$iMOBJ}")
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
                  logger.debug("value CDUV iSAP1 = {$iSAP1}")
                  logger.debug("value CDUV iSAP0 = {$iSAP0}")
                  iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/150)
                  logger.debug("value CDUV iSAP2 = {$iSAP2}")
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
   * Process 2 with data in table Extend EXT076
   *
   */
  public void traitement2(){
    logger.debug("traitement2")
    logger.debug("value iITNO = {$iITNO}")
    logger.debug("value iSAP0 = {$iSAP0}")
    logger.debug("value iMOY2 = {$iMOY2}")
    logger.debug("value iMMO2 = {$iMMO2}")
    logger.debug("value iTUM2 = {$iTUM2}")
    logger.debug("value iTUM1 = {$iTUM1}")
    logger.debug("value iTUT2 = {$iTUT2}")
    logger.debug("value iTUT1 = {$iTUT1}")
    logger.debug("value iMCUN = {$iMCUN}")
    logger.debug("value iSAP2 = {$iSAP2}")
    if(iMOY2<=(iMMO2+iTUM2) && iMOY2>=(iMMO2+iTUM1)){
      iMOY3=iMMO2
      logger.debug("value iMOY3 = {$iMOY3}")
      iSAP3= (iNEPR/(1-iMOY3))
      logger.debug("value iSAP3 = {$iSAP3}")
    }else{
      logger.debug("value A2")
      iMOY3=iMOY2
      iSAP3=iSAP2
    }
    if(iSAP0!=0){
      if((iSAP3/iSAP0)-1<=iTUT2 && (iSAP3/iSAP0)-1>=iTUT1){
        logger.debug("value B1")
        iSAP4=iSAP0
        iMOY4=1-(iNEPR/iSAP4)
      }else{
        logger.debug("value B2")
        iMOY4=iMOY3
        iSAP4=iSAP3
      }
    }else{
      logger.debug("value B3")
      iMOY4=iMOY3
      iSAP4=iSAP3
    }
    if(iMOY4<iMCUN){
      logger.debug("value C1")
      iMFIN=iMCUN
      iSAPR=iNEPR/(1-iMFIN)
    }else{//aj algo
      logger.debug("value C2")
      iMFIN=iMOY4//aj algo
      iSAPR=iSAP4//aj algo
    }
  }
  // updateCUGEX1 : Maj status CUGEX1
  public void updateCUGEX1(String istatus, String icount, String imotif){
    if(!ano){
      compteurCugex1++
      iCunoBlank=""
      if(compteurCugex1<=2){
        DBAction CUGEX1query = database.table("CUGEX1").index("00").build()
        DBContainer CUGEX1 = CUGEX1query.getContainer()
        CUGEX1.set("F1CONO", currentCompany)
        CUGEX1.set("F1FILE", "OPRICH")
        CUGEX1.set("F1PK01", iPRRF)
        CUGEX1.set("F1PK02", iCUCD)
        CUGEX1.set("F1PK03", iCunoBlank)
        CUGEX1.set("F1PK04", iFVDT)
        if (!CUGEX1query.read(CUGEX1)) {
          executeCUSEXTMIAddFieldValue("OPRICH", iPRRF, iCUCD, iCunoBlank, iFVDT, "", "", "", "", istatus, icount, imotif)
        } else {
          count = icount
          status = istatus
          motif = imotif
          DBAction tarifCompCUGEX1 = database.table("CUGEX1").index("00").selection("F1N196","F1CHNO").build()
          DBContainer CUGEX1B = tarifCompCUGEX1.getContainer()
          CUGEX1B.set("F1CONO", currentCompany)
          CUGEX1B.set("F1FILE", "OPRICH")
          CUGEX1B.set("F1PK01", iPRRF)
          CUGEX1B.set("F1PK02", iCUCD)
          CUGEX1B.set("F1PK03", iCunoBlank)
          CUGEX1B.set("F1PK04", iFVDT)
          if (!tarifCompCUGEX1.readLock(CUGEX1B,updateCallBackCUGEX1)) {

          }
        }
      }
    }

  }
  /**
   * Add CUGEX1 to set status (N196)
   */
  private executeCUSEXTMIAddFieldValue(String FILE, String PK01, String PK02, String PK03, String PK04, String PK05, String PK06, String PK07, String PK08, String A030, String N196, String A121){
    Map<String, String> params = ["FILE": FILE, "PK01": PK01, "PK02": PK02, "PK03": PK03, "PK04": PK04, "PK05": PK05, "PK06": PK06, "PK07": PK07, "PK08": PK08, "A030": A030, "N196": N196, "A121": A121]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return logger.debug("Failed CUSEXTMI.AddFieldValue: "+ response.errorMessage)
      } else {
      }
    }
    miCaller.call("CUSEXTMI", "AddFieldValue", params, handler)
  }
  /**
   * Update CUGEX1 to set status (N196)
   */
  private executeCUSEXTMIChgFieldValue(String FILE, String PK01, String PK02, String PK03, String PK04, String PK05, String PK06, String PK07, String PK08, String A030, String N196, String A121){
    Map<String, String> params = ["FILE": FILE, "PK01": PK01, "PK02": PK02, "PK03": PK03, "PK04": PK04, "PK05": PK05, "PK06": PK06, "PK07": PK07, "PK08": PK08, "A030": A030, "N196": N196, "A121": A121]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return logger.debug("Failed CUSEXTMI.ChgFieldValue: "+ response.errorMessage)
      } else {
      }
    }
    miCaller.call("CUSEXTMI", "ChgFieldValue", params, handler)
  }
}
