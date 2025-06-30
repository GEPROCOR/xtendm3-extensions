/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT100MI.UpdITM8
 * Description : Update records from the MITPOP table.
 * Date         Changed By   Description
 * 20210708     RENARN       REAX02 Recherche ITM8-EAN13
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class UpdITM8 extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final DatabaseAPI database;
  private final ProgramAPI program
  private final UtilityAPI utility;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private Integer currentCompany
  private String vfdt
  private String lvdt
  private String POPN
  private String E0PA
  private String ITNO
  private Integer	VFDT
  private Integer	LVDT
  private Integer	CNQT
  private String ALUN
  private String ORCO
  private Integer	SEQN
  private String REMK
  private Integer	CFIN
  private Integer TXID
  private String SEA1
  private String ATPE
  private Integer ATNR
  private String CHID
  private String PRNA


  public UpdITM8(MIAPI mi, LoggerAPI logger, DatabaseAPI database, ProgramAPI program, UtilityAPI utility) {
    this.mi = mi;
    this.logger = logger
    this.database = database
    this.program = program
    this.utility = utility;
  }

  public void main() {
    currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    if(mi.in.get("POPN") != null) {
      POPN = mi.in.get("POPN")
      if(POPN.length() != 8){
        mi.error("Longueur référence complémentaire doit être 8 (ITM8)");
        return;
      }
    }
    if(mi.in.get("E0PA") != null) {
      E0PA = mi.in.get("E0PA")
      if(E0PA.length() != 5){
        mi.error("Longueur partenaire doit être 5 (Code opération)");
        return;
      }
    }
    if(mi.in.get("VFDT") != null && mi.in.get("VFDT") != "0") {
      vfdt = mi.in.get("VFDT");
      logger.debug("MPT100MI vfdt = " + vfdt)
      if (!utility.call("DateUtil", "isDateValid", vfdt, "yyyyMMdd")) {
        mi.error("Date de début de validité est invalide");
        return;
      }
    }
    if(mi.in.get("LVDT") != null && mi.in.get("LVDT") != "0") {
      lvdt = mi.in.get("LVDT");
      if (!utility.call("DateUtil", "isDateValid", lvdt, "yyyyMMdd")) {
        mi.error("Date de fin de validité est invalide");
        return;
      }
    }
    DBAction query1 = database.table("MITPOP").index("20").selection("MPCNQT", "MPALUN", "MPORCO", "MPSEQN", "MPREMK", "MPCFIN", "MPTXID", "MPATPE", "MPATNR", "MPCHID", "MPPRNA").build()
    DBContainer MITPOP = query1.getContainer()
    MITPOP.set("MPCONO", currentCompany)
    MITPOP.set("MPALWT", 3)
    MITPOP.set("MPALWQ", "ITM8")
    MITPOP.set("MPPOPN", mi.in.get("POPN"))
    MITPOP.set("MPE0PA", mi.in.get("E0PA"))
    MITPOP.setInt("MPVFDT", vfdt as Integer);
    if(query1.readAll(MITPOP, 6, outData_1)){
      //mi.error("L'enregistrement existe déjà")
      //return
    } else {
      MITPOP.set("MPCONO", currentCompany)
      MITPOP.set("MPALWT", 3)
      MITPOP.set("MPALWQ", "ITM8")
      MITPOP.set("MPPOPN", mi.in.get("POPN"))
      MITPOP.set("MPE0PA", mi.in.get("E0PA"))
      MITPOP.setInt("MPVFDT", 0);
      if(!query1.readAll(MITPOP, 6, outData_2)){
        //mi.error("L'enregistrement n'existe pas")
        //return
      }
    }
  }
  Closure<?> outData_1 = { DBContainer MITPOP ->
  }
  Closure<?> outData_2 = { DBContainer MITPOP ->
    ITNO = MITPOP.get("MPITNO")
    SEA1 = MITPOP.get("MPSEA1")
    CNQT = MITPOP.get("MPCNQT")
    ALUN = MITPOP.get("MPALUN")
    ORCO = MITPOP.get("MPORCO")
    SEQN = MITPOP.get("MPSEQN")
    REMK = MITPOP.get("MPREMK")
    CFIN = MITPOP.get("MPCFIN")
    TXID = MITPOP.get("MPTXID")
    ATPE = MITPOP.get("MPATPE")
    ATNR = MITPOP.get("MPATNR")
    CHID = MITPOP.get("MPCHID")
    PRNA = MITPOP.get("MPPRNA")
    logger.debug("EXT100MI.UpdITM8 ALUN = " + ALUN)
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query2 = database.table("MITPOP").index("00").build()
    DBContainer MITPOP2 = query2.getContainer()
    MITPOP2.set("MPCONO", currentCompany)
    MITPOP2.set("MPALWT", 3)
    MITPOP2.set("MPALWQ", "ITM8")
    MITPOP2.set("MPITNO", ITNO)
    MITPOP2.set("MPPOPN", mi.in.get("POPN"))
    MITPOP2.set("MPE0PA", mi.in.get("E0PA"))
    MITPOP2.set("MPSEA1", SEA1)
    MITPOP2.setInt("MPVFDT", vfdt as Integer);
    if (!query2.read(MITPOP2)) {
      if (mi.in.get("LVDT") != null)
        MITPOP2.setInt("MPLVDT", lvdt as Integer);
      MITPOP2.set("MPCNQT", CNQT)
      MITPOP2.set("MPALUN", ALUN)
      MITPOP2.set("MPORCO", ORCO)
      MITPOP2.set("MPSEQN", SEQN)
      MITPOP2.set("MPREMK", REMK)
      MITPOP2.set("MPCFIN", CFIN)
      MITPOP2.set("MPTXID", TXID)
      MITPOP2.set("MPATPE", ATPE)
      MITPOP2.set("MPATNR", ATNR)
      MITPOP2.set("MPCHID", CHID)
      MITPOP2.set("MPPRNA", PRNA)
      MITPOP2.setInt("MPRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      MITPOP2.setInt("MPRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      MITPOP2.setInt("MPLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      MITPOP2.setInt("MPCHNO", 1)
      MITPOP2.set("MPCHID", program.getUser())
      query2.insert(MITPOP2)
    }
    DBAction query = database.table("MITPOP").index("00").build()
    DBContainer MITPOP3 = query.getContainer()
    MITPOP3.set("MPCONO", currentCompany)
    MITPOP3.set("MPALWT", 3)
    MITPOP3.set("MPALWQ", "ITM8")
    MITPOP3.set("MPITNO", ITNO)
    MITPOP3.set("MPPOPN", mi.in.get("POPN"))
    MITPOP3.set("MPE0PA", mi.in.get("E0PA"))
    MITPOP3.set("MPSEA1", SEA1)
    MITPOP3.setInt("MPVFDT", 0);
    if(!query.readLock(MITPOP3, updateCallBack)){
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
