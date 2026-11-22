# Monad Group

## Introduction

**Monads** are one of the most important yet challenging concepts in functional programming and category theory. They represent an abstract structure that enables effective management of side effects such as error handling, I/O, state, or asynchronous computations while preserving purity and composability of functions. Monads provide a unified interface for working with such effects, making them an indispensable tool in developing reliable and predictable software.

The idea of monads originates in mathematics, where they are used to describe certain types of functors with additional structure. However, thanks to their versatility and expressiveness, monads have found widespread application in programming languages such as Scala, Haskell, and others.

## Usage

Here are the main areas where monads are used:

### Functional Programming

- **Managing side effects**: Monads allow isolating side effects (e.g., I/O, state changes, error handling) and working with them in a purely functional style.
- **Composing computations**: Monads provide a mechanism for sequentially executing operations, where each operation may depend on the result of the previous one.

### Error Handling

- Monads such as `Option` and `Either` are used for [safe error handling](../fp/handling-errors.md) without the need for explicit exception usage.
- Example: In Scala, `Option` allows expressing a computation that may return a value (`Some`) or nothing (`None`).

### Working with State

- The `State` monad allows modeling computations that [depend on mutable state](../fp/state.md) while preserving function purity.
- Example: Managing state in functional programs without using mutable variables.

### Asynchronous Computations

- Monads such as `IO` and `Promise` are used [for working with asynchronous operations](../fp/external-effects/common_io.md) such as file reading, network requests, or database interaction.
- Example: In Scala, the `IO` monad allows describing I/O in a purely functional style.

### Parsing

- Monads are used to create parser combinator libraries that allow building complex parsers from simple components.
- Example: Parser libraries such as Circe in Scala.

### Working with Collections

- Monads such as `List` allow [working with sequences](../data-structures/linear_list/lists.md) and combining them into complex computations.
- Example: Generating all possible combinations of elements from a list.

### Category Theory

- In mathematics, monads are used to describe certain types of functors with additional structure, aiding in the study of algebraic and categorical properties.

### Reactive Programming

- Monads can be used to manage data streams and reactive computations where values change over time.
- Example: Reactive programming libraries such as RxJS are inspired by monadic concepts.

### Testing and Modeling

- Monads such as `Writer` allow [collecting additional information (e.g., logs)](../fp/writer.md) during computations.
- Example: Logging in functional programs.

### Domain-Specific Languages (DSL)

- Monads are used to create embedded DSLs that allow describing complex domain tasks in a convenient and expressive form.
- Example: Languages for describing business rules or configurations.

### Examples of usage in real projects:

- In Haskell, monads are used almost ubiquitously, from I/O to complex data processing libraries.
- In Scala, monads are implemented through types such as `Option`, `Future`, and `Try`, which are actively used in industrial development.
- In JavaScript, libraries such as `folktale` or `fp-ts` provide monadic structures for functional programming.

---

Thus, monads are a powerful tool that helps structure code, manage complexity, and make programs more modular, predictable, and expressive.

## Example

In Scala, monads are actively used for working with various types of computations such as error handling, asynchronous operations, working with optional values, and more. Scala provides several built-in types that are monads, such as `Option`, `Either`, `Future`, `Try`, and `List`.

### The `Option` Monad

`Option` is used to work with values that may be absent (`None`) or present (`Some(value)`). This allows avoiding `null` and explicit checks.

```dotty
def divide(a: Int, b: Int): Option[Int] =
  if b == 0 then None else Some(a / b)

val result: Option[Int] =
  for
    r1 <- divide(10, 2)
    r2 <- divide(r1, 0)
  yield r2

result // None

val result2: Option[Int] =
  for
    r1 <- divide(10, 2)
    r2 <- divide(r1, 5)
  yield r1 + r2

result2 // Some(4)
```

### The `Either` Monad

`Either` is used to represent computations that can either succeed with a value (`Right`) or fail with an error (`Left`).

