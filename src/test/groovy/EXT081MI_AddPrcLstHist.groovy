/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT081MI.AddPrcLstHist
 * Description : Add records to the EXT081 table.
 * Date         Changed By   Description
 * 20210408     RENARN       TARX03 - Add price list
 * 20211018     RENARN       Customer number no longer mandatory
 * 20220207     RENARN       Check assortment removed
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class AddPrcLstHist extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility

  public AddPrcLstHist(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
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

    // Set price list
    String prrf = mi.in.get("PRRF")

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

    DBAction priceListQuery = database.table("EXT080").index("00").build()
    DBContainer EXT080 = priceListQuery.getContainer()
    EXT080.set("EXCONO", currentCompany)
    EXT080.set("EXPRRF", prrf)
    EXT080.set("EXCUCD", cucd)
    EXT080.set("EXCUNO", cuno)
    EXT080.set("EXFVDT", fvdt as Integer)
    if (!priceListQuery.read(EXT080)) {
      mi.error("Entête sélection n'existe pas")
      return
    }

    // Check assortment
    String ascd = ""
    if(mi.in.get("ASCD") != null){
      DBAction assortmentQuery = database.table("CSYTAB").index("00").build()
      DBContainer CSYTAB = assortmentQuery.getContainer()
      CSYTAB.set("CTCONO",currentCompany)
      CSYTAB.set("CTSTCO",  "ASCD")
      CSYTAB.set("CTSTKY", mi.in.get("ASCD"))
      if (!assortmentQuery.read(CSYTAB)) {
        mi.error("Assortiment " + mi.in.get("ASCD") + " n'existe pas")
        return
      }
      ascd = mi.in.get("ASCD")
    } else {
      mi.error("Assortiment est obligatoire")
      return
    }

    // Check Assortment From date
    String fdat = ""
    if(mi.in.get("FDAT") != null){
      fdat = mi.in.get("FDAT")
      if (!utility.call("DateUtil", "isDateValid", mi.in.get("FDAT"), "yyyyMMdd")) {
        mi.error("Date de début de validité de l'assortiment est invalide")
        return
      }
    } else {
      mi.error("Date de début de validité de l'assortiment est obligatoire")
      return
    }

    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT081").index("00").build()
    DBContainer EXT081 = query.getContainer()
    EXT081.set("EXCONO", currentCompany)
    EXT081.set("EXPRRF", prrf)
    EXT081.set("EXCUCD", cucd)
    EXT081.set("EXCUNO", cuno)
    EXT081.set("EXFVDT", fvdt as Integer)
    EXT081.set("EXASCD", ascd)
    EXT081.set("EXFDAT", fdat as Integer)
    if (!query.read(EXT081)) {
      EXT081.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT081.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT081.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT081.setInt("EXCHNO", 1)
      EXT081.set("EXCHID", program.getUser())
      query.insert(EXT081)
    } else {
      mi.error("L'enregistrement existe déjà")
      return
    }
  }
}
