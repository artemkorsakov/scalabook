# Текст

## Наибольшая общая подпоследовательность

**Алгоритм:**

Задача нахождения [наибольшей общей подпоследовательности][LCS] — 
задача поиска последовательности, которая является подпоследовательностью нескольких последовательностей.

**Код:**

```dotty
object Lcs:
  def lcs(a: String, b: String): String =
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
end Lcs
 
Lcs.lcs("nothardlythefinaltest", "zzzfinallyzzz")
// "final"
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

---

**Ссылки:**

- [Наибольшая общая подпоследовательность - Wiki][LCS]

[LCS]: https://ru.wikipedia.org/wiki/%D0%9D%D0%B0%D0%B8%D0%B1%D0%BE%D0%BB%D1%8C%D1%88%D0%B0%D1%8F_%D0%BE%D0%B1%D1%89%D0%B0%D1%8F_%D0%BF%D0%BE%D0%B4%D0%BF%D0%BE%D1%81%D0%BB%D0%B5%D0%B4%D0%BE%D0%B2%D0%B0%D1%82%D0%B5%D0%BB%D1%8C%D0%BD%D0%BE%D1%81%D1%82%D1%8C
