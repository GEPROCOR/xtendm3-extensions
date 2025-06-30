/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT090MI.UpdItemPack
 * Description : The UpdItemPack transaction update records to the MITITP table. Management of this table in the MMS055 function
 * Date         Changed By   Description
 * 20210510     YYOU         REAX03 - Update Item Pack
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.text.DecimalFormat;

public class UpdItemPack extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany
  private final UtilityAPI utility;
  private String oPAMU
  private final TextFilesAPI textFiles
  private final Map<String, Long> nrOfCreates = new HashMap<>()
  private final Map<String, Long> nrOfReads = new HashMap<>()
  private final Map<String, Long> nrOfUpdates = new HashMap<>()
  private final Map<String, Long> nrOfDeletes = new HashMap<>()
  private final Set<String> tables = new HashSet<String>()
  private String logFileName
  private boolean IN60

  public UpdItemPack(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility, TextFilesAPI textFiles) {
    this.mi = mi;
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility;
    this.textFiles = textFiles
  }

  public void main() {
    if(mi.in.get("FPNM") == "EVS101")
      init()
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    int iDCCD=0
    if(mi.in.get("ITNO") == null || mi.in.get("ITNO") == ""){
      if(mi.in.get("FPNM") == "EVS101"){
        log("Article est obligatoire" + ";" + mi.in.get("ITNO") + ";" + mi.in.get("PACT") + ";" + mi.in.get("PAMU"))
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
          log("Code article " + mi.in.get("ITNO") + " n'existe pas" + ";" + mi.in.get("ITNO") + ";" + mi.in.get("PACT") + ";" + mi.in.get("PAMU"))
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
        log("Packaging est obligatoire" + ";" + mi.in.get("ITNO") + ";" + mi.in.get("PACT") + ";" + mi.in.get("PAMU"))
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
          log("Packaging " + mi.in.get("PACT") + " n'existe pas" + ";" + mi.in.get("ITNO") + ";" + mi.in.get("PACT") + ";" + mi.in.get("PAMU"))
        } else {
          mi.error("Packaging " + mi.in.get("PACT") + " n'existe pas")
          return
        }
      }
    }
    String iPAMU = ""
    if(mi.in.get("PAMU") == null || mi.in.get("PAMU") == ""){

    }else{
      iPAMU = mi.in.get("PAMU");
      if(!utility.call("NumberUtil","isValidNumber",iPAMU,".")){
        if(mi.in.get("FPNM") == "EVS101"){
          log("Format numérique incorrect" + ";" + mi.in.get("ITNO") + ";" + mi.in.get("PACT") + ";" + mi.in.get("PAMU"))
        } else {
          mi.error("Format numérique incorrect");
          return;
        }
      }
      /*
      int NbDecimal = utility.call("NumberUtil","getNumberOfDecimals",iPAMU,".")
      if(NbDecimal!=iDCCD){
        mi.error("Nombre décimal incorrect");
        return;
      }*/
    }

    if(mi.in.get("FPNM") == "EVS101" && IN60)
      return;

    oPAMU = ""
    //DecimalFormat f = new DecimalFormat();
    //f.setMaximumFractionDigits(iDCCD);
    //oPAMU= f.format(mi.in.get("PAMU"))
    oPAMU= mi.in.get("PAMU");

    DBAction query = database.table("MITITP").index("00").selection("M5CHNO").build()
    DBContainer MITITP = query.getContainer()
    MITITP.set("M5CONO", currentCompany)
    MITITP.set("M5ITNO",  mi.in.get("ITNO"))
    MITITP.set("M5PACT",  mi.in.get("PACT"))
    if(!query.readLock(MITITP, updateCallBack)){
      if(mi.in.get("FPNM") == "EVS101"){
        log("L'enregistrement n'existe pas" + ";" + mi.in.get("ITNO") + ";" + mi.in.get("PACT") + ";" + mi.in.get("PAMU"))
      } else {
        mi.error("L'enregistrement n'existe pas")
        return
      }
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("M5CHNO")
    lockedResult.set("M5PAMU", oPAMU as double)
    lockedResult.setInt("M5LMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("M5CHNO", changeNumber + 1)
    lockedResult.set("M5CHID", program.getUser())
    lockedResult.update()
  }
  void init() {
    IN60 = false
    textFiles.open("FileImport")
    logFileName = "MSG_" + program.getProgramName() + "." + "UpdItemPack" + ".csv"
    if(!textFiles.exists(logFileName))
      log("MSG;"+"ITNO;"+"PACT;"+"PAMU")
  }
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
