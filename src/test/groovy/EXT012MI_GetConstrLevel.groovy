/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT012MI.GetConstrLevel
 * Description : Retrieve records from the EXT012 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
public class GetConstrLevel extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program

  public GetConstrLevel(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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
    DBAction query = database.table("EXT012").index("00").selection("EXCONO", "EXZDES", "EXZCTY", "EXUSID", "EXZBLC", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT012 = query.getContainer()
    EXT012.set("EXCONO",currentCompany)
    EXT012.set("EXZCLV",  mi.in.get("ZCLV"))
    if(!query.readAll(EXT012, 2, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }

  Closure<?> outData = { DBContainer EXT012 ->
    String description = EXT012.get("EXZDES")

    String constraintType = EXT012.get("EXZCTY")
    // Get constraint type description
    String constraintTypeDescription = ""
    if(EXT012.get("EXZCTY") != ""){
      DBAction constraintTypeQuery = database.table("EXT011").index("00").selection ("EXTX40").build()
      DBContainer EXT011 = constraintTypeQuery.getContainer()
      EXT011.set("EXCONO", EXT012.get("EXCONO"))
      EXT011.set("EXZCTY",  EXT012.get("EXZCTY"))
      if (constraintTypeQuery.read(EXT011)) {
        constraintTypeDescription = EXT011.get("EXTX40")
      }
    }

    String user = EXT012.get("EXUSID")
    // Get user name
    String userName = ""
    if(EXT012.get("EXUSID") != ""){
      DBAction userQuery = database.table("CMNUSR").index("00").selection ("JUTX40").build()
      DBContainer CMNUSR = userQuery.getContainer()
      CMNUSR.set("JUUSID", EXT012.get("EXUSID"))
      if (userQuery.read(CMNUSR)) {
        userName = CMNUSR.get("JUTX40")
      }
    }

    String blocking = EXT012.get("EXZBLC")
    String entryDate = EXT012.get("EXRGDT")
    String entryTime = EXT012.get("EXRGTM")
    String changeDate = EXT012.get("EXLMDT")
    String changeNumber = EXT012.get("EXCHNO")
    String changedBy = EXT012.get("EXCHID")
    mi.outData.put("ZDES", description)
    mi.outData.put("ZCTY", constraintType)
    mi.outData.put("TX41", constraintTypeDescription)
    mi.outData.put("USID", user)
    mi.outData.put("TX42", userName)
    mi.outData.put("ZBLC", blocking)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
