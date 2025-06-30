/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT014MI.LstGradient
 * Description : List records from the EXT014 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
public class LstGradient extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program

  public LstGradient(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi;
    this.database = database
    this.logger = logger
    this.program = program
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    if (mi.in.get("CSCD") == null) {
      DBAction query = database.table("EXT014").index("00").selection("EXCSCD", "EXZMIR", "EXZMAR", "EXZGRA", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT014 = query.getContainer()
      EXT014.set("EXCONO", currentCompany)
      if(!query.readAll(EXT014, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    } else {
      String country = mi.in.get("CSCD")
      ExpressionFactory expression = database.getExpressionFactory("EXT014")
      expression = expression.ge("EXCSCD", country)
      DBAction query = database.table("EXT014").index("00").matching(expression).selection("EXCSCD", "EXZMIR", "EXZMAR", "EXZGRA", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT014 = query.getContainer()
      EXT014.set("EXCONO", currentCompany)
      if(!query.readAll(EXT014, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }

  Closure<?> outData = { DBContainer EXT014 ->
    int currentCompany = (Integer)program.getLDAZD().CONO
    String country = EXT014.get("EXCSCD")
    // Get country description
    String countryDescription = ""
    DBAction countryQuery = database.table("CSYTAB").index("00").selection("CTTX40").build()
    DBContainer CSYTAB = countryQuery.getContainer()
    CSYTAB.set("CTCONO", currentCompany)
    CSYTAB.set("CTSTCO", "CSCD")
    CSYTAB.set("CTSTKY", EXT014.get("EXCSCD"))
    if (countryQuery.read(CSYTAB)) {
      logger.debug("EXT014MI_LstGradient CSYTAB/TX40" + CSYTAB.get("CTTX40"))
      countryDescription = CSYTAB.get("CTTX40")
    }

    String minimumRate = EXT014.get("EXZMIR")
    String maximumRate = EXT014.get("EXZMAR")
    String gradient = EXT014.get("EXZGRA")
    String entryDate = EXT014.get("EXRGDT")
    String entryTime = EXT014.get("EXRGTM")
    String changeDate = EXT014.get("EXLMDT")
    String changeNumber = EXT014.get("EXCHNO")
    String changedBy = EXT014.get("EXCHID")
    mi.outData.put("CSCD", country)
    mi.outData.put("TX40", countryDescription)
    mi.outData.put("ZMIR", minimumRate)
    mi.outData.put("ZMAR", maximumRate)
    mi.outData.put("ZGRA", gradient)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
