# User Guide

> Unless otherwise specified, `Stream` in this document refers to `fs2.Stream`, NOT `scala.collection.immutable.Stream`.

## Overview

The FS2 library has two primary capabilities:

- The ability to **build** streams of arbitrary complexity, possibly with embedded effects.
- The ability to **transform** one or more streams using a small but powerful set of operations.

## Building streams

The `Stream[F, O]` type represents a discrete stream of values of type `O`, 
which may request evaluation of effects of type `F`. 
We will call `F` the **effect type**, and `O` the **output type**. Here are a few examples:

```dotty
import fs2.Stream

val s0  = Stream.empty                // empty stream
val s1  = Stream.emit(1)              // stream with one element
val s1a = Stream(1, 2, 3)             // varargs constructor
val s1b = Stream.emits(List(1, 2, 3)) // takes any sequence
```

The stream `s1` has type `Stream[Pure, Int]`. Its output type is `Int`, and its effect type is `Pure`, 
meaning that no effects need to be evaluated to produce the output. 
Streams that do not use effects are called **pure**. 
You can convert a pure stream to a `List` or `Vector` with:

```dotty
s1.toList
s1.toVector
```

Streams have many convenient functions similar to list functions. Here is just a small sample:

```dotty
(Stream(1, 2, 3) ++ Stream(4, 5)).toList
// List(1, 2, 3, 4, 5)
Stream(1, 2, 3).map(_ + 1).toList
// List(2, 3, 4)
Stream(1, 2, 3).filter(_ % 2 != 0).toList
// List(1, 3)
Stream(1, 2, 3).fold(0)(_ + _).toList
// List(6)
Stream(None, Some(2), Some(3)).collect { case Some(i) => i }.toList
// List(2, 3)
Stream.range(0, 5).intersperse(42).toList
// List(0, 42, 1, 42, 2, 42, 3, 42, 4)
Stream(1, 2, 3).flatMap(i => Stream(i, i)).toList
// List(1, 1, 2, 2, 3, 3)
Stream(1, 2, 3).repeat.take(9).toList
// List(1, 2, 3, 1, 2, 3, 1, 2, 3)
Stream(1, 2, 3).repeatN(2).toList
// List(1, 2, 3, 1, 2, 3)
```

Of all the operations listed above, only `flatMap` is primitive; 
all others are built with combinations of other primitives.

So far we have only considered pure streams. FS2 streams can also include effect evaluation:

```dotty
import cats.effect.IO

val eff = Stream.eval(IO { println("EVALUATING!!"); 1 + 1 })
```

`IO` is the effect type we will often see in examples. Creating an `IO` value has no side effects, 
and `Stream.eval` also does nothing at creation time — it is merely a description of what should happen 
when the stream is eventually interpreted. Note that the type of `eff` is now `Stream[IO, Int]`.

The `eval` function works for any effect type, not just `IO`. 
FS2 does not care which effect type you use for your streams. 
You can use `IO` or bring your own effect type by implementing a few interfaces for it 
(e.g., `cats.MonadError[?, Throwable]`, `cats.effect.Sync`, `cats.effect.Async`, `cats.effect.Concurrent`). 
Here is the signature of `eval`:

```dotty
def eval[F[_], A](f: F[A]): Stream[F, A]
```

`eval` creates a stream that evaluates the passed effect and then emits its result (note that `F` is unconstrained). 
Any stream created via `eval` is called **effectful** 
and cannot be run with `toList` or `toVector`. 
If we try, we get a compilation error:

```dotty
eff.toList
// value toList is not a member of fs2.Stream[cats.effect.IO, Int]
```

Here is a complete example of running an effectful stream:

```dotty
import cats.effect.unsafe.implicits.global

eff.compile.toVector.unsafeRunSync()
```

The first call `.compile.toVector` is one of several methods available for **compiling** the stream into a single effect:

```dotty
val ra = eff.compile.toVector          // collect all output into a Vector
val rb = eff.compile.drain             // effect-only (ignoring output)
val rc = eff.compile.fold(0)(_ + _)    // perform and accumulate some result
```

Note that all of them return a value of type `IO` (or other effect), 
but the compilation process itself **does not perform** any effects (no printing occurs).

If we want to run these effects "at the end of the universe", 
we can use one of the `unsafe*` methods on `IO` 
(if you use your own effect type, the running method may differ):

```dotty
ra.unsafeRunSync()
// EVALUATING!!
// Vector(2)
rb.unsafeRunSync()
// EVALUATING!!
rc.unsafeRunSync()
// EVALUATING!!
// 2
rc.unsafeRunSync()   
// running again will recompute:
// EVALUATING!!
// 2
```

Here we finally see tasks being executed. As shown with `rc`, 
re-running the task results in the entire computation being re-executed; nothing is cached automatically.

## Chunks

FS2 streams are internally chunked for performance. 
You can create a single chunk with `Stream.chunk`, which takes a `fs2.Chunk`. 
Many library functions are chunk-aware and strive to preserve chunks where possible. 
`Chunk` is a strict, finite sequence of values supporting efficient indexed access to elements.

