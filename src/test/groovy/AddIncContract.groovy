/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT032MI.AddIncContract
 * Description : The AddIncContract transaction add records to the EXT032 table. 
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm 
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddIncContract extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany
  private final UtilityAPI utility;

  public AddIncContract(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi;
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility;
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    if(mi.in.get("SUNO") == null || mi.in.get("SUNO") == ""){
      mi.error("Fournisseur est obligatoire")
      return
    }
    if(mi.in.get("AGNB") == null || mi.in.get("AGNB") == ""){
      mi.error("Num Contrat est obligatoire")
      return
    }
    if(mi.in.get("ZIPP") == null || mi.in.get("ZIPP") == ""){
      mi.error("Incoterm lieu est obligatoire")
      return
    }
    DBAction Query = database.table("MPAGRH").index("00").build()
    DBContainer MPAGRH = Query.getContainer()
    MPAGRH.set("AHCONO", currentCompany)
    MPAGRH.set("AHSUNO",  mi.in.get("SUNO"))
    MPAGRH.set("AHAGNB",  mi.in.get("AGNB"))
    if (!Query.read(MPAGRH)) {
      mi.error("NumContrat " + mi.in.get("AGNB") + " n'existe pas pour le fournisseur: " + mi.in.get("SUNO") )
      return
    }

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT032").index("00").build()
    DBContainer EXT032 = query.getContainer()
    EXT032.set("EXCONO", currentCompany)
    EXT032.set("EXAGNB",  mi.in.get("AGNB"))
    EXT032.set("EXSUNO",  mi.in.get("SUNO"))
    if(!query.readLock(EXT032, updateCallBack)){
      EXT032.set("EXZIPP", mi.in.get("ZIPP"))
      EXT032.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT032.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT032.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT032.setInt("EXCHNO", 1)
      EXT032.set("EXCHID", program.getUser())
      query.insert(EXT032)
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.set("EXZIPP", mi.in.get("ZIPP"))
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }
}
