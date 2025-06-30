/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT080MI.DelPrcLstCriter
 * Description : Delete records from the EXT080 table.
 * Date         Changed By   Description
 * 20210408     RENARN       TARX03 - Add price list
 * 20211022     DUVCYR       Customer number no longer mandatory.
 */
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class DelPrcLstCriter extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final ProgramAPI program
  private String iCUNO

  public DelPrcLstCriter(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi;
    this.database = database
    this.program = program
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    iCUNO=mi.in.get("CUNO")
    if(iCUNO==null || iCUNO==""){
      DBAction queryEXT080 = database.table("EXT080").index("20").selection("EXCUNO").build()
      DBContainer EXT080 = queryEXT080.getContainer()
      EXT080.set("EXCONO", currentCompany)
      EXT080.set("EXPRRF", mi.in.get("PRRF"))
      EXT080.set("EXCUCD", mi.in.get("CUCD"))
      EXT080.set("EXFVDT", mi.in.get("FVDT") as Integer)
      if(!queryEXT080.readAll(EXT080, 4, outDataEXT080)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
      if(iCUNO==null || iCUNO==""){
        mi.error("Client n'existe pas")
        return
      }
    }
    DBAction query = database.table("EXT080").index("00").build()
    DBContainer EXT080 = query.getContainer()
    EXT080.set("EXCONO", currentCompany);
    EXT080.set("EXPRRF", mi.in.get("PRRF"));
    EXT080.set("EXCUCD", mi.in.get("CUCD"));
    EXT080.set("EXCUNO", iCUNO);
    EXT080.set("EXFVDT", mi.in.get("FVDT") as Integer);
    if(!query.readLock(EXT080, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }

    DBAction query_EXT081 = database.table("EXT081").index("00").build()
    DBContainer EXT081 = query_EXT081.getContainer()
    EXT081.set("EXCONO", currentCompany);
    EXT081.set("EXPRRF", mi.in.get("PRRF"));
    EXT081.set("EXCUCD", mi.in.get("CUCD"));
    EXT081.set("EXCUNO", iCUNO);
    EXT081.set("EXFVDT", mi.in.get("FVDT") as Integer);
    if(!query_EXT081.readAllLock(EXT081, 5, Delete_EXT081_OOPRICH)){
      /* mi.error("L'enregistrement n'existe pas")
       return*/
    }

    DBAction TarifCompQuery = database.table("EXT075").index("00").build();
    DBContainer EXT075 = TarifCompQuery.getContainer();
    EXT075.set("EXCONO",currentCompany);
    EXT075.set("EXPRRF",mi.in.get("PRRF"));
    EXT075.set("EXCUCD",mi.in.get("CUCD"));
    EXT075.set("EXCUNO",iCUNO);
    EXT075.set("EXFVDT",mi.in.get("FVDT"));
    if(!TarifCompQuery.readAllLock(EXT075, 5, Delete_EXT075_OOPRICH)){
      /*mi.error("L'enregistrement n'existe pas")
    return*/
    }

    DBAction TarifCompQueryB = database.table("EXT076").index("00").build();
    DBContainer EXT076 = TarifCompQueryB.getContainer();
    EXT076.set("EXCONO",currentCompany);
    EXT076.set("EXPRRF",mi.in.get("PRRF"));
    EXT076.set("EXCUCD",mi.in.get("CUCD"));
    EXT076.set("EXCUNO",iCUNO);
    EXT076.set("EXFVDT",mi.in.get("FVDT"));
    if(!TarifCompQueryB.readAllLock(EXT076, 5, Delete_EXT076_OOPRICH)){
      /*mi.error("L'enregistrement n'existe pas")
      return*/
    }

  }
  Closure<?> outDataEXT080 = { DBContainer EXT080 ->
    iCUNO = EXT080.get("EXCUNO")
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  Closure<?> Delete_EXT081_OOPRICH = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  Closure<?> Delete_EXT075_OOPRICH = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  Closure<?> Delete_EXT076_OOPRICH = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
