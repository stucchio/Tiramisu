package com.chrisstucchio.tiramisu

import scalaz._
import Scalaz._

class TiramisuException(e: String) extends Exception(e)

class DuplicatedParameterException(e: String) extends TiramisuException(e)
class InvalidParameterException(e: String) extends TiramisuException(e)
class UnusedParameterException(e: String) extends TiramisuException(e)

class Query(val sql: String, val params: Map[String,anorm.ParameterValue[_]]) {
  def add(q: Query): Query = new Query(sql + q.sql, mergeParams(params, q.params))

  override def equals(other: Any) = {
    other match {
      case o: Query => (sql == o.sql) && (params == o.params)
      case _ => false
    }
  }

  def AND(q: Query) = addWithCenter(" AND ", this, q)
  def OR(q: Query) = addWithCenter(" OR ", this, q)
  def WHERE(q: Query) = addWithCenter(" WHERE ", this, q)

  def formatS(queries: Query*) = new Query(sql.format(queries: _*),
					   queries.foldLeft(params)( (x:Map[String,anorm.ParameterValue[_]],y:Query) => mergeParams(x, y.params)))
  def formatV(values: anorm.ParameterValue[_]*) = {
    val newParams = values.map(x => (Query.randomParam, x))
    new Query( sql.format(newParams.map(x => "{%s}".format(x._1)): _*), mergeParams(params, newParams.toMap))
  }

  def addParams(newParams: (String, anorm.ParameterValue[_])*): Query = new Query(sql, params ++ newParams)

  private def addWithCenter(center: String, q: Query, p: Query): Query = new Query(q.sql + center + p.sql, mergeParams(q.params, p.params))

  private def mergeParams(p: Map[String, anorm.ParameterValue[_]], q: Map[String, anorm.ParameterValue[_]]): Map[String, anorm.ParameterValue[_]] = {
    val paramIntersection = p.keySet & q.keySet
    val duplicatedParameters = paramIntersection.filter( k => p(k) != q(k) )

    if (!(p.keySet ++ q.keySet).forall(x => Query.parameterIsValid(x))) { //Make sure all parameters valid
      throw new InvalidParameterException("Invalid parameters: " + (p.keySet ++ q.keySet).filter(x => !Query.parameterIsValid(x)) )
    }

    if (!duplicatedParameters.isEmpty) { //We can't combine queries where both have the same parameter key, but the value is different
      throw new DuplicatedParameterException("Cannot combine queries, contain duplicated parameters for which the values differ: " + duplicatedParameters.map(k => (p(k), q(k))))
    }
    p ++ q
  }
}

object Query {
  private val parameterRegex = java.util.regex.Pattern.compile("^[\\w]+\\z")
  def parameterIsValid(p: String) = parameterRegex.matcher(p).matches()
  def randomParam = java.util.UUID.randomUUID().toString().replace('-','_')
}

object Tiramisu {
  import anorm._
  def Select(q: Query)(implicit connection: java.sql.Connection) = SQL(q.sql).on(q.params.toSeq: _*)()(connection)
  def Insert(q: Query)(implicit connection: java.sql.Connection) = SQL(q.sql).on(q.params.toSeq: _*).executeInsert()(connection)
  def Update(q: Query)(implicit connection: java.sql.Connection) = SQL(q.sql).on(q.params.toSeq: _*).executeUpdate()(connection)
}

object Syntax {
  implicit def queryIsMonoid: Monoid[Query] = new Monoid[Query] { //Logs can be concatenated
    def append(q1: Query, q2: => Query): Query = q1.add(q2)
    def zero: Query = new Query("", Map())
  }

  class StringToQuery(s: String) {
    def sql: Query = new Query(s, Map[String,anorm.ParameterValue[_]]())

    def sqlP(params: (String,anorm.ParameterValue[_])*): Query = new Query(s, params.toMap)
    def sqlM(params: Map[String,anorm.ParameterValue[_]]): Query = new Query(s, params)
  }

  implicit def stringToStringToQuery(s: String): StringToQuery = new StringToQuery(s)

  class ValToParam(x: anorm.ParameterValue[_]) {
    def sqlV: Query = {
      val paramName = Query.randomParam
      new Query("{%s}".format(paramName), Map[String,anorm.ParameterValue[_]](paramName -> x))
    }
  }

  implicit def valToParam(x: anorm.ParameterValue[_]):ValToParam = new ValToParam(x)

}
