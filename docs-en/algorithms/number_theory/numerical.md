# Numerical Algorithms

## Euclid's Algorithm

> Given two positive integers `m` and `n`.
> Find their greatest common divisor, i.e., the largest positive integer that divides both numbers `m` and `n` evenly.

**Algorithm:**

- **Step 1** (Compare with zero). If `n = 0`, then the algorithm terminates; `m` is the desired value.
- **Step 2** (Substitution). Assign `r ← m % n, m ← n, n ← r` and go back to **Step 1**.

**Example:**

\\((m = 4851, n = 3003) \longmapsto (m = 3003, n = 1848) \longmapsto (m = 1848, n = 1155) \longmapsto\\)
\\((m = 1155, n = 693) \longmapsto (m = 693, n = 462) \longmapsto (m = 462, n = 231) \longmapsto (m = 231, n = 0)\\)

**Explanation:**

The greatest common divisor (GCD) using Euclid's algorithm is computed based on the following considerations:

- `GCD(r, 0) = r` for any nonzero `r`, since `0` is divisible by any integer.
- Let `a = b*q + r`, then `GCD(a, b) = GCD(b, r)`, since `GCD(b, r)` is the maximum number that divides both `b*q + r` and `b`.

**Estimation:**

- **Time** - O(log n)
- **Memory** - O(1)

The time complexity of Euclid's algorithm is: \\(O(\log(\min(a, b)))\\), where a and b are the original numbers.
This is because each division reduces one of the numbers by at least a factor of two (on average),
which leads to a logarithmic number of steps.

**Code:**

```dotty
@annotation.tailrec
final def gcd(a: Long, b: Long): Long =
  if b == 0 then a else gcd(b, a % b)

gcd(4851, 3003)
// 231
```

## GCD Algorithm Based on Number Parity

> Given two positive integers `m` and `n`.
> Find their greatest common divisor, i.e., the largest positive integer that divides both numbers `m` and `n` evenly.

The algorithm for finding the GCD that takes into account number parity
is an optimized version of the classical Euclidean algorithm.
It uses properties of parity and divisibility to reduce the number of operations,
which can make it more efficient in some cases
(both numbers are even, frequent even number cases, small numbers, etc.).
However, on average, it is no better than the classical algorithm.

**Algorithm:**

The GCD algorithm, taking into account number parity:

- **Step 1**: If `u == v`, then `GCD(u, v) = u`.
- **Step 2**: `GCD(r, 0) = r` for any nonzero `r`, since `0` is divisible by any integer.
- **Step 3**: If `u` and `v` are both even, then `GCD(u, v) = 2 * GCD(u/2, v/2)`.
- **Step 4**: If the numbers have different parity, then the GCD of these two numbers equals the GCD of the odd number and the even number divided by `2`.
- **Step 5**: If both are odd, subtract the smaller from the larger and continue (the difference will be even, and we can divide by `2` immediately).

**Example:**

\\((u = 4851, v = 3003) \longmapsto (u = 924, v = 3003) \longmapsto (u = 462, v = 3003) \longmapsto\\)
\\((u = 231, v = 3003) \longmapsto (u = 1386, v = 231) \longmapsto (u = 693, v = 231) \longmapsto (u = 231, v = 231)\\)

**Explanation:**

The greatest common divisor (GCD) is computed based on the following considerations:

- **Step 1**: If both numbers are equal, then their GCD equals either of them.
- **Step 2**: If one of the numbers is zero, then the GCD equals the other number, since any number divides zero.
- **Step 3**: If both numbers are even, then their GCD is also even. Strictly speaking, if both numbers are divisible by `x`, then their GCD is also divisible by `x`.
  And `x` can be factored out as a common factor.
- **Step 4**: If one number is even and the other is odd, then the GCD is also odd.

**Estimation:**

- **Time** - O(log n)
- **Memory** - O(1)

**Code:**

```dotty
@tailrec
def gcdBasedOnParityOfNumbers(a: Long, b: Long, k: Long = 1): Long =
  val u = math.abs(a)
  val v = math.abs(b)
  if u == v then u * k
  else if u == 0 then v * k
  else if v == 0 then u * k
  else
    (~u & 1, ~v & 1) match
      case (1, 1) =>
        gcdBasedOnParityOfNumbers(a = u >> 1, b = v >> 1, k = k << 1)
      case (1, 0) =>
        gcdBasedOnParityOfNumbers(a = u >> 1, b = v, k = k)
      case (0, 1) =>
        gcdBasedOnParityOfNumbers(a = u, b = v >> 1, k = k)
      case (_, _) if u > v =>
        gcdBasedOnParityOfNumbers(a = (u - v) >> 1, b = v, k = k)
      case _ =>
        gcdBasedOnParityOfNumbers(a = (v - u) >> 1, b = u, k = k)

gcdBasedOnParityOfNumbers(4851, 3003)
// 231
```

