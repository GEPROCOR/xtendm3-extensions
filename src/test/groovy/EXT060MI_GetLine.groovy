/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT060MI.GetLine
 * Description : The GetLine transaction get records to the OXLINE or OOLINE table. 
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX04 - Recherche article
 * 20220426     RENARN       lowerCamelCase has been fixed
 */

public class GetLine extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private Integer currentCompany

  public GetLine(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
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

    if(mi.in.get("ORNO") == null || mi.in.get("ORNO") == ""){
      mi.error("Numéro Commande est obligatoire")
      return
    }

    if(mi.in.get("PONR") == null || mi.in.get("PONR") == ""){
      mi.error("Numéro Ligne Commande est obligatoire")
      return
    }

    if(mi.in.get("POSX") == null || mi.in.get("POSX") == ""){
      mi.error("Numéro Ss-Ligne Commande est obligatoire")
      return
    }

    DBAction query_OXLINE = database.table("OXLINE").index("00").selection("OBPLDT").build()
    DBContainer OXLINE = query_OXLINE.getContainer()
    OXLINE.set("OBCONO", currentCompany)
    OXLINE.set("OBORNO",  mi.in.get("ORNO"))
    OXLINE.set("OBPONR",  mi.in.get("PONR"))
    OXLINE.set("OBPOSX",  mi.in.get("POSX"))
    if(!query_OXLINE.readAll(OXLINE, 4, outData_OXLINE)){
      DBAction query = database.table("OOLINE").index("00").selection("OBPLDT").build()
      DBContainer OOLINE = query.getContainer()
      OOLINE.set("OBCONO", currentCompany)
      OOLINE.set("OBORNO",  mi.in.get("ORNO"))
      OOLINE.set("OBPONR",  mi.in.get("PONR"))
      OOLINE.set("OBPOSX",  mi.in.get("POSX"))
      if(!query.readAll(OOLINE, 4, outData)){
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }
  // Retrieve OOLINE
  Closure<?> outData = { DBContainer OOLINE ->
    String datePlanif = OOLINE.get("OBPLDT")
    mi.outData.put("PLDT", datePlanif)
    mi.write()
  }
  // Retrieve OXLINE
  Closure<?> outData_OXLINE = { DBContainer OXLINE ->
    String datePlanif = OXLINE.get("OBPLDT")
    mi.outData.put("PLDT", datePlanif)
    mi.write()
  }
}
