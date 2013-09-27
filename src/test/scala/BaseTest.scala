import org.scalacheck._

import scalaz._
import Scalaz._

import Prop.forAll

import com.chrisstucchio.tiramisu._
import com.chrisstucchio.tiramisu.ParameterInjectors._

object Generators {

  val legitimateString = Gen.alphaStr suchThat ( _.size > 0)

  val alphaList = Gen.containerOf[List,String](legitimateString).map( _.map(_.sqlV))
}

object QuerySpecificationMonoid extends Properties("Query.MonoidLaws") {
  import Generators._
  property("equality") = forAll(legitimateString, alphaList) { (a: String, b: List[SqlParameter[String]]) => {
    a.sqlP(b.map(_.sqlV): _*) == a.sqlP(b.map(_.sqlV): _*)
  } }

  property("zero") = forAll(legitimateString, alphaList) { (a: String, b: List[SqlParameter[String]]) => {
    a.sqlP(b: _*) == (a.sqlP(b: _*) |+| "".sqlP())
  } }

  property("associativity") = forAll(legitimateString, alphaList, legitimateString, alphaList, legitimateString, alphaList) { (a: String, ap: List[SqlParameter[String]], b: String, bp: List[SqlParameter[String]], c: String, cp: List[SqlParameter[String]]) => {
    //First strip out duplicated keys
    ((Query(a, ap) |+| Query(b, bp)) |+| Query(c, cp)) == (Query(a, ap) |+| (Query(b, bp) |+| Query(c, cp)))
  } }
}
