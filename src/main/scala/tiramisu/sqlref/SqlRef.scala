package com.chrisstucchio.tiramisu.sqlref

import com.chrisstucchio.tiramisu._

case class SqlTable(tableName: String, pkFieldName: String = "id")

trait PolymorphicRefType[T] {
  def table: SqlTable
  def pkClause(ref: T): Query
}

trait PolymorphicSingleTableRefType[T] extends PolymorphicRefType[T] {
  def rowIdentifyingClause(ref: T): Query
  def pkClause(ref: T): Query = ("(SELECT " + table.pkFieldName + " FROM " + table.tableName + " WHERE ").sqlP() + rowIdentifyingClause(ref) + ")".sqlP()
}
