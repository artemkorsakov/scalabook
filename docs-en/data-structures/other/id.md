# Id: Identity Monad

## The Effect of No Effect

In functional programming, [monads](../../typeclass/monad/monad.md) are often used to manage effects:
`Option` for missing values, `Either` for errors, `IO` for I/O.
But what if there is no effect? What if the value is already "pure" and doesn't need any wrapping?

The identity monad `Id` is a monad that **adds no effect**.
It simply wraps a value in itself, being a trivial wrapper that changes nothing.

## Type Definition of Id

`Id` is defined as a **type alias**:

```dotty
type Id[A] = A
```

This means that `Id[A]` is simply `A`. Values of type `A` can be freely used as values of type `Id[A]` and vice versa.

```dotty
val x: Id[Int] = 1 // x has type Int
val y: Int     = x // no conversion needed
```

Unlike other monads, `Id` does not require constructors (`Some`, `Right`, `IO(...)`),
since any value is already its own "wrapped" variant.

### Comparison with other monads

| Monad       | Type             | Constructor              | Effect           |
|-------------|------------------|--------------------------|------------------|
| `Id`        | `Id[A] = A`      | Any value                | No effect        |
| `Option`    | `Option[A]`      | `Some(a)` / `None`       | Missing value    |
| `Either`    | `Either[E, A]`   | `Right(a)` / `Left(e)`   | Errors           |
| `List`      | `List[A]`        | `List(a)` / `Nil`        | Multiplicity     |
| `IO`        | `IO[A]`          | `IO(a)` / `IO.delay(a)`  | I/O              |

## Id as a Monad

Since `Id` is just a type alias, we can treat it as a [`Monad`].
Let's look at the implementations of the main methods:

```dotty
given Monad[Id] with
  def unit[A](a: => A): Id[A] = a

  extension [A](fa: Id[A])
    def flatMap[B](f: A => Id[B]): Id[B] = f(fa)
```

The `unit` method simply returns the passed value — this is the **identity function**.

`flatMap` is just **function application** of `f` to value `fa`.

It's important to note that for `Id`, **`map` and `flatMap` have the same implementation**, since there is no additional monadic layer.

### Comparison of map and flatMap implementations

```dotty
// For Option monad
Option(1).map(_ + 1)                // Some(2)
Option(1).flatMap(x => Some(x + 1)) // Some(2)

// For Id (Identity)
1.map(_ + 1)     // 2 (just function application)
1.flatMap(_ + 1) // 2 (same thing!)
```

## Id as a Comonad

`Id` is also a [`Comonad`] — a structure dual to a monad.

```dotty
given CoMonad[Id] with
  def coUnit[A](fa: Id[A]): A =
    fa

  extension [A](fa: Id[A])
    def cobind[B](f: Id[A] => B): Id[B] =
      f(fa)
```

Since `Id[A] = A` and `Id[B] = B`, this is again straightforward.

### Signature comparison

```dotty
// All three methods have the same signature for Id!
map       [A, B](fa: Id[A])(f: A => B): Id[B]
flatMap   [A, B](fa: Id[A])(f: A => Id[B]): Id[B]
coflatMap  [A, B](fa: Id[A])(f: Id[A] => B): Id[B]

// Simplified (substituting Id[A] = A, Id[B] = B):
map       [A, B](fa: A)(f: A => B): B
flatMap   [A, B](fa: A)(f: A => B): B
coflatMap  [A, B](fa: A)(f: A => B): B
```

All three methods behave the same — simply applying a function to a value.

## Practical Application of Id

### 1. Effect Abstraction

`Id` is useful for writing polymorphic code that can work with different monads:

```dotty
def genericComputation[F[_]: Monad](value: Int): F[Int] =
  Monad[F].pure(value).map(_ + 1)

// With Id — no effect
genericComputation[Id](5) // 6

// With Option — with possible absence
genericComputation[Option](5) // Some(6)

// With List — with multiplicity
genericComputation[List](5) // List(6)
```

### 2. Testing and Prototyping

`Id` allows testing code written for monads without real effects:

```dotty
case class User(id: Long)

trait UserService[F[_]]:
  def getUser(id: Long): F[User]
  def saveUser(user: User): F[Unit]

// Test implementation with Id
class TestUserService extends UserService[Id]:
  private var users = Map.empty[Long, User]

  def getUser(id: Long): Id[User]    = users(id)
  def saveUser(user: User): Id[Unit] = users += (user.id -> user)
```

## Conclusion

**Id is an "effect-less" monad** that:

1. **Adds no behavior** — values are not wrapped in constructors
2. **Is a type alias** — `type Id[A] = A`
3. **Simplifies abstractions** — allows writing polymorphic code for any monads
4. **Useful for testing** — replaces real effects with pure values
5. **Trivially satisfies laws** — all laws reduce to identities
6. **Is both a monad and a comonad** — `map`, `flatMap` and `coflatMap` coincide

The use of `Id` is justified in situations where:

- You need to test code with abstract effects
- Composition without effects is needed as a base layer
- You are writing polymorphic code and want minimal context
- You need a monoidal structure for pure computations

`Id` is a "zero" effect, showing that monads are simply an abstraction
that can be applied to anything, even to ordinary values without effects.

## Implementation in Libraries

### Implementation in Cats

```dotty
import cats.syntax.functor.*
import cats.{Id, Monad}

import cats.Monad

def genericComputation[F[_]: Monad](value: Int): F[Int] =
  Monad[F].pure(value).map(_ + 1)

// With Id — no effect
genericComputation[Id](5) // 6
```

---

**Links:**

- [Link to exercises repository][sample]
- [Cats](https://typelevel.org/cats/datatypes/id.html)

[sample]: https://gitverse.ru/scalabook/typeclasses
