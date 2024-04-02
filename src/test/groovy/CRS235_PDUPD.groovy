public class CRS235_PDUPD extends ExtendM3Trigger {

  private final InteractiveAPI interactive
  private final ProgramAPI program
  private final LoggerAPI logger
  private final MIAPI mi
  private final MICallerAPI miCaller
  private final DatabaseAPI database;
  //private final String[] runOnlyForUsers = ["DUVCYR"]  // Leave the array empty if it should be run for everyone, otherwise add authorized usernames
  private Integer currentCompany


  public CRS235_PDUPD(InteractiveAPI interactive, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller, DatabaseAPI database) {
    this.program = program
    this.interactive = interactive
    this.logger = logger
    this.miCaller = miCaller
    this.database = database
  }

  public void main() {
    // if (!shouldRun()) return
    // logger.debug("TypeAdresse= {$interactive.display.fields.WDADTH}")
    if(interactive.display.fields.WDADTH!=4) return
    currentCompany = (Integer)program.getLDAZD().CONO
    String ADK1 =interactive.display.fields.WDADK1

    Iterator<String> it = AllConditionLivraison(currentCompany).iterator();

    while (it.hasNext()) {
      String iCONO = currentCompany;
      String iTEDL = it.next()
      String iZPLA = ADK1;
      String iZIPL = iTEDL.trim()+iZPLA.trim();
      def paramEXT031MI_Del = ["CONO": iCONO, "TEDL": iTEDL, "ZPLA": iZPLA];
      def paramEXT030MI_Del = ["CONO": iCONO, "ZIPS": iZIPL,"ZIPP": iZIPL];

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
      if (RechercheEXT031.read(EXT031)) {
        //logger.debug("EXT031MI Del")
        //interactive.showCustomError("WEADTH","Modif")
        miCaller.call("EXT031MI", "DelIncotermPlac", paramEXT031MI_Del, closure);
      }
      DBAction RechercheEXT030 = database.table("EXT030").index("00").build()
      DBContainer EXT030 = RechercheEXT030.getContainer()
      EXT030.set("EXCONO", currentCompany)
      EXT030.set("EXZIPS", iZIPL)
      EXT030.set("EXZIPP", iZIPL)
      if (RechercheEXT030.read(EXT030)) {
        //logger.debug("EXT030MI Del")
        //interactive.showCustomError("WEADTH","Modif")
        miCaller.call("EXT030MI", "DelIncPlSalPur", paramEXT030MI_Del, closure);
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
   *//*
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