```dotty
def parseNumber(s: String): Either[String, Int] =
  try
    Right(s.toInt)
  catch
    case _: NumberFormatException => Left("Not a number")

val result: Either[String, Int] =
  for
    x <- parseNumber("10")
    y <- parseNumber("5")
  yield x + y

println(result) // Right(15)
```

### The `Future` Monad

`Future` is used for asynchronous computations that execute in a separate thread.

```dotty
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

def asyncComputation(x: Int): Future[Int] =
  Future {
    Thread.sleep(1000) // Simulating a long operation
    x * 2
  }

val result: Future[Int] =
  for
    a <- asyncComputation(10)
    b <- asyncComputation(20)
  yield a + b

result.foreach(println) // After a while it will print 60
```

### The `Try` Monad

`Try` is used to handle operations that can complete successfully (`Success`) or throw an exception (`Failure`).

```dotty
import scala.util.{Try, Success, Failure}

def safeDivide(a: Int, b: Int): Try[Int] =
  Try { a / b }

val result: Try[Int] =
  for
    x <- safeDivide(10, 2)
    y <- safeDivide(x, 0) // Division by zero
  yield y

println(result) // Failure(java.lang.ArithmeticException: / by zero)
```

### The `List` Monad

`List` is a monad that allows combining lists and performing operations on their elements.

```dotty
val numbers = List(1, 2, 3)
val letters = List('a', 'b', 'c')

val result: List[String] =
  for
    n <- numbers
    l <- letters
  yield s"$n$l"

println(result) // List(1a, 1b, 1c, 2a, 2b, 2c, 3a, 3b, 3c)
```

### The `Writer` Monad (from Cats library)

`Writer` is used to collect logs or additional information during computations.

```dotty
import cats.data.Writer
import cats.implicits.*

type Logged[A] = Writer[Vector[String], A]

def logNumber(x: Int): Logged[Int] = Writer(Vector(s"Number: $x"), x)

val result: Logged[Int] =
  for
    a <- logNumber(10)
    b <- logNumber(20)
  yield a + b

println(result.run) // (Vector(Number: 10, Number: 20), 30)
```

### The `IO` Monad (from Cats Effect library)

`IO` is used to describe side effects, such as I/O, in a purely functional style.

```dotty
import cats.effect.IO
import cats.effect.unsafe.implicits.global

val program: IO[Unit] =
  for
    _    <- IO(println("Enter your name:"))
    name <- IO(scala.io.StdIn.readLine())
    _    <- IO(println(s"Hello, $name!"))
  yield ()

program.unsafeRunSync() // Running the program
```

### The `State` Monad (from Cats library)

`State` is used to model computations that depend on changing state.

```dotty
import cats.data.State

type CounterState[A] = State[Int, A]

def increment: CounterState[Int] = State { count =>
  val newCount = count + 1
  (newCount, newCount)
}

val program: CounterState[Int] =
  for
    _      <- increment
    _      <- increment
    result <- increment
  yield result

val (finalState, value) = program.run(0).value
println(s"Final state: $finalState, Value: $value") 
// Final state: 3, Value: 3
```

## Monad Group Diagram

### 1. Basic Functor Structures (core)

```mermaid
---
title: "Basic Functors"
---
classDiagram
    direction TB
    class InvariantFunctor~A~{
        +imap[B](fa: F[A], f: A => B, g: B => A) F[B]
        %% Invariant mapping (preserves structure in both directions)
    }
    class Functor~A~{
        +map[B](fa: F[A], f: A => B) F[B]
        %% Forward mapping (covariant)
    }
    class ContravariantFunctor~A~{
        +contramap[B](fb: F[B], f: A => B) F[A]
        %% Reverse mapping (contravariant)
    }
    
    InvariantFunctor <|-- Functor
    InvariantFunctor <|-- ContravariantFunctor
    
    note for InvariantFunctor "The most general functor"
    note for Functor "map preserves direction"
    note for ContravariantFunctor "contramap reverses direction"
```

### 2. Structures with Multiplication

