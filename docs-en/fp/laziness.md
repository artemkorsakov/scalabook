# Lazy Evaluation

## The Concept of Strictness and Non-Strictness of Functions

**Non-strictness** (or **lazy evaluation**) is an important property of functions that determines the strategy for evaluating their arguments.

### Strict Functions

- A **strict function** always evaluates **all** its arguments before executing the function body.
- This is the standard behavior for most programming languages, including Scala.
- Unless otherwise specified in Scala, any function is strict by default.

**Example of a strict function:**

```dotty
def square(x: Double): Double = x * x
```

When calling `square(41.0 + 1.0)`:

1. First, the argument is evaluated: `41.0 + 1.0` → `42.0`.
2. Then `square(42.0)` is called, returning `1764.0`.

### Non-Strict Functions

- A **non-strict function** may **not evaluate** some of its arguments (or may not evaluate them immediately).
- This is useful for optimization, working with infinite data structures, or controlling side effects.

## Examples of Non-Strict Evaluation

### 1. Logical Operators `&&` and `||`

These operators **do not evaluate the second argument** if the result is already determined by the first.

#### The `&&` (logical AND) Operator

- The second argument is evaluated **only if the first is `true`**.

```dotty
false && { println("This code won't execute!"); true }  
// Result: false (println is not called)
```

#### The `||` (logical OR) Operator

- The second argument is evaluated **only if the first is `false`**.

```dotty
true || { println("This code won't execute!"); false }  
// Result: true (println is not called)
```

### 2. The `if` Control Construct

In Scala, `if` is non-strict — branches are evaluated **only if the condition leads to their execution**.

```dotty
val input = List(1)
val result = if input.isEmpty then sys.error("Error!") else input
// Result: List(1) (sys.error is not called, since the condition is false)
```

### 3. Call-by-Name Parameters

In Scala, you can explicitly specify that a parameter should be evaluated lazily, using the `=> Type` syntax:

```dotty
def maybeTwice(b: Boolean, i: => Int) = if b then i + i else 0
```

Here, `i` is evaluated **every time it is referenced**. For example:

```dotty
val x = maybeTwice(true, { println("hi"); 1 + 41 })
// Output:
// hi
// hi
// Result: 84 (the block { ... } was evaluated TWICE)
```

### Caching vs. No Caching

It's important to note that **lazy arguments without caching** (as in `maybeTwice`) are recomputed on every reference. 
If you need to evaluate an argument **once**, use `lazy val`:

```dotty
def maybeTwiceCached(b: Boolean, i: => Int) =
  lazy val cachedI = i // Evaluated only on first access
  if b then cachedI + cachedI else 0

val y = maybeTwiceCached(true, { println("hi"); 1 + 41 })
// Output:
// hi (only once)
// Result: 84
```

### Summary

- **Strict functions** are the standard in Scala (arguments are evaluated immediately).
- **Non-strict functions** give control over computations (optimization, deferred operations).
- Examples of non-strictness: `&&`, `||`, `if`, call-by-name parameters (`=> Type`).
- Lazy arguments without caching are evaluated **every time**, while with `lazy val` — **once**.

This is a powerful mechanism, especially useful in functional programming for working with side effects 
and resource-intensive computations.

### Exercise 5.1.1: Lazy if-else

> [Repository with exercises and instructions for completion][sample]

Implement a `lazyIf` function that takes a condition and two lazy blocks, 
but evaluates only one of them based on the condition.

```dotty
def lazyIf[A](cond: Boolean)(onTrue: => A)(onFalse: => A): A = ???
```

**Examples:**

```dotty
val result = lazyIf(2 > 1) { 
  println("True")
  42 
} { 
  println("False")
  0 
}
// Output: "True"
// Result: 42
```

@:note(🔍 Show Solution)

The function should simply return one of the passed blocks without evaluating both in advance.

```dotty
def lazyIf[A](cond: Boolean)(onTrue: => A)(onFalse: => A): A = 
  if cond then onTrue else onFalse
```

@:@

## Lazy Lists (LazyList)

Let's consider how lazy evaluation (non-strictness) can significantly improve the efficiency 
and modularity of functional programs, using lazy lists as an example. 
Unlike ordinary strict lists, transformations in `LazyList` are fused into a single pass thanks to deferred evaluation, 
which provides significant performance advantages.

### Basic LazyList Implementation

Extended definition with explanations:

```dotty
enum LazyList[+A]:
  // Empty list (base case)
  case Empty
  
  // Constructor for non-empty list:
  // - h: lazily evaluated head element
  // - t: lazily evaluated tail of the list
  case Cons(h: () => A, t: () => LazyList[A])

import LazyList.*

object LazyList:
  // Convenient constructor from a sequence of values
  def apply[A](as: A*): LazyList[A] =
    if as.isEmpty then Empty
    else Cons(() => as.head, () => apply(as.tail*))
```

Key features:

