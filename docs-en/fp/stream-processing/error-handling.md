# Error Handling in Functional Streams

In functional programming, we strive to make side effects and errors explicit 
and manage them using type classes and combinators.

## Basic `Task` Type: Deferred Computations with Errors

At the foundation is the `Task[A]` type, which represents a **deferred (lazy) computation** 
that, when executed, can either successfully return a value of type `A` or complete with an exception (`Throwable`).

**Implementation:** `Task[A]` is implemented as a wrapper around `IO[Try[A]]`.

- `IO[_]` is responsible for deferring and possible side effects.
- `Try[_]` (which can be `Success(value)` or `Failure(exception)`) is responsible for explicitly storing either the result or an error.

**Key Idea:** `Task` allows working with computations that may complete with errors 
using standard combinators (e.g., `map`, `flatMap`) as if errors do not exist, 
until you explicitly decide to handle them.

**Example Breakdown:**

```dotty
// Creating a Task that will throw a NumberFormatException on execution
val a: Task[Int] = Task("asdf".toInt) 

// `map` is applied to the "success path". Since `a` will fail, `map` is ignored.
val b: Task[Int] = a.map(_ + 1) 

// `attempt` — a combinator that "extracts" the result from the monadic context.    
// Now we have not `Task[Int]`, but `Task[Try[Int]]`. 
// This means: "a computation that is guaranteed to complete successfully, returning either success or failure". 
val c: Task[Try[Int]] = b.attempt                       
  
// Execute the computation. `d` will contain `Failure(NumberFormatException)`.                           
val d: Try[Int] = c.unsafeRunSync(pool) 
```

**Analogy with `Try` and `Either`:** `Task` can be viewed as an asynchronous, lazy analog of `Try`. 
It adds an "error channel" on top of `IO`, just as `Try` adds it on top of regular computations.

## `MonadThrow` Type Class: Unified Interface for Errors

To abstract from the specific type of effect (`Task`, `IO`, `SyncTask`, etc.) 
that may contain errors, we introduce the [MonadError](../../typeclass/monad/monad-error.md) type class.

**Purpose:** Describes the capabilities of a [monad](../../typeclass/monad/monad.md) `F`, which can also:

1. Create a computation that definitely completes with an error (`raiseError`).
2. Allow extracting the result as `Try` (`attempt`).
3. Handle errors, transforming them into a new effect (`handleErrorWith`).

**Definition:**

```dotty
trait MonadThrow[F[_]] extends Monad[F]:
  // "Raise" an exception into the effect context F
  def raiseError[A](t: Throwable): F[A]

  // Extract the result from the effect: convert F[A] to F[Try[A]]
  extension [A](fa: F[A])
    def attempt: F[Try[A]]

    // Handle an error. Similar to `flatMap`, but for the error channel.
    def handleErrorWith(h: Throwable => F[A]): F[A] =
      attempt.flatMap {
        // If there was an error, run the handler
        case Failure(t) => h(t)
        // If success, return it back into context F
        case Success(a) => unit(a)
      }
```

## Adding Error Handling to `Stream` and `Pull`

The stream library is built on two main types:

- `Stream[F, O]` — a sequence of output values `O`, created using effects `F`.
- `Pull[F, O, R]` — a lower-level instruction describing the process of generating a stream or completing with result `R`.

To allow streams to handle errors as well, a new `case Handle` is added to `Pull`:

```dotty
enum Pull[+F[_], +O, +R]:
  // New constructor: source + error handler
  case Handle(
      // Original Pull
      source: Pull[F, O, R],
      // Function called in case of an error in `source`
      handler: Throwable => Pull[F, O, R]
  ) extends Pull[F, O, R]

  // Combinator for convenient usage
  def handleErrorWith[F2[x] >: F[x], O2 >: O, R2 >: R](
      handler: Throwable => Pull[F2, O2, R2]
  ): Pull[F2, O2, R2] =
    Pull.Handle(this, handler)
```

