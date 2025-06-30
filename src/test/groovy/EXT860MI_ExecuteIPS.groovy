/**
 * README
 * This extension is used by IEC
 *
 * Name : EXT860MI.ExecuteIPS
 * Description : Execute IPS.
 * Date         Changed By   Description
 * 20240611     ARNREN       Execute IPS
 * 20240618     ARNREN       def has been replaced
 * 20240624     ARNREN       final declaration removed
 * 20240702     ARNREN       lowerCamelCase fixed
 */
import groovy.util.slurpersupport.GPathResult
public class ExecuteIPS extends ExtendM3Transaction {

  //<editor-fold desc="ION Config">
  private String ionIpsEndpoint = "/M3/ips/service/"
  private Map<String, String> ionIpsHeaders = ["Accept": "application/json, text/plain, */*", "content-type": "application/xml"]
  //</editor-fold>

  //<editor-fold desc="APIs">
  private MIAPI mi
  private DatabaseAPI database
  private LoggerAPI logger
  private MICallerAPI miCaller
  private IonAPI ion
  private ProgramAPI program
  //</editor-fold>

  String errorMessage

  public ExecuteIPS(
    MIAPI mi,
    DatabaseAPI database,
    LoggerAPI logger,
    MICallerAPI miCaller,
    IonAPI ion,
    ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.miCaller = miCaller
    this.ion = ion
    this.program = program
  }

  public void main() {
    int inCONO = mi.in.get("CONO")
    String inDIVI = mi.in.get("DIVI")
    String inZSRV = mi.in.get("ZSRV")
    String inZMTD = mi.in.get("ZMTD")
    String inPGNM = mi.in.get("PGNM")
    String inVL01 = mi.in.get("VL01")
    String inVL02 = mi.in.get("VL02")
    String inVL03 = mi.in.get("VL03")
    String inVL04 = mi.in.get("VL04")
    String inVL05 = mi.in.get("VL05")
    String inVL06 = mi.in.get("VL06")
    String inVL07 = mi.in.get("VL07")
    String inVL08 = mi.in.get("VL08")
    String inVL09 = mi.in.get("VL09")
    String inVL10 = mi.in.get("VL10")

    if(!executeIps(inCONO, inDIVI, inZSRV, inZMTD, inPGNM, [FL01: inVL01, FL02: inVL02, FL03: inVL03, FL04: inVL04, FL05: inVL05, FL06: inVL06, FL07: inVL07, FL08: inVL08, FL09: inVL09, FL10: inVL10])) {
      mi.error(errorMessage)
    }

  }


  // Execute IPS
  private boolean executeIps(Integer company, String division, String service, String method, String program, Map<String, String> programInput = null, String childProgram = null, Map<String, String> childPrograminput= null) {
    logger.debug("IPS [$service] [$method] [$program] [$company] [$division] [$programInput] [$childProgram] [$childPrograminput]")
    String body = buildSOAPBody(company, division, service, method, program, programInput, childProgram, childPrograminput)
    IonResponse response = ion.post(ionIpsEndpoint + service, ionIpsHeaders, null, body)

    logger.debug("body = " + body)

    if (response.getError()) {
      logger.debug("Response.getError() is true")
      errorMessage = "Failed calling IPS Call, detailed error message: ${response.getErrorMessage()}"
      return false
    }

    String content = response.getContent()
    if (content == null) {
      logger.debug("content is null")
      errorMessage = "Expected content from the request but got no content"
      return false
    }

    GPathResult xml = new XmlSlurper().parseText(content)
    logger.debug("xml = " + xml)


    GPathResult faultNode = (GPathResult)xml["Body"]["Fault"]["faultstring"]
    if(!faultNode.isEmpty()) {
      // Check Error
      logger.debug("Fault node is not empty: "+ faultNode.text())
      errorMessage = faultNode.text()
      return false
    } else {
      // Get output fields
      logger.debug("xml-Get response = " + xml["Body"]["GetResponse"]["$program"])
      GPathResult responseWS = (GPathResult)xml["Body"]["GetResponse"]["$program"]
      Integer compteur = 0
      responseWS.children().each { child ->
        compteur++
        for (int i = 1; i <= 10; i++) {
          if(compteur == i) {
            mi.outData.put("OF" + String.format("%02d", i), ((GPathResult)child).name())
            mi.outData.put("OV" + String.format("%02d", i), ((GPathResult)child).text())
          }
        }

      }
      // write output
      mi.write()
      return true
    }
  }
  // build SOAP Body
  private String buildSOAPBody(Integer company, String division, String service, String method, String program,
                               Map<String, String> programInput = null, String childProgram = null, Map<String, String> childProgramInput = null) {

    /* Defined the namespace used in the SOAP body*/
    String namespace = "http://schemas.infor.com/ips/$service/$method"
    String prefix = method.substring(0, 3).toLowerCase()

    /* For the used programs, build the input nodes */
    String childInputs = buildSOAPNodeForInput(prefix, childProgram, childProgramInput)
    String programinputs = buildSOAPNodeForInput(prefix, program, programInput)

    /* For the used programs, build the body */
    String childProgramBody = buildSOAPBodyForProgram(prefix, childProgram, childInputs)
    String programBody = buildSOAPBodyForProgram(prefix, program, programinputs, childProgramBody)

    /* Build the SOAP request */
    """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:cred="http://lawson.com/ws/credentials" xmlns:$prefix="$namespace">
                       <soapenv:Header>
                           <cred:lws>
                               <cred:company>$company</cred:company>
                               <cred:division>$division</cred:division>
                           </cred:lws>
                       </soapenv:Header>
                       <soapenv:Body>
                           <$prefix:$method>
                               $programBody
                           </$prefix:$method>
                       </soapenv:Body>
                </soapenv:Envelope>"""
  }
  // build SOAP Node For Input
  private String buildSOAPNodeForInput(String prefix, String program, Map<String, String> inputs) {
    return inputs == null ? "" : inputs
      .entrySet()
      .collect{it.key}
    //.collect{"<$prefix:$it>${inputs.get(it)}</$prefix:$it>\n"}
      .collect{"<$prefix:"+mi.in.get(it)+">${inputs.get(it)}</$prefix:"+mi.in.get(it)+">\n"}
      .join("\n")
  }
  // build SOAP Body For Program
  private String buildSOAPBodyForProgram(String prefix, String program, String inputs, String childProgramBody = "") {
    return program == null ? "" :
      """<$prefix:$program>
                    $inputs
                    $childProgramBody
                </$prefix:$program>"""
  }
  //</editor-fold>

}
