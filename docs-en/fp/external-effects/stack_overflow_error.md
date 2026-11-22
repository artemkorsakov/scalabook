# TailRec: Trampolining

## The Stack Overflow Problem in Functional Programs

Let's examine the stack problem in functional programming using the `IO` type,
which represents delayed computations with effects.

### Initial `IO` Definition

```dotty
trait IO[A]:
  self =>

  def unsafeRun(): A

  def map[B](f: A => B): IO[B] = new:
    def unsafeRun(): B = f(self.unsafeRun())

  def flatMap[B](f: A => IO[B]): IO[B] = new:
    def unsafeRun(): B = f(self.unsafeRun()).unsafeRun()

  def forever[B]: IO[B] =
    lazy val t: IO[B] = self.forever
    self.flatMap(_ => t)
    
object IO:
  def apply[A](a: => A): IO[A] = new:
    def unsafeRun(): A = a    
```

The problem arises when executing infinite operations, for example:

```dotty
def PrintLine(msg: String): IO[Unit] = IO(println(msg))

val p = PrintLine("Still going...").forever
p.unsafeRun()  // Causes StackOverflowError
```

### Why Does Stack Overflow Occur?

Let's step through the `flatMap` implementation for `IO`:

```dotty
def flatMap[B](f: A => IO[B]): IO[B] = new IO[B]:
  def unsafeRun(): B =
    val a: A          = self.unsafeRun() // Step 1: execute current IO
    val nextIO: IO[B] = f(a)             // Step 2: create next IO
    nextIO.unsafeRun() // Step 3: execute next IO
```

#### Step-by-Step Execution:

1. **First `flatMap` call:**
   - A new `IO[B]` instance is created.
   - When `unsafeRun()` is called:
     - `self.unsafeRun()` executes (current operation).
     - Function `f` is applied to the result.
     - `unsafeRun()` is immediately called on the result of `f(a)`.

2. **Subsequent calls:**
   - Each next `flatMap` adds a new `unsafeRun()` call to the stack:
     ```dotty
     IO(1)
         .flatMap(x => IO(x + 1)) // Level 1
         .flatMap(y => IO(y * 2)) // Level 2
         .unsafeRun()
     ```
   - Call stack:
     ```
     unsafeRun (Level 2)
     unsafeRun (Level 1)
     unsafeRun (Original IO)
     ```

3. **Overflow problem:**
   - Each `flatMap` requires a new stack frame.
   - With long chains (e.g., in a loop), a `StackOverflowError` occurs.
   - Stack depth is typically limited to ~1000-10000 calls.

#### Root Cause:

The implementation uses **non-tail recursion**, because:

- The call `nextIO.unsafeRun()` is not the final operation (after it comes an implicit return).
- The JVM cannot perform tail call optimization.

## Explicit Modeling of Control Flow

The traditional approach to implementing `IO` usually hides control flow details inside methods
such as `flatMap`, relying on hidden state or call stack.
In the proposed approach, **control flow is materialized explicitly** as a data structure,
which provides several advantages:

1. **Transparency**: the program structure becomes visible and inspectable.
2. **Control**: we gain full control over program execution.
3. **Flexibility**: we can implement various execution strategies.

### New `IO` Definition via Algebraic Data Type

```dotty
enum IO[A]:
  self =>

  // This is a pure computation that sequentially returns A without any steps.
  case Return(a: A)

  // This is a suspension of computation, where the resumption is a function
  // that takes no arguments but produces some side effect and a result.
  case Suspend(resume: () => A)

  // This is a composition of two steps.
  // It materializes FlatMap as a data constructor, not as a function.
  case FlatMap[B, C](sub: IO[B], k: B => IO[C]) extends IO[C]

  def flatMap[B](f: A => IO[B]): IO[B] =
    FlatMap(self, f)

  def map[B](f: A => B): IO[B] =
    flatMap(a => Return(f(a)))

  def forever[B]: IO[B] =
    lazy val t: IO[B] = self.forever
    self.flatMap(_ => t)
end IO

object IO:
  def apply[A](a: => A): IO[A] =
    Suspend(() => a)
```

### Data Constructors

