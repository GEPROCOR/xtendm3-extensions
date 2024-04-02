public class CRS235_PCUPD extends ExtendM3Trigger {

  private final InteractiveAPI interactive
  private final ProgramAPI program
  private final LoggerAPI logger
  private final MIAPI mi
  private final MICallerAPI miCaller
  private final DatabaseAPI database;
  //private final String[] runOnlyForUsers = ["DUVCYR"]  // Leave the array empty if it should be run for everyone, otherwise add authorized usernames
  private Integer currentCompany

  public CRS235_PCUPD(InteractiveAPI interactive, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller, DatabaseAPI database) {
    this.program = program
    this.interactive = interactive
    this.logger = logger
    this.miCaller = miCaller
    this.database = database
  }

  public void main() {
    //if (!shouldRun()) return
    //  logger.debug("TypeAdresse= {$interactive.display.fields.CPADTH}")
    if(interactive.display.fields.CPADTH!=4) return
    String ADK1 =interactive.display.fields.CPADK1
    //logger.debug("Adresse= {$ADK1}")

    if(interactive.display.fields.CPADK1.toString().trim().length()>4){
      ADK1=interactive.display.fields.CPADK1.toString().trim().substring(0,4)
    }

    //logger.debug("Adresse= {$ADK1}")
    currentCompany = (Integer)program.getLDAZD().CONO
    Iterator<String> it = AllConditionLivraison(currentCompany).iterator();

    while (it.hasNext()) {
      String iCONO = currentCompany;
      String iTEDL = it.next()

      //logger.debug("TEDL= {$iTEDL}")

      String iZPLA = ADK1;
      String iZIPL = iTEDL.trim()+iZPLA.trim();
      //logger.debug("ZIPL= {$iZIPL}")

      def paramEXT031MI_Add = ["CONO": iCONO, "TEDL": iTEDL, "ZPLA": iZPLA, "ZIPL": iZIPL];
      def paramEXT031MI_Upd = ["CONO": iCONO, "TEDL": iTEDL, "ZPLA": iZPLA, "ZIPL": iZIPL];

      def paramEXT030MI = ["CONO": iCONO, "ZIPS": iZIPL,"ZIPP": iZIPL, "ZCOM": "1"];
      Closure<?> closure = {Map<String, String> response ->
        if(response.error != null){
          interactive.showCustomInfo(response.errorMessage)
        }
      }

      DBAction RechercheEXT031 = database.table("EXT031").index("00").build()
      DBContainer EXT031 = RechercheEXT031.getContainer()
      EXT031.set("EXCONO", currentCompany)
      EXT031.set("EXTEDL", iTEDL)
      EXT031.set("EXZPLA", iZPLA)
      if (!RechercheEXT031.read(EXT031)) {
        //logger.debug("EXT031MI Add")
        //interactive.showCustomError("WEADTH","Création")
        miCaller.call("EXT031MI", "AddIncotermPlac", paramEXT031MI_Add, closure);
      }else{
        //logger.debug("EXT031MI Upd")
        //interactive.showCustomError("WEADTH","Modif")
        miCaller.call("EXT031MI", "UpdIncotermPlac", paramEXT031MI_Upd, closure);
      }

      DBAction RechercheEXT030 = database.table("EXT030").index("00").build()
      DBContainer EXT030 = RechercheEXT030.getContainer()
      EXT030.set("EXCONO", currentCompany)
      EXT030.set("EXZIPS", iZIPL)
      EXT030.set("EXZIPP", iZIPL)
      if (!RechercheEXT030.read(EXT030)) {
        //logger.debug("EXT030MI Add")
        //interactive.showCustomError("WEADTH","Création")
        miCaller.call("EXT030MI", "AddIncPlSalPur", paramEXT030MI, closure);
      }else{
        //logger.debug("EXT030MI Upd")
        //interactive.showCustomError("WEADTH","Modif")
        miCaller.call("EXT030MI", "UpdIncPlSalPur", paramEXT030MI, closure);
      }
    }
  }
  private Set<String> AllConditionLivraison (int compagny){
    Set<String> TEDL = new HashSet()
    def ConditionLivraison = database.table("CSYTAB").index("10").selection("CTSTKY").build()
    def CSYTAB = ConditionLivraison.createContainer()
    CSYTAB.set("CTCONO", currentCompany)
    CSYTAB.set("CTDIVI",  "")
    CSYTAB.set("CTSTCO",  "TEDL")
    CSYTAB.set("CTLNCD",  "")
    ConditionLivraison.readAll(CSYTAB,4,{DBContainer record ->
      String CondLivr = record.getString("CTSTKY");
      TEDL.add(CondLivr)})
    return TEDL
  }
  /**
   * Check if script should run or not
   * @return true if script should run
   */
  /*
 boolean shouldRun() {
     if (runOnlyForUsers.length != 0) {
         String currentUser = program.LDAZD.get("RESP").toString().trim()
         boolean authorizedToRun = runOnlyForUsers.contains(currentUser)
         logger.debug("User {$currentUser} authorization check result was ${authorizedToRun}")
         return authorizedToRun
     }
     return true
 }*/
}
