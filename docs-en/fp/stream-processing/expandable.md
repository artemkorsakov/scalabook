# Expandable `Pull` and `Stream`

## Context and Motivation

Previous implementations of `Pull` and `Stream` provided an alternative,
but similarly expressive way of describing lazy computations, comparable to `LazyList`. 
The key limitation was that to perform I/O operations (such as reading a file), 
the entire program was wrapped in a monolithic `IO` structure, which hid the data source within itself. 
This prevented effects (e.g., opening a file) from being part of the stream structure itself.

The new design solves this problem by making both `Pull` and `Stream` types expandable to support arbitrary effects. 
This allows describing data sources (e.g., reading from a file or network) directly within the stream, rather than hiding them outside.

## New `Pull` Structure

The `Pull` type was parameterized by three types:

- `F[_]` — the effect type (e.g., `IO`).
- `O` — the type of output stream elements.
- `R` — the type of the final computation result.

A new data constructor was added:

```dotty
enum Pull[+F[_], +O, +R]:
  // Existing constructors:
  case Result[+R](result: R) extends Pull[Nothing, Nothing, R]
  case Output[+O](value: O)  extends Pull[Nothing, O, Unit]
  case FlatMap[+F[_], X, +O, +R](
      source: Pull[F, O, X],
      f: X => Pull[F, O, R]
  ) extends Pull[F, O, R]

  // New constructor:
  case Eval[+F[_], R](action: F[R]) extends Pull[F, Nothing, R]
```

### New Constructor Explanation:

**`Eval[+F[_], R](action: F[R])`**

- **Purpose:** This is a fundamental new feature. The `Eval` constructor allows embedding 
  an arbitrary action with effect type `F[R]` into the stream structure.
- **Semantics:** During interpretation (execution) of the stream, when execution reaches `Eval`, action `action` will be executed. 
  The result of this action (`R`) will be used to continue the stream.
- **Types:** `Eval[F, R] extends Pull[F, Nothing, R]`. This means:
    - It introduces an effect of type `F`.
    - It produces no _output stream elements_ (`Nothing` in place of `O`).
    - It computes a _result_ of type `R`.

**Type Parameter `F[_]` and Covariance**

- Parameter `F` has become covariant (`+F[_]`). This is an important feature of the type system that allows:
    - **`Result` and `Output` to fix `F` to `Nothing`:** Since these constructors perform no effects, 
      they are compatible with any other effect `F` in composition. 
      `Nothing` is the "bottom" type in Scala's hierarchy, making these constructors maximally generic.
    - **Safely combining streams with different effects** through subtyping.

### How This Changes the Approach?

- **Old approach (monolithic IO):**

  ```dotty
  // Effect (reading a file) is hidden inside IO
  val program: IO[Unit] = IO {
      val source = scala.io.Source.fromFile("file.txt")
      // ... use source to create Stream ...
  }
  ```

  The data source was created _before_ building the stream and was hidden inside `IO`.

- **New approach (effects inside Stream):**
  ```dotty
  // Effect becomes part of the stream
  def fileLines(path: String): Stream[IO, String] =
      Stream.eval(IO(scala.io.Source.fromFile(path))) // Eval effect for opening the file
        .flatMap(source => ... // Build a stream of lines from source
  ```
  The action to open the file (`IO(...)`) can now be embedded into the stream as an `Eval` element. 
  This makes the data source an explicit and manageable part of the lazy stream.

This change fundamentally increases the expressiveness of the `Pull/Stream` system. 
It can now not only transform pure data, 
but also describe _how to obtain this data_, including side effects. 
This opens the path to creating complex, composable data streams with pure resource management 
and effects directly at the stream abstraction level.

## Variance Annotations in Type `Pull`

When designing parameterized types in Scala, a key decision is choosing the variance annotation for type parameters: 
invariance (`Pull[F, O, R]`) or covariance (`Pull[+F[_], +O, +R]`).

```dotty
enum Pull[F[_], O, R]: // All parameters are invariant
  case Result(result: R)
  case Output(value: O)
  case Eval(action: F[R])
  case FlatMap[F[_], X, O, R](
      source: Pull[F, O, X],
      f: X => Pull[F, O, R]
  ) extends Pull[F, O, R]
```

**Advantage:** This definition looks simpler and more intuitive.

**Disadvantage:** It creates significant inconvenience in practical usage and composition of `Pull` values.

### Problem Example: Composing `Pull` Objects

Consider two simple, independent expressions:

```dotty
Pull.Output(1)
// val res0: Pull[Nothing, Int, Unit] = Output(1)

Pull.Result(true)
// val res1: Pull[Nothing, Nothing, Boolean] = Result(true)
```

