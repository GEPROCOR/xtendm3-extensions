/**
 * README
 *
 * Name : OIS072_PDUPD
 * Description : Delete xTend Table 
 * Date         Changed By   Description
 * 20210825     DUVALC       TARX16 Programme de calcul de tarif
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
public class OIS072_PDUPD extends ExtendM3Trigger {
  private final InteractiveAPI interactive
  private final ProgramAPI program
  private final LoggerAPI logger
  private final MIAPI mi
  private final MICallerAPI miCaller
  private final DatabaseAPI database;
  private Integer currentCompany


  public OIS072_PDUPD(InteractiveAPI interactive, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller, DatabaseAPI database) {
    this.program = program
    this.interactive = interactive
    this.logger = logger
    this.miCaller = miCaller
    this.database = database
  }

  public void main() {

    currentCompany = (Integer)program.getLDAZD().CONO
    String CONO = currentCompany;
    String ASCD = interactive.display.fields.OIASCD
    logger.debug("value ASCD= {$ASCD}")
    String ITNO =interactive.display.fields.OIITNO
    logger.debug("value ITNO= {$ITNO}")
    String FDAT =interactive.display.fields.WWFDAT
    logger.debug("value FDAT= {$FDAT}")
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
    DBAction EXT052_query = database.table("EXT052").index("10").build()
    DBContainer EXT052 = EXT052_query.getContainer();
    EXT052.set("EXCONO", currentCompany);
    EXT052.set("EXASCD", ASCD);
    EXT052.set("EXITNO", ITNO);
    EXT052.setInt("EXFDAT", oFDAT as Integer);
    if(!EXT052_query.readAllLock(EXT052, 4, Delete_EXT052)){
      logger.debug("non trouvé")
    }
  }
  Closure<?> Delete_EXT052 = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
