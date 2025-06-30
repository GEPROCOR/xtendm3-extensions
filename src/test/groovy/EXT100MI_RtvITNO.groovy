/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT100MI.RtvITNO
 * Description : The RtvITNO transaction retrieve list of alias number in MMS025.
 * Date         Changed By   Description
 * 20210510     CDUV         REAX02 Recherche ITM8-EAN13
 * 20211220     RENARN       countMITPOP added and oITNO management changed
 * 20220607     RENARN       countMITPOP removed and oITNO management changed
 * 20220609     RENARN       PUNO management added
 * 20220627     RENARN       If purchase order line is not found then retrieve values from alias number
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.format.DateTimeParseException

public class RtvITNO extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility
  private final MICallerAPI miCaller
  private String MITPOP_ITNO
  private String oITM8
  private String oE0PA
  private String oVFDT
  private Integer oVFDT_NUM
  private String oLVDT
  private Integer oLVDT_NUM
  private String oCNQT
  private String iDWDT
  private Integer iDWDT_NUM
  private String iPUNO
  private String MPLINE_ITNO
  private String dwdt
  private Integer currentCompany

  public RtvITNO(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
  }

  public void main() {
    currentCompany
    dwdt = ""
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    if(mi.in.get("ITM8") == null || mi.in.get("ITM8") == ""){
      mi.error("Code Article 8 est obligatoire")
      return
    }
    if(mi.in.get("DWDT") == null || mi.in.get("DWDT") == ""){
      mi.error("Date Livraison est obligatoire")
      return
    }else {
      dwdt = mi.in.get("DWDT")
      if (!utility.call("DateUtil", "isDateValid", dwdt, "yyyyMMdd")) {
        mi.error("Format Date de Validité incorrect")
        return
      }
    }
    iDWDT = mi.in.get("DWDT")
    iDWDT_NUM = mi.in.get("DWDT") as Integer

    if(mi.in.get("PUNO") == null || mi.in.get("PUNO") == ""){
      iPUNO = ""
    } else {
      iPUNO = mi.in.get("PUNO")
    }

    DBAction query
    query = database.table("MITPOP").index("10").selection("MPPOPN","MPITNO","MPE0PA","MPVFDT","MPLVDT","MPCNQT").build()
    DBContainer MITPOP = query.getContainer()
    MITPOP.set("MPCONO", currentCompany)
    MITPOP.set("MPALWT",  "3" as Integer)
    MITPOP.set("MPALWQ",  "ITM8")
    MITPOP.set("MPPOPN",  mi.in.get("ITM8"))
    if(!query.readAll(MITPOP, 4, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Retrieve MITPOP
  Closure<?> outData = { DBContainer MITPOP ->
    oITM8 = MITPOP.get("MPPOPN")
    MITPOP_ITNO = MITPOP.get("MPITNO")
    oE0PA = MITPOP.get("MPE0PA")
    oVFDT = MITPOP.get("MPVFDT")
    oVFDT_NUM = oVFDT as Integer
    oLVDT = MITPOP.get("MPLVDT")
    oLVDT_NUM = oLVDT as Integer
    oCNQT = MITPOP.get("MPCNQT")

    if (iPUNO.trim() == "") {
      if (oLVDT_NUM == 0) oLVDT_NUM = 99999999
      if (iDWDT_NUM >= oVFDT_NUM && iDWDT_NUM <= oLVDT_NUM) {
        mi.outData.put("ITM8", oITM8)
        mi.outData.put("DWDT", iDWDT)
        mi.outData.put("ITNO", MITPOP_ITNO)
        mi.outData.put("E0PA", oE0PA)
        mi.outData.put("VFDT", oVFDT)
        mi.outData.put("LVDT", oLVDT)
        mi.outData.put("CNQT", oCNQT)
        mi.write()
      }
    } else {
      DBAction query_MPLINE = database.table("MPLINE").index("00").selection("IBITNO").build()
      DBContainer MPLINE = query_MPLINE.getContainer()
      MPLINE.set("IBCONO", currentCompany)
      MPLINE.set("IBPUNO", iPUNO)
      if (!query_MPLINE.readAll(MPLINE, 2, outData_MPLINE)) {
        logger.debug("OA non trouvé")
        if (oLVDT_NUM == 0) oLVDT_NUM = 99999999
        if (iDWDT_NUM >= oVFDT_NUM && iDWDT_NUM <= oLVDT_NUM) {
          mi.outData.put("ITM8", oITM8)
          mi.outData.put("DWDT", iDWDT)
          mi.outData.put("ITNO", MITPOP_ITNO)
          mi.outData.put("E0PA", oE0PA)
          mi.outData.put("VFDT", oVFDT)
          mi.outData.put("LVDT", oLVDT)
          mi.outData.put("CNQT", oCNQT)
          mi.write()
        }
      }
    }
  }
  // Retrieve MPLINE
  Closure<?> outData_MPLINE = { DBContainer MPLINE ->
    MPLINE_ITNO = MPLINE.get("IBITNO")
    if(MPLINE_ITNO == MITPOP_ITNO) {
      mi.outData.put("ITM8", oITM8)
      mi.outData.put("DWDT", iDWDT)
      mi.outData.put("ITNO", MITPOP_ITNO)
      mi.outData.put("E0PA", oE0PA)
      mi.outData.put("VFDT", oVFDT)
      mi.outData.put("LVDT", oLVDT)
      mi.outData.put("CNQT", oCNQT)
      mi.write()
    }
  }
}
