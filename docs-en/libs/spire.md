# Spire

[Spire][home] is a numerical library for Scala that aims to be universal, fast, and precise.

Using features such as specialization, macros, type classes, and implicit conversions, Spire strives
to challenge conventional notions about the trade-off between performance and precision.
The main goal is to enable developers to write efficient numerical code
without the need to "bake in" specific numeric representations.
In most cases, generic implementations using Spire's specialized type classes
work identically to corresponding direct implementations.

Example of using the Spire library:

```dotty
import spire.*
import spire.implicits.*
import spire.math.*

Complex(3.0,5.0).sin
// val res0: spire.math.Complex[Double] = (10.47250853394392 + -73.460621695677i)
```

## Numeric Types

In addition to supporting all of Scala's built-in numeric types,
Spire introduces several new ones, all of which can be found in `spire.math`:

- `Natural` — an unsigned, immutable arbitrary-precision integer
- `Rational` — fractions of integers with perfect precision
- `Algebraic` — lazily evaluated arbitrary-precision algebraic numbers
- `Real` — an implementation of computable real numbers
- `Complex[A]` — complex numbers, points on the complex plane
- `Jet[A]` — N-dimensional dual numbers for automatic differentiation
- `Quaternion[A]` — an extension of complex numbers into four-dimensional space
- from `UByte` to `ULong` — value classes supporting unsigned operations
- `SafeLong` — a fast integer type with overflow protection
- `Number` — a sealed type with traditional numeric support
- `Interval[A]` — arithmetic on open, closed, and discontinuous intervals
- `Polynomial[A]` — one-variable (univariate) polynomial expressions
- `Trilean` — a value class supporting three-valued logic
- `FixedPoint` — fractions with a `Long` numerator and an implicit denominator (additionally)

## Type Classes

Spire provides type classes to support a wide range of unary and binary numeric operations.
The type classes are specialized, do not perform boxing, and use implicit conversions
to provide convenient infix syntax.

General-purpose type classes can be found in the `spire.math` package:

- `Numeric[A]` — all numeric types, making every effort to support operators
- `Fractional[A]` — fractional types where `/` is honest division
- `Integral[A]` — integer types where `/` is truncating division

Some general-purpose type classes are built on top of a set of more fundamental type classes
defined in `spire.algebra`.
Many of them correspond to concepts from abstract algebra:

- `Eq[A]` — types that can be compared for equality
- `Order[A]` — types that can be compared and ordered
- `PartialOrder[A]` — types that can be compared for equality and for which certain pairs are ordered
- `Semigroup[A]` — types with an associative binary operator `|+|`
- `Monoid[A]` — semigroups having an identity element
- `Group[A]` — monoids having an inverse operator
- `(Left/Right/)Action[P, G]` — left/right actions for semigroups/monoids/groups
- `Semiring[A]` — types forming semigroups under `+` and `*`
- `Rng[A]` — types that form a `+` group and a `*` semigroup
- `Rig[A]` — types forming monoids under `+` and `*`
- `Ring[A]` — types forming a group under `+` and a monoid under `*`
- `EuclideanRing[A]` — rings with quotients and remainders (Euclidean division)
- `Field[A]` — Euclidean rings with multiplicative inverses (mutual)
- `Signed[A]` — types that have a sign (negative, zero, positive)
- `NRoot[A]` — types supporting k-th roots, logs, and fractional powers
- `Module[V,R]` — types forming a left R-module
- `VectorSpace[V,F]` — types forming a vector space
- `NormedVectorSpace[V,F]` — types with an associated norm
- `InnerProductSpace[V,F]` — types with an inner product
- `MetricSpace[V,R]` — types with an associated metric
- `Trig[A]` — types supporting trigonometric functions
- `Bool[A]` — types forming a Boolean algebra
- `Heyting[A]` — types forming a Heyting algebra

Variants of `Semigroup`/`Monoid`/`Group`/`Action` with partial operations are defined in the `spire.algebra.partial` subpackage.

In addition to the type classes themselves, `spire.implicits` defines many implicit functions
that provide unary and infix operators for the type classes.
The simplest way to use them is to import the files `spire.implicits.*`.

### Numbers

These high-level type classes will include all relevant algebraic type classes.

- `Integral`: integer types (e.g., `Int`, `BigInt`)
- `Fractional`: fractional/decimal types (e.g., `Double`, `Rational`)
- `Numeric`: an arbitrary-number type that makes "best effort" to support operations

