package com.chrisstucchio.tiramisu

import java.sql._

class SqlEnumValue(val name: String)

trait SqlEnum[T <: SqlEnumValue] {
  val enumName: String
  val possibleValues: List[T]

  lazy val fromString: Map[String,T] = possibleValues.map(x => (x.name, x)).toMap

  implicit class SqlEnumValueToQuery(v: T) extends Query {
    def sql: String = "'" + v.name + "'::" + enumName
    val params = Seq()
  }

  implicit class ResultSetPullEnum(rs: ResultSet) {
    def getEnum(i: Int, mf: ClassManifest[T]): T = fromString(rs.getString(i))
  }

  implicit object ParameterInjector extends ParameterInjector[T] {
    def setParam(position: Int, value: T, statement: PreparedStatement) = statement.setString(position, value.name)
  }
}
