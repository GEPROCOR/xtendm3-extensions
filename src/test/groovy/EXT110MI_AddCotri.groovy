/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT100MI.AddCotri
 * Description : The AddCotri transaction adds POPPSQ to the MPOPLP table.
 * Date         Changed By   Description
 * 20210514     APACE        APPX09 - BQ Management
 * 20211213     APACE        POCKEY update added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class AddCotri extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  String PPSQ =""

  public AddCotri(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
  }

  public void main() {
    Integer currentCompany
    Integer PLPN
    Integer PLPS
    Integer PLP2

    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    if(mi.in.get("PLPN") != null){
      PLPN = (Integer)mi.in.get("PLPN")
    }else{
      mi.error("Code planning est obligatoire")
      return
    }

    if(mi.in.get("PLPS") != null){
      PLPS = (Integer)mi.in.get("PLPS")
    }else{
      mi.error("Sous-numéro ordre planifié est obligatoire")
      return
    }

    if(mi.in.get("PLP2") != null){
      PLP2 = (Integer)mi.in.get("PLP2")
    }else{
      mi.error("Sous-numéro proposition de commande est obligatoire")
      return
    }


    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("MPOPLP").index("00").build()
    DBContainer MPOPLP = query.getContainer()
    MPOPLP.set("POCONO", currentCompany)
    MPOPLP.setInt("POPLPN", PLPN)
    MPOPLP.setInt("POPLPS", PLPS)
    MPOPLP.setInt("POPLP2", PLP2)
    if(!query.readLock(MPOPLP, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Update MPOPLP
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    if(mi.in.get("PPSQ")!=null){
      PPSQ = mi.in.get("PPSQ")
      PPSQ = PPSQ.toUpperCase()
      lockedResult.set("POPPSQ", PPSQ)
    }else{
      lockedResult.set("POPPSQ", "")
    }
    lockedResult.set("POCKEY", "1 0")
    lockedResult.setInt("POLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("POCHNO", 1)
    lockedResult.set("POCHID", program.getUser())
    lockedResult.update()
  }
}
