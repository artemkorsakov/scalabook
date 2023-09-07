package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.HList.*

sealed trait Indexed:
  type Before <: HList
  type At
  type After <: HList

  def withIndex[R](f: (Before, At, After) => R): R

  // получение элемента по индексу
  def at: At = withIndex((_, a, _) => a)

  // удаление всех элементов перед заданным индексом
  def drop: HCons[At, After] = withIndex((_, a, t) => HCons(a, t))

  // получение всех элементов перед заданным индексом
  def take: Before = withIndex((b, _, _) => b)

  // замена значения текущего индекса на 'a'
  def replace[A](a: A): HList = withIndex((b, _, t) => b ::: HCons(a, t))

  // удаление значения текущего индекса
  def remove: HList = withIndex((b, _, t) => b ::: t)

  // применение функции 'f' к текущему значению
  def map[B](f: At => B): HList = withIndex((b, a, t) => b ::: HCons(f(a), t))

  // применение функции 'f', возвращающей HList, к текущему значению
  // и вставка полученного списка на место текущего индекса
  def flatMap[B <: HList](f: At => B): HList =
    withIndex((b, a, t) => b ::: f(a) ::: t)

  // вставка заданного значения
  def insert[C](c: C): HList =
    withIndex((b, a, t) => b ::: HCons(c, HCons(a, t)))

  // вставка заданного списка HList в текущий индекс
  def insertH[C <: HList](c: C): HList =
    withIndex((b, a, t) => b ::: c ::: HCons(a, t))

  // возвращает список HList, лежащий перед текущим индексом, и HList, начинающийся с заданного индекса
  def splitAt: (Before, ::[At, After]) =
    withIndex((b, a, t) => (b, HCons(a, t)))

end Indexed

final case class Indexed0[H, T <: HList](hc: H :: T) extends Indexed:
  type Before = HNil.type
  type At     = H
  type After  = T

  def withIndex[R](f: (Before, At, After) => R): R = f(HNil, hc.head, hc.tail)

final case class IndexedN[H, I <: Indexed](h: H, iTail: I) extends Indexed:
  type Before = H :: iTail.type#Before
  type At     = iTail.type#At
  type After  = iTail.type#After

  def withIndex[R](f: (Before, At, After) => R): R =
    iTail.withIndex((before, at, after) => f(HCons(h, before), at, after))

object Indexed:
  given indexed0[H, T <: HList]: (HCons[H, T] => Indexed0[H, T]) =
    hc => new Indexed0[H, T](hc)

  given indexedN[H, T <: HList, I <: Indexed](using
      iTail: T => I
  ): (HCons[H, T] => IndexedN[H, I]) =
    hc => new IndexedN[H, I](hc.head, iTail(hc.tail))
