# Tiramisu

Tiramisu is a small library for constructing prepared SQL statements to be passed into [Anorm](https://github.com/playframework/Play20/wiki/ScalaAnorm). The motivation behind Tiramisu is that while Anorm is a decent library for interacting with SQL, assorted utilities are often necessary to *construct* the SQL to be passed into Anorm.

One particular difficulty is that Anorm requires two parameters - a prepared SQL statement containing dynamic parameters, and a separate parameter list. *I completely ignore the case where string variables are deliberately inserted into SQL, since down that path lies SQL injection.* Helper functions often must then return pairs of values:

    def sqlHelper(obj: FooClass): (String, Seq[(String, anorm.ParameterValue[_])] =
      (" slug={obj_slug} AND security_token={obj_security_token} ",
       Seq("obj_slug" -> obj.slug, "obj_security_token" -> obj.token))

Then when they are used, code becomes rather convoluted:

    val baseSql = "SELECT * FROM foo WHERE "
    val (objClause, objParams) = sqlHelper(obj)
    val finalSql = baseSQL + objClause

    SQL(finalSql).on(objParams: _*)()(dbConnection)

Although for a simple query like this, one could simply in-line the parameters in baseSQL, for more complicated queries (where the parameters vary), that isn't possible:

    def sqlHelper(login: AuthToken): (String, Seq[(String, anorm.ParameterValue[_])]) = login match {
      case BasicToken(username, password) => (
          "(foo.user_id IN (SELECT user_id FROM basic_auth WHERE username={username} AND password={password}))",
          Seq("username" -> username, "password" -> password)
	  )
      case FacebookToken(token) => (
            "(foo.user_id IN (SELECT user_id FROM facebook_auth WHERE token={token}))",
            Seq("token" -> token)
	  )
      case Unauthenticated => ("(foo.is_public)", Seq())
    }

The goal of Tiramisu is to combine SQL and the parameter list into a single object. Then SQL and the necessary parameters can be seamlessly combined, resulting in cleaner code:

    def sqlHelper(obj: FooClass): Query = " slug={obj_slug} AND security_token={obj_security_token} ".sqlP("obj_slug" -> obj.slug, "obj_security_token" -> obj.token)

Then this is used as follows:

    val query = "SELECT * FROM foo WHERE ".sql |+| sqlHelper(obj)
    Tiramisu.Select(query) //Uses the implicit dbConnection

## Specifics

Import Tiramisu, together with it's syntax:

    import com.chrisstucchio.tiramisu._
    import com.chrisstucchio.tiramisu.Syntax._

The `.sql` method creates a Query object from a string:

    "SELECT * FROM foo ".sql

The `.sqlP` method creates a query object and assigns parameters:

    "SELECT * FROM foo WHERE slug={slug} AND token={token}".sqlP("slug" -> foo.slug, "token" -> foo.token)

The `.sqlV` method turns any `anorm.ParameterValue[_]` object into a query with that object into the parameter. It's best illustrated via example:

    "SELECT * FROM bar WHERE slug=".sql |+| slug.sqlV |+| " AND token=".sql |+| token.sqlV

Note that although this looks like string interpolation, we are actually parameterizing the slug and token variables. The generated SQL looks something like this (though the parameter names will vary):

    "SELECT * FROM bar WHERE
         slug={336cf1ee_7599_49d3_b1a5_626ab58319ee} AND
             token={8495c6ce_30f0_4908_b8d9_7ccbac1d78a9}"

As a convenience, Query objects have a set of convenience methods which enable us to avoid repetitive boilerplate like `" AND ".sql`:

    val offset: Long = 10
    val limit: Long = 5
    val tokenClause = "token={token}".sqlP("token" -> token)
    val slugClause = "slug={slug}".sqlP("slug -> slug)
q
    "SELECT * FROM foo ".sql WHERE tokenClause AND slugClause LIMIT limit OFFSET offset

It is often convenient to construct queries via the `String.format` method. In ordinary SQL this is dangerous, since it conflates interpolating *values* with interpolating *sql statements*. Tiramisu allows formatting, but you must specify whether you are interpolating code or data. The `formatS` method specifies you wish to insert SQL:

    "SELECT * FROM foo WHERE id = %s".sql.formatS(
        "(SELECT foo_id FROM join_table WHERE slug={slug})".sqlP("slug" -> slug)
	)

This will substitute the specified query for `%s` and it will ALSO combine the parameter lists.

The `formatV` method specifies you wish to insert data:

    "SELECT * FROM foo WHERE id=%s AND slug=%s".sql.formatV(id, slug)

The resulting SQL will be `"SELECT * FROM foo WHERE id={336cf1ee_7599_49d3_b1a5_626ab58319ee} AND slug={8495c6ce_30f0_4908_b8d9_7ccbac1d78a9}"`, and the parameters to `formatV` will be the parameter values.