It would be logical to assume that we can combine them with the `>>` operator 
(which sequentially executes two `Pull`s, ignoring the result of the first and returning the result of the second).

**Expectation:** `res0 >> res1` should have type `Pull[Nothing, Int, Boolean]`. 
First element `1` is emitted (type `O = Int`), then result `true` is computed (type `R = Boolean`).

**Reality with an invariant type:**

```dotty
res0 >> res1
-- [E007] Type Mismatch Error: -------------------------------------------------
1 |res0 >> res1
  |        ^^^^
  |        Found:    (res1 : Pull[Nothing, Nothing, Boolean])
  |        Required: Pull[Nothing, Int, Any]
```

**Explanation of the error:** The `>>` operator is defined roughly like this:

```dotty
def >>[F2[_], O2 >: O, R2](that: Pull[F2, O2, R2]): Pull[F2, O2, R2] = ...
```

For `res0 >> res1`, the compiler tries to substitute types:

1. `res0` has type `Pull[Nothing, Int, Unit]`.
2. The `>>` method for `res0` expects an argument of type `Pull[F2, Int, R2]` (since `O2 >: Int`, and the most concrete type is `Int`).
3. `res1` has type `Pull[Nothing, Nothing, Boolean]`.
4. Type `Nothing` for `O` in `res1` _is not_ a subtype of `Int`, required for `that`. 
   `Nothing` is a subtype of everything, but here the compiler needs an argument whose 
   type `O` _matches or is a supertype_ of the `O` from `res0`. 
   `Nothing` is not a supertype of `Int`. Therefore, types are incompatible.

**Workaround:** The only way to combine them is to create both expressions _in a single expression_, 
so the compiler can infer a common, unified type for all parameters _before they are fixed_:

```dotty
Pull.Output(1) >> Pull.Result(true)
// val res2: Pull[Nothing, Int, Boolean] = ...
```

Here the compiler sees the goal — combination — from the very moment of construction, 
and can infer common parameters `O = Int` and `R = Boolean` for the entire construction.

### Solution: Covariant Version (Harder Definition, More Convenient API)

```dotty
enum Pull[+F[_], +O, +R]: // All parameters are covariant
  case Result[+R](result: R) extends Pull[Nothing, Nothing, R]
  case Output[+O](value: O) extends Pull[Nothing, O, Unit]
  case Eval[+F[_], R](action: F[R]) extends Pull[F, Nothing, R]
  case FlatMap[+F[_], X, +O, +R](
    source: Pull[F, O, X], f: X => Pull[F, O, R]) extends Pull[F, O, R]
```

**Why this works:** Covariance means that `Pull[F1, O1, R1]` is a subtype of `Pull[F2, O2, R2]`, 
if `F1 <: F2`, `O1 <: O2`, and `R1 <: R2`.

Now let's consider the expression `res0 >> res1` with covariant types:

1. `res0`: `Pull[Nothing, Int, Unit]`
2. `res1`: `Pull[Nothing, Nothing, Boolean]`
3. **Important:** Thanks to covariance, type `res1` (`Pull[Nothing, Nothing, Boolean]`) 
    **is automatically a subtype of** type `Pull[Nothing, Int, Boolean]`:
    - `Nothing <: Nothing` (`F`)
    - `Nothing <: Int` (`O`) — `Nothing` is a subtype of any type.
    - `Boolean <: Boolean` (`R`)
4. Therefore, the compiler can safely "lift" type `res1` to the required `Pull[Nothing, Int, Boolean]` 
   and successfully check the types.

Choosing covariance is a classic trade-off between implementation complexity and usability (API).

- **Invariance** leads to a simpler type definition, but creates inconvenience 
  and unintuitive behavior for the user, requiring manual workarounds for compilation.
- **Covariance** complicates the internal type definition and its methods (due to the need to preserve variance), 
  but the result is a much more expressive, predictable, and pleasant API, 
  where `Pull` values are freely combined as the user expects, following the principle of least surprise.

## Analysis of the `step` Method

Adding the `Eval` constructor to support arbitrary effects (`F[_]`) fundamentally changes the semantics of type `Pull`. 
This change directly affects the `step` method, which decomposes the `Pull` structure into steps.

**Old Signature (without effects):**

```dotty
def step: Either[R, (O, Pull[O, R])]
```

It returned a pure value: either the final result `R`, or the next element `O` and the stream tail.

**New Implementation:**

