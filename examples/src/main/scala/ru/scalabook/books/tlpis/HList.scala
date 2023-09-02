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

  type Inc = FoldAny[Nat] {
    type Apply[N <: Any, Acc <: Nat] = Succ[Acc]
  }

  type Length[H <: HList]            = Inc#Foldr[H, _0]
  type LengthViaFoldLeft[H <: HList] = Inc#Foldl[H, _0]

  object AppHCons extends FoldAny[HList]:
    type Apply[N <: Any, H <: HList] = N :: H

    // будет использоваться позднее для реализации на уровне значений
    def apply[A, B <: HList](a: A, b: B) = HCons(a, b)

  type :::[A <: HList, B <: HList] =
    AppHCons.Foldr[A, B]

  type Reverse_:::[A <: HList, B <: HList] =
    AppHCons.Foldl[A, B]

  type Reverse[A <: HList] =
    AppHCons.Foldl[A, HNil.type]

  object Length extends FoldAny[Int]:
    type Apply[N <: Any, Acc <: Int] = Int
    def apply[A, B <: Int](a: A, b: B): Apply[Any, Int] = b + 1

  extension [A <: HList](a: A)
    def length: Int = Length.foldr(a, 0)

    def :::[B <: HList](b: B): HList = AppHCons.foldr(a, b)

    def reverse_:::[B <: HList](b: B): HList = AppHCons.foldl(a, b)

    def reverse: HList = AppHCons.foldl(a, HNil)

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

  summon[Length[x.type] =:= Nat._3]
  summon[Length[x.tail.type] =:= Nat._2]
  summon[Length[x.tail.tail.type] =:= Nat._1]
  summon[Length[x.tail.tail.tail.type] =:= _0]

  summon[LengthViaFoldLeft[x.type] =:= Nat._3]
  summon[LengthViaFoldLeft[x.tail.type] =:= Nat._2]
  summon[LengthViaFoldLeft[x.tail.tail.type] =:= Nat._1]
  summon[LengthViaFoldLeft[x.tail.tail.tail.type] =:= _0]

@main def runHListExamples(): Unit =
  import HList.*
  // построение разнородного списка длины 3
  // со следующими типами Int :: String :: List[Char] :: HNil
  val a = 3 :: "ai4" :: List('r', 'H') :: HNil

  // построение разнородного списка длины 4
  // со следующими типами Char :: Int :: Char :: String :: HNil
  val b = '3' :: 2 :: 'j' :: "sdfh" :: HNil

  // объединение двух HLists
  val ab = a ::: b
  // проверка значения путем сопоставления с шаблоном
  val checkAB = ab match
    case 3 :: "ai4" :: List('r', 'H') :: '3' :: 2 :: 'j' :: "sdfh" :: HNil =>
      true
    case _ => false
  require(checkAB)

//  // length of an HList, on both type and value level
//  val 7 = ab.length
//  implicitly[_7 =:= ab.Length]
//
//  // reverse
//  val reversed = b.reverse
//  // check types
//  val checkReversed: String :: Char :: Int :: Char :: HNil = reversed
//  // check values
//  val "sdfh" :: 'j' :: 2 :: '3' :: HNil = reversed
//
//  // last (implementation not shown in this post- it is implemented with a foldl)
//  val last = reversed.last
//  // check type
//  val checkLast: Char = last
//  // check value
//  val '3' = last
//
//  // reverse_:::
//  val reverseAppend = a reverse_::: b
//  // check types
//  val checkReverseAppend: List[Char] :: String :: Int :: Char :: Int :: Char :: String :: HNil = reverseAppend
//  // check values
//  val List('r', 'H') :: "ai4" :: 3 :: '3' :: 2 :: 'j' :: "sdfh" :: HNil = reverseAppend
