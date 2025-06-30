/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT100MI.RtvITM8
 * Description : The RtvITM8 transaction retrieve list of item number in MMS025.
 * Date         Changed By   Description
 * 20210510     CDUV         REAX02 Recherche ITM8-EAN13
 */

public class RtvITM8 extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility;
  private final MICallerAPI miCaller;
  private Integer currentCompany
  private String codePROMO
  private String currentDivision
  private String codeOperation
  private String codITM8

  public RtvITM8(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi;
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility;
  }
  public void main() {
    String dwdt ="";
    String cnqt ="";
    codePROMO = ""
    codeOperation=""
    codITM8=""

    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
    }
    currentDivision = program.getLDAZD().DIVI;
    if(mi.in.get("ITNO") == null || mi.in.get("ITNO") == ""){
      mi.error("Code Article est obligatoire")
      return
    }
    if(mi.in.get("DWDT") == null || mi.in.get("DWDT") == ""){
      mi.error("Date Livraison est obligatoire")
      return
    }else {
      dwdt = mi.in.get("DWDT");
      logger.debug("EXT100MI Upd dwdt="+dwdt)
      if (!utility.call("DateUtil", "isDateValid", dwdt, "yyyyMMdd")) {
        logger.debug(" Date de Validité incorrect EXT100MI Upd dwdt="+dwdt)
        mi.error("Format Date de Validité incorrect")
        return
      }
    }
    if(mi.in.get("CNQT") == null || mi.in.get("CNQT") == ""){
      mi.error("Quantité est obligatoire")
      return
    }else{
      cnqt = mi.in.get("CNQT");
      if (!utility.call("NumberUtil", "isValidNumber", cnqt, ".")) {
        mi.error("Format quantité  incorrect")
        return
      }
    }

    if(mi.in.get("PIDE") != null){
      codeOperation=""
      String iPIDE = mi.in.get("PIDE")
      if(!recherchePromo(iPIDE)){
        mi.error("Code Promo transmis n'existe pas")
        return
      }
    }
    if(mi.in.get("SUNO") != null && mi.in.get("AGNB") == null ||
      mi.in.get("SUNO") == null && mi.in.get("AGNB") != null){
      mi.error("Recherche par contrat: Fournisseur et Num Contrat sont obligatoires")
      return
    }
    if(mi.in.get("ORNO") != null && mi.in.get("PONR") == null ||
      mi.in.get("ORNO") == null && mi.in.get("PONR") != null){
      mi.error("Recherche par CDV: Num Commande et de ligne sont obligatoires")
      return
    }
    if(mi.in.get("ORNO") == null && mi.in.get("SUNO") == null && mi.in.get("PIDE") == null ){
      mi.error("Saisir au choix CDV, contrat ou Code promo")
      return
    }
    if(mi.in.get("ORNO") != null && mi.in.get("SUNO") != null){
      mi.error("Recherche par CDV ou contrat : 1 seul choix")
      return
    }
    if(mi.in.get("PIDE") != null && mi.in.get("SUNO") != null){
      mi.error("Recherche par Code promo ou contrat : 1 seul choix")
      return
    }
    if(mi.in.get("ORNO") != null && mi.in.get("PONR") != null){
      codePROMO = ""
      DBAction rechercheOOLINE = database.table("OOLINE").index("00").selection("OBPOPN","OBPIDE").build()
      DBContainer OOLINE = rechercheOOLINE.getContainer()
      OOLINE.set("OBCONO", currentCompany)
      OOLINE.set("OBORNO", mi.in.get("ORNO"))
      OOLINE.set("OBPONR", mi.in.get("PONR"))
      if(rechercheOOLINE.read(OOLINE)){
        codePROMO = OOLINE.get("OBPIDE")
        codITM8= OOLINE.get("OBPOPN")
      }
    }
    if(codITM8.trim()!=""){
      mi.outData.put("POPN", codITM8)
      mi.write()
      return
    }
    if(codePROMO.trim()!="" && codeOperation.trim()==""){
      if(!recherchePromo(codePROMO)){
        mi.error("Code Promo CDV n'existe pas")
        return
      }
    }
    if(mi.in.get("SUNO") != null && mi.in.get("AGNB") != null){
      DBAction rechercheMPAGRH = database.table("MPAGRH").index("00").selection("AHVAGN").build()
      DBContainer MPAGRH = rechercheMPAGRH.getContainer()
      MPAGRH.set("AHCONO", currentCompany)
      MPAGRH.set("AHSUNO", mi.in.get("SUNO"))
      MPAGRH.set("AHAGNB", mi.in.get("AGNB"))
      if(rechercheMPAGRH.read(MPAGRH)){
        codeOperation= MPAGRH.get("AHVAGN")
      }
    }
    if(codeOperation.trim()=="" && mi.in.get("ORNO") == null){
      mi.error("Code Opération non trouvé")
      return
    }
    if(codeOperation.trim()=="" && mi.in.get("ORNO") != null){
      Integer chb1 = 0
      DBAction rechercheOOHEAD = database.table("OOHEAD").index("00").selection("OAORTP").build()
      DBContainer OOHEAD = rechercheOOHEAD.getContainer()
      OOHEAD.set("OACONO", currentCompany)
      OOHEAD.set("OAORNO", mi.in.get("ORNO"))
      if(rechercheOOHEAD.read(OOHEAD)){
        DBAction rechercheCUGEX1 = database.table("CUGEX1").index("00").selection("F1CHB1").build()
        DBContainer CUGEX1 = rechercheCUGEX1.getContainer()
        CUGEX1.set("F1CONO", currentCompany)
        CUGEX1.set("F1FILE",  "OOTYPE")
        CUGEX1.set("F1PK01",  OOHEAD.get("OAORTP"))
        CUGEX1.set("F1PK02",  "")
        CUGEX1.set("F1PK03",  "")
        CUGEX1.set("F1PK04",  "")
        CUGEX1.set("F1PK05",  "")
        CUGEX1.set("F1PK06",  "")
        CUGEX1.set("F1PK07",  "")
        CUGEX1.set("F1PK08",  "")
        if(rechercheCUGEX1.read(CUGEX1)){
          chb1 = CUGEX1.get("F1CHB1")
        }
      }
      if(chb1 == 1){
        codeOperation = "XXXXX"
      } else {
        codeOperation = "00000"
      }
    }
    boolean enreg_Retour = false
    Iterator<String> it = rechercheItemWithQty(currentCompany, cnqt as double,dwdt as Integer,codeOperation).iterator();

    while (it.hasNext()) {
      enreg_Retour = true
      codITM8 = it.next()
      mi.outData.put("POPN", codITM8)
      mi.write()
    }
    if(!enreg_Retour){
      mi.error("Aucun enregistrement trouvé")
      return
    }
  }
  /**
   * Return list of Item number of MMS025
   */
  private Set<String> rechercheItemWithQty (int compagny, double Qty , Integer Date, String E0PA){
    Set<String> itmQty = new HashSet()
    itmQty.clear()
    String savePOPN = ""
    String savePOPN2 = ""
    boolean MITPOP_trouv=false
    double oQty = Qty
    Integer oVFDT_NUM = 0
    Integer oLVDT_NUM = 0
    String oE0PA = ""
    Integer saved_VFDT = 0                    //A ARENARD 210830
    Integer saved_VFDT2 = 0                   //A ARENARD 210830
    def rechercheMITPOP = database.table("MITPOP").index("00").selection("MPE0PA","MPVFDT","MPLVDT","MPPOPN","MPCNQT","MPITNO").build()
    def MITPOP = rechercheMITPOP.createContainer()
    MITPOP.set("MPCONO", compagny)
    MITPOP.set("MPALWT",  "3" as Integer)
    MITPOP.set("MPALWQ",  "ITM8")
    MITPOP.set("MPITNO",  mi.in.get("ITNO"))
    rechercheMITPOP.readAll(MITPOP,4,{DBContainer record ->
      oVFDT_NUM =  record.get("MPVFDT")  as Integer
      oLVDT_NUM =  record.get("MPLVDT") as Integer
      oE0PA =  record.get("MPE0PA")
      if(oLVDT_NUM ==0)oLVDT_NUM=99999999;
      if(Date>= oVFDT_NUM && Date <=oLVDT_NUM && (oE0PA.trim()==E0PA.trim() || oE0PA.trim() != "00000" && E0PA.trim() == "XXXXX")){
        //SavePOPN = record.get("MPPOPN")                      //D ARENARD 210830
        if(saved_VFDT == 0) {                                  //A ARENARD 210830
          saved_VFDT = oVFDT_NUM                               //A ARENARD 210830
          savePOPN = record.get("MPPOPN")                      //A ARENARD 210830
        } else {                                               //A ARENARD 210830
          if (oVFDT_NUM > saved_VFDT) {                        //A ARENARD 210830
            saved_VFDT = oVFDT_NUM                             //A ARENARD 210830
            savePOPN = record.get("MPPOPN")                    //A ARENARD 210830
          }                                                    //A ARENARD 210830
        }                                                      //A ARENARD 210830
        MITPOP_trouv=true
        String oCNQT = record.get("MPCNQT");
        double CNQT_lu = oCNQT as double
        //if(CNQT_lu >= oQty && SavePOPN2==""){                 //D ARENARD 210830
        if(CNQT_lu >= oQty){                                    //A ARENARD 210830
          //SavePOPN2 = record.get("MPPOPN")                    //D ARENARD 210830
          if(saved_VFDT2 == 0) {                                //A ARENARD 210830
            saved_VFDT2 = oVFDT_NUM                             //A ARENARD 210830
            savePOPN2 = record.get("MPPOPN")                    //A ARENARD 210830
          } else {                                              //A ARENARD 210830
            if (oVFDT_NUM > saved_VFDT2) {                      //A ARENARD 210830
              saved_VFDT2 = oVFDT_NUM                           //A ARENARD 210830
              savePOPN2 = record.get("MPPOPN")                  //A ARENARD 210830
            }                                                   //A ARENARD 210830
          }                                                     //A ARENARD 210830
          //String oSortie = savePOPN2.trim();                  //D ARENARD 210830
          //itmQty.add(oSortie)                                 //D ARENARD 210830
          //return itmQty                                       //D ARENARD 210830
        }
      }

    })
    if(MITPOP_trouv && savePOPN!=""){
      if(savePOPN2==""){
        String oSortie = savePOPN.trim();
        itmQty.add(oSortie)
      }else{
        String oSortie = savePOPN2.trim();
        itmQty.add(oSortie)
      }
    }
    return itmQty
  }
/**
 * Return true if found Operation Code
 */
  private boolean recherchePromo(String iPIDE){
    boolean retour=false;
    def tablePromo = database.table("OPROMH").index("00").selection("FZTX15").build()
    def OPROMH = tablePromo.getContainer()
    OPROMH.set("FZCONO", currentCompany)
    OPROMH.set("FZDIVI", currentDivision)
    OPROMH.set("FZPIDE", iPIDE)
    tablePromo.readAll(OPROMH,3,{DBContainer record ->
      String oTX15 = record.get("FZTX15")
      retour=true;
      codeOperation = oTX15.trim();
      return retour
    })
  }
}