Then this combinator is lifted to the `Stream` level via an extension method.

**Important Consequence:** The stream interpretation algorithm (`step`) now **requires** 
that the effect type `F` has a `MonadThrow[F]` instance, not just `Monad[F]`. 
This is because during the interpretation of `step`, errors that may arise 
during the execution of effects `F` must be caught and handled.

### General Purpose of the `step` Method for `Pull.Handle`

The `step` method is an interpreter (or state machine). Its task is:
**Execute one step of the `Pull` computation and return the result of that step.**
The result can be one of three variants, wrapped in effect `F2`:

1.  `Left(r)` — `Pull` completed (not necessarily due to an error!) with final result `R2`.
2.  `Right((o, next))` — `Pull` emitted an output value `o` and the next state `next` (remaining `Pull`).
3.  _Implicitly:_ An error may occur in effect `F2` (if `F2` is `Task`, an exception may be thrown).

The `Handle(source, handler)` constructor says: "Interpret `source`, 
and if an error arises during its interpretation, catch it and run `handler(error)`."

#### Detailed Breakdown of the Implementation

Let's break the code down piece by piece:

```dotty
def step[F2[x] >: F[x], O2 >: O, R2 >: R](
  using F: MonadThrow[F2]
): F2[Either[R2, (O2, Pull[F2, O2, R2])]] =
  this match
    ...
    case Handle(source, f) =>
      source match
        case Handle(s2, g) =>
          s2.handleErrorWith(x =>
            g(x).handleErrorWith(y => f(y))).step
        case other =>
          other.step
            .map:
              case Right((hd, tl)) => Right((hd, Handle(tl, f)))
              case Left(r) => Left(r)
            .handleErrorWith(t => f(t).step)
```

#### Part 1: Handling Nested Handlers (Left-associated Handle)

```dotty
case Handle(source, f) => // this is Handle(source, f)
  source match // Look at what's in `source`
    case Handle(s2, g) => // If the source ITSELF is a handler
      s2.handleErrorWith(x => g(x).handleErrorWith(y => f(y))).step
```

**Problem:** We have a construction like `Handle(Handle(s2, g), f)`. This is like nested `try-catch` blocks.

**Goal:** Rewrite left-associated handlers in a right-associated manner. 
This is a standard practice in functional libraries to ensure stack safety and predictable semantics.

**Analogy:**

Original code: `try { try { s2 } catch { case x => g(x) } } catch { case y => f(y) }`
We want to transform it into one big `try` with two handlers: 
`try { s2 } catch { case x => try { g(x) } catch { case y => f(y) } }`

**How it works:**

1. Take the innermost source `s2`.
2. Say: "If error `x` occurs in `s2`:
    1. First, pass it to the _inner_ handler `g(x)`.
    2. But the computation `g(x)` itself may fail with error `y`!
    3. If `g(x)` fails, pass error `y` to the _outer_ handler `f(y)`.
3. Apply `.step` to the result of this transformation to interpret the new, rewritten `Pull`.

**Result:** This case ensures that nested `Handle`s are processed in the correct order: 
first the inner one (`g`), then the outer one (`f`).

#### Part 2: Handling a "Plain" Source

This is the main and most frequent case, when `source` is not a `Handle`, but any other `Pull` constructor 
(e.g., `Output` or `Eval`).

```dotty
case other => // source is not Handle, but something else (Output, Eval, etc.)
  other.step // (A) Execute a step of the original Pull
    .map: // (B) If the step completed SUCCESSFULLY in effect F2, we process the result...
      case Right((hd, tl)) => Right((hd, Handle(tl, f))) // ...success with output data
      case Left(r) => Left(r) // ...successful completion
    .handleErrorWith(t => f(t).step) // (C) If the step completed with an ERROR in effect F2
```

Let's break down what happens here step by step.

**Step A: `other.step`**

