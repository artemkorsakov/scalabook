# Работа со строками

## Палиндром

**Алгоритм:**

Проверка, является ли строка палиндромом.

**Оценка:**

- **Время** - \\(O(n)\\)
- **Память** - \\(O(1)\\)

**Код:**

```dotty
def isPalindrome(s: String): Boolean =
  @tailrec
  def loop(i: Int, j: Int): Boolean =
    if i >= j then true
    else if s.charAt(i) != s.charAt(j) then false
    else loop(i + 1, j - 1)

  loop(0, s.length - 1)

isPalindrome("лёша на полке клопа нашёл".replaceAll("\\s", ""))
// true
```

### Самый длинный палиндром в строке

Поиск самого длинного палиндрома в заданной строке 's'. 

[Geeksforgeeks - Longest palindromic subsequence](https://www.geeksforgeeks.org/longest-palindromic-subsequence-dp-12/)

**Оценка:**

- **Время** - \\(O(n^{2})\\)
- **Память** - \\(O(n)\\)

**Код:**

```dotty
def longestPalindrome(s: String): String =
  @tailrec
  def check(i: Int, j: Int): Boolean =
    if i >= j then true
    else if s.charAt(i) == s.charAt(j) then check(i + 1, j - 1)
    else false

  @tailrec
  def search(i: Int, l: Int, j: Int, m: Int): String =
    if i == s.length then s.substring(j - m + 1, j + 1)
    else if i - l > 0 && check(i - l - 1, i) then
      if l + 2 > m then search(i + 1, l + 2, i, l + 2)
      else search(i + 1, l + 2, j, m)
    else if i - l >= 0 && check(i - l, i) then
      if l + 1 > m then search(i + 1, l + 1, i, l + 1)
      else search(i + 1, l + 1, j, m)
    else search(i + 1, 1, j, m)

  if s.isEmpty then s
  else search(1, 1, 1, 1)
end longestPalindrome

longestPalindrome("BBABBCAB")))
// "BBABB"
```

## Наибольшая общая подпоследовательность

**Алгоритм:**

Задача нахождения [наибольшей общей подпоследовательности][LCS] — 
задача поиска последовательности, которая является подпоследовательностью нескольких последовательностей.

**Код:**

```dotty
def longestCommonSubsequence(a: String, b: String): String =
  val C: Array[Array[Int]] = lcsLength(a, b)
  backtrack(C, a, b, a.length, b.length)

private def backtrack(
    C: Array[Array[Int]],
    aStr: String,
    bStr: String,
    x: Int,
    y: Int
): String =
  if x == 0 | y == 0 then ""
  else if aStr(x - 1) == bStr(y - 1) then
    backtrack(C, aStr, bStr, x - 1, y - 1) + aStr(x - 1)
  else if C(x)(y - 1) > C(x - 1)(y) then backtrack(C, aStr, bStr, x, y - 1)
  else backtrack(C, aStr, bStr, x - 1, y)

private def lcsLength(a: String, b: String): Array[Array[Int]] =
  val C = new Array[Array[Int]](a.length + 1)
  C.indices.foreach(i => C(i) = new Array[Int](b.length + 1))
  a.indices.foreach(i => C(i)(0) = 0)
  b.indices.foreach(j => C(0)(j) = 0)
  a.indices.foreach: i =>
    b.indices.foreach: j =>
      C(i + 1)(j + 1) =
        if a(i) == b(j) then C(i)(j) + 1
        else math.max(C(i + 1)(j), C(i)(j + 1))
  C
 
longestCommonSubsequence("anothertest", "notatest") 
// "nottest"
```

## Наибольшая общая подстрока

**Алгоритм:**

[Наибольшая общая подстрока](https://ru.wikipedia.org/wiki/%D0%9D%D0%B0%D0%B8%D0%B1%D0%BE%D0%BB%D1%8C%D1%88%D0%B0%D1%8F_%D0%BE%D0%B1%D1%89%D0%B0%D1%8F_%D0%BF%D0%BE%D0%B4%D1%81%D1%82%D1%80%D0%BE%D0%BA%D0%B0).

- [Наибольшая общая подстрока - GeeksForGeeks](https://www.geeksforgeeks.org/longest-common-substring-dp-29/)
- [Наибольшая общая подстрока- ScalaCaster](https://github.com/vkostyukov/scalacaster/blob/master/src/primitive/Strings.scala#L60)

**Оценка:**

- **Время** - \\(O(mn)\\)
- **Память** - \\(O(mn)\\)

**Код:**

```dotty
def longestCommonSubstring(a: String, b: String): String =
  @tailrec
  def loop(
      i: Int,
      j: Int,
      maxI: Int,
      maxJ: Int,
      acc: Map[(Int, Int), Int]
  ): String =
    val maxLength = acc.getOrElse((maxI, maxJ), 0)
    if i > a.length then
      b.substring(maxJ - maxLength, maxJ)
    else
      val length =
        if i > 0 && j > 0 && a(i - 1) == b(j - 1) then
          acc.getOrElse((i - 1, j - 1), 0) + 1
        else 0
      val nexAcc         = acc + ((i, j) -> length)
      val (nextI, nextJ) = if j == b.length then (i + 1, 0) else (i, j + 1)
      val (nextMaxI, nextMaxJ) =
        if length >= maxLength then (i, j) else (maxI, maxJ)
      loop(nextI, nextJ, nextMaxI, nextMaxJ, nexAcc)
  end loop

  loop(i = 0, j = 0, maxI = 0, maxJ = 0, acc = Map.empty[(Int, Int), Int])

end longestCommonSubstring

longestCommonSubstring("anothertest", "notatest") 
// "test"
```

## Проверка сбалансированности скобок

**Алгоритм:**

Проверка, сбалансированы ли "скобки" в выражении.

**Оценка:**

- **Время** - \\(O(n)\\)
- **Память** - \\(O(n)\\)

**Код:**

```dotty
def validateParenthesis(s: String): Boolean =
  @tailrec
  def loop(i: Int, sum: Int): Boolean =
    if i >= s.length then sum == 0
    else if sum < 0 then false
    else if s(i) == '(' then loop(i + 1, sum + 1)
    else if s(i) == ')' then loop(i + 1, sum - 1)
    else loop(i + 1, sum)
  loop(0, 0)
  
validateParenthesis("(1 + (2 * 3))") // true  
```

## Поиск по шаблону

Учитывая текстовую строку длиной n и шаблон длиной m, найти все вхождения шаблона в текст.

**Алгоритм:**

[Наивный алгоритм поиска по шаблону](https://www.geeksforgeeks.org/naive-algorithm-for-pattern-searching/).

**Оценка:**

- **Время** - \\(O(mn)\\)
- **Память** - \\(O(n)\\)

**Код:**

```dotty
def matchNaive(s: String, p: String): List[Int] =
  @tailrec
  def loop(i: Int, acc: List[Int]): List[Int] =
    if i > s.length - p.length then acc
    else
      matchPattern(i, 0) match
        case Some(j) => loop(i + 1, j :: acc)
        case _       => loop(i + 1, acc)

  @tailrec
  def matchPattern(i: Int, j: Int): Option[Int] =
    if j == p.length then Some(i - p.length)
    else if s.charAt(i) == p.charAt(j) then matchPattern(i + 1, j + 1)
    else None

  loop(0, List.empty)
  
matchNaive("AABAACAADAABAABA", "AABA") 
// List(12, 9, 0)  
```

## Число текстом по-русски

**Алгоритм:**

Преобразование числа в текстовое представление на русском языке.

**Код:**

```dotty
def inRussian(number: Long): Option[String] =
  val maybeResult = number match
    case n if n < 21 =>
      toBasicRussianNumbers(n)
    case n if n < hundred =>
      toBasicRussianNumbers((number / 10) * 10) |+|
        " ".some |+| inRussian(number % 10)
    case n if n < thousand =>
      toBasicRussianNumbers((number / hundred) * hundred) |+|
        " ".some |+| inRussian(number % hundred)
    case n if n < million =>
      val thousands = number / thousand
      val maybeThousandsInRu =
        (thousands % 10, (thousands % hundred) / 10 != 1) match
          case (1, true) => inRussian(thousands - 1) |+| " одна тысяча".some
          case (2, true) => inRussian(thousands - 2) |+| " две тысячи".some
          case (3 | 4, true) => inRussian(thousands) |+| " тысячи".some
          case _             => inRussian(thousands) |+| " тысяч".some
      maybeThousandsInRu |+| " ".some |+| inRussian(number % thousand)
    case n if n < billion     => constructRussian(n, million)
    case n if n < trillion    => constructRussian(n, billion)
    case n if n < quadrillion => constructRussian(n, trillion)
    case _                    => None
  maybeResult.map(_.trim)
end inRussian

private def constructRussian(n: Long, base: Long): Option[String] =
  val whole = n / base
  val suffix =
    (whole % 10, (whole % base) / 10 != 1) match
      case (1, true)         => "".some
      case (2 | 3 | 4, true) => "а".some
      case _                 => "ов".some
  inRussian(whole) |+| " ".some |+|
    toBasicRussianNumbers(base) |+| suffix |+| " ".some |+|
    inRussian(n % base)

private def toBasicRussianNumbers(n: Long): Option[String] =
  n match
    case 0              => "".some
    case 1              => "один".some
    case 2              => "два".some
    case 3              => "три".some
    case 4              => "четыре".some
    case 5              => "пять".some
    case 6              => "шесть".some
    case 7              => "семь".some
    case 8              => "восемь".some
    case 9              => "девять".some
    case 10             => "десять".some
    case 11             => "одиннадцать".some
    case 12             => "двенадцать".some
    case 13             => "тринадцать".some
    case 14             => "четырнадцать".some
    case 15             => "пятнадцать".some
    case 16             => "шестнадцать".some
    case 17             => "семнадцать".some
    case 18             => "восемнадцать".some
    case 19             => "девятнадцать".some
    case 20             => "двадцать".some
    case 30             => "тридцать".some
    case 40             => "сорок".some
    case 50             => "пятьдесят".some
    case 60             => "шестьдесят".some
    case 70             => "семьдесят".some
    case 80             => "восемьдесят".some
    case 90             => "девяносто".some
    case 100            => "сто".some
    case 200            => "двести".some
    case 300            => "триста".some
    case 400            => "четыреста".some
    case 500            => "пятьсот".some
    case 600            => "шестьсот".some
    case 700            => "семьсот".some
    case 800            => "восемьсот".some
    case 900            => "девятьсот".some
    case 1000000        => "миллион".some
    case 1000000000     => "миллиард".some
    case 1000000000000L => "триллион".some
    case _              => none
 
inRussian(19019435)
// "девятнадцать миллионов девятнадцать тысяч четыреста тридцать пять"
```

## Число текстом по-английски

**Алгоритм:**

Преобразование числа в текстовое представление на английском языке.

**Код:**

```dotty
def inEnglish(number: Long): Option[String] =
  number match
    case n if n < 21 || (n < hundred && n % 10 == 0) =>
      toBasicEnglishNumbers(n)
    case n if n < hundred && n % 10 > 0 =>
      toBasicEnglishNumbers((n / 10) * 10) |+| "-".some |+|
        toBasicEnglishNumbers(n % 10)
    case n if n < thousand    => constructEnglish(n, hundred)
    case n if n < million     => constructEnglish(n, thousand)
    case n if n < billion     => constructEnglish(n, million)
    case n if n < trillion    => constructEnglish(n, billion)
    case n if n < quadrillion => constructEnglish(n, trillion)
    case _                    => None

private def constructEnglish(n: Long, base: Long): Option[String] =
  val rest = n % base
  val restText =
    if rest == 0 then "".some
    else if base == hundred then " and ".some |+| inEnglish(rest)
    else " ".some |+| inEnglish(rest)
  inEnglish(n / base) |+| " ".some |+|
    toBasicEnglishNumbers(base) |+| restText

private def toBasicEnglishNumbers(n: Long): Option[String] =
  n match
    case 0              => "".some
    case 1              => "one".some
    case 2              => "two".some
    case 3              => "three".some
    case 4              => "four".some
    case 5              => "five".some
    case 6              => "six".some
    case 7              => "seven".some
    case 8              => "eight".some
    case 9              => "nine".some
    case 10             => "ten".some
    case 11             => "eleven".some
    case 12             => "twelve".some
    case 13             => "thirteen".some
    case 14             => "fourteen".some
    case 15             => "fifteen".some
    case 16             => "sixteen".some
    case 17             => "seventeen".some
    case 18             => "eighteen".some
    case 19             => "nineteen".some
    case 20             => "twenty".some
    case 30             => "thirty".some
    case 40             => "forty".some
    case 50             => "fifty".some
    case 60             => "sixty".some
    case 70             => "seventy".some
    case 80             => "eighty".some
    case 90             => "ninety".some
    case 100            => "hundred".some
    case 1000           => "thousand".some
    case 1000000        => "million".some
    case 1000000000     => "billion".some
    case 1000000000000L => "trillion".some
    case _              => none
 
inEnglish(999999)
// "nine hundred and ninety-nine thousand nine hundred and ninety-nine"
```

## Римские числа

**Алгоритм:**

Приведение римского числа к минимальному виду.

**Код:**

```dotty
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
      
toMinimalRomanNumeral("VIIIIIIIIIII")
// Some("XVI")         
```

Преобразование арабского числа в римское число и обратно.

**Код:**

```dotty
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
         
toArabic("MCMLXXXIV")  // 1984
toRoman(1984)          // "MCMLXXXIV"    
```

---

**Ссылки:**

- [Исходный код][sample]
- [Наибольшая общая подпоследовательность - Wiki][LCS]
- [Римские цифры - Wiki](https://ru.wikipedia.org/wiki/%D0%A0%D0%B8%D0%BC%D1%81%D0%BA%D0%B8%D0%B5_%D1%86%D0%B8%D1%84%D1%80%D1%8B)
- [About... Roman Numerals](https://projecteuler.net/about=roman_numerals)
- [Scalacaster](https://github.com/vkostyukov/scalacaster/tree/master)

[LCS]: https://ru.wikipedia.org/wiki/%D0%9D%D0%B0%D0%B8%D0%B1%D0%BE%D0%BB%D1%8C%D1%88%D0%B0%D1%8F_%D0%BE%D0%B1%D1%89%D0%B0%D1%8F_%D0%BF%D0%BE%D0%B4%D0%BF%D0%BE%D1%81%D0%BB%D0%B5%D0%B4%D0%BE%D0%B2%D0%B0%D1%82%D0%B5%D0%BB%D1%8C%D0%BD%D0%BE%D1%81%D1%82%D1%8C
[sample]: https://gitverse.ru/scalabook/algorithms
