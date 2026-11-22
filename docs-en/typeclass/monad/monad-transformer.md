# Monad Transformer

**A Monad Transformer** (or **monad transformer**) is a construct in functional programming
that allows combining multiple monads into one while preserving their individual properties.
This is especially useful when you need to work with nested contexts,
e.g., `Option[Future[A]]` or `Either[Error, Option[A]]`.

## Main Monad Transformers in Scala

- **`OptionT[F[_], A]`**:
    - Combines monad `F` with [`Option`](../../fp/handling-errors.md).
    - Useful for working with types like `F[Option[A]]`.
- **`EitherT[F[_], E, A]`**:
    - Combines monad `F` with [`Either[E, A]`](../../fp/handling-errors.md).
    - Useful for working with types like `F[Either[E, A]]`.
- **`ReaderT[F[_], R, A]`** (also known as `Kleisli`):
    - Combines monad `F` with `Reader` (a function `R => A`).
    - Useful for working with types like `R => F[A]`.
- **`WriterT[F[_], L, A]`**:
    - Combines monad `F` with [`Writer` (a value accompanied by a log)](../../fp/writer.md).
    - Useful for working with types like `F[(L, A)]`.
- **`StateT[F[_], S, A]`**:
    - Combines monad `F` with [`State` (state-dependent computations)](../../fp/state.md).
    - Useful for working with types like `S => F[(S, A)]`.

### Brief Examples

- **Asynchronous computations with an optional result**:
    - You have an asynchronous operation that returns `Future[Option[A]]`.
    - With `OptionT`, you can work with this type as a monad without manually unwrapping nested contexts.
- **Error handling in asynchronous computations**:
    - You have an asynchronous operation that returns `Future[Either[Error, A]]`.
    - With `EitherT`, you can handle errors and asynchronous computations within a single context.
- **Working with state and logs**:
    - You have a computation that depends on state (`State`) and is accompanied by a log (`Writer`).
    - With `StateT` and `WriterT`, you can combine these contexts for convenient work.

## Formal Definition

A Monad Transformer is a type `T[F[_], A]` where:

- `F[_]` is a base monad (e.g., `Option`, `Future`, `Either`).
- `T` is a transformer that adds an additional layer of context
  (e.g., `OptionT` for `Option`, `EitherT` for `Either`).

A Monad Transformer provides methods for working with nested monads,
such as `map`, `flatMap`, and `pure`, but within the context of a combined monad.

## Code

```dotty
trait MonadTransformer[T[_[_], _], M[_]: Monad as mMonad](using
    tMonad: Monad[[X] =>> T[M, X]]
):
  def lift[A](ma: M[A]): T[M, A]
```

## Exercises

### Exercise 2.19.1: Implement Monad for OptionT (Option monad transformer)

> [Repository with exercises and instructions][sample]

`OptionT[M, A]` is a monad transformer
that adds the `Option` effect (possible absence of a value) to the inner monad `M`.
This allows working with `Option` effects within the context of another monad (e.g., `IO`, `Future`, `Either`).

Structure: `OptionT[M, A] = M[Option[A]]` — the outer layer is the `M` effect, the inner layer is `Option`.

Operations:

- `unit`: lifts a value into `OptionT`, creating `M[Some(a)]`
- `flatMap`: sequentially applies a function, treating `None` as a short-circuit

Implement a `Monad` instance for `OptionT[M, *]`, assuming that `M` is a monad.

```dotty
import ru.scalabook.typeclasses.Monad

final case class OptionT[M[_], A](run: M[Option[A]])

given optionTMonad[M[_]: Monad as outerMonad]: Monad[[X] =>> OptionT[M, X]]
with
  override def unit[A](a: => A): OptionT[M, A] = ???

  extension [A](fa: OptionT[M, A])
    override def flatMap[B](f: A => OptionT[M, B]): OptionT[M, B] = ???
```

**Examples:**