The `Numeric` type class is unique because it provides the same functionality as `Fractional` and all numeric types.
Each type will attempt to "do the right thing" as much as possible; otherwise, it will throw errors.
Users who are wary of such behavior are recommended to use more precise type classes.

## Getting Started

Spire contains many types, as well as other mechanisms that ensure ease of use.
The simplest way to use Spire is to import the dependencies:

```dotty
import spire.algebra.*   // provides algebraic type classes
import spire.implicits.* // provides infix operators, instances, and conversion
import spire.math.*      // provides functions, types, and type classes
```

## Syntax

Using string interpolation and macros, Spire provides convenient syntax for numeric types.
These macros are evaluated at compile time, and any errors they encounter occur at compile time.

For example:

```dotty
object LiteralsDemo:
  import spire.syntax.literals.*

  // bytes and shorts
  val x    = b"100" // without type annotation!
  val y    = h"999"
  val mask = b"255" // unsigned constant converted to signed (-1)

  // fractions
  val n1 = r"1/3"
  val n2 = r"1599/115866" // simplifies at compile time to 13/942
```

```dotty
object SIDemo:
  // SI notation for large numbers
  import spire.syntax.literals.si.* // `.us` and `.eu` are also available
  
  val w = i"1 944 234 123" // Int
  val x = j"89 234 614 123 234 772" // Long
  val y = big"123 234 435 456 567 678 234 123 112 234 345" // BigInt
  val z = dec"1 234 456 789.123456789098765" // BigDecimal
```

Spire also provides a `cfor` loop macro, whose syntax is somewhat similar to the traditional `for` loop from C or Java.
This macro expands to a tail-recursive function that inlines literal function arguments.

The macro can nest within itself and has advantages over other loop constructs in Scala, such as `for` and `while`:

```dotty
import spire.syntax.cfor.*

// prints numbers from 0 to 9
cfor(0)(_ < 10, _ + 1): i =>
  println(i)

// a simple sorting algorithm
def selectionSort(ns: Array[Int]): Unit =
  val limit = ns.length - 1
  cfor(0)(_ < limit, _ + 1): i =>
    var k = i
    val n = ns(i)
    cfor(i + 1)(_ <= limit, _ + 1): j =>
      if ns(j) < ns(k) then k = j
    ns(i) = ns(k)
    ns(k) = n
```

## Miscellaneous

In addition, Spire provides many other methods that are "missing" from `java.Math` (and `scala.math`),
for example:

- `log(BigDecimal): BigDecimal`
- `exp(BigDecimal): BigDecimal`
- `pow(BigDecimal): BigDecimal`
- `pow(Long): Long`
- `gcd(Long, Long): Long`
- and so on...

### Tests

In addition to unit tests, Spire comes with a relatively detailed suite of [JMH](https://openjdk.org/projects/code-tools/jmh/) micro-benchmarks.
To run the tests from SBT, navigate to the `benchmark` subproject and then run `Jmh / run -l` to view the list of tests:

```
$ sbt
> project benchmark
> Jmh / run -l

[info] Benchmarks:
[info] spire.benchmark.AddBenchmarks.addComplexDoubleStateDirect
[info] spire.benchmark.AddBenchmarks.addComplexDoubleStateGeneric
[info] spire.benchmark.AddBenchmarks.addComplexFloatStateDirect
...
```

To run all available tests:

```
> Jmh / run
```

To run a specific test method:

```
> Jmh / run spire.benchmark.AddBenchmarks.addComplexDoubleStateDirect
```

To run all tests in a specific class:

```
> Jmh / run spire.benchmark.AddBenchmarks
```

To view all available JMH options:

```
> Jmh / run -h
```

If you plan to contribute to Spire, be sure to run the relevant tests
to ensure your changes do not affect performance.
Tests usually include comparisons with equivalent Scala or Java classes,
attempting to measure both relative and absolute performance.

---

**References:**

- [Spire home page][home]
- [An Intro to Generic Numeric Programming with Spire](https://typelevel.org/blog/2013/07/07/generic-numeric-programming.html)
- [How to use Spire's Ops macros in your own project](https://typelevel.org/blog/2013/10/13/spires-ops-macros.html)

[home]: https://typelevel.org/spire/
