/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT170MI.UpdPOP
 * Description : Update planned purchase order
 * Date         Changed By   Description
 * 20211213     RENARN       APPX20 - Purchase agreement retrieving
 * 20220228     RENARN       DLDT has been added
 * 20220317     RENARN       Calculation and update of the PLDT has been added
 * 20220425     RENARN       Useless line has been removed
 * 20240919     YVOYOU       Adaptation KB231988 - MMS200MI AFLM
 */
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.GregorianCalendar
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
public class UpdPOP extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private int currentCompany
  private String currentDate
  private Integer iPLPN = 0
  private Integer iPLPS = 0
  private Integer iPLP2 = 0
  private String site = ""
  private String pitd = ""
  private String elno = ""
  private String ourr = ""
  private String dldt = ""
  private String whlo = ""
  private String itno = ""
  private Integer pldt
  private String LEAT
  private String LEA1
  private Integer leat
  private Integer lea1
  private boolean IN60

  public UpdPOP(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
    this.miCaller = miCaller
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    if(mi.in.get("PLPN") != null){
      iPLPN = (Integer)mi.in.get("PLPN")
    }else{
      mi.error("Ordre planifié est obligatoire")
      return
    }

    if(mi.in.get("PLPS") != null)
      iPLPS = (Integer)mi.in.get("PLPS")

    if(mi.in.get("PLP2") != null)
      iPLP2 = (Integer)mi.in.get("PLP2")

    if(mi.in.get("SITE") != null)
      site = mi.in.get("SITE")

    if(mi.in.get("PITD") != null)
      pitd = mi.in.get("PITD")

    if(mi.in.get("ELNO") != null)
      elno = mi.in.get("ELNO")

    if(mi.in.get("OURR") != null)
      ourr = mi.in.get("OURR")

    // Check delivery date
    dldt = ""
    if(mi.in.get("DLDT") != null){
      dldt = mi.in.get("DLDT")
      if (!utility.call("DateUtil", "isDateValid", mi.in.get("DLDT"), "yyyyMMdd")) {
        mi.error("Date de livraison est invalide")
        return
      }
    }

    DBAction query = database.table("MPOPLP").index("00").selection("POWHLO","POITNO","POGETY","PORORN","PORORL","PORORX").build()
    DBContainer MPOPLP = query.getContainer()
    MPOPLP.set("POCONO", currentCompany)
    MPOPLP.setInt("POPLPN", iPLPN)
    MPOPLP.setInt("POPLPS", iPLPS)
    MPOPLP.setInt("POPLP2", iPLP2)
    if (query.read(MPOPLP)) {
      whlo = MPOPLP.get("POWHLO")
      itno = MPOPLP.get("POITNO")
      logger.debug("whlo = " + whlo)
      logger.debug("itno = " + itno)
      // Retrieve lead time from item warhouse
      leat = 0
      lea1 = 0
      executeMMS200MIGetItmWhsBasic(whlo, itno)
      leat = LEAT as Integer
      lea1 = LEA1 as Integer
      logger.debug("leat = " + leat as String)
      logger.debug("lea1 = " + lea1 as String)
      if (dldt != "" && (leat != 0 || lea1 != 0)) {
        // Calculating the planned date
        // = DLDT received as parameter + lead time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        LocalDate DATE = LocalDate.parse(dldt, formatter)
        DATE = DATE.plusDays(leat - lea1)
        pldt = DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
        logger.debug("pldt1 = " + pldt)
        // Update planned PO
        executePPS170MIUpdPOP(iPLPN as String, iPLPS as String, iPLP2 as String, pldt as String)
        logger.debug("IN60 = " + IN60)
        if(IN60){
          IN60 = false
          DATE = DATE.plusDays(1)
          pldt = DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
          logger.debug("pldt2 = " + pldt)
          executePPS170MIUpdPOP(iPLPN as String, iPLPS as String, iPLP2 as String, pldt as String)
          if(IN60){
            DATE = DATE.plusDays(1)
            pldt = DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
            logger.debug("pldt3 = " + pldt)
            executePPS170MIUpdPOP(iPLPN as String, iPLPS as String, iPLP2 as String, pldt as String)
          }
        }
      }
    }

    MPOPLP.set("POCONO", currentCompany)
    MPOPLP.setInt("POPLPN", iPLPN)
    MPOPLP.setInt("POPLPS", iPLPS)
    MPOPLP.setInt("POPLP2", iPLP2)
    if(!query.readLock(MPOPLP, updateCallBack)){
    }
  }
  // Update MPOPLP
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("POCHNO")
    if(site.trim() != "")
      lockedResult.set("POSITE", site)
    if(pitd.trim() != "")
      lockedResult.set("POPITD", pitd)
    if(elno.trim() != "")
      lockedResult.set("POELNO", elno)
    if(ourr.trim() != "")
      lockedResult.set("POOURR", ourr)
    if(dldt.trim() != "")
      lockedResult.set("PODLDT", dldt as Integer)
    lockedResult.setInt("POLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("POCHNO", changeNumber + 1)
    lockedResult.set("POCHID", program.getUser())
    lockedResult.update()
  }
  // Execute PPS170MI.UpdPOP
  private executePPS170MIUpdPOP(String PLPN, String PLPS, String PLP2, String PLDT){
    def parameters = ["PLPN": PLPN, "PLPS": PLPS, "PLP2": PLP2, "PLDT": PLDT]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        IN60 = true
        //return mi.error("Failed PPS170MI.UpdPOP: "+ response.errorMessage)
        //logger.debug("message = " + response.errorMessage)
      } else {
      }
    }
    miCaller.call("PPS170MI", "UpdPOP", parameters, handler)
  }
  // Exceute MMS200MI.GetItmWhsBasic
  private executeMMS200MIGetItmWhsBasic(String WHLO, String ITNO){
    def parameters = ["WHLO": WHLO, "ITNO": ITNO, "AFLM": "1"]
    Closure<?> handler = { Map<String, String> response ->
      LEAT = response.LEAT.trim()
      LEA1 = response.LEA1.trim()
      if (response.error != null) {
        return mi.error("Failed MMS200MI.GetItmWhsBasic: "+ response.errorMessage)
      }
    }
    miCaller.call("MMS200MI", "GetItmWhsBasic", parameters, handler)
  }

}
