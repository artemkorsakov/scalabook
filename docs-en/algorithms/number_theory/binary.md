# Binary Numbers and Other Number Systems

## Power of Two

> Given a number `x`. Determine whether `x` is a power of two.

**Algorithm:**

Bitwise AND of `x` and `(x - 1)` should return `0`.

**Explanation** and **Example:**

Bitwise AND between two numbers returns 1 only in positions where both numbers have 1 in the corresponding bits.
Consider various examples.

a) Power of two

In general, if \\(x = 2^n\\), then in binary representation x looks like \\(1000...0\\) (one bit 1 and n zeros).
When we subtract 1 from a power of two, we get a number
in which all bits to the right (less significant) of the set bit become 1, and the set bit itself becomes 0.
Now consider the bitwise AND between `x` and `(x - 1)`:
For \\(x = 2^n\\):

- \\(x = 1000...0\_2\\)
- \\(x - 1 = 0111...1\_2\\)
- \\(x \& (x - 1) = 1000...0\_2 \& 0111...1\_2 = 0000...0\_2\\)

The result is 0.

b) Other numbers

Now consider what happens if x is not a power of two:

If x has more than one bit set to 1, then subtracting 1 will not give all zeros in the result of the bitwise AND. For example:
Let x = 6 (in binary: \\(0110\_2\\)):

- \\(x - 1 = 5\\) (in binary: \\(0101\_2\\))
- \\(x \& (x - 1) = 0110\_2 \& 0101\_2 = 0100\_2\\) (result is not 0).

**Estimation:**

- **Time** - O(1)
- **Memory** - O(1)

**Code:**

```dotty
def isPowerOfTwo(x: Int): Boolean = (x & (x - 1)) == 0

isPowerOfTwo(16) // true
```

## Counting Bit Set Bits

> Given two numbers, `x` and `y`. Determine whether they contain the same number of ones in their bit representation.

**Algorithm:**

- **Step 1** (Check for zero): If one of the numbers is zero,
  then `x` and `y` have the same number of ones in their bit representation only if both are 0.
- **Step 2** (Check even and odd): If one of the numbers is even
  and the other is odd, then divide the even number by 2 and go back to **Step 1**.
- **Step 3** (Recursive call): If both are even or both are odd,
  then divide both numbers by 2 and go back to **Step 1**.

**Example:**

Consider the calculation example for the pair (6, 3):

- x = 6 (even), y = 3 (odd) → go back to the beginning of the algorithm with the pair (3, 3).
- x = 3 (odd), y = 3 (odd) → go back with (1, 1).
- x = 1 (odd), y = 1 (odd) → go back with (0, 0).
- x = 0, y = 0 → return `true`.

Thus, for the pair (6, 3) the algorithm will return `true`.

**Explanation:**

- **Step 1** (Check for zero): If both numbers are zero, then neither contains ones in their bit representation.
  If only one of the numbers is zero (has no ones in its bit representation),
  then the other contains at least one bit set to 1.
  This means that the number of ones in the bit representation is not the same.
- **Step 2** (Check even and odd): If the numbers have different parity, then dividing the even number by 2
  does not change the number of ones in its bit representation (a trailing zero is removed).
  We can continue checking for the odd number and half of the even number.
- **Step 3** If the numbers have the same parity, they end with the same digit (0 or 1) in their bit representation.
  This means that when divided by 2, the number of ones in their bit representation either remains the same or decreases by 1
  for both numbers. This means that the answer to the question of whether they contain the same number of ones in their bit representation
  is equivalent to the question of whether they contain the same number of ones in their bit representation after removing the last digit.

**Estimation:**

- **Time** - O(log(n))
- **Memory** - O(1)

**Code:**

