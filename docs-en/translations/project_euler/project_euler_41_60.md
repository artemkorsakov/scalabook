---
layout: default
title: Problems 41–60
parent: Project Euler
parent_url: /translations/project_euler/
summary: Solutions to Project Euler problems 41 to 60.
nav_order: 3
---

# Project Euler: Problems 41–60
{: .no_toc }

{% include toc.html %}

# Project Euler: Problems 41–60

## Problem 41

**Pandigital prime**

We shall say that an n-digit number is pandigital if it makes use of all the
digits 1 to n exactly once. For example, 2143 is a 4-digit pandigital and is
also prime.

What is the largest n-digit pandigital prime that exists?

```scala
object Problem41 {
  import java.util.Collections

  def isPandigital(n: Int): Boolean = {
    val s = n.toString
    val digits = s.sorted
    digits == "123456789".take(s.length)
  }

  def isPrime(n: Int): Boolean = {
    if (n < 2) false
    else (2 to math.sqrt(n).toInt).forall(n % _ != 0)
  }

  def solve: Int = {
    // The largest pandigital number is 7654321 (8 and 9 digit pandigitals
    // are always divisible by 3)
    (7654321 to 1 by -1).find(n => isPandigital(n) && isPrime(n)).get
  }
}
```

## Problem 42

**Coded triangle numbers**

The nth term of the sequence of triangle numbers is given by `t(n) = ½ n(n+1)`;
so the first ten triangle numbers are: 1, 3, 6, 10, 15, 21, 28, 36, 45, 55.

By converting each letter in a word to a number corresponding to its alphabetical
position and adding these values we form a word value. For example, the word
value of SKY is 19 + 11 + 25 = 55 = t(10).

If the word value is a triangle number we call the word a triangle word.

Using `words.txt` (a 16K text file containing nearly two-thousand common English
words), how many are triangle words?

```scala
object Problem43 {
  val triangleNumbers = {
    val gen = Iterator.from(1).map(n => n * (n + 1) / 2)
    gen.takeWhile(_ <= 1000).toSet
  }

  def wordValue(word: String): Int =
    word.toUpperCase.foldLeft(0)(_ + (_.toInt - 'A' + 1))

  def solve: Int = {
    val words = scala.io.Source.fromFile("words.txt").mkString
      .stripPrefix("\"").split("\"[,]\"").map(_.stripSuffix("\""))
    count(words.filter(w => triangleNumbers.contains(wordValue(w))))
  }
}
```

## Problems 43–60

For problems 43 through 60, see the individual solution files.

---

## Links

- [Project Euler site][euler]
- [Project Euler chat][euler_chat]

[euler]: https://projecteuler.net
[euler_chat]: https://projecteuler.chat
