# Creating and Combining Pull Operations

`Pull[O, R]` is not just a container, but a **description of a data processing process**.
We build plans for transforming information that are executed only when explicitly required.
This is similar to building a database query: we describe _what_ needs to be done, rather than executing operations immediately.

## Basic Building Blocks

### Empty Pull (`done`)

The simplest `Pull` is a process that emits nothing and completes with a `Unit` result:

```dotty
val done: Pull[Nothing, Unit] = Pull.Result(())
```

**Conceptual Analogies:**

- `Nil` in Lists — an empty collection
- `None` in `Option` — the absence of a value
- [Neutral element in Monoids](../../typeclass/monoid/monoid.md) — a base reference point

**Characteristics:**

- **Type `Nothing`** — the process produces no output values
- **Result `Unit`** — completes without a meaningful result
- **Neutral element** for the sequence composition operation

**Practical application:**

```dotty
// Base case for recursive processes
def generateWhilePositive(n: Int): Pull[Int, Unit] =
  if n <= 0 then Pull.done
  else Pull.Output(n)
```

### Creating Values (`Output`)

Creating processes that emit specific values:

```dotty
val single: Pull[Int, Unit] = Pull.Output(1)
```

**Semantics:** "Produce one value and complete with result `Unit`"

**Conceptual Analogies:**

- `List(1)` — a List with one element
- `Iterator.single(1)` — an Iterator with one element
- `Some(1)` — the presence of a concrete value

**Features:**

- **Deferred creation** — the value is not computed until execution time
- **Composability** — can be a component of a complex processing process

## Combining Operations

### Sequential Connection (`>>`)

**Syntax:**

```dotty
val combined: Pull[Int, Unit] = Pull.Output(1) >> Pull.Output(2)
```

**Semantics:** "Do A, then do B"

**How it works:**

1. Execute the left `Pull` (generates all its outputs)
2. Ignore its result
3. Execute the right `Pull`

**Equivalent via `flatMap`:**

```dotty
Pull.Output(1).flatMap(_ => Pull.Output(2))
```

**Features:**

- **Right side is lazy** — computed only when needed
- **Preserves all outputs** — combines output elements
- **Ignores results** — uses only output values

### Converting to List (`toList`)

A `Pull` can be converted to a regular List:

```dotty
val p: Pull[Int, Unit] = Pull.Output(1) >> Pull.Output(2)
val asList: List[Int] = p.toList  // List(1, 2)
```

**Warning:** Do not use for large or infinite streams!

**Alternatives for large data:**

```dotty
// Processing in chunks
p.fold(0)(_ + _)        // Sum without loading into memory
p.map(_ * 2).take(1000) // Size limiting (take is described below)
```

## Building from Collections

### From List

```dotty
def fromList[O](list: List[O]): Pull[O, Unit] =
  list match
    case Nil          => Pull.done
    case head :: tail => Pull.Output(head) >> fromList(tail)
```

**Recursive structure:**

```
Output(1) >> (Output(2) >> (Output(3) >> done))
```

How it works:

- For an empty List, return `done`
- For a non-empty List — emit the first element and recursively process the tail

Example:

```dotty
fromList(List(1, 2, 3)).toList  // List(1, 2, 3)
```

### From LazyList

```dotty
def fromLazyList[O](lazyList: LazyList[O]): Pull[O, Unit] =
  lazyList match
    case LazyList()    => Pull.done
    case head #:: tail => Pull.Output(head) >> fromLazyList(tail)
```

**Key advantages:**

- **Works with infinite streams** (limited only by memory)
- **Lazy computation** — elements are computed on demand
- **Memory efficient** — does not load all data at once

**Example with an infinite stream:**

```dotty
val naturals = LazyList.from(1)       // 1, 2, 3, ...
fromLazyList(naturals.take(5)).toList // List(1, 2, 3, 4, 5)
```

**Limitation:** `toList` on an infinite stream will not terminate

## General Creation via `unfold`