```dotty
import Monad.given

// Suppose we have IO as a monad
type OptionIO[A] = OptionT[IO, A]

// Creating a value
val opt1: OptionIO[Int] = unit(42)
opt1.run  // IO(Some(42))

// flatMap with Some
val opt2 = opt1.flatMap(x => unit(x * 2))
opt2.run  // IO(Some(84))

// flatMap with a wrapped None
val none: OptionIO[Int] = OptionT(IO(None))
val result = none.flatMap(x => unit(x * 2))
result.run  // IO(None)

// A chain of transformations
val computation = for
  a <- unit(5)
  b <- unit(10)
  c <- unit(15)
yield a + b + c
computation.run  // IO(Some(30))
```

@:note(🔍 Show solution)

`unit` creates `OptionT[M, A]` by wrapping `Some(a)` into the monad `M` via `outerMonad.unit(Some(a))`.
In `flatMap` we run the current computation `fa.run`, obtaining `M[Option[A]]`.
Using the inner monad's `flatMap` we process the `Option`:

- `Some(value)` — apply `f` to the value and run the result
- `None` — return `None`, lifted into `M` via `outerMonad.unit(None)`

```dotty
given optionTMonad[M[_]: Monad as outerMonad]: Monad[[X] =>> OptionT[M, X]]
with
  override def unit[A](a: => A): OptionT[M, A] =
    OptionT[M, A](outerMonad.unit(Some(a)))

  extension [A](fa: OptionT[M, A])
    override def flatMap[B](f: A => OptionT[M, B]): OptionT[M, B] =
      OptionT(
        fa.run.flatMap {
          case Some(value) => f(value).run
          case None        => outerMonad.unit(None)
        }
      )
```

@:@

### Exercise 2.19.2: Implementing a MonadTransformer for OptionT

`MonadTransformer` is a type class for monad transformers
that provides the `lift` operation to lift computations from the inner monad `M` into the transformer `T[M, *]`.

For `OptionT` the `lift` operation lifts a value `M[A]` into `OptionT[M, A]`.

Implement a `MonadTransformer` instance for `OptionT`.

```dotty
given optionTMonadTransformer[M[_]: Monad]: MonadTransformer[OptionT, M] with
  override def lift[A](ma: M[A]): OptionT[M, A] = ???
```

**Examples:**

```dotty
import Monad.given

val io: IO[Int] = IO(42)
val optT: OptionT[IO, Int] = lift(io)
optT.run  // IO(Some(42))

// Lifting an empty value
val emptyIO: IO[Unit] = IO.unit
val emptyOptT = lift(emptyIO)
emptyOptT.run  // IO(Some(()))

// Usage in for-comprehensions
val result = for
  a <- lift(IO(10))
  b <- unit(20)
yield a + b
result.run  // IO(Some(30))
```

@:note(🔍 Show solution)

`lift` transforms `M[A]` into `OptionT[M, A]` using the inner monad's `map`:
we apply `Some(_)` to the value inside `ma`, lifting it into `Option`.
The outer effect `M` is preserved, and `Option` is added as an additional layer.

```dotty
given optionTMonadTransformer[M[_]: Monad]: MonadTransformer[OptionT, M] with
  override def lift[A](ma: M[A]): OptionT[M, A] =
    OptionT[M, A](ma.map(Some(_)))
```

@:@

### Exercise 2.19.3: Implementing a Monad for WriterT (Writer monad transformer)

`WriterT[M, W, A]` is a monad transformer that adds the `Writer` effect (accumulating a log of type `W`)
to the inner monad `M`. It is required that `W` is a monoid.

Structure: `WriterT[M, W, A] = () => M[(W, A)]` — a lazy computation returning a pair (log, value) in the context `M`.

Implement a `Monad` instance for `WriterT[M, W, *]`.

```dotty
final case class WriterT[M[_], W, A](run: () => M[(W, A)])

given writerTMonad[M[_]: Monad as outerMonad, W: Monoid as outerMonoid]
    : Monad[[X] =>> WriterT[M, W, X]] with
  override def unit[A](a: => A): WriterT[M, W, A] = ???

  extension [A](fa: WriterT[M, W, A])
    override def flatMap[B](f: A => WriterT[M, W, B]): WriterT[M, W, B] = ???
```

