# Уточняющие типы в Scala

В этой статье по умолчанию используется Scala 3 (версия `3.2.2`) c дублированием примеров на Scala 2 (версия `2.13.10`).

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
opaque type Name = String
val name: Name = "€‡™µ"
// val name: Name = €‡™µ
```

[Пример в Scastie](https://scastie.scala-lang.org/j3H5VznPQUOz6ylAQgFMoA)

[Тот же пример в Scastie на Scala 2](https://scastie.scala-lang.org/4ojla7AbTCe4lw8EdHdTMw)

```scala
case class Name(value: String) extends AnyVal
val name: Name = Name("€‡™µ")
// val name: Name = Name(€‡™µ)
```

[Пример в Scastie](https://scastie.scala-lang.org/CjZpe7ejSwW724prXkQLqg)

[Тот же пример в Scastie на Scala 2](https://scastie.scala-lang.org/69prW6KjRG6MPcL9fJSObw)

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

Ещё одним способом решения заданной проблемы могут стать библиотеки для работы с уточняющими типами:
- [**iron**][iron lib] (для Scala 3)
- [**refined**][refined lib] (для Scala 2)

В [теории типов](https://en.wikipedia.org/wiki/Type_theory) 
[уточняющий тип (refinement type)](https://en.wikipedia.org/wiki/Refinement_type) — 
это тип, снабженный предикатом, который предполагается верным для любого элемента уточняемого типа. 
Например, натуральные числа больше 5 могут быть описаны так:

![](https://wikimedia.org/api/rest_v1/media/math/render/svg/b3771ee3aa45615fadcccdf42d259f5eb61fd0ea)

Т.о. уточняющий тип - это базовый тип + предикат, 
а значения уточняющего типа - это все значения базового типа, удовлетворяющие определенному предикату. 

Концепция уточняющих типов была впервые введена Фриманом и Пфеннингом в работе 1991 года ["Уточняющие типы для ML"](https://www.cs.cmu.edu/~fp/papers/pldi91.pdf), 
в которой представлена система типов для языка Standard ML.

Самая идея выражения ограничений на уровне типов в виде библиотеки Scala была впервые исследована Flavio W. Brasil
в библиотеке [**bond**](https://github.com/fwbrasil/bond).

И довольно сильно усовершенствована в библиотеке [**refined**][refined lib], которая начиналась как переработка [библиотеки на Haskell Никиты Волкова](http://nikita-volkov.github.io/refined/). 

Библиотека [**iron**][iron lib] - это дальнейшее развитие идеи уточненных типов в Scala 3.

Уточнение - это достаточно распространенная и естественная процедура в программировании.

Достаточно взглянуть на [примитивные типы в Scala](https://docs.scala-lang.org/scala3/book/first-look-at-types.html#scalas-value-types):

`Long` (от **-2<sup>63</sup>** до **2<sup>63</sup> - 1**) -> 
`Int` (от **-2<sup>31</sup>** до **2<sup>31</sup> - 1**) -> 
`Short` (от **-2<sup>15</sup>** до **2<sup>15</sup> - 1**) -> 
`Byte` (от **-2<sup>7</sup>** до **2<sup>7</sup> - 1**)

Каждый следующий тип в этом списке уточняет предыдущий.

## Знакомство с библиотеками iron и refined

Давайте рассмотрим решение исходной задачки с помощью **iron** (Scala 3) и **refined** (Scala 2).

Вот так можно объявить уточненный тип с помощью **iron**:

```scala
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.string.*

