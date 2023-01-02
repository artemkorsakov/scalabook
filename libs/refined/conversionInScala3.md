# Неявные преобразования в Scala 3

В Scala 3 
[неявные преобразования типов довольно сильно переработаны](https://scalabook.gitflic.space/scala/abstractions/ca-implicit-conversions).

Поэтому если задан уточняющий тип, расширяющий `String`:

```scala
import eu.timepit.refined.*
import eu.timepit.refined.api.*
import eu.timepit.refined.string.*

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]
```

то даже "валидное" (с точки зрения работы библиотеки refined в Scala 2) преобразование типов `val name: Name = "Алёна"` 
при компиляции выдаст ошибку: `Type Mismatch Error`.

Для того чтобы позволить неявное преобразование из `String` в `Name`
нужно для начала определить соответствующий `given` экземпляр, [как показано в документации](https://docs.scala-lang.org/scala3/book/ca-implicit-conversions.html)

При этом преобразования типов будут происходить во время выполнения, а не компиляции, поэтому этот способ небезопасен.
Но можно определить неявное преобразование в `Option[Name]`, что позволяет выполнять такое присваивание:

```scala
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.auto.*
import eu.timepit.refined.string.*

import scala.language.implicitConversions

type Name = String Refined MatchesRegex["[А-ЯЁ][а-яё]+"]

given Conversion[String, Option[Name]] = RefinedTypeOps[Name, String].unapply(_)

val name0: Option[Name] = "€‡™µ"    // None 
val name1: Option[Name] = "12345"   // None
val name2: Option[Name] = "Alyona"  // None
val name3: Option[Name] = "Алёна18" // None
val name4: Option[Name] = "алёна"   // None
val name5: Option[Name] = "Алёна"   // Some(Алёна)
```

[Разобранный пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FConversionExamples.sc&plain=1)

Таким образом проверка соответствия уточненным типам в Scala 3 во время компиляции
пока ещё не реализована, но это лишь вопрос времени,
учитывая мощную функциональность [метапрограммирования в Scala 3](https://docs.scala-lang.org/scala3/reference/metaprogramming/index.html).

Например, используя [inline методы](https://docs.scala-lang.org/scala3/reference/metaprogramming/inline.html) 
реализовать уточнение типа `String` до `NonEmptyString` во время компиляции можно, например так:

```scala
import eu.timepit.refined.*
import eu.timepit.refined.api.*
import eu.timepit.refined.auto.*
import eu.timepit.refined.collection.*
import eu.timepit.refined.string.*

import scala.compiletime.error

type NonEmptyString = String Refined NonEmpty

inline def refineNonEmptyString(inline str: String): NonEmptyString =
  inline str match
    case null: Null | "" => error("String must be non empty")
    case _               => refineV[NonEmpty].unsafeFrom(str)

refineNonEmptyString("")           
// Не компилируется:
// -- Error: ----------------------------------------------------------------------
// refineNonEmptyString("")
// ^^^^^^^^^^^^^^^^^^^^^^^^
// String must be non empty
refineNonEmptyString(null: String) 
// Не компилируется с аналогичной ошибкой
refineNonEmptyString("Алёна")
// Компилируется успешно!
// val res0: NonEmptyString = Алёна
```

[Разобранный пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FStringCompileTimeExamples.sc&plain=1)

Уточнение `String` до `Name` (во время компиляции) можно реализовать с [помощью макросов](https://docs.scala-lang.org/scala3/reference/metaprogramming/macros.html):

```scala
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.auto.*
import eu.timepit.refined.string.*

import scala.language.implicitConversions
import scala.quoted.{Expr, Quotes}
import scala.util.matching.Regex

val namePattern: "[А-ЯЁ][а-яё]+" = "[А-ЯЁ][а-яё]+"

def inspectNameCode(x: Expr[String])(using Quotes): Expr[String] =
  import scala.quoted.quotes.reflect.report
  if !namePattern.r.matches(x.valueOrAbort) then report.errorAndAbort("Invalid name")
  x

inline def inspectName(inline str: String): String =
  ${ inspectNameCode('str) }

inspectName("€‡™µ")
// Не компилируется:
// -- Error: ----------------------------------------------------------------------
// inspectName("€‡™µ")
// ^^^^^^^^^^^^^^^^^^^
// Invalid name
inspectName("12345")   // Не компилируется с аналогичной ошибкой
inspectName("Alyona")  // Не компилируется с аналогичной ошибкой
inspectName("Алёна18") // Не компилируется с аналогичной ошибкой
inspectName("алёна")   // Не компилируется с аналогичной ошибкой

inspectName("Алёна")
// Компилируется успешно!
// val res0: String = Алёна

type Name = String Refined MatchesRegex[namePattern.type]

given Conversion[String, Name] = RefinedTypeOps[Name, String].unsafeFrom(_)

val name0: Name = inspectName("€‡™µ")    // Не компилируется с аналогичной ошибкой
val name1: Name = inspectName("12345")   // Не компилируется с аналогичной ошибкой
val name2: Name = inspectName("Alyona")  // Не компилируется с аналогичной ошибкой
val name3: Name = inspectName("Алёна18") // Не компилируется с аналогичной ошибкой
val name4: Name = inspectName("алёна")   // Не компилируется с аналогичной ошибкой

val name5: Name = inspectName("Алёна")
// Компилируется успешно!
// val name5: Name = Алёна
```

[Разобранный пример](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FNameCompileTimeExamples.sc&plain=1)

Таким образом добиться ошибок компиляции в Scala 3 сложнее, но тоже можно.
Конечно, хотелось бы, чтобы эта функциональность поставлялась "из коробки", но, думаю, это вопрос времени.
