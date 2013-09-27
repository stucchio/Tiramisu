package com.chrisstucchio

import scalaz._
import Scalaz._

package object tiramisu extends Syntax {
  implicit def toSqlParameter[T](l: T)(implicit pi: ParameterInjector[T]): SqlParameter[T] = SqlParameter(l)

  implicit def queryIsMonoid: Monoid[Query] = new Monoid[Query] { //Logs can be concatenated
    def append(q1: Query, q2: => Query): Query = q1 + q2
    def zero: Query = new BaseQuery("", Seq())
  }
}