### Unfold Principle

`unfold` is a general way to create streams from an initial state.

**Signature:**

```dotty
def unfold[O, R](init: R)(f: R => Either[R, (O, R)]): Pull[O, R] =
  f(init) match
    case Left(result) => Pull.Result(result)
    case Right((output, nextState)) =>
      Pull.Output(output) >> unfold(nextState)(f)
```

**Logic:**

1. Start with state `init`
2. Apply function `f`:
   - Result `Left(result)` → complete with `result`
   - Result `Right((output, nextState))` → emit `output`, continue with `nextState`

### Example: Fibonacci Numbers

```dotty
val fibs: Pull[Int, (Int, Int)] = unfold((0, 1)) {
  case (a, b) => Right((a, (b, a + b)))
}
```

**Step-by-step execution:**

```
State: (0, 1) → Output(0), new state: (1, 1)
State: (1, 1) → Output(1), new state: (1, 2)
State: (1, 2) → Output(1), new state: (2, 3)
State: (2, 3) → Output(2), new state: (3, 5)
```

### Implementation Features

1. **Laziness**: `>>` does not compute the second `Pull` immediately, allowing work with infinite streams
2. **Stack safety**: Recursion in `unfold` is tail-recursive and optimized
3. **Composability**: `Pull` can be freely combined

### Practical Applications of unfold

**Generating sequences:**

```dotty
// Arithmetic progression
def arithmetic(first: Int, diff: Int): Pull[Int, Int] =
  unfold(first)(current => Right((current, current + diff)))

// Geometric progression
def geometric(first: Int, ratio: Int): Pull[Int, Int] =
  unfold(first)(current => Right((current, current * ratio)))
```

**Reading with state:**

```dotty
def readChunks(file: File, chunkSize: Int): Pull[Array[Byte], Unit] =
  unfold(openFile(file)): channel =>
    val buffer = ByteBuffer.allocate(chunkSize)
    channel.read(buffer) match
      case -1 => Left(()) // EOF
      case n  => Right((buffer.array().take(n), channel))
```

## Practical Application

The building blocks and operations described above allow:

- Converting existing collections into streams
- Creating custom data generators
- Combining multiple streams together
- Processing data as it arrives without loading into memory

### Exercise 11.2.1: Implementing fromListViaUnfold and fromLazyListViaUnfold

> [Repository with exercises and instructions for completion][sample]

**Task:**

Implement `fromListViaUnfold` and `fromLazyListViaUnfold` as methods of the `Pull` companion object, using `unfold`:

```dotty
def fromListViaUnfold[O](os: List[O]): Pull[O, Unit] = ???
def fromLazyListViaUnfold[O](os: LazyList[O]): Pull[O, Unit] = ???
```

@:note(🔍 Show Solution)

These functions convert `List[O]` and `LazyList[O]` into a `Pull[O, Unit]` stream using `unfold`:
for `List` pattern-matching on `Nil` and `hd :: tl` is used, and for `LazyList` — on `LazyList()` and `hd #:: tl`,
where each element of the sequence is emitted to the stream, and the tail becomes the new state,
until the collection is exhausted (return `Left`), after which the result is ignored via `.map(_ => ())`.

```dotty
def fromListViaUnfold[O](os: List[O]): Pull[O, Unit] =
  unfold(os):
    case Nil      => Left(Nil)
    case hd :: tl => Right((hd, tl))
  .map(_ => ())

def fromLazyListViaUnfold[O](os: LazyList[O]): Pull[O, Unit] =
  unfold(os):
    case LazyList() => Left(LazyList())
    case hd #:: tl  => Right((hd, tl))
  .map(_ => ())
```

@:@

## Infinite Streams

### Basic `continually` Implementation

The `continually` method creates an infinite stream that repeats one value:

```dotty
def continually[A](a: A): Pull[A, Nothing] =
  Output(a) >> continually(a)
```

**Type `Nothing` semantics:**

