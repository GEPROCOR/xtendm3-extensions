/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT025MI.DelMatrixLine
 * Description : Delete record to the EXT025 table.
 * Date         Changed By   Description
 * 20240821     ALLNIC          CMD40 - Delete a Preparation date assortment matrix line
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class DelMatrixLine extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private Integer currentCompany
  private Integer phase
  private Integer weekDayNumber

  public DelMatrixLine(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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
    // Check sales point
    if(mi.in.get("DLSP") == null || mi.in.get("DLSP") == ""){
      mi.error("Point de vente est obligatoire!")
      return
    }

    // Check warehouse
    if(mi.in.get("WHLO") == null || mi.in.get("WHLO") == ""){
      mi.error("Code dépôt est obligatoire!")
      return
    } else {
      DBAction Query = database.table("MITWHL").index("00").build()
      DBContainer MITWHL = Query.getContainer()
      MITWHL.set("MWCONO", currentCompany)
      MITWHL.set("MWWHLO",  mi.in.get("WHLO"))
      if (!Query.read(MITWHL)) {
        mi.error("Code dépôt " + mi.in.get("WHLO") + " n'existe pas!")
        return
      }
    }
    // Check customer order type
    if(mi.in.get("ORTP") == null || mi.in.get("ORTP") == ""){
      mi.error("Type commande de vente est obligatoire!")
      return
    } else {
      DBAction Query = database.table("OOTYPE").index("00").build()
      DBContainer OOTYPE = Query.getContainer()
      OOTYPE.set("OOCONO", currentCompany)
      OOTYPE.set("OOORTP",  mi.in.get("ORTP"))
      if (!Query.read(OOTYPE)) {
        mi.error("Code type commande de vente " + mi.in.get("ORTP") + " n'existe pas!")
        return
      }
    }
    // Check Phase
    if(mi.in.get("PHAS") == null || mi.in.get("PHAS") == ""){
      mi.error("Code phase est obligatoire!")
      return
    } else {
      phase = mi.in.get("PHAS") as Integer
      if(phase!=1 && phase!=2){
        mi.error("Code phase " + phase + " est invalide")
        return
      }
    }
    // Check Week day number
    if(mi.in.get("WDNU") == null || mi.in.get("WDNU") == ""){
      mi.error("Numero de jour est obligatoire!")
      return
    } else {
      weekDayNumber = mi.in.get("WDNU") as Integer
      if(weekDayNumber<1 || weekDayNumber>7){
        mi.error("Numero de jour " + weekDayNumber + " est invalide")
        return
      }
    }
    // Check record
    DBAction query = database.table("EXT025").index("00").build()
    DBContainer EXT025 = query.getContainer()
    EXT025.set("EXCONO", currentCompany)
    EXT025.set("EXDLSP", mi.in.get("DLSP"))
    EXT025.set("EXWHLO", mi.in.get("WHLO"))
    EXT025.set("EXORTP", mi.in.get("ORTP"))
    EXT025.set("EXPHAS", mi.in.get("PHAS") as Integer)
    EXT025.set("EXWDNU", mi.in.get("WDNU") as Integer)
    if(!query.readLock(EXT025, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Delete matrix line
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
