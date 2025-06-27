/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT013MI.UpdConstrFeat
 * Description : Update records from the EXT013 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 * 20220211     RENARN       ZRGP added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdConstrFeat extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction

  public UpdConstrFeat(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.program = program
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    DBAction query = database.table("EXT013").index("00").selection("EXCONO", "EXZCFE", "EXZDES").build()
    DBContainer EXT013 = query.getContainer()
    EXT013.set("EXCONO", currentCompany)
    EXT013.set("EXZCFE", mi.in.get("ZCFE"))
    if(!query.readLock(EXT013, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Update EXT013
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    if (mi.in.get("ZDES") != null)
      lockedResult.set("EXZDES", mi.in.get("ZDES"))
    if (mi.in.get("ZRGP") != null)
      lockedResult.set("EXZRGP", mi.in.get("ZRGP"))
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
}