- `Nothing` is a type with no values. It indicates that the stream **will never terminate** naturally
- Can only be interrupted by external operations (`take`, `limit` - described below)
- Uses recursion for infinite repetition
- Lazy execution due to the `>>` operator

**Problem:** Not stack-safe due to recursion

Usage example:

```dotty
val ones: Pull[Int, Nothing] = Pull.continually(1)
```

### Improved Version with `repeat`

We can extract the common pattern of repetition:

```dotty
enum Pull[+O, +R]:
  def repeat: Pull[O, R] =
    this >> repeat

object Pull:
  def continually[A](a: A): Pull[A, Unit] =
    Output(a).repeat
```

**Advantages:**

- **Lazy recursion** — the right side of `>>` is computed only when needed
- **Code reuse** — common pattern for repetition
- **Readability** — explicit "repeat" semantics

Usage example:

```dotty
val ones: Pull[Int, Unit] = Pull.continually(1)
```

### Exercise 11.2.2: Creating an Infinite Stream with iterate

Define `iterate` as a method of the `Pull` companion object.
It should return an infinite stream that first emits the provided initial value,
then emits the result of applying the function to that value, and so on:

```dotty
def iterate[O](initial: O)(f: O => O): Pull[O, Nothing] = ???
```

**Explanation:**

- The `iterate` method creates an infinite sequence (stream) of values
- The first value will be `initial`
- Each subsequent value is obtained by applying function `f` to the previous value

**Usage example:**

```dotty
// Creates a stream: 1, 2, 4, 8, 16...
val powersOfTwo = Pull.iterate(1)(_ * 2)
```

@:note(🔍 Show Solution)

This is a recursive implementation of the `iterate` method that creates an infinite stream of values, starting from `initial`,
where each subsequent value is computed by applying function `f` to the previous one.
First the initial value is emitted via `Output`, then the next element is recursively generated (`f(initial)`),
ensuring sequential "unfolding" of the stream.
The `>>` operator binds the steps, guaranteeing correct lazy processing.

```dotty
def iterate[O](initial: O)(f: O => O): Pull[O, Nothing] =
  Output(initial) >> iterate(f(initial))(f)
```

@:@

## Partial Stream Execution

### The `take` Operation

Allows limiting a stream to N elements:

```dotty
def take(n: Int): Pull[O, Option[R]] =
  if n <= 0 then Result(None)
  else
    step match
      case Left(r)         => Result(Some(r))
      case Right((hd, tl)) => Output(hd) >> tl.take(n - 1)
```

**Execution Strategy:**

1. **Base case:** n ≤ 0 — complete with `None`
2. **Source exhaustion:** source completed — return its result in `Some`
3. **Data available:** emit element, decrement counter, continue

**Example:**

```dotty
Pull.continually(1).take(3).toList  // List(1, 1, 1)
```

### Basic `uncons` Block

However, the previous `take` implementation has one feature:
when `n` is positive, each step of the original `pull` is immediately computed.

So let's define a basic building block for partial execution:

```dotty
def uncons: Pull[Nothing, Either[R, (O, Pull[O, R])]] =
  Pull.done >> Result(step)
```

**Purpose:** Deferred retrieval of the next step

**Advantages over direct `step`:**

- **Laziness** — computation is deferred until use
- **Safety** — can be used in compositions
- **Uniformity** — works via the `Pull` mechanism

### Improved `take` via `uncons`

```dotty
def take(n: Int): Pull[O, Option[R]] =
  if n <= 0 then Result(None)
  else
    uncons.flatMap:
      case Left(r)         => Result(Some(r))
      case Right((hd, tl)) => Output(hd) >> tl.take(n - 1)
```

The `uncons` operation wraps `step` in a `Result` constructor and defers its creation via `flatMap`.
In the `take` implementation, instead of matching on the `step` result, we now match on the `uncons.flatMap` result.

Features:

- Defers computation via `>>`
- Returns either a result or a pair (element, continuation)
- Allows safe work with infinite streams

