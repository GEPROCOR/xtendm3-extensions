/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT021MI.LstFinalSalesPt
 * Description : The LstFinalSalesPt transaction list records to the EXT021 table.
 * Date         Changed By   Description
 * 20211004     APACE         CMDX06 - Gestion des points de vente
 */
public class LstFinalSalesPt extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private final UtilityAPI utility;

  public LstFinalSalesPt(MIAPI mi, DatabaseAPI database, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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
    String type="";
    if (mi.in.get("TYPE") != null) {
      type = mi.in.get("TYPE");
    }
    Integer npvt = 0
    if (mi.in.get("NPVT") != null) {
      npvt = mi.in.get("NPVT")
    }
    String bfrs="";
    if (mi.in.get("BFRS") != null) {
      bfrs = mi.in.get("BFRS");
    }
    Integer date = 0;
    if (mi.in.get("DATE") != null) {
      date = mi.in.get("DATE");
    }


    ExpressionFactory expression = database.getExpressionFactory("EXT021");
    Boolean expressionFind = false;

    if(type!=""){
      if(expressionFind){
        expression = expression.and(expression.eq("EXTYPE", type as String));
      }else{
        expressionFind=true;
        expression = expression.eq("EXTYPE", type as String)
      }
    }
    if(npvt!=0){
      if(expressionFind){
        expression = expression.and(expression.eq("EXNPVT", npvt as String));
      }else{
        expressionFind=true;
        expression = expression.eq("EXNPVT", npvt as String);
      }
    }
    if(bfrs!=""){
      if(expressionFind){
        expression = expression.and(expression.eq("EXBFRS", bfrs as String));
      }else{
        expressionFind=true;
        expression = expression.eq("EXBFRS", bfrs as String);
      }
    }
    if(date!=0){
      if(expressionFind){
        expression = expression.and(expression.le("EXFDAT", date as String)).and(expression.ge("EXTDAT", date as String));
      }else{
        expressionFind=true;
        expression = expression.le("EXFDAT", date as String).and(expression.ge("EXTDAT", date as String));
      }
    }

    DBAction query = database.table("EXT021").index("00").matching(expression).selection("EXCONO","EXTYPE","EXNPVT","EXBFRS","EXFDAT","EXTDAT").build()
    DBContainer EXT021 = query.getContainer()
    EXT021.setInt("EXCONO", currentCompany)
    if(!query.readAll(EXT021,1,mi.getMaxRecords(),outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> outData = { DBContainer EXT120result ->
    mi.outData.put("CONO", EXT120result.get("EXCONO") as String)
    mi.outData.put("TYPE", EXT120result.get("EXTYPE") as String)
    mi.outData.put("NPVT", EXT120result.get("EXNPVT") as String)
    mi.outData.put("BFRS", EXT120result.get("EXBFRS") as String)
    mi.outData.put("FDAT", EXT120result.get("EXFDAT") as String)
    mi.outData.put("TDAT", EXT120result.get("EXTDAT") as String)
    mi.write()
  }
}
