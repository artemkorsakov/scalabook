# Уточняющие типы в Scala 3

## Введение

Набор стандартных типов весьма ограничен и покрывает только самые распространенные ситуации.
Каждый стандартный тип данных - это достаточно общее множество значений и операций над этими значениями.
Возьмем для примера `String` - строковый тип в Scala. 
Какое множество значений он представляет?
Да все что угодно: практически все алфавиты мира и всевозможные символы, 
абсолютное большинство которых никто никогда в процессе разработки не встречает. Например: `"€‡™µ"`

```scala
val str: String = "€‡™µ"
```

> Вспомним фразу [Ken Scambler](https://github.com/kenbot), процитированную [вот в этом видео](https://www.youtube.com/watch?v=zExb9x3fzKs&t=52s): валиден ли китайский перевод книг Шекспира в качестве входящего параметра типа `String`?!

В абсолютном большинстве случаев такая "свобода" не нужна.

Давайте, представим, что нам нужно создать множество, которое представляло бы собой всевозможные имена людей, 
написанные кириллицей и начинающиеся с заглавной буквы, 
для дальнейшего использования.

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

"Стандартное" решение - регулировать создание `Name` путем ограничения видимости конструктора по умолчанию
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

В этом случае нельзя создать невалидное имя напрямую, используя стандартный конструктор:

```scala
val name: Name = Name("€‡™µ") // не скомпилируется
```

А его использование происходит так, как было задумано:

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

## Уточняющие типы

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

## Знакомство с библиотекой refined

Давайте рассмотрим решение исходной задачки с помощью **refined**.
Вот так можно объявить уточненный тип:

```scala
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]
```

В библиотеке **refined** есть класс `RefinedTypeOps`, реализующий методы конвертации из базового типа в уточненный:

```scala
object Name extends RefinedTypeOps[Name, String]
```

Метод `from` возвращает `Either[String, Name]`, где слева - ошибка предиката, а справа - уточненный тип:

```scala
Name.from("€‡™µ")     // Left(Predicate failed: "€‡™µ".matches("[А-ЯЁ][а-яё]+").)
Name.from("12345")    // Left(Predicate failed: "12345".matches("[А-ЯЁ][а-яё]+").)
Name.from("Alyona")   // Left(Predicate failed: "Alyona".matches("[А-ЯЁ][а-яё]+").)
Name.from("Алёна18")  // Left(Predicate failed: "Алёна18".matches("[А-ЯЁ][а-яё]+").)
Name.from("алёна")    // Left(Predicate failed: "алёна".matches("[А-ЯЁ][а-яё]+").)
Name.from("Алёна")    // Right(Алёна)
```

`unsafeFrom` - небезопасная конвертация, бросающая исключения в невалидных случаях:

```scala
Name.unsafeFrom("€‡™µ")    // java.lang.IllegalArgumentException: Predicate failed: "€‡™µ".matches("[А-ЯЁ][а-яё]+").
Name.unsafeFrom("12345")   // java.lang.IllegalArgumentException: Predicate failed: "12345".matches("[А-ЯЁ][а-яё]+").
Name.unsafeFrom("Alyona")  // java.lang.IllegalArgumentException: Predicate failed: "Alyona".matches("[А-ЯЁ][а-яё]+").
Name.unsafeFrom("Алёна18") // java.lang.IllegalArgumentException: Predicate failed: "Алёна18".matches("[А-ЯЁ][а-яё]+").
Name.unsafeFrom("алёна")   // java.lang.IllegalArgumentException: Predicate failed: "алёна".matches("[А-ЯЁ][а-яё]+").
Name.unsafeFrom("Алёна")
// val res0: Name = Алёна
```

В Scala 2 библиотека **refined** позволяла неявное преобразование типов `val name: Name = "€‡™µ"`, 
которое выдавало ошибку компиляции в случае, если значение базового типа не удовлетворяло предикату.

В Scala 3 
[неявные преобразования типов довольно сильно переработаны](https://scalabook.gitflic.space/scala/abstractions/ca-implicit-conversions).

Для того чтобы позволить неявное преобразование из `String` в `Name`
нужно для начала определить соответствующий `given` экземпляр [как показано в документации](https://docs.scala-lang.org/scala3/book/ca-implicit-conversions.html)

При этом преобразования типов будут происходить во время выполнения, а не компиляции, поэтому этот способ (из `String` в `Name`) небезопасен.
Но можно определить неявное преобразование в `Option[Name]`, что позволяет выполнять такое присваивание:

```scala
given Conversion[String, Option[Name]] = Name.unapply(_)

val name0: Option[Name] = "€‡™µ"    // None 
val name1: Option[Name] = "12345"   // None
val name2: Option[Name] = "Alyona"  // None
val name3: Option[Name] = "Алёна18" // None
val name4: Option[Name] = "алёна"   // None
val name5: Option[Name] = "Алёна"   // Some(Алёна)
```

[Пример в Scastie](https://scastie.scala-lang.org/0dUop1dQQLmAn0hfclbqTQ)

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FMotivation.worksheet.sc&plain=1)

## Обзор библиотеки

У библиотеки [достаточно большой набор предопределенных типов (69)](refined/types) 
и есть, например, метод `refineV`,
возвращающий значение типа `Either[String, T]`, где, как уже упоминалось для `from`, слева содержится ошибка, 
если входящее значение не удовлетворяет предикату.

[Вот несколько примеров использования библиотеки](https://github.com/fthomas/refined#more-examples)

## А в чем разница?

Здесь может возникнуть резонный вопрос: 
у нас есть два способа определения уточненного типа:

- "стандартный" ---
  `case class Name private (value: String) extends AnyVal`
- через библиотеку **refined** ---
  `type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]`

А в чем принципиальная разница между этими двумя способами? 
Только лишь в удобстве справочника предопределенных типов?

### Система типов

Важным преимуществом библиотеки **refined** является то, что "типы врать не могут".

При использовании case класса значение имени - это по-прежнему строка:

```scala
Name.fromString("Алёна").get.value
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

И дальше по коду его можно использовать именно в качестве типа.

Уточненный тип расширяет базовый (в данном случае - `String`)
и его можно использовать там, где ожидается дочерний для базового тип:

```scala
val name = Name.unsafeFrom("Алёна")
def printT[T >: String](t: T): Unit = println(t)
printT(name) // Печатает "Алёна"
```

Есть очень хорошая статья по поводу [важности системы типов][thetypesystem].

### Проверка во время компиляции

Ещё одним значительным преимуществом являлось возможность проверки типов во время компиляции в Scala 2.
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

[Пример в Scastie](https://scastie.scala-lang.org/oqh3jUboQQqf3wKC8A5ZkA)

В Scala 3 не все так просто.

Проверка соответствия уточненным типам в Scala 3 во время компиляции
пока ещё не реализована, но это лишь вопрос времени,
учитывая мощную функциональность [метапрограммирования в Scala 3](https://docs.scala-lang.org/scala3/reference/metaprogramming/index.html).

Например, используя [inline методы](https://docs.scala-lang.org/scala3/reference/metaprogramming/inline.html) 
реализовать уточнение типа `String` до `NonEmptyString` во время компиляции можно, например так:

```scala
import eu.timepit.refined.refineV
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import scala.compiletime.error

type NonEmptyString = String Refined NonEmpty

inline def refineToNonEmptyString(inline str: String): NonEmptyString =
  inline str match
    case null: Null | "" => error("String must be non empty")
    case _               => refineV[NonEmpty].unsafeFrom(str)

refineToNonEmptyString("")           // Не компилируется с ошибкой "String must be non empty"
refineToNonEmptyString(null: String) // Не компилируется с ошибкой "String must be non empty"
refineToNonEmptyString("Алёна")      // Компилируется успешно!
// val res0: NonEmptyString = Алёна
```

[Пример в Scastie](https://scastie.scala-lang.org/nBe9eTIHTjqeGzHEMRu8jw)

Уточнение `String` до `Name` (во время компиляции) можно реализовать с [помощью макросов](https://docs.scala-lang.org/scala3/reference/metaprogramming/macros.html):

```scala
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.string.MatchesRegex
import scala.quoted.{Expr, Quotes, ToExpr, quotes}

type Name = String Refined MatchesRegex[Name.pattern.type]

object Name:
  val pattern: "[А-ЯЁ][а-яё]+" = "[А-ЯЁ][а-яё]+"

  extension (inline str: String)
    inline def toName: Name = ${ inspectNameCode('str) }

  private given NameToExpr: ToExpr[Name] with
    def apply(x: Name)(using Quotes) =
      import quotes.reflect.*
      Literal(StringConstant(x.toString)).asExpr.asInstanceOf[Expr[Name]]

  private def inspectNameCode(x: Expr[String])(using Quotes): Expr[Name] =
    val str        = x.valueOrAbort
    if !pattern.r.matches(str) then
      import quotes.reflect.*
      report.errorAndAbort(s"'$str' does not match pattern '$pattern'")
    val name: Name = RefinedTypeOps[Name, String].unsafeFrom(str)
    Expr(name)
```

В `object Name` определяется встроенный (_inline_) метод расширения `toName`, 
принимающий на вход `String` и возвращающий `Name`.
Преобразование безопасно, потому что выполняется во время компиляции.

[Исходный код Name](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FName.scala&plain=1)

Попытки вызвать этот метод на невалидных значениях приводят к ошибкам компиляции:

```scala
import libs.refined.Name.toName

"€‡™µ".toName       // Не компилируется с ошибкой "'€‡™µ' does not match pattern '[А-ЯЁ][а-яё]+'"
"12345".toName      // Не компилируется с ошибкой "'12345' does not match pattern '[А-ЯЁ][а-яё]+'"
"Alyona".toName     // Не компилируется с ошибкой "'Alyona' does not match pattern '[А-ЯЁ][а-яё]+'"
"Алёна18".toName    // Не компилируется с ошибкой "'Алёна18' does not match pattern '[А-ЯЁ][а-яё]+'"
"алёна".toName      // Не компилируется с ошибкой "'алёна' does not match pattern '[А-ЯЁ][а-яё]+'"
```

[Исходный код невалидных примеров](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FNameCompileErrorExamples.worksheet.sc&plain=1)

Если же строка удовлетворяет предикату, то тип результата - это уже уточненный тип:

```scala
import libs.refined.Name.toName

val name = "Алёна".toName
// val name: Name = Алёна
```

[Исходный код валидного примера](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FNameExamples.worksheet.sc&plain=1)


Таким образом добиться ошибок компиляции в Scala 3 сложнее, но тоже возможно.
Конечно, хотелось бы, чтобы эта функциональность поставлялась "из коробки", но, думаю, это вопрос времени.


Проверка во время компиляции открывает довольно обширные возможности: 
как минимум, значительную часть проверок можно переложить с модульных тестов на компилятор.
Что в свою очередь может сэкономить общее время разработки.
[По этому поводу написана отличная статья][tests].

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

Пример использования уточняющего типа для `Packed`:

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

[Пример в Scastie](https://scastie.scala-lang.org/EIwWjHrMSyu6OxrznZN38g)

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FPackedExamples.worksheet.sc&plain=1)


## Уточнение уточненного типа

Уточнить можно любой тип, в том числе уточненный - в этом случае он становится базовым для другого типа,
который будет его "уточнять".
В библиотеке **refined** уточнение уточненного типа 
равносильно использованию типа `And[A, B]` - коньюнкции предикатов `A` и `B`.

[Пример в Scastie](https://scastie.scala-lang.org/nfETvR0vSUGv77Fc984icw)

Предельным непустым уточненным типом [является литеральный тип](https://docs.scala-lang.org/sips/42.type.html).

[Пример в Scastie](https://scastie.scala-lang.org/EqPJmGGNQ4etxbUajGBBWg)


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

К счастью, **refined** предоставляет расширение [**refined-cats**](https://index.scala-lang.org/fthomas/refined/artifacts/refined-cats?pre-releases=false), 
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
```

Метод `Person.refine` делает именно то, что нужно: 
применяет все предикаты к входным данным для их проверки, 
а также возвращает более конкретные типы, с которыми можно более безопасно работать в будущем:

```scala
Person.refine("Андрей", 50, UUID.randomUUID().toString)
// Valid(Person(Андрей,50,fccec68b-cefd-45e8-ae57-b8cdd3fa3cb8))
```

А так как мы используем [Applicative](https://typelevel.org/cats/typeclasses/applicative.html), 
то всегда будут выполняться все этапы "уточнения", 
и в случае неудачи некоторых из них их ошибки будут накапливаться в `NonEmptyChain`:

```scala
Person.refine("Andrew", 150, "id")
// Invalid(Chain(
//   Predicate failed: "Andrew".matches("[А-ЯЁ][а-яё]+")., 
//   Right predicate of (!(150 < 7) && (150 < 77)) failed: Predicate failed: (150 < 77)., 
//   Uuid predicate failed: Invalid UUID string: id
// ))
```

[Пример в Scastie](https://scastie.scala-lang.org/ldZp5KvvSHKfieFPCefw7Q)

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FRefinedWithCatsExamples.worksheet.sc&plain=1)


## Итоги обзора библиотеки refined

Подведем краткие итоги обзора библиотеки **refined**:

- К основным преимуществам библиотеки **refined** относится:
  - Система типов
  - Отлов ошибок во время компиляции (с определенными трудностями в Scala 3)
  - Единая декларативная валидация
- К недостаткам:
  - При использовании нотации инфиксного типа необходимо проявлять осторожность 
    и использовать скобки или неинфиксное определение типа:
    - ~~String Refined XXX And YYY~~
    - String Refined And[XXX, YYY]
    - String Refined (XXX And YYY)
  - Уточненные примитивы всегда упакованы
  - Сообщения об ошибках валидации не всегда понятны, а порой и просто ошибочны
- У библиотеки **refined** есть альтернативы:
  - [Bond](https://github.com/fwbrasil/bond)
  - [Scalactic](https://www.scalactic.org/)
- [Интеграция с другими библиотеками](https://github.com/fthomas/refined#using-refined)
  - [refined-cats (Scala 3 + Scala 2)](https://index.scala-lang.org/fthomas/refined/artifacts/refined-cats?pre-releases=false)
  - [refined-eval (только Scala 2)](https://index.scala-lang.org/fthomas/refined/artifacts/refined-eval?pre-releases=false)
  - [refined-jsonpath (Scala 3 + Scala 2)](https://index.scala-lang.org/fthomas/refined/artifacts/refined-jsonpath?pre-releases=false)
  - [refined-pureconfig (только Scala 2)](https://index.scala-lang.org/fthomas/refined/artifacts/refined-pureconfig?pre-releases=false)
    - [Пример взаимодействия с pureconfig](refined/pureconfig)
  - [refined-scalacheck (Scala 3 + Scala 2)](https://index.scala-lang.org/fthomas/refined/artifacts/refined-scalacheck?pre-releases=false)
  - [refined-scalaz (только Scala 2)](https://index.scala-lang.org/fthomas/refined/artifacts/refined-scalaz?pre-releases=false)
  - [refined-scodec (только Scala 2)](https://index.scala-lang.org/fthomas/refined/artifacts/refined-scodec?pre-releases=false)
  - [refined-scopt (Scala 3 + Scala 2)](https://index.scala-lang.org/fthomas/refined/artifacts/refined-scopt?pre-releases=false)
  - [refined-shapeless (только Scala 2)](https://index.scala-lang.org/fthomas/refined/artifacts/refined-shapeless?pre-releases=false)


## Заключение

Методов борьбы с ошибками в программном обеспечении очень много ввиду важности проблемы.
Тестирование программного обеспечения
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

**Ссылки** (в алфавитном порядке):
- [Refined lib][refined lib]
- Видео:
  - [Better types = fewer tests - Raúl Raja](https://www.youtube.com/watch?v=TScwxX62uig)
  - [Combining Refined Types with Type Class Derivation in Scala - Lawrence Carvalho](https://www.youtube.com/watch?v=Hq2QWbUXKbE&t)
  - [Decorate your types with refined - Frank Thomas](https://www.youtube.com/watch?v=zExb9x3fzKs)
  - [Defusing the configuration time bomb with PureConfig and Refined - Leif Wickland](https://www.youtube.com/watch?v=NjqRi-cF3-g)
  - [Enhancing the type system with Refined Types - Juliano Alves](https://www.youtube.com/watch?v=Fx8WXcAZWuk)
  - [How to Build a Functional API - Julien Truffaut](https://www.youtube.com/watch?v=__zuECMFCRc)
  - [Getting Started with #refined - DevInsideYou](https://www.youtube.com/watch?v=aZsmapo1afQ)
  - [Let The Compiler Help You: How To Make The Most Of Scala’s Typesystem - Markus Hauck](https://www.youtube.com/watch?v=hhXPeuJohM4)
  - [Literal types, what they are good for? - Tamer Abdulradi](https://slideslive.com/38907881/literal-types-what-they-are-good-for)  
  - [Refined types for validated configurations – Viktor Lövgren](https://www.youtube.com/watch?v=C3ciegxMAqA)
  - [Refined types in Scala - Rock the JVM](https://www.youtube.com/watch?v=IDrGbsupaok)
  - [Refinement Types - Tipagem ainda mais forte para Scala - Marcelo Gomes](https://www.youtube.com/watch?v=Zq4rkWs_ybA)  
  - [Security with Scala: Refined Types and Object Capabilities - Will Sargent](https://slideslive.com/38908776/security-with-scala-refined-types-and-object-capabilities) 
  - [Strings are Evil: Methods to hide the use of primitive types - Noel Welsh](https://slideslive.com/38908213/strings-are-evil-methods-to-hide-the-use-of-primitive-types)
  - [Why types matter - Gabriel Volpe](https://www.youtube.com/watch?v=n1Y2V4zCZdQ)
- Статьи:
  - [A simple trick to improve type safety of your Scala code - Marcin Kubala](https://blog.softwaremill.com/a-simple-trick-to-improve-type-safety-of-your-scala-code-ba80559ca092)
  - [How we used Refined to improve type safety and error reporting in Scala - Bertrand Junqua](https://engineering.contentsquare.com/2021/scala-refined-types/)
  - [Lightweight Non-Negative Numerics for Better Scala Type Signatures - Erik Erlandson](http://erikerlandson.github.io/blog/2015/08/18/lightweight-non-negative-numerics-for-better-scala-type-signatures/)
  - [On Eliminating Error in Distributed Software Systems - Colin Breck][thetypesystem]
  - [Parse, don’t validate - Alexis King][parse, don’t validate]
  - [Refined types in Scala - Daniel Ciocîrlan](https://blog.rockthejvm.com/refined-types/)
  - [Refined types in Scala: the Good, the Bad and the Ugly - Manuel Rodríguez](https://medium.com/swlh/refined-types-the-good-the-bad-and-the-ugly-ee971e5d9137)
  - [Refined types, what are they good for? - Malcolm](https://beyondthelines.net/programming/refined-types/)
  - [Refined типы в Scala - hakain](https://habr.com/ru/post/574080)
  - [Refinement types in practice - Peter Mortier](https://kwark.github.io/refined-in-practice-bescala/#1) 
  - [Refining your data from configuration to database - Pere Villega](https://underscore.io/blog/posts/2017/03/07/refined-data-config-database.html)
  - [Safe, Expressive Code with Refinement Types - Gordon Rennie](https://tech.ovoenergy.com/safe-expressive-code-with-refinement-types/)
  - [Tests - can we have too many? - Jack Low][tests]
  - [Type safety with refined - Michał Pawlik](https://blog.michalp.net/posts/scala/refined/)
  - [Validate Service Configuration in Scala - Alexey Novakov](https://medium.com/se-notes-by-alexey-novakov/validate-service-configuration-in-scala-85f661c4b5a6)
  - [Wtf is Refined? - Methrat0n](https://medium.com/@Methrat0n/wtf-is-refined-5008eb233194)


[refined lib]: https://github.com/fthomas/refined
[conversion]: refined/conversionInScala3
[thetypesystem]: https://blog.colinbreck.com/on-eliminating-error-in-distributed-software-systems
[tests]: https://github.com/wjlow/blog/blob/3c27de716b40660801e68561252883fd0428395e/Tests.md
[parse, don’t validate]: https://lexi-lambda.github.io/blog/2019/11/05/parse-don-t-validate