**Examples:**

```dotty
import Monad.given
import Monoid.given

type WriterIO[A] = WriterT[IO, String, A]

// Creating a value with an empty log
val w1: WriterIO[Int] = unit(42)
w1.run()  // IO(("", 42))

// flatMap with log accumulation
val w2 = w1.flatMap(x => WriterT(() => IO(("log1 ", x * 2))))
w2.run()  // IO(("log1 ", 84))

// A chain with multiple logs
val computation = for
  a <- WriterT(() => IO(("step1 ", 10)))
  b <- WriterT(() => IO(("step2 ", 20)))
  c <- WriterT(() => IO(("sum ", a + b)))
yield c
computation.run()  // IO(("step1 step2 sum ", 30))
```

@:note(🔍 Show solution)

`unit` creates a lazy computation that, when invoked, returns the pair `(outerMonoid.empty, a)` in the context `M`.
In `flatMap` we first run `fa.run()`, obtaining `M[(W, A)]`.
Using the inner monad's `flatMap` we extract the pair `(wa, a)`,
then run `f(a).run()`, obtaining `M[(W, B)]`,
and with `map` we combine the logs via `outerMonoid.combine(wa, wb)`, returning `(w, b)`.

```dotty
given writerTMonad[M[_]: Monad as outerMonad, W: Monoid as outerMonoid]
    : Monad[[X] =>> WriterT[M, W, X]] with
  override def unit[A](a: => A): WriterT[M, W, A] =
    WriterT[M, W, A](() => outerMonad.unit((outerMonoid.empty, a)))

  extension [A](fa: WriterT[M, W, A])
    override def flatMap[B](f: A => WriterT[M, W, B]): WriterT[M, W, B] =
      WriterT[M, W, B]: () =>
        fa.run().flatMap: (wa, a) =>
          f(a).run().map((wb, b) => (outerMonoid.combine(wa, wb), b))
```

@:@

### Exercise 2.19.4: Implementing a MonadTransformer for WriterT

For `WriterT` the `lift` operation lifts a value `M[A]` into `WriterT[M, W, A]`.

Implement a `MonadTransformer` instance for `WriterT`.

```dotty
given writerTMonadTransformer[M[_]: Monad, W: Monoid as outerMonoid]
    : MonadTransformer[[Y[_], X] =>> WriterT[Y, W, X], M] with
  override def lift[A](ma: M[A]): WriterT[M, W, A] = ???
```

**Examples:**

```dotty
val io: IO[Int] = IO(42)
val writerT: WriterT[IO, String, Int] = lift(io)
writerT.run()  // IO(("", 42))
```

@:note(🔍 Show solution)

`lift` creates a `WriterT` which, when invoked, applies `map` to `ma`,
transforming each value `a` into a pair `(outerMonoid.empty, a)`.
Thus the original value is preserved, and the log remains empty.

```dotty
given writerTMonadTransformer[M[_]: Monad, W: Monoid as outerMonoid]
    : MonadTransformer[[Y[_], X] =>> WriterT[Y, W, X], M] with
  override def lift[A](ma: M[A]): WriterT[M, W, A] =
    WriterT[M, W, A](() => ma.map(a => (outerMonoid.empty, a)))
```

@:@

### Exercise 2.19.5: Implementing a Monad for StateT (State monad transformer)

`StateT[M, S, A]` is a monad transformer that adds the `State` effect (mutable state of type `S`)
to the inner monad `M`.

Structure: `StateT[M, S, A] = S => M[(S, A)]` — a function taking an initial state
and returning a pair (new state, value) in the context `M`.

Implement a `Monad` instance for `StateT[M, S, *]`.

```dotty
import ru.scalabook.typeclasses.Monad

final case class StateT[M[_], S, A](run: S => M[(S, A)])

given stateTMonad[M[_]: Monad as outerMonad, S]
    : Monad[[X] =>> StateT[M, S, X]] with
  override def unit[A](a: => A): StateT[M, S, A] = ???

  extension [A](fa: StateT[M, S, A])
    override def flatMap[B](f: A => StateT[M, S, B]): StateT[M, S, B] = ???
```