```dotty
import fs2.Chunk

val s1c = Stream.chunk(Chunk.array(Array(1.0, 2.0, 3.0)))
```

## Basic stream operations

Streams have a small but powerful set of operations, some of which we have already seen. 
Key operations: `++`, `map`, `flatMap`, `handleErrorWith`, and `bracket`:

```dotty
val appendEx1 = Stream(1, 2, 3) ++ Stream.emit(42)
val appendEx2 = Stream(1, 2, 3) ++ Stream.eval(IO.pure(4))

appendEx1.toVector
// Vector(1, 2, 3, 42)
appendEx2.compile.toVector.unsafeRunSync()
// Vector(1, 2, 3, 4)

appendEx1.map(_ + 1).toList
// List(2, 3, 4, 43)
```

The `flatMap` operation works the same as for lists — it maps each element and then concatenates the results:

```dotty
appendEx1.flatMap(i => Stream.emits(List(i, i))).toList
// List(1, 1, 2, 2, 3, 3, 42, 42)
```

Regardless of how the `Stream` is built, each operation runs in **constant time**. 
Thus, `s ++ s2` runs in constant time, regardless of whether `s` is a stream `Stream.emit(1)` 
or a huge stream with millions of elements and many embedded effects. 
The same applies to `s.flatMap(f)` and `handleErrorWith`, which we will encounter later. 
The execution time of these operations does not depend on the structure of `s`.

## Error handling

A stream can generate errors — either explicitly, via `Stream.raiseError`, 
or implicitly through an exception in pure code or inside an effect passed to `eval`:

```dotty
val err  = Stream.raiseError[IO](new Exception("oops!"))
val err2 = Stream(1, 2, 3) ++ (throw new Exception("!@#$"))
val err3 = Stream.eval(IO(throw new Exception("error in effect!!!")))
```

All of these streams will error out at runtime:

```dotty
try err.compile.toList.unsafeRunSync() catch { case e: Exception => println(e) }
// java.lang.Exception: oops!
// val res0: List[Nothing] | Unit = ()

try err2.toList catch { case e: Exception => println(e) }
// java.lang.Exception: !@#$
// val res1: List[Int] | Unit = ()

try err3.compile.drain.unsafeRunSync() catch { case e: Exception => println(e) }
// java.lang.Exception: error in effect!!!
```

The `handleErrorWith` method allows catching any of these errors:

```dotty
err.handleErrorWith { e => Stream.emit(e.getMessage) }.compile.toList.unsafeRunSync()
// List(oops!)
```

Note that even when using `handleErrorWith` (or `attempt`), the stream will be terminated after the error, 
and no new values will be extracted from it. In the following example, the number 4 will never be read from the stream.

```dotty
val err4 = Stream(1, 2, 3).covary[IO] ++
  Stream.raiseError[IO](new Exception("bad times!")) ++
  Stream.eval(IO(4))

err4.handleErrorWith { _ => Stream(0) }.compile.toList.unsafeRunSync()
// List(1, 2, 3, 0)
```

> Do not use `handleErrorWith` for resource cleanup; 
> for that purpose, use `bracket`, discussed in the next section. 

## Resource acquisition

If you need to acquire a resource and want to guarantee that a cleanup action is performed upon acquisition, 
use the `bracket` function:

```dotty
val count   = new java.util.concurrent.atomic.AtomicLong(0)
val acquire = IO { println("incremented: " + count.incrementAndGet()); () }
val release = IO { println("decremented: " + count.decrementAndGet()); () }

Stream.bracket(acquire)(_ => release).flatMap { _ =>
  Stream(1, 2, 3) ++ err
}.compile.drain.unsafeRunSync()
// incremented: 1
// decremented: 0
// java.lang.Exception: oops!
```

The inner stream errors out, but note that the `release` action still executes:

```dotty
count.get()
// 0
```

Regardless of how you transform an FS2 stream and wherever errors occur, the library guarantees 
that if a resource was acquired via `bracket`, the associated release action will be performed. 
Here is the signature of `bracket`:

```dotty
def bracket[F[_], R](acquire: F[R])(release: R => F[Unit]): Stream[F, R]
```

FS2 guarantees **exactly-once** semantics for cleanup actions introduced by `Stream.bracket`.

## Stream construction exercises

Try to implement `repeat`, which repeats a stream infinitely, 
`drain`, which removes all output from a stream, 
`exec`, which performs an effect and ignores its result, 
and `attempt`, which catches any errors that arise in the stream:

```dotty
Stream(1, 0).repeat.take(6).toList
// List(1, 0, 1, 0, 1, 0)
Stream(1, 2, 3).drain.toList
// List()
Stream.exec(IO.println("!!")).compile.toVector.unsafeRunSync()
// !!
// Vector()
(Stream(1, 2) ++ Stream(3).map(_ => throw new Exception("nooo!!!"))).attempt.toList
// List(Right(1), Right(2), Left(java.lang.Exception: nooo!!!))
```