opaque type Name = String :| Match["[А-ЯЁ][а-яё]+"]
```

А вот так - с помощью **refined**:

```scala
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]
```

Явное присваивание невалидного значения вызовет ошибку компиляции:

```scala
val name0: Name = "€‡™µ"    // Ошибка компиляции: Should match [А-ЯЁ][а-яё]+ 
val name1: Name = "12345"   // Ошибка компиляции: Should match [А-ЯЁ][а-яё]+ 
val name2: Name = "Alyona"  // Ошибка компиляции: Should match [А-ЯЁ][а-яё]+ 
val name3: Name = "Алёна18" // Ошибка компиляции: Should match [А-ЯЁ][а-яё]+ 
val name4: Name = "алёна"   // Ошибка компиляции: Should match [А-ЯЁ][а-яё]+ 
val name5: Name = "Алёна"   // Компиляция проходит успешно
```

[Пример в Scastie для **iron**](https://scastie.scala-lang.org/4zUXqnzARFWscb44XGlLBw)

[Тот же пример в Scastie на Scala 2 для **refined**](https://scastie.scala-lang.org/OwN8IzucSCuJ3LsBmaxL7A)

Библиотеки уточнения также позволяют преобразовывать базовое значение для более удобной работы в runtime
в `Option` (`refineOption` для **iron** / `unapply` в **refined**):

```scala
val name0: Option[Name] = "€‡™µ".refineOption     // None
val name1: Option[Name] = "12345".refineOption    // None
val name2: Option[Name] = "Alyona".refineOption   // None
val name3: Option[Name] = "Алёна18".refineOption  // None
val name4: Option[Name] = "алёна".refineOption    // None
val name5: Option[Name] = "Алёна".refineOption    // Some("Алёна")
```

[Пример в Scastie для **iron**](https://scastie.scala-lang.org/w6nxgVi4RHySQg6wGglUlQ)

[Тот же пример в Scastie на Scala 2 для **refined**](https://scastie.scala-lang.org/4q046xYGSGSJpyqft9pE8Q)

и в `Either`, где слева будет ошибка валидации, (`refineEither` для **iron** / `from` в **refined**):

```scala
val name0: Either[String, Name] = "€‡™µ".refineEither     // Left("Should match [А-ЯЁ][а-яё]+")
val name1: Either[String, Name] = "12345".refineEither    // Left("Should match [А-ЯЁ][а-яё]+")
val name2: Either[String, Name] = "Alyona".refineEither   // Left("Should match [А-ЯЁ][а-яё]+")
val name3: Either[String, Name] = "Алёна18".refineEither  // Left("Should match [А-ЯЁ][а-яё]+")
val name4: Either[String, Name] = "алёна".refineEither    // Left("Should match [А-ЯЁ][а-яё]+")
val name5: Either[String, Name] = "Алёна".refineEither    // Right("Алёна")
```

[Пример в Scastie для **iron**](https://scastie.scala-lang.org/cXvITTPiT4a27t8pU9m0fg)

[Тот же пример в Scastie на Scala 2 для **refined**](https://scastie.scala-lang.org/V9SYu9UcSx67hAcJMnRG7g)

#### Предопределенные типы

У библиотек достаточно большой набор предопределенных типов:
- [предопределенные типы в **iron**](https://iltotore.github.io/iron/io/github/iltotore/iron/constraint.html)
- [предопределенные типы в **refined**](https://github.com/fthomas/refined#provided-predicates)

Вот несколько примеров использования библиотек:
- [примеры в **iron**](https://iltotore.github.io/iron/docs/reference/refinement.html)
- [примеры в **refined**](https://github.com/fthomas/refined#more-examples)
  
## А в чем разница?

Здесь может возникнуть резонный вопрос: 
у нас есть два способа определения уточненного типа:

- "стандартный" ---
  `case class Name private (value: String) extends AnyVal`
- через библиотеку **refined** ---
  `type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]`

А в чем принципиальная разница между этими двумя способами? 
Только лишь в удобстве справочника предопределенных типов?

#### Система типов

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
val name: Name = "Алёна"
// val name: Name = Алёна
```

И дальше по коду его можно использовать именно в качестве типа.

Уточненный тип расширяет базовый (в данном случае - `String`)
и его можно использовать там, где ожидается дочерний для базового тип:

```scala
val name: Name = "Алёна"
def printT[T >: String](t: T): Unit = println(t)
printT(name) // Печатает "Алёна"
```

Есть очень хорошая статья по поводу [важности системы типов][thetypesystem].

#### Проверка во время компиляции

Ещё одним значительным преимуществом является возможность проверки типов во время компиляции.

Как уже было рассмотрено выше:

```scala
val name0: Name = "€‡™µ"    // Ошибка компиляции: Should match [А-ЯЁ][а-яё]+ 
val name1: Name = "12345"   // Ошибка компиляции: Should match [А-ЯЁ][а-яё]+ 
val name2: Name = "Alyona"  // Ошибка компиляции: Should match [А-ЯЁ][а-яё]+ 
val name3: Name = "Алёна18" // Ошибка компиляции: Should match [А-ЯЁ][а-яё]+ 
val name4: Name = "алёна"   // Ошибка компиляции: Should match [А-ЯЁ][а-яё]+ 
val name5: Name = "Алёна"   // Компиляция проходит успешно
```

Скомпилируется только последний вариант, потому что строка `"Алёна"` удовлетворяет предикату уточненного типа.

