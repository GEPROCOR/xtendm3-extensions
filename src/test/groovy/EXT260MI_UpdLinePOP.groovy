/**
 * README
 * This extension is used by EvenHub
 *
 * Name : EXT260MI.UpdLinePOP
 * Description : Update RORC/RORN
 * Date         Changed By   Description
 * 20221103     YVOYOU       APPX26 - Corr. bug update RORC/RORN
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdLinePOP extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private int xxPOSX

  public UpdLinePOP(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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
    if (mi.in.get("POSX") == null) {
      xxPOSX = 0
    }else{
      xxPOSX = mi.in.get("POSX")
    }
    DBAction query = database.table("OOLINE").index("00").selection("OBCONO", "OBORNO", "OBPONR", "OBPOSX", "OBRORC", "OBRORN").build()
    DBContainer OOLINE = query.getContainer()
    OOLINE.set("OBCONO", currentCompany)
    OOLINE.set("OBORNO", mi.in.get("ORNO"))
    OOLINE.set("OBPONR", mi.in.get("PONR"))
    OOLINE.set("OBPOSX", xxPOSX)
    if(!query.readLock(OOLINE, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("OBCHNO")
    lockedResult.set("OBLTYP", "0")
    lockedResult.set("OBRORC", 0)
    lockedResult.set("OBRORN", "")
    lockedResult.setInt("OBLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("OBCHNO", changeNumber + 1)
    lockedResult.set("OBCHID", program.getUser())
    lockedResult.update()
  }
}
