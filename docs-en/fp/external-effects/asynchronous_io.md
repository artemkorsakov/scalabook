# Non-blocking and Asynchronous I/O

Let's addressing the last remaining problem of our original `IO` monad: executing **non-blocking (asynchronous) I/O**.

In programming, I/O is often associated with operations that may wait a long time for external data or devices without consuming CPU cycles. For example:

- reading from a file,
- reading from a network socket,
- writing large volumes of data.

If such operations are executed in **blocking mode**, the program's execution thread will wait for the operation to complete, which can lead to a loss of performance. This is especially critical in multi-user systems where many concurrent requests need to be handled.

## Asynchrony via `Free`

Using [free monads](free_monad.md) (`Free[F, A]`), we can abstract I/O operations from their implementation. This allows us to write pure functions that describe a sequence of actions, but do not depend on their specific manner of execution.

### Example: Interpreter for `Console`

Suppose we have a program that interacts with the console:

```dotty
def p: Free[Console, Unit] =
  for
    _ <- printLn("What's your name?")
    n <- readLn
    _ <- n match
      case Some(n) => printLn(s"Hello, $n!")
      case None => printLn(s"Fine, be that way.")
  yield ()
```

This program has the type `Free[Console, Unit]`, meaning it describes a sequence of console operations, but does not execute them immediately. To run it, we need an interpreter.

#### Synchronous Interpreter (`Function0`)

If we want to execute this program synchronously, we can use an interpreter that translates `Console[A]` into `() => A`:

```dotty
val q = p.toThunk
q() // Executes program synchronously
```

However, this approach blocks the execution thread while waiting for input or output.

#### Asynchronous Interpreter (`Par`)

To make the program asynchronous, we can use the `Par` monad, which supports parallel and non-blocking execution:

```dotty
val q = p.toPar
```

Here `toPar` is a function that transforms `Free[Console, A]` into `Par[A]`. This is achieved using the `runFree` method, which takes a function translating `Console[A]` into `Par[A]`.

### How `runFree` Works

The `runFree` method generalizes the interpretation of `Free[F, A]` into another monad `G`, if we have a way to translate `F[x]` into `G[x]`. Here is its signature:

```dotty
def runFree[G[_]](t: [x] => F[x] => G[x])(using G: Monad[G]): G[A]
```

Example usage:

```dotty
def toPar(x: Console[x]): Par[x] = x match
  case ReadLine        => Par.lazyUnit(Try(readLine()).toOption)
  case PrintLine(line) => Par.lazyUnit(println(line))

val q = p.runFree(toPar)
```

Thus, `Free[Console, A]` becomes `Par[A]`, which can be executed asynchronously using `Par`'s distributed computation mechanism.

### Non-blocking I/O

For real asynchronous I/O, we need libraries that provide non-blocking operations. For example, consider an interface for a non-blocking data source:

```dotty
trait Source:
  def readBytes(
      numBytes: Int,
      callback: Either[Throwable, Array[Byte]] => Unit
  ): Unit
```

Here `readBytes` does not block the execution thread. Instead, it takes a callback that is invoked when data becomes available or on error. Such operations are ideal for asynchronous programming.

#### Integration with `Free`

To integrate non-blocking I/O into our program, we can create a type that describes possible operations with `Source`:

```dotty
enum FileIO[A]:
  case Read(numBytes: Int)      extends FileIO[Array[Byte]]
  case Write(data: Array[Byte]) extends FileIO[Unit]
```

Then we can use `Free[FileIO, A]` to describe programs that work with files. For interpreting such programs, we create a translation function from `FileIO` into `Par`:

```dotty
def fileToPar(x: FileIO[x]): Par[x] = x match
  case Read(n) =>
    val buffer = new Array[Byte](n)
    // Example; should be replaced with a real async call
    Par.lazyUnit(buffer)
  case Write(data) =>
    Par.lazyUnit(()) // Similarly
```

