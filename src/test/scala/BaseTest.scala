import org.scalacheck._

import scalaz._
import Scalaz._

import Prop.forAll

import com.chrisstucchio.tiramisu._
import com.chrisstucchio.tiramisu.Syntax._

object Generators {
  val legitimateString = Gen.alphaStr suchThat (_.size > 0)
  val alphaMap = for {
    x <- Gen.containerOf[Set,String](legitimateString)
  } yield (x.map( t => (t, t+t) ).toMap)

  val genQuery = for {
    sql <- legitimateString
    params <- alphaMap
  } yield new Query(sql, params)

}

object QuerySpecificationMonoid extends Properties("MonoidLaws") {
  import Generators._
  property("equality") = forAll(legitimateString, alphaMap) { (a: String, b: Map[String,String]) => {
    a.sqlP(b) == a.sqlP(b)
  } }

  property("associativity") = forAll(genQuery, genQuery, genQuery) { (a: Query, b: Query, c: Query) => {
    ((a |+| b) |+| c) == (a |+| (b |+| c))
  } }
}

object QuerySpecification extends Properties("QueryParams") {
  import Prop.forAll

  import Generators._

  property("params are set properly") = forAll { (a: Map[String,String]) => (new Query("SELECT * FROM foo;", a)).params == a }

  property("addition preserves parameters, no intersection") = forAll(alphaMap, alphaMap) { (a: Map[String,String], b: Map[String,String]) => {
    val q1 = "SELECT * FROM foo ".sqlP(a)
    val q2 = "WHERE bar;".sqlP(b -- a.keySet) //Make sure a and b have no duplicated elements
    (q1 |+| q2).params == (a ++ b)
  } }

  property("addition preserves parameters, intersection has same keys and values") = forAll(alphaMap, alphaMap, legitimateString, legitimateString) { (a: Map[String,String], b: Map[String,String], c: String, d: String) => {
    val q1 = "SELECT * FROM foo ".sqlP((a - c) + (c -> d))
    val q2 = "WHERE bar;".sqlP((b -- a.keySet - c) + (c -> d)) //Make sure a and b have no duplicated elements
    (q1 |+| q2).params == (a ++ b + (c -> d))
  } }

  property("sqlV generates parameter objects") = forAll(legitimateString) { (b: String) => {
    val r = b.sqlV
    r.params.values.toList == List(b)
  } }

  property("sqlV generates parameter objects, addition works") = forAll(legitimateString, legitimateString) { (a: String, b: String) => {
    val r = a.sql |+| b.sqlV
    val paramKey = r.params.keySet.head // An arbitrarily chosen parameter for b
    (r.params.size == 1) && (r.params.contains(paramKey)) && r.sql.endsWith("{%s}".format(paramKey))
  } }

}
