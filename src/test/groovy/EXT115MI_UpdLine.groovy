/**
 * README
 * This extension is used by EvenHub
 *
 * Name : EXT115MI.UpdLine
 * Description : Update MPLINE/ECF1 to ECF5
 * Date         Changed By   Description
 * 20210628     RENARN       APPI05-A - Envoi commande d'achat pour alimentation RFBZ22
 * 20220413     RENARN       Comment added, semicolon removed
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class UpdLine extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction

  public UpdLine(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    DBAction query = database.table("MPLINE").index("00").selection("IBCONO", "IBPUNO", "IBPNLI", "IBPNLS", "IBCHNO").build()
    DBContainer MPLINE = query.getContainer()
    MPLINE.set("IBCONO", currentCompany)
    MPLINE.set("IBPUNO", mi.in.get("PUNO"))
    MPLINE.set("IBPNLI", mi.in.get("PNLI"))
    MPLINE.set("IBPNLS", mi.in.get("PNLS"))
    if(!query.readLock(MPLINE, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Update MPLINE
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("IBCHNO")

    if(mi.in.get("ECF1") != null)
      lockedResult.set("IBECF1", mi.in.get("ECF1"))
    if(mi.in.get("ECF2") != null)
      lockedResult.set("IBECF2", mi.in.get("ECF2"))
    if(mi.in.get("ECF3") != null)
      lockedResult.set("IBECF3", mi.in.get("ECF3"))
    if(mi.in.get("ECF4") != null)
      lockedResult.set("IBECF4", mi.in.get("ECF4"))
    if(mi.in.get("ECF5") != null)
      lockedResult.set("IBECF5", mi.in.get("ECF5"))
    lockedResult.setInt("IBLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("IBCHNO", changeNumber + 1)
    lockedResult.set("IBCHID", program.getUser())
    lockedResult.update()
  }
}
