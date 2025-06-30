/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT031MI.DelIncotermPlac
 * Description : The DelIncotermPlac transaction copy records to the EXT031 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 * 20211213     CDUV         Check added, algorithm changed
 * 20241205     YYOU         Modify rechercheVente (remplace EXZIPP by EXZIPS)
 */
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class DelIncotermPlac extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final ProgramAPI program
  private Integer currentCompany
  public DelIncotermPlac(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.program = program
  }

  public void main() {

    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Check incoterm
    if(mi.in.get("TEDL") == null || mi.in.get("TEDL") == ""){
      mi.error("Incoterm est obligatoire")
      return
    }
    // Check place
    if(mi.in.get("ZPLA") == null || mi.in.get("ZPLA") == ""){
      mi.error("Lieu est obligatoire")
      return
    }
    String iTEDL = mi.in.get("TEDL")
    String iZPLA = mi.in.get("ZPLA")
    String iZIPL = iTEDL.trim()+iZPLA.trim()

    // Controle Présence Incoterm Vente EXT031
    /*if(rechercheVente(iZIPL)){
        mi.error("Code Incoterm Vente utilisé en EXT031")
        return
    }*/
    // Controle Présence Incoterm achat EXT032
    if(rechercheAchat(iZIPL)){
      mi.error("Code Incoterm Achat utilisé dans un contrat d'achat")
      return
    }
    // Controle Présence Incoterm achat EXT033
    if(rechercheAchatWHLO(iZIPL)){
      mi.error("Code Incoterm Achat utilisé EXT033")
      return
    }
    DBAction query = database.table("EXT031").index("00").build()
    DBContainer EXT031 = query.getContainer()
    EXT031.set("EXCONO", currentCompany)
    EXT031.set("EXTEDL", mi.in.get("TEDL"))
    EXT031.set("EXZPLA", mi.in.get("ZPLA"))
    if(!query.readLock(EXT031, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }

    ExpressionFactory expression = database.getExpressionFactory("EXT030")
    expression = expression.eq("EXZIPS", iZIPL).or(expression.eq("EXZIPP", iZIPL))
    DBAction query_EXT030 = database.table("EXT030").index("00").matching(expression).selection("EXZIPS", "EXZIPP").build()
    DBContainer EXT030 = query_EXT030.getContainer()
    EXT030.set("EXCONO", mi.in.get("CONO"))
    if(!query_EXT030.readAllLock(EXT030,1, updateCallBack_EXT030)){
      mi.error("L'enregistrement n'existe pas")
      return
    }

    DBAction query_EXT033 = database.table("EXT033").index("20").build()
    DBContainer EXT033 = query_EXT033.getContainer()
    EXT033.set("EXCONO", mi.in.get("CONO"))
    EXT033.set("EXZIPP", mi.in.get("ZIPP"))
    if(!query_EXT033.readAllLock(EXT033,2, updateCallBack_EXT033)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Delete EXT031
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  // Delete EXT030
  Closure<?> updateCallBack_EXT030 = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  // Delete EXT033
  Closure<?> updateCallBack_EXT033 = { LockedResult lockedResult ->
    lockedResult.delete()
  }
  /**
   * Boolean Method to check if the Incoterm sale connected to OIS831
   * if true deletion not possible
   */
  private boolean rechercheVente(String iZIPS){
    boolean retour=false
    /*
    ExpressionFactory expression = database.getExpressionFactory("OPRMTX")
      expression = expression.like("DXOBV1", iZIPS)
      expression = expression.or(expression.like("DXOBV2", iZIPS))
      expression = expression.or(expression.like("DXOBV3", iZIPS))
      expression = expression.or(expression.like("DXOBV4", iZIPS))
      expression = expression.or(expression.like("DXOBV5", iZIPS))
     def tablePrioritVente = database.table("OPRMTX").index("00").matching(expression).selection("DXOBV1","DXOBV2","DXOBV3","DXOBV4","DXOBV5").build()
     def OPRMTX = tablePrioritVente.getContainer()
      OPRMTX.set("DXCONO", currentCompany)
      tablePrioritVente.readAll(OPRMTX,1,{DBContainer record ->
      */
    def tableAchat = database.table("EXT031").index("10").build()
    def EXT031 = tableAchat.getContainer()
    EXT031.set("EXCONO", currentCompany)
    EXT031.set("EXZIPS", iZIPS)
    tableAchat.readAll(EXT031,2,{DBContainer record ->
      retour=true
      return retour
    })
    return retour
  }
  /**
   * Boolean Method to check if the Incoterm purchase connected to extend table EXT032 (extend to purchase agreement)
   * if true deletion not possible
   */
  private boolean rechercheAchat(String iZIPP){
    boolean retour=false
    /*
    ExpressionFactory expression = database.getExpressionFactory("EXT032")
      expression = expression.like("EXZIPP", iZIPP)
     def tableAchat = database.table("EXT032").index("00").matching(expression).selection("EXZIPP").build()
     def EXT032 = tableAchat.getContainer()
      EXT032.set("EXCONO", currentCompany )
      tableAchat.readAll(EXT032,1,{DBContainer record ->
      */
    def tableAchat = database.table("EXT032").index("30").build()
    def EXT032 = tableAchat.getContainer()
    EXT032.set("EXCONO", currentCompany )
    EXT032.set("EXZIPP", iZIPP)
    tableAchat.readAll(EXT032,2,{DBContainer record ->
      retour=true
      return retour
    })
    return retour
  }
  /**
   * Boolean Method to check if the Incoterm purchase connected to extend table EXT033
   * if true deletion not possible
   */
  private boolean rechercheAchatWHLO(String iZIPP){
    boolean retour=false
    def tableAchat = database.table("EXT033").index("20").build()
    def EXT033 = tableAchat.getContainer()
    EXT033.set("EXCONO", currentCompany)
    EXT033.set("EXZIPP", iZIPP)
    tableAchat.readAll(EXT033,2,{DBContainer record ->
      retour=true
      return retour
    })
    return retour
  }
}
