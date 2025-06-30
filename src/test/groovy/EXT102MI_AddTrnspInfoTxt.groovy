/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT102MI.AddTrnspInfoTxt
 * Description : Add transportation infos.
 * Date         Changed By   Description
 * 20230522     RENARN       CMDX28 - DAE number management
 * 20230525     RENARN       Comments, returns and checks have been added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddTrnspInfoTxt extends ExtendM3Transaction {
  private final MIAPI mi
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private Integer tlvl
  private Integer conn
  private Long dlix
  private String tx60
  private Long txid
  private String TXID

  public AddTrnspInfoTxt(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.program = program
    this.logger = logger
    this.miCaller = miCaller
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Check transportation info level
    tlvl = 0
    if (mi.in.get("TLVL") != null && mi.in.get("TLVL") != "") {
      tlvl = mi.in.get("TLVL")
      if(tlvl != 1 && tlvl != 2) {
        mi.error("Niveau info transport est invalide")
        return
      }
    } else {
      mi.error("Niveau info transport est obligatoire")
      return
    }
    if(tlvl==1){
      // Check shipment
      conn = 0
      if (mi.in.get("CONN") != null && mi.in.get("CONN") != "") {
        conn = mi.in.get("CONN")
        DBAction query_DCONSI = database.table("DCONSI").index("00").build()
        DBContainer DCONSI = query_DCONSI.getContainer()
        DCONSI.set("DACONO", currentCompany)
        DCONSI.set("DACONN", conn)
        if(!query_DCONSI.read(DCONSI)) {
          mi.error("Expédition " + (conn as String) + " n'existe pas")
          return
        }
      } else {
        mi.error("Expédition est obligatoire")
        return
      }
    } else {
      // Check delivery
      dlix = 0
      if (mi.in.get("DLIX") != null && mi.in.get("DLIX") != "") {
        dlix = mi.in.get("DLIX")
        DBAction query_MHDISH = database.table("MHDISH").index("00").build()
        DBContainer MHDISH = query_MHDISH.getContainer()
        MHDISH.set("OQCONO", currentCompany)
        MHDISH.set("OQINOU",  1)
        MHDISH.set("OQDLIX",  dlix)
        if(!query_MHDISH.read(MHDISH)) {
          mi.error("Index de livraison " + (dlix as String) + " n'existe pas")
          return
        }
      } else {
        mi.error("Index de livraison est obligatoire")
        return
      }
    }
    // Check text
    tx60 = ""
    if (mi.in.get("TX60") != null && mi.in.get("TX60") != "") {
      tx60 = mi.in.get("TX60")
    } else {
      mi.error("Texte ligne est obligatoire")
      return
    }
    // Check transp infos
    txid = 0
    DBAction query_DRADTR = database.table("DRADTR").index("00").selection("DRTXID").build()
    DBContainer DRADTR = query_DRADTR.getContainer()
    DRADTR.set("DRCONO", currentCompany)
    DRADTR.set("DRTLVL",  tlvl)
    DRADTR.set("DRCONN",  conn)
    DRADTR.set("DRDLIX",  dlix)
    if(query_DRADTR.read(DRADTR)) {
      txid = DRADTR.get("DRTXID")
      if(txid != 0) {
        executeCRS980MIAddTxtBlockLine(txid as String, tx60, "MSYTXH", "DRADTR")
      } else {
        executeCRS980MIRtvNewTextID("MSYTXH", "DRADTR")

        if(tlvl == 1)
          executeCRS980MIAddTxtBlockHead(TXID, "DRADTR00", conn as String, program.getUser(), "MSYTXH")
        if(tlvl == 2)
          executeCRS980MIAddTxtBlockHead(TXID, "DRADTR00", dlix as String, program.getUser(), "MSYTXH")

        executeCRS980MIAddTxtBlockLine(TXID, tx60, "MSYTXH", "DRADTR")

        // Update DRADTR with TXID
        DRADTR.set("DRCONO", currentCompany)
        DRADTR.set("DRTLVL",  tlvl)
        DRADTR.set("DRCONN",  conn)
        DRADTR.set("DRDLIX",  dlix)
        if(!query_DRADTR.readLock(DRADTR, updateCallBack)){}
      }
    } else {
      mi.error("Enregistrement n'existe pas")
      return
    }
  }
  // Update DRADTR
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("DRCHNO")
    lockedResult.set("DRTXID", TXID as Long)
    lockedResult.setInt("DRLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("DRCHNO", changeNumber + 1)
    lockedResult.set("DRCHID", program.getUser())
    lockedResult.update()
  }
  // Execute CRS980MI.RtvNewTextID
  private executeCRS980MIRtvNewTextID(String FILE, String TABL){
    def parameters = ["FILE": FILE, "TABL": TABL]
    Closure<?> handler = { Map<String, String> response ->
      TXID = response.TXID.trim()

      if (response.error != null) {
        return mi.error("Failed CRS980MI.RtvNewTextID: "+ response.errorMessage)
      }
    }
    miCaller.call("CRS980MI", "RtvNewTextID", parameters, handler)
  }
  // Execute CRS980MI.AddTxtBlockHead
  private executeCRS980MIAddTxtBlockHead(String TXID, String FILE, String KFLD, String USID, String TFIL){
    def parameters = ["TXID": TXID, "FILE": FILE, "KFLD": KFLD, "USID": USID, "TFIL": TFIL]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed CRS980MI.AddTxtBlockHead: "+ response.errorMessage)
      }
    }
    miCaller.call("CRS980MI", "AddTxtBlockHead", parameters, handler)
  }
  // Execute CRS980MI.AddTxtBlockLine
  private executeCRS980MIAddTxtBlockLine(String TXID, String TX60, String TFIL, String FILE){
    def parameters = ["TXID": TXID, "TX60": TX60, "TFIL": TFIL, "FILE": FILE]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed CRS980MI.AddTxtBlockLine: "+ response.errorMessage)
      }
    }
    miCaller.call("CRS980MI", "AddTxtBlockLine", parameters, handler)
  }
}
