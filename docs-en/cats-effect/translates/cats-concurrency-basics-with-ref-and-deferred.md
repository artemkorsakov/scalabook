# Cats Concurrency Basics with Ref and Deferred

[Translation of the article "Cats Concurrency basics with Ref and Deferred", by Krzysztof Grajek][Ref and Deffered]

## Concurrent access and referential transparency

`Ref` and `Deferred` are the core building blocks of FP structures used concurrently. 
Especially when used with the tagless final abstraction, 
these two constructs, when building business logic, can provide us **with both concurrent access and referential transparency**, 
and we can use them to create more complex structures such as counters and state machines.

Before diving into `Ref` and `Deferred`, it is useful to know 
that concurrency in Cats is based on Java `AtomicReference`, and we'll begin our journey there.

## Atomic Reference

`AtomicReference` is one of the classes in the `java.util.concurrent.atomic` package, 
and in the [Oracle documentation](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/package-summary.html) 
we can read that `java.util.concurrent.atomic` is:

> A small package of lock-free, thread-safe classes supporting single-variable atomic operations. 
> Essentially, the classes in this package extend the notion of `volatile` fields, arrays, 
> to those that also provide an atomic conditional update operation.
> 
> ...
> 
> Instances of the classes [AtomicBoolean](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicBoolean.html), 
> [AtomicInteger](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicInteger.html), 
> [AtomicLong](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicLong.html)
> and [AtomicReference](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicReference.html)
> each provide access and updates to a single variable of the corresponding type.

`AtomicReference` has been with us since Java 1.5, and it is used to 
achieve **better performance than synchronization** (which is the case, but not always).

When you need to share some data between threads, you must protect access to that data. 
The simplest example is incrementing an `int`: `i = i + 1`. 
Our example actually consists of 3 operations: 
first we read the value of `i`, then we add `1` to that value, 
and finally we assign the newly computed value to `i`. 
In a multi-threaded application, it can happen that each thread performs these 3 steps interleaved with other threads' steps, 
and the final value of `i` becomes unpredictable.

Thoughts usually turn to the `synchronized` keyword or lock classes, 
but with `atomic.*` you no longer need to worry about explicit synchronization, 
and you can use the provided atomic utility types, 
where a check is automatically included to verify that the operation completes in one step.

Take `AtomicInteger.incrementAndGet` as an example:

```java
/**
 * Atomically increments by one the current value.
 *
 * @return the updated value
 */
public final int incrementAndGet() {
    for (;;) {
        int current = get();
        int next = current + 1;
        if (compareAndSet(current, next))
            return next;
    }
}
```

With the `compareAndSet` operation, we either update our data or fail, 
but we never force a thread to wait. 
Thus, if `compareAndSet` in `incrementAndGet` fails, 
we simply retry the entire operation, fetching the current data value in `get()`. 
On the other hand, with `synchronized`-like mechanisms, there are no limits on the number of operations 
that can be executed while a lock is held, 
but this block will never fail and can force the calling thread to wait, 
creating the possibility of deadlock or reduced performance.

Now that we've covered the basics, let's move on to our first concurrency power star.

## Ref

Cats `Ref` is very similar to the Java atomic reference mentioned above, 
the main differences being that the first is used with the `F` tagless final abstraction, 
the second is that it always contains a value, 
and the value held in `Ref` is of type `A`, which is an immutable value.

```dotty
abstract class Ref[F[_], A]:
  def get: F[A]
  def set(a: A): F[Unit]
  def modify[B](f: A => (A, B)): F[B]
  // ... and so on
```

`Ref[F[_], A]` is a purely functional mutable reference:

- concurrent
- lock-free
- and always contains a value

It is created by assigning an initial value, and each operation is wrapped in `F`, such as `cats.effect.IO`.

If you look closely at the companion object for Cats `Ref`, 
you'll see that `Sync[F]` must also be provided:

```dotty
def of[F[_], A](a: A)(implicit F: Sync[F]): F[Ref[F, A]] = F.delay(unsafe(a))
```

The method above is just one example of many operations available on `Ref`, 
it is used to construct a `Ref` with an initial value. 
`Sync` provides the ability to suspend any side effects 
via its `delay` method for each operation on `Ref`.

`Ref` is a fairly straightforward construct; we can focus mainly on 
its `get`, `set`, and `of` methods to understand how it works.

### The get-and-set approach

