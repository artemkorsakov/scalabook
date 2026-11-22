# Sequences

## Fibonacci Numbers

[Fibonacci numbers](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D0%A4%D0%B8%D0%B1%D0%BE%D0%BD%D0%B0%D1%87%D1%87%D0%B8) —
elements of the numerical sequence `0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, ...`,
in which the first two numbers are `0` and `1`, and each subsequent number is the sum of the two preceding numbers.

Terms of this sequence are denoted by \\(F\_{n}\\).
Formal definition: \\(F\_{0} = 0; F\_{1} = 1; F\_{n + 2} = F\_{n+1} + F\_{n}, n \geq 0.\\)

### Some Properties

- **Formula 1**: \\(F\_{n+1}F\_{n-1} - F\_{n}^{2} = (-1)^{n}\\)
- From **Formula 1** it follows that numbers \\(F\_{n}\\) and \\(F\_{n+1}\\) are coprime,
  since any common divisor of both must also be a divisor of \\((−1)^{n}\\).
- **Formula 2**: \\(\begin{pmatrix} F\_{n+1} & F\_{n} \\\\ F\_{n} & F\_{n-1} \\\\ \end{pmatrix} = \begin{pmatrix} 1 & 1 \\\\ 1 & 0 \\\\ \end{pmatrix}^{n}\\)
- **Formula 3**: \\(F\_{n+m} = F\_{m}F\_{n+1} + F\_{m-1}F\_{n}\\)
- **Theorem A** (E. Lucase, 1876). Some integer divides both \\(F\_{m}\\) and \\(F\_{n}\\) if and only if
  it is a divisor of \\(F\_{d}\\), where \\(d = gcd(m, n)\\); in particular, \\(gcd(F\_{m}, F\_{n}) = F\_{gcd(m,n)}\\).
- **Formula 4**: \\(F\_{n} = \frac{1}{\sqrt{5}}(\phi^{n} - \hat{\phi}^{n})\\),
  where \\(\phi = \frac{1 + \sqrt{5}}{2}\\) and \\(\hat{\phi} = 1 - \phi = \frac{1 - \sqrt{5}}{2}\\).
- **Formula 5**: \\(F\_{n} = \frac{\phi^{n}}{\sqrt{5}}\\), rounded to the nearest integer.
- **Formula 6**: \\(\sum\_{k=0}^{n}F\_{k}F\_{n-k} = \frac{n - 1}{5}F\_{n} + \frac{2n}{5}F\_{n-1}\\)


### Exact Value

> Compute the n-th Fibonacci number.

**Algorithm:**

- **Step 1** (Base case). If `n` is `0`, return `0` (the first Fibonacci number).
- **Step 2** (Defining variables).
    - `a`: The previous Fibonacci number \\(F\_{n-2}\\}. Initial value: \\(F\_0 = 0\\).
    - `b`: The current Fibonacci number \\(F\_{n-1}\\}. Initial value: \\(F\_1 = 1\\).
    - `count`: The number of remaining iterations. Initial value: `n - 1`.
- **Step 3** (Iteration).
    - If `count` is `0`, return `b` (the current Fibonacci number).
    - Otherwise, go back to the previous step with new values:
        - Assign `a` the value of `b` (the next Fibonacci number becomes the previous one).
        - Assign `b` the value of `a + b` (compute the next Fibonacci number).
        - Decrease `count` by `1`.

**Example:**

Example of algorithm execution for `n = 5`

- **Initialization:** `n = 5`
- **First iteration:** `a = 0`, `b = 1`, `count = 4`
- **Second iteration:** `a = 1`, `b = 1`, `count = 3`
- **Third iteration:** `a = 1`, `b = 2`, `count = 2`
- **Fourth iteration:** `a = 2`, `b = 3`, `count = 1`
- **Fifth iteration:** `a = 3`, `b = 5`, `count = 0`
- **Result:** the 5th Fibonacci number is `5`.

**Complexity:**

The algorithm has linear complexity.

- **Time** - O(n)
- **Memory** - O(1)

**Code:**

> The algorithm is based on tail recursion, which avoids stack overflow with large values of `n`.

```dotty
def fib(n: Int): BigInt =
  @tailrec
  def loop(a: BigInt, b: BigInt, count: Int): BigInt =
    if count == 0 then b
    else loop(b, a + b, count - 1)

  if n == 0 then BigInt(0) else loop(BigInt(0), BigInt(1), n - 1)

fib(100)
// 354224848179261915075  
```