1. **Lazy parameters**: Both the head and tail are represented as functions `() => A`, deferring their evaluation
2. **Recursive structure**: Similar to ordinary lists, but with lazy semantics
3. **Type safety**: Covariance (`+A`) allows use of subtypes

### Example: Working with exists

Let's consider in detail how the `exists` method works:

```dotty
def exists(p: A => Boolean): Boolean = this match
  case Cons(h, t) => p(h()) || t().exists(p)  // Lazy OR
  case _          => false
```

How it works:

1. When checking `Cons(h, t)`, `h()` is evaluated first
2. If `p(h()) == true`, thanks to the non-strictness of `||`, the tail `t()` is not evaluated
3. Otherwise — recursive checking continues

**Example with early termination:**

```dotty
// Generate an infinite list of natural numbers: 1, 2, 3, 4, ...
val list = LazyList.from(1) 
list.exists(_ == 5)  
// true - checks only the first five elements
```

### Incremental Transformations

Similarly, implementations of other methods 
are incremental — they don't fully generate their answers.
After some other computation examines elements of the resulting `LazyList`,
the `LazyList` will perform exactly as much work as needed to produce the requested elements.
Because of this incremental nature, functions can be called
in succession without fully building intermediate results.

The lazy transformation of lists also has important implications for memory usage.
Since intermediate lazy lists are not generated,
transforming a lazy list requires exactly as much working memory
as is needed to store and transform the current element.

Key advantages of lazy lists:

**Single data pass**: 

All transformations are fused

```dotty
LazyList(1,2,3,4)
  .map(_ + 1)      // Not evaluated immediately
  .filter(_ > 2)   // Not evaluated immediately
  .take(2)         // Not evaluated immediately 
  .toList          // Work starts here only
// Will execute: 1+1→doesn't match, 2+1→take, 3+1→take
```

With `LazyList` you can construct a computation that produces a sequence of elements
without executing the steps of that computation until those elements are genuinely needed.
More generally, non-strictness allows separating the description of an expression from its evaluation.
This gives a powerful ability: you can describe a "larger" expression than needed, and then compute only its part.

**Memory savings**: Intermediate results are not stored

```dotty
// Requires O(1) extra memory instead of O(n)
(1 to 1000000).to(LazyList).map(_ * 2).filter(_ % 3 == 0).take(10)
```

**Composability**: Can build complex chains without overhead

### Infinite Sequences

Laziness allows working with infinite structures:

```dotty
enum LazyList[+A]:
  ...

  // `take(n)` — lazily returns the first `n` elements of the list, without evaluating the rest. 
  def take(n: Int): LazyList[A] = this match
    case Cons(h, t) if n > 0 => Cons(h, () => t().take(n - 1))
    case _                   => Empty

  // `toList` — converts a lazy list to a strict `List`, forcing evaluation of all elements via recursion.
  def toList: List[A] = this match
    case Cons(h, t) => h() :: t().toList
    case _          => Nil

object LazyList:
  ...
    
  def cons[A](h: => A, t: => LazyList[A]): LazyList[A] =
    Cons(() => h, () => t)
    
// Infinite list of ones
val ones: LazyList[Int] = LazyList.cons(1, ones)

ones.take(5).toList  // List(1, 1, 1, 1, 1)
ones.exists(_ == 1)   // true (checks only the first element)
```

### Performance Optimization

Comparison with strict collections:

| Operation        | LazyList | List  |
|------------------|----------|-------|
| take(10)         | O(1)     | O(n)  |
| exists(p)        | O(k)*    | O(n)  |
| Memory           | O(1)     | O(n)  |

*where k is the position of the first match

**Important warning**: Lazy lists can lead to memory leaks 
if references to the head of a computed list are retained. 
For production code, it's recommended to use the standard `LazyList` implementation from the Scala library.

### Exercise 5.1.2: Lazy Fibonacci

Implement an infinite lazy stream of Fibonacci numbers using `LazyList`. 
Each new number should be computed only upon request.

```dotty
def fib: LazyList[Int] = ???
```

@:note(🔍 Hint)

Use the additional operations **zip**, **map**, and **tail**:

- **zip** — combines two lazy lists pairwise, creating a new **LazyList** of tuples. Works only while both lists are non-empty.
- **map** — applies function **f** to each element **on the fly**, without pre-evaluating the source list.
- **tail** — returns the tail of the list, forcing evaluation of only the first deferred element.  

```dotty
enum LazyList[+A]:
  ...

  def zip[B](that: LazyList[B]): LazyList[(A, B)] =
    (this, that) match
      case (Cons(h1, t1), Cons(h2, t2)) =>
        Cons(() => (h1(), h2()), () => t1().zip(t2()))
      case _ => Empty

  def map[B](f: A => B): LazyList[B] =
    this match
      case Cons(h, t) => Cons(() => f(h()), () => t().map(f))
      case _          => Empty

  def tail: LazyList[A] =
    this match
      case Cons(_, t) => t()
      case _          => Empty
end LazyList
```

@:@

**Examples:**

