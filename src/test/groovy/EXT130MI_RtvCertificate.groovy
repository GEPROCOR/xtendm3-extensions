/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT130MI.RtvCertificate
 * Description : Manage certificate
 * Date         Changed By   Description
 * 20211103     RENARN       QUAX19 - Retrieve certificate
 */
import sun.util.resources.cldr.es.TimeZoneNames_es_419

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat
public class RtvCertificate extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  private final UtilityAPI utility;
  private final TextFilesAPI textFiles
  private final ExtensionAPI extension
  private boolean IN60
  private Integer currentCompany
  private String currentDivision
  private String orno = ""
  private Integer ponr = 0
  private Integer posx = 0
  private Integer dlix = 0
  private Integer zcid = 0
  private String do01 = ""
  private String do02 = ""
  private String do03 = ""
  private String do04 = ""
  private String do05 = ""
  private String do06 = ""
  private String do07 = ""
  private String do08 = ""
  private String do09 = ""
  private String do010 = ""
  private String do011 = ""
  private String do012 = ""
  private String do013 = ""
  private String do014 = ""
  private String do015 = ""
  private String cuno = ""
  private String faci = ""
  private String ortp = ""
  private String whlo = ""
  private String adid = ""
  private String itno = ""
  private String uca1 = ""
  private String uca2 = ""
  private String pide = ""
  private String rorn = ""
  private String rorl = ""
  private String town = ""
  private String cscd = ""
  private String csc2 = ""
  private String orco = ""
  private String cua1 = ""
  private String cua2 = ""
  private String cua3 = ""
  private String cua4 = ""
  private String adr1 = ""
  private String adr2 = ""
  private String adr3 = ""
  private String adr4 = ""
  private String CIADDR_conm = ""
  private String CIADDR_adr1 = ""
  private String CIADDR_adr2 = ""
  private String CIADDR_adr3 = ""
  private Integer stdt = 0
  private String sdes = ""
  private String parm = ""
  private Integer znco = 0
  private double plqt = 0
  private double trqt = 0
  private double cofa = 0
  private Integer conn = 0
  private Integer dsdt = 0
  private String dsdt_ddmmyyyy = ""
  private Integer dshm = 0
  private Integer dsd2 = 0
  private Integer duad = 0
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
  private String sucl = ""
  private String currentDate = ""
  private String ingredient1 = ""
  private String suno = ""
  private String sun3 = ""
  private String PO_PPO_PROD = ""
  private String CNUF = ""
  private Double saved_MNFP = 0
  private Double MPAPMA_MNFP = 0
  private String manufacturer = ""
  private String itty = ""
  private String orst = ""
  private String bano = ""
  private String zban = ""
  private String bref = ""
  private String ztem = ""
  private String zcem = ""
  private Integer expi = 0
  private Integer n196 = 0
  private Integer manufactureDate = 0
  boolean EXT015exist = false
  private String zaet = ""
  private String znaf = ""
  private String zcty = ""
  private Integer suty = 0
  private String stat = ""
  private String divi = ""

  public RtvCertificate(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, UtilityAPI utility, TextFilesAPI textFiles, MICallerAPI miCaller, ExtensionAPI extension) {
    this.mi = mi;
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility;
    this.textFiles = textFiles
    this.miCaller = miCaller
    this.extension = extension
  }

  public void main() {
    currentCompany
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    currentDivision = (String)program.getLDAZD().DIVI
    //logger.info("currentDivision 1 = " + currentDivision)

    dlix = 0
    if (mi.in.get("DLIX") != null && mi.in.get("DLIX") != "")
      dlix = mi.in.get("DLIX")

    // Delete records related to the delivery number from EXT130 table.
    deleteEXT130()

    // Retrieve informations (head level)
    retrieveInformations_1()

    // Read delivery lines
    DBAction query_MHDISL = database.table("MHDISL").index("00").selection("URRIDN", "URRIDL", "URRIDX", "URPLQT", "URTRQT").build()
    DBContainer MHDISL = query_MHDISL.getContainer()
    MHDISL.set("URCONO", currentCompany)
    MHDISL.set("URDLIX", dlix)
    MHDISL.set("URRORC", 3)
    if(!query_MHDISL.readAll(MHDISL, 3, outData_MHDISL)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  Closure<?> outData_MHDISL = { DBContainer MHDISL ->
    orno = MHDISL.get("URRIDN")
    ponr = MHDISL.get("URRIDL")
    posx = MHDISL.get("URRIDX")
    plqt = MHDISL.get("URPLQT")
    trqt = MHDISL.get("URTRQT")

    //logger.debug("MHDISL trouvé")
    //logger.debug("orno = " + orno)
    //logger.debug("ponr = " + ponr)
    //logger.debug("posx = " + posx)

    // Check if EXT015 exists for this order line before retrieving all the information otherwise it's useless
    EXT015exist = false
    DBAction query_EXT015 = database.table("EXT015").index("00").selection("EXZCID", "EXZCTY", "EXDO01", "EXDO02", "EXDO03", "EXDO04", "EXDO05", "EXDO06", "EXDO07", "EXDO08", "EXDO09", "EXDO10", "EXDO11", "EXDO12", "EXDO13", "EXDO14", "EXDO15","EXSTAT").build()
    DBContainer EXT015 = query_EXT015.getContainer()
    EXT015.set("EXCONO", currentCompany)
    EXT015.set("EXORNO", orno)
    EXT015.set("EXPONR", ponr)
    EXT015.set("EXPOSX", posx)
    if (!query_EXT015.readAll(EXT015, 4, outData_EXT015_checkExist)) {
    }

    if (EXT015exist) {
      retrieveInformations_2()

      // Read EXT015
      EXT015.set("EXCONO", currentCompany)
      EXT015.set("EXORNO", orno)
      EXT015.set("EXPONR", ponr)
      EXT015.set("EXPOSX", posx)
      if (!query_EXT015.readAll(EXT015, 4, outData_EXT015)) {
      }
    }
  }
  Closure<?> outData_EXT015_checkExist = { DBContainer EXT015 ->
    EXT015exist = true
  }
  Closure<?> outData_EXT015 = { DBContainer EXT015 ->
    //logger.debug("EXT015 trouvé")
    zcid = EXT015.get("EXZCID")
    zcty = EXT015.get("EXZCTY")
    stat = EXT015.get("EXSTAT")
    do01 = EXT015.get("EXDO01")
    do02 = EXT015.get("EXDO02")
    do03 = EXT015.get("EXDO03")
    do04 = EXT015.get("EXDO04")
    do05 = EXT015.get("EXDO05")
    do06 = EXT015.get("EXDO06")
    do07 = EXT015.get("EXDO07")
    do08 = EXT015.get("EXDO08")
    do09 = EXT015.get("EXDO09")
    do010 = EXT015.get("EXDO10")
    do011 = EXT015.get("EXDO11")
    do012 = EXT015.get("EXDO12")
    do013 = EXT015.get("EXDO13")
    do014 = EXT015.get("EXDO14")
    do015 = EXT015.get("EXDO15")
    if(do01.trim() != "")
      writeEXT130(do01)
    if(do02.trim() != "")
      writeEXT130(do02)
    if(do03.trim() != "")
      writeEXT130(do03)
    if(do04.trim() != "")
      writeEXT130(do04)
    if(do05.trim() != "")
      writeEXT130(do05)
    if(do06.trim() != "")
      writeEXT130(do06)
    if(do07.trim() != "")
      writeEXT130(do07)
    if(do08.trim() != "")
      writeEXT130(do08)
    if(do09.trim() != "")
      writeEXT130(do09)
    if(do010.trim() != "")
      writeEXT130(do010)
    if(do011.trim() != "")
      writeEXT130(do011)
    if(do012.trim() != "")
      writeEXT130(do012)
    if(do013.trim() != "")
      writeEXT130(do013)
    if(do014.trim() != "")
      writeEXT130(do014)
    if(do015.trim() != "")
      writeEXT130(do015)
  }
  // Write EXT130
  private writeEXT130(String document){
    //logger.info("currentDivision 2 = " + currentDivision)
    //logger.debug("Ecriture EXT130 - Document = " + document)
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query_EXT130 = database.table("EXT130").index("00").build()
    DBContainer EXT130 = query_EXT130.getContainer()
    EXT130.set("EXCONO", currentCompany)
    EXT130.set("EXORNO", orno)
    EXT130.set("EXPONR", ponr)
    EXT130.set("EXPOSX", posx)
    EXT130.set("EXDLIX", dlix)
    EXT130.set("EXZCID", zcid)
    EXT130.set("EXDOID", document)
    if (!query_EXT130.read(EXT130)) {
      EXT130.set("EXCUNO", cuno)
      EXT130.set("EXZCUA", cua1 + " " + cua2 + " " + cua3 + " " + cua4)
      EXT130.set("EXTOWN", town)
      EXT130.set("EXCSCD", cscd)
      EXT130.set("EXZNCO", znco)
      EXT130.set("EXCSC2", csc2)
      EXT130.set("EXCONN", conn)
      EXT130.set("EXZDSD", dsdt_ddmmyyyy + " " + dshm)
      EXT130.set("EXDSD2", dsd2)
      EXT130.set("EXZDES", CIADDR_conm + " " + CIADDR_adr1 + " " + CIADDR_adr2 + " " + CIADDR_adr3)
      EXT130.set("EXZISV", zisv)
      EXT130.set("EXZNAL", znal)
      EXT130.set("EXZPCB", zpcb)
      EXT130.set("EXMODL", modl)
      EXT130.set("EXDCPO", dcpo)
      EXT130.set("EXVENR", venr)
      EXT130.set("EXZSEA", sea0 + " " + sea1)
      EXT130.set("EXITNO", itno)
      EXT130.set("EXZCND", cofa)
      EXT130.set("EXNEWE", newe)
      EXT130.set("EXGRWE", grwe)
      EXT130.set("EXCSNO", csno)
      EXT130.set("EXSUN1", manufacturer)
      EXT130.set("EXZADR", adr1 + " " + adr2 + " " + adr3 + " " + adr4)
      EXT130.set("EXORCO", orco)
      EXT130.set("EXSPE1", ingredient1)
      EXT130.set("EXZBAN", zban)
      EXT130.set("EXZTEM", ztem)
      EXT130.set("EXZCEM", zcem)
      EXT130.set("EXZDFB", manufactureDate)
      EXT130.set("EXEXPI", expi)
      EXT130.set("EXITTY", itty)
      EXT130.set("EXZAET", zaet)
      EXT130.set("EXZNAF", znaf)
      EXT130.set("EXZCTY", zcty)
      EXT130.set("EXDIVI", divi)
      EXT130.set("EXSTAT", stat)
      EXT130.set("EXSUN3", sun3)
      EXT130.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT130.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT130.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT130.setInt("EXCHNO", 1)
      EXT130.set("EXCHID", program.getUser())
      query_EXT130.insert(EXT130)
    }
  }
  // Retrieve informations (head level)
  private void retrieveInformations_1(){
    DBAction query_MHDISH = database.table("MHDISH").index("00").selection("OQCONN", "OQMODL").build()
    DBContainer MHDISH = query_MHDISH.getContainer()
    MHDISH.set("OQCONO", currentCompany)
    MHDISH.set("OQINOU",  1)
    MHDISH.set("OQDLIX",  dlix)
    if(query_MHDISH.read(MHDISH)) {
      conn = MHDISH.get("OQCONN")
      modl = MHDISH.get("OQMODL")
      if(conn != 0){
        DBAction query_DCONSI = database.table("DCONSI").index("00").selection("DADSHM", "DADSDT").build()
        DBContainer DCONSI = query_DCONSI.getContainer()
        DCONSI.set("DACONO", currentCompany)
        DCONSI.set("DACONN",  conn)
        if(query_DCONSI.read(DCONSI)) {
          dsdt = DCONSI.get("DADSDT")
          dshm = DCONSI.get("DADSHM")
          if(dsdt != 0){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            // dsd2 = dsdt less 2 days
            LocalDate DATE = LocalDate.parse(dsdt as String, formatter)
            DATE = DATE.minusDays(2)
            dsd2 = DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
            // Format dsdt_ddmmyyyy
            dsdt_ddmmyyyy = DATE.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            DBAction query_CSYCAL = database.table("CSYCAL").index("00").selection("CDDUAD").build()
            DBContainer CSYCAL = query_CSYCAL.getContainer()
            CSYCAL.set("CDCONO", currentCompany)
            CSYCAL.set("CDYMD8",  dsd2)
            if(query_CSYCAL.read(CSYCAL)) {
              duad = CSYCAL.get("CDDUAD")
              if(duad == 1){
                // dsd2 = dsd2 less 2 days
                LocalDate DATE2 = LocalDate.parse(dsd2 as String, formatter)
                DATE2 = DATE2.minusDays(2)
                dsd2 = DATE2.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
              } else {
                // dsd2 = dsd2 less duad (Adjustment days due date)
                LocalDate DATE3 = LocalDate.parse(dsd2 as String, formatter)
                DATE3 = DATE3.plusDays(duad)
                dsd2 = DATE3.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
              }
            }
          }
        }
        DBAction query_DRADTR = database.table("DRADTR").index("00").selection("DRDCPO", "DRVENR", "DRSEA0", "DRSEA1").build()
        DBContainer DRADTR = query_DRADTR.getContainer()
        DRADTR.set("DRCONO", currentCompany)
        DRADTR.set("DRTLVL",  1)
        DRADTR.set("DRCONN",  conn)
        if(query_DRADTR.readAll(DRADTR, 3, outData_DRADTR)) {
        }
      }
    }
  }
  // Retrieve informations (line level)
  private void retrieveInformations_2(){
    cuno = ""
    faci = ""
    ortp = ""
    adid = ""
    town = ""
    cscd = ""
    cua1 = ""
    cua2 = ""
    cua3 = ""
    cua4 = ""
    adr1 = ""
    adr2 = ""
    adr3 = ""
    adr4 = ""
    whlo = ""
    itno = ""
    uca1 = ""
    uca2 = ""
    pide = ""
    rorn = ""
    rorl = ""
    cofa = 0
    znco = 0
    sdes = ""
    parm = ""
    csc2 = ""
    znal = ""
    zpcb = ""
    newe = 0
    grwe = 0
    csno = ""
    itty = ""
    orst = ""
    bano = ""
    zban = ""
    bref = ""
    ztem = ""
    zcem = ""
    expi = 0
    orco = ""
    n196 = 0
    manufactureDate = 0
    zaet = ""
    znaf = ""
    zcty = ""
    stat = ""
    CIADDR_conm = ""
    CIADDR_adr1 = ""
    CIADDR_adr2 = ""
    CIADDR_adr3 = ""
    divi = ""

    DBAction query_OOHEAD = database.table("OOHEAD").index("00").selection("OACUNO", "OAFACI", "OAORTP", "OAADID", "OAORSL", "OAORST").build()
    DBContainer OOHEAD = query_OOHEAD.getContainer()
    OOHEAD.set("OACONO", currentCompany)
    OOHEAD.set("OAORNO", orno)
    if(query_OOHEAD.read(OOHEAD)){
      faci = OOHEAD.get("OAFACI")
      ortp = OOHEAD.get("OAORTP")
      cuno = OOHEAD.get("OACUNO")
      adid = OOHEAD.get("OAADID")
      if(cuno.trim() != ""){
        // Retrieve customer
        DBAction query_OCUSMA = database.table("OCUSMA").index("00").selection("OKTOWN", "OKCSCD", "OKCUA1", "OKCUA2", "OKCUA3", "OKCUA4").build()
        DBContainer OCUSMA = query_OCUSMA.getContainer()
        OCUSMA.set("OKCONO", currentCompany)
        OCUSMA.set("OKCUNO", cuno)
        if(query_OCUSMA.read(OCUSMA)){
          town = OCUSMA.get("OKTOWN")
          cscd = OCUSMA.get("OKCSCD")
          cua1 = OCUSMA.get("OKCUA1")
          cua2 = OCUSMA.get("OKCUA2")
          cua3 = OCUSMA.get("OKCUA3")
          cua4 = OCUSMA.get("OKCUA4")
        }
      }
      if(adid.trim() != ""){
        // Retrieve customer address
        DBAction query_OCUSAD = database.table("OCUSAD").index("00").selection("OPTOWN", "OPCSCD", "OPCUA1", "OPCUA2", "OPCUA3", "OPCUA4").build()
        DBContainer OCUSAD = query_OCUSAD.getContainer()
        OCUSAD.set("OPCONO", currentCompany)
        OCUSAD.set("OPCUNO", cuno)
        OCUSAD.set("OPADRT", 1)
        OCUSAD.set("OPADID", adid)
        if(query_OCUSAD.read(OCUSAD)){
          town = OCUSAD.get("OPTOWN")
          cscd = OCUSAD.get("OPCSCD")
          cua1 = OCUSAD.get("OPCUA1")
          cua2 = OCUSAD.get("OPCUA2")
          cua3 = OCUSAD.get("OPCUA3")
          cua4 = OCUSAD.get("OPCUA4")
        }
      }
    }
    DBAction query_OOLINE = database.table("OOLINE").index("00").selection("OBCUNO", "OBWHLO", "OBITNO", "OBUCA1", "OBUCA2", "OBPIDE", "OBRORN", "OBRORL", "OBORST", "OBDIVI").build()
    DBContainer OOLINE = query_OOLINE.getContainer()
    OOLINE.set("OBCONO", currentCompany)
    OOLINE.set("OBORNO", orno)
    OOLINE.set("OBPONR", ponr)
    OOLINE.set("OBPOSX", posx)
    if(query_OOLINE.read(OOLINE)) {
      whlo = OOLINE.get("OBWHLO")
      itno = OOLINE.get("OBITNO")
      uca1 = OOLINE.get("OBUCA1")
      uca2 = OOLINE.get("OBUCA2")
      pide = OOLINE.get("OBPIDE")
      rorn = OOLINE.get("OBRORN")
      rorl = OOLINE.get("OBRORL")
      orst = OOLINE.get("OBORST")
      divi = OOLINE.get("OBDIVI")
      if (whlo.trim() != "") {
        DBAction query_MITWHL = database.table("MITWHL").index("00").selection("MWSDES").build()
        DBContainer MITWHL = query_MITWHL.getContainer()
        MITWHL.set("MWCONO", currentCompany)
        MITWHL.set("MWWHLO", whlo)
        if (query_MITWHL.read(MITWHL)) {
          sdes = MITWHL.get("MWSDES")
          //logger.debug("sdes = " + sdes)
          if (sdes.trim() != "") {
            //logger.debug("sdes = " + sdes)
            DBAction query_CUGEX1_1 = database.table("CUGEX1").index("00").selection("F1A030", "F1A130", "F1A230").build()
            DBContainer CUGEX1 = query_CUGEX1_1.getContainer()
            CUGEX1.set("F1CONO", currentCompany)
            CUGEX1.set("F1FILE", "CSYTAB")
            CUGEX1.set("F1PK01", "")
            CUGEX1.set("F1PK02", "EDES")
            CUGEX1.set("F1PK03", sdes)
            CUGEX1.set("F1PK04", "")
            CUGEX1.set("F1PK05", "")
            CUGEX1.set("F1PK06", "")
            CUGEX1.set("F1PK07", "")
            CUGEX1.set("F1PK08", "")
            if (query_CUGEX1_1.read(CUGEX1)) {
              zisv = CUGEX1.get("F1A030")
              znal = CUGEX1.get("F1A130")
              zpcb = CUGEX1.get("F1A230")
              //logger.debug("zisv = " + zisv)
              //logger.debug("znal = " + znal)
              //logger.debug("zpcb = " + zpcb)
            }
          }
        }
        // Retrieve place of loading
        DBAction query_CIADDR = database.table("CIADDR").index("00").selection("OACONM", "OAADR1", "OAADR2", "OAADR3", "OACSCD").build()
        DBContainer CIADDR = query_CIADDR.getContainer()
        CIADDR.set("OACONO", currentCompany)
        CIADDR.set("OAADTH", 1)
        CIADDR.set("OAADK2", whlo)
        if(query_CIADDR.read(CIADDR)){
          CIADDR_conm = CIADDR.get("OACONM")
          CIADDR_adr1 = CIADDR.get("OAADR1")
          CIADDR_adr2 = CIADDR.get("OAADR2")
          CIADDR_adr3 = CIADDR.get("OAADR3")
          csc2 = CIADDR.get("OACSCD")
        }
      }
      if (itno.trim() != "") {
        DBAction query_MITMAS = database.table("MITMAS").index("00").selection("MMITTY", "MMNEWE", "MMGRWE").build()
        DBContainer MITMAS = query_MITMAS.getContainer()
        MITMAS.set("MMCONO", currentCompany)
        MITMAS.set("MMITNO", itno)
        if (query_MITMAS.read(MITMAS)) {
          itty = MITMAS.get("MMITTY")
          newe = MITMAS.get("MMNEWE")
          grwe = MITMAS.get("MMGRWE")
        }
        DBAction query_MITAUN = database.table("MITAUN").index("00").selection("MUCOFA").build()
        DBContainer MITAUN = query_MITAUN.getContainer()
        MITAUN.set("MUCONO", currentCompany)
        MITAUN.set("MUITNO", itno)
        MITAUN.set("MUAUTP", 1)
        MITAUN.set("MUALUN", "COL")
        if (query_MITAUN.read(MITAUN)) {
          cofa = MITAUN.get("MUCOFA")
          if (plqt != 0 && cofa != 0)
            znco = (plqt / cofa) as Integer
          if (trqt != 0 && cofa != 0)
            znco = (trqt / cofa) as Integer
        }
      }
      if (itno.trim() != "" && (uca1.trim() != "" || uca2.trim() != "")) {
        DBAction query_CUGEX1_2 = database.table("CUGEX1").index("00").selection("F1N096", "F1N396").build()
        DBContainer CUGEX1 = query_CUGEX1_2.getContainer()
        CUGEX1.set("F1CONO", currentCompany)
        CUGEX1.set("F1FILE", "ZITPOP")
        CUGEX1.set("F1PK01", "03")
        CUGEX1.set("F1PK02", "ITM8")
        CUGEX1.set("F1PK03", itno)
        CUGEX1.set("F1PK04", uca1)
        CUGEX1.set("F1PK05", pide)
        CUGEX1.set("F1PK06", "")
        CUGEX1.set("F1PK07", "")
        CUGEX1.set("F1PK08", "")
        if (query_CUGEX1_2.read(CUGEX1)) {
          newe = CUGEX1.get("F1N096")
          grwe = CUGEX1.get("F1N396")
        } else {
          CUGEX1.set("F1PK04", uca2)
          if (query_CUGEX1_2.read(CUGEX1)) {
            newe = CUGEX1.get("F1N096")
            grwe = CUGEX1.get("F1N396")
          }
        }
      }
      if (itno.trim() != "" && faci.trim() != "") {
        //logger.debug("Recherche MITFAC cono/faci/itno = " + currentCompany + "/" + faci + "/" + itno)
        DBAction query_MITFAC = database.table("MITFAC").index("00").selection("M9CSNO").build()
        DBContainer MITFAC = query_MITFAC.getContainer()
        MITFAC.set("M9CONO", currentCompany)
        MITFAC.set("M9FACI", faci)
        MITFAC.set("M9ITNO", itno)
        if (query_MITFAC.read(MITFAC)) {
          csno = MITFAC.get("M9CSNO")
          //logger.debug("csno = " + csno)
        }
      }
      retrieveManufacturer()
      // Retrieve lot number
      if (orst.trim() >= "66") {
        DBAction query_MITTRA = database.table("MITTRA").index("30").selection("MTBANO").build()
        DBContainer MITTRA = query_MITTRA.getContainer()
        MITTRA.set("MTCONO", currentCompany)
        MITTRA.set("MTTTYP", 31)
        MITTRA.set("MTRIDN", orno)
        MITTRA.set("MTRIDL", ponr)
        MITTRA.set("MTRIDX", posx)
        MITTRA.set("MTRIDI", dlix)
        if (!query_MITTRA.readAll(MITTRA, 6, outData_MITTRA)) {
        }
      } else {
        DBAction query_MITALO = database.table("MITALO").index("10").selection("MQBANO").build()
        DBContainer MITALO = query_MITALO.getContainer()
        MITALO.set("MQCONO", currentCompany)
        MITALO.set("MQTTYP", 31)
        MITALO.set("MQRIDN", orno)
        MITALO.set("MQRIDO", 0)
        MITALO.set("MQRIDL", ponr)
        MITALO.set("MQRIDX", posx)
        MITALO.set("MQRIDI", dlix)
        if (!query_MITALO.readAll(MITALO, 7, outData_MITALO)) {
        }
      }
      if (bano.trim() != "") {
        DBAction query_MILOMA = database.table("MILOMA").index("00").selection("LMEXPI", "LMBRE2").build()
        DBContainer MILOMA = query_MILOMA.getContainer()
        MILOMA.set("LMCONO", currentCompany)
        MILOMA.set("LMITNO", itno)
        MILOMA.set("LMBANO", bano)
        if (query_MILOMA.read(MILOMA)) {
          if(MILOMA.get("LMBRE2") == "")
            expi = MILOMA.get("LMEXPI")
        }
        DBAction query_CUGEX1_3 = database.table("CUGEX1").index("00").selection("F1A330", "F1A430").build()
        DBContainer CUGEX1 = query_CUGEX1_3.getContainer()
        CUGEX1.set("F1CONO", currentCompany)
        CUGEX1.set("F1FILE", "MILOMA")
        CUGEX1.set("F1PK01", itno)
        CUGEX1.set("F1PK02", bano)
        CUGEX1.set("F1PK03", "")
        CUGEX1.set("F1PK04", "")
        CUGEX1.set("F1PK05", "")
        CUGEX1.set("F1PK06", "")
        CUGEX1.set("F1PK07", "")
        CUGEX1.set("F1PK08", "")
        if (query_CUGEX1_3.read(CUGEX1)) {
          zcem = CUGEX1.get("F1A330")
          ztem = CUGEX1.get("F1A430")
        }
      }
      if(expi != 0) {
        //logger.debug("Recherche date de fabrication - itno/CNUF = " + itno + "/" + CNUF)
        DBAction query = database.table("CUGEX1").index("00").selection("F1N196").build()
        DBContainer CUGEX1 = query.getContainer()
        CUGEX1.set("F1CONO", currentCompany)
        CUGEX1.set("F1FILE", "MITVEN")
        CUGEX1.set("F1PK01", itno)
        CUGEX1.set("F1PK02", "")
        CUGEX1.set("F1PK03", "")
        if(CNUF.trim() != "") {
          CUGEX1.set("F1PK04", CNUF)
        } else {
          CUGEX1.set("F1PK04", suno)
        }
        CUGEX1.set("F1PK05", "")
        CUGEX1.set("F1PK06", "")
        CUGEX1.set("F1PK07", "")
        CUGEX1.set("F1PK08", "")
        if (query.read(CUGEX1)) {
          n196 = CUGEX1.get("F1N196")
          //logger.debug("Recherche date de fabrication - n196 = " + n196)
          if (n196 != 0) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            // manufactureDate = expi less n196 days
            LocalDate DATE = LocalDate.parse(expi as String, formatter)
            DATE = DATE.minusDays(n196)
            manufactureDate = DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
          }
        }
      }
    }
  }
  // Retrieve manufacturer
  private void retrieveManufacturer(){
    manufacturer = ""
    suno = ""
    PO_PPO_PROD = ""
    CNUF = ""
    sun3 = ""
    DBAction suno_query = database.table("MPOPLP").index("90").selection("PORORN", "PORORL", "POSUNO", "POPROD").build()
    DBContainer MPOPLP = suno_query.getContainer()
    MPOPLP.set("POCONO", currentCompany)
    MPOPLP.set("PORORN", orno)
    MPOPLP.set("PORORL", ponr)
    MPOPLP.set("PORORX", posx)
    MPOPLP.set("PORORC", 3)
    if (suno_query.readAll(MPOPLP, 5, outData_MPOPLP)) {
    }
    //logger.debug("Après lecture MPOPLP suno = " + suno)

    if (suno.trim() == "") {
      //logger.debug("Recherche MPLINE avec orno = " + orno)
      //logger.debug("Recherche MPLINE avec ponr = " + ponr)
      //logger.debug("Recherche MPLINE avec posx = " + posx)
      DBAction suno_query2 = database.table("MPLINE").index("20").selection("IBRORN", "IBRORL", "IBSUNO", "IBPROD").build()
      DBContainer MPLINE = suno_query2.getContainer()
      MPLINE.set("IBCONO", currentCompany)
      MPLINE.set("IBRORN", orno)
      MPLINE.set("IBRORL", ponr)
      MPLINE.set("IBRORX", posx)
      MPLINE.set("IBRORC", 3)
      if (suno_query2.readAll(MPLINE, 5, outData_MPLINE)) {
      }
      //logger.debug("Après lecture MPLINE suno = " + suno)
    }
    //logger.debug("Après recherche suno = " + suno)
    suty = 0
    if (suno.trim() != "") {
      DBAction query_CIDMAS = database.table("CIDMAS").index("00").selection("IDSUTY").build()
      DBContainer CIDMAS = query_CIDMAS.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO", suno)
      if (query_CIDMAS.read(CIDMAS)) {
        suty = CIDMAS.get("IDSUTY")
        //logger.debug("suty suno = " + suty)
        if(suty == 0){
          suno = suno.substring(0, 5)
          //logger.debug("suno tronqué = " + suno)
        }
      }
    }
    //logger.debug("Après recherche PO_PPO_PROD = " + PO_PPO_PROD)
    suty = 0
    if (PO_PPO_PROD.trim() != "") {
      DBAction query_CIDMAS = database.table("CIDMAS").index("00").selection("IDSUTY").build()
      DBContainer CIDMAS = query_CIDMAS.getContainer()
      CIDMAS.set("IDCONO", currentCompany)
      CIDMAS.set("IDSUNO", PO_PPO_PROD)
      if (query_CIDMAS.read(CIDMAS)) {
        suty = CIDMAS.get("IDSUTY")
        //logger.debug("suty PO_PPO_PROD = " + suty)
        if(suty == 0){
          PO_PPO_PROD = PO_PPO_PROD.substring(0, 5)
          //logger.debug("PO_PPO_PROD tronqué = " + PO_PPO_PROD)
        }
      }
    }

    retrieveInformations_3()

    if (manufacturer.trim() != ""){
      // Retrieve manufacturer address
      DBAction query_CIDADR = database.table("CIDADR").index("10").selection("SASTDT", "SAADR1", "SAADR2", "SAADR3", "SAADR4", "SACSCD").reverse().build()
      DBContainer CIDADR = query_CIDADR.getContainer()
      CIDADR.set("SACONO", currentCompany)
      CIDADR.set("SASUNO", manufacturer)
      CIDADR.set("SAADTE", 1)
      if(!query_CIDADR.readAll(CIDADR, 3, outData_CIDADR)){
        CIDADR.set("SACONO", currentCompany)
        CIDADR.set("SASUNO", manufacturer)
        CIDADR.set("SAADTE", 3)
        if(!query_CIDADR.readAll(CIDADR, 3, outData_CIDADR)){
        }
      }
      // Retrieve other faility and approval number
      DBAction query_CUGEX1_3 = database.table("CUGEX1").index("00").selection("F1A121", "F1A130").build()
      DBContainer CUGEX1 = query_CUGEX1_3.getContainer()
      CUGEX1.set("F1CONO", currentCompany)
      CUGEX1.set("F1FILE", "CIDMAS")
      CUGEX1.set("F1PK01", manufacturer)
      CUGEX1.set("F1PK02", "")
      CUGEX1.set("F1PK03", "")
      CUGEX1.set("F1PK04", "")
      CUGEX1.set("F1PK05", "")
      CUGEX1.set("F1PK06", "")
      CUGEX1.set("F1PK07", "")
      CUGEX1.set("F1PK08", "")
      if (query_CUGEX1_3.read(CUGEX1)) {
        zaet = CUGEX1.get("F1A130")
        znaf = CUGEX1.get("F1A121")
      }
    }
  }
  Closure<?> outData_DRADTR = { DBContainer DRADTR ->
    dcpo = DRADTR.get("DRDCPO")
    venr = DRADTR.get("DRVENR")
    sea0 = DRADTR.get("DRSEA0")
    sea1 = DRADTR.get("DRSEA1")
  }
  Closure<?> outData_CIDADR = { DBContainer CIDADR ->
    stdt = CIDADR.get("SASTDT")
    if(stdt <= (currentDate as Integer) && adr1.trim() == "") {
      adr1 = CIDADR.get("SAADR1")
      adr2 = CIDADR.get("SAADR2")
      adr3 = CIDADR.get("SAADR3")
      adr4 = CIDADR.get("SAADR4")
      orco = CIDADR.get("SACSCD")
    }
  }
  Closure<?> outData_MPOPLP = { DBContainer MPOPLP ->
    suno = MPOPLP.get("POSUNO")
    PO_PPO_PROD = MPOPLP.get("POPROD")
  }
  Closure<?> outData_MPLINE = { DBContainer MPLINE ->
    suno = MPLINE.get("IBSUNO")
    PO_PPO_PROD = MPLINE.get("IBPROD")
  }
  Closure<?> outData_MITTRA = { DBContainer MITTRA ->
    bano = MITTRA.get("MTBANO")
    if (zban.trim() == ""){
      DBAction query_MILOMA = database.table("MILOMA").index("00").selection("LMBREF").build()
      DBContainer MILOMA = query_MILOMA.getContainer()
      MILOMA.set("LMCONO", currentCompany)
      MILOMA.set("LMITNO", itno)
      MILOMA.set("LMBANO", bano)
      if (query_MILOMA.read(MILOMA)) {
        bref = MILOMA.get("LMBREF")
      }
      zban = bref
    } else {
      DBAction query_MILOMA = database.table("MILOMA").index("00").selection("LMBREF").build()
      DBContainer MILOMA = query_MILOMA.getContainer()
      MILOMA.set("LMCONO", currentCompany)
      MILOMA.set("LMITNO", itno)
      MILOMA.set("LMBANO", bano)
      if (query_MILOMA.read(MILOMA)) {
        bref = MILOMA.get("LMBREF")
      }
      zban = zban + " " + bref
    }
  }
  Closure<?> outData_MITALO = { DBContainer MITALO ->
    bano = MITALO.get("MQBANO")
    if (zban.trim() == ""){
      DBAction query_MILOMA = database.table("MILOMA").index("00").selection("LMBREF").build()
      DBContainer MILOMA = query_MILOMA.getContainer()
      MILOMA.set("LMCONO", currentCompany)
      MILOMA.set("LMITNO", itno)
      MILOMA.set("LMBANO", bano)
      if (query_MILOMA.read(MILOMA)) {
        bref = MILOMA.get("LMBREF")
      }
      zban = bref
    } else {
      DBAction query_MILOMA = database.table("MILOMA").index("00").selection("LMBREF").build()
      DBContainer MILOMA = query_MILOMA.getContainer()
      MILOMA.set("LMCONO", currentCompany)
      MILOMA.set("LMITNO", itno)
      MILOMA.set("LMBANO", bano)
      if (query_MILOMA.read(MILOMA)) {
        bref = MILOMA.get("LMBREF")
      }
      zban = zban + " " + bref
    }
  }
  Closure<?> outData_MPAPMA1 = { DBContainer MPAPMA1 ->
    //logger.debug("MPAPMA1 trouvé PRIO = " + MPAPMA1.get("AMPRIO"))
    //logger.debug("MPAPMA1 trouvé OBV1 = " + MPAPMA1.get("AMOBV1"))
    //logger.debug("MPAPMA1 trouvé OBV2 = " + MPAPMA1.get("AMOBV2"))
    //logger.debug("MPAPMA1 trouvé OBV3 = " + MPAPMA1.get("AMOBV3"))
    MPAPMA_MNFP = MPAPMA1.get("AMMNFP")
    if(MPAPMA_MNFP < saved_MNFP){
      saved_MNFP = MPAPMA1.get("AMMNFP")
      CNUF = MPAPMA1.get("AMPROD")
      CNUF = CNUF.substring(0, 5)
    }
  }
  Closure<?> outData_MPAPMA2 = { DBContainer MPAPMA2 ->
    logger.debug("MPAPMA2 trouvé PRIO = " + MPAPMA2.get("AMPRIO"))
    logger.debug("MPAPMA2 trouvé OBV1 = " + MPAPMA2.get("AMOBV1"))
    logger.debug("MPAPMA2 trouvé OBV2 = " + MPAPMA2.get("AMOBV2"))
    logger.debug("MPAPMA2 trouvé OBV3 = " + MPAPMA2.get("AMOBV3"))
    MPAPMA_MNFP = MPAPMA2.get("AMMNFP")
    if(MPAPMA_MNFP < saved_MNFP){
      saved_MNFP = MPAPMA2.get("AMMNFP")
      manufacturer = MPAPMA2.get("AMPROD")
    }
    DBAction query = database.table("CUGEX1").index("00").selection("F1A030", "F1A130", "F1A230", "F1A330", "F1A430", "F1A530", "F1A630", "F1A730", "F1A830", "F1A930").build()
    DBContainer CUGEX1 = query.getContainer()
    CUGEX1.set("F1CONO", currentCompany)
    CUGEX1.set("F1FILE",  "MITVEN")
    CUGEX1.set("F1PK01",  itno)
    CUGEX1.set("F1PK02",  "")
    CUGEX1.set("F1PK03",  "")
    CUGEX1.set("F1PK04",  MPAPMA2.get("AMPROD"))
    CUGEX1.set("F1PK05",  "")
    CUGEX1.set("F1PK06",  "")
    CUGEX1.set("F1PK07",  "")
    CUGEX1.set("F1PK08",  "")
    if(query.read(CUGEX1)){
      ingredient1 = CUGEX1.get("F1A030")
    }
  }
  // Retrieve informations (manufacturer)
  private void retrieveInformations_3(){
    ingredient1 = ""
    //logger.debug("retrieveInformations")
    // Supplier has been retrieved from purchase order or planned purchase order
    sucl = ""
    if(suno != ""){
      DBAction CIDVEN_query = database.table("CIDVEN").index("00").selection("IISUCL").build()
      DBContainer CIDVEN = CIDVEN_query.getContainer()
      CIDVEN.set("IICONO", currentCompany)
      CIDVEN.set("IISUNO", suno)
      if (CIDVEN_query.read(CIDVEN)) {
        sucl = CIDVEN.get("IISUCL")
        check_PO_PPO_Supplier()
      }
    } else {
      // Supplier was not found in PO/PPO, it must be retrieved from item whlo
      DBAction MITBAL_query = database.table("MITBAL").index("00").selection("MBSUNO").build()
      DBContainer MITBAL = MITBAL_query.getContainer()
      MITBAL.set("MBCONO", currentCompany)
      MITBAL.set("MBWHLO", whlo)
      MITBAL.set("MBITNO", itno)
      if (MITBAL_query.read(MITBAL)) {
        suno = MITBAL.get("MBSUNO")
        suno = suno.substring(0, 5)
        DBAction CIDVEN_query = database.table("CIDVEN").index("00").selection("IISUCL").build()
        DBContainer CIDVEN = CIDVEN_query.getContainer()
        CIDVEN.set("IICONO", currentCompany)
        CIDVEN.set("IISUNO", suno)
        if (CIDVEN_query.read(CIDVEN)) {
          sucl = CIDVEN.get("IISUCL")
          check_ItemWarehouseSupplier()
        }
      }
    }
    if(CNUF.trim() == ""){
      sun3 = suno
    } else {
      sun3 = CNUF
    }
  }
  // Retrieve informations from supplier that has been retrieved from purchase order or planned purchase order
  public void check_PO_PPO_Supplier(){
    //logger.debug("check_PO_PPO_Supplier")
    //logger.debug("check_ItemWarehouseSupplier - sucl = " + sucl)
    LocalDateTime timeOfCreation = LocalDateTime.now()
    currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    // Supplier base
    if(sucl == "100"){
      CNUF = PO_PPO_PROD
      //logger.debug("check_PO_PPO_Supplier - CNUF is PROD = " + CNUF)
      // Retrieve manufacturer and ingredients
      saved_MNFP = 10
      ExpressionFactory expression_MPAPMA2 = database.getExpressionFactory("MPAPMA")
      expression_MPAPMA2 = expression_MPAPMA2.eq("AMMFRS", "20")
      expression_MPAPMA2 = expression_MPAPMA2.and(expression_MPAPMA2.le("AMFDAT", currentDate))
      expression_MPAPMA2 = expression_MPAPMA2.and((expression_MPAPMA2.ge("AMTDAT", currentDate)).or(expression_MPAPMA2.eq("AMTDAT", "0")))
      expression_MPAPMA2 = expression_MPAPMA2.and(expression_MPAPMA2.eq("AMOBV3", suno))
      expression_MPAPMA2 = expression_MPAPMA2.and(expression_MPAPMA2.eq("AMOBV4", CNUF))
      DBAction manufacturer_query = database.table("MPAPMA").index("00").matching(expression_MPAPMA2).selection("AMPRIO", "OBV1", "OBV2", "OBV3", "OBV4", "OBV5", "AMFDAT", "AMPROD", "AMMNFP").build()
      DBContainer MPAPMA2 = manufacturer_query.getContainer()
      MPAPMA2.set("AMCONO", currentCompany)
      MPAPMA2.set("AMPRIO", 2)
      MPAPMA2.set("AMOBV1", itno)
      if(manufacturer_query.readAll(MPAPMA2, 3, outData_MPAPMA2)){
      }
      //logger.debug("check_PO_PPO_Supplier - manufacturer = " + manufacturer)
      //logger.debug("check_PO_PPO_Supplier - ingredient1 = " + ingredient1)
    }
    // CNUF
    if(sucl == "200"){
      CNUF = ""   // In this case, suno is the CNUF
      //logger.debug("check_PO_PPO_Supplier - CNUF is blank = " + CNUF)
      // Retrieve manufacturer and ingredients
      saved_MNFP = 10
      ExpressionFactory expression_MPAPMA2 = database.getExpressionFactory("MPAPMA")
      expression_MPAPMA2 = expression_MPAPMA2.eq("AMMFRS", "20")
      expression_MPAPMA2 = expression_MPAPMA2.and(expression_MPAPMA2.le("AMFDAT", currentDate))
      expression_MPAPMA2 = expression_MPAPMA2.and((expression_MPAPMA2.ge("AMTDAT", currentDate)).or(expression_MPAPMA2.eq("AMTDAT", "0")))
      expression_MPAPMA2 = expression_MPAPMA2.and(expression_MPAPMA2.eq("AMOBV4", suno))
      DBAction manufacturer_query = database.table("MPAPMA").index("00").matching(expression_MPAPMA2).selection("AMPRIO", "OBV1", "OBV2", "OBV3", "OBV4", "OBV5", "AMFDAT", "AMPROD", "AMMNFP").build()
      DBContainer MPAPMA2 = manufacturer_query.getContainer()
      MPAPMA2.set("AMCONO", currentCompany)
      MPAPMA2.set("AMPRIO", 2)
      MPAPMA2.set("AMOBV1", itno)
      if(manufacturer_query.readAll(MPAPMA2, 3, outData_MPAPMA2)){
      }
      //logger.debug("check_PO_PPO_Supplier - manufacturer = " + manufacturer)
      //logger.debug("check_PO_PPO_Supplier - ingredient1 = " + ingredient1)
    }
  }
  // Retrieve informations from supplier that has been retrieved from item whlo
  public void check_ItemWarehouseSupplier(){
    //logger.debug("check_ItemWarehouseSupplier")
    //logger.debug("check_ItemWarehouseSupplier - sucl = " + sucl)
    LocalDateTime timeOfCreation = LocalDateTime.now()
    currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    // Supplier base
    if(sucl == "100"){
      // Retrieve CNUF
      saved_MNFP = 10
      ExpressionFactory expression_MPAPMA1 = database.getExpressionFactory("MPAPMA")
      expression_MPAPMA1 = expression_MPAPMA1.eq("AMMFRS", "20")
      expression_MPAPMA1 = expression_MPAPMA1.and(expression_MPAPMA1.le("AMFDAT", currentDate))
      expression_MPAPMA1 = expression_MPAPMA1.and((expression_MPAPMA1.ge("AMTDAT", currentDate)).or(expression_MPAPMA1.eq("AMTDAT", "0")))
      DBAction CNUF_query = database.table("MPAPMA").index("00").matching(expression_MPAPMA1).selection("AMPRIO", "OBV1", "OBV2", "OBV3", "OBV4", "OBV5", "AMFDAT", "AMPROD", "AMMNFP").build()
      DBContainer MPAPMA1 = CNUF_query.getContainer()
      MPAPMA1.set("AMCONO", currentCompany)
      MPAPMA1.set("AMPRIO", 5)
      MPAPMA1.set("AMOBV1", suno)
      MPAPMA1.set("AMOBV2", itno)
      if(CNUF_query.readAll(MPAPMA1, 4, outData_MPAPMA1)){
      }
      //logger.debug("check_ItemWarehouseSupplier - CNUF from MPAPMA = " + CNUF)
      // Retrieve manufacturer and ingredients
      saved_MNFP = 10
      ExpressionFactory expression_MPAPMA2 = database.getExpressionFactory("MPAPMA")
      expression_MPAPMA2 = expression_MPAPMA2.eq("AMMFRS", "20")
      expression_MPAPMA2 = expression_MPAPMA2.and(expression_MPAPMA2.le("AMFDAT", currentDate))
      expression_MPAPMA2 = expression_MPAPMA2.and((expression_MPAPMA2.ge("AMTDAT", currentDate)).or(expression_MPAPMA2.eq("AMTDAT", "0")))
      expression_MPAPMA2 = expression_MPAPMA2.and(expression_MPAPMA2.eq("AMOBV4", CNUF))
      DBAction manufacturer_query = database.table("MPAPMA").index("00").matching(expression_MPAPMA2).selection("AMPRIO", "OBV1", "OBV2", "OBV3", "OBV4", "OBV5", "AMFDAT", "AMPROD", "AMMNFP").build()
      DBContainer MPAPMA2 = manufacturer_query.getContainer()
      MPAPMA2.set("AMCONO", currentCompany)
      MPAPMA2.set("AMPRIO", 2)
      MPAPMA2.set("AMOBV1", itno)
      MPAPMA2.set("AMOBV2", suno)
      if(manufacturer_query.readAll(MPAPMA2, 4, outData_MPAPMA2)){
      }
      //logger.debug("check_ItemWarehouseSupplier - manufacturer = " + manufacturer)
      //logger.debug("check_ItemWarehouseSupplier - ingredient1 = " + ingredient1)
    }
    //CNUF
    if(sucl == "200"){
      logger.info("check_ItemWarehouseSupplier sucl 200")
      CNUF = ""   // In this case, suno is the CNUF
      //logger.debug("check_ItemWarehouseSupplier - CNUF is blank = " + CNUF)
      // Retrieve manufacturer and ingredients
      saved_MNFP = 10
      ExpressionFactory expression_MPAPMA2 = database.getExpressionFactory("MPAPMA")
      expression_MPAPMA2 = expression_MPAPMA2.eq("AMMFRS", "20")
      expression_MPAPMA2 = expression_MPAPMA2.and(expression_MPAPMA2.le("AMFDAT", currentDate))
      expression_MPAPMA2 = expression_MPAPMA2.and((expression_MPAPMA2.ge("AMTDAT", currentDate)).or(expression_MPAPMA2.eq("AMTDAT", "0")))
      expression_MPAPMA2 = expression_MPAPMA2.and(expression_MPAPMA2.eq("AMOBV4", suno))
      DBAction manufacturer_query = database.table("MPAPMA").index("00").matching(expression_MPAPMA2).selection("AMPRIO", "OBV1", "OBV2", "OBV3", "OBV4", "OBV5", "AMFDAT", "AMPROD", "AMMNFP").build()
      DBContainer MPAPMA2 = manufacturer_query.getContainer()
      MPAPMA2.set("AMCONO", currentCompany)
      MPAPMA2.set("AMPRIO", 2)
      MPAPMA2.set("AMOBV1", itno)
      if(manufacturer_query.readAll(MPAPMA2, 3, outData_MPAPMA2)){
      }
      logger.info("check_ItemWarehouseSupplier sucl 200 - CNUF = " + CNUF)
      //logger.debug("check_ItemWarehouseSupplier - manufacturer = " + manufacturer)
      //logger.debug("check_ItemWarehouseSupplier - ingredient1 = " + ingredient1)
    }
  }
  /**
   * Delete records related to the delivery number from EXT130 table
   */
  public void deleteEXT130(){
    LocalDateTime timeOfCreation = LocalDateTime.now();
    DBAction query = database.table("EXT130").index("20").selection("EXDLIX").build()
    DBContainer EXT130 = query.getContainer();
    EXT130.set("EXCONO", currentCompany);
    EXT130.set("EXDLIX", dlix);
    if(!query.readAllLock(EXT130, 2, updateCallBack)){
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
