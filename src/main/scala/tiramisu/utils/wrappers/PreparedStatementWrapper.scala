package com.chrisstucchio.tiramisu.utils.wrappers

trait PreparedStatementWrapper extends StatementWrapper with java.sql.PreparedStatement {
  protected val stmt: java.sql.PreparedStatement

  def addBatch(): Unit = methodWrap { stmt.addBatch() }
  def clearParameters(): Unit = methodWrap { stmt.clearParameters() }
  def execute(): Boolean = methodWrap { stmt.execute() }
  def executeQuery(): java.sql.ResultSet = methodWrap { stmt.executeQuery() }
  def executeUpdate(): Int = methodWrap { stmt.executeUpdate() }
  def getMetaData(): java.sql.ResultSetMetaData = methodWrap { stmt.getMetaData() }
  def getParameterMetaData(): java.sql.ParameterMetaData = methodWrap { stmt.getParameterMetaData() }
  def setArray(x$1: Int,x$2: java.sql.Array): Unit = methodWrap { stmt.setArray(x$1,x$2) }
  def setAsciiStream(x$1: Int,x$2: java.io.InputStream): Unit = methodWrap { stmt.setAsciiStream(x$1,x$2) }
  def setAsciiStream(x$1: Int,x$2: java.io.InputStream,x$3: Long): Unit = methodWrap { stmt.setAsciiStream(x$1,x$2, x$3) }
  def setAsciiStream(x$1: Int,x$2: java.io.InputStream,x$3: Int): Unit = methodWrap { stmt.setAsciiStream(x$1,x$2, x$3) }
  def setBigDecimal(x$1: Int,x$2: java.math.BigDecimal): Unit = methodWrap { stmt.setBigDecimal(x$1,x$2) }
  def setBinaryStream(x$1: Int,x$2: java.io.InputStream): Unit = methodWrap { stmt.setBinaryStream(x$1,x$2) }
  def setBinaryStream(x$1: Int,x$2: java.io.InputStream,x$3: Long): Unit = methodWrap { stmt.setBinaryStream(x$1,x$2,x$3) }
  def setBinaryStream(x$1: Int,x$2: java.io.InputStream,x$3: Int): Unit = methodWrap { stmt.setBinaryStream(x$1,x$2,x$3) }
  def setBlob(x$1: Int,x$2: java.io.InputStream): Unit = methodWrap { stmt.setBlob(x$1,x$2) }
  def setBlob(x$1: Int,x$2: java.io.InputStream,x$3: Long): Unit = methodWrap { stmt.setBlob(x$1,x$2, x$3) }
  def setBlob(x$1: Int,x$2: java.sql.Blob): Unit = methodWrap { stmt.setBlob(x$1,x$2) }
  def setBoolean(x$1: Int,x$2: Boolean): Unit = methodWrap { stmt.setBoolean(x$1,x$2) }
  def setByte(x$1: Int,x$2: Byte): Unit = methodWrap { stmt.setByte(x$1,x$2) }
  def setBytes(x$1: Int,x$2: Array[Byte]): Unit = methodWrap { stmt.setBytes(x$1,x$2) }
  def setCharacterStream(x$1: Int,x$2: java.io.Reader): Unit = methodWrap { stmt.setCharacterStream(x$1,x$2) }
  def setCharacterStream(x$1: Int,x$2: java.io.Reader,x$3: Long): Unit = methodWrap { stmt.setCharacterStream(x$1,x$2,x$3) }
  def setCharacterStream(x$1: Int,x$2: java.io.Reader,x$3: Int): Unit = methodWrap { stmt.setCharacterStream(x$1,x$2,x$3) }
  def setClob(x$1: Int,x$2: java.io.Reader): Unit = methodWrap { stmt.setClob(x$1,x$2) }
  def setClob(x$1: Int,x$2: java.io.Reader,x$3: Long): Unit = methodWrap { stmt.setClob(x$1,x$2,x$3) }
  def setClob(x$1: Int,x$2: java.sql.Clob): Unit = methodWrap { stmt.setClob(x$1,x$2) }
  def setDate(x$1: Int,x$2: java.sql.Date,x$3: java.util.Calendar): Unit = methodWrap { stmt.setDate(x$1,x$2,x$3) }
  def setDate(x$1: Int,x$2: java.sql.Date): Unit = methodWrap { stmt.setDate(x$1,x$2) }
  def setDouble(x$1: Int,x$2: Double): Unit = methodWrap { stmt.setDouble(x$1,x$2) }
  def setFloat(x$1: Int,x$2: Float): Unit = methodWrap { stmt.setFloat(x$1,x$2) }
  def setInt(x$1: Int,x$2: Int): Unit = methodWrap { stmt.setInt(x$1,x$2) }
  def setLong(x$1: Int,x$2: Long): Unit = methodWrap { stmt.setLong(x$1,x$2) }
  def setNCharacterStream(x$1: Int,x$2: java.io.Reader): Unit = methodWrap { stmt.setNCharacterStream(x$1,x$2) }
  def setNCharacterStream(x$1: Int,x$2: java.io.Reader,x$3: Long): Unit = methodWrap { stmt.setNCharacterStream(x$1,x$2,x$3) }
  def setNClob(x$1: Int,x$2: java.io.Reader): Unit = methodWrap { stmt.setNClob(x$1,x$2) }
  def setNClob(x$1: Int,x$2: java.io.Reader,x$3: Long): Unit = methodWrap { stmt.setNClob(x$1,x$2,x$3) }
  def setNClob(x$1: Int,x$2: java.sql.NClob): Unit = methodWrap { stmt.setNClob(x$1,x$2) }
  def setNString(x$1: Int,x$2: String): Unit = methodWrap { stmt.setNString(x$1,x$2) }
  def setNull(x$1: Int,x$2: Int,x$3: String): Unit = methodWrap { stmt.setNull(x$1,x$2,x$3) }
  def setNull(x$1: Int,x$2: Int): Unit = methodWrap { stmt.setNull(x$1,x$2) }
  def setObject(x$1: Int,x$2: Any,x$3: Int,x$4: Int): Unit = methodWrap { stmt.setObject(x$1,x$2,x$3,x$4) }
  def setObject(x$1: Int,x$2: Any): Unit = methodWrap { stmt.setObject(x$1,x$2) }
  def setObject(x$1: Int,x$2: Any,x$3: Int): Unit = methodWrap { stmt.setObject(x$1,x$2,x$3) }
  def setRef(x$1: Int,x$2: java.sql.Ref): Unit = methodWrap { stmt.setRef(x$1,x$2) }
  def setRowId(x$1: Int,x$2: java.sql.RowId): Unit = methodWrap { stmt.setRowId(x$1,x$2) }
  def setSQLXML(x$1: Int,x$2: java.sql.SQLXML): Unit = methodWrap { stmt.setSQLXML(x$1,x$2) }
  def setShort(x$1: Int,x$2: Short): Unit = methodWrap { stmt.setShort(x$1,x$2) }
  def setString(x$1: Int,x$2: String): Unit = methodWrap { stmt.setString(x$1,x$2) }
  def setTime(x$1: Int,x$2: java.sql.Time,x$3: java.util.Calendar): Unit = methodWrap { stmt.setTime(x$1,x$2,x$3) }
  def setTime(x$1: Int,x$2: java.sql.Time): Unit = methodWrap { stmt.setTime(x$1,x$2) }
  def setTimestamp(x$1: Int,x$2: java.sql.Timestamp,x$3: java.util.Calendar): Unit = methodWrap { stmt.setTimestamp(x$1,x$2,x$3) }
  def setTimestamp(x$1: Int,x$2: java.sql.Timestamp): Unit = methodWrap { stmt.setTimestamp(x$1,x$2) }
  def setURL(x$1: Int,x$2: java.net.URL): Unit = methodWrap { stmt.setURL(x$1,x$2) }
  def setUnicodeStream(x$1: Int,x$2: java.io.InputStream,x$3: Int): Unit = methodWrap { stmt.setUnicodeStream(x$1,x$2,x$3) }
}
