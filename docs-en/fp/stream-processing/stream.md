# Streams

## Recall What Pull Is

`Pull` is an algebraic data type (ADT) that represents a streaming computation. Let's examine its structure:

```dotty
// Pull is an algebraic data type representing a streaming computation
enum Pull[+O, +R]:
  // Result - completion of the computation with a result of type R
  case Result[+R](result: R) extends Pull[Nothing, R]
  
  // Output - emission of an output value of type O
  case Output[+O](value: O)  extends Pull[O, Unit]
  
  // FlatMap - composition of computations (monadic binding)
  case FlatMap[X, +O, +R](
      source: Pull[O, X],
      f: X => Pull[O, R]
  ) extends Pull[O, R]
```

### Analogy for Understanding

Imagine `Pull[O, R]` as a conveyor belt:

- `O` (Output) is the type of elements that "come out" to the outside during computation, like items on a conveyor belt
- `R` (Result) is the type of the final computation result, like a final report on the conveyor's work

## The Monadic Nature of Pull

A [Monad](../../typeclass/monad/monad.md) is a pattern in functional programming 
that allows sequential composition of computations. 
The main monad operations are:

- `unit` (or `pure`) - wraps a value into a monadic context
- `flatMap` - sequentially binds computations

### Standard Monad for Pull

For `Pull[O, A]` we can create a standard monad:

```dotty
// Standard monad instance for Pull[O, A]
given [O]: Monad[[x] =>> Pull[O, x]] with
  def unit[A](a: => A): Pull[O, A] = Result(a)  // Wraps a value into Pull
  
  extension [A](pa: Pull[O, A])
    def flatMap[B](f: A => Pull[O, B]): Pull[O, B] =
      pa.flatMap(f)  // Uses Pull's built-in flatMap
```

**How it works in practice:**

```dotty
val computation: Pull[Int, String] =
  Result(5).flatMap: n =>          // Start with the number 5
    Output(n).flatMap: _ =>        // Emit the number 5
      Result(s"Got number: $n")    // Complete with a result
```

This is similar to working with `Option` or `List`, where we sequentially bind computations.

### Idea of an Alternative Monad

What if we care only about output values, not the final result?

Consider `Pull[A, Unit]`:

- `A` - the type of output values (what we actually care about)
- `Unit` - the final result (nothing interesting, just a completion signal)

We can create an alternative monad instance where:

- `unit` is implemented via `Output(a)`
- `flatMap` works differently

### Implementation of flatMapOutput

```dotty
extension [O](self: Pull[O, Unit])
  def flatMapOutput[O2](f: O => Pull[O2, Unit]): Pull[O2, Unit] =
    self.uncons.flatMap:
      case Left(()) => Result(())        // End of stream - do nothing
      case Right((hd, tl)) =>            // There's an element + remainder
        f(hd) >> tl.flatMapOutput(f)     // Apply f to the element and continue
```

**Analogy with List:**

```dotty
// Similar to standard flatMap for List:
def flatMapOutput[B](f: A => List[B]): List[B] = 
  this.flatMap(f)
```

**Usage example:**

```dotty
val ints: Pull[Int, Unit] = Pull.fromList(List(0, 1, 2))
val doubled: Pull[Int, Unit] = ints.flatMapOutput(i => Pull.fromList(List(i, i)))
doubled.toList

// Result: List(0, 0, 1, 1, 2, 2)
// Each number is duplicated     
```

### Alternative Monad

Based on `flatMapOutput` we can create an alternative [monad](../../typeclass/monad/monad.md) instance:

```dotty
val outputMonad: Monad[[x] =>> Pull[x, Unit]] = new:
  def unit[A](a: => A): Pull[A, Unit] = Output(a)
  extension [A](pa: Pull[A, Unit])
    def flatMap[B](f: A => Pull[B, Unit]): Pull[B, Unit] =
      pa.flatMapOutput(f)
```

### When to Use Which Monad?

1. **Standard monad** (`Pull[O, A]`) - when both the process (output values) and the result matter
   - Example: reading a file, where output values are lines and the result is statistics

2. **Alternative monad** (`Pull[A, Unit]`) - when only output values matter
   - Example: transforming a data stream, where we only transform elements

**Code Examples**

```dotty
// Result is Pull
Pull.pullMonad.unit(Pull.Output(1))
// val res1: Pull[Nothing, Pull[Int, Unit]] = Result(Output(1))

// Element transformation
Pull.outputMonad.unit(Pull.Output(1))
// val res2: Pull[Pull[Int, Unit], Unit] = Output(Output(1))
```

