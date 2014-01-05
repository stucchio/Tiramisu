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

  implicit class AllRefsWrapper[B](val b: B) extends AnyVal {
    def allRefs[R](implicit car: ConstructsAllRefs[B,R]) = car.allRefs(b)
    def referencedBy[R, RP >: R](r: R)(implicit car: ConstructsAllRefs[B,RP]) = car.references(b,r)
    def ref[R](implicit car: ConstructsAllRefs[B,R]) = car.primaryRef(b)
  }
}