## Statefully transforming streams

It is often necessary to transform one or more streams while preserving state, 
possibly computing effects along the way. 
As an example, consider the task of taking the first 5 elements from a stream `s: Stream[IO, Int]`. 
To create a `Stream[IO, Int]` that takes only the first 5 elements from `s`, 
we need to repeatedly await (or extract) values from `s`, 
tracking the number of elements already seen and stopping once 5 elements have been reached. 
In more complex scenarios, we may want to compute additional effects as we extract from one or more streams.

Consider an implementation of `take` using the `scanChunksOpt` combinator:

```dotty
import fs2.*

def tk[F[_], O](n: Long): Pipe[F, O, O] =
  in =>
    in.scanChunksOpt(n) { n =>
      if n <= 0 then None
      else
        Some((c: Chunk[O]) =>
          c.size match
            case m if m < n => (n - m, c)
            case _          => (0, c.take(n.toInt))
        )
    }

Stream(1, 2, 3, 4).through(tk(2)).toList
// List(1, 2)
```

Let's break down this code line by line.

```dotty
in => in.scanChunksOpt(n) { n =>
```

Here we create an anonymous function from `Stream[F, O]` to `Stream[F, O]` and call `scanChunksOpt`, 
passing the initial state `n` and a function that we define on the following lines. 
The function takes the current state as an argument, which we deliberately named `n`, 
shadowing the `n` from the signature of `tk` to prevent accidentally referencing it.

```dotty
if n <= 0 then None
```

If the current state is 0 (or less), we are done, so we return `None`. 
This tells `scanChunksOpt` that the stream should terminate.

```dotty
else Some((c: Chunk[O]) =>
  c.size match
    case m if m < n => (n - m, c)
    case _          => (0, c.take(n.toInt))
)
```

Otherwise, we return a function that processes the next chunk in the stream. 
The function first checks the size of the chunk. 
If it is less than the number of elements to take, it returns the chunk unchanged (it will be passed further along the stream), 
as well as the number of remaining elements to take from subsequent chunks (`n - m`). 
If the chunk size is larger than the number of remaining elements, the first `n` elements of the chunk are emitted, 
and it is indicated that no more elements should be taken.

### Transforming streams with Pull

Sometimes `scanChunksOpt` is not powerful enough to express the desired transformation. 
No matter how complex the task, the `fs2.Pull` type can usually express it.

