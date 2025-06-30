/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT081MI.DelPrcLstHist
 * Description : Delete records from the EXT081 table.
 * Date         Changed By   Description
 * 20210408     RENARN       TARX03 - Add price list
 * 20211022     DUVCYR       Customer number no longer mandatory.
 */
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class DelPrcLstHist extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final ProgramAPI program
  private String iCUNO

  public DelPrcLstHist(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
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

    DBAction query = database.table("EXT081").index("00").build()
    DBContainer EXT081 = query.getContainer()
    EXT081.set("EXCONO", currentCompany);
    EXT081.set("EXPRRF", mi.in.get("PRRF"));
    EXT081.set("EXCUCD", mi.in.get("CUCD"));
    EXT081.set("EXCUNO", iCUNO);
    EXT081.set("EXFVDT", mi.in.get("FVDT") as Integer);
    EXT081.set("EXASCD", mi.in.get("ASCD"));
    EXT081.set("EXFDAT", mi.in.get("FDAT") as Integer);
    if(!query.readLock(EXT081, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> outDataEXT080 = { DBContainer EXT080 ->
    iCUNO = EXT080.get("EXCUNO")
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
