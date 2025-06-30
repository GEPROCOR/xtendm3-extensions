/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT010MI.CpyConstraint
 * Description : Copy records to the EXT010 table.
 * Date         Changed By   Description
 * 20210219     RENARN       QUAX01 - Constraints matrix
 * 20211102     RENARN       HAZI and ZPDA have been added
 * 20220228     RENARN       ZTPS, WHLO has been added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class CpyConstraint extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private String NBNR

  public CpyConstraint(MIAPI mi, DatabaseAPI database, ProgramAPI program, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.program = program
    this.miCaller = miCaller
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT010").index("00").selection("EXITNO", "EXZCFE", "EXZCLV", "EXSUNO", "EXCUNO", "EXSUN1", "EXSUN3", "EXCSCD", "EXCSNO", "EXHIE0", "EXSPE1", "EXSPE2",/**"EXSUCM",**/ "EXCFI1", "EXCFI4", "EXCFI5", "EXZSLT", "EXZPLT", "EXCSC1", "EXZONU", "EXORTP", "EXITTY", "EXHAZI", "EXZPDA", "EXZGKY", "EXZTPS", "EXWHLO", "EXDO01", "EXDO02", "EXDO03", "EXDO04", "EXDO05", "EXDO06", "EXDO07", "EXDO08", "EXDO09", "EXDO10", "EXDO11", "EXDO12", "EXDO13", "EXDO14", "EXDO15", "EXTXID", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT010 = query.getContainer()
    EXT010.set("EXCONO", currentCompany)
    EXT010.set("EXZCID", mi.in.get("ZCID"))
    if(query.read(EXT010)){
      // Retrieve constraint ID
      executeCRS165MIRtvNextNumber("ZA", "A")
      EXT010.set("EXZCID",NBNR as Integer)
      String constraintID = EXT010.get("EXZCID")
      if (!query.read(EXT010)) {
        EXT010.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT010.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        EXT010.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT010.setInt("EXCHNO", 1)
        EXT010.set("EXCHID", program.getUser())
        query.insert(EXT010)
        mi.outData.put("ZCID", constraintID)
        mi.write()
      } else {
        mi.error("L'enregistrement existe déjà")
      }
    } else {
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Execute CRS165MI.RtvNextNumber
  private executeCRS165MIRtvNextNumber(String NBTY, String NBID){
    def parameters = ["NBTY": NBTY, "NBID": NBID]
    Closure<?> handler = { Map<String, String> response ->
      NBNR = response.NBNR.trim()

      if (response.error != null) {
        return mi.error("Failed CRS165MI.RtvNextNumber: "+ response.errorMessage)
      }
    }
    miCaller.call("CRS165MI", "RtvNextNumber", parameters, handler)
  }
}
