# Resource Safety

## Introduction to the Problem

`Stream` (in the context of functional programming libraries such as **fs2** or **ZIO Streams**) — 
is not just a sequence of data. It is an abstraction over a _process_ that may consume resources. 
Resources in programming are everything that has limited availability and requires proper release:

- **Files:** File descriptors (the number of which is limited in the system).
- **Network connections:** Sockets.
- **Database connections:** Connection pools.
- **Input/output streams.**

If such resources are not closed, a leak occurs, which can lead to exhaustion of OS limits, 
`Too many open files` errors, and application performance degradation.

### Problem Analysis with a File Example

Imagine we have `lines: Stream[Task, String]`. Under the hood, this stream holds an open file descriptor.

**Goal:** Guarantee that the file descriptor is closed _regardless_ of how the stream work completes.

**Naive Approach (That Doesn't Work):**

```dotty
val fileStream: Stream[Task, String] = openFile("data.txt") // Assume this exists
val result: Task[List[String]] = fileStream.take(5).compile.toList // Take only 5 lines
```

The problem: after getting 5 lines, stream `take(5)` completes, but the interpreter does not know 
that it needs to call file closing, because the original `fileStream` was not exhausted (the file might have 1000 lines). 
The descriptor remains open.

### Taxonomy of Stream Completion

To properly handle resource closing, we need to account for all three scenarios of stream completion:

1.  **Normal Completion (Exhaustion):**
    - **What happens:** The stream reaches its natural end (e.g., reaches End Of File).
    - **Action:** The resource must be closed.

2.  **Early Completion (Interruption):**
    - **What happens:** The consuming stream decided it has enough data. Classic examples — `.take(n)`, `.find(p)`, `.takeWhile(p)`.
    - **Problem:** The source (`lines`) is not yet exhausted and is "ready" to emit more data, but no one else is listening.
    - **Action:** The resource must be closed _immediately_, not waiting until the end of the program.

3.  **Abnormal Completion (Error):**
    - **What happens:** An exception was thrown during stream processing (e.g., an error parsing a line from the file).
    - **Action:** The resource must be closed, _and only then_ propagate the error further down the chain. 
      This guarantees that a resource leak does not complicate debugging the main error.

### Guidelines for Resource Management

Let's formalize two fundamental rules that a `Stream` implementation must follow:

1.  **Producer Responsibility Principle:**
    - _"The producer must release all basic resources as soon as it knows it has no more values to produce."_
    - **Explanation:** The stream that owns the resource (our `lines`) knows itself how to close it (via function `release`). 
      It should take responsibility for its release at the moment its work is done (for any of the three reasons).

2.  **Interpreter Responsibility Principle:**
    - _"Any stream `s` that consumes values from another stream `t` 
      must guarantee that `t`'s cleanup actions are executed before `s` stops."_
    - **Explanation:** Operations like `take`, `flatMap`, or `compile.toList` do not merely transform data. 
      They manage the _interpretation_ of the stream. 
      If such an operation decides to stop consuming data from an upstream source (`t`), 
      it must signal to that source that it is time to perform cleanup, 
      before completing its own work.

## The `resource` Constructor

Instead of manually and不安全 combining `acquire` and `release` with `flatMap` and `handleErrorWith`, 
a high-level abstraction is proposed.

**Signature:**

```dotty
def resource[F[_], R](acquire: F[R])(release: R => F[Unit]): Stream[F, R]
```

- `acquire: F[R]` — the effect of acquiring the resource (e.g., `Task[File]`).
- `release: R => F[Unit]` — the function for releasing the resource (e.g., `f => Task(f.close())`).
- **Returns:** `Stream[F, R]` — a stream that emits exactly one element — the acquired resource `R`, 
  and guarantees execution of `release` upon completion.

**How this is used for reading a file:**

```dotty
def fileLines(path: String): Stream[Task, String] = {
  // 1. Create a Stream managing the File resource
  val fileResource: Stream[Task, File] = Stream.resource {
    Task(Source.fromFile(path)) // acquire
  } { source =>
    Task(source.close())        // release
  }

  // 2. "Extract" data from the resource, transforming File into Stream[String]
  fileResource.flatMap { file =>
    // Create a stream of lines from the file.
    // Now this inner stream is protected by the guarantee
    // that the release action will be called if its consumption stops.
    Stream.fromIterator(file.getLines())
  }
}

// Safe usage in any scenario

// Early completion
val first5: Task[List[String]] = fileLines("big.txt").take(5).compile.toList 
// Normal completion
val all: Task[List[String]] = fileLines("big.txt").compile.toList   
// Error         
val failed: Task[List[String]] = fileLines("big.txt").map(_ => throw Err).compile.toList 
```

In all three cases, the file will be guaranteed to be closed.

### Implementation Details (How This Might Work Internally)

The key idea — the stream interpreter must maintain a **"finalization register"** (finalization register) or stack of cleanup actions. 
This is an internal state, not directly accessible to the user.

1.  **Compilation:**
    - When the interpreter encounters `Stream.resource(acquire)(release)`, it executes effect `acquire`.
    - The acquired resource `R` is placed in the finalization register along with function `release`.
    - Resource `R` is emitted further down the stream.

2.  **Interpretation and Cleanup:**
    - On normal completion: the interpreter, upon reaching the end, inspects the register 
      and executes all `release` actions in LIFO order (last acquired — first released).
    - On early completion or error: the interpreter immediately stops processing subsequent values 
      and enters the _cleanup_ phase, executing all registered `release` actions.

3.  **Composition:**
    - If a new resource is opened inside `flatMap`, its `release` action is added to the stack. 
      This guarantees correct nesting: resources are closed in the reverse order of their opening.

> It's best not to get fixated on the implementation details of a specific library. 
> The concept of `Resource` as a monadic wrapper for a resource's lifecycle — 
> is an idiomatic and safe pattern in functional programming, 
> and its exact implementation (via `Bracket` in cats-effect, `ZManaged` in ZIO, etc.) may vary, but the semantics remain unchanged.

## The Concept of Scope

`Scope` is the central abstraction for managing the resource lifecycle in stream processing. 
You can think of `Scope` as a **execution context** that:

1. Tracks acquired resources
2. Guarantees their release in the correct order
3. Ensures safety even during interruptions and errors

Each scope can be in one of two states:

- **Open:**
    - Actively manages resources
    - May have registered finalizers
    - May contain nested subscopes
    - Ready for operations

- **Closed:**
    - Finished its work
    - All finalizers have already been executed
    - Does not accept new operations

Scopes form a tree structure:

```
Root Scope
├── Subscope 1 (finalizer A)
│   ├── Subscope 1.1 (finalizer B)
│   └── Subscope 1.2 (finalizer C)
└── Subscope 2 (finalizer D)
```

### Finalizer Registration

The `Scope` type must be parameterized by effect type `F`.

```dotty
// Conceptual interface
trait Scope[F[_]]:
  def openSubscope(finalizer: F[Unit]): F[Scope[F]]
  def close: F[Unit]
```

When opening a subscope, a finalizer is registered — an effect of type `F[Unit]` that will be executed upon scope closure.

### Scope Closure Process

Scope closure is performed in a strictly defined order:

1. **Recursive closure of subscopes:** First, all nested scopes are closed
2. **Execution of finalizers:** Then, the scope's own finalizer is executed
3. **LIFO order:** Resources are released in the reverse order of their acquisition

```dotty
// Pseudocode for the closure process
def closeScope(scope: Scope): F[Unit] =
  for
    _ <- scope.subscopes.traverse(closeScope) // Close all subscopes
    _ <- scope.finalizer                      // Execute the finalizer
  yield ()
```

## Implementing Ref: Functional Reference

In functional programming, you cannot simply use mutable variables. 
`Ref` provides a safe way to work with mutable state in the context of effects `F[_]`.

### Basic Structure

```dotty
final class Ref[F[_], A] private (
  // Low-level atomic storage
  underlying: AtomicReference[A],
  // Way to transform side effects into effect F
  delay: [A] => (() => A) => F[A]
):
```

### Key Methods

#### 1. `get: F[A]`

Reads the current value atomically:

```dotty
def get: F[A] = delay(() => underlying.get)
// Converts: () => A (side effect) to F[A] (effect)
```

#### 2. `set(a: A): F[Unit]`

Sets a new value atomically:

```dotty
def set(a: A): F[Unit] = delay(() => underlying.set(a))
```

#### 3. `modify[B](f: A => (A, B)): F[B]`

Atomically transforms the value and returns the result:

```dotty
def modify[B](f: A => (A, B)): F[B] = delay: () =>
  @annotation.tailrec
  def loop(): B =
    val oldA = underlying.get           // Read the current value
    val (newA, result) = f(oldA)        // Compute the new value and result
    if underlying.compareAndSet(oldA, newA) then
      result                            // Successfully updated
    else
      loop()                            // Retry on contention
      
  loop()
```

**How Compare-And-Set (CAS) Works:**

```java
// AtomicReference.compareAndSet(expect, update)
// "If current value == expect, set update and return true"
// Otherwise return false and retry the operation
```

### Creating a Ref

```dotty
object Ref:
  def apply[F[_], A](initial: A)(using F: Monad[F]): Ref[F, A] =
    new Ref(
      new AtomicReference[A](initial),
      [A] => (th: () => A) => F.unit(()).map(_ => th())
    )
```

Here `F.unit(())` creates a minimal effect, and `map(_ => th())` "hovers" the side effect into context `F`.

## Integrating Scope and Ref

Using `Ref` and `modify`, we can implement `Scope` as a state machine with states `Open` and `Closed`:

```dotty
class Scope[F[_]](
  parent: Option[Scope[F]],     // Reference to the parent scope
  val id: Id,                   // Unique scope identifier
  state: Ref[F, Scope.State[F]] // Atomic reference to the state
)(using F: MonadThrow[F]):      // Context: a monad with the ability to throw errors:
  import Scope.State

  // Interface methods
  def open(finalizer: F[Unit]): F[Scope[F]] = ???  // Creating a subscope
  def close: F[Unit] = ???                         // Closing the scope
```

### Scope State Model

```dotty
object Scope:
  // ADT (Algebraic Data Type) for representing state
  enum State[F[_]]:
    case Open(
      finalizer: F[Unit],         // Finalizer for this scope
      subscopes: Vector[Scope[F]] // Active subscopes
    ) extends State[F]

    case Closed() extends State[F] // Closed state
    
  def root[F[_]](using F: MonadThrow[F]): Scope[F[_]] =
    new Scope(None, new Id, Ref(State.Open(F.unit(()), Vector.empty)))
```

### Detailed Implementation of Method `open`

```dotty
def open(finalizer: F[Unit]): F[Scope[F]] =
  state  // Ref[F, State[F]]
    .modify:  // Atomic state modification
      // Case 1: Scope is open - can create subscopes
      case State.Open(myFinalizer, subscopes) =>
        // Create a new subscope
        val sub = new Scope(
          Some(this),           // Parent - the current scope
          new Id,               // New unique ID
          Ref(State.Open(finalizer, Vector.empty)) // Initial state
        )

        // Return: new state + an effect with the created subscope
        State.Open(myFinalizer, subscopes :+ sub) -> F.unit(sub)

      // Case 2: Scope is closed - try to create in the parent
      case State.Closed() =>
        val next = parent match
          case None =>
            // Root scope is closed - error
            F.raiseError(new RuntimeException("root scope already closed"))
          case Some(p) =>
            // Recursively try to create in the parent scope
            p.open(finalizer)

        // Return current state (remains Closed) + an effect for creation
        State.Closed() -> next
    .flatten  // Convert F[F[Scope[F]]] to F[Scope[F]]
```

### Detailed Implementation of Method `close`

```dotty
def close: F[Unit] =
  state
    .modify:
      // Case 1: Closing an open scope
      case State.Open(finalizer, subscopes) =>
        // Prepare a list of actions to execute:
        // 1. Close all subscopes (in reverse order)
        // 2. Execute the finalizer of the current scope
        val finalizers = (subscopes.reverseIterator.map(_.close) ++
          Iterator(finalizer)).toList

        // Function for safe execution of all finalizers
        def go(rem: List[F[Unit]], error: Option[Throwable]): F[Unit] =
          rem match
            case Nil =>
              // All finalizers executed
              error match
                case None => F.unit(())         // Success
                case Some(t) => F.raiseError(t) // There were errors

            case hd :: tl =>
              // Execute the next finalizer with error handling
              hd.attempt.flatMap: res =>
                go(tl, error orElse res.toEither.swap.toOption)

        // Transition to Closed state and return the close action
        State.Closed() -> go(finalizers, None)

      // Case 2: Scope is already closed - do nothing
      case State.Closed() =>
        State.Closed() -> F.unit(())
    .flatten
```

#### Critically Important Aspects of the `close` Implementation

##### 1. **Closure Order (LIFO)**

```dotty
subscopes.reverseIterator.map(_.close) ++ // First close subscopes
Iterator(finalizer)                       // Then the current finalizer
```

**Why `reverse`?** To observe the "last in — first out" principle.

##### 2. **Guarantee of Executing All Finalizers**

```dotty
hd.attempt.flatMap: res =>  // attempt: F[A] => F[Either[Throwable, A]]
  go(tl, error orElse res.toEither.swap.toOption)
```

**Error Handling Logic:**

- `attempt` converts a potentially failing effect into a safe one
- `error orElse ...` accumulates the first error but continues execution
- All finalizers are executed, even if some fail

##### 3. **Preserving the First Error**

```dotty
error orElse res.toEither.swap.toOption
```

Preserve the first error that occurred, but continue executing the remaining finalizers.

### Error Handling Features

#### "Upward Delegation" Strategy

For a closed scope, the `open` method delegates creation to the parent scope:

```dotty
case State.Closed() =>
  parent match
    case None    => F.raiseError(...) // Critical error
    case Some(p) => p.open(finalizer) // Recursive delegation
```

This creates an elegant mechanism where scopes can "ask" parent scopes to create resources when they are already closed.

### Completion Guarantee

The `close` method guarantees that:

1. All subscopes will be closed
2. All finalizers will be executed
3. The first error will be propagated, but will not stop the execution of other finalizers

This implementation ensures robust resource management in functional streams, 
fully consistent with the principles of functional programming and providing strict safety guarantees.

## Integrating Scope into Stream and Pull

Let's start by creating two new constructors in the `Pull` enum:

### Extending the Pull Algebra

#### New Pull Constructors

```dotty
// Creating a new scope
case OpenScope[+F[_], +O, +R](
  source: Pull[F, O, R],       // Computation inside the new scope
  finalizer: Option[F[Unit]]   // Finalizer for the new scope
) extends Pull[F, O, R]

// Working in an existing scope
case WithScope[+F[_], +O, +R](
  source: Pull[F, O, R],       // Computation inside the specified scope
  scopeId: Id,                 // Scope ID for execution
  returnScopeId: Id            // Scope ID to return to after completion
) extends Pull[F, O, R]
```

These new constructors imply that the interpreter (i.e., `fold` and `step`)
must track the active scope and allow `step` to change the current scope.
To allow changes to the active scope, we need to change the result of `step`, including the new active scope.

Let's introduce a new type representing the step result:

### Step Result Model

#### New StepResult Model

```dotty
enum StepResult[F[_], +O, +R]:
  // Completion of computation
  case Done(scope: Scope[F], result: R)

  // Emit an element and continue computation
  case Out(scope: Scope[F], head: O, tail: Pull[F, O, R])
```

**Why this is important:** Each execution step now explicitly indicates in which scope the computation should continue.

### Modified `fold` Algorithm

#### Updated Interpreter Structure

```dotty
def fold[F2[x] >: F[x], R2 >: R, A](init: A)(f: (A, O) => A)(
  using F: MonadThrow[F2]
): F2[(R2, A)] =
  // Create the root scope
  val scope = Scope.root[F2]

  // Recursive processing function
  def go(currentScope: Scope[F2], p: Pull[F2, O, R2], acc: A): F2[(R2, A)] =
    p.step(currentScope).flatMap:
      case StepResult.Done(_, r) =>
        // Computation completed, return the result
        F.unit((r, acc))

      case StepResult.Out(nextScope, hd, tl) =>
        // Element received, continue processing
        go(nextScope, tl, f(acc, hd))

  // Launch computation with the guarantee of scope closure
  go(scope, this, init).attempt.flatMap:
    case Success(result) =>
      scope.close.flatMap(_ => F.unit(result))
    case Failure(error) =>
      scope.close.flatMap(_ => F.raiseError(error))
```

### `step` Implementation for OpenScope

#### OpenScope Processing Logic

```dotty
case OpenScope(source, finalizer) =>
  // Create a new subscope with the finalizer
  val finalizerEffect = finalizer.getOrElse(F.unit(()))

  scope.open(finalizerEffect).flatMap: newSubscope =>
    // Execute source in the context of the new scope
    val wrappedSource = WithScope(source, newSubscope.id, scope.id)
    wrappedSource.step(newSubscope)
```

**Sequence of actions:**

1. Create a new subscope with the specified finalizer
2. Wrap the original computation in `WithScope`
3. Execute the computation in the context of the new scope

### `step` Implementation for WithScope

Modifying `step` for `WithScope` is a bit more complex.
First we search the scope tree for the target scope,
then use this scope for step-by-step execution of the original stream.
If the result is `Done`, then close the target scope and search for the scope to return to.
Otherwise, propagate `WithScope` over the remaining stream returned in `Out`:

```dotty
case WithScope(source, scopeId, returnScopeId) =>
  // Search for the target scope
  scope.findScope(scopeId).flatMap:
    case Some(targetScope) =>
      // Scope found, execute in it (targetScope)
      executeInScope(targetScope, closeAfterUse = true)
      
    case None =>
      // Scope not found, use the current one (scope)
      executeInScope(scope, closeAfterUse = false)

  private def executeInScope(execScope: Scope[F], closeAfterUse: Boolean): F[StepResult[F, O, R]] =
    source.step(execScope).attempt.flatMap:
      // Case 1: Successfully received an element
      case Success(StepResult.Out(resultScope, hd, tl)) =>
        F.unit(StepResult.Out(
          resultScope,
          hd,
          WithScope(tl, scopeId, returnScopeId) // Continue in the same scope
        ))

      // Case 2: Successful completion
      case Success(StepResult.Done(resultScope, r)) =>
        findReturnScope(resultScope).flatMap: returnScope =>
          if closeAfterUse then
            execScope.close.flatMap: _ =>
              F.unit(StepResult.Done(returnScope, r))
          else
            F.unit(StepResult.Done(returnScope, r))

      // Case 3: Execution error
      case Failure(error) =>
        if closeAfterUse then
          execScope.close.flatMap: _ => 
            F.raiseError(error)
        else
          F.raiseError(error)

  private def findReturnScope(currentScope: Scope[F]): F[Scope[F]] =
    currentScope.findScope(returnScopeId).map(_.getOrElse(currentScope))  
```

### Safety Guarantees

#### 1. Scope Closure Guarantee

```dotty
// All scopes are closed even on errors
.attempt.flatMap:
  case Success(result) => scope.close.flatMap(_ => F.unit(result))
  case Failure(error) => scope.close.flatMap(_ => F.raiseError(error))
```

#### 2. Correct Closure Order (LIFO)

```dotty
// WithScope guarantees closure in the correct order
case Success(StepResult.Done(resultScope, r)) =>
  findReturnScope(resultScope).flatMap: returnScope =>
    if closeAfterUse then
      execScope.close.flatMap(_ => F.unit(StepResult.Done(returnScope, r)))
```

#### 3. Error Handling in Finalizers

```dotty
// Finalizers are executed even on errors in the main computation
case Failure(error) =>
  if closeAfterUse then
    execScope.close.flatMap(_ => F.raiseError(error))
```

### Details of the Scope Search Mechanism

The `findScope` method is designed to find a `Scope` by its unique identifier in the scope tree. 
This is a critically important mechanism for:

1. **Resource management** — finding the scope in which a resource was registered
2. **Safety assurance** — correct closure of scopes in the proper order
3. **Support for nesting** — working with hierarchical scope structures

```dotty
final class Scope[F[_]](parent: Option[Scope[F]], val id: Id, state: Ref[F, Scope.State[F]])(using F: MonadThrow[F]):
  import Scope.State

  def findScope(target: Id): F[Option[Scope[F]]] = ...

  def findThisOrSubScope(target: Id): F[Option[Scope[F]]] = ...
```

#### Main Method `findScope`

```dotty
def findScope(target: Id): F[Option[Scope[F]]] =
  // 1. First search in the current scope and its subtree
  findThisOrSubScope(target).flatMap:
    case Some(s) =>
      // 2. If found - return success
      F.unit(Some(s))
    case None =>
      // 3. If not found - delegate search to the parent
      parent match
        case Some(p) => p.findScope(target)  // Recursive call
        case None    => F.unit(None)         // Reached the root - not found
```

#### Helper Method `findThisOrSubScope`

```dotty
def findThisOrSubScope(target: Id): F[Option[Scope[F]]] =
  // 1. Check the current scope
  if id == target then F.unit(Some(this))
  else
    // 2. If it doesn't match - search in subscopes
    state.get.flatMap: // Atomically read the state
      case State.Open(_, subscopes) =>
        // 3. Recursively search in each subscope
        def go(rem: List[Scope[F]]): F[Option[Scope[F]]] =
          rem match
            case Nil =>
              F.unit(None) // All subscopes checked - not found
            case hd :: tl =>
              hd.findThisOrSubScope(target).flatMap:
                case Some(s) =>
                  F.unit(Some(s)) // Found in the current subscope
                case None =>
                  go(tl)          // Continue searching in the remaining ones
        go(subscopes.toList) // Start traversing subscopes

      case State.Closed() =>
        F.unit(None) // Scope is closed - search is impossible      
```

## Implementing the `resource` Constructor

Thanks to the above changes, we can implement `resource` in terms of `Eval` and `OpenScope`:

```dotty
def resource[F[_], R](acquire: F[R])(release: R => F[Unit]): Stream[F, R] =
  // 1. Execute the resource acquisition effect
  Pull.Eval(acquire).flatMap(r =>
    // 2. Create a new scope with a finalizer
    Pull.OpenScope(
      // 3. Emit the acquired resource
      Pull.Output(r),
      // 4. Register the release function
      Some(release(r))
    )
  )
```

#### How This Works:

1. **Acquire Phase:** `Pull.Eval(acquire)` executes the effect for acquiring the resource
2. **Resource Emission:** `Pull.Output(r)` passes the resource downstream
3. **Finalizer Registration:** `Some(release(r))` registers the cleanup function
4. **Scope Isolation:** `OpenScope` creates an isolated scope for resource management

### Introducing the `scope` Operator for Early Completion

However, we still need to do one thing for handling early completion:
whenever we might potentially discard the rest of the stream, we must do so in a new scope.
This guarantees that all resources are allocated to subscopes,
and all scope subtrees will be closed at the earliest appropriate time.

Let's add to `Stream` an extension method `scope` that opens a new scope,
and then introduce such scopes for any operations
that may partially execute streams — such operations as `take`, `takeWhile`, and so on:

#### `scope` Method Implementation

```dotty
def scope: Stream[F, O] =
  // Create a new scope without a specific finalizer
  Pull.OpenScope(self, None)
```

#### Application in Early Completion Operations

```dotty
def take(n: Int): Stream[F, O] =
  // 1. Take n elements from the original stream
  // 2. Ignore the remainder (.void)
  // 3. Wrap in a new scope
  self.take(n).void.scope
```

#### Why This Is Important:

**Without `scope`:**

```dotty
// Problem: resources allocated after take(5) are not released
lines("big.txt").take(5).compile.toList
// File remains open!
```

**With `scope`:**

```dotty
// Solution: create an isolated scope for the take operation
lines("big.txt").take(5).scope.compile.toList
// All resources are properly released
```

#### Mechanism:

1. **Scope Creation:** `take(n)` creates a new scope
2. **Partial Execution:** The stream is executed up to the n-th element
3. **Scope Completion:** When `take` completes, its entire scope closes
4. **Resource Cleanup:** All resources allocated in the scope are released

## Complete Example: File Conversion with Guaranteed Safety

Putting all these pieces together, we can return to the file conversion example,
this time using the efficient version of `Stream`:

#### 1. Safe File Reading

```dotty
def file(path: String): Stream[Task, Source] =
  Stream.resource(
    // Acquire: open the file
    Task(Source.fromFile(path))
  )(
    // Release: guarantee closing
    s => Task(s.close())
  )

def lines(path: String): Stream[Task, String] =
  file(path).flatMap: source =>
    // Convert Source to a stream of lines
    Stream.eval(Task(source.getLines))
      .flatMap(Stream.fromIterator)
```

#### 2. Safe File Writing

```dotty
def fileWriter(path: String): Stream[Task, BufferedWriter] =
  Stream.resource(
    // Acquire: create a writer
    Task(Files.newBufferedWriter(Paths.get(path)))
  )(
    // Release: guarantee closing
    w => Task(w.close())
  )

def writeLines(path: String): Pipe[Task, String, Unit] =
  lines =>
    fileWriter(path).flatMap: writer =>
      // For each line, execute a write
      lines.mapEval(line => Task {
        writer.write(line)
        writer.newLine()
      })
```

#### 3. Transformation Pipeline

```dotty
val conversion: Pipe[Task, String, String] =
  trimmed andThen      // Remove whitespace
  nonEmpty andThen     // Ignore empty lines
  nonComment andThen   // Ignore comments
  asDouble andThen     // Parse to Double
  toCelsius andThen    // Convert to Celsius
  toString             // Convert back to String
```

#### 4. Full Integration

```dotty
def convert(inputFile: String, outputFile: String): Task[Unit] =
  lines(inputFile)                // Safe reading
    .pipe(conversion)             // Apply transformations
    .pipe(writeLines(outputFile)) // Safe writing
    .run                          // Execute the computation
```

### Key Advantages of the Architecture

#### 1. **End-to-End Resource Safety**

```dotty
// All resources are automatically closed
convert("input.txt", "output.txt")
// Even on errors in the middle of processing
```

#### 2. **Composability**

```dotty
// Operations are easily combined
val processing = lines("data.txt")
  .take(1000)        // Early completion is safe
  .filter(_.nonEmpty)
  .map(_.toUpperCase)
```

#### 3. **Scope Isolation**

```dotty
// Each operation works in its own scope
val result = processing
  .scope  // Isolate the entire chain
  .compile
  .toList
```

#### 4. **Finalizer Execution Guarantees**

- ✅ LIFO closure order
- ✅ Error handling in finalizers
- ✅ Atomicity of operations
- ✅ Thread-safe execution

### Under the Hood: How Scope Works

#### Hierarchy of Scopes for `take(5)`

```
Root Scope
└── Take Scope (created by take(5).scope)
    ├── Resource Scope 1 (input.txt file)
    ├── Resource Scope 2 (temporary buffer)
    └── ... (other resources)
```

#### Closure Process:

1. **`take(5)` completes:** 5 elements reached
2. **Take Scope closes:** cleanup begins
3. **Recursive closure:** All subscopes close in LIFO order
4. **Finalizer execution:** Files are closed, buffers are flushed

This architecture provides an industrial level of reliability for resource management in functional streams, 
fully consistent with the principles of functional programming.

## Dynamic Resource Allocation

Dynamic resource allocation is a pattern where the program, at runtime:

1. Reads configuration or metadata from an input stream
2. Acquires new resources based on this data
3. Performs transformations using these resources
4. Guarantees releasing all resources upon completion

### Scenario 1: Merging Multiple Files

#### Problem Statement

There is a file `fahrenheits.txt` containing a list of paths to other files:

```
data/file1.txt
data/file2.txt
data/file3.txt
```

We need to:

1. Read each file from the list
2. Combine their contents into a single stream
3. Convert temperatures to Celsius
4. Write the result to `celsius.txt`

#### Implementation with Guaranteed Safety

```dotty
def convertAll(inputFile: String, outputFile: String): Task[Unit] =
  // 1. Read the file containing the list of paths
  lines(inputFile)
    // 2. For each path, create a stream of lines from the file
    .flatMap(filePath => lines(filePath))
    // 3. Apply the chain of transformations
    .pipe(conversion)
    // 4. Write the result to the output file
    .pipe(writeLines(outputFile))
    // 5. Execute
    .run
```

#### How `flatMap` Works in This Context:

```dotty
// Conceptual representation
val dynamicStream: Stream[Task, String] =
  lines("fahrenheits.txt").flatMap: filePath =>
    // For each filePath, a NEW stream is created with its own resources
    lines(filePath) // This stream safely manages the file descriptor
```

#### Resource Lifecycle:

1. **Opening `fahrenheits.txt`** — read the list of files
2. **Sequential opening of files** — for each path in the list:
    - A file descriptor is opened
    - The content is read
    - The descriptor is closed after reading
3. **Continuous data flow** — data from all files is combined
4. **Guaranteed closing** — even on errors, all descriptors are closed

### Scenario 2: Multi-sink Output (Writing to Multiple Files)

#### Problem Statement

For each file in `fahrenheits.txt`:

1. Read its content
2. Convert temperatures
3. Create a corresponding output file
4. Write the result to a file with suffix `.celsius`

#### Implementation

```dotty
def convertMultisink(inputFile: String): Task[Unit] =
  // 1. Read the file containing the list of paths
  lines(inputFile).flatMap: file =>
    // 2. For each file, create a separate pipeline:
    lines(file)                            // Read the source file
      .pipe(conversion)                    // Apply transformations
      .pipe(writeLines(file + ".celsius")) // Write to the new file
  .run // Execute
```

#### Key Features:

**Concurrent resource processing:**

```dotty
// For each input file, an independent chain is created:
// File1: lines("file1.txt") → conversion → writeLines("file1.txt.celsius")
// File2: lines("file2.txt") → conversion → writeLines("file2.txt.celsius")
// File3: lines("file3.txt") → conversion → writeLines("file3.txt.celsius")
```

**Safety Guarantees:**

- Each file is opened and closed in its own scope
- An error in one file does not affect processing of others
- All resources are released even if the entire pipeline fails

### How flatMap Works with Resources

#### Under the Hood of flatMap:

```dotty
def flatMap[F[_], O, R](s: Stream[F, O])(f: O => Stream[F, R]): Stream[F, R] =
  // Create a new scope for each element
  s.map(f).flatten.scope // Important: each nested stream in its own scope
```

#### Hierarchy of Scopes:

```
Root Scope
├── Scope for fahrenheits.txt
│   ├── Scope for file1.txt (opened → processed → closed)
│   ├── Scope for file2.txt (opened → processed → closed)
│   └── Scope for file3.txt (opened → processed → closed)
└── Scope for celsius.txt (if in the first scenario)
```

#### Execution Process:

1. **Reading Metadata:** `fahrenheits.txt` is opened, paths are read
2. **Dynamic Resource Creation:** For each path:
    - A new scope is created
    - A file descriptor is opened
    - Data is processed and passed downstream
    - The scope is closed, the descriptor is released
3. **Stream Continuity:** Data from all files is passed sequentially

### Advantages of the Approach

#### 1. **Composability**

```dotty
// Easily combine different processing strategies
def processingPipeline(strategy: String): Pipe[Task, String, Unit] =
  if strategy == "combined" then
    lines => lines.pipe(conversion).pipe(writeLines("output.txt"))
  else
    lines => lines.flatMap: file =>
      lines(file).pipe(conversion).pipe(writeLines(file + ".celsius"))
```

#### 2. **Resource Safety**

- ✅ Automatic resource lifetime management
- ✅ Error isolation between different files
- ✅ Guaranteed release even on exceptions

#### 3. **Efficiency**

- Resources are released immediately after use
- No accumulation of open descriptors

#### 4. **Ability to Process Large Volumes of Data**

#### 4. **Purity of the Functional Approach**

- All side effects are encapsulated in effects
- Deterministic behavior
- Ease of testing and composition

### Example with Error Handling

```dotty
def robustConvertMultisink(inputFile: String): Task[Unit] =
  lines(inputFile)
    .attempt // Convert errors to values
    .flatMap {
      case Right(filePath) =>
        lines(filePath)
          .pipe(conversion)
          .pipe(writeLines(filePath + ".celsius"))
          .handleErrorWith: e =>
            Stream.eval(Task(println(s"Error processing $filePath: $e")))
      case Left(error) =>
        Stream.eval(Task(println(s"Error reading input: $error")))
    }
    .run
```

This architecture demonstrates the power of functional streams for working with dynamic resources, 
while providing the highest level of safety and reliability.

---

**Links:**

- [Link to the repository with exercises][sample]
- [Functional Programming in Scala, Second Edition, Chapter 15](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