- We launch the interpretation of the original `Pull` (`other`).
- The return type is: `F2[Either[R2, (O2, Pull[F2, O2, R2])]]`.
- Key point: this `.step` call may complete **successfully** inside effect `F2` (returning `Left` or `Right`) 
  or **fail with an error** inside effect `F2` (e.g., in `Task` this will be `Failure(exception)`).

Further processing splits into two paths: **successful result** (processed in `.map`) 
and **error** (processed in `.handleErrorWith`).

**Step B: Success Path (Processing in `.map`)**

The `.map` method is applied to the _successful result_ inside `F2`.

```dotty
.map:
  case Right((hd, tl)) => Right((hd, Handle(tl, f)))
  case Left(r) => Left(r)
```

- **Case `Right((hd, tl))`:** The step of the original `Pull` emitted output data `hd` and the remainder `tl`.
    - **What we do:** We must emit data `hd` to the outside. But! Our error handler `f` has not been used yet. 
      We cannot simply discard it, because an error may occur on the _next_ step when interpreting `tl`.
    - **Solution:** We return data `hd` and **continue** wrapping. 
      We say: "The next state will be `Handle(tl, f)`", 
      meaning the remainder `tl` must be interpreted with the same handler `f`.
    - **Analogy:** We successfully read one element from a file. We return it, but say 
      that reading the next element must still be protected by the same `try-catch`.
- **Case `Left(r)`:** The original `Pull` completed successfully with result `r`.
    - **What we do:** Simply return this result `Left(r)`. 
      Handler `f` was not needed, as the completion was successful.

**Step C: Error Path (Processing in `.handleErrorWith`)**

The `.handleErrorWith` method intercepts an error if **Step A** failed with an error inside effect `F2`.

```dotty
.handleErrorWith(t => f(t).step)
```

- **`t: Throwable`** — this is the error that occurred during `other.step`.
- **`f: Throwable => Pull[F2, O2, R2]`** — this is the handler passed to the `Handle` constructor.
- **What happens:** We "fail" with error `t`. 
  Instead of letting the error propagate and crash the entire stream, 
  we catch it and say: "Instead of this error, execute computation `f(t)`".
  But `f(t)` returns us a new `Pull` (e.g., a placeholder stream or a logging stream). 
  We need not just return it, but **interpret it**.
  Therefore, we take the result of handler `f(t)` and immediately call `.step` on it.
- **Result:** An error that occurred during interpretation of `source` is intercepted and transformed into a computation 
  that is launched in its place. This computation, in turn, is also interpreted at this same step.

**Analogy:** This is the exact equivalent of a `catch` block in `try-catch`. In the `try` block we have `other.step`. 
If it fails, control passes to the `catch` block, where we execute `f(t).step`.

#### Summary of the Entire Algorithm

The `step` method for `Handle` does the following:

1. **Optimization:** Rewrites nested handlers from left associativity to right for correct and predictable processing.
2. **Interpretation:** Interprets the inner source (`source`).
3. **Result Processing:**
    - **Success with data:** Returns data and "pushes" the handler onto the tail of the computation.
    - **Successful completion:** Simply returns the completion result.
    - **Error:** Catches the error, passes it to the handler, and interprets the _handler's result_ 
      as the next state of the entire `Pull`.

This is an elegant and purely functional way to implement `try-catch` semantics 
for a complex composable data structure (`Pull`), using existing combinators from the `MonadThrow` type class.

## The `onComplete` Combinator

Based on `handleErrorWith` and `raiseError`, a useful high-level `onComplete` combinator can be built:

```dotty
enum Pull[+F[_], +O, +R]:
  ...

  case Error(t: Throwable) extends Pull[Nothing, Nothing, Nothing]
    
object Stream:
  def raiseError[F[_], O](t: Throwable): Stream[F, O] =
    Pull.Error(t)

  extension [F[_], O](self: Stream[F, O])
    def onComplete(that: => Stream[F, O]): Stream[F, O] =
      self.handleErrorWith(t => that ++ raiseError(t)) ++ that
```

