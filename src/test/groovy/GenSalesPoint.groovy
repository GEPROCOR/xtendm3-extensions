/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT022MI.GenSalesPoint
 * Description : Generate records to the EXT022 table.
 * Date         Changed By   Description
 * 20230313     RENARN       CMDX06 - Gestion génération des points de vente
 * 20230613     YVOYOU       CMDX06 - CUNO is not mandatory
 */

import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.time.LocalDateTime

public class GenSalesPoint extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private final UtilityAPI utility

  public GenSalesPoint(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi;
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
    this.miCaller = miCaller
  }

  public void main() {
    Integer currentCompany
    currentCompany = (Integer)program.getLDAZD().CONO
    // Check customer
    if(!getInParam("CUNO").isEmpty()){
      DBAction Query = database.table("OCUSMA").index("00").build()
      DBContainer OCUSMA = Query.getContainer()
      OCUSMA.set("OKCONO", currentCompany)
      OCUSMA.set("OKCUNO",  getInParam("CUNO"))
      if (!Query.read(OCUSMA)) {
        mi.error("Code client " + getInParam("CUNO") + " n'existe pas!")
        return
      }
    } else {
      mi.error("Code client est obligatoire!")
      return
    }
    // Check customer Address
    if(!getInParam("ADID").isEmpty()){
      DBAction Query = database.table("OCUSAD").index("00").build()
      DBContainer OCUSAD = Query.getContainer()
      OCUSAD.set("OPCONO", currentCompany)
      OCUSAD.set("OPCUNO", getInParam("CUNO"))
      OCUSAD.set("OPADRT", 1)
      OCUSAD.set("OPADID", getInParam("ADID"))
      if (!Query.read(OCUSAD)) {
        mi.error("Code adresse " + getInParam("ADID") + " n'est pas valide pour le client " + getInParam("CUNO") + "!")
        return
      }
    }
    DBAction Query = database.table("EXT800").index("00").selection("EXP001","EXP002", "EXP003", "EXP004", "EXP005", "EXP006", "EXP007", "EXP008", "EXP009", "EXP010", "EXP011", "EXP012", "EXP013", "EXP014", "EXP015", "EXP016", "EXP017", "EXP018", "EXP019", "EXP020").build()
    DBContainer EXT800 = Query.getContainer()
    EXT800.set("EXCONO", currentCompany)
    EXT800.set("EXEXNM", "EXT022MI_LstUCHGroupe")
    if (!Query.read(EXT800)) {
      mi.error("Liste contraint n'est pas créé dans EXT800!")
      return
    } else {
      String fieldEXP
      String zcfe
      for (int i = 1; i <= 20; i++) {
        fieldEXP =  "EXP" + String.format("%03d", i)
        zcfe = EXT800.get(fieldEXP)
        logger.debug(String.format("Read EXT800 %s: Get field %s >> %s", String.valueOf(i), fieldEXP, zcfe.trim()))
        if (zcfe != null &&  !zcfe.trim().isEmpty()) {
          // Add value to EXT022
          DBAction action = database.table("EXT022").index("00").build()
          DBContainer EXT022 = action.createContainer()
          EXT022.set("EXCONO", currentCompany)
          EXT022.set("EXCUNO", getInParam("CUNO"))
          EXT022.set("EXADID", getInParam("ADID"))
          EXT022.set("EXZCFE", zcfe.trim())
          if (!action.read(EXT022)) {
            logger.debug(String.format("Add EXT022 CONO:%s, CUNO:%s, ADID:%s, ZCFE:%s, DLSP: ", currentCompany, getInParam("CUNO"), getInParam("ADID"), zcfe.trim()))
            EXT022.set("EXCONO", currentCompany)
            EXT022.set("EXCUNO", getInParam("CUNO"))
            EXT022.set("EXADID", getInParam("ADID"))
            EXT022.set("EXZCFE", zcfe.trim())
            EXT022.set("EXDLSP", "")

            LocalDateTime timeOfCreation = LocalDateTime.now()
            EXT022.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            EXT022.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
            EXT022.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
            EXT022.setInt("EXCHNO", 1)
            EXT022.set("EXCHID", program.getUser())
            action.insert(EXT022)
          } else {
            logger.debug(String.format("Record already exists EXT022 CONO:%s, CUNO:%s, ADID:%s, ZCFE:%s", currentCompany, getInParam("CUNO"), getInParam("ADID"), zcfe.trim()))
          }
        }
      }
    }
  }

  /**
   * GetInParam - Get input parameter
   **/
  public String getInParam(String name) {
    logger.debug(String.format("Get input parameter %s : value %s", name, (String)mi.in.get(name)))
    if (mi.in.get(name)==null) {
      logger.debug(String.format("return empty cause parameter %s is null", name))
      return ""
    } else {
      logger.debug(String.format("return value %s", ((String)mi.in.get(name)).trim()))
      return ((String)mi.in.get(name)).trim()
    }
  }
}
