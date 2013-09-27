package com.chrisstucchio.tiramisu

import java.sql._

class TiramisuException(e: String) extends Exception(e)

class DuplicatedParameterException(e: String) extends TiramisuException(e)
class InvalidParameterException(e: String) extends TiramisuException(e)
class UnusedParameterException(e: String) extends TiramisuException(e)

trait Query {
  def sql: String
  def params: Seq[SqlParameter[_]]

  def add(q: Query): Query = new BaseQuery(sql + q.sql, params ++ q.params)
  def +(q: Query): Query = add(q)

/*  def AND(q: Query) = addWithCenter(" AND ", q)
  def OR(q: Query) = addWithCenter(" OR ", q)
  def WHERE(q: Query) = addWithCenter(" WHERE ", q) */

  import ParameterInjectors._

  def LIMIT(limit: Long): Query = this + BaseQuery(" LIMIT ? ", Seq(limit))
  def OFFSET(offset: Long): Query = this + BaseQuery(" OFFSET ? ", Seq(offset))

  override def toString(): String = "Query(" + sql + ", " + params + ")"

  override def equals(other: Any) = {
    other match {
      case o: Query => (sql == o.sql) && (params == o.params)
      case _ => false
    }
  }
  protected def addWithCenter(center: String, p: Query): Query = new BaseQuery(this.sql + center + p.sql, this.params ++ p.params)

  def prepareStatement(conn: Connection): PreparedStatement = {
    val ps = conn.prepareStatement(sql)
    params.zipWithIndex.foreach( x => x._1.setParam(x._2, ps) )
    ps
  }
}

case class BaseQuery(val sql: String, val params: Seq[SqlParameter[_]]) extends Query {
  override def add(q: Query): BaseQuery = new BaseQuery(sql + q.sql, params ++ q.params)

  override def +(q: Query): BaseQuery = add(q) //duplicated here to have more specific type signature

  override def toString(): String = "BaseQuery(" + sql + ", " + params + ")"
}
