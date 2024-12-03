enum RomanNumeralSymbol(val id: Int):
  case I  extends RomanNumeralSymbol(1)
  case IV extends RomanNumeralSymbol(4)
  case V  extends RomanNumeralSymbol(5)
  case IX extends RomanNumeralSymbol(9)
  case X  extends RomanNumeralSymbol(10)
  case XL extends RomanNumeralSymbol(40)
  case L  extends RomanNumeralSymbol(50)
  case XC extends RomanNumeralSymbol(90)
  case C  extends RomanNumeralSymbol(100)
  case CD extends RomanNumeralSymbol(400)
  case D  extends RomanNumeralSymbol(500)
  case CM extends RomanNumeralSymbol(900)
  case M  extends RomanNumeralSymbol(1000)

object RomanNumeralSymbol:
  def toRomanNumeralSymbol(twoChars: String): Option[RomanNumeralSymbol] =
    RomanNumeralSymbol.values.find(_.toString.equals(twoChars)).orElse:
      RomanNumeralSymbol.values.find(_.toString.equals(twoChars.take(1)))

import RomanNumeralSymbol.*

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

    @annotation.tailrec
    def loop(i: Int, sum: Long): Option[Long] =
      if i >= min.length then Some(sum)
      else
        toRomanNumeralSymbol(temp.substring(i, i + 2)) match
          case None => None
          case Some(symbol) =>
            loop(i + symbol.toString.length, sum + symbol.id)

    loop(0, 0L)

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

println(toArabic("MCMLXXXIV"))  // 1984
println(toRoman(1984))          // "MCMLXXXIV"      
