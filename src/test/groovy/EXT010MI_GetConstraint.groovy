/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT010MI.GetConstraint
 * Description : Retrieve records from the EXT010 table.
 * Date         Changed By   Description
 * 20210219     RENARN       QUAX01 - Constraints matrix
 * 20211102     RENARN       Output parameters HAZI and ZPDA have been added
 * 20220228     RENARN       ZTPS, WHLO, WHNM has been added
 */
public class GetConstraint extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany

  public GetConstraint(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    DBAction query = database.table("EXT010").index("00").selection("EXITNO", "EXZCFE", "EXZCLV", "EXSUNO", "EXCUNO", "EXSUN1", "EXSUN3", "EXCSCD", "EXCSNO", "EXHIE0", "EXSPE1", "EXSPE2", /**"EXSUCM",**/ "EXCFI1", "EXCFI4", "EXCFI5", "EXZSLT", "EXZPLT", "EXCSC1", "EXORTP", "EXITTY", "EXZONU", "EXHAZI", "EXZPDA", "EXZTPS", "EXWHLO", "EXDO01", "EXDO02", "EXDO03", "EXDO04", "EXDO05", "EXDO06", "EXDO07", "EXDO08", "EXDO09", "EXDO10", "EXDO11", "EXDO12", "EXDO13", "EXDO14", "EXDO15", "EXTXID", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT010 = query.getContainer()
    EXT010.set("EXCONO", currentCompany)
    EXT010.set("EXZCID",  mi.in.get("ZCID"))
    if(!query.readAll(EXT010, 2, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Get EXT010
  Closure<?> outData = { DBContainer EXT010 ->
    String itemNumber = EXT010.get("EXITNO")
    // Get item description
    String itemNumber_description = ""
    if(EXT010.get("EXITNO") != ""){
      DBAction query = database.table("MITMAS").index("00").selection("MMITDS").build()
      DBContainer MITMAS = query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO",  EXT010.get("EXITNO"))
      if (query.read(MITMAS)) {
        itemNumber_description = MITMAS.get("MMITDS")
      }
    }
    String constrainingFeature = EXT010.get("EXZCFE")
    // Get constraining feature description
    String constrainingFeature_description = ""
    if(EXT010.get("EXZCFE") != ""){
      DBAction query = database.table("EXT013").index("00").selection("EXZDES").build()
      DBContainer EXT013 = query.getContainer()
      EXT013.set("EXCONO", currentCompany)
      EXT013.set("EXZCFE",  EXT010.get("EXZCFE"))
      if (query.read(EXT013)) {
        constrainingFeature_description = EXT013.get("EXZDES")
      }
    }
    String constraintLevel = EXT010.get("EXZCLV")
    // Get constraint level description
    String constraintLevel_description = ""
    if(EXT010.get("EXZCLV") != ""){
      DBAction query = database.table("EXT012").index("00").selection("EXZDES").build()
      DBContainer EXT012 = query.getContainer()
      EXT012.set("EXCONO", currentCompany)
      EXT012.set("EXZCLV",  EXT010.get("EXZCLV"))
      if (query.read(EXT012)) {
        constraintLevel_description = EXT012.get("EXZDES")
      }
    }
    String supplierNumber = EXT010.get("EXSUNO")
    // Get supplier description
    String supplierNumber_description = ""
    if(EXT010.get("EXSUNO") != ""){
      DBAction query = database.table("CIDMAS").index("00").selection("IDSUNM").build()
      DBContainer CIDMAS = query.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO",  EXT010.get("EXSUNO"))
      if (query.read(CIDMAS)) {
        supplierNumber_description = CIDMAS.get("IDSUNM")
      }
    }
    String customerNumber = EXT010.get("EXCUNO")
    // Get customer description
    String customerNumber_description = ""
    if(EXT010.get("EXCUNO") != ""){
      DBAction query = database.table("OCUSMA").index("00").selection("OKCUNM").build()
      DBContainer OCUSMA = query.getContainer()
      OCUSMA.set("OKCONO", currentCompany)
      OCUSMA.set("OKCUNO",  EXT010.get("EXCUNO"))
      if (query.read(OCUSMA)) {
        customerNumber_description = OCUSMA.get("OKCUNM")
      }
    }
    String manufacturer = EXT010.get("EXSUN1")
    // Get manufacturer description
    String manufacturer_description = ""
    if(EXT010.get("EXSUN1") != ""){
      DBAction query = database.table("CIDMAS").index("00").selection("IDSUNM").build()
      DBContainer CIDMAS = query.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO",  EXT010.get("EXSUN1"))
      if (query.read(CIDMAS)) {
        manufacturer_description = CIDMAS.get("IDSUNM")
      }
    }
    String CNUF = EXT010.get("EXSUN3")
    // Get CNUF description
    String CNUF_description = ""
    if(EXT010.get("EXSUN3") != ""){
      DBAction query = database.table("CIDMAS").index("00").selection("IDSUNM").build()
      DBContainer CIDMAS = query.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO",  EXT010.get("EXSUN3"))
      if (query.read(CIDMAS)) {
        CNUF_description = CIDMAS.get("IDSUNM")
      }
    }
    String country = EXT010.get("EXCSCD")
    // Get country description
    String country_description = ""
    if(EXT010.get("EXCSCD") != ""){
      DBAction query = database.table("CSYTAB").index("00").selection("CTTX15").build()
      DBContainer CSYTAB = query.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CSCD")
      CSYTAB.set("CTSTKY", EXT010.get("EXCSCD"))
      if (query.read(CSYTAB)) {
        country_description = CSYTAB.get("CTTX15")
      }
    }
    String HScode = EXT010.get("EXCSNO")
    // Get HS code description
    String HScode_description = ""
    if(EXT010.get("EXCSNO") != ""){
      DBAction query = database.table("CSYCSN").index("00").selection("CKTX15").build()
      DBContainer CSYCSN = query.getContainer()
      CSYCSN.set("CKCONO",currentCompany)
      CSYCSN.set("CKCSNO", EXT010.get("EXCSNO"))
      if (query.read(CSYCSN)) {
        HScode_description = CSYCSN.get("CKTX15")
      }
    }
    String IFLSfield = EXT010.get("EXHIE0")
    // Get IFLS field description
    String IFLSfield_description = ""
    if(EXT010.get("EXHIE0") != ""){
      DBAction query = database.table("MITHRY").index("00").selection("HITX15").build()
      DBContainer MITHRY = query.getContainer()
      MITHRY.set("HICONO",currentCompany)
      MITHRY.set("HIHLVL",  5)
      MITHRY.set("HIHIE0", EXT010.get("EXHIE0"))
      if (query.read(MITHRY)) {
        IFLSfield_description = MITHRY.get("HITX15")
      }
    }
    String mainIngredient = EXT010.get("EXSPE1")
    String contains = EXT010.get("EXSPE2")
    //String GLNcode = EXT010.get("EXSUCM")
    String brand = EXT010.get("EXCFI1")
    // Get brand description
    String brand_description = ""
    if(EXT010.get("EXCFI1") != ""){
      DBAction query = database.table("CSYTAB").index("00").selection("CTTX15").build()
      DBContainer CSYTAB = query.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CFI1")
      CSYTAB.set("CTSTKY", EXT010.get("EXCFI1"))
      if (query.read(CSYTAB)) {
        brand_description = CSYTAB.get("CTTX15")
      }
    }
    String controlCode = EXT010.get("EXCFI4")
    // Get controlCode description
    String controlCode_description = ""
    if(EXT010.get("EXCFI4") != ""){
      DBAction query = database.table("CSYTAB").index("00").selection("CTTX15").build()
      DBContainer CSYTAB = query.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CFI4")
      CSYTAB.set("CTSTKY", EXT010.get("EXCFI4"))
      if (query.read(CSYTAB)) {
        controlCode_description = CSYTAB.get("CTTX15")
      }
    }
    String PNM = EXT010.get("EXCFI5")
    // Get PNM description
    String PNM_description = ""
    if(EXT010.get("EXCFI5") != ""){
      DBAction query = database.table("CSYTAB").index("00").selection("CTTX15").build()
      DBContainer CSYTAB = query.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CFI5")
      CSYTAB.set("CTSTKY", EXT010.get("EXCFI5"))
      if (query.read(CSYTAB)) {
        PNM_description = CSYTAB.get("CTTX15")
      }
    }
    String standardLifetime = EXT010.get("EXZSLT")
    String percentageOfMandatoryLifetime = EXT010.get("EXZPLT")
    String origin = EXT010.get("EXCSC1")
    // Get origin description
    String origin_description = ""
    if(EXT010.get("EXCSC1") != ""){
      DBAction query = database.table("CSYTAB").index("00").selection("CTTX15").build()
      DBContainer CSYTAB = query.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CSCD")
      CSYTAB.set("CTSTKY", EXT010.get("EXCSC1"))
      if (query.read(CSYTAB)) {
        origin_description = CSYTAB.get("CTTX15")
      }
    }
    String orderType = EXT010.get("EXORTP")
    // Get order type description
    String orderType_description = ""
    if(EXT010.get("EXORTP") != ""){
      DBAction query = database.table("OOTYPE").index("00").selection("OOTX15").build()
      DBContainer OOTYPE = query.getContainer()
      OOTYPE.set("OOCONO",currentCompany)
      OOTYPE.set("OOORTP", EXT010.get("EXORTP"))
      if (query.read(OOTYPE)) {
        orderType_description = OOTYPE.get("OOTX15")
      }
    }
    String itemType = EXT010.get("EXITTY")
    // Get item type description
    String itemType_description = ""
    if(EXT010.get("EXITTY") != ""){
      DBAction query = database.table("MITTTY").index("00").selection("TYTX15").build()
      DBContainer MITTTY = query.getContainer()
      MITTTY.set("TYCONO",currentCompany)
      MITTTY.set("TYITTY", EXT010.get("EXITTY"))
      if (query.read(MITTTY)) {
        itemType_description = MITTTY.get("TYTX15")
      }
    }
    String dangerClass = EXT010.get("EXZONU")
    String dangerIndicator = EXT010.get("EXHAZI")
    String potentiallyDangerous = EXT010.get("EXZPDA")
    String warehouse = EXT010.get("EXWHLO")
    // Get warehouse description
    String warehouse_description = ""
    if(EXT010.get("EXWHLO") != ""){
      DBAction query = database.table("MITWHL").index("00").selection("MWWHNM").build()
      DBContainer MITWHL = query.getContainer()
      MITWHL.set("MWCONO",currentCompany)
      MITWHL.set("MWWHLO", EXT010.get("EXWHLO"))
      if (query.read(MITWHL)) {
        warehouse_description = MITWHL.get("MWWHNM")
      }
    }
    String documentID1 = EXT010.get("EXDO01")
    // Get document ID 1 description
    String documentID1_description = ""
    if(EXT010.get("EXDO01") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO01"))
      if (query.read(MPDDOC)) {
        documentID1_description = MPDDOC.get("DODODE")
      }
    }
    String documentID2 = EXT010.get("EXDO02")
    // Get document ID 2 description
    String documentID2_description = ""
    if(EXT010.get("EXDO02") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO02"))
      if (query.read(MPDDOC)) {
        documentID2_description = MPDDOC.get("DODODE")
      }
    }
    String documentID3 = EXT010.get("EXDO03")
    // Get document ID 3 description
    String documentID3_description = ""
    if(EXT010.get("EXDO03") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO03"))
      if (query.read(MPDDOC)) {
        documentID3_description = MPDDOC.get("DODODE")
      }
    }
    String documentID4 = EXT010.get("EXDO04")
    // Get document ID 4 description
    String documentID4_description = ""
    if(EXT010.get("EXDO04") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO04"))
      if (query.read(MPDDOC)) {
        documentID4_description = MPDDOC.get("DODODE")
      }
    }
    String documentID5 = EXT010.get("EXDO05")
    // Get document ID 5 description
    String documentID5_description = ""
    if(EXT010.get("EXDO05") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO05"))
      if (query.read(MPDDOC)) {
        documentID5_description = MPDDOC.get("DODODE")
      }
    }
    String documentID6 = EXT010.get("EXDO06")
    // Get document ID 6 description
    String documentID6_description = ""
    if(EXT010.get("EXDO06") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO06"))
      if (query.read(MPDDOC)) {
        documentID6_description = MPDDOC.get("DODODE")
      }
    }
    String documentID7 = EXT010.get("EXDO07")
    // Get document ID 7 description
    String documentID7_description = ""
    if(EXT010.get("EXDO07") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO07"))
      if (query.read(MPDDOC)) {
        documentID7_description = MPDDOC.get("DODODE")
      }
    }
    String documentID8 = EXT010.get("EXDO08")
    // Get document ID 8 description
    String documentID8_description = ""
    if(EXT010.get("EXDO08") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO08"))
      if (query.read(MPDDOC)) {
        documentID8_description = MPDDOC.get("DODODE")
      }
    }
    String documentID9 = EXT010.get("EXDO09")
    // Get document ID 9 description
    String documentID9_description = ""
    if(EXT010.get("EXDO09") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO09"))
      if (query.read(MPDDOC)) {
        documentID9_description = MPDDOC.get("DODODE")
      }
    }
    String documentID10 = EXT010.get("EXDO10")
    // Get document ID 10 description
    String documentID10_description = ""
    if(EXT010.get("EXDO10") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO10"))
      if (query.read(MPDDOC)) {
        documentID10_description = MPDDOC.get("DODODE")
      }
    }
    String documentID11 = EXT010.get("EXDO11")
    // Get document ID 11 description
    String documentID11_description = ""
    if(EXT010.get("EXDO11") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO11"))
      if (query.read(MPDDOC)) {
        documentID11_description = MPDDOC.get("DODODE")
      }
    }
    String documentID12 = EXT010.get("EXDO12")
    // Get document ID 12 description
    String documentID12_description = ""
    if(EXT010.get("EXDO12") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO12"))
      if (query.read(MPDDOC)) {
        documentID12_description = MPDDOC.get("DODODE")
      }
    }
    String documentID13 = EXT010.get("EXDO13")
    // Get document ID 13 description
    String documentID13_description = ""
    if(EXT010.get("EXDO13") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO13"))
      if (query.read(MPDDOC)) {
        documentID13_description = MPDDOC.get("DODODE")
      }
    }
    String documentID14 = EXT010.get("EXDO14")
    // Get document ID 14 description
    String documentID14_description = ""
    if(EXT010.get("EXDO14") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO14"))
      if (query.read(MPDDOC)) {
        documentID14_description = MPDDOC.get("DODODE")
      }
    }
    String documentID15 = EXT010.get("EXDO15")
    // Get document ID 15 description
    String documentID15_description = ""
    if(EXT010.get("EXDO15") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT010.get("EXDO15"))
      if (query.read(MPDDOC)) {
        documentID15_description = MPDDOC.get("DODODE")
      }
    }
    String txid = EXT010.get("EXTXID")
    String ztps = EXT010.get("EXZTPS")

    String entryDate = EXT010.get("EXRGDT")
    String entryTime = EXT010.get("EXRGTM")
    String changeDate = EXT010.get("EXLMDT")
    String changeNumber = EXT010.get("EXCHNO")
    String changedBy = EXT010.get("EXCHID")
    mi.outData.put("ITNO", itemNumber)
    mi.outData.put("ITDS", itemNumber_description)
    mi.outData.put("ZCFE", constrainingFeature)
    mi.outData.put("ZDE1", constrainingFeature_description)
    mi.outData.put("ZCLV", constraintLevel)
    mi.outData.put("ZDE2", constraintLevel_description)
    mi.outData.put("SUNO", supplierNumber)
    mi.outData.put("SUNM", supplierNumber_description)
    mi.outData.put("CUNO", customerNumber)
    mi.outData.put("CUNM", customerNumber_description)
    mi.outData.put("SUN1", manufacturer)
    mi.outData.put("SNM1", manufacturer_description)
    mi.outData.put("SUN3", CNUF)
    mi.outData.put("SNM3", CNUF_description)
    mi.outData.put("CSCD", country)
    mi.outData.put("ZCSC", country_description)
    mi.outData.put("CSNO", HScode)
    mi.outData.put("ZCSN", HScode_description)
    mi.outData.put("HIE0", IFLSfield)
    mi.outData.put("ZHIE", IFLSfield_description)
    mi.outData.put("SPE1", mainIngredient)
    mi.outData.put("SPE2", contains)
    //mi.outData.put("SUCM", GLNcode)
    mi.outData.put("CFI1", brand)
    mi.outData.put("ZCF1", brand_description)
    mi.outData.put("CFI4", controlCode)
    mi.outData.put("ZCF4", controlCode_description)
    mi.outData.put("CFI5", PNM)
    mi.outData.put("ZCF5", PNM_description)
    mi.outData.put("ZSLT", standardLifetime)
    mi.outData.put("ZPLT", percentageOfMandatoryLifetime)
    mi.outData.put("TXID", txid)
    mi.outData.put("CSC1", origin)
    mi.outData.put("ZCS1", origin_description)
    mi.outData.put("ORTP", orderType)
    mi.outData.put("ZORT", orderType_description)
    mi.outData.put("ITTY", itemType)
    mi.outData.put("ZITT", itemType_description)
    mi.outData.put("ZONU", dangerClass)
    mi.outData.put("HAZI", dangerIndicator)
    mi.outData.put("ZPDA", potentiallyDangerous)
    mi.outData.put("ZTPS", ztps)
    mi.outData.put("WHLO", warehouse)
    mi.outData.put("WHNM", warehouse_description)
    mi.outData.put("DO01", documentID1)
    mi.outData.put("DD01", documentID1_description)
    mi.outData.put("DO02", documentID2)
    mi.outData.put("DD02", documentID2_description)
    mi.outData.put("DO03", documentID3)
    mi.outData.put("DD03", documentID3_description)
    mi.outData.put("DO04", documentID4)
    mi.outData.put("DD04", documentID4_description)
    mi.outData.put("DO05", documentID5)
    mi.outData.put("DD05", documentID5_description)
    mi.outData.put("DO06", documentID6)
    mi.outData.put("DD06", documentID6_description)
    mi.outData.put("DO07", documentID7)
    mi.outData.put("DD07", documentID7_description)
    mi.outData.put("DO08", documentID8)
    mi.outData.put("DD08", documentID8_description)
    mi.outData.put("DO09", documentID9)
    mi.outData.put("DD09", documentID9_description)
    mi.outData.put("DO10", documentID10)
    mi.outData.put("DD10", documentID10_description)
    mi.outData.put("DO11", documentID11)
    mi.outData.put("DD11", documentID11_description)
    mi.outData.put("DO12", documentID12)
    mi.outData.put("DD12", documentID12_description)
    mi.outData.put("DO13", documentID13)
    mi.outData.put("DD13", documentID13_description)
    mi.outData.put("DO14", documentID14)
    mi.outData.put("DD14", documentID14_description)
    mi.outData.put("DO15", documentID15)
    mi.outData.put("DD15", documentID15_description)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
