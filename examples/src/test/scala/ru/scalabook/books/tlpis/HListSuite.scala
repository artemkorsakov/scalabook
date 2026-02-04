package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.HList.*
import weaver.FunSuite

object HListSuite extends FunSuite:
  // построение HList подобного Tuple3 ("str", true, 1.0)
  private val x = "str" :: true :: 1.0 :: HNil

  // построение разнородного списка длины 3
  // со следующими типами Int :: String :: List[Char] :: HNil
  private val a = 3 :: "ai4" :: List('r', 'H') :: HNil

  // построение разнородного списка длины 4
  // со следующими типами Char :: Int :: Char :: String :: HNil
  private val b = '3' :: 2 :: 'j' :: "sdfh" :: HNil

  // объединение двух HLists
  private val ab = a ::: b

  test("Checking the receipt of components by calling head/tail"):
    val s: String  = x.head
    val b: Boolean = x.tail.head
    val d: Double  = x.tail.tail.head
    expect.all(s == "str", b, math.abs(d - 1.0) < 0.001)

  test("Decomposition checking using pattern matching"):
    val f: (String :: Boolean :: Double :: HNil.type) => String =
      case "s" :: false :: _        => "test"
      case h :: true :: 1.0 :: HNil => h
      // ошибка компиляции из-за несовпадения конкретных типов и общей длины кортежа
      // case 3 :: "i" :: HNil => "invalid"
      case _ => "unknown"
    expect.all(f(x) == "str")

  test("Checking Length at the type level"):
    summon[Length[x.type] =:= Nat._3]
    summon[Length[x.tail.type] =:= Nat._2]
    summon[Length[x.tail.tail.type] =:= Nat._1]
    summon[Length[x.tail.tail.tail.type] =:= _0]
    success

  test("Checking Length at the type level, implemented on left fold"):
    summon[LengthViaFoldLeft[x.type] =:= Nat._3]
    summon[LengthViaFoldLeft[x.tail.type] =:= Nat._2]
    summon[LengthViaFoldLeft[x.tail.tail.type] =:= Nat._1]
    summon[LengthViaFoldLeft[x.tail.tail.tail.type] =:= _0]
    success

  test("Validating value by pattern matching"):
    val checkAB = ab match
      case 3 :: "ai4" :: List('r', 'H') :: '3' :: 2 :: 'j' :: "sdfh" :: HNil =>
        true
      case _ => false
    expect.all(checkAB)

  test("Checking HList length"):
    expect.all(ab.length == 7)

  test("Checking HList reversibility"):
    val reversed      = b.reverse
    val checkReversed = reversed match
      case "sdfh" :: 'j' :: 2 :: '3' :: HNil => true
      case _                                 => false
    expect.all(checkReversed)

  test("Validating the hit and then adding the HList"):
    val reverseAppend      = a reverse_::: b
    val checkReverseAppend = reverseAppend match
      case List('r', 'H') :: "ai4" :: 3 :: '3' :: 2 :: 'j' :: "sdfh" :: HNil =>
        true
      case _ => false
    expect.all(checkReverseAppend)
end HListSuite
