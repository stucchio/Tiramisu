# Tiramisu

## Installation

Add this to your build.scala file:

    resolvers ++= "chrisstucchio" at "http://maven.chrisstucchio.com/",
    libraryDependencies ++= "com.chrisstucchio" %% "tiramisu" % "0.15"

## Usage

Tiramisu is a small library for constructing prepared SQL statements in an intuitive manner. Specifically, you can construct SQL queries in a manner that looks syntactically like string concatenation:

    val query = """SELECT * FROM user_profiles
                        WHERE user_id = (SELECT id FROM users WHERE user_token = """.sqlP() + userToken.sqlV + ");".sqlP()

This format is handy for programmatically creating complex queries based on input of polymorphic type. Consider the following reference type:

    trait UserIdent
    case class UserIdentByEmail(email: String) extends UserIdent
    case class UserIdentByTwitter(twitterId: Long) extends UserIdent

It would be desirable to be able to write a function of the form:

    def getUserProfile(userIdent: UserIdent): UserProfile

With straight JDBC it would be tricky:

    def getUserProfile(userIdent: UserIdent) = userIdent match {
      case UserIdentByEmail(email) => { ...create a query here.. }
      case UserIdentByEmail(email) => { ...same code, with only one clause changed... }
    }

Using Tiramisu we can simplify this significantly:

    def userIdClause(userIdent: UserIdent) = userIdent match {
      case UserIdentByEmail(email) => "(SELECT id FROM users WHERE email = ?)".sqlP(email)
      case UserIdentByTwitter(twitterId) => "(SELECT id FROM users WHERE twitter_id = ?)".sqlP(twitterId)
    }

    def getUserProfile(userIdent: UserIdent) = {
      val query = "SELECT * FROM user_profiles WHERE user_id = ".sqlP() + userIdClause(userIdent)
    }

The method `userIdClause` can be used elsewhere uniformly.

SQL injection errors are caused by the fact that prepared statements are complex to write, and string concatenation is easy. The purpose of Tiramisu is to make prepared statements as easy to write as code which builds SQL by string concatenation.

## Specifics

Import Tiramisu, together with a collection of ParameterInjector objects (the details of which depend on your specific SQL driver):

    import com.chrisstucchio.tiramisu._
    import com.chrisstucchio.tiramisu.ParameterInjector._

The `.sqlP` method creates a Query object from a string:

    "SELECT * FROM foo ".sqlP()

It can also be used to assign parameters:

    "SELECT * FROM foo WHERE slug=? AND token=?".sqlP(foo.slug, foo.token)

The `.sqlV` method turns many objects into a `SqlParameter` object, which can laso be included in queries by concatenation:

    "SELECT * FROM bar WHERE slug=".sqlP() + slug.sqlV + " AND token=".sqlP() + token.sqlV

Note that although this looks like string interpolation, we are actually parameterizing the slug and token variables. The generated SQL looks something like this:

    SELECT * FROM bar WHERE slug = ? AND token = ?

As a convenience, Query objects have a set of convenience methods which enable us to avoid repetitive boilerplate like `" AND ".sqlP()`:

    val offset: Long = 10
    val limit: Long = 5
    val tokenClause = "token=?".sqlP(token)
    val slugClause = "slug=?".sqlP(slug)

    "SELECT * FROM foo ".sqlP() WHERE tokenClause AND slugClause LIMIT limit OFFSET offset

## ParameterInjector and SqlParameter

A `ParameterInjector[T]` object knows how to insert an object of type `T` into a position in a `PreparedStatement`. Apart from the basic types supported by JDBC, this is generally a property of your specific SQL driver.

The method `t.sqlV` will only work if an implicit `ParameterInjector[T]` is present. This grants a degree of type safety, making it impossible to inject objects of a type that JDBC cannot handle into a prepared statement. A custom `ParameterInjector` can also be created to handle custom types. For example, Tiramisu comes with an implicit converter from a joda `DateTime` object to a `java.sql.Timestamp` object:

    object DateInjector extends ParameterInjector[DateTime] {
      def setParam(position: Int, value: DateTime, statement: PreparedStatement) = statement.setTimestamp(position, new Timestamp(value.getMillis))
    }

## Other utilities

### com.chrisstucchio.tiramisu.utils

One useful utility for improving performance is the `PreparedStatementCachingConnection`, which is an implementation of `java.sql.Connection`. This class creates a prepared statement cache. Consider the following code:

    implicit val conn = GetConnection()
    val query1 = "SELECT * FROM foo WHERE foo_id=?".sqlP(foo1Id)
    val rs = query1.executeQuery
    ...
    val query2 = "SELECT * FROM foo WHERE foo_id=?".sqlP(foo2Id)
    val rs = query1.executeQuery
    ...

Both `query1` and `query2` create the same `PreparedStatement`, resulting in duplication and unnecessary trips across the network. To skip this unnecessary work, simply use:

    implicit val conn = PreparedStatementCachingConnection(GetConnection())

### com.chrisstucchio.tiramisu.utils.wrappers

This package contains simple traits that make it easy to build your own implementation `java.sql.*` objects. For example, to build an implementation of `java.sql.Connection` which measures how much time it spends running methods:

    class TimingConnection(protected val conn: java.sql.Connection) extends ConnectionWrapper {
        var timeSpent: Long = 0
        override protected def methodWrap[t](f: =>T) = {
	    val start = System.currentTimeMillis()
	    val result = f
	    timeSpent += (System.currentTimeMillis() - start)
	    result
	}
    }

The `ConnectionWrapper` trait handles all the boilerplate necessary to implement a `java.sql.Connection`.

    def close(): Unit = methodWrap { conn.close() }
    def commit(): Unit = methodWrap { conn.commit() }
    def createArrayOf(x: String,y: Array[Object]): java.sql.Array = methodWrap { conn.createArrayOf(x,y) }
    ...
