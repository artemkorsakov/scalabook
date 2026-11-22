# Alleycats

## Overview

**Alleycats** is a module of the [Cats](https://github.com/typelevel/cats) project,
existing to support types that are not fully "law-abiding"
or pristine from a formal law perspective,
but which may be useful or necessary in some situations.

**Why "Alleycats"?**
The name plays on the word _alley cat_ (a stray, street cat) — in contrast to the "noble" cats from the main project.
This emphasizes the "dishonorable" status of the module.

### Motivation

In some cases, a type class instance may almost (but not completely) obey the required laws.
For example, the `Monad` instance for `scala.util.Try` — it violates functor/monad laws
due to catching exceptions in `map` and `flatMap`, but in practice it is often convenient to use.

In other cases, type classes without strict laws or constraints can still be useful.
For example, `Empty[_]` — a type class providing some notion of "emptiness".

Rather than engaging in endless debates about whether to include such types in main Cats,
the developers provided them a separate "home" — slightly dubious, but welcoming.

> **Important:** If a type class instance fully obeys the laws — it belongs in main Cats.
> Everything else lands here, in Alleycats.

## Type Classes Introduced by Alleycats

Alleycats introduces several new type classes absent from main Cats. Let us examine them in detail.

### `Empty[A]`, `Zero[A]`, and `One[A]`

**Problem:** In Cats, the notion of an "empty" value is represented by `Monoid[A]`,
which requires an associative binary operation (`combine`).
But sometimes the notion of emptiness exists independently of a specific addition/merge operation.

**Solution:** Alleycats introduces `Empty[A]` — a type class that provides access
to the _neutral element_ (empty value) via the `.empty` method.

**Additional capabilities:** If an instance of `Eq[A]` (equality comparison) is available,
then `Empty[A]` also provides `.isEmpty` and `.nonEmpty` methods to check whether a value is "empty".

**`Zero[A]` and `One[A]`:** These are analogs of `Empty`, but for ring-like structures from the `algebra.ring` package:

- `Zero[A]` corresponds to the _additive_ neutral element (`.zero` method).
- `One[A]` corresponds to the _multiplicative_ neutral element (`.one` method).

**Important requirement (law):** Although these type classes themselves have no formal laws,
they **must not** violate monoid laws when applicable. This means:

- If both `Empty[A]` and `Semigroup[A]` are available, then `Empty[A].empty` **must**
  be the neutral element for `Semigroup[A].combine`.
  Together these instances form a full `Monoid[A]` (and Alleycats has an implicit conversion that creates such an instance).
- The same rule applies to `Zero[A]` (with `Semigroup` or `AdditiveSemigroup`) and `One[A]` (with `MultiplicativeSemigroup`).

### `Pure[F[_]]` and `Extract[F[_]]`

**Problem:** Sometimes you want only part of the functionality of an existing type class,
but still guarantee compatibility if the remaining parts appear.

**`Pure[F]`:**

- Represents the `.pure` method from `Applicative[F]`, but without `.map` and `.ap` methods.
- If both `Pure[F]` and `Apply[F]` are available, they **must** be consistent
  and together provide a correct `Applicative[F]` instance.

**`Extract[F]`:**

- Represents the `.extract` method (extracting a value from context) from `Comonad[F]`, but without `.coflatMap` and `.map`.
- If both `Extract[F]` and `CoflatMap[F]` are available, they must provide a correct `Comonad[F]` instance.

**Practical meaning:** These type classes allow describing partial capabilities.
For example, you can write a function that only needs the ability to "lift" a value into context (`pure`),
and does not require a full `Applicative`.

### `EmptyK[F[_]]`

**`EmptyK[F]`** is a generalization of the `.empty[A]` method from `MonoidK[F]` at the type constructor level.

**Same principle:** if both `SemigroupK[F]` (merge at the container level) and `EmptyK[F]` (empty container) are available,
they must together provide a correct instance of `MonoidK[F]`.

**Example:** For `List`, `EmptyK` provides an empty list; for `Option` — `None`.
This allows using "empty" value operations for any container type.

## Type Class Instances Provided by Alleycats

Alleycats also provides several "dubious" type class instances for standard Scala types.

### Instances for `Set[_]`

**Problem:** `Set` in Scala relies on universal availability of `.hashCode` and `.equals` methods for elements.
This makes it difficult to use **parametricity** — the ability to reason about a code's behavior purely by its types,
without knowing concrete implementations. Because of this, there are doubts about the correctness of using `Set` with functors and monads.

**Why `Set` is not a law-abiding `Functor` (and thus not `Applicative`, `Monad`, etc.):**

Although in many cases `Set` does indeed behave like a `Functor`, there are edge cases where it does not.
For example, `Set` may violate the functor composition law:

```
fa.map(f).map(g) <-> fa.map(f.andThen(g))
```

This law states that applying two mappings sequentially should yield the same result
as applying their composition in one pass.

Since internally `Set` relies on universal hashing and comparison (via `hashCode` and `equals`),
it violates this law when working with data types that override `equals` and `hashCode` such that
their instances may appear equal but actually contain different values.

**Real example of violation:**

```dotty
import scala.concurrent.duration.*

val f: String => Duration = Duration.apply
val g: Duration => String = _.toString

val set = Set("1 minute", "60 seconds", "60000 milliseconds")

set.map(f).map(g)
// Result: Set("1 minute")
set.map(f.andThen(g))
// Result: Set("1 minute", "60 seconds", "60000 milliseconds")
```

In this example:
- The strings `"1 minute"`, `"60 seconds"`, and `"60000 milliseconds"` are parsed into equivalent `Duration` objects (all represent 60 seconds).
- For `Duration`, the `equals` method considers these objects equal, but `hashCode` may give different values for different representations.
- On the first mapping (`set.map(f).map(g)`), the intermediate `Set[Duration]` collapses equivalent values
  into one element (due to `equals`), and then converts it back to a string.
- On the second mapping (`set.map(f.andThen(g))`), the composition is applied in one pass,
  and `Set` does not get a chance to "collapse" equivalent elements, so the result contains all three strings.

**Another real example:** [CIString](https://typelevel.org/case-insensitive) — case-insensitive string,
where strings differing only in case are considered equal, but may have different hashes.

**What Alleycats provides:** Despite these issues, Alleycats provides `Monad[Set]` and `Traverse[Set]` instances.

**Import:** `import alleycats.std.set.*`

**Usage example:**

```dotty
import cats.syntax.all.*
import alleycats.std.set.*

val sets = Set(1, 2) flatMap (x => Set(x, x * 2))
// Result: Set(1, 2, 4) — but order is not guaranteed!
```

**Important warning:** Use `Monad[Set]` from Alleycats **at your own risk**!
Ensure that the data types you work with do not violate functor laws.
In particular, avoid using `Set` with types where `equals` and `hashCode`
may give contradictory results (e.g., `Duration`, `CIString`, or similar).
If you are unsure about your data types' behavior, it is better to avoid using `Set`
in functor/monad operations or to use other collections,
such as `List` or `Vector`, which do not violate laws.

### Instances for `Try[_]`

**Problem:** `scala.util.Try` was created to replace `try { ... } catch { ... }` blocks and simplify error handling,
as well as to pass exceptions as data.
However, because `Try` transparently catches exceptions in `.map` and `.flatMap`,
many developers doubt that `Try` obeys functor/monad laws.

**What Alleycats provides:** A `Monad[Try]` instance.

**Import:** `import alleycats.std.try_.*`

**Usage example:**

```dotty
import scala.util.Try
import cats.syntax.all.*
import alleycats.std.try_.*

val result = Try(10).map(_ / 2)
// Success(5)
val failed = Try(10).map(_ / 0)
// Failure(java.lang.ArithmeticException: / by zero)
```

### Instances for `Iterable[_]`

**Problem:** `scala.collection.Iterable[_]` in Scala does not guarantee immutability,
since it abstracts over both mutable and immutable collections.
However, this type is used, for example, to represent collection of values (`values`) in `Map`.
And it is often convenient to treat these values as a `Foldable` collection for folding.

**What Alleycats provides:** A `Foldable[Iterable]` instance.

**Import:** `import alleycats.std.iterable.*`

**Usage example:**

```dotty
import cats.syntax.all.*
import alleycats.std.iterable.*

// Merge all Map values into one string
val result = Map(1 -> "Apple", 2 -> "Orange").values.combineAll
// Result: "AppleOrange"
```

Note that `values` returns `Iterable[String]`, and thanks to the `Foldable[Iterable]` instance
we can use `.combineAll` (summation via monoid).

## Summary

| Component        | Purpose                                    | Key import                        |
|:-----------------|:-------------------------------------------|:----------------------------------|
| **Empty/Zero/One** | Neutral elements without operation binding   | `import alleycats.Empty.*`        |
| **Pure/Extract**   | Partial functors/applicatives/comonads       | `import alleycats.Pure.*`         |
| **EmptyK**         | Empty containers (MonoidK analog)            | `import alleycats.EmptyK.*`       |
| `Set[_]`           | Monad and traverse for sets                  | `import alleycats.std.set.*`      |
| `Try[_]`           | Monad for Try                                | `import alleycats.std.try_.*`     |
| `Iterable[_]`      | Foldable for Iterable                        | `import alleycats.std.iterable.*` |

---

**References:**

- [Cats home page][home]

[home]: https://typelevel.org/cats/
