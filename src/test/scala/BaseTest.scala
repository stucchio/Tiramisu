import org.scalacheck._

import scalaz._
import Scalaz._

import Prop.forAll

import com.chrisstucchio.tiramisu._
import com.chrisstucchio.tiramisu.ParameterInjectors._

object Generators {

  val legitimateString = Gen.alphaStr suchThat ( _.size > 0)

  val alphaList = Gen.containerOf[List,String](legitimateString).map( _.map(_.param))
}

object QuerySpecificationMonoid extends Properties("Query.MonoidLaws") {
  import Generators._
  property("equality") = forAll(legitimateString, alphaList) { (a: String, b: List[SqlParameter[String]]) => {
    a.sqlP(b.map(_.param): _*) == a.sqlP(b.map(_.param): _*)
  } }

  property("zero") = forAll(legitimateString, alphaList) { (a: String, b: List[SqlParameter[String]]) => {
    a.sqlP(b: _*) == (a.sqlP(b: _*) |+| "".sql)
  } }

/*  property("associativity") = forAll(genQuery, genQuery, genQuery) { (a: Query, b: Query, c: Query) => {
    //First strip out duplicated keys
    val dupKeys = elementsInAtLeastTwoSets(Seq(a.params, b.params, c.params))
    val ap, bp, cp = (new BaseQuery(a.sql, a.params -- dupKeys), new BaseQuery(b.sql, b.params -- dupKeys), new BaseQuery(c.sql, c.params -- dupKeys))
    ((ap |+| bp) |+| cp) == (ap |+| (bp |+| cp))
  } }*/
}
