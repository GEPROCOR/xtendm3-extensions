/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT043MI.AddObjective
 * Description : The AddObjective transaction adds records to the EXT043 table.
 * Date         Changed By   Description
 * 20230713     APACE        TARX12 - Margin management
 * 20240620     ARENARD      Different fixes
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddObjective extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility

  public AddObjective(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
  }

  public void main() {
    //Load Current Company
    Integer currentCompany
    String fdat =""
    String tdat ="99999999"
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    // Check business area
    String buar = ""
    if(mi.in.get("BUAR") != null){
      DBAction buarQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = buarQuery.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "BUAR")
      CSYTAB.set("CTSTKY", mi.in.get("BUAR"))
      if (!buarQuery.read(CSYTAB)) {
        mi.error("Secteur d'activité  " + mi.in.get("BUAR") + " n'existe pas")
        return
      }
      buar = mi.in.get("BUAR")
    }


    if(mi.in.get("MARG") == null){
      mi.error("Marge est obligatoire")
      return
    }else{
      int maximumRate = 0
      maximumRate = mi.in.get("MARG")
      if(maximumRate < -100 || maximumRate > 100){
        mi.error("Marge doit être comprise entre -100% et 100%")
        return
      }
    }

    if(mi.in.get("FDAT") == null){
      mi.error("Date de Début est obligatoire")
      return
    }else{
      fdat = mi.in.get("FDAT")
      if(!utility.call("DateUtil","isDateValid",fdat,"yyyyMMdd")){
        mi.error("Format Date de début incorrect")
        return
      }
      if(mi.in.get("TDAT") != null) {
        tdat = mi.in.get("TDAT")
        if(!utility.call("DateUtil","isDateValid",tdat,"yyyyMMdd")){
          mi.error("Format Fin de début incorrect")
          return
        }
        int fdatNumber = fdat as Integer
        int tdatNumber = tdat as Integer
        if(fdatNumber>tdatNumber){
          mi.error("Date de début doit être inférieure a la date de fin")
          return
        }
      }
    }

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT043").index("00").build()
    DBContainer EXT043 = query.getContainer()
    EXT043.set("EXCONO", currentCompany)
    EXT043.set("EXBUAR", buar)
    EXT043.setInt("EXFDAT", fdat as Integer)
    //Add Objective in EXT043
    if (!query.read(EXT043)) {
      EXT043.setDouble("EXMARG", mi.in.get("MARG") as Double)
      EXT043.setInt("EXTDAT", Integer.parseInt(tdat))
      EXT043.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT043.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT043.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT043.setInt("EXCHNO", 1)
      EXT043.set("EXCHID", program.getUser())
      query.insert(EXT043)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
}
