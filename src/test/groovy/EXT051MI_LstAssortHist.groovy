/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT051MI.LstAssortHist
 * Description : The LstAssortHist transaction list records to the EXT051 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX02 - Add assortment
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class LstAssortHist extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;

  public LstAssortHist(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility) {
    this.mi = mi;
    this.database = database;
    this.logger = logger;
    this.program = program;
    this.utility = utility;
  }

  public void main() {
    Integer currentCompany;
    String cuno = "";
    String ascd = "";
    String dat1 = "";
    String type = "";
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer) program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
    }

    if (mi.in.get("CUNO") != null) {
      DBAction countryQuery = database.table("OCUSMA").index("00").build();
      DBContainer OCUSMA = countryQuery.getContainer();
      OCUSMA.set("OKCONO", currentCompany);
      OCUSMA.set("OKCUNO", mi.in.get("CUNO"));
      if (!countryQuery.read(OCUSMA)) {
        mi.error("Code Client " + mi.in.get("CUNO") + " n'existe pas");
        return;
      }
      cuno = mi.in.get("CUNO");
    }

    if (mi.in.get("ASCD") != null) {
      DBAction countryQuery = database.table("CSYTAB").index("00").build();
      DBContainer CSYTAB = countryQuery.getContainer();
      CSYTAB.set("CTCONO", currentCompany);
      CSYTAB.set("CTSTCO", "ASCD");
      CSYTAB.set("CTSTKY", mi.in.get("ASCD"));
      if (!countryQuery.read(CSYTAB)) {
        mi.error("Code Assortiment  " + mi.in.get("ASCD") + " n'existe pas");
        return;
      }
      ascd = mi.in.get("ASCD");
    }

    if (mi.in.get("DAT1") != null) {
      dat1 = mi.in.get("DAT1");
      if (!utility.call("DateUtil", "isDateValid", dat1, "yyyyMMdd")) {
        mi.error("Format Date de Validité incorrect");
        return;
      }
    }

    if (mi.in.get("TYPE") != null) {
      type = mi.in.get("TYPE");
    }

    //Create Expression
    ExpressionFactory expression = database.getExpressionFactory("EXT051");
    expression = expression.eq("EXCONO", currentCompany.toString());
    if (cuno != "") {
      expression = expression.and(expression.eq("EXCUNO", cuno));
    }
    if (ascd != "") {
      expression = expression.and(expression.eq("EXASCD", ascd));
    }
    if (dat1 != "") {
      expression = expression.and(expression.ge("EXDAT1", dat1));
    }
    if (type != "") {
      expression = expression.and(expression.eq("EXTYPE", type));
    }
    //Run Select
    DBAction query = database.table("EXT051").index("00").matching(expression).selection("EXCONO", "EXASCD", "EXCUNO", "EXDAT1", "EXTYPE", "EXCHB1", "EXDATA", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID", "EXDESI").build();
    DBContainer EXT051 = query.getContainer();
    EXT051.setInt("EXCONO", currentCompany);
    if (!query.readAll(EXT051, 1, outData)) {
      mi.error("L'enregistrement n'existe pas");
      return;
    }
  }
  Closure<?> outData = { DBContainer EXT051 ->
    String cono = EXT051.get("EXCONO");
    String ascd = EXT051.get("EXASCD");
    String cuno = EXT051.get("EXCUNO");
    String dat1 = EXT051.get("EXDAT1");
    String type = EXT051.get("EXTYPE");
    String chb1 = EXT051.get("EXCHB1");
    String data = EXT051.get("EXDATA");

    String entryDate = EXT051.get("EXRGDT");
    String entryTime = EXT051.get("EXRGTM");
    String changeDate = EXT051.get("EXLMDT");
    String changeNumber = EXT051.get("EXCHNO");
    String changedBy = EXT051.get("EXCHID");
    String desi = EXT051.get("EXDESI");

    mi.outData.put("CONO", cono);
    mi.outData.put("CUNO", cuno);
    mi.outData.put("ASCD", ascd);
    mi.outData.put("DAT1", dat1);
    mi.outData.put("TYPE", type);
    mi.outData.put("CHB1", chb1);
    mi.outData.put("DATA", data);
    mi.outData.put("DESI", desi);
    mi.outData.put("RGDT", entryDate);
    mi.outData.put("RGTM", entryTime);
    mi.outData.put("LMDT", changeDate);
    mi.outData.put("CHNO", changeNumber);
    mi.outData.put("CHID", changedBy);
    mi.write();
  }
}