1. **`Return(a: A)`**:
   - Represents a completed computation with result `a`.
   - When the interpreter encounters `Return`, it knows that the computation has ended.

2. **`Suspend(resume: () => A)`**:
   - Encapsulates a side effect that needs to be executed.
   - `resume` is a "frozen" effect that can be executed later.
   - Allows separating the description of an effect from its execution.

3. **`FlatMap[B, C](sub: IO[B], k: B => IO[C])`**:
   - Represents the sequential composition of two computations.
   - `sub` — the first computation.
   - `k` — the continuation that receives the result of `sub` and returns the next computation.
   - The key difference from traditional `flatMap`: this is not a method, but a data constructor.

### `IO` Type Methods

1. **`flatMap`**:
   - The implementation is now trivial — it simply creates the `FlatMap` constructor.
   - It performs no work; it only builds a data structure.
   - It delays real work until interpretation time.

2. **`map`**:
   - Implemented via `flatMap` and `Return`.
   - Transforms the result without adding new effects.

### Companion Object `IO`

- **`apply`** (factory method):
  - Creates a suspended computation.
  - Takes a by-name parameter (`a: => A`) to delay the computation.

### Example of Building a Program

```dotty
def printLine(s: String): IO[Unit] =
  IO(println(s))

val p = printLine("Still going...").forever
```

This program builds as a nested structure:

```dotty
FlatMap(
  Suspend(() => println("Still going...")),
  _ => FlatMap(
    Suspend(() => println("Still going...")),
    _ => FlatMap(...)))
```

## Tail-Recursive Interpreter

### Implementing `unsafeRun`

```dotty
@annotation.tailrec
final def unsafeRun(): A =
  self match
    case Return(a)  => a   // Base case - return the result
    case Suspend(r) => r() // Execute the effect
    case FlatMap(x, f) =>
      x match
        case Return(a)  => f(a).unsafeRun()   // Continue with the result
        case Suspend(r) => f(r()).unsafeRun() // Execute the effect and continue
        case FlatMap(y, g) =>
          // In this case, io is an expression like FlatMap(FlatMap(y, g), f).
          // Rewrite the left-associative chain to a right-associative one, 
          // to call unsafeRun in tail position
          y.flatMap(a => g(a).flatMap(f)).unsafeRun()
```

The `unsafeRun` interpreter executes the program represented as a data structure `IO`,
guaranteeing stack safety through tail recursion.
This means the program can run indefinitely (e.g., a server) without risk of stack overflow.

### The `@annotation.tailrec` Annotation

```dotty
@annotation.tailrec
final def unsafeRun(): A
```

- Guarantees that the compiler will verify the method is actually tail-recursive.
- If the check fails — compilation error.
- `final` prevents method overriding.

### Pattern Matching

**Case `Return(a)`**:

```dotty
case Return(a) => a
```

- Base case of recursion.
- Simply returns the computed value.
- Stops program execution.

**Case `Suspend(r)`**:

```dotty
case Suspend(r) => r()
```

- Executes the deferred effect (calls the function `r`).
- Returns the result of the effect.

**Case `FlatMap(x, f)`** (the most complex case):

```dotty
case FlatMap(x, f) =>
  x match
    case Return(a)  => f(a).unsafeRun()  
    case Suspend(r) => f(r()).unsafeRun()
    case FlatMap(y, g) =>
      y.flatMap(a => g(a).flatMap(f)).unsafeRun()
```

- This is where the stack-safety magic happens.
- We analyze the internal structure of `x`.

#### Sub-case `Return(a)`:

```dotty
case Return(a) => f(a).unsafeRun()
```

- If the sub-computation is already completed (`Return`),
- Apply the continuation `f` to result `a`.
- Recursively call `unsafeRun` on the result.

#### Sub-case `Suspend(r)`:

```dotty
case Suspend(r) => f(r()).unsafeRun()
```

- Execute effect `r()`.
- Pass the result to continuation `f`.
- Recursively call `unsafeRun`.

#### Sub-case `FlatMap(y, g)` (nested FlatMaps):

```dotty
case FlatMap(y, g) => y.flatMap(a => g(a).flatMap(f)).unsafeRun()
```

