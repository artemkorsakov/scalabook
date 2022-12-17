# Refinement types

Уточняющие типы для Scala.

## Зачем уточнять типы?

Рассмотрим стандартный строковый тип в Scala - `String`. 
Какие с ним могут быть проблемы?

- `String` может представлять и содержать все что угодно, например: `"€‡™µ"`

```scala
val b: String = "€‡™µ"
```

В абсолютном большинстве случаев такая "свобода" не нужна.

## Какие могут быть варианты ограничения типа?

Представим, что нам нужно создать переменную, которая представляла бы собой имя человека, 
написанное кириллицей и начинающееся с заглавной буквы, 
для использования в классе `Person` (`case class Person(name: Name)`).

Например, следующий вариант позволителен - `Алёна`,
а вот такие варианты - нет: `€‡™µ`, `12345`, `Alyona`, `Алёна18`, `алёна`.

### Type aliases

Одним из способов решения проблемы могут служить псевдонимы типов:

```scala
type Name = String
final case class Person(name: Name)
```

Но псевдонимы типов - это просто псевдонимы и мы по-прежнему можем использовать в качестве имени все что угодно.

```scala
Person("€‡™µ")    // Person(€‡™µ)
Person("12345")   // Person(12345)
Person("Alyona")  // Person(Alyona)
Person("Алёна18") // Person(Алёна18)
Person("алёна")   // Person(алёна)
Person("Алёна")   // Person(Алёна)
```

Проблема не решена!

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FMotivationTypeAlias.sc&plain=1)

### Case class

Ещё одной попыткой добиться желаемого поведения могут служить case классы:

Можно обернуть строку в `case class` 
и использовать наследование от `AnyVal` для избежания расходов на упаковку или распаковку типов.

```scala
case class Name(value: String) extends AnyVal
```

Но в этом случае мы по-прежнему можем обернуть в `Name` любую строку:

```scala
Person(Name("€‡™µ"))     // Person(Name(€‡™µ))
Person(Name("12345"))    // Person(Name(12345))
Person(Name("Alyona"))   // Person(Name(Alyona))
Person(Name("Алёна18"))  // Person(Name(Алёна18))
Person(Name("алёна"))    // Person(Name(алёна))
Person(Name("Алёна"))    // Person(Name(Алёна))
```

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FMotivationCaseClass.sc&plain=1)

Конечно, мы можем регулировать создание `Name`, путем ограничения видимости стандартного конструктора 
и определения метода создания экземпляра `Name` в сопутствующем объекте:

```scala
final case class Name private (value: String) extends AnyVal
object Name:
  import scala.util.matching.Regex
  private val pattern: Regex                = "[А-ЯЁ][а-яё]+".r
  def fromString(str: String): Option[Name] =
    if pattern.matches(str) then Some(Name(str))
    else None
```

В этом случае создать невалидное имя напрямую невозможно:

```scala
Name("€‡™µ")  // не скомпилируется
```

А создание `Person`, казалось бы, происходит так, как было задумано:

```scala
Name.fromString("€‡™µ").map(Person.apply)     // None
Name.fromString("12345").map(Person.apply)    // None
Name.fromString("Alyona").map(Person.apply)   // None
Name.fromString("Алёна18").map(Person.apply)  // None
Name.fromString("алёна").map(Person.apply)    // None
Name.fromString("Алёна").map(Person.apply)    // Some(Person(Name(Алёна)))
```

Но и этот способ можно "взломать" через метод `copy` (**только в Scala 2**, в Scala 3 эту лазейку убрали):

```scala
Name.fromString("Алёна").map(_.copy("€‡™µ")).map(Person.apply)     // Some(Person(Name(€‡™µ)))
Name.fromString("Алёна").map(_.copy("12345")).map(Person.apply)    // Some(Person(Name(12345)))
Name.fromString("Алёна").map(_.copy("Alyona")).map(Person.apply)   // Some(Person(Name(Alyona)))
Name.fromString("Алёна").map(_.copy("Алёна18")).map(Person.apply)  // Some(Person(Name(Алёна18)))
Name.fromString("Алёна").map(_.copy( "алёна")).map(Person.apply)   // Some(Person(Name(алёна)))
Name.fromString("Алёна").map(_.copy("Алёна")).map(Person.apply)    // Some(Person(Name(Алёна)))
```

Для обхода последней лазейки в Scala 2 требовалось объявлять класс как `sealed abstract`, вот так:

