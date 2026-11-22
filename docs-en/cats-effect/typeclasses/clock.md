# Clock — Working with Time

## Introduction

Working with time in functional effects is not just calling `System.currentTimeMillis()`.
In the context of effects `F[_]`, we need a **pure**, **referentially transparent**, and **testable** way to get the time.
`Clock[F[_]]` provides exactly such an interface.

### Problems with Direct System Time Function Calls

```dotty
// ❌ Problematic code
def measureTime[F[_]: Sync, A](fa: F[A]): F[(A, Long)] =
  for
    start  <- Sync[F].delay(System.nanoTime()) // Side effect
    result <- fa
    end    <- Sync[F].delay(System.nanoTime())
  yield (result, end - start)
```

**Problems:**

- Bound to a specific implementation (`System.nanoTime`)
- Hard to test (can't substitute time)
- No distinction between monotonic and wall-clock time
- Race conditions possible with concurrent access

`Clock` solves all these problems by providing an **abstract**, **pure** interface to time functions.

## Definition and Key Concepts

### Basic Contract

```dotty
trait Clock[F[_]]:
  def monotonic: F[FiniteDuration] // Monotonic time
  def realTime: F[FiniteDuration]  // System (wall-clock) time
```

### Two Types of Time: Why Does This Matter?

| Characteristic              | `monotonic`                                                    | `realTime`                                                     |
|-----------------------------|----------------------------------------------------------------| -----------------------------------------------------------------|
| **What it measures**         | Time since some arbitrary fixed point in the past              | Absolute system time (Unix epoch)                                 |
| **Can it go backwards?**     | No (strictly increasing)                                       | Yes (NTP synchronization, clock adjustments)                     |
| **Depends on timezone?**     | No                                                             | Yes                                                               |
| **Java equivalent**          | `System.nanoTime()`                                            | `System.currentTimeMillis()`                                      |
| **Primary use**              | Measuring intervals, timeouts, performance                     | Logging with timestamps, calendar operations                     |

### Practical Example of the Differences

```dotty
import cats.effect.{Clock, IO}
import scala.concurrent.duration.*

val clock = Clock[IO]

// ❌ DANGEROUS: measuring time via realTime
for
  start  <- clock.realTime
  result <- someEffect
  end    <- clock.realTime
  duration = end - start // Can be negative!
yield result

// ✅ CORRECT: use monotonic for intervals
for
  start  <- clock.monotonic
  result <- someEffect
  end    <- clock.monotonic
  duration = end - start // Always non-negative
yield result
```

## Detailed Breakdown of Methods

### `monotonic: F[FiniteDuration]`

**Returns:** A duration from some fixed but **arbitrary** reference point.

```dotty
val t1 = clock.monotonic
val t2 = clock.monotonic

(t1, t2).mapN: (first, second) =>
  // first can be anything (0, 123456, etc.)
  // But second - first is always >= 0
  val elapsed = second - first
  println(s"Elapsed: $elapsed")
```

**Key properties:**

- **Monotonicity:** If call `A` happened before call `B`, then `value_A <= value_B`
- **Consistency:** Within a single JVM, all calls use one reference point
- **High precision:** Usually nanosecond (platform-dependent)
- **Independent of system clocks:** Clock adjustments don't affect values

**When to use:**

- Measuring operation execution time
- Implementing timeouts
- Connection pool timeout management
- Performance metrics
- Animations and physics simulations

### `realTime: F[FiniteDuration]`

**Returns:** A duration from the **Unix epoch** (January 1, 1970, 00:00:00 UTC).

```dotty
val now = clock.realTime
now.map: duration =>
  val seconds = duration.toSeconds
  val millis  = duration.toMillis
  println(s"Unix timestamp: $seconds seconds, $millis milliseconds")
```

**Key properties:**

- **Absolute value:** Can be converted to a date and time
- **Can be non-monotonic:** NTP synchronization, daylight saving time
- **Depends on system configuration:** Timezone, sync settings
- **Precision:** Usually millisecond (microseconds on some platforms)

**When to use:**

- Logging with human-readable timestamps
- Generating timestamps for databases
- Working with calendar dates
- Computing age/duration in calendar units

## Advanced Capabilities of Clock

### Extension Methods (Syntax)

```dotty
import cats.effect.Clock
import cats.syntax.all.*

def withTimestamp[F[_]: {Clock, Monad}, A](fa: F[A]): F[(A, FiniteDuration)] =
  for
    start  <- Clock[F].monotonic
    result <- fa
    end    <- Clock[F].monotonic
  yield (result, end - start)

// Usage
val ioWithTime = withTimestamp(IO.println("Hello"))
```

## Practical Usage Examples

### Performance Measurement

```dotty
def measure[F[_]: {Clock, Monad}, A](fa: F[A]): F[(A, FiniteDuration)] =
  for
    start  <- Clock[F].monotonic
    result <- fa
    end    <- Clock[F].monotonic
  yield (result, end - start)

// Usage
val program = measure(IO.sleep(1.second) >> IO.println("Done"))
program.flatMap { case (_, duration) =>
  IO.println(s"Execution time: ${duration.toMillis}ms")
}
```

### Generating Time-Based IDs

```dotty
def timeBasedId[F[_]: {Clock, Sync}]: F[String] =
  for
    now    <- Clock[F].realTime
    nanos  <- Clock[F].monotonic.map(_.toNanos)
    random <- Sync[F].delay(scala.util.Random.nextInt(1000))
  yield s"${now.toMillis}-${nanos % 10000}-$random"
```

## Peculiarities and Pitfalls

### Precision and Resolution

```dotty
// Different behavior on different platforms
Clock[IO].monotonic.map: duration =>
  // JVM: nanoseconds (typically)
  // JS: milliseconds (performance.now)
  // Native: depends on implementation
  println(duration.toNanos)
```

### realTime and Timezones

```dotty
Clock[IO].realTime.map: duration =>
  // duration is UTC time, NOT local time!
  val localTime = duration.toMillis + TimeZone.getDefault.getRawOffset
  // Be careful with daylight saving time!
```

## Summary: Golden Rules for Working with Clock

### ✅ **Correct:**

```dotty
// Measuring intervals
val elapsed =
  for
    start <- Clock[F].monotonic
    _     <- operation
    end   <- Clock[F].monotonic
  yield end - start

// Logging absolute time
val logEntry =
  for
    now <- Clock[F].realTime
  yield s"[${now.toMillis}] Message"

// Abstraction for testing
def service[F[_]: {Clock, Monad}] = ???
```

### ❌ **Incorrect:**

```dotty
// Intervals via realTime
val bad =
  for
    start <- Clock[F].realTime
    _     <- operation
    end   <- Clock[F].realTime
  yield end - start // Can be negative!

// Binding to a specific implementation
def getTime[F[_]: Sync]: F[Long] =
  Sync[F].delay(System.currentTimeMillis()) // Prefer Clock!

// Comparing monotonic values from different instances
val clock1 = Clock[F]
val clock2 = Clock[F]
// clock1.monotonic and clock2.monotonic may have different reference points!
```

### 💡 **Key Takeaways:**

1. **Use `monotonic` for interval measurement** — always use it for timeouts and performance metrics
2. **Use `realTime` for absolute time** — only for timestamp labels in logs/databases
3. **Test with controllable time** — create custom Clock implementations for deterministic tests
4. **Never assume the same precision** — behavior depends on the platform
5. **`Clock` is a side effect** — every call may yield a different result

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Clock](https://typelevel.org/cats-effect/docs/typeclasses/clock)

[doc]: https://typelevel.org/cats-effect/docs/concepts
