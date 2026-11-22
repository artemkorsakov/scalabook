# Combination

_Combinations of `n` objects taken `k` at a time_ are the possible ways to choose `k` distinct elements from `n` objects
without regard to the order of their placement.

Here is an example of combinations of five objects _{a, b, c, d, e}_ taken three at a time:
_abc, abd, abe, acd, ace, ade, bcd, bce, bde, cde_.

## Computing

To count the total number of combinations of _n_ objects taken _k_ at a time:
there are _n(n − 1)...(n − k + 1)_ ways to choose the first _k_ objects in a permutation,
and each combination of _k_ elements occurs exactly _k!_ times,
since for any set of _k_ objects there are _k!_ permutations.
Therefore, for the number of combinations of _n_ elements taken _k_ at a time, which we denote as \\(\binom{n}{k}\\),
we get the following formula:

**Formula 1**: \\(\binom{n}{k} = \frac{n(n - 1)...(n - k + 1)}{k(k - 1)...(1)}\\)

The values \\(\binom{n}{k}\\) (read as "the number of combinations of n taken k") are called _binomial coefficients_.

**Algorithm:**

- If `k` is less than `0` or greater than `n`, then the number of combinations is `0`.
- If `k` is greater than half of `n`, use the _Symmetry Property (Formula 3 below)_
  to reduce the number of operations.
- Set variables `num` (numerator) and `den` (denominator) to `1`.
- Iterate over each `i` from `0` to `k`:
    - Multiply `num` by `n - i`, and `den` by `k - i`.
    - Proceed to the next iteration.
- The number of combinations equals `num/den`.

**Example:**

Consider the example of computing the number of combinations \\(\binom{n}{k}\\).

Suppose that n = 5 and k = 2.

- Check conditions:
    - k is not less than 0 and not greater than n (i.e., \\(0 \leq 2 \leq 5\\)).
    - k is not greater than half of n (i.e., \\(2 \leq \frac{5}{2}\\) — so we do not use the symmetry property).
- Initialize variables:
    - Set num = 1 and den = 1.
    - Iterate from 0 to k:
    - For i = 0:
        - \\(num = num \times (n - i) = 1 \times (5 - 0) = 5\\)
        - \\(den = den \times (k - i) = 1 \times (2 - 0) = 2\\)
    - For i = 1:
        - \\(num = num \times (n - i) = 5 \times (5 - 1) = 5 \times 4 = 20\\)
        - \\(den = den \times (k - i) = 2 \times (2 - 1) = 2 \times 1 = 2\\)
- Number of combinations:
    - Now that we have reached k, we can compute the number of combinations: \\(\binom{5}{2} = \frac{num}{den} = \frac{20}{2} = 10\\)

Thus, the number of combinations \\(\binom{5}{2} = 10\\).

**Estimation:**

- **Time** - O(k)
- **Memory** - O(1)

**Code:**

```dotty
@tailrec
def binomialCoefficient(n: Int, k: Int): BigInt =
  if k < 0 || k > n then BigInt(0)
  else if k > (n / 2) then binomialCoefficient(n, n - k)
  else
    val (num, den) = (0 until k).foldLeft((BigInt(1), BigInt(1))) {
      case ((num, den), i) =>
        (num * BigInt(n - i), den * BigInt(k - i))
    }
    num / den

binomialCoefficient(100, 32)
// 143012501349174257560226775
```

## Some properties

#### A. Factorial representation.

**Formula 2**: \\(\binom{n}{k} = \frac{n!}{k!(n − k)!}\\), where n and k are integers, \\(n \geq k \geq 0\\).

This formula allows representing combinations of factorials as binomial coefficients, and vice versa.

#### B. Symmetry property.

**Formula 3**: \\(\binom{n}{k} = \binom{n}{n - k}\\), where n and k are integers, \\(n \geq k \geq 0\\).

This formula holds for all integers k.
If k is negative or greater than n, then the binomial coefficient is zero
(provided that n is a non-negative integer).

#### C. Bring in/take out.

**Formula 4**: \\(\binom{r}{k} = \frac{r}{k}\binom{r - 1}{k - 1}\\), where \\(k \neq 0\\).

This formula is very useful for combining a binomial coefficient with other parts of an expression.

A consequence of the above formula is another relationship:

**Formula 5**: \\(\binom{r}{k} = \frac{r}{r - k}\binom{r - 1}{k}\\)

#### D. Addition formula.