Using `runFree`, we obtain a program as `Par[A]`, which can be run asynchronously.

### Advantages of Asynchronous I/O

1. **Performance**: Threads are not blocked waiting for I/O, allowing efficient use of resources.
2. **Flexibility**: Programs remain pure and easily testable, as effects are separated from logic.
3. **Extensibility**: We can easily add new kinds of I/O by simply changing the interpreter.

### Conclusion

Using free monads (`Free`) allows us to abstract I/O operations from their implementation. This makes programs more pure, testable, and flexible. With interpreters, we can run such programs both synchronously and asynchronously, connecting to various I/O libraries and systems.

## Abstracting Over Asynchronous I/O Using `Par` and `Free`

When we talk about non-blocking (asynchronous) I/O operations such as reading from a file or network stream, it is important not only to use system resources efficiently, but also to preserve the purity and composability of programs. Direct use of libraries such as `java.nio` can be difficult, as they often work with callbacks, making them hard to integrate into structured functional programs.

### Why Abstract from Implementation Details?

Working with low-level APIs directly is difficult, especially if we want to create pure, testable, and reusable programs. We want to:

- **Use a monadic interface** for convenient operation composition.
- **Abstract from the specific implementation** of non-blocking I/O.
- **Support asynchrony**, so as not to block the execution thread.

For this we can use the `Par` type, which represents a parallel computation, and `Free[F, A]`, which models a sequence of actions as a tree that can be interpreted in various ways.

### What is `Par` and How Does It Work?

`Par` is an abstraction for [parallel computations](../parallelism.md). It is defined as follows:

```dotty
opaque type Future[+A] = (A => Unit) => Unit
opaque type Par[+A] = ExecutorService => Future[A]
```

- `Future[A]` is a function that takes a completion handler (`A => Unit`) and returns a result when it becomes available.
- `Par[A]` is a function that takes an `ExecutorService` and returns a `Future[A]`.
- This allows us to perform computations in background threads.

Thus, `Par` allows modeling asynchronous computations that can be run in parallel and do not block the main thread.

### Adapting Async APIs to `Par`

Many I/O libraries, such as `java.nio`, use a callback-based model. For example, the `readBytes` method may look like this:

```dotty
trait Source:
  def readBytes(numBytes: Int, callback: Either[Throwable, Array[Byte]] => Unit): Unit
```

This means we pass a function (`callback`) that will be invoked when data is available or when an error occurs.

To integrate such an API with `Par`, we can use a helper `async` method, converting the callback into a `Par`:

```dotty
def async[A](f: (A => Unit) => Unit): Par[A] =
  es => cb => f(cb)
```

Now we can "wrap" asynchronous operations in `Par` for use in a pure functional style.

### Wrapping `Source.readBytes`

Suppose we have a data source `Source`, and we want to read `numBytes` bytes from it. We can define a `nonblockingRead` function that returns a `Par`:

```dotty
def nonblockingRead(source: Source, numBytes: Int): Par[Either[Throwable, Array[Byte]]] =
  async: (cb: Either[Throwable, Array[Byte]] => Unit) =>
    source.readBytes(numBytes, cb)
```

Now, to use this in the context of `Free`, we can create a `readPar` function that wraps `nonblockingRead` in `Suspend`, so it can be used as a step in a `Free[Par, A]` program:

```dotty
def readPar(source: Source, numBytes: Int): Free[Par, Either[Throwable, Array[Byte]]] =
  Suspend(nonblockingRead(source, numBytes))
```

Now we can build chains of asynchronous operations using `for-comprehension`, as if they worked with an ordinary monad:

```dotty
val src: Source = ...
val prog: Free[Par, Unit] =
  for
    chunk1 <- readPar(src, 1024)
    chunk2 <- readPar(src, 1024)
  yield ()
```

This program describes two asynchronous read operations from the `src` source.

### Implementing Async File Reading

