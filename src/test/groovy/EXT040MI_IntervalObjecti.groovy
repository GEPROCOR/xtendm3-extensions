/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT040MI.IntervalObjecti
 * Description : The IntervalObjecti transaction control interval date to the EXT040 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX12 - Margin management
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class IntervalObjecti extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public IntervalObjecti(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi;
    this.database = database;
    this.logger = logger;
    this.program = program;
    this.utility = utility;
  }

  public void main() {
    //Load Current Company
    Integer currentCompany;
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
    }

    String fdat = '';
    if(mi.in.get("FDAT") == null){
      mi.error("Date de Début est obligatoire");
      return;
    }else {
      fdat = mi.in.get("FDAT");
      if (!utility.call("DateUtil", "isDateValid", fdat, "yyyyMMdd")) {
        mi.error("Format Date de début incorrect");
        return;
      }
    }

    String tdat = '';
    if(mi.in.get("TDAT") != null){
      tdat = mi.in.get("TDAT");
      if (!utility.call("DateUtil", "isDateValid", tdat, "yyyyMMdd")) {
        mi.error("Format Date de Fin incorrect");
        return;
      }
    }



    LocalDateTime timeOfCreation = LocalDateTime.now();

    ExpressionFactory expression = database.getExpressionFactory("EXT040");
    expression = expression.eq("EXCONO", currentCompany.toString());
    expression = expression.and(expression.le("EXFDAT", fdat));
    if(tdat!=''){
      expression = expression.and(expression.ge("EXTDAT", tdat));
    }

    DBAction query = database.table("EXT040").index("00").matching(expression).selection("EXMARG").build();
    DBContainer EXT040 = query.getContainer();
    EXT040.set("EXCONO", currentCompany);
    EXT040.set("EXCUNO", '');
    EXT040.set("EXASCD", '');
    if(!query.readAll(EXT040, 3, outData)){
      mi.error("L'enregistrement n'existe pas");
      return;
    }
  }
  //Get Interval Date Marge
  Closure<?> outData = { DBContainer EXT040 ->
    String marg = EXT040.get("EXMARG");
    mi.outData.put("MARG", marg);
    mi.write();
  }
}
