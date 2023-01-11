package algorithms.str

import munit.FunSuite
import algorithms.str.ArabicNumerals.toRoman
import algorithms.str.RomanNumerals.*

class RomanNumeralsSuite extends FunSuite:
  test("toMinimalRomanNumeral") {
    assertEquals(toMinimalRomanNumeral("XXXXVIIII"), Some("XLIX"))
    assertEquals(toMinimalRomanNumeral("XXXXIIIIIIIII"), Some("XLIX"))
    assertEquals(toMinimalRomanNumeral("XXXXVIIII"), Some("XLIX"))
    assertEquals(toMinimalRomanNumeral("XXXXIX"), Some("XLIX"))
    assertEquals(toMinimalRomanNumeral("XLIIIIIIIII"), Some("XLIX"))
    assertEquals(toMinimalRomanNumeral("XLVIIII"), Some("XLIX"))
    assertEquals(toMinimalRomanNumeral("MCCCCCCVI"), Some("MDCVI"))
    assertEquals(toMinimalRomanNumeral("XIIIIII"), Some("XVI"))
    assertEquals(toMinimalRomanNumeral("VVVI"), Some("XVI"))
    assertEquals(toMinimalRomanNumeral("VIIIIIIIIIII"), Some("XVI"))
    assertEquals(toMinimalRomanNumeral(""), Some(""))
    assertEquals(toMinimalRomanNumeral("VIIIAIIIIIIII"), None)
    assertEquals(toMinimalRomanNumeral("MCMLAXXXIV"), None)
  }

  test("toArabic") {
    assertEquals[Option[Long], Option[Long]](toArabic("XLIX"), Some(49L))
    assertEquals[Option[Long], Option[Long]](toArabic("MCMLXXXIV"), Some(1984L))
    assertEquals[Option[Long], Option[Long]](
      toArabic("VIIIIIIIIIII"),
      Some(16L)
    )
    assertEquals(toArabic("MCMLAXXXIV"), None)
  }

  test("toRoman") {
    assertEquals(toRoman(-1), None)
    assertEquals(toRoman(0), Some(""))
    assertEquals(toRoman(49), Some("XLIX"))
    assertEquals(toRoman(1984), Some("MCMLXXXIV"))
  }