This is the most important part for ensuring stack safety.

**What happens**:

1. We have a structure `FlatMap(FlatMap(y, g), f)`.
2. The complete type picture before transformation: `FlatMap(FlatMap(y: IO[A], g: A => IO[B]), f: B => IO[C])`.
   Goal — compute a value of type `C`.
3. Rewrite it into an equivalent right-associative form:
   - Before: `y.flatMap(g).flatMap(f)`
   - After: `y.flatMap(a => g(a).flatMap(f))`
4. Use the [monad associative law](../../typeclass/monad/monad.md):
   `(x.flatMap(f)).flatMap(g) == x.flatMap(a => f(a).flatMap(g))`

Let's break down the expression `y.flatMap(a => g(a).flatMap(f))` step by step:

1. **Inner `flatMap` (`g(a).flatMap(f)`)**:
   - `a: A` (value extracted from `y`)
   - `g(a): IO[B]` (result of applying continuation `g` to `a`)
   - `f: B => IO[C]` (continuation of the outer `FlatMap`)
   - **`g(a).flatMap(f): IO[C]`** — apply function `f` to the result of computing `g(a)`. The type of this expression is `IO[C]`.

2. **Lambda function (`a => ...`)**:
   - `a: A` (input parameter)
   - Function body: `g(a).flatMap(f): IO[C]` (result of step 1)
   - **Lambda type: `A => IO[C]`**. This is a function that takes a value of type `A` and
     returns a new `IO` program, which ultimately computes type `C`.

3. **Outer `flatMap` (`y.flatMap(...)`)**:
   - `y: IO[A]` (original computation)
   - Lambda: `A => IO[C]` (result of step 2)
   - **`y.flatMap(a => g(a).flatMap(f)): IO[C]`** — apply our new lambda to the result of computing `y`.
     The result type is `IO[C]`, which fully matches the type of the original expression.

**Transformation Visualization**

Before (left-associative chain, stack-unsafe):

```dotty
val program: IO[C] =
  y.flatMap(g) // IO[B]
   .flatMap(f) // IO[C]
```

After (right-associative chain, stack-safe):

```dotty
val transformedProgram: IO[C] =
  y.flatMap { a: A => // Outer flatMap
    g(a)              // IO[B]
      .flatMap(f)     // IO[C] (this expression is the body of the lambda)
  }
```

The transformation **does not change the final computation type**, which always remains `IO[C]`.
Only the _structure_ of the monadic chain changes:

- Original structure: `FlatMap(FlatMap(y, g), f)`
- Transformation goal: `FlatMap(y, h)`, where `h` is the function `a => FlatMap(g(a), f)`.

This transformation allows the next call to `unsafeRun()` to be in tail position not for a nested chain,
but for the outermost computation `y` itself, which ensures stack safety.
The interpreter will first recursively process `y`,
and only then, when it reaches its result, apply the continuation `h` to it.

### Program Execution Example

Consider a specific example:

```dotty
val p = IO(println("A"))
  .flatMap(_ => IO(println("B")))
  .flatMap(_ => IO(println("C")))
```

### Original Structure

```dotty
FlatMap(
  FlatMap(
    Suspend(() => println("A")),
    _ => Suspend(() => println("B"))
  ),
  _ => Suspend(() => println("C"))
)
```

### After Rewriting

```dotty
FlatMap(
  Suspend(() => println("A")),
  _ => FlatMap(
    Suspend(() => println("B")),
    _ => Suspend(() => println("C"))
  )
)
```

### Execution Steps:

1. Start at the top level:
   ```dotty
   FlatMap(Suspend(() => println("A")), continuation1)
   ```
2. Execute `println("A")`.
3. Apply **continuation1**:
   ```dotty
   FlatMap(Suspend(() => println("B")), continuation2)
   ```
4. Execute `println("B")`.
5. Apply **continuation2**:
   ```dotty
   Suspend(() => println("C"))
   ```
6. Execute `println("C")`.

### Why Is This Stack-Safe?

Key points:

