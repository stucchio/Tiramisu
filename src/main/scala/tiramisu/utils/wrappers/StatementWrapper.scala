package com.chrisstucchio.tiramisu.utils.wrappers

trait StatementWrapper extends java.sql.Statement {
  protected def stmt: java.sql.Statement

  protected def methodWrap[T](f: =>T): T = f
  protected def wrapResultSet(rs: java.sql.ResultSet): java.sql.ResultSet = rs

  def addBatch(x$1: String): Unit = methodWrap { stmt.addBatch(x$1) }
  def cancel(): Unit = methodWrap { stmt.cancel() }
  def clearBatch(): Unit = methodWrap { stmt.clearBatch() }
  def clearWarnings(): Unit = methodWrap { stmt.clearWarnings() }
  def close(): Unit = methodWrap { stmt.close() }
  def closeOnCompletion(): Unit = methodWrap { stmt.closeOnCompletion() }
  def execute(x$1: String,x$2: Array[String]): Boolean = methodWrap { stmt.execute(x$1, x$2) }
  def execute(x$1: String,x$2: Array[Int]): Boolean = methodWrap { stmt.execute(x$1, x$2) }
  def execute(x$1: String,x$2: Int): Boolean = methodWrap { stmt.execute(x$1, x$2) }
  def execute(x$1: String): Boolean = methodWrap { stmt.execute(x$1) }
  def executeBatch(): Array[Int] = methodWrap { stmt.executeBatch() }
  def executeQuery(x$1: String): java.sql.ResultSet = wrapResultSet( methodWrap { stmt.executeQuery(x$1) })
  def executeUpdate(x$1: String,x$2: Array[String]): Int = methodWrap { stmt.executeUpdate(x$1,x$2) }
  def executeUpdate(x$1: String,x$2: Array[Int]): Int = methodWrap { stmt.executeUpdate(x$1,x$2) }
  def executeUpdate(x$1: String,x$2: Int): Int = methodWrap { stmt.executeUpdate(x$1,x$2) }
  def executeUpdate(x$1: String): Int = methodWrap { stmt.executeUpdate(x$1) }
  def getConnection(): java.sql.Connection = methodWrap { stmt.getConnection() }
  def getFetchDirection(): Int = methodWrap { stmt.getFetchDirection() }
  def getFetchSize(): Int = methodWrap { stmt.getFetchSize() }
  def getGeneratedKeys(): java.sql.ResultSet = wrapResultSet(methodWrap { stmt.getGeneratedKeys() })
  def getMaxFieldSize(): Int = methodWrap { stmt.getMaxFieldSize() }
  def getMaxRows(): Int = methodWrap { stmt.getMaxRows() }
  def getMoreResults(x$1: Int): Boolean = methodWrap { stmt.getMoreResults(x$1) }
  def getMoreResults(): Boolean = methodWrap { stmt.getMoreResults() }
  def getQueryTimeout(): Int = methodWrap { stmt.getQueryTimeout() }
  def getResultSet(): java.sql.ResultSet = wrapResultSet(methodWrap { stmt.getResultSet() })
  def getResultSetConcurrency(): Int = methodWrap { stmt.getResultSetConcurrency() }
  def getResultSetHoldability(): Int = methodWrap { stmt.getResultSetHoldability() }
  def getResultSetType(): Int = methodWrap { stmt.getResultSetType() }
  def getUpdateCount(): Int = methodWrap { stmt.getUpdateCount() }
  def getWarnings(): java.sql.SQLWarning = methodWrap { stmt.getWarnings() }
  def isCloseOnCompletion(): Boolean = methodWrap { stmt.isCloseOnCompletion() }
  def isClosed(): Boolean = methodWrap { stmt.isClosed() }
  def isPoolable(): Boolean = methodWrap { stmt.isPoolable() }
  def setCursorName(x$1: String): Unit = methodWrap { stmt.setCursorName(x$1) }
  def setEscapeProcessing(x$1: Boolean): Unit = methodWrap { stmt.setEscapeProcessing(x$1) }
  def setFetchDirection(x$1: Int): Unit = methodWrap { stmt.setFetchDirection(x$1) }
  def setFetchSize(x$1: Int): Unit = methodWrap { stmt.setFetchSize(x$1) }
  def setMaxFieldSize(x$1: Int): Unit = methodWrap { stmt.setMaxFieldSize(x$1) }
  def setMaxRows(x$1: Int): Unit = methodWrap { stmt.setMaxRows(x$1) }
  def setPoolable(x$1: Boolean): Unit = methodWrap { stmt.setPoolable(x$1) }
  def setQueryTimeout(x$1: Int): Unit = methodWrap { stmt.setQueryTimeout(x$1) }

  // Members declared in java.sql.Wrapper
  def isWrapperFor(x$1: Class[_]): Boolean = methodWrap { stmt.isWrapperFor(x$1) }
  def unwrap[T](x$1: Class[T]): T = methodWrap { stmt.unwrap(x$1) }
}