```dotty
val fib10 = fib.take(10).toList
// Should return: List(0, 1, 1, 2, 3, 5, 8, 13, 21, 34)
```

@:note(🔍 Show Solution)

This solution implements a lazy list (`LazyList`) with basic operations (`take`, `map`, `zip`, `toList`), 
where elements are computed only as needed. 
The feature — lazy generation of the infinite Fibonacci sequence: 
numbers are computed on demand (e.g., `take(10)` builds the first 10 values "on request"), 
which saves memory and computation. 
The implementation uses recursion and deferred evaluation (via `() => A`), 
demonstrating the power of lazy structures for working with potentially infinite sequences.

```dotty
enum LazyList[+A]:
  ...

  def zip[B](that: LazyList[B]): LazyList[(A, B)] =
    (this, that) match
      case (Cons(h1, t1), Cons(h2, t2)) =>
        Cons(
          () => (h1(), h2()), 
          () => t1().zip(t2())
        )
      case _ => Empty

  def map[B](f: A => B): LazyList[B] =
    this match
      case Cons(h, t) => 
        Cons(
          () => f(h()), 
          () => t().map(f)
        )
      case _          => Empty

  def tail: LazyList[A] =
    this match
      case Cons(_, t) => t()
      case _          => Empty
end LazyList

import LazyList.*

def fib: LazyList[Int] =
  val t: () => LazyList[Int] = () =>
    fib.zip(fib.tail).map { case (a, b) => a + b }
  Cons(() => 0, () => Cons(() => 1, t))

val fib10 = fib.take(10).toList
```

@:@

## Corecursion

The `unfold` function represents a powerful functional programming pattern known as corecursion. Let's examine its implementation:

```dotty
def unfold[A, S](state: S)(f: S => Option[(A, S)]): LazyList[A] = f(state) match
  case None         => // terminal case
    Empty 
  case Some((a, s)) => // recursive construction
    Cons(() => a, () => unfold(s)(f))   
```

### Corecursion vs. Recursion

Unlike ordinary recursion, which:

- **Consumes** data, breaking down input values into smaller parts
- **Terminates** when it reaches the base case

Corecursion:

- **Produces** data, generating a sequence of values
- **Can be infinite**, as long as it remains productive
- **Does not require** decreasing input values at each step

### Key Properties of Corecursion

**Productivity**:

- A function can generate elements on demand
- Each new element is created in finite time
- Example: `unfold(1)(s => Some((s, s + 1)))` generates an infinite sequence of natural numbers

**Lazy evaluation**:

```dotty
val naturals = unfold(1)(s => Some((s, s + 1)))
naturals.take(5).toList // List(1, 2, 3, 4, 5)
```

Elements are computed only upon direct access.

### Practical Application

**Working with infinite sequences**:

```dotty
// Random number generator
val randoms = unfold(Random.nextInt(100))(s => Some((s, Random.nextInt(100)))
```

**Streaming data parsing**:

```dotty
def parseStream(input: InputStream): LazyList[Byte] = 
  unfold(input)(in => Some((in.read(), in)).filter(_._1 != -1))
```

**Implementing stateful algorithms**:

```dotty
// UUID generator
val uuids = unfold(UUID.randomUUID())(_ => Some((UUID.randomUUID(), ())))
```

### Terminology

- **Guarded recursion**: another name for corecursion, emphasizing the presence of a continuation condition
- **Cotermination**: the property of stopping only when the generation of new values ceases

Corecursion is particularly useful in combination with lazy data structures, enabling:

- Efficient streaming processors
- Infinite sequence generators
- Stateful transformations in a functional style

### Exercise 5.1.3: Lazy Generation of Fibonacci Numbers

Implement an infinite sequence of Fibonacci numbers using the `unfold` function. 
The sequence should start with 0, 1, 1, 2, 3... 
Each subsequent number is the sum of the two preceding ones.

```dotty
val fibsViaUnfold: LazyList[Int] = ???
```

**Examples:**

```dotty
fibs.take(10).toList // Should return: List(0, 1, 1, 2, 3, 5, 8, 13, 21, 34)
```

@:note(🔍 Show Solution)

The following solution generates an infinite sequence of Fibonacci numbers via `unfold`, 
using a pair `(0, 1)` as the initial state. 
At each step, the function returns the current number `a` from the state and updates the state to `(b, a + b)`, 
which effectively shifts the computation window to the next pair of numbers in the sequence. 
Thanks to the laziness of `unfold`, elements are computed only on access, 
which allows working with an infinite stream without memory overflow. 

```dotty
val fibsViaUnfold: LazyList[Int] = 
  unfold((0, 1)) { case (a, b) =>
    Some((a, (b, a + b)))
  }
```

@:@

## Summary 

- Non-strictness is a useful technique that allows separating concerns and improving modularity. 
- Separating the description of a computation from its execution provides opportunities 
  for reuse and improved efficiency.

---

**References:**

- [Link to the exercises repository][sample]
- [Functional Programming in Scala, Second Edition, Chapter 5](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
