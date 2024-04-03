/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT032MI.DltIncContract
 * Description : The DltIncContract transaction delete records to the EXT032 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 */

public class DltIncContract extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany

  public DltIncContract(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi;
    this.database = database
    this.logger = logger
    this.program = program
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

    DBAction query = database.table("EXT032").index("00").build()
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
    lockedResult.delete()
  }
}