**Conclusion**

Pull-monads provide a powerful tool for working with streaming computations:

1. **Standard monad** focuses on sequential composition of computations while preserving the final result
2. **Alternative monad** focuses on transforming output values, ignoring the final result

The choice between them depends on what matters in a given case: 
only the data processing process or also the final result of that processing.

## The Idea of Creating Stream as a Specialized Type

Instead of working directly with `Pull[O, Unit]`, we create a specialized type `Stream` that:

- Is an opaque alias for `Pull[O, Unit]`
- Fixes the result type as `Unit` (focusing only on output values)
- Provides a more convenient API for working with data streams

```dotty
// Define Stream as an opaque type
opaque type Stream[+O] = Pull[O, Unit]

object Stream:
  // Create Stream from a sequence of values
  def apply[O](os: O*): Stream[O] =
    Pull.fromList(os.toList).toStream

  // Extension methods for Stream
  extension [O](self: Stream[O])
    // Conversion back to Pull
    def toPull: Pull[O, Unit] = self

    // Folding stream elements
    def fold[A](init: A)(f: (A, O) => A): A =
      self.fold(init)(f)(1)

    // Convert to List
    def toList: List[O] =
      self.toList

    // Take the first n elements
    def take(n: Int): Stream[O] =
      self.take(n).map(_ => ())

    // Concatenate streams
    def ++(that: => Stream[O]): Stream[O] =
      self >> that

  // Monad instance for Stream
  given Monad[Stream] with
    def unit[A](a: => A): Stream[A] = Pull.Output(a)
    extension [A](sa: Stream[A])
      def flatMap[B](f: A => Stream[B]): Stream[B] =
        sa.flatMapOutput(f)

object Pull:
  // Convert Pull to Stream
  extension [O](self: Pull[O, Unit])
    def toStream: Stream[O] = self
```

### Opaque Type

```dotty
opaque type Stream[+O] = Pull[O, Unit]
```

**What this means:**

- `Stream[O]` looks like a separate type externally
- Internally it is implemented as `Pull[O, Unit]`
- This ensures type safety and a clean API

**Advantages:**

- Hides internal implementation
- Prevents accidental misuse of the API
- Allows providing specialized methods

### Creating Stream

```dotty
def apply[O](os: O*): Stream[O] =
  Pull.fromList(os.toList).toStream
```

**How it works:**

- Accepts a variable number of elements of type `O`
- Converts to List
- Creates `Pull` from the List
- Converts to `Stream`

**Example:**

```dotty
val numbers: Stream[Int] = Stream(1, 2, 3, 4, 5)
```

### Stream Extension Methods

#### `toPull` - Reverse Conversion

```dotty
def toPull: Pull[O, Unit] = self
```

**Why it's needed:**

- For low-level operations with `Pull`
- When you need access to the full power of the `Pull` API

#### `fold` - Element Aggregation

```dotty
def fold[A](init: A)(f: (A, O) => A): A
```

**Analogy with Collections:**

```dotty
// Similar to List.foldLeft
List(1, 2, 3).fold(0)(_ + _)  // = 6
```

#### `toList` - Stream Materialization

```dotty
def toList: List[O]
```

**Features:**

- Fully consumes the stream
- Returns all elements as a regular List

#### `take` - Stream Limiting

```dotty
def take(n: Int): Stream[O] =
  self.take(n).map(_ => ())
```

**What `.map(_ => ())` does:**

- Converts `Pull[O, Option[Unit]]` to `Pull[O, Unit]`
- Discards the service information about the result

#### `++` - Concatenation

```dotty
def ++(that: => Stream[O]): Stream[O] =
  self >> that
```

**How it works:**

- `>>` (then) operator for `Pull`
- Executes the first stream, then the second
- Lazy evaluation of `that` (by-name)

### Monad for Stream

```dotty
given Monad[Stream] with
  def unit[A](a: => A): Stream[A] = Pull.Output(a)
  extension [A](sa: Stream[A])
    def flatMap[B](f: A => Stream[B]): Stream[B] =
      sa.flatMapOutput(f)
```

**Features of this monad:**

- `unit` creates a stream with one element
- `flatMap` works like `flatMapOutput` for `Pull`
- Allows using for-comprehensions

**Example:**

