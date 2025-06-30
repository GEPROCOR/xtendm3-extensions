/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT013MI.GetConstrFeat
 * Description : Retrieve records from the EXT013 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 * 20220211     RENARN       ZRGP added
 */
public class GetConstrFeat extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program

  public GetConstrFeat(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi
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
    DBAction query = database.table("EXT013").index("00").selection("EXZDES", "EXZRGP", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT013 = query.getContainer()
    EXT013.set("EXCONO", currentCompany)
    EXT013.set("EXZCFE",  mi.in.get("ZCFE"))
    if(!query.readAll(EXT013, 2, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Retrieve EXT013
  Closure<?> outData = { DBContainer EXT013 ->
    String description = EXT013.get("EXZDES")
    String entryDate = EXT013.get("EXRGDT")
    String entryTime = EXT013.get("EXRGTM")
    String changeDate = EXT013.get("EXLMDT")
    String changeNumber = EXT013.get("EXCHNO")
    String changedBy = EXT013.get("EXCHID")
    String groupingCode = EXT013.get("EXZRGP")
    mi.outData.put("ZDES", description)
    mi.outData.put("ZRGP", groupingCode)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
