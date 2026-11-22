# Type Classes in Spire

## Vector Space

The vector space family of type classes provides basic vector operations.
They are parameterized over two types: a vector type and a scalar type.

A very useful vector space is the function space `T => Double`.
Functions can be added by simply combining them pointwise, `(f + g)(x) = f(x) + g(x)`,
and multiplication is handled similarly.
Given this definition, the function space is quite straightforward to define:

```dotty
import spire.algebra.{Field, VectorSpace}
import spire.implicits.*

given vspace[T]: VectorSpace[T => Double, Double] with
  def scalar: Field[Double] = summon[Field[Double]]

  def zero: T => Double                                 = _ => 0.0
  def negate(f: T => Double): T => Double               = x => -1 * f(x)
  def plus(f: T => Double, g: T => Double): T => Double = x => f(x) + g(x)
  def timesl(r: Double, f: T => Double): T => Double    = x => r * f(x)
```

Once this is in place, we can treat functions of type `T => Double` as vectors:

```dotty
def f(x: String) = x.length.toDouble
def g(x: String) = x.hashCode.toDouble

val h: String => Double = 4.0 *: f _ + g _
h("test")
// val res0: Double = 3556514.0
```

Alternatively, we could define `h` as:

```dotty
def h(x: String) = 4*x.length.toDouble + x.hashCode.toDouble
```

### LeftModule

A left module is a generalization of a vector space over a field,
where scalars are elements of a ring (not necessarily commutative).

A left module defines left scalar multiplication.

- `timesl` (`*:`): scalar multiplication

```dotty
trait LeftModule[V, R]:
  def timesl(r: R, v: V): V
```

Let `V` be an abelian group (with additive notation) and `R` be a scalar ring.
The following laws must hold for `x`, `y` in `V` and `r`, `s` in `R`:

1. `r *: (x + y) = r *: x + r *: y`
2. `(r + s) *: x = r *: x + s *: x`
3. `(r * s) *: x = r *: (s *: x)`
4. `R.one * x = x`

```dotty
import spire.algebra.{Field, VectorSpace}
import spire.implicits.*

4.0 *: Vector(1.0, 5.0, 3.0)
// val res0: Vector[Double] = Vector(4.0, 20.0, 12.0)
```

### RightModule

A right module is a generalization of a vector space over a field,
where scalars are elements of a ring (not necessarily commutative).

A right module defines right scalar multiplication.

- `timesr` (`:*`): scalar multiplication

```dotty
trait RightModule[V, R]:
  def timesr(v: V, r: R): V
```

Let `V` be an abelian group (with additive notation) and `R` be a scalar ring.
The following laws must hold for `x`, `y` in `V` and `r`, `s` in `R`:

1. `(x + y) :* r = x :* r + y :* r`
2. `x :* (r + s) = x :* r + x :* s`
3. `x :* (r * s) = (x :* r) :* s`
4. `x :* R.one = x`

```dotty
import spire.algebra.{Field, VectorSpace}
import spire.implicits.*

Vector(1.0, 5.0, 3.0) :* 4.0
// val res0: Vector[Double] = Vector(4.0, 20.0, 12.0)
```

### CModule

A module over a commutative ring has, by definition, equivalent left and right modules.

```dotty
trait CModule[V, R] extends LeftModule[V, R], RightModule[V, R]:
  override def timesr(v: V, r: R): V = timesl(r, v)
```

In addition to the left and right module laws:

- `(r *: x) :* s = r *: (x :* s)`

```dotty
import spire.algebra.{Field, VectorSpace}
import spire.implicits.*

0.25 *: Vector(1.0, 5.0, 3.0) :* 4.0
// val res0: Vector[Double] = Vector(1.0, 5.0, 3.0)
```

### VectorSpace

A vector space is a group `V` that can be multiplied by scalars from a field `F`.

A vector space defines scalar division:

- `divr` (`:/`): scalar division