```dotty
val result: Stream[Int] =
  for
    a <- Stream(1, 2)
    b <- Stream(10, 20)
  yield a + b
// Result: 11, 21, 12, 22
```

### Conversion Between Pull and Stream

```dotty
// In Pull object
extension [O](self: Pull[O, Unit])
  def toStream: Stream[O] = self

// In Stream object
extension [O](self: Stream[O])
  def toPull: Pull[O, Unit] = self
```

**Bidirectional conversion:**

- `Stream → Pull`: for low-level operations
- `Pull → Stream`: for high-level operations

## Why Are Both Types Needed: Pull and Stream?

### Pull - For Building Computations

**Strengths of `Pull`:**

- More powerful and flexible
- Allows defining custom streaming computations
- Suitable for low-level operations
- Preserves information about the computation result

**Usage:**

```dotty
// Creating custom stream operations
def customProcessor[O]: Pull[O, Statistics] = ...
```

### Stream - For Working With Data

**Strengths of `Stream`:**

- Simplified API, similar to collections
- Focus on output values
- Convenient for data transformation
- Supports monadic operations

**Usage:**
```dotty
// Working like with a collection
val processed: Stream[Int] = numbers
  .map(_ * 2)
  .filter(_ > 5)
  .take(10)
```

### High-level vs Low-level API

1. **Stream API** (high-level):
   - For common data operations
   - Similar to working with `List`/`LazyList`
   - Safe and convenient

2. **Pull API** (low-level):
   - For creating new operations
   - When you need control over execution
   - For custom processing logic

### Architecture Example

```dotty
// Low-level operation (uses Pull)
def customTake[O](n: Int): Pull[O, Unit] => Pull[O, Unit] = {
  // Complex logic on Pull
}

// High-level usage (uses Stream)
val result: Stream[Int] = numbers
  .customTake(5)  // Extension method using Pull internally
  .map(transform)
```

### Conclusion

The separation into `Pull` and `Stream` provides:

1. **Flexibility** of `Pull` for creating complex computations
2. **Convenience** of `Stream` for working with data
3. **Type Safety** through opaque types
4. **Interoperability** through conversion between types

This separation allows using the right tool for each task, 
combining the power of a low-level API with the convenience of a high-level one.

## Composing Stream Transformations

The `Pipe` type is a fundamental abstraction for creating modular and composable stream transformations.

```dotty
type Pipe[-I, +O] = Stream[I] => Stream[O]
```

### What is Pipe?

**Pipe[I, O]** is a function that:

- Takes a stream of elements of type `I` as input
- Returns a stream of elements of type `O`
- Transforms one stream into another

**Simple analogy:** Imagine a plumbing pipe (pipe) that:

- Input: water of one quality
- Output: water of another quality (cleaned, enriched, etc.)

### Why Is This Useful?

1. **Modularity** - each transformation is independent
2. **Reusability** - one `Pipe` can be applied to different streams
3. **Composability** - `Pipe` can be combined like Lego
4. **Testability** - each `Pipe` can be tested separately
5. Encourages users of the streaming library to think in terms of independent stream transformations

### Basic Transformations

```dotty
// Remove empty lines
val nonEmpty: Pipe[String, String] =
  (input: Stream[String]) => input.filter(_.nonEmpty)

// Convert to lowercase
val lowerCase: Pipe[String, String] =
  (input: Stream[String]) => input.map(_.toLowerCase)
```

**How it works:**

- `nonEmpty` takes `Stream[String]`, returns `Stream[String]` without empty lines
- `lowerCase` takes `Stream[String]`, returns `Stream[String]` in lowercase

### Composing Pipes

```dotty
// Composition via andThen
val normalize: Pipe[String, String] =
  nonEmpty andThen lowerCase

// Equivalent composition via compose
val normalizeAlt: Pipe[String, String] =
  lowerCase compose nonEmpty
```

**What happens during composition:**

1. First `nonEmpty` is applied - empty lines are removed
2. Then `lowerCase` is applied - remaining lines are converted to lowercase

**Visualization:**

```
Input stream: ["Hello", "", "World"] 
  → nonEmpty:      ["Hello", "World"] 
  → lowerCase:     ["hello", "world"]
```

### Ways to Apply a Pipe

