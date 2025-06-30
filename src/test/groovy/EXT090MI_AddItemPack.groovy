/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT090MI.AddItemPack
 * Description : The AddItemPack transaction adds records to the MITITP table. Management of this table in the MMS055 function
 * Date         Changed By   Description
 * 20210510     YYOU         REAX03 - Add Item Pack
 * 20220413     RENARN       Method comment added, semicolon removed
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.text.DecimalFormat

public class AddItemPack extends ExtendM3Transaction {
  private final MIAPI mi
  private final ProgramAPI program
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final UtilityAPI utility
  private final MICallerAPI miCaller
  private final TextFilesAPI textFiles
  private final Map<String, Long> nrOfCreates = new HashMap<>()
  private final Map<String, Long> nrOfReads = new HashMap<>()
  private final Map<String, Long> nrOfUpdates = new HashMap<>()
  private final Map<String, Long> nrOfDeletes = new HashMap<>()
  private final Set<String> tables = new HashSet<String>()
  private String logFileName
  private boolean IN60
  private Integer currentCompany

  public AddItemPack(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility, MICallerAPI miCaller, TextFilesAPI textFiles) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
    this.miCaller = miCaller
    this.textFiles = textFiles
  }
  public void main() {
    //if(mi.in.get("FPNM") == "EVS101")
    //  init()
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    int iDCCD=0
    if(mi.in.get("ITNO") == null || mi.in.get("ITNO") == ""){
      if(mi.in.get("FPNM") == "EVS101"){
        logMessage("Article est obligatoire" + ";" + mi.in.get("ITNO") + ";" + mi.in.get("PACT") + ";" + mi.in.get("PAMU"))
      } else {
        mi.error("Article est obligatoire")
        return
      }
    }else{
      DBAction Query = database.table("MITMAS").index("00").selection("MMDCCD").build()
      DBContainer MITMAS = Query.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO",  mi.in.get("ITNO"))
      if (!Query.read(MITMAS)) {
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Code article " + mi.in.get("ITNO") + " n'existe pas" + ";" + mi.in.get("ITNO") + ";" + mi.in.get("PACT") + ";" + mi.in.get("PAMU"))
        } else {
          mi.error("Code article " + mi.in.get("ITNO") + " n'existe pas")
          return
        }
      }else{
        iDCCD=(Integer)MITMAS.get("MMDCCD")
      }
    }
    if(mi.in.get("PACT") == null || mi.in.get("PACT") == ""){
      if(mi.in.get("FPNM") == "EVS101"){
        logMessage("Packaging est obligatoire" + ";" + mi.in.get("ITNO") + ";" + mi.in.get("PACT") + ";" + mi.in.get("PAMU"))
      } else {
        mi.error("Packaging est obligatoire")
        return
      }
    }else{
      DBAction Query = database.table("MITPAC").index("00").build()
      DBContainer MITPAC = Query.getContainer()
      MITPAC.set("M4CONO", currentCompany)
      MITPAC.set("M4PACT",  mi.in.get("PACT"))
      if (!Query.read(MITPAC)) {
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Packaging " + mi.in.get("PACT") + " n'existe pas" + ";" + mi.in.get("ITNO") + ";" + mi.in.get("PACT") + ";" + mi.in.get("PAMU"))
        } else {
          mi.error("Packaging " + mi.in.get("PACT") + " n'existe pas")
          return
        }
      }
    }
    String iPAMU = ""
    if(mi.in.get("PAMU") == null || mi.in.get("PAMU") == ""){
      if(mi.in.get("FPNM") == "EVS101"){
        logMessage("PAMU est obligatoire" + ";" + mi.in.get("ITNO") + ";" + mi.in.get("PACT") + ";" + mi.in.get("PAMU"))
      } else {
        mi.error("PAMU est obligatoire")
        return
      }
    }else{
      iPAMU = mi.in.get("PAMU")
      if(!utility.call("NumberUtil","isValidNumber",iPAMU,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          logMessage("Format numérique incorrect" + ";" + mi.in.get("ITNO") + ";" + mi.in.get("PACT") + ";" + mi.in.get("PAMU"))
        } else {
          mi.error("Format numérique incorrect")
          return
        }
      }
      /*
      int NbDecimal = utility.call("NumberUtil","getNumberOfDecimals",iPAMU,".")
      if(NbDecimal!=iDCCD){
        mi.error("Nombre décimal incorrect")
        return
      }*/
    }

    if(mi.in.get("FPNM") == "EVS101" && IN60)
      return

    String oPAMU = ""
    //DecimalFormat f = new DecimalFormat()
    //f.setMaximumFractionDigits(iDCCD)
    //oPAMU= f.format(mi.in.get("PAMU"))
    oPAMU= mi.in.get("PAMU")

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("MITITP").index("00").build()
    DBContainer MITITP = query.getContainer()
    MITITP.set("M5CONO", currentCompany)
    MITITP.set("M5ITNO",  mi.in.get("ITNO"))
    MITITP.set("M5PACT",  mi.in.get("PACT"))
    if (!query.read(MITITP)) {
      //MITITP.set("M5PAMU", mi.in.get("PAMU") as double)
      MITITP.set("M5PAMU", oPAMU as double)
      MITITP.setInt("M5RGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      MITITP.setInt("M5RGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      MITITP.setInt("M5LMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      MITITP.setInt("M5CHNO", 1)
      MITITP.set("M5CHID", program.getUser())
      query.insert(MITITP)
    } else {
      if(mi.in.get("FPNM") == "EVS101"){
        //If EVS101 do update
        MITITP.set("M5CONO", currentCompany)
        MITITP.set("M5ITNO",  mi.in.get("ITNO"))
        MITITP.set("M5PACT",  mi.in.get("PACT"))
        if(!query.readLock(MITITP, updateCallBack)){
          if(mi.in.get("FPNM") == "EVS101"){
            logMessage("L'enregistrement ne peux pas ce mettre à jour" + ";" + mi.in.get("ITNO") + ";" + mi.in.get("PACT") + ";" + mi.in.get("PAMU"))
          } else {
            mi.error("L'enregistrement ne peux pas ce mettre à jour")
            return
          }
        }
      } else {
        mi.error("L'enregistrement existe déjà")
        return
      }
    }
  }
  // Init
  void init() {
    IN60 = false
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("M5CHNO")
    lockedResult.set("M5PAMU", mi.in.get("PAMU") as double)
    lockedResult.setInt("M5LMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("M5CHNO", changeNumber + 1)
    lockedResult.set("M5CHID", program.getUser())
    lockedResult.update()
  }
  // Log message
  void logMessage(String message) {
    textFiles.open("FileImport")
    logFileName = "MSG_" + program.getProgramName() + "." + "AddItemPack" + ".csv"
    if(!textFiles.exists(logFileName)) {
      log("MSG;"+"ITNO;"+"PACT;"+"PAMU")
      log(message)
    }
  }
  // Log
  void log(String message) {
    IN60 = true
    //logger.info(message)
    message = LocalDateTime.now().toString() + ";" + message
    Closure<?> consumer = { PrintWriter printWriter ->
      printWriter.println(message)
    }
    textFiles.write(logFileName, "UTF-8", true, consumer)
  }
}
