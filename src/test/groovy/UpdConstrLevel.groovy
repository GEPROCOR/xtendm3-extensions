/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT012MI.UpdConstrLevel
 * Description : Update records from the EXT012 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdConstrLevel extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private final MICallerAPI miCaller;

  public UpdConstrLevel(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.logger = logger
    this.miCaller = miCaller;
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Check constraint type
    if(mi.in.get("ZCTY") != null){
      DBAction query = database.table("EXT011").index("00").build()
      DBContainer EXT011 = query.getContainer()
      EXT011.set("EXCONO", currentCompany)
      EXT011.set("EXZCTY",  mi.in.get("ZCTY"))
      if (!query.read(EXT011)) {
        mi.error("Type de contrainte " + mi.in.get("ZCTY") + " n'existe pas")
        return
      }
    }
    // Check user
    if(mi.in.get("USID") != null){
      DBAction query = database.table("CMNUSR").index("00").build()
      DBContainer CMNUSR = query.getContainer()
      CMNUSR.set("JUUSID", mi.in.get("USID"))
      if (!query.read(CMNUSR)) {
        mi.error("Utilisateur " + mi.in.get("USID") + " n'existe pas")
        return
      }
    }
    // Check blocking
    if(mi.in.get("ZBLC") != null && mi.in.get("ZBLC") != 0 && mi.in.get("ZBLC") != 1){
      int zblc = mi.in.get("ZBLC")
      mi.error("Blocage " + zblc + " est invalide")
      return
    }
    DBAction query = database.table("EXT012").index("00").selection("EXCHNO").build()
    DBContainer EXT012 = query.getContainer()
    EXT012.set("EXCONO",currentCompany)
    EXT012.set("EXZCLV", mi.in.get("ZCLV"))
    if(!query.readLock(EXT012, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    if(mi.in.get("ZDES") != null)
      lockedResult.set("EXZDES", mi.in.get("ZDES"))
    if(mi.in.get("ZCTY") != null)
      lockedResult.set("EXZCTY", mi.in.get("ZCTY"))
    if(mi.in.get("USID") != null)
      lockedResult.set("EXUSID", mi.in.get("USID"))
    if(mi.in.get("ZBLC") != null)
      lockedResult.set("EXZBLC", mi.in.get("ZBLC"))
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
}
