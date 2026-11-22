---
layout: default
title: Puzzlers
parent: Translations
summary: Programming puzzles and challenging exercises.
nav_order: 5
---

# Puzzlers
{: .no_toc }

{% include toc.html %}

# Puzzlers

Here you'll find a collection of programming puzzles and exercises designed to
challenge the reader and deepen understanding of functional programming concepts.

## Puzzle 1: The Mysterious List

Given the following code:

```scala
val xs = List(1, 2, 3, 4, 5)
val result = xs.map(_ * 2).filter(_ > 4).zipWithIndex.map { case (v, i) => (v, i) }
```

What is the value of `result`?

**Answer:** `List((6, 1), (8, 2), (10, 3))`

## Puzzle 2: Type Inference

What is the type of the following expression?

```scala
def foo[A](a: A): A => A = identity
val bar = foo[List[Int]] _
```

**Answer:** `List[Int] => List[Int]`

## Puzzle 3: Monadic Identity

Prove that for any monad `M` and any value `a: M[A]`:

```
flatMap(flatMap(a)(identity))(identity) == flatMap(a)(identity)
```

## Puzzle 4: Option Chain

Given:

```scala
val opt1: Option[Int] = Some(42)
val opt2: Option[String] = None
val opt3: Option[Boolean] = Some(true)
```

What does this evaluate to?

```scala
for
  x <- opt1
  y <- opt2
  z <- opt3
yield x + y.size + b
```

**Answer:** `None`

## Puzzle 5: Custom Monoid

Implement a monoid for `String` under concatenation:

```scala
object StringMonoid:
  import cats.Monoid

  given Monoid[String] with
    def empty: String = ""
    def combine(x: String, y: String): String = x + y
```

## Puzzle 6: Tail-Recursive Function

Rewrite the following non-tail-recursive function as a tail-recursive one:

```scala
def length[A](list: List[A]): Int =
  list match
    case Nil => 0
    case Cons(_, t) => 1 + length(t)
```

**Answer:**

```scala
import scala.annotation.tailrec

@tailrec
def length[A](list: List[A], acc: Int = 0): Int =
  list match
    case Nil => acc
    case Cons(_, t) => length(t, acc + 1)
```

## Puzzle 7: ZipWith

Implement `zipWith` for lists:

```scala
def zipWith[A, B, C](as: List[A], bs: List[B])(f: (A, B) => C): List[C] =
  (as, bs) match
    case (Nil, _) | (_, Nil) => Nil
    case (Cons(h1, t1), Cons(h2, t2)) => Cons(f(h1, h2), zipWith(t1, t2)(f))
```

## Puzzle 8: Fold to Map

Implement `map` using `foldRight`:

```scala
def map[A, B](as: List[A])(f: A => B): List[B] =
  foldRight(as)(Nil: List[B]) { (a, acc) => Cons(f(a), acc) }
```

## More Puzzles

These are just a few examples. More puzzles will be added over time.
