/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT022MI.DelSalesPoint
 * Description : Delete records to the EXT022 table.
 * Date         Changed By   Description
 * 20230313     RENARN       CMDX06 - Gestion suppression des points de vente
 */
public class DelSalesPoint extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private final UtilityAPI utility
  private Integer currentCompany

  public DelSalesPoint(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi;
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
    this.miCaller = miCaller
  }

  public void main() {
    currentCompany = (Integer)program.getLDAZD().CONO
    // Check customer
    if(getInParam("CUNO").isEmpty()){
      mi.error("Code client est obligatoire!")
      return
    }

    // Remove value from EXT022
    DBAction action = database.table("EXT022").index("00").build()
    DBContainer EXT022 = action.createContainer()
    EXT022.set("EXCONO", currentCompany)
    EXT022.set("EXCUNO", getInParam("CUNO"))
    EXT022.set("EXADID", getInParam("ADID"))
    EXT022.set("EXZCFE", getInParam("ZCFE"))
    int lfNb = !getInParam("ZCFE").isEmpty()?4:3
    if (!action.readAllLock(EXT022, lfNb,deleteCallBack)) {
      mi.error(String.format("Point de vente n'existe pas pour ce client/adresse/contrainte (Clé %s) !", String.valueOf(lfNb)))
      return
    }
  }
  Closure<?> deleteCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }

  /**
   * GetInParam - Get input parameter
   **/
  public String getInParam(String name) {
    logger.debug(String.format("Get input parameter %s : value %s", (String)name, mi.in.get(name)))
    if (mi.in.get(name)==null) {
      logger.debug(String.format("return empty cause parameter %s is null", name))
      return ""
    } else {
      logger.debug(String.format("return value %s", ((String)mi.in.get(name)).trim()))
      return ((String)mi.in.get(name)).trim()
    }
  }
}