1. All recursive calls to `unsafeRun` are in tail position.
2. Deep left-associative `flatMap` chains are rewritten to right-associative form.
3. The compiler transforms tail recursion into a loop (does not use the stack).

This allows:

- Running infinite programs (e.g., `forever`).
- Processing deep chains of operations.
- Guaranteeing the absence of `StackOverflowError`.

### Comparison with Naive Implementation

**Naive (stack-unsafe) implementation**:

```dotty
def unsafeRunNaive(): A =
  self match
    case Return(a)     => a
    case Suspend(r)    => r()
    case FlatMap(x, f) => f(x.unsafeRunNaive()).unsafeRunNaive() // Danger!
```

Two recursive calls to `unsafeRunNaive` in the `FlatMap` case — not tail recursion.

## The Trampolining Principle

### The Fundamental Problem of Program Execution

On the JVM (and in most other execution environments):

1. Each function call adds a new **stack frame** to remember where to return after the call completes,
   so execution can proceed.
2. This frame contains:
   - Function parameters
   - Local variables
   - Return address
3. Deep recursion leads to stack overflow → `StackOverflowError`.

### How the Trampolining Principle Solves This Problem

#### Core Idea

Instead of using the JVM call stack:

1. **The program is represented as data** (algebraic type `IO`).
2. **The interpreter (`unsafeRun`)** executes steps sequentially.
3. **Control is passed explicitly** between the program and the interpreter.

#### Trampoline Analogy

1. **The program** is the trampoline jumper:
   - Makes a "jump" (executes one step).
   - Returns control to the "trampoline" (interpreter).
2. **The interpreter** is the trampoline itself:
   - Receives control after each jump.
   - Decides what to do next.

#### Detailed Process

1. **The program initiates a step**:
   - Option 1: returns `Suspend(effect)` or `Return(value)` — need to execute an effect or return a computed value.
   - Option 2: returns `FlatMap(sub, continuation)` — need to execute a sub-program.

2. **The interpreter receives control**:
   - Option 1: executes the effect (for `Suspend`) or returns the value (for `Return`).
   - Option 2: analyzes the sub-program (for `FlatMap`).

3. **The program receives the result**:
   - For `Suspend` and `Return` — the result value or computed value.
   - For `FlatMap` — the result of the sub-program.

4. **The cycle repeats**:
   - The program decides what to do next.
   - Returns a new instruction to the interpreter.

### Conclusion

Thus, our `IO` program is a kind of coroutine that executes cooperatively with `unsafeRun`.
It constantly makes `Suspend` or `FlatMap` requests, and each time it does,
it suspends its own execution and returns control to `unsafeRun`.
It is actually `unsafeRun` that drives program execution forward, one suspension at a time.

A function of type `unsafeRun` is often called a _trampoline_,
and the general technique of returning control to a single loop to eliminate the stack is called _trampolining_.

The "trampolining" approach solves the fundamental problem of stack safety in functional programs through:

1. **Explicit representation** of control flow as data.
2. **Tail-recursive interpreter** that executes steps sequentially.
3. **Rewriting left-associative chains** to right-associative form.

This allows running without risk of stack overflow:

- Infinite recursions.
- Deep chains of operations.
- Complex effect combinations.

## A General Solution to the Stack Overflow Problem

The `resume` function in `IO` does not necessarily perform side effects.
The `IO` type is actually a general data structure for trampolined computations —
even pure computations that perform no I/O at all!

In traditional programming, each function call adds a new level to the call stack.
When there are too many such calls (e.g., with deep recursion),
stack overflow (StackOverflowError) occurs.

**Example of the problem:**

```dotty
val f = (x: Int) => x  // Simple identity function

// Build a composition of 100,000 functions
val g = List.fill(100000)(f).foldLeft(f)(_ compose _)

g(42)  // Causes StackOverflowError
```

Here we build a chain of 100,000 nested function calls, which exceeds stack size.

### Solution via Trampoline (`TailRec`)

#### Core Idea

We can avoid stack overflow by transforming:

- Ordinary function calls → into a **data structure**.
- Recursion → into a **sequence of steps** processed by an interpreter.

But this is actually not a concept for I/O, but a concept for eliminating the call stack!

