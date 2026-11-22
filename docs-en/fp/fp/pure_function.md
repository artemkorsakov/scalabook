# Pure Functions

## Definition and Characteristics of Pure Functions

A _pure function_ is a fundamental concept in functional programming meaning that:

1. The function's result depends solely on its input parameters (_determinism_)
2. The function's execution does not cause _side effects_

**A side effect** is any action performed by a function beyond simply returning a computed value, including:

- Modifying variables (both local and global)
- Mutating data structures "in place"
- Updating object fields
- Throwing exceptions or crashing
- Interacting with the outside world:
    - Input/output (console, file system)
    - Graphic operations
    - Network requests
    - Working with system time

## Formal Specification of Pure Functions

From a mathematical perspective, a pure function `f: A => B` represents:

- A total mapping from the domain (type `A`) to the codomain (type `B`)
- Where each input value `a ∈ A` maps to exactly one output value `b ∈ B`
- And where value `b` is computed solely based on `a`, without considering:
    - The internal state of the system
    - The external environment
    - Execution time

This definition makes pure functions analogous to mathematical functions, where `f(x) = x²` will always give the same result for the same `x`.

## Referential Transparency: In-Depth Analysis

_Reference transparency_ is a key property of functional programs, meaning that:

- Any expression can be replaced with its value without changing the program's semantics
- For the same arguments, the result is always identical
- It does not depend on the execution context or the order of computation

**Advantages of referential transparency:**

1. **Correctness verification**: Ability to formally prove code correctness
2. **Simplified refactoring**: Safe replacement of expressions with equivalent ones
3. **Optimizations**:
    - Memoization (caching results)
    - Lazy evaluation
    - Parallel execution
4. **Predictability**: The program's behavior becomes fully predictable

In imperative languages, most functions are not referentially transparent due to state dependencies.

## Exercise 1.2.1: Identifying Pure Functions

> [Repository with exercises and instructions][sample]

Analyze three implementations of a division function and choose the pure version:

```dotty
def pureFunction(a: Int, b: Int): Int =
  def innerFirst(a: Int, b: Int): Int = a / b

  def innerSecond(a: Int, b: Int): Int =
    if (b == 0) Int.MaxValue else a / b

  def innerThird(a: Int, b: Int): Int =
    require(b != 0, "Division by zero!")
    a / b

  // innerFirst(a, b)
  // innerSecond(a, b)
  // innerThird(a, b)
  ???
```

@:note(🔍 Show Solution)

Analysis criteria:

1. `innerFirst`:
    - Not pure: at `b=0` it throws an exception (side effect)
    - Violates referential transparency (different behavior for `b=0`)
2. `innerSecond`:
    - Pure: always returns a value
    - Deterministic: same inputs → same outputs
    - No side effects
3. `innerThird`:
    - Not pure: `require` throws an `IllegalArgumentException`
    - Alters execution flow (side effect)

Conclusion: Only `innerSecond` satisfies all purity conditions:

- Complete handling of all input cases
- No exceptions
- Independence from external state

@:@

---

**Links:**

- [Link to repository with exercises][sample]
- [Harrison - Introduction to Functional Programming](https://funprog-ru.github.io/)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)
- [What Purity Is and Isn't](https://blog.higher-order.com/blog/2012/09/13/what-purity-is-and-isnt/)

[sample]: https://gitverse.ru/scalabook/fp