Suppose we have an object (let's call it `Shared`) that must be updated by multiple threads. 
We use `get` and `set` methods for this, creating a utility method to help us:

```dotty
def modifyShared(trace: Ref[IO, Shared], msg: String): IO[Unit] =
  for
    sh <- trace.get()
    _ <- trace.set(Shared(sh, msg))
  yield ()
```

Our `Shared` object can be created by 
applying its previous state and the new value to create a new instance.
`Shared` could actually be anything we want: a simple list, a map, 
or anything else we want concurrent-safe access to. 
I just invented `Shared(prev: Shared, msg: String)` for the purposes of this lesson.

`F` in the example above was replaced with the concrete `IO` from Cats Effect, 
but keep in mind that `Ref` is polymorphic on `F` and can be used with other libraries.

With our monadic `IO`, we execute `flatMap` at each step 
and set the value stored in our `Ref` to the desired one -- 
or... wait, maybe we don't.

With such an approach, when calling `modifyShared` concurrently, we can lose updates! 
This is because we might have a situation where, for example, 
two threads can both read the value via `get`, and each of them will execute `set` simultaneously. 
The `get` and `set` methods are not called atomically together.

### Atomic update

Of course, we can improve the example above and use other available methods from `Ref`. 
To perform `get` and `set` together, you can use `update`.

```dotty
def update(f: A => A): F[Unit]
```

This solves the value-update problem, but plain `update` has its own drawbacks. 
If we want to read the value immediately after updating it, similar to how we used `get` and `set`, 
we might end up reading stale data -- say, our `Ref` holds a simple `Int`:

```dotty
for
  _ <- someRef.update(_ + 1)
  curr <- someRef.get
  _ <- IO { println(s"current value is $curr") }
yield ()
```

### `modify` to the rescue

We can improve things slightly by using `modify`, which will do the same as `update`, 
but return the updated value for further use.

```dotty
def modify[B](f: A => (A, B)): F[B] =
  @tailrec
  def spin: B =
    val c = ar.get
    val (u, b) = f(c)
    if !ar.compareAndSet(c, u) then spin
    else b

  F.delay(spin)
```

As you can see, this is almost exactly the same implementation 
as in the `AtomicInteger.incrementAndGet` example shown earlier, just in Scala. 
You can notice that `Ref` is based on `AtomicReference` to do its job.

### Limitations of Ref

You've probably already noticed that if the update fails, 
the function passed to `update`/`modify` 
must run non-deterministically and possibly multiple times. 
The good news is that this approach turns out to be much faster overall 
than standard locking and synchronization mechanisms, 
and safer, since this approach cannot block.

Once we've learned how simple `Ref` works, we can move on to another Cats Concurrent class: `Deferred`.

## Deferred

Unlike `Ref`, `Deferred`:

- starts empty
- can be completed only once
- and once set, it cannot be modified or become empty again.

These properties make `Deferred` simple and quite interesting at the same time.

```dotty
abstract class Deferred[F[_], A]:
  def get: F[A]
  def complete(a: A): F[Unit]
```

`Deferred` is designed for **purely functional synchronization**: 
when we call `get` on an empty `Deferred`, it blocks until the value becomes available. 
[According to the class's own documentation](https://typelevel.org/cats-effect/docs/std/deferred), this blocking:

> is purely semantic; no actual threads are blocked by the implementation

The same `get` call on a non-empty `Deferred` immediately returns the stored value.

The other method -- `complete` -- will set the value if the instance is empty, 
but when called on a non-empty `Deferred` returns an error (a failed IO in the case of IO).

It is important to note here that `Deferred` requires `F` to be `Concurrent`, which means it can be cancelled.

A good example of using `Deferred` is when one part of your application needs to wait for another. 
The example below is taken from an excellent talk by Fabio Labella at Scala Italy 2019 -- 
[Composable Concurrency with Ref + Deferred](https://www.youtube.com/watch?v=-dLp3u6y2DQ).

```dotty
def consumer(done: Deferred[IO, Unit]) =
  for
    c <- Consumer.setup
    _ <- done.complete(())
    msg <- c.read
    _ <- IO(println(s"Received $msg"))
  yield ()

def producer(done: Deferred[IO, Unit]) =
  for
    p <- Producer.setup()
    _ <- done.get
    msg = "Msg A"
    _ <- p.write(msg)
    _ <- IO(println(s"Sent $msg"))
  yield ()

def prog =
  for
    d <- Deferred[IO, Unit]
    _ <- consumer(d).start
    _ <- producer(d).start
  yield ()
```

In the example above, we have a producer and a consumer, 
and we want the producer to wait for the consumer's setup to complete before starting to write messages, 
otherwise everything we write in the producer will be lost. 
To overcome this issue, we can share a `Deferred` instance 
and block on its `get` until the `Deferred` instance `done` is filled 
on the consumer side (in this case a simple `Unit` value of `()`).

Of course, the above solution is not without problems: 
if the consumer's setup never completes, we get stuck waiting, 
and the producer cannot send any messages. 
To overcome this, we can use a timeout on `get`, 
as well as use `Either[Throwable, Unit]` 
or some other construct instead of plain `Unit` inside the `Deferred`.

`Deferred` is fairly simple, but when combined with `Ref`, they can be used to 
create more complex data structures, such as semaphores, for instance.

For more information, it is recommended to visit 
[Cats' own documentation](https://typelevel.org/cats-effect/docs/getting-started), 
where you can learn more about concurrency in Cats and the data structures it provides.

---

**References:**

- [Original article - Cats Concurrency basics with Ref and Deferred][Ref and Deffered]

[Ref and Deffered]: https://blog.softwaremill.com/cats-concurrency-basics-with-ref-and-deferred-399e0335630
