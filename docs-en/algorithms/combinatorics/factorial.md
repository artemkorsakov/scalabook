# Factorial

_The n-factorial_ is a quantity expressed as: \\(n! = 1 \cdot 2 \cdot ... \cdot n = \prod\_{k=1}^{n}k \\).

For example, 5-factorial equals 5! = 120.

Here \\(0! = 1\\) — given this convention, it is clear that
the main identity \\(n! = (n − 1)!n\\) holds for all positive integers n.

## Computing factorial

> Given a number n. Compute n!.

**Algorithm:**

- **Step 1**. Assign `acc` the value `1`. Assign `i` the value `2`.
- **Step 2**. If `i` is not greater than `n`, proceed to Step 3. Otherwise, output `acc`.
- **Step 3**. Assign `acc` the value `acc * i`, and increment `i` by `1`. Go back to Step 2.

**Example:**

Example computation for `n = 5`:

- Initial accumulator value: `1`.
- Sequential multiplication: `1 * 2 = 2`, `2 * 3 = 6`, `6 * 4 = 24`, `24 * 5 = 120`.
- **Result**: The factorial of 5 is 120.

**Estimation:**

- **Time** - O(n)
- **Memory** - O(1)

**Code:**

```dotty
def factorial(n: Int): Long =
  (2 to n).foldLeft(1L)(_ * _)

factorial(8)
// 40320
```

## Approximate value using Stirling's formula

**Algorithm:**

Stirling's formula allows computing an approximate value of factorial:
\\(n!\approx \sqrt{2\pi n}\left ( \frac{n}{e} \right )^{n}\\)

> The relative error of calculations using this formula is approximately \\(\frac{1}{12n}\\).

**Example:**

Example of computing factorial using Stirling's formula for \\(n = 10\\):

- Step 1: Compute \\(\sqrt{2 \pi n}\\):
    - \\(\sqrt{2 \pi \cdot 10} = \sqrt{2 \cdot 3.14159 \cdot 10} = \sqrt{62.8318} \approx 7.926\\)
- Step 2: Compute \\(\left( \frac{n}{e} \right)^n\\):
    - \\(\left( \frac{10}{2.71828} \right)^{10} = \left( 3.6788 \right)^{10} \approx 453999.29\\)
- Step 3: Multiply the results of steps 1 and 2:
    - \\(7.926 \cdot 453999.29 \approx 3628800\\)
- Result: The approximate value of the factorial of \\(10\\) using Stirling's formula is \\(3628800\\).
- Exact value: \\(10! = 3628800\\).

**Estimation:**

- **Time** - O(n)
- **Memory** - O(1)

**Code:**

```dotty
def approximateFactorial(n: Int): BigInt =
  (BigDecimal(math.sqrt(2 * math.Pi * n)) *
    BigDecimal(n / math.E).pow(n)).toBigInt

approximateFactorial(8)
// BigInt(39902)
```

### More precise approximation

**Algorithm:**

A more precise formula:

\\(n! = \sqrt{2\pi n}\left ( \frac{n}{e} \right )^{n}(1 + \frac{1}{12n} + \frac{1}{288n^{2}} - \frac{139}{51840n^{3}} - \frac{571}{2488320n^{4}} + O(\frac{1}{n^{5}}))\\)

**Estimation:**

- **Time** - O(n)
- **Memory** - O(1)

**Code:**

```dotty
def approximateFactorial2(n: Int): BigInt =
  (BigDecimal(math.sqrt(2 * math.Pi * n)) *
    BigDecimal(n / math.E).pow(n) *
    (1 + 1 / BigDecimal(12.0 * n) + 1 / BigDecimal(288.0 * n * n) -
      139 / BigDecimal(51840.0 * n * n * n) -
      571 / BigDecimal(2488320.0 * n * n * n * n))).toBigInt

approximateFactorial2(8)
// BigInt(40319)
```

## Prime factorization of factorial

**Algorithm:**

A prime number \\(p\\) is a divisor of \\(n!\\) with multiplicity
\\(\mu = \left \lfloor \frac{n}{p} \right \rfloor + \left \lfloor \frac{n}{p^{2}} \right \rfloor + \left \lfloor \frac{n}{p^{3}} \right \rfloor + ... = \sum\_{k > 0}^{}\left \lfloor \frac{n}{p^{k}} \right \rfloor\\).

This formula allows decomposing factorial into prime factors.

**Example:**

Consider the example of computing the multiplicity of the prime number \\(p = 3\\) in factorial \\(n = 10\\).

Formula:

\\(\mu = \left \lfloor \frac{n}{p} \right \rfloor + \left \lfloor \frac{n}{p^{2}} \right \rfloor + \left \lfloor \frac{n}{p^{3}} \right \rfloor + ... = \sum\_{k > 0}^{}\left \lfloor \frac{n}{p^{k}} \right \rfloor \\)

- Step 1: Compute \\(\left \lfloor \frac{n}{p} \right \rfloor \\): \\(\left \lfloor \frac{10}{3} \right \rfloor = \left \lfloor 3.333 \right \rfloor = 3\\)
- Step 2: Compute \\(\left \lfloor \frac{n}{p^{2}} \right \rfloor \\): \\(\left \lfloor \frac{10}{3^{2}} \right \rfloor = \left \lfloor \frac{10}{9} \right \rfloor = \left \lfloor 1.111 \right \rfloor = 1\\)
- Step 3: Compute \\(\left \lfloor \frac{n}{p^{3}} \right \rfloor\\): \\(\left \lfloor \frac{10}{3^{3}} \right \rfloor = \left \lfloor \frac{10}{27} \right \rfloor = \left \lfloor 0.370 \right \rfloor = 0\\)
- Step 4: Sum all obtained values: \\(\mu = 3 + 1 + 0 = 4\\)
- Result: The prime number \\(3\\) is a divisor of \\(10!\\) with multiplicity \\(4\\).

**Estimation:**

- **Time** - O(log(p))
- **Memory** - O(1)

**Code:**

```dotty
def primeFactor(n: Int, p: Int): Int =
  @tailrec
  def loop(pj: Int, acc: Int): Int =
    if pj == 0 then acc
    else
      val next = pj / p
      loop(next, acc + next)
  loop(n, 0)

primeFactor(1000, 3)
// 498
```

---

**Links:**

- [Source code][sample]
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.5](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.11.2](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Combinatorial Algorithms in Scala - kostyukov.net][kostyukov]

[kostyukov]: https://kostyukov.net/posts/combinatorial-algorithms-in-scala/
[sample]: https://gitverse.ru/scalabook/algorithms