Let's change the name `IO` to `TailRec` to reflect this:

```dotty
enum TailRec[A]:
  // Pure value
  case Return(a: A)

  // Delayed computation (may contain an effect)
  case Suspend(resume: () => A)

  // Composition of computations
  case FlatMap[B, C](sub: TailRec[B], k: B => TailRec[C]) extends TailRec[C]

  def flatMap[B](f: A => TailRec[B]): TailRec[B] =
    FlatMap(this, f)

  def map[B](f: A => B): TailRec[B] =
    flatMap(a => Return(f(a)))

  // Renamed the run function to run, 
  // since it is no longer used for capturing effects.
  @annotation.tailrec
  final def run: A = this match
    case Return(a)  => a   // Base case
    case Suspend(r) => r() // Execute the computation
    case FlatMap(x, f) => x match
        case Return(a)  => f(a).run   // Continue with the result
        case Suspend(r) => f(r()).run // Execute and continue
        case FlatMap(y, g) =>
          y.flatMap(a => g(a).flatMap(f)).run // Rewrite the chain
```

#### How Does This Work?

1. **Transform functions**:
   Instead of `A => B`, we use `A => TailRec[B]`.

2. **Composition via `flatMap`**:
   ```dotty
   val f: Int => TailRec[Int] = x => Return(x)
   val g: Int => TailRec[Int] = x => Suspend(() => f(x))
   ```

3. **Example of safe composition**:
   ```dotty
   val safeCompose: (Int => TailRec[Int]) => 
                     (Int => TailRec[Int]) => 
                     Int => TailRec[Int] = 
     f => g => x => Suspend(() => f(x).flatMap(g))
   ```

4. **Execution via `run`**:
   The interpreter processes the data structure without stack growth.

### Practical Example

#### Problematic Code (with stack)

```dotty
def factorial(n: BigInt): BigInt =
  if n <= 1 then 1
  else n * factorial(n - 1) // Recursion -> stack grows

factorial(100_000) // StackOverflowError
```

#### Safe Version (with `TailRec`)

```dotty
def factorial(n: BigInt): TailRec[BigInt] =
  if n <= 1 then Return(1)
  else
    Suspend(() => factorial(n - 1)).flatMap: r =>
      r.flatMap(m => Return(n * m))

// Usage
factorial(100000).run // Works without StackOverflow
```

### Key Advantages

1. **Stack Safety**:
   - Deep recursions are possible.
   - No risk of StackOverflowError.

2. **Purity of Computations**:
   - Can work without side effects.
   - Suitable for purely functional computations.

3. **Flexibility**:
   - Can be combined with effects.
   - Supports complex composition scenarios.

### Performance Trade-offs

#### Slowdown

- Each step requires data structure processing.
- Additional object allocations.
- On average, 10-100 times slower than direct calls.

#### When to Use

1. **Deep recursion** (typically > 1000 levels).
2. **Composition of many functions**.
3. **Guaranteed execution** without stack overflow.

### Conclusion

The `TailRec` type provides a general solution for:

- Eliminating the call stack.
- Safe function composition.
- Control over program execution.

This is a fundamental technique in functional programming
for ensuring reliable program execution with predictable memory usage.

## `TailRec` in the Standard Library

The `TailRec` type defined here is built into the Scala standard library in the `scala.util.control.TailCalls` object.
The object defines the `TailRec[A]` type, as well as the `done` and `tailcall` constructors,
corresponding to `Return` and `Suspend`.

The `TailRec` type contains a `result` method that interprets a `TailRec[A]` value into an `A` value.

Here is the same example, using `TailCalls` instead of `TailRec`:

```dotty
import scala.util.control.TailCalls.*

def factorial(n: BigInt): TailRec[BigInt] =
  if n <= 1 then done(1)
  else tailcall(factorial(n - 1)).flatMap(r => done(n * r))

// Usage
factorial(100000).result // Works without StackOverflow
// val res0: BigInt = 28242294079603478742934215780245355184...
```

## Exercises

### Exercise 9.2.1: Fibonacci Numbers

> [Repository with exercises and instructions][sample]

**Task:**

