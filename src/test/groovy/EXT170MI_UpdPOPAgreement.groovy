/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT170MI.UpdPOPAgreement
 * Description : Update planned purchase order agreement
 * Date         Changed By   Description
 * 20211208     RENARN       APPX20 - Purchase agreement retrieving
 * 20220503     RENARN       selection on AGPT and FVDT has changed
 * 20220622     RENARN       selection on MPAGRH has changed
 * 20221208     BERWIL       retrieveAgreement() selection on MPAGRH by WHLO
 * 20230530     BERWIL       Update field TEDL and PROJ in MPOPLP
 * 20230622     YVOYOU       Update field svSUNO by SUNO in MPOPLP
 * 20240618     RENARN       Removal of incoterms from purchasing agreement searches. EXT030, EXT032, EXT033 handling removed.
 * 20250211     YVOYOU       REA32 - SITE change and no read CUGEX1 and RORC blank
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class UpdPOPAgreement extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private int currentCompany
  private String currentDate
  private Integer iPLPN = 0
  private Integer iPLPS = 0
  private Integer iPLP2 = 0
  private String oOURR
  private String iWHLO = ""
  private String iITNO = ""
  private String iFVDT = ""
  private String iPIDE = ""
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
  private Integer svAGPT = 0
  private String oVAGN = ""
  private Integer saveFVDT = 0
  private Integer iFvdtInt
  private Integer svSEQN = 0
  private String saveWHLO = ""
  private String oTEDL = ""
  private String svTEDL = ""
  private boolean delLinkOrder
  private String rorn = ""
  private String ortp = ""

  public UpdPOPAgreement(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility, MICallerAPI miCaller) {
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

    if(mi.in.get("PLPN") != null){
      iPLPN = (Integer)mi.in.get("PLPN")
    }else{
      mi.error("Ordre planifié est obligatoire")
      return
    }

    if(mi.in.get("PLPS") != null)
      iPLPS = (Integer)mi.in.get("PLPS")

    if(mi.in.get("PLP2") != null)
      iPLP2 = (Integer)mi.in.get("PLP2")

    logger.debug("iPLPN = " + iPLPN)
    logger.debug("iPLPS = " + iPLPS)
    logger.debug("iPLP2 = " + iPLP2)

    DBAction query = database.table("MPOPLP").index("00").selection("POWHLO","POITNO","POGETY","PORORN","PORORL","PORORX","POOURR","POSITE", "POPUPR", "POPPUN", "POSUNO").build()
    DBContainer MPOPLP = query.getContainer()
    MPOPLP.set("POCONO", currentCompany)
    MPOPLP.setInt("POPLPN", iPLPN)
    MPOPLP.setInt("POPLPS", iPLPS)
    MPOPLP.setInt("POPLP2", iPLP2)
    if(!query.read(MPOPLP)){
      mi.error("L'enregistrement n'existe pas")
      return
    } else {
      iWHLO = MPOPLP.get("POWHLO")
      iITNO = MPOPLP.get("POITNO")
      iGETY = (MPOPLP.get("POGETY") as String) as Integer
      iRORN = MPOPLP.get("PORORN")
      iRORL = MPOPLP.get("PORORL")
      iRORX = MPOPLP.get("PORORX")
      rorn = MPOPLP.get("PORORN")
      //Search delete linked order
      delLinkOrder = false
      if (rorn != "") {
        DBAction queryOohead = database.table("OOHEAD").index("00").selection("OAORTP").build()
        DBContainer OOHEAD = queryOohead.getContainer()
        OOHEAD.set("OACONO", currentCompany)
        OOHEAD.set("OAORNO", rorn)
        if (queryOohead.read(OOHEAD)) {
          ortp = OOHEAD.get("OAORTP")
          DBAction queryCugex1 = database.table("CUGEX1").index("00").selection("F1CHB5").build()
          DBContainer CUGEX1 = queryCugex1.getContainer()
          CUGEX1.set("F1CONO", currentCompany)
          CUGEX1.set("F1FILE", "OOTYPE")
          CUGEX1.set("F1PK01", ortp)
          if (queryCugex1.read(CUGEX1)) {
            delLinkOrder = CUGEX1.get("F1CHB5")
          }
        }
      }
      svAGPT= 0
      oVAGN="00000"
      saveFVDT=0
      iFvdtInt=iFVDT as Integer

      svSUNO = MPOPLP.get("POSUNO")

      def TableArticleDepot = database.table("MITBAL").index("00").selection("MBSUNO","MBDIVI").build()
      def MITBAL = TableArticleDepot.getContainer()
      MITBAL.set("MBCONO", currentCompany)
      MITBAL.set("MBITNO", iITNO)
      MITBAL.set("MBWHLO", iWHLO)
      TableArticleDepot.readAll(MITBAL,3,{DBContainer recordMITBAL->
        logger.debug("MITBAL trouvé")
        svDIVI = recordMITBAL.get("MBDIVI")
      })
      // Phase 1 item
      if (iGETY == 20 || iGETY == 28) {
        logger.debug("iGETY = " + iGETY)
        retrieveAgreement()
      }
      // Phase 2 item
      if (iGETY == 23 || iGETY == 24) {
        logger.debug("iGETY = " + iGETY)
        logger.debug("iRORN = + " + iRORN)
        logger.debug("iRORL = + " + iRORL)
        logger.debug("iRORX = + " + iRORX)
        DBAction OOLINE_query = database.table("OOLINE").index("00").selection("OBPIDE", "OBPROJ").build()
        DBContainer OOLINE = OOLINE_query.getContainer()
        OOLINE.set("OBCONO", currentCompany)
        OOLINE.set("OBORNO", iRORN)
        OOLINE.set("OBPONR", iRORL)
        OOLINE.set("OBPOSX", iRORX)
        if (OOLINE_query.read(OOLINE)) {
          logger.debug("OOLINE trouvé")
          iPIDE = OOLINE.get("OBPIDE")
          def TablePromo = database.table("OPROMH").index("00").selection("FZTX15").build()
          def OPROMH = TablePromo.getContainer()
          OPROMH.set("FZCONO", currentCompany)
          OPROMH.set("FZDIVI", svDIVI)
          OPROMH.set("FZPIDE", iPIDE)
          TablePromo.readAll(OPROMH, 3, { DBContainer recordOPROMH ->
            logger.debug("OPROMH trouvé")
            oVAGN = recordOPROMH.get("FZTX15")
            logger.debug("value oVAGN A = {$oVAGN}")
            if (oVAGN == "" || oVAGN == null) {
              oVAGN = "00000"
            }
          })
        }
        retrieveAgreement()
      }
    }
    logger.debug("value svAGNB = " + svAGNB)
    // A Supprimer (Début) une fois PPS170MI corrigé
    MPOPLP.set("POCONO", currentCompany)
    MPOPLP.setInt("POPLPN", iPLPN)
    MPOPLP.setInt("POPLPS", iPLPS)
    MPOPLP.setInt("POPLP2", iPLP2)
    if(!query.readLock(MPOPLP, updateCallBack)){
    }
    // A Supprimer (Fin)
    if (delLinkOrder) {
      executePPS170MIUpdPlannedPO(iPLPN+"",iPLPS+"",iPLP2+"", "0")
    }
    // Update planned purchase order
    mi.outData.put("AGNB", svAGNB)
    mi.outData.put("ITM8", svITM8)
    mi.outData.put("TEDL", svTEDL)
    mi.write()
  }
  // Retrieve agreement
  public void retrieveAgreement(){
    logger.debug("Début retrieveAgreement")
    logger.debug("value svSUNO= {$svSUNO}")
    logger.debug("value svDIVI= {$svDIVI}")
    logger.debug("value iWHLO= {$iWHLO}")
    logger.debug("value iPIDE= {$iPIDE}")
    logger.debug("value iFvdtInt= {$iFvdtInt}")
    logger.debug("value oVAGN= {$oVAGN}")
    ExpressionFactory expression_MPAGRH = database.getExpressionFactory("MPAGRH")
    expression_MPAGRH = expression_MPAGRH.eq("AHCONO", currentCompany as String)
    if(iGETY > 20 && iPIDE.trim() != "") {
      expression_MPAGRH = expression_MPAGRH.and(expression_MPAGRH.eq("AHVAGN", oVAGN))
    } else {
      expression_MPAGRH = expression_MPAGRH.and(expression_MPAGRH.eq("AHVAGN", "00000").or(expression_MPAGRH.eq("AHVAGN", "")))
    }
    // Selection contrat par dépôt
    expression_MPAGRH = expression_MPAGRH.and(expression_MPAGRH.eq("AHWHLO", iWHLO).or(expression_MPAGRH.eq("AHWHLO", "")))
    def TableContrat = database.table("MPAGRH").index("00").matching(expression_MPAGRH).selection("AHAGNB","AHVAGN","AHAGTP","AHPAST","AHFVDT","AHUVDT","AHWHLO","AHTEDL").build()
    def MPAGRH = TableContrat.getContainer()
    MPAGRH.set("AHCONO", currentCompany)
    MPAGRH.set("AHSUNO", svSUNO)
    TableContrat.readAll(MPAGRH,2,{DBContainer recordMPAGRH ->
      logger.debug("MPAGRH trouvé")
      String oAGNB = recordMPAGRH.get("AHAGNB")
      logger.debug("value oAGNB= {$oAGNB}")
      String oPAST = recordMPAGRH.get("AHPAST")
      Integer oFVDT = recordMPAGRH.get("AHFVDT")  as Integer
      Integer oUVDT = recordMPAGRH.get("AHUVDT")  as Integer
      if(oUVDT==0)oUVDT=99999999
      logger.debug("value oPAST= {$oPAST}")
      logger.debug("value oFVDT= {$oFVDT}")
      logger.debug("value oUVDT= {$oUVDT}")
      String svVAGN =recordMPAGRH.get("AHVAGN")
      logger.debug("value svVAGN= {$svVAGN}")
      String oWHLO =recordMPAGRH.get("AHWHLO")
      logger.debug("value oWHLO= {$oWHLO}")
      logger.debug("value saveWHLO= {$saveWHLO}")
      oTEDL = recordMPAGRH.get("AHTEDL")
      logger.debug("value svTEDL= {$svTEDL}")
      if((oWHLO!="" || oWHLO==saveWHLO) && iFvdtInt>= oFVDT && iFvdtInt <=oUVDT && oPAST=="40"){
        saveWHLO = oWHLO
        ExpressionFactory expressionMPAGRL = database.getExpressionFactory("MPAGRL")
        expressionMPAGRL = expressionMPAGRL.eq("AIOBV1", iITNO)
        logger.debug("MPAGRH  ok")
        def TableLigneContrat = database.table("MPAGRL").index("00").matching(expressionMPAGRL).selection("AISUNO", "AIAGNB", "AIGRPI", "AIAGPT","AIFVDT","AIUVDT","AIOBV1","AIOBV2","AIOBV3","AIOBV4","AIPPUN","AISEQN").build()
        def MPAGRL = TableLigneContrat.getContainer()
        MPAGRL.set("AICONO", currentCompany)
        MPAGRL.set("AISUNO", svSUNO)
        MPAGRL.set("AIAGNB", oAGNB)
        MPAGRL.set("AIGRPI", "30" as Integer)
        MPAGRL.set("AIOBV1", iITNO)
        TableLigneContrat.readAll(MPAGRL,5,{DBContainer recordMPAGRL ->
          logger.debug("MPAGRL trouvé")
          svPPUN = recordMPAGRL.get("AIPPUN")
          if(recordMPAGRL.get("AIPPUN") == "" || recordMPAGRL.get("AIPPUN") == null) {
            logger.debug("svPPUN vide")
            def TableMITMAS = database.table("MITMAS").index("00").selection("MMPPUN", "MMUNMS").build()
            def MITMAS = TableMITMAS.getContainer()
            MITMAS.set("MMCONO", currentCompany)
            MITMAS.set("MMITNO", iITNO)
            if(TableMITMAS.read(MITMAS)){
              logger.debug("MITMAS trouvé")
              svPPUN = MITMAS.get("MMPPUN")
              if ( MITMAS.get("MMPPUN") == null || MITMAS.get("MMPPUN") =="") {
                svPPUN = MITMAS.get("MMUNMS")
              }
            }
          }
          logger.debug("value AIPPUN= "+ recordMPAGRL.get("AIPPUN"))
          Integer oFVDT_MPAGRL = recordMPAGRL.get("AIFVDT") as Integer
          Integer oUVDT_MPAGRL = recordMPAGRL.get("AIUVDT") as Integer
          Integer oAGTP = recordMPAGRL.get("AIAGPT") as Integer
          Integer oSEQN_MPAGRL = recordMPAGRL.get("AISEQN")
          if(oUVDT_MPAGRL==0)oUVDT=99999999
          logger.debug("MPAGRL ok")
          logger.debug("value svAGPT= {$svAGPT}")
          logger.debug("value oAGTP= {$oAGTP}")
          logger.debug("value oAGNB step 1 = " + oAGNB)
          logger.debug("value svAGPT step 1 = " + svAGPT)
          logger.debug("value oAGTP step 1 = " + oAGTP)
          logger.debug("value iFvdtInt step 1 = " + iFvdtInt)
          logger.debug("value oFVDT_MPAGRL step 1 = " + oFVDT_MPAGRL)
          logger.debug("value iFvdtInt step 1 = " + iFvdtInt)
          logger.debug("value oUVDT_MPAGRL step 1 = " + oUVDT_MPAGRL)
          if((svAGPT==0 || svAGPT >= oAGTP || svAGPT == oAGTP && (saveFVDT==0 || saveFVDT>oFVDT_MPAGRL)) && iFvdtInt>= oFVDT_MPAGRL && iFvdtInt <=oUVDT_MPAGRL){
            logger.debug("value oAGNB step 2 = " + oAGNB)
            svAGPT = oAGTP
            svAGNB = oAGNB
            svSEQN = oSEQN_MPAGRL
            saveFVDT = oFVDT_MPAGRL
            svTEDL = oTEDL
            svITM8 = ""
            svPUPR = 0
            logger.debug("F1PK01 = " + recordMPAGRL.get("AISUNO"))
            logger.debug("F1PK02 = " + recordMPAGRL.get("AIAGNB"))
            logger.debug("F1PK03 = " + recordMPAGRL.get("AIGRPI"))
            logger.debug("F1PK04 = " + recordMPAGRL.get("AIOBV1"))
            logger.debug("F1PK05 = " + recordMPAGRL.get("AIOBV2"))
            logger.debug("F1PK06 = " + recordMPAGRL.get("AIOBV3"))
            logger.debug("F1PK07 = " + recordMPAGRL.get("AIOBV4"))
            logger.debug("F1PK08 = " + recordMPAGRL.get("AIFVDT") as String)
            DBAction MPAGRP_query = database.table("MPAGRP").index("00").selection("AJPUPR").build()
            DBContainer MPAGRP = MPAGRP_query.getContainer()
            MPAGRP.set("AJCONO", currentCompany)
            MPAGRP.set("AJSUNO", MPAGRL.get("AISUNO"))
            MPAGRP.set("AJAGNB", MPAGRL.get("AIAGNB"))
            MPAGRP.set("AJGRPI", MPAGRL.get("AIGRPI"))
            MPAGRP.set("AJOBV1", MPAGRL.get("AIOBV1"))
            MPAGRP.set("AJOBV2", MPAGRL.get("AIOBV2"))
            MPAGRP.set("AJOBV3", MPAGRL.get("AIOBV3"))
            MPAGRP.set("AJOBV4", MPAGRL.get("AIOBV4"))
            //MPAGRP.set("AJFVDT", MPAGRL.get("AIFVDT"))
            MPAGRP.set("AJFVDT", oFVDT_MPAGRL)
            //MPAGRP.set("AJFVDT", oFVDT)
            //logger.debug("MPAGRP date debut "+oFVDT)
            logger.debug("MPAGRL date debut "+oFVDT_MPAGRL)
            MPAGRP_query.readAll(MPAGRP,9,{DBContainer recordMPAGRP ->
              logger.debug("MPAGRP trouvé")
              logger.debug("MPAGRP AJPUPR : "+recordMPAGRP.get("AJPUPR"))
              svPUPR = recordMPAGRP.get("AJPUPR") as double
              logger.debug("MPAGRP PUPR : "+svPUPR)
            })
          }
        })
      }
    })
  }
  // Execute PPS170MI.UpdPlannedPO
  private executePPS170MIUpdPlannedPO(String PLPN, String PLPS, String PLP2, String RORC){
    def parameters = ["PLPN": PLPN, "PLPS": PLPS, "PLP2": PLP2, "RORC": RORC]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed PPS170MI.UpdPlannedPO: "+ response.errorMessage)
      } else {
      }
    }
    miCaller.call("PPS170MI", "UpdPlannedPO", parameters, handler)
  }
  // Update
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("POCHNO")
    logger.debug("MPAGRP contrat : "+svAGNB)
    logger.debug("MPAGRP unite : "+svPPUN)
    logger.debug("MPAGRP PUPR : "+svPUPR)
    logger.debug("svSEQN = " + svSEQN)
    lockedResult.set("POOURR", svAGNB)
    //lockedResult.set("POSITE", svITM8) //REA32 No modify
    lockedResult.set("POPUPR", svPUPR)
    lockedResult.set("POPPUN", svPPUN)
    lockedResult.set("POSEQN", svSEQN)
    lockedResult.set("POTEDL", svTEDL)
    lockedResult.setInt("POLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("POCHNO", changeNumber + 1)
    lockedResult.set("POCHID", program.getUser())
    lockedResult.update()
  }
}
