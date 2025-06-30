/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT082MI.MngPrcLst
 * Description : Reads the items of the assortments contained in EXT081 and adds them to the price list received in parameter.
 * Date         Changed By   Description
 * 20210408     RENARN       TARX03 - Add price list
 * 20211018     RENARN       Added management of business area, general settings via EXT800
 *                           Change in the management of customer number, executeOIS012MIAddPriceLstMtrx, EXT080 status, EXT075 table
 * 20211221     RENARN       Priorities management has been changed
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class MngPrcLst extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private int currentCompany
  private String prrf
  private String cucd
  private String cuno
  private String fvdt
  private String itno
  private String ascd
  private String sapr
  private String obv1
  private String obv2
  private String obv3
  private String obv4
  private String prex
  private String vfdt
  private String qtyl
  private String pcof
  private double PCOF
  private double zupa
  private String zipl
  private String pide = ""
  private String buar = ""
  private String whlo = ""
  private boolean item_found
  private String new_itno
  private Integer Count = 0
  private String count = 0
  private String EXT800_ORTP_1 = ""
  private String EXT800_ORTP_2 = ""
  private String EXT800_ORTP_3 = ""
  private String EXT800_ORTP_4 = ""
  private String EXT800_ORTP_5 = ""
  private String EXT800_ORTP_6 = ""
  private boolean IN60

  public MngPrcLst(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
    this.miCaller = miCaller
  }

  public void main() {
    currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    // Get general settings
    executeEXT800MIGetParam("EXT082MI_MngPrcLst")

    // Check price list
    prrf = mi.in.get("PRRF")
    cucd = mi.in.get("CUCD")
    cuno = mi.in.get("CUNO")
    fvdt = mi.in.get("FVDT")
    new_itno = mi.in.get("ITNO")
    //logger.debug("EXT082MI MngPrcLst: prrf = " + prrf)
    //logger.debug("EXT082MI MngPrcLst: cucd = " + cucd)
    //logger.debug("EXT082MI MngPrcLst: cuno = " + cuno)
    //logger.debug("EXT082MI MngPrcLst: fvdt = " + fvdt)
    //logger.debug("EXT082MI MngPrcLst: opt2 = " + mi.in.get("OPT2"))
    //logger.debug("EXT082MI MngPrcLst: new_itno = " + new_itno)
    DBAction OPRICH_query = database.table("OPRICH").index("00").build()
    DBContainer OPRICH = OPRICH_query.getContainer()
    OPRICH.set("OJCONO", currentCompany)
    OPRICH.set("OJPRRF", prrf)
    OPRICH.set("OJCUCD", cucd)
    OPRICH.set("OJCUNO", "")
    OPRICH.set("OJFVDT", fvdt as Integer)
    if (!OPRICH_query.read(OPRICH)) {
      mi.error("Tarif n'existe pas")
      return
    }

    // Check selection header
    zupa = 0
    zipl = ""
    pide = ""
    buar = ""
    whlo = ""
    DBAction EXT080_query = database.table("EXT080").index("00").selection("EXZUPA", "EXZIPL", "EXPIDE", "EXBUAR", "EXWHLO").build()
    DBContainer EXT080 = EXT080_query.getContainer()
    EXT080.set("EXCONO", currentCompany)
    EXT080.set("EXPRRF", prrf)
    EXT080.set("EXCUCD", cucd)
    EXT080.set("EXCUNO", cuno)
    EXT080.set("EXFVDT", fvdt as Integer)
    if (EXT080_query.read(EXT080)) {
      zupa = EXT080.get("EXZUPA") as Double
      zipl = EXT080.get("EXZIPL")
      pide = EXT080.get("EXPIDE")
      buar = EXT080.get("EXBUAR")
      whlo = EXT080.get("EXWHLO")
    }

    // Check option
    if(mi.in.get("OPT2") == null){
      mi.error("Option est obligatoire")
      return
    }

    if(mi.in.get("OPT2") != "1" && mi.in.get("OPT2") != "2"){
      String opt2 = mi.in.get("OPT2")
      mi.error("Option " + opt2 + " est invalide")
      return
    }

    // Update mode only
    if (mi.in.get("OPT2") == "2") {
      // Items that no longer exist in the selected assortments are removed from the price list.
      // Read Sales Price List - Basic
      DBAction OPRBAS_query = database.table("OPRBAS").index("00").build()
      DBContainer OPRBAS = OPRBAS_query.getContainer()
      OPRBAS.set("ODCONO", currentCompany)
      OPRBAS.set("ODPRRF", prrf)
      OPRBAS.set("ODCUCD", cucd)
      OPRBAS.set("ODCUNO", "")
      OPRBAS.set("ODFVDT", fvdt as Integer)
      if (!OPRBAS_query.readAll(OPRBAS, 5, outData_OPRBAS)) {
      }
    }

    // Add and update mode
    if (mi.in.get("OPT2") == "1" || mi.in.get("OPT2") == "2") {

      if (mi.in.get("OPT2") == "1")
        Update_CUGEX1("10", count)

      logger.debug("pide = " + pide)
      logger.debug("buar = " + buar)
      logger.debug("whlo = " + whlo)
      // Retrieve priority and object value 3
      prex = ""
      obv4 = ""
      if(pide.trim() != ""){
        prex = "2"
        obv4 = pide
      } else {
        if(buar.trim() != "") {
          prex = "3"
          obv4 = buar
        } else {
          prex = "8"
          obv4 = ""
        }
      }
      logger.debug("prex = " + prex)
      logger.debug("obv4 = " + obv4)
      // Add price list matrix for priority 2
      if(prex == "2") {
        DBAction OPRMTX_query = database.table("OPRMTX").index("00").build()
        DBContainer OPRMTX = OPRMTX_query.getContainer()
        // Read selected assortments
        OPRMTX.set("DXCONO", currentCompany)
        OPRMTX.set("DXPLTB", "GPC")
        OPRMTX.set("DXPREX", " 2")
        OPRMTX.set("DXOBV1", whlo)
        OPRMTX.set("DXOBV2", cuno)
        OPRMTX.set("DXOBV3", zipl)
        OPRMTX.set("DXOBV4", obv4)
        OPRMTX.set("DXOBV5", EXT800_ORTP_1)
        if (!OPRMTX_query.read(OPRMTX)) {
          // Add Price list Matrix
          if (EXT800_ORTP_1.trim() != "")
            executeOIS012MIAddPriceLstMtrx("GPC", prex, whlo, cuno, zipl, obv4, EXT800_ORTP_1, prrf)
          // Add Price list Matrix
          if (EXT800_ORTP_2.trim() != "")
            executeOIS012MIAddPriceLstMtrx("GPC", prex, whlo, cuno, zipl, obv4, EXT800_ORTP_2, prrf)
          // Add Price list Matrix
          if (EXT800_ORTP_3.trim() != "")
            executeOIS012MIAddPriceLstMtrx("GPC", prex, whlo, cuno, zipl, obv4, EXT800_ORTP_3, prrf)
          // Add Price list Matrix
          if (EXT800_ORTP_4.trim() != "")
            executeOIS012MIAddPriceLstMtrx("GPC", prex, whlo, cuno, zipl, obv4, EXT800_ORTP_4, prrf)
          // Add Price list Matrix
          if (EXT800_ORTP_5.trim() != "")
            executeOIS012MIAddPriceLstMtrx("GPC", prex, whlo, cuno, zipl, obv4, EXT800_ORTP_5, prrf)
          // Add Price list Matrix
          if (EXT800_ORTP_6.trim() != "")
            executeOIS012MIAddPriceLstMtrx("GPC", prex, whlo, cuno, zipl, obv4, EXT800_ORTP_6, prrf)
        } else {
          mi.error("Combinaison existe déjà pour la priorité 2")
          return
        }
      }
      // Add price list matrix for priority 3
      if(prex == "3"){
        managePrex3()
      }
      // Add price list matrix for priority 8
      if(prex == "8"){
        managePrex8()
      }
      logger.debug("Step 1")
      DBAction EXT081_query = database.table("EXT081").index("00").build()
      DBContainer EXT081 = EXT081_query.getContainer()
      // Read selected assortments
      EXT081.set("EXCONO", currentCompany)
      EXT081.set("EXPRRF", prrf)
      EXT081.set("EXCUCD", cucd)
      EXT081.set("EXCUNO", cuno)
      EXT081.set("EXFVDT", fvdt as Integer)
      if (!EXT081_query.readAll(EXT081, 5, outData_EXT081_2)) {
      }

      if (mi.in.get("OPT2") == "1") {
        count = Count
        Update_CUGEX1("90", count)
      }
    }
    // Update EXT080 status to 80 (Assortments updated)
    EXT080.set("EXCONO", currentCompany)
    EXT080.set("EXPRRF", prrf)
    EXT080.set("EXCUCD", cucd)
    EXT080.set("EXCUNO", cuno)
    EXT080.set("EXFVDT", fvdt as Integer)
    if(!EXT080_query.readLock(EXT080, updateCallBack_EXT080)){}
  }
  // Retrieve OPRBAS
  Closure<?> outData_OPRBAS = { DBContainer OPRBAS ->
    itno = OPRBAS.get("ODITNO")
    item_found = false
    DBAction EXT081_query = database.table("EXT081").index("00").build()
    DBContainer EXT081 = EXT081_query.getContainer()
    // Read selected assortments
    EXT081.set("EXCONO", currentCompany)
    EXT081.set("EXPRRF", prrf)
    EXT081.set("EXCUCD", cucd)
    EXT081.set("EXCUNO", cuno)
    EXT081.set("EXFVDT", fvdt as Integer)
    if (!EXT081_query.readAll(EXT081, 5, outData_EXT081)) {
    }
    if(!item_found) {
      // The item no longer exists in the selected assortments, it must be removed from the price list
      executeOIS017MIDelBasePrice(prrf, cucd, "", fvdt, itno)
      // Delete EXT075
      delEXT075()
    }
  }
  // Retrieve EXT081
  Closure<?> outData_EXT081 = { DBContainer EXT081 ->
    ascd = EXT081.get("EXASCD")
    DBAction OASITN_query = database.table("OASITN").index("00").build()
    DBContainer OASITN = OASITN_query.getContainer()
    OASITN.set("OICONO", currentCompany)
    OASITN.set("OIASCD", ascd)
    OASITN.set("OIITNO", itno)
    if (!OASITN_query.readAll(OASITN, 3, outData_OASITN)) {
      //logger.debug("EXT082MI executeOIS017MIDelBasePrice: prrf = " + prrf)
      //logger.debug("EXT082MI executeOIS017MIDelBasePrice: cucd = " + cucd)
      //logger.debug("EXT082MI executeOIS017MIDelBasePrice: cuno = " + cuno)
      //logger.debug("EXT082MI executeOIS017MIDelBasePrice: fvdt = " + fvdt)
      //logger.debug("EXT082MI executeOIS017MIDelBasePrice: itno = " + itno)
    }
  }
  // Execute OIS017MI.DelBasePrice
  private executeOIS017MIDelBasePrice(String PRRF, String CUCD, String CUNO, String FVDT, String ITNO){
    def parameters = ["PRRF": PRRF, "CUCD": CUCD, "CUNO": CUNO, "FVDT": FVDT, "ITNO": ITNO]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
      } else {
      }
    }
    miCaller.call("OIS017MI", "DelBasePrice", parameters, handler)
  }
  // Retrieve OASITN
  Closure<?> outData_OASITN = { DBContainer OASITN ->
    item_found = true
  }
  // Retrieve EXT081
  Closure<?> outData_EXT081_2 = { DBContainer EXT081 ->
    logger.debug("Step 2")
    ascd = EXT081.get("EXASCD")
    //logger.debug("logger EXT082MI MngPrcLst ascd = " + ascd)
    if(new_itno == null || new_itno == "") {
      logger.debug("Step 3")
      //logger.debug("logger EXT082MI MngPrcLst = Read all OASITN")
      DBAction OASITN_query = database.table("OASITN").index("00").build()
      DBContainer OASITN = OASITN_query.getContainer()
      OASITN.set("OICONO", currentCompany)
      OASITN.set("OIASCD", ascd)
      if (!OASITN_query.readAll(OASITN, 2, outData_OASITN_2)) {
      }
    } else {
      logger.debug("Step 4")
      //logger.debug("logger EXT082MI MngPrcLst = Read one OASITN")
      DBAction OASITN_query = database.table("OASITN").index("00").build()
      DBContainer OASITN = OASITN_query.getContainer()
      OASITN.set("OICONO", currentCompany)
      OASITN.set("OIASCD", ascd)
      OASITN.set("OIITNO", new_itno)
      if (!OASITN_query.readAll(OASITN, 3, outData_OASITN_2)) {
      }
    }
  }
  // Retrieve OASITN
  Closure<?> outData_OASITN_2 = { DBContainer OASITN ->
    logger.debug("Step 5")
    itno = OASITN.get("OIITNO")
    sapr = "0.001"
    DBAction OPRBAS_query = database.table("OPRBAS").index("00").build()
    DBContainer OPRBAS = OPRBAS_query.getContainer()
    OPRBAS.set("ODCONO", currentCompany)
    OPRBAS.set("ODPRRF", prrf)
    OPRBAS.set("ODCUCD", cucd)
    OPRBAS.set("ODCUNO", "")
    OPRBAS.set("ODFVDT", fvdt as Integer)
    OPRBAS.set("ODITNO", itno)
    if (!OPRBAS_query.readAll(OPRBAS, 6, outData_OPRBAS_2)) {
      //logger.debug("EXT082MI executeOIS017MIAddBasePrice: prrf = " + prrf)
      //logger.debug("EXT082MI executeOIS017MIAddBasePrice: cucd = " + cucd)
      //logger.debug("EXT082MI executeOIS017MIAddBasePrice: cuno = " + cuno)
      //logger.debug("EXT082MI executeOIS017MIAddBasePrice: fvdt = " + fvdt)
      //logger.debug("EXT082MI executeOIS017MIAddBasePrice: itno = " + itno)
      //logger.debug("EXT082MI executeOIS017MIAddBasePrice: sapr = " + sapr)
      // If an item from a selected assortment is missing from the corresponding price list, it is added to the price list.
      executeOIS017MIAddBasePrice(prrf, cucd, "", fvdt, itno, sapr)
      Count++
      // Add EXT075
      addEXT075()
      // Get alt unit
      qtyl = ""
      DBAction MITAUN_query = database.table("MITAUN").index("00").selection("MUCOFA").build()
      DBContainer MITAUN = MITAUN_query.getContainer()
      MITAUN.set("MUCONO", currentCompany)
      MITAUN.set("MUITNO", itno)
      MITAUN.set("MUAUTP", 1)
      MITAUN.set("MUALUN", "UPA")
      if (MITAUN_query.read(MITAUN)) {
        qtyl = MITAUN.get("MUCOFA")
      }
      // Calculate price adjustment factor
      if(zupa != 0){
        PCOF = 1 - (zupa/100)
        pcof = PCOF
        //logger.debug("EXT082MI executeOIS017MIAddGradSlsPrc: prrf = " + prrf)
        //logger.debug("EXT082MI executeOIS017MIAddGradSlsPrc: cucd = " + cucd)
        //logger.debug("EXT082MI executeOIS017MIAddGradSlsPrc: cuno = " + cuno)
        //logger.debug("EXT082MI executeOIS017MIAddGradSlsPrc: fvdt = " + fvdt)
        //logger.debug("EXT082MI executeOIS017MIAddGradSlsPrc: itno = " + itno)
        //logger.debug("EXT082MI executeOIS017MIAddGradSlsPrc: qtyl = " + qtyl)
        //logger.debug("EXT082MI executeOIS017MIAddGradSlsPrc: zupa = " + zupa)
        //logger.debug("EXT082MI executeOIS017MIAddGradSlsPrc: pcof = " + pcof)
        // Add Grad Sls Prc
        executeOIS017MIAddGradSlsPrc(prrf, cucd, "", fvdt, itno, qtyl, pcof)
      }
    }
  }
  // Retrieve OPRBAS
  Closure<?> outData_OPRBAS_2 = { DBContainer OPRBAS ->
    logger.debug("Step 6")
    obv1 = OPRBAS.get("ODOBV1")
    obv2 = OPRBAS.get("ODOBV2")
    obv3 = OPRBAS.get("ODOBV3")
    vfdt = OPRBAS.get("ODVFDT")
  }
  // Execute OIS017MI.AddBasePrice
  private executeOIS017MIAddBasePrice(String PRRF, String CUCD, String CUNO, String FVDT, String ITNO, String SAPR){
    def parameters = ["PRRF": PRRF, "CUCD": CUCD, "CUNO": CUNO, "FVDT": FVDT, "ITNO": ITNO, "SAPR": SAPR]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed OIS017MI.AddBasePrice: "+ response.errorMessage)
      } else {
      }
    }
    miCaller.call("OIS017MI", "AddBasePrice", parameters, handler)
  }
  // Add EXT075
  void addEXT075(){
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction EXT075_query = database.table("EXT075").index("00").build()
    DBContainer EXT075 = EXT075_query.getContainer()
    EXT075.set("EXCONO",currentCompany)
    EXT075.set("EXPRRF", prrf)
    EXT075.set("EXCUCD", cucd)
    EXT075.set("EXCUNO", cuno)
    EXT075.set("EXFVDT", fvdt as Integer)
    EXT075.set("EXITNO", itno)
    EXT075.set("EXOBV1", " ")
    EXT075.set("EXOBV2", " ")
    EXT075.set("EXOBV3", " ")
    EXT075.set("EXVFDT", 0)
    if(!EXT075_query.readLock(EXT075, updateCallBack_1)){
      EXT075.set("EXASCD", ascd)
      EXT075.set("EXZUPA", zupa)
      EXT075.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT075.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT075.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT075.setInt("EXCHNO", 1)
      EXT075.set("EXCHID", program.getUser())
      EXT075_query.insert(EXT075)
    }
  }
  // Update
  Closure<?> updateCallBack_1 = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.set("EXASCD", ascd)
    lockedResult.set("EXZUPA", zupa)
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
  // Delete EXT075
  void delEXT075(){
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction EXT075_query = database.table("EXT075").index("00").build()
    DBContainer EXT075 = EXT075_query.getContainer()
    EXT075.set("EXCONO",currentCompany)
    EXT075.set("EXPRRF", prrf)
    EXT075.set("EXCUCD", cucd)
    EXT075.set("EXCUNO", cuno)
    EXT075.set("EXFVDT", fvdt as Integer)
    EXT075.set("EXITNO", itno)
    EXT075.set("EXOBV1", " ")
    EXT075.set("EXOBV2", " ")
    EXT075.set("EXOBV3", " ")
    EXT075.set("EXVFDT", 0)
    if(!EXT075_query.readLock(EXT075, updateCallBack_2)){
    }
  }
  // Delete
  Closure<?> updateCallBack_2 = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  // Execute OIS017M.IAddGradSlsPrc
  private executeOIS017MIAddGradSlsPrc(String PRRF, String CUCD, String CUNO, String FVDT, String ITNO, String QTYL, String PCOF){
    def parameters = ["PRRF": PRRF, "CUCD": CUCD, "CUNO": CUNO, "FVDT": FVDT, "ITNO": ITNO, "QTYL": QTYL, "PCOF": PCOF]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
      } else {
      }
    }
    miCaller.call("OIS017MI", "AddGradSlsPrc", parameters, handler)
  }
  // Execute OIS012MI.AddPriceLstMtrx
  private executeOIS012MIAddPriceLstMtrx(String PLTB, String PREX, String OBV1, String OBV2, String OBV3, String OBV4, String OBV5, String PRRF){
    def parameters = ["PLTB": PLTB, "PREX": PREX, "OBV1": OBV1, "OBV2": OBV2, "OBV3": OBV3, "OBV4": OBV4, "OBV5": OBV5, "PRRF": PRRF]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
      } else {
      }
    }
    //logger.debug("executeOIS012MIAddPriceLstMtrx with param = " + PLTB + "/" + PREX + "/" + OBV1 + "/" + OBV2 + "/" + OBV3 + "/" + OBV4 + "/" + PRRF)
    miCaller.call("OIS012MI", "AddPriceLstMtrx", parameters, handler)
  }
  // Update CUGEX1
  public void Update_CUGEX1(String status, String count){
    DBAction CUGEX1_query = database.table("CUGEX1").index("00").build()
    DBContainer CUGEX1 = CUGEX1_query.getContainer()
    CUGEX1.set("F1CONO", currentCompany)
    CUGEX1.set("F1FILE", "OPRICH")
    CUGEX1.set("F1PK01", prrf)
    CUGEX1.set("F1PK02", cucd)
    CUGEX1.set("F1PK03", "")
    CUGEX1.set("F1PK04", fvdt)
    if (!CUGEX1_query.read(CUGEX1)) {
      //logger.debug("logger EXT052MI executeCUSEXTMIAddFieldValue : cucd = " + cucd)
      //logger.debug("logger EXT052MI executeCUSEXTMIAddFieldValue : cuno = " + cuno)
      //logger.debug("logger EXT052MI executeCUSEXTMIAddFieldValue : fvdt = " + fvdt)
      //logger.debug("logger EXT052MI executeCUSEXTMIAddFieldValue : status = " + status)
      //logger.debug("logger EXT052MI executeCUSEXTMIAddFieldValue : count = " + count)
      executeCUSEXTMIAddFieldValue("OPRICH", prrf, cucd, "", fvdt, "", "", "", "", status, count)
    } else {
      //logger.debug("logger EXT052MI executeCUSEXTMIChgFieldValue : cucd = " + cucd)
      //logger.debug("logger EXT052MI executeCUSEXTMIChgFieldValue : cuno = " + cuno)
      //logger.debug("logger EXT052MI executeCUSEXTMIChgFieldValue : fvdt = " + fvdt)
      //logger.debug("logger EXT052MI executeCUSEXTMIChgFieldValue : status = " + status)
      //logger.debug("logger EXT052MI executeCUSEXTMIChgFieldValue : count = " + count)
      executeCUSEXTMIChgFieldValue("OPRICH", prrf, cucd, "", fvdt, "", "", "", "", status, count)
    }
  }
  // Execute CUSEXTMI.AddFieldValue
  private executeCUSEXTMIAddFieldValue(String FILE, String PK01, String PK02, String PK03, String PK04, String PK05, String PK06, String PK07, String PK08, String A030, String N096){
    def parameters = ["FILE": FILE, "PK01": PK01, "PK02": PK02, "PK03": PK03, "PK04": PK04, "PK05": PK05, "PK06": PK06, "PK07": PK07, "PK08": PK08, "A030": A030, "N096": N096]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed CUSEXTMI.AddFieldValue: "+ response.errorMessage)
      } else {
      }
    }
    miCaller.call("CUSEXTMI", "AddFieldValue", parameters, handler)
  }
  // Execute CUSEXTMI.ChgFieldValue
  private executeCUSEXTMIChgFieldValue(String FILE, String PK01, String PK02, String PK03, String PK04, String PK05, String PK06, String PK07, String PK08, String A030, String N096){
    def parameters = ["FILE": FILE, "PK01": PK01, "PK02": PK02, "PK03": PK03, "PK04": PK04, "PK05": PK05, "PK06": PK06, "PK07": PK07, "PK08": PK08, "A030": A030, "N096": N096]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed CUSEXTMI.ChgFieldValue: "+ response.errorMessage)
      } else {
      }
    }
    miCaller.call("CUSEXTMI", "ChgFieldValue", parameters, handler)
  }
  // Update EXT080
  Closure<?> updateCallBack_EXT080 = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    // Update status to 80
    lockedResult.set("EXSTAT", "80")
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }

  // Execute EXT800MI GetParam to retrieve general settings
  private executeEXT800MIGetParam(String EXNM){
    def parameters = ["EXNM": EXNM]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        IN60 = true
        return
      }
      if (response.P001 != null)
        EXT800_ORTP_1 = response.P001.trim()
      if (response.P002 != null)
        EXT800_ORTP_2 = response.P002.trim()
      if (response.P003 != null)
        EXT800_ORTP_3 = response.P003.trim()
      if (response.P004 != null)
        EXT800_ORTP_4 = response.P004.trim()
      if (response.P005 != null)
        EXT800_ORTP_5 = response.P005.trim()
      if (response.P006 != null)
        EXT800_ORTP_6 = response.P006.trim()
    }
    miCaller.call("EXT800MI", "GetParam", parameters, handler)
  }
  // Manage priority 3
  public void managePrex3(){
    DBAction OPRMTX_query = database.table("OPRMTX").index("00").selection("DXPRRF").build()
    DBContainer OPRMTX = OPRMTX_query.getContainer()
    // Read selected assortments
    OPRMTX.set("DXCONO", currentCompany)
    OPRMTX.set("DXPLTB", "GPC")
    OPRMTX.set("DXPREX", " 3")
    OPRMTX.set("DXOBV1", whlo)
    OPRMTX.set("DXOBV2", cuno)
    OPRMTX.set("DXOBV3", zipl)
    OPRMTX.set("DXOBV4", obv4)
    OPRMTX.set("DXOBV5", "")
    if (!OPRMTX_query.read(OPRMTX)) {
      // Add Price list Matrix
      executeOIS012MIAddPriceLstMtrx("GPC", "3", whlo, cuno, zipl, obv4, "", prrf)
    } else {
      if(OPRMTX.get("DXPRRF") == prrf)
        return
      OPRMTX.set("DXPREX", " 4")
      if (!OPRMTX_query.read(OPRMTX)) {
        // Add Price list Matrix
        executeOIS012MIAddPriceLstMtrx("GPC", "4", whlo, cuno, zipl, obv4, "", prrf)
      } else {
        if(OPRMTX.get("DXPRRF") == prrf)
          return
        OPRMTX.set("DXPREX", " 5")
        if (!OPRMTX_query.read(OPRMTX)) {
          // Add Price list Matrix
          executeOIS012MIAddPriceLstMtrx("GPC", "5", whlo, cuno, zipl, obv4, "", prrf)
        } else {
          if(OPRMTX.get("DXPRRF") == prrf)
            return
          OPRMTX.set("DXPREX", " 6")
          if (!OPRMTX_query.read(OPRMTX)) {
            // Add Price list Matrix
            executeOIS012MIAddPriceLstMtrx("GPC", "6", whlo, cuno, zipl, obv4, "", prrf)
          } else {
            if(OPRMTX.get("DXPRRF") == prrf)
              return
            OPRMTX.set("DXPREX", " 7")
            if (!OPRMTX_query.read(OPRMTX)) {
              // Add Price list Matrix
              executeOIS012MIAddPriceLstMtrx("GPC", "7", whlo, cuno, zipl, obv4, "", prrf)
            } else {
              if(OPRMTX.get("DXPRRF") == prrf)
                return
              mi.error("Combinaison existe déjà pour la priorité 7")
              return
            }
          }
        }
      }
    }
  }
  // Manage priority 8
  public void managePrex8(){
    DBAction OPRMTX_query = database.table("OPRMTX").index("00").selection("DXPRRF").build()
    DBContainer OPRMTX = OPRMTX_query.getContainer()
    // Read selected assortments
    OPRMTX.set("DXCONO", currentCompany)
    OPRMTX.set("DXPLTB", "GPC")
    OPRMTX.set("DXPREX", " 8")
    OPRMTX.set("DXOBV1", whlo)
    OPRMTX.set("DXOBV2", cuno)
    OPRMTX.set("DXOBV3", zipl)
    OPRMTX.set("DXOBV4", obv4)
    OPRMTX.set("DXOBV5", "")
    //logger.debug("PREX 8 - OPRMTX OBV1" + whlo)
    //logger.debug("PREX 8 - OPRMTX OBV2" + cuno)
    //logger.debug("PREX 8 - OPRMTX OBV3" + zipl)
    //logger.debug("PREX 8 - OPRMTX OBV4" + obv4)
    if (!OPRMTX_query.read(OPRMTX)) {
      // Add Price list Matrix
      executeOIS012MIAddPriceLstMtrx("GPC", "8", whlo, cuno, zipl, obv4, "", prrf)
    } else {
      logger.debug("OPRMTX.PRRF = prrf ? " + (OPRMTX.get("DXPRRF") == prrf))
      logger.debug("Prio 8 existe déjà ? " + (OPRMTX.get("DXPRRF") == prrf))
      if(OPRMTX.get("DXPRRF") == prrf)
        return
      OPRMTX.set("DXPREX", " 9")
      if (!OPRMTX_query.read(OPRMTX)) {
        // Add Price list Matrix
        executeOIS012MIAddPriceLstMtrx("GPC", "9", whlo, cuno, zipl, obv4, "", prrf)
      } else {
        if(OPRMTX.get("DXPRRF") == prrf)
          return
        OPRMTX.set("DXPREX", "10")
        if (!OPRMTX_query.read(OPRMTX)) {
          // Add Price list Matrix
          executeOIS012MIAddPriceLstMtrx("GPC", "10", whlo, cuno, zipl, obv4, "", prrf)
        } else {
          if(OPRMTX.get("DXPRRF") == prrf)
            return
          mi.error("Combinaison existe déjà pour la priorité 9")
          return
        }
      }
    }
  }
}
