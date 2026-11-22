# Conclusion

## The Big Picture

In the chapter "External Effects and I/O", we have explored how to work with **external effects**
(such as I/O, network requests, database reads) in a purely functional style.
We started with simple examples and gradually built up to more complex abstractions,
showing how to apply them to write pure, predictable, and testable programs.

The main goal was to separate **pure logic** from **side effects**,
so as to preserve the benefits of functional programming:
decomposability, composability, type safety, and code reuse.

## 1. Effects Can Be Factorized

Every impure method can be represented as a combination of:

- A **pure function** that receives data and returns a result.
- A **pair of side-effect operations**: one for reading input, another for output.

Example:

```dotty
def impureFunction(): Unit =
  val input  = readFromDatabase()
  val result = clean(input)
  writeToConsole(result)
```

This can be rewritten as:

```dotty
def pureLogic(input: String): String = clean(input)

val program: IO[Unit] =
  for
    input <- Files.read("data.txt")
    result = pureLogic(input)
    _ <- Console.printLn(result)
  yield ()
```

Thus, the pure part of the program is separated from effects, which simplifies testing and understanding.

## 2. The `IO` Monad as a Universal Abstraction

We created the `IO[A]` type, which allows describing any side effects as **pure values**.
This makes it possible to:

- Store effects in collections.
- Pass them between functions.
- Combine them using monadic operations (`flatMap`, `map`, etc.).

Example:

```dotty
val greeting: IO[Unit] =
  for
    name <- Console.readLn
    _    <- Console.printLn(s"Hello, $name!")
  yield ()
```

`IO` allows **delaying effect execution**, which makes the program more flexible.

## 3. Problems with `IO`: Stack Safety and Parallelism

Despite its advantages, a simple `IO` implementation has drawbacks:

- **Stack overflow** with deep recursion.
- **Limited support for asynchrony and concurrency**.
- **Low transparency and difficulty of state inspection**.

To solve these problems, we used **trampolining** — a technique that allows executing recursive calls without stack overflow.
We also became familiar with the `TailRec[A]` and `Async[A]` types, which provide stack-safe and asynchronous processing.

## 4. Free Monads (`Free`) and Interpreters

Free monads provide a powerful abstraction that allows:

- Describing sequences of primitive operations (e.g., `readLine`, `writeFile`).
- Interpreting these sequences in different target monads (e.g., `Par`, `Future`, `TailRec`).

Example:

```dotty
trait Console[F[_]]:
  def readLn: F[String]
  def printLn(line: String): F[Unit]

def greet[F[_]](using c: Console[F]): F[Unit] =
  for
    name <- c.readLn
    _    <- c.printLn(s"Hello, $name!")
  yield ()
```

Here we can use different `Console` implementations, for example, for tests or real I/O,
without changing the program logic itself.

## 5. Capability Traits

Capability traits allow us to define a set of primitive operations that a program can use.
They provide **local abstraction**, making code simpler to understand and test.

Example:

```dotty
trait Files[F[_]]:
  def readLines(file: String): F[List[String]]
  def writeLines(file: String, lines: List[String]): F[Unit]
```

Such traits compose easily and can be implemented for different monads.

## 6. The `IO` Type Is Not the End of the Road

Although `IO` is a powerful abstraction, it is **not the ideal solution for all tasks**.
`IO`-based programs often become **monolithic**, especially when working with streams,
concurrency, or complex transformations.

Therefore, it is important to remember that `IO` is just **a tool**, and in more advanced cases
we can use **more specialized abstractions**, such as:

- Stream processors (`Stream`).
- Event systems (`EventSourcing`).
- Actor models (`Actor Systems`).

## 7. Local Effects and the Type System

We also briefly touched on **local effects**, such as temporary state changes within a limited scope.
These effects do not affect the outside world, but still require attention,
especially if we want to guarantee **predictability** and **correctness** of the program.

In the ["Local Effects and Mutable State"](../local-effects.md) chapter, we will explore in detail
how the **type system** can help us control the scope of effects and ensure their safety.

## 8. Real Libraries and Practices

In practice, the `IO` type is usually implemented in libraries such as [Cats Effect](https://typelevel.org/cats-effect/),
which provide:

- Support for **asynchronous and parallel execution**.
- **Resource management** (e.g., automatic file closing).
- **Cancellation** support for operations.
- Integration with other libraries and ecosystems.

Example of Cats Effect usage:

```dotty
import cats.effect.IO

val program: IO[Unit] =
  for
    line <- IO.readLine
    _    <- IO.println(s"You entered: $line")
  yield ()
```

## Chapter Summary

- Any impure procedure can be decomposed into a pure function and two effects: input and output.
- `IO[A]` is a universal type that allows describing side effects as pure values.
- A simple `IO` implementation has problems: stack overflow, low performance, complexity of concurrency.
- Using **trampolining** and `TailRec` and `Async` types solves the stack overflow problem.
- Free monads (`Free`) allow describing programs as sequences of primitive operations.
- Capability traits (`Capabilities`) provide local abstraction and simplify testing.
- `IO` is a powerful tool, but it should not be used in all situations.
- There are more specialized abstractions for working with streams, events, and concurrency.
- Local effects also require attention, especially when working with temporary state changes.
- Libraries such as Cats Effect provide advanced `IO` implementation with support for asynchrony, resources, and cancellation.

---

**Links:**

- [Repository with exercises][sample]
- [Functional Programming in Scala, Second Edition, Chapter 13](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
