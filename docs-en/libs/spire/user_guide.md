# User Guide

This guide describes Spire's architecture, layout, and usage. 
First, we will look at some of the basic structures and patterns used by Spire. 
Then we will cover many of the concrete types that Spire defines. 
Finally, we will take a look at some of the complex and tricky aspects of the library.

## Type Classes

Like many Scala libraries, Spire uses type classes to define generic operations.

All the following code examples assume the following import:

```dotty
import spire.algebra.*   // defines all type classes
import spire.implicits.* // instances and syntax for all type classes
```

For example, `Ring[A]` is a type class that defines a number of basic operations, such as `+` and `*`, on type `A`. 
When using type classes, it is important to try to distinguish the following:

- The type class itself (`Ring[A]`). Often this is a `trait`.
- Concrete instances of the type class, such as `Ring[Int]`.
- Implicit syntaxes that use the type class to define operators.

Methods in these type classes are always given text names (e.g., `plus`). 
In some cases, these names correspond to symbolic operators: in the case of `plus`, it corresponds to `+`. 
When using these type classes, users have the option to use the symbolic syntax 
directly on values or to call the method on the type class instance:

```dotty
def usingSymbols[A: Ring](x: A, y: A): A           = x + y
def usingNames[A: Ring as r](x: A, y: A): A = r.plus(x, y)
```

Some methods (e.g., `sqrt`) have no corresponding symbols. 
In these cases, the method name itself can be used on values:

```dotty
def sqrt[A: NRoot](x: A): A = x.sqrt
```

### Package Level

In the case of `Ring[A]`, the type class itself lives in `spire.algebra`. 
With a few exceptions, all Spire type classes can be found in the `spire.algebra` package.

Type class instances can be found in two different places. 
For types defined in Spire, or code supporting Spire, 
the type class instance should be placed in the type's companion object. 
For example, `UByte` (unsigned byte type) has a `Ring[UByte]` instance contained in its companion object.

For types defined elsewhere that Spire supports directly (e.g., built-in numeric types), 
Spire defines `spire.std` objects that contain their instances. 
So, to get all `Int` instances, you must import them from `spire.std.int.*`. 
To get all these "standard instances" at once, import `spire.std.any.*`. 
This pattern should also be followed when supporting other numeric types not supported by Spire.

Finally, implicit syntax is imported from objects in `spire.syntax`. 
To get the `Ring[A]` syntax, you must import `spire.syntax.ring.*`. 
Again, there is a shortcut package: you can import `spire.syntax.all.*` to get all syntax.

This kind of importing may seem a bit confusing, but it is very useful in situations 
where Spire types or operators conflict with other libraries. 
There is an even simpler import (`spire.implicits.*`) if you want both all instances and all operators. 
This is convenient when working in the console or experimenting, 
and also in cases where you are confident no conflict will arise.

### Usage

In most cases, type classes are used as context bounds. 
For example:

```dotty
object Demo:
  import spire.algebra.*
  import spire.std.any.*
  import spire.syntax.ring.*
  
  def double[A: Ring](x: A): A = x + x
  def triple[A: Ring](x: A): A = x * 3
  println((double(3), triple(4)))
```

This code eventually becomes equivalent to:

```dotty
object Demo2:
  def double[A: Ring as ev](x: A): A = ev.plus(x, x)
  def triple[A: Ring as ev](x: A): A = ev.times(x, ev.fromInt(3))
  println((double(3)(IntAlgebra), triple(4)(IntAlgebra)))
```

The `IntAlgebra` type extends `Ring[Int]` and was imported via `spire.std.any.*`. 
All implicit functions providing binary operators `+` and `*` 
(as well as implicit conversion functions for integer literals to `A`), 
were imported in the form `spire.syntax.ring.*`.
And the `Ring` context bound 
is actually just "sugar" for an implicit parameter (a type class instance).

### Specialization

