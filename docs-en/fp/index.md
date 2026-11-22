# Functional Programming

## Introduction to the Functional Paradigm

[Functional Programming (FP)][wiki] is a declarative programming paradigm
based on mathematical functions, where computations are modeled as a sequence of
data transformations through the application of functions. Unlike the imperative approach,
FP avoids state mutations (_state_) and working with mutable (_mutable_) data,
focusing on:

- Function purity (absence of side effects)
- Data immutability
- Function composition as the primary way of building programs
- Expressions over statements

### Key Advantages of the Functional Approach

1. **Improved code readability and maintainability** - thanks to small function sizes,
    their atomicity, and the absence of hidden dependencies
2. **Simplified debugging** - since pure functions depend only on their arguments
    and have no hidden state, their behavior is fully predictable
3. **Natural parallelization** - the absence of mutable state and side effects
    eliminates data race (_race conditions_) issues in multi-threaded execution
4. **Modularity and reusability** - functions as building blocks easily combine into
    more complex constructs

In this section we will study the fundamental concepts of FP in detail, including:

- Pure functions
- Recursion and tail-call optimization
- Higher-order functions
- Lazy evaluation
- Algebraic data types and pattern matching

## Comparing Imperative and Functional Styles

Let's consider the key difference between [imperative programming](https://en.wikipedia.org/wiki/Imperative_programming)
and functional programming with a practical example of computing the sum of elements in a list of numbers.

### Imperative Approach

Characterized by:

- Explicit state management through variables
- Use of loops for iteration
- A sequence of state-mutating commands

```dotty
def sum(ints: List[Int]): Int =
  var sum = 0                         // Mutable state
  var i = 0                           // Another mutable state
  while i < ints.length do            // Imperative loop
    sum += ints(i)                    // State mutation
    i += 1                            // State mutation
  sum                                 // Return result

sum(List(1, 2, 3))                    // 6
```

### Functional Approach

Distinguished by:

- Absence of mutable state
- Use of recursion instead of loops
- Data immutability

```dotty
def sum(xs: List[Int]): Int =
  // Helper function
  def loop(xs: List[Int], acc: Int): Int =  
    xs match                          // Pattern matching
      case Nil       => acc           // Base recursive case
      case x :: tail => loop(tail, acc + x) // Recursive case

  loop(xs, 0)                         // Initial call with accumulator = 0

sum(List(1, 2, 3))                    // 6
```

### Detailed Explanation of the Functional Solution

1. **Structure**:
    - The main function `sum` initializes the process by calling the helper function `loop`
      with an initial accumulator of `0`
    - The `loop` function implements recursion for accumulating the result
2. **Pattern matching**:
    - `Nil` - terminal case for an empty list
    - `x :: tail` - decomposes the list into head (`x`) and tail (`tail`)
3. **Recursive process**:
    - On each step, the current element `x` is added to the accumulator `acc`
    - The recursive call is made with the tail of the list and the new accumulator value
    - Upon reaching an empty list, the accumulated value is returned
4. **Immutability**:
    - All parameters (`xs`, `acc`) are immutable
    - New values are passed through recursive call arguments

## Key Rules of Functional Style

1. **Avoiding null** - using `Option[T]` to represent the possible absence of a value
2. **Pure functions** - guarantee that a function with the same inputs always returns
    the same result without side effects
3. **Immutability** - preference for `val` over `var`, immutable data structures
4. **Avoiding OOP classes** - using case classes for data and traits for behavior
5. **Expressions over statements** - `if` should return a value, not perform actions
6. **Composition** - building complex logic through combination of simple functions

## Exercise 1.1.1: Implementing Factorial

> [Repository with exercises and instructions][sample]

Your task is to complete the implementation of the factorial computation function
by replacing the omitted parts (`???`) with correct code:

```dotty
def factorial(n: Int, acc: Long = 1): Long =
  if (n <= 1) ???
  else factorial(n - 1, ???)
```

**Hints**:

1. The function uses recursion
2. The parameter `acc` serves as an accumulator to store the result
3. When `n <= 1`, recursion should terminate, returning the accumulated value
4. On each step, the accumulator needs to be updated

@:note(T🔍 Show Solution)

How the function works:

1. Base case: when `n` reaches `1` or below, return the accumulator,
    containing the product of all numbers from `n` down to `1`
2. Recursive case: decrease `n` by `1` and multiply the accumulator by the current `n`
3. Initial value: by default `acc = 1`, which is correct for `factorial(0)` and `factorial(1)`

```dotty
def factorial(n: Int, acc: Long = 1): Long =
  // Terminal condition
  if (n <= 1) acc  
  // Recursive call                    
  else factorial(n - 1, n * acc)       
```

**Example calls**:

```dotty
factorial(5)    // 120 = 5 * 4 * 3 * 2 * 1
factorial(0)    // 1    (by definition)
factorial(10)   // 3628800
```

@:@

---

**Links:**

