/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT171MI.UpdPOAgreement
 * Description : Update purchase order line agreement
 * Date         Changed By   Description
 * 20210412     RENARN       APPX20 - Purchase agreement retrieving
 * 20250211     YVOYOU       REA32 - SITE change and no read CUGEX1
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class UpdPOAgreement extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private int currentCompany
  private String currentDate
  private String iPUNO = ""
  private Integer iPNLI = 0
  private Integer iPNLS = 0
  private String oOURR
  private String iWHLO = ""
  private String iITNO = ""
  private String iFVDT = ""
  private String iPIDE = ""
  private String iZIPL = ""
  private String iRORN = ""
  private Integer iRORL = 0
  private Integer iRORX = 0
  private Integer iGETY = 0
  private String svAGNB = ""
  private double svPUPR = 0
  private String svPPUN = ""
  private String svITM8 = ""
  private String svSUNO = ""
  private String svDIVI = ""
  private Integer SVAGPT = 0
  private String oVAGN = ""
  private String SVPRIO = ""
  private String oZIPP = ""
  private String oPRIO = ""
  private Integer saveFVDT = 0
  private Integer iFvdtInt
  private Integer svSEQN = 0
  private String saveWHLO = ""
  private String oTEDL = ""
  private String svTEDL = ""
  private String svZIPP = ""

  public UpdPOAgreement(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility, MICallerAPI miCaller) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
    this.miCaller = miCaller
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    // Get current date
    LocalDateTime timeOfCreation = LocalDateTime.now()
    currentDate = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    iFVDT = currentDate

    if(mi.in.get("PUNO") != null){
      iPUNO = mi.in.get("PUNO")
    }else{
      mi.error("Ordre planifié est obligatoire")
      return
    }

    if(mi.in.get("PNLI") != null)
      iPNLI = (Integer)mi.in.get("PNLI")

    if(mi.in.get("PNLS") != null)
      iPNLS = (Integer)mi.in.get("PNLS")

    logger.debug("iPUNO = " + iPUNO)
    logger.debug("iPNLI = " + iPNLI)
    logger.debug("iPNLS = " + iPNLS)

    DBAction query = database.table("MPLINE").index("00").selection("IBWHLO","IBITNO", "IBSUNO", "IBGETY","IBRORN","IBRORL","IBRORX", "IBSITE").build()
    DBContainer dbMPLINE = query.getContainer()
    dbMPLINE.set("IBCONO", currentCompany)
    dbMPLINE.set("IBPUNO", iPUNO)
    dbMPLINE.setInt("IBPNLI", iPNLI)
    dbMPLINE.setInt("IBPNLS", iPNLS)
    if(!query.read(dbMPLINE)){
      mi.error("L'enregistrement n'existe pas")
      return
    } else {
      iWHLO = dbMPLINE.get("IBWHLO")
      iITNO = dbMPLINE.get("IBITNO")
      svSUNO = dbMPLINE.get("IBSUNO")
      iGETY = (dbMPLINE.get("IBGETY") as String) as Integer
      iRORN = dbMPLINE.get("IBRORN")
      iRORL = dbMPLINE.get("IBRORL")
      iRORX = dbMPLINE.get("IBRORX")
      svITM8 = dbMPLINE.get("IBSITE")
      SVAGPT= 0
      oVAGN="00000"
      SVPRIO=""
      oZIPP=""
      oPRIO=""
      saveFVDT=0
      iFvdtInt=iFVDT as Integer

      def tableArticleDepot = database.table("MITBAL").index("00").selection("MBSUNO","MBDIVI").build()
      def MITBAL = tableArticleDepot.getContainer()
      MITBAL.set("MBCONO", currentCompany)
      MITBAL.set("MBITNO", iITNO)
      MITBAL.set("MBWHLO", iWHLO)
      tableArticleDepot.readAll(MITBAL,3,{DBContainer recordMITBAL->
        logger.debug("MITBAL trouvé")
        svDIVI = recordMITBAL.get("MBDIVI")
      })
      // Phase 1 item
      logger.debug("*** Phase 1 Item")
      if (true || iGETY == 20 || iGETY == 28) {
        logger.debug("iGETY = " + iGETY)
        def tableEXT033 = database.table("EXT033").index("10").selection("EXZIPP","EXPRIO").build()
        def EXT033 = tableEXT033.getContainer()
        EXT033.set("EXCONO", currentCompany)
        EXT033.set("EXWHLO", iWHLO)
        tableEXT033.readAll(EXT033,2,{DBContainer recordEXT033->
          oZIPP = recordEXT033.get("EXZIPP")
          oPRIO = recordEXT033.get("EXPRIO")
          logger.debug(String.format("EXT033 found WHLO=%s > ZIPP=%s, PRIO=%s",iWHLO, oZIPP, oPRIO))
          retrieveAgreement()
        })
      }
      // Phase 2 item
      logger.debug("*** Phase 2 Item")
      if (false || iGETY == 23 || iGETY == 24) {
        logger.debug("iGETY = " + iGETY)
        logger.debug("iRORN = + " + iRORN)
        logger.debug("iRORL = + " + iRORL)
        logger.debug("iRORX = + " + iRORX)
        DBAction queryOOLINE = database.table("OOLINE").index("00").selection("OBPIDE", "OBPROJ").build()
        DBContainer dbOOLINE = queryOOLINE.getContainer()
        dbOOLINE.set("OBCONO", currentCompany)
        dbOOLINE.set("OBORNO", iRORN)
        dbOOLINE.set("OBPONR", iRORL)
        dbOOLINE.set("OBPOSX", iRORX)
        if (queryOOLINE.read(dbOOLINE)) {
          iPIDE = dbOOLINE.get("OBPIDE")
          iZIPL = dbOOLINE.get("OBPROJ")
          logger.debug(String.format("OOLINE found ORNO=%s, PONR=%s > PIDE=%s, PROJ=%s",iRORN, iRORL, iPIDE, iZIPL))
          def queryOPROMH = database.table("OPROMH").index("00").selection("FZTX15").build()
          def dbOPROMH = queryOPROMH.getContainer()
          dbOPROMH.set("FZCONO", currentCompany)
          dbOPROMH.set("FZDIVI", svDIVI)
          dbOPROMH.set("FZPIDE", iPIDE)
          queryOPROMH.readAll(dbOPROMH, 3, { DBContainer recordOPROMH ->
            oVAGN = recordOPROMH.get("FZTX15")
            logger.debug(String.format("OPROMH found PIDE=%s, > VAGN=%s", iPIDE, oVAGN))
            if (oVAGN == "" || oVAGN == null) {
              oVAGN = "00000"
            }
          })
        }
        def queryEXT030 = database.table("EXT030").index("10").selection("EXZIPP","EXPRIO").build()
        def dbEXT030 = queryEXT030.getContainer()
        dbEXT030.set("EXCONO", currentCompany)
        dbEXT030.set("EXZIPS", iZIPL)
        queryEXT030.readAll(dbEXT030,2,{DBContainer recordEXT030->
          logger.debug("EXT030 trouvé")
          oZIPP = recordEXT030.get("EXZIPP")
          oPRIO = recordEXT030.get("EXPRIO")
          retrieveAgreement()
        })
      }
    }
    logger.debug("*** End : Upd PO Line PPS200MI")
    logger.debug("value svAGNB = " + svAGNB)
    logger.debug("executePPS200MIUpdLine iPUNO =" + iPUNO)
    logger.debug("executePPS200MIUpdLine iPNLI =" + iPNLI)
    logger.debug("executePPS200MIUpdLine iPNLS =" + iPNLS)
    logger.debug("executePPS200MIUpdLine oOURR =" + oOURR)
    // Update purchase order line
    logger.debug(String.format("Program ending : return AGNB=%s, ITM8=%s, TEDL=%s, ZIPP=%s", svAGNB, svITM8, svTEDL, svZIPP))
    mi.outData.put("PUNO", iPUNO)
    mi.outData.put("PNLI", iPNLI as String)
    mi.outData.put("PNLS", iPNLS as String)
    mi.outData.put("AGNB", svAGNB)
    mi.outData.put("ITM8", svITM8)
    mi.outData.put("SITE", svITM8)
    mi.outData.put("OURT", "1")
    mi.outData.put("OURR", svAGNB)
    mi.outData.put("PUPR", svPUPR as String)
    mi.outData.put("PPUN", svPPUN)
    mi.outData.put("TEDL", svTEDL)
    mi.outData.put("ZIPP", svZIPP)
    mi.write()
  }
  // Retrieve agreement
  public void retrieveAgreement(){
    logger.debug("Début retrieveAgreement - svSUNO={$svSUNO}, svDIVI={$svDIVI}, iWHLO={$iWHLO}, iPIDE={$iPIDE}, iFvdtInt={$iFvdtInt}, oVAGN={$oVAGN}")
    def queryEXT032 = database.table("EXT032").index("30").selection("EXAGNB").build()
    def dbEXT032 = queryEXT032.getContainer()
    dbEXT032.set("EXCONO", currentCompany)
    dbEXT032.set("EXZIPP", oZIPP)
    dbEXT032.set("EXSUNO", svSUNO)
    queryEXT032.readAll(dbEXT032,3,{DBContainer recordEXT032->
      logger.debug("EXT032 trouvé")
      String oAGNB = recordEXT032.get("EXAGNB")
      logger.debug("value oAGNB = " + oAGNB)
      ExpressionFactory expression_MPAGRH = database.getExpressionFactory("MPAGRH")
      expression_MPAGRH = expression_MPAGRH.eq("AHCONO", currentCompany as String)
      if(iGETY > 20 && iPIDE.trim() != "") {
        expression_MPAGRH = expression_MPAGRH.and(expression_MPAGRH.eq("AHVAGN", oVAGN))
      } else {
        expression_MPAGRH = expression_MPAGRH.and(expression_MPAGRH.eq("AHVAGN", "00000").or(expression_MPAGRH.eq("AHVAGN", "")))
      }
      // Selection contrat par dépôt
      expression_MPAGRH = expression_MPAGRH.and(expression_MPAGRH.eq("AHWHLO", iWHLO).or(expression_MPAGRH.eq("AHWHLO", "")))
      def queryMPAGRH = database.table("MPAGRH").index("00").matching(expression_MPAGRH).selection("AHVAGN","AHPAST","AHFVDT","AHUVDT","AHWHLO","AHTEDL").build()
      def dbMPAGRH = queryMPAGRH.getContainer()
      dbMPAGRH.set("AHCONO", currentCompany)
      dbMPAGRH.set("AHSUNO", svSUNO)
      dbMPAGRH.set("AHAGNB", oAGNB)
      queryMPAGRH.readAll(dbMPAGRH,3,{DBContainer recordMPAGRH ->
        String oPAST = recordMPAGRH.get("AHPAST")
        Integer oFVDT = recordMPAGRH.get("AHFVDT")  as Integer
        Integer oUVDT = recordMPAGRH.get("AHUVDT")  as Integer
        if(oUVDT==0)oUVDT=99999999
        String svVAGN =recordMPAGRH.get("AHVAGN")
        String oWHLO =recordMPAGRH.get("AHWHLO")
        oTEDL = recordMPAGRH.get("AHTEDL")
        logger.debug("MPAGRH found > oPAST= {$oPAST}, oFVDT= {$oFVDT}, oUVDT= {$oUVDT}, svVAGN= {$svVAGN}, oWHLO= {$oWHLO}, saveWHLO= {$saveWHLO}, oTEDL= {$oTEDL}")
        if((oWHLO!="" || oWHLO==saveWHLO) && iFvdtInt>= oFVDT && iFvdtInt <=oUVDT && oPAST=="40"){
          saveWHLO = oWHLO
          ExpressionFactory expressionMPAGRL = database.getExpressionFactory("MPAGRL")
          expressionMPAGRL = expressionMPAGRL.eq("AIOBV1", iITNO)
          logger.debug("MPAGRH  ok")
          def queryMPAGRL = database.table("MPAGRL").index("00").matching(expressionMPAGRL).selection("AISUNO", "AIAGNB", "AIGRPI", "AIAGPT","AIFVDT","AIUVDT","AIOBV1","AIOBV2","AIOBV3","AIOBV4","AIPPUN","AISEQN").build()
          def dbMPAGRL = queryMPAGRL.getContainer()
          dbMPAGRL.set("AICONO", currentCompany)
          dbMPAGRL.set("AISUNO", svSUNO)
          dbMPAGRL.set("AIAGNB", oAGNB)
          dbMPAGRL.set("AIGRPI", "30" as Integer)
          dbMPAGRL.set("AIOBV1", iITNO)
          queryMPAGRL.readAll(dbMPAGRL,5,{DBContainer recordMPAGRL ->
            svPPUN = recordMPAGRL.get("AIPPUN")
            if(recordMPAGRL.get("AIPPUN") == "" || recordMPAGRL.get("AIPPUN") == null) {
              def queryMITMAS = database.table("MITMAS").index("00").selection("MMPPUN", "MMUNMS").build()
              def dbMITMAS = queryMITMAS.getContainer()
              dbMITMAS.set("MMCONO", currentCompany)
              dbMITMAS.set("MMITNO", iITNO)
              if(queryMITMAS.read(dbMITMAS)){
                svPPUN = dbMITMAS.get("MMPPUN")
                if ( dbMITMAS.get("MMPPUN") == null || dbMITMAS.get("MMPPUN") =="") {
                  svPPUN = dbMITMAS.get("MMUNMS")
                }
              }
            }
            logger.debug("value AIPPUN= "+ recordMPAGRL.get("AIPPUN"))
            Integer oFVDT_MPAGRL = recordMPAGRL.get("AIFVDT") as Integer
            Integer oUVDT_MPAGRL = recordMPAGRL.get("AIUVDT") as Integer
            Integer oAGTP = recordMPAGRL.get("AIAGPT") as Integer
            Integer oSEQN_MPAGRL = recordMPAGRL.get("AISEQN")
            if(oUVDT_MPAGRL==0)oUVDT=99999999
            logger.debug("MPAGRL found > SVAGPT= {$SVAGPT}, oAGTP= {$oAGTP}, oAAGNB= {$oAGNB}, oFVDT_MPAGRL= {$oFVDT_MPAGRL}, oUVDT_MPAGRL={$oUVDT_MPAGRL}")
            if((SVAGPT==0 || SVAGPT >= oAGTP || SVAGPT == oAGTP && (saveFVDT==0 || saveFVDT>oFVDT_MPAGRL)) && iFvdtInt>= oFVDT_MPAGRL && iFvdtInt <=oUVDT_MPAGRL){
              logger.debug("value oAGNB step 2 = " + oAGNB)
              if(SVPRIO=="" || oPRIO==SVPRIO){
                SVAGPT = oAGTP
                svAGNB = oAGNB
                svZIPP = oZIPP
                svSEQN = oSEQN_MPAGRL
                SVPRIO = oPRIO
                saveFVDT = oFVDT_MPAGRL
                svTEDL = oTEDL
                svPUPR = 0
                DBAction queryMPAGRP = database.table("MPAGRP").index("00").selection("AJPUPR").build()
                DBContainer dbMPAGRP = queryMPAGRP.getContainer()
                dbMPAGRP.set("AJCONO", currentCompany)
                dbMPAGRP.set("AJSUNO", dbMPAGRL.get("AISUNO"))
                dbMPAGRP.set("AJAGNB", dbMPAGRL.get("AIAGNB"))
                dbMPAGRP.set("AJGRPI", dbMPAGRL.get("AIGRPI"))
                dbMPAGRP.set("AJOBV1", dbMPAGRL.get("AIOBV1"))
                dbMPAGRP.set("AJOBV2", dbMPAGRL.get("AIOBV2"))
                dbMPAGRP.set("AJOBV3", dbMPAGRL.get("AIOBV3"))
                dbMPAGRP.set("AJOBV4", dbMPAGRL.get("AIOBV4"))
                dbMPAGRP.set("AJFVDT", oFVDT)
                logger.debug("MPAGRP date debut "+oFVDT)
                queryMPAGRP.readAll(dbMPAGRP,9,{DBContainer recordMPAGRP ->
                  svPUPR = recordMPAGRP.get("AJPUPR") as double
                  logger.debug("MPAGRP found > svPUPR={$svPUPR} ")
                })
              }
            }
          })
        }
      })
    })
  }
  // Execute PPS200MI.UpdLine
  private executePPS200MIUpdLine(String PUNO, String PNLI, String PNLS, String OURT, String OURR, String SITE, String PUPR, String PPUN, String TEDL){
    def parameters = ["PUNO": PUNO, "PNLI": PNLI, "PNLS": PNLS, "OURT": OURT, "OURR": OURR, "SITE": SITE, "PUPR": PUPR, "PPUN":PPUN, "TEDL": TEDL]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed PPS200MI.UpdLine: "+ response.errorMessage)
        logger.debug("message = " + response.errorMessage)
      } else {
        logger.debug("PPS200MI/UpdLine is OK")
      }
    }
    logger.debug("Call PPS200MI/UpdLine =PUNO:"+PUNO+",PNLI:"+PNLI+",PNLS:"+PNLS+",OURT:"+OURT+",OURR:"+OURR+",SITE:"+SITE+",PUPR:"+PUPR+",PPUN:"+PPUN+",TEDL:"+TEDL)
    miCaller.call("PPS200MI", "UpdLine", parameters, handler)
  }
}
