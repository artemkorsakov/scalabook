package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.HCons.*
import ru.scalabook.books.tlpis.HList.*
import ru.scalabook.books.tlpis.HNil.*

sealed trait Indexed:
  type Before <: HList
  type After <: HList
  type At

  def withIndex[R](f: (Before, At, After) => R): R

  // получение элемента по индексу
  def at: At = withIndex((_, a, _) => a)

  // удаление всех элементов перед заданным индексом
  def drop: HCons[At, After] =
    withIndex((_, a, t) => HCons(a, t))

  // получение всех элементов перед заданным индексом
  def take: Before =
    withIndex((b, _, _) => b)

  // замена значения текущего индекса на 'a'
  def replace[A](a: A): HList =
    withIndex((b, _, t) => b ::: HCons(a, t))

  // удаление значения текущего индекса
  def remove: HList =
    withIndex((b, _, t) => b ::: t)

  // применение функции 'f' к текущему значению
  def map[B](f: At => B): HList =
    withIndex((b, a, t) => b ::: HCons(f(a), t))

  // применение функции 'f' к текущему значению, возвращающей HList,
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

object Indexed:
  import ru.scalabook.books.tlpis.HList.*

  val example: Indexed {
    type Before = Int :: Boolean :: HNil.type; type After = Char :: HNil.type;
    type At     = String
  } =
    new Indexed:
      type Before = Int :: Boolean :: HNil.type
      type After  = Char :: HNil.type
      type At     = String

      def withIndex[R](f: (Before, At, After) => R): R =
        f(3 :: true :: HNil, "blue", 'h' :: HNil)
