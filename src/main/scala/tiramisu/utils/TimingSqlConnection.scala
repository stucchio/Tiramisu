package com.chrisstucchio.tiramisu.utils

import wrappers._

object TimingSqlConnection {
  def apply(conn: java.sql.Connection) = new TimingSqlConnection(conn)
}

class TimingSqlConnection(protected val conn: java.sql.Connection) extends ConnectionWrapper {
  private val timeCounter = new java.util.concurrent.atomic.AtomicLong(0)

  def getTime: Long = timeCounter.get

  private def time[T](f: =>T): T = {
    val start = System.nanoTime()
    try {
      f
    } finally {
      val end = System.nanoTime()
      timeCounter.addAndGet(end-start)
    }

  }

  override protected def methodWrap[T](f: =>T): T = time(f)

  protected case class TimingResultSet(protected val rs: java.sql.ResultSet) extends ResultSetWrapper {
    override def methodWrap[T](f: =>T): T = time(f)
  }

  protected case class TimingStatement(protected val stmt: java.sql.Statement) extends StatementWrapper {
    override def methodWrap[T](f: =>T): T = time(f)
    override def wrapResultSet(rs: java.sql.ResultSet) = TimingResultSet(rs)
  }

  protected case class TimingPreparedStatement(protected val stmt: java.sql.PreparedStatement) extends PreparedStatementWrapper {
    override def methodWrap[T](f: =>T): T = time(f)
    override def wrapResultSet(rs: java.sql.ResultSet) = TimingResultSet(rs)
  }

  protected case class TimingCallableStatement(protected val stmt: java.sql.CallableStatement) extends CallableStatementWrapper {
    override def methodWrap[T](f: =>T): T = time(f)
    override def executeQuery(): java.sql.ResultSet = methodWrap { TimingResultSet(stmt.executeQuery()) }
    override def getResultSet(): java.sql.ResultSet = methodWrap { TimingResultSet(stmt.getResultSet()) }
  }

  override protected def wrapStatement(stmt: java.sql.Statement) = TimingStatement(stmt)
  override protected def wrapPreparedStatement(stmt: java.sql.PreparedStatement) = TimingPreparedStatement(stmt)
  override protected def wrapCallableStatement(stmt: java.sql.CallableStatement) = TimingCallableStatement(stmt)
}
