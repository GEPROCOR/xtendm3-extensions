/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT033MI.AddIncotermWhPu
 * Description : The AddIncotermWhPu transaction add records to the EXT033 table.
 * Date         Changed By   Description
 * 20211213     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 */

public class LstIncotermWhPu extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private int currentCompany

  public LstIncotermWhPu(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi
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


    if((mi.in.get("WHLO") == null || mi.in.get("WHLO") == "")&&
      (mi.in.get("ZIPP") == null || mi.in.get("ZIPP") == "")){
      DBAction query = database.table("EXT033").index("00").selection("EXZIPP","EXWHLO","EXPRIO", "EXZCOM", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT033 = query.getContainer()
      EXT033.set("EXCONO", currentCompany)
      if(!query.readAll(EXT033, 1, outData)){
        mi.error("L'enregistrement 1 n'existe pas")
        return
      }
      return
    }
    if((mi.in.get("WHLO") != null || mi.in.get("WHLO") != "")&&
      (mi.in.get("ZIPP") == null || mi.in.get("ZIPP") == "")){
      DBAction query = database.table("EXT033").index("00").selection("EXZIPP","EXWHLO","EXPRIO", "EXZCOM", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT033 = query.getContainer()
      EXT033.set("EXCONO", mi.in.get("CONO"))
      EXT033.set("EXWHLO", mi.in.get("WHLO"))
      if(!query.readAll(EXT033, 2, outData)){
        mi.error("L'enregistrement 2 n'existe pas")
        return
      }
      return
    }
    if((mi.in.get("WHLO") == null || mi.in.get("WHLO") == "")&&
      (mi.in.get("ZIPP") != null || mi.in.get("ZIPP") != "")){
      DBAction query = database.table("EXT033").index("20").selection("EXZIPP","EXWHLO","EXPRIO", "EXZCOM", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT033 = query.getContainer()
      EXT033.set("EXCONO", mi.in.get("CONO"))
      EXT033.set("EXZIPP", mi.in.get("ZIPP"))
      if(!query.readAll(EXT033, 2, outData)){
        mi.error("L'enregistrement 3 n'existe pas")
        return
      }
      return
    }
    if((mi.in.get("WHLO") != null || mi.in.get("WHLO") != "")&&
      (mi.in.get("ZIPP") != null || mi.in.get("ZIPP") != "")){
      DBAction query = database.table("EXT033").index("00").selection("EXZIPP","EXWHLO","EXPRIO", "EXZCOM", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
      DBContainer EXT033 = query.getContainer()
      EXT033.set("EXCONO", mi.in.get("CONO"))
      EXT033.set("EXZIPP", mi.in.get("ZIPP"))
      EXT033.set("EXWHLO", mi.in.get("WHLO"))
      if(!query.readAll(EXT033, 3, outData)){
        mi.error("L'enregistrement 4 n'existe pas")
        return
      }
      return
    }
  }
  // Retrieve EXT033
  Closure<?> outData = { DBContainer EXT033 ->
    String depot = EXT033.get("EXWHLO")
    String incoterm = EXT033.get("EXZIPP")
    String compatible = EXT033.get("EXZCOM")
    String entryDate = EXT033.get("EXRGDT")
    String entryTime = EXT033.get("EXRGTM")
    String changeDate = EXT033.get("EXLMDT")
    String changeNumber = EXT033.get("EXCHNO")
    String changedBy = EXT033.get("EXCHID")
    String PRIO = EXT033.get("EXPRIO")
    mi.outData.put("WHLO", depot)
    mi.outData.put("ZIPP", incoterm)
    mi.outData.put("PRIO", PRIO)
    mi.outData.put("ZCOM", compatible)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
