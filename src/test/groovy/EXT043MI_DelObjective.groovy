/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT043MI.DelObjective
 * Description : The DelObjective transaction delete records to the EXT043 table.
 * Date         Changed By   Description
 * 20230713     APACE        TARX12 - Margin management
 * 20240620     ARENARD      Mandatory fields added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class DelObjective extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility

  public DelObjective(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
  }

  public void main() {
    Integer currentCompany
    Integer buar
    String fdat = ""

    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    if(mi.in.get("BUAR") != null){
      buar = mi.in.get("BUAR")
    }

    if(mi.in.get("FDAT") == null){
      mi.error("Date de Début est obligatoire")
      return
    }else {
      fdat = mi.in.get("FDAT")
      if (!utility.call("DateUtil", "isDateValid", fdat, "yyyyMMdd")) {
        mi.error("Format Date de début incorrect")
        return
      }
    }

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT043").index("00").build()
    DBContainer EXT043 = query.getContainer()
    EXT043.set("EXCONO", currentCompany)
    EXT043.set("EXBUAR", buar)
    EXT043.setInt("EXFDAT", fdat as Integer)
    if(!query.readLock(EXT043, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  //Delete Objective in EXT043
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
