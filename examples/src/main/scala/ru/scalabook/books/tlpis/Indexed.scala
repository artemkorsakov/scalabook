package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.HList.*

import scala.annotation.tailrec

sealed trait Indexed:
  self =>

  @tailrec
  final def withIndex[R](f: (HList, Any, HList) => R): R =
    self match
      case Indexed0(hc) => f(HNil, hc.head, hc.tail)
      case IndexedN(h, iTail) =>
        iTail.withIndex((before, at, after) => f(HCons(h, before), at, after))

  // получение элемента по индексу
  def at = withIndex((_, a, _) => a)

  // удаление всех элементов перед заданным индексом
  def drop = withIndex((_, a, t) => HCons(a, t))

  // получение всех элементов перед заданным индексом
  def take = withIndex((b, _, _) => b)

  // замена значения текущего индекса на 'a'
  def replace[A](a: A) = withIndex((b, _, t) => b ::: HCons(a, t))

  // удаление значения текущего индекса
  def remove = withIndex((b, _, t) => b ::: t)

  // применение функции 'f' к текущему значению
  def map[B](f: Any => B) = withIndex((b, a, t) => b ::: HCons(f(a), t))

  // применение функции 'f', возвращающей HList, к текущему значению
  // и вставка полученного списка на место текущего индекса
  def flatMap[B <: HList](f: Any => B) =
    withIndex((b, a, t) => b ::: f(a) ::: t)

  // вставка заданного значения
  def insert[C](c: C) =
    withIndex((b, a, t) => b ::: HCons(c, HCons(a, t)))

  // вставка заданного списка HList в текущий индекс
  def insertH[C <: HList](c: C) =
    withIndex((b, a, t) => b ::: c ::: HCons(a, t))

  // возвращает список HList, лежащий перед текущим индексом, и HList, начинающийся с заданного индекса
  def splitAt = withIndex((b, a, t) => (b, HCons(a, t)))

end Indexed

final case class Indexed0[H, T <: HList](hc: H :: T)       extends Indexed
final case class IndexedN[H, I <: Indexed](h: H, iTail: I) extends Indexed

//type BeforeI[I <: Indexed] <: HList =
//  I match
//    case Indexed0[_, _] => HNil.type
//    case IndexedN[h, i] => HCons[h, Before[i]]
//
//type AtI[I <: Indexed] =
//  I match
//    case Indexed0[h, _] => h
//    case IndexedN[_, i] => At[i]
//
//type AfterI[I <: Indexed] <: HList =
//  I match
//    case Indexed0[_, t] => t
//    case IndexedN[_, i] => After[i]
