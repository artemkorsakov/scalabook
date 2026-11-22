# Concurrency with Cats Effect

[Translation of the article "Concurrency with Cats Effect", written by Krzysztof Atłasik][Article]

Concurrency is hard. Brian Goetz writes in the book "Java Concurrency in Practice":

> Writing correct programs is hard; writing correct concurrent programs is harder. 
> A concurrent program can have more errors than a sequential one.

Fortunately, Scala has many excellent tools 
helping to "tame" concurrency programming, 
such as [Akka](https://akka.io), [ZIO](https://zio.dev), [Finagle](https://twitter.github.io/finagle/guide/index.html) 
or [Cats Effect](https://typelevel.org/cats-effect/) (hereinafter CE). 
In this article, we will focus on the features offered by the latter library.

## What is a fiber?

The core building block of CE concurrency is a _fiber_. 
In short, a fiber is a lightweight logical thread that represents a sequence of actions. 
In the case of CE, it is a list of operations suspended by the IO monad and ordered via flatMap.

Fibers take up less space than OS threads. 
Most importantly, they are quite cheap in terms of memory usage. 
Therefore, they are not as scarce a resource as threads. 
We don't need to create fiber pools; we can simply create a new one when needed.

Unlike OS threads, fibers do not rely on the OS scheduler for context switching. 
Instead, they use a runtime-level scheduler that controls when and how they run. 
A single fiber is not bound to a specific thread. 
The Cats Effect runtime can create tens of thousands of fibers across several OS threads. 
On the JVM, the runtime typically uses a fixed-size, CPU-bound thread pool, 
but for single-threaded environments like Scala.js, it can even run in a single thread.

![fibers](https://softwaremill.com/user/pages/blog/48.concurrency-with-cats-effect/image1.png?g-225826a2)

## Managing fibers

In CE, you can fork a computation using the `start` method. 
It returns an instance of type `Fiber`, which provides access to the `join` method. 
Calling `join` makes the calling-side fiber wait for the joined fiber to complete.

```dotty
import cats.effect.*
import scala.concurrent.duration.*

val task =
  for
    _ <- IO.println("Task started")
    _ <- IO.sleep(1.second)
    _ <- IO.println("Task completed")
  yield ()

for
  fiber <- task.start
  _ <- IO.println("Hello from main fiber")
  // wait 1 second for the forked fiber to complete
  _ <- fiber.join
yield ()
```

[Example discussed on Scastie](https://scastie.scala-lang.org/W1qDRO72QlaDn40qQ0A5lw)

Another method available on `Fiber` is `cancel`. 
It allows you to cancel a fiber along with the computations being performed on it. 
The `cancel` method will not return control until all resources allocated in the fiber 
have been properly cleaned up and all underlying operations have been cancelled.

You can configure a finalizer via `onCancel(fin: IO[Unit])` that runs when the task is cancelled.

```dotty
import cats.effect.*
import scala.concurrent.duration.*

val task =
  for
    _ <- IO.sleep(5.seconds)
    _ <- IO.println("Computations complete")
  yield ()

for
  fiber <- task.onCancel(IO.println("Task canceled!")).start
  _ <- IO.sleep(1.second)
  // the fiber would have completed in 5 seconds, but is cancelled after 1 second
  _ <- fiber.cancel
yield ()
```

[Example discussed on Scastie](https://scastie.scala-lang.org/DpUgfL8bTdaX1zkR9JiLxg)

The `join` method returns a value of type `Outcome`, which has the following subtypes: `Succeeded`, `Errored`, and `Canceled`. 
You can pattern match on the result to inspect the completed status of the fiber.

The value returned by the fiber will be available as the result field of `Succeeded`. 
Thus, you can pass values from a forked task to the calling side.

```dotty
import cats.effect.*
import scala.concurrent.duration.*

val task = IO.sleep(500.millis) *> IO.pure("Hello world!")

for
  fiber <- task.start
  _ <- fiber.join.flatMap {
    case Outcome.Succeeded(value) =>
      value.flatMap(v => IO.println(s"Computed value: $v"))
    case Outcome.Errored(e) => IO.println(s"Error: $e")
    case Outcome.Canceled() => IO.println("Task was canceled!")
  }
yield ()
```

[Example discussed on Scastie](https://scastie.scala-lang.org/LglCtHUsSZeOxipO6OxQ2Q)

There is another variant of `join`: `joinWith(onCancel: F[A])`. 
It allows specifying a fallback computation in case of fiber cancellation 
and has two specialized variants: `joinWithNever` -- shorthand for `joinWith(IO.never)`, 
and `joinWithUnit` -- shorthand for `joinWith(IO.unit)`.

## Timeouts

You can use the `timeout` method to specify a period after which the fiber will be cancelled.

```dotty
task
  .onCancel(IO.println("Task has timed out!"))
  .timeout(500.millis)
  .start
```

The `timeoutTo` method allows specifying a fallback for cancelled computations.

```dotty
task
  .onCancel(IO.println("Task has timed out!"))
  .timeoutTo(500.millis, planB)
  .start
```

Similar to `cancel`, timeout methods will always wait for all underlying resources allocated in the fiber 
to be properly cleaned up. Waiting can be skipped using `timeoutAndForget`. 
After invocation, it immediately returns control to the calling side and runs finalizers asynchronously. 
Keep in mind that if the finalizers never complete, the fiber will leak.

## Structured concurrency

If a fiber is neither joined nor cancelled, it will run in parallel with the calling-side fiber. 
A fiber completes only if its computations complete or an error occurs. 
If the computations on a fiber never end, the fiber never completes.

```dotty
val task =
  for
    time <- IO.realTimeInstant
    _ <- IO.println(f"Current date and time is $time")
    _ <- IO.sleep(1.second)
  yield ()

for
  _ <- task.foreverM.start // task runs as an infinite loop
  // and the fiber never completes
  _ <- IO.sleep(5.seconds)
  _ <- IO.println("Bye")
yield ()
```

This can lead to resource leaks. 
Fibers are quite lightweight, but if you create enough of them, 
it can still lead to exhausting available memory.

Methods like `start` and `join` are CE concurrency primitives. 
They are handy tools, but should be used with care. 
Otherwise, you can easily cause memory leaks or deadlocks.

To prevent leaks, we should either explicitly cancel excess fibers 
or use safer methods like `background`. 
The `background` method ties the fiber's lifecycle to a `Resource` data type. 
Whenever the resource completes, it also cancels the underlying fiber.

```dotty
val backgroundTask: Resource[IO, Unit] = task
  .onCancel(IO.println("Closing the background task"))
  .background

backgroundTask.surround {
  for
    _ <- IO.sleep(5.seconds)
    _ <- IO.println("Bye") // after the foreground task completes
                           // the background fiber will be cancelled
  yield ()
}
```

The fiber's status corresponds to the code's syntactic structure. 
This way of managing fiber lifecycles is called _structured concurrency_.

Another tool for structured concurrency offered by CE is `Supervisor`.
A `Supervisor` is a resource capable of spawning fibers whose lifecycles will be tied to its own. 
Whenever the supervisor completes, it also ensures all its child fibers are terminated.

`Supervisor` ships with two modes. 
We can select a mode by passing the `await` flag when creating the resource. 
If `await` is `false`, the supervisor will cancel all its active fibers when it completes. This is the default. 
If `await` is `true`, supervisor completion will block until all fibers complete.

```dotty
Supervisor[IO](await = false).use { supervisor =>
  for
    _ <- supervisor.supervise(
      IO.println("A").andWait(1.second).foreverM
    )
    _ <- supervisor.supervise(
      IO.println("B").andWait(2.second).foreverM
    )
    _ <- IO.sleep(5.seconds) // fibers will print A and B
                              // for 5 seconds, then be terminated
  yield ()
}
```

By adding the import `import cats.effect.implicits.*`, 
we can also bring the `supervise` extension method for `IO` into scope. 
This allows calling `supervise` similarly to how `start` is called.

```dotty
import cats.effect.implicits.*

Supervisor[IO](await = true).use { supervisor =>
  IO.println("Hello")
    .andWait(1.second)
    .foreverM
    .timeout(5.seconds)
    .supervise(supervisor)
}
```

## Concurrency operators

CE provides many convenient high-level abstractions for common concurrency-related operations.

For example, with `IO.race` you can take two `IO`s, run them in parallel, and wait for the faster one. 
The slower computation will be cancelled. The result type of `race` is `IO[Either[A, B]]`.

```dotty
val raced: IO[Either[String, Int]] = IO.race(
  IO("Faster").delayBy(5.seconds),
  IO(999).delayBy(10.seconds)
)
```

[Example discussed on Scastie](https://scastie.scala-lang.org/CA02wdioQo6v2xKzaWNYcg)

The `IO.racePair` method gives more control over how to handle the losing fiber. 
It returns both the faster task as an `Outcome` and an instance of `Fiber`, 
which can be used to cancel or join the slower fiber. 
The returned type may initially seem a bit confusing: 
`Either[(OutcomeIO[A], FiberIO[B]), (FiberIO[A, OutcomeIO[B])]`. 
If the left task "wins", we get a left `Either` value: 
a tuple containing the result of the left fiber and a fiber descriptor for the right. 
If the right "wins", we get the right value, this time with the result of the right fiber and the descriptor of the left.

```dotty
IO.racePair(IO(10), IO(5).delayBy(2.seconds)).flatMap {
 case Left((resL, fibR))  => 
    fibR.cancel *> IO(s"Left fiber outcome: $resL")
 case Right((fibL, resR)) => 
    fibL.cancel *> IO(s"Right fiber outcome: $resR")
}
```

[Example discussed on Scastie](https://scastie.scala-lang.org/3lqHHMwWTNOEUgHNrHMh1Q)

If we need to run two computations in parallel and wait for both to complete, we have several options. 
The `IO.both` method returns a tuple containing both results: `IO[(A, B)]`. 
If either operation is cancelled or completes with an error, the other is also cancelled.

```dotty
IO.both(IO(1).delayBy(1.second), IO("Hello"))
```

`IO.bothOutcome` does not interrupt the other fiber even if the first finishes. 
It returns a tuple containing the results of both fibers.

The Cats library provides many operators for executing effects in parallel. 
They are implemented as extension methods, so you need to import the appropriate implicits for their use.

```dotty
import cats.syntax.all.*
```

The `parTupled` method is very similar to `IO.both`. 
It can be called on a tuple of two `IO`s, and both will run in parallel. The result type is `IO[(A, B)]`. 
The semantics of this method are the same as `tupled`, except that `tupled` effects run sequentially.

If you need to execute an arbitrary number of effects concurrently, 
you can use the parallel counterparts of `traverse` and `sequence`: `parTraverse` and `parSequence`.

```dotty
val r: IO[List[Int]] = List(
  IO(1).delayBy(1.second),
  IO(2).delayBy(2.seconds),
  IO(3).delayBy(3.seconds)
).parSequence
```

[Example discussed on Scastie](https://scastie.scala-lang.org/kOE1QhM5QGyESu3KYHNfqg)

You can limit the number of simultaneously executing tasks using `parTraverseN` and `parSequenceN`. 
They take an additional `Int` parameter specifying the maximum level of parallelism.

By default, both `parTraverse` and `parSequence` preserve task order, 
which may lead to a slight performance hit. 
On the other hand, `parTraverseUnordered` and `parSequenceUnordered` do not preserve order, 
which can potentially speed up execution.

Last but not least, we can use two helpful symbolic operators: `&>` and `<&`. 
They also allow executing two effects in parallel. Failure in one `IO` will cancel the other as well. 
If the entire computation is cancelled, both actions are cancelled. The operator `&>` returns the right value, `<&` returns the left.

```dotty
// We get 1 in approximately 2 seconds
IO(1).delayBy(1.second) <& IO(2).delayBy(2.seconds)
```

These operators can be thought of as analogues of the sequential `*>`, `<*`, `<<`, and `>>`.

---

**References:**

- [Original article - Concurrency with Cats Effect][Article]

[Article]: https://softwaremill.com/concurrency-with-cats-effect/
