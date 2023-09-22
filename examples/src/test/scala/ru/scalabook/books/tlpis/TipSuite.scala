package ru.scalabook.books.tlpis

import munit.FunSuite

class TipSuite extends FunSuite:
  test("Проверка индексации по типу"):
    val x = 3 :: true :: "asfd" :: 'k' :: () :: 9.3 :: HNil

    // get the Boolean value
    /* true */
    val b2: Boolean = x.t[Boolean].tipToInd.at
    println(b2)

//    // drop everything before the String
//    /* asfd :: k :: () :: 9.3 :: HNil */
//    val pre = x.t[String].drop
//
//    // replace the String value with the integer 19
//    /* 3 :: true :: 19 :: k :: () :: 9.3 :: HNil */
//    val rep = x.t[String].replace(19)
//
//    // replace the Char with true if it is lowercase, false otherwise
//    /* 3 :: true :: asfd :: true :: () :: 9.3 :: HNil */
//    val mp = x.t[Char].map(_.isLower)
//
//    // remove the Unit value
//    /* 3 :: true :: asfd :: k :: 9.3 :: HNil */
//    val rm = x.t[Unit].remove
//
//    // remove the String and insert an HList derived from its value
//    /* 3 :: true :: a :: sfd :: k :: () :: 9.3 :: HNil */
//    val fmp = x.t[String].flatMap(s => s.charAt(0) :: s.substring(1) :: HNil)
//
//    // insert a value before the Int
//    /* List(3, 4) :: 3 :: true :: asfd :: k :: () :: 9.3 :: HNil */
//    val ins0 = x.t[Int].insert(List(3, 4))
//
//    // insert a value before the Double
//    /* 3 :: true :: asfd :: false :: k :: () :: -3.0 :: 9.3 :: HNil */
//    val ins7 = x.t[Double].insert(-3.0f)
//
//    // insert an HList before the String
//    /* 3 :: true :: h :: true :: Some(3) :: None :: asfd :: k :: 9.3 :: HNil */
//    val insH = rm.t[String].insertH('h' :: b2 :: Some(3) :: None :: HNil)
//
//    // split the HList around the Unit value
//    /* (3 :: true :: asfd :: k :: HNil, () :: -3.0 :: 9.3 :: HNil) */
//    val (aa, bb) = ins7.t[Unit].splitAt
//
//    // encoding of drop right
//    /* 3 :: true :: asfd :: k :: HNil */
//    val dropRight = x.reverse.t[Char].drop.reverse