```dotty
val lines: Stream[String] = Stream("Hello", "", "World!")

// Method 1: Direct function call
val normalized1: Stream[String] = normalize(lines)

// Method 2: Using the pipe operator
import scala.util.chaining.scalaUtilChainingOps
val normalized2: Stream[String] = lines.pipe(normalize)

// Method 3: Sequential application
val normalized3: Stream[String] = lines.pipe(nonEmpty).pipe(lowerCase)
```

**Differences between methods:**

- All three methods are functionally equivalent
- Choice depends on coding style and readability
- The `pipe` operator often reads more naturally

## Exercises

### Exercise 11.3.1: Implementation of exists. Strategy 1: Only Final Result (Stop on Find)

> [Repository with exercises and instructions for completion][sample]

**Task:**

Implement `exists[I](f: I => Boolean): Pipe[I, Boolean]`,
stopping on finding the first result.

```dotty
exists1[I](f: I => Boolean): Pipe[I, Boolean] = ???
```

**Behavior:** `exists1(_ % 2 == 0)(Stream(1, 3, 5, 6, 7)) → Stream(true)`

**Usage Example:**

```dotty
// Check for even numbers in a stream
val hasEvenNumbers: Pipe[Int, Boolean] = exists1(_ % 2 == 0)

// Apply to different streams
val stream1: Stream[Int] = Stream(1, 3, 5, 7, 9)
val stream2: Stream[Int] = Stream(1, 3, 5, 6, 7)

val result1: Stream[Boolean] = hasEvenNumbers(stream1) // Stream(false)
val result2: Stream[Boolean] = hasEvenNumbers(stream2) // Stream(true)
```

@:note(🔍 Show Solution)

```dotty
def exists1[I](f: I => Boolean): Pipe[I, Boolean] =
  (input: Stream[I]) =>
    input
      .mapOutput(f)        // Convert each element to Boolean
      .dropWhile(i => !i)  // Skip all false until the first true
      .takeWhile(identity) // Take all true (but usually only the first)
      .take(1)             // Guarantee taking only one element
      .uncons.flatMap:
        // If we reached an empty result, no element satisfying the predicate is in the original stream
        case Left(_)  => Pull.Output(false)
        // Otherwise, such an element exists
        case Right(_) => Pull.Output(true)       
```

@:@

### Exercise 11.3.2: Implementation of exists. Strategy 2: All Intermediate Results (Stop on Find)

**Task:**

Implement `exists[I](f: I => Boolean): Pipe[I, Boolean]` - stop and emit all intermediate results.

```dotty
exists[I](f: I => Boolean): Pipe[I, Boolean] = ???
```

**Behavior:** `exists2(_ % 2 == 0)(Stream(1, 3, 5, 6, 7)) → Stream(false, false, false, true)`

**Usage Example:**

```dotty
// Search for the first even number with intermediate results
val trackEvenSearch: Pipe[Int, Boolean] = exists2(_ % 2 == 0)

// Apply to a stream of numbers
val numbers: Stream[Int] = Stream(1, 3, 5, 6, 7, 8, 9)
val searchProcess: Stream[Boolean] = trackEvenSearch(numbers)

// Result: false (1), false (3), false (5), true (6) → stop
```

**Advantages of this Strategy:**

- ✅ Shows the entire search process
- ✅ Stops at the first success
- ✅ Memory efficient (lazy processing)
- ✅ Suitable for debugging and monitoring

**Application in Real Scenarios:**

- Tracking the search process in large datasets
- Real-time monitoring of condition execution
- Debug information for complex pipelines
- Finding the first matching element with a history of checks

@:note(🔍 Show Solution)

```dotty
def exists2[I](f: I => Boolean): Pipe[I, Boolean] =
  (input: Stream[I]) =>
    // Helper recursive function
    def go(p: Stream[Boolean]): Stream[Boolean] =
      p.uncons.flatMap:
        // End of stream - complete
        case Left(r)              => Pull.done
        // Found true - emit and complete
        case Right((hd, _)) if hd => Pull.Output(true) >> Pull.done
        // Found false - emit and continue
        case Right((hd, tl))      => Pull.Output(hd) >> go(tl)

    // Transform input stream and process
    go(input.mapOutput(f))
```

**Step-by-step explanation of the solution:**

1. **`input.mapOutput(f)`** - converts each element to `Boolean`
2. **`go`** - recursively processes the stream of `Boolean` values:
   - **`case Left(())`** - stream ended, complete
   - **`case Right((true, _))`** - found `true`, emit and stop
   - **`case Right((false, tail))`** - found `false`, emit and continue
@:@

