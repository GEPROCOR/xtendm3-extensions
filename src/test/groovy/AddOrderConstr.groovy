/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT015MI.AddOrderConstr
 * Description : Add records to the EXT015 table.
 * Date         Changed By   Description
 * 20210311     RENARN       QUAX07 - Constraints engine
 * 20220228     RENARN       ZTPS has been added
 * 20230904     MAXLEC       NEW FIELD OREF IN EXT015
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddOrderConstr extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private final UtilityAPI utility
  private String NBNR

  public AddOrderConstr(MIAPI mi, DatabaseAPI database, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.miCaller = miCaller
    this.utility = utility
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT015").index("00").build()
    String orderNumber = mi.in.get("ORNO")
    Integer orderNumberLine = mi.in.get("PONR")
    Integer orderLineSuffix = mi.in.get("POSX")
    Integer constraintLine = mi.in.get("ZCSL")
    DBContainer EXT015 = query.getContainer()
    EXT015.set("EXCONO", currentCompany)
    EXT015.set("EXORNO", orderNumber)
    EXT015.set("EXPONR", orderNumberLine)
    EXT015.set("EXPOSX", orderLineSuffix)
    EXT015.set("EXZCSL", constraintLine)
    if (!query.read(EXT015)) {
      String oref = mi.in.get("OREF")
      EXT015.set("EXOREF", oref)
      String customerNumber = mi.in.get("CUNO")
      EXT015.set("EXCUNO", customerNumber)
      String itemNumber = mi.in.get("ITNO")
      EXT015.set("EXITNO", itemNumber)
      Double orderQuantity = mi.in.get("ORQT")
      EXT015.set("EXORQT", orderQuantity)
      String unit = mi.in.get("UNMS")
      EXT015.set("EXUNMS", unit)
      Double lineAmount = mi.in.get("LNAM")
      EXT015.set("EXLNAM", lineAmount)
      String lineStatus = mi.in.get("ORST")
      EXT015.set("EXORST", lineStatus)
      Integer constraintID = mi.in.get("ZCID")
      EXT015.set("EXZCID", constraintID)
      String constrainingType = mi.in.get("ZCTY")
      EXT015.set("EXZCTY", constrainingType)
      String constrainingFeature = mi.in.get("ZCFE")
      EXT015.set("EXZCFE", constrainingFeature)
      String constraintLevel = mi.in.get("ZCLV")
      EXT015.set("EXZCLV", constraintLevel)
      String status = "20"
      EXT015.set("EXSTAT", status)
      String documentID1 = mi.in.get("DO01")
      EXT015.set("EXDO01", documentID1)
      String documentID2 = mi.in.get("DO02")
      EXT015.set("EXDO02", documentID2)
      String documentID3 = mi.in.get("DO03")
      EXT015.set("EXDO03", documentID3)
      String documentID4 = mi.in.get("DO04")
      EXT015.set("EXDO04", documentID4)
      String documentID5 = mi.in.get("DO05")
      EXT015.set("EXDO05", documentID5)
      String documentID6 = mi.in.get("DO06")
      EXT015.set("EXDO06", documentID6)
      String documentID7 = mi.in.get("DO07")
      EXT015.set("EXDO07", documentID7)
      String documentID8 = mi.in.get("DO08")
      EXT015.set("EXDO08", documentID8)
      String documentID9 = mi.in.get("DO09")
      EXT015.set("EXDO09", documentID9)
      String documentID10 = mi.in.get("DO10")
      EXT015.set("EXDO10", documentID10)
      String documentID11 = mi.in.get("DO11")
      EXT015.set("EXDO11", documentID11)
      String documentID12 = mi.in.get("DO12")
      EXT015.set("EXDO12", documentID12)
      String documentID13 = mi.in.get("DO13")
      EXT015.set("EXDO13", documentID13)
      String documentID14 = mi.in.get("DO14")
      EXT015.set("EXDO14", documentID14)
      String documentID15 = mi.in.get("DO15")
      EXT015.set("EXDO15", documentID15)
      Integer textID1 = mi.in.get("TXI1")
      EXT015.set("EXTXI1", textID1)
      Integer constraintTreated = mi.in.get("ZCTR")
      EXT015.set("EXZCTR", constraintTreated)
      Integer textID2 = mi.in.get("TXI2")
      EXT015.set("EXTXI2", textID2)
      Integer ztps = mi.in.get("ZTPS")
      EXT015.set("EXZTPS", ztps)
      EXT015.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT015.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT015.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT015.setInt("EXCHNO", 1)
      EXT015.set("EXCHID", program.getUser())
      query.insert(EXT015)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
}
