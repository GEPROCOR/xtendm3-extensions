/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT015MI.DelByOrderLine
 * Description : Disable records in the EXT015 table by order line.
 * Date         Changed By   Description
 * 20210311     RENARN       QUAX07 - Constraints engine
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class DelByOrderLine extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final ProgramAPI program

  public DelByOrderLine(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi;
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
    DBAction query = database.table("EXT015").index("00").build()
    DBContainer EXT015 = query.getContainer()
    EXT015.set("EXCONO", currentCompany)
    EXT015.set("EXORNO",  mi.in.get("ORNO"))
    EXT015.set("EXPONR",  mi.in.get("PONR"))
    EXT015.set("EXPOSX",  mi.in.get("POSX"))
    if(!query.readAllLock(EXT015, 4, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.set("EXSTAT", "90")
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
}
