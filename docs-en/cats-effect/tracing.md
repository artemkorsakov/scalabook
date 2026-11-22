# Tracing — Stack Traces

Tracing is an advanced `IO` feature that provides developers with detailed information
about a fiber's execution graph. This unlocks huge possibilities in **debugging and introspection** —
not only in local development environments, but in critical production environments.

### Problem: Async Code Kills Stack Traces

One of the significant problems when working with async code on the JVM is that
**stack traces no longer provide valuable context** about the program's execution path.

Why does this happen?

```dotty
// Sync code — stack traces are useful
def syncProgram(): Int =
  val a = computeA()  // line 5
  val b = computeB(a) // line 6
  a + b // line 7
// An error in computeB will show the trace: syncProgram -> computeB

// Async code — stack traces lose context
def asyncProgram: IO[Int] =
  for
    a <- IO(computeA())  // stack: IO.map → IOFiber.runLoop → ...
    b <- IO(computeB(a)) // stack: the same one, no indication of above lines
  yield a + b
```

In the synchronous world, every function call adds a frame to the stack. On exception, you see a clear call chain.
In the async world (and especially in `Future`, where an async boundary is inserted after every operation), this chain **is lost**.

`IO` suffers from a similar problem. Moreover, even for fully synchronous `IO` programs,
the stack trace is **polluted** by internal implementation methods of the `IO` execution loop
(such as `IOFiber.runLoop`, `IOFiber.execR`, `WorkerThread.run`, etc.),
which have nothing to do with user code.

### Solution: Async Stack Traces

`IO` solves this problem by collecting stack traces at various `IO` operations as the fiber executes
and combining them into a more coherent, meaningful representation of the execution path.

But tracing in Cats Effect is not just about collecting and combining stack frames. It's a whole set of interconnected capabilities:

1. **Async Stack Tracing**: Collecting stack frames across async boundaries for a given fiber.
2. **Constructor/Combinator Inference**: Stack traces are used to determine
   which specific `IO` constructor or combinator was invoked by user code.
   For example, `void` and `as` are combinators derived from `map`, and they should appear in the trace, not `map` itself.
   The same applies to `IO` constructors such as `async`
   (which internally consists of many low-level implementation details) —
   none of these details should appear in the final trace.
3. **JVM- and GC-safe trace caching**: Starting with version 3.2,
   Cats Effect uses the platform API `java.lang.ClassValue` — a native JVM caching solution, which provides:
    - **ClassLoader Safety**: Long-running Cats Effect applications
      fully respect dynamic class loading environments, releasing references to unloaded classes per JVM instruction,
      which aids the garbage collector.
    - **Lock-Free Data Structures**: Native to the JVM and JIT compiler,
      giving maximum performance.
      Switching to `ClassValue` improved performance by 5-10% over the previous caching implementation.
4. **Performance improvements for full (non-cached) tracing**: Cats Effect 3 is a complete redesign of the project.
   The stack trace collection process was reviewed and optimized,
   leading to up to **5x** performance improvement over the previous mechanism.
   This gives noticeable speedup in debug mode `full` (see below).
5. **Enhanced Exceptions**: Exceptions originating in user code
   are caught by the Cats Effect runtime. Unnecessary internal frames (IO implementation frames) are removed
   and information is added from the async stack trace
   collected during execution (including info about IO constructors and combinators).
   Only then is the enriched exception presented to the user.

### Important Performance Warning

Stack tracing (especially in `full` mode) leads to additional costs: higher CPU usage,
additional memory consumption, and GC pressure.
**Always benchmark your application with tracing enabled and disabled**
before deploying to production!

## Tracing Configuration

The stack tracing mode for an application is configured via a JVM system property:

```
-Dcats.effect.tracing.mode=<mode>
```

For JavaScript, an environment variable is used:

```bash
CATS_EFFECT_TRACING_MODE=<mode>
```

There are **three tracing modes** (values are case-insensitive):

| Mode                           | Description                                                                                                                                                            | Performance                                                                   | When to use                                                                                                                               |
|:-------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------|
| **`cached`** (default)         | Stack traces are captured and **cached** for each call to `map`, `flatMap`, `async`, etc. The cache is indexed by the lambda class reference.                         | High (<<30% overhead in synthetic benchmarks, negligible in real apps)        | **Production (recommended)** — provides an excellent balance between useful information and performance.                                  |
| **`full`**                     | Stack traces are collected on **every** call to an `IO` combinator/constructor.                                                                                        | Low (significant performance degradation)                                     | **Development and debugging** — when `cached` gives inaccurate traces (see warning below) or maximum detail is needed.                    |
| **`none`**                     | Tracing is **fully disabled**. All related data structures and operations are removed from the code.                                                                     | Maximum (minimal overhead)                                                    | High-performance apps with strict latency requirements where trace info is not needed.                                                    |

