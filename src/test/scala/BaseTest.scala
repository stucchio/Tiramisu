import org.scalacheck._

import scalaz._
import Scalaz._
import anorm._

import Prop.forAll

import com.chrisstucchio.tiramisu._
import com.chrisstucchio.tiramisu.Syntax._

object Generators {
  val legitimateString = Gen.alphaStr suchThat (_.size > 0)

  val alphaMap = for {
    x <- Gen.containerOf[Set,String](legitimateString)
  } yield (x.map( t => (t, toParameterValue(t+t)) ).toMap)

  val genQuery = for {
    sql <- legitimateString
    params <- alphaMap
  } yield new Query(sql, params)

  def elementsInAtLeastTwoSets(m: Seq[Map[String,_]]): Set[String] = {
    val pairsOfMaps = for {
        x <- m
        y <- m
      } yield (x,y)
    val duplicatedElements = pairsOfMaps.map( t => t._1.keySet intersect t._2.keySet ).fold(Set())( (a,b) => a union b)
    duplicatedElements
  }

  def noDupes[A](a: Map[String,A], b: Map[String,A]): (Map[String,A], Map[String,A]) = (a -- (a.keySet intersect b.keySet), b -- (a.keySet intersect b.keySet))
}

object QuerySpecificationMonoid extends Properties("Query.MonoidLaws") {
  import Generators._
  property("equality") = forAll(legitimateString, alphaMap) { (a: String, b: Map[String,ParameterValue[_]]) => {
    a.sqlP(b.toSeq: _*) == a.sqlP(b.toSeq: _*)
  } }

  property("zero") = forAll(legitimateString, alphaMap) { (a: String, b: Map[String,ParameterValue[_]]) => {
    a.sqlM(b) == (a.sqlM(b) |+| "".sql)
  } }

  property("associativity") = forAll(genQuery, genQuery, genQuery) { (a: Query, b: Query, c: Query) => {
    //First strip out duplicated keys
    val dupKeys = elementsInAtLeastTwoSets(Seq(a.params, b.params, c.params))
    val ap, bp, cp = (new Query(a.sql, a.params -- dupKeys), new Query(b.sql, b.params -- dupKeys), new Query(c.sql, c.params -- dupKeys))
    ((ap |+| bp) |+| cp) == (ap |+| (bp |+| cp))
  } }
}

object QuerySpecification extends Properties("Query.NormalUse") {
  import Prop._

  import Generators._

  property("params are set properly") = forAll(legitimateString, alphaMap) { (q: String, a: Map[String,ParameterValue[_]]) => (new Query(q, a)).params == a }

  property("addition preserves parameters, no intersection") = forAll(alphaMap, alphaMap) { (a: Map[String,ParameterValue[_]], b: Map[String,ParameterValue[_]]) => {
    val (ac, bc) = noDupes(a, b)
    val q1 = "SELECT * FROM foo ".sqlM(ac)
    val q2 = "WHERE bar;".sqlM(bc) //Make sure a and b have no duplicated elements
    ((q1 |+| q2).params == (ac ++ bc))
  } }

  property("addition preserves parameters, intersection has same keys and values") = forAll(legitimateString, legitimateString, alphaMap, alphaMap) { (c: String, d: String, a: Map[String,ParameterValue[_]], b: Map[String,ParameterValue[_]]) => {
    val (ac, bc) = noDupes(a - c, b - c)
    val q1 = "SELECT * FROM foo ".sqlM(ac + (c -> d))
    val q2 = "WHERE bar;".sqlM(bc + (c -> d)) //Make sure a and b have no duplicated elements
    (c != "") ==> ((q1 |+| q2).params == (ac ++ bc + (c -> d)))
  } }

  property("sqlV generates parameter objects") = forAll(legitimateString) { (b: String) => {
    val r = toParameterValue(b).sqlV
    r.params.values.toList.size == 1
  } }

  property("sqlV generates parameter objects, addition works") = forAll(legitimateString, legitimateString) { (a: String, b: String) => {
    val r = a.sql |+| toParameterValue(b).sqlV
    val paramKey = r.params.keySet.head // An arbitrarily chosen parameter for b
    (r.params.size == 1) && (r.params.contains(paramKey)) && r.sql.endsWith("{%s}".format(paramKey))
  } }

}

object QueryErrorSpecification extends Properties("Query.ErrorChecks") {
  import Prop.{forAll, throws}

  import Generators._

  property("throws error when parameter key is duplicated, values different") = forAll(legitimateString, alphaMap, legitimateString, legitimateString) { (a: String, b: Map[String,ParameterValue[_]], c: String, d: String) => {
    val q1 = a.sqlM(b + (c -> d))
    val q2 = a.sqlM(b + (c -> (d + "_suffix")))
    throws({q1 |+| q2}, classOf[DuplicatedParameterException])
  } }

}