### Exercise 11.3.3: Implementation of exists. Strategy 3: All Results (No Stop)

**Task:**

Implement `exists[I](f: I => Boolean): Pipe[I, Boolean]` - no stop and emit all intermediate results.

```dotty
exists[I](f: I => Boolean): Pipe[I, Boolean] = ???
```

**Behavior:** `exists3(_ % 2 == 0)(Stream(1, 3, 5, 6, 7)) → Stream(false, false, false, true, false)`

**Usage Example:**

```dotty
// Search for even numbers with full check history
val trackAllEvens: Pipe[Int, Boolean] = exists3(_ % 2 == 0)

// Apply to a stream of numbers
val numbers: Stream[Int] = Stream(1, 3, 5, 6, 7, 8, 9)
val allResults: Stream[Boolean] = trackAllEvens(numbers)

// Result: false (1), false (3), false (5), true (6), false (7), true (8), false (9)
```

**Advantages of this Strategy:**

- ✅ **Full history** - all intermediate results are preserved
- ✅ **Continuous processing** - does not interrupt on finding an element
- ✅ **Determinism** - processes all elements of the stream
- ✅ **Suitable for aggregation** - can build full statistics

**Practical Applications:**

- Real-time data quality monitoring
- Building a complete history of condition checks
- Aggregating statistics across the entire stream
- Feature monitoring in ML pipelines
- Audit and logging of checking processes

@:note(🔍 Show Solution)

```dotty
def exists3[I](f: I => Boolean): Pipe[I, Boolean] =
  (input: Stream[I]) => input.mapOutput(f)
```

@:@

### Choosing an exist Strategy

**Selection Criteria:**

1. **Efficiency** - is it necessary to process the entire stream?
2. **Laziness** - is it possible to stop early?
3. **Informative** - are intermediate results needed?

**Differences Between Strategies:**

| Strategy | Stop            | Results             | Usage                               |
|-----------|------------------|---------------------|-------------------------------------|
| **1**     | At first `true`  | Only final          | When only the fact of existence matters |
| **2**     | At first `true`  | All up to first `true` | For debugging the search process    |
| **3**     | Does not stop    | All results         | For full statistics and monitoring  |

**For the line counting task:**

We can now express the main transformation for the line counting task as `count andThen exists(_ > 40000)`,
and can easily attach filters and other transformations to the pipeline.

```dotty
val count: Pipe[String, Int] = // counting transformation
val thresholdCheck: Pipe[Int, Boolean] = exists(_ > 40000)

// Pipeline composition
val pipeline: Pipe[String, Boolean] = count andThen thresholdCheck
```

## Practical Application in Real Tasks

### Example: Log File Analysis

```dotty
// Different processing stages
val readLines: Pipe[String, String] = identity // reading a file
val filterErrors: Pipe[String, String] = _.filter(_.contains("ERROR"))
val extractTimestamps: Pipe[String, String] = _.map(_.take(19)) // extracting time
val countByHour: Pipe[String, (String, Int)] = // grouping by hour

// Pipeline composition
val errorAnalysis: Pipe[String, (String, Int)] =
  readLines andThen
  filterErrors andThen
  extractTimestamps andThen
  countByHour
```

### Advantages of This Approach

1. **Flexibility** - easy to add/remove processing stages
2. **Testability** - each `Pipe` is tested separately
3. **Readability** - code reflects business logic
4. **Reusability** - `Pipe` can be used in different contexts

## File Processing

Previously, we solved the problem of checking whether a file contains more than 40,000 elements. 
The solution is based on working with pure, lazy data streams (`Stream`). 
Instead of generating a new stream as a result, we can aggregate all its elements into a single final value, 
which is ideal for tasks like counting or checking a condition.

### The `void` Method

```dotty
enum Pull[+O, +R]:
  ...

  def void: Pull[O, Unit] =
    map(_ => ())
```

- **Purpose:** Ignores the output data (elements) of the stream, preserving only its structure.
- **Transformation:** Converts `Pull[O, R]` to `Pull[Nothing, Unit]`.
- **Analogy:** Similar to the Unix `>` operator, which redirects output to nowhere.

### The `Stream.fromIterator` Constructor

```dotty
object Stream:
  def fromIterator[O](itr: Iterator[O]): Stream[O] =
    Pull.unfold(itr)(itr =>
      if itr.hasNext then Right((itr.next(), itr))
      else Left(itr)
    ).void.toStream
```

