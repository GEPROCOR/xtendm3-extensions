/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT020MI.AddSalesPoint
 * Description : The AddSalesPoint transaction adds records to the EXT020 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX06 - Gestion des points de vente
 */


import java.time.format.DateTimeFormatter
import java.time.LocalDateTime


public class AddSalesPoint extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final ProgramAPI program
  private final LoggerAPI logger
  private int currentCompany //ACDUV

  public AddSalesPoint(MIAPI mi, DatabaseAPI database, ProgramAPI program,LoggerAPI logger) {
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
    if (mi.in.get("FPVT").equals("")) {
      mi.error("Point de vente début obligatoire")
      return
    }
    if (mi.in.get("TPVT").equals("")) {
      mi.error("Point de vente fin obligatoire")
      return
    }
    int svFPVT =mi.in.get("FPVT")
    int svTPVT =mi.in.get("TPVT")
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

    if (mi.in.get("WHTY").equals("")) {
      mi.error("Type de Dépot est obligatoire")
      return
    }

    DBAction queryCSYTAB_WHTY = database.table("CSYTAB").index("00").selection("CTCONO", "CTSTCO", "CTSTKY", "CTTX40").build()
    DBContainer CSYTAB_WHTY = queryCSYTAB_WHTY.getContainer()
    CSYTAB_WHTY.set("CTCONO", mi.in.get("CONO"))
    CSYTAB_WHTY.set("CTDIVI", "")
    CSYTAB_WHTY.set("CTSTCO", "WHTY")
    CSYTAB_WHTY.set("CTSTKY", mi.in.get("WHTY"))
    if (!queryCSYTAB_WHTY.read(CSYTAB_WHTY)) {
      mi.error("Type de Dépot est obligatoire")
      return
    }

    if (!mi.in.get("COPE").toString().trim().equalsIgnoreCase("0.0") && !mi.in.get("COPE").toString().trim().equalsIgnoreCase("1.0")) {
      mi.error("Code opération invalide"+mi.in.get("COPE"))
      return
    }

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

    if (!mi.in.get("CDAN").toString().equalsIgnoreCase("0.0") && !mi.in.get("CDAN").toString().equalsIgnoreCase("1.0")) {
      mi.error("Code danger invalide")
      return
    }

    if (mi.in.get("OTYG").equals("")) {
      mi.error("Groupe de type de commande")
      return
    }

    DBAction queryOOTYPG = database.table("OOTYPG").index("00").selection("OGCONO", "OGOTYG").build()
    DBContainer OOTYPG = queryOOTYPG.getContainer()
    OOTYPG.set("OGCONO", mi.in.get("CONO"))
    OOTYPG.set("OGOTYG", mi.in.get("OTYG"))
    if (!queryOOTYPG.readAll(OOTYPG, 2, release)) {
      mi.error("Groupe de type de commande n existe pas")
      return
    }

    if (!mi.in.get("STAT").toString().equalsIgnoreCase("0.0") && !mi.in.get("STAT").toString().equalsIgnoreCase("1.0")) {
      mi.error("statut invalide")
      return
    }

    DBAction queryEXT020 = database.table("EXT020").index("00").selection("EXCONO", "EXTYPE").build()
    DBContainer EXT020 = queryEXT020.createContainer()

    EXT020.set("EXCONO", mi.in.get("CONO"))
    EXT020.set("EXTYPE", mi.in.get("TYPE"))
    if (!queryEXT020.read(EXT020)) {
      EXT020.set("EXFPVT", mi.in.get("FPVT"))
      EXT020.set("EXTPVT", mi.in.get("TPVT"))
      EXT020.set("EXWHTY", mi.in.get("WHTY"))
      EXT020.set("EXTX40", CSYTAB_WHTY.get("CTTX40").toString())
      EXT020.set("EXCOPE", mi.in.get("COPE"))
      EXT020.set("EXSCAT", mi.in.get("SCAT"))
      EXT020.set("EXLSCA", CSYTAB.get("CTTX40").toString())
      EXT020.set("EXCUNO", mi.in.get("CUNO"))
      EXT020.set("EXCUNM", OCUSMA.get("OKCUNM").toString())
      EXT020.set("EXCDAN", mi.in.get("CDAN"))
      EXT020.set("EXOTYG", mi.in.get("OTYG"))
      EXT020.set("EXSTAT", mi.in.get("STAT"))
      LocalDateTime timeOfCreation = LocalDateTime.now()
      EXT020.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT020.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT020.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT020.setInt("EXCHNO", 1)
      EXT020.set("EXCHID", program.getUser())
      queryEXT020.insert(EXT020)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
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
