# Glossary

## Symbol Meanings

Below is a list of symbols used in the Cats library.

The symbols `~>`, `⊥`, `⊤`, `:<:`, and `:≺:` can be imported using `import cats.*`.

All other symbols can be imported using `import cats.syntax.all.*`.

## Cats Operators and Symbols

| Symbol               | Name                                                | Type Class            | Method Signature                                            |
|:---------------------|:----------------------------------------------------|:----------------------|:------------------------------------------------------------|
| `fa *> fb`           | product right                                       | `Apply[F[_]]`         | `productR(fa: F[A])(fb: F[B]): F[B]`                        |
| `fa <* fb`           | product left                                        | `Apply[F[_]]`         | `productL(fa: F[A])(fb: F[B]): F[A]`                        |
| `x === y`            | equality                                            | `Eq[A]`               | `eqv(x: A, y: A): Boolean`                                  |
| `x =!= y`            | inequality                                          | `Eq[A]`               | `neqv(x: A, y: A): Boolean`                                 |
| `fa >>= f`           | flatMap                                             | `FlatMap[F[_]]`       | `flatMap(fa: F[A])(f: A => F[B]): F[B]`                     |
| `fa >> fb`           | followed by                                         | `FlatMap[F[_]]`       | `>>(fb: => F[B]): F[B]`                                     |
| `x |-| y`            | remove                                              | `Group[A]`            | `remove(x: A, y: A): A`                                     |
| `x > y`              | greater than                                        | `PartialOrder[A]`     | `gt(x: A, y: A): Boolean`                                   |
| `x >= y`             | greater than or equal to                            | `PartialOrder[A]`     | `gteq(x: A, y: A): Boolean`                                 |
| `x < y`              | less than                                           | `PartialOrder[A]`     | `lt(x: A, y: A): Boolean`                                   |
| `x <= y`             | less than or equal to                               | `PartialOrder[A]`     | `lteq(x: A, y: A): Boolean`                                 |
| `x |+| y`            | Semigroup combination                               | `Semigroup[A]`        | `combine(x: A, y: A): A`                                    |
| `x <+> y`            | SemigroupK combination                              | `SemigroupK[F[_]]`    | `combineK(x: F[A], y: F[A]): F[A]`                          |
| `f <<< g`            | Arrow composition                                   | `Compose[F[_, _]]`    | `compose(f: F[B, C], g: F[A, B]): F[A, C]`                  |
| `f >>> g`            | Arrow composition (andThen)                         | `Compose[F[_, _]]`    | `andThen(f: F[A, B], g: F[B, C]): F[A, C]`                  |
| `f &&& g`            | Arrow merge (fanout)                                | `Arrow[F[_, _]]`      | `merge[A, B, C](f: F[A, B], g: F[A, C]): F[A, (B, C)]`      |
| `f -< g`             | combine and bypass                                  | `Arrow[F[_, _]]`      | `combineAndByPass[A, B, C](f: F[A, B], g: F[B, C]): F[A, (B, C)]` |
| `F ~> G`             | natural transformation                              | `FunctionK[F[_], G[_]]` | alias for `FunctionK`                                       |
| `F :<: G`            | injectK                                             | `InjectK[F[_], G[_]]`   | alias for `InjectK`                                       |
| `F :≺: G`            | injectK                                             | `InjectK[F[_], G[_]]`   | alias for `InjectK`                                       |
| `fa &> fb`           | parallel product right                              | `Parallel[M[_]]`      | `parProductR[A, B](ma: M[A])(mb: M[B]): M[B]`               |
| `fa <& fb`           | parallel product left                               | `Parallel[M[_]]`      | `parProductL[A, B](ma: M[A])(mb: M[B]): M[A]`               |
| `⊥`                  | bottom                                              | N/A                   | `Nothing`                                                   |
| `⊤`                  | top                                                 | N/A                   | `Any`                                                       |
| `fa << fb` (deprecated) | product left                                     | `FlatMap[F[_]]`       | `productL(fa: F[A])(fb: F[B]): F[A]`                        |

## Detailed Explanations

### Operators for `Apply` (`*>`, `<*`)

