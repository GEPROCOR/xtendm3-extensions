/**
 * README
 * This extension is used by Interface Script or CMS045
 *
 * Name : EXT120MI.CallCRS418
 * Description : Cost price calculation
 * Date         Changed By   Description
 * 20220221     APACE       CDGX18 - Calculdu prix de revient
 */
public class CallCRS418 extends ExtendM3Transaction {
  private final MIAPI mi
  private final IonAPI ion
  private final LoggerAPI logger
  private final ProgramAPI program

  private Integer currentCompany
  private String ORNO
  private String DIVI

  public CallCRS418(IonAPI ion,MIAPI mi,LoggerAPI logger,ProgramAPI program) {
    this.mi = mi
    this.ion = ion
    this.logger = logger
    this.program = program
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    if (mi.in.get("ORNO") != null) {
      ORNO = mi.in.get("ORNO")
    }

    if (mi.in.get("DIVI") != null) {
      DIVI = mi.in.get("DIVI")
    }

    logger.debug("Run ips CRS418_OI01 =>")
    def endpoint = "/M3/ips/service/CRS418_OI01"
    def headers = ["Accept": "application/xml", "Content-Type": "application/xml"]
    def queryParameters = (Map)null
    def formParameters = (Map)null

    def body = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "+
      "xmlns:cred=\"http://lawson.com/ws/credentials\" "+
      "xmlns:crs=\"http://schemas.infor.com/ips/CRS418_OI01/CRS418_OI01\">"+
      "<soapenv:Header>"+
      "<cred:lws>"+
      "<cred:company>"+currentCompany+"</cred:company><cred:division>"+DIVI+"</cred:division>"+
      "</cred:lws>"+
      "</soapenv:Header>"+
      "<soapenv:Body>"+
      "<crs:CRS418_OI01>"+
      "<crs:CRS418>"+
      "<crs:WWORNO>"+ORNO+"</crs:WWORNO>"+
      "<crs:OIS110/>"+
      "</crs:CRS418>"+
      "</crs:CRS418_OI01>"+
      "</soapenv:Body>"+
      "</soapenv:Envelope>"

    IonResponse response = ion.post(endpoint, headers, queryParameters, body)
    logger.debug(response.getContent())
    if (response.getError()) {
      logger.debug("Failed calling ION API, detailed error message: ${response.getErrorMessage()}")
    }
    if (response.getStatusCode() != 200) {
      logger.debug("Expected status 200 but got ${response.getStatusCode()} instead ${response.getContent()} ")
    }
  }
}
