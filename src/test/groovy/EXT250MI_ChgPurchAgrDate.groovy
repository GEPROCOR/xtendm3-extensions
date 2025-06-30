/**
 * README
 * This extension is used by extensions
 *
 * Name : EXT250MI.ChgPurchAgrDate
 * Description : Get general settings by log
 * Date         Changed By   Description
 * 20220125     YOUYVO       REAX27-X Change Purchase Agreement Date
 * 20220131     YOUYVO       Check of input dates has been added
 * 20240605     RENARN       nvdt handling modified/AISRGR handling added
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.format.DateTimeParseException

public class ChgPurchAgrDate extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private String nvdt = ""
  private String fvdt = ""
  private Integer iNVDT_Ancien = 0
  private String aNVDT_Ancien

  public ChgPurchAgrDate(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.miCaller = miCaller
    this.utility = utility
  }

  public void main() {
    Integer currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    if (mi.in.get("SUNO") == null) {
      mi.error("Code fournisseur est obligatoire")
      return
    }
    if (mi.in.get("AGNB") == null) {
      mi.error("Contrat fournisseur est obligatoire")
      return
    }
    if (mi.in.get("FVDT") == null || mi.in.get("FVDT") == "") {
      mi.error("Date debut validite est obligatoire")
      return
    }else{
      fvdt = mi.in.get("FVDT")
      if (!utility.call("DateUtil", "isDateValid", fvdt, "yyyyMMdd")) {
        mi.error("Format date debut validite est incorrect")
        return
      }
    }
    if(mi.in.get("NVDT") == null || mi.in.get("NVDT") == ""){
      mi.error("Nouvelle date debut validite est obligatoire")
      return
    }else {
      nvdt = mi.in.get("NVDT")
      if (!utility.call("DateUtil", "isDateValid", nvdt, "yyyyMMdd")) {
        mi.error("Format nouvelle date de Validité incorrect")
        return
      }
    }
    DBAction queryMPAGRH = database.table("MPAGRH").index("00").selection("AHCONO", "AHSUNO", "AHAGNB", "AHFVDT").build()
    DBContainer MPAGRH = queryMPAGRH.getContainer()
    MPAGRH.set("AHCONO", currentCompany)
    MPAGRH.set("AHSUNO",  mi.in.get("SUNO"))
    MPAGRH.set("AHAGNB",  mi.in.get("AGNB"))
    if(queryMPAGRH.read(MPAGRH)){
      // Update agreement supplier
      executePPS100MIUpdAgrHead(currentCompany as String, mi.in.get("SUNO") as String, mi.in.get("AGNB") as String, nvdt)
      //Lecture des lignes pour changement de date
      DBAction queryMPAGRL = database.table("MPAGRL").index("00").selection("AICONO",	"AISUNO",	"AIAGNB",	"AIFVDT",	"AIUVDT",	"AIAGPT",	"AIDLSH",	"AIAGQT",	"AIPUQT",	"AIPAAM",	"AISUPR",	"AILEA1",	"AIPUCD",	"AIVOLI",	"AIPODI",	"AIUASU",	"AIBOFO",	"AIBOGE",	"AIREQU",	"AIRENA",	"AIAGFF",	"AISEQN",	"AIPTCD",	"AIWAL1",	"AIWAL2",	"AIWAL3",	"AIWAL4",	"AIGRPI",	"AIOBV1",	"AIOBV2",	"AIOBV3",	"AIOBV4",	"AISAGL",	"AITXID",	"AIDTID",	"AIUNIT",	"AIORTY",	"AIGRMT",	"AIITNO",	"AIRGDT",	"AILMDT",	"AICHID",	"AIRGTM",	"AICHNO",	"AIORCO",	"AILMTS",	"AIXHUN",	"AIDEFD",	"AIDEFH",	"AIDEFM",	"AIWSCA",	"AIRVQT",	"AIPUUN",	"AIPPUN",	"AIPORG",	"AIRDUR").build()
      DBContainer MPAGRL_0 = queryMPAGRL.getContainer()
      MPAGRL_0.set("AICONO", currentCompany)
      MPAGRL_0.set("AISUNO",  mi.in.get("SUNO"))
      MPAGRL_0.set("AIAGNB",  mi.in.get("AGNB"))
      if(!queryMPAGRL.readAll(MPAGRL_0, 3, updateCallBackMPAGRL)){
      }
      //Lecture des lignes prix pour changement de date
      DBAction queryMPAGRP = database.table("MPAGRP").index("00").selection("AJCONO",	"AJFACI",	"AJSUNO",	"AJAGNB",	"AJSEQN",	"AJFRQT",	"AJPUPR",	"AJDIP3",	"AJMAPR",	"AJGRPI",	"AJOBV1",	"AJOBV2",	"AJOBV3",	"AJOBV4",	"AJFVDT",	"AJTXID",	"AJRGDT",	"AJLMDT",	"AJCHID",	"AJRGTM",	"AJCHNO").build()
      DBContainer MPAGRP_0 = queryMPAGRP.getContainer()
      MPAGRP_0.set("AJCONO", currentCompany)
      MPAGRP_0.set("AJSUNO",  mi.in.get("SUNO"))
      MPAGRP_0.set("AJAGNB",  mi.in.get("AGNB"))
      if(!queryMPAGRP.readAll(MPAGRP_0, 3, updateCallBackMPAGRP)){
      }
      //Lecture des lignes cugex1 changement de date
      DBAction queryCUGEX1 = database.table("CUGEX1").index("00").selection("F1CONO",	"F1FILE",	"F1PK01",	"F1PK02",	"F1PK03",	"F1PK04",	"F1PK05",	"F1PK06",	"F1PK07",	"F1PK08",	"F1A030",	"F1A130",	"F1A230",	"F1A330",	"F1A430",	"F1A530",	"F1A630",	"F1A730",	"F1A830",	"F1A930",	"F1N096",	"F1N196",	"F1N296",	"F1N396",	"F1N496",	"F1N596",	"F1N696",	"F1N796",	"F1N896",	"F1N996",	"F1MIGR",	"F1A256",	"F1A121",	"F1A122",	"F1DTID",	"F1LMTS",	"F1TXID",	"F1RGDT",	"F1RGTM",	"F1LMDT",	"F1CHNO",	"F1CHID",	"F1CHB1",	"F1CHB2",	"F1CHB3",	"F1CHB4",	"F1CHB5",	"F1CHB6",	"F1CHB7",	"F1CHB8",	"F1CHB9",	"F1DAT8",	"F1DAT1",	"F1DAT2",	"F1DAT3",	"F1DAT4",	"F1DAT5",	"F1DAT6",	"F1DAT7",	"F1DAT9").build()
      DBContainer CUGEX1_0 = queryCUGEX1.getContainer()
      CUGEX1_0.set("F1CONO", currentCompany)
      CUGEX1_0.set("F1FILE",  "MPAGRL")
      CUGEX1_0.set("F1PK01",  mi.in.get("SUNO"))
      CUGEX1_0.set("F1PK02",  mi.in.get("AGNB"))
      if(!queryCUGEX1.readAll(CUGEX1_0, 4, updateCallBackCUGEX1)){
      }
    }
  }
  // Execute PPS100MI.UpdAgrHead
  private executePPS100MIUpdAgrHead(String CONO, String SUNO, String AGNB, String NVDT){
    def parameters = ["CONO": CONO, "SUNO": SUNO, "AGNB": AGNB, "FVDT": NVDT]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed PPS100MI.UpdAgrHead: "+ response.errorMessage)
      }else{
      }
    }
    miCaller.call("PPS100MI", "UpdAgrHead", parameters, handler)
  }
  // Insert/Delete MPAGRL
  Closure<?> updateCallBackMPAGRL = { DBContainer MPAGRL_1 ->
    DBAction queryNewMPAGRL = database.table("MPAGRL").index("00").selection("AICONO",	"AISUNO",	"AIAGNB",	"AIFVDT",	"AIUVDT",	"AIAGPT",	"AIDLSH",	"AIAGQT",	"AIPUQT",	"AIPAAM",	"AISUPR",	"AILEA1",	"AIPUCD",	"AIVOLI",	"AIPODI",	"AIUASU",	"AIBOFO",	"AIBOGE",	"AIREQU",	"AIRENA",	"AIAGFF",	"AISEQN",	"AIPTCD",	"AIWAL1",	"AIWAL2",	"AIWAL3",	"AIWAL4",	"AIGRPI",	"AIOBV1",	"AIOBV2",	"AIOBV3",	"AIOBV4",	"AISAGL",	"AITXID",	"AIDTID",	"AIUNIT",	"AIORTY",	"AIGRMT",	"AIITNO",	"AIRGDT",	"AILMDT",	"AICHID",	"AIRGTM",	"AICHNO",	"AIORCO",	"AILMTS",	"AIXHUN",	"AIDEFD",	"AIDEFH",	"AIDEFM",	"AIWSCA",	"AIRVQT",	"AIPUUN",	"AIPPUN",	"AIPORG",	"AIRDUR","AISRGR").build()
    DBContainer MPAGRL_New = queryNewMPAGRL.getContainer()
    MPAGRL_New.set("AICONO", MPAGRL_1.get("AICONO"))
    MPAGRL_New.set("AISUNO", MPAGRL_1.get("AISUNO"))
    MPAGRL_New.set("AIAGNB", MPAGRL_1.get("AIAGNB"))
    MPAGRL_New.set("AIGRPI", MPAGRL_1.get("AIGRPI"))
    MPAGRL_New.set("AIOBV1", MPAGRL_1.get("AIOBV1"))
    MPAGRL_New.set("AIOBV2", MPAGRL_1.get("AIOBV2"))
    MPAGRL_New.set("AIOBV3", MPAGRL_1.get("AIOBV3"))
    MPAGRL_New.set("AIOBV4", MPAGRL_1.get("AIOBV4"))
    MPAGRL_New.set("AIFVDT", MPAGRL_1.get("AIFVDT"))
    if (queryNewMPAGRL.read(MPAGRL_New)) {
      MPAGRL_New.set("AIFVDT", nvdt as Integer)
      if (!queryNewMPAGRL.read(MPAGRL_New)) {
        MPAGRL_New.set("AICONO",  MPAGRL_1.get("AICONO"))
        MPAGRL_New.set("AIFACI",  MPAGRL_1.get("AIFACI"))
        MPAGRL_New.set("AISUNO",  MPAGRL_1.get("AISUNO"))
        MPAGRL_New.set("AIAGNB",  MPAGRL_1.get("AIAGNB"))
        MPAGRL_New.set("AIFVDT", nvdt as Integer)
        MPAGRL_New.set("AIUVDT",  MPAGRL_1.get("AIUVDT"))
        MPAGRL_New.set("AIAGPT",  MPAGRL_1.get("AIAGPT"))
        MPAGRL_New.set("AIDLSH",  MPAGRL_1.get("AIDLSH"))
        MPAGRL_New.set("AIAGQT",  MPAGRL_1.get("AIAGQT"))
        MPAGRL_New.set("AIPUQT",  MPAGRL_1.get("AIPUQT"))
        MPAGRL_New.set("AIPAAM",  MPAGRL_1.get("AIPAAM"))
        MPAGRL_New.set("AISUPR",  MPAGRL_1.get("AISUPR"))
        MPAGRL_New.set("AILEA1",  MPAGRL_1.get("AILEA1"))
        MPAGRL_New.set("AIPUCD",  MPAGRL_1.get("AIPUCD"))
        MPAGRL_New.set("AIVOLI",  MPAGRL_1.get("AIVOLI"))
        MPAGRL_New.set("AIPODI",  MPAGRL_1.get("AIPODI"))
        MPAGRL_New.set("AIUASU",  MPAGRL_1.get("AIUASU"))
        MPAGRL_New.set("AIBOFO",  MPAGRL_1.get("AIBOFO"))
        MPAGRL_New.set("AIBOGE",  MPAGRL_1.get("AIBOGE"))
        MPAGRL_New.set("AIREQU",  MPAGRL_1.get("AIREQU"))
        MPAGRL_New.set("AIRENA",  MPAGRL_1.get("AIRENA"))
        MPAGRL_New.set("AIAGFF",  MPAGRL_1.get("AIAGFF"))
        MPAGRL_New.set("AISEQN",  MPAGRL_1.get("AISEQN"))
        MPAGRL_New.set("AIPTCD",  MPAGRL_1.get("AIPTCD"))
        MPAGRL_New.set("AIWAL1",  MPAGRL_1.get("AIWAL1"))
        MPAGRL_New.set("AIWAL2",  MPAGRL_1.get("AIWAL2"))
        MPAGRL_New.set("AIWAL3",  MPAGRL_1.get("AIWAL3"))
        MPAGRL_New.set("AIWAL4",  MPAGRL_1.get("AIWAL4"))
        MPAGRL_New.set("AIGRPI",  MPAGRL_1.get("AIGRPI"))
        MPAGRL_New.set("AIOBV1",  MPAGRL_1.get("AIOBV1"))
        MPAGRL_New.set("AIOBV2",  MPAGRL_1.get("AIOBV2"))
        MPAGRL_New.set("AIOBV3",  MPAGRL_1.get("AIOBV3"))
        MPAGRL_New.set("AIOBV4",  MPAGRL_1.get("AIOBV4"))
        MPAGRL_New.set("AISAGL",  MPAGRL_1.get("AISAGL"))
        MPAGRL_New.set("AITXID",  MPAGRL_1.get("AITXID"))
        MPAGRL_New.set("AIDTID",  MPAGRL_1.get("AIDTID"))
        MPAGRL_New.set("AIUNIT",  MPAGRL_1.get("AIUNIT"))
        MPAGRL_New.set("AIORTY",  MPAGRL_1.get("AIORTY"))
        MPAGRL_New.set("AIGRMT",  MPAGRL_1.get("AIGRMT"))
        MPAGRL_New.set("AIITNO",  MPAGRL_1.get("AIITNO"))
        MPAGRL_New.set("AIORCO",  MPAGRL_1.get("AIORCO"))
        MPAGRL_New.set("AILMTS",  MPAGRL_1.get("AILMTS"))
        MPAGRL_New.set("AIXHUN",  MPAGRL_1.get("AIXHUN"))
        MPAGRL_New.set("AIDEFD",  MPAGRL_1.get("AIDEFD"))
        MPAGRL_New.set("AIDEFH",  MPAGRL_1.get("AIDEFH"))
        MPAGRL_New.set("AIDEFM",  MPAGRL_1.get("AIDEFM"))
        MPAGRL_New.set("AIWSCA",  MPAGRL_1.get("AIWSCA"))
        MPAGRL_New.set("AIRVQT",  MPAGRL_1.get("AIRVQT"))
        MPAGRL_New.set("AIPUUN",  MPAGRL_1.get("AIPUUN"))
        MPAGRL_New.set("AIPPUN",  MPAGRL_1.get("AIPPUN"))
        MPAGRL_New.set("AIPORG",  MPAGRL_1.get("AIPORG"))
        MPAGRL_New.set("AIRDUR",  MPAGRL_1.get("AIRDUR"))
        MPAGRL_New.set("AISRGR",  MPAGRL_1.get("AISRGR"))
        LocalDateTime timeOfCreation = LocalDateTime.now()
        MPAGRL_New.setInt("AIRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        MPAGRL_New.setInt("AILMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        MPAGRL_New.setInt("AIRGTM",  timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        MPAGRL_New.setInt("AICHNO", 1)
        MPAGRL_New.set("AICHID", program.getUser())
        queryNewMPAGRL.insert(MPAGRL_New)
      }
    }
    MPAGRL_New.set("AICONO", MPAGRL_1.get("AICONO"))
    MPAGRL_New.set("AISUNO", MPAGRL_1.get("AISUNO"))
    MPAGRL_New.set("AIAGNB", MPAGRL_1.get("AIAGNB"))
    MPAGRL_New.set("AIGRPI", MPAGRL_1.get("AIGRPI"))
    MPAGRL_New.set("AIOBV1", MPAGRL_1.get("AIOBV1"))
    MPAGRL_New.set("AIOBV2", MPAGRL_1.get("AIOBV2"))
    MPAGRL_New.set("AIOBV3", MPAGRL_1.get("AIOBV3"))
    MPAGRL_New.set("AIOBV4", MPAGRL_1.get("AIOBV4"))
    MPAGRL_New.set("AIFVDT", MPAGRL_1.get("AIFVDT"))
    if (!queryNewMPAGRL.readLock(MPAGRL_New, updateCallBackMPAGRL_Sup)) {
    }
  }
  // Delete MPAGRL
  Closure<?> updateCallBackMPAGRL_Sup = { LockedResult lockedResult1 ->
    if ((nvdt as Integer) != lockedResult1.get("AIFVDT")) {
      lockedResult1.delete()
    }
  }
  // Insert/Delete MPAGRP
  Closure<?> updateCallBackMPAGRP = { DBContainer MPAGRP_1 ->
    DBAction queryNewMPAGRP = database.table("MPAGRP").index("00").selection("AJCONO",	"AJFACI",	"AJSUNO",	"AJAGNB",	"AJSEQN",	"AJFRQT",	"AJPUPR",	"AJDIP3",	"AJMAPR",	"AJGRPI",	"AJOBV1",	"AJOBV2",	"AJOBV3",	"AJOBV4",	"AJFVDT",	"AJTXID",	"AJRGDT",	"AJLMDT",	"AJCHID",	"AJRGTM",	"AJCHNO").build()
    DBContainer MPAGRP_New = queryNewMPAGRP.getContainer()
    MPAGRP_New.set("AJCONO", MPAGRP_1.get("AJCONO"))
    MPAGRP_New.set("AJSUNO", MPAGRP_1.get("AJSUNO"))
    MPAGRP_New.set("AJAGNB", MPAGRP_1.get("AJAGNB"))
    MPAGRP_New.set("AJGRPI", MPAGRP_1.get("AJGRPI"))
    MPAGRP_New.set("AJOBV1", MPAGRP_1.get("AJOBV1"))
    MPAGRP_New.set("AJOBV2", MPAGRP_1.get("AJOBV2"))
    MPAGRP_New.set("AJOBV3", MPAGRP_1.get("AJOBV3"))
    MPAGRP_New.set("AJOBV4", MPAGRP_1.get("AJOBV4"))
    MPAGRP_New.set("AJFVDT", MPAGRP_1.get("AJFVDT"))
    MPAGRP_New.set("AJFRQT", 0)
    if (queryNewMPAGRP.read(MPAGRP_New)) {
      MPAGRP_New.set("AJFVDT", nvdt as Integer)
      if (!queryNewMPAGRP.read(MPAGRP_New)) {
        MPAGRP_New.set("AJCONO",  MPAGRP_1.get("AJCONO"))
        MPAGRP_New.set("AJFACI",  MPAGRP_1.get("AJFACI"))
        MPAGRP_New.set("AJSUNO",  MPAGRP_1.get("AJSUNO"))
        MPAGRP_New.set("AJAGNB",  MPAGRP_1.get("AJAGNB"))
        MPAGRP_New.set("AJSEQN",  MPAGRP_1.get("AJSEQN"))
        MPAGRP_New.set("AJFRQT",  MPAGRP_1.get("AJFRQT"))
        MPAGRP_New.set("AJPUPR",  MPAGRP_1.get("AJPUPR"))
        MPAGRP_New.set("AJDIP3",  MPAGRP_1.get("AJDIP3"))
        MPAGRP_New.set("AJMAPR",  MPAGRP_1.get("AJMAPR"))
        MPAGRP_New.set("AJGRPI",  MPAGRP_1.get("AJGRPI"))
        MPAGRP_New.set("AJOBV1",  MPAGRP_1.get("AJOBV1"))
        MPAGRP_New.set("AJOBV2",  MPAGRP_1.get("AJOBV2"))
        MPAGRP_New.set("AJOBV3",  MPAGRP_1.get("AJOBV3"))
        MPAGRP_New.set("AJOBV4",  MPAGRP_1.get("AJOBV4"))
        MPAGRP_New.set("AJFVDT",  nvdt as Integer)
        MPAGRP_New.set("AJTXID",  MPAGRP_1.get("AJTXID"))
        LocalDateTime timeOfCreation = LocalDateTime.now()
        MPAGRP_New.setInt("AJRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        MPAGRP_New.setInt("AJLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        MPAGRP_New.setInt("AJRGTM",  timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        MPAGRP_New.setInt("AJCHNO", 1)
        MPAGRP_New.set("AJCHID", program.getUser())
        queryNewMPAGRP.insert(MPAGRP_New)
      }
    }
    MPAGRP_New.set("AJCONO", MPAGRP_1.get("AJCONO"))
    MPAGRP_New.set("AJSUNO", MPAGRP_1.get("AJSUNO"))
    MPAGRP_New.set("AJAGNB", MPAGRP_1.get("AJAGNB"))
    MPAGRP_New.set("AJGRPI", MPAGRP_1.get("AJGRPI"))
    MPAGRP_New.set("AJOBV1", MPAGRP_1.get("AJOBV1"))
    MPAGRP_New.set("AJOBV2", MPAGRP_1.get("AJOBV2"))
    MPAGRP_New.set("AJOBV3", MPAGRP_1.get("AJOBV3"))
    MPAGRP_New.set("AJOBV4", MPAGRP_1.get("AJOBV4"))
    MPAGRP_New.set("AJFVDT", MPAGRP_1.get("AJFVDT"))
    MPAGRP_New.set("AJFRQT", 0)
    if (!queryNewMPAGRP.readLock(MPAGRP_New, updateCallBackMPAGRP_Sup)) {
    }
  }
  // Delete MPAGRP
  Closure<?> updateCallBackMPAGRP_Sup = { LockedResult lockedResult2 ->
    if ((nvdt as Integer) != lockedResult2.get("AJFVDT")) {
      lockedResult2.delete()
    }
  }
  // Insert/Delete CUGEX1
  Closure<?> updateCallBackCUGEX1 = { DBContainer CUGEX1_1 ->
    DBAction queryNewCUGEX1 = database.table("CUGEX1").index("00").selection("F1CONO",	"F1FILE",	"F1PK01",	"F1PK02",	"F1PK03",	"F1PK04",	"F1PK05",	"F1PK06",	"F1PK07",	"F1PK08",	"F1A030",	"F1A130",	"F1A230",	"F1A330",	"F1A430",	"F1A530",	"F1A630",	"F1A730",	"F1A830",	"F1A930",	"F1N096",	"F1N196",	"F1N296",	"F1N396",	"F1N496",	"F1N596",	"F1N696",	"F1N796",	"F1N896",	"F1N996",	"F1MIGR",	"F1A256",	"F1A121",	"F1A122",	"F1DTID",	"F1LMTS",	"F1TXID",	"F1RGDT",	"F1RGTM",	"F1LMDT",	"F1CHNO",	"F1CHID",	"F1CHB1",	"F1CHB2",	"F1CHB3",	"F1CHB4",	"F1CHB5",	"F1CHB6",	"F1CHB7",	"F1CHB8",	"F1CHB9",	"F1DAT8",	"F1DAT1",	"F1DAT2",	"F1DAT3",	"F1DAT4",	"F1DAT5",	"F1DAT6",	"F1DAT7",	"F1DAT9").build()
    DBContainer CUGEX1_New = queryNewCUGEX1.getContainer()
    CUGEX1_New.set("F1CONO", CUGEX1_1.get("F1CONO"))
    CUGEX1_New.set("F1FILE", "MPAGRL")
    CUGEX1_New.set("F1PK01", CUGEX1_1.get("F1PK01"))
    CUGEX1_New.set("F1PK02", CUGEX1_1.get("F1PK02"))
    CUGEX1_New.set("F1PK03", CUGEX1_1.get("F1PK03"))
    CUGEX1_New.set("F1PK04", CUGEX1_1.get("F1PK04"))
    CUGEX1_New.set("F1PK05", CUGEX1_1.get("F1PK05"))
    CUGEX1_New.set("F1PK06", CUGEX1_1.get("F1PK06"))
    CUGEX1_New.set("F1PK07", CUGEX1_1.get("F1PK07"))
    CUGEX1_New.set("F1PK08", CUGEX1_1.get("F1PK08"))
    if (queryNewCUGEX1.read(CUGEX1_New)) {
      CUGEX1_New.set("F1PK08", nvdt)
      if (!queryNewCUGEX1.read(CUGEX1_New)) {
        CUGEX1_New.set("F1CONO",  CUGEX1_1.get("F1CONO"))
        CUGEX1_New.set("F1FILE",  CUGEX1_1.get("F1FILE"))
        CUGEX1_New.set("F1PK01",  CUGEX1_1.get("F1PK01"))
        CUGEX1_New.set("F1PK02",  CUGEX1_1.get("F1PK02"))
        CUGEX1_New.set("F1PK03",  CUGEX1_1.get("F1PK03"))
        CUGEX1_New.set("F1PK04",  CUGEX1_1.get("F1PK04"))
        CUGEX1_New.set("F1PK05",  CUGEX1_1.get("F1PK05"))
        CUGEX1_New.set("F1PK06",  CUGEX1_1.get("F1PK06"))
        CUGEX1_New.set("F1PK07",  CUGEX1_1.get("F1PK07"))
        CUGEX1_New.set("F1PK08",  nvdt)
        CUGEX1_New.set("F1A030",  CUGEX1_1.get("F1A030"))
        CUGEX1_New.set("F1A130",  CUGEX1_1.get("F1A130"))
        CUGEX1_New.set("F1A230",  CUGEX1_1.get("F1A230"))
        CUGEX1_New.set("F1A330",  CUGEX1_1.get("F1A330"))
        CUGEX1_New.set("F1A430",  CUGEX1_1.get("F1A430"))
        CUGEX1_New.set("F1A530",  CUGEX1_1.get("F1A530"))
        CUGEX1_New.set("F1A630",  CUGEX1_1.get("F1A630"))
        CUGEX1_New.set("F1A730",  CUGEX1_1.get("F1A730"))
        CUGEX1_New.set("F1A830",  CUGEX1_1.get("F1A830"))
        CUGEX1_New.set("F1A930",  CUGEX1_1.get("F1A930"))
        CUGEX1_New.set("F1N096",  CUGEX1_1.get("F1N096"))
        CUGEX1_New.set("F1N196",  CUGEX1_1.get("F1N196"))
        CUGEX1_New.set("F1N296",  CUGEX1_1.get("F1N296"))
        CUGEX1_New.set("F1N396",  CUGEX1_1.get("F1N396"))
        CUGEX1_New.set("F1N496",  CUGEX1_1.get("F1N496"))
        CUGEX1_New.set("F1N596",  CUGEX1_1.get("F1N596"))
        CUGEX1_New.set("F1N696",  CUGEX1_1.get("F1N696"))
        CUGEX1_New.set("F1N796",  CUGEX1_1.get("F1N796"))
        CUGEX1_New.set("F1N896",  CUGEX1_1.get("F1N896"))
        CUGEX1_New.set("F1N996",  CUGEX1_1.get("F1N996"))
        CUGEX1_New.set("F1MIGR",  CUGEX1_1.get("F1MIGR"))
        CUGEX1_New.set("F1A256",  CUGEX1_1.get("F1A256"))
        CUGEX1_New.set("F1A121",  CUGEX1_1.get("F1A121"))
        CUGEX1_New.set("F1A122",  CUGEX1_1.get("F1A122"))
        CUGEX1_New.set("F1DTID",  CUGEX1_1.get("F1DTID"))
        CUGEX1_New.set("F1LMTS",  CUGEX1_1.get("F1LMTS"))
        CUGEX1_New.set("F1TXID",  CUGEX1_1.get("F1TXID"))
        CUGEX1_New.set("F1CHB1",  CUGEX1_1.get("F1CHB1"))
        CUGEX1_New.set("F1CHB2",  CUGEX1_1.get("F1CHB2"))
        CUGEX1_New.set("F1CHB3",  CUGEX1_1.get("F1CHB3"))
        CUGEX1_New.set("F1CHB4",  CUGEX1_1.get("F1CHB4"))
        CUGEX1_New.set("F1CHB5",  CUGEX1_1.get("F1CHB5"))
        CUGEX1_New.set("F1CHB6",  CUGEX1_1.get("F1CHB6"))
        CUGEX1_New.set("F1CHB7",  CUGEX1_1.get("F1CHB7"))
        CUGEX1_New.set("F1CHB8",  CUGEX1_1.get("F1CHB8"))
        CUGEX1_New.set("F1CHB9",  CUGEX1_1.get("F1CHB9"))
        CUGEX1_New.set("F1DAT8",  CUGEX1_1.get("F1DAT8"))
        CUGEX1_New.set("F1DAT1",  CUGEX1_1.get("F1DAT1"))
        CUGEX1_New.set("F1DAT2",  CUGEX1_1.get("F1DAT2"))
        CUGEX1_New.set("F1DAT3",  CUGEX1_1.get("F1DAT3"))
        CUGEX1_New.set("F1DAT4",  CUGEX1_1.get("F1DAT4"))
        CUGEX1_New.set("F1DAT5",  CUGEX1_1.get("F1DAT5"))
        CUGEX1_New.set("F1DAT6",  CUGEX1_1.get("F1DAT6"))
        CUGEX1_New.set("F1DAT7",  CUGEX1_1.get("F1DAT7"))
        CUGEX1_New.set("F1DAT9",  CUGEX1_1.get("F1DAT9"))
        LocalDateTime timeOfCreation = LocalDateTime.now()
        CUGEX1_New.setInt("F1RGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        CUGEX1_New.setInt("F1LMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
        CUGEX1_New.setInt("F1RGTM",  timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
        CUGEX1_New.setInt("F1CHNO", 1)
        CUGEX1_New.set("F1CHID", program.getUser())
        queryNewCUGEX1.insert(CUGEX1_New)
      }
    }
    CUGEX1_New.set("F1CONO", CUGEX1_1.get("F1CONO"))
    CUGEX1_New.set("F1FILE", "MPAGRL")
    CUGEX1_New.set("F1PK01", CUGEX1_1.get("F1PK01"))
    CUGEX1_New.set("F1PK02", CUGEX1_1.get("F1PK02"))
    CUGEX1_New.set("F1PK03", CUGEX1_1.get("F1PK03"))
    CUGEX1_New.set("F1PK04", CUGEX1_1.get("F1PK04"))
    CUGEX1_New.set("F1PK05", CUGEX1_1.get("F1PK05"))
    CUGEX1_New.set("F1PK06", CUGEX1_1.get("F1PK06"))
    CUGEX1_New.set("F1PK07", CUGEX1_1.get("F1PK07"))
    CUGEX1_New.set("F1PK08", CUGEX1_1.get("F1PK08"))
    if (!queryNewCUGEX1.readLock(CUGEX1_New, updateCallBackCUGEX1_Sup)) {
    }
  }
  // Delete CUGEX1
  Closure<?> updateCallBackCUGEX1_Sup = { LockedResult lockedResult3 ->
    aNVDT_Ancien = lockedResult3.get("F1PK08")
    iNVDT_Ancien = Integer.parseInt(aNVDT_Ancien.trim())
    if ((nvdt as Integer) != iNVDT_Ancien) {
      lockedResult3.delete()
    }
  }
}