With this definition, partially executable infinite streams can now be created:

```dotty
val ints      = Pull.fromLazyList(LazyList.from(0))
val firstFive = ints.take(5).toList
// List(0, 1, 2, 3, 4)
```

### Exercise 11.2.3: Implementing drop for Pull

Implement the `drop` method:

```dotty
def drop(n: Int): Pull[O, R] = ???
```

**Explanation:**

- `drop(n)` — skips the first `n` elements of the stream
- The method works via recursive unfolding of the stream (`uncons`)

**Usage examples:**

```dotty
val nums = Pull.fromList(List(1, 2, 3, 4, 5))

nums.drop(2).toList          // List(3, 4, 5)
```

@:note(🔍 Show Solution)

`drop` skips the first `n` elements via recursive unfolding of the stream (`uncons`), returning the remainder.

```dotty
def drop(n: Int): Pull[O, R] =
  if n <= 0 then this
  else
    uncons.flatMap:
      // End of stream
      case Left(r) => Result(r)
      // Skip current element
      case Right((_, tl)) => tl.drop(n - 1)
```

@:@

### Exercise 11.2.4: Implementing takeWhile for Pull

Implement the `takeWhile` method:

```dotty
def takeWhile(p: O => Boolean): Pull[O, Pull[O, R]] = ???
```

**Explanation:**

- `takeWhile(p)` — emits elements while predicate `p` is true, then stops
- The method works via recursive unfolding of the stream (`uncons`)

**Usage examples:**

```dotty
val nums = Pull.fromList(List(1, 2, 3, 4, 5))

nums.takeWhile(_ < 4).toList // List(1, 2, 3)
```

@:note(🔍 Show Solution)

`takeWhile` sequentially emits elements satisfying predicate `f`, then returns the rest of the stream.

```dotty
def takeWhile(f: O => Boolean): Pull[O, Pull[O, R]] =
  uncons.flatMap:
    // End of stream
    case Left(r)         => Result(Result(r))
    case Right((hd, tl)) =>
      // Emit a matching element
      if f(hd) then
        Output(hd) >> tl.takeWhile(f)
      // Return the remaining stream
      else Result(Output(hd) >> tl)
```

@:@

### Exercise 11.2.5: Implementing dropWhile for Pull

Implement the `dropWhile` method:

```dotty
def dropWhile(f: O => Boolean): Pull[O, R] = ???
```

**Explanation:**

- `dropWhile(p)` — skips elements while predicate `p` is true, then returns the rest
- The method works via recursive unfolding of the stream (`uncons`)

**Usage examples:**

```dotty
val nums = Pull.fromList(List(1, 2, 3, 4, 5))

nums.dropWhile(_ < 3).toList // List(3, 4, 5)
```

@:note(🔍 Show Solution)

`dropWhile` skips elements while they satisfy `f`, then returns the rest.

```dotty
def dropWhile(f: O => Boolean): Pull[O, R] =
  uncons.flatMap:
    // End of stream
    case Left(r)         => Result(r)
    case Right((hd, tl)) =>
      // Skip a matching element
      if f(hd) then tl.dropWhile(f)
      // Return the remaining stream
      else Output(hd) >> tl  
```

@:@

## Stream Transformations

### Transforming Elements (`mapOutput`)

Transforms each element of a stream:

```dotty
def mapOutput[O2](f: O => O2): Pull[O2, R] =
  uncons.flatMap:
    case Left(r)         => Result(r)
    case Right((hd, tl)) => Output(f(hd)) >> tl.mapOutput(f)
```

**Semantics:** "Apply `f` to each element of the stream"

**Example:**

```dotty
Pull.fromList(List(1, 2, 3)).mapOutput(_ * 2).toList
// List(2, 4, 6)
```

**Features:**

- **Lazy execution** — transformations are applied as needed
- **Structure preservation** — the result has the same termination type `R`

### Filtering (`filter`)

Filters elements by a condition:

