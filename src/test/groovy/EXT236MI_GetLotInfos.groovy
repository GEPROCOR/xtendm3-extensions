/**
 * README
 * This extension is used by MEC
 *
 * Name : EXT236MI.GetLotInfos
 * Description : Retrieve records from the EXT236 table.
 * Date         Changed By   Description
 * 20211130     YVOYOU       QUAX24-01 - Manage lot
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class GetLotInfos extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program

  public GetLotInfos(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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

    DBAction query = database.table("EXT236").index("00").selection("EXEXPI", "EXPROD", "EXITM8", "EXMFDT", "EXA030", "EXA730", "EXBREF").build()
    DBContainer EXT236 = query.getContainer()
    EXT236.set("EXCONO", currentCompany)
    EXT236.set("EXITNO",  mi.in.get("ITNO"))
    EXT236.set("EXEBAN",  mi.in.get("EBAN"))
    if(!query.read(EXT236)){
      mi.error("L'enregistrement n'existe pas")
      return
    }else{
      String itno = EXT236.get("EXITNO")
      String eban = EXT236.get("EXEBAN")
      String expi = EXT236.get("EXEXPI")
      String prod = EXT236.get("EXPROD")
      String itm8 = EXT236.get("EXITM8")
      String mfdt = EXT236.get("EXMFDT")
      String a030 = EXT236.get("EXA030")
      String a730 = EXT236.get("EXA730")
      String bref = EXT236.get("EXBREF")
      mi.outData.put("ITNO", itno)
      mi.outData.put("EBAN", eban)
      mi.outData.put("EXPI", expi)
      mi.outData.put("PROD", prod)
      mi.outData.put("ITM8", itm8)
      mi.outData.put("MFDT", mfdt)
      mi.outData.put("A030", a030)
      mi.outData.put("A730", a730)
      mi.outData.put("BREF", bref)
      mi.write()
    }
  }
}
