/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT015MI.LstOrderConstr
 * Description : List records from the EXT015 table.
 * Date         Changed By   Description
 * 20210311     RENARN       QUAX07 - Constraints engine
 * 20220228     RENARN       ZTPS and ROUT has been added
 * 20230803     MAXLEC       OREF has been added
 */
public class LstOrderConstr extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany

  public LstOrderConstr(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi;
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
    if (mi.in.get("ORNO") == null) {
      DBAction query = database.table("EXT015").index("00").selection("EXORNO", "EXPONR", "EXPOSX", "EXZCSL", "EXCUNO", "EXITNO", "EXORQT", "EXUNMS", "EXLNAM", "EXORST", "EXZCID", "EXZCTY", "EXZCFE", "EXZCLV", "EXSTAT", "EXROUT", "EXDO01", "EXDO02", "EXDO03", "EXDO04", "EXDO05", "EXDO06", "EXDO07", "EXDO08", "EXDO09", "EXDO10", "EXDO11", "EXDO12", "EXDO13", "EXDO14", "EXDO15", "EXTXI1", "EXZCTR", "EXTXI2", "EXZTPS", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT015 = query.getContainer()
      EXT015.set("EXCONO", currentCompany)
      if(!query.readAll(EXT015, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    } else {
      String orderNumber = mi.in.get("ORNO")
      String orderLineNumber = mi.in.get("PONR") as Integer
      String lineSuffix = mi.in.get("PONR") as Integer
      ExpressionFactory expression_EXT015 = database.getExpressionFactory("EXT015")
      if (mi.in.get("PONR") != null) {
        expression_EXT015 = expression_EXT015.eq("EXPONR", orderLineNumber)
      }
      DBAction query = database.table("EXT015").index("00").matching(expression_EXT015).selection("EXORNO", "EXPONR", "EXPOSX", "EXZCSL", "EXCUNO", "EXITNO", "EXORQT", "EXUNMS", "EXLNAM", "EXORST", "EXZCID", "EXZCTY", "EXZCFE", "EXZCLV", "EXSTAT", "EXROUT", "EXDO01", "EXDO02", "EXDO03", "EXDO04", "EXDO05", "EXDO06", "EXDO07", "EXDO08", "EXDO09", "EXDO10", "EXDO11", "EXDO12", "EXDO13", "EXDO14", "EXDO15", "EXTXI1", "EXZCTR", "EXTXI2", "EXZTPS", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT015 = query.getContainer()
      EXT015.set("EXCONO", currentCompany)
      EXT015.set("EXORNO", orderNumber)
      if(!query.readAll(EXT015, 2, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }

  Closure<?> outData = { DBContainer EXT015 ->
    String orderNumber = EXT015.get("EXORNO")
    String orderLineNumber = EXT015.get("EXPONR")
    String lineSuffix = EXT015.get("EXPOSX")
    String constraintLine = EXT015.get("EXZCSL")

    String customerNumber = EXT015.get("EXCUNO")
    // Get customer description
    String customerNumberDescription = ""
    if(EXT015.get("EXCUNO") != ""){
      DBAction query = database.table("OCUSMA").index("00").selection("OKCUNM").build()
      DBContainer OCUSMA = query.getContainer()
      OCUSMA.set("OKCONO", currentCompany)
      OCUSMA.set("OKCUNO",  EXT015.get("EXCUNO"))
      if (query.read(OCUSMA)) {
        customerNumberDescription = OCUSMA.get("OKCUNM")
      }
    }

    String itemNumber = EXT015.get("EXITNO")
    // Get item description
    String itemNumberDescription = ""
    if(EXT015.get("EXITNO") != ""){
      DBAction query = database.table("MITMAS").index("00").selection("MMITDS").build()
      DBContainer MITMAS = query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO",  EXT015.get("EXITNO"))
      if (query.read(MITMAS)) {
        itemNumberDescription = MITMAS.get("MMITDS")
      }
    }

    String orderQuantity = EXT015.get("EXORQT")

    String unit = EXT015.get("EXUNMS")
    // Get unit description
    String unitDescription = ""
    if(EXT015.get("EXUNMS") != ""){
      DBAction query = database.table("CSYTAB").index("00").selection("CTTX15").build()
      DBContainer CSYTAB = query.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "UNIT")
      CSYTAB.set("CTSTKY", EXT015.get("EXUNMS"))
      if (query.read(CSYTAB)) {
        unitDescription = CSYTAB.get("CTTX15")
      }
    }

    String lineAmount = EXT015.get("EXLNAM")
    String lineStatus = EXT015.get("EXORST")
    String constraintID = EXT015.get("EXZCID")

    String constraintType = EXT015.get("EXZCTY")
    // Get constraint type description
    String constraintTypeDescription = ""
    if(EXT015.get("EXZCTY") != ""){
      DBAction query = database.table("EXT011").index("00").selection("EXTX40").build()
      DBContainer EXT011 = query.getContainer()
      EXT011.set("EXCONO", currentCompany)
      EXT011.set("EXZCTY",  EXT015.get("EXZCTY"))
      if (query.read(EXT011)) {
        constraintTypeDescription = EXT011.get("EXTX40")
      }
    }

    String constrainingFeature = EXT015.get("EXZCFE")
    // Get constraining feature description
    String constrainingFeatureDescription = ""
    if(EXT015.get("EXZCFE") != ""){
      DBAction query = database.table("EXT013").index("00").selection("EXZDES").build()
      DBContainer EXT013 = query.getContainer()
      EXT013.set("EXCONO", currentCompany)
      EXT013.set("EXZCFE",  EXT015.get("EXZCFE"))
      if (query.read(EXT013)) {
        constrainingFeatureDescription = EXT013.get("EXZDES")
      }
    }
    String constraintLevel = EXT015.get("EXZCLV")
    // Get constraint level description
    String constraintLevelDescription = ""
    if(EXT015.get("EXZCLV") != ""){
      DBAction query = database.table("EXT012").index("00").selection("EXZDES").build()
      DBContainer EXT012 = query.getContainer()
      EXT012.set("EXCONO", currentCompany)
      EXT012.set("EXZCLV",  EXT015.get("EXZCLV"))
      if (query.read(EXT012)) {
        constraintLevelDescription = EXT012.get("EXZDES")
      }
    }
    String status = EXT015.get("EXSTAT")
    String documentId1 = EXT015.get("EXDO01")
    // Get document ID 1 description
    String documentId1Description = ""
    if(EXT015.get("EXDO01") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO01"))
      if (query.read(MPDDOC)) {
        documentId1Description = MPDDOC.get("DODODE")
      }
    }
    String documentID2 = EXT015.get("EXDO02")
    // Get document ID 2 description
    String documentId2Description = ""
    if(EXT015.get("EXDO02") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO02"))
      if (query.read(MPDDOC)) {
        documentId2Description = MPDDOC.get("DODODE")
      }
    }
    String documentId3 = EXT015.get("EXDO03")
    // Get document ID 3 description
    String documentId3Description = ""
    if(EXT015.get("EXDO03") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO03"))
      if (query.read(MPDDOC)) {
        documentId3Description = MPDDOC.get("DODODE")
      }
    }
    String documentId4 = EXT015.get("EXDO04")
    // Get document ID 4 description
    String documentId4Description = ""
    if(EXT015.get("EXDO04") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO04"))
      if (query.read(MPDDOC)) {
        documentId4Description = MPDDOC.get("DODODE")
      }
    }
    String documentId5 = EXT015.get("EXDO05")
    // Get document ID 5 description
    String documentId5Description = ""
    if(EXT015.get("EXDO05") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO05"))
      if (query.read(MPDDOC)) {
        documentId5Description = MPDDOC.get("DODODE")
      }
    }
    String documentId6 = EXT015.get("EXDO06")
    // Get document ID 6 description
    String documentId6Description = ""
    if(EXT015.get("EXDO06") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO06"))
      if (query.read(MPDDOC)) {
        documentId6Description = MPDDOC.get("DODODE")
      }
    }
    String documentId7 = EXT015.get("EXDO07")
    // Get document ID 7 description
    String documentId7Description = ""
    if(EXT015.get("EXDO07") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO07"))
      if (query.read(MPDDOC)) {
        documentId7Description = MPDDOC.get("DODODE")
      }
    }
    String documentId8 = EXT015.get("EXDO08")
    // Get document ID 8 description
    String documentId8Description = ""
    if(EXT015.get("EXDO08") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO08"))
      if (query.read(MPDDOC)) {
        documentId8Description = MPDDOC.get("DODODE")
      }
    }
    String documentId9 = EXT015.get("EXDO09")
    // Get document ID 9 description
    String documentId9Description = ""
    if(EXT015.get("EXDO09") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO09"))
      if (query.read(MPDDOC)) {
        documentId9Description = MPDDOC.get("DODODE")
      }
    }
    String documentId10 = EXT015.get("EXDO10")
    // Get document ID 10 description
    String documentId10Description = ""
    if(EXT015.get("EXDO10") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO10"))
      if (query.read(MPDDOC)) {
        documentId10Description = MPDDOC.get("DODODE")
      }
    }
    String documentId11 = EXT015.get("EXDO11")
    // Get document ID 11 description
    String documentId11Description = ""
    if(EXT015.get("EXDO11") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO11"))
      if (query.read(MPDDOC)) {
        documentId11Description = MPDDOC.get("DODODE")
      }
    }
    String documentId12 = EXT015.get("EXDO12")
    // Get document ID 12 description
    String documentId12Description = ""
    if(EXT015.get("EXDO12") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO12"))
      if (query.read(MPDDOC)) {
        documentId12Description = MPDDOC.get("DODODE")
      }
    }
    String documentId13 = EXT015.get("EXDO13")
    // Get document ID 13 description
    String documentId13Description = ""
    if(EXT015.get("EXDO13") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO13"))
      if (query.read(MPDDOC)) {
        documentId13Description = MPDDOC.get("DODODE")
      }
    }
    String documentId14 = EXT015.get("EXDO14")
    // Get document ID 14 description
    String documentId14Description = ""
    if(EXT015.get("EXDO14") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO14"))
      if (query.read(MPDDOC)) {
        documentId14Description = MPDDOC.get("DODODE")
      }
    }
    String documentId15 = EXT015.get("EXDO15")
    // Get document ID 15 description
    String documentId15Description = ""
    if(EXT015.get("EXDO15") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO15"))
      if (query.read(MPDDOC)) {
        documentId15Description = MPDDOC.get("DODODE")
      }
    }

    String textID1 = EXT015.get("EXTXI1")
    String constraintTreated = EXT015.get("EXZCTR")
    String textID2 = EXT015.get("EXTXI2")
    String ztps = EXT015.get("EXZTPS")

    String rout = EXT015.get("EXROUT")
    // Get route description
    String routeDescription = ""
    if(EXT015.get("EXROUT") != "") {
      DBAction query = database.table("DROUTE").index("00").selection("DRTX15").build()
      DBContainer DROUTE = query.getContainer()
      DROUTE.set("DRCONO", currentCompany)
      DROUTE.set("DRROUT", EXT015.get("EXROUT"))
      if (query.read(DROUTE)) {
        routeDescription = DROUTE.get("DRTX15")
      }
    }

    String entryDate = EXT015.get("EXRGDT")
    String entryTime = EXT015.get("EXRGTM")
    String changeDate = EXT015.get("EXLMDT")
    String changeNumber = EXT015.get("EXCHNO")
    String changedBy = EXT015.get("EXCHID")
    String oref = EXT015.get("EXOREF")

    mi.outData.put("ORNO", orderNumber)
    mi.outData.put("PONR", orderLineNumber)
    mi.outData.put("POSX", lineSuffix)
    mi.outData.put("ZCSL", constraintLine)
    mi.outData.put("CUNO", customerNumber)
    mi.outData.put("CUNM", customerNumberDescription)
    mi.outData.put("ITNO", itemNumber)
    mi.outData.put("ITDS", itemNumberDescription)
    mi.outData.put("ORQT", orderQuantity)
    mi.outData.put("UNMS", unit)
    mi.outData.put("TX15", unitDescription)
    mi.outData.put("LNAM", lineAmount)
    mi.outData.put("ORST", lineStatus)
    mi.outData.put("ZCID", constraintID)
    mi.outData.put("ZCTY", constraintType)
    mi.outData.put("TX40", constraintTypeDescription)
    mi.outData.put("ZCFE", constrainingFeature)
    mi.outData.put("ZDE1", constrainingFeatureDescription)
    mi.outData.put("ZCLV", constraintLevel)
    mi.outData.put("ZDE2", constraintLevelDescription)
    mi.outData.put("STAT", status)
    mi.outData.put("DO01", documentId1)
    mi.outData.put("DD01", documentId1Description)
    mi.outData.put("DO02", documentID2)
    mi.outData.put("DD02", documentId2Description)
    mi.outData.put("DO03", documentId3)
    mi.outData.put("DD03", documentId3Description)
    mi.outData.put("DO04", documentId4)
    mi.outData.put("DD04", documentId4Description)
    mi.outData.put("DO05", documentId5)
    mi.outData.put("DD05", documentId5Description)
    mi.outData.put("DO06", documentId6)
    mi.outData.put("DD06", documentId6Description)
    mi.outData.put("DO07", documentId7)
    mi.outData.put("DD07", documentId7Description)
    mi.outData.put("DO08", documentId8)
    mi.outData.put("DD08", documentId8Description)
    mi.outData.put("DO09", documentId9)
    mi.outData.put("DD09", documentId9Description)
    mi.outData.put("DO10", documentId10)
    mi.outData.put("DD10", documentId10Description)
    mi.outData.put("DO11", documentId11)
    mi.outData.put("DD11", documentId11Description)
    mi.outData.put("DO12", documentId12)
    mi.outData.put("DD12", documentId12Description)
    mi.outData.put("DO13", documentId13)
    mi.outData.put("DD13", documentId13Description)
    mi.outData.put("DO14", documentId14)
    mi.outData.put("DD14", documentId14Description)
    mi.outData.put("DO15", documentId15)
    mi.outData.put("DD15", documentId15Description)
    mi.outData.put("TXI1", textID1)
    mi.outData.put("ZCTR", constraintTreated)
    mi.outData.put("TXI2", textID2)
    mi.outData.put("ZTPS", ztps)
    mi.outData.put("ROUT", rout)
    mi.outData.put("ZDE3", routeDescription)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.outData.put("OREF", oref)
    mi.write()
  }
}