```dotty
trait VectorSpace[V, F] extends CModule[V, F]:
  given def scalar: Field[F]

  def divr(v: V, f: F): V = timesl(scalar.reciprocal(f), v)
```

The following laws must hold for a vector space:

- _Distributive law_: scalar multiplication must distribute over vector addition:
    - `x *: (v + w) === x *: v + x *: w`
    - and scalar addition `(x + y) *: v === x *: v + y *: v`
- Scalar multiplication by 1 in `F` is the identity function:
    - `1 *: v === v`
- Scalar multiplication is "associative":
    - `x *: y *: v === (x * y) *: v`

```dotty
import spire.algebra.{Field, VectorSpace}
import spire.implicits.*

Vector(1.0, 5.0, 3.0) :/ 0.5
// val res0: Vector[Double] = Vector(2.0, 10.0, 6.0)
```

### MetricSpace\[V,R\]

`MetricSpace[V,R]` — types with an associated metric

This type class models a metric space `V`.
The distance between two points in `V` is measured in `R`, which must be a real number (i.e., `IsReal[R]` exists).

```dotty
import spire.algebra.*
import spire.implicits.*

MetricSpace.distance(Vector(1.0, 5.0), Vector(5.0, 1.0))
// val res2: Double = 5.656854249492381
```

### NormedVectorSpace

A normed vector space is a vector space equipped with a norm function: `V => F`.
The main constraint is that the norm of a vector must scale linearly when the vector is scaled;
that is, `norm(k *: v) == k.abs * norm(v)`.
Additionally, a normed vector space is also a `MetricSpace`,
where `distance(v, w) = norm(v - w)`, and therefore must obey the triangle inequality.
An example of a normed vector space is \\(R^{n}\\),
equipped with the Euclidean vector length as the norm.

```dotty
trait NormedVectorSpace[V, F] extends VectorSpace[V, F], MetricSpace[V, F]:
  def norm(v: V): F
  
  def normalize(v: V): V = divr(v, norm(v))
  def distance(v: V, w: V): F = norm(minus(v, w))
```

Operations of a normed vector space:

- `norm`: vector norm
- `normalize`: normalize the vector (so the norm is 1)

```dotty
import spire.algebra.*
import spire.implicits.*

Vector(1.0, 5.0, 3.0).norm
// val res0: Double = 5.916079783099616
Vector(1.0, 5.0, 3.0).normalize
// val res1: Vector[Double] = Vector(0.1690308509457033, 0.8451542547285165, 0.50709255283711)

MetricSpace.distance(Vector(1.0, 5.0), Vector(5.0, 1.0))
// val res2: Double = 5.656854249492381
```

### InnerProductSpace

- `dot` (`⋅`, `dot`): vector inner product

```dotty
import spire.algebra.InnerProductSpace
import spire.implicits.*

Vector(1.0, 5.0, 3.0).dot(Vector(1.0, 5.0, 3.0))
// val res0: Double = 35.0
```


## Heyting Algebra

Heyting algebras are bounded lattices
that are also equipped with an additional binary operation `imp` (for implication, also written as `→`).

Implication obeys the following laws:

- `a → a = 1`
- `a ∧ (a → b) = a ∧ b`
- `b ∧ (a → b) = b`
- `a → (b ∧ c) = (a → b) ∧ (a → c)`

In Heyting algebras, `and` is equivalent to `meet` and `or` is equivalent to `join`; both methods are available.

A Heyting algebra also defines a complement operation (`complement`) (sometimes written as `¬a`).
The complement of `a` is equivalent to (`a → 0`), and the following laws hold:

- `a ∧ ¬a = 0`

However, in Heyting algebras, this operation is only a pseudo-complement,
because Heyting algebras do not necessarily guarantee the law of excluded middle.
This means there is no guarantee that `(a ∨ ¬a) = 1`.

