# Function Composition

Function composition is a foundational concept in functional programming that enables
the creation of complex operations by combining simple functions. This
is analogous to mathematical composition, where the result of one function becomes the input for another.

**Key benefits:**

- 🧩 Modularity — breaking complex logic into simple components
- 🧩 Improved readability — the sequence of operations is expressed explicitly
- 🔄 Reusability — components can be combined in different ways
- 🧪 Testability — simple functions are easier to test in isolation

## Mathematical Foundations

In mathematics, the composition of functions f and g is written as g∘f and means applying f first, then g: `(g∘f)(x) = g(f(x))`

In functional programming, this concept is implemented through:

- The composition operator (∘ in some languages)
- `compose` and `andThen` methods
- Function combinators

## Comparing compose and andThen

In Scala there are two main ways to compose functions:

```dotty
val f: A => B = ???
val g: B => C = ???

// First: compose - g(f(x))
val h1 = g compose f  // "g after f"

// Second: andThen - f andThen g means f first, then g
val h2 = f andThen g   // "f then g"
```

**The difference between them:**

- `compose` applies functions from right to left (mathematical notation)
- `andThen` applies functions from left to right (more intuitive for sequential operations)

## Detailed Examples

### Basic Example

```dotty
val toUpperCase: String => String = _.toUpperCase
val toLowerCase: String => String = _.toLowerCase

// Composition 1: mathematical style
val emphasize1 = toLowerCase compose toUpperCase

// Composition 2: sequential style
val emphasize2 = toLowerCase andThen toUpperCase

emphasize1("Hello") // "Hello" -> "HELLO" -> "hello"
emphasize2("Hello") // "Hello" -> "hello" -> "HELLO"
```

### Working with Different Types

```dotty
case class User(name: String, age: Int)

val parseUser: String => User = json => /* parsing JSON */ 
val isAdult: User => Boolean = _.age >= 18
val toResult: Boolean => String = if (_) "Access granted" else "Access denied"

val checkAccess = parseUser andThen isAdult andThen toResult

checkAccess("""{"name":"Alice","age":20}""") // "Access granted"
```

## Properties of Composition

a) **Associativity**:

```dotty
(f andThen g) andThen h == f andThen (g andThen h)
```

The order of grouping does not affect the result.

b) **Non-commutativity** in general:

```dotty
f andThen g != g andThen f
```

The order of functions matters!

c) **Existence of an identity element**:

```dotty
f andThen identity == f == identity andThen f
```

Where `identity` is the [identity function](https://ru.wikipedia.org/wiki/%D0%A2%D0%BE%D0%B6%D0%B4%D0%B5%D1%81%D1%82%D0%B2%D0%B5%D0%BD%D0%BD%D0%BE%D0%B5_%D0%BE%D1%82%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5): 
`def identity[A](a: A): A = a`.

## Practical Applications

### Data Processing Pipelines

```dotty
val cleanInput: String => String = _.trim
val parseNumber: String => Option[Int] = s => Try(s.toInt).toOption
val validate: Int => Option[Int] = n => if (n > 0) Some(n) else None
val double: Int => Int = _ * 2

val process = cleanInput andThen parseNumber andThen (_.flatMap(validate)) andThen (_.map(double))

process(" 10 ") // Some(20)
process("-5")   // None
```

### Building a DSL

```dotty
case class Query(filters: Map[String, String], limit: Int)

val addFilter: (String, String) => Query => Query =
  (key, value) => query => query.copy(filters = query.filters + (key -> value))

val setLimit: Int => Query => Query =
  limit => query => query.copy(limit = limit)

// Composing query transformers
val buildQuery = (addFilter("status", "active")(_))
  .andThen(setLimit(10))

val initial = Query(Map.empty, 0)
buildQuery(initial) // Query(Map("status" -> "active"), 10)
```

## Exercise 1.7.1: Basic Operations

> [Repository with exercises and instructions][sample]

**Implement a transformation chain**:

1. Convert a string to an integer (use `.toIntOption` with default value `0`)
2. Increase the number by `10`
3. Convert the result back to a string (use `.toString`)

```dotty
val solution: String => String = ???
```

**Requirements**:

- Use function composition (`andThen` or `compose`)
- Handle the basic case (empty string should not cause an error)

**Usage examples**:

```dotty
solution("5")    // "15"
solution("-3")   // "7"
solution("")     // "10"
```

@:note(🔍 Show Solution)

```dotty
val toNumber: String => Int =
  _.toIntOption.getOrElse(0)

val addTen: Int => Int = _ + 10

val solution: String => String =
  toNumber andThen addTen andThen (_.toString)
```

@:@

## Exercise 1.7.2: Safe Composition

**Task**: Create a composition that:

1. Parses a string into `Double` (on invalid input, use default value `0.0`)
2. Computes the square root (handle negative numbers, use default value `0.0` instead)
3. Converts the result to a string with 2 decimal places

```dotty
val solution2: String => String = ???
```

**Requirements**:

- Each step must be a separate function
- Result format: `"Result: X.XX"`
- On error, use default value `0.0`

**Usage examples**:

```dotty
solution2("16.0")  // "Result: 4.00"
solution2("-4.0")  // "Result: 0.00"
solution2("abc")   // "Result: 0.00"
```

@:note(🔍 Show Solution)

```dotty
val parseDouble: String => Double =
  _.toDoubleOption.getOrElse(0.0)

val sqrtSafe: Double => Double =
  x => if x >= 0 then math.sqrt(x) else 0.0

val formatResult: Double => String =
  x => f"Result: $x%.2f"

val solution2: String => String =
  parseDouble andThen
    sqrtSafe andThen
    formatResult
```

@:@

## Conclusion

Function composition is a powerful tool that enables:

- Building complex logic from simple components
- Creating readable and expressive code
- Easily modifying and reusing components

**Recommendations for use:**

1. Break complex operations into simple functions
2. Use `andThen` for sequential transformations
3. Follow the single responsibility principle for composable functions

---

**Links:**

- [Link to repository with exercises][sample]
- [Harrison - Introduction to Functional Programming](https://funprog-ru.github.io/)
- [Function Composition in Scala - Baeldung](https://www.baeldung.com/scala/function-composition)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)

[sample]: https://gitverse.ru/scalabook/fp
