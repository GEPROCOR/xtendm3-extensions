/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT070
 * Description : The CalcStatistic transaction launch calcul from OSBSTD records to agregate in the EXT070 table (EXT070MI.CalcStatistic).
 * Date         Changed By   Description
 * 20211201     CDUV         TARX16 - Calcul du tarif
 * 20220519     CDUV         lowerCamelCase and date calculation have been fixed
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class EXT070 extends ExtendM3Batch {
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

  private Integer currentDate
  private String currentDivision
  private double totalSaam
  private double saam_Cumul_lu
  private double saam_Maj
  private double taux_lu
  private Integer flag_lu
  private double doubleSaam_lu
  private Set<String> listClient = new HashSet()
  private String iCuno
  private String iItno
  private double doubleIvqt_lu
  private double totalIvqt
  private double ivqt_Maj

  private String  inCuno
  private String  inItno

  private String OutCUNO

  public EXT070(LoggerAPI logger, DatabaseAPI database, ProgramAPI program, BatchAPI batch, MICallerAPI miCaller, TextFilesAPI textFiles) {
    this.logger = logger
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

    if(batch.getReferenceId().isPresent()){
      Optional<String> data = getJobData(batch.getReferenceId().get())
      performActualJob(data)
    } else {
      // No job data found
    }
  }
  // Get job data
  private Optional<String> getJobData(String referenceId){
    def query = database.table("EXTJOB").index("00").selection("EXDATA").build()
    def container = query.createContainer()
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
    inCuno = getFirstParameter()
    inItno = getNextParameter()

    if(inCuno==null)inCuno="";
    if(inItno==null)inItno="";
    currentCompany = (Integer)program.getLDAZD().CONO

    // Perform Job
    iItno=""
    iCuno=""

    // Delete record EXT070
    suppressionExt070()

    // Launch Extract OSBSTD
    currentDivision = program.getLDAZD().DIVI
    LocalDateTime timeOfCreation = LocalDateTime.now()
    //currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    //currentDate = currentDate - 10000
    //String DateJ = currentDate
    currentDate = timeOfCreation.minusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    String DateJ = currentDate

    ExpressionFactory expression = database.getExpressionFactory("OSBSTD")
    if (inCuno == "" && inItno == "") {
      expression = expression.ge("UCLMDT", DateJ)
    }
    if (inCuno != "" && inItno != "") {
      iCuno=inCuno
      iItno=inItno
      expression = expression.eq("UCCUNO", iCuno)
      expression = expression.and(expression.eq("UCITNO", iItno))
      expression = expression.and(expression.ge("UCLMDT", DateJ))

    }
    if (inCuno != "" && inItno == "") {
      iCuno=inCuno
      expression = expression.eq("UCCUNO", iCuno)
      expression = expression.and(expression.ge("UCLMDT", DateJ))
    }
    if (inCuno == "" && inItno != "") {
      iItno=inItno
      expression = expression.eq("UCITNO", iItno)
      expression = expression.and(expression.ge("UCLMDT", DateJ))
    }

    DBAction query = database.table("OSBSTD").index("00").matching(expression).selection("UCCUNO", "UCITNO", "UCSAAM", "UCLMDT","UCIVQT").build()
    DBContainer OSBSTD = query.getContainer()
    OSBSTD.set("UCCONO", currentCompany)
    OSBSTD.set("UCDIVI", currentDivision)
    if(!query.readAll(OSBSTD, 2, outDataOSBSTD)){
      return
    }
    // Update EXT070
    Iterator<String> IteratorClient = listClient.iterator()
    while (IteratorClient.hasNext()) {
      String oCUNO = IteratorClient.next()

      saam_Cumul_lu=0d
      totalSaam=0d
      taux_lu=0d
      flag_lu = 0
      doubleSaam_lu=0d
      DBAction RechercheEXT070 = database.table("EXT070").index("00").selection("EXCUNO","EXITNO","EXSAAM").build()
      DBContainer EXT070_totalSaam = RechercheEXT070.createContainer()
      EXT070_totalSaam.set("EXCONO", currentCompany)
      EXT070_totalSaam.set("EXCUNO", oCUNO)
      if(!RechercheEXT070.readAll(EXT070_totalSaam, 2, outData)){
        return
      }

      Iterator<String> it = AllLine(totalSaam,oCUNO).iterator()
      while (it.hasNext()) {
        String iSortie = it.next()
        String[] colonnes
        colonnes = iSortie.split(";")
        String CUNO_lu =colonnes[0]
        String ITNO_lu =colonnes[1]
        saam_Cumul_lu =colonnes[2] as double
        taux_lu =colonnes[3] as double
        flag_lu =colonnes[4] as Integer
        DBAction query_Maj = database.table("EXT070").index("00").selection("EXFLAG","EXSAAT","EXTAUX","EXSAAC","EXCHNO").build()
        DBContainer EXT070_Maj = query_Maj.getContainer()
        EXT070_Maj.set("EXCONO", currentCompany)
        EXT070_Maj.set("EXCUNO",  CUNO_lu)
        EXT070_Maj.set("EXITNO",  ITNO_lu)
        if(!query_Maj.readLock(EXT070_Maj, updateCallBack)){
          return
        }
      }
    }
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
  //  Delete records related to the current job from EXTJOB table
  public void deleteEXTJOB(){
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXTJOB").index("00").build()
    DBContainer EXTJOB = query.getContainer()
    EXTJOB.set("EXRFID", batch.getReferenceId().get())
    if(!query.readAllLock(EXTJOB, 1, updateCallBack_EXTJOB)){
    }
  }
  // Delete EXTJOB
  Closure<?> updateCallBack_EXTJOB = { LockedResult lockedResult ->
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
  // Delete EXT070
  Closure<?> DeleteEXT070 = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  // Retrieve OSBSTD
  Closure<?> outDataOSBSTD = { DBContainer OSBSTD ->
    String oCUNO = OSBSTD.get("UCCUNO")
    String oITNO = OSBSTD.get("UCITNO")
    String oSAAM = OSBSTD.get("UCSAAM")
    String oRGDT = OSBSTD.get("UCLMDT")
    String oIVQT = OSBSTD.get("UCIVQT")
    saam_Maj = oSAAM as double
    ivqt_Maj = oIVQT as double
    oCUNO=rechercheClient(oCUNO)
    listClient.add(oCUNO)

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT070").index("00").selection("EXCONO", "EXCUNO", "EXITNO", "EXSAAM").build()
    DBContainer EXT070 = query.getContainer()
    EXT070.set("EXCONO", currentCompany)
    EXT070.set("EXCUNO",  oCUNO)
    EXT070.set("EXITNO",  oITNO)
    if(!query.readLock(EXT070, updateEXT070)){
      EXT070.set("EXIVQT", oIVQT as double)
      EXT070.set("EXSAAM", oSAAM as double)
      EXT070.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT070.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT070.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT070.setInt("EXCHNO", 1)
      EXT070.set("EXCHID", program.getUser())
      query.insert(EXT070)
    }
  }
  // Retrieve EXT070
  Closure<?> outData = { DBContainer EXT070_totalSaam ->
    doubleSaam_lu = EXT070_totalSaam.get("EXSAAM") as double
    totalSaam = totalSaam + doubleSaam_lu
  }
  // Update EXT070
  Closure<?> updateEXT070 = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    double iSaam = lockedResult.get("EXSAAM") as double
    double iIvqt = lockedResult.get("EXIVQT") as double

    iSaam = iSaam + saam_Maj
    lockedResult.set("EXSAAM", iSaam)
    iIvqt = iIvqt + ivqt_Maj
    lockedResult.set("EXIVQT", iIvqt)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // Update file
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.set("EXSAAC", saam_Cumul_lu)
    lockedResult.set("EXSAAT", totalSaam)
    lockedResult.set("EXTAUX", taux_lu)
    lockedResult.set("EXFLAG", flag_lu)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  /**
   * AllLine : return lit of record order by amount desc of table EXT070 to calculate the rate between total_read_amount/total_amount_customer
   *
   * if >=80% FLAG=1
   * if <80%  FLAG=2
   * if total_amount_customer=0 Flag=0
   */
  private Set<String> AllLine (double iSaam, String entryCuno){
    double saam_Cumul = 0d
    Set<String> listLine = new HashSet()
    def rechercheExt070_prep = database.table("EXT070").index("10").selection("EXCUNO","EXSAAM","EXITNO").build()
    def EXT070 = rechercheExt070_prep.createContainer()
    EXT070.set("EXCONO", currentCompany)
    EXT070.set("EXCUNO", entryCuno)
    rechercheExt070_prep.readAll(EXT070,2,{DBContainer record ->
      String oCUNO = record.get("EXCUNO")
      String oITNO = record.get("EXITNO")
      double oSAAM = record.get("EXSAAM") as double
      saam_Cumul = saam_Cumul + oSAAM
      double oTaux = 100*(saam_Cumul/iSaam)
      String oFlag="0"
      if(oTaux<80d){
        oFlag = "2"
      }
      if(oTaux>=80d){
        oFlag = "1"
      }
      if(oSAAM==0d){
        oFlag = "0"
        oTaux = 0d
      }
      String oSortie = oCUNO.trim()+";"+oITNO.trim()+";"+saam_Cumul +";"+oTaux+";"+oFlag
      listLine.add(oSortie)})
    return listLine
  }
  // Retrieve EXT070
  Closure<?> outDataEXT070_Del = { DBContainer EXT070_Del ->
    String delCuno=""
    String delItno=""
    delCuno = EXT070_Del.get("EXCUNO")
    delItno = EXT070_Del.get("EXITNO")

    DBAction query = database.table("EXT070").index("00").build()
    DBContainer EXT070 = query.getContainer()

    EXT070.set("EXCONO", currentCompany)
    EXT070.set("EXCUNO", delCuno)
    EXT070.set("EXITNO", delItno)
    if(!query.readLock(EXT070, DeleteEXT070)){

    }

  }
  /**
   * suppressionExt070 : Delete EXT070
   */
  private void suppressionExt070(){
    String delCuno=""
    String delItno=""
    ExpressionFactory expression = database.getExpressionFactory("EXT070")
    if (inCuno != "" && inItno != "") {
      delCuno=inCuno
      delCuno=rechercheClient(delCuno)
      delItno=inItno
      expression = expression.eq("EXCUNO", delCuno)
      expression = expression.and(expression.eq("EXITNO", delItno))

    }
    if (inCuno != "" && inItno == "") {
      delCuno=inCuno
      delCuno=rechercheClient(delCuno)
      expression = expression.eq("EXCUNO", delCuno)
    }
    if (inCuno == "" && inItno != "") {
      delItno=inItno
      expression = expression.eq("EXITNO", delItno)
    }

    DBAction query = database.table("EXT070").index("00").matching(expression).selection("EXITNO", "EXCUNO").build()
    DBContainer EXT070 = query.getContainer()
    EXT070.set("EXCONO", currentCompany)
    if(!query.readAll(EXT070, 1, outDataEXT070_Del)){

    }
  }
  /**
   * Research Customer
   **/
  private String rechercheClient(String entryCuno){
    OutCUNO = entryCuno

    DBAction queryPere = database.table("OCHCUS").index("00").selection("OSCUNO").build()
    DBContainer OCHCUS = queryPere.getContainer()
    OCHCUS.set("OSCONO", currentCompany)
    OCHCUS.set("OSCHCT", entryCuno)
    if(!queryPere.readAll(OCHCUS, 2, outDataOCHCUS)){
      DBAction queryFils = database.table("OCHCUS").index("20").selection("OSCHCT").build()
      DBContainer OCHCUS_F = queryFils.getContainer()
      OCHCUS_F.set("OSCONO", currentCompany)
      OCHCUS_F.set("OSCUNO", entryCuno)
      if(!queryFils.readAll(OCHCUS_F, 2, outDataOCHCUS_F)){
      }
    }

    return OutCUNO
  }
  // Retrieve OCHCUS CUNO
  Closure<?> outDataOCHCUS = { DBContainer OCHCUS_Pere ->
    OutCUNO = OCHCUS_Pere.get("OSCUNO")
  }
  // Retrieve OCHCUS CHCT
  Closure<?> outDataOCHCUS_F = { DBContainer OCHCUS_Pere ->
    OutCUNO = OCHCUS_Pere.get("OSCHCT")
  }
}
