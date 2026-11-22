# Refinement Types in Scala

This article was published on the [Kryptonite company blog on Habr](https://habr.com/ru/companies/kryptonite/articles/719488/).

No programmer is shielded from situations where their program receives invalid values as input.
Whether it is user input or a raw data array — it does not matter.
The important thing is that Scala has several techniques that minimize the possibility of such errors.
One of them is the use of refined types.
They play a huge role when it comes to code reliability and often can be used without additional tests.

Refinement provides stricter, bounded types for use throughout our code.
At parse time, they ensure that
no variable is assigned an invalid (unsuitable for further processing) value.
This idea is well explored here.

By default, all examples in this article use Scala 3 (version `3.2.2`),
and most of them are also duplicated for Scala 2 (version `2.13.10`).

## Standard Types and "Pure" Value Classes

The set of standard types is quite limited and covers only the most common situations.
Each standard type is a fairly general set of values and operations on those values.
Take `String` — the string type in Scala, for example.
What set of values does it represent?
Anything goes: almost every alphabet in the world and all sorts of symbols,
the vast majority of which no one ever encounters during development. For example: `"€‡™µ"`

```dotty
val str: String = "€‡™µ"
```

> Recall the phrase by [Ken Scambler](https://github.com/kenbot), quoted [in this video](https://www.youtube.com/watch?v=zExb9x3fzKs&amp;t=52s): is a Chinese translation of Shakespeare's books a valid input for a `String` type?!

In the vast majority of cases, such "freedom" is not needed.

Let us suppose we need to create a set that would represent all possible human names
written in Cyrillic and starting with an uppercase letter,
for further use.

For example, `Алёна` would be acceptable,
but these would not be: `€‡™µ`, `12345`, `Alyona`, `Алёна18`, `алёна`.

But all the listed invalid values are `String`.
That is, this type describes not only the set we need but also the set of invalid variants,
which does not suit us.

How can we more precisely define the required set of values?

Type aliases and "pure" case classes (or [value classes](https://docs.scala-lang.org/overviews/core/value-classes.html))
do not work because they are only a "wrapper" around `String`,
and still allow "sneaking in" an invalid value.

```dotty
opaque type Name = String
val name: Name = "€‡™µ"
// name: String = "€‡™µ"
```

[Example on Scastie](https://scastie.scala-lang.org/j3H5VznPQUOz6ylAQgFMoA)

[Same example on Scastie for Scala 2](https://scastie.scala-lang.org/4ojla7AbTCe4lw8EdHdTMw)

```dotty
case class Name(value: String)
val name: Name = Name("€‡™µ")
// name: Name = Name(value = "€‡™µ")
```

[Example on Scastie](https://scastie.scala-lang.org/CjZpe7ejSwW724prXkQLqg)

[Same example on Scastie for Scala 2](https://scastie.scala-lang.org/69prW6KjRG6MPcL9fJSObw)

The "standard" solution — control `Name` creation by restricting the visibility of the default constructor
and defining an instance creation method in the companion object:

```dotty
import scala.util.matching.Regex

final case class Name private (value: String)

object Name:
  private val pattern: Regex = "[A-ZА-ЯЁ][а-яё]+".r

  def fromString(str: String): Option[Name] =
    if pattern.matches(str) then Some(Name(str))
    else None
```

In this case, you cannot create an invalid name directly using the standard constructor:

```dotty
val name: Name = Name("€‡™µ") // Will not compile
```

And its usage works as intended:

```dotty
Name.fromString("€‡™µ")     
// res2: Option[Name] = None     
Name.fromString("12345")    
// res3: Option[Name] = None    
Name.fromString("Alyona")   
// res4: Option[Name] = None   
Name.fromString("Alena18")  
// res5: Option[Name] = None  
Name.fromString("alena")    
// res6: Option[Name] = None    
Name.fromString("Alena")    
// res7: Option[Name] = Some(value = Name(value = "Alena"))   
```

[Example on Scastie](https://scastie.scala-lang.org/Esu8N76RQO6s0kORDsW1Kg)

> In Scala 2, this approach can be "broken" using the `copy` method (in Scala 3 this loophole was fixed):
>
> `Name.fromString("Alena").map(_.copy("€‡™µ")) // Some(Name(€‡™µ))`
>
> To prevent the use of the `copy` method or overriding through inheritance,
> in Scala 2 the class needed to be declared as `sealed abstract`, like this:
>
> `sealed abstract case class Name private (value: String) extends AnyVal`
>
> [Example of "break" via copy in Scala 2 on Scastie](https://scastie.scala-lang.org/JjnQcld3S9iPVoz1isQKqA)

## Refinement Types

Another way to solve the given problem may be libraries for working with refinement types:

- [**iron**][iron lib] (for Scala 3)
- [**refined**][refined lib] (for Scala 2)

In [type theory](https://en.wikipedia.org/wiki/Type_theory),
a [refinement type](https://en.wikipedia.org/wiki/Refinement_type) is
a type equipped with a predicate that is assumed to hold for any element of the refined type.
For example, natural numbers greater than 5 can be described as:

\\(f: \mathbb{N} \to \\{ n \in \mathbb{N} | n > 5 \\}\\)

Thus, a refinement type is a base type + a predicate,
and values of a refinement type are all values of the base type that satisfy a given predicate.

The concept of refinement types was first introduced by Freeman and Pfenning in their 1991 paper ["Refinement Types for ML"](https://www.cs.cmu.edu/~fp/papers/pldi91.pdf),
which presents a type system for the Standard ML language.

The very idea of expressing constraints at the type level as a Scala library was first explored by Flavio W. Brasil
in the [**bond**](https://github.com/fwbrasil/bond) library.

It was then greatly improved in the [**refined**][refined lib] library, which started as a rewrite of [Nikita Volkov's Haskell library](http://nikita-volkov.github.io/refined/).

The [**iron**][iron lib] library is a further development of the refinement type idea in Scala 3.

Refinement is a fairly common and natural procedure in programming.

Just look at [primitive types in Scala](https://docs.scala-lang.org/scala3/book/first-look-at-types.html#scalas-value-types):

`Long` (from \\(-2^{63}\\) to \\(2^{63} - 1\\)) ->
`Int` (from \\(-2^{31}\\) to \\(2^{31} - 1\\)) ->
`Short` (from \\(-2^{15}\\) to \\(2^{15} - 1\\)) ->
`Byte` (from \\(-2^{7}\\) to \\(2^{7} - 1\\))

Each subsequent type in this list refines the previous one.

## Getting to Know iron and refined Libraries

Let us consider a solution to the original problem using **iron** (Scala 3) and **refined** (Scala 2).

Here is how you can declare a refined type using **iron**:

```dotty
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.string.*

opaque type Name = String :| Match["[A-ZА-ЯЁ][a-яё]+"]
```

And using **refined**:

```dotty
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex

type Name = String Refined MatchesRegex["[A-ZА-ЯЁ][a-яё]+"]
```

Explicitly assigning an invalid value will cause a compilation error:

```dotty
val name0: Name = "€‡™µ"    // Compilation error: Should match [A-ZА-ЯЁ][a-яё]+
val name1: Name = "12345"   // Compilation error: Should match [A-ZА-ЯЁ][a-яё]+
val name2: Name = "Alyona"  // Compilation error: Should match [A-ZА-ЯЁ][a-яё]+
val name3: Name = "Alena18" // Compilation error: Should match [A-ZА-ЯЁ][a-яё]+
val name4: Name = "alena"   // Compilation error: Should match [A-ZА-ЯЁ][a-яё]+
val name5: Name = "Alena"   // Compilation succeeds
```

[Example on Scastie for **iron**](https://scastie.scala-lang.org/4zUXqnzARFWscb44XGlLBw)

[Same example on Scastie for Scala 2 using **refined**](https://scastie.scala-lang.org/OwN8IzucSCuJ3LsBmaxL7A)

Refinement libraries also allow transforming base values for more convenient work
into [`Option[T]`](https://www.scala-lang.org/api/3.2.2/scala/Option.html) (`refineOption` for **iron** / `unapply` in **refined**):

```dotty
val name0: Option[Name] = "€‡™µ".refineOption     // None
val name1: Option[Name] = "12345".refineOption    // None
val name2: Option[Name] = "Alyona".refineOption   // None
val name3: Option[Name] = "Alena18".refineOption  // None
val name4: Option[Name] = "alena".refineOption    // None
val name5: Option[Name] = "Alena".refineOption    // Some("Alena")
```

[Example on Scastie for **iron**](https://scastie.scala-lang.org/w6nxgVi4RHySQg6wGglUlQ)

[Same example on Scastie for Scala 2 using **refined**](https://scastie.scala-lang.org/e1JE6BfaRBG8cBAEqvshYg)

and into [`Either[String, T]`](https://www.scala-lang.org/api/3.2.2/scala/util/Either.html),
where the left side holds the validation error (`refineEither` for **iron** / `from` in **refined**):

```dotty
val name0: Either[String, Name] = "€‡™µ".refineEither     
// Left("Should match [A-ZА-ЯЁ][a-яё]+")
val name1: Either[String, Name] = "12345".refineEither    
// Left("Should match [A-ZА-ЯЁ][a-яё]+")
val name2: Either[String, Name] = "Alyona".refineEither   
// Left("Should match [A-ZА-ЯЁ][a-яё]+")
val name3: Either[String, Name] = "Alena18".refineEither  
// Left("Should match [A-ZА-ЯЁ][a-яё]+")
val name4: Either[String, Name] = "alena".refineEither    
// Left("Should match [A-ZА-ЯЁ][a-яё]+")
val name5: Either[String, Name] = "Alena".refineEither    
// Right("Alena")
```

[Example on Scastie for **iron**](https://scastie.scala-lang.org/cXvITTPiT4a27t8pU9m0fg)

[Same example on Scastie for Scala 2 using **refined**](https://scastie.scala-lang.org/GHkxyKFNRzCttHVgwSiFbw)

#### Predefined Types

The libraries have quite a large set of predefined types:

- [predefined types in **iron**](https://iltotore.github.io/iron/io/github/iltotore/iron/constraint.html)
- [predefined types in **refined**](https://github.com/fthomas/refined#provided-predicates)

Here are a few usage examples:

- [examples in **iron**](https://iltotore.github.io/iron/docs/reference/refinement.html)
- [examples in **refined**](https://github.com/fthomas/refined#more-examples)

## What is the Difference?

A reasonable question may arise here:
we have two ways to define a refined type:

- "standard" approach ---
  `case class Name private (value: String) extends AnyVal`
- via the **refined** library ---
  `type Name = String Refined MatchesRegex["[A-ZА-ЯЁ][a-яё]+"]`

What is the fundamental difference between these two approaches?
Is it just about the convenience of the predefined types reference?

#### Type System

An important advantage of the **refined** library is that "types cannot lie."

When using a case class, the name value is still a string:

```dotty
Name.fromString("Alena").get.value
// val res0: String = Alena
```

We once again get an overly "broad" set of values.
And further use of `Name.value` in code is possible only as `String`.
At the same time, potentially useful information about what kind of string it is has been discarded.

A refined type, on the other hand, is a specific type:

```dotty
val name: Name = "Alena"
// val name: Name = Alena
```

And it can be used throughout the code exactly as that type.

A refined type extends the base type (in this case — `String`),
and can be used where a subtype of the base type is expected:

```dotty
val name: Name = "Alena"
def printT[T >: String](t: T): Unit = println(t)
printT(name) // Prints "Alena"
```

There is a very good article about [the importance of the type system][thetypesystem].

#### Compile-Time Checking

Another significant advantage is the ability to perform type checking at compile time.

As already shown above:

```dotty
val name0: Name = "€‡™µ"    // Compilation error: Should match [A-ZА-ЯЁ][a-яё]+
val name1: Name = "12345"   // Compilation error: Should match [A-ZА-ЯЁ][a-яё]+
val name2: Name = "Alyona"  // Compilation error: Should match [A-ZА-ЯЁ][a-яё]+
val name3: Name = "Alena18" // Compilation error: Should match [A-ZА-ЯЁ][a-яё]+
val name4: Name = "alena"   // Compilation error: Should match [A-ZА-ЯЁ][a-яё]+
val name5: Name = "Alena"   // Compilation succeeds
```

Only the last variant will compile because the string `"Alena"` satisfies the predicate of the refined type.

[Example on Scastie for **iron**](https://scastie.scala-lang.org/4zUXqnzARFWscb44XGlLBw)

[Same example on Scastie for Scala 2 using **refined**](https://scastie.scala-lang.org/OwN8IzucSCuJ3LsBmaxL7A)

Compile-time checking opens up quite extensive possibilities:
at minimum, a significant portion of checks can be moved from unit tests to the compiler.
This in turn can save overall development time.
[A great article has been written on this subject][tests].

## Refining an Arbitrary Type

A refinement type can be created for any type.

Suppose we have a type and some predicate for values of the given type:

```dotty
opaque type Packed = Any

val predicate: Packed => Boolean =
  case str: String => Option(str).exists(_.trim.nonEmpty)
  case num: Int    => num > 0
  case _           => false
```

A refinement type `NonEmpty` for `Packed` can be defined by a predicate:

```dotty
import io.github.iltotore.iron.{given, *}
import io.github.iltotore.iron.constraint.all.*

final class NonEmpty

given Constraint[Packed, NonEmpty] with

  override inline def test(value: Packed): Boolean = predicate(value)

  override inline def message: String = "Should be non empty"
```

Here, the predicate is defined in the `test` method, which is assumed to hold for all values of the given type.

The `message` method defines the error message if the passed value does not satisfy the predicate.

Example of using a refined type for `Packed`:

```dotty
(null: Packed).refineEither[NonEmpty]     // Left("Should be non empty")
("": Packed).refineEither[NonEmpty]       // Left("Should be non empty")
(" ": Packed).refineEither[NonEmpty]      // Left("Should be non empty")
("   ": Packed).refineEither[NonEmpty]    // Left("Should be non empty")
(0: Packed).refineEither[NonEmpty]        // Left("Should be non empty")
(-42: Packed).refineEither[NonEmpty]      // Left("Should be non empty")
(true: Packed).refineEither[NonEmpty]     // Left("Should be non empty")

("value": Packed).refineEither[NonEmpty]  // Right(value)
(42: Packed).refineEither[NonEmpty]       // Right(42)
```

[Example on Scastie for **iron**](https://scastie.scala-lang.org/6MsuJONXTHWZcV5h7pLhJQ)

[Same example on Scastie for Scala 2 using **refined**](https://scastie.scala-lang.org/F7Qf7XD8SGKdfVG3OmffDg)

Any type can be refined, including a refined one — in this case it becomes the base type for another
type that will "refine" it.

In the **iron** library, refining a refined type
is equivalent to using the `A & B` type — the conjunction of predicates `A` and `B`.

In the **refined** library — `And[A, B]`.

[Example on Scastie for **iron**](https://scastie.scala-lang.org/QdC8NLLJS8OZLygJaN8pMA)

[Same example on Scastie for Scala 2 using **refined**](https://scastie.scala-lang.org/8rtUUguHSVOrGttlP5J7mQ)

The ultimate non-empty refined type [is the literal type](https://docs.scala-lang.org/sips/42.type.html),
added in Scala 2.13.

[Example on Scastie](https://scastie.scala-lang.org/hEnqG1UxQkGeBpKX8wt40A)

[Same example on Scastie for Scala 2](https://scastie.scala-lang.org/JefKM7P2S3GaJfvxNR9yxg)

## Accumulating Validation Errors

The `refineEither` use case considered above is quite simple,
and developers often encounter it when getting data from some input stream.
But stopping the process on the first detected error is undesirable.
After all, errors can be quickly responded to and issues with the input stream can be resolved in batches.
For this reason, simple monadic composition of `Either` chains does not suit us.
In other words, ["railway-oriented"](https://fsharpforfunandprofit.com/rop/) checking,
which stops at an error predicate, would be insufficient.

Let us consider the problem of accumulating errors during parsing/validation.
It is undesirable to chain individual message validation steps with other monadic approaches,
because this approach would return only the first one.
Instead, it would be ideal if these steps were on the same level.
We should preserve all validation errors,
and then aggregate the result either into the desired form if everything succeeded,
or into a list of errors if at least one step failed.

This is exactly what `ValidatedNec` from the [cats](https://typelevel.org/cats/index.html) library allows us to do.

Fortunately, **iron** provides the [**iron-cats**](https://iltotore.github.io/iron/docs/modules/cats.html) extension,
which allows returning validation steps of type `ValidatedNec[String, A]` instead of `Either[String, A]`:

For the **refined** library, there is a similar extension: [**refined-cats**][refined cats]:

```dotty
import cats.data.ValidatedNec
import cats.syntax.all.*

import io.github.iltotore.iron.*
import io.github.iltotore.iron.cats.*
import io.github.iltotore.iron.constraint.all.*

import java.util.UUID

opaque type Name = String :| Match["[A-ZА-ЯЁ][a-яё]+"]
opaque type Age  = Int :| Interval.Open[7, 77]
opaque type Id   = String :| ValidUUID

final case class Person(name: Name, age: Age, id: Id)

object Person:
  def refine(name: String, age: Int, id: String): ValidatedNec[String, Person] =
    (
      name.refineValidatedNec[Match["[A-ZА-ЯЁ][a-яё]+"]],
      age.refineValidatedNec[Interval.Open[7, 77]],
      id.refineValidatedNec[ValidUUID]
    ).mapN(Person.apply)
```

The `Person.refine` method does exactly what is needed:
it applies all predicates to input data for validation,
and also returns more specific types that are safer to work with in the future:

```dotty
Person.refine("Andrew", 50, UUID.randomUUID().toString)
// Valid(Person(Andrew,50,fccec68b-cefd-45e8-ae57-b8cdd3fa3cb8))
```

And because we use [Applicative](https://typelevel.org/cats/typeclasses/applicative.html),
all "refinement" stages will always be executed,
and if some of them fail, their errors will be accumulated in `NonEmptyChain`:

```dotty
Person.refine("Andrew", 150, "id")
// Invalid(Chain(
//   "Should match [A-ZА-ЯЁ][a-яё]+", 
//   "Should be included in (7, 77)", 
//   "Should be an UUID"
// ))
```

[Example on Scastie for **iron**](https://scastie.scala-lang.org/4seolbH9SXeHosgAUNDzFw)

[Same example on Scastie for Scala 2 using **refined**](https://scastie.scala-lang.org/roViFMw2SsaCWXB1vkMDdA)

## Summary of **iron** and **refined** Refinement Type Libraries

Let us summarize the **iron** and **refined** library review:

- The main advantages of the libraries include:
    - Type system
    - Catching errors at compile time
    - Unified declarative validation
- The disadvantages include:
    - When using infix type notation, one must exercise caution
        and use parentheses or an uninfix type definition (only for **refined**):
        - ~~String Refined XXX And YYY~~
        - `String Refined And[XXX, YYY]`
        - `String Refined (XXX And YYY)`
    - Validation error messages are not always clear
- **iron** and **refined** have alternatives:
    - [Bond](https://github.com/fwbrasil/bond)
    - [Scalactic](https://www.scalactic.org/)
- Integration with other libraries:
    - [**iron**](https://iltotore.github.io/iron/docs/modules/index.html)
    - [**refined**](https://github.com/fthomas/refined#using-refined)

## Boundaries of Applicability

There are very many methods for combating software errors due to the criticality of the problem. 
Software testing is an important (if not the most important) stage of product release. 
Almost every company has a dedicated QA department, which sometimes rivals 
the development department in size, knowledge, and competence. 
The test codebase sometimes exceeds the code being tested.

Refined types are certainly not a panacea, but yet another way to improve product quality. 
However, it is important to remember that _more is not always better_ when it comes to writing types. 
Sometimes it is better to leave a universal type or generalize it to something simpler, 
when the downstream logic does not actually require refining that type.

Otherwise, your logic becomes tightly coupled to what you are doing at the moment, 
and you lose the reuse potential that is so dear to functional programmers.

The empirical rule is this: _always code for what your logic needs, not just what you can_. 
Yes, and do not be afraid to relax constraints later!


---

**References** (in alphabetical order):

- [iron lib][iron lib]
- [refined lib][refined lib]
- Videos:
    - [Better types = fewer tests - Raúl Raja](https://www.youtube.com/watch?v=TScwxX62uig)
    - [Combining Refined Types with Type Class Derivation in Scala - Lawrence Carvalho](https://www.youtube.com/watch?v=Hq2QWbUXKbE&amp;t)
    - [Decorate your types with refined - Frank Thomas](https://www.youtube.com/watch?v=zExb9x3fzKs)
    - [Defusing the configuration time bomb with PureConfig and Refined - Leif Wickland](https://www.youtube.com/watch?v=NjqRi-cF3-g)
    - [Enhancing the type system with Refined Types - Juliano Alves](https://www.youtube.com/watch?v=Fx8WXcAZWuk)
    - [How to Build a Functional API - Julien Truffaut](https://www.youtube.com/watch?v=__zuECMFCRc)
    - [Getting Started with #refined - DevInsideYou](https://www.youtube.com/watch?v=aZsmapo1afQ)
    - [Let The Compiler Help You: How To Make The Most Of Scala's Typesystem - Markus Hauck](https://www.youtube.com/watch?v=hhXPeuJohM4)
    - [Literal types, what are they good for? - Tamer Abdulradi](https://slideslive.com/38907881/literal-types-what-they-are-good-for)
    - [Refined types for validated configurations – Viktor Lövgren](https://www.youtube.com/watch?v=C3ciegxMAqA)
    - [Refined types in Scala - Rock the JVM](https://www.youtube.com/watch?v=IDrGbsupaok)
    - [Refinement Types - Tipagem ainda mais forte para Scala - Marcelo Gomes](https://www.youtube.com/watch?v=Zq4rkWs_ybA)
    - [Security with Scala: Refined Types and Object Capabilities - Will Sargent](https://slideslive.com/38908776/security-with-scala-refined-types-and-object-capabilities)
    - [Strings are Evil: Methods to hide the use of primitive types - Noel Welsh](https://slideslive.com/38908213/strings-are-evil-methods-to-hide-the-use-of-primitive-types)
    - [Why types matter - Gabriel Volpe](https://www.youtube.com/watch?v=n1Y2V4zCZdQ)
- Articles:
    - [A simple trick to improve type safety of your Scala code - Marcin Kubala](https://blog.softwaremill.com/a-simple-trick-to-improve-type-safety-of-your-scala-code-ba80559ca092)
    - [How we used Refined to improve type safety and error reporting in Scala - Bertrand Junqua](https://engineering.contentsquare.com/2021/scala-refined-types/)
    - [Lightweight Non-Negative Numerics for Better Scala Type Signatures - Erik Erlandson](http://erikerlandson.github.io/blog/2015/08/18/lightweight-non-negative-numerics-for-better-scala-type-signatures/)
    - [On Eliminating Error in Distributed Software Systems - Colin Breck][thetypesystem]
    - [Parse, don't validate - Alexis King][parse, don't validate]
    - [Refined types in Scala - Daniel Ciocîrlan](https://blog.rockthejvm.com/refined-types/)
    - [Refined types in Scala: the Good, the Bad and the Ugly - Manuel Rodríguez](https://medium.com/swlh/refined-types-the-good-the-bad-and-the-ugly-ee971e5d9137)
    - [Refined types, what are they good for? - Malcolm](https://beyondthelines.net/programming/refined-types/)
    - [Refined types in Scala - hakain](https://habr.com/ru/articles/574080/)
    - [Refinement types in practice - Peter Mortier](https://kwark.github.io/refined-in-practice-bescala/#1)
    - [Refining your data from configuration to database - Pere Villega](https://underscore.io/blog/posts/2017/03/07/refined-data-config-database.html)
    - [Tests - can we have too many? - Jack Low][tests]
    - [Type safety with Iron - Michał Pawlik](https://blog.michal.pawlik.dev/posts/scala/iron/)
    - [Type safety with refined - Michał Pawlik](https://blog.michalp.net/posts/scala/refined/)
    - [Validate Service Configuration in Scala - Alexey Novakov](https://medium.com/se-notes-by-alexey-novakov/validate-service-configuration-in-scala-85f661c4b5a6)
    - [Wtf is Refined? - Methrat0n](https://medium.com/@Methrat0n/wtf-is-refined-5008eb233194)


[iron lib]: https://github.com/Iltotore/iron
[refined lib]: https://github.com/fthomas/refined
[refined cats]: https://index.scala-lang.org/fthomas/refined/artifacts/refined-cats?pre-releases=false
[conversion]: refined/conversionInScala3
[thetypesystem]: https://blog.colinbreck.com/on-eliminating-error-in-distributed-software-systems/
[tests]: https://github.com/wjlow/blog/blob/3c27de716b40660801e68561252883fd0428395e/Tests.md
[parse, don't validate]: https://lexi-lambda.github.io/blog/2019/11/05/parse-don-t-validate/
