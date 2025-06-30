/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT053MI.UpdAssortItems
 * Description : Read EXT052 table, delete items from the assortment that no longer apply (CRS105MI/DltAssmItem) and add new items (CRS105MI/AddAssmItem)
 * Date         Changed By   Description
 * 20210429     RENARN       TARX02 - Add assortment
 * 20210830     RENARN       Minor fix on fdat
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class UpdAssortItems extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;
  private int currentCompany
  private String ascd
  private String cuno
  private String fdat
  private String itno

  public UpdAssortItems(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi;
    this.database = database;
    this.logger = logger;
    this.program = program;
    this.utility = utility;
    this.miCaller = miCaller
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer) program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
    }
    ascd = mi.in.get("ASCD")
    cuno = mi.in.get("CUNO")

    fdat ="";
    if (mi.in.get("FDAT") == null){
      mi.error("Date de début est obligatoire");
      return;
    } else {
      fdat = mi.in.get("FDAT");
      if (!utility.call("DateUtil", "isDateValid", fdat, "yyyyMMdd")) {
        mi.error("Date de début est invalide");
        return;
      }
    }
    logger.debug("EXT053MI_UpdAssortItems fdat = " + fdat)

    // Check selection header
    DBAction EXT050_query = database.table("EXT050").index("00").build()
    DBContainer EXT050 = EXT050_query.getContainer();
    EXT050.set("EXCONO", currentCompany);
    EXT050.set("EXASCD", ascd);
    EXT050.set("EXCUNO", cuno);
    EXT050.setInt("EXDAT1", fdat as Integer);
    if(!EXT050_query.readAll(EXT050, 4, EXT050_outData)){
      mi.error("Entête sélection n'existe pas");
      return;
    }

    // Delete of non-selected items present in the assortment
    ExpressionFactory expression = database.getExpressionFactory("OASITN")
    expression = expression.eq("OIFDAT", fdat)
    DBAction OASITN_query = database.table("OASITN").index("00").matching(expression).build()
    DBContainer OASITN = OASITN_query.getContainer();
    OASITN.set("OICONO", currentCompany);
    OASITN.set("OIASCD", ascd);
    if (!OASITN_query.readAll(OASITN, 2, OASITN_outData)) {
    }

    // Add of selected items that are not in the assortment
    DBAction EXT052_query = database.table("EXT052").index("00").selection("EXITNO").build();
    DBContainer EXT052 = EXT052_query.getContainer();
    EXT052.set("EXCONO", currentCompany);
    EXT052.set("EXASCD", ascd);
    EXT052.set("EXCUNO", cuno);
    EXT052.set("EXFDAT", fdat as Integer);
    if (!EXT052_query.readAll(EXT052, 4, EXT052_outData)) {
    }
  }

  Closure<?> EXT050_outData = { DBContainer EXT050 ->
  }
  Closure<?> EXT052_outData = { DBContainer EXT052 ->
    itno = EXT052.get("EXITNO")
    DBAction query = database.table("OASITN").index("00").build()
    DBContainer OASITN = query.getContainer();
    OASITN.set("OICONO", currentCompany);
    OASITN.set("OIASCD", ascd);
    OASITN.set("OIITNO", itno);
    OASITN.set("OIFDAT", fdat as Integer);
    if (!query.read(OASITN)) {
      logger.debug("logger EXT053MI executeCRS105MIAddAssmItem : ascd = " + ascd)
      logger.debug("logger EXT053MI executeCRS105MIAddAssmItem : itno = " + itno)
      logger.debug("logger EXT053MI executeCRS105MIAddAssmItem : fdat = " + fdat)
      // Add assortment item
      executeCRS105MIAddAssmItem(ascd, itno, fdat)
    }
  }
  Closure<?> OASITN_outData = { DBContainer OASITN ->
    itno = OASITN.get("OIITNO")
    DBAction EXT052_query = database.table("EXT052").index("00").selection("EXITNO").build();
    DBContainer EXT052 = EXT052_query.getContainer();
    EXT052.set("EXCONO", currentCompany);
    EXT052.set("EXASCD", ascd);
    EXT052.set("EXCUNO", cuno);
    EXT052.set("EXFDAT", fdat as Integer);
    EXT052.set("EXITNO", itno);
    if (!EXT052_query.read(EXT052)) {
      // Delete non-selected item from assortment
      executeCRS105MIDltAssmItem(ascd, itno, fdat)
    }
  }
  private executeCRS105MIAddAssmItem(String ASCD, String ITNO, String FDAT){
    def parameters = ["ASCD": ASCD, "ITNO": ITNO, "FDAT": FDAT]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
      } else {
      }
    }
    miCaller.call("CRS105MI", "AddAssmItem", parameters, handler)
  }
  private executeCRS105MIDltAssmItem(String ASCD, String ITNO, String FDAT){
    def parameters = ["ASCD": ASCD, "ITNO": ITNO, "FDAT": FDAT]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed CRS105MI.DltAssmItem: "+ response.errorMessage)
      } else {
      }
    }
    miCaller.call("CRS105MI", "DltAssmItem", parameters, handler)
  }
}
