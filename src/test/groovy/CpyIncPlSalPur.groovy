/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT030MI.CpyIncPlSalPur
 * Description : The CpyIncPlSalPur transaction copy records to the EXT030 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class CpyIncPlSalPur extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction

  public CpyIncPlSalPur(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi;
    this.database = database
    this.program = program
  }

  public void main() {
    // Check incoterm place sale
    if(mi.in.get("ZIPS") == null || mi.in.get("ZIPS") == ""){
      mi.error("Incoterm lieu vente est obligatoire")
      return
    }

    // Check incoterm place purchase
    if(mi.in.get("ZIPP") == null || mi.in.get("ZIPP") == ""){
      mi.error("Incoterm lieu achat est obligatoire")
      return
    }
    // Check incoterm place purchase
    if(mi.in.get("CZIP") == null || mi.in.get("CZIP") == ""){
      mi.error("Incoterm lieu vente est obligatoire")
      return
    } else {
      DBAction incotermPlaceQuery = database.table("EXT031").index("10").build()
      DBContainer EXT031 = incotermPlaceQuery.getContainer()
      EXT031.set("EXCONO", mi.in.get("CONO"))
      EXT031.set("EXZIPL", mi.in.get("CZIP"))
      if(!incotermPlaceQuery.readAll(EXT031, 2, closure)){
        mi.error("Incoterm lieu achat " + mi.in.get("CZIP") + " n'existe pas")
        return
      }
    }

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT030").index("00").selection("EXZCOM").build()
    DBContainer EXT030 = query.getContainer()
    EXT030.set("EXCONO", mi.in.get("CONO"))
    EXT030.set("EXZIPS", mi.in.get("ZIPS"))
    EXT030.set("EXZIPP", mi.in.get("ZIPP"))
    if(query.read(EXT030)){
      EXT030.set("EXZIPP", mi.in.get("CZIP"))
      if (!query.read(EXT030)) {
        EXT030.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT030.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        EXT030.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT030.setInt("EXCHNO", 1)
        EXT030.set("EXCHID", program.getUser())
        query.insert(EXT030)
      } else {
        mi.error("L'enregistrement existe déjà")
      }
    } else {
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> closure = { DBContainer EXT030 ->
  }
}
