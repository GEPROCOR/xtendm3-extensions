/**
 * README
 *
 * Name : OIS100_PKUPD
 * Description : Call constraint engine by order
 * Date         Changed By   Description
 * 20220125     RENARN       QUAX07 - Constraints engine
 */
public class OIS100_PKUPD extends ExtendM3Trigger {

  private final InteractiveAPI interactive
  private final ProgramAPI program
  private final LoggerAPI logger
  private final MIAPI mi
  private final MICallerAPI miCaller
  private final DatabaseAPI database
  private Integer currentCompany

  public OIS100_PKUPD(InteractiveAPI interactive, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller, DatabaseAPI database) {
    this.program = program
    this.interactive = interactive
    this.logger = logger
    this.miCaller = miCaller
    this.database = database
  }

  public void main() {
    currentCompany = (Integer)program.getLDAZD().CONO
    String CONO = currentCompany
    String ORNO = interactive.display.fields.OAORNO
    logger.debug('ORNO = ' + ORNO)
    executeEXT820MISubmitBatch(currentCompany as String, "EXT016", ORNO)
  }
  // Execute EXT820MI.SubmitBatch
  private executeEXT820MISubmitBatch(String CONO, String JOID, String P001){
    def parameters = ["CONO": CONO, "JOID": JOID, "P001": P001]
    Closure<?> handler = { Map<String, String> response ->
      if (response.error != null) {
        return mi.error("Failed EXT820MI.SubmitBatch: "+ response.errorMessage)
      } else {
      }
    }
    miCaller.call("EXT820MI", "SubmitBatch", parameters, handler)
  }
}