## Extended Euclidean Algorithm

> Given two positive integers `m` and `n`.
> Find their greatest common divisor `d` and two integers `a` and `b` such that `am + bn = d`.

**Algorithm:**

- **Step 1**: (Initialization). Assign \\(\acute{a} \leftarrow b \leftarrow 1, a \leftarrow \acute{b} \leftarrow 0, c \leftarrow m, d \leftarrow n\\).
- **Step 2**: (Division). Let \\(q\\) and \\(r\\) be the quotient and remainder of dividing \\(c\\) by \\(d\\) respectively.
  (Then \\(c = qd + r\\), where \\(0 \leq r < d\\)).
- **Step 3**: (Remainder — zero?). If \\(r = 0\\), then the algorithm terminates;
  in this case we have \\(am + bn = d\\), as required.
- **Step 4**: (Repeat loop). Assign \\(c \leftarrow d, d \leftarrow r, t \leftarrow \acute{a}, \acute{a} \leftarrow a, a \leftarrow t − qa, t \leftarrow \acute{b}, \acute{b} \leftarrow b, b \leftarrow t − qb\\) and go back to **Step 2**.

**Example:**

Suppose we are given two numbers `m = 1769` and `n = 551`.

Consider the iterations of the algorithm:

- \\(c = 1769, d = 551, q = 1769 / 551 = 3, r = 1769 \% 551 = 116, a = 0, \acute{a} = 1, b = 1, \acute{b} = 0\\)
- \\(c = 551, d = 116, q = 551 / 116 = 4, r = 551 \% 116 = 87, a = 1 - 3 \times 0 = 1, \acute{a} = 0, b = 0 - 3 \times 1 = -3, \acute{b} = 1\\)
- \\(c = 116, d = 87, q = 116 / 87 = 1, r = 116 \% 87 = 29, a = 0 - 4 \times 1 = -4, \acute{a} = 1, b = 1 - 4 \times (-3) = 13, \acute{b} = -3\\)
- \\(c = 87, d = 29, q = 87 / 29 = 3, r = 87 \% 29 = 0, a = 1 - 1 \times (-4) = 5, \acute{a} = -4, b = -3 - 1 \times (13) = -16, \acute{b} = 13\\)
- \\(a = 5, m = 1769, b = -16, n = 551, d = 29\\)

Final result: \\(5 \times 1769 + (-16) \times 551 = 29\\)

**Explanation:**

- The algorithm is finite, because `c` and `d` are constantly decreasing (at a logarithmic rate).
  Thus, there will come a moment when the remainder of dividing `c` by `d` will be `0` (at the very least, when `d` decreases to `1`).

**Estimation:**

- **Time** - O(log n)
- **Memory** - O(1)

**Code:**

```dotty
def generalizedEuclideanAlgorithm(m: Long, n: Long): (Long, Long, Long) =
  @annotation.tailrec
  def loop(
      aa: Long,
      b: Long,
      a: Long,
      bb: Long,
      c: Long,
      d: Long
  ): (Long, Long, Long) =
    val r = c % d
    if r == 0 then (d, a, b)
    else
      val q = c / d
      loop(aa = a, b = bb - q * b, a = aa - q * a, bb = b, c = d, d = r)

  loop(aa = 1, b = 1, a = 0, bb = 0, c = m, d = n)
end generalizedEuclideanAlgorithm

generalizedEuclideanAlgorithm(1769, 551)
// (d = 29, a = 5, b = -16)
```

## Modular Inverse

