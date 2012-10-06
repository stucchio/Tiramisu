package com.chrisstucchio.tiramisu

import scalaz._
import Scalaz._

class TiramisuException(e: String) extends Exception(e)

class DuplicatedParameterException(e: String) extends TiramisuException(e)
class InvalidParameterException(e: String) extends TiramisuException(e)
class UnusedParameterException(e: String) extends TiramisuException(e)

trait Query extends {
  def sql: String
  def params: Map[String, anorm.ParameterValue[_]]

  def add(q: Query): Query = new BaseQuery(sql + q.sql, mergeParams(params, q.params))
  def +(q: Query): Query = add(q)

  def AND(q: Query) = addWithCenter(" AND ", q)
  def OR(q: Query) = addWithCenter(" OR ", q)
  def WHERE(q: Query) = addWithCenter(" WHERE ", q)

  def LIMIT(limit: Long): Query = addWithParam(" LIMIT {%s} ", limit)
  def OFFSET(offset: Long): Query = addWithParam(" OFFSET {%s} ", offset)

  override def toString(): String = "Query(" + sql + ", " + params + ")"

  override def equals(other: Any) = {
    other match {
      case o: Query => (sql == o.sql) && (params == o.params)
      case _ => false
    }
  }

  protected def addWithParam(s: String, x: anorm.ParameterValue[_]): Query = {
    val paramName = Query.randomParam
    new BaseQuery(sql + s.format(paramName), params + (paramName -> x))
  }

  protected def addWithCenter(center: String, p: Query): Query = new BaseQuery(this.sql + center + p.sql, mergeParams(this.params, p.params))

  private def getParam(p: Map[String, anorm.ParameterValue[_]], k: String) = p.get(k).aValue.get.aValue

  protected def mergeParams(p: Map[String, anorm.ParameterValue[_]], q: Map[String, anorm.ParameterValue[_]]): Map[String, anorm.ParameterValue[_]] = {
    val paramIntersection = p.keySet & q.keySet
    val duplicatedParameters = paramIntersection.filter( k => (getParam(p,k) != getParam(q,k)) )

    if (!(p.keySet ++ q.keySet).forall(x => Query.parameterIsValid(x))) { //Make sure all parameters valid
      throw new InvalidParameterException("Invalid parameters: " + (p.keySet ++ q.keySet).filter(x => !Query.parameterIsValid(x)) )
    }

    if (!duplicatedParameters.isEmpty) { //We can't combine queries where both have the same parameter key, but the value is different
      throw new DuplicatedParameterException("Cannot combine queries, contain duplicated parameters for which the values differ: " + duplicatedParameters.map(k => (p(k), q(k))))
    }
    p ++ q
  }
}

case class ParamQuery(k: String, v: anorm.ParameterValue[_]) extends Query {
  if (!Query.parameterIsValid(k)) {
    throw new InvalidParameterException("Invalid parameter: " + (k -> v))
  }
  override val sql = "{%s}".format(k)
  override def params = Map(k -> v)
  override def toString(): String = "ParamQuery(" + k + ", " + v + ")"
}

class BaseQuery(val sql: String, val params: Map[String,anorm.ParameterValue[_]]) extends Query {
  //Error check
  {
    if (!params.keys.forall(x => Query.parameterIsValid(x))) {
      throw new InvalidParameterException("Invalid parameters: " + params.filter(x => !Query.parameterIsValid(x._1)) )
    }
  }

  override def add(q: Query): BaseQuery = new BaseQuery(sql + q.sql, mergeParams(params, q.params))

  override def +(q: Query): BaseQuery = add(q) //duplicated here to have more specific type signature

  def formatS(queries: Query*) = new BaseQuery(sql.format(queries: _*),
					   queries.foldLeft(params)( (x:Map[String,anorm.ParameterValue[_]],y:Query) => mergeParams(x, y.params)))
  def formatV(values: anorm.ParameterValue[_]*) = {
    val newParams = values.map(x => (Query.randomParam, x))
    new BaseQuery( sql.format(newParams.map(x => "{%s}".format(x._1)): _*), mergeParams(params, newParams.toMap))
  }

  def addParams(newParams: (String, anorm.ParameterValue[_])*): Query = new BaseQuery(sql, params ++ newParams)

  override def toString(): String = "BaseQuery(" + sql + ", " + params + ")"
}

object Query {
  private val parameterRegex = java.util.regex.Pattern.compile("^[\\w]+\\z")
  def parameterIsValid(p: String) = parameterRegex.matcher(p).matches()
  def randomParam: String = java.util.UUID.randomUUID().toString().replace('-','_')
}

object Tiramisu {
  import anorm._
  def Select(q: Query)(implicit connection: java.sql.Connection) = SQL(q.sql).on(q.params.toSeq: _*)()(connection)
  def Insert(q: Query)(implicit connection: java.sql.Connection) = SQL(q.sql).on(q.params.toSeq: _*).executeInsert()(connection)
  def Update(q: Query)(implicit connection: java.sql.Connection) = SQL(q.sql).on(q.params.toSeq: _*).executeUpdate()(connection)
}

object Syntax {
  implicit def baseQueryIsMonoid: Monoid[BaseQuery] = new Monoid[BaseQuery] { //Logs can be concatenated
    def append(q1: BaseQuery, q2: => BaseQuery): BaseQuery = q1.add(q2)
    def zero: BaseQuery = new BaseQuery("", Map())
  }

  implicit def queryIsMonoid: Monoid[Query] = new Monoid[Query] { //Logs can be concatenated
    def append(q1: Query, q2: => Query): Query = q1.add(q2)
    def zero: Query = new BaseQuery("", Map())
  }


  class StringToQuery(s: String) {
    def sql: BaseQuery = new BaseQuery(s, Map[String,anorm.ParameterValue[_]]())
    def sqlP(params: (String,anorm.ParameterValue[_])*): BaseQuery = new BaseQuery(s, params.toMap)
    def sqlM(params: Map[String,anorm.ParameterValue[_]]): BaseQuery = new BaseQuery(s, params)
  }

  implicit def stringToStringToQuery(s: String): StringToQuery = new StringToQuery(s)

  class ValToParam(k: String, v: anorm.ParameterValue[_]) {
    def sqlV: ParamQuery = new ParamQuery(k, v)
  }

  implicit def valToParamV(x: anorm.ParameterValue[_]):ValToParam = new ValToParam(Query.randomParam, x)
  implicit def valToParamKVString(x: (String, String)):ValToParam = new ValToParam(x._1, x._2)
  implicit def valToParamKVLong(x: (String, Long)):ValToParam = new ValToParam(x._1, x._2)
}
