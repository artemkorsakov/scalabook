# IO

`IO` is a type class that represents an abstraction
for [working with input/output (I/O) in functional programming](../../fp/external-effects/common_io.md).
It allows describing I/O operations without executing them immediately,
which helps avoid side effects and makes code more predictable and testable.

**Where is it used?**

- **Asynchronous programming**: `IO` allows performing asynchronous operations,
  such as reading and writing files, network requests, and database interactions, without blocking the main execution thread.
- **Testing**: Thanks to its abstraction, `IO` makes it easy to test code
  that performs I/O operations by replacing real operations with mocks or stubs.
- **Side effect management**: `IO` helps control and manage side effects,
  allowing developers to explicitly specify when and how I/O operations should be executed.
- **Composing operations**: `IO` makes it easy to combine various I/O operations,
  creating complex sequences of actions.

**Advantages:**

- **Safety**: `IO` prevents unexpected side effects since operations are not executed immediately.
- **Readability**: Code becomes more understandable and structured since I/O operations are explicitly described.
- **Testability**: It's easy to test functions that use `IO` by replacing real operations with mocks or stubs.

## Formal definition

`IO` is a type encapsulating computations that may interact with the outside world
(e.g., reading from a file, writing to a screen, networking, etc.).

## Code definition in Scala

```dotty
sealed trait IO[+A]:
  private[effect] def apply(rw: World[RealWorld]): Trampoline[(World[RealWorld], A)]
```

## Implementation in libraries

### Implementation in ScalaZ

```dotty
import scalaz.*
import Scalaz.*
import scalaz.effect.*
import scalaz.effect.IO.*

val action1 =
  for
    x <- readLn
    _ <- putStrLn("Hello, " + x + "!")
  yield ()
action1.unsafePerformIO()
```

### Implementation in Cats

```dotty
import cats.effect.IO
import cats.effect.unsafe.implicits.global

// Define an IO action that prints a string
val printHello: IO[Unit] = IO {
  println("Hello, World!")
}

// Define an IO action that reads a string from the console
val readName: IO[String] = IO {
  scala.io.StdIn.readLine("Enter your name: ")
}

// Define an IO action that composes the previous actions
val program: IO[Unit] =
  for
    _    <- printHello
    name <- readName
    _    <- IO { println(s"Hello, $name!") }
  yield ()

// Run the program
program.unsafeRunSync()
```

---

**References:**

- [Repository with exercises][sample]
- [Functional Programming in Scala, Second Edition, Chapter 13](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/IO+Monad.html)
- [Towards an Effect System in Scala, Part 2: IO Monad](https://apocalisp.wordpress.com/2011/12/19/towards-an-effect-system-in-scala-part-2-io-monad/)

[sample]: https://gitverse.ru/scalabook/typeclasses