- **Purpose:** Creates a lazy `Stream[O]` from an `Iterator[O]`.
- **How it works:**
    1.  **`Pull.unfold`:** Recursively generates elements. Takes an initial state (iterator `itr`) and a function.
    2.  **Generator Function:**
        - **`if itr.hasNext`**: If the iterator has a next element, returns `Right((element, new_state))`. 
          The element (`itr.next()`) is sent to the stream, and the new state is the remaining iterator.
        - **`else`**: If there are no elements, returns `Left(state)`, signaling the end of the stream.
    3.  **`.void`:** Ignores the final state (remaining iterator) returned by `unfold`.
    4.  **`.toStream`:** Converts the low-level `Pull` structure into a ready-to-use `Stream`.

### Main Function `processFile`

```dotty
def processFile[A](
    file: java.io.File,
    p: Pipe[String, A] // Channel for transforming strings into values of type A
)(using m: Monoid[A]): IO[A] = IO.IO: // Implicit parameter: way to combine values of type A
  val source = scala.io.Source.fromFile(file)
  try
    () => // This lambda expression is the body of IO, to be executed later
      fromIterator(source.getLines) // 1. Create a Stream from file lines
        .pipe(p)                    // 2. Apply the provided transformation channel
        .fold(m.empty)(m.combine)   // 3. Fold the entire stream into one value
  finally source.close()            // Guaranteed to close the file, even on error
```

1.  **Opening a File and Resource Management:**
    - `scala.io.Source.fromFile(file)` creates an object for reading the file.
    - The `try/finally` block is critically important for safety. 
      It guarantees that the file resource will be closed (`source.close()`) in any case — 
      both on successful completion and on an exception.

2.  **Creating a Data Stream:**
    - `source.getLines()` returns an `Iterator[String]`, where each element is a line from the file.
    - `fromIterator(source.getLines)` converts this iterator into a lazy `Stream[String]`. 
      This means lines will be loaded as needed, not all at once into memory.

3.  **Applying Transformations (Channel - `Pipe`):**
    - `.pipe(p)` — this is the key extension point. 
      The stream of lines is passed to the user-provided channel `p: Pipe[String, A]`.
    - **`Pipe[String, A]`** — this is a function type that transforms `Stream[String] => Stream[A]`. 
      Any operations can be performed inside this channel: filtering, parsing, mapping, etc.

4.  **Result Aggregation (Folding):**
    - `.fold(m.empty)(m.combine)` — a terminal operation that traverses the entire `Stream[A]` stream 
      and combines all its elements into a single value of type `A`.
    - **`m.empty`** — the initial value (neutral element of a [monoid](../../typeclass/monoid/monoid.md)). 
      For example, `0` for `Int` or `false` for `Boolean`.
    - **`m.combine`** — the operation for combining two values. For example, `+` for `Int` or `||` for `Boolean`.
    - This operation is efficient and safe because the stream is processed lazily and sequentially.

5.  **Returning a Deferred Computation (`IO`):**
    - All logic is wrapped in `IO { ... }`. This means the `processFile` function 
      does not execute file reading and data processing immediately upon call.
    - It returns a **value of type `IO[A]`**, which is a description (program) of _how_ to obtain the result. 
      This description will be executed later, preserving referential transparency and enabling side-effect management.

### Example Usage: Checking a File for More Than 40,000 Elements

```dotty
def checkFileForGt40K(file: java.io.File): IO[Boolean] =
  processFile(file, count andThen exists(_ > 40000))(using Monoid.booleanOr)
```

- **Channel `count`:** This is a standard channel of type `Pipe[O, Int]`. 
  It transforms an input stream of any type into a stream containing a single value — 
  the total number of elements in the original stream. For `Stream[String]` this means the line count.
- **Channel `exists(predicate)`:** This is a channel of type `Pipe[Int, Boolean]`. 
  It checks whether at least one element of the stream satisfies a given predicate. 
  In this case, the predicate `_ > 40000` checks whether the number is greater than 40,000.
- **Channel Composition: `count andThen exists`**
    - The `andThen` operator first applies the left channel (`count`), then the right one (`exists`) to the result.
    - The resulting channel has type `Pipe[String, Boolean]`: takes a stream of lines, counts them, and checks whether the count exceeds 40,000.
