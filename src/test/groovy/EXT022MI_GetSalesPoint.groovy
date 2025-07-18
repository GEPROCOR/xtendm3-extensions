/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT022MI.GetSalesPoint
 * Description : Get records to the EXT022 table.
 * Date         Changed By   Description
 * 20230313     RENARN       CMDX06 - Gestion récupération d'un point de vente
 * 20230613     YVOYOU       CMDX06 - ZCFE  is not mandatory
 */
public class GetSalesPoint extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private final UtilityAPI utility
  private Integer currentCompany

  public GetSalesPoint(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
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
    // Check constraint
    if(getInParam("ZCFE").isEmpty()){
      mi.error("Contrainte est obligatoire!")
      return
    }
    DBAction query = database.table("EXT022").index("00").selection("EXCUNO", "EXADID", "EXZCFE", "EXDLSP").build()
    DBContainer EXT022 = query.getContainer()
    EXT022.set("EXCONO", currentCompany)
    EXT022.set("EXCUNO", getInParam("CUNO"))
    EXT022.set("EXADID", getInParam("ADID"))
    EXT022.set("EXZCFE", getInParam("ZCFE"))
    if(!query.readAll(EXT022, 4, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> outData = { DBContainer EXT022 ->
    mi.outData.put("CUNO", (String)EXT022.get("EXCUNO"))
    mi.outData.put("ADID", (String)EXT022.get("EXADID"))
    mi.outData.put("ZCFE", (String)EXT022.get("EXZCFE"))
    mi.outData.put("DLSP", (String)EXT022.get("EXDLSP"))
    mi.write()
  }

  /**
   * GetInParam - Get input parameter
   **/
  public String getInParam(String name) {
    logger.debug(String.format("Get input parameter %s : value %s", name, mi.in.get(name)))
    if (mi.in.get(name)==null) {
      return ""
    } else {
      return ((String)mi.in.get(name)).trim()
    }
  }
}
