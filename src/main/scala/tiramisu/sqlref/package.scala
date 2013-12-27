package com.chrisstucchio.tiramisu

package object sqlref  {
  class PolymorphicRef[T](ref: T, pmrt: PolymorphicRefType[T]) {
    def pk: Query = pmrt.pkClause(ref)
  }

  class SingleTablePolymorphicRef[T](ref: T, pmrt: PolymorphicSingleTableRefType[T]) {
    def rowIdent: Query = pmrt.rowIdentifyingClause(ref)
  }

  implicit def polymorphicRef[T](ref: T)(implicit pmrt: PolymorphicRefType[T]) = new PolymorphicRef[T](ref, pmrt)
  implicit def singleTablePolymorphicRef[T](ref: T)(implicit pmrt: PolymorphicSingleTableRefType[T]) = new SingleTablePolymorphicRef[T](ref, pmrt)
}
