# Random — Functional Random Number Generator

## What is `Random`?

`Random[F[_]]` is a **type class** providing a purely functional interface for generating random numbers.
It abstracts over various entropy sources (`scala.util.Random`, `java.util.Random`, `SecureRandom`, etc.)
and allows using them in the context of the `F` effect.

```dotty
trait Random[F[_]]:
  // Integers
  def nextInt: F[Int]
  def nextIntBounded(n: Int): F[Int] // from 0 (inclusive) to n (exclusive)
  def betweenInt(minInclusive: Int, maxExclusive: Int): F[Int]

  // Longs
  def nextLong: F[Long]
  def nextLongBounded(n: Long): F[Long]
  def betweenLong(minInclusive: Long, maxExclusive: Long): F[Long]

  // Other numbers
  ...
  
  // Boolean and bytes
  def nextBoolean: F[Boolean]
  def nextBytes(n: Int): F[Array[Byte]]

  // Working with collections
  def shuffleList[A](list: List[A]): F[List[A]]
  def shuffleVector[A](vector: Vector[A]): F[Vector[A]]

  // Generating strings (letters, digits, alphanumeric)
  def nextAlphaNumeric: F[Char]
  def nextPrintableChar: F[Char]
  def nextString(length: Int): F[String]

  // Distributions
  def nextGaussian: F[Double] // normal distribution (μ=0, σ=1)
end Random
```

**Key idea:** All operations return `F[A]`, which means
that generating a random number is an **effect** (not a pure function).
This is important because each call should potentially give a different result.

## Why Do We Need `Random` If We Have `scala.util.Random`?

| Aspect                              | `scala.util.Random`                                  | `cats.effect.std.Random`                                       |
|:------------------------------------|:-----------------------------------------------------|:---------------------------------------------------------------|
| **Purity**                          | ❌ Each call is a side effect (mutating state)        | ✅ Effect wrapped in `F`, referential transparency preserved   |
| **Platforms**                       | ❌ JVM only                                           | ✅ JVM, JS, Native                                              |
| **Entropy Source**                  | Fixed (`java.util.Random`)                           | Flexible: `ScalaRandom`, `JavaRandom`, `SecureRandom`          |
| **Thread-safety Control**            | ❌ May suffer from races                            | ✅ Cats Effect fibers don't conflict                           |
| **Composition**                      | ❌ Imperative                                         | ✅ Monadic (`flatMap`, `for`)                                  |

## Randomness Sources

`cats.effect.std.Random` can work on top of different generators. The choice depends on your needs:

### 1. `scala.util.Random` (default)

Uses `scala.util.Random` (under the hood — `java.util.Random` on JVM, its own on Scala.js).

```dotty
import cats.effect.IO
import cats.effect.std.Random

val random: IO[Random[IO]] = Random.scalaUtilRandom[IO]
```

**Characteristics:**

- Fast, but **not cryptographically secure** (predictable).
- Has internal state that mutates on each call.
- Thread-safe (uses CAS).

### 2. `java.util.Random` (JVM Only)

```dotty
import cats.effect.std.Random
import java.util.Random

val javaRandom: IO[Random[IO]] =
  Random.javaUtilRandom[IO](new java.util.Random(seed))
```

**Characteristics:**

- Faster than standard `scala.util.Random` in some scenarios.
- Also not cryptographically secure.

### 3. `java.security.SecureRandom` (Cryptographically Secure)

```dotty
import cats.effect.std.SecureRandom

val secureRandom: IO[Random[IO]] = SecureRandom.javaSecuritySecureRandom[IO]
```

**Characteristics:**

- Suitable for generating keys, tokens, passwords.
- **Slower** than regular generators.
- Blocks if system entropy is exhausted (can be configured).

## Usage Examples

### Basic: Dice Roll (d6)

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.{Console, Random}

