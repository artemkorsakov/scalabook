# Prime Numbers and Factorization

[A prime number](https://ru.wikipedia.org/wiki/%D0%9F%D1%80%D0%BE%D1%81%D1%82%D0%BE%D0%B5_%D1%87%D0%B8%D1%81%D0%BB%D0%BE) —
a natural number having exactly two distinct natural divisors.
In other words, a natural number `p` is prime if it is distinct from `1`
and is divisible without remainder only by `1` and by `p` itself.
This is an infinite sequence: `<2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,...>`.
One of the popular applications of prime numbers is cryptography.
Prime numbers are also used in hash functions to avoid collisions,
in random number generators for uniform distribution, and in error-correcting codes to eliminate noise.

[A composite number](https://ru.wikipedia.org/wiki/%D0%A1%D0%BE%D1%81%D1%82%D0%B0%D0%B2%D0%BD%D0%BE%D0%B5_%D1%87%D0%B8%D1%81%D0%BB%D0%BE) —
a natural number that has divisors other than one and itself.
Every composite number is the product of two or more natural numbers greater than one.

All natural numbers fall into three disjoint categories: prime, composite, and one.

## Prime Number List

> Construct a list of prime numbers from 2 to n.

**Algorithm:**

- **Step 1** (Current list of prime numbers). Set `primes` equal to `{ 2 }` as the current list of prime numbers.
- **Step 2** (Current number). For each `i` from `3` to `n`:
    - **Step 2.1** (Primality test). For each prime number `p` in `primes`, less than the square root of `i`,
      check whether `i` is divisible by `p`.
      If `i` is divisible by `p`, then `i` is composite; proceed to the next number `i`.
    - **Step 2.2** (Adding a prime number). If `i` is not divisible by any such `p`,
      then `i` is prime; add it to `primes`. Proceed to the next number `i`.
- **Step 3** (Return the list of prime numbers). Return `primes`.

**Example:**

Construct the list of prime numbers from 2 to 10.

- **Step 1**: Start with the current list of prime numbers: `primes = {2}`.
- **Step 2**: Start checking numbers from `3` to `10`.
    - `i = 3`:
        - **Step 2.1**: Check whether `3` is divisible by prime numbers in `primes` less than the square root of `3` (approximately `1.73`).
          `primes` only contains `2`, and `2 > 1.73`, so no check is needed.
        - **Step 2.2**: `3` is not divisible by any prime number; add `3` to `primes`.
          Now `primes = {2, 3}`.
    - `i = 4`:
        - **Step 2.1**: Check whether `4` is divisible by prime numbers in `primes` less than the square root of `4` (which is `2`).
          Check `2`: `4` is divisible by `2`, so `4` is composite. Proceed to the next number.
    - `i = 5`:
        - **Step 2.1**: Check whether `5` is divisible by prime numbers in `primes` less than the square root of `5` (approximately `2.24`).
          Check `2`: `5` is not divisible by `2`.
        - **Step 2.2**: `5` is not divisible by any prime number; add `5` to `primes`.
          Now `primes = {2, 3, 5}`.
    - `i = 6`:
        - **Step 2.1**: Check whether `6` is divisible by prime numbers in `primes` less than the square root of `6` (approximately `2.45`).
          Check `2`: `6` is divisible by `2`, so `6` is composite. Proceed to the next number.
    - `i = 7`:
        - **Step 2.1**: Check whether `7` is divisible by prime numbers in `primes` less than the square root of `7` (approximately `2.65`).
          Check `2`: `7` is not divisible by `2`.
        - **Step 2.2**: `7` is not divisible by any prime number; add `7` to `primes`.
          Now `primes = {2, 3, 5, 7}`.
    - `i = 8`:
        - **Step 2.1**: Check whether `8` is divisible by prime numbers in `primes` less than the square root of `8` (approximately `2.83`).
          Check `2`: `8` is divisible by `2`, so `8` is composite. Proceed to the next number.
    - `i = 9`:
        - **Step 2.1**: Check whether `9` is divisible by prime numbers in `primes` less than the square root of `9` (which is `3`).
          Check `2`: `9` is not divisible by `2`.
          Check `3`: `9` is divisible by `3`, so `9` is composite. Proceed to the next number.
    - `i = 10`:
        - **Step 2.1**: Check whether `10` is divisible by prime numbers in `primes` less than the square root of `10` (approximately `3.16`).
          Check `2`: `10` is divisible by `2`, so `10` is composite. Proceed to the next number.
- **Step 3**: Return the list of prime numbers. Thus, the prime numbers from `2` to `10` are: `{2, 3, 5, 7}`.

**Explanation:**

A composite number is a number that has more than two divisors,
meaning it can be expressed as the product of two or more natural numbers
other than one and the number itself.

Consider a composite number n.
If n is composite, it can be factored into a and b such that \\(n = a \times b\\).

If both factors a and b were greater than the square root of n (i.e., \\(a > \sqrt{n}\\) and \\(b > \sqrt{n}\\)),
then the product of these two factors would be greater than \\(\sqrt{n} \times \sqrt{n} = n\\).

Therefore, at least one of the factors must be less than or equal to the square root of n.

If no such factor exists, the number is prime.

**Estimation:**

- **Time** - \\(O(n \cdot \sqrt{n})\\)
- **Memory** - \\(O\left(\frac{n}{\log(n)}\right)\\)

The time estimation is related to the fact that for each number i, divisibility is tested against all prime numbers smaller than its square root.

The memory estimation is based on the fact that in the worst case, if n is sufficiently large,
the number of primes up to n is approximately \\(\frac{n}{\log(n)}\\) (by the prime number theorem).

**Code:**

An "infinite" list of prime numbers can be implemented via `LazyList` using the following algorithm:

- Start the list of prime numbers with an infinite lazy list starting from `2`.
- For each subsequent number `x`, starting from `3`, take this same list of prime numbers up to the square root of `x`
  and check that none of them are divisors of `x`.

```dotty
lazy val primes: LazyList[Int] =
  2 #::
    LazyList
      .from(start = 3, step = 2)
      .filter: x =>
        primes
          .takeWhile(p => p <= math.sqrt(x))
          .forall(p => x % p != 0)

primes.take(10).toVector
// Vector(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
```

## Sieve of Eratosthenes

> Construct a list of prime numbers from 2 to n.

**Algorithm:**

The [Sieve of Eratosthenes][sieve] algorithm (finding prime numbers up to n):

- **Step 1: Initialization**
    - Create a list of numbers from 2 to n.
    - Mark all numbers in the list as "prime".
- **Step 2: Main loop**
    - Take the first unmarked number in the list (on the first iteration this will be 2).
    - Mark all numbers in the list that are multiples of this number as "composite".
    - Repeat steps 1-2 until we reach a number whose square is greater than n.
- **Step 3: Result**
    - All unmarked numbers in the list are prime.

**Notes:**

- Instead of marking "prime"/"composite", we can use a boolean array where `true` means prime and `false` means composite.
- The algorithm can be optimized by starting to mark multiples from the square of the number.

**Example:**

Find all prime numbers up to 30.

- **Step 1: Initialization**
    - Create the list: \[2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30\]
- **Step 2: Main loop**
    - Take the first unmarked number — 2.
    - Mark multiples of 2 as "composite": \[2, 3, **4**, 5, **6**, 7, **8**, 9, **10**, 11, **12**, 13, **14**, 15, **16**, 17, **18**, 19, **20**, 21, **22**, 23, **24**, 25, **26**, 27, **28**, 29, **30**\]
    - Take the next unmarked number — 3.
    - Mark multiples of 3 as "composite": \[2, 3, **4**, 5, **6**, 7, **8**, **9**, **10**, 11, **12**, 13, **14**, **15**, **16**, 17, **18**, 19, **20**, **21**, **22**, 23, **24**, 25, **26**, **27**, **28**, 29, **30**\]
    - Take the next unmarked number — 5.
    - Mark multiples of 5 as "composite": \[2, 3, **4**, 5, **6**, 7, **8**, **9**, **10**, 11, **12**, 13, **14**, **15**, **16**, 17, **18**, 19, **20**, **21**, **22**, 23, **24**, **25**, **26**, **27**, **28**, 29, **30**\]
    - The next unmarked number is 7. But \\(7^2 = 49 > 30\\), so the loop terminates.
- **Step 3: Result**
    - Prime numbers up to 30: \[2, 3, 5, 7, 11, 13, 17, 19, 23, 29\]

**Explanation:**

The idea of the sieve is to sequentially "sieve" numbers,
excluding from consideration those that are multiples of already found prime numbers.

Thus, only prime numbers remain.

**Estimation:**

- **Time** - O(n log log n)
- **Memory** - O(n)

Time complexity:

- The main loop of the algorithm runs for each prime number p up to √n.
- For each prime number p, we mark \\(\frac{n}{p}\\) numbers as "composite".
- The total number of marking operations is:
  \\(\frac{n}{2} + \frac{n}{3} + \frac{n}{5} + \frac{n}{7} + ... + \frac{n}{p}\\), where p is the largest prime ≤ √n.
- This sum is asymptotically equal to \\(n \times (\frac{1}{2} + \frac{1}{3} + \frac{1}{5} + \frac{1}{7} + ... + \frac{1}{p})\\), which equals O(n log log n).

Space complexity:

- Storing information about the primality of numbers from 2 to n requires an array of size n.
- Therefore, the space complexity is O(n).

Conclusion:

The Sieve of Eratosthenes is a very efficient algorithm for finding all prime numbers up to a given n.
Its time complexity O(n log log n) is significantly better than the naive algorithm, which has time complexity O(n²).

**Code:**

```dotty
def sieveOfEratosthenes(n: Int): Array[Boolean] =
  val result = Array.fill(n + 1)(true)
  result(0) = false
  result(1) = false
  (4 to n by 2).foreach(j => result(j) = false)
  for
    i <- 3 to math.sqrt(n).toInt by 2
    if result(i)
    j <- i to n / i
  do result(j * i) = false
  result

sieveOfEratosthenes(10)
// Array(
//   false, false, true, true, false,
//   true, false, true, false, false, false
// )
```

## Primality Test

> Given a number n. Determine whether it is prime.

**Algorithm:**

Checking a given number for primality can be done using the algorithm proposed
[in the review of the following problem](https://projecteuler.net/problem=7).

- **Step 1**. If the number is less than two, it is not prime. Otherwise, proceed to the next step.
- **Step 2**. If the number is less than four, it is prime. Otherwise, proceed to the next step.
- **Step 3**. If the number is even, it is not prime. Otherwise, proceed to the next step.
- **Step 4**. If the number is less than 9, it is prime. Otherwise, proceed to the next step.
- **Step 5**. If the number is a multiple of 3, it is not prime. Otherwise, proceed to the next step.
- **Step 6**. Assign the pair `pair` the value (5, 7). If the number is divisible by any number in the pair, it is not prime. Otherwise, proceed to the next step.
- **Step 7**. Increment each value in the pair by 6.
  Go back to the previous step if one of the values in the pair is not greater than the square root of the given number.
  Otherwise, the number is prime.

**Example:**

Consider the example of a primality test for the number 29.

- **Step 1**: 29 is not less than 2; proceed to the next step.
- **Step 2**: 29 is not less than 4; proceed to the next step.
- **Step 3**: 29 is odd; proceed to the next step.
- **Step 4**: 29 is not less than 9; proceed to the next step.
- **Step 5**: 29 is not a multiple of 3; proceed to the next step.
- **Step 6**: 29 is not divisible by 5 and is not divisible by 7; proceed to the next step.
- **Step 7**: Increment each value in the pair by 6. Now the pair is (11, 13).
  The pair is greater than the square root of 29 (5.38), so 29 is prime.

Thus, by the algorithm, 29 is a prime number.

**Explanation:**

- `1` is not a prime number.
- All prime numbers, except `2`, are odd.
- All prime numbers greater than `3` can be written in the form `6k ± 1`.

**Estimation:**

- **Time** - \\(O(\sqrt{n})\\)
- **Memory** - \\(O(1)\\)

**Code:**

```dotty
def isPrime(n: Long): Boolean =
  if n < 2 then false
  else if n < 4 then true // 2 and 3 are prime
  else if n % 2 == 0 then false
  else if n < 9 then true // we've already excluded 4, 6, and 8
  else if n % 3 == 0 then false
  else
    val limit = math.ceil(math.sqrt(n.toDouble)).toLong

    @annotation.tailrec
    def loop(f: Long): Boolean =
      if f > limit then true
      else if n % f == 0 || n % (f + 2) == 0 then false
      else loop(f + 6)

    loop(5)

isPrime(999_999_000_001L)
// true
```

## Fermat Primality Test

> Given a number n. Determine whether it is prime with a certain probability.

**Explanation:**

Fermat's primality test allows determining whether a number is [prime with a certain probability](https://ru.wikipedia.org/wiki/%D0%92%D0%B5%D1%80%D0%BE%D1%8F%D1%82%D0%BD%D0%BE%D1%81%D1%82%D0%BD%D1%8B%D0%B9_%D0%B0%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC).

[Fermat's little theorem](https://ru.wikipedia.org/wiki/%D0%9C%D0%B0%D0%BB%D0%B0%D1%8F_%D1%82%D0%B5%D0%BE%D1%80%D0%B5%D0%BC%D0%B0_%D0%A4%D0%B5%D1%80%D0%BC%D0%B0)
states: if p is a prime number and 1 <= n < p, then \\(n^{p-1}\\) % p = 1.

This formula may also be true when p is a composite number.
In this case n is called a _Fermat liar_, since it erroneously indicates that p is prime.
If \\(n^{p-1}\\) % p != 1, then n is called a _Fermat witness_ and this value shows that
p is not a prime number.

Strictly speaking, for a natural number p, at least half of the values of n between 1 and p are Fermat witnesses.
Thus, if we run k tests and they all pass successfully, then p is composite
with probability \\(\frac{1}{2^{k}}\\).

**Code:**

A probabilistic algorithm that fails in \\(\frac{1}{2^{max}}\\) cases.

```dotty
import scala.util.Random
import scala.annotation.tailrec

@tailrec
def isProbablyPrime(p: Long, max: Int): Boolean =
  (max <= 0) || {
    (BigInt(Random.nextLong(p)).modPow(p - 1, p) == 1) &&
    isProbablyPrime(p, max - 1)
  }
```

## Finding the Next Prime Number

> Given a prime number p. Find the next prime number.

**Algorithm with explanation:**

- **Step 1**: If `2` is given, return `3`.
- **Step 2**: If `3` is given, return `5`.
- **Step 3**: If any other prime number is given, it is congruent to either `1` or `5` modulo `6` (the others are divisible by `2` and/or `3`),
  which means we need to check `given + 4` (if `1` modulo `6`) or `given + 2` (if `5` modulo `6`) for primality.
- **Step 4**: If the next number is composite, go back to **Step 3**.

**Example:**

Consider the example for the number `13`.

- **Step 1**: Check whether `13` equals `2`. No; proceed to the next step.
- **Step 2**: Check whether `13` equals `3`. No; proceed to the next step.
- **Step 3**: Since `13` is congruent to `1` modulo `6` (the remainder of `13` divided by `6` is `1`), we check the primality of `13 + 4 = 17`.
- **Step 4**: Check whether `17` is a prime number. Yes, it is a prime number.
- **Result**: Return `17`.

**Estimation:**

- **Time** - O(n * √n) = O(n^(3/2))
- **Memory** - O(1)

In the worst case:

- Primality test of a number:
    - The naive algorithm for testing a number's primality has complexity O(√n).
- Finding the next prime number:
    - In the worst case, the algorithm checks every other number after the given one until it finds a prime.
    - This means that in the worst case, the algorithm will perform the primality test O(n) times.

**Code:**

```dotty
def nextPrime(n: Long): Option[Long] =
  if !isPrime(n) then None
  else if n == 2 then Some(3)
  else if n == 3 then Some(5)
  else
    @tailrec
    def loop(p: Long): Long =
      val nextPrime = if p % 3 == 1 then p + 4 else p + 2
      if isPrime(nextPrime) then nextPrime else loop(nextPrime)
    Some(loop(n))

nextPrime(503) // Some(509)
```

## Finding Prime Factors

> Given a number n. Find the set of prime factors of n.

**Algorithm:**

- **Step 1**: Define the list of prime factors as an empty list.
- **Step 2**: If the number is even, add 2 to the list of factors. Divide the number n by 2 until the result is no longer even.
- **Step 3**: Let n be the result from the previous step of reducing n.
  From 3 to √n for each found odd divisor of n, repeat the procedure:
    - **Step 3.1**: Divide n by the found odd divisor as long as it is divisible.
      Add the found odd divisor to the list of factors.
      Assign the resulting reduced n back to n.
- **Step 4**: If anything is left after finding odd divisors, the remainder is also a divisor (and it is prime).
  Add it to the list of factors.

**Example:**

Consider the example of the algorithm working with the number `84`.

- **Step 1**: The list of prime factors is empty.
- **Step 2**: Check whether the number is even:
    - `84` is even.
    - Add `2` to the list of factors: `primes = [2]`.
    - Divide `84` by `2`: `84 / 2 = 42`. `42` is even. Divide `42` by `2`: `42 / 2 = 21`. `21` is odd.
- **Step 3**: Search for odd divisors from `3` to `√21` (≈ 4.58):
    - Check divisibility by `3`: `21 % 3 = 0` (divisible).
    - Add `3` to the list of factors: `primes = [2, 3]`.
    - Divide `21` by `3`: `21 / 3 = 7`.
    - `7` is greater than `√21`.
- **Step 4**: Add `7` to the list of factors: `primes = [2, 3, 7]`.
- **Result**: The set of prime factors of the number `84` is `[2, 3, 7]`.

**Estimation:**

- **Time complexity**:
    - **Step 2**:
        - In the worst case, the number is divisible by `2` log₂(n) times.
        - Complexity: O(log n).
    - **Step 3**:
        - In the worst case, the algorithm checks all odd numbers from `3` to √n.
        - The number of such values: √n / 2.
        - For each number, a division operation is performed.
        - Complexity: O(√n).
    - **Step 4**:
        - In the worst case, the remainder is a prime number.
        - Complexity: O(1).
    - **Overall time complexity**:
        - O(log n) + O(√n) + O(1) = O(√n).
- **Space complexity**:
    - **List of prime factors**:
        - In the worst case, the number of prime factors is log n.
        - Complexity: O(log n).

**Code:**

```dotty
def primeFactors(n: Long): List[Long] =
  @tailrec
  def reduceLoop(n: Long, i: Long): Long =
    if n % i == 0 then reduceLoop(n / i, i) else n

  @tailrec
  def loop(n: Long, i: Long, acc: List[Long]): List[Long] =
    val limit = math.sqrt(n.toDouble)
    if i > limit then
      if n > 1 then n :: acc else acc
    else if n % i == 0 then loop(reduceLoop(n, i), i + 2, i :: acc)
    else loop(n, i + 2, acc)

  if n % 2 == 0 then loop(reduceLoop(n, 2), 3, 2 :: Nil)
  else loop(n, 3, Nil)
end primeFactors

primeFactors(777111)
// List(7001, 37, 3)
```

## Finding Prime Factors with Multiplicities

> Given a number n. Find the set of prime factors of n and the exponents of their occurrences.

**Algorithm:**

- **Step 1**: Define the list of prime factors as an empty list.
- **Step 2**: If the number is even, divide the number n by 2 as long as the result is even.
  Store the number of divisions: \\((2 \to k\_{2})\\).
- **Step 3**: Let n be the result from the previous step of reducing n.
  From 3 to √n for each found odd divisor of n, repeat the procedure:
    - **Step 3.1**: Divide n by the found odd divisor as long as it is divisible.
      Add the resulting count: \\((2 \to k\_{2}, ..., d \to k\_{d})\\).
      Assign the resulting reduced n back to n.
- **Step 4**: If anything is left after finding odd divisors, the remainder is also a divisor (and it is prime).
  Add it to the list of factors with exponent 1 (because it is greater than the square root of the original number).

**Example:**

Consider the example of the algorithm working with the number `84`.

- **Step 1**: The list of prime factors is empty.
- **Step 2**: Check whether the number is even:
    - `84` is even.
    - Divide `84` by `2`: `84 / 2 = 42`. `42` is even. Divide `42` by `2`: `42 / 2 = 21`. `21` is odd.
    - Store the number of divisions: `(2 -> 2)`.
- **Step 3**: Search for odd divisors from `3` to `√21` (≈ 4.58):
    - Check divisibility by `3`: `21 % 3 = 0` (divisible).
    - Divide `21` by `3`: `21 / 3 = 7`.
    - `7` is greater than `√21`.
    - Store the number of divisions: `(2 -> 2, 3 -> 1)`.
- **Step 4**: Add `7` to the list of factors with exponent `1`: `(2 -> 2, 3 -> 1, 7 -> 1)`.
- **Result**: The prime factors of `84` and their multiplicities are `(2 -> 2, 3 -> 1, 7 -> 1)`.

**Estimation:**

- **Time complexity**:
    - **Step 2**:
        - In the worst case, the number is divisible by `2` log₂(n) times.
        - Complexity: O(log n).
    - **Step 3**:
        - In the worst case, the algorithm checks all odd numbers from `3` to √n.
        - The number of such values: √n / 2.
        - For each number, a division operation is performed.
        - Complexity: O(√n).
    - **Step 4**:
        - In the worst case, the remainder is a prime number.
        - Complexity: O(1).
    - **Overall time complexity**:
        - O(log n) + O(√n) + O(1) = O(√n).
- **Space complexity**:
    - **List of prime factors**:
        - In the worst case, the number of prime factors is log n.
        - Complexity: O(log n).

**Code:**

```dotty
def primeFactorsWithPow(n: Long): Map[Long, Int] =
  @tailrec
  def reduceLoop(n: Long, i: Long, count: Int): (Long, Int) =
    if n % i == 0 then reduceLoop(n / i, i, count + 1) else (n, count)

  @tailrec
  def loop(n: Long, i: Long, acc: Map[Long, Int]): Map[Long, Int] =
    val limit = math.sqrt(n.toDouble)
    if i > limit then
      if n > 1 then acc + (n -> 1) else acc
    else if n % i == 0 then
      val (newN, newCount) = reduceLoop(n, i, 0)
      loop(newN, i + 2, acc + (i -> newCount))
    else loop(n, i + 2, acc)

  if n % 2 == 0 then
    val (newN, newCount) = reduceLoop(n, 2, 0)
    loop(newN, 3, Map(2L -> newCount))
  else loop(n, 3, Map.empty)
end primeFactorsWithPow

primeFactorsWithPow(777111)
// Map(3 -> 1, 37 -> 1, 7001 -> 1)
```

## Sum of Divisors

> Given a number n. Find the sum of divisors of this number.

**Algorithm:**

According to the [divisor functions][divisorfunc], the sum of divisors of a number factored into prime factors with exponents:
\\(n = \prod\_{i=1}^{r}\rho\_{i}^{\alpha\_{i}}\\) can be computed using the formula:

\\(\sigma\_{1}(n) = \prod\_{i=1}^{r}\frac{\rho\_{i}^{\alpha\_{i} + 1} - 1}{\rho\_{i} - 1}\\)

**Example:**

The prime factors of `84` and their multiplicities are `(2 -> 2, 3 -> 1, 7 -> 1)`.

Using the formula above, the sum of divisors of `84` is:

\\(\sigma\_{1}(84) = \frac{2^{2 + 1} - 1}{2 - 1} \times \frac{3^{1 + 1} - 1}{3 - 1} \times \frac{7^{1 + 1} - 1}{7 - 1} = 224\\)

**Estimation:**

The estimation is the same as for the "Finding Prime Factors with Multiplicities" function.

**Code:**

```dotty
def sumOfDivisors(number: Long): Long =
  val primeDivisors = primeFactorsWithPow(number)
  primeDivisors.foldLeft(1L) { case (mul, (prime, power)) =>
    val num = math.pow(prime, power + 1).toLong - 1
    val den = prime - 1
    mul * (num / den)
  }

sumOfDivisors(220) // 504
sumOfDivisors(284) // 504
```

## Count of Divisors

> Given a number n. Find the count of divisors of this number.

**Algorithm:**

According to the [divisor functions][divisorfunc], the count of divisors of a number factored into prime factors with exponents:
\\(n = \prod\_{i=1}^{r}\rho\_{i}^{\alpha\_{i}}\\) can be computed using the formula:

\\(\sigma\_{0}(n) = \prod\_{i=1}^{r}(\alpha\_{i} + 1)\\)

**Example:**

The prime factors of `84` and their multiplicities are `(2 -> 2, 3 -> 1, 7 -> 1)`.

Using the formula above, the count of divisors of `84` is:

\\(\sigma\_{0}(84) = (2 + 1) \times (1 + 1) \times (1 + 1) = 12\\)

**Estimation:**

The estimation is the same as for the "Finding Prime Factors with Multiplicities" function.

**Code:**

```dotty
def countOfDivisors(number: Long): Long =
  primeFactorsWithPow(number).values.foldLeft(1L): (mul, a) =>
    mul * (a + 1)

countOfDivisors(100) // 9
```

## Perfect Number

**Algorithm:**

[A perfect number](https://ru.wikipedia.org/wiki/%D0%A1%D0%BE%D0%B2%D0%B5%D1%80%D1%88%D0%B5%D0%BD%D0%BD%D0%BE%D0%B5_%D1%87%D0%B8%D1%81%D0%BB%D0%BE) —
is a positive integer that is equal to the sum of its positive divisors, excluding the number itself.

[A deficient number](https://ru.wikipedia.org/wiki/%D0%9D%D0%B5%D0%B4%D0%BE%D1%81%D1%82%D0%B0%D1%82%D0%BE%D1%87%D0%BD%D1%8B%D0%B5_%D1%87%D0%B8%D1%81%D0%BB%D0%B0) —
is a positive integer that is less than the sum of its positive divisors, excluding the number itself.

[A abundant number](https://ru.wikipedia.org/wiki/%D0%98%D0%B7%D0%B1%D1%8B%D1%82%D0%BE%D1%87%D0%BD%D1%8B%D0%B5_%D1%87%D0%B8%D1%81%D0%BB%D0%B0) —
is a positive integer that is greater than the sum of its positive divisors, excluding the number itself.

**Code:**

```dotty
enum PerfectNumbersType:
  case Perfect, Deficient, Abundant

def perfectNumbersType(n: Long): PerfectNumbersType =
  val sum = sumOfDivisors(n) - n
  if sum == n then Perfect
  else if sum < n then Deficient
  else Abundant

perfectNumbersType(6)  // Perfect
perfectNumbersType(7)  // Deficient
perfectNumbersType(12) // Abundant
```

## Euler's Totient Function

> [Euler's totient function][euler] of a natural number \\(\varphi(n)\\) —
> is a multiplicative arithmetic function, the value of which equals the count of natural numbers
> less than or equal to n and coprime to it.
>
> Given a number n. Find the value of Euler's totient function for n.

**Algorithm:**

For an arbitrary natural number, the value of \\(\varphi(n)\\):

\\(\varphi(n) = n\prod_{p|n}^{}\frac{p - 1}{p}\\)

- **Step 1**: Set \\(\varphi(n) = n\\).
- **Step 2**: If the number is even, divide the number n by 2 as long as the result is even.
  Update \\(\varphi(n) = \varphi(n) \times \frac{1}{2}\\).
- **Step 3**: Let n be the result from the previous step of reducing n.
  From 3 to √n for each found odd divisor of n, repeat the procedure:
    - **Step 3.1**: Divide n by the found odd divisor as long as it is divisible.
      Update \\(\varphi(n) = \varphi(n) \times \frac{d - 1}{d}\\).
      Assign the resulting reduced n back to n.
- **Step 4**: If anything is left after finding odd divisors, the remainder is also a divisor (and it is prime).
  Update \\(\varphi(n) = \varphi(n) \times \frac{d - 1}{d}\\).

**Example:**

Consider the example of the algorithm working with the number `84`.

- **Step 1**: \\(\varphi(n) = 84\\).
- **Step 2**: Check whether the number is even:
    - `84` is even.
    - Divide `84` by `2`: `84 / 2 = 42`. `42` is even. Divide `42` by `2`: `42 / 2 = 21`. `21` is odd.
    - Update \\(\varphi(n) = 84 \times \frac{1}{2} = 42\\).
- **Step 3**: Search for odd divisors from `3` to `√21` (≈ 4.58):
    - Check divisibility by `3`: `21 % 3 = 0` (divisible).
    - Divide `21` by `3`: `21 / 3 = 7`.
    - `7` is greater than `√21`.
    - Update \\(\varphi(n) = 42 \times \frac{2}{3} = 28\\).
- **Step 4**: Include `7` in the result: \\(\varphi(n) = 28 \times \frac{6}{7} = 24\\).
- **Result**: Euler's totient function of `84` is `24`.

**Estimation:**

The estimation is the same as for the "Finding Prime Factors with Multiplicities" function.

**Code:**

```dotty
def totient(n: Long): Long =
  if n < 1 then 0
  else
    @tailrec
    def reduceLoop(n: Long, i: Long): Long =
      if n % i == 0 then reduceLoop(n / i, i) else n

    @tailrec
    def loop(n: Long, i: Long, acc: Long): Long =
      val limit = math.sqrt(n.toDouble)
      if i > limit then
        if n > 1 then acc * (n - 1) / n else acc
      else if n % i == 0 then
        loop(reduceLoop(n, i), i + 2, acc * (i - 1) / i)
      else loop(n, i + 2, acc)

    if n % 2 == 0 then loop(reduceLoop(n, 2), 3, n / 2)
    else loop(n, 3, n)

totient(5654317604L)
// 2609145504
```

## Array of Euler's Totient Values

> Find an array of Euler's totient values for all numbers from 1 to n.

**Algorithm:**

According to [generalized multiplicativity of Euler's totient function][euler]:
\\(\varphi (m \cdot n) = \varphi (m) \cdot \varphi (n) \cdot {\frac {d}{\varphi (d)}}\\)
, where d is the greatest common divisor of n and m.
Thus, for all primes p:

- \\(\varphi (n \cdot p) = \varphi (n) \cdot p\\), if n is divisible by p
- \\(\varphi (n \cdot p) = \varphi (n) \cdot (p - 1)\\), if n is not divisible by p

Thus, based on the array of prime numbers, we can construct an array of Euler's totient values.

- **Step 1**: Build a list of prime numbers not greater than n using the Sieve of Eratosthenes.
- **Step 2**: For each prime number p, not greater than n, build a set of coefficients k from 1 to n/p.
- **Step 3**: Set \\(\varphi (1) = 1\\).
- **Step 4**: For each coefficient, set the value of Euler's totient function \\(\varphi (k \cdot p) = \varphi (k) * m\\),
  where m equals p if k is divisible by p, and m equals p - 1 otherwise.

**Example:**

Consider the example of the algorithm working with the number `n = 10`.

- **Step 1**: Build a list of prime numbers not greater than `n` using the Sieve of Eratosthenes.
    - List of prime numbers up to `10`: `[2, 3, 5, 7]`.
- **Step 2**: For each prime number p not greater than `n`, build a set of coefficients k from `1` to `n/p`.
    - For `p = 2`: `k = [1, 2, 3, 4, 5]`.
    - For `p = 3`: `k = [1, 2, 3]`.
    - For `p = 5`: `k = [1, 2]`.
    - For `p = 7`: `k = [1]`.
- **Step 3**: Set \\(\varphi (1) = 1\\).
- **Step 4**: For each coefficient, set the value of Euler's totient function \\(\varphi (k \cdot p) = \varphi (k) * m\\), where `m` equals `p` if `k` is divisible by `p`, and `m` equals `p - 1` otherwise.
    - For `p = 2`:
        - `k = 1`: \\(\varphi (2) = \varphi (1 \cdot 2) = \varphi (1) * 1 = 1\\).
        - `k = 2`: \\(\varphi (4) = \varphi (2 \cdot 2) = \varphi (2) * 2 = 1 * 2 = 2\\).
        - `k = 3`: \\(\varphi (6) = \varphi (3 \cdot 2) = \varphi (3) * 1 = 2 * 1 = 2\\).
        - `k = 4`: \\(\varphi (8) = \varphi (4 \cdot 2) = \varphi (4) * 2 = 2 * 2 = 4\\).
        - `k = 5`: \\(\varphi (10) = \varphi (5 \cdot 2) = \varphi (5) * 1 = 4 * 1 = 4\\).
    - For `p = 3`:
        - `k = 1`: \\(\varphi (3) = \varphi (1 \cdot 3) = \varphi (1) * 2 = 1 * 2 = 2\\).
        - `k = 2`: \\(\varphi (6) = \varphi (2 \cdot 3) = \varphi (2) * 2 = 1 * 2 = 2\\).
        - `k = 3`: \\(\varphi (9) = \varphi (3 \cdot 3) = \varphi (3) * 3 = 2 * 3 = 6\\).
    - For `p = 5`:
        - `k = 1`: \\(\varphi (5) = \varphi (1 \cdot 5) = \varphi (1) * 4 = 1 * 4 = 4\\).
        - `k = 2`: \\(\varphi (10) = \varphi (2 \cdot 5) = \varphi (2) * 4 = 1 * 4 = 4\\).
    - For `p = 7`:
        - `k = 1`: \\(\varphi (7) = \varphi (1 \cdot 7) = \varphi (1) * 6 = 1 * 6 = 6\\).
- **Result**: Values of Euler's totient function for numbers from `1` to `10`:
    - \\(\varphi (1) = 1\\)
    - \\(\varphi (2) = 1\\)
    - \\(\varphi (3) = 2\\)
    - \\(\varphi (4) = 2\\)
    - \\(\varphi (5) = 4\\)
    - \\(\varphi (6) = 2\\)
    - \\(\varphi (7) = 6\\)
    - \\(\varphi (8) = 4\\)
    - \\(\varphi (9) = 6\\)
    - \\(\varphi (10) = 4\\)

**Estimation:**

- **Time complexity**:
    - **Step 1**:
        - Building the list of prime numbers up to `n` using the Sieve of Eratosthenes has complexity O(n log log n).
    - **Step 2**:
        - For each prime number `p`, we build a set of coefficients from `1` to `n/p`.
        - The number of prime numbers up to `n` is approximately `n / log n`.
        - Total number of coefficients: \\(\sum\_{p \leq n} \frac{n}{p} \approx n \sum\_{p \leq n} \frac{1}{p} \approx n \log \log n\\).
        - Complexity: O(n log log n).
    - **Step 3**:
        - Complexity: O(1).
    - **Step 4**:
        - For each coefficient, a multiplication operation is performed.
        - Complexity: O(n log log n).
    - **Overall time complexity**:
        - O(n log log n) + O(n log log n) + O(1) + O(n log log n) = O(n log log n).
- **Space complexity**:
    - **List of prime numbers**:
        - In the worst case, the number of prime numbers up to `n` is `n / log n`.
        - Complexity: O(n / log n).
    - **List of coefficients**:
        - In the worst case, the total number of coefficients is `n log log n`.
        - Complexity: O(n log log n).
    - **Euler's totient values**:
        - In the worst case, the number of values is `n`.
        - Complexity: O(n).
    - **Overall space complexity**:
        - O(n / log n) + O(n log log n) + O(n) = O(n log log n).
- **Conclusion**:
    - **Time complexity**: O(n log log n).
    - **Space complexity**: O(n log log n).

**Code:**

```dotty
def primesNoMoreThanN(n: Int): Array[Int] =
  sieveOfEratosthenes(n).zipWithIndex.collect { case (true, prime) => prime }

def totientArray(limit: Int): Array[Long] =
  val phiArray = new Array[Long](limit + 1)
  phiArray(1) = 1

  primesNoMoreThanN(limit).foreach: p =>
    (1 to limit / p).foreach: n =>
      val k = if n % p == 0 then p else p - 1
      phiArray(n * p) = phiArray(n) * k

  phiArray

totientArray(20)
// Array(0, 1, 1, 2, 2, 4, 2, 6, 4, 6, 4, 10, 4, 12, 6, 8, 8, 16, 6, 18, 8)
```

## Radical

> The radical of a number n, \\(rad(n)\\), is the product of the distinct prime divisors of n.
> For example, \\(504 = 2^{3} \times 3^{2} \times 7\\), so \\(rad(504) = 2 \times 3 \times 7 = 42\\).
>
> Given a number n. Compute the radical of n.

**Algorithm:**

The algorithm for finding the radical of n is similar to the "Finding Prime Factors" algorithm:

- **Step 1**: Assign \\(rad(n) \longleftarrow 1\\).
- **Step 2**: If the number is even, divide the number n by 2 as long as the result is even.
  Assign \\(rad(n) \longleftarrow rad(n) \times 2\\).
- **Step 3**: Let n be the result from the previous step of reducing n.
  From 3 to √n for each found odd divisor of n, repeat the procedure:
    - **Step 3.1**: Divide n by the found odd divisor d as long as it is divisible.
      Assign \\(rad(n) \longleftarrow rad(n) \times d\\).
      Assign the resulting reduced n back to n.
- **Step 4**: If anything is left after finding odd divisors, the remainder is also a divisor (and it is prime).
  Assign \\(rad(n) \longleftarrow rad(n) \times d\\).

**Example:**

Consider the example of computing the radical of number n = 84.

- **Step 1**: Assign \\(rad(n) \longleftarrow 1\\).
- **Step 2**: Check whether the number is even:
    - 84 is even. Divide 84 by 2: 84 / 2 = 42. 42 is even. Divide 42 by 2: 42 / 2 = 21. 21 is odd.
    - Assign \\(rad(n) \longleftarrow rad(n) \times 2 = 1 \times 2 = 2\\).
- **Step 3**: Search for odd divisors from 3 to √21 (≈ 4.58):
    - Check divisibility by 3: 21 % 3 = 0 (divisible). Divide 21 by 3: 21 / 3 = 7. 7 is odd.
    - Assign \\(rad(n) \longleftarrow rad(n) \times 3 = 2 \times 3 = 6\\).
- **Step 4**: Check whether there is a remainder after finding odd divisors:
    - 7 is a prime number.
    - Assign \\(rad(n) \longleftarrow rad(n) \times 7 = 6 \times 7 = 42\\).
- **Result**: The radical of 84 is 42.

**Estimation:**

The estimation is the same as for the "Finding Prime Factors" algorithm.

**Code:**

```dotty
def rad(n: Long): Long =
  @tailrec
  def reduceLoop(n: Long, i: Long): Long =
    if n % i == 0 then reduceLoop(n / i, i) else n

  @tailrec
  def loop(n: Long, i: Long, acc: Long): Long =
    val limit = math.sqrt(n.toDouble)
    if i > limit then
      if n > 1 then n * acc else acc
    else if n % i == 0 then loop(reduceLoop(n, i), i + 2, i * acc)
    else loop(n, i + 2, acc)

  if n % 2 == 0 then loop(reduceLoop(n, 2), 3, 2)
  else loop(n, 3, 1)

rad(100) // 10
rad(7)   // 7
```

## Array of Radians

> The radical of a number n, \\(rad(n)\\), is the product of the distinct prime divisors of n.
> For example, \\(504 = 2^{3} \times 3^{2} \times 7\\), so \\(rad(504) = 2 \times 3 \times 7 = 42\\).
>
> Given a number n. Compute an array of radicals of all numbers from 1 to n.

**Algorithm:**

- **Step 1**: Build a list of prime numbers not greater than n using the Sieve of Eratosthenes.- **Step 2**: For each prime number p not greater than n, build a set of coefficients i from 1 to n/p.
- **Step 3**: For each pair `(p, i)`, set the radical of the number p * i to p, if it hasn't been set yet.
  Or, if the radical of the number p * i has already been set, set it to the current value multiplied by p.

**Example:**

Consider the example of the algorithm working with the number `n = 10`.

- **Step 1**: Build a list of prime numbers not greater than `n` using the Sieve of Eratosthenes.
    - List of prime numbers up to `10`: `[2, 3, 5, 7]`.
- **Step 2**: For each prime number p not greater than `n`, build a set of coefficients `i` from `1` to `n/p`.
    - For `p = 2`: `i = [1, 2, 3, 4, 5]`.
    - For `p = 3`: `i = [1, 2, 3]`.
    - For `p = 5`: `i = [1, 2]`.
    - For `p = 7`: `i = [1]`.
- **Step 3**: For each pair `(p, i)`, set the radical of the number `p * i` to `p`, if it hasn't been set yet. Or, if the radical of the number `p * i` has already been set, set it to the current value multiplied by `p`.
    - For `p = 2`:
        - `i = 1`: \\(rad(2) = rad(2 \cdot 1) = 2\\).
        - `i = 2`: \\(rad(4) = rad(2 \cdot 2) = 2\\).
        - `i = 3`: \\(rad(6) = rad(2 \cdot 3) = 2\\).
        - `i = 4`: \\(rad(8) = rad(2 \cdot 4) = 2\\).
        - `i = 5`: \\(rad(10) = rad(2 \cdot 5) = 2\\).
    - For `p = 3`:
        - `i = 1`: \\(rad(3) = rad(3 \cdot 1) = 3\\).
        - `i = 2`: \\(rad(6) = rad(3 \cdot 2) = 3 \cdot 2 = 6\\).
        - `i = 3`: \\(rad(9) = rad(3 \cdot 3) = 3\\).
    - For `p = 5`:
        - `i = 1`: \\(rad(5) = rad(5 \cdot 1) = 5\\).
        - `i = 2`: \\(rad(10) = rad(5 \cdot 2) = 5 \cdot 2 = 10\\).
    - For `p = 7`:
        - `i = 1`: \\(rad(7 \cdot 1) = rad(7) = 7\\).
- **Result**: Radical values for numbers from `1` to `10`:
    - \\(rad(1) = 1\\)
    - \\(rad(2) = 2\\)
    - \\(rad(3) = 3\\)
    - \\(rad(4) = 2\\)
    - \\(rad(5) = 5\\)
    - \\(rad(6) = 6\\)
    - \\(rad(7) = 7\\)
    - \\(rad(8) = 2\\)
    - \\(rad(9) = 3\\)
    - \\(rad(10) = 10\\)

**Explanation:**

For all prime numbers not greater than the given limit, the radical of a number that is a multiple of this prime number has this prime number as a factor.
For other numbers — it does not.

**Estimation:**

- **Time complexity**:
    - **Step 1**:
        - Building the list of prime numbers up to `n` using the Sieve of Eratosthenes has complexity O(n log log n).
    - **Step 2**:
        - For each prime number `p`, we build a set of coefficients from `1` to `n/p`.
        - The number of prime numbers up to `n` is approximately `n / log n`.
        - Total number of coefficients: \\(\sum\_{p \leq n} \frac{n}{p} \approx n \sum\_{p \leq n} \frac{1}{p} \approx n \log \log n\\).
        - Complexity: O(n log log n).
    - **Step 3**:
        - For each pair `(p, i)`, a multiplication operation is performed.
        - Complexity: O(n log log n).
    - **Overall time complexity**:
        - O(n log log n) + O(n log log n) + O(n log log n) = O(n log log n).
- **Space complexity**:
    - **List of prime numbers**:
        - In the worst case, the number of prime numbers up to `n` is `n / log n`.
        - Complexity: O(n / log n).
    - **List of coefficients**:
        - In the worst case, the total number of coefficients is `n log log n`.
        - Complexity: O(n log log n).
    - **Radical values**:
        - In the worst case, the number of values is `n`.
        - Complexity: O(n).
- **Overall space complexity**:
    - O(n / log n) + O(n log log n) + O(n) = O(n log log n).
- **Conclusion**:
    - **Time complexity**: O(n log log n).
    - **Space complexity**: O(n log log n).

**Code:**

```scala
def allRads(limit: Int): Array[Int] =
  val rads = new Array[Int](limit + 1)
  rads(1) = 1
  primesNoMoreThanN(limit).foreach: p =>
    (1 to limit / p).foreach: i =>
      rads(i * p) = if rads(i * p) == 0 then p else rads(i * p) * p
  rads

allRads(10)
// Array(0, 1, 2, 3, 2, 5, 6, 7, 2, 3, 10)
```

## Square-Free Number

> A [square-free number](https://ru.wikipedia.org/wiki/%D0%A1%D0%B2%D0%BE%D0%B1%D0%BE%D0%B4%D0%BD%D0%BE%D0%B5_%D0%BE%D1%82_%D0%BA%D0%B2%D0%B0%D0%B4%D1%80%D0%B0%D1%82%D0%BE%D0%B2_%D1%87%D0%B8%D1%81%D0%BB%D0%BE) —
> is a number that is not divisible by any square other than 1.
>
> Given a number `n`. Determine whether it is square-free.

**Algorithm:**

- **Step 1**: Take the first prime number 2. If it is greater than the square root of n, then the number is square-free.
  If not, proceed to the next step.
- **Step 2**: Check whether the number n is divisible by the square of the prime number p from the previous step. If it is, the number is **not** square-free.
  If it is not, proceed to the next step.
- **Step 3**: Take the next prime number and go back to **Step 1**.

**Example:**

Consider the example of the algorithm working with the number `n = 30`.

- **Step 1**: Take the first prime number `2`. `2` is not greater than √30 (≈ 5.48). Proceed to the next step.
- **Step 2**: Check whether `30` is divisible by the square of prime number `2` (i.e., by `4`). `30 % 4 ≠ 0` (not divisible). Proceed to the next step.
- **Step 3**: Take the next prime number `3` and go back to **Step 1**.
- **Step 1**: Take the prime number `3`. `3` is not greater than √30 (≈ 5.48). Proceed to the next step.
- **Step 2**: Check whether `30` is divisible by the square of prime number `3` (i.e., by `9`). `30 % 9 ≠ 0` (not divisible). Proceed to the next step.
- **Step 3**: Take the next prime number `5` and go back to **Step 1**.
- **Step 1**: Take the prime number `5`. `5` is not greater than √30 (≈ 5.48). Proceed to the next step.
- **Step 2**: Check whether `30` is divisible by the square of prime number `5` (i.e., by `25`). `30 % 25 ≠ 0` (not divisible). Proceed to the next step.
- **Step 3**: Take the next prime number `7` and go back to **Step 1**.
- **Step 1**: Take the prime number `7`. `7` is greater than √30 (≈ 5.48). The number `30` is square-free.
- **Result**: The number `30` is square-free.

**Estimation:**

- **Time complexity**:
    - **Step 1**:
        - Checking whether the prime number `p` is greater than the square root of `n` takes O(1).
    - **Step 2**:
        - Checking whether `n` is divisible by the square of prime number `p` takes O(1).
    - **Step 3**:
        - Finding the next prime number takes O(n * √n) = O(n^(3/2)),
          if the "Finding the Next Prime Number" algorithm is used.
    - **Overall time complexity**:
        - In the worst case, the algorithm checks all prime numbers up to √n.
        - The number of prime numbers up to √n is approximately √n / log(√n) = 2√n / log n.
        - Complexity: O(n^(3/2)) + O(√n / log n) = O(n^(3/2)).
- **Space complexity**:
    - **List of prime numbers**:
        - In the worst case, the number of prime numbers up to √n is √n / log n.
        - Complexity: O(√n / log n).
- **Conclusion**:
    - **Time complexity**: O(n^(3/2)).
    - **Space complexity**: O(√n / log n).

**Code:**

```scala
def isSquareFree(n: Long): Boolean =
  val s      = math.sqrt(n.toDouble).toLong

  @tailrec
  def loop(p: Long): Boolean =
    if p > s then true
    else if n % (p * p) == 0 then false
    else
      nextPrime(p) match
        case Some(nextP) => loop(nextP)
        case _           => true

  loop(2L)

isSquareFree(10)  // true:  10 = 2 * 5
isSquareFree(20)  // false: 20 = 2^2 * 5
isSquareFree(100) // false: 100 = 2^2 * 5^2
```

---

**Links:**

- [Source code][sample]
- [Sieve of Eratosthenes][sieve]
- [Rod Stephens — Algorithms: Theory and Practice. Chapter 2. Numerical Algorithms](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854/)
- [Bhim P. Upadhyaya — Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Project Euler, Problem 7](https://projecteuler.net/problem=7)
- [SICP: Abelson H., Sussman G. — Structure and Interpretation of Computer Programs][sicp]
- [Scalacaster](https://github.com/vkostyukov/scalacaster)

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf
[sieve]: https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes
[euler]: https://en.wikipedia.org/wiki/Euler%27s_totient_function
[sample]: https://gitverse.ru/scalabook/algorithms
