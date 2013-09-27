package com.chrisstucchio.tiramisu

import java.sql._

import ParameterInjectors._

class TiramisuException(e: String) extends Exception(e)

class DuplicatedParameterException(e: String) extends TiramisuException(e)
class InvalidParameterException(e: String) extends TiramisuException(e)
class UnusedParameterException(e: String) extends TiramisuException(e)

trait Query {
  def sql: String
  def params: Seq[SqlParameter[_]]

  def +(q: Query): Query = new StringBuilderQuery(List(sql, q.sql), params ++ q.params)

  def AND(q: Query) = addWithCenter(" AND ", q)
  def OR(q: Query) = addWithCenter(" OR ", q)
  def WHERE(q: Query) = addWithCenter(" WHERE ", q)
  def LIMIT(limit: Long): Query = this + BaseQuery(" LIMIT ? ", Seq(limit))
  def OFFSET(offset: Long): Query = this + BaseQuery(" OFFSET ? ", Seq(offset))

  override def toString(): String = "Query(" + sql + ", " + params + ")"

  override def equals(other: Any) = {
    other match {
      case o: Query => (sql == o.sql) && (params == o.params)
      case _ => false
    }
  }
  protected def addWithCenter(center: String, other: Query): Query = this + center.sqlP() + other

  def prepareStatement(conn: Connection): PreparedStatement = {
    val ps = conn.prepareStatement(sql)
    params.zipWithIndex.foreach( x => x._1.setParam(x._2, ps) )
    ps
  }
}

object Query {
  def apply(sql: String, params: Seq[SqlParameter[_]]): Query = BaseQuery(sql, params)
}

private case class BaseQuery(val sql: String, val params: Seq[SqlParameter[_]]) extends Query {
  override def toString(): String = "BaseQuery(" + sql + ", " + params + ")"
}

private case class StringBuilderQuery(val sqlStrings: List[String], val params: Seq[SqlParameter[_]]) extends Query {
  lazy val sql: String = {
    val sb = new StringBuilder()
    sqlStrings.foreach(s => sb.append(s))
    sb.toString()
  }
  override def +(q: Query): Query = q match {
    case StringBuilderQuery(otherStrings, otherParams) => StringBuilderQuery(sqlStrings ++ otherStrings, params ++ otherParams)
    case (other:Query) => StringBuilderQuery(sqlStrings ++ Seq(other.sql), params ++ other.params)
  }
}
