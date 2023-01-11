package algorithms.str

/** I = 1 V = 5 X = 10 L = 50 C = 100 D = 500 M = 1000
  */
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
    RomanNumeralSymbol.values
      .find(v => v.toString.equals(twoChars))
      .orElse(
        RomanNumeralSymbol.values.find(v =>
          v.toString.equals(twoChars.head.toString)
        )
      )
