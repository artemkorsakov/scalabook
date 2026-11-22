# Number Theory

## Introduction

Algorithms based on number theory represent an important category of algorithms
that are used to solve various problems related to integers.
These algorithms find applications in cryptography, number theory, combinatorics, and other fields.
Here are several key algorithms based on number theory:

## Examples of Algorithms

### Euclidean Algorithm

The Euclidean algorithm is used to find the greatest common divisor (GCD) of two integers.
It is based on the properties of divisibility and works on the principle of recursive division.

```dotty
@annotation.tailrec
final def gcd(a: Long, b: Long): Long =
  if b == 0 then a
  else gcd(b, a % b)

gcd(4851, 3003)
// 231  
```

### Extended Euclidean Algorithm

This algorithm not only finds the GCD of two numbers,
but also allows expressing it as a linear combination of those numbers \\(x \times a + y \times b = d\\).
This is useful for solving Diophantine equations.

```dotty
def generalizedEuclideanAlgorithm(m: Long,n: Long): (Long, Long, Long) =
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

### Factorization Algorithm

Factorization is the process of decomposing a number into a product of prime factors.
There are several algorithms for factorization, including:

- **Trial division algorithm**: Checks divisibility of a number by all prime numbers up to its square root.
- **Fermat's algorithm**: Uses the difference of squares to find divisors.
- **Quadratic sieve algorithm**: A more complex and efficient method for factorizing large numbers.

### Miller-Rabin Algorithm

[Miller-Rabin algorithm][miller] is a probabilistic primality test
that determines whether a number is prime.
It is based on the properties of numbers in modular arithmetic and is used in cryptography.

### Chinese Remainder Theorem Algorithm

The [Chinese Remainder Theorem][china] allows solving systems of linear congruences with different moduli.
The algorithm is used to find a unique solution within the product of the moduli.

---

**References:**

- [Source code][sample]
- [Euclidean Algorithm](https://ru.wikipedia.org/wiki/%D0%90%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC_%D0%95%D0%B2%D0%BA%D0%BB%D0%B8%D0%B4%D0%B0)
- [Miller-Rabin Algorithm][miller]
- [Chinese Remainder Theorem][china]
- [Divisor function][divisorfunc]

[china]: https://en.wikipedia.org/wiki/Chinese_remainder_theorem
[divisorfunc]: https://en.wikipedia.org/wiki/Divisor_function
[miller]: https://en.wikipedia.org/wiki/Miller%E2%80%93Rabin_primality_test
[sample]: https://gitverse.ru/scalabook/algorithms
