# Refinement types

Уточняющие типы для Scala.

Библиотека [refined][refined lib] позволяет уточнять типы для отсеивания невалидных значений.

Пример использования:

```scala
import eu.timepit.refined.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto.*
import eu.timepit.refined.numeric.*

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





## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Flibs%2Frefined%2FExamples.sc&plain=1)


---

**References:**
- [Github][refined lib]

[refined lib]: https://github.com/fthomas/refined
