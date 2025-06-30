/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT080MI.AddPrcLstCriter
 * Description : Add records to the EXT080 table.
 * Date         Changed By   Description
 * 20210408     RENARN       TARX03 - Add price list
 * 20211018     RENARN       Customer number no longer mandatory. New input parameter : business area
 * 20211206     RENARN       New input parameter : warehouse
 * 20231103     RENARN       New input parameter : flag coût logistique
 * 20240625		  RENARN		   Snake case has been fixed
 * 20240331		  YVOYOU		   Change IPDE string 10
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class AddPrcLstCriter extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility

  public AddPrcLstCriter(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    String currentDivision = program.getLDAZD().DIVI

    // Check currency
    String cucd = ""
    if(mi.in.get("CUCD") != null){
      DBAction currencyQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = currencyQuery.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "CUCD")
      CSYTAB.set("CTSTKY", mi.in.get("CUCD"))
      if (!currencyQuery.read(CSYTAB)) {
        mi.error("Devise  " + mi.in.get("CUCD") + " n'existe pas")
        return
      }
      cucd = mi.in.get("CUCD")
    } else {
      mi.error("Devise est obligatoire")
      return
    }

    // Check customer
    String cuno = ""
    if(mi.in.get("CUNO") != null){
      DBAction customerQuery = database.table("OCUSMA").index("00").build()
      DBContainer OCUSMA = customerQuery.getContainer()
      OCUSMA.set("OKCONO",currentCompany)
      OCUSMA.set("OKCUNO",mi.in.get("CUNO"))
      if (!customerQuery.read(OCUSMA)) {
        mi.error("Code client " + mi.in.get("CUNO") + " n'existe pas")
        return
      }
      cuno = mi.in.get("CUNO")
    }

    // Check From date
    String fvdt = ""
    if(mi.in.get("FVDT") != null){
      fvdt = mi.in.get("FVDT")
      if (!utility.call("DateUtil", "isDateValid", mi.in.get("FVDT"), "yyyyMMdd")) {
        mi.error("Date de début de validité est invalide")
        return
      }
    } else {
      mi.error("Date de début de validité est obligatoire")
      return
    }

    // Check To date
    String lvdt = "0"
    if(mi.in.get("LVDT") != null){
      lvdt = mi.in.get("LVDT")
      if (!utility.call("DateUtil", "isDateValid", mi.in.get("LVDT"), "yyyyMMdd")) {
        mi.error("Date de fin de validité est invalide")
        return
      }
    }

    // Check From date vs To date
    if((mi.in.get("LVDT") != null) && lvdt < fvdt){
      mi.error("La date de fin de validité ne peut pas être inférrieure à la date de début de validité")
      return
    }

    // Check UPA degradation
    double zupa = 0
    if(mi.in.get("ZUPA") != null){
      zupa = mi.in.get("ZUPA") as double
      if(zupa < 0 || zupa > 3){
        mi.error("UPA doit être comprise entre 0 et 3")
        return
      }
    }

    // Check Incoterm place
    String zipl = ""
    if(mi.in.get("ZIPL") != null) {
      DBAction incotermPlaceQuery = database.table("EXT031").index("10").selection("EXZIPL").build()
      DBContainer EXT031 = incotermPlaceQuery.getContainer()
      EXT031.set("EXCONO", currentCompany)
      EXT031.set("EXZIPL", mi.in.get("ZIPL"))
      if (!incotermPlaceQuery.readAll(EXT031, 2, outDataEXT031)) {
        mi.error("Incoterm lieu " + mi.in.get("ZIPL") + " n'existe pas")
      }
      zipl = mi.in.get("ZIPL")
    }

    // Check promotion
    String pide = ""
    if(mi.in.get("PIDE") != null) {
      DBAction promotionQuery = database.table("OPROMH").index("00").selection("FZPIDE").build()
      DBContainer OPROMH = promotionQuery.getContainer()
      OPROMH.set("FZCONO", currentCompany)
      OPROMH.set("FZDIVI", currentDivision)
      OPROMH.set("FZPIDE", mi.in.get("PIDE"))
      if (!promotionQuery.readAll(OPROMH, 3, outDataOPROMH)) {
        mi.error("Promotion " + mi.in.get("PIDE") + " n'existe pas")
      }
      pide = mi.in.get("PIDE")
    }

    // Check automatic update
    int zupd = 0
    if(mi.in.get("ZUPD") != null){
      zupd = mi.in.get("ZUPD")
      if(zupd != 0 && zupd != 1 && zupd != 2 && zupd != 3){
        mi.error("Mise à jour auto doit être 0, 1, 2 ou 3")
        return
      }
    }

    // Check status
    String stat = ""
    if(mi.in.get("STAT") != null){
      stat = mi.in.get("STAT")
      if(stat != "10" && stat != "90"){
        mi.error("Statut est invalide")
        return
      }
    }

    // Check business area
    String buar = ""
    if(mi.in.get("BUAR") != null){
      DBAction queryBuar = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = queryBuar.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "BUAR")
      CSYTAB.set("CTSTKY", mi.in.get("BUAR"))
      if (!queryBuar.read(CSYTAB)) {
        mi.error("Positionnement prix  " + mi.in.get("BUAR") + " n'existe pas")
        return
      }
      buar = mi.in.get("BUAR")
    }

    // Check warehouse
    String whlo = ""
    if(mi.in.get("WHLO") != null) {
      DBAction queryWhlo = database.table("MITWHL").index("00").build()
      DBContainer MITWHL = queryWhlo.getContainer()
      MITWHL.set("MWCONO", currentCompany)
      MITWHL.set("MWWHLO", mi.in.get("WHLO"))
      if (!queryWhlo.read(MITWHL)) {
        mi.error("Dépôt " + mi.in.get("WHLO") + " n'existe pas")
        return
      }
      whlo = mi.in.get("WHLO")
    }

    // Check flag coût logistique
    int flag = 0
    if(mi.in.get("FLAG") != null){
      flag = mi.in.get("FLAG") as Integer
      if(flag != 0 && flag != 1){
        mi.error("Flag coût logistique doit être 0 ou 1")
        return
      }
    }

    // Set price list
    String prrf = mi.in.get("PRRF")

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT080").index("00").build()
    DBContainer EXT080 = query.getContainer()
    EXT080.set("EXCONO", currentCompany)
    EXT080.set("EXPRRF", prrf)
    EXT080.set("EXCUCD", cucd)
    EXT080.set("EXCUNO", cuno)
    EXT080.set("EXFVDT", fvdt as Integer)
    if (!query.read(EXT080)) {
      EXT080.set("EXLVDT", lvdt as Integer)
      EXT080.set("EXZUPA", zupa)
      EXT080.set("EXZIPL", zipl)
      EXT080.set("EXPIDE", pide)
      EXT080.set("EXZUPD", zupd as Integer)
      EXT080.set("EXSTAT", stat)
      EXT080.set("EXBUAR", buar)
      EXT080.set("EXWHLO", whlo)
      EXT080.set("EXFLAG", flag)
      EXT080.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT080.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT080.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT080.setInt("EXCHNO", 1)
      EXT080.set("EXCHID", program.getUser())
      query.insert(EXT080)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }

  Closure<?> outDataEXT031 = { DBContainer EXT031 ->
  }
  Closure<?> outDataOPROMH = { DBContainer OPROMH ->
  }
}
