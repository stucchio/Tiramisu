package com.chrisstucchio.tiramisu

import java.sql._
import org.joda.time.DateTime

trait ParameterInjector[T] {
  def setParam(position: Int, value: T, statement: PreparedStatement): Unit
  def apply(value: T): SqlParameter[T] = SqlParameter(value)(this)
}

case class SqlParameter[T](value: T)(implicit injector: ParameterInjector[T]) extends Query {
  def sqlV: SqlParameter[T] = this // Useful so that x.param will translate to
  def setParam(position: Int, statement: PreparedStatement): Unit = injector.setParam(position, value, statement)

  def params: Seq[SqlParameter[_]] = Seq(this)
  def sql: String = " ? "
}

case class OptionalSqlParameter[T](value: Option[T])(implicit injector: ParameterInjector[T]) extends IsQueryHasQuery {
  def sqlV: Query = this // Used so that x.param will translate to
  protected def innerQuery = value.map( v => "?".sqlP(v)).getOrElse("NULL".sqlP())
}

object ParameterInjectors {
  implicit object LongInjector extends ParameterInjector[Long] {
    def setParam(position: Int, value: Long, statement: PreparedStatement) = statement.setLong(position, value)
  }
  implicit object IntInjector extends ParameterInjector[Int] {
    def setParam(position: Int, value: Int, statement: PreparedStatement) = statement.setInt(position, value)
  }
  implicit object StringInjector extends ParameterInjector[String] {
    def setParam(position: Int, value: String, statement: PreparedStatement) = statement.setString(position, value)
  }
  implicit object DateInjector extends ParameterInjector[DateTime] {
    def setParam(position: Int, value: DateTime, statement: PreparedStatement) = statement.setTimestamp(position, new Timestamp(value.getMillis))
  }
  implicit object BooleanInjector extends ParameterInjector[Boolean] {
    def setParam(position: Int, value: Boolean, statement: PreparedStatement) = statement.setBoolean(position, value)
  }
}

object PostgresqlInjectors {
  implicit object UUIDInjector extends ParameterInjector[java.util.UUID] {
    def setParam(position: Int, value: java.util.UUID, statement: PreparedStatement) = statement.setObject(position, value)
  }
}
