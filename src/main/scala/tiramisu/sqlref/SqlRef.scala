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

trait ConstructsAllRefs[B,T] {
  // Represents a class which can be used to construct ALL references to the same object.
  //
  // For example, consider a table foo. The class FooFull(pk: Long, slug: String) represents a full row from this table.
  // There is a FooRef class (having subclasses FooRefByPrimaryKey(Long) and FooRefBySlug(String)
  //
  // A ConstructsAllRefs[B,T] object would satisfy:
  //    allRefs(fooFull) == Seq(FooRefByLong(fooFull.pk), FooRefBySlug(fooFull.slug))
  def allRefs(b: B): Seq[T]
}
