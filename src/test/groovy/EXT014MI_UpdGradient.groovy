/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT014MI.UpdGradient
 * Description : Update records from the EXT014 table.
 * Date         Changed By   Description
 * 20210215     RENARN       QUAX01 - Constraints matrix
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdGradient extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private final MICallerAPI miCaller;

  public UpdGradient(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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
    // Check country
    if(mi.in.get("CSCD") != null){
      DBAction countryQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = countryQuery.getContainer()
      CSYTAB.set("CTCONO", currentCompany)
      CSYTAB.set("CTSTCO",  "CSCD")
      CSYTAB.set("CTSTKY", mi.in.get("CSCD"))
      if (!countryQuery.read(CSYTAB)) {
        mi.error("Pays " + mi.in.get("CSCD") + " n'existe pas")
        return
      }
    }
    // Check minimum rate
    int minimumRate = 0
    if(mi.in.get("ZMIR") != null){
      minimumRate = mi.in.get("ZMIR")
      if(minimumRate < 0 || minimumRate > 100){
        mi.error("Taux minimum doit être entre 0 and 100%")
        return
      }
    }
    // Check maximum rate
    int maximumRate = 0
    if(mi.in.get("ZMAR") != null){
      maximumRate = mi.in.get("ZMAR")
      if(maximumRate < 0 || maximumRate > 100){
        mi.error("Taux maximum doit être entre 0 and 100%")
        return
      }
    }

    if(mi.in.get("ZMIR") != null && mi.in.get("ZMAR") != null){
      if(maximumRate < minimumRate){
        mi.error("Taux maximum ne peut pas être inférieur au taux minimum")
        return
      }
    }

    // Check gradient
    if(mi.in.get("ZGRA") == '') {
      mi.error("Gradient est obligatoire")
      return
    }

    DBAction query = database.table("EXT014").index("00").selection("EXCHNO").build()
    DBContainer EXT014 = query.getContainer()
    EXT014.set("EXCONO", currentCompany)
    EXT014.set("EXCSCD",  mi.in.get("CSCD"))
    EXT014.set("EXZMIR",  mi.in.get("ZMIR"))
    if(!query.readLock(EXT014, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    if (mi.in.get("ZMAR") != null)
      lockedResult.set("EXZMAR", mi.in.get("ZMAR"))
    if (mi.in.get("ZGRA") != null)
      lockedResult.set("EXZGRA", mi.in.get("ZGRA"))
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
}