- **`fa *> fb` (product right)**: Executes `fa` and `fb`, returning the result of `fb`. 
  Used when you only care about the effect of `fa` and want to discard its value.
- **`fa <* fb` (product left)**: Executes `fa` and `fb`, returning the result of `fa`. 
  Used when you only care about the effect of `fb` (e.g., for logging or condition checking) and want to discard its value.

### Operators for `Eq` (`===`, `=!=`)

- **`x === y` (equality)**: An alternative to `==`, but uses an `Eq` instance (the equality type class). 
  Guarantees no side effects and consistency with `hashCode`.
- **`x =!= y` (inequality)**: The negation of `===` — checks that values are not equal.

### Operators for `FlatMap` (`>>=`, `>>`)

- **`fa >>= f` (flatMap)**: Chains two monadic computations. Equivalent to `fa.flatMap(f)`. 
  Allows using the result of the first computation as input to the second.
- **`fa >> fb` (followed by)**: Executes `fa`, discards its result, then executes `fb`. 
  Useful for sequential actions without passing data between them.

### Operator for `Group` (`|-|`)

- `x |-| y` **(remove)**: Subtraction operation in a group. For numeric groups, this is standard subtraction. 
  For other types (e.g., sets), this may represent element removal.

### Operators for `PartialOrder` (`>`, `>=`, `<`, `<=`)

- Comparison operators for partially ordered types. 
  Unlike Scala's standard `>`, `<`, these operators use a `PartialOrder` instance, 
  allowing work with types that do not have a total order (e.g., floating-point numbers where NaN exists).

### Operator for `Semigroup` (`|+|`)

- `x |+| y` **(Semigroup combine)**: An associative combination operation. 
  For numbers this is addition, for strings it is concatenation, for lists it is concatenation.

### Operator for `SemigroupK` (`<+>`)

- **`x <+> y` (SemigroupK combine)**: A version of `|+|` at the type constructor level. 
  Combines two containers of the same type (e.g., two lists or two options). 
  For `Option`: `Some(1) <+> Some(2)` yields `Some(1)` (first successful).

### Operators for `Compose` and `Arrow` (`<<<`, `>>>`, `&&&`, `-<`)

These operators work with arrows (`Arrow`) — a generalization of functions that allows computations with context.

- **`f <<< g` (compose)**: Arrow composition: `g` first, then `f`. Analog of `f.compose(g)`.
- **`f >>> g` (andThen)**: Arrow composition: `f` first, then `g`. Analog of `f.andThen(g)`.
- **`f &&& g` (merge)**: Applies `f` and `g` to the same input value, returning a tuple of results.
- **`f -< g` (combine and bypass)**: Combines `f` and `g`, skipping certain computations. 
  A more complex operator for specialized scenarios.

### Operators for Natural Transformations (`~>`, `:<:`, `:≺:`)

- **`F ~> G` (natural transformation)**: An alias for `FunctionK[F, G]`. 
  Represents a transformation from one functor to another, preserving structure.
- **`F :<: G` and `F :≺: G` (injectK)**: Aliases for `InjectK[F, G]`. 
  Used in free monads for working with algebras, allowing one functor to be "injected" into another.

### Operators for `Parallel` (`&>`, `<&`)

- **`fa &> fb` (parallel product right)**: The parallel version of `*>`. 
  Executes `fa` and `fb` in parallel (where possible), returning the result of `fb`.
- **`fa <& fb` (parallel product left)**: The parallel version of `<*`. 
  Executes `fa` and `fb` in parallel, returning the result of `fa`.

### Special Symbols (`⊥`, `⊤`)

- **`⊥` (bottom)**: Represents the type `Nothing` — a type with no values. 
  Used in category theory and functional programming to denote "bottom".
- **`⊤` (top)**: Represents the type `Any` — a universal type. 
  Used to denote "top".

### Deprecated Operator (`<<`)

- **`fa << fb` (product left)**: A deprecated version of `fa <* fb`. Not recommended for new code.

## How to Import Symbols

**Group 1** (imported via `import cats.*`):

- `~>`, `⊥`, `⊤`, `:<:`, `:≺:`

