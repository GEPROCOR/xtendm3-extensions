/**
 * README
 * This extension is used by MEC
 *
 * Name : EXT420MI.RelForPick
 * Description : Adds the item received as a parameter in the assortments and price lists
 * Date         Changed By   Description
 * 20231214     YVOYOU       CMDX33 - RelForPick MWS420MI correction
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RelForPick extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;
  private int currentCompany
  private int olup
  private long dlix
  private int pgrs

  public RelForPick(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi;
    this.database = database;
    this.logger = logger;
    this.program = program;
    this.utility = utility;
    this.miCaller = miCaller
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer) program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
    }

    // Check item
    dlix = mi.in.get("DLIX") as long
    if(mi.in.get("DLIX") != null){
      DBAction query = database.table("MHDISH").index("00").selection("OQDLIX", "OQPGRS").build()
      DBContainer MHDISH = query.getContainer()
      MHDISH.set("OQCONO", currentCompany)
      MHDISH.set("OQINOU", 1)
      MHDISH.set("OQDLIX", dlix)
      if (!query.readLock(MHDISH, outData_MHDISH)) {
        mi.error("Indexe " + dlix + " n'existe pas")
        return
      }
    } else {
      mi.error("Indexe est obligatoire")
      return
    }
    // RelForPick
    olup = 0
    executeMWS410MIRelForPick(currentCompany+"", dlix+"", olup+"")
  }

  Closure<?> outData_MHDISH = { LockedResult lockedResult ->
    pgrs = lockedResult.get("OQPGRS").toString() as int
    // Search index is valid
    if(pgrs < 50) {
      // Search Picking List is not exist
      DBAction query = database.table("MHPICH").index("00").selection("PIDLIX", "PIPLSX").build()
      DBContainer MHPICH = query.getContainer()
      MHPICH.set("PICONO", currentCompany)
      MHPICH.set("PIDLIX", dlix)
      MHPICH.set("PIPLSX", 1)
      if (!query.readAll(MHPICH, 3, outData_MHPICH)) {
        //Update MHDISH
        LocalDateTime timeOfCreation = LocalDateTime.now()
        int changeNumber = lockedResult.get("OQCHNO")
        lockedResult.set("OQRLTD", 0)
        lockedResult.setInt("OQLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        lockedResult.setInt("OQCHNO", changeNumber + 1)
        lockedResult.set("OQCHID", program.getUser())
        lockedResult.update()
      }
    }else{
      mi.error("Indexe déjà libéré pour prélèvement")
      return
    }

  }

  Closure<?> outData_MHPICH = { DBContainer MHPICH ->
  }


  // Release LP for DLIX
  private executeMWS410MIRelForPick(String CONO, String DLIX, String OLUP){
    Map parameters = ["CONO": CONO, "DLIX": DLIX, "OLUP": OLUP]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
      }
    }
    miCaller.call("MWS410MI", "RelForPick", parameters, handler)
  }

}
