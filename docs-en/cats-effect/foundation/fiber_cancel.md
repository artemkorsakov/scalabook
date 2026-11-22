# Fiber Cancellation

In traditional JVM threads, cancellation is a **headache**. A thread can only be interrupted (`Thread.interrupt()`), but this:

- Only works in a limited number of places (e.g., `Thread.sleep()`, `Object.wait()`).
- Does not clean up resources automatically.
- Is easily lost or ignored.
- Requires manual checking of the `Thread.isInterrupted()` flag in loops.

**Cats Effect fibers** solve this problem radically: cancellation is a **first-class operation** built into the computation model itself. It:

1. **Consistent** -- a fiber can control when to honor it.
2. **Safe** -- guarantees finalizer execution (`guarantee`, `bracket`).
3. **Automatic** -- high-level operators (e.g., `parTraverse`) cancel unnecessary fibers themselves.

### Simple cancellation example

```dotty
import cats.effect.{IO, IOApp}
import scala.concurrent.duration.*

object CancelDemo extends IOApp.Simple:
  def run: IO[Unit] =
    for
      // Start a fiber that prints text
      fiber <- IO.println("Catch me if you can!").foreverM.start

      // Wait 3 seconds
      _ <- IO.sleep(3.seconds)

      // Cancel the fiber
      _ <- IO.println("Cancelling!")
      _ <- fiber.cancel

      // Wait one more second -- no more printing
      _ <- IO.sleep(1.second)
      _ <- IO.println("Done")
    yield ()
```

**Output:**

```
Catch me if you can!
...
Catch me if you can!
...
Catch me if you can!
Cancelling!
Done
```

**What happened here:**

1. The main fiber creates a child fiber `fiber` that infinitely prints the message.
2. Both fibers run concurrently.
3. After 3 seconds, the main fiber calls `fiber.cancel` -- sending a **cancellation signal**.
4. The child fiber receives the signal, stops execution at the nearest **cancellation point** (in this case -- after the current `println` completes).
5. No more print attempts after cancellation -- the fiber is stopped.

> **Important:** If the fiber were busy with CPU-intensive computations without cancellation points, it would not stop instantly.

### How cancellation works under the hood?

```dotty
// Cancellation is a coordinated process
fiber.cancel  // Sends a cancellation signal

// The fiber must "agree" to be canceled
// Cats Effect automatically checks for cancellation:
// - At flatMap points
// - In sleep operations
// - In async operations
```

#### Cancellation mechanism (simplified)

1. **Signal** -- `fiber.cancel` sets an internal "cancel requested" flag.
2. **Check** -- upon entering each operation that is a **cancellation point**, the fiber checks this flag.
3. **Reaction** -- if the flag is set, the fiber throws a special `CanceledException` and begins the finalization process.
4. **Cleanup** -- all `guarantee`, `bracket`, and other resource handlers are executed.
5. **Completion** -- the fiber transitions to the `Canceled` state, and its `join` returns `Outcome.Canceled`.

#### Where are the cancellation points?

Cats Effect automatically inserts cancellation checks in:

- **Every `flatMap`** -- after one effect completes and before transitioning to the next.
- **`IO.sleep`** -- can wake up early due to cancellation.
- **`IO.async`** and `IO.async_` -- while waiting for a callback.
- **`IO.cede`** -- explicit yield (also a cancellation point).
- **`IO.defer`**, `IO.raiseError`, `IO.pure` -- in some cases.

> This means that **most code is already cancellable** without additional effort.

## CPU-intensive operations and the "uncancellable" problem

If a fiber is busy with **pure computations** without cancellation points (e.g., a long loop, complex math), 
the cancellation signal will not be processed until the computations finish. This can lead to delays or even "hanging".

```dotty
// Bad: this code will ignore cancellation for 10 seconds
MonadCancel[F].uncancelable: _ =>
  // Loop without cancellation points!
  (0 until 1_000_000_000).foreach: i =>
    math.sqrt(i) // Heavy computation
```

**Solution:** add **explicit cancellation points** via `poll` and `IO.cede`.

```dotty
// Good: periodically check for cancellation
MonadCancel[F].uncancelable: poll =>
  def loop(i: Int): F[Unit] =
    if i >= 1_000_000_000 then MonadCancel[F].unit
    else
      // Allow cancellation on each iteration
      poll(IO.cede) >> // ← cancellation point
        IO {
          math.sqrt(i)
          i + 1
        }.flatMap(loop)

  loop(0)
```

