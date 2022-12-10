# Refinement types

Уточняющие типы для Scala.

Библиотека [refined][refined lib] позволяет уточнять типы для отсеивания невалидных значений.

Пример использования:

```scala
import eu.timepit.refined.*
import eu.timepit.refined.api.{RefType, Refined}
import eu.timepit.refined.auto.*
import eu.timepit.refined.boolean.*
import eu.timepit.refined.char.*
import eu.timepit.refined.collection.*
import eu.timepit.refined.generic.*
import eu.timepit.refined.numeric.*
import eu.timepit.refined.string.*
import shapeless.{::, HNil}

val x = 42
refineV[Positive](x)         // Right(42)
refineV[Positive](-x)        // Left("Predicate failed: (-42 > 0).")

refineV[NonEmpty]("Hello")   // Right("Hello")
refineV[NonEmpty]("")        // Left("Predicate isEmpty() did not fail.")

type ZeroToOne = Not[Less[0.0]] And Not[Greater[1.0]]
refineV[ZeroToOne](1.8)      
// Left("Right predicate of (!(1.8 < 0.0) && !(1.8 > 1.0)) failed: Predicate (1.8 > 1.0) did not fail.")

refineV[AnyOf[Digit :: Letter :: Whitespace :: HNil]]('F')   // Right(F)

refineV[MatchesRegex["[0-9]+"]]("123.")                      // Left(Predicate failed: "123.".matches("[0-9]+").)

type Age = Int Refined Interval.ClosedOpen[7, 77]
val userInput                       = 55
val ageEither1: Either[String, Age] = refineV(userInput)                // Right(55)
val ageEither2                      = RefType.applyRef[Age](userInput)  // Right(55)
```

[Пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FExamples.sc&plain=1)


## Boolean предикаты

Список булевых предикатов следующий:

- `True` - постоянный предикат всегда равный `true`
- `False` - постоянный предикат всегда равный `false`
- `Not[P]` - отрицание предиката `P`
- `And[A, B]` - коньюнкция предикатов `A` и `B`
- `Or[A, B]` - дизъюнкция предикатов `A` и `B`
- `Xor[A, B]` - исключительная дизъюнкция предикатов `A` и `B`
- `Nand[A, B]` - инвертированная конъюнкция предикатов `A` и `B`
- `Nor[A, B]` - отрицательная дизъюнкция предикатов `A` и `B`
- `AllOf[PS]` - конъюнкция всех предикатов в `PS`
- `AnyOf[PS]` - дизъюнкция всех предикатов в `PS`
- `OneOf[PS]` - исключительная дизъюнкция всех предикатов в `PS`

[Пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FBooleanExamples.sc&plain=1)


## Numeric предикаты

- `Less[N]` - проверяет, меньше ли числовое значение `N`
- `LessEqual[N]` - проверяет, меньше или равно числовое значение `N`
- `Greater[N]` - проверяет, больше ли числовое значение, чем `N`
- `GreaterEqual[N]` - проверяет, больше или равно числовое значение `N`
- `Positive` - проверяет, положительное ли
- `NonPositive` - проверяет, является ли числовое значение 0 или отрицательным
- `Negative` - проверяет, отрицательное ли значение
- `NonNegative` - проверяет, является ли числовое значение 0 или положительным
- `Interval.Open[L, H]` - проверяет, находится ли числовое значение в интервале `(L, H)`
- `Interval.OpenClosed[L, H]` - проверяет, находится ли числовое значение в интервале `(L, H]`
- `Interval.ClosedOpen[L, H]` - проверяет, находится ли числовое значение в интервале `[L, H)`
- `Interval.Closed[L, H]` - проверяет, находится ли числовое значение в интервале `[L, H]`
- `Modulo[N, O]` - проверяет, является ли остаток числа по модулю `N` равным `O`
- `Divisible[N]` - проверяет, делится ли число на `N` без остатка
- `NonDivisible[N]` - проверяет, не делится ли целое число на `N` без остатка
- `Even` - проверяет, является ли число четным
- `Odd` - проверяет, является ли число нечетным
- `NonNaN` - проверяет, не является ли число с плавающей запятой `NaN`

[Пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FNumericExamples.sc&plain=1)


## Char предикаты

- `Digit` - проверяет, является ли `Char` цифрой
- `Letter` - проверяет, является ли `Char` буквой
- `LetterOrDigit` - проверяет, является ли `Char` буквой или цифрой
- `LowerCase` - проверяет, является ли `Char` символом нижнего регистра
- `UpperCase` - проверяет, является ли `Char` символом верхнего регистра
- `Whitespace` - проверяет, является ли `Char` пробелом

[Пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FCharExamples.sc&plain=1)


## String предикаты

EndsWith[S]: проверяет, заканчивается ли строка суффиксом S.
IPv4: проверяет, является ли строка допустимым IPv4.
IPv6: проверяет, является ли строка допустимым IPv6.
MatchesRegex[S]: проверяет, соответствует ли строка регулярному выражению S.
Regex: проверяет, является ли строка допустимым регулярным выражением.
StartsWith[S]: проверяет, начинается ли строка с префикса S.
Uri: проверяет, является ли строка допустимым URI.
Url: проверяет, является ли строка допустимым URL-адресом.
Uuid: проверяет, является ли строка допустимым UUID
ValidByte: проверяет, является ли строка разборчивым байтом.
ValidShort: проверяет, является ли String разборчивым Short
ValidInt: проверяет, является ли String разборчивым Int
ValidLong: проверяет, является ли String парсируемым Long
ValidFloat: проверяет, является ли String парсируемым числом с плавающей запятой.
ValidDouble: проверяет, является ли String парсируемым Double
ValidBigInt: проверяет, является ли String разборчивым BigInt
ValidBigDecimal: проверяет, является ли String разборчивым BigDecimal
Xml: проверяет, является ли строка правильно сформированным XML
XPath: проверяет, является ли строка допустимым выражением XPath.
Обрезано: проверяет, нет ли в строке начальных или конечных пробелов
HexStringSpec: проверяет, представляет ли строка шестнадцатеричное число.


## Collection предикаты

Содержит [U]: проверяет, содержит ли Iterable значение, равное U.
Count[PA, PC]: подсчитывает количество элементов в Iterable, удовлетворяющих предикату PA, и передает результат предикату PC.
Пусто: проверяет, пуст ли Iterable
NonEmpty: проверяет, не является ли Iterable пустым.
Forall[P]: проверяет, выполняется ли предикат P для всех элементов Iterable.
Exists[P]: проверяет, выполняется ли предикат P для некоторых элементов Iterable.
Head[P]: проверяет, выполняется ли предикат P для первого элемента Iterable.
Index[N, P]: проверяет, выполняется ли предикат P для элемента с индексом N последовательности.
Init[P]: проверяет, выполняется ли предикат P для всех элементов Iterable, кроме последнего.
Last[P]: проверяет, выполняется ли предикат P для последнего элемента Iterable.
Tail[P]: проверяет, выполняется ли предикат P для всех элементов Iterable, кроме первого.
Size[P]: проверяет, удовлетворяет ли размер Iterable предикату P
MinSize[N]: проверяет, больше ли размер Iterable или равен N
MaxSize[N]: проверяет, меньше ли размер Iterable N или равен ему.


## Generic предикаты

Equal[U]: проверяет, равно ли значение U


---

**References:**
- [Github][refined lib]

[refined lib]: https://github.com/fthomas/refined
