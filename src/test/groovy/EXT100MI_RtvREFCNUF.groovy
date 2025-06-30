/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT100MI.RtvREFCNUF
 * Description : The RtvREFCNUF transaction retrieve item number in MMS025.
 * Date         Changed By   Description
 * 20211217     RENARN       REAX02 Recherche ITM8-EAN13
 * 20220406     RENARN       Search with replaced item added
 */

public class RtvREFCNUF extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private final UtilityAPI utility
  private final MICallerAPI miCaller
  private Integer currentCompany
  private String oITM8 = ""
  private String ridn = ""
  private Integer ridl = 0
  private Integer ridx = 0
  private Integer zrep = 0
  private String repi = ""
  private String rorn = ""
  private String uca1 = ""
  private String itno = ""
  private Integer intc = 0

  public RtvREFCNUF(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program,UtilityAPI utility) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
  }
  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    zrep = mi.in.get("ZREP")
    oITM8 = ""
    // Purchase order
    if(mi.in.get("PUNO") != null && (mi.in.get("ORNO") == null)){
      DBAction query_MPLINE = database.table("MPLINE").index("00").selection("IBOURR", "IBSUNO", "IBSEQN", "IBRORN", "IBRORL", "IBRORX", "IBUCA1", "IBITNO").build()
      DBContainer MPLINE = query_MPLINE.getContainer()
      MPLINE.set("IBCONO", currentCompany)
      MPLINE.set("IBPUNO", mi.in.get("PUNO"))
      MPLINE.set("IBPNLI", mi.in.get("PNLI"))
      MPLINE.set("IBPNLS", mi.in.get("PNLS"))
      if (query_MPLINE.read(MPLINE)) {
        logger.debug("MPLINE trouvé")
        rorn = MPLINE.get("IBRORN")
        uca1 = MPLINE.get("IBUCA1")
        itno = MPLINE.get("IBITNO")
        if(zrep == 1){
          if (uca1.trim() != "") {
            intc = 0
            DBAction query_MITALT = database.table("MITALT").index("20").selection("MAINTC").build()
            DBContainer MITALT = query_MITALT.getContainer()
            MITALT.set("MACONO", currentCompany)
            MITALT.set("MAITNO", itno)
            MITALT.set("MAALIT", uca1)
            if (query_MITALT.readAll(MITALT, 3, outData_MITALT)) {
            }
            if(intc != 5) {
              ExpressionFactory expression = database.getExpressionFactory("MPAGRL")
              expression = expression.eq("AIOBV1", uca1)
              DBAction query_MPAGRL = database.table("MPAGRL").index("30").matching(expression).selection("AISUNO", "AIAGNB", "AIGRPI", "AIOBV1", "AIOBV2", "AIOBV3", "AIOBV4", "AIFVDT").build()
              DBContainer MPAGRL = query_MPAGRL.getContainer()
              MPAGRL.set("AICONO", currentCompany)
              MPAGRL.set("AISUNO", MPLINE.get("IBSUNO"))
              MPAGRL.set("AIAGNB", MPLINE.get("IBOURR"))
              if (query_MPAGRL.readAll(MPAGRL, 3, outData_MPAGRL)) {
              }
            }
          }
          if (oITM8.trim() == "" && rorn.trim() != "") {
            DBAction query = database.table("OOLINE").index("00").selection("OBORNO", "OBPONR", "OBPOSX", "OBRORC", "OBRORN", "OBRORL", "OBWHLO", "OBITNO", "OBUCA1", "OBREPI").build()
            DBContainer OOLINE = query.getContainer()
            OOLINE.set("OBCONO", currentCompany)
            OOLINE.set("OBORNO", MPLINE.get("IBRORN"))
            OOLINE.set("OBPONR", MPLINE.get("IBRORL"))
            OOLINE.set("OBPOSX", MPLINE.get("IBRORX"))
            if (query.read(OOLINE)) {
              repi = OOLINE.get("OBREPI")
              if(repi.trim() != "") {
                intc = 0
                DBAction query_MITALT = database.table("MITALT").index("20").selection("MAINTC").build()
                DBContainer MITALT = query_MITALT.getContainer()
                MITALT.set("MACONO", currentCompany)
                MITALT.set("MAITNO", itno)
                MITALT.set("MAALIT", repi)
                if (query_MITALT.readAll(MITALT, 3, outData_MITALT)) {
                }
                if(intc != 5) {
                  ExpressionFactory expression = database.getExpressionFactory("MPAGRL")
                  expression = expression.eq("AIOBV1", repi)
                  DBAction query_MPAGRL = database.table("MPAGRL").index("30").matching(expression).selection("AISUNO", "AIAGNB", "AIGRPI", "AIOBV1", "AIOBV2", "AIOBV3", "AIOBV4", "AIFVDT").build()
                  DBContainer MPAGRL = query_MPAGRL.getContainer()
                  MPAGRL.set("AICONO", currentCompany)
                  MPAGRL.set("AISUNO", MPLINE.get("IBSUNO"))
                  MPAGRL.set("AIAGNB", MPLINE.get("IBOURR"))
                  if (query_MPAGRL.readAll(MPAGRL, 3, outData_MPAGRL)) {
                  }
                }
              }
            }
          }
        }
        if(oITM8.trim() == "") {
          DBAction query_MPAGRL = database.table("MPAGRL").index("30").selection("AISUNO", "AIAGNB", "AIGRPI", "AIOBV1", "AIOBV2", "AIOBV3", "AIOBV4", "AIFVDT").build()
          DBContainer MPAGRL = query_MPAGRL.getContainer()
          MPAGRL.set("AICONO", currentCompany)
          MPAGRL.set("AISUNO", MPLINE.get("IBSUNO"))
          MPAGRL.set("AIAGNB", MPLINE.get("IBOURR"))
          MPAGRL.set("AISEQN", MPLINE.get("IBSEQN"))
          if (query_MPAGRL.readAll(MPAGRL, 4, outData_MPAGRL)) {
          }
        }
      }
    }

    // Customer order number
    if(mi.in.get("PUNO") == null && (mi.in.get("ORNO") != null)){
      DBAction query = database.table("OOLINE").index("00").selection("OBORNO", "OBPONR", "OBPOSX", "OBRORC", "OBRORN", "OBRORL", "OBWHLO", "OBITNO", "OBUCA1", "OBREPI").build()
      DBContainer OOLINE = query.getContainer()
      OOLINE.set("OBCONO", currentCompany)
      OOLINE.set("OBORNO",  mi.in.get("ORNO"))
      OOLINE.set("OBPONR",  mi.in.get("PONR"))
      OOLINE.set("OBPOSX",  mi.in.get("POSX"))
      if(query.read(OOLINE)){
        logger.debug("OOLINE trouvé")
        itno = OOLINE.get("OBITNO")
        repi = OOLINE.get("OBREPI")
        if(zrep == 1 && repi.trim() != "") {
          intc = 0
          DBAction query_MITALT = database.table("MITALT").index("20").selection("MAINTC").build()
          DBContainer MITALT = query_MITALT.getContainer()
          MITALT.set("MACONO", currentCompany)
          MITALT.set("MAITNO", itno)
          MITALT.set("MAALIT", repi)
          if (query_MITALT.readAll(MITALT, 3, outData_MITALT)) {
          }
          if(intc != 5) {
            // Planned purchase order
            if (OOLINE.get("OBRORC") == 2 && OOLINE.get("OBRORL") == 0) {
              DBAction query_MPOPLP = database.table("MPOPLP").index("91").selection("POOURR", "POSUNO", "POSEQN").build()
              DBContainer MPOPLP = query_MPOPLP.getContainer()
              MPOPLP.set("POCONO", currentCompany)
              MPOPLP.set("PORORC", 3)
              MPOPLP.set("PORORN", OOLINE.get("OBORNO"))
              MPOPLP.set("PORORL", OOLINE.get("OBPONR"))
              MPOPLP.set("PORORX", OOLINE.get("OBPOSX"))
              if (query_MPOPLP.readAll(MPOPLP, 5, outData_MPOPLP2)) {
              }
            }
            // Purchase order
            if (OOLINE.get("OBRORC") == 2 && OOLINE.get("OBRORL") != 0) {
              DBAction query_MPLINE = database.table("MPLINE").index("20").selection("IBOURR", "IBSUNO", "IBSEQN").build()
              DBContainer MPLINE = query_MPLINE.getContainer()
              MPLINE.set("IBCONO", currentCompany)
              MPLINE.set("IBRORC", 3)
              MPLINE.set("IBRORN", OOLINE.get("OBORNO"))
              MPLINE.set("IBRORL", OOLINE.get("OBPONR"))
              MPLINE.set("IBRORX", OOLINE.get("OBPOSX"))
              if (query_MPLINE.readAll(MPLINE, 5, outData_MPLINE2)) {
              }
            }
          }
        }
        if(oITM8.trim() == "") {
          // Planned purchase order
          if (OOLINE.get("OBRORC") == 2 && OOLINE.get("OBRORL") == 0) {
            DBAction query_MPOPLP = database.table("MPOPLP").index("91").selection("POOURR", "POSUNO", "POSEQN").build()
            DBContainer MPOPLP = query_MPOPLP.getContainer()
            MPOPLP.set("POCONO", currentCompany)
            MPOPLP.set("PORORC", 3)
            MPOPLP.set("PORORN", OOLINE.get("OBORNO"))
            MPOPLP.set("PORORL", OOLINE.get("OBPONR"))
            MPOPLP.set("PORORX", OOLINE.get("OBPOSX"))
            if (query_MPOPLP.readAll(MPOPLP, 5, outData_MPOPLP)) {
            }
          }
          // Purchase order
          if (OOLINE.get("OBRORC") == 2 && OOLINE.get("OBRORL") != 0) {
            DBAction query_MPLINE = database.table("MPLINE").index("20").selection("IBOURR", "IBSUNO", "IBSEQN").build()
            DBContainer MPLINE = query_MPLINE.getContainer()
            MPLINE.set("IBCONO", currentCompany)
            MPLINE.set("IBRORC", 3)
            MPLINE.set("IBRORN", OOLINE.get("OBORNO"))
            MPLINE.set("IBRORL", OOLINE.get("OBPONR"))
            MPLINE.set("IBRORX", OOLINE.get("OBPOSX"))
            if (query_MPLINE.readAll(MPLINE, 5, outData_MPLINE)) {
            }
          }
          // Not a planned purchase order nor purchase order
          if (OOLINE.get("OBRORC") != 2) {
            oITM8 = OOLINE.get("OBUCA1")
          }
        }
      }
    }
    mi.outData.put("ZRCN", oITM8)
    mi.write()
  }
  // Retrieve MPAGRL
  Closure<?> outData_MPAGRL = { DBContainer MPAGRL ->
    logger.debug("MPAGRL trouvé")
    DBAction CUGEX1_query = database.table("CUGEX1").index("00").selection("F1A030").build()
    DBContainer CUGEX1 = CUGEX1_query.getContainer()
    CUGEX1.set("F1CONO", currentCompany)
    CUGEX1.set("F1FILE", "MPAGRL")
    CUGEX1.set("F1PK01", MPAGRL.get("AISUNO"))
    CUGEX1.set("F1PK02", MPAGRL.get("AIAGNB"))
    CUGEX1.set("F1PK03", MPAGRL.get("AIGRPI") as String)
    CUGEX1.set("F1PK04", MPAGRL.get("AIOBV1"))
    CUGEX1.set("F1PK05", MPAGRL.get("AIOBV2"))
    CUGEX1.set("F1PK06", MPAGRL.get("AIOBV3"))
    CUGEX1.set("F1PK07", MPAGRL.get("AIOBV4"))
    CUGEX1.set("F1PK08", MPAGRL.get("AIFVDT") as String)
    if (CUGEX1_query.read(CUGEX1)) {
      logger.debug("CUGEX1 trouvé")
      oITM8 = CUGEX1.get("F1A030")
    }
  }
  // Retrieve MPOPLP
  Closure<?> outData_MPOPLP = { DBContainer MPOPLP ->
    logger.debug("MPOPLP trouvé")
    DBAction query_MPAGRL = database.table("MPAGRL").index("30").selection("AISUNO", "AIAGNB", "AIGRPI", "AIOBV1", "AIOBV2", "AIOBV3", "AIOBV4", "AIFVDT").build()
    DBContainer MPAGRL = query_MPAGRL.getContainer()
    MPAGRL.set("AICONO", currentCompany)
    MPAGRL.set("AISUNO", MPOPLP.get("POSUNO"))
    MPAGRL.set("AIAGNB", MPOPLP.get("POOURR"))
    MPAGRL.set("AISEQN", MPOPLP.get("POSEQN"))
    if(query_MPAGRL.readAll(MPAGRL, 4, outData_MPAGRL)){
    }
  }
  // Retrieve MPLINE
  Closure<?> outData_MPLINE = { DBContainer MPLINE ->
    logger.debug("MPLINE trouvé")
    DBAction query_MPAGRL = database.table("MPAGRL").index("30").selection("AISUNO", "AIAGNB", "AIGRPI", "AIOBV1", "AIOBV2", "AIOBV3", "AIOBV4", "AIFVDT").build()
    DBContainer MPAGRL = query_MPAGRL.getContainer()
    MPAGRL.set("AICONO", currentCompany)
    MPAGRL.set("AISUNO", MPLINE.get("IBSUNO"))
    MPAGRL.set("AIAGNB", MPLINE.get("IBOURR"))
    MPAGRL.set("AISEQN", MPLINE.get("IBSEQN"))
    if(query_MPAGRL.readAll(MPAGRL, 4, outData_MPAGRL)){
    }
  }
  // Retrieve MPOPLP
  Closure<?> outData_MPOPLP2 = { DBContainer MPOPLP ->
    logger.debug("MPOPLP trouvé")
    ExpressionFactory expression = database.getExpressionFactory("MPAGRL")
    expression = expression.eq("AIOBV1", repi)
    DBAction query_MPAGRL = database.table("MPAGRL").index("30").matching(expression).selection("AISUNO", "AIAGNB", "AIGRPI", "AIOBV1", "AIOBV2", "AIOBV3", "AIOBV4", "AIFVDT").build()
    DBContainer MPAGRL = query_MPAGRL.getContainer()
    MPAGRL.set("AICONO", currentCompany)
    MPAGRL.set("AISUNO", MPOPLP.get("POSUNO"))
    MPAGRL.set("AIAGNB", MPOPLP.get("POOURR"))
    if(query_MPAGRL.readAll(MPAGRL, 3, outData_MPAGRL)){
    }
  }
  // Retrieve MPLINE
  Closure<?> outData_MPLINE2 = { DBContainer MPLINE ->
    logger.debug("MPLINE trouvé")
    ExpressionFactory expression = database.getExpressionFactory("MPAGRL")
    expression = expression.eq("AIOBV1", repi)
    DBAction query_MPAGRL = database.table("MPAGRL").index("30").matching(expression).selection("AISUNO", "AIAGNB", "AIGRPI", "AIOBV1", "AIOBV2", "AIOBV3", "AIOBV4", "AIFVDT").build()
    DBContainer MPAGRL = query_MPAGRL.getContainer()
    MPAGRL.set("AICONO", currentCompany)
    MPAGRL.set("AISUNO", MPLINE.get("IBSUNO"))
    MPAGRL.set("AIAGNB", MPLINE.get("IBOURR"))
    if(query_MPAGRL.readAll(MPAGRL, 3, outData_MPAGRL)){
    }
  }
  // Retrieve MITALT
  Closure<?> outData_MITALT = { DBContainer MITALT ->
    intc = MITALT.get("MAINTC")
  }
}
