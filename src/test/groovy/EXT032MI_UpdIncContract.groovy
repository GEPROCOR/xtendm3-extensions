/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT032MI.UpdIncContract
 * Description : The UpdIncContract transaction update records to the EXT032 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdIncContract extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany
  private final UtilityAPI utility;

  public UpdIncContract(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
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

    DBAction query = database.table("EXT032").index("00").selection("EXCHNO").build()
    DBContainer EXT032 = query.getContainer()
    EXT032.set("EXCONO", currentCompany)
    EXT032.set("EXAGNB",  mi.in.get("AGNB"))
    EXT032.set("EXSUNO",  mi.in.get("SUNO"))
    if(!query.readLock(EXT032, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
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
