/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT042MI.AddToc
 * Description : The AddToc transaction adds records to the EXT042 table.
 * Date         Changed By   Description
 * 20210514     APACE        TARX12 - Margin management
 */
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class AddToc extends ExtendM3Transaction {
  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private final UtilityAPI utility
  public String NBNR='';

  public AddToc(MIAPI mi, DatabaseAPI database, ProgramAPI program, MICallerAPI miCaller, UtilityAPI utility) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.miCaller = miCaller
    this.utility = utility
  }

  public void main() {
    Integer currentCompany;
    String cuno = '';
    String cunm = '';
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO;
    } else {
      currentCompany = mi.in.get("CONO");
    }

    if(mi.in.get("CUNO") != null){
      DBAction countryQuery = database.table("OCUSMA").index("00").selection("OKCUNM").build();
      DBContainer OCUSMA = countryQuery.getContainer();
      OCUSMA.set("OKCONO",currentCompany);
      OCUSMA.set("OKCUNO",mi.in.get("CUNO"));
      if (!countryQuery.read(OCUSMA)) {
        mi.error("Code Client " + mi.in.get("CUNO") + " n'existe pas");
        return;
      }
      cuno = mi.in.get("CUNO");
      cunm = OCUSMA.get("OKCUNM");
    }else{
      mi.error("Code Client est obligatoire");
      return;
    }
    /*if(mi.in.get("CUNM") != null){
      cunm = mi.in.get("CUNM");
    }*/


    String hie1 = '';
    if(mi.in.get("HIE1") != null){
      DBAction MITHRYQuery = database.table("MITHRY").index("00").build();
      DBContainer MITHRY = MITHRYQuery.getContainer();
      MITHRY.set('HIHIE0',mi.in.get("HIE1").toString());
      MITHRY.set('HIHLVL',1);
      MITHRY.set('HICONO',currentCompany);
      if (!MITHRYQuery.read(MITHRY)) {
        mi.error("Département " + mi.in.get("HIE1") + " n'existe pas");
        return;
      }
      hie1 = mi.in.get("HIE1");
    }
    String hie2 = '';
    if(mi.in.get("HIE2") != null){
      DBAction MITHRYQuery = database.table("MITHRY").index("00").build();
      DBContainer MITHRY = MITHRYQuery.getContainer();
      MITHRY.set('HIHIE0',mi.in.get("HIE2").toString());
      MITHRY.set('HIHLVL',3);
      MITHRY.set('HICONO',currentCompany);
      if (!MITHRYQuery.read(MITHRY)) {
        mi.error("Famille " + mi.in.get("HIE2") + " n'existe pas");
        return;
      }
      hie2 = mi.in.get("HIE2");
    }
    String hie3 = '';
    if(mi.in.get("HIE3") != null){
      DBAction MITHRYQuery = database.table("MITHRY").index("00").build();
      DBContainer MITHRY = MITHRYQuery.getContainer();
      MITHRY.set('HIHIE0',mi.in.get("HIE3").toString());
      MITHRY.set('HIHLVL',4);
      MITHRY.set('HICONO',currentCompany);
      if (!MITHRYQuery.read(MITHRY)) {
        mi.error("Sous Famille " + mi.in.get("HIE3") + " n'existe pas");
        return;
      }
      hie3 = mi.in.get("HIE3");
    }
    String hie4 = '';
    if(mi.in.get("HIE4") != null){
      DBAction MITHRYQuery = database.table("MITHRY").index("00").build();
      DBContainer MITHRY = MITHRYQuery.getContainer();
      MITHRY.set('HIHIE0',mi.in.get("HIE4").toString());
      MITHRY.set('HIHLVL',5);
      MITHRY.set('HICONO',currentCompany);
      if (!MITHRYQuery.read(MITHRY)) {
        mi.error("Segment " + mi.in.get("HIE4") + " n'existe pas");
        return;
      }
      hie4 = mi.in.get("HIE4");
    }
    String hie5 = '';
    if(mi.in.get("HIE5") != null){
      DBAction MITHRYQuery = database.table("MITHRY").index("00").build();
      DBContainer MITHRY = MITHRYQuery.getContainer();
      MITHRY.set('HIHIE0',mi.in.get("HIE5").toString());
      MITHRY.set('HIHLVL',2);
      MITHRY.set('HICONO',currentCompany);
      if (!MITHRYQuery.read(MITHRY)) {
        mi.error("Rayon " + mi.in.get("HIE5") + " n'existe pas");
        return;
      }
      hie5 = mi.in.get("HIE5");
    }
    String cfi5 = '';
    if(mi.in.get("CFI5") != null){
      DBAction CSYTABQuery = database.table("CSYTAB").index("00").build();
      DBContainer CSYTAB = CSYTABQuery.getContainer();
      CSYTAB.set('CTCONO',currentCompany);
      CSYTAB.set('CTSTCO','CFI5');
      CSYTAB.set('CTSTKY',mi.in.get("CFI5").toString());
      if (!CSYTABQuery.read(CSYTAB)) {
        mi.error("Pnmf " + mi.in.get("CFI5") + " n'existe pas");
        return;
      }
      cfi5 = mi.in.get("CFI5");
    }
    String popn = '';
    if(mi.in.get("POPN") != null){
      def paramsGetFieldVM = ["FILE":"MITMAS","CUER":"","FLDI":"F1A930","SEQN":"10","AL30":mi.in.get("POPN").toString()];
      Closure<?> closureGetFieldVM = {Map<String, String> responseUpd ->
        if(responseUpd.error != null){
          mi.error("Enseigne " + mi.in.get("POPN") + " n'existe pas");
          return;
        }
      }
      miCaller.call("CUSEXTMI", "GetFieldVM", paramsGetFieldVM, closureGetFieldVM);
      popn = mi.in.get("POPN");
    }
    String buar = '';
    if(mi.in.get("BUAR") != null){
      DBAction CSYTABQuery = database.table("CSYTAB").index("00").build();
      DBContainer CSYTAB = CSYTABQuery.getContainer();
      CSYTAB.set('CTCONO',currentCompany);
      CSYTAB.set('CTSTCO','BUAR');
      CSYTAB.set('CTSTKY',mi.in.get("BUAR").toString());
      if (!CSYTABQuery.read(CSYTAB)) {
        mi.error("Position Prix " + mi.in.get("BUAR") + " n'existe pas");
        return;
      }
      buar = mi.in.get("BUAR");
    }
    String cfi1 = '';
    if(mi.in.get("CFI1") != null){
      DBAction CSYTABQuery = database.table("CSYTAB").index("00").build();
      DBContainer CSYTAB = CSYTABQuery.getContainer();
      CSYTAB.set('CTCONO',currentCompany);
      CSYTAB.set('CTSTCO','CFI1');
      CSYTAB.set('CTSTKY',mi.in.get("CFI1").toString());
      if (!CSYTABQuery.read(CSYTAB)) {
        mi.error("Marque " + mi.in.get("CFI1") + " n'existe pas");
        return;
      }
      cfi1 = mi.in.get("CFI1");
    }


    String tx15 = '';
    if(mi.in.get("TX15") != null){
      tx15 = mi.in.get("TX15");
    }
    Double adjt = 0;
    if(mi.in.get("ADJT") != null){
      adjt = mi.in.get("ADJT");
    }
    Integer fvdt = 0;
    if(mi.in.get("FVDT") != null){
      fvdt = mi.in.get("FVDT");
    }
    Integer lvdt = 0;
    if(mi.in.get("LVDT") != null){
      lvdt = mi.in.get("LVDT");
    }

    if(fvdt == 0){
      mi.error("Date de Début est obligatoire");
      return;
    }else {
      if (!utility.call("DateUtil", "isDateValid", fvdt, "yyyyMMdd")) {
        mi.error("Format Date de Début incorrect");
        return;
      }
    }

    if(lvdt == 0){
      lvdt = 99999999;
    }else {
      if (!utility.call("DateUtil", "isDateValid", lvdt, "yyyyMMdd")) {
        mi.error("Format Date de Début incorrect");
        return;
      }
    }

    if(lvdt<fvdt){
      mi.error("La date de Début doit être supérieure a la date de Fin.");
      return;
    }

    LocalDateTime timeOfCreation = LocalDateTime.now();

    ExpressionFactory expression = database.getExpressionFactory("EXT050");
    expression = expression.eq("EXCONO", currentCompany.toString());
    if(cuno!=""){
      expression =  expression.and(expression.eq("EXCUNO", cuno));
    }
    if(hie1!=""){
      expression =  expression.and(expression.eq("EXHIE1", hie1));
    }
    if(hie2!=""){
      expression =  expression.and(expression.eq("EXHIE2", hie2));
    }
    if(hie3!=""){
      expression =  expression.and(expression.eq("EXHIE3", hie3));
    }
    if(hie4!=""){
      expression =  expression.and(expression.eq("EXHIE4", hie4));
    }
    if(hie5!=""){
      expression =  expression.and(expression.eq("EXHIE5", hie5));
    }
    if(cfi5!=""){
      expression =  expression.and(expression.eq("EXCFI5", cfi5));
    }
    if(popn!=""){
      expression =  expression.and(expression.eq("EXPOPN", popn));
    }
    if(buar!=""){
      expression =  expression.and(expression.eq("EXBUAR", buar));
    }
    if(cfi1!=""){
      expression =  expression.and(expression.eq("EXCFI1", cfi1));
    }
    if(tx15!=""){
      expression =  expression.and(expression.eq("EXTX15", tx15));
    }

    expression =  expression.and(expression.eq("EXFVDT", fvdt.toString()));


    DBAction query_ = database.table("EXT042").index("00").matching(expression).build();
    DBContainer EXT042_ = query_.getContainer();
    EXT042_.set("EXCONO", currentCompany);
    if (!query_.read(EXT042_)) {
      DBAction query = database.table("EXT042").index("00").build();
      DBContainer EXT042 = query.getContainer();
      EXT042.set("EXCONO", currentCompany);
      // Retrieve constraint ID
      executeCRS165MIRtvNextNumber("ZB", "A")
      EXT042.setLong("EXCLEF", NBNR as Long);
      //if (!query.read(EXT042)) {
      EXT042.set("EXCUNO", cuno);
      if(hie1!=''){
        EXT042.set("EXHIE1", hie1);
      }
      if(hie2!='') {
        EXT042.set("EXHIE2", hie2);
      }
      if(hie3!=''){
        EXT042.set("EXHIE3", hie3);
      }
      if(hie4!=''){
        EXT042.set("EXHIE4", hie4);
      }
      if(hie5!='') {
        EXT042.set("EXHIE5", hie5);
      }
      if(cfi5!='') {
        EXT042.set("EXCFI5", cfi5);
      }
      if(popn!='') {
        EXT042.set("EXPOPN", popn);
      }
      if(buar!='') {
        EXT042.set("EXBUAR", buar);
      }
      if(cfi1!='') {
        EXT042.set("EXCFI1", cfi1);
      }
      if(tx15!='') {
        EXT042.set("EXTX15", tx15);
      }
      if(adjt!='') {
        EXT042.setDouble("EXADJT", adjt);
      }
      if(fvdt!='') {
        EXT042.setInt("EXFVDT", fvdt);
      }
      if(lvdt!='') {
        EXT042.setInt("EXLVDT", lvdt);
      }
      if(cunm!='') {
        EXT042.set("EXCUNM", cunm);
      }
      EXT042.setInt("EXRGDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
      EXT042.setInt("EXRGTM", timeOfCreation.format(DateTimeFormatter.ofPattern("HHmmss")) as Integer);
      EXT042.setInt("EXLMDT", timeOfCreation.format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer);
      EXT042.setInt("EXCHNO", 1);
      EXT042.set("EXCHID", program.getUser());
      //Add Toc in EXT041
      query.insert(EXT042);
      //}
    }else{
      mi.error("Cette enregistrement est existant.");
      return;
    }
  }
  //Get Id with CRS615MI
  private executeCRS165MIRtvNextNumber(String NBTY, String NBID){
    def parameters = ["NBTY": NBTY, "NBID": NBID]
    Closure<?> handler = { Map<String, String> response ->
      NBNR = response.NBNR.trim()
      if (response.error != null) {
        return mi.error("Failed CRS165MI.RtvNextNumber: "+ response.errorMessage)
      }
    }
    miCaller.call("CRS165MI", "RtvNextNumber", parameters, handler)
  }
}