**Formula 6**: \\(\binom{r}{k} = \binom{r - 1}{k} + \binom{r - 1}{k - 1}\\), for integers k,

> Each value equals the sum of two values from the previous row,
> one in the same column and the other in the nearest column to the left.

The formula can be proved, for example, from formulas **4** and **5**:

\\(r\binom{r - 1}{k} + r\binom{r - 1}{k - 1} = (r - k)\binom{r}{k} + k\binom{r}{k} = r\binom{r}{k}\\)

The addition formula is often used in proofs by induction on r, where r is an integer.

#### E. Summation formulas.

**Formula 7**: \\(\sum_{k=0}^{n}\binom{r+k}{k} = \binom{r}{0} + \binom{r + 1}{1} + ... + \binom{r + n}{n} = \binom{r + n + 1}{n}\\), where \\(n \geq 0\\).

**Formula 8**: \\(\sum_{k=0}^{n}\binom{k}{m} = \binom{0}{m} + \binom{1}{m} + ... + \binom{n}{m} = \binom{n + 1}{m + 1}\\), where \\(m \geq 0\\) and \\(n \geq 0\\).

#### F. Binomial theorem.

**Formula 9**: \\((x + y)^{r} = \sum_{k}^{}\binom{r}{k}x^{k}y^{r-k}\\), where \\(r \geq 0\\).

A special case of the formula for y = 0:

**Formula 10**: \\(\sum_{k}^{}\binom{r}{k}x^{k} = (1 + x)^{r}\\), where \\(r \geq 0\\) or \\(|x| < 1\\).

Generalization of the binomial formula:

**Formula 11**: \\((x + y)^{n} = \sum_{k}^{}\binom{n}{k}x(x - kz)^{k - 1}(y + kz)^{n - k}\\), where \\(n \geq 0, x \neq 0\\).

#### G. Reversal of the upper index.

The following identity follows directly from the definition of a binomial coefficient,
if each term in the numerator is taken with the opposite sign and multiplied by (−1).

**Formula 12**: \\(\binom{r}{k} = (-1)^{k}\binom{k - r - 1}{k}\\), where k is an integer.

A simple consequence of the above relationship is a summation formula:

**Formula 13**: \\(\sum_{k \leq n}^{}\binom{r}{k}(-1)^{k} = \binom{r}{0} - \binom{r}{1} + ... + (-1)^n\binom{r}{n} = (-1)^n\binom{r - 1}{n}\\), where n is an integer.

For an integer r, another important consequence of the formula can be derived:

**Formula 14**: \\(\binom{n}{m} = (-1)^{n-m}\binom{-(m + 1)}{n - m}\\), where \\(n \geq 0\\) and m is an integer.

Thus, one can move n from the upper position to the lower position.

#### H. Simplification of products.

Products of binomial coefficients can be expressed in several different ways,
by expanding them into factorials and then converting back to binomial coefficient notation.
For example,

**Formula 15**: \\(\binom{r}{m}\binom{m}{k} = \binom{r}{k}\binom{r - k}{m - k}\\), where m and k are integers.

The above formula is very useful in cases where the index (namely, m) appears in both
the upper and lower positions, and we need it in only one place.

#### I. Sums of products.

The following formulas show the values of the sums of products of two binomial coefficients
for various positions of the summation index k:

