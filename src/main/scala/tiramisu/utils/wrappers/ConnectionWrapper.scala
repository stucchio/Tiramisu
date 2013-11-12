package com.chrisstucchio.tiramisu.utils.wrappers

trait ConnectionWrapper extends java.sql.Connection {
  protected val conn: java.sql.Connection

  protected def methodWrap[T](f: =>T): T = f
  protected def wrapStatement(stmt: java.sql.Statement) = stmt
  protected def wrapPreparedStatement(stmt: java.sql.PreparedStatement) = stmt
  protected def wrapCallableStatement(stmt: java.sql.CallableStatement) = stmt

  def abort(x: java.util.concurrent.Executor): Unit = methodWrap { conn.abort(x) }
  def clearWarnings(): Unit = methodWrap { conn.clearWarnings() }
  def close(): Unit = methodWrap { conn.close() }
  def commit(): Unit = methodWrap { conn.commit() }
  def createArrayOf(x: String,y: Array[Object]): java.sql.Array = methodWrap { conn.createArrayOf(x,y) }
  def createBlob(): java.sql.Blob = methodWrap { conn.createBlob() }
  def createClob(): java.sql.Clob = methodWrap { conn.createClob() }
  def createNClob(): java.sql.NClob = methodWrap { conn.createNClob() }
  def createSQLXML(): java.sql.SQLXML = methodWrap { conn.createSQLXML() }
  def createStatement(x: Int,y: Int,z: Int): java.sql.Statement = wrapStatement(methodWrap { conn.createStatement(x,y,z) })
  def createStatement(x: Int,y: Int): java.sql.Statement = wrapStatement(methodWrap { conn.createStatement(x,y) })
  def createStatement(): java.sql.Statement = wrapStatement(methodWrap { conn.createStatement() })
  def createStruct(x: String,y: Array[Object]): java.sql.Struct = methodWrap { conn.createStruct(x,y) }
  def getAutoCommit(): Boolean = methodWrap { conn.getAutoCommit() }
  def getCatalog(): String = methodWrap { conn.getCatalog() }
  def getClientInfo(): java.util.Properties = methodWrap { conn.getClientInfo() }
  def getClientInfo(x: String): String = methodWrap { conn.getClientInfo(x) }
  def getHoldability(): Int = methodWrap { conn.getHoldability() }
  def getMetaData(): java.sql.DatabaseMetaData = methodWrap { conn.getMetaData() }
  def getNetworkTimeout(): Int = methodWrap { conn.getNetworkTimeout() }
  def getSchema(): String = methodWrap { conn.getSchema() }
  def getTransactionIsolation(): Int = methodWrap { conn.getTransactionIsolation() }
  def getTypeMap(): java.util.Map[String,Class[_]] = methodWrap { conn.getTypeMap() }
  def getWarnings(): java.sql.SQLWarning = methodWrap { conn.getWarnings() }
  def isClosed(): Boolean = methodWrap { conn.isClosed() }
  def isReadOnly(): Boolean = methodWrap { conn.isReadOnly() }
  def isValid(x: Int): Boolean = methodWrap { conn.isValid(x) }
  def nativeSQL(x: String): String = methodWrap { conn.nativeSQL(x) }
  def prepareCall(x: String,y: Int,z: Int,w: Int): java.sql.CallableStatement = wrapCallableStatement(methodWrap { conn.prepareCall(x,y,z,w) })
  def prepareCall(x: String,y: Int,z: Int): java.sql.CallableStatement = wrapCallableStatement(methodWrap { conn.prepareCall(x,y,z) })
  def prepareCall(x: String): java.sql.CallableStatement = wrapCallableStatement(methodWrap { conn.prepareCall(x) })
  def prepareStatement(x: String,y: Array[String]): java.sql.PreparedStatement = wrapPreparedStatement(methodWrap { conn.prepareStatement(x,y) })
  def prepareStatement(x: String,y: Array[Int]): java.sql.PreparedStatement = wrapPreparedStatement(methodWrap { conn.prepareStatement(x,y) })
  def prepareStatement(x: String,y: Int): java.sql.PreparedStatement = wrapPreparedStatement(methodWrap { conn.prepareStatement(x,y) })
  def prepareStatement(x: String,y: Int,z: Int,w: Int): java.sql.PreparedStatement = wrapPreparedStatement(methodWrap { conn.prepareStatement(x,y,z,w) })
  def prepareStatement(x: String,y: Int,z: Int): java.sql.PreparedStatement = wrapPreparedStatement(methodWrap { conn.prepareStatement(x,y,z) })
  def prepareStatement(x: String): java.sql.PreparedStatement = wrapPreparedStatement(methodWrap { conn.prepareStatement(x) })
  def releaseSavepoint(x: java.sql.Savepoint): Unit = methodWrap { conn.releaseSavepoint(x) }
  def rollback(x: java.sql.Savepoint): Unit = methodWrap { conn.rollback(x) }
  def rollback(): Unit = methodWrap { conn.rollback() }
  def setAutoCommit(x: Boolean): Unit = methodWrap { conn.setAutoCommit(x) }
  def setCatalog(x: String): Unit = methodWrap { conn.setCatalog(x) }
  def setClientInfo(x: java.util.Properties): Unit = methodWrap { conn.setClientInfo(x) }
  def setClientInfo(x: String,y: String): Unit = methodWrap { conn.setClientInfo(x,y) }
  def setHoldability(x: Int): Unit = methodWrap { conn.setHoldability(x) }
  def setNetworkTimeout(x: java.util.concurrent.Executor,y: Int): Unit = methodWrap { conn.setNetworkTimeout(x,y) }
  def setReadOnly(x: Boolean): Unit = methodWrap { conn.setReadOnly(x) }
  def setSavepoint(x: String): java.sql.Savepoint = methodWrap { conn.setSavepoint(x) }
  def setSavepoint(): java.sql.Savepoint = methodWrap { conn.setSavepoint() }
  def setSchema(x: String): Unit = methodWrap { conn.setSchema(x) }
  def setTransactionIsolation(x: Int): Unit = methodWrap { conn.setTransactionIsolation(x) }
  def setTypeMap(x: java.util.Map[String,Class[_]]): Unit = methodWrap { conn.setTypeMap(x) }
  def isWrapperFor(x: Class[_]): Boolean = methodWrap { conn.isWrapperFor(x) }
  def unwrap[T](x: Class[T]): T = methodWrap { conn.unwrap(x) }
}