object DiceRoll extends IOApp.Simple:
  def rollD6(using Random[IO]): IO[Int] =
    Random[IO].betweenInt(1, 7) // from 1 to 6 inclusive

  val run: IO[Unit] =
    for
      given Random[IO] <- Random.scalaUtilRandom[IO]
      roll             <- rollD6
      _                <- Console[IO].println(s"Rolled: $roll")
    yield ()
```

### Generating a Random Password

```dotty
def randomPassword(length: Int)(using Random[IO]): IO[String] =
  List.fill(length)(Random[IO].nextPrintableChar).sequence.map(_.mkString)

val program =
  for
    given Random[IO] <- Random.scalaUtilRandom[IO]
    pwd              <- randomPassword(12)
    _                <- IO.println(s"Your password: $pwd")
  yield ()
// Your password: "N}D.ETBr5{?  
```

### Shuffling a List

```dotty
val items = List("apple", "banana", "cherry", "date", "elderberry")
val shuffled: IO[List[String]] =
  for
    given Random[IO] <- Random.scalaUtilRandom[IO]
    shuffled         <- Random[IO].shuffleList(items)
    _                <- IO.println(s"Shuffled: $shuffled")
  yield shuffled
```

### Generating a UUID (Based on SecureRandom)

```dotty
import java.util.UUID

def randomUUID(using Random[IO]): IO[UUID] =
  for
    mostSig  <- Random[IO].nextLong
    leastSig <- Random[IO].nextLong
  yield new UUID(mostSig, leastSig)

val secureProgram =
  for
    given Random[IO] <- SecureRandom.javaSecuritySecureRandom[IO]
    uuid             <- randomUUID
    _                <- IO.println(s"UUID: $uuid")
  yield ()
// UUID: c4dd3d7a-305f-f461-c838-10b29d9dbbd1  
```

## Random and `Sync` — Why This Matters

All `Random` sources require `Sync[F]`. This is related to the fact that:

1. **Generating a random number is a side effect** (mutating the generator's state).
2. `Sync` provides `delay`, which allows wrapping impure code in an effect.

```dotty
// Simplified implementation of nextInt for scala.util.Random
def scalaUtilRandom[F[_]: Sync]: F[Random[F]] = Sync[F].delay {
  new Random[F]:
    private val rng     = new scala.util.Random()
    def nextInt: F[Int] = Sync[F].delay(rng.nextInt())
    // ...
}
```

## Comparison with Other Randomization Approaches in Scala

| Approach                                      | Purity         | Platforms        | Crypto-Security  | Performance       |
|:----------------------------------------------|:---------------|:-----------------|:-----------------|:------------------|
| `scala.util.Random`                           | ❌              | JVM              | ❌                | High              |
| `cats.effect.std.Random` (scalaUtilRandom)    | ✅              | JVM, JS, Native  | ❌                | High              |
| `cats.effect.std.SecureRandom`                | ✅              | JVM              | ✅                | Low               |
| `cats.effect.std.Random` (javaUtilRandom)     | ✅              | JVM              | ❌                | Very High         |
| `cats.effect.std.Random` (systemRandom)       | ✅              | All              | ❌                | Medium            |
| `IO(java.security.SecureRandom)` manually     | ❌ (sometimes)  | JVM              | Depends          | Low               |

## Summary: When to Use Which Source?

| Scenario                                                            | Recommended Source                                 |
|:--------------------------------------------------------------------|:---------------------------------------------------|
| **Games, simulations, predictable randomness**                      | `Random.scalaUtilRandom`                           |
| **High-performance generation (JVM)**                               | `Random.javaUtilRandom` (with a dedicated instance)  |
| **Generating tokens, keys, passwords**                              | `SecureRandom.javaSecuritySecureRandom`            |
| **Cross-platform app (minimal requirements)**                       | `Random.systemRandom`                              |

**Key idea:** `Random` is a functional, safe, and flexible abstraction for working with randomization in Cats Effect.
It allows choosing the right entropy source while writing clean, composable, and testable code.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Random](https://typelevel.org/cats-effect/docs/std/random)

[doc]: https://typelevel.org/cats-effect/docs/concepts
