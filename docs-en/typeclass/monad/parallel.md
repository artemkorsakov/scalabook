# Parallel

While studying various monads, you may have noticed that some data types share the same structure,
but instead of a monad provide an [`Applicative`](applicative.md) instance. A classic example is `Either` and `Validated`.

Why does this happen? The fact is that defining a `Monad` instance for data types like `Validated`
would be incompatible with their error-accumulation behavior. To understand this, we need to recall the fundamental difference:

- **[Monads](monad.md)** (`Monad`) describe **dependent computations** — the result of the next step depends on the result of the previous one (uses `flatMap`).
- **[Applicatives](applicative.md)** (`Applicative`) describe **independent computations** — computations can be executed in parallel
  and do not depend on each other (uses `mapN`).

With `Validated`, we want to accumulate all errors, not stop at the first one,
as `Either` does in a monadic context.
Therefore, `Validated` has an `Applicative` instance, but not a `Monad` instance.

However, in practice, there are often situations where you need to use both approaches together.
For example, you want to perform several independent validations (via `Applicative`),
and then continue computations in a monadic style (via `Monad`).
Manual conversion between types like `Either` and `Validated` is possible, but becomes cumbersome and repetitive.

## Example: Validating User Data

Consider a specific task. We have simple case classes representing user data:

```dotty
import cats.syntax.all.*
import cats.data.*

case class Name(value: String)
case class Age(value: Int)
case class Person(name: Name, age: Age)
```

And we have functions for parsing and validating string values:

```dotty
def parse(s: String): Either[NonEmptyList[String], Int] =
  if s.matches("-?[0-9]+") then Right(s.toInt)
  else Left(NonEmptyList.one(s"$s is not a valid integer."))

def validateAge(a: Int): Either[NonEmptyList[String], Age] =
  if a > 18 then Right(Age(a))
  else Left(NonEmptyList.one(s"$a is not old enough"))

def validateName(n: String): Either[NonEmptyList[String], Name] =
  if n.length >= 8 then Right(Name(n))
  else Left(NonEmptyList.one(s"$n Does not have enough characters"))
```

Now we want to parse two strings into a `Person` value.
Name and age validations are independent (we want to collect all errors), so `Applicative` is the logical choice.
However, parsing depends on the result of a previous step, so `Monad` is needed.

**The problem**: Without `Parallel`, we are forced to manually convert types:

```dotty
def parsePerson(ageString: String, nameString: String) =
  for
    age    <- parse(ageString)
    person <-
      (validateName(nameString).toValidated, validateAge(age).toValidated)
        .mapN(Person)
        .toEither
  yield person
```

We are forced to:

1. Convert `Either` to `Validated` using `.toValidated`
2. Use `mapN` for parallel combination
3. Convert back to `Either` using `.toEither`

This approach works, but becomes increasingly cumbersome as the number of combined `Either` values grows.

## Solution: the Parallel type class

To alleviate this pain, [Cats](https://typelevel.org/cats/) introduces the `Parallel` type class.
It abstracts over monads that also support parallel composition via a corresponding applicative.

```dotty
trait Parallel[M[_]]:
  type F[_]

  def sequential: F ~> M

  def parallel: M ~> F
```

Where:

- `M[_]` — a type with a `Monad` instance (e.g., `Either`)
- `F[_]` — a type with an `Applicative` instance (e.g., `Validated`)
- `sequential` — converts applicative `F` back to monad `M`
- `parallel` — converts monad `M` to applicative `F`

![Parallel typeclass converts between monad M[_] and applicative F[_].](../images/parallel.png)

Recall that `~>` is an alias for [`FunctionK`](../../cats-effect/index.md),
representing a natural transformation between container types.

## Using Parallel in Practice

With `Parallel`, our code becomes much cleaner:

```dotty
def parsePerson(ageString: String, nameString: String) =
  for
    age    <- parse(ageString)
    person <- (validateName(nameString), validateAge(age)).parMapN(Person)
  yield person
```

Instead of manual conversions, we simply use `.parMapN` instead of `.mapN`. Cats automatically:

1. Converts `Either` to the corresponding applicative (in this case `Validated`)
2. Applies `mapN` for parallel composition
3. Converts the result back to `Either`

### Traverse with Parallel

`Parallel` also allows using `parTraverse` and `parSequence` for traversing collections:

```dotty
List(
  Either.right(42),
  Either.left(NonEmptyList.one("Error 1")),
  Either.left(NonEmptyList.one("Error 2"))
).parSequence
// Left(NonEmptyList(Error 1, Error 2))
```

This code processes all elements of the list in parallel, accumulating errors in `NonEmptyList`,
instead of stopping at the first error (which is the behavior of ordinary `sequence` with a monad).

### Zipping Collections

`Parallel` is also useful for the `zip` operation on collections.
The standard `Applicative` instance for `List`, `Vector`, etc. behaves like the Cartesian product of collections:

```dotty
(List(1, 2, 3), List(4, 5, 6)).mapN(_ + _)
// Result: List(5, 6, 7, 6, 7, 8, 7, 8, 9) - all combinations
```

However, we often just want to "chain" collections element by element (like `zip`).
`Parallel` allows using `parMapN` for this:

```dotty
(List(1, 2, 3), List(4, 5, 6)).parMapN(_ + _)
// Result: List(5, 7, 9) - element-wise addition
```

In this case, `Parallel` for lists uses an alternative applicative instance
that implements `zip` instead of the Cartesian product.

## NonEmptyParallel: A Weakened Version of Parallel

Some types cannot form neither `Monad` nor `Applicative`,
because it is impossible to implement the `pure` function (which places a value into the context).
However, they may have instances of [`Bind`](bind.md) (without `unit`) or [`Apply`](apply.md) (without `unit`).

For such types, there is the `NonEmptyParallel` class.
A classic example is `ZipList`. `ZipList` is a list that allows combining values element by element,
but cannot be `Applicative`, because it is impossible to define `pure` for infinite lists.

Features of `NonEmptyParallel`:

- Cannot use `parTraverse` and `parSequence` (they require `pure` to return a result)
- Can use `parMapN` (similar to `Parallel`)
- `parNonEmptyTraverse` and `parNonEmptySequence` are available — counterparts of functions from [`NonEmptyTraverse`](non-empty-traverse.md),
  which work with non-empty structures

## Conclusion

The `Parallel` type class solves an important problem: it allows using both monadic composition (for dependent computations)
and applicative composition (for independent parallel computations) without manual type conversions. This makes code:

- **Cleaner** — boilerplate type-conversion code disappears
- **Safer** — fewer chances to err in manual conversions
- **More flexible** — easy to change the composition strategy (sequential vs. parallel)
- **More universal** — works with various data types through a unified interface

`Parallel` is especially useful for:

- Form validation with accumulation of all errors
- Parallel processing of collections
- Combining independent computations in a monadic context
- Element-wise work with collections (zip instead of Cartesian product)

---

**References:**

- [Link to the repository with exercises][sample]
- [Cats](https://typelevel.org/cats/typeclasses/parallel.html)

[sample]: https://gitverse.ru/scalabook/typeclasses