For example, let's consider how we can use `AsynchronousFileChannel` from the `java.nio` library to read a file asynchronously.

#### Implementation Steps

1. Create a `ByteBuffer` where data will be written.
2. Use `file.read(...)` with a completion handler.
3. On successful read, call `cb(Right(arr))`.
4. On error, call `cb(Left(err))`.

Here is the implementation of the `read` function:

```dotty
import java.nio._
import java.nio.channels._

def read(
    file: AsynchronousFileChannel,
    fromPosition: Long,
    numBytes: Int
): Free[Par, Either[Throwable, Array[Byte]]] =
  Suspend(
    Par.async: (cb: Either[Throwable, Array[Byte]] => Unit) =>
      val buf = ByteBuffer.allocate(numBytes)
      file.read(
        buf,
        fromPosition,
        (),
        new CompletionHandler[Integer, Unit]:
          def completed(bytesRead: Integer, ignore: Unit): Unit =
            // Shift buffer to get only the read data
            buf.flip()
            val arr = new Array[Byte](bytesRead)
            buf.get(arr, 0, bytesRead)
            cb(Right(arr))

          def failed(err: Throwable, ignore: Unit): Unit =
            cb(Left(err))
      )
  )
```

#### How Does This Work?

- `Par.async` takes a function that receives `cb` — a completion handler.
- We call `file.read(...)`, passing `buf` and `CompletionHandler`.
- In the `completed` method, we extract the read data and pass it via `cb`.
- In the `failed` method, we pass the error via `cb`.

Thus, the `Par` value created inside `Par.async` completes when the completion handler is invoked.

### Why Is This Useful?

- **Purity**: Our program is separated from the implementation of effects.
- **Composition**: We can easily combine asynchronous operations.
- **Flexibility**: We can change the program's interpretation (for example, replace real file read with a simulation).
- **Performance**: Operations are performed asynchronously and do not block the thread.

### Summary

Using `Par` and `Free` allows abstracting away from the implementation details of asynchronous I/O operations. This makes our programs more pure, testable, and flexible. We can easily combine asynchronous actions via `for-comprehension`, and use ready-made libraries such as `java.nio` with minimal effort.

## Composing Free Algebras

In previous sections, we learned how to use `Free[F, A]` to abstract over various effects, such as I/O (`IO`, `Console`) and parallel computations (`Par`). However, there is often a need to combine several different algebras — for example, to work with both the file system and the console simultaneously. Below we examine how free algebras can be composed and combined into a single program.

### What Is a "Free Algebra"?

A free algebra is a way of describing a set of primitive operations (such as read or write) that can be performed, but are not executed immediately. It is defined through an algebraic data type (enum) and modeled using `Free[F, A]`.

For example, for working with the console we can define:

```dotty
enum Console[A]:
  case ReadLine                extends Console[Option[String]]
  case PrintLine(line: String) extends Console[Unit]

object Console:
  def readLn: Free[Console, Option[String]] = Free.Suspend(ReadLine)

  def printLn(line: String): Free[Console, Unit] =
    Free.Suspend(PrintLine(line))
```

We can also create a similar algebra for working with files:

```dotty
enum Files[A]:
  case ReadLines(file: String) extends Files[List[String]]
  case WriteLines(file: String, lines: List[String]) extends Files[Unit]

object Files:
  def readLines(file: String): Free[Files, List[String]] = 
    Free.Suspend(ReadLines(file))
    
  def writeLines(file: String, lines: List[String]): Free[Files, Unit] =
    Free.Suspend(WriteLines(file, lines))
```

Each of these types allows describing sequences of operations, but they are **independent** of each other. Problems arise when we need to **combine** programs that use different algebras.

### Problem: Inability to Mix Different Algebras

Suppose we want to write a `cat` utility that reads lines from a file and prints them to the console:

```dotty
def cat(file: String): Free[Files, Unit] =
  for
    lines <- Files.readLines(file)
    _     <- Console.printLn(lines.mkString("\n"))
  yield ()
```

This program will not compile! The error will look something like this:

```
Found:    Free[Console, Unit]
Required: Free[Files, Unit]
```

The reason is that `Files.readLines(file)` returns a value of type `Free[Files, List[String]]`, while `Console.printLn(...)` returns `Free[Console, Unit]`. But `Free[Console, Unit]` is **not a subtype** of `Free[Files, Unit]`. Thus, Scala cannot understand how to combine them.

### Solution: Algebra Composition

To solve this problem, we can use **algebra composition**. That is, we create a new type that includes operations from both algebras. In Scala 3, this is done using **union types**.

#### Step 1: Creating a Union Algebra

We want to create a new algebra containing all operations from `Console` and `Files`. For this we declare a generalized type `[x] =>> Console[x] | Files[x]`, where `|` denotes union.

#### Step 2: Using the `union` Method

We can transform `Free[F, A]` into `Free[[x] =>> F[x] | G[x], A]` using the `union` method. This allows us to **lift** values from one algebra into the union.

The `union` method is defined as follows:

```dotty
enum Free[+F[_], A]:
  ...
  def union[G[_]]: Free[[x] =>> F[x] | G[x], A] = this match
    case Return(a) => Return(a)
    case Suspend(s) => 
      // s has type F[A], so we can assign type F[A] | G[A]
      Suspend(s: F[A] | G[A])
    case FlatMap(s, f) => 
      // Propagate union through both arguments of FlatMap
      FlatMap(s.union, a => f(a).union)
```

#### Step 3: Rewriting `cat` with `union`

Now we can rewrite the `cat` function using `union` to combine `Files` and `Console`:

```dotty
def cat(file: String): Free[[x] =>> Files[x] | Console[x], Unit] =
  for
    lines <- Files.readLines(file).union
    _     <- Console.printLn(lines.mkString("\n")).union
  yield ()
```

Here, each expression returning `Free[Console, ...]` or `Free[Files, ...]` is transformed into `Free[[x] =>> Files[x] | Console[x], ...]` via `.union`.

### Advantages of Algebra Composition

1. **Flexibility**: We can write programs using multiple effects.
2. **Purity**: All effects remain separated from program logic.
3. **Separation of Concerns**: Each algebra describes its own domain.
4. **Easy Interpretation**: We can write interpreters for each algebra separately.

## Improving Free Program Composition with Covariance and `union`

In the previous section we examined how to combine different algebras (such as `Console` and `Files`) within a single `Free` type to create programs that can use multiple effects. However, this required manually calling the `union` method for each program step, making the code cumbersome and hard to infer.

Now we will see how to improve this using **covariance** and a correct definition of the `flatMap` method. This will allow us to write cleaner, more convenient code, without the need to explicitly call `union`.

### Problem: Need for Manual `union` Calls

Consider the `cat` function, which reads a file and prints its contents to the console:

```dotty
def cat(file: String) =
  Files.readLines(file).union[Console].flatMap: lines =>
    Console.printLn(lines.mkString("\n")).union[Files]
```

This code works, but it looks complex and requires manually calling `.union` for each expression. Why is this the case?

The reason is that the `Free[F, A]` type is **not covariant** in the parameter `F` by default. That is, if we have `Free[Console, A]`, we cannot directly use it where `Free[[x] =>> Console[x] | Files[x], A]` is expected. Therefore, we must manually transform the type using `union`.

### Solution: Covariance and the Correct `flatMap` Definition

To avoid manual `union` calls, we can take advantage of **covariance**. If we declare `Free` as covariant in `F`, then `Free[F, A]` becomes a subtype of `Free[G, A]` whenever `F` is a subtype of `G`.

#### Declaring Covariance

We declare `Free` as follows:

```dotty
enum Free[+F[_], A]:
  case Return(a: A) extends Free[Nothing, A]
  case Suspend(s: F[A])
  case FlatMap[F2[_], A, B](
    s: Free[F2, A],
    f: A => Free[F2, B]) extends Free[F2, B]

  def flatMap[F2[x] >: F[x], B](f: A => Free[F2, B]): Free[F2, B] =
    FlatMap(this, f)
```

Here:

- `+F[_]` tells Scala that `F` is covariant.
- In `FlatMap` we add the parameter `F2[x] >: F[x]`, which allows `flatMap` to return `Free[F2, B]` where `F2` is a supertype of `F`.

#### Why Is This Important?

Covariance allows automatically converting `Free[F, A]` to `Free[G, A]` when `F` is a subtype of `G`. For example, `Free[Console, A]` is a subtype of `Free[[x] =>> Console[x] | Files[x], A]`.

Because of this, we no longer **need to call `union` manually**. We simply use `Free` as usual, and Scala performs the necessary transformations automatically.

### Simplifying `union` and Introducing `covary`

With covariance, `Free` makes it simpler to define the `union` method. Now it can be simply:

```dotty
enum Free[+F[_], A]:
  ...
  def union[G[_]]: Free[[x] =>> F[x] | G[x], A] = this
```

This is possible because `Free[F, A]` is already a subtype of `Free[[x] =>> F[x] | G[x], A]`, and no additional transformations are needed.

Additionally, we can introduce the `covary` operation, which allows specifying a supertype `F2` without restating the entire type expression:

```dotty
enum Free[+F[_], A]:
  ...
  def covary[F2[x] >: F[x]]: Free[F2, A] = this
```

This is mere syntactic sugar. For example, instead of:

```dotty
Files.readLines("file.txt").union[Console]
```

we could write:

```dotty
Files.readLines("file.txt").covary[[x] =>> Console[x] | Files[x]]
```

However, thanks to covariance, even this is unnecessary — it is enough to simply use `Free` in a context where a union type is required.

### Rewriting `cat` Without `union`

Now that we have a covariant version of `Free`, we can rewrite the `cat` function without using `union`:

```dotty
def cat(file: String): Free[[x] =>> Files[x] | Console[x], Unit] =
  for
    lines <- Files.readLines(file)
    _     <- Console.printLn(lines.mkString("\n"))
  yield ()
```

Note:

- We no longer call `.union`.
- We do not change the signatures of `Files.readLines` or `Console.printLn`.
- We explicitly specify the return type of `cat`, because Scala does not infer the union type automatically.

### Why Does This Work?

Everything works thanks to:

1. `Free[F, A]` is now covariant in `F`.
2. `Files.readLines(file)` returns `Free[Files, List[String]]`.
3. `Console.printLn(...)` returns `Free[Console, Unit]`.
4. Since `Free[Files, ...]` and `Free[Console, ...]` are subtypes of `Free[[x] =>> Files[x] | Console[x], ...]`, they can be used together in one program without manual transformation.

### Conclusion

Covariance and correct use of `flatMap` allow significantly simplifying work with multiple algebras within a single `Free`. Now:

- There is no need to call `union` manually.
- Programs become cleaner and easier to read.
- Scala helps with automatic type inference, especially if we explicitly specify return types.

This makes `Free` an even more powerful tool for modeling effects in functional programming.

## Exercises

### Exercise 9.4.1: Defining `step` for `Free[+F[_], A]`

> [Repository with exercises and instructions][sample]

Refine the `step` method for the covariant type `Free[+F[_], A]`. The `step` method is used to simplify the `Free` structure to avoid nested `FlatMap`.

```dotty
final def step: Free[F, A] = ???
```

@:note(🔍 Show solution)

The `step` method simplifies the `Free` structure by combining nested `FlatMap`s to avoid their accumulation. In the covariant version of `Free[+F[_], A]`, it handles cases where a `FlatMap` is nested within another `FlatMap` or follows a `Return`, converting them into a simpler form using `flatMap`. Additionally, it uses `covary` to preserve correctness of types when switching between algebras.