**Explanation:**

- `poll(IO.cede)` -- temporarily allows cancellation inside `uncancelable`.
- `IO.cede` -- forcibly yields control to the scheduler and **checks for cancellation**.
- If there is a cancellation signal, execution will skip to the finalizers, bypassing the remaining computation.

## Real-world example: parallel HTTP requests with auto-cancellation

```dotty
import cats.effect.IO
import cats.syntax.all.*

val urls = List(
  "https://api1.example.com",
  "https://api2.example.com",
  "https://api3.example.com",
  "https://api4.example.com"
)

// Load all URLs in parallel
val result: IO[List[String]] = urls.parTraverse: url =>
  httpClient.get(url).flatMap: response =>
    if response.isSuccess then IO.pure(response.body)
    else IO.raiseError(new Exception(s"Error: $url"))
```

**What happens on error:**

1. Four fibers start simultaneously, each loading its URL.
2. Say `api2` returned an error. Its fiber calls `IO.raiseError`.
3. `parTraverse` **immediately cancels all other fibers** (`api1`, `api3`, `api4`).
4. Cancelled fibers stop loading (possibly partially) and release connections.
5. The outer `IO` completes with an error, without waiting for unnecessary requests to finish.

**Savings:** instead of waiting for all 4 requests (including slow or hanging ones), we bail on the very first error. 
In real systems this saves seconds and resources.

## Comparison: fiber cancellation vs thread interruption

| Characteristic              | `Thread.interrupt()`                           | `Fiber.cancel`                              |
|-----------------------------|------------------------------------------------|---------------------------------------------|
| **Model**                   | Imperative, exception-based                    | Functional, built into `F[_]`               |
| **Resource cleanup guarantee** | No (need `try/finally`)                       | Yes (`guarantee`, `bracket`)                |
| **Cancellation points**     | Only blocking operations (`sleep`, `wait`)     | Any `flatMap`, `cede`, `async`              |
| **Nesting**                 | Interruption "punches" through all stacks      | Cooperative, respects `uncancelable`        |
| **CPU-intensive tasks**     | Do not stop (need manual check)                | Do not stop (also need `cede`)              |
| **Safety**                  | Can interrupt at the wrong moment              | `uncancelable` protects critical sections   |

**Conclusion:** fibers provide **controlled, predictable, and safe cancellation**, which classical threads lack.

## High-level patterns with auto-cancellation

Cats Effect and Cats provide many operators that **automatically manage cancellation**:

| Operator      | What it does                                                         | Behavior on error                |
|---------------|----------------------------------------------------------------------|----------------------------------|
| `parTraverse` | Process each element in parallel                                     | Cancel all if any fails          |
| `parSequence` | Execute a list of effects in parallel                                | Cancel all if any fails          |
| `race`        | Race between two effects                                             | Cancel the loser                 |
| `racePair`    | Race with access to the loser's fiber                                | Controlled cancellation          |
| `timeout`     | Limit execution time                                                 | Cancel on timeout                |
| `background`  | Run in background with auto-cancellation on scope exit               | Cancel on finalization           |

## Practical tips for working with cancellation

1. **Always use `guarantee` or `bracket` for resources** -- never rely on "code will just finish".
2. **Add `IO.cede` in long CPU loops** -- otherwise the fiber may "stick" and not respond to cancellation.
3. **Use `uncancelable` only where truly needed** -- overuse hinders cancellation.
4. **When managing fibers manually** -- store the `Fiber` and call `cancel` in finalizers.
5. **For background processes** -- use `background` instead of manual `start`/`cancel`.

```dotty
// Bad: fiber leak
def startBackground[F[_]: Spawn](task: F[Unit]): F[Unit] =
  task.start.void

// Good: fiber will cancel on exit from use
def startBackgroundSafe[F[_]: Spawn: MonadCancel](task: F[Unit]): Resource[F, Unit] =
  Resource.make(task.start)(_.cancel).void
```

---

**References:**

- [Cats effect documentation][doc]
- [Cats effect MonadCancel](https://typelevel.org/cats-effect/docs/typeclasses/monadcancel)
- [Essential Effects by Adam Rosien](https://essentialeffects.dev/)

[doc]: https://typelevel.org/cats-effect/docs/concepts
