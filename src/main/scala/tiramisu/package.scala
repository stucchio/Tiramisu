package com.chrisstucchio

import scalaz._
import Scalaz._

package object tiramisu extends Syntax {
  implicit def baseQueryIsMonoid: Monoid[BaseQuery] = new Monoid[BaseQuery] { //Logs can be concatenated
    def append(q1: BaseQuery, q2: => BaseQuery): BaseQuery = q1.add(q2)
    def zero: BaseQuery = new BaseQuery("", Seq())
  }

  implicit def queryIsMonoid: Monoid[Query] = new Monoid[Query] { //Logs can be concatenated
    def append(q1: Query, q2: => Query): Query = q1.add(q2)
    def zero: Query = new BaseQuery("", Seq())
  }
}
