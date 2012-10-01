package com.chrisstucchio.tiramisu

import scalaz._
import Scalaz._

class TiramisuException(e: String) extends Exception(e)

class DuplicatedParameterException(e: String) extends TiramisuException(e)
class InvalidParameterException(e: String) extends TiramisuException(e)
class UnusedParameterException(e: String) extends TiramisuException(e)

class Query(val sql: String, val params: Map[String,Any]) {
  def add(q: Query): Query = {
    val finalSql = sql + q.sql
    val paramIntersection = params.keySet & q.params.keySet
    val duplicatedParameters = paramIntersection.filter( k => params(k) != q.params(k) )

    if (!(params.keySet ++ q.params.keySet).forall(p => Query.parameterIsValid(p))) { //Make sure all parameters valid
      throw new InvalidParameterException("Invalid parameters: " + (params.keySet ++ q.params.keySet).filter(p => !Query.parameterIsValid(p)) )
    }

    if (!duplicatedParameters.isEmpty) { //We can't combine queries where both have the same parameter key, but the value is different
      throw new DuplicatedParameterException("Cannot combine queries, contain duplicated parameters for which the values differ: " + duplicatedParameters.map(k => (params(k), q.params(k))))
    }
    new Query(sql + q.sql, params ++ q.params)
  }

  override def equals(other: Any) = {
    other match {
      case o: Query => (sql == o.sql) && (params == o.params)
      case _ => false
    }
  }
}

object Query {
  private val parameterRegex = java.util.regex.Pattern.compile("^[\\w]+\\z")
  def parameterIsValid(p: String) = parameterRegex.matcher(p).matches()
}

object Syntax {
  implicit def queryIsMonoid: Monoid[Query] = new Monoid[Query] { //Logs can be concatenated
    def append(q1: Query, q2: => Query): Query = q1.add(q2)
    def zero: Query = new Query("", Map())
  }

  class StringToQuery(s: String) {
    def sql: Query = new Query(s, Map[String,Any]())

    def sqlP(params: (String,Any)*): Query = new Query(s, params.toMap)
    def sqlP(params: Map[String,Any]): Query = new Query(s, params)
  }

  implicit def stringToStringToQuery(s: String): StringToQuery = new StringToQuery(s)

  class ValToParam(x: Any) {
    def sqlV: Query = {
      val paramName = java.util.UUID.randomUUID().toString().replace('-','_')
      new Query("{%s}".format(paramName), Map[String,Any](paramName -> x))
    }
  }

  implicit def valToParam(x: Any):ValToParam = new ValToParam(x)

}
