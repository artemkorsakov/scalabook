# Stream processing and incremental I/O

## Introduction

Consider the task of determining whether a file contains more than forty thousand non-empty lines.

### Problems with the imperative approach

In the usual (imperative) style this task often looks like this:

```dotty
import cats.effect.IO
import scala.io.Source

def linesGt40k(filename: String): IO[Boolean] = IO:
  // Open the file
  val src = Source.fromFile(filename)
  try
    var count = 0
    // Get a "reader" of lines from the file
    val lines: Iterator[String] = src.getLines()
    // While we haven't counted 40000 and there are still lines
    while count <= 40000 && lines.hasNext do
      // Read the next line
      val row = lines.next
      // Increment the counter if the line is not empty
      count += (if row.nonEmpty then 1 else 0)
    count > 40000   // Return the result
  finally src.close // Always close the file!
```

**What is good here:**

- We don't load the entire file into memory at once
- We can stop early once we have counted 40000 lines

**What is dangerous here:**

1. **It's easy to forget to close the file** - if we remove `finally`, the file stays open
2. **The code is hard to reuse** - if we want a different check, we have to copy all of this code
3. **Logic and resource management are mixed** - it's hard to see where file handling ends and line counting begins
4. **Errors can happen anywhere** - if something goes wrong in the middle, the file may not get closed

### The idea of the functional approach

In functional programming, code looks like a chain of transformations:

```dotty
// In an ideal world we would want to write it like this:
linesFromFile("file.txt")
  .filter(_.nonEmpty)         // remove empty lines
  .take(40000)                // take the first 40000 lines
  .exists(_.startsWith("//")) // another check, e.g.: check whether there are comments
  ...
  // additional checks and processing
```

Such code:

- **Is clear** - it reads as an instruction top to bottom
- **Is composable** - individual steps can be changed easily
- **Is safe** - no need to worry about file-handling details

### Why simply implementing `linesFromFile` is a bad idea?

Let's try to make a simple version:

```dotty
def linesFromFile(filename: String): IO[LazyList[String]] = IO:
  val src = Source.fromFile(filename)
  // Create a lazy collection of lines
  LazyList.from[String](src.getLines()).appendedAll:
    src.close() // Try to close the file at the end
    LazyList.empty
```

**Problems with this approach:**

1. **When will the file close?** - only when we have read **all** lines to the end
2. And if we read only a part, the file stays open!
3. **Reuse** - if we try to read the file a second time, it will already be closed
4. **Unexpected surprises** - effects (opening/closing the file) happen at unpredictable moments

We need a solution that works like the "ideal" example above, but at the same time:

1. **Resources are safe** - files close automatically, even if an error occurred
2. **It's combinable** - simple operations are assembled into complex chains
3. **It works in chunks** - doesn't load all data into memory at once
4. **It's predictable** - we know exactly when operations with the outside world happen

### What does it look like in practice?

Imagine we have a special tool for working with data streams:

```dotty
// Create a safe stream from a file
Stream.resource(openFile("data.txt")) { file =>
  // Describe the chain of transformations
  Stream.fromIterator(file.lines)   // Take lines from the file
    .filter(_.nonEmpty)             // Filter out empty ones
    .take(40000)                    // Take the first 40000
    .exists(_.startsWith("foo"))    // Check a condition
}
// Run the whole chain
.compile.toList 
```

**What happens here step by step:**

1. `Stream.resource` - guarantees that the file will be closed in any case
2. `Stream.fromIterator` - creates a data stream from the file's lines
3. `.filter`, `.take`, `.exists` - describe which transformations to apply
4. `.compile.toList` - runs the whole described chain of operations

> **Key idea:** we first **describe** what we want to do, and then **run** that description.

### What does this give us?

1. **Safety** - no need to remember to close files
2. **Readability** - code looks like a clear chain of operations
3. **Reuse** - individual steps can be changed easily
4. **Efficiency** - data is processed gradually, not all at once
5. **Predictability** - we know exactly when interaction with the outside world happens

This approach allows writing code that is simultaneously:

- Elegant and clear (as in functional programming)
- Safe and efficient (as in good imperative code)
- Easily modifiable and composable (_composable_)

