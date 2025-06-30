/**
 * README
 * This extension is used manually
 *
 * Name : EXT810MI.ClearCpyAll
 * Description : Clear and Copy XtendM3 table
 * Date         Changed By   Description
 * 20211103     YOUYVO       REAX99 - Clear and copy
 */

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

public class ClearCopy extends ExtendM3Transaction {

  private final MIAPI mi;
  private final LoggerAPI logger;
  private final ProgramAPI program
  private final DatabaseAPI database;
  private final SessionAPI session;
  private final TransactionAPI transaction
  private final MICallerAPI miCaller
  private Integer fromCompany
  private Integer toCompany
  private String file
  private Integer delete = 0
  private Integer copy = 0
  private String password

  public ClearCopy(MIAPI mi, DatabaseAPI database, ProgramAPI program, MICallerAPI miCaller) {
    this.mi = mi;
    this.database = database
    this.program = program
    this.miCaller = miCaller
  }

  public void main() {
    if (mi.in.get("FCON") == null) {
      fromCompany = (Integer)program.getLDAZD().CONO
    } else {
      fromCompany = mi.in.get("FCON")
    }
    toCompany = mi.in.get("TCON")

    file = ""
    if (mi.in.get("FILE") != null && mi.in.get("FILE") != "")
      file = mi.in.get("FILE")

    delete = 0
    if (mi.in.get("ZDEL") != null && mi.in.get("ZDEL") != "")
      delete = mi.in.get("ZDEL")

    copy = 0
    if (mi.in.get("ZCPY") != null && mi.in.get("ZCPY") != "")
      copy = mi.in.get("ZCPY")

    password = ""
    if (mi.in.get("ZPSW") != null && mi.in.get("ZPSW") != "")
      password = mi.in.get("ZPSW")

    /////////////////////////////////////////////////////////////////////
    //Clear to company
    /////////////////////////////////////////////////////////////////////
    if (password.trim() == "MDBEXTUSR") {
      if (delete == 1) {
        if (file.trim() == "" || file == "EXT010") {
          // Delete EXT010
          DBAction query2_EXT010 = database.table("EXT010").index("00").build()
          DBContainer EXT010_11 = query2_EXT010.getContainer()
          EXT010_11.set("EXCONO", toCompany)
          if (!query2_EXT010.readAllLock(EXT010_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT011") {
          // Delete EXT011
          DBAction query2_EXT011 = database.table("EXT011").index("00").build()
          DBContainer EXT011_11 = query2_EXT011.getContainer()
          EXT011_11.set("EXCONO", toCompany)
          if (!query2_EXT011.readAllLock(EXT011_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT012") {
          // Delete EXT012
          DBAction query2_EXT012 = database.table("EXT012").index("00").build()
          DBContainer EXT012_11 = query2_EXT012.getContainer()
          EXT012_11.set("EXCONO", toCompany)
          if (!query2_EXT012.readAllLock(EXT012_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT013") {
          // Delete EXT013
          DBAction query2_EXT013 = database.table("EXT013").index("00").build()
          DBContainer EXT013_11 = query2_EXT013.getContainer()
          EXT013_11.set("EXCONO", toCompany)
          if (!query2_EXT013.readAllLock(EXT013_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT014") {
          // Delete EXT014
          DBAction query2_EXT014 = database.table("EXT014").index("00").build()
          DBContainer EXT014_11 = query2_EXT014.getContainer()
          EXT014_11.set("EXCONO", toCompany)
          if (!query2_EXT014.readAllLock(EXT014_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT015") {
          // Delete EXT015
          DBAction query2_EXT015 = database.table("EXT015").index("00").build()
          DBContainer EXT015_11 = query2_EXT015.getContainer()
          EXT015_11.set("EXCONO", toCompany)
          if (!query2_EXT015.readAllLock(EXT015_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT020") {
          // Delete EXT020
          DBAction query2_EXT020 = database.table("EXT020").index("00").build()
          DBContainer EXT020_11 = query2_EXT020.getContainer()
          EXT020_11.set("EXCONO", toCompany)
          if (!query2_EXT020.readAllLock(EXT020_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT021") {
          // Delete EXT021
          DBAction query2_EXT021 = database.table("EXT021").index("00").build()
          DBContainer EXT021_11 = query2_EXT021.getContainer()
          EXT021_11.set("EXCONO", toCompany)
          if (!query2_EXT021.readAllLock(EXT021_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT030") {
          // Delete EXT030
          DBAction query2_EXT030 = database.table("EXT030").index("00").build()
          DBContainer EXT030_11 = query2_EXT030.getContainer()
          EXT030_11.set("EXCONO", toCompany)
          if (!query2_EXT030.readAllLock(EXT030_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT031") {
          // Delete EXT031
          DBAction query2_EXT031 = database.table("EXT031").index("00").build()
          DBContainer EXT031_11 = query2_EXT031.getContainer()
          EXT031_11.set("EXCONO", toCompany)
          if (!query2_EXT031.readAllLock(EXT031_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT032") {
          // Delete EXT032
          DBAction query2_EXT032 = database.table("EXT032").index("00").build()
          DBContainer EXT032_11 = query2_EXT032.getContainer()
          EXT032_11.set("EXCONO", toCompany)
          if (!query2_EXT032.readAllLock(EXT032_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT040") {
          // Delete EXT040
          DBAction query2_EXT040 = database.table("EXT040").index("00").build()
          DBContainer EXT040_11 = query2_EXT040.getContainer()
          EXT040_11.set("EXCONO", toCompany)
          if (!query2_EXT040.readAllLock(EXT040_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT041") {
          // Delete EXT041
          DBAction query2_EXT041 = database.table("EXT041").index("00").build()
          DBContainer EXT041_11 = query2_EXT041.getContainer()
          EXT041_11.set("EXCONO", toCompany)
          if (!query2_EXT041.readAllLock(EXT041_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT042") {
          // Delete EXT042
          DBAction query2_EXT042 = database.table("EXT042").index("00").build()
          DBContainer EXT042_11 = query2_EXT042.getContainer()
          EXT042_11.set("EXCONO", toCompany)
          if (!query2_EXT042.readAllLock(EXT042_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT050") {
          // Delete EXT050
          DBAction query2_EXT050 = database.table("EXT050").index("00").build()
          DBContainer EXT050_11 = query2_EXT050.getContainer()
          EXT050_11.set("EXCONO", toCompany)
          if (!query2_EXT050.readAllLock(EXT050_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT051") {
          // Delete EXT051
          DBAction query2_EXT051 = database.table("EXT051").index("00").build()
          DBContainer EXT051_11 = query2_EXT051.getContainer()
          EXT051_11.set("EXCONO", toCompany)
          if (!query2_EXT051.readAllLock(EXT051_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT052") {
          // Delete EXT052
          DBAction query2_EXT052 = database.table("EXT052").index("00").build()
          DBContainer EXT052_11 = query2_EXT052.getContainer()
          EXT052_11.set("EXCONO", toCompany)
          if (!query2_EXT052.readAllLock(EXT052_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT070") {
          // Delete EXT070
          DBAction query2_EXT070 = database.table("EXT070").index("00").build()
          DBContainer EXT070_11 = query2_EXT070.getContainer()
          EXT070_11.set("EXCONO", toCompany)
          if (!query2_EXT070.readAllLock(EXT070_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT075") {
          // Delete EXT075
          DBAction query2_EXT075 = database.table("EXT075").index("00").build()
          DBContainer EXT075_11 = query2_EXT075.getContainer()
          EXT075_11.set("EXCONO", toCompany)
          if (!query2_EXT075.readAllLock(EXT075_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT076") {
          // Delete EXT076
          DBAction query2_EXT076 = database.table("EXT076").index("00").build()
          DBContainer EXT076_11 = query2_EXT076.getContainer()
          EXT076_11.set("EXCONO", toCompany)
          if (!query2_EXT076.readAllLock(EXT076_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT077") {
          // Delete EXT077
          DBAction query2_EXT077 = database.table("EXT077").index("00").build()
          DBContainer EXT077_11 = query2_EXT077.getContainer()
          EXT077_11.set("EXCONO", toCompany)
          if (!query2_EXT077.readAllLock(EXT077_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT080") {
          // Delete EXT080
          DBAction query2_EXT080 = database.table("EXT080").index("00").build()
          DBContainer EXT080_11 = query2_EXT080.getContainer()
          EXT080_11.set("EXCONO", toCompany)
          if (!query2_EXT080.readAllLock(EXT080_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT081") {
          // Delete EXT081
          DBAction query2_EXT081 = database.table("EXT081").index("00").build()
          DBContainer EXT081_11 = query2_EXT081.getContainer()
          EXT081_11.set("EXCONO", toCompany)
          if (!query2_EXT081.readAllLock(EXT081_11, 1, updateCallBack)) {
          }
        }
        if (file.trim() == "" || file == "EXT236") {
          // Delete EXT236
          DBAction query2_EXT236 = database.table("EXT236").index("00").build()
          DBContainer EXT236_11 = query2_EXT236.getContainer()
          EXT236_11.set("EXCONO", toCompany)
          if (!query2_EXT236.readAllLock(EXT236_11, 1, updateCallBack)) {
          }
        }
      }
      /////////////////////////////////////////////////////////////////////
      //Copy from company -> to company
      /////////////////////////////////////////////////////////////////////
      if (copy == 1) {
        if (file.trim() == "" || file == "EXT010") {
          // Copy EXT010
          DBAction query_EXT010 = database.table("EXT010").index("00").build()
          DBContainer EXT010_1 = query_EXT010.getContainer()
          EXT010_1.set("EXCONO", fromCompany)
          if (!query_EXT010.readAll(EXT010_1, 1, outData_EXT010)) {
          }
        }
        if (file.trim() == "" || file == "EXT011") {
          // Copy EXT011
          DBAction query_EXT011 = database.table("EXT011").index("00").build()
          DBContainer EXT011_1 = query_EXT011.getContainer()
          EXT011_1.set("EXCONO", fromCompany)
          if (!query_EXT011.readAll(EXT011_1, 1, outData_EXT011)) {
          }
        }
        if (file.trim() == "" || file == "EXT012") {
          // Copy EXT012
          DBAction query_EXT012 = database.table("EXT012").index("00").build()
          DBContainer EXT012_1 = query_EXT012.getContainer()
          EXT012_1.set("EXCONO", fromCompany)
          if (!query_EXT012.readAll(EXT012_1, 1, outData_EXT012)) {
          }
        }
        if (file.trim() == "" || file == "EXT013") {
          // Copy EXT013
          DBAction query_EXT013 = database.table("EXT013").index("00").build()
          DBContainer EXT013_1 = query_EXT013.getContainer()
          EXT013_1.set("EXCONO", fromCompany)
          if (!query_EXT013.readAll(EXT013_1, 1, outData_EXT013)) {
          }
        }
        if (file.trim() == "" || file == "EXT014") {
          // Copy EXT014
          DBAction query_EXT014 = database.table("EXT014").index("00").build()
          DBContainer EXT014_1 = query_EXT014.getContainer()
          EXT014_1.set("EXCONO", fromCompany)
          if (!query_EXT014.readAll(EXT014_1, 1, outData_EXT014)) {
          }
        }
        if (file.trim() == "" || file == "EXT015") {
          // Copy EXT015
          DBAction query_EXT015 = database.table("EXT015").index("00").build()
          DBContainer EXT015_1 = query_EXT015.getContainer()
          EXT015_1.set("EXCONO", fromCompany)
          if (!query_EXT015.readAll(EXT015_1, 1, outData_EXT015)) {
          }
        }
        if (file.trim() == "" || file == "EXT020") {
          // Copy EXT020
          DBAction query_EXT020 = database.table("EXT020").index("00").build()
          DBContainer EXT020_1 = query_EXT020.getContainer()
          EXT020_1.set("EXCONO", fromCompany)
          if (!query_EXT020.readAll(EXT020_1, 1, outData_EXT020)) {
          }
        }
        if (file.trim() == "" || file == "EXT021") {
          // Copy EXT021
          DBAction query_EXT021 = database.table("EXT021").index("00").build()
          DBContainer EXT021_1 = query_EXT021.getContainer()
          EXT021_1.set("EXCONO", fromCompany)
          if (!query_EXT021.readAll(EXT021_1, 1, outData_EXT021)) {
          }
        }
        if (file.trim() == "" || file == "EXT030") {
          // Copy EXT030
          DBAction query_EXT030 = database.table("EXT030").index("00").build()
          DBContainer EXT030_1 = query_EXT030.getContainer()
          EXT030_1.set("EXCONO", fromCompany)
          if (!query_EXT030.readAll(EXT030_1, 1, outData_EXT030)) {
          }
        }
        if (file.trim() == "" || file == "EXT031") {
          // Copy EXT031
          DBAction query_EXT031 = database.table("EXT031").index("00").build()
          DBContainer EXT031_1 = query_EXT031.getContainer()
          EXT031_1.set("EXCONO", fromCompany)
          if (!query_EXT031.readAll(EXT031_1, 1, outData_EXT031)) {
          }
        }
        if (file.trim() == "" || file == "EXT032") {
          // Copy EXT032
          DBAction query_EXT032 = database.table("EXT032").index("00").build()
          DBContainer EXT032_1 = query_EXT032.getContainer()
          EXT032_1.set("EXCONO", fromCompany)
          if (!query_EXT032.readAll(EXT032_1, 1, outData_EXT032)) {
          }
        }
        if (file.trim() == "" || file == "EXT040") {
          // Copy EXT040
          DBAction query_EXT040 = database.table("EXT040").index("00").build()
          DBContainer EXT040_1 = query_EXT040.getContainer()
          EXT040_1.set("EXCONO", fromCompany)
          if (!query_EXT040.readAll(EXT040_1, 1, outData_EXT040)) {
          }
        }
        if (file.trim() == "" || file == "EXT041") {
          // Copy EXT041
          DBAction query_EXT041 = database.table("EXT041").index("00").build()
          DBContainer EXT041_1 = query_EXT041.getContainer()
          EXT041_1.set("EXCONO", fromCompany)
          if (!query_EXT041.readAll(EXT041_1, 1, outData_EXT041)) {
          }
        }
        if (file.trim() == "" || file == "EXT042") {
          // Copy EXT042
          DBAction query_EXT042 = database.table("EXT042").index("00").build()
          DBContainer EXT042_1 = query_EXT042.getContainer()
          EXT042_1.set("EXCONO", fromCompany)
          if (!query_EXT042.readAll(EXT042_1, 1, outData_EXT042)) {
          }
        }
        if (file.trim() == "" || file == "EXT050") {
          // Copy EXT050
          DBAction query_EXT050 = database.table("EXT050").index("00").build()
          DBContainer EXT050_1 = query_EXT050.getContainer()
          EXT050_1.set("EXCONO", fromCompany)
          if (!query_EXT050.readAll(EXT050_1, 1, outData_EXT050)) {
          }
        }
        if (file.trim() == "" || file == "EXT051") {
          // Copy EXT051
          DBAction query_EXT051 = database.table("EXT051").index("00").build()
          DBContainer EXT051_1 = query_EXT051.getContainer()
          EXT051_1.set("EXCONO", fromCompany)
          if (!query_EXT051.readAll(EXT051_1, 1, outData_EXT051)) {
          }
        }
        if (file.trim() == "" || file == "EXT052") {
          // Copy EXT052
          DBAction query_EXT052 = database.table("EXT052").index("00").build()
          DBContainer EXT052_1 = query_EXT052.getContainer()
          EXT052_1.set("EXCONO", fromCompany)
          if (!query_EXT052.readAll(EXT052_1, 1, outData_EXT052)) {
          }
        }
        if (file.trim() == "" || file == "EXT070") {
          // Copy EXT070
          DBAction query_EXT070 = database.table("EXT070").index("00").build()
          DBContainer EXT070_1 = query_EXT070.getContainer()
          EXT070_1.set("EXCONO", fromCompany)
          if (!query_EXT070.readAll(EXT070_1, 1, outData_EXT070)) {
          }
        }
        if (file.trim() == "" || file == "EXT075") {
          // Copy EXT075
          DBAction query_EXT075 = database.table("EXT075").index("00").build()
          DBContainer EXT075_1 = query_EXT075.getContainer()
          EXT075_1.set("EXCONO", fromCompany)
          if (!query_EXT075.readAll(EXT075_1, 1, outData_EXT075)) {
          }
        }
        if (file.trim() == "" || file == "EXT076") {
          // Copy EXT076
          DBAction query_EXT076 = database.table("EXT076").index("00").build()
          DBContainer EXT076_1 = query_EXT076.getContainer()
          EXT076_1.set("EXCONO", fromCompany)
          if (!query_EXT076.readAll(EXT076_1, 1, outData_EXT076)) {
          }
        }
        if (file.trim() == "" || file == "EXT077") {
          // Copy EXT077
          DBAction query_EXT077 = database.table("EXT077").index("00").build()
          DBContainer EXT077_1 = query_EXT077.getContainer()
          EXT077_1.set("EXCONO", fromCompany)
          if (!query_EXT077.readAll(EXT077_1, 1, outData_EXT077)) {
          }
        }
        if (file.trim() == "" || file == "EXT080") {
          // Copy EXT080
          DBAction query_EXT080 = database.table("EXT080").index("00").build()
          DBContainer EXT080_1 = query_EXT080.getContainer()
          EXT080_1.set("EXCONO", fromCompany)
          if (!query_EXT080.readAll(EXT080_1, 1, outData_EXT080)) {
          }
        }
        if (file.trim() == "" || file == "EXT081") {
          // Copy EXT081
          DBAction query_EXT081 = database.table("EXT081").index("00").build()
          DBContainer EXT081_1 = query_EXT081.getContainer()
          EXT081_1.set("EXCONO", fromCompany)
          if (!query_EXT081.readAll(EXT081_1, 1, outData_EXT081)) {
          }
        }
        if (file.trim() == "" || file == "EXT236") {
          // Copy EXT236
          DBAction query_EXT236 = database.table("EXT236").index("00").build()
          DBContainer EXT236_1 = query_EXT236.getContainer()
          EXT236_1.set("EXCONO", fromCompany)
          if (!query_EXT236.readAll(EXT236_1, 1, outData_EXT236)) {
          }
        }
      }
    } else {
      mi.error("Mot de passe invalide")
      return
    }
  }
  Closure<?> outData_EXT010 = { DBContainer EXT010 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT010").index("00").selection("EXITNO", "EXZCFE", "EXZCLV", "EXSUNO", "EXCUNO", "EXSUN1", "EXSUN3", "EXCSCD", "EXCSNO", "EXHIE0", "EXSPE1", "EXSPE2", "EXZGKY", "EXCFI1", "EXCFI4", "EXCFI5", "EXZSLT", "EXZPLT", "EXCSC1", "EXORTP", "EXITTY", "EXZONU", "EXDO01", "EXDO02", "EXDO03", "EXDO04", "EXDO05", "EXDO06", "EXDO07", "EXDO08", "EXDO09", "EXDO10", "EXDO11", "EXDO12", "EXDO13", "EXDO14", "EXDO15", "EXTXID", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT010_2 = query.getContainer()
    EXT010_2.set("EXCONO", fromCompany)
    EXT010_2.set("EXZCID",  EXT010.get("EXZCID"))
    if(query.read(EXT010_2)){
      EXT010_2.set("EXCONO", toCompany)
      if (!query.read(EXT010_2)) {
        query.insert(EXT010_2)
      }
    }
  }
  Closure<?> outData_EXT011 = { DBContainer EXT011 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT011").index("00").selection("EXTX40", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT011_2 = query.getContainer()
    EXT011_2.set("EXCONO", fromCompany)
    EXT011_2.set("EXZCTY",  EXT011.get("EXZCTY"))
    if(query.read(EXT011_2)){
      EXT011_2.set("EXCONO", toCompany)
      if (!query.read(EXT011_2)) {
        query.insert(EXT011_2)
      }
    }
  }
  Closure<?> outData_EXT012 = { DBContainer EXT012 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT012").index("00").selection("EXCONO", "EXZDES", "EXZCTY", "EXUSID", "EXZBLC", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT012_2 = query.getContainer()
    EXT012_2.set("EXCONO",fromCompany)
    EXT012_2.set("EXZCLV",  EXT012.get("EXZCLV"))
    if(query.read(EXT012_2)){
      EXT012_2.set("EXCONO", toCompany)
      if (!query.read(EXT012_2)) {
        query.insert(EXT012_2)
      }
    }
  }
  Closure<?> outData_EXT013 = { DBContainer EXT013 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT013").index("00").selection("EXZDES", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT013_2 = query.getContainer()
    EXT013_2.set("EXCONO", fromCompany)
    EXT013_2.set("EXZCFE",  EXT013.get("EXZCFE"))
    if(query.read(EXT013_2)){
      EXT013_2.set("EXCONO", toCompany)
      if (!query.read(EXT013_2)) {
        query.insert(EXT013_2)
      }
    }
  }
  Closure<?> outData_EXT014 = { DBContainer EXT014 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT014").index("00").selection("EXZMAR", "EXZGRA", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT014_2 = query.getContainer()
    EXT014_2.set("EXCONO", fromCompany)
    EXT014_2.set("EXCSCD", EXT014.get("EXCSCD"))
    EXT014_2.set("EXZMIR", EXT014.get("EXZMIR"))
    if(query.read(EXT014_2)){
      EXT014_2.set("EXCONO", toCompany)
      if (!query.read(EXT014_2)) {
        query.insert(EXT014_2)
      }
    }
  }
  Closure<?> outData_EXT015 = { DBContainer EXT015 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT015").index("00").selection("EXORNO", "EXPONR", "EXPOSX", "EXZCSL", "EXCUNO", "EXITNO", "EXORQT", "EXUNMS", "EXLNAM", "EXORST", "EXZCID", "EXZCTY", "EXZCFE", "EXZCLV", "EXSTAT", "EXDO01", "EXDO02", "EXDO03", "EXDO04", "EXDO05", "EXDO06", "EXDO07", "EXDO08", "EXDO09", "EXDO10", "EXDO11", "EXDO12", "EXDO13", "EXDO14", "EXDO15", "EXTXI1", "EXZCTR", "EXTXI2", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT015_2 = query.getContainer()
    EXT015_2.set("EXCONO", fromCompany)
    EXT015_2.set("EXORNO",  EXT015.get("EXORNO"))
    EXT015_2.set("EXPONR",  EXT015.get("EXPONR"))
    EXT015_2.set("EXPOSX",  EXT015.get("EXPOSX"))
    EXT015_2.set("EXZCSL",  EXT015.get("EXZCSL"))
    if(query.read(EXT015_2)){
      EXT015_2.set("EXCONO", toCompany)
      if (!query.read(EXT015_2)) {
        query.insert(EXT015_2)
      }
    }
  }
  Closure<?> outData_EXT020 = { DBContainer EXT020 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT020").index("00").selection("EXCONO", "EXTYPE", "EXFPVT", "EXTPVT", "EXWHTY", "EXTX40", "EXCOPE", "EXSCAT", "EXLSCA", "EXCUNO", "EXCUNM", "EXCDAN", "EXOTYG", "EXSTAT", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT020_2 = query.getContainer()
    EXT020_2.set("EXCONO", fromCompany)
    EXT020_2.set("EXTYPE",EXT020.get("EXTYPE"))
    if(query.read(EXT020_2)){
      EXT020_2.set("EXCONO", toCompany)
      if (!query.read(EXT020_2)) {
        query.insert(EXT020_2)
      }
    }
  }
  Closure<?> outData_EXT021 = { DBContainer EXT021 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT021").index("00").selection("EXCONO", "EXTYPE", "EXNPVT", "EXBFRS", "EXFDAT", "EXTDAT", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT021_2 = query.getContainer()
    EXT021_2.set("EXCONO", fromCompany)
    EXT021_2.set("EXTYPE", EXT021.get("EXTYPE"))
    EXT021_2.set("EXNPVT", EXT021.get("EXNPVT"))
    EXT021_2.set("EXBFRS", EXT021.get("EXBFRS"))
    EXT021_2.set("EXFDAT", EXT021.get("EXFDAT"));
    EXT021_2.set("EXTDAT", EXT021.get("EXTDAT"));
    if(query.read(EXT021_2)){
      EXT021_2.set("EXCONO", toCompany)
      if (!query.read(EXT021_2)) {
        query.insert(EXT021_2)
      }
    }
  }
  Closure<?> outData_EXT030 = { DBContainer EXT030 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT030").index("00").selection("EXZCOM", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT030_2 = query.getContainer()
    EXT030_2.set("EXCONO", EXT030.get("EXCONO"))
    EXT030_2.set("EXZIPS", EXT030.get("EXZIPS"))
    EXT030_2.set("EXZIPP", EXT030.get("EXZIPP"))
    if(query.read(EXT030_2)){
      EXT030_2.set("EXCONO", toCompany)
      if (!query.read(EXT030_2)) {
        query.insert(EXT030_2)
      }
    }
  }
  Closure<?> outData_EXT031 = { DBContainer EXT031 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT031").index("00").selection("EXZIPL", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID", "EXCONM").build()
    DBContainer EXT031_2 = query.getContainer()
    EXT031_2.set("EXCONO", fromCompany)
    EXT031_2.set("EXTEDL", EXT031.get("EXTEDL"))
    EXT031_2.set("EXZPLA", EXT031.get("EXZPLA"))
    if(query.read(EXT031_2)){
      EXT031_2.set("EXCONO", toCompany)
      if (!query.read(EXT031_2)) {
        query.insert(EXT031_2)
      }
    }
  }
  Closure<?> outData_EXT032 = { DBContainer EXT032 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT032").index("00").selection("EXCONO","EXSUNO","EXAGNB","EXZIPP").build()
    DBContainer EXT032_2 = query.getContainer()
    EXT032_2.set("EXCONO", fromCompany)
    EXT032_2.set("EXSUNO", EXT032.get("EXSUNO"))
    EXT032_2.set("EXAGNB", EXT032.get("EXAGNB"))
    EXT032_2.set("EXZIPP", EXT032.get("EXZIPP"))
    if(query.read(EXT032_2)){
      EXT032_2.set("EXCONO", toCompany)
      if (!query.read(EXT032_2)) {
        query.insert(EXT032_2)
      }
    }
  }
  Closure<?> outData_EXT040 = { DBContainer EXT040 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT040").index("00").selection("EXCONO", "EXCUNO", "EXCUNM", "EXASCD", "EXMARG","EXFDAT" ,"EXTDAT" , "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT040_2 = query.getContainer()
    EXT040_2.set("EXCONO", fromCompany);
    EXT040_2.set("EXCUNO", EXT040.get("EXCUNO"));
    EXT040_2.set("EXASCD", EXT040.get("EXASCD"));
    EXT040_2.set("EXFDAT", EXT040.get("EXFDAT"));
    if(query.read(EXT040_2)){
      EXT040_2.set("EXCONO", toCompany)
      if (!query.read(EXT040_2)) {
        query.insert(EXT040_2)
      }
    }
  }
  Closure<?> outData_EXT041 = { DBContainer EXT041 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT041").index("00").selection("EXCONO", "EXTYPE", "EXCUNO", "EXCUNM", "EXBOBE", "EXBOHE", "EXBOBM","EXBOHM", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT041_2 = query.getContainer()
    EXT041_2.set("EXCONO", fromCompany);
    EXT041_2.set("EXCUNO", EXT041.get("EXCUNO"));
    EXT041_2.set("EXTYPE", EXT041.get("EXTYPE"));
    if(query.read(EXT041_2)){
      EXT041_2.set("EXCONO", toCompany)
      if (!query.read(EXT041_2)) {
        query.insert(EXT041_2)
      }
    }
  }
  Closure<?> outData_EXT042 = { DBContainer EXT042 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT042").index("00").selection("EXCONO","EXCLEF","EXCUNO","EXCUNM","EXHIE1","EXHIE2","EXHIE3","EXHIE4","EXHIE5",
      "EXCFI5","EXPOPN","EXBUAR","EXCFI1","EXTX15","EXADJT","EXFVDT","EXLVDT").build()
    DBContainer EXT042_2 = query.getContainer()
    EXT042_2.set("EXCONO", fromCompany)
    EXT042_2.set("EXCLEF",  EXT042.get("EXCLEF"))
    if(query.read(EXT042_2)){
      EXT042_2.set("EXCONO", toCompany)
      if (!query.read(EXT042_2)) {
        query.insert(EXT042_2)
      }
    }
  }
  Closure<?> outData_EXT050 = { DBContainer EXT050 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT050").index("00").selection("EXCONO", "EXASCD", "EXDAT1", "EXCUNO", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT050_2 = query.getContainer()
    EXT050_2.set("EXCONO", fromCompany);
    EXT050_2.set("EXCUNO", EXT050.get("EXCUNO"));
    EXT050_2.set("EXASCD", EXT050.get("EXASCD"));
    EXT050_2.set("EXDAT1", EXT050.get("EXDAT1"));
    if(query.read(EXT050_2)){
      EXT050_2.set("EXCONO", toCompany)
      if (!query.read(EXT050_2)) {
        query.insert(EXT050_2)
      }
    }
  }
  Closure<?> outData_EXT051 = { DBContainer EXT051 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT051").index("00").selection("EXCONO", "EXASCD", "EXCUNO", "EXDAT1", "EXTYPE", "EXCHB1", "EXDATA", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID", "EXDESI").build()
    DBContainer EXT051_2 = query.getContainer()
    EXT051_2.set("EXCONO", fromCompany);
    EXT051_2.set("EXCUNO", EXT051.get("EXCUNO"));
    EXT051_2.set("EXASCD", EXT051.get("EXASCD"));
    EXT051_2.set("EXDAT1", EXT051.get("EXDAT1"));
    EXT051_2.set("EXTYPE", EXT051.get("EXTYPE"))
    EXT051_2.set("EXDATA", EXT051.get("EXDATA"))
    if(query.read(EXT051_2)){
      EXT051_2.set("EXCONO", toCompany)
      if (!query.read(EXT051_2)) {
        query.insert(EXT051_2)
      }
    }
  }
  Closure<?> outData_EXT052 = { DBContainer EXT052 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT052").index("00").selection("EXCONO", "EXASCD", "EXCUNO", "EXFDAT", "EXITNO", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT052_2 = query.getContainer()
    EXT052_2.set("EXCONO", fromCompany);
    EXT052_2.set("EXASCD", EXT052.get("EXASCD"));
    EXT052_2.set("EXCUNO", EXT052.get("EXCUNO"));
    EXT052_2.set("EXFDAT", EXT052.get("EXFDAT"));
    EXT052_2.set("EXITNO", EXT052.get("EXITNO"))
    if(query.read(EXT052_2)){
      EXT052_2.set("EXCONO", toCompany)
      if (!query.read(EXT052_2)) {
        query.insert(EXT052_2)
      }
    }
  }
  Closure<?> outData_EXT070 = { DBContainer EXT070 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT070").index("00").selection("EXCONO", "EXCUNO", "EXITNO", "EXSAAM", "EXSAAC", "EXTAUX","EXFLAG", "EXSAAT","EXIVQT").build()
    DBContainer EXT070_2 = query.getContainer()
    EXT070_2.set("EXCONO", fromCompany)
    EXT070_2.set("EXCUNO", EXT070.get("EXCUNO"))
    EXT070_2.set("EXITNO", EXT070.get("EXITNO"))
    if(query.read(EXT070_2)){
      EXT070_2.set("EXCONO", toCompany)
      if (!query.read(EXT070_2)) {
        query.insert(EXT070_2)
      }
    }
  }
  Closure<?> outData_EXT075 = { DBContainer EXT075 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT075").index("00").selection("EXPRRF","EXCUCD","EXCUNO","EXFVDT","EXVFDT","EXITNO","EXOBV1","EXOBV2","EXOBV3","EXITTY","EXMMO2","EXHIE3","EXHIE2","EXPOPN","EXMOY4","EXSAP4",
      "EXMOY3","EXSAP3","EXTUT2","EXTUT1","EXTUM2","EXTUM1","EXMOY2","EXSAP2","EXMOY1","EXSAP1","EXMOY0","EXREM0","EXSAP0","EXMFIN","EXSAPR","EXZUPA",
      "EXMDIV","EXMCUN","EXMOBJ","EXNEPR","EXPUPR","EXFLAG","EXFPSY","EXZIPL","EXTEDL", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID","EXSUNO","EXAGNB").build()
    DBContainer EXT075_2 = query.getContainer()
    EXT075_2.set("EXCONO", fromCompany)
    EXT075_2.set("EXPRRF", EXT075.get("EXPRRF"))
    EXT075_2.set("EXCUCD", EXT075.get("EXCUCD"))
    EXT075_2.set("EXCUNO", EXT075.get("EXCUNO"))
    EXT075_2.set("EXFVDT", EXT075.get("EXFVDT"))
    EXT075_2.set("EXITNO", EXT075.get("EXITNO"))
    EXT075_2.set("EXOBV1", EXT075.get("EXOBV1"))
    EXT075_2.set("EXOBV2", EXT075.get("EXOBV2"))
    EXT075_2.set("EXOBV3", EXT075.get("EXOBV3"))
    EXT075_2.set("EXVFDT", EXT075.get("EXVFDT"))
    if(query.read(EXT075_2)){
      EXT075_2.set("EXCONO", toCompany)
      if (!query.read(EXT075_2)) {
        query.insert(EXT075_2)
      }
    }
  }
  Closure<?> outData_EXT076 = { DBContainer EXT076 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT076").index("00").selection("EXCONO", "EXPRRF", "EXCUCD", "EXCUNO", "EXFVDT", "EXPOPN", "EXITTY", "EXHIE2", "EXHIE3", "EXASCD", "EXMMO2", "EXCPTL", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT076_2 = query.getContainer()
    EXT076_2.set("EXCONO", fromCompany)
    EXT076_2.set("EXPRRF",  EXT076.get("EXPRRF"))
    EXT076_2.set("EXCUCD",  EXT076.get("EXCUCD"))
    EXT076_2.set("EXCUNO",  EXT076.get("EXCUNO"))
    EXT076_2.set("EXFVDT",  EXT076.get("EXFVDT"))
    EXT076_2.set("EXPOPN",  EXT076.get("EXPOPN"))
    EXT076_2.set("EXITTY",  EXT076.get("EXITTY"))
    EXT076_2.set("EXHIE2",  EXT076.get("EXHIE2"))
    EXT076_2.set("EXHIE3",  EXT076.get("EXHIE3"))
    if(query.read(EXT076_2)){
      EXT076_2.set("EXCONO", toCompany)
      if (!query.read(EXT076_2)) {
        query.insert(EXT076_2)
      }
    }
  }
  Closure<?> outData_EXT077 = { DBContainer EXT077 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT077").index("00").selection("EXCONO", "EXPRRF", "EXCUCD", "EXCUNO", "EXFVDT", "EXPOPN", "EXITTY", "EXHIE2", "EXHIE3", "EXASCD", "EXMMO2", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT077_2 = query.getContainer()
    EXT077_2.set("EXCONO", fromCompany)
    EXT077_2.set("EXPRRF",  EXT077.get("EXPRRF"))
    EXT077_2.set("EXCUCD",  EXT077.get("EXCUCD"))
    EXT077_2.set("EXCUNO",  EXT077.get("EXCUNO"))
    EXT077_2.set("EXFVDT",  EXT077.get("EXFVDT"))
    EXT077_2.set("EXPOPN",  EXT077.get("EXPOPN"))
    EXT077_2.set("EXITTY",  EXT077.get("EXITTY"))
    EXT077_2.set("EXHIE2",  EXT077.get("EXHIE2"))
    EXT077_2.set("EXHIE3",  EXT077.get("EXHIE3"))
    if(query.read(EXT077_2)){
      EXT077_2.set("EXCONO", toCompany)
      if (!query.read(EXT077_2)) {
        query.insert(EXT077_2)
      }
    }
  }
  Closure<?> outData_EXT080 = { DBContainer EXT080 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT080").index("00").selection("EXPRRF", "EXCUCD", "EXCUNO", "EXFVDT", "EXLVDT", "EXZUPA", "EXZIPL", "EXPIDE", "EXZUPD", "EXSTAT", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT080_2 = query.getContainer()
    EXT080_2.set("EXCONO", fromCompany);
    EXT080_2.set("EXPRRF", EXT080.get("EXPRRF"));
    EXT080_2.set("EXCUCD", EXT080.get("EXCUCD"));
    EXT080_2.set("EXCUNO", EXT080.get("EXCUNO"));
    EXT080_2.set("EXFVDT", EXT080.get("EXFVDT"));
    if(query.read(EXT080_2)){
      EXT080_2.set("EXCONO", toCompany)
      if (!query.read(EXT080_2)) {
        query.insert(EXT080_2)
      }
    }
  }
  Closure<?> outData_EXT081 = { DBContainer EXT081 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT081").index("00").selection("EXPRRF", "EXCUCD", "EXCUNO", "EXFVDT", "EXASCD", "EXFDAT", "EXSTAT", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT081_2 = query.getContainer()
    EXT081_2.set("EXCONO", fromCompany);
    EXT081_2.set("EXPRRF", EXT081.get("EXPRRF"));
    EXT081_2.set("EXCUCD", EXT081.get("EXCUCD"));
    EXT081_2.set("EXCUNO", EXT081.get("EXCUNO"));
    EXT081_2.set("EXFVDT", EXT081.get("EXFVDT"));
    EXT081_2.set("EXASCD", EXT081.get("EXASCD"));
    EXT081_2.set("EXFDAT", EXT081.get("EXFDAT"));
    if(query.read(EXT081_2)){
      EXT081_2.set("EXCONO", toCompany)
      if (!query.read(EXT081_2)) {
        query.insert(EXT081_2)
      }
    }
  }
  Closure<?> outData_EXT236 = { DBContainer EXT236 ->
    LocalDateTime timeOfCreation = LocalDateTime.now()
    DBAction query = database.table("EXT236").index("00").selection("EXPUNO", "EXITNO", "EXBREF", "EXITM8", "EXPROD", "EXEXPI", "EXMFDT", "EXRGDT", "EXRGTM", "EXLMDT", "EXCHNO", "EXCHID").build()
    DBContainer EXT236_2 = query.getContainer()
    EXT236_2.set("EXCONO", fromCompany);
    EXT236_2.set("EXPRRF", EXT236.get("EXPRRF"));
    EXT236_2.set("EXCUCD", EXT236.get("EXCUCD"));
    EXT236_2.set("EXCUNO", EXT236.get("EXCUNO"));
    EXT236_2.set("EXFVDT", EXT236.get("EXFVDT"));
    EXT236_2.set("EXASCD", EXT236.get("EXASCD"));
    EXT236_2.set("EXFDAT", EXT236.get("EXFDAT"));
    if(query.read(EXT236_2)){
      EXT236_2.set("EXCONO", toCompany)
      if (!query.read(EXT236_2)) {
        query.insert(EXT236_2)
      }
    }
  }
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    lockedResult.delete()
  }
}
