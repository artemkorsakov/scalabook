# Partitions, Coins, and Others

## Coins

> The number of ways to obtain a given sum using a given set of coins.
>
> [Problem 31 from Project Euler](https://projecteuler.net/problem=31)

**Algorithm:**

- **Step 1** (Initialization).
    - Create an array `ways` of length `sum + 1`, initialized with zeros for all elements.
    - Set `ways(0) = 1`, since there is one way to obtain the sum `0` (by using no coins).
- **Step 2** (Loop over coins). For each coin in the `coins` array, perform the following steps:
    - Iterate over all sums from the current coin's value to `sum` (inclusive).
    - For each sum `i`, update the number of ways to obtain it: `ways(i) += ways(i - coin)`, where `coin` is the current coin.
      This means that if we can obtain the sum `i - coin`, then adding the current coin gives us the sum `i`.
      Therefore, the number of ways to obtain sum `i` increases by the number of ways to obtain the sum `i - coin` for each `i`.
- Return result:
  After completing all iterations, return the value `ways(sum)`, which will contain the number of ways to obtain the target sum.

**Example:**

Consider the example of computing the number of ways to obtain the sum `4`, using coins `(1, 2, 3)`:

- **Step 1**: `ways = [1, 0, 0, 0, 0]`, `coins = [1, 2, 3]`, `sum = 4`.
- **Step 2**:
    - Iteration 1 (coin = 1):
        - For `i = 1`: `ways(1) += ways(1 - 1) => ways(1) += ways(0) => ways(1) = 1`
        - For `i = 2`: `ways(2) += ways(2 - 1) => ways(2) += ways(1) => ways(2) = 1`
        - For `i = 3`: `ways(3) += ways(3 - 1) => ways(3) += ways(2) => ways(3) = 1`
        - For `i = 4`: `ways(4) += ways(4 - 1) => ways(4) += ways(3) => ways(4) = 1`
    - The `ways` array after the first iteration: `ways = [1, 1, 1, 1, 1]`
    - Iteration 2 (coin = 2):
        - For `i = 2`: `ways(2) += ways(2 - 2) => ways(2) += ways(0) => ways(2) = 2`
        - For `i = 3`: `ways(3) += ways(3 - 2) => ways(3) += ways(1) => ways(3) = 2`
        - For `i = 4`: `ways(4) += ways(4 - 2) => ways(4) += ways(2) => ways(4) = 3`
    - The `ways` array after the second iteration: `ways = [1, 1, 2, 2, 3]`
    - Iteration 3 (coin = 3):
        - For `i = 3`: `ways(3) += ways(3 - 3) => ways(3) += ways(0) => ways(3) = 3`
        - For `i = 4`: `ways(4) += ways(4 - 3) => ways(4) += ways(1) => ways(4) = 4`
    - The `ways` array after the third iteration: `ways = [1, 1, 2, 3, 4]`
- **Step 3**: Return result: Return `ways(4)`, which equals `4`.
- **Summary**: Thus, there are 4 ways to obtain the sum 4, using coins 1, 2, and 3.

**Estimation:**

- Time complexity:
    - The outer loop iterates over all coins, which is O(n), where n is the number of distinct coins.
    - The inner loop iterates from the current coin's value to the given sum (sum).
      In the worst case, if all coins have the minimum value (e.g., 1), this loop runs sum times.
    - Thus, the overall time complexity is O(n * sum).
- Space complexity:
    - The algorithm uses a `ways` array of size (sum + 1) to store the number of ways for each sum from 0 to sum. This requires O(sum) memory.
    - No other significant data structures are used in the algorithm, so the overall space complexity is O(sum).

- **Time** - O(n * sum)
- **Memory** - O(sum)

**Code:**

```dotty
def countWays(coins: Array[Int], sum: Int): Long =
  val ways = Array.fill(sum + 1)(0L)
  ways(0) = 1
  for
    coin <- coins
    i    <- coin to sum
  do ways(i) += ways(i - coin)
  ways(sum)

countWays(Array(1, 2, 5, 10, 20, 50, 100, 200), 200)
// 73682
```

## Partitions

> [Number of ways](https://ru.wikipedia.org/wiki/%D0%A0%D0%B0%D0%B7%D0%B1%D0%B8%D0%B5%D0%BD%D0%B8%D0%B5_%D1%87%D0%B8%D1%81%D0%BB%D0%B0)
> to write n as a sum of positive integers.

**Code:**

```dotty
def partition(number: Int): BigInt =
  val array = new Array[BigInt](number + 1)
  array(0) = BigInt(1)
  if number >= 1 then array(1) = BigInt(1)
  if number >= 2 then array(2) = BigInt(2)

  def partitionPart(s: Int, n: Int, pS: BigInt): BigInt =
    @tailrec
    def loop(k: Int, p: BigInt): BigInt =
      val op = k * (3 * k - 1) / 2
      if op > n then p
      else
        val newP =
          if (k + 1) % 2 == 0 then p + array(n - op)
          else p - array(n - op)
        loop(k + s, newP)

    loop(s, pS)

  (3 to number).foreach: n =>
    val p = partitionPart(1, n, BigInt(0))
    array(n) = partitionPart(-1, n, p)
  array(number)
end partition

partition(1000)
// BigInt("24061467864032622473692149727991")
```

---

**Links:**

- [Source code][sample]
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.5](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.11.2](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)

[sample]: https://gitverse.ru/scalabook/algorithms
