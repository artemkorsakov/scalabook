# Уточняющие типы в Scala

Код примеров написан на Scala версии `3.2.1`, если не сказано иное.

## Зачем уточнять типы?

Набор стандартных типов весьма ограничен и покрывает только самые распространенные ситуации.
Создатели языков программирования не могут, да и не должны предугадывать все типы,
которые будут использоваться разработчиками.
Каждый стандартный тип данных - это достаточно общее множество значений и операций над этими значениями.

Возьмем для примера `String` - строковый тип в Scala. 
Какое множество значений он представляет?
Да все что угодно: практически все алфавиты мира и всевозможные символы, 
абсолютное большинство которых никто никогда в процессе разработки не встречает. Например: `"€‡™µ"`

```scala
val str: String = "€‡™µ"
```

> Вспомним фразу [Ken Scambler](https://github.com/kenbot), процитированную [вот в этом видео](https://www.youtube.com/watch?v=zExb9x3fzKs&t=52s): валиден ли китайский перевод книг Шекспира в качестве входящего параметра `String`?!

В абсолютном большинстве случаев такая "свобода" не нужна.

Давайте, представим, что нам нужно создать множество, которое представляло бы собой всевозможные имена людей, 
написанные кириллицей и начинающиеся с заглавной буквы, 
для использования в заданном классе `Person` в качестве имени человека.

Например, следующий вариант позволителен - `Алёна`,
а вот такие варианты - нет: `€‡™µ`, `12345`, `Alyona`, `Алёна18`, `алёна`.

Но все перечисленные невалидные варианты - это `String`.
Получается, что этот тип описывает не только нужное нам множество, но ещё и множество невалидных вариантов,
что нас не устраивает.

Как можно более четко сформулировать необходимое множество значений?

Псевдонимы типов и "чистые" case классы (или [классы значений](https://docs.scala-lang.org/overviews/core/value-classes.html)) 
не подходят, потому что они представляют собой только "оболочку" над `String`,
и по-прежнему позволяют "подложить" невалидное значение.

```scala
type Name = String
val name: Name = "€‡™µ"
// val name: Name = €‡™µ
```

[Пример в Scastie](https://scastie.scala-lang.org/keYiEtDCQkuOlfzZUVyXjQ)

```scala
case class Name(value: String) extends AnyVal
val name: Name = Name("€‡™µ")
// val name: Name = Name(€‡™µ)
```

[Пример в Scastie](https://scastie.scala-lang.org/CjZpe7ejSwW724prXkQLqg)

Конечно, можно регулировать создание `Name` путем ограничения видимости стандартного конструктора 
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

В этом случае создать невалидное имя напрямую, используя стандартный конструктор, невозможно:

```scala
val name: Name = Name("€‡™µ") // не скомпилируется
```

А его использование, казалось бы, происходит так, как было задумано:

```scala
Name.fromString("€‡™µ")     // None
Name.fromString("12345")    // None
Name.fromString("Alyona")   // None
Name.fromString("Алёна18")  // None
Name.fromString("алёна")    // None
Name.fromString("Алёна")    // Some(Name(Алёна))
```

[Пример в Scastie](https://scastie.scala-lang.org/HvKxeWpVT22Cv50N8GnbCw)

> В Scala 2 этот способ можно "взломать" через метод `copy` (в Scala 3 эту лазейку убрали): 
>
> `Name.fromString("Алёна").map(_.copy("€‡™µ")) // Some(Name(€‡™µ))`
>
> Для запрета на использование метода `copy` или переопределения через наследование 
в Scala 2 требовалось объявлять класс как `sealed abstract`, вот так:
>
> `sealed abstract case class Name private (value: String) extends AnyVal`
>
> [Пример "взлома" через copy в Scala 2 на Scastie](https://scastie.scala-lang.org/9Uzl00WPTs6TJg8jW7GzOg)

## Введение в уточняющие типы

Ещё одним способом решения заданной проблемы может стать библиотека для работы с уточняющими типами [**refined**][refined lib].

В [теории типов](https://en.wikipedia.org/wiki/Type_theory) 
[уточняющий тип (refinement type)](https://en.wikipedia.org/wiki/Refinement_type) — 
это тип, снабженный предикатом, который предполагается верным для любого элемента уточняемого типа. 
Например, натуральные числа больше 5 могут быть описаны так:

![](https://wikimedia.org/api/rest_v1/media/math/render/svg/b3771ee3aa45615fadcccdf42d259f5eb61fd0ea)

Т.о. уточняющий тип - это базовый тип + предикат, 
а значения уточняющего типа - это все значения базового типа, удовлетворяющие определенному предикату. 

Концепция уточняющих типов была впервые введена Фриманом и Пфеннингом в работе 1991 года ["Уточняющие типы для ML"](https://www.cs.cmu.edu/~fp/papers/pldi91.pdf), 
в которой представлена система типов для языка Standard ML.

Библиотека **refined** начиналась как переработка [библиотеки на Haskell Никиты Волкова](http://nikita-volkov.github.io/refined/). 

Самая идея выражения ограничений на уровне типов в виде библиотеки Scala была впервые исследована Flavio W. Brasil
в библиотеке [bond](https://github.com/fwbrasil/bond).

Уточнение - это достаточно распространенная и естественная процедура в программировании.

Достаточно взглянуть на [примитивные типы в Scala](https://docs.scala-lang.org/scala3/book/first-look-at-types.html#scalas-value-types):

`Long` (от **-2<sup>63</sup>** до **2<sup>63</sup> - 1**) -> 
`Int` (от **-2<sup>31</sup>** до **2<sup>31</sup> - 1**) -> 
`Short` (от **-2<sup>15</sup>** до **2<sup>15</sup> - 1**) -> 
`Byte` (от **-2<sup>7</sup>** до **2<sup>7</sup> - 1**)

Каждый следующий тип в этом списке уточняет предыдущий.

## Знакомство с библиотекой

Давайте рассмотрим решение исходной задачки с помощью **refined**:

```scala
import eu.timepit.refined.*
import eu.timepit.refined.api.*
import eu.timepit.refined.string.*

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]
final case class Person(name: Name)
```

В библиотеке **refined** есть класс `RefinedTypeOps`, который определяет метод `upapply`, позволяющий
в сопутствующем объекте определить создание `Person` с валидными значениями, например так:

```scala
object Person:
  object Name extends RefinedTypeOps[Name, String]
  def fromString(str: String): Option[Person] =
    str match
      case Name(name) => Some(Person(name))
      case _          => None
```

Тогда создание экземпляров `Person` происходит так:

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

[Либо использовать неявные преобразования типов.][conversion]

[Разобранный пример в Scala Worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FMotivation.sc&plain=1)


## Обзор библиотеки

У библиотеки достаточно большой набор предопределенных типов и есть, например, метод `refineV`,
возвращающий значение типа `Either[String, T]`, где слева содержится ошибка, если входящее значение 
не удовлетворяет предикату.

Вот несколько примеров использования библиотеки:

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

[Разобранный пример в Scala Worksheet](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FRefinedExamples.sc&plain=1)

[Полный список предопределенных типов (69 типов)](refined/types)

> Примечание для пользователей Scala < 2.13.
> Некоторые уточненные типы принимают параметр, и если вы используете Scala < 2.13, 
> то вам нужно будет использовать shapeless 
> (уже предоставленный усовершенствованной библиотекой в качестве удобного псевдонима `import eu.timepit.refined.W`), 
> чтобы переправить литералы на уровень типа:
> вместо `StartsWith["@"]` надо будет использовать ``StartsWith[W.`"@"`.T]``
> 
> Первоначально эта проблема была рассмотрена в [SIP-23 «Одиночные типы на основе литералов»](https://docs.scala-lang.org/sips/42.type.html).


## А в чем разница?

Здесь может возникнуть резонный вопрос: 
у нас есть два способа определения уточненного типа:

- "стандартный" ---
  `case class Name private (value: String) extends AnyVal`
- через библиотеку **refined** ---
  `type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]`

А в чем принципиальная разница между этими двумя способами? 
Только лишь в удобстве справочника предопределенных типов?

1) Важным преимуществом библиотеки **refined** является возможность проверки типов во время компиляции.
Следующий код на Scala 2 даже не скомпилится:

```scala
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.string._

object Example {
  type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]

  val name0: Name = "€‡™µ"     // Compile error: Predicate failed: "€‡™µ".matches("[А-ЯЁ][а-яё]+").
  val name1: Name = "12345"    // Compile error: Predicate failed: "12345".matches("[А-ЯЁ][а-яё]+").
  val name2: Name = "Alyona"   // Compile error: Predicate failed: "Alyona".matches("[А-ЯЁ][а-яё]+").
  val name3: Name = "Алёна18"  // Compile error: Predicate failed: "Алёна18".matches("[А-ЯЁ][а-яё]+").
  val name4: Name = "алёна"    // Compile error: Predicate failed: "алёна".matches("[А-ЯЁ][а-яё]+").
  val name5: Name = "Алёна"    // Ок
}
```

Скомпилится только последний вариант, потому что строка `"Алёна"` подходит под уточненный тип.

[Разобранный пример на Scastie](https://scastie.scala-lang.org/oqh3jUboQQqf3wKC8A5ZkA)

[В Scala 3 не все так просто.][conversion]

Проверка во время компиляции открывает довольно обширные возможности: 
как минимум, значительную часть проверок можно переложить с модульных тестов на компилятор.
Что в свою очередь может сэкономить общее время разработки.
[По этому поводу написана отличная статья][tests].

2) Ещё одним значительным преимуществом является то, что "типы врать не могут".

При использовании case класса значение имени - это по-прежнему строка:

```scala
Name.fromString("Алёна").map(_.value).getOrElse("")
// val res0: String = Алёна
```

Мы опять получили слишком "широкое" множество значений.
И дальнейшее использование `Name.value` в коде возможно только в качестве `String`.
При этом отброшена потенциально полезная информация о том, какая это строка.

Уточняющий тип же - это конкретный тип:

```scala
Name.unsafeFrom("Алёна") 
// val res0: Name = Алёна
```

И дальше по коду его можно использовать в качестве именно типа, который в данном случае расширяет `String`.

Есть очень хорошая статья по поводу [важности системы типов][thetypesystem].

## Уточнение произвольного типа

Уточняющий тип можно создать для любого типа.

Допустим у нас есть тип и некий предикат для значений заданного типа:

```scala
type Packed = Any

val nonEmpty: Packed => Boolean =
  case str: String => Option(str).exists(_.trim.nonEmpty)
  case num: Int    => num > 0
  case _           => false
```

Уточняющий тип `NonEmpty` для `Packed` можно определить по предикату:

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

Здесь в методе `validate` определяется предикат, который предполагается верным для всех значений заданного типа.

Метод `showExpr` определяет сообщение об ошибке, если переданное значение не удовлетворяет предикату.

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

[Разобранный пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FPackedExamples.sc&plain=1)


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

[Разобранный пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FRefineRefinementTypeExamples.sc&plain=1)


## Литеральные типы

Предельным непустым уточненным типом является литеральный тип:

```scala
var foo: "foo" = "foo"
foo = "foo" // Позволительно
foo = "bar" // Ошибка компиляции
// Type Mismatch Error: -------------------------------------------------
// foo = "bar"
//       ^^^^^
//       Found:    ("bar" : String)
//       Required: ("foo" : String)

var one: 1 = 1
one = 1 // Позволительно
one = 2 // Ошибка компиляции
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

[Разобранный пример на Scastie](https://scastie.scala-lang.org/JdqGqYyQQneu4N8vfku1Kw)

[Разобранный пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FLiteralTypes.sc&plain=1)


## Промежуточные итоги

К основным преимуществам библиотеки **refined** относится:

- Отлов ошибок во время компиляции (пока только в Scala 2)
- Единая декларативная валидация
- Уточнение времени компиляции литеральных значений

К недостаткам же:

- При использовании нотации инфиксного типа необходимо проявлять осторожность 
  и использовать скобки или неинфиксное определение типа:
  - ~~String Refined XXX And YYY~~
  - String Refined And[XXX, YYY]
  - String Refined (XXX And YYY)
- Уточненные примитивы всегда упакованы
- Сообщения об ошибках валидации не всегда понятны, а порой и просто ошибочны: `Empty did not fail`

У библиотеки **refined** есть альтернативы:

- [Bond](https://github.com/fwbrasil/bond)
- [Scalactic](https://www.scalactic.org/)

Библиотека **refined** интегрирована со следующими библиотеками:

- [Полный список](https://github.com/fthomas/refined#using-refined)
  - refined-cats (Scala 3 + Scala 2)
  - refined-eval (только Scala 2)
  - refined-jsonpath (Scala 3 + Scala 2)
  - refined-pureconfig (только Scala 2)
  - refined-scalacheck (Scala 3 + Scala 2)
  - refined-scalaz (только Scala 2)
  - refined-scodec (только Scala 2)
  - refined-scopt (Scala 3 + Scala 2)
  - refined-shapeless (только Scala 2)
- Релизы на [Scaladex](https://index.scala-lang.org/fthomas/refined)

## Накопление ошибок валидации

Вариант использования `refineV`, рассмотренный выше, довольно прост, 
и с ним часто столкнется любой разработчик: получение данных от некоего восходящего потока.

Но мы не хотим, чтобы наш процесс прервался, как только обнаружилась первая ошибка. 
Мы можем быстро реагировать на них и решать проблемы восходящего потока пачками.

По этой причине просто монадическая композиция цепочек `Either` не подходит для нашего варианта использования.
Другими словами, ["ориентированная на железную дорогу"](https://fsharpforfunandprofit.com/rop/) проверка, 
которая останавливается на ошибочном предикате, будет недостаточной.

Давайте сначала рассмотрим проблему накопления ошибок синтаксического анализа/валидации. 
Мы не хотим связывать этапы проверки одного сообщения с другими монадическими способами, 
потому что этот способ возвращал бы только первую из них. 
Вместо этого мы бы хотели, чтобы все эти шаги были на одном уровне. 
Мы должны перенести их все, а затем агрегировать результат либо в желаемую форму, если все прошло хорошо, 
либо в список ошибок в возможных случаях.

Это именно то, что можно сделать с `ValidatedNec` из библиотеки [cats](https://typelevel.org/cats/index.html).

К счастью, **refined** предоставляет расширение **refined-cats**, 
которое позволяет возвращать шаги проверки `ValidatedNec[String, A]` вместо `Either[String, A]`:

```scala
import cats.data.ValidatedNec
import cats.implicits.*
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.cats.syntax.*
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.refineV
import eu.timepit.refined.string.*

import java.util.UUID

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]
object Name extends RefinedTypeOps[Name, String]

type Age = Int Refined Interval.ClosedOpen[7, 77]
object Age extends RefinedTypeOps[Age, Int]

type Id = String Refined Uuid

final case class Person(name: Name, age: Age, id: Id)

object Person:
  def refine(name: String, age: Int, id: String): ValidatedNec[String, Person] =
    (
      Name.validateNec(name),
      Age.validateNec(age),
      refineV[Uuid](id).toValidatedNec
    ).mapN(Person.apply)

Person.refine("Andrew", 150, "id")
// val res0: cats.data.ValidatedNec[String, Person] = 
//   Invalid(Chain(
//     Predicate failed: "Andrew".matches("[А-ЯЁ][а-яё]+")., 
//     Right predicate of (!(150 < 7) && (150 < 77)) failed: Predicate failed: (150 < 77)., 
//     Uuid predicate failed: Invalid UUID string: id
//   ))

Person.refine("Андрей", 50, UUID.randomUUID().toString)
// val res1: cats.data.ValidatedNec[String, Person] = Valid(Person(Андрей,50,fccec68b-cefd-45e8-ae57-b8cdd3fa3cb8))
```

Метод `refinePerson` делает именно то, что нужно: 
применяет все предикаты к входным данным для их проверки, 
а также возвращает более конкретные типы, с которыми можно более безопасно работать в будущем. 

А так как мы используем [Applicative](https://typelevel.org/cats/typeclasses/applicative.html), 
то всегда будут выполняться все этапы "уточнения", 
и в случае неудачи некоторых из них их ошибки будут накапливаться в `NonEmptyChain`.

[Разобранный пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FRefinedWithCatsExamples.sc&plain=1)


## Пример взаимодействия с другими библиотеками

Рассмотрим взаимодействие библиотеки **refined** с [**pureconfig**](https://pureconfig.github.io/). 
На данный момент библиотека [собрана только под Scala 2](https://index.scala-lang.org/fthomas/refined/artifacts/refined-pureconfig?pre-releases=false)
, поэтому будем рассматривать код на этой версии Scala.

Предположим, что у нас есть некий конфиг с настройками подключения к БД:

```text
{
    host: "example.com"
    port: 8080
}
```

Довольно часто в современных проектах используется микросервисная архитектура 
и конфиги разрастаются до огромных размеров, усложняющих их поддержку и нахождения в них ошибок и опечаток.

Что если разработчик указал пустой хост 
(допустим для того, чтобы указать его позже, когда станет известна конфигурация на стенде) 
и опечатался в `port`?!

```text
{
    host: ""
    port: 808
}
```

Даже такая конфигурация будет успешно прочитана:

```scala
import pureconfig._
import pureconfig.generic.auto._

object Main {
  private case class Config(host: String, port: Int)
  
  def main(args: Array[String]): Unit = {
    parseConfig("""{
              host: ""
              port: 808
          }""")
  }
  
  private def parseConfig(source: String): Unit = {
    println("---")
    ConfigSource.string(source).load[Config] match {
      case Right(config) =>
        println(s"Configuration loaded successfully:\n$config")
      case Left(error) =>
        println(s"Error loading configuration:\n$error")
    }
    println("---")
  }
}
```

В этом случае будет выдано сообщение о том, что конфиг задан верно:

```text
---
Configuration loaded successfully:
Config(,808)
---
```

[Разобранный пример на Scastie](https://scastie.scala-lang.org/vq79scDfRyOA4GoDh1MFnw)

Безусловно, ошибка была бы найдена при развертывании на стенде, например, при получении ошибки подключения.
Но сколько времени ушло бы на поиск опечатки в хосте или порту?

Здесь нам очень помогли бы уточняющие типы, более детально описывающие, какая конфигурация требуется заданному стенду.
Уточняющие типы могли бы исключить использование `localhost` на боевом стенде, да и многое другое.

Рассмотрим самый простой пример, уточняющий хост до непустой строки и порт - как число, большее `1024`:

```scala
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Greater
import eu.timepit.refined.pureconfig._
import pureconfig._
import pureconfig.generic.auto._

object Main {
  private type NonEmptyString = String Refined NonEmpty
  private type ServerPort = Int Refined Greater[1024]

  private case class Config(host: NonEmptyString, port: ServerPort)
  
  def main(args: Array[String]): Unit = {
    parseConfig("""{
              host: ""
              port: 808
          }""")
    parseConfig("""{
              host: "example.com"
              port: 8080
          }""")
  }

  private def parseConfig(source: String): Unit = {
    println("---")
    ConfigSource.string(source).load[Config] match {
      case Right(config) =>
        println(s"Configuration loaded successfully:\n$config")
      case Left(error) =>
        println(s"Error loading configuration:\n$error")
    }
    println("---")
  }
}
```

В этом случае успешно распарсился бы только второй конфиг, а в логах было бы выведено следующее:

```text
---
Error loading configuration:
ConfigReaderFailures(
  ... Predicate isEmpty() did not fail.) ... host),
  ... Predicate failed: (808 > 1024).) ... port)
)
---
---
Configuration loaded successfully:
Config(example.com,8080)
---
```

[Разобранный пример на Scastie](https://scastie.scala-lang.org/Lr7fwKLJT7GNZ7TApANjgg)

В лице уточняющих типов мы можем более четко формулировать, какие значения считаются валидными в конфигах
и отсеивать явные опечатки.


## Заключение

Методов борьбы с ошибками в программном обеспечении очень много ввиду важности проблемы.
[Тестирование программного обеспечения](https://ru.wikipedia.org/wiki/%D0%A2%D0%B5%D1%81%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5_%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%BD%D0%BE%D0%B3%D0%BE_%D0%BE%D0%B1%D0%B5%D1%81%D0%BF%D0%B5%D1%87%D0%B5%D0%BD%D0%B8%D1%8F)
является важным (если не самым важным) этапом выпуска продукта.
Почти в каждой компании есть выделенное подразделение QA, порой по численности, знаниям и компетентности
не уступающее подразделению разработки.
Кодовая база тестов порой превышает тестируемый код.

Уточненные типы - безусловно не панацея, а ещё один способ повысить качество выпускаемого продукта.
Уточненные типы имеют огромное значение, когда речь идет о возможности уверенности в нашем коде, 
и часто они могут использоваться без дополнительных тестов.

Уточнение во время синтаксического анализа не только гарантирует, что мы не забудем о невалидных данных, 
но также дает более точные, ограниченные типы для использования во всем нашем коде. 
Эта идея хорошо разобрана [в следующей статье][parse, don’t validate].

Однако необходимо помнить, что _больше не всегда лучше_, когда дело доходит до набора текста. 
Иногда лучше оставить универсальный тип или обобщить его до чего-то более простого, 
когда нижестоящая логика на самом деле не требует уточнения этого типа.

В противном случае ваша логика становится тесно связанной с тем, что вы делаете в данный момент, 
и вы теряете возможность повторного использования, которая так дорога функциональным программистам.

Эмпирическое правило таково: всегда кодируйте то, что нужно вашей логике, а не только то, что вы можете. 
И не бойтесь ослаблять ограничения в дальнейшем.


---

**References:**
- [Refined lib - Github][refined lib]
- Видео:
  - [Better types = fewer tests - Raúl Raja](https://www.youtube.com/watch?v=TScwxX62uig)
  - [Combining Refined Types with Type Class Derivation in Scala - Lawrence Carvalho](https://www.youtube.com/watch?v=Hq2QWbUXKbE&t)
  - [Decorate your types with refined – Frank Thomas](https://www.youtube.com/watch?v=zExb9x3fzKs)
  - [Defusing the configuration time bomb with PureConfig and Refined - Leif Wickland](https://www.youtube.com/watch?v=NjqRi-cF3-g)
  - [Enhancing the type system with Refined Types - Juliano Alves](https://www.youtube.com/watch?v=Fx8WXcAZWuk)
  - [How to Build a Functional API - Julien Truffaut](https://www.youtube.com/watch?v=__zuECMFCRc)
  - [Getting Started with #refined - DevInsideYou](https://www.youtube.com/watch?v=aZsmapo1afQ)
  - [Let The Compiler Help You: How To Make The Most Of Scala’s Typesystem - Markus Hauck](https://www.youtube.com/watch?v=hhXPeuJohM4)
  - [Literal types, what they are good for? - Tamer Abdulradi](https://slideslive.com/38907881/literal-types-what-they-are-good-for)  
  - [Refinement Types - Tipagem ainda mais forte para Scala - Marcelo Gomes](https://www.youtube.com/watch?v=Zq4rkWs_ybA)
  - [Refined types for validated configurations – Viktor Lövgren](https://www.youtube.com/watch?v=C3ciegxMAqA)
  - [Refined Types in Scala - Rock the JVM](https://www.youtube.com/watch?v=IDrGbsupaok)
  - [Security with Scala: Refined Types and Object Capabilities - Will Sargent](https://slideslive.com/38908776/security-with-scala-refined-types-and-object-capabilities) 
  - [Strings are Evil: Methods to hide the use of primitive types - Noel Welsh](https://slideslive.com/38908213/strings-are-evil-methods-to-hide-the-use-of-primitive-types)
  - [Why types matter - Gabriel Volpe](https://www.youtube.com/watch?v=n1Y2V4zCZdQ)
- Статьи:
  - [A simple trick to improve type safety of your Scala code](https://blog.softwaremill.com/a-simple-trick-to-improve-type-safety-of-your-scala-code-ba80559ca092)
  - [How we used Refined to improve type safety and error reporting in Scala](https://engineering.contentsquare.com/2021/scala-refined-types/)
  - [Lightweight Non-Negative Numerics for Better Scala Type Signatures](http://erikerlandson.github.io/blog/2015/08/18/lightweight-non-negative-numerics-for-better-scala-type-signatures/)
  - [On Eliminating Error in Distributed Software Systems][thetypesystem]
  - [Parse, don’t validate][parse, don’t validate]
  - [Refined types, what are they good for?](https://beyondthelines.net/programming/refined-types/)
  - [Refined типы в Scala](https://habr.com/ru/post/574080)
  - [Refinement Types In Practice](https://kwark.github.io/refined-in-practice-bescala/#1) 
  - [Refined Types in Scala](https://blog.rockthejvm.com/refined-types/)
  - [Refined Types in Scala: the Good, the Bad and the Ugly](https://medium.com/swlh/refined-types-the-good-the-bad-and-the-ugly-ee971e5d9137)
  - [Refining your data from configuration to database](https://underscore.io/blog/posts/2017/03/07/refined-data-config-database.html)
  - [Safe, Expressive Code with Refinement Types](https://tech.ovoenergy.com/safe-expressive-code-with-refinement-types/)
  - [Tests - can we have too many?][tests]
  - [Type safety with refined](https://blog.michalp.net/posts/scala/refined/)
  - [Validate Service Configuration in Scala](https://medium.com/se-notes-by-alexey-novakov/validate-service-configuration-in-scala-85f661c4b5a6)
  - [Wtf is Refined?](https://medium.com/@Methrat0n/wtf-is-refined-5008eb233194)


[refined lib]: https://github.com/fthomas/refined
[conversion]: refined/conversionInScala3
[thetypesystem]: https://blog.colinbreck.com/on-eliminating-error-in-distributed-software-systems
[tests]: https://github.com/wjlow/blog/blob/3c27de716b40660801e68561252883fd0428395e/Tests.md
[parse, don’t validate]: https://lexi-lambda.github.io/blog/2019/11/05/parse-don-t-validate/#footnote-ref-1-1
