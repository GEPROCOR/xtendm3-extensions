import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class FIN76736 extends ExtendM3Transaction {
  private final MIAPI mi
  private final ProgramAPI program
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final TextFilesAPI textFiles
  private final String[] runOnlyForUsers = [] // Leave the array empty if it should be run for everyone, otherwise add authorized usernames
  private final Map<String, List<String>> tableKeys = new HashMap<>()
  private final Map<String, List<String>> impactedColumns = new HashMap<>()
  private final Map<String, Long> nrOfCreates = new HashMap<>()
  private final Map<String, Long> nrOfReads = new HashMap<>()
  private final Map<String, Long> nrOfUpdates = new HashMap<>()
  private final Map<String, Long> nrOfDeletes = new HashMap<>()
  private final Set<String> tables = new HashSet<String>()
  private String reportName
  private boolean dryRun
  private final String CREATE = "CREATE"
  private final String READ   = "READ"
  private final String UPDATE = "UPDATE"
  private final String DELETE = "DELETE"

  public FIN76736(MIAPI mi, ProgramAPI program, DatabaseAPI database, LoggerAPI logger, TextFilesAPI textFiles) {
    this.mi = mi
    this.program = program
    this.database = database
    this.logger = logger
    this.textFiles = textFiles
  }

  public void main() {
    Optional<String> error = shouldRun()
    if (error.isPresent()) {
      mi.error(error.get())
      return
    }
    dryRun = mi.in.get("ZDRR") == 1
    init()
    fixRecords()
    finish()
  }

  void fixRecords() {
    updateFGLEDG()
  }

  void updateFGLEDG() {
    ExpressionFactory filter = database.getExpressionFactory("FGLEDG")
    if(mi.in.get("VONO") != null) {
      filter = filter.eq("EGVONO", mi.inData.get("VONO").trim())
    }
    DBAction query = database.table("FGLEDG")
      .index("00")
      .selection("EGCONO", "EGDIVI", "EGYEA4", "EGJRNO", "EGJSNO", "EGVONO", "EGACDT", "EGRECO", "EGREDE")
      .matching(filter)
      .build()
    DBContainer FGLEDG = query.getContainer()
    int nrOfKeys = 4;
    FGLEDG.set("EGCONO", mi.in.get("CONO"))
    FGLEDG.set("EGDIVI", mi.in.get("DIVI"))
    FGLEDG.set("EGYEA4", mi.in.get("YEA4"))
    FGLEDG.set("EGJRNO", mi.in.get("JRNO"))
    if (mi.in.get("JSNO") != null) {
      FGLEDG.set("EGJSNO", mi.in.get("JSNO"))
      nrOfKeys++
    }
    int reads = query.readAllLock(FGLEDG, nrOfKeys, migrateFGLEDGRecords)
    trackDataRead("FGLEDG", reads)
  }

  Closure<?> migrateFGLEDGRecords = { LockedResult record ->
    String keyString = buildKeyString("FGLEDG", record)
    String oldValues = buildColumnValuesString("FGLEDG", record)
    if (mi.in.get("ACDT") != null) {
      record.set("EGACDT", mi.in.get("ACDT"))
    }
    if (mi.in.get("RECO") != null) {
      record.set("EGRECO", mi.in.get("RECO"))
    }
    if (mi.in.get("REDE") != null) {
      record.set("EGREDE", mi.in.get("REDE"))
    }
    if (!dryRun) {
      record.update()
    }
    String newValues = buildColumnValuesString("FGLEDG", record)
    trackDataModification(UPDATE, "FGLEDG", keyString, oldValues, newValues)
    logger.debug("XtendM3 FixProgram: FGLEDG updated: ${record.get("EGCONO")}, ${record.get("EGDIVI")}, ${record.get("EGACDT")}, ${record.get("EGRECO")}, ${record.get("EGREDE")}")
  }

  // GENERIC METHODS, ALL CODES SHOULD BE ABOVE


  /**
   * Check if script should run or not
   * @return true if script should run
   */
  Optional<String> shouldRun() {
    if (runOnlyForUsers.length != 0) {
      String currentUser = program.LDAZD.get("RESP").toString().trim()
      boolean authorizedToRun = runOnlyForUsers.contains(currentUser)
      logger.debug("User {$currentUser} authorization check result was ${authorizedToRun}")
      if (!authorizedToRun) {
        return Optional.of("Not authorized to run this extension")
      }
    }
    if (mi.in.get("ZDRR") == 1 && mi.getMaxRecords() != 10000) {
      return Optional.of("When in dry run mode, max record should be set to 10000.")
    }
    return Optional.empty()
  }

  /**
   * Initialize extension
   */
  void init() {
    // Set up report file name
    reportName = program.getTenantId() + "-FGLEDG-migration-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) + ".txt"
    // Set up keys for tables that are modified
    tableKeys.put("FGLEDG", ["EGCONO", "EGDIVI", "EGYEA4", "EGJRNO", "EGJSNO", "EGVONO", "EGACDT", "EGRECO", "EGREDE"])
    // Set up columns that will be modified
    impactedColumns.put("FGLEDG", ["EGCONO", "EGDIVI", "EGACDT", "EGRECO", "EGREDE"])
  }

  String buildKeyString(String table, DBContainer container) {
    List<String> keys = tableKeys.get(table) // If not present this method will throw NullPointerException and that is expected behavior
    return concatKeyValues(container, keys)
  }

  String buildColumnValuesString(String table, DBContainer container) {
    List<String> keys = impactedColumns.get(table) // If not present this method will throw NullPointerException and that is expected behavior
    return concatKeyValues(container, keys)
  }

  private String concatKeyValues(DBContainer container, List<String> columns) {
    List<String> columnValues = []
    for (String column : columns) {
      columnValues.add(column)
      columnValues.add(container.get(column).toString())
    }
    return String.join(",", columnValues)
  }

  void finish() {
    if (!dryRun) {
      log("Migration started...")
      for (String table : tables) {
        log("Statistics for table ${table}")
        log(nrOfCreates.getOrDefault(table, 0L) + " records created")
        log(nrOfReads.getOrDefault(table, 0L) + " records read")
        log(nrOfUpdates.getOrDefault(table, 0L) + " records updated")
        log(nrOfDeletes.getOrDefault(table, 0L) + " records deleted")
        addDatabaseOperationMetrics(table, CREATE, nrOfCreates.getOrDefault(table, 0L))
        addDatabaseOperationMetrics(table, READ, nrOfReads.getOrDefault(table, 0L))
        addDatabaseOperationMetrics(table, UPDATE, nrOfUpdates.getOrDefault(table, 0L))
        addDatabaseOperationMetrics(table, DELETE, nrOfDeletes.getOrDefault(table, 0L))
      }
      log("Migration finished.")
    }
  }

  void addDatabaseOperationMetrics(String table, String operation, long total) {
    if (total != 0L) {
      mi.outData.put("ZFIL", table)
      mi.outData.put("ZOPR", operation)
      mi.outData.put("ZTOT", total.toString())
      mi.outData.put("ZKEY", "N/A")
      mi.outData.put("ZOLD", "N/A")
      mi.outData.put("ZNEW", "N/A")
      mi.write()
    }
  }

  void log(String message) {
    message = "XtendM3 FixProgram - " + message
    logger.info(message)
    message = LocalDateTime.now().toString() + " " + message
    if (mi.in.get("ZWRT") == 1) {
      Closure<?> consumer = { PrintWriter printWriter ->
        printWriter.println(message)
      }
      textFiles.write(reportName, "UTF-8", true, consumer)
    }
  }

  void trackDataRead(String table, int recordCount) {
    long current = nrOfReads.getOrDefault(table, 0L)
    nrOfReads.put(table, current + recordCount)
    tables.add(table)
  }

  void trackDataModification(String operation, String table, String keyString, String oldValues, String newValues) {
    oldValues = oldValues != null ? oldValues : "N/A"
    newValues = newValues != null ? newValues : "N/A"
    if (operation == CREATE) {
      long current = nrOfCreates.getOrDefault(table, 0L)
      nrOfCreates.put(table, ++current)
      tables.add(table)
    }
    if (operation == UPDATE) {
      long current = nrOfUpdates.getOrDefault(table, 0L)
      nrOfUpdates.put(table, ++current)
      tables.add(table)
    }
    if (operation == DELETE) {
      long current = nrOfDeletes.getOrDefault(table, 0L)
      nrOfDeletes.put(table, ++current)
      tables.add(table)
    }
    if (dryRun) {
      mi.outData.put("ZFIL", table)
      mi.outData.put("ZOPR", operation)
      mi.outData.put("ZTOT", String.valueOf(1))
      mi.outData.put("ZKEY", keyString)
      mi.outData.put("ZOLD", oldValues)
      mi.outData.put("ZNEW", newValues)
      mi.write()
    }
  }
}