**Semantics:** Execute stream `that` **after** the original stream `self` completes, 
**regardless of how it completed** (success or error). 
If `self` completed with an error, this error will be re-thrown _after_ `that` is executed.

### Resource Management Issues with `onComplete`

Does `onComplete` allow safely acquiring and releasing resources, such as file descriptors?

**Correct, but non-composable approach:**

```dotty
// Resource is acquired and used in one continuous stream
Stream.eval(acquire("file")).flatMap { resource =>
  use(resource)        // Stream that uses the resource
    .onComplete(Stream.eval(release(resource))) // Release at the end
}
// This works because the entire "lifecycle" of the resource is enclosed in one `flatMap`.
```

**Problematic approach (after refactoring):**

```dotty
Stream.eval(acquire("file")).flatMap { resource =>
  Stream.eval(Task(source.getLines))    // Part of using the resource
    .flatMap(itr => Stream.fromIterator(itr))
    .onComplete(Stream.eval(release(resource))) // Release here...
} // ...but flatMap ends, and resource `resource` goes out of scope!
.mapEval(line => Task(println(line)))   // ...but processing happens here, outside!
```

**Why this breaks:**

1. **Scope:** Variable `resource` exists only within the `flatMap` block. 
   The `onComplete` combinator captures it in a closure, but this is a dangerous pattern.
2. **Composition:** If the subsequent `.mapEval` throws an exception, the `onComplete` handler **will not be called**, 
   because it was part of an inner stream that already completed successfully, passing data further. 
   The error would occur "outside" the scope of `onComplete`.
3. **Early completion:** If `.take(10)` is placed after `onComplete`, the stream will complete after 10 elements, 
   and `onComplete` (and thus `release`) will never be computed.

**Conclusion:** `onComplete` is not the tool that provides **guaranteed** resource release. 
For this, more powerful mechanisms are needed, 
which manage the lifecycle directly and are composable.
We will examine this mechanism in the next chapter.

## Consequences for Eliminators (Eliminators)

Eliminators are functions like `runLog` or `compile.toList` that run a stream 
and convert it into a single final value in effect `F` (e.g., `F[List[O]]`).

**Key change:** After adding error handling, the signature of eliminators must change.

Previously it was:

```dotty
def toList(implicit F: Monad[F]): F[List[O]]
```

Now it is:

```dotty
def toList(implicit F: MonadThrow[F]): F[List[O]]
```

This means we will not be able to run `Stream[IO, Int]` unless a `MonadThrow` instance is defined for `IO`. 
This is a conscious trade-off: error-handling functionality requires stricter constraints on the effect type.

## Special Case: Streams Without Effects (`SyncTask`)

For testing or effect-free computations, streams can be interpreted into a special monad `TailRec` (for tail recursion). 
But `TailRec` itself cannot work with errors.

**Solution:** Create a new type `SyncTask[A]` that will represent `TailRec[Try[A]]`. 
For this type, a `MonadThrow` instance can be easily defined, "hiding" `Try` inside the `TailRec` computation. 
This allows using all the new error-handling functionality in effect-free mode as well.

```dotty
opaque type SyncTask[A] = TailRec[Try[A]]
```

## Summary

1. **Explicitness:** Errors are not exceptions, but part of the type (`Try` inside `Task`).
2. **Abstraction:** Type classes (`MonadThrow`) allow writing generalized error-handling code for any such monads.
3. **Composition:** Combinators (`handleErrorWith`, `onComplete`) are built from simple building blocks (`attempt`, `flatMap`) 
   and allow declaratively describing failure-handling logic.
4. **Trade-offs:** Adding new functionality (errors) tightens type requirements 
   (requires `MonadThrow` instead of `Monad`) and changes the API of eliminators.
5. **Caution with Resources:** Not every combinator is suitable for guaranteed resource management. 
   You must use the right abstractions (`bracket`/`Resource`).

---

**Links:**

- [Link to the repository with exercises][sample]
- [Functional Programming in Scala, Second Edition, Chapter 15](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
