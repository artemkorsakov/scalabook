package ru.scalabook.algorithms.str

import ru.scalabook.algorithms.str.RomanNumeralSymbol.*
import scala.annotation.tailrec

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

  def toArabic(roman: String): Option[Long] =
    toMinimalRomanNumeral(roman).flatMap: min =>
      val temp = min + "  "

      @tailrec
      def loop(i: Int, sum: Long): Option[Long] =
        if i >= min.length then Some(sum)
        else
          toRomanNumeralSymbol(temp.substring(i, i + 2)) match
            case None => None
            case Some(symbol) =>
              loop(i + symbol.toString.length, sum + symbol.id)

      loop(0, 0L)

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
