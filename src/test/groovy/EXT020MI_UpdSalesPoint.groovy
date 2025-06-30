/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT020MI.UpdSalesPoint
 * Description : The UpdSalesPoint transaction update records to the EXT020 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX06 - Gestion des points de vente
 */


import java.time.format.DateTimeFormatter
import java.time.LocalDateTime


public class UpdSalesPoint extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final ProgramAPI program
  private final LoggerAPI logger
  private int currentCompany //ACDUV
  private String oOCUSMA_CUNM
  private String oCSYTAB_SCAT_TX40
  private String oCSYTAB_WHTY_TX40

  public UpdSalesPoint(MIAPI mi, DatabaseAPI database, ProgramAPI program,LoggerAPI logger) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.logger = logger
  }

  public void main() {
    //Control field
    if (mi.in.get("CONO").equals("")) {
      mi.error("Division est obligatoire")
      return
    }
    if (mi.in.get("TYPE").equals("")) {
      mi.error("Type point de vente est obligatoire")
      return
    }

    DBAction queryEXT020_Recherche = database.table("EXT020").index("00").selection("EXCHNO").build()
    DBContainer EXT020_Recherche = queryEXT020_Recherche.createContainer()
    EXT020_Recherche.set("EXCONO", mi.in.get("CONO"))
    EXT020_Recherche.set("EXTYPE", mi.in.get("TYPE"))
    if (queryEXT020_Recherche.read(EXT020_Recherche)) {
      int svFPVT =mi.in.get("FPVT")
      int svTPVT =mi.in.get("TPVT")
      int FPVT_EXT020 = EXT020_Recherche.get("EXFPVT")as Integer
      int TPVT_EXT020 = EXT020_Recherche.get("EXTPVT") as  Integer
      if(svTPVT!=TPVT_EXT020 || svFPVT!=FPVT_EXT020){
        String svTYPE =  mi.in.get("TYPE")
        currentCompany = mi.in.get("CONO")
        Iterator<String> it = allType(currentCompany).iterator();
        while (it.hasNext()) {
          String iSortie = it.next()
          String[] colonnes
          colonnes = iSortie.split(";")
          String Type_lu =colonnes[0] ;
          Integer FPVT_lu =colonnes[1] as Integer;
          Integer TPVT_lu =colonnes[2] as Integer;
          if(!svTYPE.equals(Type_lu)){
            if(svFPVT>=FPVT_lu && svFPVT<=TPVT_lu
              ||svTPVT>=FPVT_lu && svTPVT<=TPVT_lu
              ||svFPVT<FPVT_lu && svTPVT>TPVT_lu
            ){
              mi.error("Plage de Points de Vente déjà utilisée sur Type={$Type_lu}")
              return
            }
          }
        }
      }
    }
    oCSYTAB_WHTY_TX40 =""
    if (!mi.in.get("WHTY").equals("")) {
      DBAction queryCSYTAB_WHTY = database.table("CSYTAB").index("00").selection("CTCONO", "CTSTCO", "CTSTKY", "CTTX40").build()
      DBContainer CSYTAB_WHTY = queryCSYTAB_WHTY.getContainer()
      CSYTAB_WHTY.set("CTCONO", mi.in.get("CONO"))
      CSYTAB_WHTY.set("CTDIVI", "")
      CSYTAB_WHTY.set("CTSTCO", "WHTY")
      CSYTAB_WHTY.set("CTSTKY", mi.in.get("WHTY"))
      if (!queryCSYTAB_WHTY.read(CSYTAB_WHTY)) {
        mi.error("Type de Dépot n'existe pas")
        return
      }
      oCSYTAB_WHTY_TX40 =CSYTAB_WHTY.get("CTTX40").toString()
    }


    if (!mi.in.get("COPE").equals("")) {
      if (!mi.in.get("COPE").toString().trim().equalsIgnoreCase("0.0") && !mi.in.get("COPE").toString().trim().equalsIgnoreCase("1.0")) {
        mi.error("Code opération invalide"+mi.in.get("COPE"))
        return
      }
    }
    oCSYTAB_SCAT_TX40 = ""
    if (!mi.in.get("SCAT").equals("")) {
      DBAction queryCSYTAB = database.table("CSYTAB").index("00").selection("CTCONO", "CTSTCO", "CTSTKY", "CTTX40").build()
      DBContainer CSYTAB = queryCSYTAB.getContainer()
      if (mi.in.get("SCAT") != null) {
        CSYTAB.set("CTCONO", mi.in.get("CONO"))
        CSYTAB.set("CTDIVI",  program.LDAZD.DIVI)
        CSYTAB.set("CTSTCO", "CFC3")
        CSYTAB.set("CTSTKY", mi.in.get("SCAT"))
        if (!queryCSYTAB.read(CSYTAB)) {
          CSYTAB.set("CTCONO", mi.in.get("CONO"))
          CSYTAB.set("CTDIVI",  "")
          CSYTAB.set("CTSTCO", "CFC3")
          CSYTAB.set("CTSTKY", mi.in.get("SCAT"))
          if (!queryCSYTAB.read(CSYTAB)) {
            mi.error("Sous catégorie n existe pas")
            return
          }
        }
      }
      oCSYTAB_SCAT_TX40 = CSYTAB.get("CTTX40").toString()
    }
    oOCUSMA_CUNM = ""
    if (!mi.in.get("CUNO").equals("")) {
      DBAction queryOCUSMA = database.table("OCUSMA").index("00").selection("OKCONO", "OKCUNO", "OKCUNM").build()
      DBContainer OCUSMA = queryOCUSMA.getContainer()
      if (mi.in.get("CUNO") != null) {
        OCUSMA.set("OKCONO", mi.in.get("CONO"))
        OCUSMA.set("OKCUNO", mi.in.get("CUNO"))
        if (!queryOCUSMA.read(OCUSMA)) {
          mi.error("Code client n existe pas")
          return
        }
      }
      oOCUSMA_CUNM = OCUSMA.get("OKCUNM").toString()
    }

    if (!mi.in.get("CDAN").equals("")) {
      if (!mi.in.get("CDAN").toString().equalsIgnoreCase("0.0") && !mi.in.get("CDAN").toString().equalsIgnoreCase("1.0")) {
        mi.error("Code danger invalide")
        return
      }
    }
    if (!mi.in.get("OTYG").equals("")) {
      DBAction queryOOTYPG = database.table("OOTYPG").index("00").selection("OGCONO", "OGOTYG").build()
      DBContainer OOTYPG = queryOOTYPG.getContainer()
      OOTYPG.set("OGCONO", mi.in.get("CONO"))
      OOTYPG.set("OGOTYG", mi.in.get("OTYG"))
      if (!queryOOTYPG.readAll(OOTYPG, 2, release)) {
        mi.error("Groupe de type de commande n existe pas")
        return
      }
    }
    if (!mi.in.get("STAT").equals("")) {
      if (!mi.in.get("STAT").toString().equalsIgnoreCase("0.0") && !mi.in.get("STAT").toString().equalsIgnoreCase("1.0")) {
        mi.error("statut invalide")
        return
      }
    }

    DBAction queryEXT020 = database.table("EXT020").index("00").selection("EXCHNO").build()
    DBContainer EXT020 = queryEXT020.createContainer()

    EXT020.set("EXCONO", mi.in.get("CONO"))
    EXT020.set("EXTYPE", mi.in.get("TYPE"))
    if (!queryEXT020.readLock(EXT020, updateCallBack)) {
      mi.error("L'enregistrement existe déjà");
      return;
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")

    if(mi.in.get("FPVT")!="")lockedResult.set("EXFPVT", mi.in.get("FPVT"))
    if(mi.in.get("TPVT")!="")lockedResult.set("EXTPVT", mi.in.get("TPVT"))
    if(mi.in.get("WHTY")!="")lockedResult.set("EXWHTY", mi.in.get("WHTY"))
    if(mi.in.get("WHTY")!="")lockedResult.set("EXTX40", oCSYTAB_WHTY_TX40)
    if(mi.in.get("COPE")!="")lockedResult.set("EXCOPE", mi.in.get("COPE"))
    if(mi.in.get("SCAT")!="")lockedResult.set("EXSCAT", mi.in.get("SCAT"))
    if(mi.in.get("SCAT")!="")lockedResult.set("EXLSCA", oCSYTAB_WHTY_TX40)
    if(mi.in.get("CUNO")!="")lockedResult.set("EXCUNO", mi.in.get("CUNO"))
    if(mi.in.get("CUNO")!="")lockedResult.set("EXCUNM", oOCUSMA_CUNM)
    if(mi.in.get("CDAN")!="")lockedResult.set("EXCDAN", mi.in.get("CDAN"))
    if(mi.in.get("OTYG")!="")lockedResult.set("EXOTYG", mi.in.get("OTYG"))
    if(mi.in.get("STAT")!="")lockedResult.set("EXSTAT", mi.in.get("STAT"))

    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  Closure<?> release = {DBContainer container ->
    String TypeCommande = container.get("OGORTP")
  }
  /**
   * save values in Hashset ListType to check no record already in the date range
   * values return in a string containing Type,FPVT and TPVT
   *
   */

  private Set<String> allType (int compagny){
    Set<String> listType = new HashSet()
    def rechercheEXT020 = database.table("EXT020").index("00").selection("EXTYPE","EXFPVT","EXTPVT").build()
    def EXT020 = rechercheEXT020.createContainer()
    EXT020.set("EXCONO", currentCompany)
    rechercheEXT020.readAll(EXT020,1,{DBContainer record ->
      String oType = record.get("EXTYPE")
      String oFPVT = record.get("EXFPVT");
      String oTPVT = record.get("EXTPVT");
      String oSortie = oType.trim()+";"+oFPVT.trim()+";"+oTPVT.trim();
      listType.add(oSortie)})
    return listType
  }
}
