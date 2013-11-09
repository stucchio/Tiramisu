package com.chrisstucchio.tiramisu

import java.sql._
import utils.ConnectionWrapper

object PreparedStatementCachingConnection {
  def apply(conn: Connection) = new PreparedStatementCachingConnection(conn)
}

class PreparedStatementCachingConnection(protected val conn: Connection) extends ConnectionWrapper {
  private val psCache: collection.mutable.Map[String,PreparedStatement] = scala.collection.mutable.HashMap()
  private def clearCache = psCache.clear

  private def clearThen[T](f: =>T) = {
    clearCache
    f
  }

  override def commit() = clearThen { conn.commit() }
  override def close() = clearThen { conn.commit() }
  override def rollback(x: java.sql.Savepoint): Unit = clearThen { conn.rollback(x) }
  override def rollback(): Unit = clearThen { conn.rollback() }
  override def prepareStatement(x: String) = {
    val result = psCache.get(x)
    if (result.isEmpty) {
      val ps = conn.prepareStatement(x)
      psCache.put(x, ps)
      ps
    } else {
      result.get
    }
  }
}