### Fibonacci Numbers with Logarithmic Complexity

> Compute the Fibonacci number with logarithmic complexity

**Algorithm:**

The algorithm is taken from **Exercise 1.19** in [SICP][sicp]:

- The transformation of state variables a and b: \\(\acute{a} ← a + b\\) and \\(\acute{b} ← a\\}.
- Let us call this transformation T and notice that applying T n times, starting from 1 and 0,
  gives us the pair \\(F\_{n+1}\\) and \\(F\_{n}\\}.
  In other words, Fibonacci numbers are obtained by applying \\(T^{n}\\}, the n-th power of transformation T, to the pair (1,0).
- Now consider T as a special case of p = 0, q = 1 in a family of transformations \\(T\_{pq}\\},
  where \\(T\_{pq}\\} transforms the pair (a,b) by the rule \\(a \leftarrow bq + aq + ap\\}, \\(b \leftarrow bp + aq\\}.
- Applying the transformation \\(T\_{pq}\\} twice is equivalent to applying the transformation
  \\(T\_{\acute{p}\acute{q}}\\} of the same type, where \\(\acute{p} = p^{2} + q^{2}\\} and \\(\acute{q} = 2pq + q^{2}\\}
- This gives a direct way to square such transformations, and thus \\(T^{n}\\} can be computed
  using successive squaring

**Example:**

Consider an example of executing the code to compute the 10th Fibonacci number (`n = 10`).

- **Step 1: Initialization**
    - `a = 1`, `b = 0`, `p = 0`, `q = 1`, `count = 10`
- **Step 2: Iterative process**
    - **Iteration 1:**
        - Checking condition `count == 0`: `10 != 0` — continue.
        - Checking condition `count % 2 == 0`: `10 % 2 == 0` — condition is true.
        - Call loop with new values:
        - `a = 1`, `b = 0`, `p = p * p + q * q = 0 * 0 + 1 * 1 = 1`, `q = 2 * p * q + q * q = 2 * 0 * 1 + 1 * 1 = 1`, `count = count / 2 = 10 / 2 = 5`
    - **Iteration 2:**
        - Checking condition `count == 0`: `5 != 0` — continue.
        - Checking condition `count % 2 == 0`: `5 % 2 != 0` — condition is false.
        - Call loop with new values:
        - `a = b * q + a * (p + q) = 0 * 1 + 1 * (1 + 1) = 2`, `b = b * p + a * q = 0 * 1 + 1 * 1 = 1`, `p = 1`, `q = 1`, `count = count - 1 = 5 - 1 = 4`
    - **Iteration 3:**
        - Checking condition `count == 0`: `4 != 0` — continue.
        - Checking condition `count % 2 == 0`: `4 % 2 == 0` — condition is true.
        - Call loop with new values:
        - `a = 2`, `b = 1`, `p = p * p + q * q = 1 * 1 + 1 * 1 = 2`, `q = 2 * p * q + q * q = 2 * 1 * 1 + 1 * 1 = 3`, `count = count / 2 = 4 / 2 = 2`
    - **Iteration 4:**
        - Checking condition `count == 0`: `2 != 0` — continue.
        - Checking condition `count % 2 == 0`: `2 % 2 == 0` — condition is true.
        - Call loop with new values:
        - `a = 2`, `b = 1`, `p = p * p + q * q = 2 * 2 + 3 * 3 = 13`, `q = 2 * p * q + q * q = 2 * 2 * 3 + 3 * 3 = 21`, `count = count / 2 = 2 / 2 = 1`
    - **Iteration 5:**
        - Checking condition `count == 0`: `1 != 0` — continue.
        - Checking condition `count % 2 == 0`: `1 % 2 != 0` — condition is false.
        - Call loop with new values:
        - `a = b * q + a * (p + q) = 1 * 21 + 2 * (13 + 21) = 109`, `b = b * p + a * q = 1 * 13 + 2 * 21 = 55`, `p = 13`, `q = 21`, `count = count - 1 = 1 - 1 = 0`
    - **Iteration 6:**
        - Checking condition `count == 0`: `0 == 0` — condition is true.
        - Return value `b = 55`.

**Complexity:**

- **Time** - O(log(n))
- **Memory** - O(1)

**Code:**

