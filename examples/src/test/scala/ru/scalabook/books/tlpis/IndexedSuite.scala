package ru.scalabook.books.tlpis

import ru.scalabook.books.tlpis.HList.*
import ru.scalabook.books.tlpis.Indexed.given
import ru.scalabook.books.tlpis.Nat.*
import weaver.FunSuite

object IndexedSuite extends FunSuite:
  private val x = 3 :: true :: "asfd" :: false :: 'k' :: () :: 13 :: 9.3 :: HNil

  test("getting boolean value at index 3"):
    val b2: Boolean = x.i[_3].at
    expect.all(!b2)

  test("remove all values before index 3 and then get the first value"):
    val pre: Boolean = x.i[_3].drop.i[_0].at
    expect.all(!pre)

  test("replacing the Boolean value at index 3 with the number 19"):
    val rep = x.i[_3].replace(19)
    expect.all(
      rep == 3 :: true :: "asfd" :: 19 :: 'k' :: () :: 13 :: 9.3 :: HNil
    )

  test(
    "converting the value at index 4 to `true` if the character is lowercase, otherwise `false`"
  ):
    val mp = x.i[_4].map(_.isLower)
    expect.all(
      mp == 3 :: true :: "asfd" :: false :: true :: () :: 13 :: 9.3 :: HNil
    )

  test("Deleting a value at index 5"):
    val rm = x.i[_5].remove
    expect.all(rm == 3 :: true :: "asfd" :: false :: 'k' :: 13 :: 9.3 :: HNil)

  test(
    "converting value at index 2 to HList created from value"
  ):
    val fmp = x.i[_2].flatMap(s => s.charAt(0) :: s.substring(1) :: HNil)
    expect.all(
      fmp == 3 :: true :: 'a' :: "sfd" :: false :: 'k' :: () :: 13 :: 9.3 :: HNil
    )

  test("inserting a value at the beginning of an HList"):
    val ins0 = x.i[_0].insert(List(3, 4))
    expect.all(
      ins0 == List(
        3,
        4
      ) :: 3 :: true :: "asfd" :: false :: 'k' :: () :: 13 :: 9.3 :: HNil
    )

  test("inserting a value at index 7"):
    val ins7 = x.i[_7].insert(-3.0f)
    expect.all(
      ins7 == 3 :: true :: "asfd" :: false :: 'k' :: () :: 13 :: -3.0 :: 9.3 :: HNil
    )

  test("insert HList at index 3"):
    type R = Int :: Boolean :: String :: Boolean :: Char :: Int :: Double ::
      HNil.type
    val rm   = x.i[_5].remove.asInstanceOf[R] // scalafix:ok
    val b2   = x.i[_3].at
    val insH = rm.i[_3].insertH('h' :: b2 :: Some(3) :: None :: HNil)
    expect.all(
      insH ==
        3 :: true :: "asfd" :: 'h' :: false :: Some(
          3
        ) :: None :: false :: 'k' :: 13 :: 9.3 :: HNil
    )

  test("splitting HList at index 6"):
    type R = Int :: Boolean :: String :: Boolean :: Char :: Unit :: Int ::
      Double :: Double :: HNil.type
    val ins7     = x.i[_7].insert(-3.0f).asInstanceOf[R] // scalafix:ok
    val (aa, bb) = ins7.i[_6].splitAt
    expect.all(
      aa == 3 :: true :: "asfd" :: false :: 'k' :: () :: HNil,
      bb == 13 :: -3.0 :: 9.3 :: HNil
    )

  test("analogue of deletion on the right"):
    type R = Double :: Int :: Unit :: Char :: Boolean :: String :: Boolean ::
      Int :: HNil.type
    val y         = x.reverse.asInstanceOf[R] // scalafix:ok
    val dropRight = y.i[_3].drop.reverse
    expect.all(dropRight == 3 :: true :: "asfd" :: false :: 'k' :: HNil)
end IndexedSuite