**Group 2** (imported via `import cats.syntax.all.*`):

- All other symbols: `*>`, `<*`, `===`, `=!=`, `>>=`, `>>`, `|-|`, `>`, `>=`, `<`, `<=`, `|+|`, `<+>`, 
  `<<<`, `>>>`, `&&&`, `-<`, `&>`, `<&`, `<<`

**Tip:** For targeted imports, you can use the respective packages, for example:

- `import cats.syntax.eq.*` for `===` and `=!=`
- `import cats.syntax.semigroup.*` for `|+|`

## Glossary

This is a catalog of the main features, type classes, and data types in the **Cats** library. 
It serves as a high-level overview of each type class's capabilities.
It is also intended as a reference for Cats users who may not recall answers to questions such as:

- What is the difference between `unit` and `void`?
- To discard the first value and keep only the first effect, should you use `<*` or `*>`?
- How to force computation `F[A]` to fail when a condition on the value is not met?

Signatures and type classes in this document are simplified (for details, see the [Simplifications section](#simplifications)).
For a printable version, you can also check out the [cats-cheatsheet](https://arosien.github.io/cats-cheatsheets/typeclasses.pdf).

## Type Classes over `F[_]`

### Functor

[`Functor`](../../typeclass/monad/functor.md) is a type class 
that provides the ability to apply a function to a value inside a context (container) without changing the context itself.

| Type                              | Method Name      | Notes                                                                                    |
|:----------------------------------|:-----------------|:------------------------------------------------------------------------------------------|
| `F[A] => F[Unit]`                 | `void`           | Ignores the value inside the container, replacing it with `Unit`                          |
| `F[A] => B => F[B]`               | `as`             | Replaces the value inside the container with the passed value `B`                         |
| `F[A] => (A => B) => F[B]`        | `map`            | Applies a function `A => B` to the value inside the container                             |
| `F[A] => (A => A1) => F[A1]`      | `mapOrKeep`      | Partial function: if defined — applies, otherwise keeps the original value; `A1 >: A`     |
| `F[A] => (A => B) => F[(A, B)]`   | `fproduct`       | Applies a function and keeps the original value alongside the result                      |
| `F[A] => (A => B) => F[(B, A)]`   | `fproductLeft`   | Applies a function and keeps the result alongside the original value (order reversed)     |
| `F[A] => B => F[(B, A)]`          | `tupleLeft`      | Adds value `B` to the left of the tuple                                                   |
| `F[A] => B => F[(A, B)]`          | `tupleRight`     | Adds value `B` to the right of the tuple                                                  |
| `(A => B) => (F[A] => F[B])`      | `lift`           | Lifts a plain function to a function over `F`                                             |

### Apply

[`Apply`](../../typeclass/monad/apply.md) extends `Functor`, 
adding the ability to apply a function that itself is in a context.

| Type                                      | Method Name | Symbol |
|:------------------------------------------|:------------|:-------|
| `F[A] => F[B] => F[A]`                    | `productL`  | `<*`   |
| `F[A] => F[B] => F[B]`                    | `productR`  | `*>`   |
| `F[A] => F[B] => F[(A, B)]`               | `product`   | —      |
| `F[A => B] => F[A] => F[B]`               | `ap`        | `<*>`  |
| `F[A => B => C] => F[A] => F[B] => F[C]`  | `ap2`       | —      |
| `F[A] => F[B] => (A => B => C) => F[C]`   | `map2`      | —      |

**Explanation:**

- **`<*` and `*>`**: allow executing two effects, keeping the value of only one of them.
- **`ap` (`<*>`)**: applies a context-wrapped function to a context-wrapped value.
- **`map2`**: applies a binary function to two context-wrapped values.

### Applicative

[`Applicative`](../../typeclass/monad/applicative.md) extends `Apply`, 
adding the ability to lift a plain value into a context.

| Type                              | Method Name | Notes                                               |
|:----------------------------------|:------------|:-----------------------------------------------------|
| `A => F[A]`                       | `pure`      | Lifts a value into a context                         |
| `=> F[Unit]`                      | `unit`      | Creates an `F[Unit]`                                 |
| `Boolean => F[Unit] => F[Unit]`   | `whenA`     | Executes an effect only if the condition is true     |
| `Boolean => F[Unit] => F[Unit]`   | `unlessA`   | Executes an effect only if the condition is false    |

### FlatMap

[`FlatMap`](../../typeclass/monad/bind.md) extends `Apply`, adding the ability to chain computations (`flatMap`).

| Type                                   | Method Name |
|:---------------------------------------|:------------|
| `F[F[A]] => F[A]`                      | `flatten`   |
| `F[A] => (A => F[B]) => F[B]`          | `flatMap`   |
| `F[A] => (A => F[B]) => F[(A, B)]`     | `mproduct`  |
| `F[Boolean] => F[A] => F[A] => F[A]`   | `ifM`       |
| `F[A] => (A => F[B]) => F[A]`          | `flatTap`   |

**Explanation:**

- **`flatten`**: "unwraps" a nested context (e.g., `F[F[A]]` → `F[A]`).
- **`flatTap`**: executes an effect with a side effect, but returns the original value.

### FunctorFilter

`FunctorFilter` is a functor that allows filtering values within a context.

| Type                                 | Method Name     | Notes                                                         |
|:-------------------------------------|:----------------|:---------------------------------------------------------------|
| `F[A] => (A => Boolean) => F[A]`     | `filter`        | Keeps only values that satisfy the predicate                  |
| `F[A] => (A => Option[B]) => F[B]`   | `mapFilter`     | Applies a function returning `Option` and filters out `None`  |
| `F[A] => (A => B) => F[B]`           | `collect`       | Partial function: applies only to certain values              |
| `F[Option[A]] => F[A]`               | `flattenOption` | Converts `F[Option[A]]` to `F[A]`, removing `None`            |

### ApplicativeError

[This type class](../../typeclass/monad/applicative-error.md) adds the ability to work with errors in an applicative context.
The variable `E` is used in Cats source code for the error type.

| Type                              | Method Name       | Notes                                                    |
|:----------------------------------|:------------------|:----------------------------------------------------------|
| `E => F[A]`                       | `raiseError`      | Creates an effect with an error                           |
| `F[A] => F[Either[E, A]]`         | `attempt`         | Converts an error to `Left` in `Either`                   |
| `F[A] => (E => A) => F[A]`        | `handleError`     | Handles an error by converting it to a value              |
| `F[A] => (E => F[A]) => F[A]`     | `handleErrorWith` | Handles an error by returning a new effect                |
| `F[A] => (E => A) => F[A]`        | `recover`         | Partial function for recovering from an error             |
| `F[A] => (E => F[A]) => F[A]`     | `recoverWith`     | Partial function for recovering with a new effect         |
| `F[A] => (E => F[Unit]) => F[A]`  | `onError`         | Partial function for performing a side effect on error    |
| `Either[E, A] => F[A]`            | `fromEither`      | Converts `Either` to an effect                            |
| `Option[A] => E => F[A]`          | `liftFromOption`  | Converts `Option` to an effect, using `E` for `None`      |

### MonadError

[`MonadError`](../../typeclass/monad/monad-error.md) extends `ApplicativeError` and `Monad`, 
adding methods for working with errors in a monadic context.

| Type                                              | Method Name    | Notes                                                       |
|:--------------------------------------------------|:---------------|:------------------------------------------------------------|
| `F[A] => E => (A => Boolean) => F[A]`             | `ensure`       | Checks a condition; if false — returns error `E`            |
| `F[A] => (A => E) => (A => Boolean) => F[A]`      | `ensureOr`     | Checks a condition; if false — computes the error from value |
| `F[A] => (E => E) => F[A]`                        | `adaptError`   | Partial function for converting an error type               |
| `F[Either[E, A]] => F[A]`                         | `rethrow`      | Converts `F[Either[E, A]]` to `F[A]`                        |

### UnorderedFoldable

`UnorderedFoldable` is a type class for collections where element order does not matter (e.g., sets).

| Type                                  | Method Name       | Constraints                                                 |
|:--------------------------------------|:------------------|:-------------------------------------------------------------|
| `F[A] => Boolean`                     | `isEmpty`         | Checks whether the collection is empty                       |
| `F[A] => Boolean`                     | `nonEmpty`        | Checks whether the collection is non-empty                   |
| `F[A] => Long`                        | `size`            | Returns the collection size                                  |
| `F[A] => (A => Boolean) => Boolean`   | `forall`          | Checks whether all elements satisfy the predicate            |
| `F[A] => (A => Boolean) => Boolean`   | `exists`          | Checks whether any element satisfies the predicate           |
| `F[A] => A`                           | `unorderedFold`   | Folds the collection; requires `A: CommutativeMonoid`        |
| `F[A] => (A => B) => B`               | `unorderedFoldMap` | Maps and folds; requires `B: CommutativeMonoid`             |

### Foldable

[`Foldable`](../../typeclass/monad/foldable.md) is a type class for collections 
that support element folding (aggregation) in sequence order.

| Type                                              | Method Name        | Constraints                                                                |
|:--------------------------------------------------|:-------------------|:----------------------------------------------------------------------------|
| `F[A] => A`                                       | `fold`             | Folds the collection; requires `A: Monoid`                                  |
| `F[A] => B => ((B, A) => B) => B`                 | `foldLeft`         | Left-associative fold with initial value                                    |
| `F[A] => (A => B) => B`                           | `foldMap`          | Maps and folds; requires `B: Monoid`                                        |
| `F[A] => (A => G[B]) => G[B]`                     | `foldMapM`         | Monadic version of `foldMap`; requires `G: Monad` and `B: Monoid`           |
| `F[A] => (A => B) => Option[B]`                   | `collectFirst`     | Partial function: finds the first element to which the function applies     |
| `F[A] => (A => Option[B]) => Option[B]`           | `collectFirstSome` | Finds the first element for which the function returns `Some`               |
| `F[A] => (A => G[B]) => G[Unit]`                  | `traverse_`        | Traverses and ignores result; requires `G: Applicative`                     |
| `F[G[A]] => G[Unit]`                              | `sequence_`        | Swaps effects, ignoring result; requires `G: Applicative`                   |
| `F[A] => (A => Either[B, C]) => (F[B], F[C])`     | `partitionEither`  | Partitions the collection by `Either`; requires `G: Applicative`            |

### Reducible

[`Reducible`](../../typeclass/monad/reducible.md) is a type class for non-empty collections that support folding without an initial value.

| Type                        | Method Name   | Constraints                                     |
|:----------------------------|:--------------|:------------------------------------------------|
| `F[A] => ((A, A) => A) => A` | `reduceLeft`  | Left-associative fold without initial value     |
| `F[A] => A`                 | `reduce`      | Folds the collection; requires `A: Semigroup`   |

### traverse

[`Traverse`](../../typeclass/monad/traverse.md) extends `Foldable` and `Functor`, 
adding the ability to traverse a collection with effects.

| Type                                      | Method Name          | Constraints                                                    |
|:------------------------------------------|:---------------------|:----------------------------------------------------------------|
| `F[G[A]] => G[F[A]]`                      | `sequence`           | Swaps effects; requires `G: Applicative`                        |
| `F[A] => (A => G[B]) => G[F[B]]`          | `traverse`           | Traverses collection with effects; requires `G: Applicative`    |
| `F[A] => (A => G[F[B]]) => G[F[B]]`       | `flatTraverse`       | Traverses with flattening; requires `F: FlatMap` and `G: Applicative` |
| `F[G[F[A]]] => G[F[A]]`                   | `flatSequence`       | Sequential flattening; requires `G: Applicative` and `F: FlatMap` |
| `F[A] => F[(A, Int)]`                     | `zipWithIndex`       | Adds index to each element                                      |
| `F[A] => ((A, Int) => B) => F[B]`         | `mapWithIndex`       | Maps elements with their indices                                |
| `F[A] => ((A, Int) => G[B]) => G[F[B]]`   | `traverseWithIndexM`  | Traverses with indices in monadic context; requires `F: Monad` |

### SemigroupK

[`SemigroupK`](../../typeclass/monoid/semigroup.md) is a version of `Semigroup` for type constructors (e.g., for lists or options).

| Type                                    | Method Name            |
|:-----------------------------------------|:------------------------|
| `F[A] => F[A] => F[A]`                   | `combineK`              |
| `F[A] => Int => F[A]`                    | `combineNK`             |
| `F[A] => F[B] => F[Either[A, B]]`        | `sum` (requires `F: Functor`) |
| `IterableOnce[F[A]] => Option[F[A]]`     | `combineAllOptionK`     |

### MonoidK

[`MonoidK`](../../typeclass/monoid/monoid.md) extends `SemigroupK`, adding a neutral element.

| Type                      | Method Name   |
|:--------------------------|:--------------|
| `F[A]`                    | `empty`       |
| `F[A] => Boolean`         | `isEmpty`     |
| `IterableOnce[F[A]] => F[A]` | `combineAllK` |

### Alternative

[`Alternative`](../../typeclass/monad/alternative.md) combines `Applicative` and `MonoidK`.

| Type                      | Method Name       | Constraints                                                     |
|:--------------------------|:------------------|:----------------------------------------------------------------|
| `F[G[A]] => F[A]`         | `unite`           | Requires `F: FlatMap` and `G: Foldable`                         |
| `F[G[A, B]] => (F[A], F[B])` | `separate`      | Requires `F: FlatMap` and `G: Bifoldable`                       |
| `F[G[A, B]] => (F[A], F[B])` | `separateFoldable` | Requires `F: Foldable` and `G: Bifoldable`                   |
| `Boolean => F[Unit]`      | `guard`           | Executes effect only if condition is true                       |
| `IterableOnce[A] => F[A]` | `fromIterableOnce` | Creates an effect from an iterable collection                |
| `G[A] => F[A]`            | `fromFoldable`    | Creates an effect from a foldable collection; requires `G: Foldable` |

### NonEmptyAlternative

[`NonEmptyAlternative`](../../typeclass/monad/alternative.md) extends `Alternative`, adding operations for non-empty collections.

| Type               | Method Name  |
|:-------------------|:-------------|
| `A => F[A] => F[A]` | `prependK`  |
| `F[A] => A => F[A]` | `appendK`   |

## Transformers (Transformers)

### Constructors and Wrappers

| Data Type                  | Is Alias or Wrapper For                  |
|:---------------------------|:------------------------------------------|
| `OptionT[F[_], A]`         | `F[Option[A]]`                            |
| `EitherT[F[_], A, B]`      | `F[Either[A, B]]`                         |
| `Kleisli[F[_], A, B]`      | `A => F[B]`                               |
| `Reader[A, B]`             | `A => B`                                  |
| `ReaderT[F[_], A, B]`      | `Kleisli[F, A, B]`                        |
| `Writer[A, B]`             | `(A, B)`                                  |
| `WriterT[F[_], A, B]`      | `F[(A, B)]`                               |
| `Tuple2K[F[_], G[_], A]`   | `(F[A], G[A])`                            |
| `EitherK[F[_], G[_], A]`   | `Either[F[A], G[A]]`                      |
| `FunctionK[F[_], G[_]]`    | `F[X] => G[X]` for any `X`                |
| `F ~> G`                   | Alias for `FunctionK[F, G]`               |

### OptionT

Here `OT` is used as a shorthand for `OptionT`.

| Type                                               | Method Name  | Constraints      |
|:---------------------------------------------------|:-------------|:-----------------|
| `=> OT[F, A]`                                      | `none`       | `F: Applicative` |
| `A => OT[F, A]`                                    | `some` or `pure` | `F: Applicative` |
| `F[A] => OT[F, A]`                                 | `liftF`      | `F: Functor`     |
| `Boolean => F[A] => OT[F, A]`                      | `whenF`      | `F: Applicative` |
| `F[Boolean] => F[A] => OT[F, A]`                   | `whenM`      | `F: Monad`       |
| `OT[F, A] => F[Option[A]]`                         | `value`      | —                |
| `OT[F, A] => A => Boolean => OT[F, A]`             | `filter`     | `F: Functor`     |
| `OT[F, A] => A => F[Boolean] => OT[F, A]`          | `filterF`    | `F: Monad`       |
| `OT[F, A] => (A => B) => OT[F, B]`                 | `map`        | `F: Functor`     |
| `OT[F, A] => (F ~> G) => OT[G, B]`                 | `mapK`       | —                |
| `OT[F, A] => (A => Option[B]) => OT[F, B]`         | `mapFilter`  | `F: Functor`     |
| `OT[F, A] => B => (A => B) => F[B]`                | `fold` or `cata` | —              |
| `OT[F, A] => (A => OT[F, B]) => OT[F, B]`          | `flatMap`    | —                |
| `OT[F, A] => (A => F[Option[B]]) => OT[F, B]`      | `flatMapF`   | `F: Monad`       |
| `OT[F, A] => A => F[A]`                            | `getOrElse`  | `F: Functor`     |
| `OT[F, A] => F[A] => F[A]`                         | `getOrElseF` | `F: Monad`       |
| `OT[F, A] => OT[F, A] => OT[F, A]`                 | `orElse`     | —                |

### EitherT

Here `ET` is used as a shorthand for `EitherT`; `A` and `B` are type variables for the left and right sides of `Either`.

| Type                                               | Method Name   | Constraints      |
|:---------------------------------------------------|:--------------|:-----------------|
| `A => ET[F, A, B]`                                 | `leftT`       | `F: Applicative` |
| `B => ET[F, A, B]`                                 | `rightT`      | `F: Applicative` |
| —                                                  | `pure`        | `F: Applicative` |
| `F[A] => ET[F, A, B]`                              | `left`        | `F: Applicative` |
| `F[B] => ET[F, A, B]`                              | `right`       | `F: Applicative` |
| —                                                  | `liftF`       | `F: Applicative` |
| `Either[A, B] => ET[F, A, B]`                      | `fromEither`  | `F: Applicative` |
| `Option[B] => A => ET[F, A, B]`                    | `fromOption`  | `F: Applicative` |
| `F[Option[B]] => A => ET[F, A, B]`                 | `fromOptionF` | `F: Functor`     |
| `F[Option[B]] => F[A] => ET[F, A, B]`              | `fromOptionM` | `F: Monad`       |
| `Boolean => B => A => ET[F, A, B]`                 | `cond`        | `F: Applicative` |
| `ET[F, A, B] => (A => C) => (B => C) => F[C]`      | `fold`        | `F: Functor`     |
| `ET[F, A, B] => ET[F, B, A]`                       | `swap`        | `F: Functor`     |
| `ET[F, A, A] => F[A]`                              | `merge`       | —                |

### Kleisli (or ReaderT)

Here `Ki` is used as a shorthand for `Kleisli`.

| Type                                               | Method Name | Constraints    |
|:---------------------------------------------------|:------------|:---------------|
| `Ki[F, A, B] => (A => F[B])`                       | `run`       | —              |
| `Ki[F, A, B] => A => F[B]`                         | `apply`     | —              |
| `A => Ki[F, A, A]`                                 | `ask`       | `F: Applicative` |
| `B => Ki[F, A, B]`                                 | `pure`      | `F: Applicative` |
| `F[B] => Ki[F, A, B]`                              | `liftF`     | —              |
| `Ki[F, A, B] => (C => A) => Ki[F, C, B]`           | `local`     | —              |
| `Ki[F, A, B] => Ki[F, A, A]`                       | `tap`       | —              |
| `Ki[F, A, B] => (B => C) => Ki[F, A, C]`           | `map`       | —              |
| `Ki[F, A, B] => (F ~> G) => Ki[G, A, B]`           | `mapK`      | —              |
| `Ki[F, A, B] => (F[B] => G[C]) => Ki[F, A, C]`     | `mapF`      | —              |
| `Ki[F, A, B] => Ki[F, A, F[B]]`                    | `lower`     | —              |

## Type Classes for Types `F[_, _]`

### Bifunctor

[`Bifunctor`](../../typeclass/bifunctor/bifunctor.md) is a functor for types with two parameters (e.g., `Either` or `Tuple2`).

| Type                                               | Method Name        |
|:---------------------------------------------------|:-------------------|
| `F[A, B] => (A => C) => F[C, B]`                   | `leftMap`          |
| `F[A, B] => (B => D) => F[A, D]`                   | `.rightFunctor` and `.map` |
| `F[A, B] => (A => C) => (B => D) => F[C, D]`       | `bimap`            |

### Profunctor

[`Profunctor`](../../typeclass/arrow/profunctor.md) is a functor contravariant in the first argument and covariant in the second.

| Type                                               | Method Name |
|:---------------------------------------------------|:------------|
| `F[A, B] => (B => C) => F[A, C]`                   | `rmap`      |
| `F[A, B] => (C => A) => F[C, B]`                   | `lmap`      |
| `F[A, B] => (C => A) => (B => D) => F[C, D]`       | `dimap`     |

### Strong Profunctor

| Type                              | Method Name |
|:----------------------------------|:------------|
| `F[A, B] => F[(A, C), (B, C)]`    | `first`     |
| `F[A, B] => F[(C, A), (C, B)]`    | `second`    |

### Compose, Category, Choice

| Type                                            | Method Name | Symbol  |
|:------------------------------------------------|:------------|:--------|
| `F[A, B] => F[C, A] => F[C, B]`                 | `compose`   | `<<<`   |
| `F[A, B] => F[B, C] => F[A, C]`                 | `andThen`   | `>>>`   |
| `=> F[A, A]`                                    | `id`        | —       |
| `F[A, B] => F[C, B] => F[Either[A, C], B]`       | `choice`    | `\|\|\|` |
| `=> F[Either[A, A], A]`                          | `codiagonal`| —       |

### Arrow

| Type                                       | Method Name | Symbol |
|:-------------------------------------------|:------------|:-------|
| `(A => B) => F[A, B]`                      | `lift`      | —      |
| `F[A, B] => F[C, D] => F[(A, C), (B, D)]`  | `split`     | `***`  |
| `F[A, B] => F[A, C] => F[A, (B, C)]`        | `merge`     | `&&&`  |

### ArrowChoice

| Type                                                      | Method Name | Symbol |
|:----------------------------------------------------------|:------------|:-------|
| `F[A, B] => F[C, D] => F[Either[A, C], Either[B, D]]`     | `choose`    | `+++`  |
| `F[A, B] => F[Either[A, C], Either[B, C]]`                | `left`      | —      |
| `F[A, B] => F[Either[C, A], Either[C, B]]`                | `right`     | —      |


## Simplifications

Since Cats is a Scala library, and Scala has many settings and nuances,
the actual definitions and implementations of functions and type classes in Cats may look complex at first.
To ease understanding, this glossary focuses on simple type signatures and ignores many Scala details.
In particular, in our type signatures:

- We use `A`, `B`, `C` for kinds of `*` (normal types) and `F`, `G`, `H` for higher-kinded type variables.
- We write signatures in curried form: parameters are taken one at a time, separated by an arrow `=>`.
  In Scala, method parameters can be divided into multiple comma-separated lists.
- We do not distinguish between methods from a type class trait (e.g., `trait Functor`), the companion object,
  or extension methods.
- For functions defined as trait methods of a type class, we ignore the receiver.
- We ignore implicit parameters representing type class constraints, and move them to a separate column.
- We use `A => B` for both `Function1[A, B]` and `PartialFunction[A, B]`, without distinguishing.
  We add a note when talking about a partial function.
- Some functions are defined using the Partially Applied Type Params pattern. We ignore this.
- We ignore the difference between call-by-name and call-by-value parameters.
  We use the notation `=> A` without parameters to indicate constant functions.
- We ignore Scala variance annotations. We also ignore additional type parameters
  that in some methods are added with a subtype bound (e.g., `B >: A`).
  They are usually there for flexibility, but we replace each with its bound.

---

**References:**

- [Cats home page][home]
- [Higher-Rank Polymorphism in Scala](https://apocalisp.wordpress.com/2010/07/02/higher-rank-polymorphism-in-scala/)

[home]: https://typelevel.org/cats/
