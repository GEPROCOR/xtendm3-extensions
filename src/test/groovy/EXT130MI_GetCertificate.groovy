/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT130MI.GetCertificate
 * Description : Get certificate
 * Date         Changed By   Description
 * 20211123     RENARN       QUAX19 - Retrieve certificate
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class GetCertificate extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private String currentDivision
  private String orno = ""
  private Integer ponr = 0
  private Integer posx = 0
  private Integer dlix = 0
  private Integer zcid = 0
  private String doid = ""
  private String phno = ""
  private String phn2 = ""
  private String cuno = ""
  private String itno = ""
  private String town = ""
  private String cscd = ""
  private String csc2 = ""
  private String orco = ""
  private String zdes = ""
  private Integer znco = 0
  private Integer conn = 0
  private Integer dsdt = 0
  private Integer dsd2 = 0
  private String zisv = ""
  private String znal = ""
  private String zpcb = ""
  private String modl = ""
  private String dcpo = ""
  private String venr = ""
  private String sea0 = ""
  private String sea1 = ""
  private double newe = 0
  private double grwe = 0
  private String csno = ""
  private String suno = ""
  private String itty = ""
  private String zban = ""
  private String bref = ""
  private String ztem = ""
  private String zcem = ""
  private Integer expi = 0
  private String zcua = ""
  private String zsea = ""
  private Integer zcnd = 0
  private String sun1 = ""
  private String zadr = ""
  private String spe1 = ""
  private Integer zdfb = 0
  private String zdsd = 0
  private String zaet = ""
  private String znaf = ""
  private String zcty = ""
  private String divi = ""
  private String stat = ""

  public GetCertificate(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    DBAction query = database.table("EXT130").index("00").selection("EXCUNO","EXZCUA","EXTOWN","EXCSCD","EXZNCO","EXCSC2","EXCONN","EXDSD2","EXZDES","EXZISV","EXZNAL","EXZPCB","EXMODL","EXDCPO","EXVENR","EXZSEA","EXITNO","EXZCND","EXNEWE","EXGRWE","EXCSNO","EXSUN1","EXZADR","EXORCO","EXSPE1","EXZBAN","EXZTEM","EXZCEM","EXZDFB","EXEXPI","EXITTY","EXRGDT","EXRGTM","EXLMDT","EXCHNO","EXCHID","EXZDSD","EXZAET","EXZNAF","EXZCTY","EXDIVI","EXSTAT").build()
    DBContainer EXT130 = query.getContainer()
    EXT130.set("EXCONO", currentCompany)
    EXT130.set("EXORNO",  mi.in.get("ORNO"))
    EXT130.set("EXPONR",  mi.in.get("PONR"))
    EXT130.set("EXPOSX",  mi.in.get("POSX"))
    EXT130.set("EXDLIX",  mi.in.get("DLIX"))
    EXT130.set("EXZCID",  mi.in.get("ZCID"))
    EXT130.set("EXDOID",  mi.in.get("DOID"))
    if(query.read(EXT130)){
      cuno = EXT130.get("EXCUNO")
      zcua = EXT130.get("EXZCUA")
      town = EXT130.get("EXTOWN")
      cscd = EXT130.get("EXCSCD")
      znco = EXT130.get("EXZNCO")
      csc2 = EXT130.get("EXCSC2")
      conn = EXT130.get("EXCONN")
      dsd2 = EXT130.get("EXDSD2")
      zdes = EXT130.get("EXZDES")
      zisv = EXT130.get("EXZISV")
      znal = EXT130.get("EXZNAL")
      zpcb = EXT130.get("EXZPCB")
      modl = EXT130.get("EXMODL")
      dcpo = EXT130.get("EXDCPO")
      venr = EXT130.get("EXVENR")
      zsea = EXT130.get("EXZSEA")
      itno = EXT130.get("EXITNO")
      zcnd = EXT130.get("EXZCND")
      newe = EXT130.get("EXNEWE")
      grwe = EXT130.get("EXGRWE")
      csno = EXT130.get("EXCSNO")
      sun1 = EXT130.get("EXSUN1")
      zadr = EXT130.get("EXZADR")
      orco = EXT130.get("EXORCO")
      spe1 = EXT130.get("EXSPE1")
      zban = EXT130.get("EXZBAN")
      ztem = EXT130.get("EXZTEM")
      zcem = EXT130.get("EXZCEM")
      zdfb = EXT130.get("EXZDFB")
      expi = EXT130.get("EXEXPI")
      itty = EXT130.get("EXITTY")
      zdsd = EXT130.get("EXZDSD")
      zaet = EXT130.get("EXZAET")
      znaf = EXT130.get("EXZNAF")
      zcty = EXT130.get("EXZCTY")
      divi = EXT130.get("EXDIVI")
      stat = EXT130.get("EXSTAT")
      String entryDate = EXT130.get("EXRGDT")
      String entryTime = EXT130.get("EXRGTM")
      String changeDate = EXT130.get("EXLMDT")
      String changeNumber = EXT130.get("EXCHNO")
      String changedBy = EXT130.get("EXCHID")
      mi.outData.put("CUNO", cuno)
      mi.outData.put("ZCUA", zcua)
      mi.outData.put("TOWN", town)
      mi.outData.put("CSCD", cscd)
      mi.outData.put("ZNCO", znco as String)
      mi.outData.put("CSC2", csc2)
      mi.outData.put("CONN", conn as String)
      mi.outData.put("DSD2", dsd2  as String)
      mi.outData.put("ZDES", zdes)
      mi.outData.put("ZISV", zisv)
      mi.outData.put("ZNAL", znal)
      mi.outData.put("ZPCB", zpcb)
      mi.outData.put("MODL", modl)
      mi.outData.put("DCPO", dcpo)
      mi.outData.put("VENR", venr)
      mi.outData.put("ZSEA", zsea)
      mi.outData.put("ITNO", itno)
      mi.outData.put("ZCND", zcnd as String)
      mi.outData.put("NEWE", newe as String)
      mi.outData.put("GRWE", grwe as String)
      mi.outData.put("CSNO", csno)
      mi.outData.put("SUN1", sun1)
      mi.outData.put("ZADR", zadr)
      mi.outData.put("ORCO", orco)
      mi.outData.put("SPE1", spe1)
      mi.outData.put("ZBAN", zban)
      mi.outData.put("ZTEM", ztem)
      mi.outData.put("ZCEM", zcem)
      mi.outData.put("ZDFB", zdfb as String)
      mi.outData.put("EXPI", expi as String)
      mi.outData.put("ITTY", itty)
      mi.outData.put("ZDSD", zdsd)
      mi.outData.put("ZAET", zaet)
      mi.outData.put("ZNAF", znaf)
      mi.outData.put("ZCTY", zcty)
      mi.outData.put("DIVI", divi)
      mi.outData.put("STAT", stat)
      mi.outData.put("RGDT", entryDate)
      mi.outData.put("RGTM", entryTime)
      mi.outData.put("LMDT", changeDate)
      mi.outData.put("CHNO", changeNumber)
      mi.outData.put("CHID", changedBy)
      mi.write()
    }
  }
}
