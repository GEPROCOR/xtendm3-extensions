/**
 * README
 * This extension is used by EXT120MI.CostPriceCalcul
 *
 * Name : EXT120MI.UpdLine
 * Description : Upd OOLINE
 * Date         Changed By   Description
 * 202109206     APACE       CDGX18 - Calculdu prix de revient
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdLine extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private final UtilityAPI utility


  private Integer currentCompany
  public UpdLine(MIAPI mi, DatabaseAPI database, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility, LoggerAPI logger) {
    this.mi = mi
    this.database = database
    this.program = program
    this.miCaller = miCaller
    this.logger = logger
    this.utility = utility
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    if(mi.in.get("ORNO") != null){
      DBAction RechercheOOHEAD = database.table("OOHEAD").index("00").build()
      DBContainer OOHEAD = RechercheOOHEAD.getContainer()
      OOHEAD.set("OACONO", currentCompany)
      OOHEAD.set("OAORNO", mi.in.get("ORNO"))
      if(!RechercheOOHEAD.read(OOHEAD)){
        mi.error("La commande n'existe pas")
        return
      }
    }else{
      mi.error("Le N° de commande est obligatoire")
      return
    }

    if(mi.in.get("PONR") != null && mi.in.get("POSX") != null){
      DBAction RechercheOOLINE = database.table("OOLINE").index("00").build()
      DBContainer OOLINE = RechercheOOLINE.getContainer()
      OOLINE.set("OBCONO", currentCompany)
      OOLINE.set("OBORNO", mi.in.get("ORNO"))
      OOLINE.set("OBPONR", mi.in.get("PONR") as Integer)
      OOLINE.set("OBPOSX", mi.in.get("POSX") as Integer)
      if(!RechercheOOLINE.readLock(OOLINE, updateCallBack)){
        mi.error("La ligne de commande n'existe pas")
        return
      }
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("OBCHNO")
    if(mi.in.get("UCA1")!=null){
      lockedResult.set("OBUCA1", mi.in.get("UCA1"))
    }
    if(mi.in.get("UCA2")!=null){
      lockedResult.set("OBUCA2", mi.in.get("UCA2"))
    }
    if(mi.in.get("UCA3")!=null){
      lockedResult.set("OBUCA3", mi.in.get("UCA3"))
    }
    if(mi.in.get("UCA4")!=null){
      lockedResult.set("OBUCA4", mi.in.get("UCA4"))
    }
    if(mi.in.get("UCA5")!=null){
      lockedResult.set("OBUCA5", mi.in.get("UCA5"))
    }
    if(mi.in.get("UCA6")!=null){
      lockedResult.set("OBUCA6", mi.in.get("UCA6"))
    }
    if(mi.in.get("UCA7")!=null){
      lockedResult.set("OBUCA7", mi.in.get("UCA7"))
    }
    if(mi.in.get("UCA8")!=null){
      lockedResult.set("OBUCA8", mi.in.get("UCA8"))
    }
    if(mi.in.get("UCA9")!=null){
      lockedResult.set("OBUCA9", mi.in.get("UCA9"))
    }
    if(mi.in.get("UCA0")!=null){
      lockedResult.set("OBUCA0", mi.in.get("UCA0"))
    }
    if(mi.in.get("UDN1")!=null){
      lockedResult.setDouble("OBUDN1", mi.in.get("UDN1") as Double)
    }
    if(mi.in.get("UDN2")!=null){
      lockedResult.setDouble("OBUDN2", mi.in.get("UDN2") as Double)
    }
    if(mi.in.get("UDN3")!=null){
      lockedResult.setDouble("OBUDN3", mi.in.get("UDN3") as Double)
    }
    if(mi.in.get("UDN4")!=null){
      lockedResult.setDouble("OBUDN4", mi.in.get("UDN4") as Double)
    }
    if(mi.in.get("UDN5")!=null){
      lockedResult.setDouble("OBUDN5", mi.in.get("UDN5") as Double)
    }
    if(mi.in.get("UDN6")!=null){
      lockedResult.setDouble("OBUDN6", mi.in.get("UDN6") as Double)
    }
    if(mi.in.get("UID1")!=null){
      lockedResult.setInt("OBUID1", mi.in.get("UID1") as Integer)
    }
    if(mi.in.get("UID2")!=null){
      lockedResult.setInt("OBUID2", mi.in.get("UID2") as Integer)
    }
    if(mi.in.get("UID3")!=null){
      lockedResult.setInt("OBUID3", mi.in.get("UID3") as Integer)
    }
    if(mi.in.get("UCT1")!=null){
      lockedResult.set("OBUCT1", mi.in.get("UCT1"))
    }
    if(mi.in.get("INPR")!=null){
      lockedResult.setInt("OBINPR", mi.in.get("INPR") as Integer)
    }
    lockedResult.setInt("OBLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("OBCHNO", changeNumber + 1)
    lockedResult.set("OBCHID", program.getUser())
    lockedResult.update()
  }
}
