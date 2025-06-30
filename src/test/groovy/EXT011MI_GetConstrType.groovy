/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT011MI.GetConstrType
 * Description : Retrieve records from the EXT011 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
public class GetConstrType extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program

  public GetConstrType(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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
    DBAction query = database.table("EXT011").index("00").selection("EXTX40", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT011 = query.getContainer()
    EXT011.set("EXCONO", currentCompany)
    EXT011.set("EXZCTY",  mi.in.get("ZCTY"))
    if(!query.readAll(EXT011, 2, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }

  Closure<?> outData = { DBContainer EXT011 ->
    String description = EXT011.get("EXTX40")
    String entryDate = EXT011.get("EXRGDT")
    String entryTime = EXT011.get("EXRGTM")
    String changeDate = EXT011.get("EXLMDT")
    String changeNumber = EXT011.get("EXCHNO")
    String changedBy = EXT011.get("EXCHID")
    mi.outData.put("TX40", description)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
