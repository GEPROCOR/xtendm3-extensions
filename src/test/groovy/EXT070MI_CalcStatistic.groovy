/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT070MI.CalcStatistic
 * Description : The CalcStatistic transaction launch calcul from OSBSTD records to agregate in the EXT070 table.
 * Date         Changed By   Description
 * 20210510     CDUV         TARX16 - Calcul du tarif
 * 20220204     CDUV         Search customer added
 * 20220519     CDUV         lowerCamelCase has been fixed
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class CalcStatistic extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final ProgramAPI program
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller

  private Integer currentCompany
  private Integer currentDate
  private final LoggerAPI logger
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
  private String outCuno
  private String entryCuno
  private String entryItno

  public CalcStatistic(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.program = program
    this.logger = logger

    this.miCaller = miCaller
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      mi.error("Company est obligatoire")
      return
    } else {
      currentCompany = mi.in.get("CONO")
    }

    entryItno = mi.in.get("ITNO")
    entryCuno = mi.in.get("CUNO")
    if(entryItno==null)entryItno=""
    if(entryCuno==null)entryCuno=""

    iItno=""
    iCuno=""

    suppressionExt070()
    currentDivision = program.getLDAZD().DIVI
    LocalDateTime timeOfCreation = LocalDateTime.now()
    //currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    // currentDate = currentDate - 10000
    //String dateJ = currentDate
    currentDate = timeOfCreation.minusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    //logger.debug("value currentDate= {$currentDate}")
    String dateJ = currentDate

    ExpressionFactory expression = database.getExpressionFactory("OSBSTD")

    if (entryCuno == "" && entryItno == "") {
      expression = expression.ge("UCLMDT", dateJ)
    }
    if (entryCuno != "" && entryItno != "") {
      iCuno=mi.in.get("CUNO")
      iItno=mi.in.get("ITNO")
      expression = expression.eq("UCCUNO", iCuno)
      expression = expression.and(expression.eq("UCITNO", iItno))
      expression = expression.and(expression.ge("UCLMDT", dateJ))

    }
    if (entryCuno != "" && entryItno == "") {
      iCuno=mi.in.get("CUNO")
      expression = expression.eq("UCCUNO", iCuno)
      expression = expression.and(expression.ge("UCLMDT", dateJ))
    }
    if (entryCuno == "" && entryItno != "") {
      iItno=mi.in.get("ITNO")
      expression = expression.eq("UCITNO", iItno)
      expression = expression.and(expression.ge("UCLMDT", dateJ))
    }

    DBAction query = database.table("OSBSTD").index("00").matching(expression).selection("UCCUNO", "UCITNO", "UCSAAM", "UCLMDT","UCIVQT").build()
    DBContainer OSBSTD = query.getContainer()
    OSBSTD.set("UCCONO", currentCompany)
    OSBSTD.set("UCDIVI", currentDivision)
    if(!query.readAll(OSBSTD, 2, outDataOsbstd)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
    // Maj EXT070
    Iterator<String> iteratorClient = listClient.iterator()
    while (iteratorClient.hasNext()) {
      String oCuno = iteratorClient.next()

      saam_Cumul_lu=0d
      totalSaam=0d
      taux_lu=0d
      flag_lu = 0
      doubleSaam_lu=0d
      DBAction RechercheEXT070 = database.table("EXT070").index("00").selection("EXCUNO","EXITNO","EXSAAM").build()
      DBContainer EXT070_totalSaam = RechercheEXT070.createContainer()
      EXT070_totalSaam.set("EXCONO", currentCompany)
      EXT070_totalSaam.set("EXCUNO", oCuno)
      if(!RechercheEXT070.readAll(EXT070_totalSaam, 2, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }

      Iterator<String> it = AllLine(totalSaam,oCuno).iterator()
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
          mi.error("L'enregistrement n'existe pas")
          return
        }
      }
    }
  }
  // DeleteEXT070 : Delete EXT070
  Closure<?> DeleteEXT070 = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  // outDataOsbstd : Retrieve OSBSTD and Add or Update EXT070
  Closure<?> outDataOsbstd = { DBContainer OSBSTD ->
    String oCuno = OSBSTD.get("UCCUNO")
    String oItno = OSBSTD.get("UCITNO")
    String oSaam = OSBSTD.get("UCSAAM")
    String oRgdt = OSBSTD.get("UCLMDT")
    String oIvqt = OSBSTD.get("UCIVQT")
    saam_Maj = oSaam as double
    ivqt_Maj = oIvqt as double
    oCuno=rechercheClient(oCuno)
    listClient.add(oCuno)

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT070").index("00").selection("EXCONO", "EXCUNO", "EXITNO", "EXSAAM").build()
    DBContainer EXT070 = query.getContainer()
    EXT070.set("EXCONO", currentCompany)
    EXT070.set("EXCUNO",  oCuno)
    EXT070.set("EXITNO",  oItno)
    if(!query.readLock(EXT070, updateEXT070)){
      EXT070.set("EXIVQT", oIvqt as double)
      EXT070.set("EXSAAM", oSaam as double)
      EXT070.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT070.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT070.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT070.setInt("EXCHNO", 1)
      EXT070.set("EXCHID", program.getUser())
      query.insert(EXT070)
    }
  }
  //outData : Retrieve EXT070 SAAM
  Closure<?> outData = { DBContainer EXT070_totalSaam ->
    doubleSaam_lu = EXT070_totalSaam.get("EXSAAM") as double
    totalSaam = totalSaam + doubleSaam_lu
  }
  // updateEXT070 : Update EXT070
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
  // updateCallBack : Update EXT070
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
    double SAAM_Cumul = 0d
    Set<String> ListLine = new HashSet()
    def RechercheEXT070_prep = database.table("EXT070").index("10").selection("EXCUNO","EXSAAM","EXITNO").build()
    def EXT070 = RechercheEXT070_prep.createContainer()
    EXT070.set("EXCONO", currentCompany)
    EXT070.set("EXCUNO", entryCuno)
    RechercheEXT070_prep.readAll(EXT070,2,{DBContainer record ->
      String oCuno = record.get("EXCUNO")
      String oItno = record.get("EXITNO")
      double oSaam = record.get("EXSAAM") as double
      SAAM_Cumul = SAAM_Cumul + oSaam
      double oTaux = 100*(SAAM_Cumul/iSaam)
      String oFlag="0"
      if(oTaux<80d){
        oFlag = "2"
      }
      if(oTaux>=80d){
        oFlag = "1"
      }
      if(oSaam==0d){
        oFlag = "0"
        oTaux = 0d
      }
      String oSortie = oCuno.trim()+";"+oItno.trim()+";"+SAAM_Cumul +";"+oTaux+";"+oFlag;
      ListLine.add(oSortie)})
    return ListLine
  }
  //outDataEXT070_Del : Delete EXT070
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
    if (entryCuno != "" && entryItno != "") {
      delCuno=mi.in.get("CUNO")
      delCuno=rechercheClient(delCuno)
      delItno=mi.in.get("ITNO")
      expression = expression.eq("EXCUNO", delCuno)
      expression = expression.and(expression.eq("EXITNO", delItno))

    }
    if (entryCuno != "" && entryItno == "") {
      delCuno=mi.in.get("CUNO")
      delCuno=rechercheClient(delCuno)
      expression = expression.eq("EXCUNO", delCuno)
    }
    if (entryCuno == "" && entryItno != "") {
      delItno=mi.in.get("ITNO")
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
    outCuno = entryCuno

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

    return outCuno
  }
  // outDataOCHCUS : Retrieve OCHCUS Father
  Closure<?> outDataOCHCUS = { DBContainer OCHCUS_Pere ->
    outCuno = OCHCUS_Pere.get("OSCUNO")
  }
  // outDataOCHCUS_F: Retrieve OCHCUS Son
  Closure<?> outDataOCHCUS_F = { DBContainer OCHCUS_Pere ->
    outCuno = OCHCUS_Pere.get("OSCHCT")
  }
}