```dotty
enum Pull[+F[_], +O, +R]:
  ...

  def flatMap[F2[x] >: F[x], O2 >: O, R2](f: R => Pull[F2, O2, R2])
      : Pull[F2, O2, R2] =
    Pull.FlatMap(this, f)

  def step[F2[x] >: F[x], O2 >: O, R2 >: R](using
      F: Monad[F2] // #1
  ): F2[Either[R2, (O2, Pull[F2, O2, R2])]] =
    this match
      case Result(r)    => F.unit(Left(r))              // #2
      case Output(o)    => F.unit(Right(o, Pull.done))  // #3
      case Eval(action) => action.map(Left(_))          // #4
      case FlatMap(source, f) =>                        
        source match
          case FlatMap(s2, g) =>                        // #5
            s2.flatMap(x => g(x).flatMap(y => f(y))).step
          case other => other.step.flatMap:             // #6        
            case Left(r)         => f(r).step
            case Right((hd, tl)) => F.unit(Right((hd, tl.flatMap(f))))
end Pull

object Pull:
  val done: Pull[Nothing, Nothing, Unit] = Pull.Result(())
```

### Breakdown of the Implementation for Each Constructor

Let's examine how each case in the pattern match is handled, as well as the new signature:

1. **New Signature (with effects):**

    ```dotty
    def step[F2[x] >: F[x], O2 >: O, R2 >: R](
      using F: Monad[F2] // #1
    ): F2[Either[R2, (O2, Pull[F2, O2, R2])]]
    ```
    
    - **Return type:** `F2[Either[...]]`. Now the step execution process itself (`step`) is an action with an effect.
      The result of this action is an `Either`, wrapped in context `F2`.
    - **Execution Context:** For working with effect `F2`, a `Monad[F2]` implementation is required ([what is a monad?][monad]).
      This allows using `map` and `flatMap` operations inside the implementation of `step`.
    - **Parameters with lower bounds (`>:`):** `F2[x] >: F[x]`, `O2 >: O`, `R2 >: R`.
      These bounds are necessary to preserve the covariance of parameters `F, O, R`
      and to ensure the ability to "expand" the type when combining with other `Pull` objects.

2. **`case Result(r)`** — Final Result.

    ```dotty
    case Result(r) => F.unit(Left(r)) // #2
    ```

    - **Action:** An effect action (`F.unit`) is created that immediately returns the pure result `r`, wrapped in `Left`.
    - **`F.unit`**: A [monad][monad] operation that lifts a pure value into the effect context (`A => F[A]`).

3. **`case Output(o)`** — Output Element.

    ```dotty
    case Output(o)    => F.unit(Right(o, Pull.done))  // #3
    ```

    - **Action:** An effect action is created that returns element `o` and an empty tail (`Pull.done`), wrapped in `Right`.

4. **`case Eval(action)`** — Effectful Action.

    ```dotty
    case Eval(action) => action.map(Left(_))          // #4
    ```

    - **Action:** This is the key change. The effectful action `action: F[R]` itself is executed.
    - **Result:** The result of action `action` (value of type `R`) is mapped to `Left(R)`, 
      signaling stream completion with this result. 
      _Important:_ `Eval` produces no output elements (`O`), only a result.

