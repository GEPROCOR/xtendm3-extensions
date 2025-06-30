/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT070MI.AddStatistic
 * Description : The AddStatistic transaction add records to the EXT070 table. 
 * Date         Changed By   Description
 * 20210510     CDUV         TARX16 - Calcul du tarif
 * 20220204     CDUV         Logger and semicolon removed
 * 20220519     CDUV         lowerCamelCase has been fixed
 * 20220620     RENARN       Check ITNO and SUNO fixed
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddStatistic extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private double totalSaam
  private Integer currentCompany
  private double saam_Cumul_lu
  private double taux_lu
  private Integer flag_lu
  private double double_lu

  public AddStatistic(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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

    if(mi.in.get("CUNO") == null || mi.in.get("CUNO") == ""){
      mi.error("Code Client est obligatoire")
      return
    }

    if(mi.in.get("ITNO") == null || mi.in.get("ITNO") == ""){
      mi.error("Code Article est obligatoire")
      return
    }

    if(mi.in.get("SAAM") == null || mi.in.get("SAAM") == ""){
      mi.error("Montant est obligatoire")
      return
    }

    // Check Code Client
    if(mi.in.get("CUNO") != null || mi.in.get("CUNO") != ""){
      DBAction Client_Query = database.table("OCUSMA").index("00").build()
      DBContainer OCUSMA = Client_Query.getContainer()
      OCUSMA.set("OKCONO", mi.in.get("CONO"))
      OCUSMA.set("OKCUNO", mi.in.get("CUNO"))
      if(!Client_Query.read(OCUSMA)) {
        mi.error("Code Client " + mi.in.get("CUNO") + " n'existe pas")
        return
      }
    }
    // Check Code Article
    if(mi.in.get("ITNO") != null || mi.in.get("ITNO") != ""){
      DBAction Article_Query = database.table("MITMAS").index("00").build()
      DBContainer MITMAS = Article_Query.getContainer()
      MITMAS.set("MMCONO", mi.in.get("CONO"))
      MITMAS.set("MMITNO", mi.in.get("ITNO"))
      if(!Article_Query.read(MITMAS)) {
        mi.error("Code Article " + mi.in.get("ITNO") + " n'existe pas")
        return
      }
    }
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT070").index("00").build()
    DBContainer EXT070 = query.getContainer()
    EXT070.set("EXCONO", currentCompany)
    EXT070.set("EXCUNO",  mi.in.get("CUNO"))
    EXT070.set("EXITNO",  mi.in.get("ITNO"))
    if (!query.read(EXT070)) {
      EXT070.set("EXSAAM", mi.in.get("SAAM") as double)
      EXT070.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT070.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT070.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT070.setInt("EXCHNO", 1)
      EXT070.set("EXCHID", program.getUser())
      query.insert(EXT070)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
    // Maj EXT070
    saam_Cumul_lu=0d
    totalSaam=0d
    taux_lu=0d
    flag_lu = 0
    double_lu=0d
    DBAction rechercheExt070 = database.table("EXT070").index("00").selection("EXSAAM").build()
    DBContainer EXT070_totalSaam = rechercheExt070.getContainer()
    EXT070_totalSaam.set("EXCONO", currentCompany)
    EXT070_totalSaam.set("EXCUNO", mi.in.get("CUNO"))
    if(!rechercheExt070.readAll(EXT070_totalSaam, 2, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
    Iterator<String> it = allLine(totalSaam).iterator()
    while (it.hasNext()) {
      String iSortie = it.next()
      String[] colonnes
      colonnes = iSortie.split(";")
      String cuno_lu =colonnes[0]
      String itno_lu =colonnes[1]
      saam_Cumul_lu =colonnes[2] as double
      taux_lu =colonnes[3] as double
      flag_lu =colonnes[4] as Integer
      DBAction query_Maj = database.table("EXT070").index("00").selection("EXCHNO").build()
      DBContainer EXT070_Maj = query_Maj.getContainer()
      EXT070_Maj.set("EXCONO", currentCompany)
      EXT070_Maj.set("EXCUNO",  cuno_lu)
      EXT070_Maj.set("EXITNO",  itno_lu)
      if(!query_Maj.readLock(EXT070_Maj, updateCallBack)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }
  // outData  : Retrieve EXT070 SAAM
  Closure<?> outData = { DBContainer EXT070_totalSaam ->
    double_lu = EXT070_totalSaam.get("EXSAAM") as double
    totalSaam = totalSaam + double_lu
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
   * allLine : return lit of record order by amount desc of table EXT070 to calculate the rate between total_read_amount/total_amount_customer
   *
   * if >=80% FLAG=1
   * if <80%  FLAG=2
   * if total_amount_customer=0 Flag=0
   */
  private Set<String> allLine (double iSaam){
    double saam_Cumul = 0d
    Set<String> listLine = new HashSet()
    def rechercheExt070_prep = database.table("EXT070").index("10").selection("EXCUNO","EXSAAM","EXITNO").build()
    def EXT070 = rechercheExt070_prep.createContainer()
    EXT070.set("EXCONO", currentCompany)
    EXT070.set("EXCUNO", mi.in.get("CUNO"))
    rechercheExt070_prep.readAll(EXT070,2,{DBContainer record ->
      String oCuno = record.get("EXCUNO")
      String oItno = record.get("EXITNO")
      double oSaam = record.get("EXSAAM")
      saam_Cumul = saam_Cumul + oSaam
      double oTaux = 100*(saam_Cumul/iSaam)
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
      String oSortie = oCuno.trim()+";"+oItno.trim()+";"+saam_Cumul +";"+oTaux+";"+oFlag;
      listLine.add(oSortie)})
    return listLine
  }
}
