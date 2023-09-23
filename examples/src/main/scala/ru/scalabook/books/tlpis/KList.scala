package ru.scalabook.books.tlpis

import cats.Id
import ru.scalabook.books.tlpis.HList.::

sealed trait KList[+M[_], HL <: HList]:
  def transform[N[_]](f: [A] => M[A] => N[A]): KList[N, HL]

  def down(f: [A] => M[A] => Id[A]): HL

final case class KCons[H, T <: HList, +M[_]](head: M[H], tail: KList[M, T])
    extends KList[M, H :: T]:
  self =>
  // добавление в начало
  def :^:[N[X] >: M[X], G](g: N[G]): KCons[G, H :: T, N] = KCons(g, self)

  def transform[N[_]](f: [A] => M[A] => N[A]) =
    KCons(f(head), tail transform f)

  def down(f: [A] => M[A] => Id[A]) = HCons(f(head), tail down f)

sealed class KNil extends KList[Nothing, HNil.type]:
  def :^:[M[_], H](h: M[H]): KCons[H, HNil.type, M] = KCons(h, this)

  def transform[N[_]](f: [A] => Nothing => N[A]) = KNil

  def down(f: [A] => Nothing => Id[A]) = HNil

object KNil extends KNil

object KList:
  // более удобный псевдоним для сопоставления с образцом
  val :^: : KCons.type = KCons