Heyting algebras model intuitionistic logic.
For modeling classical logic, see the boolean algebra type class below, implemented as `Bool`.

`Heyting[A]` — types forming a Heyting algebra

- `complement` (unary `~`): logical negation
- `and` (`&`, `meet`): conjunction ("AND")
- `or` (`|`, `join`): disjunction ("OR")
- `xor` (`^`): exclusive disjunction: `or(and(a, complement(b)), and(complement(a), b))`
- `imp`: implication ("NOT a or b"), equivalent to `~a | b`
- `nand`: "not AND", equivalent to `~(a & b)`: `complement(and(a, b))`
- `nor`: "not OR", equivalent to `~(a | b)`: `complement(or(a, b))`
- `nxor`: "not XOR", equivalent to `~(a ^ b)`: `complement(xor(a, b))`

## Bool

`Bool` supports a boolean algebra — an abstraction of familiar bitwise logical operators.

Boolean algebras are Heyting algebras with an additional constraint,
namely that the law of excluded middle holds (which is equivalent to double negation holding).
This means that, in addition to the laws obeyed by Heyting algebras, boolean algebras also obey the following:

- `(a ∨ ¬a) = 1`
- `¬¬a = a`

Boolean algebras generalize classical logic: one is equivalent to "true", and zero to "false".
Boolean algebras provide additional commonly used logical operators such as `xor`, `nand`, `nor`, and `nxor`.
Every boolean algebra has a dual algebra, which includes swapping true/false and AND/OR.

- `complement` (unary `~`): logical negation
- `and` (`&`, `meet`): conjunction ("AND")
- `or` (`|`, `join`): disjunction ("OR")
- `xor` (`^`): exclusive disjunction: `or(and(a, complement(b)), and(complement(a), b))`
- `imp`: implication ("NOT a or b"), equivalent to `~a | b`
- `nand`: "not AND", equivalent to `~(a & b)`: `complement(and(a, b))`
- `nor`: "not OR", equivalent to `~(a | b)`: `complement(or(a, b))`
- `nxor`: "not XOR", equivalent to `~(a ^ b)`: `complement(xor(a, b))`

Instances of `Bool` exist not only for `Boolean`,
but also for `Byte`, `Short`, `Int`, `Long`, `UByte`, `UShort`, `UInt`, and `ULong`.

## Trigonometry

`Trig` provides an abstraction for any type that defines trigonometric functions.
For this, types must be able to reasonably approximate real values.

The following methods are supported:

- `e`: Euler's number, `2.71828...`
- `pi`: the number \\(\pi\\) — a mathematical constant equal to the ratio of a circle's circumference to its diameter, `3.14159...`
- `exp`: exponential function, \\(e^{x}\\)
- `expm1`: \\(e^{x} - 1\\)
- `log`: natural logarithm of `a` (`r` such that `exp(r) = a`)
- `log1p`: `log(x + 1)`
- `sin`: sine — the y-coordinate of the unit circle.
- `cos`: cosine — the x-coordinate of the unit circle.
- `tan`: tangent — equivalent to `sin(a) / cos(a)`.
- `asin`: arcsine, `asin(sin(a)) = a`.
- `acos`: arccosine, `acos(cos(a)) = a`.
- `atan`: arctangent, `atan(tan(a)) = a`.
- `atan2`: similar to `atan`, but returns results in `(-pi, pi]` format.
- `sinh`: hyperbolic sine, the y-coordinate of the unit hyperbola.
- `cosh`: hyperbolic cosine, the x-coordinate of the unit hyperbola.
- `tanh`: hyperbolic tangent, `sinh(a) / cosh(a)`.
- `toRadians`: convert degrees (e.g., `180`) to pi-radians (e.g., `pi`).
- `toDegrees`: convert pi-radians (e.g., `pi/2`) to degrees (e.g., `90`).