```dotty
def fastFib(n: Int): BigInt =
  @tailrec
  def loop(a: BigInt, b: BigInt, p: BigInt, q: BigInt, count: Int): BigInt =
    if count == 0 then b
    else if count % 2 == 0 then
      loop(a, b, p * p + q * q, 2 * p * q + q * q, count / 2)
    else loop(b * q + a * (p + q), b * p + a * q, p, q, count - 1)

  loop(1, 0, 0, 1, n)

fastFib(100)
// 354224848179261915075  
```

### Approximate Value

**Algorithm:**

An approximate value of a Fibonacci number can be computed [using Binet's formula](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D0%A4%D0%B8%D0%B1%D0%BE%D0%BD%D0%B0%D1%87%D1%87%D0%B8#%D0%A4%D0%BE%D1%80%D0%BC%D1%83%D0%BB%D0%B0_%D0%91%D0%B8%D0%BD%D0%B5):

\\(F\_{n} = \frac{(\frac{1+\sqrt{5}}{2})^{n} - (\frac{1-\sqrt{5}}{2})^{n}}{\sqrt{5}} = \frac{(\varphi)^{n} - (-\varphi)^{-n}}{\varphi - (-\varphi)^{-1}} = \frac{(\varphi)^{n} - (-\varphi)^{-n}}{2\varphi - 1}\\)

**Complexity:**

- **Time** - O(log n)
- **Memory** - O(1)

**Code:**

```dotty
private val PHI: BigDecimal = BigDecimal((1.0 + math.sqrt(5.0)) / 2.0)

def approximateFibonacci(n: Int): BigInt =
  (PHI.pow(n) / BigDecimal(math.sqrt(5)))
    .setScale(0, BigDecimal.RoundingMode.HALF_UP)
    .toBigInt
```

## Pythagorean Triple

> [Pythagorean triple](https://ru.wikipedia.org/wiki/%D0%9F%D0%B8%D1%84%D0%B0%D0%B3%D0%BE%D1%80%D0%BE%D0%B2%D0%B0_%D1%82%D1%80%D0%BE%D0%B9%D0%BA%D0%B0) —
> is a set of three natural numbers \\(a < b < c\\},
> for which \\(a^{2} + b^{2} = c^{2}\\}.
> Let us represent \\(a = m^{2} - n^{2}, b = 2\*m\*n, c = m^{2} + n^{2}\\}. Then \\((m,n)\\} is a Pythagorean duad.
>
> [The tree of primitive Pythagorean triples](https://ru.wikipedia.org/wiki/%D0%94%D0%B5%D1%80%D0%B5%D0%B2%D0%BE_%D0%BF%D1%80%D0%B8%D0%BC%D0%B8%D1%82%D0%B8%D0%B2%D0%BD%D1%8B%D1%85_%D0%BF%D0%B8%D1%84%D0%B0%D0%B3%D0%BE%D1%80%D0%BE%D0%B2%D1%8B%D1%85_%D1%82%D1%80%D0%BE%D0%B5%D0%BA)
> can be constructed using either a Pythagorean duad or a Pythagorean triple.

**Code:**

Pythagorean duad:

```dotty
final case class PythagoreanDeuce(m: Long, n: Long):
  lazy val a: Long                     = m * m - n * n
  lazy val b: Long                     = 2 * m * n
  lazy val c: Long                     = m * m + n * n
  lazy val triplet: PythagoreanTriplet = PythagoreanTriplet(a, b, c)
  lazy val nextPythagoreanDeuce: List[PythagoreanDeuce] =
    List(
      PythagoreanDeuce(2 * m - n, m),
      PythagoreanDeuce(2 * m + n, m),
      PythagoreanDeuce(m + 2 * n, n)
    )
end PythagoreanDeuce

object PythagoreanDeuce:
  val primitivePythagoreanDeuce: PythagoreanDeuce = PythagoreanDeuce(2, 1)

import PythagoreanDeuce.primitivePythagoreanDeuce

primitivePythagoreanDeuce.triplet
// PythagoreanTriplet(3,4,5)
primitivePythagoreanDeuce.nextPythagoreanDeuce
// List(PythagoreanDeuce(3,2), PythagoreanDeuce(5,2), PythagoreanDeuce(4,1))
```

Pythagorean triple:

```dotty
final case class PythagoreanTriplet(a: Long, b: Long, c: Long):
  lazy val nextPythagoreanTriplet: List[PythagoreanTriplet] =
    List(
      PythagoreanTriplet(
        a - 2 * b + 2 * c,
        2 * a - b + 2 * c,
        2 * a - 2 * b + 3 * c
      ),
      PythagoreanTriplet(
        a + 2 * b + 2 * c,
        2 * a + b + 2 * c,
        2 * a + 2 * b + 3 * c
      ),
      PythagoreanTriplet(
        -a + 2 * b + 2 * c,
        -2 * a + b + 2 * c,
        -2 * a + 2 * b + 3 * c
      )
    )
end PythagoreanTriplet

object PythagoreanTriplet:
  val primitivePythagoreanTriplet: PythagoreanTriplet =
    PythagoreanTriplet(3, 4, 5)

import PythagoreanTriplet.primitivePythagoreanTriplet

primitivePythagoreanTriplet.nextPythagoreanTriplet
// List(PythagoreanTriplet(5,12,13), PythagoreanTriplet(21,20,29), PythagoreanTriplet(15,8,17))
```

Finding [Pythagorean triples with a given sum](https://projecteuler.net/problem=9):

```dotty
def pythagoreanTripletsWithGivenSum(sum: Long): Array[PythagoreanTriplet] =
  if sum % 2 == 1 then new Array(0)
  else
    val arrayBuffer = mutable.ArrayBuffer.empty[PythagoreanTriplet]
    val s2          = sum / 2
    val sqrt        = math.sqrt(s2.toDouble).toLong
    val mLimit      = if sqrt * sqrt == s2 then sqrt - 1 else sqrt

    @tailrec
    def reduceLoop(n: Long, i: Long): Long =
      if n % i == 0 then reduceLoop(n / i, i) else n

    for m <- 2L to mLimit do
      if s2 % m == 0 then
        // reduce the search space by removing all factors of 2
        val sm = reduceLoop(s2 / m, 2)

        @tailrec
        def loop(k: Long): Unit =
          if k >= 2 * m || k > sm then ()
          else if sm % k == 0 && gcd(k, m) == 1 then
            val d = s2 / (k * m)
            val n = k - m
            val a = d * (m * m - n * n)
            val b = 2 * d * m * n
            val c = d * (m * m + n * n)
            arrayBuffer += PythagoreanTriplet(a, b, c)
            loop(k + 2)
          else loop(k + 2)

        loop(if m % 2 == 1 then m + 2 else m + 1)

    arrayBuffer.toArray

Array(PythagoreanTriplet(375,200,425))    
```

## Cyclic Sequences

Cyclic sequences:

- Triangular numbers; \\(T\_{n} = \frac{n(n+1)}{2}\\}; 1, 3, 6, 10, 15, ...
- Square numbers; \\(P\_{4,n} = n^{2}\\}; 1, 4, 9, 16, 25, ...
- Pentagonal numbers; \\(P\_{5,n} = \frac{n(3n−1)}{2}\\}; 1, 5, 12, 22, 35, ...
- Hexagonal numbers; \\(H\_{n} = n(2n−1)\\}; 1, 6, 15, 28, 45, ...
- Heptagonal numbers; \\(P\_{7,n} = \frac{n(5n−3)}{2}\\}; 1, 7, 18, 34, 55, ...
- Octagonal numbers; \\(P\_{8,n} = n(3n−2)\\}; 1, 8, 21, 40, 65, ...

**Code:**

```dotty
object CyclicNumbers:
  def triangleNumber(n: Long): Long = n * (n + 1) / 2

  def isTriangle(n: Long): Boolean =
    val sol = math.round(-1.0 / 2.0 + math.sqrt(2.0 * n + 1.0 / 4.0))
    triangleNumber(sol) == n

  def squareNumber(n: Long): Long = n * n

  def isSquare(n: Long): Boolean = math.sqrt(n.toDouble).isWhole

  def pentagonalNumber(n: Long): Long = n * (3 * n - 1) / 2

  def isPentagonal(n: Long): Boolean =
    val sol = math.round(1.0 / 6.0 + math.sqrt(2.0 * n / 3.0 + 1.0 / 36.0))
    pentagonalNumber(sol) == n

  def hexagonalNumber(n: Long): Long = n * (2 * n - 1)

  def isHexagonal(n: Long): Boolean =
    val sol = math.round(1.0 / 4.0 + math.sqrt(n / 2.0 + 1.0 / 16.0))
    hexagonalNumber(sol) == n

  def heptagonalNumber(n: Long): Long = n * (5 * n - 3) / 2

  def isHeptagonal(n: Long): Boolean =
    val sol = math.round(3.0 / 10.0 + math.sqrt(2.0 * n / 5.0 + 9.0 / 100.0))
    heptagonalNumber(sol) == n

  def octagonalNumber(n: Long): Long = n * (3 * n - 2)

  def isOctagonal(n: Long): Boolean =
    val sol = math.round(1.0 / 3.0 + math.sqrt(n / 3.0 + 1.0 / 9.0))
    octagonalNumber(sol) == n
end CyclicNumbers

triangleNumber(1_000_000)    // 500000500000
squareNumber(1_000_000)      // 1000000000000
pentagonalNumber(1_000_000)  // 1499999500000
hexagonalNumber(1_000_000)   // 1999999000000
heptagonalNumber(1_000_000)  // 2499998500000
octagonalNumber(1_000_000)   // 2999998000000
```

## Collatz Conjecture

> Take any natural number n. If it is even, divide it by 2,
> and if it is odd, multiply it by 3 and add 1 (we get 3n + 1).
> Perform the same actions on the resulting number, and so on.
>
> [The Collatz conjecture](https://ru.wikipedia.org/wiki/%D0%93%D0%B8%D0%BF%D0%BE%D1%82%D0%B5%D0%B7%D0%B0_%D0%9A%D0%BE%D0%BB%D0%BB%D0%B0%D1%82%D1%86%D0%B0)
> states that no matter what starting number n we choose, we will eventually reach 1.

**Algorithm:**

Count `collatz` — the number of iterations to reach `1` from a given `n`:

- If `n` is even, then `collatz(n) = collatz(n / 2) + 1`
- If `n` is odd, then `3n + 1` is even (and not `1`), so `collatz(n) = collatz((3 * n + 1) / 2) + 2`

**Example:**

Let us see how this algorithm works with the number `6`.

- Start with `n = 6`.
- Since `6` is even, we divide it by `2`: `n = 6 / 2 = 3`. Increment the counter by `1` (`count = 1`).
- Now we have `n = 3`, which is odd. Apply the formula `3n + 1: n = (3 * 3 + 1) / 2 = 5`. Increment the counter by `2` (`count = 3`).
- Now we have `n = 5`, which is odd. Apply the formula: `n = (3 * 5 + 1) / 2 = 8`. Increment the counter by `2` (`count = 5`).
- Now `n = 8`, even. Divide by `2`: `n = 8 / 2 = 4`. Increment the counter by `1` (`count = 6`).
- `n = 4`, even. Divide by `2`: `n = 4 / 2 = 2`. Increment the counter by `1` (`count = 7`).
- `n = 2`, even. Divide by `2`: `n = 2 / 2 = 1`. Increment the counter by `1` (`count = 8`).
- Now we have reached `n = 1`, and the algorithm terminates.
- The total number of steps (or iterations) required to reach `1` is `8`.

**Complexity:**

- Time complexity:
    - Worst case: The Collatz sequence can grow exponentially before reaching 1.
      In the worst case, the time complexity of the algorithm can be exponential \\(O(2^{n})\\}, but this depends on the specific values of n.
    - Average case: On average, the time complexity of the algorithm is estimated as O(log n),
      since the Collatz sequence usually converges quickly to 1.

Summary:

- **Time** - on average O(log n), in the worst case \\(O(2^{n})\\}
- **Memory** - O(1)

**Code:**

```dotty
@tailrec
def nextCollatz(n: Long, count: Long = 0): Long =
  if n == 1 then count
  else if n % 2 == 0 then nextCollatz(n / 2, count + 1)
  else nextCollatz((3 * n + 1) / 2, count + 2)

nextCollatz(27)
// 111    
```

## Hamming Numbers

> A positive number number is a generalized Hamming number of type n
> if all its prime factors do not exceed n.

**Algorithm:**

- **Step 1** (Checking initial conditions).
    - If number is less than 2 or less than or equal to n, then it is a Hamming number, since all prime factors of a number less than or equal to n are less than or equal to n.
    - If n is less than 2, then number is not a Hamming number, since there are no prime numbers less than 2.
- **Step 2** (Generating a list of prime numbers).
    - Generate a list of prime numbers up to n using ["Sieve of Eratosthenes"](number_theory/primes.md).
- **Step 3** (Checking prime factors).
    - For each prime number in the list:
        - If the prime number is greater than n, then number is not a Hamming number.
        - If number is divisible by the prime number, reduce it. Assign number the value after reduction.
        - If number becomes 1, then it is a Hamming number.
        - If number is still greater than 1, repeat step 3 for the next prime number.

**Example:**

Consider the example for number 30 and n = 5:

- **Step 1**: Checking initial conditions: number = 30, n = 5
    - 30 is greater than 2 and greater than 5.
- **Step 2**: Generating a list of prime numbers
    - Generate the list of prime numbers up to 5. That is: 2, 3, 5.
- **Step 3**: Checking prime factors
    - First prime number: 2. 30 is divisible by 2: 30 / 2 = 15. Now number = 15.
    - Second prime number: 3. 15 is divisible by 3: 15 / 3 = 5. Now number = 5.
    - Third prime number: 5. 5 is divisible by 5: 5 / 5 = 1. Now number = 1.
- **Conclusion**: Since number has become 1, we can conclude that 30 is a Hamming number with respect to n = 5.

**Complexity:**

- Asymptotic complexity
- Generating prime numbers: If using Sieve of Eratosthenes, that would be \\(O(n \log \log n)\\}.
- Loop: In the worst case, for each prime number we can perform division, which in general can take \\(O(\log(\text{number}))\\} for each prime number.
  If k is the number of prime numbers up to n, then this will be \\(O(k \cdot \log(\text{number}))\\}.
- Thus, the overall asymptotic complexity of the algorithm will be \\(O(n \log \log n + k \cdot \log(\text{number}))\\}, where k is the number of prime numbers up to n.

Summary:

- **Time** - \\(O(n \log \log n + k \cdot \log(\text{number}))\\}, where k is the number of prime numbers up to n.
- **Memory** - \\(O(k)\\}.

**Code:**

```dotty
def isHammingNumber(number: Long, n: Int): Boolean =
  number < 2 || number <= n || {
    if n < 2 then false
    else
      val primesList = primesNoMoreThanN(n)

      @tailrec
      def reduceLoop(n: Long, i: Long): Long =
        if n % i == 0 then reduceLoop(n / i, i) else n

      @tailrec
      def loop(n: Long, i: Int): Boolean =
        i < primesList.length && {
          val newN = reduceLoop(n, primesList(i))
          newN == 1 || loop(newN, i + 1)
        }

      loop(number, 0)
  }

private def primesNoMoreThanN(n: Int): Array[Int] = ... // Described in the "Factorization" chapter
    
isHammingNumber(2125764000, 5)
// true    
```

## Harshad Numbers

> [_Harshad numbers_](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D1%85%D0%B0%D1%80%D1%88%D0%B0%D0%B4),
> or Niven numbers, are natural numbers that are evenly divisible by the sum of their digits.
> For example, 1729 is such a number, since 1729 = (1 + 7 + 2 + 9) × 91.

> A _strong Harshad number_ is a Harshad number that when divided by the sum of its digits yields a prime number.

> 201 is a Harshad number because it is divisible by 3 (the sum of its digits).
> When we remove the last digit from 201, we get 20, which is a Harshad number.
> When we remove the last digit from 20, we get 2, which is also a Harshad number.
> Let us call a Harshad number that, upon recursive removal of the last digit, always results in a Harshad number,
> a _right-truncatable Harshad number_.

> Take the number 2011, which is prime.
> If we remove the last digit from it, we get 201 — a strong Harshad number that is also right-truncatable.
> Let us call such prime numbers _strong, right-truncatable Harshad primes_.

**Code:**

```dotty
object HarshadNumber:
  /** A Harshad number is an integer that is divisible by the sum of its digits.
    */
  def isHarshadNumber(number: Long): Boolean = number % sumOfDigits(number) == 0

  /** A strong Harshad number is a Harshad number that when divided by the sum
    * of its digits yields a prime number.
    */
  def isStrongHarshadNumber(number: Long): Boolean =
    val sod = sumOfDigits(number)
    number % sod == 0 && isPrime(number / sod)

  /** Returns all strong, right-truncatable Harshad primes less than
    * 10<sup>p</sup>.
    */
  def getStrongRightTruncatableHarshadPrimes(p: Int): Seq[Long] =
    (1 until p - 1).foldLeft((
      Seq(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L),
      Seq.empty[Long]
    )) { case ((numbers, acc), _) =>
      val next = rightTruncatableHarshadNumbers(numbers)
      val newAcc = acc ++
        next
          .withFilter(isStrongHarshadNumber)
          .flatMap(strongRightTruncatableHarshadPrimes)
      (next, newAcc)
    }._2

  /** Take the number 2011, which is prime. If we remove the last digit
    * from it, we get 201 — a strong Harshad number that is also
    * right-truncatable. Let us call such prime numbers strong, right-truncatable
    * Harshad primes.
    */
  private def strongRightTruncatableHarshadPrimes(
      strongHarshadNumber: Long
  ): Seq[Long] =
    (1 to 9 by 2)
      .withFilter(i => isPrime(strongHarshadNumber * 10 + i))
      .map(strongHarshadNumber * 10 + _)

  /** 201 is a Harshad number because it is divisible by 3 (the sum of its digits).
    * When we remove the last digit from 201, we get 20, which is a
    * Harshad number. When we remove the last digit from 20, we get 2,
    * which is also a Harshad number. Let us call a Harshad number that, upon
    * recursive removal of the last digit, always results in a Harshad number,
    * a right-truncatable Harshad number.
    */
  private def rightTruncatableHarshadNumbers(
      harshadNumbers: Seq[Long]
  ): Seq[Long] =
    harshadNumbers.flatMap(rightTruncatableHarshadNumbers)

  private def rightTruncatableHarshadNumbers(harshadNumber: Long): Seq[Long] =
    val sod       = sumOfDigits(harshadNumber)
    val newNumber = harshadNumber * 10
    (0 to 9)
      .withFilter(i => (newNumber + i) % (sod + i) == 0)
      .map(newNumber + _)

  private def sumOfDigits(number: BigInt): Int =
    number.toString.foldLeft(0)((s, ch) => s + ch.asDigit)

  private def isPrime(n: Long): Boolean = ... // From the section on prime numbers
end HarshadNumber
    
getStrongRightTruncatableHarshadPrimes(3)
// Vector(181, 211, 271, 277, 421, 457, 631) 
```

## Bernoulli Numbers

[Bernoulli numbers](https://ru.wikipedia.org/wiki/%D0%A7%D0%B8%D1%81%D0%BB%D0%B0_%D0%91%D0%B5%D1%80%D0%BD%D1%83%D0%BB%D0%BB%D0%B8)

For Bernoulli numbers, the following recurrence formula exists:

- \\(B\_{0} = 1\\)
- \\(B\_{n}={\frac{-1}{n+1}}\sum\_{k=1}^{n}{\binom {n+1}{k+1}}B\_{n-k}, \quad n \in \mathbb {N} \\)

All Bernoulli numbers with odd indices, except \\(B\_{1} = -\frac{1}{2}\\}, are zero,
and the signs of Bernoulli numbers with even indices alternate.

Therefore, the formula for computing \\(B\_{n}\\} for even \\(n \geq 2\\} can be simplified:
\\(B\_{n} = \frac{1}{2} + {\frac{-1}{n+1}}\sum\_{k=2,4,...}^{n}{\binom {n+1}{k+1}}B\_{n-k}, \quad n \in \mathbb {N} \\)

**Code:**

[Bernoulli number generator](https://ru.numberempire.com/bernoullinumbers.php),
using the [spire](../../libs/spire.md) library

```dotty
import spire.compat.fractional
import spire.math.Rational
import scala.collection.mutable
  
val bernoulli: Int => Rational =
  val cache = mutable.HashMap.empty[Int, Rational]

  def compute(n: Int): Rational = cache.getOrElseUpdate(
    n, {
      n match
        case 0 => Rational(1)
        case 1 => Rational(-1, 2)
        case n if n % 2 == 1 => Rational(0)
        case n =>
          val s = (2 to n by 2).map { l =>
            compute(n - l) * Rational(binomialCoefficient(n + 1, l + 1))
          }.sum
          Rational(1, 2) - (s / (n + 1))
    }
  )

  compute
        
bernoulli(50)
// 495057205241079648212477525/66 
```

## Padovan Numbers

**Algorithm:**

[Padovan sequence (or Padovan numbers)](https://oeis.org/A000931) is computed by the formula:

- \\(a\_{0} = 1, a\_{1} = a\_{2} = 0\\)
- \\(a\_{n} = a\_{n-2} + a\_{n-3}\\)

**Complexity:**

- **Time** - O(n)
- **Memory** - O(1)

**Code:**

```dotty
def padovan(n: Int): BigInt =
  @tailrec
  def loop(a: BigInt, b: BigInt, c: BigInt, i: Int): BigInt =
    if i == 0 then c else loop(b, c, a + b, i - 1)

  if n == 0 then 1
  else if n <= 2 then 0
  else loop(1, 0, 0, n - 2)
    
padovan(100)
// 288627200960 
```

## Jacobsthal Numbers

**Algorithm:**

[Jacobsthal sequence (or Jacobsthal numbers)](https://oeis.org/A001045):
\\(a\_{n} = a\_{n-1} + 2\*a\_{n-2}\\}, where \\(a\_{0} = 0, a\_{1} = 1\\};
also \\(a\_{n}\\} is the nearest integer to \\(\frac{2^n}{3}\\}.

**Complexity:**

- **Time** - O(n)
- **Memory** - O(1)

**Code:**

```dotty
def jacobsthal(i: Int): BigInt =
  @tailrec
  def loop(a: BigInt, b: BigInt, i: Int): BigInt =
    if i == 0 then b
    else loop(b, b + 2 * a, i - 1)

  if i <= 1 then i
  else loop(0, 1, i - 1)

jacobsthal(100)
// 422550200076076467165567735125
```

## Pell Numbers

**Algorithm:**

[Pell numbers](https://oeis.org/A000129):
\\(a\_{n} = 2\*a\_{n-1} + a\_{n-2}\\}, where \\(a\_{0} = 0, a\_{1} = 1\\}.

**Complexity:**

- **Time** - O(n)
- **Memory** - O(1)

**Code:**

```dotty
def pell(i: Int): BigInt =
  @tailrec
  def loop(a: BigInt, b: BigInt, i: Int): BigInt =
    if i == 0 then b
    else loop(b, 2 * b + a, i - 1)

  if i <= 1 then i
  else loop(0, 1, i - 1)
    
pell(100)
// 66992092050551637663438906713182313772
```

## Tribonacci Numbers

**Algorithm:**

[Tribonacci numbers](https://oeis.org/A000073):
\\(a\_{n} = a\_{n-1} + a\_{n-2} + a\_{n-3}\\} for \\(n \geq 3\\}, with \\(a\_{0} = a\_{1} = 0, a\_{2} = 1\\}.

**Complexity:**

- **Time** - O(n)
- **Memory** - O(1)

**Code:**

```dotty
def tribonacci(i: Int): BigInt =
  @tailrec
  def loop(a: BigInt, b: BigInt, c: BigInt, i: Int): BigInt =
    if i == 0 then c
    else loop(b, c, a + b + c, i - 1)

  if i <= 1 then 0
  else if i == 2 then 1
  else loop(0, 0, 1, i - 2)
    
tribonacci(100)
// 53324762928098149064722658  
```

## Tetranacci Numbers

**Algorithm:**

[Tetranacci numbers](https://oeis.org/A000078):
\\(a\_{n} = a\_{n-1} + a\_{n-2} + a\_{n-3} + a\_{n-4}\\}
for \\(n \geq 4\\}, with \\(a\_{0} = a\_{1} = a\_{2} = 0, a\_{3} = 1\\}.

**Complexity:**

- **Time** - O(n)
- **Memory** - O(1)

**Code:**

```dotty
def tetranacci(i: Int): BigInt =
  @tailrec
  def loop(
      a: BigInt,
      b: BigInt,
      c: BigInt,
      d: BigInt,
      i: Int
  ): BigInt =
    if i == 0 then d
    else loop(b, c, d, a + b + c + d, i - 1)

  if i <= 2 then 0
  else if i == 3 then 1
  else loop(0, 0, 0, 1, i - 3)
    
tetranacci(100)
// 2505471397838180985096739296
```

---

**References:**

- [Source code][sample]
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Memo.html)
- [SICP: Abelson H., Sussman D. - Structure and Interpretation of Computer Programs][sicp]
- [Scalacaster](https://github.com/vkostyukov/scalacaster)
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.8](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf
[sample]: https://gitverse.ru/scalabook/algorithms
