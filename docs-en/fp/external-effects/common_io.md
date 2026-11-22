# General-purpose I/O Type: IO

## Introduction

When working with side effects (I/O, database reads, etc.) in functional programming, it is important to preserve the purity of the program's core. To achieve this, we often use abstractions such as `Free` algebras to separate the description of actions from their execution.

However, using `Free` requires complex types and additional logic for interpreting programs. This can make the code harder to understand, especially for beginners.

In this section, we explore an alternative approach: **modeling effects through capability traits**. This method allows us to:

- Simplify effect composition.
- Make code more readable and maintainable.
- Preserve the benefits of the principle of least power.

### The Principle of Least Power

The principle of least power means that a program should use the minimal necessary set of operations to accomplish its task. This helps to:

- Avoid unnecessary side effects.
- Increase program predictability.
- Simplify testing and code reuse.

For example, if a program only works with the console, it should not depend on file system or network operations.

## Modeling Effects through Capability Traits

Instead of using `Free` algebras, we can model effects as **traits** parameterized by a target monad. The trait defines a set of primitive operations that can be performed within that monad.

### Example: `Console` Trait

```dotty
trait Console[F[_]]:
  def readLn: F[Option[String]]
  def printLn(line: String): F[Unit]
```

Here:

- `F[_]` is the monad type in which operations are executed.
- `readLn` returns a value of type `F[Option[String]]`, which represents a console read operation.
- `printLn` returns a value of type `F[Unit]`, representing a console output operation.

Such traits allow creating programs that work with effects while remaining pure and easily testable.

### Implementing Traits for Concrete Monads

We can provide implementations of these traits for different monads, such as `Par` (asynchronous parallelism) or `Function0` (a lazily evaluated zero-argument function).

#### Example: Console for `Par`

```dotty
given parConsole: Console[Par] with
  def readLn: Par[Option[String]] =
    Par.lazyUnit(Try(readLine()).toOption)

  def printLn(line: String): Par[Unit] =
    Par.lazyUnit(println(line))
```

#### Example: Console for `Function0`

```dotty
given thunkConsole: Console[Function0] with
  def readLn: () => Option[String] =
    () => Try(readLine()).toOption

  def printLn(line: String): () => Unit =
    () => println(line)
```

Each implementation corresponds to a different way of executing effects, but the program logic remains the same.

### Effect Composition

With capability traits, composition becomes simpler. We can combine different effects in one program without worrying about complex types or explicit algebra composition.

#### Example: Greeting Program

```dotty
def greet[F[_]](using c: Console[F], m: Monad[F]): F[Unit] =
  for
    _ <- c.printLn("What's your name?")
    n <- c.readLn
    _ <- n match
      case Some(n) => c.printLn(s"Hello, $n!")
      case None    => c.printLn(s"Fine, be that way.")
  yield ()
```

Here:

- We use for-comprehension for sequential operation execution.
- `Monad[F]` allows ordering actions via `flatMap`.
- All effects are described through `Console[F]`.

### Composition of Multiple Effects

Consider an example where a program uses multiple traits: `Console` and `Files`.

#### Example: `cat` Operation (printing file contents)

```dotty
trait Files[F[_]]:
  def readLines(file: String): F[List[String]]
  def writeLines(file: String, lines: List[String]): F[Unit]

def cat[F[_]](file: String)(
  using c: Console[F], f: Files[F], m: Monad[F]
): F[Unit] =
  f.readLines(file).flatMap: lines =>
    c.printLn(lines.mkString("\n"))
```

Here:

- `Files[F]` provides file read and write operations.
- `cat` combines reading from a file and printing to the console.
- All operations occur within the same monad `F`.

### Advantages of This Approach

1. **Simplified Composition**
   - No need for complex types such as algebra unions (`[x] =>> Files[x] | Console[x]`).
   - Simple combination of different effects in one program.

2. **Easy Testing**
   - Real effects can be replaced with test implementations of traits.
   - For example, instead of real console reading, you can use a mock implementation.

3. **Stack Safety**
   - If the target monad (`F`) provides stack safety (e.g., `TailRec`), the whole program will be stack-safe.
   - This is especially important for recursive programs.

4. **Flexibility**
   - You can choose any monad depending on needs (synchronous, asynchronous, lazy, etc.).
   - Changing the monad affects only the outer part of the program.

### Summary

The capability trait approach allows creating programs with effects while preserving the benefits of functional programming. It simplifies composition, makes code more readable and testable, and allows flexible strategy selection for execution.

This approach is a good alternative to `Free` algebras, especially when code simplicity and readability are important.

## General-purpose I/O Type: IO

### General Methodology for Working with I/O

