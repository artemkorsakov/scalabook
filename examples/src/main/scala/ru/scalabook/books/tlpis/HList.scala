package ru.scalabook.books.tlpis

sealed trait HList

case object HNil extends HList:
  self =>
  def ::[A](v: A): HCons[A, HNil.type] = HCons(v, self)

final case class HCons[H, T <: HList](head: H, tail: T) extends HList:
  self =>
  def ::[A](v: A): HCons[A, HCons[H, T]] = HCons(v, self)

// алиасы для построения типов HList и для сопоставления с шаблоном
object HList:
  type ::[H, T <: HList] = HCons[H, T]
  val :: = HCons

@main def runHList(): Unit =
  import HList.*

  // построение HList подобного Tuple3 ("str", true, 1.0)
  val x = "str" :: true :: 1.0 :: HNil

  // получение компонентов по вызову head/tail
  val s: String  = x.head
  val b: Boolean = x.tail.head
  val d: Double  = x.tail.tail.head
  println(s)
  println(b)
  println(d)

  // ошибка компиляции
  // val e = x.tail.tail.tail.head

  // или декомпозиция с помощью сопоставления с шаблоном
  val f: (String :: Boolean :: Double :: HNil.type) => String =
    case "s" :: false :: _        => "test"
    case h :: true :: 1.0 :: HNil => h
    // ошибка компиляции из-за несовпадения конкретных типов и общей длины кортежа
    // case 3 :: "i" :: HNil => "invalid"
    case _ => "unknown"
  println(f(x))
