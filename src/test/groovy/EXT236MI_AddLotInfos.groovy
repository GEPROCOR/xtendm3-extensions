/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT236MI.AddLotInfos
 * Description : Add records to the EXT236 table.
 * Date         Changed By   Description
 * 20210929     RENARN       QUAX24 - Updates Item Lot
 * 20211130     YVOYOU       QUAX24-01 - Updates Item Lot amenagment
 * 20230110     YVOYOU       QUAX24-02 - Updates Item Lot amenagment - No Controle SUCL
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class AddLotInfos extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private final UtilityAPI utility;

  public AddLotInfos(MIAPI mi, DatabaseAPI database, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.utility = utility;
    this.logger = logger
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    if (mi.in.get("ITNO") == null) {
      mi.error("Code article est obligatoire")
      return;
    } else {
      DBAction query = database.table("MITMAS").index("00").build()
      DBContainer MITMAS = query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO", mi.in.get("ITNO"))
      if(!query.read(MITMAS)){
        mi.error("Code article " + mi.in.get("ITNO") + " n'existe pas")
        return
      }
    }
    if (mi.in.get("EBAN") == null) {
      mi.error("Référence lot externe est obligatoire")
      return;
    }
    if (mi.in.get("PROD") != null) {
      ExpressionFactory expression = database.getExpressionFactory("CIDVEN")
      //expression = expression.eq("IISUCL", "500")
      //expression = expression.eq("IISUCL", "200")
      //DBAction query = database.table("CIDVEN").index("00").matching(expression).build()
      DBAction query = database.table("CIDVEN").index("00").build()
      DBContainer CIDVEN = query.getContainer()
      CIDVEN.set("IICONO", currentCompany)
      CIDVEN.set("IISUNO",  mi.in.get("PROD"))
      if (!query.read(CIDVEN)) {
        mi.error("Fabricant " + mi.in.get("PROD") + " n'existe pas")
        return
      }
    }
    String expi = "0"
    if (mi.in.get("EXPI") != null && mi.in.get("EXPI") != "0" && mi.in.get("EXPI") != "") {
      expi = mi.in.get("EXPI");
      if (!utility.call("DateUtil", "isDateValid", expi, "yyyyMMdd")) {
        mi.error("Date d'expiration " + mi.in.get("EXPI") + " est incorrecte")
        return;
      }
    }
    String mfdt = "0"
    if (mi.in.get("MFDT") != null && mi.in.get("MFDT") != "0" && mi.in.get("MFDT") != "") {
      mfdt = mi.in.get("MFDT");
      if (!utility.call("DateUtil", "isDateValid", mfdt, "yyyyMMdd")) {
        mi.error("Date de fabrication " + mi.in.get("MFDT") + " est incorrecte")
        return;
      }
    }

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT236").index("00").build()
    DBContainer EXT236 = query.getContainer()
    EXT236.set("EXCONO", currentCompany)
    EXT236.set("EXITNO",  mi.in.get("ITNO"))
    EXT236.set("EXEBAN",  mi.in.get("EBAN"))
    if (!query.read(EXT236)) {
      EXT236.set("EXITM8", mi.in.get("ITM8"))
      EXT236.set("EXPROD", mi.in.get("PROD"))
      EXT236.set("EXA030", mi.in.get("A030"))
      EXT236.set("EXA730", mi.in.get("A730"))
      EXT236.set("EXBREF", mi.in.get("BREF"))
      EXT236.setInt("EXEXPI", expi as Integer);
      EXT236.setInt("EXMFDT", mfdt as Integer);
      EXT236.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT236.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT236.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT236.setInt("EXCHNO", 1)
      EXT236.set("EXCHID", program.getUser())
      query.insert(EXT236)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
}