Functional streaming libraries (for example, [Functional Streams for Scala (FS2)](https://fs2.io)) allow you to:

- Preserve the composability of pure code
- Manage resources automatically
- Efficiently process large/infinite streams
- Avoid hidden side effects

This brings the benefits of the functional approach even to I/O-intensive tasks.

## Simple streaming transformations

Let's look at the concept of streaming transformations.

A stream processor is a data transformer that:

- Accepts a stream (sequence) of input data
- Processes it as it arrives
- Returns a new stream of output data

Examples of streams:

- Lines from a file
- HTTP requests to a server
- Mouse click coordinates
- Sensor readings

### The `Pull` data type

Imagine you need to process a huge 100 GB file. 
Loading the whole file into memory (into a `List` or `Array`) is impossible. 
Streaming processing solves this problem by allowing data to be processed in chunks, as it arrives.

**`Pull[O, R]`** — is a functional abstraction for describing such streaming operations. 
It's not data, but a _plan of computation_ that is executed lazily.

```dotty
enum Pull[+O, +R]:
  case Result[+R](result: R) extends Pull[Nothing, R]
  case Output[+O](value: O)  extends Pull[O, Unit]
  case FlatMap[X, +O, +R](
      source: Pull[O, X],
      f: X => Pull[O, R]
  ) extends Pull[O, R]

  def flatMap[O2 >: O, R2](
      f: R => Pull[O2, R2]
  ): Pull[O2, R2] =
    Pull.FlatMap(this, f)

  def >>[O2 >: O, R2](next: => Pull[O2, R2]): Pull[O2, R2] =
    flatMap(_ => next)

  def map[R2](f: R => R2): Pull[O, R2] =
    flatMap(r => Result(f(r)))
end Pull
```

The `Pull` type has two fundamental parameters:

- **`O` (Output)** — the type of elements we _emit_ to the outside during processing (for example, lines of a file).
- **`R` (Result)** — the type of the _final result_ we return after all output operations finish 
  (for example, the number of processed bytes, the file close status).

In short: `Pull` emits zero or more values of type `O` and at the end returns one value of type `R`.

**Analogy:** Imagine a conveyor at a factory (`Pull`). 
It can:

1. Release finished goods (`Output[O]`) onto the belt.
2. At the end of its work, report a summary: "Produced 1000 units, defect 2%" (`Result[R]`).

### Result: `Result[+R](result: R)`

This is the terminal point of a computation. It says: "Everything, processing is done. Here is the final result".

```dotty
val done: Pull[Nothing, Int] = Pull.Result(42)
// The process finished with result 42: Result(42)
val unitDone: Pull[Nothing, Unit] = Pull.Result(())
// The process finished without a meaningful result: Result(())
```

### Output: `Output[+O](value: O)`

This is the main action for generating a stream element. 
It says: "I emit one element `value`, and then I'll continue working". 
Since the process should continue after emitting an element, its result type is `Unit`.

```dotty
val hello: Pull[String, Unit] = Pull.Output("Hello") // Emit one string into the stream
// Type: emits a String, and at the end returns Unit (an empty result).
```

### Combination: `FlatMap[X, +O, +R](source: Pull[O, X], f: X => Pull[O, R])`

This is the most important case. 
It allows _sequentially combining_ `Pull` operations.
This is the mechanism by which we build complex pipelines from simple steps.

**How to read `FlatMap`?**

"First run the computation `source`. When it finishes and returns some value `X` (of type `X`), 
pass that value to the function `f` to get the next computation `Pull[O, R]`, and run it."

**A simple example:**

```dotty
// Step 1: A process that emits the number 1.
val step1: Pull[Int, Unit] = Pull.Output(1)

// Step 2: A function that takes the result of the previous step (but here it's Unit)
// and decides what to do next.
val step2: Unit => Pull[Int, Unit] = (_: Unit) => Pull.Output(2)

// Combination: first step1, then step2(result of step1)
val combined: Pull[Int, Unit] = Pull.FlatMap(step1, step2)
// Equivalent to the simpler `flatMap` notation:
val combinedSimple: Pull[Int, Unit] = step1.flatMap(step2)
```

This process will emit `1`, and then emit `2`.

## Combinators

When working with stream transformations, we constantly need to:

1. **Transform** computation results
2. **Combine** several operations into sequences
3. **Build** complex pipelines from simple components

For that, three main combinators are implemented in `Pull`: `flatMap`, `map`, and `>>`.

### The `flatMap` combinator: sequential execution

`flatMap` allows creating a **sequence of operations**,
where the result of the first operation becomes the input for the next.

**Signature:**

```dotty
def flatMap[O2 >: O, R2](f: R => Pull[O2, R2]): Pull[O2, R2]
```

**How to read:**

"Take the result of the current `Pull` (of type `R`), pass it to the function `f`, and run the `Pull` that this function returns."

```dotty
Pull.Result(1).flatMap: x =>
  Pull.Result(x * 2)
```

**Step-by-step execution:**

1. The original `Pull` is `Result(1)`. It immediately finishes with the result `1`
2. The function `f` takes this result: `x = 1`
3. The function returns a new `Pull`: `Result(1 * 2) = Result(2)`
4. The final process finishes with the result `2`

### The `map` combinator: a simple transformation

`map` — is a simplified version of `flatMap` that allows **only transforming** the final result,
without producing new side effects (outputs).

**Signature:**

```dotty
def map[R2](f: R => R2): Pull[O, R2]
```

**How to read:**

"When the current `Pull` finishes with the result `R`, transform it into `R2` using the function `f`."

```dotty
Pull.Result(1).map(_ * 2)
```

**Step-by-step execution:**

1. The original `Pull` is `Result(1)`
2. `map(_ * 2)` transforms this result: `1 * 2 = 2`
3. The new `Pull` will finish with the result `2`

**Difference from `flatMap`:**

```dotty
// map: only transforms the result
Pull.Result(3).map(_ + 1) // Result(4)

// flatMap: can create new operations
Pull.Result(3).flatMap(x => Pull.Result(x + 1)) // Result(4)
// The result is the same, but the mechanism is different
```

### The `>>` combinator (then): sequential execution of actions

The operator `>>` (pronounced "then") runs two processes **sequentially**, completely ignoring the result of the first.

**Signature:**

```dotty
def >>[O2 >: O, R2](next: => Pull[O2, R2]): Pull[O2, R2]
```

**How to read:**

"Run the current process, and then run the process `next`."

**Key features:**

1. **Ignores the result** of the first process
2. **Preserves all output data** of both processes
3. **Lazy parameter** — `next` is computed only when needed

**Perfect for chains of actions where only the output (`O`) matters, not intermediate results.**

```dotty
val writeHeader: Pull[String, Unit] = Pull.Output("## Header")
val writeLine1: Pull[String, Unit] = Pull.Output("First line")
val writeLine2: Pull[String, Unit] = Pull.Output("Second line")

// Building a pipeline: emit the header, then the first line, then the second line.
val pipeline: Pull[String, Unit] = writeHeader >> writeLine1 >> writeLine2
```

**Equivalent via `flatMap`:**

```dotty
// writeHeader >> writeLine1 is equivalent to:
writeHeader.flatMap(_ => writeLine1)

// The full pipeline:
writeHeader.flatMap(_ => writeLine1.flatMap(_ => writeLine2))
```

**Why is `>>` useful?**

- **Simplifies code** — no need to invent names for unused results
- **Improves readability** — clearly shows the sequence of actions
- **Works with side-effects** — ideal for chains of I/O operations

## A plan and its execution

`Pull[O, R]` — is not data, but a **plan of computations**. 
By itself it does nothing — it only describes which operations need to be performed. 
The methods `step`, `fold`, and `toList` are different ways to **execute** this plan.

### The `.step` method: step-by-step plan execution

The `.step` method is the basic execution mechanism that handles the plan **one step at a time**. 
It's a low-level operation on which all the others are built.

**Signature:** `def step: Either[R, (O, Pull[O, R])]`

- **`Left(r: R)`** -> The plan has been executed _completely_. The final result is `r`.
- **`Right((o: O, next: Pull[O, R]))`** -> The plan is not yet finished:
    - At this step a value `o` was emitted.
    - And `next` — the continuation of the plan for the following steps.

**A detailed walkthrough of the step algorithm**

```dotty
def step: Either[R, (O, Pull[O, R])] = this match
  // Base cases:
  case Result(r) => Left(r)  // Finish with the result
  case Output(o) => Right(o, Pull.Result(())) // Emit a value and finish
  
  // Recursive case: unfold FlatMap
  case FlatMap(source, f) =>
    source match
      // Optimization: eliminate nested FlatMap (associativity law)
      case FlatMap(s2, g) =>
        s2.flatMap(x => g(x).flatMap(y => f(y))).step
      
      // Handle the source of the FlatMap
      case other => other.step match
          case Left(r)         => f(r).step // Source finished, apply f
          case Right((hd, tl)) => Right((hd, tl.flatMap(f))) // Continue
```

**An example of step-by-step analysis:**

```dotty
val ourPull: Pull[Int, Unit] = Pull.Output(1) >> Pull.Output(2)

// Step 1
val step1: Either[Unit, (Int, Pull[Int, Unit])] = ourPull.step
// step1 = Right((1, Pull.Output(2)))
// Value: 1. Next step: the process Pull.Output(2).

// Step 2 (execute `next` from step 1)
val nextPull: Pull[Int, Unit]                   = Pull.Output(2)
val step2: Either[Unit, (Int, Pull[Int, Unit])] = nextPull.step
// step2 = Right((2, Result(())))
// Value: 2. Next step: the process Result(()).

// Step 3 (execute `next` from step 2)
val finalPull: Pull[Int, Unit]                  = Pull.Result(())
val step3: Either[Unit, (Int, Pull[Int, Unit])] = finalPull.step
// step3 = Left(()) // The process is finished.
```

**Why do we need such a complex algorithm for FlatMap?**

The algorithm ensures **correct execution** of operation chains:

1. **Laziness** — computations happen only when needed
2. **Stack safety** — we avoid deep recursion
3. **Preservation of semantics** — we guarantee the correct order of execution

### The `.fold` method: a universal stream folding

The `fold` method uses `step` to process all elements sequentially:

```dotty
@annotation.tailrec
final def fold[A](init: A)(f: (A, O) => A): (R, A) =
  step match
    case Left(r)         => (r, init)               // The process is finished
    case Right((hd, tl)) => tl.fold(f(init, hd))(f) // Continue
```

**Key features:**

- **Tail recursion** — works without stack overflow
- **Incrementality** — processes elements one by one
- **Universality** — suitable for any folding operations

#### A visualization of how fold works

```dotty
val p = Pull.Output(1) >> Pull.Output(2)
p.fold(0)(_ + _)
```

**Iteration 1:**

- `p.step` → `Right((1, Pull.Output(2)))`
- `init = 0`, `hd = 1`
- `f(0, 1) = 1` (new accumulator)
- Recursion: `Pull.Output(2).fold(1)(_ + _)`

**Iteration 2:**

- `Pull.Output(2).step` → `Right((2, Result(())))`
- `init = 1`, `hd = 2`
- `f(1, 2) = 3` (new accumulator)
- Recursion: `Result(()).fold(3)(_ + _)`

**Iteration 3:**

- `Result(()).step` → `Left(())` (termination)
- Result: `((), 3)`

### The `.toList` method: a specialized transformation

The `toList` method is a specialized version of `fold` for building a list:

```dotty
def toList: List[O] =
  fold(List.newBuilder[O])((bldr, o) => bldr += o)(1).result
```

**An equivalent implementation (for understanding):**

```dotty
def toListSimple: List[O] =
  @annotation.tailrec
  def loop(current: Pull[O, Any], acc: List[O]): List[O] =
    current.step match
      case Left(_) => acc.reverse // Reverse, since we prepended
      case Right((hd, tl)) => loop(tl, hd :: acc)

  loop(this, Nil)
```

#### Usage examples

**A simple transformation:**

```dotty
val p = Pull.Output(1) >> Pull.Output(2) >> Pull.Output(3)
p.toList // List(1, 2, 3)
```

**With an empty result:**

```dotty
val empty = Pull.Result("done")
empty.toList // List()
```

**With complex types:**

```dotty
case class Person(name: String, age: Int)
val people = Pull.Output(Person("Alice", 30)) >> Pull.Output(Person("Bob", 25))
people.toList // List(Person("Alice", 30), Person("Bob", 25))
```

#### Important limitations:

1. **Memory consumption** — all elements are loaded into memory
2. **Infinite streams** — calling `toList` on an infinite stream never terminates
3. **Only output data** — the final result `R` is ignored

### Exercise 11.1.1: Fibonacci numbers

> [Repository with exercises and instructions][sample]

**Task:**

Implement a method that emits a given number of Fibonacci numbers into the output stream, and at the end returns a string with the n-th number.

```dotty
def fib(n: Int): Pull[Int, String] = ???
```

**Expected behavior:**

```dotty
fib(5).toList         // Should return: List(0, 1, 1, 2, 3)
fib(5).fold(0)(_ + _) // Should return: ("The 5th Fibonacci number is 3", 7)
```

@:note(🔍 Show solution)

**Explanation of the solution:**

1. We use a recursive approach with accumulators
2. At each step we emit the current Fibonacci number
3. When we reach the n-th number, we terminate with a string result
4. We handle edge cases (n ≤ 0, n = 1)

This solution demonstrates the full power of the `Pull` combinators — we create a lazy data stream
that is computed only as needed!

```dotty
def fib(n: Int): Pull[Int, String] =
  def fibHelper(remaining: Int, current: Int, next: Int): Pull[Int, String] =
    if remaining == 1 then
      Pull.Output(current) >>
        Pull.Result(s"$n-th Fibonacci number is $current")
    else
      Pull.Output(current) >>
        fibHelper(remaining - 1, next, current + next)

  if n <= 0 then Pull.Result("n must be a positive number")
  else if n == 1 then
    Pull.Output(0) >> Pull.Result("The 1st Fibonacci number is 0")
  else Pull.Output(0) >> fibHelper(n - 1, 1, 1)
```

@:@

## Practical example: reading a file

Let's implement the function `fileLines`.

```dotty
// Suppose there is a function that reads one line from a file.
// It returns `None` when the end of the file (EOF) is reached.
def readLine(file: File): Option[String] = ???

def fileLines(file: File): Pull[String, Unit] =
  // Try to read a line
  readLine(file) match
    case None =>
      // The file has ended, finish the process with an empty result.
      Pull.Result(())
    case Some(line) =>
      // We read a line! Our plan:
      // 1. Emit this line into the stream (Output).
      // 2. And THEN (>>) run this same process (fileLines) recursively
      //    to read the remaining lines.
      Pull.Output(line) >> fileLines(file)

// Usage
val fileProcessor: Pull[String, Unit] = fileLines(new File("data.txt"))
// So far we have only described the process. No line has been read yet!
```

**Key takeaway:** `fileLines` doesn't read the file immediately. 
It builds a _chain of actions_ (`Output >> Output >> ... >> Result`) that can be executed lazily, and not even necessarily in full.

## Advantages of `Pull`

1. **Composability**: Small, simple processes (`Output`, `Result`) 
   are easily combined with `flatMap` and `>>` into complex pipelines. This is a clean, functional design.
2. **Laziness & Incremental Processing**: `Pull` describes _what to do_, rather than doing it immediately. 
   Execution is controlled through `.step` or `.fold`, which allows processing data in chunks, without consuming all the memory.
3. **Resource Safety**: Since we control the execution, 
   we can guarantee that files are closed and connections are broken at the right moment 
   (for example, by inserting logic into `Result` or using `try`/`finally` in `.fold`).
4. **Testability**: It's easy to test individual steps, 
   because these are just data values (`case class`), not side effects.

`Pull` — is a powerful low-level building block for creating stream processing libraries (such as FS2 or ZIO Streams). 
You work not with data streams directly, but with plans for processing them.

### Why is this useful?

1. **Laziness** - data is processed as needed
2. **Composability** - simple operations can be combined
3. **Safety** - control over execution

`Pull` gives us:

- A simple way to describe streaming transformations
- The ability to combine operations
- Control over execution without unnecessary effects

This is the foundation for building more complex streaming APIs in a functional style.

---

**Links:**

- [Link to the repository with exercises][sample]
- [Functional Programming in Scala, Second Edition, Chapter 15](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp