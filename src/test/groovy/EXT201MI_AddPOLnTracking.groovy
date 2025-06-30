/**
 * README
 * This extension is used by interface
 *
 * Name : EXT201MI.AddPOLnTracking
 * Description : Add external tracking on PO line
 * Date         Changed By   Description
 * 20230419     RENARN       LOG02-1-6 Ajout référence de livraison
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class AddPOLnTracking extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private int currentCompany
  private String currentDate
  private String currentTime
  private String iPUNO
  private Integer iPNLI
  private Integer iPNLS
  private String iETRN

  public AddPOLnTracking(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility, MICallerAPI miCaller) {
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

    // Get current date
    LocalDateTime timeOfCreation = LocalDateTime.now()
    currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    currentTime = timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss"))
    // Get Input parameters
    if(mi.in.get("PUNO") != null){
      iPUNO = mi.in.get("PUNO")
    }else{
      mi.error("Ordre achat est obligatoire")
      return
    }
    if(mi.in.get("PNLI") != null){
      iPNLI = (Integer)mi.in.get("PNLI")
    }else{
      mi.error("Ligne ordre achat est obligatoire")
      return
    }
    if(mi.in.get("PNLS") != null){
      iPNLS = (Integer)mi.in.get("PNLS")
    }else{
      mi.error("Suffixe de ligne ordre achat est obligatoire")
      return
    }
    if(mi.in.get("ETRN") != null){
      iETRN = mi.in.get("ETRN")
    }else{
      mi.error("Numéro tracabilité externe est obligatoire")
      return
    }
    logger.debug(String.format("Input parameters : PUNO=%s, PNLI=%s, PNLS=%s, ETRN=%s", iPUNO, String.valueOf(iPNLI), String.valueOf(iPNLS), iETRN))
    // Check input parameters' validity
    DBAction query
    query = database.table("MPLINE").index("00").selection("IBITNO","IBRORN","IBRORL","IBRORX").build()
    DBContainer MPLINE = query.getContainer()
    MPLINE.set("IBCONO", currentCompany)
    MPLINE.set("IBPUNO", iPUNO)
    MPLINE.setInt("IBPNLI", iPNLI)
    MPLINE.setInt("IBPNLS", iPNLS)
    if(!query.read(MPLINE)){
      mi.error(String.format("La ligne d'ordre d'achat %s/%s-%s n'existe pas", iPUNO, String.valueOf(iPNLI), String.valueOf(iPNLS)))
      return
    }
    // Add record in EXT201MI
    query = database.table("EXT201").index("00").build()
    DBContainer EXT201 = query.getContainer()
    EXT201.set("EXCONO", currentCompany)
    EXT201.set("EXPUNO", iPUNO)
    EXT201.setInt("EXPNLI", iPNLI)
    EXT201.setInt("EXPNLS", iPNLS)
    EXT201.set("EXETRN", iETRN)
    EXT201.setInt("EXCHNO", 0)
    if (!query.read(EXT201)){
      EXT201.setInt("EXRGDT", currentDate as Integer)
      EXT201.setInt("EXRGTM", currentTime as Integer)
      EXT201.setInt("EXLMDT", currentDate as Integer)
      EXT201.set("EXCHID", program.getUser())
      query.insert(EXT201)
    }else{
      mi.error(String.format("La référence %s est déjà rattachée à la ligne OA %s/%s-%s", iETRN, iPUNO, String.valueOf(iPNLI), String.valueOf(iPNLS)))
      return
    }
  }
}
