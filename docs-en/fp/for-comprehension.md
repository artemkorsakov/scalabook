# For-Comprehension

Imagine you need to work with values wrapped in containers (e.g., `List`, `Option`).
These containers say: "I have a value, but to work with it, you need to first 'extract' it from the container."

**For-comprehension** is a special syntax in Scala that makes working with such containers convenient and readable.
It is "syntactic sugar" -- a more pleasant way to write chains of `map`, `flatMap`, and `filter` calls.

## Simplest Example: One Generator

Suppose we have a list of numbers, and we want to multiply each by 2:

```dotty
for x <- List(1, 2, 3) // "extract" each element from the list
yield x * 2 // transform each element
// Result: List(2, 4, 6)
```

This is equivalent to calling:

```dotty
List(1, 2, 3).map(x => x * 2)
```

Here `x <- List(1, 2, 3)` works like `map` -- applying a function to each element.

## Multiple Generators: Combinations

Now imagine we want to get all possible pairs from two lists:

```dotty
for
  number <- List(1, 2)     // take a number from the first list
  letter <- List('a', 'b') // for EACH number, take all letters
yield (number, letter) // construct a pair
// Result: List((1,'a'), (1,'b'), (2,'a'), (2,'b'))
```

Equivalent code without for-comprehension:

```dotty
List(1, 2).flatMap: number =>
  List('a', 'b').map: letter =>
    (number, letter)
```

The key point: the second generator `letter <- List('a', 'b')` runs for **each** element from the first generator.
This works like a nested loop.

## Filtering: Adding Conditions

We can filter values directly inside for-comprehension:

```dotty
for
  x <- 1 to 10 // take numbers from 1 to 10
  if x % 2 == 0 // keep only even numbers
yield x * x // square them
// Result: Vector(4, 16, 36, 64, 100)
```

The `if` condition filters out elements that do not satisfy the condition.

## Practical Utility: Working with Option

`Option` is a container that may hold a value (`Some(value)`) or be empty (`None`).
For-comprehension is ideal for sequential work with multiple `Option`s:

```dotty
def getUser(id: Int): Option[String] =
  if id == 123 then Some("Anna") else None // may return Some or None

def getAge(name: String): Option[Int] =
  if name == "Anna" then Some(30) else None

for
  name <- getUser(123) // if getUser returns None,
  age  <- getAge(name) // then getAge won't even be called
yield s"$name is $age years old"
// Result: Some("Anna is 30 years old")
```

**Here is what makes this code powerful:**

- If `getUser(123)` returns `None`, computation stops and the entire result is `None`
- If `getAge(name)` returns `None`, the result is also `None`
- Only if **all** operations return `Some` does the `yield` block execute

This spares you from multi-level checks like:

```dotty
// Without for-comprehension (less readable)
getUser(123).flatMap(name =>
  getAge(name).map(age => s"$name is $age years old")
)
```

## How Does It Work Internally?

Scala transforms for-comprehension into a chain of calls:

1. `x <- collection` -> `.flatMap(x => ...)`
2. Final `yield` -> `.map(...)`
3. `if condition` -> `.withFilter(x => condition)`

For example:

```dotty
for
  a <- listA
  b <- listB
  if condition(a, b)
  c <- listC
yield f(a, b, c)
```

Transforms into:

```dotty
listA.flatMap(a =>
  listB.withFilter(b => condition(a, b))
    .flatMap(b =>
      listC.map(c => f(a, b, c))
    )
)
```

## What Types Can Be Used?

Any type that has these methods:

- `flatMap` -- for extracting values (`<-`)
- `map` -- for transforming (`yield`)
- `withFilter` -- for filtering (`if`)

This includes: `List`, `Option`, `Try`, `Future`, `Either`, and many others.

## When to Use?

**Use for-comprehension when:**

- Working with multiple nested containers
- Need to combine values from different sources
- Want to improve readability of `map`/`flatMap` chains

**Do not use when:**

- Performing a single simple operation (one `map` is sufficient)
- Working with types that don't support `flatMap`/`map`

## Summary

For-comprehension is:

- ✅ A readable way to work with nested computations
- ✅ Relief from "hellish" nesting of `map`/`flatMap`
- ✅ A unified syntax for different container types
- ✅ Elegant handling of missing values (`Option`, `Try`)

Start using for-comprehension when you feel that `map`/`flatMap` chains become too complex to parse.

## Exercises

### Exercise 4.1.1: Nested Structures

> [Repository with exercises and instructions][sample]

Given a list of lists (a 3x2 matrix):

```dotty
val matrix = List(List(1, 2), List(3, 4), List(5, 6))  
```

Write a for-comprehension that transforms this matrix into a flat list, extracting all elements in order.

**Expected result**:

```dotty
List(1, 2, 3, 4, 5, 6)
```  

@:note(T🔍 Show Solution)

This for-comprehension transforms the nested list `matrix` into a flat one, using two generators: 
the first (`row <- matrix`) sequentially extracts each sublist, 
and the second (`num <- row`) iterates over the elements of the current sublist, 
after which `yield num` collects all elements into a flat list. 

```dotty
for
  // Iterate over the outer list 
  // (get List(1,2), then List(3,4), etc.)
  row <- matrix  
  // Iterate over each inner list   
  num <- row   
// Return each element individually       
yield num           
```

@:@

### Exercise 4.1.2: Own Type for For-Comprehension

Given the class:

```dotty
case class Box[A](value: A)  
```

Make it so that `Box` can be used with for-comprehension.
To do this, implement the `map` and `flatMap` methods,
which are part of the for-comprehension contract in Scala.

**Example**:

```dotty
val result =
  for
    a <- Box(2)
    b <- Box(3)
  yield a + b
// Expected result: Box(5)
```  

@:note(T🔍 Show Solution)

For for-comprehension to work with type `Box[A]`, we need to implement:

1. `map` - transforms the value inside `Box` using a function `A => B`
2. `flatMap` - allows sequentially composing operations while preserving the `Box` context

```dotty
case class Box[A](value: A):
  def map[B](f: A => B): Box[B] = 
    Box(f(value))
  def flatMap[B](f: A => Box[B]): Box[B] = 
    f(value)
```

Explanation:

- `map` creates a new `Box`, applying function `f` to the value
- `flatMap` applies a function that already returns `Box`, and "unwrap"s the result
- These two methods are sufficient for basic for-comprehension (without filtering)

@:@

### Exercise 4.1.3: Type Compatibility in For-Comprehension

Consider two variants of code:

**Variant 1**:

```dotty
val a = Option(1)
val b = List(1, 2, 3)

val c =
  for
    x <- a  // Generator of Option
    y <- b  // Generator of List
  yield x + y
```

**Variant 2** (generators swapped):

```dotty
val c =
  for
    y <- b  // Generator of List
    x <- a  // Generator of Option
  yield x + y
```

**Questions**:

1. What type and value will `c` be in the first variant?
2. What type and value will `c` be in the second variant?

@:note(T🔍 Show Solution)

**Answer 1**: The first variant **will not compile**.

**Reason**:

- Equivalent code: `a.flatMap(x => b.map(y => x + y))`
- `Option.flatMap` expects a function `A => Option[B]`, but `b.map` returns `List[Int]`
- There is no implicit conversion `List` -> `Option`

**Answer 2**: The second variant will give `List(2, 3, 4)`.

**Reason**:

- Equivalent code: `b.flatMap(y => a.map(x => x + y))`
- `List.flatMap` expects `A => IterableOnce[B]`
- `Option` is implicitly converted to `IterableOnce`
- Result: for each `y` in the list, `x` is taken from `Option(1)`

**Additional Question**:

How to make the first variant work (when `Option` comes first)?

**Answer 3**: Explicitly convert `Option` to a collection

```dotty
val c =
  for
    x <- a.toList
    y <- b
  yield x + y
```

@:@

---

**Links:**

- [Repository with exercises][sample]
- [A Comprehensive Guide to For-Comprehension in Scala - Baeldung](https://www.baeldung.com/scala/for-comprehension)

[sample]: https://gitverse.ru/scalabook/fp