```mermaid
---
title: "Structures with combination"
---
classDiagram
    direction TB
    class Semigroupal~A~{
        +product[A,B](fa: F[A], fb: F[B]) F[(A,B)]
        %% Combines two values into a pair
    }
    class Apply~A~{
        +apply[A,B](fab: F[A=>B])(fa: F[A]) F[B]
        %% Applies a function in context
    }
    
    class Divide~A~{
        +divide[A,B,C](fa: F[A], fb: F[B])(f: C => (A,B)) F[C]
        %% Contravariant combination
    }
    
    Functor <|-- Apply
    Semigroupal <|-- Apply
    ContravariantFunctor <|-- Divide
    
    note for Apply "Functor with applicative application"
    note for Divide "Contravariant analog of Apply"
```

### 3. Invariant Structures

```mermaid
---
title: "Invariant Structures"
---
classDiagram
    direction TB
    class InvariantApplicative~A~{
        +xunit0[A](a: => A) F[A]
        %% Lifts a value into context
    }
    
    class Divisible~A~{
        +conquer[A]: F[A]
        %% Neutral element for Divide
    }
    
    InvariantFunctor <|-- InvariantApplicative
    Divide <|-- Divisible
    InvariantApplicative <|-- Divisible
    
    note for Divisible "Contravariant analog of Applicative"
```

### 4. Applicative Structures

```mermaid
---
title: "Applicative and its variants"
---
classDiagram
    direction TB
    class Applicative~A~{
        +unit[A](a: => A) F[A]
        %% Lifts a value into context
    }
    
    Apply <|-- Applicative
    InvariantApplicative <|-- Applicative
    
    class ApplicativePlus~A~{
        %% Applicative with choice capability
    }
    
    class Plus~A~{
        +plus[A](fa1: F[A], fa2: => F[A]) F[A]
        %% Union/choice operation
    }
    
    class PlusEmpty~A~{
        +empty[A]: F[A]
        %% Empty/neutral element
    }
    
    Applicative <|-- ApplicativePlus
    Plus <|-- PlusEmpty
    PlusEmpty <|-- ApplicativePlus
    
    class IsEmpty~A~{
        +isEmpty[A](fa: F[A]) Boolean
        %% Check for emptiness
    }
    
    PlusEmpty <|-- IsEmpty
    
    note for Applicative "Functor with unit + apply"
    note for PlusEmpty "Monoid for contexts"
```

### 5. Monad Structures (core)

```mermaid
---
title: "Monads and Bind"
---
classDiagram
    direction TB
    class Bind~A~{
        +flatMap[B](fa: F[A], f: A => F[B]) F[B]
        %% Binding computations
    }
    
    class Monad~A~{
        %% Combination of Applicative + Bind
    }
    
    Apply <|-- Bind
    Applicative <|-- Monad
    Bind <|-- Monad
    
    class MonadPlus~A~{
        %% Monad with choice capability
    }
    
    Monad <|-- MonadPlus
    ApplicativePlus <|-- MonadPlus
    
    note for Bind "Foundation for monads"
    note for Monad "The most powerful structure"
    note for MonadPlus "Monad with alternatives"
```

### 6. Co-structural

```mermaid
---
title: "Co-structural"
---
classDiagram
    direction TB
    class CoBind~A~{
        +cobind[B](fa: F[A], f: F[A] => B) F[B]
        %% Dual of flatMap
    }
    
    class CoMonad~A~{
        +coUnit[A](fa: F[A]) A
        %% Extracting value from context
    }
    
    Functor <|-- CoBind
    CoBind <|-- CoMonad
    
    class Bimonad~A~{
        %% Combination of Monad and Comonad
    }
    
    Monad <|-- Bimonad
    CoMonad <|-- Bimonad
    
    note for CoBind "flatMap in reverse"
    note for CoMonad "Dual of monad"
    note for Bimonad "Rare structure"
```

### 7. Structures for Working with Collections