> A number that is [the modular inverse of integer `m`](https://ru.wikipedia.org/wiki/%D0%9E%D0%B1%D1%80%D0%B0%D1%82%D0%BD%D0%BE%D0%B5_%D0%BF%D0%BE_%D0%BC%D0%BE%D0%B4%D1%83%D0%BB%D1%8E_%D1%87%D0%B8%D1%81%D0%BB%D0%BE) —
> is an integer `x` such that the product `m*x` is congruent to `1` modulo `n`: \\(mx \equiv 1\\) (mod n).

**Algorithm:**

To compute the modular inverse, it is sufficient to use the "Extended Euclidean Algorithm."
If two numbers `m` and `n` are coprime (`d = 1`), then `a` from the formula `am + bn = 1`
is the desired value.

If the numbers are not coprime, then such a number `x` does not exist.

**Estimation:**

The estimation is the same as for the Extended Euclidean Algorithm.

**Code:**

```dotty
def gcdInverse(m: Long, n: Long): Option[Long] =
  val (d, a, _) = generalizedEuclideanAlgorithm(m, n)
  if d == 1 then Some((a % n + n) % n) else None

gcdInverse(7, 26) // Some(15)
```

## Least Common Multiple

> The least common multiple (LCM) of two integers `m` and `n` is the smallest natural number that is divisible by both `m` and `n`.

**Algorithm:**

The least common multiple is computed as the product of two numbers divided by their GCD.

**Estimation:**

The estimation is the same as for GCD computation.

**Code:**

```dotty
def lcm(x: Long, y: Long): Long = math.abs(x * y) / gcd(x, y)
```

## Chinese Remainder Theorem

> [The Chinese Remainder Theorem][china]:
>
> If natural numbers \\(a\_1, a\_2, \dots, a\_n\\) are pairwise coprime, then for any integers \\(r\_1, r\_2, \dots, r\_n\\) such that
> \\(0 \le r\_i < a\_i\\) for all \\(i \in \{1, 2, \dots, n\}\\), there exists a number N
> which when divided by \\(a\_i\\) gives remainder \\(r\_i\\) for all \\(i \in \{1, 2, \dots, n\}\\).
> Moreover, if two such numbers \\(N\_1\\) and \\(N_{2}\\) (satisfying the above)
> are found, then \\(N\_1 \equiv N\_2\\) (mod \\(a\_{1} \cdot a\_{2} \cdot \ldots \cdot a\_{n}\\)).

**Algorithm:**

- **Step 1**: Compute the product of all moduli: \\(M = a\_1 \times a\_2 \times \ldots \times a\_n\\)

- **Step 2**: Compute partial products:
  for each i from 1 to n compute the partial product: \\(M\_i = \frac{M}{a\_i}\\),
  where \\(M\_i\\) is the product of all moduli except \\(a\_i\\).

- **Step 3**: Compute the inverses:
  for each i, find the inverse element \\(b\_i\\) of \\(M\_i\\) modulo \\(a\_i\\):
  \\(M\_i \cdot b\_i \equiv 1 \mod a\_i\\).
  This can be done using the extended Euclidean algorithm.

- **Step 4**: Form the general solution:
  now we can express the solution x as: \\(x \equiv \sum\_{i=1}^{n} r\_i \cdot M\_i \cdot b\_i \mod M\\)

**Example:**

Consider the system:

- \\(x \equiv 1 \mod 2\\)
- \\(x \equiv 2 \mod 3\\)

Solution:

- \\(M = 2 \times 3 = 6\\)
- \\(M\_1 = 3, M\_2 = 2\\)

Find the inverses:

- \\(3 \times b\_1 \equiv 1 \mod 2\\) → \\(b\_1 = 1\\)
- \\(2 \times b\_2 \equiv 1 \mod 3\\) → \\(b\_2 = 2\\)

Substitute into the formula:

\\(x \equiv ((1 \times 3 \times 1) + (2 \times 2 \times 2)) \mod 6 = 5\\)

The answer is correct:

- \\(5 \equiv 1 \mod 2\\)
- \\(5 \equiv 2 \mod 3\\)

**Estimation:**

- **Time** - \\(O(n \log m)\\)
- **Memory** - \\(O(n)\\)

Time estimation:

- Computing the product M requires O(n) operations.
- For each i, it is necessary to compute \\(M\_i\\) and find the inverse value \\(b\_i\\).
  The inverse can be found in \\(O(\log m\_i)\\) using the extended Euclidean algorithm.
- Thus, the overall complexity is: \\(O(n \log(m))\\), where m is the maximum value among \\(m\_i\\).

Memory for storing data:

- Storing the remainders \\(r\_i\\) and moduli \\(m\_i\\) will require O(n) memory.
- Memory is also needed for storing intermediate values \\(M\_i\\), \\(b\_i\\), and the total product M, which also requires O(n) memory.
- Thus, the overall memory estimate is: O(n)

**Code:**

```dotty
object ChineseRemainderTheorem:
  def solution(aArray: Array[Long], rArray: Array[Long]): Option[Long] =
    val m      = aArray.product         // Step 1
    val mArray = aArray.map(a => m / a) // Step 2
    val maybeMMinus1Array =
      mArray.indices.foldLeft(Option(ListBuffer.empty[Long])): (acc, i) => // Step 3
        (acc, gcdInverse(mArray(i), aArray(i))) match
          case (Some(list), Some(mMinus)) => Some(list :+ mMinus)
          case _ => None
    maybeMMinus1Array.map: mMinus1Array =>
      mArray.indices.foldLeft(0L): (x, i) => // Step 4
        x + (((rArray(i) * mArray(i)) % m) * mMinus1Array(i)) % m

solution(Array(2, 3), Array(1, 2))
// 5, because 5 % 2 = 1, 5 % 3 = 2

solution(Array(707, 527), Array(0, 5))
// 258762, because 258762 % 707 = 0, 258762 % 527 = 5
```

## Exponentiation

> Given a number `a` and an integer `n`. Find `a` raised to the power `n`.

**Algorithm:**

- **Step 1** (Initialization): Assign \\(base \leftarrow a, power \leftarrow n, acc \leftarrow 1\\).
- **Step 2** (Check power): If power is 0, then return acc. Otherwise, proceed to the next step.
- **Step 3** (Even power): If power is even, then assign \\(base \leftarrow base^2, power \leftarrow \frac{power}{2}, acc \leftarrow acc\\).
  Go back to **Step 2**.
- **Step 4** (Odd power): If power is odd, then assign \\(base \leftarrow base, power \leftarrow (power - 1), acc \leftarrow (base \times acc)\\).
  Go back to **Step 2**.

**Example:**

Let us compute \\(2^{17}\\):

- **Step 1**: base = 2, power = 17, acc = 1
- **Step 4**: base = 2, power = 16, acc = 2
- **Step 3**: base = 4, power = 8, acc = 2
- **Step 3**: base = 16, power = 4, acc = 2
- **Step 3**: base = 256, power = 2, acc = 2
- **Step 3**: base = 65536, power = 1, acc = 2
- **Step 4**: base = 65536, power = 0, acc = 131072
- **Step 2**: Result: 131072

**Explanation:**

Exponentiation is based on two key formulas:

- \\(A^{2*M} = (A^{2})^{M}\\)
- \\(A^{M+N} = A^{M} * A^{N}\\)

The first allows quickly computing the power of number `A`, by squaring `A` and raising it to half the original power;
the second helps combine powers in any convenient way.

**Estimation:**

In the worst case, if n is odd, we first decrement it by 1,
which gives us (n - 1), and then continue dividing by 2.

Thus, the number of steps needed to reach 0 can be estimated as:

- Decrement by 1: 1 step
- Division by 2: \\(O(\log n)\\) steps

Thus, the total number of steps will be at most \\( 2 \times O(\log n) = O(\log n) \\).

- **Time** - O(log n)
- **Memory** - O(1)

**Code:**

Implementation of the algorithm (_Exercise 1.16_ from [SICP][sicp]):

```dotty
def power(a: Long, n: Long): Long =
  @annotation.tailrec
  def loop(base: Long, power: Long, acc: Long): Long =
    if power == 0 then acc
    else if power % 2 == 0 then loop(base * base, power / 2, acc)
    else loop(base, power - 1, base * acc)
  loop(a, n, 1)

power(2, 17) // 131072
```

## Finding the Square Root

> Given a number `x`. Find a number `y` such that `y * y = x`.

**Algorithm:**

The computation of the square root using Newton's method is well described
[in the chapter "Example: Newton's Method for Square Roots"
of the book "Structure and Interpretation of Computer Programs"][sicp].

[Newton's method](../newton.md) is based on the fact that with some approximate value `y` for the square root of number `x`,
we can get a more precise value by taking the average between `y` and `x/y`.

- **Step 1** (Initialization): Assign \\(guess \leftarrow 1, delta \leftarrow 0.00001\\), where \\(0.00001\\) is the desired accuracy.
- **Step 2** (Check the answer): If \\(\left| guess \times guess - x \right| \le delta\\), then return guess. Otherwise, proceed to the next step.
- **Step 3** (Refine the candidate): Assign \\(guess \leftarrow \frac{guess^2 + x}{2 \times guess}\\). Go back to **Step 2**.

**Example:**

Let us compute \\(\sqrt{2}\\):

- \\(guess = 1.0\\)
- \\(guess = \frac{1^2 + 2}{2 \times 1} = 1.5\\)
- \\(guess = \frac{1.5^2 + 2}{2 \times 1.5} = 1.4166666666666665\\)
- \\(guess = \frac{1.4166666666666665^2 + 2}{2 \times 1.4166666666666665} = 1.4142156862745097\\)

**Explanation:**

[Newton's method is described on a separate page.](../newton.md)

**Estimation:**

- **Time** - O(n)
- **Memory** - O(n)

**Code:**

_Exercise 1.7_ from [SICP][sicp]:

```dotty
/** Finding the square root */
def sqrt(x: Double): Double =
  val delta: Double = 1e-5

  def isGoodEnough(guess: Double): Boolean =
    math.abs(guess * guess - x) < delta

  def improve(guess: Double): Double = (guess + x / guess) / 2

  @tailrec
  def sqrtIter(guess: Double): Double =
    if isGoodEnough(guess) then guess
    else sqrtIter(improve(guess))

  sqrtIter(1.0)

sqrt(2)
// 1.4142156862745097
```

## Finding the Cube Root

> Given a number `x`. Find a number `y` such that \\(y^3 = x\\).

**Algorithm:**

Finding the cube root is based on the fact that
with some approximate value `y` for the cube root of number `x`,
a more precise value can be obtained using the formula:
\\((\frac{x}{y^2} + 2y) / 3\\).

- **Step 1** (Initialization): Assign \\(guess \leftarrow 1, delta \leftarrow 0.00001\\), where \\(0.00001\\) is the desired accuracy.
- **Step 2** (Check the answer): If \\(\left| guess^3 - x \right| \le delta\\), then return guess. Otherwise, proceed to the next step.
- **Step 3** (Refine the candidate): Assign \\(guess \leftarrow \frac{\frac{x}{guess^2} + 2 \times guess}{3}\\). Go back to **Step 2**.

**Example:**

Let us compute \\(\sqrt\[3\]{2}\\):

- \\(guess = 1.0\\)
- \\(guess = \frac{\frac{2}{1.0^2} + 2 \times 1.0}{3} = 1.3333333333333333\\)
- \\(guess = \frac{\frac{2}{1.3333333333333333^2} + 2 \times 1.3333333333333333}{3} = 1.2638888888888888\\)
- \\(guess = \frac{\frac{2}{1.2638888888888888^2} + 2 \times 1.2638888888888888}{3} = 1.259933493449977\\)
- \\(guess = \frac{\frac{2}{1.259933493449977^2} + 2 \times 1.259933493449977}{3} = 1.2599210500177698\\)
- \\(1.2599210500177698^3 = 2.0000000005852585\\)

**Explanation:**

[Newton's method is described on a separate page.](../newton.md)

**Estimation:**

- **Time** - O(n)
- **Memory** - O(n)

**Code:**

_Exercise 1.8_ from [SICP][sicp]:

```dotty
def cubeRootOf(x: Double): Double =
  val delta: Double = 1e-5

  def isGoodEnough(guess: Double): Boolean =
    math.abs(guess * guess * guess - x) < delta

  def improve(guess: Double): Double = (x / (guess * guess) + 2 * guess) / 3

  def sqrtIter(guess: Double): Double =
    if isGoodEnough(guess) then guess
    else sqrtIter(improve(guess))

  sqrtIter(1.0)

cubeRootOf(2)
// 1.2599210500177698
```

## Sum of the First Natural Numbers

### Sum of natural numbers up to a given number

> Given a number n. Find the sum of numbers from 1 to n.

**Algorithm:**

The sum of the first natural numbers can be computed using the formula:
\\(\frac{n * (n + 1)}{2}\\)

**Example:**

\\(1 + 2 + 3 + 4 + 5 = \frac{5 * 6}{2} = 15\\)

**Explanation:**

Consider the sum of the first n natural numbers: \\(S = 1 + 2 + 3 + \ldots + n\\)

Now, if we write this sum in reverse order, we get: \\(S = n + (n - 1) + (n - 2) + \ldots + 1\\)

Now, if we add both expressions element by element, we get: \\(2S = (1 + n) + (2 + (n - 1)) + (3 + (n - 2)) + \ldots + (n + 1)\\)

Each pair in parentheses equals (n + 1), and there are n such pairs. Thus, we can write: \\(2S = n(n + 1)\\)

Now, to find S, divide both sides by 2: \\(S = \frac{n(n + 1)}{2}\\)

**Estimation:**

- **Time** - O(1)
- **Memory** - O(1)

**Code:**

```dotty
def sumToGiven(n: Long): Long = n * (n + 1) / 2

sumToGiven(1000000)
// 500000500000L
```

### Sum of squares of the first natural numbers up to a given number

> Given a number n. Find the sum of squares of numbers from 1 to n.

**Algorithm:**

The sum of squares of the first natural numbers can be computed [using the formula](https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D0%BB%D0%BD%D1%8B%D0%B9_%D0%BA%D0%B2%D0%B0%D0%B4%D1%80%D0%B0%D1%82):
\\(\frac{n * (n + 1) * (2 * n + 1)}{6}\\)

**Example:**

\\(1^2 + 2^2 + 3^2 + 4^2 + 5^2 = \frac{5 * 6 * 11}{6} = 55\\)

**Explanation:**

Proof by induction:

- Base case: n = 1. \\(1^2 = \frac{1 * 2 * 3}{6}\\)
- Step to k + 1: \\(1^2 + 2^2 + ... + k^2 + (k + 1)^2 = \frac{k * (k + 1) * (2 * k + 1)}{6} + (k + 1)^2 = \frac{(k + 1) * (2k^2 + 7k + 6)}{6} = \frac{(k + 1) * (k + 2) * (2 (k + 1) + 1)}{6}\\)

**Estimation:**

- **Time** - O(1)
- **Memory** - O(1)

**Code:**

```dotty
def sumOfSquaresTo(n: Long): Long = n * (n + 1) * (2 * n + 1) / 6

sumOfSquaresTo(1000)
// 333833500L
```

### Sum of cubes of the first natural numbers up to a given number

> Given a number n. Find the sum of cubes of numbers from 1 to n.

**Algorithm:**

The sum of cubes of the first natural numbers can be computed [using the formula](https://ru.wikipedia.org/wiki/%D0%9A%D1%83%D0%B1_(%D0%B0%D0%BB%D0%B3%D0%B5%D0%B1%D1%80%D0%B0)):
\\(\left ( \frac{n * (n + 1)}{2} \right )^{2}\\)

**Example:**

\\(1^3 + 2^3 + 3^3 + 4^3 + 5^3 = (\frac{5 * 6}{2})^2 = 225\\)

**Explanation:**

Proof by induction:

- Base case: n = 1. \\(1^3 = (\frac{1 * 2}{2})^2\\)
- Step to k + 1: \\(1^3 + 2^3 + ... + k^3 + (k + 1)^3 = (\frac{k * (k + 1)}{2})^2 + (k + 1)^3 = (k + 1)^2 \times \frac{k^2 + 4k + 4}{4} = (\frac{(k + 1) * (k + 2)}{2})^2\\)

**Estimation:**

- **Time** - O(1)
- **Memory** - O(1)

**Code:**

```dotty
def sumOfCubesTo(n: Long): Long =
  val s = sumToGiven(n)
  s * s

sumOfCubesTo(1000)
// 250500250000L
```

## Data Randomization - Linear Congruential Generator

**Algorithm:**

A linear congruential generator uses the following recurrence
to generate pseudo-random values

\\(X\_{n+1}\\) = (A * \\(X\_{n}\\) + B) % M, where A, B, and M are constants.

The value \\(X_{0}\\) is called the _seed_.

**Code:**

```dotty
val A = 7
val B = 5
val M = 11
val generator = LazyList.iterate(0)(x => (A * x + B) % M)
generator.take(11).toList
// List(0, 5, 7, 10, 9, 2, 8, 6, 3, 4, 0)
```

---

**Links:**

- [Source code][sample]
- [Euclid's algorithm](https://ru.wikipedia.org/wiki/%D0%90%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC_%D0%95%D0%B2%D0%BA%D0%BB%D0%B8%D0%B4%D0%B0)
- [Rod Stephens - Algorithms: Theory and Practice. Chapter 2. Numerical Algorithms](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854/)
- [Divisor function][divisorfunc]
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [SICP: Abelson H., Sussman G. - Structure and Interpretation of Computer Programs][sicp]
- [Scalacaster](https://github.com/vkostyukov/scalacaster)
- [The Art of Computer Programming - Donald E. Knuth](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf
[sample]: https://gitverse.ru/scalabook/algorithms
[divisorfunc]: https://ru.wikipedia.org/wiki/%D0%A4%D1%83%D0%BD%D0%BA%D1%86%D0%B8%D1%8F_%D0%B4%D0%B5%D0%BB%D0%B8%D1%82%D0%B5%D0%BB%D0%B5%D0%B9
