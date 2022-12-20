# Refinement types

Уточняющие типы для Scala.

## Зачем уточнять типы?

Рассмотрим стандартный строковый тип в Scala - `String`. 
Какие с ним могут быть проблемы?

- `String` может представлять и содержать все что угодно, например: `"€‡™µ"`

```scala
val str: String = "€‡™µ"
```

В абсолютном большинстве случаев такая "свобода" не нужна.

## Какие могут быть варианты ограничения типа?

Представим, что нам нужно создать переменную, которая представляла бы собой имя человека, 
написанное кириллицей и начинающееся с заглавной буквы, 
для использования в классе `Person` (`case class Person(name: Name)`).

Например, следующий вариант позволителен - `Алёна`,
а вот такие варианты - нет: `€‡™µ`, `12345`, `Alyona`, `Алёна18`, `алёна`.

### Type aliases

Одним из способов решения проблемы могут послужить псевдонимы типов:

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
и использовать [классы значений](https://docs.scala-lang.org/overviews/core/value-classes.html).

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
Name.fromString("Алёна").map(_.copy("алёна")).map(Person.apply)    // Some(Person(Name(алёна)))
Name.fromString("Алёна").map(_.copy("Алёна")).map(Person.apply)    // Some(Person(Name(Алёна)))
```

Для запрета на использование метода `copy` или переопределения через наследование 
в Scala 2 требовалось объявлять класс как `sealed abstract`, вот так:

```scala
sealed abstract case class Name private (value: String) extends AnyVal
```

[Scastie](https://scastie.scala-lang.org/tptzXCgMRVy9a7TIgiijeQ)

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FMotivationCCPC.sc&plain=1)

## Введение в уточняющие типы

Ещё одним способом решения заданной проблемы может стать библиотека [refined][refined lib].

[refined][refined lib] — это библиотека Scala для работы с уточняющими типами.

В [теории типов](https://en.wikipedia.org/wiki/Type_theory) 
[уточняющий тип (refinement type)](https://en.wikipedia.org/wiki/Refinement_type) — 
это тип, снабженный предикатом, который предполагается верным для любого элемента уточняемого типа. 
Например, натуральные числа больше 5 могут быть описаны так:

![](https://wikimedia.org/api/rest_v1/media/math/render/svg/b3771ee3aa45615fadcccdf42d259f5eb61fd0ea)

Т.о. уточняющий тип - это базовый тип + предикат, 
а значения уточняющего типа - это все значения базового типа, удовлетворяющие определенному предикату. 

Концепция уточняющих типов была впервые введена Фриманом и Пфеннингом в работе 1991 года ["Уточняющие типы для ML"](https://www.cs.cmu.edu/~fp/papers/pldi91.pdf), 
в которой представлена система типов для языка Standard ML.

Библиотека refined начиналась как переработка [библиотеки на Haskell Никиты Волкова](http://nikita-volkov.github.io/refined/). 

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

[Полный список предопределенных типов (69 типов)](refined/types)


## Уточнение произвольного типа

Уточняющий тип можно создать для любого типа.

Допустим у нас есть тип и предикат для заданного типа:

```scala
type Packed = Any

val nonEmpty: Packed => Boolean =
  case str: String => Option(str).exists(_.trim.nonEmpty)
  case num: Int    => num > 0
  case _           => false
```

Уточняющий тип `NonEmpty` для заданного типа можно определить по предикату:

```scala
import eu.timepit.refined.*
import eu.timepit.refined.api.*
import eu.timepit.refined.boolean.*
import eu.timepit.refined.collection.*

given Validate[Packed, NonEmpty] with
  override type R = NonEmpty
  override def validate(packed: Packed): Res    =
    Result.fromBoolean(nonEmpty(packed), Not(Empty()))
  override def showExpr(packed: Packed): String = s"Empty packed value: $packed"
```

Здесь в методе `validate` определяется предикат, по которому определяется, удовлетворяет ли тип заданному уточнению.

Метод `showExpr` определяет сообщение об ошибке.

Тогда можно использовать уточняющий тип следующим образом:

```scala
refineV[NonEmpty](null: Packed)    // Left(Predicate failed: Empty packed value: null.)
refineV[NonEmpty]("": Packed)      // Left(Predicate failed: Empty packed value: .)
refineV[NonEmpty](" ": Packed)     // Left(Predicate failed: Empty packed value:  .)
refineV[NonEmpty]("   ": Packed)   // Left(Predicate failed: Empty packed value:    .)
refineV[NonEmpty](0: Packed)       // Left(Predicate failed: Empty packed value: 0.)
refineV[NonEmpty](-42: Packed)     // Left(Predicate failed: Empty packed value: -42.)
refineV[NonEmpty](true: Packed)    // Left(Predicate failed: Empty packed value: true.)

refineV[NonEmpty]("value": Packed) // Right(value)
refineV[NonEmpty](42: Packed)      // Right(42)
```

[Пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FPackedExamples.sc&plain=1)


## Уточнение уточненного типа

Уточнить можно любой тип, в том числе уточненный - в этом случае он становится базовым для другого типа,
который будет его "уточнять".

Рассмотрим следующую цепочку уточнения:
- Положительные числа
- Четные положительные числа
- Четные положительные числа, делящиеся на 3

```scala
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.boolean.And
import eu.timepit.refined.numeric.{Divisible, Even, Positive}

// Положительные числа
type PositiveInt = Int Refined Positive
object PositiveInt extends RefinedTypeOps[PositiveInt, Int]

PositiveInt.from(-6)                // Left(Predicate failed: (-6 > 0).)
PositiveInt.from(3)                 // Right(3)
PositiveInt.from(4)                 // Right(4)
PositiveInt.from(6)                 // Right(6)

// Четные положительные числа
type PositiveEvenInt = Int Refined (Positive And Even)
object PositiveEvenInt extends RefinedTypeOps[PositiveEvenInt, Int]

PositiveEvenInt.from(-6)            // Left(Left predicate of ((-6 > 0) && (-6 % 2 == 0)) failed: Predicate failed: (-6 > 0).)  
PositiveEvenInt.from(3)             // Left(Right predicate of ((3 > 0) && (3 % 2 == 0)) failed: Predicate failed: (3 % 2 == 0).)
PositiveEvenInt.from(4)             // Right(4)
PositiveEvenInt.from(6)             // Right(6)

// Четные положительные числа, делящиеся на 3
type PositiveDivisibleBySixInt = Int Refined (Positive And Even And Divisible[3])
object PositiveDivisibleBySixInt extends RefinedTypeOps[PositiveDivisibleBySixInt, Int]

PositiveDivisibleBySixInt.from(-6)  // Left(Left predicate of (((-6 > 0) && (-6 % 2 == 0)) && (-6 % 3 == 0)) failed: Left predicate of ((-6 > 0) && (-6 % 2 == 0)) failed: Predicate failed: (-6 > 0).)  
PositiveDivisibleBySixInt.from(3)   // Left(Left predicate of (((3 > 0) && (3 % 2 == 0)) && (3 % 3 == 0)) failed: Right predicate of ((3 > 0) && (3 % 2 == 0)) failed: Predicate failed: (3 % 2 == 0).)
PositiveDivisibleBySixInt.from(4)   // Left(Right predicate of (((4 > 0) && (4 % 2 == 0)) && (4 % 3 == 0)) failed: Predicate failed: (4 % 3 == 0).)
PositiveDivisibleBySixInt.from(6)   // Right(6)
```

[Пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FRefineRefinementType.sc&plain=1)


## Литеральные типы

Предельным непустым уточненным типом является литеральный тип:

```scala
var foo: "foo" = "foo"
foo = "foo" // Позволительно
foo = "bar"
// Type Mismatch Error: -------------------------------------------------
// foo = "bar"
//       ^^^^^
//       Found:    ("bar" : String)
//       Required: ("foo" : String)

var one: 1 = 1
one = 1 // Позволительно
one = 2
// Type Mismatch Error: -------------------------------------------------
// one = 2
//       ^
//       Found:    (2 : Int)
//       Required: (1 : Int)
```

Ограничение типа на использование только литеральных типов реализуется так - `T <: Singleton`, например:

```scala1
case class Narrow[T <: Singleton](var t: T)
Narrow("foo")  // Позволительно
Narrow(1)      // Позволительно

// Попытка использования нелитерального типа, например, String приводит к ошибке компиляции
Narrow("foo": String)
// Narrow("foo": String)
//        ^^^^^^^^^^^^^
//        Found:    String
//        Required: Singleton
```

[Scastie](https://scastie.scala-lang.org/JdqGqYyQQneu4N8vfku1Kw)

[Пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FLiteralTypes.sc&plain=1)


## Преимущества библиотеки refined

- Отлов ошибок во время компиляции
- Единая декларативная валидация
- Уточнение времени компиляции литеральных значений


## Недостатки библиотеки refined

В библиотеке уточненных типов присутствуют следующие ошибки/недостатки:

- При использовании нотации инфиксного типа необходимо проявлять осторожность 
  и использовать скобки или неинфиксное определение типа:
  - ~~String Refined XXX And YYY~~
  - String Refined And[XXX, YYY]
  - String Refined (XXX And YYY)
- Уточненные примитивы всегда упакованы
- Сообщения об ошибках проверки не всегда понятны, а порой просто ошибочны: `Empty did not fail`


## Альтернативы библиотеки refined

- [Bond](https://github.com/fwbrasil/bond)
- [Scalactic](https://www.scalactic.org/)


## Интеграция с другими библиотеками

- [Полный список](https://github.com/fthomas/refined#using-refined)
  - refined-cats
  - refined-eval
  - refined-jsonpath
  - refined-pureconfig
  - refined-scalacheck
  - refined-scalaz
  - refined-scodec
  - refined-scopt
  - refined-shapeless



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
