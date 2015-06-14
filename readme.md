# Tiramisu

## Installation

Add this to your build.scala file:

    resolvers ++= "chrisstucchio" at "http://maven.chrisstucchio.com/",
    libraryDependencies ++= "com.chrisstucchio" %% "tiramisu" % "0.19"

## Usage

Tiramisu is a small library for constructing prepared SQL statements in an intuitive manner. Specifically, you can construct SQL queries in a manner that looks syntactically like string concatenation:

    val query = """SELECT * FROM user_profiles
                        WHERE user_id = (SELECT id FROM users WHERE user_token = """.sqlP() + userToken.sqlV + ");".sqlP()

This format is handy for programmatically creating complex queries. See the section on Polymorphic References for an example of where this comes in very handy.

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

## Polymorphic References

The package `com.chrisstucchio.tiramisu.sqlref` provides support for Polymorphic Reference types. A polymorphic reference is a product type which uniquely identifies a row in a SQL table. For example, consider the table:

    CREATE TABLE sites (
      id BIGINT PRIMARY KEY NOT NULL,
      domain VARCHAR(256) NOT NULL UNIQUE,
      uuid UUID NOT NULL UNIQUE
    )

The polymorphic ref type in scala might look like this:

    sealed trait SiteRef
    case class SiteRefByDomain(domain: String) extends SiteRef
    case class SiteRefByUUID(uuid: UUID) extends SiteRef

The polymorphic ref typeclass would then be:

    object SiteRefPolymorphicRefType[SiteRef] {
      val table = SqlTable("sites", "id")
      def pkClause(ref: SiteRef): Query = ref match {
        case SiteRefByDomain(domain) => "(SELECT id FROM sites WHERE domain=?)".sqlP(domain)
        case SiteRefByUUID(uuid) => "(SELECT id FROM sites WHERE uuid=?)".sqlP(uuid)
      }
    }

You can then use this typeclass as follows:

    val query = "SELECT * FROM content WHERE content.site_id = ".sqlP() + siteRef.pk

The val `siteRef` can be either a `SiteRefByDomain` or a `SiteRefByUUID`. The generate SQL will then be:

    "SELECT * FROM content WHERE content.site_id = (SELECT id FROM sites WHERE domain=?)"

or

    "SELECT * FROM content WHERE content.site_id = (SELECT id FROM sites WHERE uuid=?)"

### Caching based on Polymorphic References

It might be suggested that polymorphic references are a problem for caching, and this suggestion is correct. However, this problem can be solved for functions which return a complete object, i.e. an object from which all polymorphic references can be constructed. We have a typeclass which handles this:

    trait ConstructsAllRefs[-B,R] {
      def allRefs(b: B): NonEmptyList[R]
    }

The `allRefs` function returns a `NonEmptyList` because it doesn't make sense for an object to have no reference.

Given this typeclass, we have a set of helper traits which can be mixed into any cache. The first trait is the `CacheStore[K,V]` which handles the details of putting objects *into* the cache:

    trait CacheStore[K,V] {
      protected def putInternal(k: K, v: V)
      def put(k: K, v: V): Unit = putInternal(k,v)
      def invalidate(k: K): Unit
    }

Note the absence of a get method.

For caches of complete rows, we then have the `FullRowSqlCache`:

    trait FullRowSqlCache[K,V] extends CacheStore[K,V] {
      protected val refConstructor: ConstructsAllRefs[VP,K]

      def putObj(v: V): Unit = refConstructor.allRefs(v).foreach(k => putInternal(k,v))
      def invalidateObj(v: V): Unit = refConstructor.allRefs(v).foreach(invalidate)
    }

If you provide this trait a `ConstructsAllRefs[V,K]` instance in addition to the standard `CacheStore` methods, then you have the power to invalidate all references for the object.

Finally, there are traits which handle retrieving objects *from* the cache. The simplest is `SqlCache`, which provides the method `get(k: K): Option[V]`. A more interesting one is `SqlFunctorCache`:

    trait SqlFunctorCache[M[_],K,V] extends CacheStore[K,V] {
      protected implicit val mFunctor: Functor[M]

      def get(k: K): Option[M[V]]
      def getCertain(k: K)(fallback: =>M[V]): M[V] = {
        get(k).getOrElse({
          val result: M[V] = fallback
          result.map( r => put(k,r) )
          result
        })
      }
    }

This is useful for retrieval methods which are wrapped in a functor. The standard example of this is a cache which returns a `Future`, rather than a value:

    trait SqlFutureCache[K,V] extends SqlFunctorCache[Future,K,V] {
      protected implicit val executionContext: ExecutionContext
      protected implicit object mFunctor extends Functor[Future] {
        def map[A, B](fa: Future[A])(f: A => B): Future[B] = fa.map(f)
      }
    }

## Other utilities

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