To achieve speed comparable to straight (unspecialized) code, you will need to use [specialization](https://www.scala-lang.org/old/sites/default/files/sids/dragos/Thu,%202010-05-06,%2017:56/sid-spec.pdf). 
The good news is that most of Spire's code is already specialized (and benchmarked). 
The bad news is that you will have to annotate all generic code like this:

```dotty
object Demo3:
  import spire.algebra.*
  import spire.std.any.*
  import spire.syntax.ring.*
  
  import scala.specialized as sp
  
  def double[@sp A: Ring](x: A): A = x + x
  def triple[@sp A: Ring](x: A): A = x * 3
  println((double(3), triple(4)))
```

Too many specialization gotchas to list here. 
But the (very) short guide to specialization is:

- It is much easier to specialize methods.
- Calls from generic code to specialized code are not specialized.
- Limit specialization to types you will use with `@sp(Int, Double)`.
- Specialization will increase bytecode size by 2–10x.

If you have questions about specialization, feel free to ask in the `#spire` channel on the [Typelevel Discord](https://discord.com/invite/XF3CXcMzqD). 
You may notice that some code in Spire is structured in an unusual way, 
and this is often to ensure specialization works properly.

You may find that developing generic code is easier without using specialization first (to keep things simple), 
and then, if needed, coming back and adding annotations later. 
This helps keep things simple while your code works correctly, and it is a (relatively) small change 
to enable specialization later (as long as you are consistent).

Of course, if your code is not generic, you can call Spire's specialized code 
without worrying about it (and the result will be unboxed and fast).

### Properties

Spire type classes are often described in terms of properties (or "laws"). 
These properties must hold regardless of which values are used.

Here is a brief description of some of the most common properties:

- _associativity_: `|+|` is associative if `(a |+| b) |+| c = a |+| (b |+| c)`.
- _identity_: for `|+|` there exists an identity value `id` such that `a |+| id = a = id |+| a`.
- _invertibility_: `|+|` includes an `inverse` operation such that `a |+| a.inverse = id = a.inverse |+| a`.
- _commutativity_: `|+|` is commutative if `a |+| b = b |+| a`.

In some cases, operator names differ (e.g., `+`, `*`), but the properties themselves remain the same.

## Types

This section attempts to describe existing number types in terms of their capabilities and caveats.

### Byte, Short, Int, and Long

All of these built-in integer types are signed and have a fixed width (8, 16, 32, and 64 bits, respectively). 
Division with these types truncates, and overflow can occur silently 
when numbers become too large (or too small). 
Division by zero will throw an exception.

It is worth noting that the JVM does not support operating directly on `Byte` and `Short`: operations on them usually return `Int`. 
This can cause confusion when using type inference, 
and may also lead to differences between straight code (where adding bytes creates an integer) 
and generic code (where adding bytes creates a byte).

### Float and Double

These fractional types correspond to IEEE-754 floating point (32- and 64-bit, respectively). 
They contain three special values: positive and negative infinity, and `NaN`. 
Large positive and negative values will overflow to the corresponding infinity value, 
and division by zero will silently go to infinity.

The semantics of comparison and equality for `NaN` are complex (e.g., `NaN == NaN` is false). 
This also means that for double values there is no total order corresponding to IEEE comparisons. 
For a total alternative `Ordering[Double]`, see `spire.optional.totalfloat`.

Since floating-point values are approximations of real values, 
adding values of different magnitudes can lead to a loss of precision. 
Thus, many operations are not always associative. 
Spire assumes that users working with `Float` and `Double` are aware of these caveats, 
and provides instances such as `Ring[Double]`, although in some cases operations cannot be associative.

### BigInt

This integer type is unbounded — it never overflows 
(though operations will become slower and slower as the value grows). 
This is probably one of the least error-prone types to use correctly.

### BigDecimal

This fractional type differs from the previous floating-point values. 
It contains a `MathContext` object that defines a specific number of decimal digits of precision (default 34). 
Results will be rounded to this level of precision, which also makes this type non-associative in some cases 
(though with user-defined precision it is easier to avoid cases where it matters).

`MathContext` also defines how values should be rounded. 
Since this type is decimal, it can exactly represent any decimal number 
(unlike a floating-point value),
although its `MathContext` may require enough digits to do so.

As with floating point, Spire goes to great lengths to support this type, 
even though issues related to precision and rounding may arise. 
Spire also provides capabilities not present in the base type, 
including root extraction, fractional powers, and trigonometric methods.

### Rational

This fractional type represents a rational number, a fraction of two integers (`n/d`). 
It is an exact type, although, as expected, it cannot represent irrational numbers 
without approximating them as rational. It is unbounded, although as the 
fraction grows larger or more complex, operations become slower. 
Rational calculations are always kept in simplest form to speed up future computations.

This is probably the simplest and most correct-to-use fractional type.

### SafeLong

This integer type is also unbounded, like `BigInt`. 
However, it is more efficient for small values, where a `Long` would be used instead. 
There is usually no reason to prefer `BigInt` over `SafeLong`, 
except when most values are expected to exceed the capacity of `Long` storage.

### Natural

This is a simple unbounded unsigned integer type. 
It models natural numbers as a list of digits (each "digit" is an unsigned 32-bit integer). 
For relatively small values (32–128 bits), it is often faster than `SafeLong` or `BigInt`. 
For large values, it becomes slower.

The `Natural` type is slightly unconventional at present. 
However, the fact that it is guaranteed to be non-negative is useful.

### UByte, UShort, UInt, and ULong

These unsigned integer types are provided by Spire. 
They perform most of the same operations as their signed counterparts, 
though they use unsigned division, which is slightly more complex.

These are value classes, so in most cases there should be no overhead 
over their primitive counterparts. The only exception is arrays. 
`Array[UInt]` will be packed, while `Array[Int]` will not. 
Since conversions between `UInt` and `Int` complete only at compile time, 
this issue is easy to work around by keeping `UInt` instances in an `Array[Int]`.

Writing unsigned literal values is slightly more verbose 
than their signed counterparts (consider `UInt(7)` compared to `7`). 
Spire provides a syntax import that makes writing easier:

```dotty
import spire.syntax.literals.*

ui"7" // equivalent to UInt(7)
// res0: spire.math.UInt = 7
```

### FixedPoint

This value class uses a `Long` with an implicit denominator. The type itself contains no information about the denominator. 
Instead, an implicit `FixedScale` instance is required to provide this context 
when needed (e.g., during multiplication). 
Like the previous unsigned values, fixed-point values will not be boxed in most cases.

This type is designed to solve a certain kind of problem and should only be used in situations 
where a large number of rational numbers with the same denominator are needed and performance is critical.

### Complex\[A\] and Quaternion\[A\]

These generic types represent complex numbers (`x + yi`) and quaternions (`w + xi + yj + zk`), respectively. 
They can be parameterized by any fractional type `A` implementing `Field[A]`, `NRoot[A]`, and `Trig[A]`. 
Overall, these values are as precise as their base `A` values, 
though in some cases approximate results are necessarily returned 
(in cases where roots or trigonometric functions are used).

These types are specialized, so most operations should execute reasonably quickly 
and not cause unnecessary boxing. However, these types use more memory 
than the non-generic complex number based on `Double` values, and run slightly slower.

### Number

This is a compact number type that approximates the semantics of numbers in a dynamically typed numeric tower 
(e.g., Scheme or Python). 
There are four subtypes of `Number`, based on `SafeLong`, `Double`, `BigDecimal`, and `Rational`. 
Combining two numbers will always return the number of the highest precision.

`Number` is a good choice for users who need simple and correct numeric operations. 
This type ensures maximum operation safety while providing access to all operators and methods.

### Interval\[A\]

Interval supports arithmetic operations over a range of possible values of `A`. 
This can be viewed as representing uncertainty about a single actual value 
or as acting simultaneously on a whole set of values. 
Any type with `Order[A]` can be used in an interval, 
though most arithmetic operations will require additional type classes 
(ranging from `AdditiveSemigroup[A]` for `+` up to `Field[A]` for `/`).

Intervals can be unbounded on both sides, and the bounds can be open or closed. 
(an interval includes closed bounds, but not open bounds). 
Here are some string representations of various intervals:

- `[3, 6]` — a set of values from 3 to 6 (inclusive).
- `(2, 4)` — a set of values from 2 to 4 (exclusive).
- `[1, 2)` — a half-open interval including 1 but not 2.
- `(-∞, 5)` — a set of values less than 5.

Intervals model continuous spaces, even if the type `A` is discrete. 
So, for example, when `(3, 4)` is an `Interval[Int]`, it is not considered "empty", 
even though there are no `Int` values between `3` and `4`. 
This is because we can multiply the interval by `2` to get `(6, 8)`, which is explicitly non-empty. 
The underlying continuous interval contains values 
that, when multiplied by a scalar, become valid `Int` values.

### Polynomial\[C\]

Currently, Spire supports one-dimensional polynomials. 
These are polynomials with a single variable (e.g., \\(x\\)) of the following structure:
\\(c\_{0} + (c\_{1} * x^{1}) + (c\_{2} * x^{2}) + ... + (c\_{n} * x^{n})\\)

The coefficients (from \\(c\_{0}\\) to \\(c\_{n}\\)) are values of type `C`, 
and the exponents (from 1 to n) are `Int` values 
(this means Spire's implementation only supports polynomials whose exponents are less than `2147483648`).

As with intervals, arithmetic on polynomials is performed using type classes for `C`, 
such as `Semiring[C]`. With the right type classes, polynomials can support all arithmetic operations 
covered by Euclidean rings, but not fields. 
Division operations and inverse operations are not possible, 
since polynomials do not support fractional or negative exponents. 
Polynomials also support `interval`, `derivative`, and other operations.

Spire supports a convenient syntax for literal polynomials. 
By importing `spire.syntax.literals.*` (or simply `spire.implicits.*`), 
you can use the `poly` string interpolator to create `Polynomial[Rational]` instances:

```dotty
import spire.syntax.literals.*

poly"3x^2 - 5x + 1"
poly"5/4x^6 - 7x - 2"
poly"1.2x^3 - 6.1x^2 + 9x - 3.33"
```

Spire actually supports two types of polynomials: dense and sparse. 
For most simple polynomials used in these examples, you probably want dense polynomials. 
However, in cases where your polynomials have few terms with very large exponents, 
the sparse implementation will be more efficient. 
In either case, the underlying representation is an implementation detail, 
and both types support the same operations (and can interact).

### Algebraic

The `Algebraic` type represents a number implementation for "Exact Geometric Computation". 
It represents algebraic numbers using an AST of operations performed on them. 
`Algebraic` numbers can be compared purely and exactly. 
This means that if we have two numbers `a` and `b`, then `a compare b` is always correct, 
regardless of whether they are irrational or incredibly close to each other. 
They are suitable for use in algorithms that use square or n-th roots 
and rely on sign checks and numeric comparisons to work correctly.

In addition to exact comparisons/sign checks, `Algebraic` can approximate itself to any desired precision post-hoc. 
This works for both absolute approximations such as `x +/- 0.00001` 
and relative approximations such as `x.toBigDecimal(new MathContext(10000))`.

Since `Algebraic` can represent algebraic numbers 
(note: Spire adds support for polynomial roots, not just n-th roots), 
they have a wider range than `Rational`. 
However, while `Rational` exactly represents numbers, `Algebraic` can only compare them exactly. 
To achieve this, `Algebraic` also sacrifices performance, 
so they are not suitable for use where you need performance 
and can tolerate some error.

### Real

`Real` means "computable real". 
Spire's `Real` implementation is based on ERA, written in Haskell by David Lester. 
Computable real numbers are numbers that can be computed (i.e., approximated) to any desired precision. 
Unlike `Double` and `BigDecimal`, `Real` values are not stored as approximations, 
but rather as a function from desired precision to the closest approximate value.

If we have a `Real` instance _x_ that approximates a real number _r_, 
this means that for any precision _p_ (in bits), our instance will yield _x_ such that 
\\(\frac{x}{2^{p}}\\) is the closest rational value to _r_. 
In Scala terms, this means `x.apply(p)` returns a `SafeLong` value `x`, 
which is `Rational(x, SafeLong(2).pow(p))` — the best approximation for _r_.

Spire represents two types of `Real` values: `Exact` and `Inexact`. 
The former represent rational values for which an existing `Rational` instance exists, 
and are "cheap" to work with. 
The latter are functions to approximate (potentially) irrational values, 
lazily evaluated and memoized, and their computation can potentially be very expensive.

As with `Rational` values, operations on `Real` values 
can obey the corresponding algebraic identities. 
But unlike `Rational`, `Real` supports roots and trigonometric functions. 
Furthermore, important trigonometric identities are preserved:

```dotty
import spire.math.Real.{cos, sin}
import spire.math.{Real, sqrt}

// will return Real(1) regardless of the passed value
def circle(a: Real): Real = sqrt(cos(a).pow(2) + sin(a).pow(2))
```

Keep in mind that root computation is not absolutely exact. 
See the ["Irrational and Transcendent Type Classes"](../../typeclass/monoid/field.md) section for more information.

One interesting consequence of building computable reals is that 
discrete operations (such as sign checks, comparison, and equality) cannot be performed exactly. 
If `x.apply(p)` returns `0`, it is impossible to know whether the value is actually zero 
or just a very small value (positive or negative!) that is approximately zero at this precision. 
Similarly, it is impossible to say that `x` equals `y`, only that they are equivalent (or not) at a given precision.

Currently, Spire computes with "default" precision to use with such methods. 
Moreover, these methods will always work with `Exact` values: 
problems only arise when `Inexact` values are used. 
Given that the alternative to using `Real` is using another approximate type, 
providing approximate comparisons and equality seems like a reasonable tradeoff.

## What number types should I use?

Spire provides many number types, and it is not always obvious what their relative advantages are. 
This section explains the differences between them, which can help you decide which numeric representation(s) to use.

There is usually a contradiction between numbers that 
have correctness constraints (e.g., possible overflow or precision issues), 
and numbers that have performance constraints (e.g., boxing overhead and/or slower code). 
Spire provides a wide range of numeric types to meet most needs.

### Natural numbers (unsigned, integers)

For non-negative numbers, the safe type is `Natural`. 
It is quite fast at representing small numbers (128 bits and smaller), 
but has no upper bound on the values it can represent. 
However, its unique structure means that for very large values, `BigInt` and `SafeLong` may be faster. 
Since `Natural` only supports non-negative values, subtraction is not total (and may throw an exception).

If your values are guaranteed to be small (or you are willing to define truncation), 
then you can use `UByte` (8-bit), `UShort` (16-bit), `UInt` (32-bit), or `ULong` (64-bit), 
depending on what dimensions you need. 
These types have the same unsigned semantics as unsigned types in languages like C. 
These types are not packed, though care should be taken with arrays (as with any value class).

### Integers (signed, whole numbers)

There are two safe types you can use with integer values: `SafeLong` and `BigInt`. 
Both support arbitrarily large values, as well as standard semantics for things like integer division (`quot`). 
The first option (`SafeLong`) performs much better for values that can be represented by `Long` 
(e.g., 64-bit or less), and is a good default choice. 
When working with values that are mostly or entirely very large, `BigInt` may be slightly faster.

As in the unsigned case, you can use `Byte` (8-bit), `Short` (16-bit), `Int` (32-bit) 
or `Long` (64-bit) to handle cases where your values are small 
or when you want to avoid allocation and solve truncation issues yourself. 
These types are provided by Scala (and ultimately the JVM) and do not cause object allocation.

### Fractional numbers (numbers that can be divided)

There are many different options for fractional numbers 
that support various tradeoffs between expressiveness, precision, and performance.

Fractional types come in two main kinds: exact and inexact. 
Inexact types (e.g., `Double`) will accumulate errors and in some cases are not associative 
(this means `(x + y) + z` may give a different result than `x + (y + z)`). 
These types often run faster than exact types, but their use can be risky.

Exact numbers give a more reliable guarantee of accuracy, but at the cost of performance or expressiveness. 
They are often slightly slower and may restrict supported operations (to maintain accuracy guarantees).

#### Exact types

The most powerful exact type is `Real`. It represents computable real numbers 
and supports all expected operations, including roots and trigonometry. 
However, irrational values (e.g., `Real(2).sqrt` or `Real.pi`) 
are represented as functions from precision to approximations. 
This means that in some situations this type may be too slow 
or use too much memory. 
Additionally, operations requiring comparison or equality checks 
can only be computed approximately. 
However, this type should never accumulate errors, 
so your results will always be correctly approximated to the precision you need.

The next exact type is `Algebraic`. This type supports all rational values as well as roots. 
However, it cannot represent transcendental values such as "pi", 
making its values a subset of `Real`. 
Unlike `Real`, this type is capable of exact sign checks 
(and therefore equality checks and comparisons). 
Since the `Algebraic` AST is used to represent expressions, 
execution can be slow and involve a large amount of computation.

Finally, there is `Rational`. 
This type represents values as irreducible fractions (e.g., `n/d`). 
`Rational` cannot represent irrational values (e.g., roots), 
but efficiently implements all operations on rational values. 
This type has the least performance overhead, although obviously 
processing fractions with large numerators or denominators will take more time.

#### Inexact types

These types are more efficient than exact types but require care and analysis 
to ensure correctness and sufficient precision of results.

The inexact type with the greatest potential precision is `BigDecimal`, provided by Scala. 
This number approximates real values to decimal digits (base 10) (default 34). 
Unlike floating-point values, this type has an exact representation of values such as `0.111110`, 
and the user can use `java.math.MathContext` to configure the precision used. 
Despite this, the type is still subject to accumulated rounding error 
and is therefore not truly associative.

Next are the built-in `Float` and `Double`, the JVM's 32- and 64-bit floating-point implementations. 
The pitfalls of using floating-point values are well known (and documented in other sources), 
but these types are very fast.

Finally, Spire supports an experimental `FixedPoint` class. 
This value class uses unpacked `Long` values to represent fractions 
in terms of a user-specified denominator (provided via an implicit `FixedScale` instance). 
This is a very specialized type that should only be used in cases 
where floating-point approximations are problematic and unboxed values are required. 
Avoid this type unless your application has a specific need for fixed-point arithmetic.

### Other types

The remaining numeric and pseudo-numeric types (e.g., `Polynomial`, `Interval`, `Complex`, and `Quaternion`) 
implement specific functionality, so there should be less confusion about which type to use. 
The sections describing these types explain their properties and tradeoffs.

## Design

This section is intended to describe design goals, conventions, and implementation notes for the various types.

### SafeLong

The goal of `SafeLong` development is to provide robust arbitrary-precision integers 
that, nevertheless, have performance approaching packed `Long` 
for computations that remain within the range of a 64-bit signed integer.

This is achieved by having two variants: `SafeLongLong(x: Long)` is used for numbers within the `Long` range. 
`SafeLongBigInt(x: BigInt)` is used for numbers that fall outside `Long`.

#### Conventions

A number in the `Long` range _must_ always be represented as `SafeLongLong`. 
This is used in both `SafeLong` operations and other `spire.math` classes. 
Consequences of this convention:

- `SafeLongBigInt` can never equal `0`
- `SafeLongBigInt` can never equal `SafeLongLong`

#### Implementation Notes

`SafeLongBigInt` operations use the underlying `BigInt` and create the appropriate result type 
depending on whether the result is a valid `Long`.

`SafeLongLong` operations use the Checked macro to perform operations using integers within the `Long` range 
and fall back to using only `BigInt` in the case of numeric overflow.

#### Performance Tips

- zero comparison is more efficient using `Signum` than using `compare`
- the `isValidLong` method can be used to check whether a `SafeLong` can be losslessly converted to `long` via `toLong`

### Rational

The goal of `Rational` development is to ensure good performance and low memory overhead 
in the very common case of small-magnitude rational numbers, 
while still being able to represent rational numbers of arbitrary size. 

To achieve this, there are two different cases. 
`LongRational(n: Long, d: Long)` is used to represent small rational numbers. 
`BigRational(n: BigInt, d: BigInt)` is used whenever the numerator or denominator is too large 
to store in `LongRational`.

#### Conventions

Rational numbers are always stored in normalized form, so each number has a unique representation:

- Common factors of the numerator and denominator are reduced, so for example, `3/9` is stored as `n = 1, d = 3`.
- Negative rational numbers are always stored with a negative numerator. Therefore, the denominator is always positive.
- A rational number with a zero denominator is invalid.
- Zero will always be stored as `n = 0, d = 1`.

Beyond these conventions, which are fairly common in many rational number implementations, 
there are additional conventions related to using `LongRational` or `BigRational`:

- Every rational number that can be stored in `LongRational` given the above conventions 
  will always be stored in `LongRational`. Consequences of this:
     - Valid range for the numerator is `[Long.MinValue, Long.MaxValue]` and for the denominator is `[1, Long.MaxValue]`.
     - Since the denominator is always stored as a positive number, e.g., 
       `Rational(1, Long.MinValue)` must be stored as `BigRational`.
- `BigRational` can never equal `LongRational`.
     - The numerator of `BigRational` can never be zero, since zero is always represented as `LongRational(0, 1)`
     - When implementing operations for `Rational`, it is important to ensure that conventions are not violated. 
       For example, when operating on two `BigRational`s that result in a small number 
       that can be represented as `LongRational`, the result must be `LongRational`.

#### Implementation Notes

The `BigRational` implementation is quite straightforward. Operations are typically performed using `SafeLong`, 
and the result is created via a constructor that takes `SafeLong` as the numerator and denominator 
and emits the correct rational type given the conventions.

`LongRational` operations are a bit more complex: the core operations use the Checked macro 
to attempt to perform the operation using `long` integers only, 
and fall back to using only `SafeLong` in the case of numeric overflow. 
The advantage of this approach is that in the very common case 
where there is no overflow, no unnecessary object allocation occurs.

#### Performance Tips

The performance characteristics of rational number operations differ slightly 
from regular integer or floating-point operations.

- basic `+`, `-`, `*`, `/` operations have roughly the same cost
- comparison is cheaper than basic operations and does not allocate objects for small rational numbers.
- zero comparison is more efficient using `Signum` than using `compare`
- comparison with one is best done via the `CompareToOne` method rather than using `compare`

---

**References:**

- [Spire User's Guide](https://typelevel.org/spire/guide.html)
- [Spire Design](https://typelevel.org/spire/DESIGN.html)
