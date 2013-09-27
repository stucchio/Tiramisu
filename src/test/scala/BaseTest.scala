import org.scalacheck._

import scalaz._
import Scalaz._

import java.sql._

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

object QuerySpecification extends Properties("Query.Specification") {
  import Generators._
  property("addition adds sql strings") = forAll(legitimateString, alphaList, legitimateString, alphaList) { (a: String, ap: List[SqlParameter[String]], b: String, bp: List[SqlParameter[String]]) => {
    (a.sqlP(ap.map(_.sqlV): _*) + b.sqlP(bp.map(_.sqlV): _*)).sql == (a + b)
  } }

  property("addition adds params") = forAll(legitimateString, alphaList, legitimateString, alphaList) { (a: String, ap: List[SqlParameter[String]], b: String, bp: List[SqlParameter[String]]) => {
    (a.sqlP(ap.map(_.sqlV): _*) + b.sqlP(bp.map(_.sqlV): _*)).params == (ap ++ bp)
  } }

  property("three way addition adds sql strings") = forAll(legitimateString, alphaList, legitimateString, alphaList, legitimateString, alphaList) { (a: String, ap: List[SqlParameter[String]], b: String, bp: List[SqlParameter[String]], c: String, cp: List[SqlParameter[String]]) => {
    //First strip out duplicated keys
    (Query(a, ap) |+| Query(b, bp) |+| Query(c, cp)).sql == a + b + c
  } }

  property("three way addition adds params") = forAll(legitimateString, alphaList, legitimateString, alphaList, legitimateString, alphaList) { (a: String, ap: List[SqlParameter[String]], b: String, bp: List[SqlParameter[String]], c: String, cp: List[SqlParameter[String]]) => {
    //First strip out duplicated keys
    (Query(a, ap) |+| Query(b, bp) |+| Query(c, cp)).params == ap ++ bp ++ cp
  } }
}

/*object QueryDatabaseSpecification extends Properties("Query.Database") {
  Class.forName("org.postgresql.Driver")

  def getConnection = {
    val url = "jdbc:postgresql://172.17.0.2/bayesianwitch"
    val props = new java.util.Properties()
    props.setProperty("user", "bayesianwitch")
    props.setProperty("password", "RNpHEOUg89fmQ")
    DriverManager.getConnection(url, props)
  }

  import Generators._
  property("addition adds sql strings") = forAll(legitimateString) { (a: String) => {
    val conn = getConnection
    true
  } }
}
 */
