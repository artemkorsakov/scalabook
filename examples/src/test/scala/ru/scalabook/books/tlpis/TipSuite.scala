package ru.scalabook.books.tlpis

import weaver.FunSuite

import scala.language.implicitConversions

object TipSuite extends FunSuite:
  import HList.::

  test("Checking indexing by type"):
    val x = 3 :: true :: "asfd" :: 'k' :: () :: 9.3 :: HNil

    // получение значения типа Boolean
    /* true */
    val b2: Boolean = x.t[Boolean].toIndexed.at

    // Удалить все перед значением типа String
    val pre = x.t[String].drop

    // замена значения типа String на число 19
    val rep = x.t[String].replace(19)

    // замена символа Char на true если он в нижнем регистре, false - в ином случае
    val mp = x.t[Char].map(_.isLower)

    // удаление значения типа Unit
    val rm = x.t[Unit].remove

    // замена значения типа String на HList, построенный на её значениях
    val fmp = x.t[String].flatMap(s => s.charAt(0) :: s.substring(1) :: HNil)

    // вставка значения перед полем типа Int
    val ins0 = x.t[Int].insert(List(3, 4))

    // вставка значения перед полем типа  Double
    val ins7 = x.t[Double].insert(-3.0f)

    // вставка HList перед значением типа String
    val insH = x.t[String].insertH('h' :: b2 :: Some(3) :: None :: HNil)

    // разбиение HList перед значением с типом Unit
    val (aa, bb) = x.t[Unit].splitAt

    // аналог операции удаление справа
    type R = Double :: Unit :: Char :: String :: Boolean :: Int :: HNil.type
    val reversed  = x.reverse.asInstanceOf[R] // scalafix:ok
    val dropRight = reversed.t[Char].drop.reverse

    expect.all(
      b2,
      pre == "asfd" :: 'k' :: () :: 9.3 :: HNil,
      rep == 3 :: true :: 19 :: 'k' :: () :: 9.3 :: HNil,
      mp == 3 :: true :: "asfd" :: true :: () :: 9.3 :: HNil,
      rm == 3 :: true :: "asfd" :: 'k' :: 9.3 :: HNil,
      fmp == 3 :: true :: 'a' :: "sfd" :: 'k' :: () :: 9.3 :: HNil,
      ins0 == List(3, 4) :: 3 :: true :: "asfd" :: 'k' :: () :: 9.3 :: HNil,
      ins7 == 3 :: true :: "asfd" :: 'k' :: () :: -3.0 :: 9.3 :: HNil,
      insH == 3 :: true :: 'h' :: true :: Some(
        3
      ) :: None :: "asfd" :: 'k' :: () :: 9.3 :: HNil,
      aa == 3 :: true :: "asfd" :: 'k' :: HNil,
      bb == () :: 9.3 :: HNil,
      dropRight == 3 :: true :: "asfd" :: 'k' :: HNil
    )
