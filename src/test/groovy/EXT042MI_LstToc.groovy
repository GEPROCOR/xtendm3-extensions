/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT042MI.LstToc
 * Description : The LstToc transaction list records to the EXT042 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX12 - Margin management
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class LstToc extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility

  public LstToc(MIAPI mi, DatabaseAPI database, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.miCaller = miCaller
    this.utility = utility
  }

  public void main() {
    Integer currentCompany;
    String cuno = '';
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
    }
    if(mi.in.get("CUNO") != null){
      DBAction countryQuery = database.table("OCUSMA").index("00").build();
      DBContainer OCUSMA = countryQuery.getContainer();
      OCUSMA.set("OKCONO",currentCompany);
      OCUSMA.set("OKCUNO",mi.in.get("CUNO"));
      if (!countryQuery.read(OCUSMA)) {
        mi.error("Code Client " + mi.in.get("CUNO") + " n'existe pas");
        return;
      }
      cuno = mi.in.get("CUNO");
    }

    String hie1 = '';
    if(mi.in.get("HIE1") != null){
      hie1 = mi.in.get("HIE1");
    }
    String hie2 = '';
    if(mi.in.get("HIE2") != null){
      hie2 = mi.in.get("HIE2");
    }
    String hie3 = '';
    if(mi.in.get("HIE3") != null){
      hie3 = mi.in.get("HIE3");
    }
    String hie4 = '';
    if(mi.in.get("HIE4") != null){
      hie4 = mi.in.get("HIE4");
    }
    String hie5 = '';
    if(mi.in.get("HIE5") != null){
      hie5 = mi.in.get("HIE5");
    }
    String cfi1 = '';
    if(mi.in.get("CFI1") != null){
      cfi1 = mi.in.get("CFI1");
    }

    Integer fvdt = 0;
    if(mi.in.get("FVDT") != null){
      fvdt = mi.in.get("FVDT");
      if(fvdt != 0){
        if (!utility.call("DateUtil", "isDateValid", fvdt, "yyyyMMdd")) {
          mi.error("Format Date de Début incorrect");
          return;
        }
      }
    }

    LocalDateTime timeOfCreation = LocalDateTime.now();
    ExpressionFactory expression = database.getExpressionFactory("EXT050");
    expression = expression.eq("EXCONO", currentCompany.toString());
    if(cuno!=""){
      expression =  expression.and(expression.eq("EXCUNO", cuno));
    }
    if(hie1!=""){
      expression =  expression.and(expression.eq("EXHIE1", hie1));
    }
    if(hie2!=""){
      expression =  expression.and(expression.eq("EXHIE2", hie2));
    }
    if(hie3!=""){
      expression =  expression.and(expression.eq("EXHIE3", hie3));
    }
    if(hie4!=""){
      expression =  expression.and(expression.eq("EXHIE4", hie4));
    }
    if(hie5!=""){
      expression =  expression.and(expression.eq("EXHIE5", hie5));
    }
    if(cfi1!=""){
      expression =  expression.and(expression.eq("EXCFI1", cfi1));
    }
    if(fvdt!=0){
      expression =  expression.and(expression.ge("EXCFI1", fvdt.toString()));
    }
    DBAction query = database.table("EXT042").index("00").matching(expression).selection("EXCONO","EXCLEF","EXCUNO","EXCUNM","EXHIE1","EXHIE2","EXHIE3","EXHIE4","EXHIE5",
      "EXCFI5","EXPOPN","EXBUAR","EXCFI1","EXTX15","EXADJT","EXFVDT","EXLVDT").build();
    DBContainer EXT042 = query.createContainer();
    EXT042.set("EXCONO", currentCompany);
    //Lst Toc with EXT041
    if(!query.readAll(EXT042, 1, outData)) {
      mi.error("L'enregistrement n'existe pas");
      return;
    }
  }
  //Lst Toc with EXT041
  Closure<?> outData = { DBContainer EXT042 ->
    mi.outData.put("CONO", EXT042.get("EXCONO").toString());
    mi.outData.put("CLEF", EXT042.get("EXCLEF").toString());
    mi.outData.put("CUNO", EXT042.get("EXCUNO").toString());
    mi.outData.put("CUNM", EXT042.get("EXCUNM").toString());
    mi.outData.put("HIE1", EXT042.get("EXHIE1").toString());
    mi.outData.put("HIE2", EXT042.get("EXHIE2").toString());
    mi.outData.put("HIE3", EXT042.get("EXHIE3").toString());
    mi.outData.put("HIE4", EXT042.get("EXHIE4").toString());
    mi.outData.put("HIE5", EXT042.get("EXHIE5").toString());
    mi.outData.put("CFI5", EXT042.get("EXCFI5").toString());
    mi.outData.put("POPN", EXT042.get("EXPOPN").toString());
    mi.outData.put("BUAR", EXT042.get("EXBUAR").toString());
    mi.outData.put("CFI1", EXT042.get("EXCFI1").toString());
    mi.outData.put("TX15", EXT042.get("EXTX15").toString());
    mi.outData.put("ADJT", EXT042.get("EXADJT").toString());
    mi.outData.put("FVDT", EXT042.get("EXFVDT").toString());
    mi.outData.put("LVDT", EXT042.get("EXLVDT").toString());
    mi.write();
  }

}