[Пример в Scastie для **iron**](https://scastie.scala-lang.org/4zUXqnzARFWscb44XGlLBw)

[Тот же пример в Scastie на Scala 2 для **refined**](https://scastie.scala-lang.org/OwN8IzucSCuJ3LsBmaxL7A)

Проверка во время компиляции открывает довольно обширные возможности: 
как минимум, значительную часть проверок можно переложить с модульных тестов на компилятор.
Что в свою очередь может сэкономить общее время разработки.
[По этому поводу написана отличная статья][tests].

## Уточнение произвольного типа

Уточняющий тип можно создать для любого типа.

Допустим у нас есть тип и некий предикат для значений заданного типа:

```scala
opaque type Packed = Any

val predicate: Packed => Boolean =
  case str: String => Option(str).exists(_.trim.nonEmpty)
  case num: Int    => num > 0
  case _           => false
```

Уточняющий тип `NonEmpty` для `Packed` можно определить по предикату:

```scala
import io.github.iltotore.iron.{given, *}
import io.github.iltotore.iron.constraint.all.*

final class NonEmpty

given Constraint[Packed, NonEmpty] with

  override inline def test(value: Packed): Boolean = predicate(value)

  override inline def message: String = "Should be non empty"
```

Здесь в методе `test` определяется предикат, который предполагается верным для всех значений заданного типа.

Метод `message` определяет сообщение об ошибке, если переданное значение не удовлетворяет предикату.

Пример использования уточняющего типа для `Packed`:

```scala
(null: Packed).refineEither[NonEmpty]     // Left(Should be non empty)
("": Packed).refineEither[NonEmpty]       // Left(Should be non empty)
(" ": Packed).refineEither[NonEmpty]      // Left(Should be non empty)
("   ": Packed).refineEither[NonEmpty]    // Left(Should be non empty)
(0: Packed).refineEither[NonEmpty]        // Left(Should be non empty)
(-42: Packed).refineEither[NonEmpty]      // Left(Should be non empty)
(true: Packed).refineEither[NonEmpty]     // Left(Should be non empty)

("value": Packed).refineEither[NonEmpty]  // Right(value)
(42: Packed).refineEither[NonEmpty]       // Right(42)
```

[Пример в Scastie для **iron**](https://scastie.scala-lang.org/oXHm4xGtQVqgcpoOGLCxEw)

[Тот же пример в Scastie на Scala 2 для **refined**](https://scastie.scala-lang.org/9ssdbLvETGytvfqves0xlQ)

Уточнить можно любой тип, в том числе уточненный - в этом случае он становится базовым для другого типа,
который будет его "уточнять".

В библиотеке **iron** уточнение уточненного типа 
равносильно использованию типа `A & B` - коньюнкции предикатов `A` и `B`.

В библиотеке **refined** - `And[A, B]`.

[Пример в Scastie для **iron**](https://scastie.scala-lang.org/u8piqnZDQHuQNxqvlK4Ubw)

[Тот же пример в Scastie на Scala 2 для **refined**](https://scastie.scala-lang.org/8rtUUguHSVOrGttlP5J7mQ)

Предельным непустым уточненным типом [является литеральный тип](https://docs.scala-lang.org/sips/42.type.html),
добавленный в версии Scala 2.13.

[Пример в Scastie](https://scastie.scala-lang.org/hEnqG1UxQkGeBpKX8wt40A)

[Тот же пример в Scastie на Scala 2](https://scastie.scala-lang.org/JefKM7P2S3GaJfvxNR9yxg)

## Накопление ошибок валидации

Вариант использования `refineEither`, рассмотренный выше, довольно прост, 
и с ним часто сталкиваются разработчики: получение данных от некоего входящего потока.
Но прерывание процесса на первой обнаруженной ошибке нежелательно. 
Ведь на ошибки можно быстро реагировать и решать проблемы входящего потока пачками.
По этой причине просто монадическая композиция цепочек `Either` не подходит.
Другими словами, ["ориентированная на железную дорогу"](https://fsharpforfunandprofit.com/rop/) проверка, 
которая останавливается на ошибочном предикате, будет недостаточной.

Давайте рассмотрим проблему накопления ошибок синтаксического анализа/валидации. 
Нежелательно связывать этапы проверки одного сообщения с другими монадическими способами, 
потому что этот способ возвращал бы только первую из них. 
Вместо этого было бы идеально, если бы эти шаги находились на одном уровне. 
Надо сохранять все ошибки валидации, 
а затем агрегировать результат либо в желаемую форму, если все прошло хорошо, 
либо в список ошибок, если хотя бы один шаг не пройден.

Это именно то, что можно сделать с `ValidatedNec` из библиотеки [cats](https://typelevel.org/cats/index.html).

К счастью, **iron** предоставляет расширение [**iron-cats**](https://iltotore.github.io/iron/docs/modules/cats.html), 
которое позволяет возвращать шаги проверки `ValidatedNec[String, A]` вместо `Either[String, A]`:

Для библиотеки **refined** есть аналогичное расширение [**refined-cats**][refined cats]:

```scala
import cats.data.ValidatedNec
import cats.syntax.all.*

import io.github.iltotore.iron.*
import io.github.iltotore.iron.cats.*
import io.github.iltotore.iron.constraint.all.*

import java.util.UUID

opaque type Name = String :| Match["[А-ЯЁ][а-яё]+"]
opaque type Age  = Int :| Interval.Open[7, 77]
opaque type Id   = String :| ValidUUID

final case class Person(name: Name, age: Age, id: Id)

object Person:
  def refine(name: String, age: Int, id: String): ValidatedNec[String, Person] =
    (
      name.refineValidatedNec[Match["[А-ЯЁ][а-яё]+"]],
      age.refineValidatedNec[Interval.Open[7, 77]],
      id.refineValidatedNec[ValidUUID]
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
//   "Should match [А-ЯЁ][а-яё]+", 
//   "Should be included in (7, 77)", 
//   "Should be an UUID"
// ))
```

[Пример в Scastie для **iron**](https://scastie.scala-lang.org/4seolbH9SXeHosgAUNDzFw)

[Тот же пример в Scastie на Scala 2 для **refined**](https://scastie.scala-lang.org/roViFMw2SsaCWXB1vkMDdA)

## Итоги обзора библиотек уточенных типов **iron** и **refined**

Подведем краткие итоги обзора библиотек **iron** и **refined**:

- К основным преимуществам библиотек относится:
  - Система типов
  - Отлов ошибок во время компиляции
  - Единая декларативная валидация
- К недостаткам:
  - При использовании нотации инфиксного типа необходимо проявлять осторожность 
    и использовать скобки или неинфиксное определение типа:
    - **iron**
      - ~~String :| XXX & YYY~~
      - String :| &[XXX, YYY]
      - String :| (XXX & YYY)
    - **refined**
      - ~~String Refined XXX And YYY~~
      - String Refined And[XXX, YYY]
      - String Refined (XXX And YYY)
  - Уточненные примитивы всегда упакованы
  - Сообщения об ошибках валидации не всегда понятны
- У библиотек **iron** и **refined** есть альтернативы:
  - [Bond](https://github.com/fwbrasil/bond)
  - [Scalactic](https://www.scalactic.org/)
- Интеграция с другими библиотеками:
  - [**iron**](https://iltotore.github.io/iron/docs/modules/index.html)
  - [**refined**](https://github.com/fthomas/refined#using-refined)

## Заключение

Методов борьбы с ошибками в программном обеспечении очень много ввиду критичности проблемы.
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

Однако необходимо помнить, что _больше - не всегда лучше_, когда дело доходит до набора текста. 
Иногда лучше оставить универсальный тип или обобщить его до чего-то более простого, 
когда нижестоящая логика на самом деле не требует уточнения этого типа.

В противном случае ваша логика становится тесно связанной с тем, что вы делаете в данный момент, 
и вы теряете возможность повторного использования, которая так дорога функциональным программистам.

Эмпирическое правило таково: всегда кодируйте то, что нужно вашей логике, а не только то, что вы можете. 
И не бойтесь ослаблять ограничения в дальнейшем.


---

**Ссылки** (в алфавитном порядке):
- [iron lib][iron lib]
- [refined lib][refined lib]
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


[iron lib]: https://github.com/Iltotore/iron
[refined lib]: https://github.com/fthomas/refined
[refined cats]: https://index.scala-lang.org/fthomas/refined/artifacts/refined-cats?pre-releases=false
[conversion]: refined/conversionInScala3
[thetypesystem]: https://blog.colinbreck.com/on-eliminating-error-in-distributed-software-systems
[tests]: https://github.com/wjlow/blog/blob/3c27de716b40660801e68561252883fd0428395e/Tests.md
[parse, don’t validate]: https://lexi-lambda.github.io/blog/2019/11/05/parse-don-t-validate
