package com.chrisstucchio.tiramisu.sqlref

import com.chrisstucchio.tiramisu._
import scala.concurrent.{Future, ExecutionContext}
import scalaz._
import Scalaz._

trait CacheStore[K,V] {
  // handles the putting and getting of a cache
  protected def putInternal(k: K, v: V)
  def put(k: K, v: V): Unit = putInternal(k,v)
  def invalidate(k: K): Unit
}

trait FullRowSqlCache[K,V, VP >: V] extends CacheStore[K,V] {
  // Given the ability to construct all references from an object
  // we can now populate all cache keys rather than just a single one.
  protected val refConstructor: ConstructsAllRefs[VP,K]

  def putObj(v: V): Unit = refConstructor.allRefs(v).foreach(k => putInternal(k,v))
  override def put(k: K, v: V): Unit = putObj(v)
  def invalidateObj(v: V): Unit = refConstructor.allRefs(v).foreach(invalidate)
}

trait SqlCache[K,V] extends CacheStore[K,V] {
  def get(k: K): Option[V]
}

trait SqlFunctorCache[M[_],K,V] extends CacheStore[K,V] {
  // Useful for, e.g. caches that return Futures
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

trait SqlFutureCache[K,V] extends SqlFunctorCache[Future,K,V] {
  protected implicit val executionContext: ExecutionContext
  protected implicit object mFunctor extends Functor[Future] {
    def map[A, B](fa: Future[A])(f: A => B): Future[B] = fa.map(f)
  }
}
