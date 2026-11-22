---
layout: default
title: Problems 81–100
parent: Project Euler
parent_url: /translations/project_euler/
summary: Solutions to Project Euler problems 81 to 100.
nav_order: 5
---

# Project Euler: Problems 81–100
{: .no_toc }

{% include toc.html %}

# Project Euler: Problems 81–100

## Problem 81

**Path sum: two ways**

In the 5×5 matrix below, the minimum path sum from top-left to bottom-right,
by only moving to the right and down, is given in bold red and is equal to 2427.

```
131    673    234    103     18
201    96     342    965     150
630    803    746    422    111
537    699    497    121   956
805    732    524     37    331
```

Find the minimal path sum from top-left to bottom-right in `matrix.txt` (a 31K text
file containing a 80×80 matrix), by only moving right and down.

```scala
object Problem81 {
  def minPathSum(matrix: Array[Array[Int]]): Int = {
    val m = matrix.length
    val n = matrix(0).length
    val dp = Array.ofDim[Int](m, n)

    dp(0)(0) = matrix(0)(0)
    for (i <- 1 until m) dp(i)(0) = dp(i - 1)(0) + matrix(i)(0)
    for (j <- 1 until n) dp(0)(j) = dp(0)(j - 1) + matrix(0)(j)

    for (i <- 1 until m; j <- 1 until n)
      dp(i)(j) = matrix(i)(j) + math.min(dp(i - 1)(j), dp(i)(j - 1))

    dp(m - 1)(n - 1)
  }
}
```

## Problem 90

**Cube digit pairs**

Each of the six faces of a cube has a different digit (0 to 6) written on it.

The following two sets of cubes, denoted by `A` and `B` respectively, can be used
to form all square numbers below two-hundred:

- A = {0, 1, 4, 2, 5, 6}
- B = {0, 1, 9, 3, 8, 4}

Note that you can flip 6 to use as 9.

In exactly one way can two sets of cubes be formed that allow all square numbers
to be formed?

```scala
object Problem90 {
  val squares = List(1, 4, 9, 16, 25, 36, 49, 64, 81)

  def canFormDigits(d1: Int, d2: Int): List[Int] = {
    val digits = if (d1 == 6 || d1 == 9) List(d1, 9)
                 else if (d2 == 6 || d2 == 9) List(d2, 9)
                 else List(d1, d2)
    digits
  }

  def solve: Int = {
    // Iterate over all possible cube configurations
    // and count valid pairs
    val validPairs = for {
      a <- allCubeConfigurations
      b <- allCubeConfigurations.dropWhile(_ <= a)
      if squares.forall { square =>
        val tens = square / 10
        val ones = square % 10
        (canFormDigits(a(0), a(1)) ++ canFormDigits(a(2), a(3)) ++
         canFormDigits(a(4), a(5))).contains(tens) &&
        (canFormDigits(b(0), b(1)) ++ canFormDigits(b(2), b(3)) ++
         canFormDigits(b(4), b(5))).contains(ones)
      }
    } yield 1
    validPairs.size
  }
}
```

## Problems 82–80, 91–99

For problems 82–89 and 91–99, see the individual solution files.

---

## Links

- [Project Euler site][euler]
- [Project Euler chat][euler_chat]

[euler]: https://projecteuler.net
[euler_chat]: https://projecteuler.chat
