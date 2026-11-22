# Standard Library

This section is a collection of **powerful, ready-to-use concurrency primitives**.
They are built on top of the basic types (`Ref`, `Deferred`) and
solve common multi-threaded programming tasks in a functional style.

| Category                    | Primitives and Their Essentials                                                                                                                                                                           |
|:----------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Lifecycle**               | **`Resource`**: Fundamental building block for safe resource management (files, connections), guaranteeing their proper closure.                                                                          |
| **Safe State**              | **`Ref` / `AtomicCell` / `MapRef`**: Atomic references for concurrent data access (from a single value to entire `Map`).                                                                                  |
| **Async Queues**            | **`Queue`, `Dequeue`, `Priority Queue`**: Thread-safe queues for data exchange between fibers (FIFO, bidirectional, priority-based).                                                                     |
| **Synchronization**         | **`Semaphore`, `Mutex`, `CountDownLatch`, `CyclicBarrier`**: Classic primitives for concurrency control: access limiting, mutual exclusion, waiting for signals from a group of fibers.                   |
| **Fiber Management**        | **`Supervisor`**, **`Hotswap`**: `Supervisor` monitors child fibers and cancels them on error. `Hotswap` allows atomically replacing an active resource.                                                  |
| **IO Integration**          | **`Dispatcher`**: A bridge from the `IO` world to imperative code (e.g., for callbacks from Java libraries).                                                                                              |
| **I/O, Environment**       | **`Console`, `Env`, `Random`**: Type-safe, effectful wrappers for I/O operations, reading environment variables, and generating random numbers.                                                          |
| **Utilities**               | **`Backpressure`**: Helps control data processing speed, preventing system overflows.                                                                                                                    |
| **Experimental**            | **`Async/Await`**: Provides syntax similar to `async/await` from other languages (may change in the future).                                                                                            |

### Example of one primitive: `CountDownLatch`

As you can see from the documentation in your link, `CountDownLatch` works simply:

```dotty
import cats.effect.IO
import cats.effect.std.CountDownLatch

val program: IO[Unit] =
  for
    latch <- CountDownLatch[IO](2) // 1. Create a latch for 2 events
    _ <- (latch.await >> IO.println("Unlocked!")).start // 2. Fiber waits
    _ <- IO.println("First signal") >> latch.release // 3. Signal 1
    _ <- IO.println("Second signal") >> latch.release // 4. Signal 2
  // After two signals, the awaiting fiber prints "Unlocked!"
  yield ()
```

**Key features of the Cats Effect standard library:**

- **All primitives are cancelable** and work correctly with fibers.
- **Non-blocking** — they use semantic blocking, not stopping system threads.
- **Composable** — they easily combine with each other for complex logic.

---

**Links:**

- [Cats effect documentation][doc]

[doc]: https://typelevel.org/cats-effect/docs/concepts