- [Repository with exercises][sample]
- [16 Months of Functional Programming](https://habr.com/ru/articles/251303/)
- [Ten Reasons Not to Use a Statically Typed Functional Programming Language](https://habr.com/ru/articles/190492/)
- [Functional Programming Jargon](https://habr.com/ru/articles/310172/)
- [Don't use `null`](https://docs.scala-lang.org/scala3/book/first-look-at-types.html#nothing-and-null)
- [Don't Bother Me with Your Functional Programming](https://habr.com/ru/companies/oleg-bunin/articles/462505/)
- [From Aristotle to Turing: What Is Functional Programming and How It Makes Life Easier](https://habr.com/ru/companies/avito/articles/926544/)
- [Why Is Functional Programming So Hard?](https://habr.com/ru/articles/505928/)
    - [Deeper into FP](https://habr.com/ru/articles/506782/) 
- [Functional Programming: Too Many Ceremonies in Java and C#](https://habr.com/ru/companies/jugru/articles/328042/)
- [Functional Programming: What You Were (Probably) Told. If You Listened](https://habr.com/ru/articles/492460/)
- [Harrisson - Introduction to Functional Programming](https://funprog-ru.github.io/)
- [Functional Programming Cheat Sheet](https://habr.com/ru/companies/yandex/articles/547786/)
- [A Beginner-Friendly Tour through Functional Programming in Scala](https://degoes.net/articles/easy-monads)
- [A bite of functional programming (in Scala)](https://scalac.io/blog/functional-programming-in-scala/)
- [A Critique of Impure Reason](https://blog.higher-order.com/blog/2009/04/27/a-critique-of-impure-reason/)
- [Business benefits of functional programming and how to make it work for your company](https://www.youtube.com/watch?v=EcGZdzECmIk)
- [Error Handling in FP](https://gvolpe.com/blog/error-handling-scala3/)
- [FP vs. OO List Processing](https://blog.cleancoder.com/uncle-bob/2018/12/17/FPvsOO-List-processing.html)
- [FP Anti-Patterns](https://scalajobs.com/blog/functional-programming-anti-patterns-in-scala)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming for Beginners](https://apocalisp.wordpress.com/2011/01/10/functional-programming-for-beginners/)
- [Functional Programming Group](https://www.youtube.com/watch?app=desktop&amp;v=s2ay9nEW3ak&amp;list=PLNo5uJesLLQjRmM9lASwVeL0fFtJuKU0U)
- [Functional Programming in Scala](https://www.youtube.com/playlist?list=PLJGDHERh23x8AD0LFiS9iPg_tKLS1i1wZ)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Functional Programming Isn't the Answer](https://degoes.net/articles/fp-is-not-the-answer)
- [Functional Scala](https://www.youtube.com/playlist?list=PLjtl9Y7VJMWtZz3g0LjMhO54_KBL8Tfwl)
- [Imperative vs Functional Programming](https://apocalisp.wordpress.com/2011/05/30/imperative-vs-functional-programming/)
- [In Defense of OOFP](https://alexn.org/blog/2018/02/12/in-defense-oofp/)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Martians vs Monads: Null Considered Harmful](https://james-iry.blogspot.com/2007/08/martians-vs-monads-null-considered.html)
- [Noumenal Null](https://apocalisp.wordpress.com/2008/05/03/null-vs-pure-reason/)
- [Objects, Identity, and Concept-formation](https://blog.higher-order.com/blog/2008/12/04/no-such-thing/)
- [On Scala, Functional Programming and Type-Classes](https://alexn.org/blog/2012/11/02/scala-functional-programming-type-classes/)
- [Phantom types - Make illegal states unrepresentable](https://blog.rhetoricalmusings.com/posts/phantom-types)
    - [Phantom types - Part 2](https://blog.rhetoricalmusings.com/posts/phantom-types-2/) 
- [Scala Exceptions - Try, try..catch and a caveat](https://yadukrishnan.live/scala-exceptions-try-trycatch-and-a-caveat)
- [Scala Functional Programming Tutorial - Baeldung](https://www.baeldung.com/scala/functional-programming-series)
- [Scala's isInstanceOf is an Anti-Pattern - Alexandru Nedelcu](https://alexn.org/blog/2019/08/11/isinstanceof-anti-pattern/)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)
- [Shared State in Functional Programming](https://typelevel.org/blog/2018/06/07/shared-state-in-fp.html)
- [Some History of Functional Programming Languages](https://www.youtube.com/watch?v=QVwm9jlBTik)
- [Unleashing the Power: The Advantages of Functional Programming in the Digital Age](https://scalac.io/blog/unleashing-the-power-the-advantages-of-functional-programming-in-the-digital-age/)
- [What's Functional Programming All About?](https://www.lihaoyi.com/post/WhatsFunctionalProgrammingAllAbout.html)
- [What is Functional Programming? - Alexandru Nedelcu](https://alexn.org/blog/2017/10/15/functional-programming/)
- [What is functional programming and why it matters for your business?](https://scalac.io/blog/benefits-of-functional-programming/)
- [Why is functional programming awesome](https://abuseofnotation.github.io/fp/)
- [What is an Effect?](https://www.inner-product.com/posts/what-is-an-effect/)
- [What makes a function pure?](https://blog.kubukoz.com/what-makes-a-function-pure/)
- [Why Functional Programming Matters](https://blog.rhetoricalmusings.com/posts/why-fp-matters/)
- [Wikipedia][wiki]

[sample]: https://gitverse.ru/scalabook/fp
[wiki]: https://en.wikipedia.org/wiki/Functional_programming
