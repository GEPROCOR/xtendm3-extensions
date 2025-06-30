/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT070MI.DltStatistic
 * Description : The DltStatistic transaction delete records to the EXT070 table. 
 * Date         Changed By   Description
 * 20210510     CDUV         TARX16 - Calcul du tarif
 * 20220204     CDUV         Method comment added, semicolons at the end of the line removed
 * 20220519     CDUV         lowerCamelCase has been fixed
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class DltStatistic extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private double totalSaam
  private Integer currentCompany
  private double saam_Cumul_lu
  private double taux_lu
  private Integer flag_lu
  private double double_lu

  public DltStatistic(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
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
    String iCUNO=mi.in.get("CUNO")
    String iITNO=mi.in.get("ITNO")
    DBAction query = database.table("EXT070").index("00").build()
    DBContainer EXT070 = query.getContainer()
    EXT070.set("EXCONO", currentCompany)
    EXT070.set("EXCUNO", iCUNO)
    EXT070.set("EXITNO", iITNO)
    if(!query.readLock(EXT070, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
    // Maj EXT070
    saam_Cumul_lu=0d
    totalSaam=0d
    taux_lu=0d
    flag_lu = 0
    double_lu=0d
    DBAction RechercheEXT070 = database.table("EXT070").index("00").selection("EXCUNO","EXITNO","EXSAAM").build()
    DBContainer EXT070_totalSaam = RechercheEXT070.createContainer()
    EXT070_totalSaam.set("EXCONO", currentCompany)
    EXT070_totalSaam.set("EXCUNO", mi.in.get("CUNO"))
    if(!RechercheEXT070.readAll(EXT070_totalSaam, 2, outData)){
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
      if(!query_Maj.readLock(EXT070_Maj, updateCallBack_Maj)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }
  // outData : Retrieve EXT070 SAAM
  Closure<?> outData = { DBContainer EXT070_totalSaam ->
    double_lu = EXT070_totalSaam.get("EXSAAM") as double
    logger.debug("double_lu= {$double_lu}")
    totalSaam = totalSaam + double_lu
  }
  // updateCallBack : Delete EXT070
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  // updateCallBack_Maj : Update EXT070
  Closure<?> updateCallBack_Maj = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.set("EXSAAC", saam_Cumul_lu)
    lockedResult.set("EXSAAT", totalSaam)
    lockedResult.set("EXtaux", taux_lu)
    lockedResult.set("EXflag", flag_lu)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  /**
   * allLine : return lit of record order by amount desc of table EXT070 to calculate the rate between total_read_amount/total_amount_customer
   *
   * if >=80% flag=1
   * if <80%  flag=2
   * if total_amount_customer=0 flag=0
   */
  private Set<String> allLine (double iSAAM){
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
      double otaux = saam_Cumul/iSAAM
      String oflag="0"
      if(otaux<0.8d){
        oflag = "2"
      }
      if(otaux>=0.8d){
        oflag = "1"
      }
      if(oSaam==0d){
        oflag = "0"
        otaux = 0d
      }
      String oSortie = oCuno.trim()+";"+oItno.trim()+";"+saam_Cumul +";"+otaux+";"+oflag;
      listLine.add(oSortie)})
    return listLine
  }
}
