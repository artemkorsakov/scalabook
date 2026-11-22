# Jump Start Guide

This jump start guide barely scratches the surface of what Cats can do,
but provides a concise practical introduction to the patterns you will most likely want to use in your Scala project.

If you work daily with constructs like
[`Future`](http://www.scala-lang.org/api/current/scala/concurrent/Future.html),
[`Option`](http://www.scala-lang.org/api/current/scala/Option.html)
or [`Either`](http://www.scala-lang.org/api/current/scala/util/Either.html),
Cats is very likely able to simplify and improve the readability of your code.

> **Note:** This guide uses Cats version 2.13.0.

Let us walk through the library package by package, examining the syntax available in each.

## Helper Methods for Option

```dotty
import cats.syntax.all.*
```

Importing this package adds the `obj.some` syntax — equivalent to `Some(obj)`.
The only real difference is that the value is already cast to type `Option[T]` (not `Some[T]`),
which helps the compiler with type inference.

Using `obj.some` instead of `Some(obj)` can improve readability,
for example, when you need to provide a dummy implementation of a service method for testing purposes.

Consider this example. If you bring into scope the following implicit class:

```dotty
import scala.concurrent.Future

extension [T](obj: T)
  def asFuture: Future[T] = Future.successful(obj)
```

then you can use a chain like the one below:

```dotty
class Account { /* ... */ }

trait AccountService:
  def getAccountById(id: Int): Future[Option[Account]]

class DummyAccountServiceImpl extends AccountService:

  def dummyAccount: Account = ???

  /* ... */
  override def getAccountById(id: Int): Future[Option[Account]] =
    dummyAccount.some.asFuture
  /* ... */

```

This is much more readable than `Future.successful(Some(dummyAccount))`, especially if this pattern often repeats.
The chain `.some.asFuture` at the end allows you to focus on what exactly is returned,
rather than what wrapper it is packed into.

`none[T]`, in turn, is shorthand for `Option.empty[T]`,
that is, `None`, but already cast from `None.type` to `Option[T]`.
Specifying a more specific type sometimes helps the Scala compiler to correctly infer the type of expressions containing `None`.

## Helper Methods for Either

```dotty
import cats.syntax.all.*
```

`obj.asRight` is `Right(obj)`, and `obj.asLeft` is `Left(obj)`.
In both cases, the return type extends from `Right` or `Left` to `Either`.
As with `.some`, these helper methods conveniently combine with `.asFuture` to improve readability:

```dotty
case class User(accountId: Long) { /* ... */ }

trait UserService:
  def ensureUserExists(id: Int): Future[Either[Exception, User]]

class UserServiceSpec extends UserService:

  def dummyUser: User = ???

  /* ... */
  override def ensureUserExists(id: Int): Future[Either[Exception, User]] =
    dummyUser.asRight.asFuture // instead of Future.successful(Right(dummyUser))
  /* ... */
```

`Either.fromOption(option: Option[A], ifNone: => E)`, in turn,
is a useful helper method for converting `Option` to `Either`.
If the passed `option` equals `Some(x)`, the result will be `Right(x)`.
Otherwise, `Left` with the specified `ifNone` value is returned.

## Tuples and `mapN`

The `apply` package provides `(..., ..., ...).mapN` syntax, which allows intuitively applying a function
that takes multiple parameters to multiple effectful values (e.g., future results).

Suppose we have 3 futures: one of type `Int`, one of type `String`, and one of type `User`,
plus a method that takes three parameters — `Int`, `String`, and `User`.

```dotty
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class User

class ProcessingResult { /* ... */ }

def intFuture: Future[Int]       = /* ... */ ???
def stringFuture: Future[String] = /* ... */ ???
def userFuture: Future[User]     = /* ... */ ???

def process(
    value: Int,
    contents: String,
    user: User
): ProcessingResult = /* ... */ ???
```

Our goal is to apply a function to values computed by these three futures.
With `apply` syntax, this becomes very simple and concise:

```dotty
import cats.syntax.all.*

def processAsync: Future[ProcessingResult] =
  (intFuture, stringFuture, userFuture).mapN {
    (value, contents, user) =>
      process(value, contents, user)
  }
```

Implicit instances (namely, `Functor[Future]` and `Semigroupal[Future]`, which are required for `mapN` to work) are always visible by default.
They are in their respective companion objects, so there is no need to import them explicitly.

The same idea can be expressed even more concisely:

```dotty
def processAsync2: Future[ProcessingResult] = (intFuture, stringFuture, userFuture).mapN(process)
```

If any of the chained futures completes with an error,
the resulting future will also complete with that same error (this is **fail-fast** behavior).

**Important:** all futures will be executed in parallel, unlike what happens in a `for`-comprehension:

```dotty
def processAsync3: Future[ProcessingResult] =
  for
    value    <- intFuture
    contents <- stringFuture
    user     <- userFuture
  yield process(value, contents, user)
```

In the code above (which is translated under the hood into `flatMap` and `map` calls),
`stringFuture` will not start until `intFuture` completes successfully,
and similarly `userFuture` will be started only after `stringFuture` completes.
But since these computations are independent of each other, they can quite well be run in parallel using `mapN`.

## Traverse (Collection Traversal)

```dotty
import cats.syntax.all.*
```

### `traverse`

If you have an instance `obj` of type `F[A]` that can be mapped (e.g., `Future`),
and a function `fun` of type `A => G[B]`, calling `obj.map(fun)` will yield `F[G[B]]`.
In many common real-world cases, for example when `F` is `Option` and `G` is `Future`,
you get `Option[Future[B]]`, which is most likely not what you need.

`traverse` comes to the rescue.
If you call `traverse` instead of `map`, e.g. `obj.traverse(fun)`, you get `G[F[A]]`,
which in our case is `Future[Option[B]]` — much more useful and easier to handle than `Option[Future[B]]`.

```dotty
def updateUser(user: User): Future[User] = /* ... */ ???

def updateUsers(users: List[User]): Future[List[User]] =
  users.traverse(updateUser)
```

**Note:** There is also a special method `Future.traverse` in the [`Future`](http://www.scala-lang.org/api/current/scala/concurrent/Future$.html) companion object,
but the Cats version is much more readable and can work with any structure
for which certain type classes are available.

### `sequence`

`sequence` represents an even simpler concept:
you can think of it as a simple type swap from `F[G[A]]` to `G[F[A]]`
without mapping the nested value (unlike `traverse`).

```dotty
val foo: List[Future[String]] = List(Future("hello"), Future("world"))
val bar = foo.sequence // type will be Future[List[String]]
```

In Cats, `obj.sequence` is actually implemented as `obj.traverse(identity)`.

On the other hand, `obj.traverse(fun)` is roughly equivalent to `obj.map(fun).sequence`.

### `flatTraverse`

If you have `obj` of type `F[A]` and a function `fun` of type `A => G[F[B]]`,
calling `obj.map(fun)` will give `F[G[F[B]]]` — extremely unlikely to be what you wanted.

Running traverse on the object instead of mapping helps a bit: you get `G[F[F[B]]]`.
Since `G` is usually something like `Future` and `F` is `List` or `Option`,
you would get `Future[Option[Option[A]]]` or `Future[List[List[A]]]` — quite inconvenient to handle.

```dotty
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

lazy val valueOpt: Option[Int]                    = /* ... */ ???
def compute(value: Int): Future[Option[Int]]      = /* ... */ ???
def computeOverValue: Future[Option[Option[Int]]] =
  valueOpt.traverse(compute)
// has type Future[Option[Option[Int]]], not great
```

The solution may be to apply `_.flatten` to the result:

```dotty
def computeOverValue2: Future[Option[Int]] = valueOpt.traverse(compute).map(_.flatten)
```

Thus you get the desired type `G[F[B]]`.

However, there is a convenient shorthand for this — `flatTraverse`:

```dotty
def computeOverValue3: Future[Option[Int]] = valueOpt.flatTraverse(compute)
```

And that solves our problem completely.

## Monad Transformers

### OptionT

```dotty
import cats.data.OptionT
```

An instance of [`OptionT[F, A]`](../../typeclass/monad/monad-transformer.md) can be thought of as a wrapper over `F[Option[A]]`,
which adds a few useful methods specific to nested types that are not available in `F` or `Option` themselves.
Most often your `F` will be `Future` (or sometimes `DBIO` from Slick, but for this you will need a Cats type class implementation,
such as `Functor` or `Monad`, for `DBIO`).
Wrappers like `OptionT` are usually called [**monad transformers**](../../typeclass/monad/monad-transformer.md).

A very common pattern is mapping the inner value
stored in `F[Option[A]]` to `F[Option[B]]` with a function of type `A => B`.
This can be done with rather verbose syntax:

```dotty
lazy val resultFuture: Future[Option[Int]] = ???

def mappedResultFuture: Future[Option[String]] =
  resultFuture.map: maybeValue =>
    maybeValue.map: value =>
      // Do something with value and return String
      ???
```

With `OptionT` this can be simplified:

```dotty
def mappedResultFuture2: OptionT[Future, String] =
  OptionT(resultFuture).map: value =>
    // Do something with value and return String
    ???
```

The `map` call above will return a value of type `OptionT[Future, String]`.

To get the base value `Future[Option[String]]`, simply call `.value` on the `OptionT` instance.
It is also entirely viable to fully switch to using `OptionT[Future, A]`
in method parameter/return types and completely (or almost completely) abandon `Future[Option[A]]` in type declarations.

#### Ways to create OptionT

|                    Method                    |    Accepts     |     Returns     |
|:----------------------------------------------|:--------------:|:---------------:|
| `OptionT.apply` or `OptionT(...)`             | `F[Option[A]]` | `OptionT[F, A]` |
|      `OptionT.fromOption`                     |  `Option[A]`   | `OptionT[F, A]` |
|          `OptionT.liftF`                      |     `F[A]`     | `OptionT[F, A]` |
|            `OptionT.pure`                     |      `A`       | `OptionT[F, A]` |

In production code, you will most often use the `OptionT(...)` syntax
to wrap an instance of `Future[Option[A]]` into `OptionT[F, A]`.
The other methods, in turn, are useful for configuring dummy `OptionT` values in unit tests.

#### OptionT Methods

|       Method       |       Accepts        |     Returns     |
|:------------------:|:--------------------:|:---------------:|
|     `map[B]`       |       `A => B`       | `OptionT[F, B]` |
| `subflatMap[B]`    |   `A => Option[B]`   | `OptionT[F, B]` |
| `semiflatMap[B]`   |     `A => F[B]`      | `OptionT[F, B]` |
|  `flatMapF[B]`     | `A => F[Option[B]]`  | `OptionT[F, B]` |
|   `flatMap[B]`     | `A => OptionT[F, B]` | `OptionT[F, B]` |

In practice, you will most likely use `map` and `semiflatMap`.

As always with `flatMap` and `map`, you can use them not only explicitly, but also inside `for`-comprehensions, as in the example below:

```dotty
class Money { /* ... */ }

def findUserById(userId: Long): OptionT[Future, User] = /* ... */ ???

def findAccountById(accountId: Long): OptionT[Future, Account] = /* ... */ ???

def getReservedFundsForAccount(account: Account)
    : OptionT[Future, Money] = /* ... */ ???

def getReservedFundsForUser(userId: Long): OptionT[Future, Money] =
  for
    user    <- findUserById(userId)
    account <- findAccountById(user.accountId)
    funds   <- getReservedFundsForAccount(account)
  yield funds
```

The `OptionT[Future, Money]` instance returned by `getReservedFundsForUser` will contain `None`
if any of the three called methods returns an `OptionT` corresponding to `None`.
Otherwise, if all three calls return `Some`, the final result will also contain `Some`.

### EitherT

```dotty
import cats.data.EitherT
```

`EitherT[F, A, B]` is a [monad transformer](../../typeclass/monad/monad-transformer.md) for `Either`.
It can be thought of as a wrapper over a value of `F[Either[A, B]]`.

#### Ways to create EitherT

|                 Method                 |    Accepts     |                           Returns                           |
|:----------------------------------------|:--------------:|:-----------------------------------------------------------:|
| `EitherT.apply` or `EitherT(...)`       | `F[Either[A, B]]` |              `EitherT[F, A, B]`              |
|         `EitherT.fromEither`            |  `Either[A, B]`   |   `EitherT[F, A, B]` (wraps passed `Either` in `F`)   |
| `EitherT.right` or `EitherT.liftF`      |      `F[B]`       |  `EitherT[F, A, B]` (wraps value in `F[B]` in `Right`)   |
|           `EitherT.left`                |      `F[A]`       |  `EitherT[F, A, B]` (wraps value in `F[A]` in `Left`)    |
|           `EitherT.pure`                |        `A`        | `EitherT[F, A, B]` (wraps value in `Right`, then in `F`) |

Another useful way to create `EitherT` is using `toLeft` and `toRight` methods from `OptionT`:

```dotty
abstract class BaseException(message: String) extends Exception(message)

case class UserNotFoundException(message: String) extends BaseException(message)

def getUserById(userId: Int): Future[Option[User]] = /* ... */ ???

def ensureUserExists(userId: Int): EitherT[Future, BaseException, User] =
  OptionT(getUserById(userId))
    .toRight(left = UserNotFoundException(s"user not found, userId=$userId"))
```

`toRight` is quite similar to the `Either.fromOption` method mentioned earlier:
just as `fromOption` builds an `Either` from `Option`, `toRight` creates an `EitherT` from `OptionT`.
If the original `OptionT` holds a `Some` value, it will be wrapped in `Right`;
otherwise, the value passed as the `left` parameter will be wrapped in `Left`.
There is a corresponding method `toRightF` for passing the `left` value inside the monad.

`toLeft` is an analog of `toRight` that wraps a `Some` value in `Left` and converts `None` to `Right`,
containing the passed `right` value.
In practice, this is less commonly used, but may come in handy,
for example, for uniqueness checking: we return `Left` if a value is found, and `Right` if it is not yet in the system.

#### EitherT Methods (right-biased)

`EitherT` is _right-biased_, so the regular `map` works with the `Right` value.

|       Method       | Accepts               | Returns          |
|:------------------:|-----------------------|------------------|
|     `map[D]`       | `B => D`              | `EitherT[F, A, D]` |
| `subflatMap[D]`    | `B => Either[A, D]`   | `EitherT[F, A, D]` |
| `semiflatMap[D]`   | `B => F[D]`           | `EitherT[F, A, D]` |
|  `flatMapF[D]`     | `B => F[Either[A, D]]` | `EitherT[F, A, D]` |
|   `flatMap[D]`     | `B => EitherT[F, A, D]` | `EitherT[F, A, D]` |

> `EitherT` also has methods working with `Left`, e.g., `leftMap`, or with both sides, e.g. `fold` and `bimap`.

#### Practical Example: Chain of Checks with Fail-Fast

```dotty
case class Item(state: String)
class ItemOrder { /* ... */ }

case class ItemNotFoundException(message: String) extends BaseException(message)
case class InvalidItemStateException(message: String)
    extends BaseException(message)

def getItemById(itemId: Int): Future[Option[Item]] = /* .. */ ???

def ensureItemExists(itemId: Int): EitherT[Future, BaseException, Item] =
  OptionT(getItemById(itemId))
    .toRight(ItemNotFoundException(s"item not found, itemId = $itemId"))

def ensureItemStateIs(
    actual: String,
    expected: String
): EitherT[Future, BaseException, Unit] =
  // Returns Unit wrapped in Right, then Future, if condition is true,
  // otherwise — exception wrapped in Left, then Future.
  EitherT.cond(
    actual == expected,
    (),
    InvalidItemStateException(s"actual=$actual, expected=$expected")
  )

def placeOrderForItem(
    userId: Int,
    itemId: Int,
    count: Int
): Future[ItemOrder] = /* ... */ ???

def buyItem(
    userId: Int,
    itemId: Int,
    count: Int
): EitherT[Future, BaseException, ItemOrder] =
  for
    user <- ensureUserExists(userId)
    item <- ensureItemExists(itemId)
    _    <- ensureItemStateIs(item.state, "AVAILABLE_IN_STOCK")
    // EitherT.liftF is needed to convert Future[ItemOrder] to EitherT[Future, BaseException, ItemOrder]
    placedOrder <- EitherT.liftF(placeOrderForItem(userId, itemId, count))
  yield placedOrder
```

In this example, we sequentially perform checks.
If any check fails, the resulting `EitherT` will contain `Left`.
Otherwise (all checks yield `Right`), the final result will also contain `Right`.
This is **fail-fast** behavior: the `for`-comprehension flow stops at the first `Left` result.

If you need error-accumulating validation (for example, when working with user-entered form data),
`cats.data.Validated` may be a good choice.

## Imports

The simplest approach to Cats imports is to import everything you usually need:

```dotty
import cats.*
import cats.data.*
import cats.syntax.all.*
```

**`cats.*`** — brings many [type classes](../../typeclass/index.md) (interface analogs),
such as [`Monad`](../../typeclass/monad/monad.md), [`Semigroup`](../../typeclass/monoid/semigroup.md),
[`Foldable`](../../typeclass/monad/foldable.md) and others.
Instead of the entire `cats` package, you can import only the types you need, e.g.:

```dotty
import cats.Monad
import cats.Semigroup
import cats.Foldable
```

**`cats.data.*`** — brings data structures,
such as [`Validated`](../../fp/handling-errors.md) and [`State`](../../fp/state.md).
Instead of the entire package, you can import only what you need:

```dotty
import cats.data.Validated
import cats.data.State
```

**`cats.syntax.all.*`** — adds syntax enrichment to some types,
providing convenient methods such as right-biased combinators for `Either`:

```dotty
import cats.syntax.all.*

// Cats adds right-biased combinators to standard Either
val e: Either[String, Int] = Right(3)
e.map(_ + 1)

// Cats adds orEmpty method to standard Option
val o: Option[String] = None
o.orEmpty
```

If you prefer to import only what you need, you can import from `cats.syntax` for syntax enrichment.
For example, to import syntax for `Semigroup`:

```dotty
import cats.syntax.semigroup.*

"Hello, " |+| "World!"
```

This import adds the `|+|` syntax.

## What is `tailRecM`?

### General Idea

The `FlatMap` type class contains the `tailRecM` method with the following signature:

```dotty
def tailRecM[A, B](a: A)(f: A => F[Either[A, B]]): F[B]
```

**What does this method do?**

`tailRecM` (short for _tail-recursive monad_) is a method
that allows performing recursive computations in a monadic context without the risk of stack overflow.
It takes an initial value of type `A` and a function `f` that for each value `A` returns an effect `F` containing either:

- `Left[A]` — continue recursion with a new value `A`;
- `Right[B]` — complete computation and return result `B`.

In short, this is a monadic analog of tail recursion that guarantees stack safety with multiple nested `flatMap` calls.

### Properties of a Law-Abiding `tailRecM`

When defining a `FlatMap` instance, its `tailRecM` implementation must satisfy two properties
for the instance to be considered "law-abiding".

#### First Property: Semantic Equivalence

`tailRecM` should return the same result you would get
if you recursively called `flatMap` until you got a `Right` value
(assuming you had an unlimited stack).

In other words, `tailRecM` should give the same result as this naive implementation:

```dotty
trait Monad[F[_]]:
  def pure[A](x: A): F[A] =
    ???
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] =
    ???

  def tailRecM[A, B](a: A)(f: A => F[Either[A, B]]): F[B] =
    flatMap(f(a)) {
      case Right(b)    => pure(b)
      case Left(nextA) => tailRecM(nextA)(f)
    }
```

**How it works:**

1. Call `f(a)`, get `F[Either[A, B]]`.
2. Extract the result with `flatMap`:
    - If it is `Right(b)` — computation is complete, return `pure(b)`.
    - If it is `Left(nextA)` — recursively call `tailRecM(nextA)(f)`.

#### Second Property: Stack Safety

Why can't we just use this naive implementation for all type constructors?
And why is `tailRecM` even useful?

The reason is that for many monadic types, recursive `flatMap` in this way **will quickly exhaust the stack**.
Each recursive call adds a new frame to the call stack,
and when the number of iterations reaches a few thousand (usually around 2000–5000),
**stack overflow** (`StackOverflowError`) occurs.

### Example: `Option`

`Option` is one example of a monadic type where `flatMap` consumes the stack such that
deeply nested `flatMap` calls lead to stack overflow.

However, we can provide a stack-safe implementation of `tailRecM` for `Option` using tail recursion:

```dotty
import cats.FlatMap
import scala.annotation.tailrec

given FlatMap[Option] = new FlatMap[Option]:
  def map[A, B](fa: Option[A])(f: A => B): Option[B] =
    fa.map(f)
  def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] =
    fa.flatMap(f)

  @tailrec
  def tailRecM[A, B](a: A)(f: A => Option[Either[A, B]]): Option[B] =
    f(a) match
      case None           => None
      case Some(Left(a1)) => tailRecM(a1)(f)
      case Some(Right(b)) => Some(b)
```

**Why this works:**

- We use the `@tailrec` annotation, which guarantees that the recursive call `tailRecM(a1)(f)`
  is **tail** — i.e., it is the last operation in the function.
- The Scala compiler optimizes the tail call into a loop, which prevents stack growth.
- Now we do not need to worry about stack overflow, no matter how many
  times we need to call `tailRecM` until getting `Right`.

### Why is this useful?

Any operation you could write using recursive `flatMap`
can be rewritten using `tailRecM`.
If the `FlatMap` instance for your type constructor is law-abiding,
you do not need to worry about stack safety — `tailRecM` will handle it for you.

**Typical use cases:**

- Implementing loops in monadic code (e.g., reading data until an end marker is encountered).
- Processing nested data structures without the risk of stack overflow.
- Writing recursive algorithms in a functional style where recursion depth is not known in advance.

### Implementation Challenges

The flip side: writing a law-abiding `tailRecM` for your type constructor is not always obvious.

| Type Constructor                    | What to do                                                                                                                                          |
|:------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------|
| **`Future`**                        | Recursive `flatMap` is already safe (executed asynchronously). You can use the naive implementation.                                                |
| **`Option` and `Try`**              | Need to organize recursion so that `tailRecM` calls are tail calls (use `@tailrec`).                                                                 |
| **Collections (`List`, `Vector`, etc.)** | Require a different approach. For example, Cats' implementation for `List` uses an accumulator and reverse traversal to achieve stack safety. |

**Tip:** If you have trouble implementing a law-abiding `tailRecM`,
find an instance in Cats itself for a type semantically similar to yours.
All `FlatMap` instances provided by Cats have law-abiding and stack-safe `tailRecM` implementations.

### What to do if `tailRecM` implementation is impossible?

In some cases, you may conclude that providing a law-abiding `tailRecM` is impractical
or even impossible (if so, Cats developers would be glad to hear about it).
For such cases, there is a way to test all monad laws **except** for `tailRecM` stack-safety:

**Instead of:**

```dotty
MonadTests[F].monad[A, B, C]
```

**Use:**

```dotty
MonadTests[F].stackUnsafeMonad[A, B, C]
```

This allows checking correctness of monadic operations (associativity of `flatMap`, laws of `pure`, etc.),
but skips the `tailRecM` stack-safety check.
This approach is useful for types where implementing a stack-safe `tailRecM` is difficult or impossible,
but you still want to check the other laws.

### Quick Summary

| Aspect                | Description                                                                                                                |
|:----------------------|:---------------------------------------------------------------------------------------------------------------------------|
| **What is `tailRecM`?** | Monad analog of tail recursion for stack-safe recursive computations.                                                    |
| **Signature**         | `def tailRecM[A, B](a: A)(f: A => F[Either[A, B]]): F[B]`                                                                 |
| **Main purpose**      | Prevent stack overflow with deep recursive `flatMap` calls.                                                                |
| **When to use**        | In any recursive algorithms within a monadic context.                                                                      |
| **How to check**      | `MonadTests[F].monad[A, B, C]` for full check, `MonadTests[F].stackUnsafeMonad` — if `tailRecM` is not implemented.      |
| **Where to find examples** | In Cats source code — all monads have stack-safe `tailRecM` implementations.                                             |

---

**References:**

- [Cats home page][home]

[home]: https://typelevel.org/cats/
