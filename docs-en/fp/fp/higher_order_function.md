# Higher-Order Functions

## Definition and Key Characteristics

[A Higher-Order Function (HOF)](https://en.wikipedia.org/wiki/First-class_function) is
a fundamental concept in functional programming describing functions that:

1. **Accept other functions as parameters** — can receive one or more functions for further processing
2. **Return functions as results** — can generate and return new functions

These properties make HOFs a powerful tool for:

- Abstracting common processing patterns
- Creating flexible and reusable components
- Implementing deferred computation
- Building compositions of operations

## Practical Example: Universal Result Formatter

Let's consider the implementation of `formatResult`, which demonstrates how HOFs work:

```dotty
// Compute the absolute value of a number
def abs(n: Int): Int =
  if n < 0 then -n
  else n

// Compute factorial (tail-recursive version)
def factorial(n: Int): Int =
  @annotation.tailrec
  def go(n: Int, acc: Int): Int =
    if n <= 0 then acc
    else go(n - 1, n * acc)

  go(n, 1)

// Higher-order function for formatting results
def formatResult(name: String, n: Int, f: Int => Int): String =
  val msg = "The %s of %d is %d."
  msg.format(name, n, f(n))
```

### Implementation Analysis

1. **HOF Signature**:
    - `formatResult` accepts a function `f` of type `Int => Int`
    - This means `f` must take an `Int` and return an `Int`
    - This approach allows abstracting away from the specific operation
2. **Type Compatibility**:
    - Functions `abs` and `factorial` fully match the type `Int => Int`
    - This allows passing them to `formatResult`

**Usage examples**:

```dotty
formatResult("absolute value", -42, abs)
// Result: "The absolute value of -42 is 42."

formatResult("factorial", 7, factorial)
// Result: "The factorial of 7 is 5040."
```

## Advantages of This Approach

1. **Generality**:
    - A single formatter can work with any functions of the appropriate type
    - No need to write separate formatters for each type of computation
2. **Flexibility**:
    - Easy to add new operations without modifying `formatResult`
    - Functions can be combined to create complex behaviors
3. **Readability**:
    - Explicit type annotations make the code more understandable
    - Formatting logic is separated from computation logic

## Exercise 1.5.1: Practical HOF Application

> [Repository with exercises and instructions][sample]

**Task**: Implement a higher-order function that:

1. Accepts a transformation function `A => B` and a list of elements of type `A`
2. Returns a new list where each element is transformed using the provided function
3. Preserves the order of elements from the original list

```dotty
def map[A, B](as: List[A])(f: A => B): List[B] = ???
```

**Requirements**:

- Use parametric types (generics)
- Support working with different data types
- Implement a recursive version

**Usage examples**:

```dotty
map(List(1, 2, 3))(_ * 2) // List(2, 4, 6)
map(List("a", "b"))(_.toUpperCase) // List("A", "B")
```

This exercise demonstrates the classic higher-order function `map`, which is the foundation of functional programming.

@:note(🔍 Show Solution)

```dotty
def map[A, B](as: List[A])(f: A => B): List[B] =
  as match
    case Nil => Nil
    case h :: t => f(h) :: map(t)(f)
```

@:@

## Exercise 1.5.2: Function-Validator Factory

**Task**: Implement a higher-order function that:

1. Accepts a predicate (check function) of type `A => Boolean`
2. Returns a new validator function of type `A => Either[String, A]`
3. The returned function should:
    - Accept a value of type `A`
    - Return `Right(value)` if the predicate returns `true`
    - Return `Left(errorMessage)` if the predicate returns `false`

```dotty
def validatorFactory[A](predicate: A => Boolean, errorMessage: String): A => Either[String, A] = ???
```

**Requirements**:

- Use parametric types (generics)
- Support custom error messages
- Preserve the original value on successful validation
- Return a function (do not compute the result immediately)

**Usage examples**:

```dotty
// Create validators
val ageValidator = validatorFactory[Int](_ >= 18, "Age must be >= 18")
val emailValidator = validatorFactory[String](_.contains("@"), "Invalid email format")

// Apply validators
ageValidator(25)    // Right(25)
ageValidator(16)    // Left("Age must be >= 18")
emailValidator("test@example.com") // Right("test@example.com")
emailValidator("invalid")          // Left("Invalid email format")
```

@:note(🔍 Show Solution)

```dotty
def validatorFactory[A](predicate: A => Boolean, errorMessage: String): A => Either[String, A] =
  (value: A) =>
    if predicate(value) then Right(value)
    else Left(errorMessage)
```

@:@

## Anonymous Functions

[Anonymous functions](https://en.wikipedia.org/wiki/Anonymous_function)
(also known as function literals or lambda expressions) are functions that:

- Have no name (not bound to an identifier)
- Can be defined in place of use
- Are often passed as arguments to higher-order functions

**Advantages of anonymous functions:**

1. **Locality** — defining the function directly at its point of use
2. **Conciseness** — reducing code by avoiding naming
3. **Flexibility** — quick creation of specialized functions

**Usage example**

Consider a function that finds the first element in an array satisfying a condition:

```dotty
def findFirst[A](as: Array[A], predicate: A => Boolean): Int =
  @annotation.tailrec
  def loop(index: Int): Int =
    if index >= as.length then -1
    else if predicate(as(index)) then index
    else loop(index + 1)

  loop(0) // Start search from index 0
```

Usage with an anonymous function:

```dotty
// Find the number 9 in an array
findFirst(Array(7, 9, 13), (x: Int) => x == 9)
// Result: 1 (index of the found element)
```

**Anonymous function syntax in Scala**

The expression `(x: Int) => x == 9` consists of:

- Parameter list `(x: Int)`
- Arrow/lambda symbol `=>`
- Function body `x == 9`

**Additional examples**

Simple transformation:

```dotty
val square = (x: Int) => x * x
square(5) // 25
```

Filtering a list:

```dotty
List(1, 2, 3).filter(x => x % 2 == 1) // List(1, 3)
```

---

**Links:**

- [Link to repository with exercises][sample]
- [Harrison - Introduction to Functional Programming](https://funprog-ru.github.io/)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Higher-Order Functions in Scala - Baeldung](https://www.baeldung.com/scala/higher-order-functions)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)

[sample]: https://gitverse.ru/scalabook/fp
