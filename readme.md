# Tiramisu

Tiramisu is a small library for constructing prepared SQL statements in a more intuitive manner. Specifically, you can construct SQL queries in a manner that looks syntactically like string concatenation:

    val query = "SELECT * FROM user_profiles WHERE user_id = (SELECT id FROM users WHERE user_token = ".sqlP() + userToken.sqlV + ");".sqlP()

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

Import Tiramisu, together with it's syntax:

    import com.chrisstucchio.tiramisu._
    import com.chrisstucchio.tiramisu.ParameterInjector._

The `.sqlP` method creates a Query object from a string:

    "SELECT * FROM foo ".sqlP()

It can also be used to assign parameters:

    "SELECT * FROM foo WHERE slug=? AND token=?".sqlP(foo.slug.sqlV, foo.token.sqlV)

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