Implement computation of the Nth Fibonacci number via `TailCalls`, avoiding stack overflow.

```dotty
import scala.util.control.TailCalls.*

def fibonacci(n: Int): TailRec[BigInt] =
  def loop(a: BigInt, b: BigInt, step: Int): TailRec[BigInt] =
    if step >= n then ???
    else ???

  if n <= 1 then ???
  else loop(0, 1, 0)
```

**Examples:**

```dotty
// Should return a large Fibonacci number
fibonacci(1000).result 
```

@:note(🔍 Show solution)

This solution computes the Nth Fibonacci number via tail recursion using `TailRec`
to avoid stack overflow.
The inner `loop` function sequentially accumulates values `(a, b)`,
where each subsequent number equals the sum of the previous two, until the Nth step is reached.
The base case (`n ≤ 1`) returns `n`, and for other cases, `loop` is launched with initial values 0 and 1.
Using `tailcall` guarantees stack safety even for very large N.

```dotty
def fibonacci(n: Int): TailRec[BigInt] =
  def loop(a: BigInt, b: BigInt, step: Int): TailRec[BigInt] =
    if step >= n then done(a)
    else tailcall(loop(b, a + b, step + 1))

  if n <= 1 then done(n)
  else loop(0, 1, 0)
```

@:@

### Exercise 9.2.2: Combining Multiple `TailRec` Values

**Task:**

Write a function that combines the results of two `TailRec` computations (e.g., the sum of two factorials).

```dotty
def factorial(n: BigInt): TailRec[BigInt] =
  if n <= 1 then done(1)
  else tailcall(factorial(n - 1)).flatMap(r => done(n * r))
  
def sumFactorials(a: Int, b: Int): TailRec[BigInt] =
  ???
```

**Examples:**

```dotty
sumFactorials(1000, 1000).result // Should work for large numbers
```

@:note(🔍 Show solution)

This solution computes the sum of two factorials via stack-safe recursion using `TailRec`.
The `factorial` function calculates a factorial via tail recursion
(wrapping the recursive call in `tailcall` and multiplying the result by `n`),
while `sumFactorials` combines two factorials via for-comprehension, calling them via `tailcall` and summing results.
The approach guarantees no stack overflow even for large numbers thanks to the `TailRec` trampolining mechanism.

```dotty
def factorial(n: BigInt): TailRec[BigInt] =
  if n <= 1 then done(1)
  else tailcall(factorial(n - 1)).flatMap(r => done(n * r))
  
def sumFactorials(a: Int, b: Int): TailRec[BigInt] =
  for
    fa <- tailcall(factorial(a))
    fb <- tailcall(factorial(b))
  yield fa + fb
```

@:@

### Exercise 9.2.3: Mutually Recursive Functions

**Task:**

Implement two mutually recursive functions (e.g., even/odd checking)
that do not crash with `StackOverflowError`.

```dotty
def isEven(n: Int): TailRec[Boolean] =
  ??? // For 0 returns true, for other n calls isOdd for n-1

def isOdd(n: Int): TailRec[Boolean] =
  ??? // For 0 returns false, for other n calls isEven for n-1
```

**Examples:**

```dotty
isEven(100001).result // Should return false
```

@:note(🔍 Show solution)

This solution implements mutual recursion for number parity checking via `TailRec`, avoiding stack overflow.
The `isEven` and `isOdd` functions call each other, decrementing `n` to the base case (`n == 0`),
where they return `true` for even and `false` for odd.
Using `tailcall` makes calls stack-safe,
transforming mutual recursion into a sequence of tail calls,
which allows correct operation even with large numbers.

```dotty
def isEven(n: Int): TailRec[Boolean] =
  if n == 0 then done(true)
  else tailcall(isOdd(n - 1))

def isOdd(n: Int): TailRec[Boolean] =
  if n == 0 then done(false)
  else tailcall(isEven(n - 1))
```

@:@

---

**Links:**

- [Repository with exercises][sample]
- [Trampoline Effect in Scala. WTF?!](https://vkvideo.ru/video-211972724_456239234)
- [Functional Programming in Scala, Second Edition, Chapter 13](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
