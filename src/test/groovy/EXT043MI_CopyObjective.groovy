/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT043MI.CopyObjective
 * Description : The CopyObjective transaction copy records to the EXT043 table.
 * Date         Changed By   Description
 * 20230713     APACE        TARX12 - Margin management
 * 20240620     ARENARD      Different fixes
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class CopyObjective extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  public Integer currentCompany
  public String buar
  public String buarCopy
  public String fdat = ""
  public String tdat = ""
  public CopyObjective(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
    this.miCaller = miCaller
  }

  public void main() {

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

    // Check copy business area
    String buarCopy = ""
    if(mi.in.get("ZBUA") != null){
      DBAction buarQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = buarQuery.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "BUAR")
      CSYTAB.set("CTSTKY", mi.in.get("ZBUA"))
      if (!buarQuery.read(CSYTAB)) {
        mi.error("Copie secteur d'activité  " + mi.in.get("ZBUA") + " n'existe pas")
        return
      }
      buarCopy = mi.in.get("ZBUA")
    }

    if(mi.in.get("FDAT") == null){
      mi.error("Date de Début est obligatoire")
      return
    }else{
      fdat = mi.in.get("FDAT")
      if(!utility.call("DateUtil","isDateValid",fdat,"yyyyMMdd")){
        mi.error("Format Date de début incorrect : "+fdat)
        return
      }
    }

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT043").index("00").selection("EXTDAT","EXMARG").build()
    DBContainer EXT043 = query.getContainer()
    EXT043.set("EXCONO", currentCompany)
    EXT043.set("EXBUAR", buarCopy)
    EXT043.setInt("EXFDAT", fdat as Integer)
    if (!query.read(EXT043)) {
      EXT043.set("EXCONO", currentCompany)
      EXT043.set("EXBUAR", buar)
      EXT043.setInt("EXFDAT", fdat as Integer)
      if (query.read(EXT043)) {
        EXT043.set("EXBUAR", buarCopy)
        tdat = EXT043.get("EXTDAT")
        EXT043.setDouble("EXMARG", EXT043.get("EXMARG") as Double)
        EXT043.setInt("EXTDAT", Integer.parseInt(tdat))
        EXT043.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT043.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        EXT043.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT043.setInt("EXCHNO", 1)
        EXT043.set("EXCHID", program.getUser())
        //Copy Objective in EXT043
        query.insert(EXT043)
      } else {
        mi.error("L'enregistrement à copier n'existe pas")
        return
      }
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }

}
