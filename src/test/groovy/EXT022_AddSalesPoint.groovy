/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT022MI.AddSalesPoint
 * Description : Add records to the EXT022 table.
 * Date         Changed By   Description
 * 20230313     RENARN       CMDX06 - Gestion ajout des points de vente
 * 20230613     YVOYOU       CMDX06 - ZCFE is not mandatory
 */

import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.time.LocalDateTime

public class AddSalesPoint extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private final UtilityAPI utility
  private Integer currentCompany

  public AddSalesPoint(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi;
    this.database = database
    this.logger = logger
    this.program = program
    this.utility = utility
    this.miCaller = miCaller
  }

  public void main() {
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
      OCUSAD.set("OPCUNO",  getInParam("CUNO"))
      OCUSAD.set("OPADRT",  1)
      OCUSAD.set("OPADID",  getInParam("ADID"))
      if (!Query.read(OCUSAD)) {
        mi.error("Code adresse " + getInParam("ADID") + " n'est pas valide pour le client " + getInParam("CUNO") + "!")
        return
      }
    }
    // Check constraint
    if(!getInParam("ZCFE").isEmpty()){
      DBAction Query = database.table("EXT800").index("00").selection("EXP001","EXP002", "EXP003", "EXP004", "EXP005", "EXP006", "EXP007", "EXP008", "EXP009", "EXP010", "EXP011", "EXP012", "EXP013", "EXP014", "EXP015", "EXP016", "EXP017", "EXP018", "EXP019", "EXP020").build()
      DBContainer EXT800 = Query.getContainer()
      EXT800.set("EXCONO", currentCompany)
      EXT800.set("EXEXNM", "EXT022MI_LstUCHGroupe")
      if (Query.read(EXT800)) {
        if (getInParam("ZCFE").equals(EXT800.get("EXP001")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP002")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP003")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP004")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP005")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP006")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP007")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP008")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP009")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP010")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP011")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP012")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP013")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP014")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP015")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP016")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP017")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP018")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP019")) &&
          getInParam("ZCFE").equals(EXT800.get("EXP020"))) {
          mi.error("Contrainte " + getInParam("ZCFE") + " n'est pas valide!")
          return
        }
      } else {
        mi.error("Liste contraint n'est pas créé dans EXT800!")
        return
      }
    }
    // Add value to EXT022
    DBAction action = database.table("EXT022").index("00").selection("EXCHNO").build()
    DBContainer EXT022 = action.createContainer()
    EXT022.set("EXCONO", currentCompany)
    EXT022.set("EXCUNO", getInParam("CUNO"))
    EXT022.set("EXADID", getInParam("ADID"))
    EXT022.set("EXZCFE", getInParam("ZCFE"))
    if (!action.readLock(EXT022, updateCallBack)) {
      EXT022.set("EXCONO", currentCompany)
      EXT022.set("EXCUNO", getInParam("CUNO"))
      EXT022.set("EXADID", getInParam("ADID"))
      EXT022.set("EXZCFE", getInParam("ZCFE"))
      EXT022.set("EXDLSP", getInParam("DLSP"))

      LocalDateTime timeOfCreation = LocalDateTime.now()
      EXT022.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT022.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer)
      EXT022.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      EXT022.setInt("EXCHNO", 1)
      EXT022.set("EXCHID", program.getUser())
      action.insert(EXT022)
    }
  }

  // Update value to EXT022
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    int changeNumber = lockedResult.get("EXCHNO")
    lockedResult.set("EXDLSP", getInParam("DLSP"))
    lockedResult.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
    lockedResult.setInt("EXCHNO", changeNumber + 1)
    lockedResult.set("EXCHID", program.getUser())
    lockedResult.update()
  }


  /**
   * GetInParam - Get input parameter
   **/
  public String getInParam(String name) {
    logger.debug(String.format("Get input parameter %s : value %s", name, mi.in.get(name)))
    if (mi.in.get(name)==null) {
      return ""
    } else {
      return ((String)mi.in.get(name)).trim()
    }
  }
}
