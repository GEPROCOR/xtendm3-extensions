/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT015MI.GetOrderConstr
 * Description : Retrieve records from the EXT015 table.
 * Date         Changed By   Description
 * 20210311     RENARN       QUAX07 - Constraints engine
 * 20220228     RENARN       ZTPS and ROUT has been added
 * 20230904     MAXLEC       NEW FIELD OREF IN EXT015
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class GetOrderConstr extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany

  public GetOrderConstr(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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
    DBAction query = database.table("EXT015").index("00").selection("EXORNO", "EXPONR", "EXPOSX", "EXZCSL", "EXCUNO", "EXITNO", "EXORQT", "EXUNMS", "EXLNAM", "EXORST", "EXZCID", "EXZCTY", "EXZCFE", "EXZCLV", "EXSTAT", "EXROUT", "EXDO01", "EXDO02", "EXDO03", "EXDO04", "EXDO05", "EXDO06", "EXDO07", "EXDO08", "EXDO09", "EXDO10", "EXDO11", "EXDO12", "EXDO13", "EXDO14", "EXDO15", "EXTXI1", "EXZCTR", "EXTXI2", "EXZTPS", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT015 = query.getContainer()
    EXT015.set("EXCONO", currentCompany)
    EXT015.set("EXORNO",  mi.in.get("ORNO"))
    EXT015.set("EXPONR",  mi.in.get("PONR"))
    EXT015.set("EXPOSX",  mi.in.get("POSX"))
    EXT015.set("EXZCSL",  mi.in.get("ZCSL"))
    if(!query.readAll(EXT015, 5, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }

  Closure<?> outData = { DBContainer EXT015 ->
    String orderNumber = EXT015.get("EXORNO")
    String orderLineNumber = EXT015.get("EXPONR")
    String lineSuffix = EXT015.get("EXPOSX")
    String constraintLine = EXT015.get("EXZCSL")

    String customerNumber = EXT015.get("EXCUNO")
    // Get customer description
    String customerNumber_description = ""
    if(EXT015.get("EXCUNO") != ""){
      DBAction query = database.table("OCUSMA").index("00").selection("OKCUNM").build()
      DBContainer OCUSMA = query.getContainer()
      OCUSMA.set("OKCONO", currentCompany)
      OCUSMA.set("OKCUNO",  EXT015.get("EXCUNO"))
      if (query.read(OCUSMA)) {
        customerNumber_description = OCUSMA.get("OKCUNM")
      }
    }

    String itemNumber = EXT015.get("EXITNO")
    // Get item description
    String itemNumber_description = ""
    if(EXT015.get("EXITNO") != ""){
      DBAction query = database.table("MITMAS").index("00").selection("MMITDS").build()
      DBContainer MITMAS = query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO",  EXT015.get("EXITNO"))
      if (query.read(MITMAS)) {
        itemNumber_description = MITMAS.get("MMITDS")
      }
    }

    String orderQuantity = EXT015.get("EXORQT")

    String unit = EXT015.get("EXUNMS")
    // Get unit description
    String unit_description = ""
    if(EXT015.get("EXUNMS") != ""){
      DBAction query = database.table("CSYTAB").index("00").selection("CTTX15").build()
      DBContainer CSYTAB = query.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "UNIT")
      CSYTAB.set("CTSTKY", EXT015.get("EXUNMS"))
      if (query.read(CSYTAB)) {
        unit_description = CSYTAB.get("CTTX15")
      }
    }

    String lineAmount = EXT015.get("EXLNAM")
    String lineStatus = EXT015.get("EXORST")
    String constraintID = EXT015.get("EXZCID")

    String constraintType = EXT015.get("EXZCTY")
    // Get constraint type description
    String constraintType_description = ""
    if(EXT015.get("EXZCTY") != ""){
      DBAction query = database.table("EXT011").index("00").selection("EXTX40").build()
      DBContainer EXT011 = query.getContainer()
      EXT011.set("EXCONO", currentCompany)
      EXT011.set("EXZCTY",  EXT015.get("EXZCTY"))
      if (query.read(EXT011)) {
        constraintType_description = EXT011.get("EXTX40")
      }
    }

    String constrainingFeature = EXT015.get("EXZCFE")
    // Get constraining feature description
    String constrainingFeature_description = ""
    if(EXT015.get("EXZCFE") != ""){
      DBAction query = database.table("EXT013").index("00").selection("EXZDES").build()
      DBContainer EXT013 = query.getContainer()
      EXT013.set("EXCONO", currentCompany)
      EXT013.set("EXZCFE",  EXT015.get("EXZCFE"))
      if (query.read(EXT013)) {
        constrainingFeature_description = EXT013.get("EXZDES")
      }
    }
    String constraintLevel = EXT015.get("EXZCLV")
    // Get constraint level description
    String constraintLevel_description = ""
    if(EXT015.get("EXZCLV") != ""){
      DBAction query = database.table("EXT012").index("00").selection("EXZDES").build()
      DBContainer EXT012 = query.getContainer()
      EXT012.set("EXCONO", currentCompany)
      EXT012.set("EXZCLV",  EXT015.get("EXZCLV"))
      if (query.read(EXT012)) {
        constraintLevel_description = EXT012.get("EXZDES")
      }
    }
    String status = EXT015.get("EXSTAT")
    String documentID1 = EXT015.get("EXDO01")
    // Get document ID 1 description
    String documentID1_description = ""
    if(EXT015.get("EXDO01") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO01"))
      if (query.read(MPDDOC)) {
        documentID1_description = MPDDOC.get("DODODE")
      }
    }
    String documentID2 = EXT015.get("EXDO02")
    // Get document ID 2 description
    String documentID2_description = ""
    if(EXT015.get("EXDO02") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO02"))
      if (query.read(MPDDOC)) {
        documentID2_description = MPDDOC.get("DODODE")
      }
    }
    String documentID3 = EXT015.get("EXDO03")
    // Get document ID 3 description
    String documentID3_description = ""
    if(EXT015.get("EXDO03") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO03"))
      if (query.read(MPDDOC)) {
        documentID3_description = MPDDOC.get("DODODE")
      }
    }
    String documentID4 = EXT015.get("EXDO04")
    // Get document ID 4 description
    String documentID4_description = ""
    if(EXT015.get("EXDO04") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO04"))
      if (query.read(MPDDOC)) {
        documentID4_description = MPDDOC.get("DODODE")
      }
    }
    String documentID5 = EXT015.get("EXDO05")
    // Get document ID 5 description
    String documentID5_description = ""
    if(EXT015.get("EXDO05") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO05"))
      if (query.read(MPDDOC)) {
        documentID5_description = MPDDOC.get("DODODE")
      }
    }
    String documentID6 = EXT015.get("EXDO06")
    // Get document ID 6 description
    String documentID6_description = ""
    if(EXT015.get("EXDO06") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO06"))
      if (query.read(MPDDOC)) {
        documentID6_description = MPDDOC.get("DODODE")
      }
    }
    String documentID7 = EXT015.get("EXDO07")
    // Get document ID 7 description
    String documentID7_description = ""
    if(EXT015.get("EXDO07") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO07"))
      if (query.read(MPDDOC)) {
        documentID7_description = MPDDOC.get("DODODE")
      }
    }
    String documentID8 = EXT015.get("EXDO08")
    // Get document ID 8 description
    String documentID8_description = ""
    if(EXT015.get("EXDO08") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO08"))
      if (query.read(MPDDOC)) {
        documentID8_description = MPDDOC.get("DODODE")
      }
    }
    String documentID9 = EXT015.get("EXDO09")
    // Get document ID 9 description
    String documentID9_description = ""
    if(EXT015.get("EXDO09") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO09"))
      if (query.read(MPDDOC)) {
        documentID9_description = MPDDOC.get("DODODE")
      }
    }
    String documentID10 = EXT015.get("EXDO10")
    // Get document ID 10 description
    String documentID10_description = ""
    if(EXT015.get("EXDO10") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO10"))
      if (query.read(MPDDOC)) {
        documentID10_description = MPDDOC.get("DODODE")
      }
    }
    String documentID11 = EXT015.get("EXDO11")
    // Get document ID 11 description
    String documentID11_description = ""
    if(EXT015.get("EXDO11") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO11"))
      if (query.read(MPDDOC)) {
        documentID11_description = MPDDOC.get("DODODE")
      }
    }
    String documentID12 = EXT015.get("EXDO12")
    // Get document ID 12 description
    String documentID12_description = ""
    if(EXT015.get("EXDO12") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO12"))
      if (query.read(MPDDOC)) {
        documentID12_description = MPDDOC.get("DODODE")
      }
    }
    String documentID13 = EXT015.get("EXDO13")
    // Get document ID 13 description
    String documentID13_description = ""
    if(EXT015.get("EXDO13") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO13"))
      if (query.read(MPDDOC)) {
        documentID13_description = MPDDOC.get("DODODE")
      }
    }
    String documentID14 = EXT015.get("EXDO14")
    // Get document ID 14 description
    String documentID14_description = ""
    if(EXT015.get("EXDO14") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO14"))
      if (query.read(MPDDOC)) {
        documentID14_description = MPDDOC.get("DODODE")
      }
    }
    String documentID15 = EXT015.get("EXDO15")
    // Get document ID 15 description
    String documentID15_description = ""
    if(EXT015.get("EXDO15") != ""){
      DBAction query = database.table("MPDDOC").index("00").selection("DODODE").build()
      DBContainer MPDDOC = query.getContainer()
      MPDDOC.set("DOCONO", currentCompany)
      MPDDOC.set("DODOID",  EXT015.get("EXDO15"))
      if (query.read(MPDDOC)) {
        documentID15_description = MPDDOC.get("DODODE")
      }
    }

    String textID1 = EXT015.get("EXTXI1")
    String constraintTreated = EXT015.get("EXZCTR")
    String textID2 = EXT015.get("EXTXI2")
    String ztps = EXT015.get("EXZTPS")

    String rout = EXT015.get("EXROUT")
    // Get route description
    String route_description = ""
    if(EXT015.get("EXROUT") != "") {
      DBAction query = database.table("DROUTE").index("00").selection("DRTX15").build()
      DBContainer DROUTE = query.getContainer()
      DROUTE.set("DRCONO", currentCompany)
      DROUTE.set("DRROUT", EXT015.get("EXROUT"))
      if (query.read(DROUTE)) {
        route_description = DROUTE.get("DRTX15")
      }
    }

    String entryDate = EXT015.get("EXRGDT")
    String entryTime = EXT015.get("EXRGTM")
    String changeDate = EXT015.get("EXLMDT")
    String changeNumber = EXT015.get("EXCHNO")
    String changedBy = EXT015.get("EXCHID")

    String oref = EXT015.get("EXOREF")

    mi.outData.put("CUNO", customerNumber)
    mi.outData.put("OREF", oref)

    mi.outData.put("CUNM", customerNumber_description)
    mi.outData.put("ITNO", itemNumber)
    mi.outData.put("ITDS", itemNumber_description)
    mi.outData.put("ORQT", orderQuantity)
    mi.outData.put("UNMS", unit)
    mi.outData.put("TX15", unit_description)
    mi.outData.put("LNAM", lineAmount)
    mi.outData.put("ORST", lineStatus)
    mi.outData.put("ZCID", constraintID)
    mi.outData.put("ZCTY", constraintType)
    mi.outData.put("TX40", constraintType_description)
    mi.outData.put("ZCFE", constrainingFeature)
    mi.outData.put("ZDE1", constrainingFeature_description)
    mi.outData.put("ZCLV", constraintLevel)
    mi.outData.put("ZDE2", constraintLevel_description)
    mi.outData.put("STAT", status)
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
    mi.outData.put("TXI1", textID1)
    mi.outData.put("ZCTR", constraintTreated)
    mi.outData.put("TXI2", textID2)
    mi.outData.put("ZTPS", ztps)
    mi.outData.put("ROUT", rout)
    mi.outData.put("ZDE3", route_description)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
