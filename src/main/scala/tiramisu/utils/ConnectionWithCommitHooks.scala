package com.chrisstucchio.tiramisu.utils

import wrappers._

case class ConnectionWithCommitHooks(protected val conn: java.sql.Connection) extends ConnectionWrapper {
  case class CannotAddHookException(msg: String) extends java.sql.SQLException(msg)

  private def checkAutoCommit[T](f: =>T) = {
    if (getAutoCommit()) {
      throw CannotAddHookException("Cannot add hooks to connection in autocommit mode. Call setAutoCommit(false) first.")
    }
    f
  }

  private val commitHooks: scala.collection.mutable.ArrayBuffer[()=>Unit] = scala.collection.mutable.ArrayBuffer()
  def addCommitHook(f: ()=>Unit) = checkAutoCommit { commitHooks.append(f) } //called AFTER successful commit

  private val rollbackHooks: scala.collection.mutable.ArrayBuffer[()=>Unit] = scala.collection.mutable.ArrayBuffer()
  def addRollbackHook(f: ()=>Unit) = checkAutoCommit { rollbackHooks.append(f) } //called AFTER successful rollback

  private val uncommittedHooks: scala.collection.mutable.ArrayBuffer[()=>Unit] = scala.collection.mutable.ArrayBuffer()
  def addUncommittedHook(f: ()=>Unit) = checkAutoCommit { uncommittedHooks.append(f) } //Adds a hook to be called on close if neither commit nor rollback are called

  override def setAutoCommit(autoCommit: Boolean) = methodWrap {
    if (autoCommit && ((commitHooks.size > 0) || (rollbackHooks.size > 0) || (uncommittedHooks.size > 0))) {
      throw CannotAddHookException("Cannot turn on autocommit when hooks are present.")
    }
    conn.setAutoCommit(autoCommit)
  }

  override def commit(): Unit = methodWrap {
    conn.commit();
    commitHooks.foreach(f => f())
    uncommittedHooks.clear()
  }

  override def rollback(): Unit = methodWrap {
    conn.rollback()
    rollbackHooks.foreach(f => f())
    uncommittedHooks.clear()
  }

  override def close(): Unit = methodWrap {
    conn.close()
    uncommittedHooks.foreach(f => f())
  }
}
