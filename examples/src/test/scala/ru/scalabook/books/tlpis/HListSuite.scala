package ru.scalabook.books.tlpis

import weaver.FunSuite
import ru.scalabook.books.tlpis.HList.*

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

  test("Проверка получения компонентов по вызову head/tail"):
    val s: String  = x.head
    val b: Boolean = x.tail.head
    val d: Double  = x.tail.tail.head
    assert(s == "str")
    assert(b)
    assert(math.abs(d - 1.0) < 0.001)

  test("Проверка декомпозиции с помощью сопоставления с шаблоном"):
    val f: (String :: Boolean :: Double :: HNil.type) => String =
      case "s" :: false :: _        => "test"
      case h :: true :: 1.0 :: HNil => h
      // ошибка компиляции из-за несовпадения конкретных типов и общей длины кортежа
      // case 3 :: "i" :: HNil => "invalid"
      case _ => "unknown"
    assert(f(x) == "str")

  test("Проверка Length на уровне типов"):
    summon[Length[x.type] =:= Nat._3]
    summon[Length[x.tail.type] =:= Nat._2]
    summon[Length[x.tail.tail.type] =:= Nat._1]
    summon[Length[x.tail.tail.tail.type] =:= _0]

  test("Проверка Length на уровне типов, реализованного на свертке слева"):
    summon[LengthViaFoldLeft[x.type] =:= Nat._3]
    summon[LengthViaFoldLeft[x.tail.type] =:= Nat._2]
    summon[LengthViaFoldLeft[x.tail.tail.type] =:= Nat._1]
    summon[LengthViaFoldLeft[x.tail.tail.tail.type] =:= _0]

  test("Проверка значения путем сопоставления с шаблоном"):
    val checkAB = ab match
      case 3 :: "ai4" :: List('r', 'H') :: '3' :: 2 :: 'j' :: "sdfh" :: HNil =>
        true
      case _ => false
    assert(checkAB)

  test("Проверка длины HList"):
    assert(ab.length == 7)

  test("Проверка обратимости HList"):
    val reversed = b.reverse
    val checkReversed = reversed match
      case "sdfh" :: 'j' :: 2 :: '3' :: HNil => true
      case _                                 => false
    assert(checkReversed)

  test("Проверка обращения, а затем добавление HList"):
    val reverseAppend = a reverse_::: b
    val checkReverseAppend = reverseAppend match
      case List('r', 'H') :: "ai4" :: 3 :: '3' :: 2 :: 'j' :: "sdfh" :: HNil =>
        true
      case _ => false
    assert(checkReverseAppend)
