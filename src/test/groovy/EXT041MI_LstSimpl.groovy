/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT041MI.LstSimpl
 * Description : The LstSimpl transaction list records to the EXT041 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX12 - Margin management
 */
public class LstSimpl extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;

  public LstSimpl(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi;
    this.database = database;
    this.logger = logger;
    this.program = program;
  }

  public void main() {
    //Load Current Company
    Integer currentCompany;
    String type = '';
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
    }
    if(mi.in.get("TYPE") != null){
      type = mi.in.get("TYPE");
    }else{
      mi.error("Type est obligatoire");
      return;
    }
    //Load Parameters
    String cuno = '';
    if(mi.in.get("CUNO")!=null){
      cuno=mi.in.get("CUNO");
    }
    //Create Expression
    ExpressionFactory expression = database.getExpressionFactory("EXT041");
    expression = expression.ge("EXCUNO", cuno);

    //Run Select
    DBAction query = database.table("EXT041").index("00").matching(expression).selection("EXCONO", "EXCUNO","EXCUNM", "EXBOBE", "EXBOHE","EXBOBM" ,"EXBOHM" , "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build();
    DBContainer EXT041 = query.getContainer();
    EXT041.setInt("EXCONO",currentCompany);
    EXT041.set("EXTYPE",type);
    //Lst Simple with EXT041
    if(!query.readAll(EXT041, 2, outData)){
      mi.error("L'enregistrement n'existe pas");
      return;
    }

  }
  //Lst Simple with EXT041
  Closure<?> outData = { DBContainer EXT041 ->
    String cono = EXT041.get("EXCONO");
    String cuno = EXT041.get("EXCUNO");
    String cunm = EXT041.get("EXCUNM");
    String type = EXT041.get("EXTYPE");
    String bobe = EXT041.get("EXBOBE");
    String bohe = EXT041.get("EXBOHE");
    String bobm = EXT041.get("EXBOBM");
    String bohm = EXT041.get("EXBOHM");
    String entryDate = EXT041.get("EXRGDT");
    String entryTime = EXT041.get("EXRGTM");
    String changeDate = EXT041.get("EXLMDT");
    String changeNumber = EXT041.get("EXCHNO");
    String changedBy = EXT041.get("EXCHID");

    mi.outData.put("CONO", cono);
    mi.outData.put("CUNO", cuno);
    mi.outData.put("CUNM", cunm);
    mi.outData.put("TYPE", type);
    mi.outData.put("BOBE", bobe);
    mi.outData.put("BOHE", bohe);
    mi.outData.put("BOBM", bobm);
    mi.outData.put("BOHM", bohm);
    mi.outData.put("RGDT", entryDate);
    mi.outData.put("RGTM", entryTime);
    mi.outData.put("LMDT", changeDate);
    mi.outData.put("CHNO", changeNumber);
    mi.outData.put("CHID", changedBy);
    mi.write();
  }
}
