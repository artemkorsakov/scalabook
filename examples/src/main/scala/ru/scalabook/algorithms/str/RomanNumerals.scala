package ru.scalabook.algorithms.str

import ru.scalabook.algorithms.str.RomanNumeralSymbol.*

/** Roman Numerals.
  *
  * @see
  *   <a href="https://en.wikipedia.org/wiki/Roman_numerals">Roman Numerals</a>
  * @see
  *   <a href="https://projecteuler.net/about=roman_numerals">About Roman
  *   Numerals</a>
  */
object RomanNumerals:

  def toMinimalRomanNumeral(roman: String): Option[String] =
    if roman.exists(ch => toRomanNumeralSymbol(ch.toString).isEmpty) then None
    else
      Some(
        roman.toUpperCase
          .replaceAll("IIIII", "V")
          .replaceAll("VV", "X")
          .replaceAll("XXXXX", "L")
          .replaceAll("LL", "C")
          .replaceAll("CCCCC", "D")
          .replaceAll("DD", "M")
          .replaceAll("VIIII", "IX")
          .replaceAll("IIII", "IV")
          .replaceAll("LXXXX", "XC")
          .replaceAll("XXXX", "XL")
          .replaceAll("DCCCC", "CM")
          .replaceAll("CCCC", "CD")
      )

  @SuppressWarnings(Array(
    "scalafix:DisableSyntax.var",
    "scalafix:DisableSyntax.while"
  ))
  def toArabic(roman: String): Option[Long] =
    toMinimalRomanNumeral(roman).map: min =>
      val temp = min + "  "
      var i    = 0
      var sum  = 0L
      while i < min.length do
        toRomanNumeralSymbol(temp.substring(i, i + 2)).foreach: symbol =>
          sum += symbol.id
          i += symbol.toString.length
      sum

end RomanNumerals

object ArabicNumerals:
  def toRoman(number: Long): Option[String] =
    if number < 0 then None
    else if number == 0 then Some("")
    else
      val maybeMax =
        RomanNumeralSymbol.values.filter(v => v.id <= number).maxByOption(_.id)
      for
        max  <- maybeMax
        rest <- toRoman(number - max.id)
      yield s"$max$rest"
