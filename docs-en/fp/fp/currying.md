# Currying

[Currying](https://en.wikipedia.org/wiki/Currying) is
a fundamental technique in functional programming, named after the mathematician Haskell Curry.
This concept allows transforming functions with multiple arguments into a chain of single-argument functions.

Although the concept is named after [Haskell Curry](https://en.wikipedia.org/wiki/Haskell_Curry),
it was initially proposed by [Moses Schönfinkel](https://en.wikipedia.org/wiki/Moses_Sch%C3%B6nfinkel) in 1924.
Currying became an important part of lambda calculus and functional programming languages.

## Core Idea

Currying transforms a function of type `(A, B) => C` into a function of type `A => (B => C)`. This means:

- Instead of a function that takes both arguments at once, we get a function that:
    - Takes the first argument (`A`)
    - Returns a new function that expects the second argument (`B`)
    - After receiving the second argument, returns the result (`C`)

## Comparing Curried and Uncurried Functions

Let's consider two implementations of a minimum-finding function:

```dotty
// Curried version
def min(x: Int)(y: Int): Int = if x < y then x else y

// Uncurried version
def min2(x: Int, y: Int): Int = if x < y then x else y
```

### Key Differences

| Feature                  | Curried (`min`)              | Uncurried (`min2`)           |
|--------------------------|------------------------------|------------------------------|
| **Function type**        | `Int => (Int => Int)`        | `(Int, Int) => Int`          |
| **Invocation method**    | `min(5)(10)` or `min(5)`     | `min2(5, 10)`                |
| **Partial application**  | Supported                    | Requires explicit conversion |
| **Flexibility**          | Higher                       | Lower                        |

### Type Analysis

**Curried version**:

```dotty
min: Int => (Int => Int)
min(5): Int => Int  // intermediate function
min(5)(10): Int     // final result
```

**Uncurried version**:

```dotty
min2: (Int, Int) => Int
min2(5, 10): Int    // immediate final result
```

## Advantages of Currying

**Partial application** — the ability to fix some arguments:

```dotty
val positiveMin = min(0)  // function that finds the minimum between 0 and another number
positiveMin(-5)  // -5
positiveMin(10)  // 0
```

**Function composition** — convenience in creating transformation chains:

```dotty
List(1, 2, 3).map(min(2))  // [1, 2, 2]
```

**Lazy evaluation** — deferred execution until all arguments are received:

```dotty
val expensiveComputation: Int => Int = x => { /* expensive computation */ }
val curried = (x: Int) => (y: Int) => expensiveComputation(x) + y
val stage1 = curried(42)  // computation is not executed
```

**Modularity** — the ability to split complex logic into simple steps:

```dotty
def process(data: Data)(filter: Filter)(transformer: Transformer): Result = ???

val dataProcessor = process(data) _    // fix the data
val withFilter = dataProcessor(filter) // add a filter
val result = withFilter(transformer)   // apply the transformation
```

## Extended Example: Curried Addition

```dotty
def add(x: Int)(y: Int): Int = x + y
```

### Practical Application:

```dotty
// Creating specialized functions
val increment = add(1)      // y => 1 + y
val decrement = add(-1)     // y => -1 + y
val identity = add(0)       // y => y

// Using in combinations
List(1, 2, 3).map(increment)  // [2, 3, 4]
List(5, 6, 7).map(add(10))    // [15, 16, 17]
```

## Currying and Uncurrying Functions

### Basic Transformations

```dotty
// Converting an uncurried function to curried
def curry[A, B, C](f: (A, B) => C): A => (B => C) =
  a => b => f(a, b)

// Converting in the opposite direction
def uncurry[A, B, C](f: A => B => C): (A, B) => C =
  (a, b) => f(a)(b)
```

### Type Conversion Table

| Source Type        | Curried Type          | Usage Example              |
|--------------------|-----------------------|----------------------------|
| `(A,B) => C`       | `A => B => C`         | `curry((a,b) => a + b)`    |
| `(A,B,C) => D`     | `A => B => C => D`    | `curry3((a,b,c) => a + b + c)` |
| `(A1,...,An) => R` | `A1 => ... => An => R` | General conversion         |

## Currying Invariant

The `curry` and `uncurry` transformations are inverses of each other:

```dotty
uncurry(curry(f)) == f  // For any f: (A, B) => C
curry(uncurry(g)) == g  // For any g: A => B => C
```

This means we can freely convert functions between curried and uncurried forms without losing information.

## Practical Usage in Scala

### Implementation Specifics in Scala

**Syntactic sugar**:

```dotty
// Equivalent notations
def add(x: Int)(y: Int) = x + y
def add = (x: Int) => (y: Int) => x + y
```

**Automatic currying**:

```dotty
val sum = (x: Int, y: Int) => x + y
val curriedSum = sum.curried  // standard Scala method
```

**Partial application**:

```dotty
List(1, 2, 3).map(_ + 1)     // using placeholder syntax
List(1, 2, 3).map(add(1))    // with a curried function
```

### Typical Use Cases

**Configuring functions**:

```dotty
def connect(dbUrl: String)(credentials: Credentials): Connection = ???

val prodConnection = connect("jdbc:prod://db") _
val devConnection = connect("jdbc:dev://localhost") _
```

**DSL Creation**:

```dotty
def html(tag: String)(content: String): String = s"<$tag>$content</$tag>"
val div = html("div") _
val p = html("p") _

div(p("Hello world"))  // <div><p>Hello world</p></div>
```

**Dependency Injection**:

```dotty
def service(userRepo: UserRepository)(auth: AuthService): BusinessLogic = ???

val prodLogic = service(prodUserRepo)(prodAuth)
val testLogic = service(testUserRepo)(mockAuth)
```

**Data Processing Pipelines**:

```dotty
def pipeline(validate: Validator)(transform: Transformer)(save: Saver): Data => Result = 
  data => save(transform(validate(data)))

val dataPipeline = pipeline(jsonValidator)(jsonToModel)(dbSaver)
```

## Exercise 1.6.1: Currying and Partial Application Practice

> [Repository with exercises and instructions][sample]

**Task**: Implement and test curried function versions, following these requirements:

1. Implement a `multiply` function that takes three arguments one at a time (`x`, `y`, `z`) and returns their product
2. Create a curried version of `concatStrings` that adds a delimiter between strings
3. Implement generic `curry` and `uncurry` functions for three arguments

```dotty
// 1. Curried multiplication (three-argument)
def multiply(x: Int)(y: Int)(z: Int): Int = ???

// 2. Curried string concatenation with delimiter
def concatStrings(delimiter: String)(s1: String)(s2: String): String = ???

// 3. Generic conversion functions
def curry3[A,B,C,D](f: (A,B,C) => D): A => B => C => D = ???
def uncurry3[A,B,C,D](f: A => B => C => D): (A,B,C) => D = ???
```

**Requirements**:

- All functions must be pure (no side effects)
- Use parametric polymorphism for `curry3`/`uncurry3`
- Support arbitrary argument types

**Usage examples**:

```dotty
// 1. Multiplication
val tripleMultiply = multiply(2)(3)(_)  // Partially applied
tripleMultiply(4) // 24

// 2. Concatenation
val joinWithComma = concatStrings(", ")("Hello")(_) 
joinWithComma("World") // "Hello, World"

// 3. Conversions
val sum3 = (a: Int, b: Int, c: Int) => a + b + c
val curriedSum = curry3(sum3)
curriedSum(1)(2)(3)  // 6

val uncurried = uncurry3(curriedSum)
uncurried(1, 2, 3)  // 6
```

@:note(🔍 Show Solution)

```dotty
def multiply(x: Int)(y: Int)(z: Int): Int = x * y * z

def concatStrings(delimiter: String)(s1: String)(s2: String): String =
  s"$s1$delimiter$s2"

def curry3[A, B, C, D](f: (A, B, C) => D): A => B => C => D =
  a => b => c => f(a, b, c)

def uncurry3[A, B, C, D](f: A => B => C => D): (A, B, C) => D =
  (a, b, c) => f(a)(b)(c)
```

@:@

## Conclusion

Currying is a powerful tool in functional programming that:

✓ Enables creating more flexible and composable functions  
✓ Supports partial application and creating specialized functions  
✓ Simplifies dependency injection and configuration  
✓ Forms the basis for many functional programming techniques

---

**Links:**

- [Link to repository with exercises][sample]
- [Harrison - Introduction to Functional Programming](https://funprog-ru.github.io/)
- [Currying in Scala - Baeldung](https://www.baeldung.com/scala/currying)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Mastering Currying in Scala: A Functional Programming Powerhouse](https://iamsarathchandra.medium.com/mastering-currying-in-scala-a-functional-programming-powerhouse-ce88a3edf3ce)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)

[sample]: https://gitverse.ru/scalabook/fp
