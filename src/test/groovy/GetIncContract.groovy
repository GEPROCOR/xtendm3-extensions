/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT032MI.GetIncContract
 * Description : The GetIncContract transaction get records to the EXT032 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 */
public class GetIncContract extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany

  public GetIncContract(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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
    DBAction query = database.table("EXT032").index("00").selection("EXCONO","EXSUNO","EXAGNB","EXZIPP").build()
    DBContainer EXT032 = query.getContainer()
    EXT032.set("EXCONO", currentCompany)
    EXT032.set("EXAGNB",  mi.in.get("AGNB"))
    EXT032.set("EXSUNO",  mi.in.get("SUNO"))
    if(!query.readAll(EXT032, 3, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }

  Closure<?> outData = { DBContainer EXT032 ->
    String oCONO = EXT032.get("EXCONO")
    String oSUNO = EXT032.get("EXSUNO")
    String oAGNB = EXT032.get("EXAGNB")
    String oZIPP = EXT032.get("EXZIPP")
    mi.outData.put("CONO", oCONO)
    mi.outData.put("SUNO", oSUNO)
    mi.outData.put("AGNB", oAGNB)
    mi.outData.put("ZIPP", oZIPP)
    mi.write()
  }
}
