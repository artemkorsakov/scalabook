package ru.scalabook.books.tlpis

import cats.Id

object ZipWith:
  def zipWith[HL <: HList, T](
      kl: KList[LazyList, HL]
  )(f: HL => T): LazyList[T] =
    if (anyEmpty(kl))
      LazyList.empty
    else
      LazyList.cons(f(kl down heads), zipWith(kl transform tails)(f))

  def anyEmpty(kl: KList[LazyList, _]): Boolean =
    kl match
      case KCons(head, tail) => head.isEmpty || anyEmpty(tail)
      case _                 => false

  val heads: [A] => LazyList[A] => Id[A] =
    [A] => (list: LazyList[A]) => list.head

  val tails: [A] => LazyList[A] => LazyList[A] =
    [A] => (list: LazyList[A]) => list.tail