```mermaid
---
title: "Traversal and Folding"
---
classDiagram
    direction TB
    
    class Foldable~A~{
        +foldRight[B](fa: F[A])(acc: B)(f: (A,B)=>B) B
        +foldLeft[B](fa: F[A])(acc: B)(f: (B,A)=>B) B
        %% Folding a collection
    }
    
    class Traverse~A~{
        +traverse[G[_]: Applicative, B](fa: F[A])(f: A => G[B]) G[F[B]]
        +sequence[G[_]: Applicative, A](fga: F[G[A]]) G[F[A]]
        %% Traversal with effects
    }
    
    Functor <|-- Traverse
    Foldable <|-- Traverse
    
    class Reducible~A~{
        +reduceLeftTo[A,B](fa: F[A])(f: A=>B)(g: (B,A)=>B) B
        +reduceRightTo[A,B](fa: F[A])(f: A=>B)(g: (A,B)=>B) B
        %% Folding without initial value
    }
    
    class NonEmptyTraverse~A~{
        +nonEmptyTraverse[G[_]: Apply, A, B](fa: F[A])(f: A=>G[B]) G[F[B]]
        +nonEmptySequence[G[_]: Apply, A](fga: F[G[A]]) G[F[A]]
        %% Traversal of non-empty collections
    }
    
    Foldable <|-- Reducible
    Reducible <|-- NonEmptyTraverse
    Traverse <|-- NonEmptyTraverse
    
    note for Foldable "Knows how to fold"
    note for Traverse "Knows how to traverse with effects"
    note for NonEmptyTraverse "For non-empty structures"
```

### Full Hierarchy (overall picture)

**Type Structures:**
- **Functors** - mapping values while preserving context
- **Applicatives** - applying functions within context
- **Monads** - binding computations with dependency on previous results
- **Comonads** - dual structures (working "in reverse")
- **Traversals** - working with collections and effects

**Key Relationships:**
- Arrow `-->` denotes inheritance (a subtype is an extension of its supertype)
- Inheritance always adds new functionality

```mermaid
---
title: "Full Hierarchy of the Monad Group"
---
classDiagram
    direction TB
    
    InvariantFunctor --> Functor
    InvariantFunctor --> ContravariantFunctor
    Functor --> Apply
    Semigroupal --> Apply
    ContravariantFunctor --> Divide
    Apply --> Applicative
    InvariantApplicative --> Applicative
    Divide --> Divisible
    InvariantApplicative --> Divisible
    Apply --> Bind
    Applicative --> Monad
    Bind --> Monad
    Monad --> MonadPlus
    ApplicativePlus --> MonadPlus
    Functor --> CoBind
    CoBind --> CoMonad
    Monad --> Bimonad
    CoMonad --> Bimonad
    Functor --> Traverse
    Foldable --> Traverse
    Traverse --> NonEmptyTraverse
    Foldable --> Reducible
    Reducible --> NonEmptyTraverse
    
    class InvariantFunctor {
        <<abstract>>
        +imap
    }
    class Functor {
        +map
    }
    class ContravariantFunctor {
        +contramap
    }
    class Apply {
        +apply
    }
    class Applicative {
        +unit
    }
    class Monad {
        <<interface>>
    }
    
    note for InvariantFunctor "Foundation of all functors"
    note for Functor "Forward mapping"
    note for ContravariantFunctor "Reverse mapping"
    note for Apply "Application in context"
    note for Applicative "Lifting values"
    note for Monad "Sequential computations"
```

**References:**

- [Link to exercise repository][sample]
- [Functors, Applicatives, And Monads in Pictures](https://habr.com/ru/articles/183150)
- [A Monads Approach for Beginners, in Scala - Rock the JVM](https://www.youtube.com/watch?v=d-dy1x33moA&amp;list=PLmtsMNDRU0Bzj7INIrLugi3a_WClwQuiS)
- [Functors and things using Scala - Tony Morris](https://blog.tmorris.net/posts/functors-and-things-using-scala/index.html)
- [Functors, Applicatives, And Monads In Pictures](https://www.adit.io/posts/2013-04-17-functors,_applicatives,_and_monads_in_pictures.html)
- [More on Monoids and Monads](https://apocalisp.wordpress.com/2010/07/21/more-on-monoids-and-monads/)

[sample]: https://gitverse.ru/scalabook/typeclasses