### Tracing Buffer Configuration

To prevent unbounded memory usage,
trace information for each fiber is accumulated in an internal buffer.
If more traces are collected than the buffer can hold, the oldest information is **overwritten**.

- **Default buffer size:** `16`
- **Configuration (JVM):** `-Dcats.effect.tracing.buffer.size=<size>`
- **Configuration (JS):** `CATS_EFFECT_TRACING_BUFFER_SIZE=<size>`

**Important:** The value must be a **power of two** (2, 4, 8, 16, 32, 64, 128...).
If you specify another number, it will be automatically rounded up to the nearest power of two.

### Example Configuration

```bash
# Enable full tracing with a 1024 buffer (JVM)
java -Dcats.effect.tracing.mode=full -Dcats.effect.tracing.buffer.size=1024 -jar my-app.jar

# Enable cached tracing with a 256 buffer (Node.js)
export CATS_EFFECT_TRACING_MODE=cached
export CATS_EFFECT_TRACING_BUFFER_SIZE=256
node my-app.js
```

### Scala.js Specifics

- **Recommendation:** Install the npm package [source-map-support](https://www.npmjs.com/package/source-map-support).
  This allows using source maps to obtain stack traces with **line numbers from the original Scala code**,
  not from the generated JS code.
- **Limitation:** Tracing in Scala.js is supported **only in `fastLinkJS` mode** (`fastOptJS`).
  In `fullLinkJS` (`fullOptJS`) mode, tracing is **fully disabled**,
  and the corresponding code is cut from the build for maximum performance.

## Deep Dive into Tracing Modes

### `none` Mode

- **What happens:** Tracing is completely disabled. All data structures and operations related to stack collection are absent.
- **Object size:** `IO` objects and internal fiber data structures become slightly smaller (by ~8-16 bytes).
- **Performance:** Identical to a hypothetical `IO` execution loop without tracing support.
- **When to use:** Production environments where every extra CPU cycle counts,
  and trace info is not needed (e.g., high-frequency trading systems, game servers).

### `cached` Mode (Default)

**How it works:**

1. On the first call to a combinator (e.g., `map` on a specific lambda), a stack trace is captured.
2. This trace is **cached** and bound to the **lambda function's class**.
3. On subsequent calls to the same combinator with the same lambda, the cached trace is used.

**Performance:**

- Synthetic benchmarks show **less than 30%** overhead for programs with intensive `IO` usage
  (a mix of sync and async combinators).
- In real applications that do useful work (I/O, computations), the actual overhead is **significantly lower**.
- Important to understand: `IO` with tracing enabled is still **orders of magnitude faster** than an equivalent `Future` program.
  The 30% figure refers to micro-benchmarks that hyperinflate the difference.

**⚠️ Warning: When `cached` May Give Inaccurate Traces**

The cache is indexed by the **lambda function class reference**. This causes problems in the following cases:

1. **Monad Transformer Composition:**
   When using `OptionT[IO, *]` or `EitherT[IO, *]`, the lambdas involved may be wrapped
   and reused in ways that break the one-to-one correspondence between the lambda class and the call site.

2. **Using a Named Function in Multiple Places:**

```dotty
// Problem: the same multiplyByTwo function
// is used in three different places
def multiplyByTwo(x: Int): Int = x * 2

val program =
  for
    a <- IO(1).map(multiplyByTwo) // Cache for multiplyByTwo
    b <- IO(2).map(multiplyByTwo) // Uses the same cache!
    c <- IO(3).map(multiplyByTwo) // Same cache
  yield (a, b, c)
// The stack trace will show the SAME stack for all three calls,
// even though they actually occur at different locations `a`, `b`, and `c`.
```

**Recommendation:** If you suspect tracing inaccuracies (e.g., you see identical lines for different calls),
switch to `full` mode temporarily for debugging.

### `full` Mode

**How it works:** Stack traces are captured on **every** call to an `IO` combinator/constructor. No caching is used.

**Performance:** Significant performance degradation compared to `cached`.
Overhead can be **hundreds of percent** for CPU-intensive programs.

**When to use:**

- **Exclusively for debugging** in a development environment.
- When `cached` gives too inaccurate traces (e.g., with heavy use of transformers or reusable functions).
- For writing tests where trace determinism is important.

**Never use `full` in production!**

## Enhanced Exceptions

### Problem: "Dirty" and Useless Stack Traces

Without tracing (or with `none`), a stack trace from an `IO`-caught exception looks like this:

```
Exception in thread "main" java.lang.Throwable: Boom!
        at Example$.$anonfun$program$5(Main.scala:25)
        at cats.effect.IO.$anonfun$ifM$1(IO.scala:409)
        at cats.effect.IOFiber.runLoop(IOFiber.scala:383)
        at cats.effect.IOFiber.execR(IOFiber.scala:1126)
        at cats.effect.IOFiber.run(IOFiber.scala:125)
        at cats.effect.unsafe.WorkerThread.run(WorkerThread.scala:359)
```

**What's wrong here?**

1. **Too much noise:** `IOFiber.runLoop`, `execR`, `WorkerThread.run` — these are internals of Cats Effect,
   unrelated to user code.
2. **Lost context:** The call chain `fib → flatMap → ...` is completely gone.
   You only see the very last operation (`Example$.$anonfun$program$5`).
   In complex async programs, this makes debugging practically impossible.

### Solution: Enhanced Exceptions

When async tracing is enabled (modes `cached` or `full`),
Cats Effect **enriches** caught exceptions with frames from the async stack trace.
At the same time, **all internal `IO` frames are removed**.

**Result (the same example with tracing enabled):**

```
Exception in thread "main" java.lang.Throwable: Boom!
        at Example$.$anonfun$program$5(Main.scala:25)
        at apply @ Example$.$anonfun$program$3(Main.scala:24)
        at ifM @ Example$.$anonfun$program$3(Main.scala:25)
        at map @ Example$.$anonfun$program$3(Main.scala:24)
        at apply @ Example$.$anonfun$program$1(Main.scala:23)
        at flatMap @ Example$.$anonfun$program$1(Main.scala:23)
        at apply @ Example$.fib(Main.scala:13)
        at flatMap @ Example$.fib(Main.scala:13)
        ... (many more frames from recursive fib)
        at flatMap @ Example$.program(Main.scala:22)
        at run$ @ Example$.run(Main.scala:10)
```

**What do we see now?**

- **Clear call chain:** `program → fib (repeated) → ifM → map → flatMap → apply → exception`.
- **No internal `IO` frames:** The stack is clean from implementation.
- **Informative annotations:** `apply @`, `flatMap @`, `ifM @` — each async operation is annotated with the combinator that spawned it.

This information is incredibly useful for debugging complex async programs,
allowing quick localization of error sources even across dozens of async boundaries and monad transformers.

### Enhanced Exceptions Configuration

The enhanced exceptions feature is managed by a system property (or environment variable):

- **Enabled (default):** `-Dcats.effect.tracing.exceptions.enhanced=true`
- **Disabled:** `-Dcats.effect.tracing.exceptions.enhanced=false`

### Example Code for Generating Traces

```dotty
import cats.effect.{IO, IOApp}
import scala.util.Random

object Example extends IOApp.Simple:
  // Recursive function for demonstrating deep stacks
  def fib(n: Int, a: Long = 0, b: Long = 1): IO[Long] =
    IO(a + b).flatMap: b2 =>
      if n > 0 then fib(n - 1, b, b2)
      else IO.pure(b2)

  def program: IO[Unit] =
    for
      x <- fib(20) // Deep recursive call
      _ <- IO.println(s"The 20th fibonacci number is $x")
      // Generate an exception randomly
      _ <- IO(Random.nextBoolean()).ifM(
        ifTrue = IO.raiseError(new Throwable("Boom!")),
        ifFalse = IO.unit
      )
    yield ()

  override def run: IO[Unit] = program
end Example
```

## Summary and Recommendations

| Role                               | Mode                 | Enhanced               | Buffer             | Rationale                                                                                                                                            |
|:-----------------------------------|:---------------------|:-----------------------|:-------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------|
| **Local development**              | `full` (or `cached`) | `true` (default)       | 64-256             | Maximum information for debugging. `full` gives precise stacks, but is slower.                                                                      |
| **Production (standard)**          | `cached`             | `true`                 | 16 (default)       | Best balance. Caching gives good performance, and enhanced exceptions give valuable error context.                                                   |
| **Production (high load)**         | `none`               | `false`                | N/A                | If CPU and memory are critical, and error stack traces can be recovered via other means (logging, metrics).                                          |
| **Debugging the cache (`cached`)** | `full` (temporary)   | `true`                 | 16                 | If you suspect the cache gives inaccurate traces, temporarily enable `full` to get precise data and determine if code needs rewriting.              |

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Tracing](https://typelevel.org/cats-effect/docs/tracing)

[doc]: https://typelevel.org/cats-effect/docs/concepts
