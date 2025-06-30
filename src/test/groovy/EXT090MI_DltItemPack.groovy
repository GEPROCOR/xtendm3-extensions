/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT090MI.DltItemPack
 * Description : The DltItemPack transaction delete records to the MITITP table. Management of this table in the MMS055 function
 * Date         Changed By   Description
 * 20210510     YYOU         Creation extension
 */

public class DltItemPack extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final ProgramAPI program
  private int currentCompany

  public DltItemPack(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi;
    this.database = database
    this.program = program
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    if(mi.in.get("ITNO") == null || mi.in.get("ITNO") == ""){
      mi.error("Article est obligatoire")
      return
    }else{
      DBAction Query = database.table("MITMAS").index("00").build()
      DBContainer MITMAS = Query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO",  mi.in.get("ITNO"))
      if (!Query.read(MITMAS)) {
        mi.error("Code article " + mi.in.get("ITNO") + " n'existe pas")
        return
      }
    }
    if(mi.in.get("PACT") == null || mi.in.get("PACT") == ""){
      mi.error("Packaging est obligatoire")
      return
    }else{
      DBAction Query = database.table("MITPAC").index("00").build()
      DBContainer MITPAC = Query.getContainer()
      MITPAC.set("M4CONO", currentCompany)
      MITPAC.set("M4PACT",  mi.in.get("PACT"))
      if (!Query.read(MITPAC)) {
        mi.error("Packaging " + mi.in.get("PACT") + " n'existe pas")
        return
      }
    }
    DBAction query = database.table("MITITP").index("00").selection("M5CHNO").build()
    DBContainer MITITP = query.getContainer()
    MITITP.set("M5CONO", currentCompany)
    MITITP.set("M5ITNO",  mi.in.get("ITNO"))
    MITITP.set("M5PACT",  mi.in.get("PACT"))
    if(!query.readLock(MITITP, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