```scala
sealed abstract case class Name private (value: String) extends AnyVal
```

[Scastie](https://scastie.scala-lang.org/tptzXCgMRVy9a7TIgiijeQ)

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FMotivationCCPC.sc&plain=1)

## Введение в уточняющие типы

Ещё одним способом решения заданной проблемы может стать библиотека [refined][refined lib].

[refined][refined lib] — это библиотека Scala для уточнения типов с помощью предикатов уровня типа, 
ограничивающих набор значений, описываемых уточненным типом. 

Она начиналась как переработка [библиотеки на Haskell Никиты Волкова](http://nikita-volkov.github.io/refined/). 

Самая идея выражения ограничений на уровне типов в виде библиотеки Scala была впервые исследована Flavio W. Brasil
в библиотеке [bond](https://github.com/fwbrasil/bond).

Давайте рассмотрим решение исходной задачки с помощью `refined`:

```scala
import eu.timepit.refined.*
import eu.timepit.refined.api.*
import eu.timepit.refined.string.*

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]
final case class Person(name: Name)
```

В библиотеке `refined` есть класс `RefinedTypeOps`, который определяет метод `upapply`, позволяющий
в сопутствующем объекте определить создание `Person` с валидными значениями, например так:

```scala
object Person:
  object Name extends RefinedTypeOps[Name, String]
  def fromString(str: String): Option[Person] =
    str match
      case Name(name) => Some(Person(name))
      case _          => None
```

Тогда создание экземпляров происходит так:

```scala
Person.fromString("€‡™µ")     // None
Person.fromString("12345")    // None
Person.fromString("Alyona")   // None
Person.fromString("Алёна18")  // None
Person.fromString("алёна")    // None
Person.fromString("Алёна")    // Some(Person(Алёна))
```

Мы добились тех же самых результатов с помощью уточняющих типов.

Тип `Name` можно создать из строки напрямую (точнее `Either[String, Name]`):

```scala
Name.from("€‡™µ")     // Left(Predicate failed: "€‡™µ".matches("[А-ЯЁ][а-яё]+").)
Name.from("12345")    // Left(Predicate failed: "12345".matches("[А-ЯЁ][а-яё]+").)
Name.from("Alyona")   // Left(Predicate failed: "Alyona".matches("[А-ЯЁ][а-яё]+").)
Name.from("Алёна18")  // Left(Predicate failed: "Алёна18".matches("[А-ЯЁ][а-яё]+").)
Name.from("алёна")    // Left(Predicate failed: "алёна".matches("[А-ЯЁ][а-яё]+").)
Name.from("Алёна")    // Right(Алёна)
```

Существует и небезопасная конвертация, бросающая исключения в невалидных случаях:

```scala
Name.unsafeFrom("€‡™µ")    // java.lang.IllegalArgumentException: Predicate failed: "€‡™µ".matches("[А-ЯЁ][а-яё]+").
Name.unsafeFrom("12345")   // java.lang.IllegalArgumentException: Predicate failed: "12345".matches("[А-ЯЁ][а-яё]+").
Name.unsafeFrom("Alyona")  // java.lang.IllegalArgumentException: Predicate failed: "Alyona".matches("[А-ЯЁ][а-яё]+").
Name.unsafeFrom("Алёна18") // java.lang.IllegalArgumentException: Predicate failed: "Алёна18".matches("[А-ЯЁ][а-яё]+").
Name.unsafeFrom("алёна")   // java.lang.IllegalArgumentException: Predicate failed: "алёна".matches("[А-ЯЁ][а-яё]+").
Name.unsafeFrom("Алёна")
// val res0: Name = Алёна
```

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FMotivation.sc&plain=1)


## Обзор библиотеки

У библиотеки достаточно большой набор предопределенных типов и есть, например, метод `refineV`,
возвращающий значение типа `Either[String, T]`, где слева содержится ошибка, если входящее значение 
не удовлетворяет предикату.

Вот несколько рассмотренных примеров:

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

// Уточняющие типы можно между собой комбинировать, создавая более сложные типы
type ZeroToOne = Not[Less[0.0]] And Not[Greater[1.0]]
refineV[ZeroToOne](0.8)      // Right(0.8)
refineV[ZeroToOne](1.8)      
// Left("Right predicate of (!(1.8 < 0.0) && !(1.8 > 1.0)) failed: Predicate (1.8 > 1.0) did not fail.")

// Или использовать несколько уточняющих типов
refineV[AnyOf[Digit :: Letter :: Whitespace :: HNil]]('F')   // Right(F)

refineV[MatchesRegex["[0-9]+"]]("123.")                      // Left(Predicate failed: "123.".matches("[0-9]+").)

type Age = Int Refined Interval.ClosedOpen[7, 77]
val userInput                       = 55
val ageEither1: Either[String, Age] = refineV(userInput)                // Right(55)
val ageEither2                      = RefType.applyRef[Age](userInput)  // Right(55)
```

[Пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FExamples.sc&plain=1)

## Список типов

Список предопределенных типов следующий:

### Boolean предикаты

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


### Numeric предикаты

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


### Char предикаты

- `Digit` - проверяет, является ли `Char` цифрой
- `Letter` - проверяет, является ли `Char` буквой
- `LetterOrDigit` - проверяет, является ли `Char` буквой или цифрой
- `LowerCase` - проверяет, является ли `Char` символом нижнего регистра
- `UpperCase` - проверяет, является ли `Char` символом верхнего регистра
- `Whitespace` - проверяет, является ли `Char` пробелом

[Пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FCharExamples.sc&plain=1)


### String предикаты

- `EndsWith[S]` - проверяет, заканчивается ли строка суффиксом `S`
- `StartsWith[S]` - проверяет, начинается ли строка с префикса `S`
- `MatchesRegex[S]` - проверяет, соответствует ли строка регулярному выражению `S`
- `Regex` - проверяет, является ли строка допустимым регулярным выражением
- `IPv4` - проверяет, является ли строка допустимым `IPv4`
- `IPv6` - проверяет, является ли строка допустимым `IPv6`
- `Uri` - проверяет, является ли строка допустимым URI
- `Url` - проверяет, является ли строка допустимым URL-адресом
- `Uuid` - проверяет, является ли строка допустимым UUID
- `XPath` - проверяет, является ли строка допустимым выражением XPath
- `Trimmed` - проверяет, нет ли в строке начальных или конечных пробелов
- `HexStringSpec` - проверяет, представляет ли строка шестнадцатеричное число
- `ValidByte` - проверяет, является ли значение строки валидным типом `Byte`
- `ValidShort` - проверяет, является ли значение строки валидным типом `Short`
- `ValidInt` - проверяет, является ли значение строки валидным типом `Int`
- `ValidLong` - проверяет, является ли значение строки валидным типом `Long`
- `ValidFloat` - проверяет, является ли значение строки валидным типом `Float`
- `ValidDouble` - проверяет, является ли значение строки валидным типом `Double`
- `ValidBigInt` - проверяет, является ли значение строки валидным типом `BigInt`
- `ValidBigDecimal` - проверяет, является ли значение строки валидным типом `BigDecimal`

[Пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FStringExamples.sc&plain=1)


### Collection предикаты

- `Contains[U]` - проверяет, содержит ли `Iterable` значение, равное `U`
- `Count[PA, PC]` - подсчитывает количество элементов в `Iterable`, удовлетворяющих предикату `PA`, и передает результат предикату `PC`
- `Empty` - проверяет, пуст ли `Iterable`
- `NonEmpty` - проверяет, не пустой ли `Iterable`
- `Forall[P]` - проверяет, выполняется ли предикат `P` для всех элементов `Iterable`
- `Exists[P]` - проверяет, выполняется ли предикат `P` для некоторых элементов `Iterable`
- `Head[P]` - проверяет, выполняется ли предикат `P` для первого элемента `Iterable`
- `Index[N, P]` - проверяет, выполняется ли предикат `P` для элемента с индексом `N` последовательности
- `Init[P]` - проверяет, выполняется ли предикат `P` для всех элементов `Iterable`, кроме последнего
- `Last[P]` - проверяет, выполняется ли предикат `P` для последнего элемента `Iterable`
- `Tail[P]` - проверяет, выполняется ли предикат `P` для всех элементов `Iterable`, кроме первого
- `Size[P]` - проверяет, удовлетворяет ли размер `Iterable` предикату `P`
- `MinSize[N]` - проверяет, больше ли размер `Iterable` или равен `N`
- `MaxSize[N]` - проверяет, меньше ли размер `Iterable` или равен `N`

[Пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FCollectionExamples.sc&plain=1)


### Generic предикаты

- `Equal[U]` - проверяет, равно ли значение `U`

[Пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FGenericExamples.sc&plain=1)


## Refined type для произвольного класса

Уточняющие типы можно определить для любого произвольного типа.

Допустим у нас есть класс:

```scala
final case class Packed(value: Any):
  lazy val nonEmpty: Boolean =
    value match
      case str: String => Option(str).exists(_.trim.nonEmpty)
      case num: Int    => num > 0
      case _           => false
```

Уточняющий тип для него можно определить следующим образом:

```scala
import eu.timepit.refined.*
import eu.timepit.refined.api.*
import eu.timepit.refined.boolean.*
import eu.timepit.refined.collection.*

given Validate[Packed, NonEmpty] with
  override type R = NonEmpty
  override def validate(packed: Packed): Res    =
    Result.fromBoolean(packed.nonEmpty, Not(Empty()))
  override def showExpr(packed: Packed): String = s"Empty packed value: $packed"
```

Здесь в методе `validate` определяется предикат, по которому определяется, удовлетворяет ли тип заданному уточнению.

Метод `showExpr` определяет сообщение об ошибке.

Тогда можно использовать уточняющий тип следующим образом:

```scala
refineV[NonEmpty](Packed(null))    // Left(Predicate failed: Empty packed value: Packed(null).)
refineV[NonEmpty](Packed(""))      // Left(Predicate failed: Empty packed value: Packed().)
refineV[NonEmpty](Packed(" "))     // Left(Predicate failed: Empty packed value: Packed( ).)
refineV[NonEmpty](Packed("   "))   // Left(Predicate failed: Empty packed value: Packed(   ).)
refineV[NonEmpty](Packed(0))       // Left(Predicate failed: Empty packed value: Packed(0).)
refineV[NonEmpty](Packed(-42))     // Left(Predicate failed: Empty packed value: Packed(-42).)
refineV[NonEmpty](Packed(true))    // Left(Predicate failed: Empty packed value: Packed(true).)

refineV[NonEmpty](Packed("value")) // Right(Packed(value))
refineV[NonEmpty](Packed(42))      // Right(Packed(42))
```

[Пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FPersonExamples.sc&plain=1)


---

**References:**
- [Refined lib - Github][refined lib]
- Видео:
  - [Better types = fewer tests - Raúl Raja](https://www.youtube.com/watch?v=TScwxX62uig)
  - [Defusing the configuration time bomb with PureConfig and Refined - Leif Wickland](https://www.youtube.com/watch?v=NjqRi-cF3-g)
  - [How to Build a Functional API - Julien Truffaut](https://www.youtube.com/watch?v=__zuECMFCRc)
  - [Let The Compiler Help You: How To Make The Most Of Scala’s Typesystem - Markus Hauck](https://www.youtube.com/watch?v=hhXPeuJohM4)
  - [Literal types, what they are good for? - Tamer Abdulradi](https://slideslive.com/38907881/literal-types-what-they-are-good-for)  
  - [Refinement Types - Tipagem ainda mais forte para Scala - Marcelo Gomes](https://www.youtube.com/watch?v=Zq4rkWs_ybA)
  - [Security with Scala: Refined Types and Object Capabilities - Will Sargent](https://slideslive.com/38908776/security-with-scala-refined-types-and-object-capabilities) 
  - [Strings are Evil: Methods to hide the use of primitive types - Noel Welsh](https://slideslive.com/38908213/strings-are-evil-methods-to-hide-the-use-of-primitive-types)
- Статьи:
  - [Lightweight Non-Negative Numerics for Better Scala Type Signatures](http://erikerlandson.github.io/blog/2015/08/18/lightweight-non-negative-numerics-for-better-scala-type-signatures/)
  - [Refined types, what are they good for?](https://beyondthelines.net/programming/refined-types/)
  - [Refinement Types In Practice](https://kwark.github.io/refined-in-practice-bescala/#1) 
  - [Refining your data from configuration to database](https://underscore.io/blog/posts/2017/03/07/refined-data-config-database.html)
  - [Tests - can we have too many?](https://github.com/wjlow/blog/blob/3c27de716b40660801e68561252883fd0428395e/Tests.md)
  - [Validate Service Configuration in Scala](https://medium.com/se-notes-by-alexey-novakov/validate-service-configuration-in-scala-85f661c4b5a6)


[refined lib]: https://github.com/fthomas/refined
