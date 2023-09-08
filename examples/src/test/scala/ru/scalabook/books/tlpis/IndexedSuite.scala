package ru.scalabook.books.tlpis

import munit.FunSuite
import ru.scalabook.books.tlpis.HList.*
import ru.scalabook.books.tlpis.Indexed.given
import ru.scalabook.books.tlpis.Nat.*

class IndexedSuite extends FunSuite:
  private val x = 3 :: true :: "asfd" :: false :: 'k' :: () :: 13 :: 9.3 :: HNil

  test("получение булева значения по индексу 3"):
    val b2: Boolean = x.i[_3].at
    assert(!b2)

  test(
    "удалить все значения перед индексом 3 и затем получить первое значение"
  ):
    val pre: Boolean = x.i[_3].drop.i[_0].at
    assert(!pre)

  test("замена булева значения по индексу 3 на число 19"):
    val rep = x.i[_3].replace(19)
    assertEquals(
      rep,
      3 :: true :: "asfd" :: 19 :: 'k' :: () :: 13 :: 9.3 :: HNil
    )

  test(
    "преобразование значения по индексу 4 на `true` если символ в нижнем регистре, иначе - `false`"
  ):
    val mp = x.i[_4].map(_.isLower)
    assertEquals(
      mp,
      3 :: true :: "asfd" :: false :: true :: () :: 13 :: 9.3 :: HNil
    )

  test("Удаление значения по индексу 5"):
    val rm = x.i[_5].remove
    assertEquals(
      rm,
      3 :: true :: "asfd" :: false :: 'k' :: 13 :: 9.3 :: HNil
    )

  test(
    "преобразование значения по индексу 2 на HList, созданного на основе значения"
  ):
    val fmp = x.i[_2].flatMap(s => s.charAt(0) :: s.substring(1) :: HNil)
    assertEquals(
      fmp,
      3 :: true :: 'a' :: "sfd" :: false :: 'k' :: () :: 13 :: 9.3 :: HNil
    )

  test("вставка значения в начало HList"):
    val ins0 = x.i[_0].insert(List(3, 4))
    assertEquals(
      ins0,
      List(
        3,
        4
      ) :: 3 :: true :: "asfd" :: false :: 'k' :: () :: 13 :: 9.3 :: HNil
    )

  test("вставка значения по индексу 7"):
    val ins7 = x.i[_7].insert(-3.0f)
    assertEquals(
      ins7,
      3 :: true :: "asfd" :: false :: 'k' :: () :: 13 :: -3.0 :: 9.3 :: HNil
    )

//  test("вставка HList по индексу 3"):
//    val rm    = x.i[_5].remove
//    val b2    = x.i[_3].at
//    val value = 'h' :: b2 :: Some(3) :: None :: HNil
//    val insH  = rm.i[_3].insertH(value)
//    assertEquals(
//      insH,
//      3 :: true :: "asfd" :: 'h' :: false :: Some(
//        3
//      ) :: None :: false :: 'k' :: 13 :: 9.3 :: HNil
//    )

//  test("разбиение HList по индексу 6"):
//    val ins7     = x.i[_7].insert(-3.0f)
//    val (aa, bb) = ins7.i[_6].splitAt
//    assertEquals(aa, 3 :: true :: "asfd" :: false :: 'k' :: () :: HNil)
//    assertEquals(bb, 13 :: -3.0 :: 9.3 :: HNil)

  test("аналог удаления справа"):
    type R = HCons[Double, HCons[Int, HCons[Unit, HCons[
      Char,
      HCons[Boolean, HCons[String, HCons[Boolean, HCons[Int, HNil.type]]]]
    ]]]]
    val y         = x.reverse.asInstanceOf[R]
    val dropRight = y.i[_3].drop.reverse
    assertEquals(
      dropRight,
      3 :: true :: "asfd" :: false :: 'k' :: HNil
    )
