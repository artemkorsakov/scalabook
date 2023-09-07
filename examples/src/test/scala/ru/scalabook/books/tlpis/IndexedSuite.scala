package ru.scalabook.books.tlpis

import munit.FunSuite
import ru.scalabook.books.tlpis.Indexed.given
import ru.scalabook.books.tlpis.Nat.*

class IndexedSuite extends FunSuite:
  private val x = 3 :: true :: "asfd" :: false :: 'k' :: () :: 13 :: 9.3 :: HNil

  test("Получение значения по индексу"):
    val b2 = x.i[_3].at
    println(s"Indexed.example = $b2")

/*


// get the boolean value 'true' at index 3
   /* false */
val b2: Boolean = x.i[_3].at

// drop everything before index 3 and then get the first value
   /* false */
val pre: Boolean = x.i[_3].drop.i[_0].at

// replace the boolean value at index 3 with the integer 19
   /* 3 :: true :: asfd :: 19 :: k :: () :: 13 :: 9.3 :: HNil */
val rep = x.i[_3].replace(19)

// replace the character at index 4 with true if it is lowercase, false otherwise
   /* 3 :: true :: asfd :: false :: true :: () :: 13 :: 9.3 :: HNil */
val mp = x.i[_4].map(_.isLower)

// remove the value at index 5
   /* 3 :: true :: asfd :: false :: k :: 13 :: 9.3 :: HNil */
val rm = x.i[_5].remove

// remove the value at index 2 and insert an HList derived from its value
   /* 3 :: true :: a :: sfd :: false :: k :: () :: 13 :: 9.3 :: HNil */
val fmp = x.i[_2].flatMap( s => s.charAt(0) :: s.substring(1) :: HNil )

// insert a value at the beginning of the HList
   /* List(3, 4) :: 3 :: true :: asfd :: false :: k :: () :: 13 :: 9.3 :: HNil */
val ins0 = x.i[_0].insert(List(3,4))

// insert a value at index 7
   /* 3 :: true :: asfd :: false :: k :: () :: 13 :: -3.0 :: 9.3 :: HNil */
val ins7 = x.i[_7].insert(-3.0f)

// insert an HList at index 3
   /* 3 :: true :: asfd :: h :: false :: Some(3) :: None :: false :: k :: 13 :: 9.3 :: HNil */
val insH = rm.i[_3].insertH( 'h' :: b2 :: Some(3) :: None :: HNil )

// split the HList around index 6
   /* (3 :: true :: asfd :: false :: k :: () :: HNil, 13 :: -3.0 :: 9.3 :: HNil) */
val (aa, bb) = ins7.i[_6].splitAt

// encoding of drop right
   /* 3 :: true :: asfd :: false :: k :: HNil */
val dropRight = x.reverse.i[_3].drop.reverse
 */
