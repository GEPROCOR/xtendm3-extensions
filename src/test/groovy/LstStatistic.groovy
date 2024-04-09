/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT070MI.LstStatistic
 * Description : The LstStatistic transaction list  records to the EXT070 table. 
 * Date         Changed By   Description
 * 20210510     CDUV         TARX16 - Calcul du tarif
 * 20220204     CDUV         Method comment added, logger removed
 * 20220519     CDUV         lowerCamelCase has been fixed
 */
public class LstStatistic extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private Integer currentCompany

  private final ProgramAPI program
  private final SessionAPI session
  private final TransactionAPI transaction
  private final LoggerAPI logger

  public LstStatistic(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
    this.mi = mi
    this.database = database
    this.program = program
    this.logger = logger
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      mi.error("Company est obligatoire")
      return
    } else {
      currentCompany = mi.in.get("CONO")
    }

    if (mi.in.get("CUNO") == null && mi.in.get("ITNO") == null) {
      DBAction query = database.table("EXT070").index("10").selection("EXCONO","EXCUNO", "EXITNO", "EXSAAM", "EXSAAC", "EXTAUX","EXFLAG", "EXSAAT", "EXIVQT").build()
      DBContainer EXT070 = query.getContainer()
      EXT070.set("EXCONO", currentCompany)
      if(!query.readAll(EXT070, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }else{
      if (mi.in.get("CUNO") != null && mi.in.get("ITNO") != null) {
        DBAction query = database.table("EXT070").index("00").selection("EXCONO","EXCUNO", "EXITNO", "EXSAAM", "EXSAAC", "EXTAUX","EXFLAG", "EXSAAT", "EXIVQT").build()
        DBContainer EXT070 = query.getContainer()
        EXT070.set("EXCONO", currentCompany)
        EXT070.set("EXCUNO", mi.in.get("CUNO"))
        EXT070.set("EXITNO", mi.in.get("ITNO"))
        if(!query.readAll(EXT070, 3, outData)){
          mi.error("L'enregistrement n'existe pas")
          return
        }
      }

      if (mi.in.get("CUNO") != null && mi.in.get("ITNO") == null) {
        DBAction query = database.table("EXT070").index("10").selection("EXCONO","EXCUNO", "EXITNO", "EXSAAM", "EXSAAC", "EXTAUX","EXFLAG", "EXSAAT", "EXIVQT").build()
        DBContainer EXT070 = query.getContainer()
        EXT070.set("EXCONO", currentCompany)
        EXT070.set("EXCUNO", mi.in.get("CUNO"))
        if(!query.readAll(EXT070, 2, outData)){
          mi.error("L'enregistrement n'existe pas")
          return
        }
      }
      if (mi.in.get("CUNO") == null && mi.in.get("ITNO") != null) {
        DBAction query = database.table("EXT070").index("20").selection("EXCONO","EXCUNO", "EXITNO", "EXSAAM", "EXSAAC", "EXTAUX","EXFLAG", "EXSAAT", "EXIVQT").build()
        DBContainer EXT070 = query.getContainer()
        EXT070.set("EXCONO", currentCompany)
        EXT070.set("EXITNO", mi.in.get("ITNO"))
        if(!query.readAll(EXT070, 2, outData)){
          mi.error("L'enregistrement n'existe pas")
          return
        }
      }
    }
  }
  // outData : Retrieve EXT070
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