5. **`case FlatMap(source, f)`** — Sequential Composition.
    This is the most complex case, ensuring stack safety.
    ```dotty
    case FlatMap(source, f) =>
      source match
        // Associativity rule: regroupging nested FlatMaps
        case FlatMap(s2, g) =>             // #5
          s2.flatMap(x => g(x).flatMap(y => f(y))).step // Recursive step call
        // Base case when source is not a FlatMap.
        // An effectful chain begins!
        case other => other.step.flatMap:  // #6
          // If source completed with result r, apply f to r and go to the next step
          case Left(r) => f(r).step 
          // If source emitted an element, emit it, applying f to the tail
          case Right((hd, tl)) => F.unit(Right((hd, tl.flatMap(f)))) 
    ```

    - **Associativity (#5):** The first case rewrites `source.flatMap(g).flatMap(f)` into `source.flatMap(x => g(x).flatMap(f))`, 
      which prevents stack growth due to `FlatMap` nesting.
    - **Effectful recursion (#6):** Note the call `other.step.flatMap`. 
      Recursion now occurs not in the call stack, but _inside_ the `flatMap` chain of monad `F`. 
      This makes the process stack-safe, but **only if monad `F` itself has a stack-safe `flatMap` implementation** 
      (e.g., `IO` or `cats.Eval`).

### Complexity and Alternative: Extension Methods

The `step` method signature has become extremely complex due to the need to:

- Preserve covariance.
- Introduce parameters with lower bounds.
- Require an implicit `Monad`.

**Alternative Solution: Extension Method (Extension Method)**

```dotty
// The definition becomes simpler, as there is no need to introduce F2, O2, R2
def step(using F: Monad[F]): F[Either[R, (O, Pull[F, O, R])]] =
  self match
    ... // implementation is the same as above
```

- **Advantage:** Much simpler and more readable type signature. No need for complex parameters with bounds.
- **Disadvantage:** Slight degradation of type inference. 
  The compiler may find it harder to infer concrete types at call sites, especially with complex transformation chains.

### Summary

Adding effects drastically increased the complexity of core primitives like `step`. 
Code moved from the world of pure recursive functions into the world of monadic computations. 
This is the price of expressiveness and the ability to describe data sources and effects directly inside the stream. 
Critically important for correct operation is the requirement that monad `F` must be stack-safe.

## Adapting `fold`, `toList`, and `uncons` Operations

Adding effects turns previously simple tail-recursive operations into monadically-recursive ones.

### `fold` Method

```dotty
// Returns an effectful action whose result is a tuple (FinalResult, Accumulator)
def fold[F2[x] >: F[x], R2 >: R, A](init: A)(f: (A, O) => A)(
  using F: Monad[F2]
): F2[(R2, A)] = 
  // Execute a step. The result (F[Either[...]]) is wrapped in flatMap
  step.flatMap: 
    // If the stream completed, return the current accumulator and result
    case Left(r)         => F.unit((r, init)) 
    // If there's an element, update the accumulator and recursively continue folding the tail
    case Right((hd, tl)) => tl.fold(f(init, hd))(f) 
```

- **Change:** The method can no longer be marked as `@tailrec`. 
  Recursion now occurs _inside_ the `flatMap` chain of monad `F2`.
- **Stack safety:** Now fully depends on the implementation of `flatMap` in monad `F2`. 
  If `F2.flatMap` is stack-safe (e.g., uses [trampolining][trampolining]), 
  then `fold` will also be stack-safe.

### `toList` Method

```dotty
def toList[F2[x] >: F[x]: Monad, O2 >: O]: F2[List[O2]] =
  // Use Builder for efficient List construction
  fold(List.newBuilder[O])((bldr, o) => bldr += o) 
    // After folding, we get F2[(R, Builder)]. 
    // Take the second element of the tuple and call .result
    .map(_(1).result) 
```

`toList` is built on `fold`, using `List.newBuilder` for efficient element accumulation.

### The Problem with `uncons` Operation and Its Solution

`uncons` is a fundamental operation that "deconstructs" a stream into head and tail or detects its completion.

**Naive Approach (Problematic)**

The first attempt at adaptation is similar to the pure version:

```dotty
// NON-WORKING variant
def uncons[F2[x] >: F[x], O2 >: O, R2 >: R](
  using Monad[F2]
): Pull[F2, Nothing, Either[R2, (O2, Pull[F2, O2, R2])]] =
  Eval(step) // Try to wrap the step call in an effectful Eval node
```

**Problem:** It imposes a `Monad[F2]` constraint even on _constructing_ the `Pull` value. 
This makes it impossible to call `uncons` for a Pull without effects (where `F = Nothing`), 
since there is no instance of `Monad[Nothing]`. This breaks polymorphism.

**Solution: Introducing a New Constructor `Uncons`**

To defer the monad requirement to the moment of _interpretation_ (execution of `step`), 
rather than _construction_, we introduce a new data constructor:

```dotty
// New case in enum Pull
case Uncons(source: Pull[F, O, R])
  extends Pull[F, Nothing, Either[R, (O, Pull[F, O, R])]]
```

- **Semantics:** `Uncons` is a promise that someday later (when `step` is called) we will deconstruct `source` into parts.
- **Types:** The constructor produces no output elements (`Nothing`), 
  and its result is `Either[R, (O, Pull[F, O, R])]`, describing the possible deconstruction results.
- **Deferred execution:** The requirement for `Monad[F]` appears only in the `step` method, when it encounters this constructor.

Now we can define the `uncons` method without constraints:

```dotty
def uncons: Pull[F, Nothing, Either[R, (O, Pull[F, O, R])]] =
  Uncons(this) // Simply wrap the current Pull in the Uncons constructor
```

### Handling `Uncons` in the `step` Method

The `step` method must be extended to handle the new constructor:

```dotty
def step[F2[x] >: F[x], O2 >: O, R2 >: R](
  using F: Monad[F2]
): F2[Either[R2, (O2, Pull[F2, O2, R2])]] =
  this match
    // ... other cases (Result, Output, Eval, FlatMap) ...
    case Uncons(source) =>
      source.step.map(s => Left(s.asInstanceOf[R2]))
```

**Explanation:** When `step` encounters `Uncons(source)`, it:

1. Recursively calls `source.step`. This is where `Monad[F2]` is required.
2. The result of `source.step` has type `F2[Either[R, (O, Pull[F, O, R])]]`.
3. Using `.map` we "extract" `Either` from context `F2` and wrap it back in `Left`. 
   _Note on type:_ Here a cast `s.asInstanceOf[R2]` is required, 
   which is safe due to covariance and lower bound type `R2 >: R`. 
   The step result of `source` (`R`) is always a subtype of `R2`.

### Summary and Consequences

1. **Deferred Constraints:** Introducing `Uncons` allows separating _construction_ of stream transformations 
   (which remains polymorphic and without constraints) from their _interpretation_ 
   (where trait instances like `Monad` are required).
2. **Extensibility:** Most other operations over `Pull` and `Stream` (such as `map`, `filter`, `flatMap`) 
   carry over with minimal changes, often requiring only adding effect parameter `F` to their signatures.
3. **Power:** `Pipe[F, I, O]` now naturally defines as `Stream[F, I] => Stream[F, O]`, 
   and these pipes can themselves contain effects of type `F`, 
   making the entire system extremely expressive for describing real data processing pipelines with `IO`.

## `Stream` Type

The `Stream` type remains an alias for `Pull`, but now inherits its expanded parameterization:

```dotty
opaque type Stream[+F[_], +O] = Pull[F, O, Unit]
```

This definition means:

- `Stream[F, O]` is a stream of elements of type `O`.
- For execution, the stream can use arbitrary effects, 
  described by type constructor `F[_]` (e.g., `IO`, `Option`, `TailRec`).
- The final result (`R`) of the execution of a stream is always `Unit`, 
  since the main purpose of a stream is producing elements, not computing a final value.

All constructors and methods of `Stream` were mechanically adapted to account for the new type parameter `F`.

### Constructors

```dotty
type Nothing1[A] = Nothing

object Stream:
  // #1: Empty stream without effects
  def empty: Stream[Nothing1, Nothing] = Pull.done

  // #2: Stream from a variable number of arguments (no effects)
  def apply[O](os: O*): Stream[Nothing1, O] =
    fromList(os.toList)

  def fromList[O](os: List[O]): Stream[Nothing1, O] =
    os match
      case Nil => Pull.done
      case hd :: tl => Pull.Output(hd) >> fromList(tl) // Recursive construction
```

Key point: constructors that create streams from pure data fix the effect type to `Nothing1`, 
indicating no need for effects for their generation.

### Extension Methods

Methods that "deconstruct" or "interpret" a stream now return a value wrapped in effect context `F`.

```dotty
extension [F[_], O](self: Stream[F, O])
  def toPull: Pull[F, O, Unit] = self // Simple type cast

  // #3: Folding stream elements. Returns an effectful action F[A].
  def fold[A](init: A)(f: (A, O) => A)(using Monad[F]): F[A] =
    self.fold(init)(f).map(_(1)) // Calls fold on Pull and extracts the accumulator

  // #4: Converting a stream to a List. Returns an effectful action F[List[O]].
  def toList(using Monad[F]): F[List[O]] =
    self.toList // Delegates to the Pull toList method   
```

**Important:** These methods require an implicit `Monad[F]` instance. 
This is necessary so that stream steps can be sequentially executed within context `F`.

### Working With Effect-Free Streams

Streams with `F = Nothing` (pure, effect-free streams) remain very useful. 
However, for their interpretation, you now need to explicitly specify _in which monad_ stack-safe interpretation should occur.

```dotty
// Previously: 
Stream(1, 2, 3).repeat.take(5).toList
// Now:
val x: List[Int] =
  Stream(1, 2, 3)
    .repeat  // #1: Infinite repetition (lazy)
    .take(5)
    .toList(using tailrecMonad) // Explicitly specify tailrec monad Trampoline
    .result // Execute Trampoline computation to get a pure result
// val x: List[Int] = List(1, 2, 3, 1, 2)
```

Here `tailrecMonad` is a [TailRec monad][trampolining], 
which ensures stack-safe recursion through transformation into a loop:

```dotty
given tailrecMonad: Monad[TailRec] with
  def unit[A](a: => A) = TailCalls.done(a)

  extension [A](fa: TailRec[A])
    def flatMap[B](f: A => TailRec[B]) =
      fa.flatMap(f)
```

### Convenient Extension Methods for `Stream[Nothing, O]`

To avoid the need for explicitly specifying a monad for pure streams, special extension methods can be created:

```dotty
extension [O](self: Stream[Nothing, O])
  // Effect-free folding. 
  // Internally uses the TailRec monad for safety and extracts the result.
  def fold[A](init: A)(f: (A, O) => A): A =
    self.fold(init)(f)(using tailrecMonad).result(1) // .result "runs" the Trampoline

  // Effect-free conversion to List.
  def toList: List[O] =
    self.toList(using tailrecMonad).result // Analogously

// Usage example:
val s = Stream(1, 2, 3).repeat.take(5)
// Type inferred as Nothing for effect
// val s: Stream[Nothing, Int] = ... 

// #1: Call our new extension method
val x = s.toList 
// Get a pure result immediately
// val x: List[Int] = List(1, 2, 3, 1, 2) 

// But if the stream has effects, the standard method is called
val y = (s: Stream[IO, Int]).toList // #2: Cast type to a stream with effects
val y: IO[List[Int]] = ... // Returns an IO action, not the List itself
```

**Summary:** The separation between pure and effectful streams becomes more explicit at the type level. 
This ensures type safety: the compiler will not allow accidentally executing an effectful action 
without specifying the correct interpretation context. 
Convenient methods are provided for pure streams, hiding the stack-safe execution mechanism under the hood.

### Exercise 11.4.1: Implementing the `eval` Constructor for `Stream`

> [Repository with exercises and instructions for completion][sample]

**Task:**

Implement method `eval` in the companion object `Stream`, which creates a stream of one element 
obtained by executing effect `F[O]`. 
The method should have the following signature:

```dotty
object Stream:
  def eval[F[_], O](fo: F[O]): Stream[F, O]
```

**Requirements:**

- The method must return a `Stream[F, O]` containing exactly one element
- The element must be obtained by executing the passed effect `fo`
- The implementation must use the existing primitives `Pull.Eval` and `Pull.Output`

**Usage Example:**

```dotty
// Create an effect that returns a random number
val randomNumber: IO[Int] = IO(scala.util.Random.nextInt(100))

// Create a stream from the effect
val stream: Stream[IO, Int] = Stream.eval(randomNumber)
      
// Run the stream and get the result
val result = stream.toList.unsafeRunSync()

// Result will contain List(randomNumber). For example:
// val result: List[Int] = List(91)
```

@:note(🔍 Show Solution)

Method `eval` creates a stream of one element by:

1. First executing effect `Pull.Eval(fo)` — this creates a `Pull` that executes effect `fo`
2. Then the result of the effect is passed to `Pull.Output(_)` via `flatMap`
3. `Pull.Output` wraps the result in a stream output element
4. The combination of these operations creates a `Stream` that, on execution:
    - Will execute effect `fo`
    - Will emit the obtained value `O`
    - Will complete the stream

```dotty
def eval[F[_], O](fo: F[O]): Stream[F, O] =
  Pull.Eval(fo).flatMap(Pull.Output(_))    
```

@:@

### Exercise 11.4.2: Implementing Method `mapEval` for `Stream`

**Task:**

Implement method `mapEval` as an extension method for type `Stream[F, O]`. 
The method should apply an effectful function to each element of the stream and combine the results into a new stream.

```dotty
extension [F[_], O](self: Stream[F, O])
  def mapEval[O2](f: O => F[O2]): Stream[F, O2]
```

**Requirements:**

- The method should take a function `f: O => F[O2]` that transforms an element into an effect
- For each element of the input stream, effect `f(o)` should be executed
- The results of executing effects should be collected into the output stream in the same order
- The implementation must be lazy — effects are executed only upon stream execution
- Error and resource handling must be supported within effect `F`

**Usage Example:**

```dotty
// Asynchronous element transformation
case class User(id: Int, name: String)

// Function to load additional user data
def loadUserDetails(user: User): IO[String] = IO {
  // Simulate an async API call
  s"Details for ${user.name} (ID: ${user.id})"
}

val users = Stream(
  User(1, "Alice"),
  User(2, "Bob"),
  User(3, "Charlie")
)

val userDetailsStream: Stream[IO, String] =
  users.mapEval(loadUserDetails)

// Result: Stream with details for each user
```

@:note(🔍 Show Solution)

Method `mapEval` is implemented by combining existing primitives:

1. `Stream.flatMap` is used to process each element of the input stream
2. For each element `o`, function `f(o)` is applied, which returns effect `F[O2]`
3. `Stream.eval` converts effect `F[O2]` into a stream with one element
4. `flatMap` combines all these streams into one resulting stream

```dotty
extension [F[_], O](self: Stream[F, O])
  def mapEval[O2](f: O => F[O2]): Stream[F, O2] =
    Stream.flatMap(self)(o => Stream.eval(f(o)))    
```

@:@

### Exercise 11.4.3: Implementing `unfoldEval` Generators for `Stream` and `Pull`

**Task:**

Implement generative methods `unfoldEval` in the companion objects `Stream` and `Pull`. 
These methods create streams and pulls by sequentially applying an effectful function to an initial state.

```dotty
// For Stream - generates a stream of elements until the function returns Some
object Stream:
  def unfoldEval[F[_], O, R](init: R)(f: R => F[Option[(O, R)]]): Stream[F, O]

// For Pull - generates elements until the function returns Right, 
// either completes with a result when the function returns Left
object Pull:
  def unfoldEval[F[_], O, R](init: R)(f: R => F[Either[R, (O, R)]]): Pull[F, O, R]
```

**Differences Between Versions:**

- `Stream.unfoldEval`: uses `Option` for controlling completion (`None` = end of stream)
- `Pull.unfoldEval`: uses `Either` for controlling completion (`Left` = completion with a result)

**Requirements:**

1. **Lazy execution:** Effects must be executed only upon stream/pull execution
2. **Sequential processing:** State must be passed sequentially between iterations
3. **Correct completion:** Proper handling of completion conditions must be ensured
4. **Effectfulness:** Support for effects of type `F[_]` at each generation step

**Usage Example of `Pull.unfoldEval`:**

```dotty
// Generation with an explicit completion result
def nextWithResult(state: Int): IO[Either[Int, (String, Int)]] =
  IO {
    if state < 3 then Right((s"Current state: $state", state + 1))
    else Left(state)
  }

val pullWithResult: Pull[IO, String, Int] =
  Pull.unfoldEval[IO, String, Int](0)(nextWithResult)
pullWithResult.toList.unsafeRunSync()
// val res0: List[String] = List(Current state: 0, Current state: 1, Current state: 2)
```

**Usage Example of `Stream.unfoldEval`:**

```dotty
// Generating a sequence of numbers with effectful logic
def nextNumber(state: Int): IO[Option[(Int, Int)]] = IO {
  if state < 5 then Some((state * 2, state + 1))
  else None
}

val numberStream: Stream[IO, Int] =
  Stream.unfoldEval(0)(nextNumber)
// Result: Stream(0, 2, 4, 6, 8)
```

@:note(🔍 Show Solution)

**Pull.unfoldEval:**

```dotty
object Pull:
  def unfoldEval[F[_], O, R](init: R)(f: R => F[Either[R, (O, R)]]): Pull[F, O, R] =
    Pull.Eval(f(init)).flatMap:
      case Left(result) => 
        // Complete with result
        Pull.Result(result)
      case Right((output, nextState)) => 
        // Emit element and continue recursively
        Pull.Output(output) >> unfoldEval(nextState)(f)
```

**Step-by-step execution of `Pull.unfoldEval`:**

1. Execute effect `f(init)` → get `F[Either[R, (O, R)]]`
2. If `Left(result)` → complete with result `result`
3. If `Right((o, r))` → emit `o` and recursively continue with state `r`

**Stream.unfoldEval:**

```dotty
object Stream:
  def unfoldEval[F[_], O, R](init: R)(f: R => F[Option[(O, R)]]): Stream[F, O] =
    Pull.Eval(f(init)).flatMap:
      case None => 
        // Complete the stream
        Stream.empty
      case Some((output, nextState)) => 
        // Emit element and continue recursively
        Pull.Output(output) ++ unfoldEval(nextState)(f)
```

**Step-by-step execution of `Stream.unfoldEval`:**

1. Execute effect `f(init)` → get `F[Option[(O, R)]]`
2. If `None` → return an empty stream
3. If `Some((o, r))` → emit `o` and recursively continue with state `r`

**Key Features:**

- Recursion is done via `flatMap`, ensuring lazy execution
- State is passed iteratively between calls
- Effects are executed sequentially in generation order
- Proper resource and error handling is supported

@:@

## Updating the `Pipe` Definition

With the addition of effect support in `Stream`, the `Pipe` type (transformation channel) definition must also be updated:

```dotty
type Pipe[F[_], -I, +O] = Stream[F, I] => Stream[F, O]
```

**Key Features of the New Definition:**

- **Effect Parameter `F[_]`:** The channel now requires both input and 
  output streams to work in the same effect context `F`.
- **Invariance of `F`:** Parameter `F` is declared invariant. 
  Attempting to make it covariant (`+F[_]`) or contravariant (`-F[_]`) will cause compiler errors, 
  as `F` is used in both a contravariant position (`Stream[F, I]` as a function argument) 
  and a covariant position (`Stream[F, O]` as the return type).
- **Alternative (more expressive) definition:** 
  Theoretically, `Pipe` could be defined with two effect parameters for greater flexibility:
  ```dotty
  type Pipe[-F[_], -I, +G[_], +O] = Stream[F, I] => Stream[G, O]
  ```
  This would allow a channel not only to transform elements, but also to change the effect type itself 
  (e.g., transform `Stream[IO, I]` to `Stream[Option, O]`). 
  However, this would significantly complicate the type and its usage. 
  Since `Pipe` is just a type alias, for complex cases you can always use an explicit function, 
  and for most standard scenarios, the simpler variant with a single `F` is sufficient.

### Sinks in Effectful Streams

If a data source is represented as `Stream[F, I]`, and a transformation as `Pipe[F, I, O]`, 
how should a _sink_ — the terminal point that consumes elements — be represented?

It turns out no special type is needed for a sink! 
A sink is simply a special kind of `Pipe` that transforms an input stream into a stream that:

1. Produces side effects for each element (e.g., writing to a file, sending over the network).
2. Produces no meaningful output elements (its output type is `Unit` or `Nothing`).

This logic can be easily implemented using the `mapEval` method (which applies an effectful function to each element).

```dotty
def mapEval[O2](f: O => F[O2]): Stream[F, O2] =
  Stream.flatMap(self)(o => Stream.eval(f(o)))
```

#### Sink Examples

```dotty
// A sink that logs each element to the console
def log[O]: Pipe[IO, O, Unit] =
  // For each element `o` an IO action for printing is created
  _.mapEval(o => IO(println(o))) 

// A sink that simply consumes (ignores) all stream elements
def drain[F[_], O]: Pipe[F, O, Nothing] =
  // Each element is replaced with an empty stream
  _.flatMap(_ => Stream.empty) 
```

- **`log`:** Type `Pipe[IO, O, Unit]` says: "Take a stream of elements `O` with effects `IO` and return a stream `Unit` with the same effects". 
  The side effect (printing) occurs inside `IO`.
- **`drain`:** Type `Pipe[F, O, Nothing]` says: "Take a stream with any effects `F` 
  and return a stream that produces no elements (`Nothing`)". This is pure consumption.

#### Building a Full Pipeline

Now we can assemble the entire pipeline: source → transformer → sink.

```dotty
// Suppose we have a data model and a message source from a queue (an effectful action)
case class Message(id: UUID, timestamp: Instant, ...)
def format(m: Message): String = s"id: $id, timestamp: $timestamp, ..."

// Effectful action: get the next message from the queue
val dequeue: IO[Message] = ... 

// Assemble the stream: repeat the dequeue action, 
// format each message, send to log
val logAll: Stream[IO, Unit] =
  Stream.eval(dequeue) // Create a stream of one element by executing dequeue
    .repeat // Infinite repetition of the previous stream (get an infinite stream of messages)
    .map(format) // Pure transformation: Message -> String
    .pipe(log)   // Apply the logging sink (transformation with effect: String -> Unit)

// Result type: Stream[IO, Unit]
```

### Running an Effectful Stream: The `run` Method

We have a stream `Stream[IO, Unit]`. We do not want to accumulate its results into a List (`.toList`), 
as we are interested in the side effects (logging), not the `Unit` values themselves.

We need a new method that simply "runs" the stream, executing all effects and ignoring output elements.

```dotty
extension [F[_], O](self: Stream[F, O])
  // Run the stream, executing all effects and ignoring output elements.
  def run(using Monad[F]): F[Unit] =
    // Fold the stream, ignoring elements and the accumulator
    self.fold(())((_, _) => ()).map(_(1)) 

// Now we can "run" our pipeline
val program: IO[Unit] = logAll.run
```

**How `run` works:**

1. `self.fold(())((_, _) => ())` runs a stream fold. The initial accumulator value is `()`. 
   Function `f` simply ignores both the accumulator and the current element, returning `()`. 
   This call returns `F[(Unit, Unit)]` — an effectful action whose result is the tuple `(final_result, accumulator)`. 
   For `Stream`, `final_result` is always `Unit`.
2. `.map(_(1))` takes the result of this action (the tuple) and uses `map` to extract its second element — 
   the accumulator, which is also `Unit`. The result is `F[Unit]`.

Now `program` represents a value of type `IO[Unit]` — a description of the entire logging program, 
which can be executed at the "run point" (via `unsafeRunSync` or similar).

---

**Links:**

- [Link to the repository with exercises][sample]
- [Functional Programming in Scala, Second Edition, Chapter 15](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
[monad]: ../../typeclass/monad/monad.md
[trampolining]: ../external-effects/stack_overflow_error.md
