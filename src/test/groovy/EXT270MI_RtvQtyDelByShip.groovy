/**
 * README
 * This extension is used by script
 *
 * Name : EXT270MI.RtvQtyDelByShip
 * Description : The RtvQtyDelByShip transaction retrieve sum delivery quantity by shipment
 * Date         Changed By   Description
 * 20231004     YVOYOU       CMDX030 - Create new API
 * 20231106     YVOYOU       CMDX030 - Add selection by plan
 * 20231107     YVOYOU       CMDX030 - return 0 if no record in MHDISH, MPTRNS, MHDISL
 * 20230819     YVOYOU       CMDX55 - Change input parameter PLAN size 10 replace by 30
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.format.DateTimeParseException

public class RtvQtyDelByShip extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility
  private String oDLIX
  private Integer iCONN
  private String iPLAN
  private Long outNumDLIX
  private String oDLQT
  private Double outNumDLQT
  private Integer currentCompany
  private Integer nbRecords

  public RtvQtyDelByShip(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    nbRecords = 1
    iCONN = mi.in.get("CONN")
    iPLAN = mi.in.get("PLAN")

    //Retrieve CONN by PLAN
    if(iCONN == null || iCONN == "") {
      if(mi.in.get("PLAN") != null || mi.in.get("PLAN") != ""){
        ExpressionFactory expression = database.getExpressionFactory("DRADTR")
        expression = expression.eq("DRUDE1", iPLAN)
        DBAction queryDRADTR = database.table("DRADTR").index("00").matching(expression).selection("DRCONO", "DRCONN", "DRTLVL").build()
        DBContainer DRADTR = queryDRADTR.getContainer()
        DRADTR.set("DRCONO", currentCompany)
        DRADTR.set("DRTLVL",  "1" as Integer)
        if(!queryDRADTR.readAll(DRADTR, 2, nbRecords, outDataDRADTR)){
          mi.error("L'enregistrement n'existe pas dans DRADTR")
          return
        }
      }
    }
    if(iCONN == null || iCONN == ""){
      if(mi.in.get("PLAN") == null || mi.in.get("PLAN") == ""){
        mi.error("Expedition est obligatoire")
        return
      }
    }
    nbRecords = 10000
    outNumDLQT = 0d
    DBAction query
    query = database.table("MHDISH").index("20").selection("OQDLIX", "OQDPOL").build()
    DBContainer MHDISH = query.getContainer()
    MHDISH.set("OQCONO", currentCompany)
    MHDISH.set("OQINOU",  "1" as Integer)
    MHDISH.set("OQCONN",  iCONN)
    if(!query.readAll(MHDISH, 3, nbRecords, outData)){
      outNumDLQT = 0d
    }
    mi.outData.put("DLQT", String.valueOf(outNumDLQT));
    mi.write()
  }

  // Retrieve DRADTR
  Closure<?> outDataDRADTR = { DBContainer DRADTR ->
    iCONN = DRADTR.get("DRCONN")
  }

  // Retrieve MHDISH
  Closure<?> outData = { DBContainer MHDISH ->
    oDLIX = MHDISH.get("OQDLIX")
    outNumDLIX = oDLIX as Long
    if (MHDISH.get("OQDPOL") != "31A") {
      DBAction query_MPTRNS = database.table("MPTRNS").index("03").selection("ORDLQT").build()
      DBContainer MPTRNS = query_MPTRNS.getContainer()
      MPTRNS.set("ORCONO", currentCompany)
      MPTRNS.set("ORDLIX", outNumDLIX)
      if (!query_MPTRNS.readAll(MPTRNS, 2, nbRecords, outDataMPTRNS)) {
        outNumDLQT = 0
      }
    }else{
      DBAction query_MHDISL = database.table("MHDISL").index("00").selection("URTRQT").build()
      DBContainer MHDISL = query_MHDISL.getContainer()
      MHDISL.set("URCONO", currentCompany)
      MHDISL.set("URDLIX", outNumDLIX)
      if (!query_MHDISL.readAll(MHDISL, 2, nbRecords, outDataMHDISL)) {
        outNumDLQT = 0
      }
    }

  }
  // Retrieve MHDISL
  Closure<?> outDataMHDISL = { DBContainer MHDISL ->
    oDLQT = MHDISL.get("URTRQT")
    outNumDLQT += Double.valueOf(oDLQT)
  }
  // Retrieve MPTRNS
  Closure<?> outDataMPTRNS = { DBContainer MPTRNS ->
    oDLQT = MPTRNS.get("ORDLQT")
    outNumDLQT += Double.valueOf(oDLQT)
  }
}