```dotty
@annotation.tailrec
final def step: Free[F, A] = this match
  case FlatMap(FlatMap(fx, f), g) =>
    fx.flatMap(x => f(x).flatMap(y => g(y).covary[F])).step
  case FlatMap(Return(x), f) => f(x).step
  case _                     => this
```

@:@

### Exercise 9.4.2: Defining `run` for `Free[+F[_], A]`

Implement the `run` method for `Free[+F[_], A]`. This method executes a `Free[F, A]` program in the context of monad `F2`.

```dotty
def run[F2[x] >: F[x]](using F: Monad[F2]): F2[A] = ???
```

@:note(🔍 Show solution)

The `run` method executes a `Free[F, A]` program using monad `F2`, which is a supertype of `F`. It works with the simplified version of the program after calling `step`: if the program is completed (`Return`), it returns the result; if it contains `Suspend`, it returns the corresponding value; if it starts with `FlatMap(Suspend(...), ...)`, it executes the `flatMap` and continues execution recursively. Nested `FlatMap`s are eliminated by `step`, so `sys.error` is impossible.

```dotty
def run[F2[x] >: F[x]](using F: Monad[F2]): F2[A] = step match
  case Return(a)               => F.unit(a)
  case Suspend(fa)             => fa
  case FlatMap(Suspend(fa), f) => fa.flatMap(a => f(a).run)
  case FlatMap(_, _) =>
    sys.error("Impossible, since `step` eliminates these cases")
```

@:@

### Exercise 9.4.3: Defining `runFree` for `Free[+F[_], A]`

Implement the `runFree[G[_]]` method, which allows interpreting `Free[F, A]` in another monad `G`, if we have a way to translate `F[x]` into `G[x]`.

```dotty
def runFree[G[_]](t: [x] => F[x] => G[x])(using G: Monad[G]): G[A] = ???
```

@:note(🔍 Show solution)

The `runFree` method interprets a `Free[F, A]` program in another monad `G`, using the function `t` that translates `F[x]` into `G[x]`. It works with the simplified structure after `step`: if the program is completed (`Return`), it returns the result; if it contains `Suspend`, it applies `t`; if it starts with `FlatMap(Suspend(...), ...)`, it continues execution recursively via `flatMap`. Nested `FlatMap`s are excluded by `step`, so `sys.error` is impossible.

```dotty
def runFree[G[_]](t: [x] => F[x] => G[x])(using G: Monad[G]): G[A] =
  step match
    case Return(a)  => G.unit(a)
    case Suspend(r) => t(r)
    case FlatMap(Suspend(r), f) =>
      t(r).flatMap[A](a => f(a).covary[F].runFree[G](t))
    case FlatMap(_, _) =>
      sys.error("Impossible, since `step` eliminates these cases")
```

@:@

## Running a Program with Multiple Algebras

Now that we have a `Free[[x] =>> Files[x] | Console[x], Unit]`, we need to define how to run it. For this we use `runFree`, passing a function that interprets each operation.

```dotty
extension [A](fa: Free[[x] =>> Files[x] | Console[x], A])
  def toThunk: () => A =
    fa.runFree([x] => (fx: Files[x] | Console[x]) => fx match
      case c: Console[x] => c.toThunk
      case f: Files[x] => f.toThunk
    )
```

- We define an extension `toThunk` for `Free`, which returns `() => A`.
- Inside we use `runFree`, passing a function that:
  - If it receives a value of type `Console[x]`, uses `c.toThunk`.
  - If it receives a value of type `Files[x]`, uses `f.toThunk`.
- This way, we can run a program that uses both algebras.

---

**Links:**

- [Repository with exercises][sample]
- [Functional Programming in Scala, Second Edition, Chapter 13](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
