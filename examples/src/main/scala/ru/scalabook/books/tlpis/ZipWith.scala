package ru.scalabook.books.tlpis

import cats.Id

import scala.annotation.tailrec

object ZipWith:
  def zipWith[HL <: HList, T](
      kl: KList[LazyList, HL]
  )(f: HL => T): LazyList[T] =
    if anyEmpty(kl) then
      LazyList.empty
    else
      LazyList.cons(f(kl down heads), zipWith(kl transform tails)(f))

  @tailrec
  def anyEmpty(kl: KList[LazyList, ?]): Boolean =
    kl match
      case KCons(head, tail) => head.isEmpty || anyEmpty(tail)
      case _                 => false

  val heads: [A] => LazyList[A] => Id[A] =
    [A] => (list: LazyList[A]) => list.head

  val tails: [A] => LazyList[A] => LazyList[A] =
    [A] => (list: LazyList[A]) => list.tail

  def zipped[HL <: HList](kl: KList[LazyList, HL]): LazyList[HL] =
    zipWith(kl)(x => x)

  def foreach[HL <: HList, T](kl: KList[LazyList, HL])(f: HL => T): Unit =
    zipped(kl).foreach(f)

  def collect[HL <: HList, T](kl: KList[LazyList, HL])(
      f: HL => Option[T]
  ): LazyList[T] =
    zipped(kl).collect { case hl if f(hl).isDefined => f(hl).get }

  def flatMap[HL <: HList, T](kl: KList[LazyList, HL])(
      f: HL => LazyList[T]
  ): LazyList[T] =
    zipped(kl).flatMap(f)

  def forall[HL <: HList](kl: KList[LazyList, HL])(f: HL => Boolean): Boolean =
    zipped(kl).forall(f)

  def exists[HL <: HList](kl: KList[LazyList, HL])(f: HL => Boolean): Boolean =
    zipped(kl).exists(f)
