/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT075MI.DltRateSales
 * Description : The DltRateSales transaction delete records to the EXT075 table.
 * Date         Changed By   Description
 * 20210510     CDUV         TARX16 - Calcul du tarif
 * 20211030     CDUV         Check added, reading keys modified
 * 20220519     CDUV         lowerCamelCase has been fixed
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class DltRateSales extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private String iCUNO

  public DltRateSales(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
  }

  public void main() {
    String iFVDT=""

    if(mi.in.get("FVDT") != ""){
      iFVDT = mi.in.get("FVDT")
      if (!utility.call("DateUtil", "isDateValid", iFVDT, "yyyyMMdd")) {
        mi.error("Format Date de début Validité incorrect")
        return
      }
    }

    Integer currentCompany
    DBAction TarifCompQuery = database.table("EXT075").index("00").build()
    DBContainer EXT075 = TarifCompQuery.getContainer()
    EXT075.set("EXCONO",currentCompany)
    EXT075.set("EXPRRF",mi.in.get("PRRF"))
    EXT075.set("EXCUCD",mi.in.get("CUCD"))
    iCUNO=mi.in.get("CUNO")
    if(iCUNO==null || iCUNO==""){
      DBAction queryEXT080 = database.table("EXT080").index("20").selection("EXCUNO").build()
      DBContainer EXT080 = queryEXT080.getContainer()
      EXT080.set("EXCONO", currentCompany)
      EXT080.set("EXPRRF", mi.in.get("PRRF"))
      EXT080.set("EXCUCD", mi.in.get("CUCD"))
      EXT080.set("EXFVDT", iFVDT as Integer)
      if(!queryEXT080.readAll(EXT080, 4, outDataEXT080)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
      if(iCUNO==null || iCUNO==""){
        mi.error("Client n'existe pas")
        return
      }
    }

    EXT075.set("EXCUNO", iCUNO)
    EXT075.set("EXFVDT",iFVDT as Integer)
    if(!TarifCompQuery.readAllLock(EXT075, 5, Delete_EXT075_OOPRICH)){
      mi.error("L'enregistrement n'existe pas")
      return
    }

    DBAction TarifCompQueryB = database.table("EXT076").index("00").build()
    DBContainer EXT076 = TarifCompQueryB.getContainer()
    EXT076.set("EXCONO",currentCompany)
    EXT076.set("EXPRRF",mi.in.get("PRRF"))
    EXT076.set("EXCUCD",mi.in.get("CUCD"))
    EXT076.set("EXCUNO",iCUNO)
    EXT076.set("EXFVDT",iFVDT as Integer)
    if(!TarifCompQueryB.readAllLock(EXT076, 5, Delete_EXT076_OOPRICH)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // outDataEXT080 : Retrieve EXT080 CUNO
  Closure<?> outDataEXT080 = { DBContainer EXT080 ->
    iCUNO = EXT080.get("EXCUNO")
  }
  // Delete_EXT075_OOPRICH : Delete EXT075
  Closure<?> Delete_EXT075_OOPRICH = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  //Delete_EXT076_OOPRICH : Delete EXT076
  Closure<?> Delete_EXT076_OOPRICH = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
