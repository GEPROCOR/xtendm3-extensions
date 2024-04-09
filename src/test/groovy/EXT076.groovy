/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT075
 * Description : The CalcRateSalesDi transaction launch calcul to the EXT075 table on differences since last launch (EXT075MI.CalcRateSalesDi conversion).
 * Date         Changed By   Description
 * 20210510     CDUV         TARX16 - Calcul du tarif
 * 20220519     CDUV         lowerCamelCase has been fixed
 * 20221208     YYOU         updateCUGEX1() - Ajout motif erreur
 * 20230201     YYOU         updateCUGEX1() - Reset motif erreur
 * 20230214     YYOU         recherchePrixBrut() - Suppression ctrl date validité contrat
 * 20230726     YYOU         delete of a starting date control
 * 20240124     SLAISNE      fix SAP0 initialization in function rechercheT0, ticket 0071775
 * 20240220     ARENARD      lowerCamelCase has been fixed, Map has been changed, EXLMDT handling has been fixed
 * 20240226     ARENARD      lowerCamelCase has been fixed, unused/commented out code removed
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class EXT076 extends ExtendM3Batch {
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
  private double szSAPR = 0d
  private double szMMO2 = 0d
  private Integer szCPTL = 0
  private String iCunoBlank
  private String motif
  public EXT076(LoggerAPI logger, UtilityAPI utility,DatabaseAPI database, ProgramAPI program, BatchAPI batch, MICallerAPI miCaller, TextFilesAPI textFiles) {
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
    if(batch.getReferenceId().isPresent()){
      Optional<String> data = getJobData(batch.getReferenceId().get())
      performActualJob(data)
    } else {
      // No job data found
    }
  }
  // Get job data
  private Optional<String> getJobData(String referenceId){
    DBAction query = database.table("EXTJOB").index("00").selection("EXDATA").build()
    DBContainer container = query.createContainer()
    container.set("EXRFID", referenceId)
    if (query.read(container)){
      return Optional.of(container.getString("EXDATA"))
    } else {
    }
    return Optional.empty()
  }
  // Perform actual job
  private performActualJob(Optional<String> data){
    if(!data.isPresent()){
      return
    }
    rawData = data.get()
    String inPRRF = getFirstParameter()
    String inCUCD = getNextParameter()
    String inCUNO = getNextParameter()
    String inFVDT = getNextParameter()

    logger.debug("value inPRRF= {$inPRRF}")
    logger.debug("value inCUCD= {$inCUCD}")
    logger.debug("value inCUNO= {$inCUNO}")
    logger.debug("value inFVDT= {$inFVDT}")

    currentCompany = (Integer)program.getLDAZD().CONO

    // Perform Job
    IN60=false
    LocalDateTime timeOfCreation = LocalDateTime.now()
    dateJour=timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    iCunoBlank=""

    if(inPRRF == null || inPRRF == "" ){
      return
    }
    if(inCUCD == null ||  inCUCD == ""){
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
      if(!queryEXT080.readAll(EXT080, 4, outDataEXT080)){
        return
      }
      if(iCUNO==null || iCUNO==""){
        return
      }
    }
    if(inFVDT == null || inFVDT == ""){
      return
    }else {
      iFVDT = inFVDT
    }
    iPRRF = inPRRF
    iCUCD = inCUCD
    iWHLO=""
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
        return
      }
    }

    updateCUGEX1("95", count, "?")
    DBAction tarifCompQueryC = database.table("EXT077").index("00").build()
    DBContainer EXT077 = tarifCompQueryC.getContainer()
    EXT077.set("EXCONO",currentCompany)
    EXT077.set("EXPRRF",inPRRF)
    EXT077.set("EXCUCD",inCUCD)
    EXT077.set("EXCUNO",iCUNO)
    EXT077.set("EXFVDT",inFVDT as Integer)
    if(!tarifCompQueryC.readAllLock(EXT077, 5 ,deleteEXT077)){
    }

    iZIPL=""
    iPIDE=""
    DBAction incotermQuery = database.table("EXT080").index("00").selection("EXPIDE","EXWHLO","EXZIPL").build()
    DBContainer EXT080 = incotermQuery.getContainer()
    EXT080.set("EXCONO",currentCompany)
    EXT080.set("EXPRRF",iPRRF)
    EXT080.set("EXCUCD",iCUCD)
    EXT080.set("EXCUNO",iCUNO)
    EXT080.set("EXFVDT",iFVDT as Integer)
    if (!incotermQuery.readAll(EXT080, 5, outDataEXT0801)) {
      updateCUGEX1("97", count, "Erreur Injection Tarif MEA ou pré-requis")
      return
    }

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
    if (!tableSimpQuery.readAll(EXT041, 3, outDataMargEXT041)){
      EXT041.set("EXCONO",currentCompany)

      EXT041.set("EXTYPE","MARG")
      EXT041.set("EXCUNO"," ")
      if (!tableSimpQuery.readAll(EXT041, 3, outDataMargEXT041)){
      }
    }

    tableSimpQuery = database.table("EXT041").index("00").selection("EXBOBE","EXBOHE","EXBOBM","EXBOHM").build()
    EXT041 = tableSimpQuery.getContainer()
    EXT041.set("EXCONO",currentCompany)
    EXT041.set("EXTYPE","T0T3")
    EXT041.set("EXCUNO",iCUNO)
    if (!tableSimpQuery.readAll(EXT041, 3, outDataT0t3EXT041)){
      EXT041.set("EXCONO",currentCompany)

      EXT041.set("EXTYPE","T0T3")
      EXT041.set("EXCUNO"," ")
      if (!tableSimpQuery.readAll(EXT041, 3, outDataT0t3EXT041)){
      }
    }
    iASCD = ""
    ExpressionFactory expressionTableCompTarif = database.getExpressionFactory("EXT075")
    expressionTableCompTarif = expressionTableCompTarif.eq("EXSAPR", "0")
    DBAction tableCompTarif = database.table("EXT075").index("00").matching(expressionTableCompTarif).selection("EXCUNO","EXASCD","EXITNO","EXOBV1","EXOBV2","EXOBV3","EXVFDT").build()

    DBContainer EXT075 = tableCompTarif.getContainer()
    EXT075.set("EXCONO",currentCompany)
    EXT075.set("EXPRRF",iPRRF)
    EXT075.set("EXCUCD",iCUCD)
    EXT075.set("EXCUNO",iCUNO)
    EXT075.set("EXFVDT",iFVDT as Integer)
    if (!tableCompTarif.readAll(EXT075, 5, outDataEXT075)) {
      updateCUGEX1("97", count, "Erreur Pré-requis (vérifier compte P, Incoterm, Devise…)")
      return
    }
    if(IN60){
      return
    }
    majEXT076()

    majTraitement2EXT075()

    count = numCount
    updateCUGEX1("99", count, "?")

    updateEXT080("90")

    // Delete file EXTJOB
    deleteEXTJOB()
  }
  // Get first parameter
  private String getFirstParameter(){
    rawDataLength = rawData.length()
    beginIndex = 0
    endIndex = rawData.indexOf(";")
    // Get parameter
    String parameter = rawData.substring(beginIndex, endIndex)
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
  // Update EXTJOB
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
    message = LocalDateTime.now().toString() + ";" + message
    Closure<?> consumer = { PrintWriter printWriter ->
      printWriter.println(message)
    }
    textFiles.write(logFileName, "UTF-8", true, consumer)
  }
  // Get EXT080
  Closure<?> outDataEXT080 = { DBContainer EXT080 ->
    iCUNO = EXT080.get("EXCUNO")
  }
  // Get EXT051
  Closure<?> outDataEXT051 = { DBContainer EXT051 ->
    iPOPN = EXT051.get("EXDATA")
  }
  // Get EXT081
  Closure<?> outDataEXT081 = { DBContainer EXT081 ->
    String oFDAT = EXT081.get("EXFDAT")
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
  // Get EXT075
  Closure<?> outDataEXT075= { DBContainer EXT075 ->
    iASCD = EXT075.get("EXASCD")
    iITNO = EXT075.get("EXITNO")
    iOBV1 = EXT075.get("EXOBV1")
    iOBV2 = EXT075.get("EXOBV2")
    iOBV3 = EXT075.get("EXOBV3")
    iVFDT = EXT075.get("EXVFDT")
    iCUNO = EXT075.get("EXCUNO")

    szSAPR = 0d
    // OPRBAS prix=0.001
    DBAction oprbasQuery = database.table("OPRBAS").index("00").selection("ODSAPR").build()
    DBContainer OPRBAS = oprbasQuery.getContainer()
    OPRBAS.set("ODCONO",currentCompany)
    OPRBAS.set("ODPRRF",iPRRF)
    OPRBAS.set("ODCUCD",iCUCD)
    OPRBAS.set("ODCUNO",iCunoBlank)
    OPRBAS.set("ODFVDT",iFVDT as Integer)
    OPRBAS.set("ODITNO",iITNO)
    if (!oprbasQuery.readAll(OPRBAS, 6, outDataOPRBAS)) {
      updateCUGEX1("97", count, "L'article "+iITNO+" n'existe pas dans le tarif")
      return
    }

    if(szSAPR==0.001){

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
        return
      }
      // Recherche contrat
      iSUNO = ""
      iVAGN=""
      iPUPR = recherchePrixBrut()
      iNEPR =  rechercheNEPR(iPUPR)
      iSAP0 = rechercheT0()
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

      // Marge objectif et Marge Plancher EXT040
      iMOBJ = 0
      iMCUN = 0
      iMDIV = 0
      rechercheMarge()

      if(IN60){
        return
      }
      // Flag 80/20
      iFLAG=rechercheFlag8020()
      // TOC EXT042
      iAjustement=0
      rechercheToc()

      iMOBJ = iMOBJ +iAjustement

      iSAP1=0
      iSAP2=0
      traitement1()
      iMOY2=0
      if(iSAP2!=0){
        iMOY2=1-(iNEPR/iSAP2)
      }
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

          // ecriture EXT077
          DBAction axesSimpQueryC = database.table("EXT077").index("00").selection("EXCPTL","EXMMO2","EXCHNO").build()
          DBContainer EXT077 = axesSimpQueryC.getContainer()
          EXT077.set("EXCONO",currentCompany)
          EXT077.set("EXPRRF",iPRRF)
          EXT077.set("EXCUCD",iCUCD)
          EXT077.set("EXCUNO",iCUNO)
          EXT077.set("EXFVDT",iFVDT as Integer)
          EXT077.set("EXPOPN",iPOPN)
          EXT077.set("EXITTY",iITTY)
          EXT077.set("EXHIE2",iHIE2)
          EXT077.set("EXHIE3",iHIE3)
          if (!axesSimpQueryC.readLock(EXT077,updateCallBackEXT077)) {
            EXT077.set("EXMMO2", iMOY2)
            EXT077.set("EXCPTL", 1)
            EXT077.set("EXASCD", iASCD)
            EXT077.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            EXT077.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
            EXT077.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            EXT077.setInt("EXCHNO", 1)
            EXT077.set("EXCHID", program.getUser())
            axesSimpQueryC.insert(EXT077)
          }
        }
      }
    }
    iSAPR=0
  }
  // Update CUGEX1
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
  // Delete EXT077
  Closure<?> deleteEXT077 = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  // Update EXT077
  Closure<?> updateCallBackEXT077 = { LockedResult  lockedResult ->
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
  // Update EXT076
  Closure<?> updateCallBackEXT076 = { LockedResult  lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    int compteurLigne = lockedResult.get("EXCPTL")
    double luMMO2 = lockedResult.get("EXMMO2")
    // ecriture EXT077
    DBAction axesSimpQueryC = database.table("EXT077").index("00").selection("EXCPTL","EXMMO2","EXCHNO").build()
    DBContainer EXT077 = axesSimpQueryC.getContainer()
    EXT077.set("EXCONO",currentCompany)
    EXT077.set("EXPRRF",iPRRF)
    EXT077.set("EXCUCD",iCUCD)
    EXT077.set("EXCUNO",iCUNO)
    EXT077.set("EXFVDT",iFVDT as Integer)
    EXT077.set("EXPOPN",iPOPN)
    EXT077.set("EXITTY",iITTY)
    EXT077.set("EXHIE2",iHIE2)
    EXT077.set("EXHIE3",iHIE3)
    if (axesSimpQueryC.readLock(EXT077,updateCallBackEXT077)) {
      luMMO2=luMMO2+iMOY2
      lockedResult.set("EXMMO2", luMMO2)
      lockedResult.setInt("EXCPTL", compteurLigne + 1)
      lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      lockedResult.setInt("EXCHNO", changeNumber + 1)
      lockedResult.set("EXCHID", program.getUser())
      lockedResult.update()
    }
  }
  // Get EXT076
  Closure<?> outDataEXT076 = { DBContainer EXT076 ->
    String oPOPN = EXT076.get("EXPOPN")
    String oITTY = EXT076.get("EXITTY")
    String oHIE2 = EXT076.get("EXHIE2")
    String oHIE3 = EXT076.get("EXHIE3")
    DBAction axesSimpQueryMaj = database.table("EXT076").index("00").selection("EXCPTL","EXMMO2","EXCHNO").build()
    DBContainer EXT076Maj = axesSimpQueryMaj.getContainer()
    EXT076Maj.set("EXCONO",currentCompany)
    EXT076Maj.set("EXPRRF",iPRRF)
    EXT076Maj.set("EXCUCD",iCUCD)
    EXT076Maj.set("EXCUNO",iCUNO)
    EXT076Maj.set("EXFVDT",iFVDT as Integer)
    EXT076Maj.set("EXPOPN",oPOPN)
    EXT076Maj.set("EXITTY",oITTY)
    EXT076Maj.set("EXHIE2",oHIE2)
    EXT076Maj.set("EXHIE3",oHIE3)
    if (!axesSimpQueryMaj.readLock(EXT076Maj,updateCallBackEXT076Maj)) {
    }
  }
  // Get EXT077
  Closure<?> outDataEXT077 = { DBContainer EXT077 ->
    String oPOPN = EXT077.get("EXPOPN")
    String oITTY = EXT077.get("EXITTY")
    String oHIE2 = EXT077.get("EXHIE2")
    String oHIE3 = EXT077.get("EXHIE3")
    DBAction axesSimpQueryMaj = database.table("EXT077").index("00").selection("EXCPTL","EXMMO2","EXCHNO").build()
    DBContainer EXT077Maj = axesSimpQueryMaj.getContainer()
    EXT077Maj.set("EXCONO",currentCompany)
    EXT077Maj.set("EXPRRF",iPRRF)
    EXT077Maj.set("EXCUCD",iCUCD)
    EXT077Maj.set("EXCUNO",iCUNO)
    EXT077Maj.set("EXFVDT",iFVDT as Integer)
    EXT077Maj.set("EXPOPN",oPOPN)
    EXT077Maj.set("EXITTY",oITTY)
    EXT077Maj.set("EXHIE2",oHIE2)
    EXT077Maj.set("EXHIE3",oHIE3)
    if (!axesSimpQueryMaj.readLock(EXT077Maj,updateCallBackEXT077Maj)) {
    }
  }
  // Get EXT077
  Closure<?> outDataEXT077Maj76 = { DBContainer EXT077 ->
    String oPOPN = EXT077.get("EXPOPN")
    String oITTY = EXT077.get("EXITTY")
    String oHIE2 = EXT077.get("EXHIE2")
    String oHIE3 = EXT077.get("EXHIE3")
    szMMO2 = EXT077.get("EXMMO2") as double
    szCPTL = EXT077.get("EXCPTL") as Integer

    DBAction axesSimpQueryMaj = database.table("EXT076").index("00").selection("EXCHNO").build()
    DBContainer EXT076Maj = axesSimpQueryMaj.getContainer()
    EXT076Maj.set("EXCONO",currentCompany)
    EXT076Maj.set("EXPRRF",iPRRF)
    EXT076Maj.set("EXCUCD",iCUCD)
    EXT076Maj.set("EXCUNO",iCUNO)
    EXT076Maj.set("EXFVDT",iFVDT as Integer)
    EXT076Maj.set("EXPOPN",oPOPN)
    EXT076Maj.set("EXITTY",oITTY)
    EXT076Maj.set("EXHIE2",oHIE2)
    EXT076Maj.set("EXHIE3",oHIE3)
    if (!axesSimpQueryMaj.readLock(EXT076Maj,updateCallBackEXT076Maj2)) {
    }
  }
  // Update EXT077
  Closure<?> updateCallBackEXT077Maj= { LockedResult  lockedResult ->
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
  // Get OPRBAS
  Closure<?> outDataOPRBAS = { DBContainer EXT076 ->
    szSAPR = EXT076.get("ODSAPR") as double
  }
  // Update EXT076
  Closure<?> updateCallBackEXT076Maj= { LockedResult  lockedResult ->
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
  // Update EXT076
  Closure<?> updateCallBackEXT076Maj2= { LockedResult  lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.set("EXMMO2", szMMO2)
    lockedResult.set("EXCPTL", szCPTL)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // Get EXT075
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
    szSAPR = 0d
    // OPRBAS prix=0.001
    DBAction oprbasQuery = database.table("OPRBAS").index("00").selection("ODSAPR").build()
    DBContainer OPRBAS = oprbasQuery.getContainer()
    OPRBAS.set("ODCONO",currentCompany)
    OPRBAS.set("ODPRRF",iPRRF)
    OPRBAS.set("ODCUCD",iCUCD)
    OPRBAS.set("ODCUNO",iCunoBlank)
    OPRBAS.set("ODFVDT",iFVDT as Integer)
    OPRBAS.set("ODITNO",iITNO)
    if (!oprbasQuery.readAll(OPRBAS, 6, outDataOPRBAS)) {
      updateCUGEX1("97", count, "L'article "+iITNO+" n'existe pas dans le tarif "+ iPRRF + " (OIS017)")
      return
    }
    if(szSAPR==0.001){
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

        DBAction EXT075QueryMaj = database.table("EXT075").index("00").selection("EXCHNO").build()
        DBContainer EXT075Maj = EXT075QueryMaj.getContainer()
        EXT075Maj.set("EXCONO",currentCompany)
        EXT075Maj.set("EXPRRF",iPRRF)
        EXT075Maj.set("EXCUCD",iCUCD)
        EXT075Maj.set("EXCUNO",iCUNO)
        EXT075Maj.set("EXFVDT",iFVDT as Integer)
        EXT075Maj.set("EXITNO",iITNO)
        EXT075Maj.set("EXOBV1",iOBV1)
        EXT075Maj.set("EXOBV2",iOBV2)
        EXT075Maj.set("EXOBV3",iOBV3)
        EXT075Maj.set("EXVFDT",iVFDT as Integer)
        if (!EXT075QueryMaj.readLock(EXT075Maj,updateCallBackEXT075Maj)) {
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
        return
      }
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

      Map<String, String> paramOIS017MI = ["CONO": oCONO, "PRRF": iPRRF, "CUCD": iCUCD, "CUNO": iCunoBlank, "FVDT": iFVDT, "ITNO": iITNO, "OBV1": iOBV1, "OBV2": iOBV2, "OBV3": iOBV3, "SAPR": oPRIX]
      Closure<?> closure = {Map<String, String> response ->
        if(response.error != null){
          updateCUGEX1("97", count, "Echec mise à jour prix de l'article "+iITNO+":"+oPRIX+"-"+response.errorMessage)
          return
        }
      }
      miCaller.call("OIS017MI", "UpdBasePrice", paramOIS017MI, closure)
      numCount++
    }
  }
  // Get EXT076
  Closure<?> outDataEXT076T2 = { DBContainer EXT076 ->
    iMMO2 = EXT076.get("EXMMO2")  as double
    if(iMMO2>0.99)iMMO2=0.99
  }
  // Get EXT080
  Closure<?> outDataEXT0801= { DBContainer EXT080 ->
    iZIPL = EXT080.get("EXZIPL")
    iWHLO = EXT080.get("EXWHLO")
    iPIDE = EXT080.get("EXPIDE")
  }
  // Get EXT041
  Closure<?> outDataMargEXT041= { DBContainer EXT041 ->
    iTUM1 = EXT041.get("EXBOBM") as double
    iTUM2 = EXT041.get("EXBOHM") as double
    iTUM1 = iTUM1/100
    iTUM2 = iTUM2/100
  }
  // Get EXT041
  Closure<?> outDataT0t3EXT041= { DBContainer EXT041 ->
    iTUT1 = EXT041.get("EXBOBE") as double
    iTUT2 = EXT041.get("EXBOHE") as double
    iTUT1 = iTUT1/100
    iTUT2 = iTUT2/100
  }
  // Get EXT042
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
      }
    }
  }
  // Get OPRMTX
  Closure<?> outDataOPRMTX = { DBContainer OPRMTX ->
    iZIPL = OPRMTX.get("DXOBV2")
  }
  // Get OCUSMA
  Closure<?> outDataOCUSMA = { DBContainer OCUSMA ->
    iPLTB = OCUSMA.get("OKPLTB")
  }
  // Get OPRICH
  Closure<?> outDataOPRICH = { DBContainer OPRICH ->
  }
  // Get CUGEX1
  Closure<?> outDataCUGEX1 = { DBContainer CUGEX1 ->
    iFPSY = CUGEX1.get("F1CHB1") as Integer
  }
  // Get MITMAS
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
  // Get MITMAS
  Closure<?> outDataMITMAS2 = { DBContainer MITMAS ->
    iPDCC = MITMAS.get("MMPDCC")
  }
  // Get CSYTAB
  Closure<?> outDataCSYTAB = { DBContainer CSYTAB ->
    String oPDCC=CSYTAB.get("CTPARM")
    if(oPDCC.trim().length()>=8){
      iPDCC = oPDCC.trim().substring(7,8)
    }
  }
  // Get MITPOP
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
  // Get EXT040
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
  // Get EXT070
  Closure<?> outDataEXT070 = { DBContainer EXT070 ->
    iFLAG = EXT070.get("EXFLAG") as Integer
  }
  // Update EXT075
  Closure<?> updateCallBackEXT075 = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
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
    if(svAGNB != "") lockedResult.set("EXAGNB",svAGNB)
    if(svSUNO != "") lockedResult.set("EXSUNO",svSUNO)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // Update EXT075
  Closure<?> updateCallBackEXT075Maj = { LockedResult lockedResult ->
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
   * recherchePrixBrut
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
      if(oVAGN=="" ||oVAGN==null){
        oVAGN="00000"
      }
    })
    DBAction tableEXT030 = database.table("EXT030").index("10").selection("EXZIPP","EXPRIO").build()
    DBContainer EXT030 = tableEXT030.getContainer()
    EXT030.set("EXCONO", currentCompany)
    EXT030.set("EXZIPS", iZIPL)
    tableEXT030.readAll(EXT030,2,{DBContainer recordEXT030->
      String oZIPP = recordEXT030.get("EXZIPP")
      oPRIO = recordEXT030.get("EXPRIO")
      DBAction tableCompContrat = database.table("EXT032").index("30").selection("EXAGNB").build()
      DBContainer EXT032Liste = tableCompContrat.getContainer()
      EXT032Liste.set("EXCONO", currentCompany)
      EXT032Liste.set("EXZIPP", oZIPP)
      EXT032Liste.set("EXSUNO", svSUNO)
      tableCompContrat.readAll(EXT032Liste,3,{DBContainer recordEXT032->
        String oAGNB = recordEXT032.get("EXAGNB")
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
          String svVAGN =recordMPAGRH.get("AHVAGN")
          logger.debug("Get MPAGRH AGNB={$oAGNB} / iFVDT={$iIntFVDT} : PAST={$oPAST}, FVDT={$oFVDT}, UVDT={$oUVDT}")
          if(iIntFVDT>= oFVDT && iIntFVDT <=oUVDT && oPAST=="40"){
            if(recordMPAGRH.get("AHVAGN")==oVAGN ){
              ExpressionFactory expressionMPAGRL = database.getExpressionFactory("MPAGRL")
              expressionMPAGRL = expressionMPAGRL.eq("AIOBV1", iITNO)
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
                DBAction tableLigneCompContrat = database.table("MPAGRP").index("00").selection("AJPUPR").build()
                DBContainer MPAGRP = tableLigneCompContrat.getContainer()
                MPAGRP.set("AJCONO", currentCompany)
                MPAGRP.set("AJSUNO", svSUNO)
                MPAGRP.set("AJAGNB", oAGNB)
                MPAGRP.set("AJGRPI", "30" as Integer)
                MPAGRP.set("AJOBV1", iITNO)
                tableLigneCompContrat.readAll(MPAGRP,5,{DBContainer recordMPAGRP ->
                  String oPUPR = recordMPAGRP.get("AJPUPR")
                  logger.debug("Get MPAGRL AGNB={$oAGNB} ITNO={$iITNO} : AGPT={$oAGTP}({$svAGPT}), FVDT={$oMpagrlFVDT}({$saveFVDT}), UVDT={$oMpagrlUVDT}, PRIO={$svPRIO}({$oPRIO})")
                  if((svAGPT==0 || svAGPT >= oAGTP) && iIntFVDT>= oMpagrlFVDT && iIntFVDT <=oMpagrlUVDT){
                    if(svPRIO=="" || oPRIO==svPRIO){
                      logger.debug("Save MPAGRL AGNB={$oAGNB}")
                      svAGPT = oAGTP
                      oNumPUPR=oPUPR as double
                      iSUNO = svSUNO
                      svAGNB = oAGNB
                      szOBV1 = recordMPAGRL.get("AIOBV1")
                      szOBV2 = recordMPAGRL.get("AIOBV2")
                      szOBV3 = recordMPAGRL.get("AIOBV3")
                      szOBV4 = recordMPAGRL.get("AIOBV4")
                      szFVDT = oMpagrlFVDT
                      svPRIO=oPRIO
                      saveFVDT=oMpagrlFVDT
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
    DBAction CUGEX1Query = database.table("CUGEX1").index("00").selection("F1N096","F1N196","F1N296","F1A030","F1A130","F1A230","F1A330").build()
    DBContainer CUGEX1 = CUGEX1Query.getContainer()
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
    CUGEX1Query.readAll(CUGEX1,10,{DBContainer recordCUGEX1 ->
      String oA330 = recordCUGEX1.get("F1A330")
      double oN096 = recordCUGEX1.get("F1N096") as double
      double oN196 = recordCUGEX1.get("F1N196") as double
      double oN296 = recordCUGEX1.get("F1N296") as double
      oTaxeSp = oN096
      oFraisFSA = oN196
      oRFA = oN296
      DBAction tauxRfaMPCOVE = database.table("MPCOVE").index("00").selection("IJOVHE").build()
      DBContainer MPCOVE = tauxRfaMPCOVE.getContainer()
      MPCOVE.set("IJCONO",currentCompany)
      MPCOVE.set("IJCEID", "RFAFRS")
      MPCOVE.set("IJOVK1", oA330)
      tauxRfaMPCOVE.readAll(MPCOVE,3,{DBContainer recordMPCOVE ->
        String oOVHE = recordMPCOVE.get("IJOVHE")
        oTauxRFA = oOVHE as double
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
      updateCUGEX1("97", count, "L'enregistrement Marge Plancher n'existe pas pour le client "+iCUNO)
      return
    }
    iMCUN=iMarge

    // Recherche Client / assortiment
    iMarge = 0
    query = database.table("EXT040").index("10").selection("EXMARG","EXFDAT","EXTDAT").build()
    EXT040 = query.getContainer()
    EXT040.set("EXCONO", currentCompany)
    EXT040.set("EXASCD", iASCD)
    if(!query.readAll(EXT040, 2, outDataEXT040)){
      IN60=true
      updateCUGEX1("97", count, "L'enregistrement Marge Plancher n'existe pas pour l'assortiment "+iASCD)
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
    DBContainer maj2EXT075 = tarifCompMajQuery.getContainer()
    maj2EXT075.set("EXCONO",currentCompany)
    maj2EXT075.set("EXPRRF",iPRRF)
    maj2EXT075.set("EXCUCD",iCUCD)
    maj2EXT075.set("EXCUNO",iCUNO)
    maj2EXT075.set("EXFVDT",iFVDT as Integer)
    maj2EXT075.set("EXITNO",iITNO)
    maj2EXT075.set("EXOBV1",iOBV1)
    maj2EXT075.set("EXOBV2",iOBV2)
    maj2EXT075.set("EXOBV3",iOBV3)
    maj2EXT075.set("EXVFDT",iVFDT as Integer)
    if(!tarifCompMajQuery.readLock(maj2EXT075, updateCallBackEXT075)){
    }
  }
  /**
   * Update EXT076 before Process 1
   *
   */
  private void majEXT076(){
    // Maj EXT077
    DBAction axesSimpQueryMajQuery = database.table("EXT077").index("00").selection("EXPOPN","EXITTY","EXHIE2","EXHIE3","EXCHNO","EXMMO2","EXCPTL").build()
    DBContainer EXT077 = axesSimpQueryMajQuery.getContainer()
    EXT077.set("EXCONO",currentCompany)
    EXT077.set("EXPRRF",iPRRF)
    EXT077.set("EXCUCD",iCUCD)
    EXT077.set("EXCUNO",iCUNO)
    EXT077.set("EXFVDT",iFVDT as Integer)
    if(!axesSimpQueryMajQuery.readAll(EXT077, 5, outDataEXT077)){
    }
    // Maj EXT076
    DBAction axesSimpQueryMajQueryB = database.table("EXT077").index("00").selection("EXPOPN","EXITTY","EXHIE2","EXHIE3","EXCHNO","EXMMO2","EXCPTL").build()
    DBContainer EXT077B = axesSimpQueryMajQueryB.getContainer()
    EXT077B.set("EXCONO",currentCompany)
    EXT077B.set("EXPRRF",iPRRF)
    EXT077B.set("EXCUCD",iCUCD)
    EXT077B.set("EXCUNO",iCUNO)
    EXT077B.set("EXFVDT",iFVDT as Integer)
    if(!axesSimpQueryMajQueryB.readAll(EXT077B, 5, outDataEXT077Maj76)){
    }

  }
  /**
   * Update EXT075 before Process 1
   *
   */
  private void majTraitement2EXT075(){
    ExpressionFactory expressionTableCompTarif = database.getExpressionFactory("EXT075")
    expressionTableCompTarif = expressionTableCompTarif.eq("EXSAPR", "0")
    DBAction tarifCompMajQuery = database.table("EXT075").index("00").matching(expressionTableCompTarif).selection("EXSAP0","EXSAP2","EXSAPR","EXFPSY","EXMCUN","EXPOPN","EXITTY","EXHIE2","EXHIE3","EXITNO","EXOBV1","EXOBV2","EXOBV3","EXVFDT","EXOBV2","EXTUT1","EXPUPR","EXNEPR",
      "EXMOY2","EXTUM1","EXTUM2","EXTUT2").build()
    DBContainer maj2EXT075 = tarifCompMajQuery.getContainer()
    maj2EXT075.set("EXCONO",currentCompany)
    maj2EXT075.set("EXPRRF",iPRRF)
    maj2EXT075.set("EXCUCD",iCUCD)
    maj2EXT075.set("EXCUNO",iCUNO)
    maj2EXT075.set("EXFVDT",iFVDT as Integer)
    if(!tarifCompMajQuery.readAll(maj2EXT075, 5, outDataEXT075T2)){
    }
    String oCONO = currentCompany
    String oPRLP = "0"
    Map<String, String> paramOIS017MIChgPriceList = ["CONO": oCONO, "PRRF": iPRRF, "CUCD": iCUCD, "CUNO": iCunoBlank, "FVDT": iFVDT, "PRLP": oPRLP]
    Closure<?> closure = {Map<String, String> response ->
      if(response.error != null){
        updateCUGEX1("97", count, "Echec mise à jour tarif "+iPRRF+"-"+response.errorMessage)
        return
      }
    }
    miCaller.call("OIS017MI", "ChgPriceList", paramOIS017MIChgPriceList, closure)

  }
  /**
   * Process 1 before data in table Extend EXT076
   *
   */
  public void traitement1(){
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
                  iSAP2 = iSAP1 *( 1 -((iSAP1/iSAP0)-1)/150)
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
    if(iMOY2<=(iMMO2+iTUM2) && iMOY2>=(iMMO2+iTUM1)){
      iMOY3=iMMO2
      iSAP3= (iNEPR/(1-iMOY3))
    }else{
      iMOY3=iMOY2
      iSAP3=iSAP2
    }
    if(iSAP0!=0){
      if((iSAP3/iSAP0)-1<=iTUT2 && (iSAP3/iSAP0)-1>=iTUT1){
        iSAP4=iSAP0
        iMOY4=1-(iNEPR/iSAP4)
      }else{
        iMOY4=iMOY3
        iSAP4=iSAP3
      }
    }else{
      iMOY4=iMOY3
      iSAP4=iSAP3
    }
    if(iMOY4<iMCUN){
      iMFIN=iMCUN
      iSAPR=iNEPR/(1-iMFIN)
    }else{//aj algo
      iMFIN=iMOY4//aj algo
      iSAPR=iSAP4//aj algo
    }
  }
  // Update EXT080
  public void updateEXT080(String iStatus){
    status = iStatus
    DBAction query = database.table("EXT080").index("00").build()
    DBContainer EXT080 = query.getContainer()
    EXT080.set("EXCONO", currentCompany)
    EXT080.set("EXPRRF", iPRRF)
    EXT080.set("EXCUCD", iCUCD)
    EXT080.set("EXCUNO", iCUNO)
    EXT080.set("EXFVDT", iFVDT as Integer)
    if(!query.readLock(EXT080, updateCallBackEXT080)){
      return
    }
  }
  // Update EXT080
  Closure<?> updateCallBackEXT080 = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.set("EXSTAT", status)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // Update CUGEX1
  public void updateCUGEX1(String iStatus, String iCount, String iMotif){
    if(!ano){
      compteurCugex1++
      iCunoBlank=""
      if(compteurCugex1<=2){
        DBAction CUGEX1Query = database.table("CUGEX1").index("00").build()
        DBContainer CUGEX1 = CUGEX1Query.getContainer()
        CUGEX1.set("F1CONO", currentCompany)
        CUGEX1.set("F1FILE", "OPRICH")
        CUGEX1.set("F1PK01", iPRRF)
        CUGEX1.set("F1PK02", iCUCD)
        CUGEX1.set("F1PK03", iCunoBlank)
        CUGEX1.set("F1PK04", iFVDT)
        if (!CUGEX1Query.read(CUGEX1)) {
          executeCUSEXTMIAddFieldValue("OPRICH", iPRRF, iCUCD, iCunoBlank, iFVDT, "", "", "", "", iStatus, iCount, iMotif)
        } else {
          count = iCount
          status = iStatus
          motif = iMotif
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
        return
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
        return
      } else {
      }
    }
    miCaller.call("CUSEXTMI", "ChgFieldValue", params, handler)
  }
}
