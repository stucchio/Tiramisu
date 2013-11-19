package com.chrisstucchio

import scalaz._
import Scalaz._

package object tiramisu  {
  implicit def toSqlParameter[T](l: T)(implicit pi: ParameterInjector[T]): SqlParameter[T] = SqlParameter(l)
  implicit def optionToSqlParameter[T](l: Option[T])(implicit pi: ParameterInjector[T]): OptionalSqlParameter[T] = OptionalSqlParameter(l)

  implicit def queryIsMonoid: Monoid[Query] = new Monoid[Query] { //Logs can be concatenated
    def append(q1: Query, q2: => Query): Query = q1 + q2
    def zero: Query = new BaseQuery("", Seq())
  }

  implicit class StringToQuery(val s: String) extends AnyVal {
    def sql: Query = Query(s, Seq())
    def sqlP(params: SqlParameter[_]*): Query = Query(s, params)
  }
}
