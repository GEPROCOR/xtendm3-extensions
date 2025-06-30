/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT053MI.AddAssortItems
 * Description : Read EXT052 table and call "CRS105MI/AddAssmItem" for each item
 * Date         Changed By   Description
 * 20210429     RENARN       TARX02 - Add assortment
 * 20210830     RENARN       Minor fix on fdat
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class AddAssortItems extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;
  private int currentCompany
  private String ascd = ""
  private String cuno = ""
  private String fdat = ""
  private String itno = ""

  public AddAssortItems(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility, MICallerAPI miCaller) {
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
    logger.debug("EXT053MI_AddAssortItems fdat = " + fdat)

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
    logger.debug("logger EXT053MI executeCRS105MIAddAssmItem : ascd = " + ascd)
    logger.debug("logger EXT053MI executeCRS105MIAddAssmItem : itno = " + itno)
    logger.debug("logger EXT053MI executeCRS105MIAddAssmItem : fdat = " + fdat)
    executeCRS105MIAddAssmItem(ascd, itno, fdat)
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
}
