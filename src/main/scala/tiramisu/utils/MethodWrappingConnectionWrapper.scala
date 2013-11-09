package com.chrisstucchio.tiramisu.utils

trait MethodWrappingConnectionWrapper extends ConnectionWrapper {

  protected def methodWrap[T](f: =>T): T

  override def abort(x: java.util.concurrent.Executor): Unit = methodWrap { conn.abort(x) }
  override def clearWarnings(): Unit = methodWrap { conn.clearWarnings() }
  override def close(): Unit = methodWrap { conn.close() }
  override def commit(): Unit = methodWrap { conn.commit() }
  override def createArrayOf(x: String,y: Array[Object]): java.sql.Array = methodWrap { conn.createArrayOf(x,y) }
  override def createBlob(): java.sql.Blob = methodWrap { conn.createBlob() }
  override def createClob(): java.sql.Clob = methodWrap { conn.createClob() }
  override def createNClob(): java.sql.NClob = methodWrap { conn.createNClob() }
  override def createSQLXML(): java.sql.SQLXML = methodWrap { conn.createSQLXML() }
  override def createStatement(x: Int,y: Int,z: Int): java.sql.Statement = methodWrap { conn.createStatement(x,y,z) }
  override def createStatement(x: Int,y: Int): java.sql.Statement = methodWrap { conn.createStatement(x,y) }
  override def createStatement(): java.sql.Statement = methodWrap { conn.createStatement() }
  override def createStruct(x: String,y: Array[Object]): java.sql.Struct = methodWrap { conn.createStruct(x,y) }
  override def getAutoCommit(): Boolean = methodWrap { conn.getAutoCommit() }
  override def getCatalog(): String = methodWrap { conn.getCatalog() }
  override def getClientInfo(): java.util.Properties = methodWrap { conn.getClientInfo() }
  override def getClientInfo(x: String): String = methodWrap { conn.getClientInfo(x) }
  override def getHoldability(): Int = methodWrap { conn.getHoldability() }
  override def getMetaData(): java.sql.DatabaseMetaData = methodWrap { conn.getMetaData() }
  override def getNetworkTimeout(): Int = methodWrap { conn.getNetworkTimeout() }
  override def getSchema(): String = methodWrap { conn.getSchema() }
  override def getTransactionIsolation(): Int = methodWrap { conn.getTransactionIsolation() }
  override def getTypeMap(): java.util.Map[String,Class[_]] = methodWrap { conn.getTypeMap() }
  override def getWarnings(): java.sql.SQLWarning = methodWrap { conn.getWarnings() }
  override def isClosed(): Boolean = methodWrap { conn.isClosed() }
  override def isReadOnly(): Boolean = methodWrap { conn.isReadOnly() }
  override def isValid(x: Int): Boolean = methodWrap { conn.isValid(x) }
  override def nativeSQL(x: String): String = methodWrap { conn.nativeSQL(x) }
  override def prepareCall(x: String,y: Int,z: Int,w: Int): java.sql.CallableStatement = methodWrap { conn.prepareCall(x,y,z,w) }
  override def prepareCall(x: String,y: Int,z: Int): java.sql.CallableStatement = methodWrap { conn.prepareCall(x,y,z) }
  override def prepareCall(x: String): java.sql.CallableStatement = methodWrap { conn.prepareCall(x) }
  override def prepareStatement(x: String,y: Array[String]): java.sql.PreparedStatement = methodWrap { conn.prepareStatement(x,y) }
  override def prepareStatement(x: String,y: Array[Int]): java.sql.PreparedStatement = methodWrap { conn.prepareStatement(x,y) }
  override def prepareStatement(x: String,y: Int): java.sql.PreparedStatement = methodWrap { conn.prepareStatement(x,y) }
  override def prepareStatement(x: String,y: Int,z: Int,w: Int): java.sql.PreparedStatement = methodWrap { conn.prepareStatement(x,y,z,w) }
  override def prepareStatement(x: String,y: Int,z: Int): java.sql.PreparedStatement = methodWrap { conn.prepareStatement(x,y,z) }
  override def prepareStatement(x: String): java.sql.PreparedStatement = methodWrap { conn.prepareStatement(x) }
  override def releaseSavepoint(x: java.sql.Savepoint): Unit = methodWrap { conn.releaseSavepoint(x) }
  override def rollback(x: java.sql.Savepoint): Unit = methodWrap { conn.rollback(x) }
  override def rollback(): Unit = methodWrap { conn.rollback() }
  override def setAutoCommit(x: Boolean): Unit = methodWrap { conn.setAutoCommit(x) }
  override def setCatalog(x: String): Unit = methodWrap { conn.setCatalog(x) }
  override def setClientInfo(x: java.util.Properties): Unit = methodWrap { conn.setClientInfo(x) }
  override def setClientInfo(x: String,y: String): Unit = methodWrap { conn.setClientInfo(x,y) }
  override def setHoldability(x: Int): Unit = methodWrap { conn.setHoldability(x) }
  override def setNetworkTimeout(x: java.util.concurrent.Executor,y: Int): Unit = methodWrap { conn.setNetworkTimeout(x,y) }
  override def setReadOnly(x: Boolean): Unit = methodWrap { conn.setReadOnly(x) }
  override def setSavepoint(x: String): java.sql.Savepoint = methodWrap { conn.setSavepoint(x) }
  override def setSavepoint(): java.sql.Savepoint = methodWrap { conn.setSavepoint() }
  override def setSchema(x: String): Unit = methodWrap { conn.setSchema(x) }
  override def setTransactionIsolation(x: Int): Unit = methodWrap { conn.setTransactionIsolation(x) }
  override def setTypeMap(x: java.util.Map[String,Class[_]]): Unit = methodWrap { conn.setTypeMap(x) }
  override def isWrapperFor(x: Class[_]): Boolean = methodWrap { conn.isWrapperFor(x) }
  override def unwrap[T](x: Class[T]): T = methodWrap { conn.unwrap(x) }
}
