import org.scalacheck._

import scalaz._
import Scalaz._
import anorm._

import Prop.forAll

import com.chrisstucchio.tiramisu._
import com.chrisstucchio.tiramisu.Syntax._

object Generators {

  val legitimateString = Gen.alphaStr suchThat ( _.size > 0)

  val alphaMap = for {
    x <- Gen.containerOf[Set,String](legitimateString)
  } yield (x.map( t => (t, toParameterValue(t+t)) ).toMap)

  val genQuery = for {
    sql <- legitimateString
    params <- alphaMap
  } yield new BaseQuery(sql, params)

  def elementsInAtLeastTwoSets(m: Seq[Map[String,_]]): Set[String] = {
    val pairsOfMaps = for {
        x <- m
        y <- m
      } yield (x,y)
    val duplicatedElements = pairsOfMaps.map( t => t._1.keySet intersect t._2.keySet ).fold(Set())( (a,b) => a union b)
    duplicatedElements
  }

  def noDupes[A](a: Map[String,A], b: Map[String,A]): (Map[String,A], Map[String,A]) = (a -- (a.keySet intersect b.keySet), b -- (a.keySet intersect b.keySet))

  def condThenPropOrException(cond: =>Boolean, prop: =>Boolean, e: java.lang.Exception) = {
    if (cond) {
      prop
    } else {
      try {
	prop
      } catch {
	case x => {
	  println(x.getClass() == e.getClass())
	  println(x.getClass())
	  println(e.getClass())
	  (x.getClass() == e.getClass())
	}
      }
    }
  }

  def getParam(p: Map[String, ParameterValue[_]], k: String) = p.get(k).aValue.get.aValue
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
    val ap, bp, cp = (new BaseQuery(a.sql, a.params -- dupKeys), new BaseQuery(b.sql, b.params -- dupKeys), new BaseQuery(c.sql, c.params -- dupKeys))
    ((ap |+| bp) |+| cp) == (ap |+| (bp |+| cp))
  } }
}

object QuerySpecification extends Properties("Query.NormalUse") {
  import Prop._

  import Generators._

  property("params are set properly") = forAll(legitimateString, alphaMap) { (q: String, a: Map[String,ParameterValue[_]]) => (new BaseQuery(q, a)).params == a }

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
    (c != "") ==> (
      ((q1 |+| q2).params.keySet == (ac ++ bc + (c -> d)).keySet) :| "Key set matches"
      //We only check keyset, since comparing anorm ParameterValue objects is hard
      )
  } }

  property("sqlV generates parameter objects") = forAll(legitimateString) { (b: String) => {
    val r = toParameterValue(b).sqlV
    r.params.values.toList.size == 1
  } }

  property("sqlV generates parameter objects, addition works") = forAll(Gen.alphaStr, Gen.alphaStr) { (a: String, b: String) => {
    (a != "") ==> {
      val r = a.sql + toParameterValue(b).sqlV
      val paramKey = r.params.keySet.head // An arbitrarily chosen parameter for b
      r.sql.endsWith("{%s}".format(paramKey)) :| "SQL ends with parameter mapping" &&
      (getParam(r.params, paramKey) == b) :| "Paramter value is correct"
    }
  } }
}

object QueryConvenienceSpecification extends Properties("Query.Convenience") {
  import Prop._

  import Generators._

  def ignoreDuplicatedParameter(x: => Prop) = try { x } catch {case y:DuplicatedParameterException => true :| "Non-duplicate error should never be raised"
							       case _ => false :| "Non-duplicate error should never be raised"}

  property("AND works, no params") = forAll(genQuery, genQuery) { (q1: Query, q2: Query) => {
    ignoreDuplicatedParameter {
      val combined = q1 AND q2
      (combined.params == (q1.params ++ q2.params)) :| "Parameters are combined" &&
      (combined.sql.matches(q1.sql + "\\s+AND\\s+" + q2.sql) :| "SQL is properly joined")
    }
  } }

  property("OR works, no params") = forAll(genQuery, genQuery) { (q1: Query, q2: Query) => {
    ignoreDuplicatedParameter {
      val combined = q1 OR q2
      (combined.params == (q1.params ++ q2.params)) :| "Parameters are combined" &&
      combined.sql.matches(q1.sql + "\\s+OR\\s+" + q2.sql) :| "SQL is properly joined"
    }
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

  property("throws no error when parameter key is duplicated, value the same") = forAll(legitimateString, alphaMap, legitimateString, legitimateString) { (a: String, b: Map[String,ParameterValue[_]], c: String, d: String) => {
    val q1 = a.sqlM(b + (c -> d))
    val q2 = a.sqlM(b + (c -> d))
    q1 |+| q2
    true
  } }

  property("throws an error when passed an empty string for a parameter") = forAll { (a: String, b: Long) => {
    throws({ a.sqlP("" -> b) }, classOf[InvalidParameterException])
  } }

}
