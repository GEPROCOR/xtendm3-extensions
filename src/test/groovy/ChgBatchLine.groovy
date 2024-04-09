/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT060MI.ChgBatchLine
 * Description : Update OXLINE
 * Date         Changed By   Description
 * 20220310     RENARN       CMDX04 - Recherche article
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class ChgBatchLine extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction

  public ChgBatchLine(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

    if(mi.in.get("ORNO") == null || mi.in.get("ORNO") == ""){
      mi.error("Numéro Commande est obligatoire")
      return
    }

    if(mi.in.get("PONR") == null || mi.in.get("PONR") == ""){
      mi.error("Numéro ligne commande est obligatoire")
      return
    }

    if(mi.in.get("POSX") == null || mi.in.get("POSX") == ""){
      mi.error("Numéro Ss-ligne commande est obligatoire")
      return
    }
    if(mi.in.get("RSC1") == null) {
      mi.error("Motif transaction est obligatoire")
      return
    } else {
      DBAction query_CSYTAB = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = query_CSYTAB.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "RSCD")
      CSYTAB.set("CTSTKY", mi.in.get("RSC1"))
      if (!query_CSYTAB.read(CSYTAB)) {
        mi.error("Motif transaction " + mi.in.get("RSC1") + " n'existe pas")
        return
      }
    }
    DBAction query = database.table("OXLINE").index("00").selection("OBCONO", "OBRSC1").build()
    DBContainer OXLINE = query.getContainer()
    OXLINE.set("OBCONO", currentCompany)
    OXLINE.set("OBORNO", mi.in.get("ORNO"))
    OXLINE.set("OBPONR", mi.in.get("PONR"))
    if(!query.readLock(OXLINE, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Update OXLINE
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("OBCHNO")
    if (mi.in.get("RSC1") != null)
      lockedResult.set("OBRSC1", mi.in.get("RSC1"))
    lockedResult.setInt("OBLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("OBCHNO", changeNumber + 1)
    lockedResult.set("OBCHID", program.getUser())
    lockedResult.update()
  }
}