Spire is capable of computing trigonometric values (e.g., the number \\(\pi\\)) and functions (e.g., sine)
with arbitrary precision when using `BigDecimal`.
Unlike `NRoot`, there is no support for creating `Trig[Rational]` instances with arbitrary precision here
(although a `Double` instance with precision can be found in `spire.optional.rationalTrig`).

## Pseudorandom Number Generators

The `spire.random` package contains random number generators suitable for many different tasks,
as well as a functional interface for creating uniformly distributed values.

### Pseudorandom Number Generators

The `mutable.Generator` trait represents a PRNG strategy.
Using uniformly generated `Int` or `Long` values,
it can generate random values, arrays of values, etc.
Defining a generator is relatively straightforward.

By default, generators are not thread-safe.
A synchronized generator can be created from an unsynchronized one via the `sync` method.
Generators can be copied, and their initial values can be saved and restored.
This allows users to create deterministic streams of values
using the same initial seed.
In general, users are better off creating and using their own generators
rather than relying on a single generator across multiple threads.

Although the `mutable.Generator` trait provides only low-level methods such as `nextInt`,
it can generate values of any type using the `Dist[A]` type class, discussed below.

The `immutable.Generator` trait is similar to `mutable.Generator`, although the state it stores is immutable.
Each time a number is generated, a new generator is also returned,
allowing these generators to be used in a purely functional context.
The same `Dist[A]` examples that would be used with a mutable generator also apply here.

### Creating random values with Dist\[A\]

The `Dist[A]` type class represents a strategy for generating a distribution of values `A` for a given `Generator` instance.
`Dist[A]` makes no guarantees about how values `A` are distributed
(e.g., it may always return the same value).
Users interested in specific implementations should use the traits
`Uniform[A]`, `Gaussian[A]`, and `Exponential[A]` to create `Dist[A]` instances matching their needs.

The `Dist[A]` objects themselves are immutable and created from generators (both mutable and immutable).
They can be transformed via `map`, `flatMap`, and other combinators.
When an appropriate structure `A` with `Dist[A]` instances is available, they can also be treated as values.

### Distributions

Currently, `spire.random` provides the `Uniform[A]`, `Gaussian[A]`, and `Exponential[A]` type classes,
which can create `Dist[A]` instances given appropriate parameters.
Since most types have (approximately) an infinite number of possible values,
bounds and other constraints must be imposed on these types
before we can meaningfully talk about (or implement) probability distributions in Spire.

- Given `min` and `max`, a `Uniform[A]` instance can create a uniformly distributed `Dist[A]` instance.
- Given `mean` and `stdDev`, a `Gaussian[A]` instance can create a `Dist[A]`,
  whose values are distributed according to the desired Gaussian distribution.
- Given `rate`, an `Exponential[A]` instance can create a `Dist[A]`,
  whose values are distributed according to the desired exponential distribution.

For example:

```dotty
object DistDemo:
  import spire.math.Complex
  import spire.random.Dist
  import spire.std.double.DoubleAlgebra
  
  val rng = spire.random.rng.Cmwc5()
  rng.nextLong() 
  
  // generates a map with 10 to 20 entries
  given Dist[Map[Int, Complex[Double]]] =
    Dist.map[Int, Complex[Double]](10, 20)
  val m = rng.next[Map[Int, Complex[Double]]]
  
  // generates a double in the range [0.0, 1.0)
  val n = rng.next[Double]
  
  // generates a complex number with real and imaginary parts in the range [0.0, 1.0)
  val q = rng.next[Complex[Double]]
```

Many default instances are provided, and custom instances are easy to create for user-defined types.

---

**References:**

- [Spire home page](https://typelevel.org/spire/)
- [Learning Spire - Vector Spaces!](https://www.chrisstucchio.com/blog/2013/learning_spire_vector_space.html)
- [Learning Spire - Boolean Algebras are pretty cool](https://www.chrisstucchio.com/blog/2013/learning_spire_boolean_algebra.html)
