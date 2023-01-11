package algorithms.str

import munit.FunSuite
import algorithms.str.RomanNumeralSymbol
import algorithms.str.RomanNumeralSymbol.*

class RomanNumeralSymbolSuite extends FunSuite:
  test("RomanNumeralSymbol enum") {
    assertEquals(RomanNumeralSymbol.I.id, 1)
    assertEquals(RomanNumeralSymbol.IV.id, 4)
    assertEquals(RomanNumeralSymbol.V.id, 5)
    assertEquals(RomanNumeralSymbol.IX.id, 9)
    assertEquals(RomanNumeralSymbol.X.id, 10)
    assertEquals(RomanNumeralSymbol.XL.id, 40)
    assertEquals(RomanNumeralSymbol.L.id, 50)
    assertEquals(RomanNumeralSymbol.XC.id, 90)
    assertEquals(RomanNumeralSymbol.C.id, 100)
    assertEquals(RomanNumeralSymbol.CD.id, 400)
    assertEquals(RomanNumeralSymbol.D.id, 500)
    assertEquals(RomanNumeralSymbol.CM.id, 900)
    assertEquals(RomanNumeralSymbol.M.id, 1000)
  }

  test("toRomanNumeralSymbol") {
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.I),
      toRomanNumeralSymbol("I")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.I),
      toRomanNumeralSymbol("II")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.I),
      toRomanNumeralSymbol("IL")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.I),
      toRomanNumeralSymbol("III")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.I),
      toRomanNumeralSymbol("IIIL")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.IV),
      toRomanNumeralSymbol("IV")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.V),
      toRomanNumeralSymbol("V")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.V),
      toRomanNumeralSymbol("VV")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.IX),
      toRomanNumeralSymbol("IX")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.X),
      toRomanNumeralSymbol("X")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.XL),
      toRomanNumeralSymbol("XL")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.L),
      toRomanNumeralSymbol("L")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.XC),
      toRomanNumeralSymbol("XC")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.C),
      toRomanNumeralSymbol("C")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.CD),
      toRomanNumeralSymbol("CD")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.D),
      toRomanNumeralSymbol("D")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.CM),
      toRomanNumeralSymbol("CM")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      Some(RomanNumeralSymbol.M),
      toRomanNumeralSymbol("M")
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      toRomanNumeralSymbol("A"),
      None
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      toRomanNumeralSymbol("AB"),
      None
    )
    assertEquals[Option[RomanNumeralSymbol], Option[RomanNumeralSymbol]](
      toRomanNumeralSymbol("ABC"),
      None
    )
  }