**Examples:**

```dotty
import Monad.given

type StateIO[A] = StateT[IO, Int, A]

// Creating a value without changing state
val s1: StateIO[Int] = unit(42)
s1.run(5)  // IO((5, 42))

// flatMap with state passing
val increment: StateIO[Int] = StateT(s => IO((s + 1, s)))
val result = increment.flatMap(x => unit(x * 2))
result.run(10)  // IO((11, 20))

// for-comprehensions with state
val program = for
  a <- increment
  b <- increment
  c <- unit(a + b)
yield c
program.run(5)  // IO((7, 5+6=11))
```

@:note(🔍 Show solution)

`unit` creates a `StateT` that takes a state `s` and returns the pair `(s, a)` in the context `M` — the state does not change.
In `flatMap` we get the new state `s1` and the value `a` from `fa.run(s)`,
then run `f(a).run(s1)`, passing the updated state onwards.

```dotty
given stateTMonad[M[_]: Monad as outerMonad, S]
    : Monad[[X] =>> StateT[M, S, X]] with
  override def unit[A](a: => A): StateT[M, S, A] =
    StateT[M, S, A](s => outerMonad.unit((s, a)))

  extension [A](fa: StateT[M, S, A])
    override def flatMap[B](f: A => StateT[M, S, B]): StateT[M, S, B] =
      StateT[M, S, B]: s =>
        fa.run(s).flatMap((s1, a) => f(a).run(s1))
```

@:@

### Exercise 2.19.6: Implementing a MonadTransformer for StateT

For `StateT` the `lift` operation lifts a value `M[A]` into `StateT[M, S, A]`,
creating a computation that does not change state and returns the original value.

Implement a `MonadTransformer` instance for `StateT`.

```dotty
given stateTMonadTransformer[M[_]: Monad, S]
    : MonadTransformer[[Y[_], X] =>> StateT[Y, S, X], M] with
  override def lift[A](ma: M[A]): StateT[M, S, A] = ???
```

**Examples:**

```dotty
val io: IO[Int] = IO(42)
val stateT: StateT[IO, String, Int] = lift(io)
stateT.run("state")  // IO(("state", 42))

// Combining lifted values with state changes
val update: StateT[IO, Int, Unit] = StateT(s => IO((s + 1, ())))
val result = for
  a <- lift(IO(10))
  _ <- update
  b <- lift(IO(20))
yield a + b
result.run(5)  // IO((6, 30))
```

@:note(🔍 Show solution)

`lift` creates a `StateT` that takes a state `s`
and returns the pair `(s, a)` in the context `M`, where `a` is the value from `ma`.
The state remains unchanged, and `ma` runs in the context of the outer monad.

```dotty
given stateTMonadTransformer[M[_]: Monad, S]
    : MonadTransformer[[Y[_], X] =>> StateT[Y, S, X], M] with
  override def lift[A](ma: M[A]): StateT[M, S, A] =
    StateT[M, S, A](s => ma.map(a => (s, a)))
```

@:@

---

**References:**

- [Repository with exercises][sample]
- [A comprehensive introduction to Cats-mtl](https://typelevel.org/blog/2018/10/06/intro-to-mtl.html)
- [Cats: EitherT](https://typelevel.org/cats/datatypes/eithert.html)
- [Cats: IorT](https://typelevel.org/cats/datatypes/iort.html)
- [Cats: OptionT](https://typelevel.org/cats/datatypes/optiont.html)
- [Cats: StateT](http://typelevel.org/cats/datatypes/statet.html)
- [Cats: WriterT](https://typelevel.org/cats/datatypes/writert.html)
- [Learn Functional Programming course/tutorial on Scala](https://github.com/dehun/learn-fp)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Monad+transformers.html)
- [Scalaz Monad Transformers](https://noelwelsh.com/posts/scalaz-monad-transformers/)

[sample]: https://gitverse.ru/scalabook/typeclasses