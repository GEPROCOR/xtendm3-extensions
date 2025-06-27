/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT020MI.DltSalesPoint
 * Description : The DltSalesPoint transaction delete records to the EXT020 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX06 - Gestion des points de vente
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class DltSalesPoint extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger

  public DltSalesPoint(MIAPI mi, DatabaseAPI database, LoggerAPI logger) {
    this.mi = mi;
    this.database = database
    this.logger = logger
  }

  public void main() {
    DBAction action = database.table("EXT020").index("00").selection("EXCONO", "EXTYPE", "EXFPVT", "EXTPVT", "EXWHTY", "EXTX40", "EXCOPE", "EXSCAT", "EXLSCA", "EXCUNO", "EXCUNM", "EXCDAN", "EXOTYG", "EXSTAT", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT020 = action.createContainer()
    EXT020.setInt("EXCONO", mi.inData.get("CONO").toInteger())
    EXT020.set("EXTYPE", mi.inData.get("TYPE"))
    action.readAllLock(EXT020, 2, releaseExtends)
  }

  Closure<?> releaseExtends = { LockedResult record ->
    record.delete()
  }
}
