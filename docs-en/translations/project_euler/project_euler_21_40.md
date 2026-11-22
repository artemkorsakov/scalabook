---
layout: default
title: Problems 21–40
parent: Project Euler
parent_url: /translations/project_euler/
summary: Solutions to Project Euler problems 21 to 40.
nav_order: 2
---

# Project Euler: Problems 21–40
{: .no_toc }

{% include toc.html %}

# Project Euler: Problems 21–40

## Problem 21

**Amicable numbers**

Let `d(n)` be defined as the sum of proper divisors of `n` (numbers less than `n`
which divide evenly into `n`).

If `d(a) = b` and `d(b) = a`, where `a ≠ b`, then `a` and `b` are an amicable
pair and each of `a` and `b` are called amicable numbers.

For example, the proper divisors of 220 are 1, 2, 4, 5, 10, 11, 20, 22, 44, 55
and 110; therefore `d(220) = 284`. The proper divisors of 284 are 1, 2, 4, 71
and 142; so `d(284) = 220`.

Evaluate the sum of all the amicable numbers under 10000.

```scala
object Problem21 {
  def sumDivisors(n: Int): Int =
    (1 to n / 2).filter(n % _ == 0).sum

  def solve: Int =
    (1 until 10000).filter { a =>
      val b = sumDivisors(a)
      b != a && sumDivisors(b) == a
    }.sum
}
```

## Problem 22

**Names scores**

Using `names.txt` (a 46K text file containing over five-thousand first names),
begin by sorting it into alphabetical order. Then compute a score for each name
by finding the sum of the alphabetic positions of its letters, and multiplying
this sum by the name's position in the sorted list.

Find the total of all name scores.

```scala
object Problem22 {
  def nameScore(name: String, position: Int): Int = {
    val letterSum = name.toUpperCase.foldLeft(0)(_ + (_.toInt - 'A' + 1))
    letterSum * position
  }

  def solve: Int = {
    val names = scala.io.Source.fromFile("names.txt").mkString
      .stripPrefix("\"").stripSuffix("\"")
      .split("\"[,]\"").map(_.stripPrefix("\"").stripSuffix("\""))
      .sorted
    names.zipWithIndex.map { case (name, i) => nameScore(name, i + 1) }.sum
  }
}
```

## Problems 23–40

For problems 23 through 40, see the individual solution files.

---

## Links

- [Project Euler site][euler]
- [Project Euler chat][euler_chat]

[euler]: https://projecteuler.net
[euler_chat]: https://projecteuler.chat
