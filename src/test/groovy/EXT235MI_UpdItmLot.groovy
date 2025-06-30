/**
 * README
 * This extension is used by EventHub
 *
 * Name : EXT235MI.UpdItmLot
 * Description : Update MILOMA with lot informations stored in EXT236
 * Date         Changed By   Description
 * 20210929     RENARN       QUAX24 - Updates Item Lot
 * 20211130     YVOYOU       QUAX24-01 - Updates Item Lot New BREF management
 * 20211208     YVOYOU       Read EXT236 with MILOMA.BANO, update CUGEX1
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdItmLot extends ExtendM3Transaction {
  private final MIAPI mi;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private final UtilityAPI utility;
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private String whlo = ""
  private String itno = ""
  private String whsl = ""
  private String bano = ""
  private String camu = ""
  private String a030 = "" //A° 20211130 YVOYOU QUAX24-01
  private String a730 = "" //A° 20211130 YVOYOU QUAX24-01
  private String bref = "" //A° 20211130 YVOYOU QUAX24-01
  private Integer repn = 0
  private String itm8 = ""
  private String prod = ""
  private Integer expi = 0
  private Integer mfdt = 0
  private Integer aloc = 0
  private String stas = ""

  public UpdItmLot(MIAPI mi, DatabaseAPI database, ProgramAPI program, UtilityAPI utility, LoggerAPI logger, MICallerAPI miCaller) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.utility = utility;
    this.logger = logger
    this.miCaller = miCaller
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    if(mi.in.get("WHLO") == null) {
      mi.error("Dépôt est obligatoire");
      return;
    } else {
      whlo = mi.in.get("WHLO")
    }
    if(mi.in.get("ITNO") == null) {
      mi.error("Code article est obligatoire");
      return;
    } else {
      itno = mi.in.get("ITNO")
    }
    if(mi.in.get("WHSL") == null) {
      mi.error("Emplacement est obligatoire");
      return;
    } else {
      whsl = mi.in.get("WHSL")
    }
    if(mi.in.get("BANO") == null) {
      mi.error("Numéro de lot est obligatoire");
      return;
    } else {
      bano = mi.in.get("BANO")
    }
    if(mi.in.get("CAMU") != null)
      camu = mi.in.get("CAMU")
    if(mi.in.get("REPN") != null)
      repn = mi.in.get("REPN") as Integer

    // Get balance id
    DBAction query_MITLOC = database.table("MITLOC").index("00").selection("MLALOC", "MLSTAS").build()
    DBContainer MITLOC = query_MITLOC.getContainer()
    MITLOC.set("MLCONO", currentCompany)
    MITLOC.set("MLWHLO", mi.in.get("WHLO"))
    MITLOC.set("MLITNO", mi.in.get("ITNO"))
    MITLOC.set("MLWHSL", mi.in.get("WHSL"))
    MITLOC.set("MLBANO", mi.in.get("BANO"))
    MITLOC.set("MLCAMU", mi.in.get("CAMU"))
    MITLOC.set("MLREPN", mi.in.get("REPN") as Integer)
    if(query_MITLOC.read(MITLOC)){
      logger.debug("MITLOC found")
      aloc = MITLOC.get("MLALOC")
      stas = MITLOC.get("MLSTAS")
      // Get lot
      DBAction query_MILOMA = database.table("MILOMA").index("00").selection("LMRORN", "LMITNO", "LMBREF").build()
      DBContainer MILOMA = query_MILOMA.getContainer()
      MILOMA.set("LMCONO", currentCompany)
      MILOMA.set("LMITNO", mi.in.get("ITNO"))
      MILOMA.set("LMBANO", mi.in.get("BANO"))
      if(query_MILOMA.read(MILOMA)) {
        logger.debug("MILOMA found")
        // Get lot informations
        DBAction query_EXT236 = database.table("EXT236").index("00").selection("EXITM8", "EXPROD", "EXEXPI", "EXMFDT", "EXA030", "EXA730", "EXBREF").build()
        DBContainer EXT236 = query_EXT236.getContainer()
        EXT236.set("EXCONO", currentCompany)
        //EXT236.set("EXPUNO", MILOMA.get("LMRORN"))//D° 20211130 YVOYOU QUAX24-01
        EXT236.set("EXITNO", MILOMA.get("LMITNO"))
        //EXT236.set("EXBREF", MILOMA.get("LMBREF")) //D° 20211130 YVOYOU QUAX24-01
        EXT236.set("EXEBAN", MILOMA.get("LMBANO")) //A° 20211130 YVOYOU QUAX24-01
        if (query_EXT236.read(EXT236)) {
          //logger.debug("EXT236 found")
          itm8 = EXT236.get("EXITM8")
          prod = EXT236.get("EXPROD")
          expi = EXT236.get("EXEXPI")
          mfdt = EXT236.get("EXMFDT")
          a030 = EXT236.get("EXA030")
          a730 = EXT236.get("EXA730")
          bref = EXT236.get("EXBREF")
          LocalDateTime timeOfCreation = LocalDateTime.now()
          // Update lot (prod, mfdt)
          //executeMMS235MIUpdItmLot(itno, bano, prod, mfdt as String) //D° 20211130 YVOYOU QUAX24-01
          executeMMS235MIUpdItmLot(itno, bano, prod, bref, mfdt as String) //A° 20211130 YVOYOU QUAX24-01
          //Modification CUGEX1 // Begin 20211130 YVOYOU QUAX24-01
          DBAction queryCUGEX1 = database.table("CUGEX1").index("00").selection("F1CHNO").build()
          DBContainer CUGEX1 = queryCUGEX1.createContainer()
          CUGEX1.set("F1CONO", currentCompany)
          CUGEX1.set("F1FILE", "MILOMA")
          CUGEX1.set("F1PK01", MILOMA.get("LMITNO"))
          CUGEX1.set("F1PK02", MILOMA.get("LMBANO"))
          if (!queryCUGEX1.readLock(CUGEX1, updateCallBack)) {
            CUGEX1.set("F1CONO", currentCompany)
            CUGEX1.set("F1FILE", "MILOMA")
            CUGEX1.set("F1PK01", MILOMA.get("LMITNO"))
            CUGEX1.set("F1PK02", MILOMA.get("LMBANO"))
            if (a030 != null)
              CUGEX1.set("F1A030", a030)
            if (a730 != null)
              CUGEX1.set("F1A730", a730)
            CUGEX1.setInt("F1RGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            CUGEX1.setInt("F1RGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
            CUGEX1.setInt("F1LMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            CUGEX1.setInt("F1CHNO", 1)
            CUGEX1.set("F1CHID", program.getUser())
            queryCUGEX1.insert(CUGEX1)
          }
          // end 20211130 YVOYOU QUAX24-01
          // Update lot (expi)
          executeMMS850MIAddRclLotSts("*EXE", "WS", "WMS", whlo, itno, whsl, bano, camu, aloc as String, stas, expi as String)
          logger.debug("whlo = " + whlo)
          logger.debug("itno = " + itno)
          logger.debug("whsl = " + whsl)
          logger.debug("bano = " + bano)
          logger.debug("camu = " + camu)
          logger.debug("aloc = " + aloc)
          logger.debug("stas = " + stas)
          logger.debug("expi = " + expi)
        }
      }
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("LMCHNO")
    if (a030 != null)
      lockedResult.set("F1A030", a030)
    if (a730 != null)
      lockedResult.set("F1A730", a730)
    lockedResult.setInt("F1LMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("F1CHNO", changeNumber + 1)
    lockedResult.set("F1CHID", program.getUser())
    lockedResult.update()
  }
  //private executeMMS235MIUpdItmLot(String ITNO, String BANO, String PROD, String MFDT){ //D° 20211130 YVOYOU QUAX24-01
  private executeMMS235MIUpdItmLot(String ITNO, String BANO, String PROD, String BREF, String MFDT){ //A° 20211130 YVOYOU QUAX24-01
    //def parameters = ["ITNO": ITNO, "BANO": BANO, "PROD": PROD, "MFDT": MFDT]  //D° 20211130 YVOYOU QUAX24-01
    def parameters = ["ITNO": ITNO, "BANO": BANO, "BREF": BREF, "PROD": PROD, "MFDT": MFDT]  //A° 20211130 YVOYOU QUAX24-01
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
      } else {
      }
    }
    miCaller.call("MMS235MI", "UpdItmLot", parameters, handler)
  }
  private executeMMS850MIAddRclLotSts(String PRFL, String E0PA, String E065, String WHLO, String ITNO, String WHSL, String BANO, String CAMU, String ALOC, String STAS, String EXPI){
    def parameters = ["PRFL": PRFL, "E0PA": E0PA, "E065": E065, "WHLO": WHLO, "ITNO": ITNO, "WHSL": WHSL, "BANO": BANO, "CAMU": CAMU, "ALOC": ALOC, "STAS": STAS, "EXPI": EXPI]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
      } else {
      }
    }
    miCaller.call("MMS850MI", "AddRclLotSts", parameters, handler)
  }
}
