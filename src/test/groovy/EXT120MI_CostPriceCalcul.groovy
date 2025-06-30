/**
 * README
 * This extension is used by Interface Script or CMS045
 *
 * Name : EXT120MI.CostPriceCalcul
 * Description : Cost price calculation
 * Date         Changed By   Description
 * 20210906     APACE       CDGX18 - Calculdu prix de revient
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class CostPriceCalcul extends ExtendM3Transaction {

  private final MIAPI mi
  private final IonAPI ion
  private final LoggerAPI logger
  private final ProgramAPI program
  private final DatabaseAPI database
  private final SessionAPI session
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private final UtilityAPI utility

  private Integer currentCompany
  private String ORNO
  private Integer PONR
  private Integer POSX
  private Integer CHB1
  private String DIVI
  private String CUNO
  private String CUCD
  private Integer ORDT
  private String ORTP
  private Integer retenu=0

  private DBContainer MPLINESave
  private DBContainer MPAGRLSave
  private DBContainer MPAGRPSave
  private DBContainer MPCOVESave
  private DBContainer OSBSTDSave
  private DBContainer MILOMASave
  private DBContainer OPRBASSave
  private DBContainer EXT075Save
  private DBContainer MPAGRHSave
  private DBContainer OPROMHSave
  private DBContainer OPRICHSave
  private DBContainer CUGEX1Save
  private DBContainer MPOPLPSave
  private String AL30
  private String AL31
  private String AL32

  private String AGNB
  private String SUNO

  private Double PUPR


  private Double DCOS
  private Double UCOS

  private boolean findOSBSTD=false
  private boolean multiLine=true
  private boolean findCUGEX1=false
  private boolean findCRS418 = false

  public CostPriceCalcul(IonAPI ion,MIAPI mi, DatabaseAPI database, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility, LoggerAPI logger) {
    this.mi = mi
    this.database = database
    this.program = program
    this.miCaller = miCaller
    this.logger = logger
    this.utility = utility
    this.ion = ion
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }

    ORNO = ""
    PONR = 0
    POSX = 0
    DIVI = ""
    CUNO = ""
    CUCD = ""
    ORDT = 0
    ORTP = ""
    AGNB = ""
    SUNO = ""

    if(mi.in.get("ORNO") != null){
      DBAction RechercheOOHEAD = database.table("OOHEAD").index("00").selection("OAORNO","OADIVI","OACUNO","OACUCD","OAORDT","OAORTP").build()
      DBContainer OOHEAD = RechercheOOHEAD.getContainer()
      OOHEAD.set("OACONO", currentCompany)
      OOHEAD.set("OAORNO", mi.in.get("ORNO"))
      if(RechercheOOHEAD.read(OOHEAD)){
        ORNO = OOHEAD.get("OAORNO").toString()
        DIVI = OOHEAD.get("OADIVI").toString()
        CUNO = OOHEAD.get("OACUNO").toString()
        CUCD = OOHEAD.get("OACUCD").toString()
        ORDT = OOHEAD.get("OAORDT").toString() as Integer
        ORTP = OOHEAD.get("OAORTP").toString()
      }else{
        mi.error("La commande n'existe pas")
        return
      }
    }else{
      mi.error("Le N° de commande est obligatoire")
      return
    }

    if(mi.in.get("PONR") != null && mi.in.get("POSX") != null){
      DBAction RechercheOOLINE = database.table("OOLINE").index("00").selection("OBORNO").build()
      DBContainer OOLINE = RechercheOOLINE.getContainer()
      OOLINE.set("OBCONO", currentCompany)
      OOLINE.set("OBORNO", mi.in.get("ORNO"))
      OOLINE.set("OBPONR", mi.in.get("PONR"))
      OOLINE.set("OBPOSX", mi.in.get("POSX"))
      if(RechercheOOLINE.read(OOLINE)){
        PONR = OOLINE.get("OBPONR").toString() as Integer
        POSX = OOLINE.get("OBPOSX").toString() as Integer
        multiLine = false
      }else{
        mi.error("La ligne de commande n'existe pas")
        return
      }
    }



    CHB1 = 0
    def params2 = ["FILE":"OOTYPE", "PK01":ORTP]
    Closure<?> closure2 = {Map<String, String> response ->
      logger.debug("Response = ${response}")
      if(response.error == null){
        CHB1 = response.CHB1 as Integer
      }
    }
    miCaller.call("CUSEXTMI", "GetFieldValueEx", params2, closure2)
    readAllLinesOrOneLine(multiLine,findOSBSTD,findCUGEX1)
  }

  //Read OOLINE Multiple or Single
  private readAllLinesOrOneLine(boolean multiLine,boolean findOSBSTD, boolean findCUGEX1){
    logger.debug("MultiLine:"+multiLine)
    logger.debug("OSBSTD:"+findOSBSTD)
    logger.debug("CUGEX1:"+findCUGEX1)
    if(multiLine){
      DBAction RechercheOOLINE = database.table("OOLINE").index("00").selection("OBORNO","OBPONR","OBPOSX","OBPRRF","OBITNO","OBPIDE","OBRORN","OBRORL","OBRORX","OBSUNO","OBPROJ","OBFACI","OBCUNO","OBDIVI","OBCUCD","OBUCOS","OBORQT","OBRORC","OBORST","OBWHLO").build()
      DBContainer OOLINE = RechercheOOLINE.getContainer()
      OOLINE.set("OBCONO", currentCompany)
      OOLINE.set("OBORNO", ORNO)
      //Read OOLINE
      RechercheOOLINE.readAll(OOLINE,2,readOOLINE)
      if(findCRS418){
        callCRS418(ORNO)
      }
    }else{
      DBAction RechercheOOLINE = database.table("OOLINE").index("00").selection("OBORNO","OBPONR","OBPOSX","OBPRRF","OBITNO","OBPIDE","OBRORN","OBRORL","OBRORX","OBSUNO","OBPROJ","OBFACI","OBCUNO","OBDIVI","OBCUCD","OBUCOS","OBORQT","OBRORC","OBORST","OBWHLO").build()
      DBContainer OOLINE = RechercheOOLINE.getContainer()
      OOLINE.set("OBCONO", currentCompany)
      OOLINE.set("OBORNO", ORNO)
      OOLINE.set("OBPONR", PONR)
      OOLINE.set("OBPOSX", POSX)
      //Read OOLINE
      RechercheOOLINE.readAll(OOLINE,4,readOOLINE)
      if(findCRS418){
        callCRS418(ORNO)
      }
    }

  }

  //Loop OOLINE
  Closure<?> readOOLINE = { DBContainer OOLINE ->
    findOSBSTD = false
    ExpressionFactory expression = database.getExpressionFactory("OSBSTD")
    expression = expression.eq("UCPONR", OOLINE.get("OBPONR").toString()).and(expression.eq("UCPOSX", OOLINE.get("OBPOSX").toString()))
    DBAction RechercheOSBSTD = database.table("OSBSTD").index("00").matching(expression).selection("UCDCOS","UCUCOS","UCDLIX").build()
    DBContainer OSBSTD = RechercheOSBSTD.getContainer()
    OSBSTD.set("UCCONO", currentCompany)
    OSBSTD.setString("UCDIVI", DIVI)
    OSBSTD.setString("UCORNO", ORNO)
    RechercheOSBSTD.readAll(OSBSTD,3,{ DBContainer OSBSTDresult ->
      findOSBSTD = OSBSTDresult
    })
    if(findOSBSTD){
      findCRS418 = false
      logger.debug("Methode 1")
      methode1(OOLINE)
    }else{
      findCRS418 = true
      if(OOLINE.get("OBORST").toString().equals("05")){
        if(CHB1 == 0){
          logger.debug("Methode 2")
          methode2(OOLINE)
        }else{
          logger.debug("Methode 3")
          methode3(OOLINE)
        }
      }else{
        if(OOLINE.get("OBRORC").toString().equals("2")){
          if(OOLINE.get("OBRORL").toString().equals("0")){
            methode51(OOLINE)
          }else{
            methode52(OOLINE)
          }
        }else{
          methode4(OOLINE)
        }
      }
    }
  }
  //Call ws => type:IPS/name:CRS418_OI01
  private callCRS418(String ORNO){
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
    if (response.getError()) {
      logger.debug("Failed calling ION API, detailed error message: ${response.getErrorMessage()}")
    }
    if (response.getStatusCode() != 200) {
      logger.debug("Expected status 200 but got ${response.getStatusCode()} instead ${response.getContent()} ")
    }

  }

  //Methode 1
  private methode1(DBContainer OOLINE){
    PUPR = 0
    DCOS = 0
    UCOS = 0
    if(findOSBSTDLine(OOLINE)){
      if(OOLINE.get("OBRORN").toString().isBlank()){
        def BANO = ""
        DBAction RechercheMITTRA = database.table("MITTRA").index("30").selection("MTBANO").build()
        DBContainer MITTRA = RechercheMITTRA.getContainer()
        MITTRA.set("MTCONO", currentCompany)
        MITTRA.set("MTRIDN", OOLINE.get("OBORNO").toString())
        MITTRA.set("MTRIDL", OOLINE.get("OBPONR").toString() as Integer)
        MITTRA.set("MTRIDX", OOLINE.get("OBPOSX").toString() as Integer)
        MITTRA.set("MTRIDI", OSBSTDSave.get("UCDLIX").toString() as Integer)
        MITTRA.set("MTITNO", OOLINE.get("OBITNO").toString())
        MITTRA.set("MTTTYP", 31)
        RechercheMITTRA.readAll(MITTRA,7,{ DBContainer MITTRAresult ->
          BANO = MITTRAresult.get("MTBANO").toString()
          return
        })
        if(!BANO.isBlank()){
          DBAction RechercheMILOMA = database.table("MILOMA").index("00").selection("LMRORN","LMRORL","LMRORX").build()
          DBContainer MILOMA = RechercheMILOMA.getContainer()
          MILOMA.set("LMCONO", currentCompany)
          MILOMA.set("LMITNO", OOLINE.get("OBITNO").toString())
          MILOMA.set("LMBANO", BANO)
          if(RechercheMILOMA.read(MILOMA)){
            MILOMASave = MILOMA
          }
          if(getMPLINEbyMILOMA(MILOMA)){
            if(getMPAGRL(OOLINE,MPLINESave)){
              if(getMPAGRP(OOLINE,MPLINESave,MPAGRLSave)){
                if(getCUGEX1(MPAGRLSave)){
                  if(getMPCOVE(CUGEX1Save.get("F1A330").toString(),ORDT as Integer)){
                    logger.debug("Lignes methode1 avec MITTRA=>")
                    logger.debug("DIVI:"+DIVI)
                    logger.debug("ORNO:"+OOLINE.get("OBORNO").toString())
                    logger.debug("PONR:"+OOLINE.get("OBPONR").toString())
                    logger.debug("POSX:"+OOLINE.get("OBPOSX").toString())
                    logger.debug("AL30:"+CUGEX1Save.get("F1A330").toString())
                    logger.debug("FVDT:"+MPAGRLSave.get("AIFVDT").toString())
                    logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
                    logger.debug("DCOS: "+OSBSTDSave.get("UCDCOS").toString())
                    logger.debug("UCOS: "+OSBSTDSave.get("UCUCOS").toString())
                    logger.debug("OVHE: "+MPCOVESave.get("IJOVHE").toString())

                    def ORQT = OOLINE.get("OBORQT").toString() as Double

                    def DCOS = (OSBSTDSave.get("UCDCOS").toString() as Double)/ORQT
                    def UCOS = (OSBSTDSave.get("UCDCOS").toString() as Double)/ORQT

                    def N096 = CUGEX1Save.get("F1N096").toString() as Double
                    def N196 = CUGEX1Save.get("F1N196").toString() as Double
                    def N296 = CUGEX1Save.get("F1N296").toString() as Double
                    def PUPR = MPAGRPSave.get("AJPUPR").toString() as Double
                    def OVHE = MPCOVESave.get("IJOVHE").toString() as Double

                    def montant = 0
                    /*if(DCOS>0){
                      montant = ((DCOS - N096 - N196)*(OVHE/100))*(-1)
                      logger.debug("(("+DCOS +"-"+N096+"-"+N196+")*("+OVHE+"/100))*(-1)" )
                    }else{
                      montant = ((UCOS - N096 - N196)*(OVHE/100))*(-1)
                      logger.debug("(("+UCOS +"-"+N096+"-"+N196+")*("+OVHE+"/100))*(-1)" )
                    }*/
                    if(N296>0){
                      if(DCOS>0){
                        montant = ((N296*(1-(OVHE/100))) + N096 + N196)-DCOS
                      }else{
                        montant = ((N296*(1-(OVHE/100))) + N096 + N196)-UCOS
                      }
                    }else{
                      if(DCOS>0){
                        montant = ((DCOS*(1-(OVHE/100))) + N096 + N196)-DCOS
                      }else{
                        montant = ((UCOS*(1-(OVHE/100))) + N096 + N196)-UCOS
                      }
                    }
                    //montant = (montant/ORQT)*10000
                    montant = (montant)*10000
                    logger.debug("Montant: "+montant)

                    def calcul = Math.round((montant as Double) * 100) / 100
                    logger.debug("Montant: "+calcul)
                    AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1",calcul as String)
                    updLineEXT120method1(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString())
                  }else{
                    logger.debug("Lignes methode1 =>")
                    logger.debug("Montant: "+"0")
                    AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1","0")
                    updLineEXT120method1(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString())
                  }
                }else{
                  logger.debug("Lignes methode1 =>")
                  logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
                  logger.debug("AGNB: "+MPAGRLSave.get("AIAGNB").toString())
                  //updLineEXT120method1(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"",MPAGRPSave.get("AJPUPR").toString() as Double)
                  //UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRPSave.get("AJPUPR").toString())
                  updLineEXT120method1(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"")
                }
              }
            }
          }
        }
      }else{
        if(getMPLINE(OOLINE)){
          if(getMPAGRL(OOLINE,MPLINESave)){
            if(getMPAGRP(OOLINE,MPLINESave,MPAGRLSave)){
              if(getCUGEX1(MPAGRLSave)){
                if(getMPCOVE(CUGEX1Save.get("F1A330").toString(),ORDT as Integer)){
                  logger.debug("Lignes methode1 avec MPLINE uniquement=>")
                  logger.debug("DIVI:"+DIVI)
                  logger.debug("ORNO:"+OOLINE.get("OBORNO").toString())
                  logger.debug("PONR:"+OOLINE.get("OBPONR").toString())
                  logger.debug("POSX:"+OOLINE.get("OBPOSX").toString())
                  logger.debug("AL30:"+CUGEX1Save.get("F1A330").toString())
                  logger.debug("FVDT:"+MPAGRLSave.get("AIFVDT").toString())
                  logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
                  logger.debug("UCOS: "+OOLINE.get("OBUCOS").toString())
                  logger.debug("OVHE: "+MPCOVESave.get("IJOVHE").toString())
                  //def UCOS = OOLINE.get("OBUCOS").toString() as Double
                  def N096 = CUGEX1Save.get("F1N096").toString() as Double
                  def N196 = CUGEX1Save.get("F1N196").toString() as Double
                  def N296 = CUGEX1Save.get("F1N296").toString() as Double
                  def PUPR = MPAGRPSave.get("AJPUPR").toString() as Double
                  def OVHE = MPCOVESave.get("IJOVHE").toString() as Double
                  def ORQT = OOLINE.get("OBORQT").toString() as Double
                  def montant = 0
                  //montant = (((UCOS - N096 - N196)*(OVHE/100))*(-1))
                  /*if(N296>0){
                    montant = (N296*(OVHE/100))*(-1)
                  }else{
                    montant = (PUPR*(OVHE/100))*(-1)
                  }*/
                  if(N296>0){
                    //montant = (N296*(OVHE/100))*(-1)
                    montant = ((N296*(1-(OVHE/100))) + N096 + N196)-PUPR
                  }else{
                    //montant = (PUPR*(OVHE/100))*(-1)
                    montant = ((PUPR*(1-(OVHE/100))) + N096 + N196)-PUPR
                  }
                  montant = (montant/ORQT)*10000
                  logger.debug("Montant: "+montant)
                  def calcul = Math.round((montant as Double) * 100) / 100
                  logger.debug("Montant: "+calcul)
                  AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1",calcul as String)
                  updLineEXT120method1(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString())
                }else{
                  logger.debug("Lignes methode1 =>")
                  logger.debug("Montant: "+"0")
                  AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1","0")
                  updLineEXT120method1(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString())
                }
              }else{
                logger.debug("Lignes methode1 =>")
                logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
                logger.debug("AGNB: "+MPAGRLSave.get("AIAGNB").toString())
                //updLineEXT120method1(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"",MPAGRPSave.get("AJPUPR").toString() as Double)
                //UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRPSave.get("AJPUPR").toString())
                updLineEXT120method1(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"")
              }
            }
          }
        }
      }
    }
  }
  //Methode 2
  private methode2(DBContainer OOLINE){
    getEXT075(OOLINE,ORDT as String)
  }
  //Methode 3
  private methode3(DBContainer OOLINE){
    if(getOPROMH(OOLINE) && getOPRICH(OOLINE)){
      if(getMPAGRHbyOPRICHbyOPROMH(OPROMHSave,OPRICHSave)){
        if(getMPAGRLbyMPAGRH(OOLINE,MPAGRHSave)){
          if(getMPAGRPByMPAGRH(OOLINE,MPAGRHSave,MPAGRLSave)){
            if(getCUGEX1(MPAGRLSave)){
              if(getMPCOVE(CUGEX1Save.get("F1A330").toString(),ORDT as Integer)){
                logger.debug("Lignes methode3 =>")
                logger.debug("DIVI:"+DIVI)
                logger.debug("ORNO:"+OOLINE.get("OBORNO").toString())
                logger.debug("PONR:"+OOLINE.get("OBPONR").toString())
                logger.debug("POSX:"+OOLINE.get("OBPOSX").toString())
                logger.debug("AL30:"+CUGEX1Save.get("F1A330").toString())
                logger.debug("FVDT:"+MPAGRLSave.get("AIFVDT").toString())
                logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
                logger.debug("OVHE: "+MPCOVESave.get("IJOVHE").toString())
                def PUPR = MPAGRPSave.get("AJPUPR").toString() as Double
                def N096 = CUGEX1Save.get("F1N096").toString() as Double
                def N196 = CUGEX1Save.get("F1N196").toString() as Double
                def N296 = CUGEX1Save.get("F1N296").toString() as Double
                def OVHE = MPCOVESave.get("IJOVHE").toString() as Double
                def montant = 0
                //montant = ((PUPR - N096 - N196)*(OVHE/100))*(-1)
                if(N296>0){
                  //montant = (N296*(OVHE/100))*(-1)
                  montant = ((N296*(1-(OVHE/100))) + N096 + N196)-PUPR
                }else{
                  //montant = (PUPR*(OVHE/100))*(-1)
                  montant = ((PUPR*(1-(OVHE/100))) + N096 + N196)-PUPR
                }
                montant = montant*10000
                logger.debug("Montant: "+montant)
                def calcul = Math.round((montant as Double) * 100) / 100
                logger.debug("Montant: "+calcul)
                AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1",calcul as String)
                updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),PUPR)
                UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),PUPR as String)
                updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),PUPR)
              }else{
                logger.debug("Lignes methode3 =>")
                logger.debug("Montant: "+"0")
                logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
                AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1","0")
                updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),MPAGRPSave.get("AJPUPR").toString()  as Double)
                UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRPSave.get("AJPUPR").toString())
                updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),MPAGRPSave.get("AJPUPR").toString()  as Double)
              }
            }else{
              logger.debug("Lignes methode3 =>")
              logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
              logger.debug("AGNB: "+MPAGRLSave.get("AIAGNB").toString())
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"",MPAGRPSave.get("AJPUPR").toString() as Double)
              UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRPSave.get("AJPUPR").toString())
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"",MPAGRPSave.get("AJPUPR").toString() as Double)
            }
          }
        }
      }else{
        def PUPR = 0
        DBAction RechercheMITMAS = database.table("MITMAS").index("00").selection("MMPUPR","MMECMA").build()
        DBContainer MITMAS = RechercheMITMAS.getContainer()
        MITMAS.set("MMCONO", currentCompany)
        MITMAS.set("MMITNO", OOLINE.get("OBITNO").toString())
        if(RechercheMITMAS.read(MITMAS)){
          if((MITMAS.get("MMECMA").toString() as Integer)==0){
            PUPR = 0
          }else{
            PUPR = (MITMAS.get("MMPUPR").toString() as Double)
          }
          if(PUPR==0){
            DBAction RechercheMITFAC = database.table("MITFAC").index("00").selection("M9APPR").build()
            DBContainer MITFAC = RechercheMITFAC.getContainer()
            MITFAC.set("M9CONO", currentCompany)
            MITFAC.set("M9FACI", OOLINE.get("OBFACI").toString())
            MITFAC.set("M9ITNO", OOLINE.get("OBITNO").toString())
            if(RechercheMITFAC.read(MITFAC)){
              PUPR = (MITFAC.get("M9APPR").toString() as Double)
            }
          }
          //Suite =>
          logger.debug("Lignes methode3 avec MITMAS/MITFAC =>")
          logger.debug("Montant: "+PUPR)
          UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),PUPR as String)
          //updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString())
        }
      }
    }
  }

  //Methode 4
  private methode4(DBContainer OOLINE){
    String FACISave = ""
    DBAction RechercheMITWHL = database.table("MITWHL").index("00").selection("MWFACI").reverse().build()
    DBContainer MITWHL = RechercheMITWHL.getContainer()
    MITWHL.set("MWCONO", currentCompany)
    MITWHL.set("MWWHLO", OOLINE.get("OBWHLO").toString())
    if(RechercheMITWHL.read(MITWHL)){
      FACISave = MITWHL.get("MWFACI").toString()
    }

    ExpressionFactory expression = database.getExpressionFactory("FCAAVP")
    expression = expression.eq("A7OCAT", "2")
    DBAction RechercheFCAAVP = database.table("FCAAVP").index("00").matching(expression).selection("A7APPR","A7RIDN","A7RIDL","A7RIDX").reverse().build()
    DBContainer FCAAVP = RechercheFCAAVP.getContainer()
    FCAAVP.set("A7CONO", currentCompany)
    FCAAVP.set("A7FACI", FACISave)
    FCAAVP.set("A7ITNO", OOLINE.get("OBITNO").toString())
    boolean findFCAAVP = false
    RechercheFCAAVP.readAll(FCAAVP,3,1,{ DBContainer FCAAVPResult ->
      findFCAAVP = true
      Double APPR = FCAAVPResult.get("A7APPR").toString() as Double
      //if(getOPROMH(OOLINE) && getOPRICH(OOLINE)){
      //if(getMPAGRHbyOPRICHbyOPROMH(OPROMHSave,OPRICHSave)){
      if(getMPLINEByFCAAVP(FCAAVPResult)){
        if(getMPAGRLMethode5(OOLINE,MPLINESave)){
          if(getMPAGRP(OOLINE,MPLINESave,MPAGRLSave)){
            if(getCUGEX1(MPAGRLSave)){
              if(getMPCOVE(CUGEX1Save.get("F1A330").toString(),ORDT as Integer)){
                logger.debug("Lignes methode4 =>")
                logger.debug("DIVI:"+DIVI)
                logger.debug("ORNO:"+OOLINE.get("OBORNO").toString())
                logger.debug("PONR:"+OOLINE.get("OBPONR").toString())
                logger.debug("POSX:"+OOLINE.get("OBPOSX").toString())
                logger.debug("AL30:"+CUGEX1Save.get("F1A330").toString())
                logger.debug("FVDT:"+MPAGRLSave.get("AIFVDT").toString())
                logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
                logger.debug("OVHE: "+MPCOVESave.get("IJOVHE").toString())
                def PUPR = APPR//MPAGRPSave.get("AJPUPR").toString() as Double
                def N096 = CUGEX1Save.get("F1N096").toString() as Double
                def N196 = CUGEX1Save.get("F1N196").toString() as Double
                def N296 = CUGEX1Save.get("F1N296").toString() as Double
                def OVHE = MPCOVESave.get("IJOVHE").toString() as Double
                def montant = 0
                //montant = ((PUPR - N096 - N196)*(OVHE/100))*(-1)
                if(N296>0){
                  //montant = (N296*(OVHE/100))*(-1)
                  montant = ((N296*(1-(OVHE/100))) + N096 + N196)-PUPR
                }else{
                  //montant = (PUPR*(OVHE/100))*(-1)
                  montant = ((PUPR*(1-(OVHE/100))) + N096 + N196)-PUPR
                }
                montant = montant*10000
                logger.debug("Montant: "+montant)
                def calcul = Math.round((montant as Double) * 100) / 100
                logger.debug("Montant: "+calcul)
                AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1",calcul as String)
                updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),PUPR)
                UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),PUPR as String)
                updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),PUPR)
              }else{
                logger.debug("Lignes methode4 =>")
                logger.debug("Montant: "+"0")
                logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
                AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1","0")
                updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),APPR)
                UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),APPR as String)
                updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),APPR)
              }
            }else{
              logger.debug("Lignes methode4 =>")
              logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
              logger.debug("AGNB: "+MPAGRLSave.get("AIAGNB").toString())
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"",MPAGRPSave.get("AJPUPR").toString() as Double)
              UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRPSave.get("AJPUPR").toString())
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"",MPAGRPSave.get("AJPUPR").toString() as Double)
            }
          }
        }
        //}
        //}
      }else{
        def PUPR = 0
        DBAction RechercheMITMAS = database.table("MITMAS").index("00").selection("MMPUPR","MMECMA").build()
        DBContainer MITMAS = RechercheMITMAS.getContainer()
        MITMAS.set("MMCONO", currentCompany)
        MITMAS.set("MMITNO", OOLINE.get("OBITNO").toString())
        if(RechercheMITMAS.read(MITMAS)){
          if((MITMAS.get("MMECMA").toString() as Integer)==0){
            PUPR = 0
          }else{
            PUPR = (MITMAS.get("MMPUPR").toString() as Double)
          }
          if(PUPR==0){
            DBAction RechercheMITFAC = database.table("MITFAC").index("00").selection("M9APPR").build()
            DBContainer MITFAC = RechercheMITFAC.getContainer()
            MITFAC.set("M9CONO", currentCompany)
            MITFAC.set("M9FACI", OOLINE.get("OBFACI").toString())
            MITFAC.set("M9ITNO", OOLINE.get("OBITNO").toString())
            if(RechercheMITFAC.read(MITFAC)){
              PUPR = (MITFAC.get("M9APPR").toString() as Double)
            }
          }
          //Suite =>
          logger.debug("Lignes methode 4 avec MITMAS/MITFAC =>")
          logger.debug("Montant: "+PUPR)
          UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),PUPR as String)
          //updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString())
        }
      }
    })
    if(!findFCAAVP){
      logger.debug("Methode 4")
      methode2(OOLINE)
    }
  }
  //Methode 51
  private methode51(DBContainer OOLINE){
    //if(getOPROMH(OOLINE) && getOPRICH(OOLINE)){
    //if(getMPAGRHbyOPRICHbyOPROMH(OPROMHSave,OPRICHSave)){
    if(getMPOPLP(OOLINE)){
      if(getMPAGRLbyMPOPLP(OOLINE,MPOPLPSave)){
        if(getMPAGRPbyMPOPLP(OOLINE,MPOPLPSave,MPAGRLSave)){
          if(getCUGEX1(MPAGRLSave)){
            if(getMPCOVE(CUGEX1Save.get("F1A330").toString(),ORDT as Integer)){
              logger.debug("Lignes methode3 =>")
              logger.debug("DIVI:"+DIVI)
              logger.debug("ORNO:"+OOLINE.get("OBORNO").toString())
              logger.debug("PONR:"+OOLINE.get("OBPONR").toString())
              logger.debug("POSX:"+OOLINE.get("OBPOSX").toString())
              logger.debug("AL30:"+CUGEX1Save.get("F1A330").toString())
              logger.debug("FVDT:"+MPAGRLSave.get("AIFVDT").toString())
              logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
              logger.debug("OVHE: "+MPCOVESave.get("IJOVHE").toString())
              def PUPR = MPAGRPSave.get("AJPUPR").toString() as Double
              def N096 = CUGEX1Save.get("F1N096").toString() as Double
              def N196 = CUGEX1Save.get("F1N196").toString() as Double
              def N296 = CUGEX1Save.get("F1N296").toString() as Double
              def OVHE = MPCOVESave.get("IJOVHE").toString() as Double
              def montant = 0
              //montant = ((PUPR - N096 - N196)*(OVHE/100))*(-1)
              if(N296>0){
                //montant = (N296*(OVHE/100))*(-1)
                montant = ((N296*(1-(OVHE/100))) + N096 + N196)-PUPR
              }else{
                //montant = (PUPR*(OVHE/100))*(-1)
                montant = ((PUPR*(1-(OVHE/100))) + N096 + N196)-PUPR
              }
              montant = montant*10000
              logger.debug("Montant: "+montant)
              def calcul = Math.round((montant as Double) * 100) / 100
              logger.debug("Montant: "+calcul)
              AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1",calcul as String)
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),PUPR)
              UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),PUPR as String)
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),PUPR)
            }else{
              logger.debug("Lignes methode51 =>")
              logger.debug("Montant: "+"0")
              logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
              AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1","0")
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),MPAGRPSave.get("AJPUPR").toString() as Double)
              UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRPSave.get("AJPUPR").toString())
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),MPAGRPSave.get("AJPUPR").toString() as Double)
            }
          }else{
            logger.debug("Lignes methode51 =>")
            logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
            logger.debug("AGNB: "+MPAGRLSave.get("AIAGNB").toString())
            updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"",MPAGRPSave.get("AJPUPR").toString() as Double)
            UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRPSave.get("AJPUPR").toString())
            updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"",MPAGRPSave.get("AJPUPR").toString() as Double)
          }
        }
      }
      // }
      //}
    }else{
      def PUPR = 0
      DBAction RechercheMITMAS = database.table("MITMAS").index("00").selection("MMPUPR","MMECMA").build()
      DBContainer MITMAS = RechercheMITMAS.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO", OOLINE.get("OBITNO").toString())
      if(RechercheMITMAS.read(MITMAS)){
        if((MITMAS.get("MMECMA").toString() as Integer)==0){
          PUPR = 0
        }else{
          PUPR = (MITMAS.get("MMPUPR").toString() as Double)
        }
        if(PUPR==0){
          DBAction RechercheMITFAC = database.table("MITFAC").index("00").selection("M9APPR").build()
          DBContainer MITFAC = RechercheMITFAC.getContainer()
          MITFAC.set("M9CONO", currentCompany)
          MITFAC.set("M9FACI", OOLINE.get("OBFACI").toString())
          MITFAC.set("M9ITNO", OOLINE.get("OBITNO").toString())
          if(RechercheMITFAC.read(MITFAC)){
            PUPR = (MITFAC.get("M9APPR").toString() as Double)
          }
        }
        //Suite =>
        logger.debug("Lignes methode3 avec MITMAS/MITFAC =>")
        logger.debug("Montant: "+PUPR)
        UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),PUPR as String)
        //updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString())
      }
    }
  }
  //Methode 52
  private methode52(DBContainer OOLINE){
    //if(getOPROMH(OOLINE) && getOPRICH(OOLINE)){
    //if(getMPAGRHbyOPRICHbyOPROMH(OPROMHSave,OPRICHSave)){
    if(getMPLINE(OOLINE)){
      if(getMPAGRL(OOLINE,MPLINESave)){
        if(getMPAGRP(OOLINE,MPLINESave,MPAGRLSave)){
          if(getCUGEX1(MPAGRLSave)){
            if(getMPCOVE(CUGEX1Save.get("F1A330").toString(),ORDT as Integer)){
              logger.debug("Lignes methode3 =>")
              logger.debug("DIVI:"+DIVI)
              logger.debug("ORNO:"+OOLINE.get("OBORNO").toString())
              logger.debug("PONR:"+OOLINE.get("OBPONR").toString())
              logger.debug("POSX:"+OOLINE.get("OBPOSX").toString())
              logger.debug("AL30:"+CUGEX1Save.get("F1A330").toString())
              logger.debug("FVDT:"+MPAGRLSave.get("AIFVDT").toString())
              logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
              logger.debug("OVHE: "+MPCOVESave.get("IJOVHE").toString())
              def PUPR = MPAGRPSave.get("AJPUPR").toString() as Double
              def N096 = CUGEX1Save.get("F1N096").toString() as Double
              def N196 = CUGEX1Save.get("F1N196").toString() as Double
              def N296 = CUGEX1Save.get("F1N296").toString() as Double
              def OVHE = MPCOVESave.get("IJOVHE").toString() as Double
              def montant = 0
              //montant = ((PUPR - N096 - N196)*(OVHE/100))*(-1)
              if(N296>0){
                //montant = (N296*(OVHE/100))*(-1)
                montant = ((N296*(1-(OVHE/100))) + N096 + N196)-PUPR
              }else{
                //montant = (PUPR*(OVHE/100))*(-1)
                montant = ((PUPR*(1-(OVHE/100))) + N096 + N196)-PUPR
              }
              montant = montant*10000
              logger.debug("Montant: "+montant)
              def calcul = Math.round((montant as Double) * 100) / 100
              logger.debug("Montant: "+calcul)
              AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1",calcul as String)
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),PUPR)
              UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),PUPR as String)
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),PUPR)
            }else{
              logger.debug("Lignes methode3 =>")
              logger.debug("Montant: "+"0")
              logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
              AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1","0")
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),MPAGRPSave.get("AJPUPR").toString() as Double)
              UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRPSave.get("AJPUPR").toString())
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),MPAGRPSave.get("AJPUPR").toString() as Double)
            }
          }else{
            logger.debug("Lignes methode52 =>")
            logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
            logger.debug("AGNB: "+MPAGRLSave.get("AIAGNB").toString())
            updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"",MPAGRPSave.get("AJPUPR").toString() as Double)
            UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRPSave.get("AJPUPR").toString())
            updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"",MPAGRPSave.get("AJPUPR").toString() as Double)
          }
        }
      }
      // }
      //}
    }else{
      def PUPR = 0
      DBAction RechercheMITMAS = database.table("MITMAS").index("00").selection("MMPUPR","MMECMA").build()
      DBContainer MITMAS = RechercheMITMAS.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO", OOLINE.get("OBITNO").toString())
      if(RechercheMITMAS.read(MITMAS)){
        if((MITMAS.get("MMECMA").toString() as Integer)==0){
          PUPR = 0
        }else{
          PUPR = (MITMAS.get("MMPUPR").toString() as Double)
        }
        if(PUPR==0){
          DBAction RechercheMITFAC = database.table("MITFAC").index("00").selection("M9APPR").build()
          DBContainer MITFAC = RechercheMITFAC.getContainer()
          MITFAC.set("M9CONO", currentCompany)
          MITFAC.set("M9FACI", OOLINE.get("OBFACI").toString())
          MITFAC.set("M9ITNO", OOLINE.get("OBITNO").toString())
          if(RechercheMITFAC.read(MITFAC)){
            PUPR = (MITFAC.get("M9APPR").toString() as Double)
          }
        }
        //Suite =>
        logger.debug("Lignes methode3 avec MITMAS/MITFAC =>")
        logger.debug("Montant: "+PUPR)
        UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),PUPR as String)
        //updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString())
      }
    }
  }
  //Methode get OPRICH with OOLINE
  private getOPRICH(DBContainer OOLINE){
    def OPRICHfind = false
    OPRICHSave
    ExpressionFactory expression = database.getExpressionFactory("OPRICH")
    expression = expression.le("OJFVDT", ORDT as String).and(expression.ge("OJLVDT", ORDT as String))
    DBAction RechercheOPRICH = database.table("OPRICH").index("00").matching(expression).selection("OJTX15").build()
    DBContainer OPRICH = RechercheOPRICH.getContainer()
    OPRICH.set("OJCONO", currentCompany)
    OPRICH.set("OJPRRF", OOLINE.get("OBPRRF").toString())
    OPRICH.set("OJCUCD", CUCD)
    OPRICH.set("OJCUNO", OOLINE.get("OBCUNO").toString())
    RechercheOPRICH.readAll(OPRICH,4,{ DBContainer OPRICHresult ->
      OPRICHSave = OPRICHresult
      OPRICHfind = true
    })
    if(!OPRICHfind){
      ExpressionFactory expression2 = database.getExpressionFactory("OPRICH")
      expression2 = expression2.le("OJFVDT", ORDT as String).and(expression2.ge("OJLVDT", ORDT as String))
      DBAction RechercheOPRICH2 = database.table("OPRICH").index("00").matching(expression2).selection("OJTX15").build()
      DBContainer OPRICH2 = RechercheOPRICH2.getContainer()
      OPRICH2.set("OJCONO", currentCompany)
      OPRICH2.set("OJPRRF", OOLINE.get("OBPRRF").toString())
      OPRICH2.set("OJCUCD", CUCD)
      //OPRICH.set("OJCUNO", OOLINE.get("OBCUNO").toString())
      RechercheOPRICH2.readAll(OPRICH,3,{ DBContainer OPRICHresult2 ->
        OPRICHSave = OPRICHresult2
        OPRICHfind = true
      })
    }
    return OPRICHfind
  }
  //Methode get OPROMH with OOLINE
  private getOPROMH(DBContainer OOLINE){
    def OPROMHFind=false
    ExpressionFactory expression = database.getExpressionFactory("OPROMH")
    expression = expression.le("FZFVDT", ORDT as String).and(expression.ge("FZLVDT", ORDT as String))
    DBAction RechercheOPROMH = database.table("OPROMH").index("00").matching(expression).selection("FZTX15").build()
    DBContainer OPROMH = RechercheOPROMH.getContainer()
    OPROMH.set("FZCONO", currentCompany)
    OPROMH.set("FZDIVI", DIVI)
    OPROMH.set("FZPIDE", OOLINE.get("OBPIDE").toString())
    RechercheOPROMH.readAll(OPROMH,3,{ DBContainer OPROMHresult ->
      OPROMHSave = OPROMHresult
      OPROMHFind = true
    })
    return OPROMHFind
  }
  //Methode get MPAGRH with OPRICH and OPROMH
  private getMPAGRHbyOPRICHbyOPROMH(DBContainer OPROMH,DBContainer OPRICH){
    def MPAGRHFind=false
    ExpressionFactory expression = database.getExpressionFactory("MPAGRH")
    expression = expression.eq("AHVAGN", OPROMH.get("FZTX15").toString())
    DBAction RechercheMPAGRH = database.table("MPAGRH").index("00").matching(expression).selection("AHAGNB","AHSUNO").build()
    DBContainer MPAGRH = RechercheMPAGRH.getContainer()
    MPAGRH.set("AHCONO", currentCompany)
    MPAGRH.set("AHSUNO", OPRICH.get("OJTX15").toString())
    RechercheMPAGRH.readAll(MPAGRH,2,1,{ DBContainer MPAGRHresult ->
      MPAGRHSave = MPAGRHresult
      MPAGRHFind = true
    })
    return MPAGRHFind
  }
  //Methode get MPAGRL with MPAGRH
  private getMPAGRLbyMPAGRH(DBContainer OOLINE,DBContainer MPAGRH){
    def MPAGRLFind = false
    ExpressionFactory expression = database.getExpressionFactory("MPAGRL")
    expression = expression.le("AIFVDT", ORDT as String).and(expression.ge("AIUVDT", ORDT as String))
    DBAction RechercheMPAGRL = database.table("MPAGRL").index("00").matching(expression).selection("AIFVDT","AISUNO","AIAGNB","AIGRPI","AIOBV1","AIOBV2","AIOBV3","AIOBV4").build()
    DBContainer MPAGRL = RechercheMPAGRL.getContainer()
    MPAGRL.set("AICONO", currentCompany)
    MPAGRL.set("AISUNO", MPAGRH.get("AHSUNO").toString())
    MPAGRL.set("AIAGNB", MPAGRH.get("AHAGNB").toString())
    MPAGRL.set("AIGRPI", 30)
    MPAGRL.set("AIOBV1", OOLINE.get("OBITNO").toString())
    RechercheMPAGRL.readAll(MPAGRL,5,{ DBContainer MPAGRLresult ->
      MPAGRLSave = MPAGRLresult
      MPAGRLFind = true
      logger.debug("MPAGRL Retenu Boucle ITNO => "+MPAGRLresult.get("AIOBV1").toString())
    })
    return MPAGRLFind
  }
  //Methode get MPAGRL with OOLINE and MPLINE
  private getMPAGRL(DBContainer OOLINE,DBContainer MPLINE){
    def MPAGRLFind = false
    ExpressionFactory expression = database.getExpressionFactory("MPAGRL")
    expression = expression.le("AIFVDT", ORDT as String).and(expression.ge("AIUVDT", ORDT as String)).and(expression.eq("AISEQN", MPLINE.get("IBSEQN").toString()))
    DBAction RechercheMPAGRL = database.table("MPAGRL").index("00").matching(expression).selection("AIFVDT","AISUNO","AIAGNB","AIGRPI","AIOBV1","AIOBV2","AIOBV3","AIOBV4").build()
    DBContainer MPAGRL = RechercheMPAGRL.getContainer()
    MPAGRL.set("AICONO", currentCompany)
    MPAGRL.set("AISUNO", MPLINE.get("IBSUNO").toString())
    MPAGRL.set("AIAGNB", MPLINE.get("IBOURR").toString())
    MPAGRL.set("AIGRPI", 30)
    MPAGRL.set("AIOBV1", OOLINE.get("OBITNO").toString())
    RechercheMPAGRL.readAll(MPAGRL,5,{ DBContainer MPAGRLresult ->
      MPAGRLSave = MPAGRLresult
      MPAGRLFind = true
      logger.debug("MPAGRL Retenu Boucle ITNO => "+MPAGRLresult.get("AIOBV1").toString())
    })
    return MPAGRLFind
  }
  //Methode get MPAGRL with OOLINE and MPLINE
  private getMPAGRLMethode5(DBContainer OOLINE,DBContainer MPLINE){
    def MPAGRLFind = false
    ExpressionFactory expression = database.getExpressionFactory("MPAGRL")
    expression = expression.le("AIFVDT", ORDT as String).and(expression.ge("AIUVDT", ORDT as String)).and(expression.eq("AISEQN", MPLINE.get("IBSEQN").toString()))
    DBAction RechercheMPAGRL = database.table("MPAGRL").index("00").matching(expression).selection("AIFVDT","AISUNO","AIAGNB","AIGRPI","AIOBV1","AIOBV2","AIOBV3","AIOBV4").build()
    DBContainer MPAGRL = RechercheMPAGRL.getContainer()
    MPAGRL.set("AICONO", currentCompany)
    MPAGRL.set("AISUNO", MPLINE.get("IBSUNO").toString())
    MPAGRL.set("AIAGNB", MPLINE.get("IBOURR").toString())
    MPAGRL.set("AIGRPI", 30)
    MPAGRL.set("AIOBV1", OOLINE.get("OBITNO").toString())
    RechercheMPAGRL.readAll(MPAGRL,5,{ DBContainer MPAGRLresult ->
      MPAGRLSave = MPAGRLresult
      MPAGRLFind = true
      logger.debug("MPAGRL Retenu Boucle ITNO => "+MPAGRLresult.get("AIOBV1").toString())
    })
    return MPAGRLFind
  }

  //Methode get MPAGRP with MPAGRH and OOLINE and MPAGRL
  private getMPAGRPByMPAGRH(DBContainer OOLINE,DBContainer MPAGRH,DBContainer MPAGRL){
    def MPAGRPFind = false
    DBAction RechercheMPAGRP = database.table("MPAGRP").index("00").selection("AJPUPR").build()
    DBContainer MPAGRP = RechercheMPAGRP.getContainer()
    MPAGRP.set("AJCONO", currentCompany)
    MPAGRP.set("AJSUNO", MPAGRH.get("AHSUNO").toString())
    MPAGRP.set("AJAGNB", MPAGRH.get("AHAGNB").toString())
    MPAGRP.set("AJGRPI", 30)
    MPAGRP.set("AJOBV1", OOLINE.get("OBITNO").toString())
    MPAGRP.set("AJFVDT", MPAGRL.get("AIFVDT").toString() as Integer)
    MPAGRP.set("AJFRQT", 0)
    if(RechercheMPAGRP.read(MPAGRP)){
      PUPR = MPAGRP.get("AJPUPR").toString() as Double
      MPAGRPSave = MPAGRP
      MPAGRPFind = true
    }
    return MPAGRPFind
  }
  //Methode get MPAGRP with MPLINE and OOLINE and MPAGRL
  private getMPAGRP(DBContainer OOLINE,DBContainer MPLINE,DBContainer MPAGRL){
    def MPAGRPFind = false
    DBAction RechercheMPAGRP = database.table("MPAGRP").index("00").selection("AJPUPR").build()
    DBContainer MPAGRP = RechercheMPAGRP.getContainer()
    MPAGRP.set("AJCONO", currentCompany)
    MPAGRP.set("AJSUNO", MPLINE.get("IBSUNO").toString())
    MPAGRP.set("AJAGNB", MPLINE.get("IBOURR").toString())
    MPAGRP.set("AJGRPI", 30)
    MPAGRP.set("AJOBV1", OOLINE.get("OBITNO").toString())
    MPAGRP.set("AJFVDT", MPAGRL.get("AIFVDT").toString() as Integer)
    MPAGRP.set("AJFRQT", 0)
    if(RechercheMPAGRP.read(MPAGRP)){
      PUPR = MPAGRP.get("AJPUPR").toString() as Double
      MPAGRPSave = MPAGRP
      MPAGRPFind = true
    }
    return MPAGRPFind
  }
  // Get MPAGRL MPOPLP
  private getMPAGRPbyMPOPLP(DBContainer OOLINE,DBContainer MPOPLP,DBContainer MPAGRL){
    def MPAGRPFind = false
    DBAction RechercheMPAGRP = database.table("MPAGRP").index("00").selection("AJPUPR").build()
    DBContainer MPAGRP = RechercheMPAGRP.getContainer()
    MPAGRP.set("AJCONO", currentCompany)
    MPAGRP.set("AJSUNO", MPOPLP.get("POSUNO").toString())
    MPAGRP.set("AJAGNB", MPOPLP.get("POOURR").toString())
    MPAGRP.set("AJGRPI", 30)
    MPAGRP.set("AJOBV1", OOLINE.get("OBITNO").toString())
    MPAGRP.set("AJFVDT", MPAGRL.get("AIFVDT").toString() as Integer)
    MPAGRP.set("AJFRQT", 0)
    if(RechercheMPAGRP.read(MPAGRP)){
      PUPR = MPAGRP.get("AJPUPR").toString() as Double
      MPAGRPSave = MPAGRP
      MPAGRPFind = true
    }
    return MPAGRPFind
  }

  //Get and find OSBSTD with OOLINE
  private findOSBSTDLine(DBContainer OOLINE){
    boolean valid = false
    ExpressionFactory expression = database.getExpressionFactory("OSBSTD")
    expression = expression.eq("UCPONR", OOLINE.get("OBPONR").toString()).and(expression.eq("UCPOSX", OOLINE.get("OBPOSX").toString()))
    DBAction RechercheOSBSTD = database.table("OSBSTD").index("00").matching(expression).selection("UCDCOS","UCUCOS","UCDLIX").build()
    DBContainer OSBSTD = RechercheOSBSTD.getContainer()
    OSBSTD.set("UCCONO", currentCompany)
    OSBSTD.setString("UCDIVI", DIVI)
    OSBSTD.setString("UCORNO", ORNO)
    RechercheOSBSTD.readAll(OSBSTD,3,{ DBContainer OSBSTDresult ->
      valid = true
      OSBSTDSave = OSBSTDresult
    })
    return valid
  }
  //Get CUGEX1 with MPAGRLRetenu
  private getCUGEX1(DBContainer MPAGRLRetenu){
    def valid = false
    DBAction RechercheCUGEX1 = database.table("CUGEX1").index("00").selection("F1A030","F1A330","F1N096","F1N196","F1N296").build()
    DBContainer CUGEX1 = RechercheCUGEX1.getContainer()
    CUGEX1.set("F1CONO", currentCompany)
    CUGEX1.set("F1FILE", "MPAGRL")
    CUGEX1.set("F1PK01", MPAGRLRetenu.get("AISUNO").toString())
    CUGEX1.set("F1PK02", MPAGRLRetenu.get("AIAGNB").toString())
    CUGEX1.set("F1PK03", MPAGRLRetenu.get("AIGRPI").toString())
    CUGEX1.set("F1PK04", MPAGRLRetenu.get("AIOBV1").toString())
    CUGEX1.set("F1PK05", MPAGRLRetenu.get("AIOBV2").toString())
    CUGEX1.set("F1PK06", MPAGRLRetenu.get("AIOBV3").toString())
    CUGEX1.set("F1PK07", MPAGRLRetenu.get("AIOBV4").toString())
    CUGEX1.set("F1PK08", MPAGRLRetenu.get("AIFVDT").toString())
    if(RechercheCUGEX1.read(CUGEX1)){
      CUGEX1Save = CUGEX1
      valid = true
    }
    return valid
  }

  //get MPOPLP with OOLINE
  private getMPOPLP(DBContainer OOLINE){
    def MPOPLPFind = false
    logger.debug("OBORNO: "+OOLINE.get("OBORNO").toString())
    logger.debug("OBPONR: "+OOLINE.get("OBPONR").toString())
    logger.debug("OBPOSX: "+OOLINE.get("OBPOSX").toString())
    DBAction RechercheMPOPLP = database.table("MPOPLP").index("90").selection("POAGNB","POSUNO","POSEQN","POOURR").build()
    DBContainer MPOPLP = RechercheMPOPLP.getContainer()
    MPOPLP.set("POCONO", currentCompany)
    MPOPLP.set("PORORN", OOLINE.get("OBORNO").toString())
    MPOPLP.set("PORORL", OOLINE.get("OBPONR").toString() as Integer)
    MPOPLP.set("PORORX", OOLINE.get("OBPOSX").toString() as Integer)
    RechercheMPOPLP.readAll(MPOPLP,4,1,{ DBContainer MPOPLPResult ->
      //AGNB = MPOPLPResult.get("POAGNB").toString()
      MPOPLPSave = MPOPLPResult
      MPOPLPFind = true
    })
    return MPOPLPFind
  }
  //Methode get MPAGRL with OOLINE and MPOPLP
  private getMPAGRLbyMPOPLP(DBContainer OOLINE,DBContainer MPOPLP){
    def MPAGRLFind = false
    logger.debug("AGNB: "+MPOPLP.get("POOURR").toString())
    logger.debug("SUNO: "+MPOPLP.get("POSUNO").toString())
    logger.debug("SEQN: "+MPOPLP.get("POSEQN").toString())
    ExpressionFactory expression = database.getExpressionFactory("MPAGRL")
    expression = expression.le("AIFVDT", ORDT as String).and(expression.ge("AIUVDT", ORDT as String)).and(expression.eq("AISEQN", MPOPLP.get("POSEQN").toString()))
    DBAction RechercheMPAGRL = database.table("MPAGRL").index("00").matching(expression).selection("AIFVDT","AISUNO","AIAGNB","AIGRPI","AIOBV1","AIOBV2","AIOBV3","AIOBV4").build()
    DBContainer MPAGRL = RechercheMPAGRL.getContainer()
    MPAGRL.set("AICONO", currentCompany)
    MPAGRL.set("AISUNO", MPOPLP.get("POSUNO").toString())
    MPAGRL.set("AIAGNB", MPOPLP.get("POOURR").toString())
    MPAGRL.set("AIGRPI", 30)
    MPAGRL.set("AIOBV1", OOLINE.get("OBITNO").toString())
    RechercheMPAGRL.readAll(MPAGRL,5,{ DBContainer MPAGRLresult ->
      MPAGRLSave = MPAGRLresult
      MPAGRLFind = true
      logger.debug("MPAGRL Retenu Boucle ITNO => "+MPAGRLresult.get("AIOBV1").toString())
    })
    return MPAGRLFind
  }
  //Get MPLINE with OOLINE
  private getMPLINE(DBContainer OOLINE){
    def MPLINEFind = false
    DBAction RechercheMPLINE = database.table("MPLINE").index("00").selection("IBOURR","IBSUNO","IBSEQN").build()
    DBContainer MPLINE = RechercheMPLINE.getContainer()
    MPLINE.set("IBCONO", currentCompany)
    MPLINE.set("IBPUNO", OOLINE.get("OBRORN").toString())
    MPLINE.set("IBPNLI", OOLINE.get("OBRORL").toString() as Integer)
    MPLINE.set("IBPNLS", OOLINE.get("OBRORX").toString() as Integer)
    if(RechercheMPLINE.read(MPLINE)){
      AGNB = MPLINE.get("IBOURR").toString()
      SUNO = MPLINE.get("IBSUNO").toString()
      MPLINESave = MPLINE
      MPLINEFind = true
    }
    return MPLINEFind
  }
  // Get MPLINE FCAAVP
  private getMPLINEByFCAAVP(DBContainer FCAAVP){
    def MPLINEFind = false
    DBAction RechercheMPLINE = database.table("MPLINE").index("00").selection("IBOURR","IBSUNO","IBSEQN").build()
    DBContainer MPLINE = RechercheMPLINE.getContainer()
    MPLINE.set("IBCONO", currentCompany)
    MPLINE.set("IBPUNO", FCAAVP.get("A7RIDN").toString())
    MPLINE.set("IBPNLI", FCAAVP.get("A7RIDL").toString() as Integer)
    MPLINE.set("IBPNLS", FCAAVP.get("A7RIDX").toString() as Integer)
    if(RechercheMPLINE.read(MPLINE)){
      MPLINESave = MPLINE
      MPLINEFind = true
    }
    return MPLINEFind
  }
  //Get MPLINE with MILOMA
  private getMPLINEbyMILOMA(DBContainer MILOMA){
    //MPLINE
    def MPLINEFind = false
    DBAction RechercheMPLINE = database.table("MPLINE").index("00").selection("IBOURR","IBSUNO").build()
    DBContainer MPLINE = RechercheMPLINE.getContainer()
    MPLINE.set("IBCONO", currentCompany)
    MPLINE.set("IBPUNO", MILOMA.get("LMRORN").toString())
    MPLINE.set("IBPNLI", MILOMA.get("LMRORL").toString() as Integer)
    MPLINE.set("IBPNLS", MILOMA.get("LMRORX").toString() as Integer)
    if(RechercheMPLINE.read(MPLINE)){
      AGNB = MPLINE.get("IBOURR").toString()
      SUNO = MPLINE.get("IBSUNO").toString()
      MPLINESave = MPLINE
      MPLINEFind = true
    }
    return MPLINEFind
  }
  //Get MPCOVE with AL30 and FVDT
  private getMPCOVE(String AL30,Integer FVDT){
    //MPCOVE
    def MPCOVEFind = false
    ExpressionFactory expression = database.getExpressionFactory("MPCOVE")
    expression = expression.le("IJVFDT", FVDT as String)
    DBAction RechercheMPCOVE = database.table("MPCOVE").index("00").matching(expression).selection("IJOVHE","IJVFDT").build()
    DBContainer MPCOVE = RechercheMPCOVE.getContainer()
    MPCOVE.set("IJCONO", currentCompany)
    MPCOVE.set("IJCEID", "RFAFRS")
    MPCOVE.set("IJOVK1", AL30)
    RechercheMPCOVE.readAll(MPCOVE,3,{ DBContainer MPCOVEresult ->
      MPCOVESave = MPCOVEresult
      MPCOVEFind = true
    })
    return MPCOVEFind
  }
  //Call API OIS100MI UpdPriceInfo with ORNO,PONR,POSX,UCOS
  private UpdPriceInforOIS100MI(String ORNO,String PONR,String POSX,String UCOS){
    def paramsUpd = ["ORNO":ORNO,"PONR":PONR,"POSX":POSX,"UCOS":UCOS]
    Closure<?> closureUpd = {Map<String, String> responseUpd ->
      if(responseUpd.error == null){

      }
    }
    miCaller.call("OIS100MI", "UpdPriceInfo", paramsUpd, closureUpd)
  }
  //Upd OOLINE with  CONO ORNO PONR POSX UCA9 UCA0 UCA2
  private updLineEXT120method1(String CONO,String ORNO,String PONR,String POSX,String UCA9,String UCA0,String UCA2){
    DBAction RechercheOOLINE = database.table("OOLINE").index("00").build()
    DBContainer OOLINE = RechercheOOLINE.getContainer()
    OOLINE.set("OBCONO", currentCompany)
    OOLINE.set("OBORNO", ORNO)
    OOLINE.set("OBPONR", PONR as Integer)
    OOLINE.set("OBPOSX", POSX as Integer)
    if(!RechercheOOLINE.readLock(OOLINE, { LockedResult lockedResult ->
      LocalDateTime timeOfCreation = LocalDateTime.now()
      int changeNumber = lockedResult.get("OBCHNO")
      if(UCA9){
        lockedResult.set("OBUCA9", UCA9.trim())
      }
      if(UCA0){
        lockedResult.set("OBUCA0", UCA0.trim())
      }
      if(UCA2){
        lockedResult.set("OBUCA2", UCA2.trim())
      }
      lockedResult.setInt("OBLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      lockedResult.setInt("OBCHNO", changeNumber + 1)
      lockedResult.set("OBCHID", program.getUser())
      lockedResult.update()
    })){
      mi.error("La ligne de commande n'existe pas")
      return
    }
  }
  //Upd OOLINE with  CONO ORNO PONR POSX UCA7 UCA8 UCA1
  private updLineEXT120method2(String CONO,String ORNO,String PONR,String POSX,String UCA7,String UCA8,String UCA1, Double PUPR){
    DBAction RechercheOOLINE = database.table("OOLINE").index("00").build()
    DBContainer OOLINE = RechercheOOLINE.getContainer()
    OOLINE.set("OBCONO", currentCompany)
    OOLINE.set("OBORNO", ORNO)
    OOLINE.set("OBPONR", PONR as Integer)
    OOLINE.set("OBPOSX", POSX as Integer)
    if(!RechercheOOLINE.readLock(OOLINE, { LockedResult lockedResult ->
      LocalDateTime timeOfCreation = LocalDateTime.now()
      int changeNumber = lockedResult.get("OBCHNO")
      if(UCA7!=""){
        lockedResult.set("OBUCA7", UCA7.trim())
      }
      if(UCA8!=""){
        lockedResult.set("OBUCA8", UCA8.trim())
      }
      if(UCA1!=""){
        lockedResult.set("OBUCA1", UCA1.trim())
      }
      lockedResult.set("OBINPR", PUPR as Double)
      lockedResult.setInt("OBLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer)
      lockedResult.setInt("OBCHNO", changeNumber + 1)
      lockedResult.set("OBCHID", program.getUser())
      lockedResult.update()
    })){
      mi.error("La ligne de commande n'existe pas")
      return
    }
  }
  //Get EXT075 with OOLINE ORDT
  private getEXT075(DBContainer OOLINE,String ORDT){
    def findOPRBAS = false
    def findEXT075 = false
    ExpressionFactory expression = database.getExpressionFactory("OPRBAS")
    expression = expression.le("ODFVDT", ORDT as String).and(expression.ge("ODLVDT", ORDT as String)).and(expression.eq("ODITNO",OOLINE.get("OBITNO").toString()))
    DBAction RechercheOPRBAS = database.table("OPRBAS").index("00").matching(expression).selection("ODPRRF","ODCUCD","ODCUNO","ODFVDT","ODITNO").build()
    DBContainer OPRBAS = RechercheOPRBAS.getContainer()
    OPRBAS.set("ODCONO", currentCompany)
    //OPRBAS.set("ODCUNO", OOLINE.get("OBCUNO").toString())
    OPRBAS.set("ODCUNO", "")
    OPRBAS.set("ODPRRF", OOLINE.get("OBPRRF").toString())
    OPRBAS.set("ODCUCD", CUCD)
    RechercheOPRBAS.readAll(OPRBAS,4,{ DBContainer OPRBASLresult ->
      findOPRBAS = true
      OPRBASSave = OPRBASLresult
    })
    if(findOPRBAS){
      DBAction query = database.table("EXT075").index("00").selection("EXSAPR","EXAGNB","EXSUNO").build()
      DBContainer EXT075 = query.getContainer()
      EXT075.set("EXCONO", currentCompany)
      EXT075.set("EXPRRF", OPRBASSave.get("ODPRRF").toString())
      EXT075.set("EXCUCD", OPRBASSave.get("ODCUCD").toString())
      //EXT075.set("EXCUNO", OPRBASSave.get("ODCUNO").toString())
      EXT075.set("EXCUNO", OOLINE.get("OBCUNO").toString())
      EXT075.set("EXFVDT", OPRBASSave.get("ODFVDT").toString() as Integer)
      EXT075.set("EXITNO", OPRBASSave.get("ODITNO").toString())
      query.readAll(EXT075, 6, {DBContainer EXT075Response ->
        findEXT075 = true
        EXT075Save = EXT075Response
      })
    }
    if(findEXT075){
      if(getMPAGRLByEXT075(OOLINE,EXT075Save,ORDT)){
        if(getMPAGRPByEXT075(OOLINE,EXT075Save,MPAGRLSave,ORDT)){
          if(getCUGEX1(MPAGRLSave)){
            if(getMPCOVE(CUGEX1Save.get("F1A330").toString(),ORDT as Integer)){
              logger.debug("Lignes methode2 avec EXT075=>")
              logger.debug("DIVI:"+DIVI)
              logger.debug("ORNO:"+OOLINE.get("OBORNO").toString())
              logger.debug("PONR:"+OOLINE.get("OBPONR").toString())
              logger.debug("POSX:"+OOLINE.get("OBPOSX").toString())
              logger.debug("AL30:"+CUGEX1Save.get("F1A030").toString())
              logger.debug("FVDT:"+MPAGRLSave.get("AIFVDT").toString())
              logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
              logger.debug("OVHE: "+MPCOVESave.get("IJOVHE").toString())
              def PUPR = 0
              PUPR = MPAGRPSave.get("AJPUPR").toString() as Double
              def N096 = CUGEX1Save.get("F1N096").toString() as Double
              def N196 = CUGEX1Save.get("F1N196").toString() as Double
              def N296 = CUGEX1Save.get("F1N296").toString() as Double
              def OVHE = MPCOVESave.get("IJOVHE").toString() as Double

              def montant = 0
              //montant = ((PUPR - N096 - N196)*(OVHE/100))*(-1)
              /*if(N296>0){
                montant = (N296*(OVHE/100))*(-1)
              }else{
                montant = (PUPR*(OVHE/100))*(-1)
              }*/
              if(N296>0){
                //montant = (N296*(OVHE/100))*(-1)
                montant = ((N296*(1-(OVHE/100))) + N096 + N196)-PUPR
              }else{
                //montant = (PUPR*(OVHE/100))*(-1)
                montant = ((PUPR*(1-(OVHE/100))) + N096 + N196)-PUPR
              }
              montant = montant*10000
              logger.debug("Montant: "+montant)
              def calcul = Math.round((montant as Double) * 100) / 100
              logger.debug("Montant: "+calcul)
              AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1",calcul as String)
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),PUPR)
              UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),PUPR as String)
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),PUPR)
            }else{
              logger.debug("Lignes methode2 =>")
              logger.debug("Montant: "+"0")
              AddUpdLineCharge(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),"RFAFRS","1","0")
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),MPAGRPSave.get("AJPUPR").toString() as Double)
              UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRPSave.get("AJPUPR").toString())
              updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),CUGEX1Save.get("F1A030").toString(),MPAGRPSave.get("AJPUPR").toString() as Double)
            }
          }else{
            logger.debug("Lignes methode2 =>")
            logger.debug("PUPR: "+MPAGRPSave.get("AJPUPR").toString())
            logger.debug("AGNB: "+MPAGRLSave.get("AIAGNB").toString())
            updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"",MPAGRPSave.get("AJPUPR").toString() as Double)
            UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRPSave.get("AJPUPR").toString())
            updLineEXT120method2(currentCompany as String,OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),MPAGRLSave.get("AISUNO").toString(),MPAGRLSave.get("AIAGNB").toString(),"",MPAGRPSave.get("AJPUPR").toString() as Double)
          }
        }
      }
    }else{
      def PUPR = 0
      DBAction RechercheMITMAS = database.table("MITMAS").index("00").selection("MMPUPR","MMECMA").build()
      DBContainer MITMAS = RechercheMITMAS.getContainer()
      MITMAS.set("MMCONO", currentCompany)
      MITMAS.set("MMITNO", OOLINE.get("OBITNO").toString())
      if(RechercheMITMAS.read(MITMAS)){
        if((MITMAS.get("MMECMA").toString() as Integer)==0){
          PUPR = 0
        }else{
          PUPR = (MITMAS.get("MMPUPR").toString() as Double)
        }
        if(PUPR==0){
          DBAction RechercheMITFAC = database.table("MITFAC").index("00").selection("M9APPR").build()
          DBContainer MITFAC = RechercheMITFAC.getContainer()
          MITFAC.set("M9CONO", currentCompany)
          MITFAC.set("M9FACI", OOLINE.get("OBFACI").toString())
          MITFAC.set("M9ITNO", OOLINE.get("OBITNO").toString())
          if(RechercheMITFAC.read(MITFAC)){
            PUPR = (MITFAC.get("M9APPR").toString() as Double)
          }
        }
        logger.debug("Lignes methode2 avec MITMAS=>")
        logger.debug("Montant:"+PUPR)
        UpdPriceInforOIS100MI(OOLINE.get("OBORNO").toString(),OOLINE.get("OBPONR").toString(),OOLINE.get("OBPOSX").toString(),PUPR as String)
      }
    }
  }
  //Get MPAGRP with OOLINE EXT075 MPAGRL ORDT
  private getMPAGRPByEXT075(DBContainer OOLINE,DBContainer EXT075,DBContainer MPAGRL,ORDT){
    def MPAGRPFind = false
    DBAction RechercheMPAGRP = database.table("MPAGRP").index("00").selection("AJPUPR").build()
    DBContainer MPAGRP = RechercheMPAGRP.getContainer()
    MPAGRP.set("AJCONO", currentCompany)
    MPAGRP.set("AJSUNO", EXT075.get("EXSUNO").toString())
    MPAGRP.set("AJAGNB", EXT075.get("EXAGNB").toString())
    MPAGRP.set("AJGRPI", 30)
    MPAGRP.set("AJOBV1", OOLINE.get("OBITNO").toString())
    MPAGRP.set("AJFVDT", MPAGRL.get("AIFVDT").toString() as Integer)
    MPAGRP.set("AJFRQT", 0)
    if(RechercheMPAGRP.read(MPAGRP)){
      PUPR = MPAGRP.get("AJPUPR").toString() as Double
      MPAGRPSave = MPAGRP
      MPAGRPFind = true
    }
    return MPAGRPFind
  }
  //Get MPAGRL with OOLINE EXT075 ORDT
  private getMPAGRLByEXT075(DBContainer OOLINE,DBContainer EXT075,ORDT){
    def MPAGRLFind = false
    ExpressionFactory expression = database.getExpressionFactory("MPAGRL")
    expression = expression.le("AIFVDT", ORDT as String).and(expression.ge("AIUVDT", ORDT as String))
    DBAction RechercheMPAGRL = database.table("MPAGRL").index("00").matching(expression).selection("AIFVDT","AISUNO","AIAGNB","AIGRPI","AIOBV1","AIOBV2","AIOBV3","AIOBV4").build()
    DBContainer MPAGRL = RechercheMPAGRL.getContainer()
    MPAGRL.set("AICONO", currentCompany)
    MPAGRL.set("AISUNO", EXT075.get("EXSUNO").toString())
    MPAGRL.set("AIAGNB", EXT075.get("EXAGNB").toString())
    MPAGRL.set("AIGRPI", 30)
    MPAGRL.set("AIOBV1", OOLINE.get("OBITNO").toString())
    RechercheMPAGRL.readAll(MPAGRL,5,{ DBContainer MPAGRLresult ->
      MPAGRLSave = MPAGRLresult
      MPAGRLFind = true
      logger.debug("MPAGRL Retenu Boucle ITNO => "+MPAGRLresult.get("AIOBV1").toString())
    })
    return MPAGRLFind
  }
  //Call OIS100MI GetLineChrg => Upd or Add LineCharges with ORNO PONR POSX CRID CRTY CRAM
  private AddUpdLineCharge(String ORNO,String PONR,String POSX,String CRID,String CRTY,String CRAM){
    def findLinesCharges=false
    def paramsGet = ["ORNO":ORNO,"PONR":PONR,"POSX":POSX,"CRID":CRID,"CRTY":CRTY]
    Closure<?> closureGet = {Map<String, String> responseGet ->
      if(responseGet.error == null){
        findLinesCharges = true
      }
    }
    miCaller.call("OIS100MI", "GetLineChrg", paramsGet, closureGet)
    if(findLinesCharges){
      UpdLineCharges(ORNO,PONR,POSX,CRID,CRTY,CRAM)
    }else{
      AddLineCharges(ORNO,PONR,POSX,CRID,CRTY,CRAM)
      UpdLineCharges(ORNO,PONR,POSX,CRID,CRTY,CRAM)
    }
  }
  //Call API OIS100MI AddLineCharge with  ORNO PONR POSX CRID CRTY CRAM
  private AddLineCharges(String ORNO,String PONR,String POSX,String CRID,String CRTY,String CRAM){
    def paramsAdd = ["ORNO":ORNO,"PONR":PONR,"POSX":POSX,"CRID":CRID,"CRAM":CRAM,"CHPD":"10000"]
    Closure<?> closureAdd = {Map<String, String> responseAdd ->
      logger.debug("Response = ${responseAdd}")
    }
    miCaller.call("OIS100MI", "AddLineCharge", paramsAdd, closureAdd)
  }
  //Call API OIS100MI UpdLineCharge with  ORNO PONR POSX CRID CRTY CRAM
  private UpdLineCharges(String ORNO,String PONR,String POSX,String CRID,String CRTY,String CRAM){
    def paramsUpd = ["ORNO":ORNO,"PONR":PONR,"POSX":POSX,"CRID":CRID,"CRAM":CRAM,"CHPD":"10000"]
    Closure<?> closureUpd = {Map<String, String> responseUpd ->
      logger.debug("Response = ${responseUpd}")
    }
    miCaller.call("OIS100MI", "UpdLineCharge", paramsUpd, closureUpd)
  }
}
