/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT021MI.DelFinalSalesPt
 * Description : The LstFinalSalesPt transaction list records to the EXT021 table.
 * Date         Changed By   Description
 * 20211004     APACE         CMDX06 - Gestion des points de vente
 */
public class DelFinalSalesPt extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private final UtilityAPI utility;

  public DelFinalSalesPt(MIAPI mi, DatabaseAPI database, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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
    if (mi.in.get("TYPE") == null) {
      mi.error("Type point de vente est obligatoire");
      return;
    }
    Integer npvt = 0
    if (mi.in.get("NPVT") == null) {
      mi.error("Numéro point de vente est obligatoire");
      return;
    } else {
      npvt = mi.in.get("NPVT")
    }
    if (mi.in.get("BFRS") == null) {
      mi.error("Base fournisseur est obligatoire");
      return;
    }
    String fdat = ""
    if (mi.in.get("FDAT") == null) {
      mi.error("Date de début est obligatoire");
      return;
    } else {
      fdat = mi.in.get("FDAT");
      if (!utility.call("DateUtil", "isDateValid", fdat, "yyyyMMdd")) {
        mi.error("Date de début est incorrecte");
        return;
      }
    }
    String tdat = ""
    if (mi.in.get("TDAT") == null) {
      mi.error("Date de fin est obligatoire");
      return;
    } else {
      tdat = mi.in.get("TDAT");
      if (!utility.call("DateUtil", "isDateValid", tdat, "yyyyMMdd")) {
        mi.error("Date de fin est incorrecte");
        return;
      }
    }

    DBAction query = database.table("EXT021").index("00").build()
    DBContainer EXT021 = query.getContainer()
    EXT021.set("EXCONO", currentCompany)
    EXT021.set("EXTYPE",  mi.in.get("TYPE"))
    EXT021.set("EXNPVT",  npvt)
    EXT021.set("EXBFRS",  mi.in.get("BFRS"))
    EXT021.setInt("EXFDAT", fdat as Integer);
    EXT021.setInt("EXTDAT", tdat as Integer);
    if (!query.readLock(EXT021,updateCallBack)) {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
