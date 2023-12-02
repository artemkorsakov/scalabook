package ru.scalabook.algorithms.str

import ru.scalabook.algorithms.str.RomanNumeralSymbol.*
import weaver.FunSuite

object RomanNumeralSymbolSuite extends FunSuite:
  test("RomanNumeralSymbol enum"):
    expect.all(
      RomanNumeralSymbol.I.id == 1,
      RomanNumeralSymbol.IV.id == 4,
      RomanNumeralSymbol.V.id == 5,
      RomanNumeralSymbol.IX.id == 9,
      RomanNumeralSymbol.X.id == 10,
      RomanNumeralSymbol.XL.id == 40,
      RomanNumeralSymbol.L.id == 50,
      RomanNumeralSymbol.XC.id == 90,
      RomanNumeralSymbol.C.id == 100,
      RomanNumeralSymbol.CD.id == 400,
      RomanNumeralSymbol.D.id == 500,
      RomanNumeralSymbol.CM.id == 900,
      RomanNumeralSymbol.M.id == 1000
    )

  test("toRomanNumeralSymbol"):
    expect.all(
      toRomanNumeralSymbol("I").contains(RomanNumeralSymbol.I),
      toRomanNumeralSymbol("II").contains(RomanNumeralSymbol.I),
      toRomanNumeralSymbol("IL").contains(RomanNumeralSymbol.I),
      toRomanNumeralSymbol("III").contains(RomanNumeralSymbol.I),
      toRomanNumeralSymbol("IIIL").contains(RomanNumeralSymbol.I),
      toRomanNumeralSymbol("IV").contains(RomanNumeralSymbol.IV),
      toRomanNumeralSymbol("V").contains(RomanNumeralSymbol.V),
      toRomanNumeralSymbol("VV").contains(RomanNumeralSymbol.V),
      toRomanNumeralSymbol("IX").contains(RomanNumeralSymbol.IX),
      toRomanNumeralSymbol("X").contains(RomanNumeralSymbol.X),
      toRomanNumeralSymbol("XL").contains(RomanNumeralSymbol.XL),
      toRomanNumeralSymbol("L").contains(RomanNumeralSymbol.L),
      toRomanNumeralSymbol("XC").contains(RomanNumeralSymbol.XC),
      toRomanNumeralSymbol("C").contains(RomanNumeralSymbol.C),
      toRomanNumeralSymbol("CD").contains(RomanNumeralSymbol.CD),
      toRomanNumeralSymbol("D").contains(RomanNumeralSymbol.D),
      toRomanNumeralSymbol("CM").contains(RomanNumeralSymbol.CM),
      toRomanNumeralSymbol("M").contains(RomanNumeralSymbol.M),
      toRomanNumeralSymbol("A").isEmpty,
      toRomanNumeralSymbol("AB").isEmpty,
      toRomanNumeralSymbol("ABC").isEmpty
    )
end RomanNumeralSymbolSuite
