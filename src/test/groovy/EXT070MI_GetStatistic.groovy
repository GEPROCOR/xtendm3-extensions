/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT070MI.GetStatistic
 * Description : The GetStatistic transaction get records to the EXT070 table. 
 * Date         Changed By   Description
 * 20210510     CDUV         TARX16 - Calcul du tarif
 * 20220204     CDUV         Method comment added, semicolons at the end of the line removed
 * 20220519     CDUV         lowerCamelCase has been fixed
 */


public class GetStatistic extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private Integer currentCompany

  public GetStatistic(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.program = program
    this.logger = logger
    this.miCaller = miCaller
  }
  public void main() {
    if (mi.in.get("CONO") == null) {
      mi.error("Company est obligatoire")
      return
    } else {
      currentCompany = mi.in.get("CONO")
    }

    if(mi.in.get("CUNO") == null || mi.in.get("CUNO") == ""){
      mi.error("Code Client est obligatoire")
      return
    }

    if(mi.in.get("ITNO") == null || mi.in.get("ITNO") == ""){
      mi.error("Code Article est obligatoire")
      return
    }
    DBAction query = database.table("EXT070").index("00").selection("EXCONO", "EXCUNO", "EXITNO", "EXSAAM", "EXSAAC", "EXTAUX","EXFLAG", "EXSAAT","EXIVQT").build()
    DBContainer EXT070 = query.getContainer()
    EXT070.set("EXCONO", currentCompany)
    EXT070.set("EXCUNO", mi.in.get("CUNO"))
    EXT070.set("EXITNO", mi.in.get("ITNO"))
    if(!query.readAll(EXT070, 3, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Retrieve EXT070
  Closure<?> outData = { DBContainer EXT070 ->
    String oCONO = EXT070.get("EXCONO")
    String oCUNO = EXT070.get("EXCUNO")
    String oITNO = EXT070.get("EXITNO")
    String oSAAM = EXT070.get("EXSAAM")
    String oSAAC = EXT070.get("EXSAAC")
    String oTAUX = EXT070.get("EXTAUX")
    String oFLAG = EXT070.get("EXFLAG")
    String oSAAT = EXT070.get("EXSAAT")
    String oIVQT = EXT070.get("EXIVQT")
    mi.outData.put("CONO", oCONO)
    mi.outData.put("CUNO", oCUNO)
    mi.outData.put("ITNO", oITNO)
    mi.outData.put("SAAM", oSAAM)
    mi.outData.put("SAAC", oSAAC)
    mi.outData.put("TAUX", oTAUX)
    mi.outData.put("FLAG", oFLAG)
    mi.outData.put("SAAT", oSAAT)
    mi.outData.put("IVQT", oIVQT)
    mi.write()
  }
}
