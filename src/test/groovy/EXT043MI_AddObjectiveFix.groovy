import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddObjective extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility

  private static final Integer DEFAULT_VALUE = 99999999;

  public AddObjective(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
  }

  public void main() {
    //Load Current Company
    Integer currentCompany = mi.in.get("CONO") ?: program.getLDAZD().CONO

    // Validate input fields
    Integer buar = validateInput("BUAR", "Position prix");
    Integer marg = validateInput("MARG", "Marge", "range", -100, 100);
    Integer fdat = validateInput("FDAT", "Date début", "date", "yyyyMMdd");
    Integer tdat = validateInput("TDAT", "Date fin", "date", "yyyyMMdd", DEFAULT_VALUE);

    // Validate date range
    if (fdat > tdat) {
      mi.error("Date de début doit être inférieure à la date de fin");
      return;
    }

    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT043").index("00").build();
    DBContainer EXT043 = query.getContainer();
    EXT043.set("EXCONO", currentCompany);
    EXT043.set("EXBUAR", buar);
    EXT043.setInt("EXFDAT", fdat);

    //Add Objective in EXT043
    if (!query.read(EXT043)) {
      EXT043.setDouble("EXMARG", marg.doubleValue());
      EXT043.setInt("EXTDAT", tdat);
      EXT043.setInt("EXRGDT", formatDateTime(timeOfCreation, "yyyyMMdd"));
      EXT043.setInt("EXRGTM", formatDateTime(timeOfCreation, "HHmmss"));
      EXT043.setInt("EXLMDT", formatDateTime(timeOfCreation, "yyyyMMdd"));
      EXT043.setInt("EXCHNO", 1);
      EXT043.set("EXCHID", program.getUser());
      query.insert(EXT043);
    } else {
      mi.error("L'enregistrement existe déjà");
    }
  }

  private Integer formatDateTime(LocalDateTime dateTime, String format) {
    return Integer.parseInt(dateTime.format(DateTimeFormatter.ofPattern(format)));
  }

  private Integer validateInput(String fieldName, String fieldDesc, Object... crits) {
    Integer value = mi.in.get(fieldName);
    Object lastCrit = crits.length > 0 ? crits[crits.length - 1] : null;
    if (value == null) {
      if (lastCrit instanceof Integer) {
        value = (Integer) lastCrit;
      } else {
        mi.error("$fieldDesc est obligatoire");
        return null;
      }
    }
    for (int i = 0; i < crits.length - (value == DEFAULT_VALUE ? 1 : 0); i += 2) {
      Object crit1 = crits[i];
      Object crit2 = crits[i + 1];
      switch (crit1) {
        case "range":
          Integer min = (Integer) crit2;
          Integer max = (Integer) crits[i + 3];
          if (value < min || value > max) {
            mi.error("$fieldDesc doit être compris(e) entre $min et $max");
          }
          break;
        case "date":
          String format = (String) crit2;
          if (!utility.call("DateUtil", "isDateValid", value.toString(), format)) {
            mi.error("Format $fieldDesc invalide");
          }
          break;
      }
    }
    return value;
  }
}
