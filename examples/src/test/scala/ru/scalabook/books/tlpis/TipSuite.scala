package ru.scalabook.books.tlpis

import weaver.FunSuite

import scala.language.implicitConversions

object TipSuite extends FunSuite:
  import HList.::

  test("Проверка индексации по типу"):
    val x = 3 :: true :: "asfd" :: 'k' :: () :: 9.3 :: HNil

    // получение значения типа Boolean
    /* true */
    val b2: Boolean = x.t[Boolean].toIndexed.at
    assert(b2)

    // Удалить все перед значением типа String
    val pre = x.t[String].drop
    assertEquals(pre, "asfd" :: 'k' :: () :: 9.3 :: HNil)

    // замена значения типа String на число 19
    val rep = x.t[String].replace(19)
    assertEquals(rep, 3 :: true :: 19 :: 'k' :: () :: 9.3 :: HNil)

    // замена символа Char на true если он в нижнем регистре, false - в ином случае
    val mp = x.t[Char].map(_.isLower)
    assertEquals(mp, 3 :: true :: "asfd" :: true :: () :: 9.3 :: HNil)

    // удаление значения типа Unit
    val rm = x.t[Unit].remove
    assertEquals(rm, 3 :: true :: "asfd" :: 'k' :: 9.3 :: HNil)

    // замена значения типа String на HList, построенный на её значениях
    val fmp = x.t[String].flatMap(s => s.charAt(0) :: s.substring(1) :: HNil)
    assertEquals(fmp, 3 :: true :: 'a' :: "sfd" :: 'k' :: () :: 9.3 :: HNil)

    // вставка значения перед полем типа Int
    val ins0 = x.t[Int].insert(List(3, 4))
    assertEquals(
      ins0,
      List(3, 4) :: 3 :: true :: "asfd" :: 'k' :: () :: 9.3 :: HNil
    )

    // вставка значения перед полем типа  Double
    val ins7 = x.t[Double].insert(-3.0f)
    assertEquals(ins7, 3 :: true :: "asfd" :: 'k' :: () :: -3.0 :: 9.3 :: HNil)

    // вставка HList перед значением типа String
    val insH = x.t[String].insertH('h' :: b2 :: Some(3) :: None :: HNil)
    assertEquals(
      insH,
      3 :: true :: 'h' :: true :: Some(
        3
      ) :: None :: "asfd" :: 'k' :: () :: 9.3 :: HNil
    )

    // разбиение HList перед значением с типом Unit
    val (aa, bb) = x.t[Unit].splitAt
    assertEquals(aa, 3 :: true :: "asfd" :: 'k' :: HNil)
    assertEquals(bb, () :: 9.3 :: HNil)

    // аналог операции удаление справа
    type R = Double :: Unit :: Char :: String :: Boolean :: Int :: HNil.type
    val reversed  = x.reverse.asInstanceOf[R] // scalafix:ok
    val dropRight = reversed.t[Char].drop.reverse
    assertEquals(dropRight, 3 :: true :: "asfd" :: 'k' :: HNil)
