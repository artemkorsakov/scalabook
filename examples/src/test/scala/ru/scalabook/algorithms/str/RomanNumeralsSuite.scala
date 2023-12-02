package ru.scalabook.algorithms.str

import ru.scalabook.algorithms.str.ArabicNumerals.toRoman
import ru.scalabook.algorithms.str.RomanNumerals.*
import weaver.FunSuite

object RomanNumeralsSuite extends FunSuite:
  test("toMinimalRomanNumeral") {
    expect.all(
      toMinimalRomanNumeral("XXXXVIIII").contains("XLIX"),
      toMinimalRomanNumeral("XXXXIIIIIIIII").contains("XLIX"),
      toMinimalRomanNumeral("XXXXVIIII").contains("XLIX"),
      toMinimalRomanNumeral("XXXXIX").contains("XLIX"),
      toMinimalRomanNumeral("XLIIIIIIIII").contains("XLIX"),
      toMinimalRomanNumeral("XLVIIII").contains("XLIX"),
      toMinimalRomanNumeral("MCCCCCCVI").contains("MDCVI"),
      toMinimalRomanNumeral("XIIIIII").contains("XVI"),
      toMinimalRomanNumeral("VVVI").contains("XVI"),
      toMinimalRomanNumeral("VIIIIIIIIIII").contains("XVI"),
      toMinimalRomanNumeral("").contains(""),
      toMinimalRomanNumeral("VIIIAIIIIIIII").isEmpty,
      toMinimalRomanNumeral("MCMLAXXXIV").isEmpty
    )
  }

  test("toArabic"):
    expect.all(
      toArabic("XLIX").contains(49L),
      toArabic("MCMLXXXIV").contains(1984L),
      toArabic("VIIIIIIIIIII").contains(16L),
      toArabic("MCMLAXXXIV").isEmpty
    )

  test("toRoman"):
    expect.all(
      toRoman(-1).isEmpty,
      toRoman(0).contains(""),
      toRoman(49).contains("XLIX"),
      toRoman(1984).contains("MCMLXXXIV")
    )
end RomanNumeralsSuite
