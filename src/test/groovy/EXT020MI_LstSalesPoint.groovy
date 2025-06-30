/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT020MI.LstSalesPoint
 * Description : The LstSalesPoint transaction list records to the EXT020 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX06 - Gestion des points de vente
 */
public class LstSalesPoint extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program

  public LstSalesPoint(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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
    if (mi.inData.get("FPVT").trim().isEmpty()) {
      DBAction action = database.table("EXT020").index("00").selection("EXCONO", "EXTYPE", "EXFPVT", "EXTPVT", "EXWHTY", "EXTX40", "EXCOPE", "EXSCAT", "EXLSCA", "EXCUNO", "EXCUNM", "EXCDAN", "EXOTYG", "EXSTAT", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT020 = action.createContainer()
      EXT020.set("EXCONO", currentCompany)
      if (mi.inData.get("TYPE").trim().isEmpty()) {
        action.readAll(EXT020, 1, releaseExtends)
      }else {
        EXT020.set("EXTYPE", mi.inData.get("TYPE"))
        action.readAll(EXT020, 2, releaseExtends)
      }
    }else{
      DBAction action = database.table("EXT020").index("10").selection("EXCONO", "EXTYPE", "EXFPVT", "EXTPVT", "EXWHTY", "EXTX40", "EXCOPE", "EXSCAT", "EXLSCA", "EXCUNO", "EXCUNM", "EXCDAN", "EXOTYG", "EXSTAT", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT020 = action.createContainer()
      EXT020.set("EXCONO", currentCompany)
      if (mi.inData.get("FPVT").trim().isEmpty()) {
        action.readAll(EXT020, 1, releaseExtends)
      }else {
        EXT020.setInt("EXFPVT", mi.inData.get("FPVT") as Integer);
        action.readAll(EXT020, 2, releaseExtends)
      }
    }
  }

  Closure<?> releaseExtends = { DBContainer EXT020 ->
    //logger.debug("XtendM3 EXT001MI: apres lecture ITNO : ${EXT020.get("EXCONO")}")
    //logger.debug("XtendM3 EXT001MI: apres lecture CUS1 : ${EXT001.get("EXCUS1")}")
    mi.outData.put("CONO", EXT020.get("EXCONO").toString())
    mi.outData.put("TYPE", EXT020.get("EXTYPE").toString())
    mi.outData.put("FPVT", EXT020.get("EXFPVT").toString())
    mi.outData.put("TPVT", EXT020.get("EXTPVT").toString())
    mi.outData.put("WHTY", EXT020.get("EXWHTY").toString())
    mi.outData.put("TX40", EXT020.get("EXTX40").toString())
    mi.outData.put("COPE", EXT020.get("EXCOPE").toString())
    mi.outData.put("SCAT", EXT020.get("EXSCAT").toString())
    mi.outData.put("LSCA", EXT020.get("EXLSCA").toString())
    mi.outData.put("CUNO", EXT020.get("EXCUNO").toString())
    mi.outData.put("CUNM", EXT020.get("EXCUNM").toString())
    mi.outData.put("CDAN", EXT020.get("EXCDAN").toString())
    mi.outData.put("OTYG", EXT020.get("EXOTYG").toString())
    mi.outData.put("STAT", EXT020.get("EXSTAT").toString())
    mi.write()
  }
}