```dotty
def filter(p: O => Boolean): Pull[O, R] =
  uncons.flatMap:
    case Left(r) => Result(r)
    case Right((hd, tl)) =>
      (if p(hd) then Output(hd) else Pull.done) >> tl.filter(p)
```

**Strategy:** Skip element if predicate is `false`, otherwise emit

**Example:**

```dotty
Pull.fromList(List(1, 2, 3, 4)).filter(_ % 2 == 0).toList
// List(2, 4)
```

## Working with State

### Counting Elements (`count`)

```dotty
def count: Pull[Int, R] =
  def go(total: Int, p: Pull[O, R]): Pull[Int, R] =
    p.uncons.flatMap:
      case Left(r) => Result(r)
      case Right((_, tl)) =>
        val newTotal = total + 1
        Output(newTotal) >> go(newTotal, tl)

  Output(0) >> go(0, this)
```

**Output after each element:** 0, 1, 2, 3, ...

How it works:

1. Starts at 0
2. For each element, increments the counter
3. Emits the current count after each element

**Example:**

```dotty
Pull.fromList(List("a", "b", "c")).count.toList
// List(0, 1, 2, 3)
```

**Alternative — only the final result:**

```dotty
def totalCount: Pull[Nothing, Int] =
  fold(0)((count, _) => count + 1).map(_._1)
```

### General `mapAccumulate`

Combines `map` and `fold` with state preservation:

```dotty
def mapAccumulate[S, O2](init: S)(f: (S, O) => (S, O2)): Pull[O2, (S, R)] =
  uncons.flatMap:
    case Left(r) => Result((init, r))
    case Right((hd, tl)) =>
      val (s, out) = f(init, hd)
      Output(out) >> tl.mapAccumulate(s)(f)
```

**Example — Accumulating a Sum:**

```dotty
Pull.fromList(List(1, 2, 3)).mapAccumulate(0)((sum, x) =>
  (sum + x, s"Element: $x, Running total: ${sum + x}")
).toList
// List("Element: 1, Running total: 1",
//      "Element: 2, Running total: 3",
//      "Element: 3, Running total: 6")
```

**Applications:**

- Accumulating statistics
- Numbering elements
- Moving averages
- Stateful transformations

### Exercise 11.2.6: Implementing countViaMapAccumulate

Redefine `count` using `mapAccumulate` instead of explicit recursion.

```dotty
def countViaMapAccumulate: Pull[Int, R] = ???
```

**Usage examples:**

```dotty
Pull.fromList(List("a", "b", "c")).countViaMapAccumulate.toList
// List(0, 1, 2, 3)
```

@:note(🔍 Show Solution)

This solution implements counting elements in a stream via `mapAccumulate`, starting from 0:
initial value 0 is emitted first (`Output(0)`),
then for each stream element the state-counter is accumulated (incremented by 1),
at each step the current counter value is emitted (`s + 1`),
and `map(_(1))` extracts only the emitted values (the second element of the tuple), ignoring intermediate states.
The result is a stream of numbers from 1 to N (where N is the element count), preceded by zero.

```dotty
def countViaMapAccumulate: Pull[Int, R] =
  Output(0) >> mapAccumulate(0)((s, o) => (s + 1, s + 1)).map(_(1))
```

@:@

## Conclusion: Design Patterns

1. **Recursive Descent** — recursive construction via `>>`
2. **Unfold Pattern** — generation from state
3. **Transformer Pattern** — transformations via `mapOutput`, `filter`
4. **Accumulator Pattern** — state accumulation via `mapAccumulate`

**Advantages of the Approach:**

- **Composability** — small blocks assembled into complex pipelines
- **Laziness** — computations only on demand
- **Safety** — control over execution
- **Expressiveness** — clear operation semantics

This DSL allows building complex streaming processing from simple, well-composable components.

---

**Links:**

- [Link to the repository with exercises][sample]
- [Functional Programming in Scala, Second Edition, Chapter 15](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
