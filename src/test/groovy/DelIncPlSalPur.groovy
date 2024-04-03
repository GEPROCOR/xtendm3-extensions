/**
 * README
 * This extension is used by Mashup
 *
 * Name : EXT030MI.DelIncPlSalPur
 * Description : The DelIncPlSalPur transaction delete records to the EXT030 table.
 * Date         Changed By   Description
 * 20210510     CDUV         CMDX13 - Gestion compatibilité d'incoterm
 * 20211213     CDUV         Check added, algorithm changed
 */

import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

public class DelIncPlSalPur extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final LoggerAPI logger
  private final ProgramAPI program
  private Integer currentCompany

  public DelIncPlSalPur(MIAPI mi, DatabaseAPI database, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi
    this.database = database
    this.logger = logger
    this.program = program
  }

  public void main() {
    if (mi.in.get("CONO") == null) {
      currentCompany = (Integer)program.getLDAZD().CONO
    } else {
      currentCompany = mi.in.get("CONO")
    }
    // Check incoterm place sale
    if(mi.in.get("ZIPS") == null || mi.in.get("ZIPS") == ""){
      mi.error("Incoterm lieu vente est obligatoire")
      return
    }

    // Check incoterm place purchase
    if(mi.in.get("ZIPP") == null || mi.in.get("ZIPP") == ""){
      mi.error("Incoterm lieu achat est obligatoire")
      return
    }
    // Controle Présence Incoterm Vente EXT031
    String iZIPS = mi.in.get("ZIPS")
    if(rechercheVente(iZIPS)){
      mi.error("Code Incoterm Vente utilisé en EXT031")
      return
    }
    // Controle Présence Incoterm achat EXT032
    String iZIPP = mi.in.get("ZIPP")
    if(rechercheAchat(iZIPP)){
      mi.error("Code Incoterm Achat utilisé dans un contrat d'achat")
      return
    }
    // Controle Présence Incoterm achat EXT033
    if(rechercheAchatWHLO(iZIPP)){
      mi.error("Code Incoterm Achat utilisé EXT033")
      return
    }

    DBAction query = database.table("EXT030").index("00").build()
    DBContainer EXT030 = query.getContainer()
    EXT030.set("EXCONO", currentCompany)
    EXT030.set("EXZIPS", mi.in.get("ZIPS"))
    EXT030.set("EXZIPP", mi.in.get("ZIPP"))
    if(!query.readLock(EXT030, updateCallBack)){
      mi.error("L'enregistrement n'existe pas")
      return
    }
  }
  // Delete EXT030
  Closure<?> updateCallBack = { LockedResult lockedResult ->
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
     ExpressionFactory expression = database.getExpressionFactory("EXT031")
     expression = expression.like("EXZIPP", iZIPP)
     def tableAchat = database.table("EXT031").index("00").matching(expression).selection("EXZIPP").build()
     def EXT031 = tableAchat.getContainer()
      EXT031.set("EXCONO", currentCompany)
      tableAchat.readAll(EXT031,1,{DBContainer record ->
     */
    def tableAchat = database.table("EXT031").index("10").build()
    def EXT031 = tableAchat.getContainer()
    EXT031.set("EXCONO", currentCompany)
    EXT031.set("EXZIPP", iZIPS)
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
