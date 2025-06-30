/**
 * README
 *
 * Name : OIS021_PDUPD
 * Description : Delete xTend Table 
 * Date         Changed By   Description
 * 20210825     DUVALC       TARX16 Programme de calcul de tarif
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class OIS021_PDUPD extends ExtendM3Trigger {
  private final InteractiveAPI interactive
  private final ProgramAPI program
  private final LoggerAPI logger
  private final MIAPI mi
  private final MICallerAPI miCaller
  private final DatabaseAPI database;
  private Integer currentCompany
  private String iCUNO

  public OIS021_PDUPD(InteractiveAPI interactive, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller, DatabaseAPI database) {
    this.program = program
    this.interactive = interactive
    this.logger = logger
    this.miCaller = miCaller
    this.database = database
  }

  public void main() {
    currentCompany = (Integer)program.getLDAZD().CONO
    String CONO = currentCompany;
    String PRRF =interactive.display.fields.ODPRRF
    String CUCD =interactive.display.fields.ODCUCD
    String CUNO =interactive.display.fields.ODCUNO
    String FDAT =interactive.display.fields.WWFVDT
    String ITNO =interactive.display.fields.ODITNO
    String OBV1 =interactive.display.fields.ODOBV1
    String OBV2 =interactive.display.fields.ODOBV2
    String OBV3 =interactive.display.fields.ODOBV3

    String oFDAT=null
    String DTFM = program.getLDAZD().DTFM
    logger.debug("value DTFM= {$DTFM}")
    LocalDateTime timeOfCreation = LocalDateTime.now()
    String Annee = timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer
    String YY = Annee.substring(0,2)
    logger.debug("value YY= {$YY}")
    if(DTFM=="YMD"){
      oFDAT=YY+FDAT
    }
    logger.debug("value FDAT= {$oFDAT}")
    if(DTFM=="MDY"){
      oFDAT=YY+FDAT.substring(4,6)+FDAT.substring(0,4)
    }
    logger.debug("value FDAT= {$oFDAT}")
    if(DTFM=="DMY"){
      oFDAT=YY+FDAT.substring(4,6)+FDAT.substring(2,4)+FDAT.substring(0,2)
    }
    logger.debug("value FDAT= {$oFDAT}")

    if(oFDAT==null){
      mi.error("Format date non reconnu")
      return
    }
    DBAction TarifCompQuery = database.table("EXT075").index("00").build();
    DBContainer EXT075 = TarifCompQuery.getContainer();
    EXT075.set("EXCONO", currentCompany);
    EXT075.set("EXPRRF", PRRF);
    EXT075.set("EXCUCD", CUCD);
    EXT075.set("EXCUNO", CUNO);
    EXT075.set("EXFVDT", oFDAT as Integer);
    EXT075.set("EXITNO", ITNO);
    EXT075.set("EXOBV1", OBV1);
    EXT075.set("EXOBV2", OBV2);
    EXT075.set("EXOBV3", OBV3);
    if(!TarifCompQuery.readAllLock(EXT075, 9, Delete_EXT075_OOPRICH)){
      iCUNO=CUNO
      if(CUNO==null || CUNO==""){
        DBAction queryEXT080 = database.table("EXT080").index("20").selection("EXCUNO").build()
        DBContainer EXT080 = queryEXT080.getContainer()
        EXT080.set("EXCONO", currentCompany)
        EXT080.set("EXPRRF", PRRF)
        EXT080.set("EXCUCD", CUCD)
        EXT080.set("EXFVDT", oFDAT as Integer)
        if(!queryEXT080.readAll(EXT080, 4, outDataEXT080)){

        }
      }
      EXT075.set("EXCONO", currentCompany);
      EXT075.set("EXPRRF", PRRF);
      EXT075.set("EXCUCD", CUCD);
      EXT075.set("EXCUNO", iCUNO);
      EXT075.set("EXFVDT", oFDAT as Integer);
      EXT075.set("EXITNO", ITNO);
      EXT075.set("EXOBV1", OBV1);
      EXT075.set("EXOBV2", OBV2);
      EXT075.set("EXOBV3", OBV3);
      if(!TarifCompQuery.readAllLock(EXT075, 9, Delete_EXT075_OOPRICH)){

      }
    }
  }
  Closure<?> outDataEXT080 = { DBContainer EXT080 ->
    iCUNO = EXT080.get("EXCUNO")
  }
  Closure<?> Delete_EXT075_OOPRICH = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