When creating programs that interact with the outside world (e.g., reading files, writing data, printing to the console), we strive to preserve the purity and predictability of the core code. To do this, we use abstractions such as `IO` to separate the description of actions from their execution.

So: what is `IO`?

`IO` is a data type that allows describing computations related to I/O without directly executing side effects. It models actions that can be performed later, for example:

- Printing a string to the console.
- Reading a file.
- Saving data to a database.

Unlike imperative code, where effects happen immediately, `IO` represents these actions as values that can be stored, passed, and combined without invoking them immediately.

```dotty
opaque type IO[A] = Free[Par, A]
```

Here:

- `Free[Par, A]` is a [free monad](../../typeclass/monad/free-monads.md) that allows building sequences of actions.
- `Par` is a type for asynchronous computations (e.g., parallel or non-blocking).
- `A` is the result of the computation, which can be of any type.

Thus, `IO[A]` represents a program that:

- Can perform I/O operations.
- Can run asynchronously.
- Can be interpreted later, when needed.

### The Main Program at the End of the World

When we write an application that runs on the JVM, we need to implement a `main` method. Typically, it has the signature `def main(args: Array[String]): Unit`, and its execution starts the program.

However, in functional programming, we want our program to be **pure**, meaning it should not contain side effects. To achieve this, we can split the program into two parts:

1. **The pure part** — `pureMain`, which returns a value of type `IO[Unit]`.
2. **The interpreter** — `main`, which runs the `pureMain` program.

#### Example Implementation of `IOApp`

```dotty
trait IOApp:
  import java.util.concurrent.{Executors, ExecutorService}
  
  // Extension adding the `unsafeRunSync` method to the `IO` type
  extension [A](ioa: IO[A])
    def unsafeRunSync(pool: ExecutorService): A =
      ioa.run.run(pool)
  
  // The `main` method that runs the program
  def main(args: Array[String]): Unit =
    val pool = Executors.newFixedThreadPool(8) // Create a thread pool
    pureMain(args.toList).unsafeRunSync(pool)  // Run the pure program
  
  // Pure method describing the program as `IO[Unit]`
  def pureMain(args: List[String]): IO[Unit]
```

#### How Does This Work?

- `pureMain` is a pure function that returns `IO[Unit]`.
  It does not contain side effects directly; it simply describes what actions should be performed.

- `main` is the method that starts the program using a thread pool (`ExecutorService`).
  It interprets `IO[Unit]` and executes all actions described in `pureMain`.

- `unsafeRunSync` is the method that converts `IO[A]` into `A`, actually performing all actions.
  The _"unsafe"_ name indicates that this is the entry point for real side effects.

### Difference Between Effects and Side Effects

It is important to understand the difference between **effects** and **side effects**:

- **Effects** are actions that change the state of the outside world, but are described within `IO`.
  For example, reading a file or printing a message to the screen.

- **Side Effects** are unexpected and unpredictable changes
  that violate the principles of functional programming.
  For example, changing a global variable or writing to a file without explicit indication.

In our case, `pureMain` is a pure function that **does not contain side effects**,
but describes a program that contains **effects**.
The execution of these effects occurs only in the `main` method, outside the scope of `pureMain`.

Thus, we say that our program **has effects** but **does not have side effects**.

### Advantages of This Approach

1. **Purity of the Program Core**
   - The main program logic is in `pureMain`, which does not contain side effects.
   - This makes it easily testable and predictable.

2. **Execution Flexibility**
   - We can use different `IO` implementations, for example, for synchronous or asynchronous execution.
   - Changing the execution method affects only `main`, not the entire program.

3. **Type Safety**
   - All effects are described through types, allowing the compiler to verify program correctness.

4. **Scalability**
   - We can easily add new effects by defining new traits and implementations for `IO`.

### Summary

The `IO` type provides a powerful abstraction for working with I/O in functional programming.
It allows describing effects as pure values, avoiding side effects in the core code.
With `IOApp`, we can split the program into a pure part and an interpreter,
making it safer, more flexible, and easier to test.

This approach becomes especially useful when working with asynchronous, parallel, or resource-intensive operations,
where control over execution and order of actions is critically important.

## Why the I/O Type Is Insufficient for Streaming I/O

The `IO` type and monads in functional programming provide a powerful abstraction for working with side effects.
They allow describing I/O-related computations as pure values,
making code more predictable and testable.
However, despite all the advantages of `IO`, this type has its limitations,
especially when working with **streaming I/O**.

Below we examine why `IO` may be insufficient for efficient stream processing,
and how we can improve the situation with a more composable and flexible architecture.

### Problem: Processing Large Files

