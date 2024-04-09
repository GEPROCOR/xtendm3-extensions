/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT078
 * Description : Sales price calculation
 * Date         Changed By   Description
 * 20231011     ARENARD      TARX16 - Calcul du tarif V2
 * 20240226		ARENARD		 lowerCamelCase has been fixed, unused/commented out code removed, def was replaced
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class EXT078 extends ExtendM3Batch {
  private final LoggerAPI logger
  private final DatabaseAPI database
  private final ProgramAPI program
  private final BatchAPI batch
  private final MICallerAPI miCaller
  private final TextFilesAPI textFiles
  private final UtilityAPI utility

  private Integer currentCompany
  private String rawData
  private int rawDataLength
  private int beginIndex
  private int endIndex
  private String logFileName
  private boolean IN60
  private String jobNumber
  private String currentDivision
  private Integer currentDate
  private String iCUNO
  private String iPRRF
  private String iFVDT
  private String iCUCD
  private String iOBV1
  private String iOBV2
  private String iOBV3
  private String iVFDT
  private String iZIPL
  private Integer iFLAG
  private double iPUPR
  private double iNEPR
  private double iCOST
  private double cost
  private double cofa
  private double iMDIV
  private double iMarge
  private double iSAPR
  private String iPOPN
  private String iITTY
  private String iBUAR
  private String iASCD
  private String iITNO
  private String iSUNO
  private String iHIE2
  private String iHIE3
  private String iWHLO
  private String iPIDE
  private String iPDCC
  private String svAGNB
  private String svSUNO
  private String svDIVI
  private String szOBV1
  private String szOBV2
  private String szOBV3
  private String szOBV4
  private String szFVDT
  private Integer numCount = 0
  private String count = "0"
  private Integer counterCUGEX1 = 0
  private boolean ano = false
  private String status
  private String motif
  private String iCunoBlank
  private Integer maxRecord
  private Integer miCounter
  private double saprOPRBAS

  public EXT078(LoggerAPI logger, UtilityAPI utility,DatabaseAPI database, ProgramAPI program, BatchAPI batch, MICallerAPI miCaller, TextFilesAPI textFiles) {
    this.logger = logger
    this.utility = utility
    this.database = database
    this.program = program
    this.batch = batch
    this.miCaller = miCaller
    this.textFiles = textFiles
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

    logger.debug("value inPRRF= {$inPRRF}")
    logger.debug("value inCUCD= {$inCUCD}")
    logger.debug("value inCUNO= {$inCUNO}")
    logger.debug("value inFVDT= {$inFVDT}")

    // Perform Job
    currentCompany = (Integer)program.getLDAZD().CONO
    LocalDateTime timeOfCreation = LocalDateTime.now()
    currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    IN60 = false

    // Check price list
    if(inPRRF == null || inPRRF == "" ){
      logger.debug("Code Tarif est obligatoire")
      return
    }

    // Check currency
    if(inCUCD == null ||  inCUCD == ""){
      logger.debug("Devise est obligatoire")
      return
    }

    // Check from date
    if(inFVDT == null || inFVDT == ""){
      logger.debug("Date Début Validité est obligatoire")
      return
    } else {
      iFVDT = inFVDT
      if (!utility.call("DateUtil", "isDateValid", iFVDT, "yyyyMMdd")) {
        logger.debug("Format Date de Validité incorrect")
        return
      }
    }

    // Check/retrieve customer
    iCunoBlank=""
    iCUNO = inCUNO
    if(iCUNO == null ||  iCUNO == ""){
      DBAction queryEXT080 = database.table("EXT080").index("20").selection("EXCUNO").build()
      DBContainer EXT080 = queryEXT080.getContainer()
      EXT080.set("EXCONO", currentCompany)
      EXT080.set("EXPRRF", inPRRF)
      EXT080.set("EXCUCD", inCUCD)
      EXT080.set("EXFVDT", inFVDT as Integer)
      if(!queryEXT080.readAll(EXT080, 4, outDataEXT080)){
        logger.debug("L'enregistrement EXT080 n'existe pas donc pas de client")
        return
      }
      if(iCUNO==null || iCUNO==""){
        logger.debug("Code client est obligatoire")
        return
      }
    }

    // Price list must exist
    iPRRF = inPRRF
    iCUCD = inCUCD
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

    // Retrieve EXT080
    iZIPL=""
    iWHLO = ""
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
      logger.debug("Paramétrage EXT080 n'existe pas")
      return
    }

    // Read/update EXT075
    DBAction tableCompTarif = database.table("EXT075").index("00").selection("EXASCD","EXITNO","EXOBV1","EXOBV2","EXOBV3","EXVFDT","EXCUNO","EXFLAG").build()
    DBContainer EXT075 = tableCompTarif.getContainer()
    EXT075.set("EXCONO",currentCompany)
    EXT075.set("EXPRRF",iPRRF)
    EXT075.set("EXCUCD",iCUCD)
    EXT075.set("EXCUNO",iCUNO)
    EXT075.set("EXFVDT",iFVDT as Integer)
    if (!tableCompTarif.readAll(EXT075, 5, outDataEXT075)) {
      updateCUGEX1("97", count, "Erreur Pré-requis (vérifier compte P, Incoterm, Devise…)")
      logger.debug("Aucune ligne Tarif déja créée")
      return
    }

    if(IN60){
      return
    }
    logger.debug("Ligne Tarif comp OK")

    // Update price list
    updatePriceList()

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
  // Retrieve EXT080
  Closure<?> outDataEXT080 = { DBContainer EXT080 ->
    iCUNO = EXT080.get("EXCUNO")
  }
  // Retrieve EXT051
  Closure<?> outDataEXT051 = { DBContainer EXT051 ->
    iPOPN = EXT051.get("EXDATA")
    logger.debug("value iPOPN EXT051 = {$iPOPN}")
  }
  // Retrieve EXT081
  Closure<?> outDataEXT081 = { DBContainer EXT081 ->
    logger.debug("EXT081 POPN OK")
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
  // Retrieve CUGEX1
  Closure<?> outDataCUGEX1 = { DBContainer CUGEX1 ->
    cost = CUGEX1.get("F1N096")
    logger.debug("CUGEX1 ITTY trouvé - cost =" + cost)
  }
  // Read EXT075 and Process1
  Closure<?> outDataEXT075= { DBContainer EXT075 ->
    iASCD = EXT075.get("EXASCD")
    iITNO = EXT075.get("EXITNO")
    iOBV1 = EXT075.get("EXOBV1")
    iOBV2 = EXT075.get("EXOBV2")
    iOBV3 = EXT075.get("EXOBV3")
    iVFDT = EXT075.get("EXVFDT")
    iCUNO = EXT075.get("EXCUNO")
    iFLAG = EXT075.get("EXFLAG")

    logger.debug("iITNO = " + iITNO)

    saprOPRBAS = 0d
    // OPRBAS prix=0.001
    DBAction queryOPRBAS = database.table("OPRBAS").index("00").selection("ODSAPR").build()
    DBContainer OPRBAS = queryOPRBAS.getContainer()
    OPRBAS.set("ODCONO",currentCompany)
    OPRBAS.set("ODPRRF",iPRRF)
    OPRBAS.set("ODCUCD",iCUCD)
    OPRBAS.set("ODCUNO",iCunoBlank)
    OPRBAS.set("ODFVDT",iFVDT as Integer)
    OPRBAS.set("ODITNO",iITNO)
    if (!queryOPRBAS.readAll(OPRBAS, 6, outDataOPRBAS)) {
      updateCUGEX1("97", count, "L'article "+iITNO+" n'existe pas dans le tarif")
      return
    }
    logger.debug("saprOPRBAS = " + saprOPRBAS)
    if(saprOPRBAS == 0.001){

      // Retrieve item
      retrieveItem()

      // Retrieve alias number (Enseigne)
      retrieveAliasNumber()
      logger.debug("value iPOPN= {$iPOPN}")

      // Retrieve purchase price
      iPUPR = retrievePurchasePrice()
      logger.debug("value iPUPR= {$iPUPR}")

      // Retrieve net price
      iNEPR = retrieveNetPrice(iPUPR)
      logger.debug("value iNEPR= {$iNEPR}")

      // Retrieve company margin (Marge société)
      iMDIV = retrieveCompanyMargin()
      logger.debug("value iMDIV= {$iMDIV}")

      // Retrieve logistics cost
      iCOST = retrieveLogisticsCost()
      logger.debug("value iCOST= {$iCOST}")

      // if error, program exit
      if (IN60) {
        return
      }

      // Calculate sales price
      iSAPR = retrieveSalesPrice()
      logger.debug("value iSAPR= {$iSAPR}")

      // Update EXT075
      updateEXT075()
    }
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
  // Retrieve EXT075 and update price list                                                                                                                                                                   075T2 :: Retrieve EXT075 with
  Closure<?> outDataEXT075T2 = { DBContainer EXT075 ->
    iPOPN = EXT075.get("EXPOPN")
    iITTY = EXT075.get("EXITTY")
    iHIE2 = EXT075.get("EXHIE2")
    iHIE3 = EXT075.get("EXHIE3")
    iITNO = EXT075.get("EXITNO")
    iNEPR = EXT075.get("EXNEPR")
    iSAPR = EXT075.get("EXSAPR")
    iPUPR = EXT075.get("EXPUPR")
    iPDCC = ""

    saprOPRBAS = 0d
    // OPRBAS prix=0.001
    DBAction queryOPRBAS = database.table("OPRBAS").index("00").selection("ODSAPR").build()
    DBContainer OPRBAS = queryOPRBAS.getContainer()
    OPRBAS.set("ODCONO",currentCompany)
    OPRBAS.set("ODPRRF",iPRRF)
    OPRBAS.set("ODCUCD",iCUCD)
    OPRBAS.set("ODCUNO",iCunoBlank)
    OPRBAS.set("ODFVDT",iFVDT as Integer)
    OPRBAS.set("ODITNO",iITNO)
    if (!queryOPRBAS.readAll(OPRBAS, 6, outDataOPRBAS)) {
      updateCUGEX1("97", count, "L'article "+iITNO+" n'existe pas dans le tarif "+ iPRRF + " (OIS017)")
      return
    }
    if(saprOPRBAS == 0.001){

      String iDIVI = ""
      DBAction deviseQuery = database.table("CSYTAB").index("00").selection("CTPARM").build()
      DBContainer CSYTAB = deviseQuery.getContainer()
      CSYTAB.set("CTCONO", currentCompany)
      CSYTAB.set("CTDIVI", iDIVI)
      CSYTAB.set("CTSTCO", "CUCD")
      CSYTAB.set("CTSTKY", iCUCD)
      if (!deviseQuery.readAll(CSYTAB, 4, outDataCSYTAB)) {
        updateCUGEX1("97", count, "La devise " + iCUCD + " n'existe pas (Sélection de la devise dans la liste du Mashup)")
        logger.debug("Devise n'existe pas")
        return
      }
      logger.debug("value iPDCC= {$iPDCC}")
      logger.debug("value iSAPR= {$iSAPR}")
      logger.debug("value iPUPR= {$iPUPR}")
      if (iPUPR <= 0.001) iSAPR = 0
      String oCONO = currentCompany
      String oPRIX = ""
      if (iPDCC == "0") oPRIX = (double) Math.round(iSAPR)
      if (iPDCC == "1") oPRIX = (double) Math.round(iSAPR * 10) / 10
      if (iPDCC == "2") oPRIX = (double) Math.round(iSAPR * 100) / 100
      if (iPDCC == "3") oPRIX = (double) Math.round(iSAPR * 1000) / 1000
      if (iPDCC == "4") oPRIX = (double) Math.round(iSAPR * 10000) / 10000
      if (iPDCC == "5") oPRIX = (double) Math.round(iSAPR * 100000) / 100000
      if (iPDCC == "6") oPRIX = (double) Math.round(iSAPR * 1000000) / 1000000

      logger.debug("value oPRIX = {$oPRIX}")
      Map<String, String> paramOIS017MI = ["CONO": oCONO, "PRRF": iPRRF, "CUCD": iCUCD, "CUNO": iCunoBlank, "FVDT": iFVDT, "ITNO": iITNO, "OBV1": iOBV1, "OBV2": iOBV2, "OBV3": iOBV3, "SAPR": oPRIX]
      Closure<?> closure = { Map<String, String> response ->
        if (response.error != null) {
          logger.debug("OIS017MI" + response.errorMessage)
          updateCUGEX1("97", count, "Echec mise à jour prix de l'article " + iITNO + ":" + oPRIX + "-" + response.errorMessage)
          return
        }
      }
      miCaller.call("OIS017MI", "UpdBasePrice", paramOIS017MI, closure)
      numCount++
    }
  }
  //Retrieve EXT080
  Closure<?> outDataEXT0801= { DBContainer EXT080 ->
    iZIPL = EXT080.get("EXZIPL")
    iWHLO = EXT080.get("EXWHLO")
    iPIDE = EXT080.get("EXPIDE")
  }
  //Retrieve OPRICH
  Closure<?> outDataOPRICH = { DBContainer OPRICH ->
  }
  // Retrieve OPRBAS
  Closure<?> outDataOPRBAS = { DBContainer OPRBAS  ->
    saprOPRBAS = OPRBAS .get("ODSAPR") as double
  }
  //Retrieve MITMAS for Research in Process1
  Closure<?> outDataMITMAS = { DBContainer MITMAS ->
    logger.debug("MITMAS OK")
    iHIE2 = MITMAS.get("MMHIE2")
    iHIE3 = MITMAS.get("MMHIE3")
    iITTY = MITMAS.get("MMITTY")
    iBUAR = MITMAS.get("MMBUAR")
  }
  //Retrieve CSYTAB
  Closure<?> outDataCSYTAB = { DBContainer CSYTAB ->
    String oPDCC=CSYTAB.get("CTPARM")
    logger.debug("value oPDCC= {$oPDCC}")
    if(oPDCC.trim().length()>=8){
      iPDCC = oPDCC.trim().substring(7,8)
    }
    logger.debug("value iPDCC= {$iPDCC}")
  }
  //Retrieve MITPOP
  Closure<?> outDataMITPOP = { DBContainer MITPOP ->
    iPOPN = MITPOP.get("MPPOPN")
    logger.debug("value iPOPN MITPOP = {$iPOPN}")
  }
  //Retrieve EXT043
  Closure<?> outDataEXT043= { DBContainer EXT043 ->
    iMarge = EXT043.get("EXMARG") as double
  }
  //Update EXT075
  Closure<?> updateCallBackEXT075 = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    if(iITTY != "") lockedResult.set("EXITTY",iITTY)
    if(iHIE3 != "") lockedResult.set("EXHIE3",iHIE3)
    if(iHIE2 != "") lockedResult.set("EXHIE2",iHIE2)
    if(iPOPN != "") lockedResult.set("EXPOPN",iPOPN)
    lockedResult.set("EXSAPR",iSAPR)
    if(iMDIV != "") lockedResult.set("EXMDIV",iMDIV)
    if(iCOST != "") lockedResult.set("EXZLOC",iCOST)
    if(iNEPR != "") lockedResult.set("EXNEPR",iNEPR)
    if(iPUPR != "") lockedResult.set("EXPUPR",iPUPR)
    if(iZIPL != "") lockedResult.set("EXZIPL",iZIPL)
    if(svAGNB != "") lockedResult.set("EXAGNB",svAGNB)
    if(svSUNO != "") lockedResult.set("EXSUNO",svSUNO)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }

  /**
   * Retrieve item
   */
  private void retrieveItem(){
    logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXretrieveItemXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    logger.debug("value iITNO= {$iITNO}")
    iHIE2=""
    iHIE3=""
    iITTY=""
    iBUAR=""
    DBAction articleQuery = database.table("MITMAS").index("00").selection("MMITTY","MMHIE2","MMHIE3","MMBUAR").build()
    DBContainer MITMAS = articleQuery.getContainer()
    MITMAS.set("MMCONO",currentCompany)
    MITMAS.set("MMITNO",iITNO)
    if (!articleQuery.readAll(MITMAS, 2, outDataMITMAS)){
      updateCUGEX1("97", count, "L'article "+iITNO+" n'existe pas dans la base article (MMS001)")
      logger.debug("Article {$iITNO} n'existe pas")
    }
  }
  /**
   * Retrieve alias number
   */
  private void retrieveAliasNumber(){
    logger.debug("--------------------------retrieveAliasNumber--------------------------")
    iPOPN=""
    ExpressionFactory expressionMITPOP = database.getExpressionFactory("MITPOP")
    expressionMITPOP = expressionMITPOP.le("MPVFDT", currentDate as String)
    expressionMITPOP.and(expressionMITPOP.ge("MPLVDT", currentDate as String))
    DBAction enseigneQuery = database.table("MITPOP").index("00").matching(expressionMITPOP).selection("MPPOPN").build()
    DBContainer MITPOP = enseigneQuery.getContainer()
    MITPOP.set("MPCONO",currentCompany)
    MITPOP.set("MPALWT","3" as Integer)
    MITPOP.set("MPALWQ","ENS")
    MITPOP.set("MPITNO", iITNO)
    if (!enseigneQuery.readAll(MITPOP, 4, outDataMITPOP)){
    }
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
  }
  /**
   * Research Purchase Price in agreement
   */
  private double retrievePurchasePrice(){
    logger.debug("--------------------------retrievePurchasePrice--------------------------")
    iSUNO = ""
    svAGNB=""
    svSUNO=""
    svDIVI=""
    szOBV1=""
    szOBV2=""
    szOBV3=""
    szOBV4=""
    szFVDT=""
    boolean Retour=false
    double oPUPRNum=0
    Integer SVAGPT= 0
    String oVAGN="00000"
    String SVPRIO=""
    String oPRIO=""
    Integer savedFVDT=99999999
    Integer iFVDTInt=iFVDT as Integer

    DBAction tableArticleDepot = database.table("MITBAL").index("00").selection("MBDIVI", "MBSUNO").build()
    DBContainer MITBAL = tableArticleDepot.getContainer()
    MITBAL.set("MBCONO", currentCompany)
    MITBAL.set("MBITNO", iITNO)
    MITBAL.set("MBWHLO", iWHLO)
    tableArticleDepot.readAll(MITBAL,3,{DBContainer recordMITBAL->
      svDIVI = recordMITBAL.get("MBDIVI")
      svSUNO = recordMITBAL.get("MBSUNO")
    })
    logger.debug("value svDIVI= {$svDIVI}")
    logger.debug("value MITBAL svUNO= {$svSUNO}")

    // Retrieve supplier (svSUNO)
    miCounter = 0
    maxRecord = 1
    logger.debug("value svSUNO= {$svSUNO}")

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
    logger.debug("value iWHLO= {$iWHLO}")
    logger.debug("value iPIDE= {$iPIDE}")
    logger.debug("value iFVDTInt= {$iFVDTInt}")
    logger.debug("value oVAGN= {$oVAGN}")

    logger.debug("value iZIPL= {$iZIPL}")
    DBAction tableEXT030 = database.table("EXT030").index("10").selection("EXZIPP","EXPRIO").build()
    DBContainer EXT030 = tableEXT030.getContainer()
    EXT030.set("EXCONO", currentCompany)
    EXT030.set("EXZIPS", iZIPL)
    tableEXT030.readAll(EXT030,2,{DBContainer recordEXT030->
      logger.debug("EXT030 OK")
      String oZIPP = recordEXT030.get("EXZIPP")
      oPRIO = recordEXT030.get("EXPRIO")

      logger.debug("value oZIPP= {$oZIPP}")
      logger.debug("value svSUNO= {$svSUNO}")
      logger.debug("value oPRIO= {$oPRIO}")
      DBAction tableCompContrat = database.table("EXT032").index("30").selection("EXAGNB").build()
      DBContainer EXT032Liste = tableCompContrat.getContainer()
      EXT032Liste.set("EXCONO", currentCompany)
      EXT032Liste.set("EXZIPP", oZIPP)
      EXT032Liste.set("EXSUNO", svSUNO)
      tableCompContrat.readAll(EXT032Liste,3,{DBContainer recordEXT032->
        logger.debug("EXT032 OK")
        String oAGNB = recordEXT032.get("EXAGNB")
        logger.debug("value oAGNB= {$oAGNB}")
        DBAction tableContrat = database.table("MPAGRH").index("00").selection("AHAGNB","AHVAGN","AHAGTP","AHPAST","AHFVDT","AHUVDT").build()
        DBContainer MPAGRH = tableContrat.getContainer()
        MPAGRH.set("AHCONO", currentCompany)
        MPAGRH.set("AHSUNO", svSUNO)
        MPAGRH.set("AHAGNB", oAGNB)
        tableContrat.readAll(MPAGRH,3,{DBContainer recordMPAGRH ->
          logger.debug("MPAGRH 1 OK")
          String oPAST = recordMPAGRH.get("AHPAST")
          Integer oFVDT = recordMPAGRH.get("AHFVDT")  as Integer
          Integer oUVDT = recordMPAGRH.get("AHUVDT")  as Integer
          if(oUVDT==0)oUVDT=99999999
          logger.debug("value oPAST= {$oPAST}")
          logger.debug("value oFVDT= {$oFVDT}")
          logger.debug("value oUVDT= {$oUVDT}")
          String svVAGN =recordMPAGRH.get("AHVAGN")
          logger.debug("value svVAGN= {$svVAGN}")
          if(iFVDTInt>= oFVDT && iFVDTInt <=oUVDT && oPAST=="40" && oFVDT < savedFVDT){
            if(recordMPAGRH.get("AHVAGN")==oVAGN ){
              ExpressionFactory expressionMPAGRL = database.getExpressionFactory("MPAGRL")
              expressionMPAGRL = expressionMPAGRL.eq("AIOBV1", iITNO)
              logger.debug("MPAGRH 2 OK")
              logger.debug("value oAGNB= {$oAGNB}")
              logger.debug("value oPAST= {$oPAST}")
              logger.debug("value oFVDT= {$oFVDT}")
              logger.debug("value oUVDT= {$oUVDT}")
              DBAction tableLigneContrat = database.table("MPAGRL").index("00").matching(expressionMPAGRL).selection("AIAGPT","AIFVDT","AIUVDT","AIOBV1","AIOBV2","AIOBV3","AIOBV4").build()
              DBContainer MPAGRL = tableLigneContrat.getContainer()
              MPAGRL.set("AICONO", currentCompany)
              MPAGRL.set("AISUNO", svSUNO)
              MPAGRL.set("AIAGNB", oAGNB)
              MPAGRL.set("AIGRPI", "30" as Integer)
              MPAGRL.set("AIOBV1", iITNO)
              tableLigneContrat.readAll(MPAGRL,5,{DBContainer recordMPAGRL ->
                Integer oFVDTMpagrl = recordMPAGRL.get("AIFVDT") as Integer
                Integer oUVDTMpagrl = recordMPAGRL.get("AIUVDT") as Integer
                Integer oAGTP = recordMPAGRL.get("AIAGPT") as Integer
                if(oUVDTMpagrl==0)oUVDT=99999999
                logger.debug("MPAGRL OK")

                DBAction tableLigneCompContrat = database.table("MPAGRP").index("00").selection("AJPUPR").build()
                DBContainer MPAGRP = tableLigneCompContrat.getContainer()
                MPAGRP.set("AJCONO", currentCompany)
                MPAGRP.set("AJSUNO", svSUNO)
                MPAGRP.set("AJAGNB", oAGNB)
                MPAGRP.set("AJGRPI", "30" as Integer)
                MPAGRP.set("AJOBV1", iITNO)
                tableLigneCompContrat.readAll(MPAGRP,5,{DBContainer recordMPAGRP ->
                  logger.debug("MPAGRP 1 OK")
                  String oPUPR = recordMPAGRP.get("AJPUPR")
                  logger.debug("value oPUPR= {$oPUPR}")
                  logger.debug("value SVAGPT= {$SVAGPT}")
                  logger.debug("value oAGTP= {$oAGTP}")
                  if((SVAGPT==0 || SVAGPT >= oAGTP) && iFVDTInt>= oFVDTMpagrl && iFVDTInt <=oUVDTMpagrl){
                    if(SVPRIO=="" || oPRIO==SVPRIO){
                      logger.debug("MPAGRP 2 OK")
                      logger.debug("value oAGNB= {$oAGNB}")
                      logger.debug("value oPAST= {$oPAST}")
                      logger.debug("value oFVDT= {$oFVDT}")
                      logger.debug("value oUVDT= {$oUVDT}")
                      logger.debug("value oPUPR= {$oPUPR}")
                      SVAGPT = oAGTP
                      oPUPRNum=oPUPR as double
                      iSUNO = svSUNO
                      svAGNB = oAGNB
                      szOBV1 = recordMPAGRL.get("AIOBV1")
                      szOBV2 = recordMPAGRL.get("AIOBV2")
                      szOBV3 = recordMPAGRL.get("AIOBV3")
                      szOBV4 = recordMPAGRL.get("AIOBV4")
                      szFVDT = oFVDTMpagrl
                      SVPRIO=oPRIO
                      savedFVDT=recordMPAGRH.get("AHFVDT")  as Integer
                    }
                  }
                })
              })
            }
          }
        })
      })
    })
    return oPUPRNum
  }
  /**
   * Research Calcul NEPR
   **/
  private double retrieveNetPrice(double iPUPR){
    logger.debug("--------------------------retrieveNetPrice--------------------------")
    double oNEPR=iPUPR
    double oTaxeSp = 0
    double oFraisFSA = 0
    double oTauxRFA = 0
    double oRFA = 0
    String decimalSeparator = ","
    DBAction queryCUGEX1 = database.table("CUGEX1").index("00").selection("F1N096","F1N196","F1N296","F1A030","F1A130","F1A230","F1A330").build()
    DBContainer CUGEX1 = queryCUGEX1.getContainer()
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
    queryCUGEX1.readAll(CUGEX1,10,{DBContainer recordCUGEX1 ->
      String oA330 = recordCUGEX1.get("F1A330")
      double oN096 = recordCUGEX1.get("F1N096") as double
      double oN196 = recordCUGEX1.get("F1N196") as double
      double oN296 = recordCUGEX1.get("F1N296") as double
      oTaxeSp = oN096
      oFraisFSA = oN196
      oRFA= oN296
      DBAction MPCOVETauxRFA = database.table("MPCOVE").index("00").selection("IJOVHE").build()
      DBContainer MPCOVE = MPCOVETauxRFA.getContainer()
      MPCOVE.set("IJCONO",currentCompany)
      MPCOVE.set("IJCEID", "RFAFRS")
      MPCOVE.set("IJOVK1", oA330)
      MPCOVETauxRFA.readAll(MPCOVE,3,{DBContainer recordMPCOVE ->
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
   * Calculate sales price
   */
  private double retrieveSalesPrice(){
    logger.debug("--------------------------retrieveSalesPrice--------------------------")
    double salesPrice = 0
    double netPrice = iNEPR
    double logisticsCost = iCOST
    double companyMargin = iMDIV
    logger.debug("value netPrice= {$netPrice}")
    logger.debug("value logisticsCost= {$logisticsCost}")
    logger.debug("value companyMargin= {$companyMargin}")
    if (iFLAG == 1) {
      salesPrice = (netPrice+logisticsCost)/(1-companyMargin/100)
    } else {
      salesPrice = netPrice/(1-companyMargin/100)
    }
    return salesPrice
  }
  /**
   * Retrieve logistics cost
   *
   */
  private double retrieveLogisticsCost(){
    logger.debug("--------------------------retrieveLogisticsCost--------------------------")
    // Get logistics cost
    cost = 0
    logger.debug("value iITTY= {$iITTY}")
    DBAction queryCUGEX1 = database.table("CUGEX1").index("00").selection("F1N096").build()
    DBContainer CUGEX1 = queryCUGEX1.getContainer()
    CUGEX1.set("F1CONO", currentCompany)
    CUGEX1.set("F1FILE", "MITTTY")
    CUGEX1.set("F1PK02", iITTY)
    if(!queryCUGEX1.readAll(CUGEX1, 4, outDataCUGEX1)){}
    logger.debug("value cost= {$cost}")

    // Get conversion factor
    cofa = 0
    DBAction queryMITAUN = database.table("MITAUN").index("00").selection("MUCOFA").build()
    DBContainer MITAUN = queryMITAUN.getContainer()
    MITAUN.set("MUCONO", currentCompany)
    MITAUN.set("MUITNO", iITNO)
    MITAUN.set("MUAUTP", 1)
    MITAUN.set("MUALUN", "COL")
    if(queryMITAUN.read(MITAUN)) {
      cofa = MITAUN.get("MUCOFA")
    }
    double COST = 0
    if(cofa != 0){
      COST = cost / cofa
    } else {
      COST = cost
    }
    logger.debug("value cofa= {$cofa}")
    return COST
  }
  /**
   * Retrieve margin
   *
   */
  private double retrieveCompanyMargin(){
    logger.debug("--------------------------retrieveCompanyMargin--------------------------")
    logger.debug("value iBUAR= {$iBUAR}")
    iMarge = 0
    ExpressionFactory expressionEXT043 = database.getExpressionFactory("EXT043")
    expressionEXT043 = expressionEXT043.le("EXFDAT", currentDate as String)
    expressionEXT043.and(expressionEXT043.ge("EXTDAT", currentDate as String))
    DBAction query = database.table("EXT043").index("00").matching(expressionEXT043).selection("EXMARG").build()
    DBContainer EXT043 = query.getContainer()
    EXT043.set("EXCONO", currentCompany)
    EXT043.set("EXBUAR", iBUAR)
    if(!query.readAll(EXT043, 2, outDataEXT043)){
      IN60=true
      updateCUGEX1("97", count, "L'enregistrement Marge Plancher niveau société n'existe pas")
      logger.debug("L'enregistrement Marge niveau société n'existe pas")
    }
    return iMarge
  }
  /**
   * Update EXT075
   *
   */
  private void updateEXT075(){
    logger.debug("--------------------------updateEXT075--------------------------")
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction tarifCompMajQuery = database.table("EXT075").index("00").selection("EXCHNO").build()
    DBContainer majEXT075 = tarifCompMajQuery.getContainer()
    majEXT075.set("EXCONO",currentCompany)
    majEXT075.set("EXPRRF",iPRRF)
    majEXT075.set("EXCUCD",iCUCD)
    majEXT075.set("EXCUNO",iCUNO)
    majEXT075.set("EXFVDT",iFVDT as Integer)
    majEXT075.set("EXITNO",iITNO)
    majEXT075.set("EXOBV1",iOBV1)
    majEXT075.set("EXOBV2",iOBV2)
    majEXT075.set("EXOBV3",iOBV3)
    majEXT075.set("EXVFDT",iVFDT as Integer)
    if(!tarifCompMajQuery.readLock(majEXT075, updateCallBackEXT075)){
    }
  }
  /**
   * Update EXT075 before Process 1
   *
   */
  private void updatePriceList(){
    DBAction tarifCompMajQuery = database.table("EXT075").index("00").selection("EXSAPR","EXPOPN","EXITTY","EXHIE2","EXHIE3","EXITNO","EXOBV1","EXOBV2","EXOBV3","EXVFDT","EXPUPR","EXNEPR").build()
    DBContainer majEXT075 = tarifCompMajQuery.getContainer()
    majEXT075.set("EXCONO",currentCompany)
    majEXT075.set("EXPRRF",iPRRF)
    majEXT075.set("EXCUCD",iCUCD)
    majEXT075.set("EXCUNO",iCUNO)
    majEXT075.set("EXFVDT",iFVDT as Integer)
    if(!tarifCompMajQuery.readAll(majEXT075, 5, outDataEXT075T2)){
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
  // updateCUGEX1 : Maj status CUGEX1
  public void updateCUGEX1(String istatus, String icount, String imotif){
    if(!ano){
      counterCUGEX1++
      iCunoBlank=""
      if(counterCUGEX1<=2){
        DBAction queryCUGEX1 = database.table("CUGEX1").index("00").build()
        DBContainer CUGEX1 = queryCUGEX1.getContainer()
        CUGEX1.set("F1CONO", currentCompany)
        CUGEX1.set("F1FILE", "OPRICH")
        CUGEX1.set("F1PK01", iPRRF)
        CUGEX1.set("F1PK02", iCUCD)
        CUGEX1.set("F1PK03", iCunoBlank)
        CUGEX1.set("F1PK04", iFVDT)
        if (!queryCUGEX1.read(CUGEX1)) {
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
  private executeOIS340MILstSupplSummary(String FACI, String ORTP, String WHLO, String CUNO, String POPN, String ORQA){
    Map<String, String> params = ["FACI": FACI, "ORTP": ORTP, "WHLO": WHLO, "CUNO": CUNO, "POPN": POPN, "ORQA": ORQA]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
      } else {
        miCounter++
        if(miCounter <= maxRecord){
          svSUNO = response.SUNO.trim()
        }
      }
    }
    miCaller.call("OIS340MI", "LstSupplSummary", params, handler)
  }
}
