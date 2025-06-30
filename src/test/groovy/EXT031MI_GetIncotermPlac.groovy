/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT031MI.GetIncotermPlac
 * Description : The GetIncotermPlac transaction get records to the EXT031 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class GetIncotermPlac extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program

  public GetIncotermPlac(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi;
    this.database = database
    this.logger = logger
    this.program = program
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Check incoterm
    if(mi.in.get("TEDL") == null || mi.in.get("TEDL") == ""){
      mi.error("Incoterm est obligatoire")
      return
    }
    // Check place
    if(mi.in.get("ZPLA") == null || mi.in.get("ZPLA") == ""){
      mi.error("Lieu est obligatoire")
      return
    }
    DBAction query = database.table("EXT031").index("00").selection("EXZIPL", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT031 = query.getContainer()
    EXT031.set("EXCONO", currentCompany)
    EXT031.set("EXTEDL", mi.in.get("TEDL"))
    EXT031.set("EXZPLA", mi.in.get("ZPLA"))
    if(!query.readAll(EXT031, 3, outData)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }

  Closure<?> outData = { DBContainer EXT031 ->
    String incotermPlace = EXT031.get("EXZIPL")
    String entryDate = EXT031.get("EXRGDT")
    String entryTime = EXT031.get("EXRGTM")
    String changeDate = EXT031.get("EXLMDT")
    String changeNumber = EXT031.get("EXCHNO")
    String changedBy = EXT031.get("EXCHID")
    mi.outData.put("ZIPL", incotermPlace)
    mi.outData.put("RGDT", entryDate)
    mi.outData.put("RGTM", entryTime)
    mi.outData.put("LMDT", changeDate)
    mi.outData.put("CHNO", changeNumber)
    mi.outData.put("CHID", changedBy)
    mi.write()
  }
}
