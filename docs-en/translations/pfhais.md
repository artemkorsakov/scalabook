---
layout: default
title: Parsing Functional Haskell
parent: Translations
summary: Reading and understanding the "Parsing Functional Haskell" tutorial.
nav_order: 9
---

# Parsing Functional Haskell
{: .no_toc }

{% include toc.html %}

# Parsing Functional Haskell

This section contains a translation and commentary on the "Parsing Functional Haskell"
tutorial, which introduces parser combinators and their application in functional programming.

## What Are Parsers?

A parser is a function that takes input text and produces a structured representation.
In functional programming, parsers are often built using **combinators** — small parsing
units combined to form complex parsers.

## Basic Combinators

```scala
sealed trait Parser[+A]:
  def apply(in: String): Option[(A, String)]

case class Success[A](value: A) extends Parser[A]:
  def apply(in: String): Option[(A, String)] = Some((value, in))

case class Failure(error: String) extends Parser[Nothing]:
  def apply(in: String): Option[(Nothing, String)] = None
```

## Parser Combinators

```scala
extension [A](p: Parser[A])
  def map[B](f: A => B): Parser[B] =
    Parser { in =>
      p(in).map { case (a, rest) => (f(a), rest) }
    }

  def flatMap[B](f: A => Parser[B]): Parser[B] =
    Parser { in =>
      p(in).flatMap { case (a, rest) => f(a)(rest) }
    }

  def or[B >: A](p2: Parser[B]): Parser[B] =
    Parser { in =>
      p(in).orElse(p2(in))
    }

  def many: Parser[List[A]] =
    Parser { in =>
      val results = scala.collection.mutable.ListBuffer[A]()
      var current = in
      var continued = true
      while continued:
        p(current) match
          case Some((a, rest)) =>
            results += a
            current = rest
          case None =>
            continued = false
      Some((results.toList, current))
    }

  def many1: Parser[List[A]] =
    Parser { in =>
      p(in) match
        case Some((a, rest)) =>
          many.map(list => (a :: list, rest))(rest)
        case None => None
    }
```

## Example: Parsing Integers

```scala
val digit: Parser[Int] =
  Parser {
    case c +: rest if c.isDigit => (c.getNumericValue, rest)
    case _ => None
  }

val integer: Parser[Int] =
  digit.map(_.toString) ++ digit.many.map(_.mkString("").toInt)
```

## Recursive Descent

```scala
def expression: Parser[Int] =
  term.flatMap { t =>
    (plus | minus).flatMap { op =>
      expression.map { e =>
        if op == '+' then t + e else t - e
      }
    }
  }.orElse(Success(t))

def term: Parser[Int] =
  factor.flatMap { f =>
    (times | divide).flatMap { op =>
      term.map { t =>
        if op == '*' then f * t else f / t
      }
    }
  }.orElse(Success(f))
```

## Further Reading

- [Parsing Functional Haskell](https://www.haskell.org/tutorial/parsing.html)
- [Combinator Parser Design](https://www.cs.nott.ac.uk/~pszgmh/ParserCombinators.pdf)
