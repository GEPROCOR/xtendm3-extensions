/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT033MI.CpyIncotermWhPu
 * Description : The CpyIncotermWhPu transaction cpy records to the EXT033 table. 
 * Date         Changed By   Description
 * 20211213     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class CpyIncotermWhPu extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private Integer currentCompany

  public CpyIncotermWhPu(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.program = program
    this.logger = logger
    this.miCaller = miCaller
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    // Check incoterm place purchase
    if(mi.in.get("ZIPP") == null || mi.in.get("ZIPP") == ""){
      mi.error("Incoterm lieu achat est obligatoire")
      return
    }

    // Check Warehouse
    if(mi.in.get("WHLO") == null || mi.in.get("WHLO") == ""){
      mi.error("Dépot est obligatoire")
      return
    }

    // Check incoterm place purchase
    if((mi.in.get("CZIP") == null || mi.in.get("CZIP") == "")&&
      (mi.in.get("CWHL") == null || mi.in.get("CWHL") == "")){
      mi.error("Incoterm lieu vente ou dépot est obligatoire")
      return
    }
    String ZippTo
    if(mi.in.get("CZIP") != ""){
      DBAction incotermPlaceQuery = database.table("EXT031").index("10").build()
      DBContainer EXT031 = incotermPlaceQuery.getContainer()
      EXT031.set("EXCONO", mi.in.get("CONO"))
      EXT031.set("EXZIPL", mi.in.get("CZIP"))
      if(!incotermPlaceQuery.readAll(EXT031, 2, closure)){
        mi.error("Incoterm lieu achat " + mi.in.get("CZIP") + " n'existe pas")
        return
      }
      ZippTo = mi.in.get("CZIP")
    }else{
      ZippTo = mi.in.get("ZIPP")
    }
    String WhloTo
    if(mi.in.get("CWHL") != "") {
      DBAction query_whlo = database.table("MITWHL").index("00").build()
      DBContainer MITWHL = query_whlo.getContainer()
      MITWHL.set("MWCONO", currentCompany)
      MITWHL.set("MWWHLO", mi.in.get("CWHL"))
      if (!query_whlo.read(MITWHL)) {
        mi.error("Dépôt " + mi.in.get("CWHL") + " n'existe pas")
        return
      }
      WhloTo = mi.in.get("CWHL")
    }else{
      WhloTo = mi.in.get("WHLO")
    }
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT033").index("00").selection("EXZCOM").build()
    DBContainer EXT033 = query.getContainer()
    EXT033.set("EXCONO", mi.in.get("CONO"))
    EXT033.set("EXWHLO", mi.in.get("WHLO"))
    EXT033.set("EXZIPP", mi.in.get("ZIPP"))
    if(query.read(EXT033)){
      EXT033.set("EXZIPP", ZippTo)
      EXT033.set("EXWHLO", WhloTo)
      if (!query.read(EXT033)) {
        EXT033.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT033.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        EXT033.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        EXT033.setInt("EXCHNO", 1)
        EXT033.set("EXCHID", program.getUser())
        query.insert(EXT033)
      } else {
        mi.error("L'enregistrement existe déjà")
      }
    } else {
      mi.error("L'enregistrement n'existe pas")
      return
    }

  }
  Closure<?> closure = { DBContainer EXT031 ->
  }
}