- **Monoid `Monoid.booleanOr`:**
    - `empty = false` (neutral element for the OR operation).
    - `combine = (a, b) => a || b` (logical OR operation).
    - Since after the `exists` channel the stream consists of only one element (`true` or `false`), 
      folding with this monoid simply returns that value.

### Exercise 11.3.4: Temperature Converter

**Task:**

Write a program that reads Fahrenheit degrees as `Double` values from a file, one value per line,
sends each value through a channel to convert it to Celsius, and writes the result to another file.
Your program should ignore empty lines in the input file, lines starting with a `#` character (comments),
as well as lines that could not be converted to `Double`.
You can use the `toCelsius` function:

```dotty
def toCelsius(fahrenheit: Double): Double =
  (5.0 / 9.0) * (fahrenheit - 32.0)

def convert(inputFile: String, outputFile: String): IO[Unit]
```

**trimmed, nonEmpty, nonComment:**

```dotty
def trimmed: Pipe[String, String] = ???

def nonEmpty: Pipe[String, String] = ???

def nonComment: Pipe[String, String] = ???
```

@:note(🔍 Show Solution)

This solution represents a composition of pure transformations (`Pipe`), 
where each component (`trimmed`, `nonEmpty`, `nonComment`) is responsible for one specific operation on a stream of strings: 
trimming, filtering empty lines, and removing comments, 
which allows flexible and declarative data processing by combining these primitives in the required sequence.

```dotty
def trimmed: Pipe[String, String] =
  src => src.mapOutput(_.trim)

def nonEmpty: Pipe[String, String] =
  src => src.filter(_.nonEmpty)

def nonComment: Pipe[String, String] =
  src => src.filter(_.headOption.forall(_ != '#'))
```

@:@

**asDouble, convertToCelsius, conversion:**

```dotty
def asDouble: Pipe[String, Double] = ???

def convertToCelsius: Pipe[Double, Double] = ???

val conversion: Pipe[String, Double] = ???
```

@:note(🔍 Show Solution)

This solution represents a declarative composition of a data processing pipeline, 
where each step (`trimmed`, `nonEmpty`, `nonComment`, `asDouble`, `convertToCelsius`) is a pure, 
lazy function that sequentially transforms a stream of strings: 
first the input data is cleaned and filtered, then safe parsing into numbers occurs, 
and finally business logic is applied (conversion to Celsius), 
which ultimately forms a reliable and readable ETL pipeline.

```dotty   
def asDouble: Pipe[String, Double] =
  src =>
    src.flatMapOutput: str =>
      str.toDoubleOption match
        case Some(d) => Pull.Output(d)
        case None    => Pull.done        

def convertToCelsius: Pipe[Double, Double] =
  src => src.map(toCelsius)

val conversion: Pipe[String, Double] =
  trimmed andThen
    nonEmpty andThen
    nonComment andThen
    asDouble andThen
    convertToCelsius
```

@:@

**convert:**

```dotty
def convert(inputFile: String, outputFile: String): IO[Unit] = ???
```

@:note(🔍 Show Solution)

This solution implements safe data conversion from an input file to an output file using stream composition: 
it lazily reads lines from the file, passes them through a predefined conversion pipeline `conversion`, 
then writes each resulting element to a new file, 
ensuring proper resource cleanup (both source and writer) through nested `try/finally` blocks, 
and wraps the entire operation in `IO` for deferred execution and side-effect control.

```dotty
import java.nio.file.{Files, Paths}
import scala.util.chaining.scalaUtilChainingOps

def convert(inputFile: String, outputFile: String): IO[Unit] =
  IO:
    val source = scala.io.Source.fromFile(inputFile)
    try
      val writer = Files.newBufferedWriter(Paths.get(outputFile))
      try
        Pull.fromLazyList(LazyList.from(source.getLines))
          .pipe(conversion)
          .fold(()): (_, a) =>
            writer.write(a.toString)
            writer.newLine()
          ._1
      finally writer.close()
    finally source.close()
```

@:@

## Conclusion

The `Pipe` concept provides a powerful tool for building complex stream processors:

1. **Abstraction** - hides the complexity of internal implementation
2. **Composition** - allows building complex transformations from simple ones
3. **Flexibility** - supports different processing strategies
4. **Practicality** - applicable to real data processing tasks

This approach turns stream data processing from imperative code into a declarative description of transformations, 
making the code cleaner, more understandable, and more maintainable.

---

**Links:**

- [Link to the repository with exercises][sample]
- [Functional Programming in Scala, Second Edition, Chapter 15](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