**Formula 16 (Vandermonde's convolution)**: \\(\sum_{k}^{}\binom{r}{k}\binom{s}{n - k} = \binom{r + s}{n}\\), where n is an integer.

**Formula 17**: \\(\sum_{k}^{}\binom{r}{m + k}\binom{s}{n + k} = \binom{r + s}{r - m + n}\\),
where m, n are integers and \\(r \geq 0\\).

**Formula 18**: \\(\sum_{k}^{}\binom{r}{k}\binom{s + k}{n}(-1)^{r-k} = \binom{s}{n - r}\\), where n is an integer and \\(r \geq 0\\).

**Formula 19**: \\(\sum_{k=0}^{r}\binom{r-k}{m}\binom{s}{k - t}(-1)^{k-t} = \binom{r - t - s}{r - t - m}\\),
where \\(t \geq 0\\), \\(r \geq 0\\), and \\(m \geq 0\\).

**Formula 20**: \\(\sum_{k=0}^{r}\binom{r-k}{m}\binom{s + k}{n} = \binom{r + s + 1}{m + n + 1}\\),
where \\(n \geq s \geq 0\\), \\(m \geq 0\\), and \\(r \geq 0\\).

**Formula 21**: \\(\sum_{k \geq 0}^{}\binom{r - tk}{k}\binom{s - t(n - k)}{n - k}\frac{r}{r - tk} = \binom{r + s - tn}{n}\\), where n is an integer.

## Generating combinations

> Given a collection of elements. Generate all possible combinations from this collection of a given length n. Order does not matter.
>
> Taken from [the article "Combinatorial Algorithms in Scala"][kostyukov]

**Algorithm:**

- **Step 1** (Check length): If `n` is greater than the list size `list`, return an empty list,
  as it is impossible to choose more elements than exist in the list.
- **Step 2** (Base cases): If `n` equals `1`, return a list consisting of all elements of `list`,
  each wrapped in a separate list.
  If `n` equals the size of `list`, then in this case there is only one possible combination — the list itself.
- **Step 3** (Recursive case): Split the list `list` into the head element (`hd`) and the tail (`tl`) — all elements except the head.
    - Get all combinations that include the head element.
        - For the tail `tl`, recursively go back to **Step 1**, decreasing `n` by `1`.
        - Add the head element `hd` to each of the obtained combinations.
    - Get all combinations that do not include the head element.
        - For the tail `tl`, recursively go back to **Step 1** without decreasing `n`.
    - Combine both results.
- **Step 4** (Return result): Return the combined list of all found combinations.

**Example:**

Consider the example of computing for the list `("a", "b", "c")` with combination length `n = 2`.

- Input: List: `list = ("a", "b", "c")`, Combination length: `n = 2`.
- Length check: `n` (`2`) is not greater than the list size (`3`).
- Base case: `n` is not equal to `1` or the list size, so proceed to the recursive case.
- Recursive case:
    - Split the list into head (`hd`) and tail (`tl`): `hd = "a", tl = ("b", "c")`.
    - Go back to Step 1 for the tail `tl` with `n - 1` (i.e., `1`): the algorithm returns `(("b"), ("c"))`.
    - Add `hd` to the obtained combinations:
        - `("a") :: ("b") → ("a", "b")`
        - `("a") :: ("c") → ("a", "c")`
    - Now we have: `(("a", "b"), ("a", "c"))`.
    - Go back to Step 1 for the tail `tl` without decreasing `n`.
      We get the base case since `n` equals the list size, so we return `("b", "c")`.
- Combine results: `(("a", "b"), ("a", "c"), ("b", "c"))`.

**Estimation:**

- **Time** - \\(O(C\_{n}^{k})\\)
- **Memory** - \\(O(C\_{n}^{k})\\)

**Code:**

```dotty
extension [A](list: List[A])
  def xcombinations(n: Int): List[List[A]] =
    if n > list.length then Nil
    else if n == list.length then List(list)
    else
      list match
        case _ :: _ if n == 1 => list.map(List(_))
        case hd :: tl =>
          tl.xcombinations(n - 1).map(hd :: _) ::: tl.xcombinations(n)
        case _ => Nil

List("a", "b", "c").xcombinations(2)
// List(List("a", "b"), List("a", "c"), List("b", "c"))
```

## Generating combinations of all lengths

> Given a collection of elements. Generate all possible combinations from this collection of all possible lengths.
> Order does not matter.
>
> The total number of combinations in a set of length n can be calculated as follows:
>
> \\(C\_{n}^{}= \sum\_{1}^{n}C\_{n}^{k} = 2^n\\)
>
> Taken from [the article "Combinatorial Algorithms in Scala"][kostyukov]

**Algorithm:**

- **Step 1**. Iterate `i` over all possible combination lengths from `1` to `n`.
- **Step 2**. For each length `i`, use the algorithm for generating combinations of a given length.

**Estimation:**

- **Time** - \\(O(2^{n})\\)
- **Memory** - \\(O(2^{n})\\)

**Code:**

```dotty
extension [A](list: List[A])
  def xsubsets: List[List[A]] =
    (2 to list.length).foldLeft(list.xcombinations(1)): (a, i) =>
      list.xcombinations(i) ::: a

List("a", "b", "c").xsubsets
// List(List("a", "b", "c"),
// List("a", "b"), List("a", "c"), List("b", "c"),
// List("a"), List("b"), List("c"))
```

---

**Links:**

- [Source code][sample]
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.6](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Combinatorial Algorithms in Scala - kostyukov.net][kostyukov]

[kostyukov]: https://kostyukov.net/posts/combinatorial-algorithms-in-scala/
[sample]: https://gitverse.ru/scalabook/algorithms
