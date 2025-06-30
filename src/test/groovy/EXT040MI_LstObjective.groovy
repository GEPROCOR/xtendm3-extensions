/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT040MI.LstObjective
 * Description : The LstObjective transaction list records to the EXT040 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX12 - Margin management
 */
public class LstObjective extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;

  public LstObjective(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi;
    this.database = database;
    this.logger = logger;
    this.program = program;
  }

  public void main() {
    //Load Current Company
    Integer currentCompany;
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
    }

    //Load Parameters
    String cuno = '';
    if(mi.in.get("CUNO")!=null){
      cuno=mi.in.get("CUNO");
    }
    String ascd = '';
    if(mi.in.get("ASCD")!=null){
      ascd=mi.in.get("ASCD");
    }
    String fdat = '';
    if(mi.in.get("FDAT")!=null){
      fdat=mi.in.get("FDAT");
    }
    String tdat = '';
    if(mi.in.get("TDAT")!=null){
      tdat=mi.in.get("TDAT");
    }

    //Create Expression
    ExpressionFactory expression = database.getExpressionFactory("EXT040");
    expression = expression.eq("EXCONO", currentCompany.toString());
    if(cuno!="" && cuno!="*"){
      expression = expression.and(expression.ge("EXCUNO", cuno));
    }
    if(ascd!="" && ascd!="*") {
      expression = expression.and(expression.ge("EXASCD", ascd));
    }
    if(fdat!="") {
      expression = expression.and(expression.ge("EXFDAT", fdat));
    }
    if(tdat!="") {
      expression = expression.and(expression.le("EXTDAT", tdat));
    }
    if(ascd=="*" && cuno=="*"){
      expression = expression.and(expression.eq("EXCUNO", ''));
      expression = expression.and(expression.eq("EXASCD", ''));
    }
    //Run Select
    DBAction query = database.table("EXT040").index("00").matching(expression).selection("EXCONO", "EXCUNO","EXCUNM", "EXASCD", "EXMARG","EXFDAT" ,"EXTDAT" , "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build();

    DBContainer EXT040 = query.getContainer();
    EXT040.setInt("EXCONO",currentCompany);

    if(!query.readAll(EXT040, 1, 10000, outData)){
      mi.error("L'enregistrement n'existe pas");
      return;
    }
  }
  //Lst Objective with EXT040
  Closure<?> outData = { DBContainer EXT040 ->
    String cono = EXT040.get("EXCONO");
    String cuno = EXT040.get("EXCUNO");
    String cunm = EXT040.get("EXCUNM");
    String ascd = EXT040.get("EXASCD");
    String marg = EXT040.get("EXMARG");
    String fdat = EXT040.get("EXFDAT");
    String tdat = EXT040.get("EXTDAT");
    String entryDate = EXT040.get("EXRGDT");
    String entryTime = EXT040.get("EXRGTM");
    String changeDate = EXT040.get("EXLMDT");
    String changeNumber = EXT040.get("EXCHNO");
    String changedBy = EXT040.get("EXCHID");

    mi.outData.put("CONO", cono);
    mi.outData.put("CUNO", cuno);
    mi.outData.put("CUNM", cunm);
    mi.outData.put("ASCD", ascd);
    mi.outData.put("MARG", marg);
    mi.outData.put("FDAT", fdat);
    mi.outData.put("TDAT", tdat);
    mi.outData.put("RGDT", entryDate);
    mi.outData.put("RGTM", entryTime);
    mi.outData.put("LMDT", changeDate);
    mi.outData.put("CHNO", changeNumber);
    mi.outData.put("CHID", changedBy);
    mi.write();
  }
}