The `Pull[F[_], O, R]` type represents a program that can extract values from one or more streams, 
write **output** of type `O`, and return a **result** of type `R`. 
It forms a [monad](../../typeclass/monad/monad.md) over `R` and is equipped with many other useful operations. 
For a full set of `Pull` operations, see the [`Pull` type class](https://github.com/functional-streams-for-scala/fs2/blob/main/core/shared/src/main/scala/fs2/Pull.scala).

A `Pull` that writes a single output value of type `Int` can be constructed with `Pull.output1`.

```dotty
import fs2.*

val p1 = Pull.output1(1)
// Output(Chunk(1))
```

It can be directly converted to a stream, equivalent to `Stream(1)`.

```dotty
val s1 = p1.stream
// val s1: Stream[Nothing, Int] = Stream(..)
```

`Pull` form a monad over their result `R` and can be combined via monadic operations. 
The following code creates a `Pull` corresponding to `Stream(1, 2)`.

```dotty
p1 >> Pull.output1(2)
// val res0: Pull[[x] =>> Nothing, Int, Unit] = <function1>
```

A `Pull` can be created from a stream via various operations available through the `pull` function. 
For example, `echo` transforms a stream into its corresponding `Pull` representation.

```dotty
s1.pull.echo
// InScope(Output(Chunk(1)),false)
```

A more useful `Pull` is created with `uncons`. It creates a `Pull` that extracts the next chunk from the stream.

```dotty
s1.pull.uncons
// val res2: Pull[Nothing, Nothing, Option[(Chunk[Int], Stream[Nothing, Int])]] = <function1>
```

Let's examine its result type:

- `Option` is non-empty if there is a chunk to extract from the stream. If the stream has terminated and there are no more chunks, the result is `None`.
- `Chunk[Int]` represents the extracted chunk.
- `Stream[Nothing, Int]` represents the tail of the stream.

Note that the output type is `Nothing` because this particular `Pull` emits no elements.

Consider an implementation of `take` using `uncons`:

```dotty
import fs2.*

def tk[F[_], O](n: Long): Pipe[F, O, O] =
  def go(s: Stream[F, O], n: Long): Pull[F, O, Unit] =
    s.pull.uncons.flatMap {
      case Some((hd, tl)) =>
        hd.size match
          case m if m <= n => Pull.output(hd) >> go(tl, n - m)
          case _           => Pull.output(hd.take(n.toInt))
      case None => Pull.done
    }
  in => go(in, n).stream
// def tk[F[_$1], O](n: Long): Pipe[F, O, O]  

Stream(1, 2, 3, 4).through(tk(2)).toList
// List(1, 2)
```

Line by line breakdown:

```dotty
def go(s: Stream[F, O], n: Long): Pull[F, O, Unit] = 
```

We implement this with a recursive function returning a `Pull`. 
At each call, we pass a `Stream[F, O]` and the remaining number of elements `n`.

```dotty
s.pull.uncons.flatMap {
```

Calling `s.pull` gives us many methods that transform the stream into a `Pull`. 
We use `uncons` to extract the next chunk from the stream, 
which gives us `Pull[F, Nothing, Option[(Chunk[O], Stream[F, O])]]`. 
We then apply `flatMap` to this `Pull` to gain access to the `Option`.

```dotty
case Some((hd, tl)) =>
  hd.size match
    case m if m <= n => Pull.output(hd) >> go(tl, n - m)
    case _           => Pull.output(hd.take(n.toInt))
```

If we get a `Some`, we destructure the tuple as `hd: Chunk[O]` and `tl: Stream[F, O]`. 
We then check the size of the head chunk. 
If the chunk size is less than or equal to the remaining number of elements, the chunk is emitted via `Pull.output`, 
and we recursively call `go` on the tail stream `tl`, passing the remaining number of elements. 
Otherwise, we emit the first `n` elements of the head.

```dotty
case None => Pull.done
```

`None` is obtained when there are no more elements in the `in` stream. 
We terminate the pull with `Pull.done` — a `Pull` that does nothing.

```dotty
in => go(in, n).stream
```

Finally, we create an anonymous function from `Stream[F, O]` to `Stream[F, O]` and call `go` with the initial value `n`. 
We get a `Pull[F, O, Unit]`, which we convert back to `Stream[F, O]` via the `.stream` method.

```dotty
val s2 = Stream(1, 2, 3, 4).through(tk(2))
s2.toList
// List(1, 2)
```

FS2 guarantees that all resources acquired by `Pull` are released when the stream completes. 
Note that **nothing happens** when we call `.stream` on a `Pull` — it is merely a conversion back to the `Stream` API.

In practice, explicit recursion is rarely required — methods within `pull` usually recurse for us. 
Explicit recursion in `tk` could be eliminated by using `pull.take`.

```dotty
import fs2.*

def tk[F[_], O](n: Long): Pipe[F, O, O] =
  in => in.pull.take(n).void.stream

Stream(1, 2, 3, 4).through(tk(2)).toList
// List(1, 2)
```

In [`Stream`](https://github.com/functional-streams-for-scala/fs2/blob/main/core/shared/src/main/scala/fs2/Stream.scala) 
there are many useful transformation functions built on top of the `Pull` type.

## Stream transformation exercises

Try to implement `takeWhile`, `intersperse`, and `scan`:

```dotty
Stream.range(0, 100).takeWhile(_ < 7).toList
// List(0, 1, 2, 3, 4, 5, 6)
Stream("Alice", "Bob", "Carol").intersperse("|").toList
// List(Alice, |, Bob, |, Carol)
Stream.range(1, 10).scan(0)(_ + _).toList // cumulative sum
// List(0, 1, 3, 6, 10, 15, 21, 28, 36, 45)
```

## Concurrency

FS2 comes with a large number of operations for concurrent work. 
The `merge` function runs two streams concurrently, merging their outputs. 
It stops when both input streams have completed:

```dotty
import cats.effect.IO
import cats.effect.unsafe.implicits.global

Stream(1, 2, 3).merge(Stream.eval(IO {
  Thread.sleep(200); 4
))).compile.toVector.unsafeRunSync()
// Vector(1, 2, 3, 4)
```

The `merge` function supports concurrency. FS2 has many other useful concurrency functions, 
such as `concurrently` (runs another stream concurrently and ignores its output), 
`interruptWhen` (stops when the left branch emits `true`), `either` (like `merge` but returns `Either`), and more.

Depending on how you want to stop the resulting stream, 
you can use one of three variants of `merge`: `mergeHaltR`, `mergeHaltL`, and `mergeHaltBoth`. 
The resulting stream will terminate when the right stream, left stream, or either stream completes, respectively:

```dotty
val finite   = Stream('a', 'b', 'c', 'd', 'e').covary[IO]
val infinite = Stream.iterate(0)(_ + 1).covary[IO]

// Left --------- Right
finite.mergeHaltL(infinite) // Will complete
infinite.mergeHaltL(finite) // Will not complete

finite.mergeHaltR(infinite) // Will not complete
infinite.mergeHaltR(finite) // Will complete

finite.mergeHaltBoth(infinite) // Will complete
infinite.mergeHaltBoth(finite) // Will also complete
```

Since it is quite common to terminate one stream as soon as the other completes (as in a producer-consumer environment), 
there are optimizations compared to the `merge` variants. 
`concurrently` is one of them: it terminates the resulting stream when the left stream completes. 
The right stream will also terminate at that point, discarding its values (similar to `finite.mergeHaltL(infinite.drain)`):

```dotty
import cats.effect.std.{Queue, Random}
import scala.concurrent.duration.*

def producer(queue: Queue[IO, Option[Int]])(using
    rnd: Random[IO]
): Stream[IO, Option[Int]] =
  Stream
    .repeatEval(Random[IO].betweenInt(100, 800))
    .evalTap(n => IO.println(s"Produced: $n"))
    .flatMap(t =>
      Stream.sleep[IO](t.milliseconds) >>
        Stream.emit(if t >= 750 then None else Some(t))
    )
    .evalTap(queue.offer)

def consumer(queue: Queue[IO, Option[Int]]): Stream[IO, Unit] =
  Stream.fromQueueNoneTerminated(queue, 10).evalMap(n =>
    IO.println(s"Consumed: $n")
  )

val concurrentlyDemo =
  Stream.eval(Queue.bounded[IO, Option[Int]](20)).flatMap { queue =>
    Stream.eval(Random.scalaUtilRandom[IO]).flatMap { implicit rnd =>
      consumer(queue).concurrently(producer(queue))
    }
  }

concurrentlyDemo.compile.drain.unsafeRunSync()
// Produced: 398
// Produced: 752
// Consumed: 398
// Produced: 528
```

In the example above, the `consumer` stream will terminate if an item produced by `producer` is processed for more than 750 milliseconds.

The `parEvalMap` function allows evaluating effects concurrently and emitting results sequentially on up to `maxConcurrent` fibers simultaneously, similar to the `parTraverseN` method commonly used in standard library collections:

```dotty
// This will evaluate 5 effects concurrently
Stream(1, 2, 3, 4, 5).parEvalMap(5)(n => IO.pure(n * 2)).compile.toVector.unsafeRunSync()
// Vector(2, 4, 6, 8, 10)
```

However, its use with pure operations is rare; 
it is more commonly used with functions or combinators that may have side effects:

```dotty
import fs2.io.file.{Path, Files}
import fs2.io.readInputStream

import java.net.{URI, URL}
import java.io.InputStream

def getConnectionStream(url: URL): IO[InputStream] =
  IO(url.openConnection().getInputStream)

// The Adventures of Tom Sawyer, Mark Twain
val bookParts = Stream(
  "7193/pg7193.txt", // Part 1
  "7194/pg7194.txt", // Part 2
  "7195/pg7195.txt", // Part 3
  "7196/pg7196.txt"  // Part 4
).map(part => new URI(s"https://www.gutenberg.org/cache/epub/$part").toURL)

bookParts
  .covary[IO]
  .parEvalMap(4)(url =>
    IO.println(s"Getting connection from $url") >> getConnectionStream(url)
  )
  .flatMap(inps => readInputStream[IO](IO(inps), 4096))
  .through(Files[IO].writeAll(Path("testdata/tom_sawyer.txt")))
  .compile
  .drain
  .unsafeRunSync()
```

Although in most cases the order of the stream does not matter. This may be due to various reasons, 
for example, if the resulting values do not matter, 
if the function you pass can take significantly different amounts of time depending on inputs, etc. 
For such cases, there is a `parEvalMapUnordered` method. 
For example, if you simply want to log effects as soon as they complete:

```dotty
def slowFibo(n: Int): Int = // Just simulating expensive computation
  if n <= 0 then n
  else if n == 1 then 1
  else slowFibo(n - 1) + slowFibo(n - 2)

Stream.eval(Random.scalaUtilRandom[IO]).flatMap { rnd =>
  Stream.repeatEval[IO, Int](rnd.nextIntBounded(40))
    .parEvalMapUnordered(2)(n =>
      IO.println(s"Emitted value for $n with result: ${slowFibo(n)}")
    )
}
  .interruptAfter(3.seconds)
  .compile
  .drain
  .unsafeRunSync()
```

Note that if you need unbounded concurrency, 
there are also `parEvalMapUnbounded` and `parEvalMapUnorderedUnbounded` versions that do not take a `maxConcurrent` argument.

The `parJoin` function runs multiple streams concurrently; 
it is very useful when you need to run multiple streams as independent processes, 
rather than making them dependent on each other. Signature:

```dotty
// note the Concurrent[F] constraint
import cats.effect.Concurrent
def parJoin[F[_]: Concurrent, O](maxOpen: Int)(outer: Stream[F, Stream[F, O]]): Stream[F, O]
```

It flattens a nested stream, allowing up to `maxOpen` inner streams to execute simultaneously. 
Like `parEvalMapUnbounded`, there is a `parJoinUnbounded` method for unbounded concurrency.

Example of running three different processes concurrently using `parJoin`:

```dotty
val processA =
  Stream.awakeEvery[IO](1.second).map(_ => 1).evalTap(x =>
    IO.println(s"Process A: $x")
  )

val processB =
  Stream.iterate[IO, Int](0)(_ + 1).metered(400.milliseconds).evalTap(x =>
    IO.println(s"Process B: $x")
  )

val processC = Stream(1, 2, 3, 4, 5)
  .flatMap(t => Stream.sleep[IO]((t * 200).milliseconds) >> Stream.emit(t))
  .repeat
  .evalTap(x => IO.println(s"Process C: $x"))

Stream(processA, processB, processC).parJoin(3)
  .interruptAfter(5.seconds)
  .compile
  .drain
  .unsafeRunSync()
```

The `Concurrent` constraint on `F` is required wherever concurrency is used in the library. 
As mentioned earlier, users can bring their own effect types provided 
they also provide a `Concurrent` instance in implicit scope.

Additionally, there are a number of other concurrency primitives — asynchronous queues, signals, and semaphores. 
See the [Concurrency Primitives](concurrency-primitives.md) section for more examples.

## Concurrency exercises

Without looking at the implementation, try to implement `mergeHaltBoth`:

```dotty
type Pipe2[F[_], -I, -I2, +O] = (Stream[F, I], Stream[F, I2]) => Stream[F, O]

/** Like `merge`, but stops as soon as _either_ branch stops. */
def mergeHaltBoth[F[_]: Concurrent, O]: Pipe2[F, O, O, O] = (s1, s2) => ???
```

## Interruption

Sometimes some tasks should only execute under certain conditions or until another task completes. 
Fortunately, `Stream` defines several very useful methods that allow us to do this. 
In the following example, we will see how `interruptWhen` helps us describe such cases. 
We will describe a program consisting of two parallel streams: 
the first will print the current time to the console every second, and the second will stop the first.

First, we need to set up the environment with several imports and declare some implicit values.

```dotty
import fs2.Stream
import cats.effect.{Deferred, IO}
import cats.effect.unsafe.implicits.global
import scala.concurrent.duration.*
```

The example looks like this:

```dotty
val program =
  Stream.eval(Deferred[IO, Unit]).flatMap { switch =>
    val switcher =
      Stream.eval(switch.complete(())).delayBy(5.seconds)

    val program =
      Stream.repeatEval(IO(println(java.time.LocalTime.now))).metered(1.second)

    program
      .interruptWhen(switch.get.attempt)
      .concurrently(switcher)
  }

program.compile.drain.unsafeRunSync()
```

Now let's break down this code line by line to understand what is happening.

```dotty
val program =
  Stream.eval(Deferred[IO, Unit]).flatMap { switch =>
```

Here we create a `Stream[IO, Deferred[IO, Unit]]`. 
[`Deferred`](../../cats-effect/typeclasses/concurrent/deferred.md) is a concurrency primitive 
representing a condition that has not yet been fulfilled. 
We will use the obtained `Deferred[IO, Unit]` instance as a mechanism to signal task completion. 
Given this purpose, we name this instance `switch`.

```dotty
val switcher =
  Stream.eval(switch.complete(())).delayBy(5.seconds)
```

`switcher` is a stream that "switches" the `switch` after 5 seconds by calling `complete` on it. 
`delayBy` adds a delay before executing the stream, effectively postponing its evaluation for the specified duration.

```dotty
val program =
  Stream.repeatEval(IO(println(java.time.LocalTime.now))).metered(1.second)
```

This is the program we want to interrupt. `repeatEval` is the effectful version of `repeat`. 
`metered`, in turn, causes our stream to emit values at a given rate (in this case — one value per second).

```dotty
program
  .interruptWhen(switch.get.attempt)
  .concurrently(switcher)
```

In this line, we call `interruptWhen` on the stream, getting a stream that will stop evaluating 
as soon as the "switch is flipped"; then, thanks to `concurrently`, we specify that we want 
`switcher` to run in the **background**, ignoring its output. 
This returns us to the program described at the beginning of the chapter.

This is a way to create a program that runs for a specified duration, in this example — 5 seconds. 
Timer-based interruption is such a common case that FS2 defines an `interruptAfter` method. 
Armed with this knowledge, we can rewrite our example as:

```dotty
val program1 =
  Stream
    .repeatEval(IO(println(java.time.LocalTime.now)))
    .metered(1.second)
    .interruptAfter(5.seconds)

program1.compile.drain.unsafeRunSync()
```

## Interacting with the outside world

When interacting with the outside world, you may encounter several different situations:

- Functions that perform effects **synchronously**. This is the simplest case.
- Functions that perform effects **asynchronously** and call a callback **once** upon completion. 
  Example: reading 4 MB from a disk file may be a function that takes a callback to invoke when bytes become available.
- Functions that perform effects asynchronously and call a callback **one or more times** as results become available. 
  Example: a database API that asynchronously delivers query results as they become available.

Let's consider each of these cases in order.

### Synchronous effects

These are the easiest to work with. Simply wrap such effects in `Stream.eval`:

```dotty
def destroyUniverse(): Unit = println("BOOOOOM!!!"); // placeholder

val s = Stream.exec(IO { destroyUniverse() }) ++ Stream("...moving on")
s.compile.toVector.unsafeRunSync()
// BOOOOOM!!!
// val res0: Vector[String] = Vector(...moving on)
```

The way you introduce synchronous effects into your effect type may differ. 
`Sync.delay` can be used for this generally, without tying to a specific effect:

```dotty
import cats.effect.Sync

val T  = Sync[IO]
val s2 = Stream.exec(T.delay { destroyUniverse() }) ++
  Stream("...moving on")
s2.compile.toVector.unsafeRunSync()
```

When using this approach, ensure that the expression passed to `delay` does not throw exceptions.

### Asynchronous effects (single callback invocation)

You will often deal with APIs like this:

```dotty
trait Connection:
  def readBytes(
      onSuccess: Array[Byte] => Unit,
      onFailure: Throwable => Unit
  ): Unit

  // or perhaps:
  def readBytesE(onComplete: Either[Throwable, Array[Byte]] => Unit): Unit =
    readBytes(bs => onComplete(Right(bs)), e => onComplete(Left(e)))

  override def toString = "<connection>"
```

That is, we provide a `Connection` with two callbacks (or one taking an `Either`), 
and at some later point this callback will be invoked **once**. 
The `cats.effect.Async` trait provides a convenient function in such situations:

```dotty
trait Async[F[_]] extends Sync[F] with Temporal[F]:
  ...

  /** Creates an `F[A]` from an asynchronous computation of the form of a function with
    * which we can register a callback. This can
    * be used to transition from callback-based APIs to the simple monadic
    * version.
    */
  def async_[A](register: (Either[Throwable, A] => Unit) => Unit): F[A]
```

Here is a complete example:

```dotty
val c = new Connection:
  def readBytes(
      onSuccess: Array[Byte] => Unit,
      onFailure: Throwable => Unit
  ): Unit =
    Thread.sleep(200)
    onSuccess(Array(0, 1, 2))

val bytes = IO.async_[Array[Byte]] { cb => c.readBytesE(cb) }

Stream.eval(bytes).map(_.toList).compile.toVector.unsafeRunSync()
```

Be sure to check out the [`fs2.io`](https://github.com/functional-streams-for-scala/fs2/tree/main/io/) package, 
which has excellent FS2 bindings to Java NIO libraries using exactly this approach.

### Asynchronous effects (multiple callback invocation)

The beauty of single-invocation callback APIs is that 
back-pressure can be organized within FS2 itself. 
If you don't need more values — just don't read them, and they won't be produced! 
But sometimes you deal with an API that calls the callbacks you pass **more than once**. 
Perhaps it is some streaming API, and it calls your callback when new data arrives. 
In such cases, you can use an asynchronous queue to organize interoperability between 
the FS2 streaming world and the external API, 
as well as use any suitable mechanism for throttling the producer provided by that API.

> Some of such APIs do not provide any means of throttling the producer, 
> in which case you will either have to accept potentially unbounded memory consumption 
> (if the producer and consumer run at significantly different speeds), 
> or use blocking concurrency primitives such as `cats.effect.std.Queue.bounded` or primitives from `java.util.concurrent`.

Consider a complete example:

```dotty
import fs2.*
import cats.effect.Async
import cats.effect.std.{Dispatcher, Queue}

type Row        = List[String]
type RowOrError = Either[Throwable, Row]

trait CSVHandle:
  def withRows(cb: RowOrError => Unit): Unit

def rows[F[_]: Async as F](h: CSVHandle): Stream[F, Row] =
  for
    dispatcher <- Stream.resource(Dispatcher.sequential[F])
    q          <- Stream.eval(Queue.unbounded[F, Option[RowOrError]])
    _          <- Stream.eval {
      F.delay {
        def enqueue(v: Option[RowOrError]): Unit =
          dispatcher.unsafeRunAndForget(q.offer(v))

        // Fill data — withRows blocks while reading the file,
        // asynchronously invoking the passed callback for each row
        h.withRows(e => enqueue(Some(e)))
        // After returning from withRows, signal that the stream is done.
        enqueue(None)
      }
    }
    // Thanks to fromQueueNoneTerminated, the stream will terminate upon encountering None
    row <- Stream.fromQueueNoneTerminated(q).rethrow
  yield row
```

See [`Queue`](../../cats-effect/std/queue.md) for information about other useful methods. 
Most concurrent queues in Cats Effect support tracking their own size, 
which is convenient for implementing size-based producer throttling.

## Reactive Streams

The [reactive streams](http://www.reactive-streams.org/) initiative is complex, mutable, and unsafe — 
it is undesirable for use on top of fs2. 
But there are cases when we need to use fs2 together with another streaming library, 
and here reactive streams comes into play.

Any reactive streams system can interoperate with any other reactive streams system, 
providing `org.reactivestreams.Publisher` or `org.reactivestreams.Subscriber`.

The `reactive-streams` library provides instances of publishers and subscribers, 
conforming to the reactive streams specification, to simplify interoperability with other streaming libraries.

#### Usage

You may need the following imports:

```dotty
import fs2.*
import fs2.interop.reactivestreams.*
import cats.effect.{IO, Resource}
```

To convert a `Stream` into a downstream unicast `org.reactivestreams.Publisher`:

```dotty
val stream = Stream(1, 2, 3).covary[IO]
stream.toUnicastPublisher
```

To convert an upstream `org.reactivestreams.Publisher` into a `Stream`:

```dotty
val publisher: Resource[IO, StreamUnicastPublisher[IO, Int]] =
  Stream(1, 2, 3).covary[IO].toUnicastPublisher
publisher.use { p =>
  p.toStreamBuffered[IO](10).compile.toList
}
```

Unicast publishers must have only a single subscriber.

## Applications

### How stream interruption works

In FS2, a stream can terminate in one of three ways:

1. **Normal input exhaustion.** For example, the stream `Stream(1, 2, 3)` terminates after 
    emitting a single chunk (containing values `1, 2, 3`).
2. **Uncaught exception.** For example, the stream `Stream(1, 2, 3) ++ (throw Err)` terminates with error `Err` after emitting a single chunk.
3. **Stream interruption by the consumer.** Interruption can be **synchronous**, as in `(Stream(1) ++ (throw Err)) take 1`, 
    which deterministically stops the stream before the `++` executes, or **asynchronous**, as in `s1 merge s2 take 3`.

Regarding point 3:

- A stream will never be interrupted during **acquiring** a resource (via `bracket`) or during **releasing** a resource. 
  The `bracket` function guarantees that if FS2 begins acquiring a resource, the corresponding release action will be performed.
- Otherwise, streams can be interrupted between any two "steps" of the stream. The steps themselves are atomic from FS2's perspective. 
  `Stream.eval(eff)` is a single step, `Stream.emit(1)` is one step, `Stream(1, 2, 3)` is one step (emitting a chunk), 
  and all other operations (e.g., `handleErrorWith`, `++`, `flatMap`) are multiple steps and can be interrupted.
- **Always use `bracket` or a function based on it, such as `onFinalize`, for providing resource cleanup logic 
  or any other logic you want to execute regardless of how the stream terminated. 
  Do not use `handleErrorWith` or `++` for this purpose.**

Consider a few examples of how this works, starting with the case of synchronous interruption:

```dotty
import fs2.*
import cats.effect.IO
import cats.effect.unsafe.implicits.global

case object Err extends Throwable

(Stream(1) ++ Stream(2).map(_ => throw Err)).take(1).toList
(Stream(1) ++ Stream.raiseError[IO](Err)).take(1).compile.toList.unsafeRunSync()
```

`take 1` uses `Pull` but does not explore the entire stream, and neither of these examples will ever throw an error. 
This is logical. A slightly more subtle point: the following code also **will never throw an error**:

```dotty
(Stream(1) ++ Stream.raiseError[IO](Err)).take(1).compile.toList.unsafeRunSync()
```

The reason is simple: the consumer (`take(1)`) terminates as soon as it receives an element. 
Once it receives it, it stops consuming the stream and does not trigger further steps, 
so the stream never terminates normally — it was interrupted before that could happen. 
In this case, we can see that there is nothing after the emitted `1`, but FS2 does not know this until it executes the next step of the stream.

If instead we use `onFinalize`, the code is guaranteed to execute, regardless of whether `take` interrupts:

```dotty
Stream(1)
  .covary[IO]
  .onFinalize(IO { println("finalizing!") })
  .take(1).compile.toVector.unsafeRunSync()
```

This covers synchronous interruptions. Now let's consider asynchronous interruptions. 
Let's look at what the result of `merged` could be in the following example:

```dotty
val s1 = (Stream(1) ++ Stream(2)).covary[IO]
val s2 = (Stream.empty ++ Stream.raiseError[IO](Err)).handleErrorWith { e =>
  println(e); Stream.raiseError[IO](e)
}
val merged = s1 merge s2 take 1
```

The result is highly **non-deterministic**. Here are some possible scenarios:

- `s1` may complete before the error in `s2` is detected, then nothing will be printed and no error will occur.
- `s2` may detect the error before `s1` emits anything. When the error is propagated through `s2`, 
  this will cause `merge` to terminate and asynchronously interrupt `s1`, and `take` will terminate with the same error.
- `s2` may detect the error before `s1` emits anything, but during the period when the error is being caught by `handleErrorWith`, 
  `s1` may emit a value, and `take(1)` may complete, initiating interruption of both `s1` 
  and `s2`, before the error is propagated, but **after** the exception has been printed! 
  In this case, the stream will still terminate without an error.

The correctness of your program must not depend on how different streams interleave, and again: 
do not use `handleErrorWith` or other interruptible functions for resource cleanup. 
Use `bracket` or `onFinalize` for this purpose.

---

**References:**

- [Fs2 home page][home]

[home]: https://fs2.io/#/