```dotty
@annotation.tailrec
def isSnoob(x: Int, y: Int): Boolean =
  if x == 0 && y == 0 then true
  else if x == 0 || y == 0 then false
  else if x % 2 == 1 && y % 2 == 0 then isSnoob(x, y >> 1)
  else if x % 2 == 0 && y % 2 == 1 then isSnoob(x >> 1, y)
  else isSnoob(x >> 1, y >> 1)

isSnoob(11, 13)
// true: "1101", "1011"
```

## Converting Decimal to Binary

> Given a number in decimal notation. Convert this number to binary.

**Algorithm:**

Conversion algorithm:

- **Step 1**. Starting from the given number, create a sequence of numbers such that
  each subsequent number is half of the previous one.
  Continue until the last element satisfies the condition 2 > x > 0,
  where x is the last number in the sequence.
  Formally S = {\\(x\_{1}\\), \\(x\_{2}\\),..., \\(x\_{n}\\)},
  where \\(x\_{2}\\) = \\(\frac{x\_{1}}{2}\\), \\(x\_{3}\\) = \\(\frac{x\_{2}}{2}\\), etc., and 2 > \\(x\_{n}\\) > 0.
- **Step 2**. For each number in the above list, divide by 2 and store the remainder in a container.
- **Step 3**. The container now contains the binary equivalent bits in reverse order, \\(b\_{1}b\_{2}...b\_{n}\\),
  so reverse the order to get the binary equivalent number, \\(b\_{n}...b\_{2}b\_{1}\\).

**Example:**

Consider the example of computation using the given algorithm, starting with the integer 13.

- **Step 1**. Create a sequence by dividing the number by 2 until we obtain a value satisfying the condition (2 > x > 0).

- \\(x\_1 = 13\\)
- \\(x\_2 = \frac{13}{2} = 6.5\\) (rounded to integer: 6)
- \\(x\_3 = \frac{6}{2} = 3\\)
- \\(x\_4 = \frac{3}{2} = 1.5\\) (rounded to integer: 1)
- \\(x\_5 = \frac{1}{2} = 0.5\\) (rounded to integer: 0)
- The sequence will be: (S = {13, 6, 3, 1}). \\(x\_5 = 0\\) does not satisfy the condition (2 > 0 > 0), so we stop at \\(x\_4 = 1\\).

- **Step 2**. Now for each number in the sequence, divide by 2 and store the remainder.

- For \\((x\_1 = 13): (13 \mod 2 = 1)\\) (remainder 1)
- For \\((x\_2 = 6): (6 \mod 2 = 0)\\) (remainder 0)
- For \\((x\_3 = 3): (3 \mod 2 = 1)\\) (remainder 1)
- For \\((x\_4 = 1): (1 \mod 2 = 1)\\) (remainder 1)
- Remainders in the container: {1, 0, 1, 1}.

- **Step 3**. In reverse order: (1101).

Thus, the binary equivalent number for the integer 13 using the given algorithm is (1101).

**Explanation:**

For any number the following holds: \\(a = \left\lfloor \frac{a}{2} \right\rfloor \times 2 + (a \mod 2)\\).
Therefore, the binary representation of a number equals the binary representation of half the number with the remainder of dividing the number by 2 appended.

**Estimation:**

- **Time** - O(log(n))
- **Memory** - O(log(n))

**Code:**

```dotty
def decToBinConv(x: Int): String =
  val seqOfDivByTwo = Iterator.iterate(x)(a => a / 2)
  val binList = seqOfDivByTwo
    .takeWhile(a => a > 0)
    .map(a => a % 2)
  binList.mkString.reverse

decToBinConv(8) // "1000"
decToBinConv(7) // "111"
```

## Palindrome

