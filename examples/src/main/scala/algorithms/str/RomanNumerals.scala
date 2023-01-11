package algorithms.str

import algorithms.str.RomanNumeralSymbol.*

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
    val min = toMinimalRomanNumeral(roman)
    if min.isEmpty then None
    else
      val temp = min.get + "  "
      var i    = 0
      var sum  = 0L
      while i < min.get.length do
        val symbol = toRomanNumeralSymbol(temp.substring(i, i + 2)).get
        sum += symbol.id
        i += symbol.toString.length
      Some(sum)

end RomanNumerals

object ArabicNumerals:
  def toRoman(number: Long): Option[String] =
    if number < 0 then None
    else if number == 0 then Some("")
    else
      val max =
        RomanNumeralSymbol.values.filter(v => v.id <= number).maxBy(_.id)
      toRoman(number - max.id).map(str => s"$max$str")
