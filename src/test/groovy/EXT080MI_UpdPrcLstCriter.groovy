/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT080MI.UpdPrcLstCriter
 * Description : Update records from the EXT080 table.
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

public class UpdPrcLstCriter extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final UtilityAPI utility
  private String cucd
  private String cuno
  private String fvdt
  private String lvdt
  private double zupa
  private String zipl
  private String pide
  private int zupd
  private String stat
  private String prrf
  private String buar
  private String whlo
  private int flag

  public UpdPrcLstCriter(MIAPI mi, DatabaseAPI database, ProgramAPI program,  UtilityAPI utility) {
    this.mi = mi
    this.database = database
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
    cucd = ""
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
    cuno = ""
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
    String fvdt = "0"
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
    lvdt = "0"
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
    zupa = 0
    if(mi.in.get("ZUPA") != null){
      zupa = mi.in.get("ZUPA") as double
      if(zupa < 0 || zupa > 3){
        mi.error("UPA doit être comprise entre 0 et 3")
        return
      }
    }

    // Check Incoterm place
    zipl = ""
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
    pide = ""
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
    zupd = 0
    if(mi.in.get("ZUPD") != null){
      zupd = mi.in.get("ZUPD")
      if(zupd != 0 && zupd != 1 && zupd != 2 && zupd != 3){
        mi.error("Mise à jour auto doit être 0, 1, 2 ou 3")
        return
      }
    }

    // Check status
    stat = ""
    if(mi.in.get("STAT") != null){
      stat = mi.in.get("STAT")
      if(stat != "10" && stat != "90"){
        mi.error("Statut est invalide")
        return
      }
    }

    // Check business area
    buar = ""
    if(mi.in.get("BUAR") != null){
      DBAction currencyQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = currencyQuery.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "BUAR")
      CSYTAB.set("CTSTKY", mi.in.get("BUAR"))
      if (!currencyQuery.read(CSYTAB)) {
        mi.error("Positionnement prix  " + mi.in.get("BUAR") + " n'existe pas")
        return
      }
      buar = mi.in.get("BUAR")
    }

    // Check warehouse
    whlo = ""
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
    flag = 0
    if(mi.in.get("FLAG") != null){
      flag = mi.in.get("FLAG") as Integer
      if(flag != 0 && flag != 1){
        mi.error("Flag coût logistique doit être 0 ou 1")
        return
      }
    }

    // Check price list
    prrf = ""
    if (mi.in.get("PRRF") != null){
      DBAction priceListQuery = database.table("OPRICH").index("00").build()
      DBContainer OPRICH = priceListQuery.getContainer()
      OPRICH.set("OJCONO",currentCompany)
      OPRICH.set("OJPRRF",mi.in.get("PRRF"))
      OPRICH.set("OJCUCD",mi.in.get("CUCD"))
      //OPRICH.set("OJCUNO",mi.in.get("CUNO"))
      OPRICH.set("OJFVDT", fvdt as Integer)
      if (!priceListQuery.read(OPRICH)) {
        mi.error("Tarif n'existe pas")
        return
      }
      prrf = mi.in.get("PRRF")
    }

    DBAction query = database.table("EXT080").index("00").build()
    DBContainer EXT080 = query.getContainer()
    EXT080.set("EXCONO", currentCompany)
    EXT080.set("EXPRRF", prrf)
    EXT080.set("EXCUCD", cucd)
    EXT080.set("EXCUNO", cuno)
    EXT080.set("EXFVDT", fvdt as Integer)
    if(!query.readLock(EXT080, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Update EXT080
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    if(mi.in.get("LVDT") != null) {
      lockedResult.set("EXLVDT", lvdt as Integer)
    }
    if(mi.in.get("ZUPA") != null) {
      lockedResult.set("EXZUPA", zupa)
    }
    if(mi.in.get("ZIPL") != null) {
      lockedResult.set("EXZIPL", zipl)
    }
    if(mi.in.get("PIDE") != null) {
      lockedResult.set("EXPIDE", pide)
    }
    if(mi.in.get("ZUPD") != null) {
      lockedResult.set("EXZUPD", zupd)
    }
    if(mi.in.get("STAT") != null) {
      lockedResult.set("EXSTAT", stat)
    }
    if(mi.in.get("BUAR") != null) {
      lockedResult.set("EXBUAR", buar)
    }
    if(mi.in.get("WHLO") != null) {
      lockedResult.set("EXWHLO", whlo)
    }
    if(mi.in.get("FLAG") != null) {
      lockedResult.set("EXFLAG", flag)
    }
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }

  Closure<?> outDataEXT031 = { DBContainer EXT031 ->
  }
  Closure<?> outDataOPROMH = { DBContainer OPROMH ->
  }
}