Suppose we have a text file `fahrenheit.txt` containing a sequence of temperatures in Fahrenheit.
We want to convert each temperature to Celsius and save the result in a new file `celsius.txt`.

#### Using `IO` with Full File Loading

First, we can try using the `IO` type to implement this task:

```dotty
trait Files[F[_]]:
  def readLines(file: String): F[List[String]]
  def writeLines(file: String, lines: List[String]): F[Unit]

def convert[F[_]](using f: Files[F]): F[Unit] =
  for
    lines <- f.readLines("fahrenheit.txt")
    cs = lines.map(s => fahrenheitToCelsius(s.toDouble).toString)
    _ <- f.writeLines("celsius.txt", cs)
  yield ()
```

This code works correctly, but has a significant drawback: it loads the **entire file into memory**.
If the file is large (e.g., contains millions of lines), this can lead to **high memory consumption**
and even to **stack overflow or program crash**.

### Optimization Attempt: Streaming Processing

To avoid fully loading the file into memory, we can use a low-level API
that allows working with the file line by line or in chunks:

```dotty
trait Files[F[_]]:
  def openRead(file: String): F[HandleR]
  def openWrite(file: String): F[HandleW]
  def readLine(h: HandleR): F[Option[String]]
  def writeLine(h: HandleW, line: String): F[Unit]

trait HandleR
trait HandleW
```

Now we can implement line-by-line file processing:

```dotty
def loop[F[_]](hr: HandleR, hw: HandleW)(using f: Files[F]): F[Unit] =
  f.readLine(hr).flatMap:
    case None => IO.unit(())
    case Some(s) =>
      f.writeLine(hw, fahrenheitToCelsius(s.toDouble))
        .flatMap(_ => loop(hr, hw))

def convertFiles[F[_]](using f: Files[F]): F[Unit] =
  for
    hr <- f.openRead("fahrenheit.txt")
    hw <- f.openWrite("celsius.txt")
    _  <- loop(hr, hw)
  yield ()
```

This approach allows processing a file **one line at a time**, saving memory.
However, it has a **serious drawback**: the code becomes **monolithic** and **incomposable**.

### The Monolithic Loop Problem

In the above example, we used a recursive `loop` function that processes each line and calls itself.
Although this works, this approach **does not compose** — if we need to add additional logic,
such as calculating a moving average, we would have to change the `loop` function itself.

Compare this with list processing, where we can easily combine operations:

```dotty
def movingAvg(n: Int)(l: List[Double]): List[Double] = ???

val cs =
  movingAvg(5)(
    lines.map(s => fahrenheitToCelsius(s.toDouble))
  ).map(_.toString)
```

Here, we can apply functions to a list like a pipeline, and add new steps without changing existing code.
Such **compositional flexibility** is absent in our streaming code with `IO`.

### Essence of the Problem

The problem is not that `IO` is bad, but that it **does not provide convenient enough primitives for streaming**.
We are forced to write **state manipulators** and **loops** that are hard to read, test, and modify.

Additionally, `IO` is a **low-level abstraction** that requires us to implement all processing details.
We would like to be able to express streaming computations as simply
as we do with lists or other composable structures.

### How Can We Fix This?

Functional programming gives us tools for creating **custom abstractions**
that can replace `IO` [in the case of stream processing](../stream-processing.md).
For example, we can create an abstraction similar to a list, but that works with streams:

- We can define `Stream[A]` as an infinite or finite list of elements that can be processed in parts.
- We can create operations over streams, such as `map`, `filter`, `fold`, `windowed`, etc.
- These operations will be composable, like ordinary list functions.

#### Example: Computing a Moving Average

If we had a `Stream[Double]`, we could easily compute a moving average:

```dotty
def windowed[A](n: Int, s: Stream[A])(f: A => B)(using m: Monoid[B]): Stream[B] = ???

val stream = Stream.fromFile("fahrenheit.txt").map(fahrenheitToCelsius)

val avgStream = windowed(5, stream)(_ => ...)

avgStream.toFile("celsius.txt")
```

This code is much easier to understand and modify.
Moreover, it uses the same composition principles as working with lists.

### Summary

The `IO` type is an excellent abstraction for working with effects,
but it is **not an ideal solution for data streaming**.
It requires monolithic loops and manual state management, making code complex and less flexible.

To improve the situation, we can:

- Create [custom composable stream abstractions](../stream-processing.md).
- Use list analogies to make work with streams more intuitive.
- Develop universal combinators such as `windowed`, `map`, `filter`, etc.

Functional programming gives us the freedom to choose: we can use `IO` when needed,
but we can also build higher-level abstractions that better suit our tasks.

---

**Links:**

- [Repository with exercises][sample]
- [Functional Programming in Scala, Second Edition, Chapter 13](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
