/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT090MI.LstItemPack
 * Description : The LstItemPack transaction list records to the MITITP table. Management of this table in the MMS055 function
 * Date         Changed By   Description
 * 20210510     YYOU         Creation extension
 */

import java.text.DecimalFormat;

public class LstItemPack extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany

  public LstItemPack(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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
    if (mi.in.get("ITNO") == null && mi.in.get("PACT") == null) {
      DBAction query = database.table("MITITP").index("00").selection("M5CONO", "M5ITNO", "M5PACT", "M5PAMU").build()
      DBContainer MITITP = query.getContainer()
      MITITP.set("M5CONO", currentCompany)
      if(!query.readAll(MITITP, 1, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }else{
      if (mi.in.get("ITNO") != null && mi.in.get("PACT") != null) {
        DBAction query = database.table("MITITP").index("00").selection("M5CONO", "M5ITNO", "M5PACT", "M5PAMU").build()
        DBContainer MITITP = query.getContainer()

        if(!query.readAll(MITITP, 3, outData)){
          mi.error("L'enregistrement n'existe pas")
          return
        }
      }else{
        if (mi.in.get("ITNO") != null && mi.in.get("PACT") == null) {
          DBAction query = database.table("MITITP").index("00").selection("M5CONO", "M5ITNO", "M5PACT", "M5PAMU").build()
          DBContainer MITITP = query.getContainer()
          MITITP.set("M5CONO", currentCompany)
          MITITP.set("M5ITNO",  mi.in.get("ITNO"))
          if(!query.readAll(MITITP, 2, outData)){
            mi.error("L'enregistrement n'existe pas")
            return
          }
        }
        if (mi.in.get("ITNO") == null && mi.in.get("PACT") != null) {
          DBAction query = database.table("MITITP").index("10").selection("M5CONO", "M5ITNO", "M5PACT", "M5PAMU").build()
          DBContainer MITITP = query.getContainer()
          MITITP.set("M5CONO", currentCompany)
          MITITP.set("M5PACT",  mi.in.get("PACT"))
          if(!query.readAll(MITITP, 2, outData)){
            mi.error("L'enregistrement n'existe pas")
            return
          }
        }
      }
    }
  }
  Closure<?> outData = { DBContainer MITITP ->
    String oCONO = MITITP.get("M5CONO")
    String oITNO = MITITP.get("M5ITNO")
    String oPACT = MITITP.get("M5PACT")
    String oPAMU = ""
    mi.outData.put("CONO", oCONO)
    mi.outData.put("ITNO", oITNO)
    mi.outData.put("PACT", oPACT)
    int iDCCD=0
    DBAction Query = database.table("MITMAS").index("00").selection("MMDCCD").build()
    DBContainer MITMAS = Query.getContainer()
    MITMAS.set("MMCONO", MITITP.get("M5CONO"))
    MITMAS.set("MMITNO", MITITP.get("M5ITNO"))
    if (Query.read(MITMAS)) {
      iDCCD=(Integer)MITMAS.get("MMDCCD")
    }
    DecimalFormat f = new DecimalFormat();
    f.setMaximumFractionDigits(iDCCD);
    oPAMU=f.format(MITITP.get("M5PAMU"))
    mi.outData.put("PAMU", oPAMU)
    mi.write()
  }
}
