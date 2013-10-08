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

/*
To run this you need a database as follows.

CREATE TABLE clients (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4(),
    CONSTRAINT client_name_is_unique UNIQUE(name),
    CONSTRAINT client_uuid_is_unique UNIQUE(uuid)
);

CREATE TABLE content (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    content_type content_type NOT NULL,
    content TEXT NOT NULL
);
 */


object QueryDatabaseSpecification extends Properties("Query.Database") {
  Class.forName("org.postgresql.Driver")

  def withConnection[T](f: Connection => T) = {
    val url = "jdbc:postgresql://localhost:6432/bayesianwitch"
    val props = new java.util.Properties()
    props.setProperty("user", "bayesianwitch")
    props.setProperty("password", "FOSTdsPjuAiqA")
    val conn = DriverManager.getConnection(url, props)
    val result = f(conn)
    conn.close()
    result
  }

  def pullFromSql(uuid: java.util.UUID, conn: Connection): String = {
    val resultQuery = "SELECT name FROM clients WHERE name = ?".sqlP(uuid.toString.sqlV)
    val psr = resultQuery.prepareStatement(conn)
    val rs = psr.executeQuery()
    rs.next()
    rs.getString(1)
  }

  import Generators._
  property("sql insert works") = forAll(legitimateString) { (a: String) => withConnection(conn => {
    val uuid = java.util.UUID.randomUUID()
    val query = "INSERT INTO clients (name) VALUES (?)".sqlP(uuid.toString.sqlV)
    val ps = query.prepareStatement(conn)
    ps.executeUpdate()
    pullFromSql(uuid, conn) == uuid.toString
  }) }

  property("sql insert works 2") = forAll(legitimateString) { (a: String) => withConnection(conn => {
    val uuid = java.util.UUID.randomUUID()
    val query = "INSERT INTO clients (name) VALUES (".sqlP() + (uuid.toString.sqlV) + ")".sqlP()
    val ps = query.prepareStatement(conn)
    ps.executeUpdate()
    pullFromSql(uuid, conn) == uuid.toString
  }) }

  sealed class ContentType(name: String) extends SqlEnumValue(name)

  object ContentTypes extends SqlEnum[ContentType] {
    case object Javascript extends ContentType("text/javascript")
    case object Html extends ContentType("text/html")
    case object Json extends ContentType("application/json")

    val enumName = "content_type"
    val possibleValues = List(Javascript, Html, Json)
  }

  import ContentTypes._

  property("enum types work") = forAll(legitimateString) { (a: String) => withConnection(implicit conn => {
    val uuid = java.util.UUID.randomUUID().toString
    val query = "INSERT INTO content (content_type, content) VALUES (".sqlP() + ContentTypes.Json + ", ?);".sqlP(uuid)
    query.executeUpdate
    val pullQuery = "SELECT content, content_type::VARCHAR FROM content WHERE content = ?".sqlP(uuid) + " AND content_type = ".sqlP() + ContentTypes.Json
    val rs = pullQuery.executeQuery
    rs.next()
    val content = rs.getString(1)
    val contentType = rs.getEnum(2, classManifest[ContentType])
    (content == uuid) && (contentType == ContentTypes.Json)
  })}
}