[A palindrome](https://ru.wikipedia.org/wiki/%D0%9F%D0%B0%D0%BB%D0%B8%D0%BD%D0%B4%D1%80%D0%BE%D0%BC) is
a number, word, or text that reads the same forwards and backwards.

> Given a number x. Determine whether this number is a palindrome in a given number system.

**Algorithm:**

- **Step 1** (Base condition: zero). If the number is zero, then it is a palindrome. Otherwise, proceed to the next step.
- **Step 2** (Base condition: divisibility by the base). If the number is divisible by the number system base,
  then it is not a palindrome. Otherwise, proceed to the next step.
- **Step 3** (Compare the number with its reversed version). Assign \\(k \leftarrow number, r \leftarrow 0\\).
  If \\(k \le r\\), then the number is a palindrome if and only if \\(k = r\\) or \\(k = \frac{r}{base}\\). Otherwise, the number is not a palindrome.
  If \\(k > r\\), then proceed to the next step.
- **Step 4** (Process the number). Assign \\(k \leftarrow \frac{k}{base}, r \leftarrow r * base + k \% base\\).

**Example:**

Given the number 12321.

- **Step 1**: 12321 ≠ 0, proceed to the next step.
- **Step 2**: 12321 % 10 ≠ 0, proceed to the next step.
- **Step 3**: Assign: \\(k \leftarrow 12321, r \leftarrow 0\\). Since \\(k > r\\), proceed to Step 4.
- **Step 4**:
    - Process the number.
        - \\(k \leftarrow \frac{12321}{10} = 1232\\)
        - \\(r \leftarrow 0 * 10 + 12321 \% 10 = 1\\)
    - Repeat Step 3: \\(k \leftarrow 1232\\), \\(r \leftarrow 1\\). Since \\(k > r\\), proceed to Step 4.
    - Process again:
        - \\(k \leftarrow \frac{1232}{10} = 123\\)
        - \\(r \leftarrow 1 * 10 + 1232 \% 10 = 12\\)
    - Repeat Step 3: \\(k \leftarrow 123\\), \\(r \leftarrow 12\\). Since \\(k > r\\), proceed to Step 4.
    - Repeat:
        - \\(k \leftarrow \frac{123}{10} = 12\\)
        - \\(r \leftarrow 12 * 10 + 123 \% 10 = 123\\)
    - Now \\(k \leq r\\) and \\(k = \frac{r}{base}\\). Therefore, 12321 is a palindrome.

**Explanation:**

- **Step 1**: 0 is a palindrome in any number system.
- **Step 2**: If a number is greater than 0 and divisible by the base of the number system,
  then such a number cannot be a palindrome because it starts with 1 and ends with 0.
- **Step 3**: The variable k stores the first half of the number, and the variable r stores the reversed second half.
  If the number is a palindrome, then the first and second halves should be equal if the number contains an even number of digits.
  If the number contains an odd number of digits, then the first half should equal the second half without the last digit.
- **Step 4**: The computation of k is done by sequentially dividing k by the base to remove the last digit,
  and r is computed by multiplying r by the base and adding the remainder of k (appending the last digit of k to the end of r).

**Estimation:**

- **Time** - O(log(n))
- **Memory** - O(1)

**Code:**

```dotty
def isPalindrome(number: Long): Boolean = isPalindrome(number, 10)

def isPalindrome(number: Long, base: Int): Boolean =
  number == 0 || number > 0 && number % base != 0 && {
    def loop(k: Long, r: Long): Boolean =
      if k <= r then k == r || k == r / base
      else loop(k / base, r * base + k % base)
    loop(number, 0L)
  }

isPalindrome(12321) // true
```

---

**Links:**

- [Source code][sample]
- [Algorithmica - Bitwise representation of numbers](https://ru.algorithmica.org/cs/arithmetic/bit-representation/)
- [Rod Stephens - Algorithms: Theory and Practice. Chapter 2. Numerical Algorithms](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854/)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [SICP: Abelson H., Sussman G. - Structure and Interpretation of Computer Programs][sicp]
- [Scalacaster](https://github.com/vkostyukov/scalacaster)
- [The Art of Computer Programming - Donald E. Knuth](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)

[sicp]: https://web.mit.edu/6.001/6.037/sicp.pdf
[sample]: https://gitverse.ru/scalabook/algorithms
