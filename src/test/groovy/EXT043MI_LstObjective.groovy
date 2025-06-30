/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT043MI.LstObjective
 * Description : The LstObjective transaction list records to the EXT043 table.
 * Date         Changed By   Description
 * 20230713     APACE        TARX12 - Margin management
 * 20240620     ARENARD      Different fixes
 */
public class LstObjective extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private Integer nbMaxRecord = 10000

  public LstObjective(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
  }

  public void main() {
    //Load Current Company
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    //Load Parameters
    String buar = ''
    if(mi.in.get("BUAR")!=null){
      buar=mi.in.get("BUAR")
    }
    String fdat = ''
    if(mi.in.get("FDAT")!=null){
      fdat=mi.in.get("FDAT")
    }
    String tdat = ''
    if(mi.in.get("TDAT")!=null){
      tdat=mi.in.get("TDAT")
    }

    //Create Expression
    ExpressionFactory expression = database.getExpressionFactory("EXT043")
    expression = expression.eq("EXCONO", currentCompany.toString())
    if(fdat!="") {
      expression = expression.and(expression.ge("EXFDAT", fdat))
    }
    if(tdat!="") {
      expression = expression.and(expression.le("EXTDAT", tdat))
    }
    //Run Select
    DBAction query = database.table("EXT043").index("00").matching(expression).selection("EXCONO", "EXBUAR", "EXMARG","EXFDAT" ,"EXTDAT" , "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()

    DBContainer EXT043 = query.getContainer()
    EXT043.setInt("EXCONO",currentCompany)

    if(!query.readAll(EXT043, 1, nbMaxRecord, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  //Lst Objective with EXT043
  Closure<?> outData = { DBContainer EXT043 ->
    String cono = EXT043.get("EXCONO")
    String buar = EXT043.get("EXBUAR")
    String marg = EXT043.get("EXMARG")
    String fdat = EXT043.get("EXFDAT")
    String tdat = EXT043.get("EXTDAT")
    String entryDate = EXT043.get("EXRGDT")
    String entryTime = EXT043.get("EXRGTM")
    String changeDate = EXT043.get("EXLMDT")
    String changeNumber = EXT043.get("EXCHNO")
    String changedBy = EXT043.get("EXCHID")

    mi.outData.put("CONO", cono)
    mi.outData.put("BUAR", buar)
    mi.outData.put("MARG", marg)
    mi.outData.put("FDAT", fdat)
    mi.outData.put("TDAT", tdat)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
