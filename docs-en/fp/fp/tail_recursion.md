# Tail Recursion

## Introduction to Recursion

Recursion in programming is a technique where a function calls itself to solve a problem.
In Scala and other functional languages, recursion plays a key role,
often replacing iterative constructs (loops) from imperative languages.

There are two main types of recursion:

1. **Head recursion** — the recursive call is not the last operation,
    and additional computations are performed with the result afterward.
2. **Tail recursion** — the recursive call is the last operation in the function,
    with no further computations needed on its result.

### The Problem with Head Recursion

The main problem with head recursion is the potential for stack overflow.
Each recursive call adds a new frame to the stack,
and with deep recursion, this can lead to a `StackOverflowError`.

**Head recursion example**:

```dotty
def factorial(n: Int): Int =
  if n <= 1 then 1
  // Not tail recursion — multiplication happens after the recursive call
  else n * factorial(n - 1)
```

## Tail Recursion in Detail

### How It Works

Tail recursion enables Scala's compiler to perform **Tail Call Optimization (TCO)**.
Instead of adding a new stack frame for each recursive call,
the compiler can reuse the current frame, effectively turning recursion into a loop.

**Key characteristics of tail recursion**:

1. The recursive call must be the last operation in the function
2. An accumulator is often used to collect the result
3. Requires the `@tailrec` annotation for compiler verification that the recursion is indeed tail-recursive

### Example: Computing List Length

Let's consider a more detailed example of computing list length.

#### Head Recursion (Problematic Version)

```dotty
def recursiveLength(list: List[String]): Long =
  list match
    case Nil       => 0
    case _ :: tail => 1 + recursiveLength(tail)
    
recursiveLength((1 to 100000).map(_.toString).toList)
// java.lang.StackOverflowError
//   at scala.collection.immutable.List.equals(List.scala:613)
//   at rs$line$1$.recursiveLength(rs$line$1:3)
//   at rs$line$1$.recursiveLength(rs$line$1:4)
//   at rs$line$1$.recursiveLength(rs$line$1:4)
//   ...
```

**Problems**:

1. After the recursive call `recursiveLength(tail)`, the addition operation (`1 + ...`) is performed
2. Each call saves information on the stack about needing to return and add `1`
3. For large lists (e.g., 100,000 elements), this will cause a stack overflow

#### Tail Recursion (Optimized Version)

```dotty
import scala.annotation.tailrec

@tailrec
def tailRecursiveLength(list: List[String], accumulator: Long = 0): Long =
  list match
    case Nil       => accumulator
    case _ :: tail => tailRecursiveLength(tail, accumulator + 1)

tailRecursiveLength((1 to 100000).map(_.toString).toList)
// val res0: Long = 100000
```

**Advantages**:

1. The recursive call is the last operation
2. The accumulator (`accumulator`) stores the intermediate result
3. The compiler can optimize this into an equivalent loop
4. Works for arbitrarily large lists without stack overflow

### How the Optimization Works

Let's see how each version executes for the list `List("a", "b", "c")`:

**Head recursion**:

```
recursiveLength(["a", "b", "c"])
1 + recursiveLength(["b", "c"])
1 + (1 + recursiveLength(["c"]))
1 + (1 + (1 + recursiveLength([])))
1 + (1 + (1 + 0)))
1 + (1 + 1)
1 + 2
3
```

Each step requires preserving the context for subsequent addition.

**Tail recursion**:

```
tailRecursiveLength(["a", "b", "c"], 0)
tailRecursiveLength(["b", "c"], 1)
tailRecursiveLength(["c"], 2)
tailRecursiveLength([], 3)
3
```

Here, each step completes fully before the next call, enabling stack frame reuse.

## Practical Examples

### Example 1: Factorial

**Head recursion**:

```dotty
def factorial(n: Int): Int =
  if n <= 1 then 1
  // Multiplication after recursive call
  else n * factorial(n - 1)
```

**Tail recursion**:

```dotty
@tailrec
def factorialTailRec(n: Int, accumulator: Int = 1): Int =
  if n <= 1 then accumulator
  // All computations before the call
  else factorialTailRec(n - 1, n * accumulator)
```

### Example 2: Fibonacci Numbers

**Head recursion (inefficient)**:

```dotty
def fib(n: Int): Int =
  if n <= 1 then n
  // Two recursive calls + addition
  else fib(n - 1) + fib(n - 2)
```

**Tail recursion (efficient)**:

```dotty
@tailrec
def fibTailRec(n: Int, a: Int = 0, b: Int = 1): Int =
  if n == 0 then a
  // Tail call with state transfer
  else fibTailRec(n - 1, b, a + b)
```

## Working with the Compiler

### The @tailrec Annotation

The `@tailrec` annotation is a contract with the compiler:

1. The compiler verifies that the recursion is truly tail-recursive
2. If not — it produces a compilation error
3. If yes — it applies the optimization

**Example of incorrect usage**:

```dotty
@tailrec // Will cause a compilation error
def sum(list: List[Int]): Int =
  list match
    case Nil    => 0
    case h :: t => h + sum(t) // Addition after recursive call

// -- Error: ----------------------------------------------------------------------
// 5 |    case h :: t => h + sum(t)
//   |                       ^^^^^^
//   |                 Cannot rewrite recursive call: it is not in tail position
// 1 error found
```

### Common Errors When Attempting Tail Recursion

1. **Computations after the recursive call**:
    ```dotty
    @tailrec
    def foo(n: Int): Int =
      if n == 0 then 0
      else foo(n - 1) + 1 // Error: addition after call
    ```

2. **Non-direct tail call**:
    ```dotty
    @tailrec
    def bar(n: Int): Int =
      if n == 0 then 0
      else bar(bar(n - 1))  // Error: nested call
    ```

3. **Missing base case**:
    ```dotty
    @tailrec
    def infinite(n: Int): Int = infinite(n + 1)  // Technically tail-recursive, but infinite
    ```

## Advanced Techniques

### Transforming to Tail Recursion

Not all algorithms are obviously expressible via tail recursion.
Sometimes refactoring is required:

**Example: tree traversal**

```dotty
sealed trait Tree
case class Node(value: Int, left: Tree, right: Tree) extends Tree
case object Leaf                                     extends Tree

// Head recursion
def sumTree(tree: Tree): Int = tree match
  case Leaf          => 0
  case Node(v, l, r) => v + sumTree(l) + sumTree(r)

// Tail recursion with an explicit stack
def sumTreeTailRec(tree: Tree): Int =
  @tailrec
  def loop(remaining: List[Tree], acc: Int): Int = remaining match
    case Nil                   => acc
    case Leaf :: tail          => loop(tail, acc)
    case Node(v, l, r) :: tail => loop(l :: r :: tail, acc + v)

  loop(List(tree), 0)
```

## Performance

### Comparison with Iterative Solutions

After TCO optimization, tail recursion is performance-equivalent to iterative solutions:

```dotty
// Iterative version
def lengthIterative(list: List[String]): Long =
  var acc     = 0L
  var current = list
  while current.nonEmpty do
    acc += 1
    current = current.tail
  acc

// Tail recursion (after compilation, will be similar to iterative)
@tailrec
def lengthTailRec(list: List[String], acc: Long = 0): Long =
  list match
    case Nil       => acc
    case _ :: tail => lengthTailRec(tail, acc + 1)
```

### JVM Limitations

On the JVM, there are some limitations for TCO (_Tail Call Optimization_):
full optimization only works for direct tail calls within a single method.

## Conclusion

Tail recursion is a powerful tool in Scala that:

1. Prevents stack overflow
2. Preserves the declarative style of functional programming
3. Delivers performance comparable to iterative solutions

**Usage recommendations**:

1. Always use `@tailrec` for public recursive methods
2. Transform head recursion to tail recursion using accumulators
3. For complex data structures (trees, graphs), use explicit state management (stack/queue)
4. Benchmark performance in edge cases

Tail recursion is not just an optimization,
but a way of thinking about recursive algorithms where state is explicitly passed through parameters,
not implicitly through the call stack.

## Exercises

### Exercise 1.4.1: Finding the Maximum Element in a List

> [Repository with exercises and instructions][sample]

**Task**:

Write a tail-recursive function that finds the maximum element in a list.

```dotty
def maxTailRec(list: List[Int]): Option[Int] =
  @annotation.tailrec
  def loop(???): Int =
    ???

  ???
```

**Examples**:

```dotty
maxTailRec(List.empty)          // None
maxTailRec(List(1))             // Some(1)
maxTailRec(List(1, 2, 3, 4, 5)) // Some(5)
```

@:note(🔍 Show Solution)

The function `maxTailRec` finds the maximum element in a list using tail recursion:
the inner function `loop` sequentially compares list elements with the current maximum (`currentMax`),
updating it when a larger value is found,
while the outer function
handles the empty list (returning `None`)
and starts the recursion with the first element as the initial maximum (returning the result in `Some`).
The `@tailrec` annotation ensures the recursion will be optimized into a loop.

```dotty
def maxTailRec(list: List[Int]): Option[Int] =
  @annotation.tailrec
  def loop(remaining: List[Int], currentMax: Int): Int =
    remaining match
      case Nil => currentMax
      case head :: tail =>
        val newMax =
          if head > currentMax then head
          else currentMax
        loop(tail, newMax)

  list match
    case Nil          => None
    case head :: tail => Some(loop(tail, head))
```

@:@

### Exercise 1.4.2: Counting Element Occurrences in a List

**Task**:

Write a tail-recursive function that counts how many times an element appears in a list.

```dotty
def countOccurrences[A](list: List[A], target: A): Int =
  ???
```

**Examples**:

```dotty
countOccurrences(List.empty, 1)             // 0
countOccurrences(List(1, 2, 3), 1)          // 1
countOccurrences(List(1, 2, 3, 2, 4, 2), 2) // 3
```

@:note(🔍 Show Solution)

The function `countOccurrences` counts the number of occurrences of element `target`
in list `list` using tail recursion:
the inner function `loop` iterates through the list, incrementing the accumulator `acc` by 1 when an element matches `target`,
and returns the final accumulator value after processing the entire list.
The `@tailrec` annotation ensures the recursion is optimized into a loop.

```dotty
def countOccurrences[A](
    list: List[A],
    target: A
): Int =
  @annotation.tailrec
  def loop(remaining: List[A], acc: Int): Int =
    remaining match
      case Nil => acc
      case head :: tail =>
        val newAcc =
          if head == target then acc + 1
          else acc
        loop(tail, newAcc)

  loop(list, 0)
```

@:@

### Exercise 1.4.3: Checking if a Number is Prime

**Task**:

Write a tail-recursive function that checks whether a number is prime.

**Hint**:

- A prime number is divisible only by `1` and itself.
- It is sufficient to check divisors from `2` to `sqrt(n)`.

```dotty
def isPrime(n: Int): Boolean =
  ???
```

**Examples**:

```dotty
isPrime(11) // true
isPrime(12) // false
```

@:note(🔍 Show Solution)

The function `isPrime` checks whether number `n` is prime, using tail recursion:
the inner function `checkDivisor` sequentially checks divisors from `2` up to the square root of `n`,
returning `false` when a divisor is found or `true` if no divisors are found.
For numbers ≤ 1, `false` is returned immediately,
and the `@tailrec` annotation ensures the recursive calls are optimized into a loop.

```dotty
def isPrime(n: Int): Boolean =
  @annotation.tailrec
  def checkDivisor(
      current: Int,
      maxDivisor: Int
  ): Boolean =
    if current > maxDivisor then true
    else if n % current == 0 then false
    else checkDivisor(current + 1, maxDivisor)

  if n <= 1 then false
  else checkDivisor(2, math.sqrt(n).toInt)
```

@:@

### Exercise 1.4.4: Reversing a List

**Task**: Write a tail-recursive function that reverses a list.

**Regular recursion**:

```dotty
def reverse[A](list: List[A]): List[A] =
  list match
    case Nil          => Nil
    case head :: tail => reverse(tail) :+ head
```

**Problem**: `:+` (append to end) is inefficient for lists.

**Task**: Rewrite using tail recursion and an accumulator.

```dotty
def reverseTailRec[A](list: List[A]): List[A] =
  ???
```

**Examples**:

```dotty
reverseTailRec(List.empty)          // List()
reverseTailRec(List(1))             // List(1)
reverseTailRec(List(1, 2, 3, 4, 5)) // List(5, 4, 3, 2, 1)
```

@:note(🔍 Show Solution)

The function `reverseTailRec` reverses a list using tail recursion:
the inner function `loop` sequentially moves elements from the source list (`remaining`) into the accumulator (`acc`),
adding each current element to the head of the accumulator, which ultimately gives the reversed order.
The `@tailrec` annotation ensures the recursion is optimized into a loop,
and the initial call `loop(list, Nil)` starts the process with an empty accumulator.

```dotty
def reverseTailRec[A](list: List[A]): List[A] =
  @annotation.tailrec
  def loop(remaining: List[A], acc: List[A]): List[A] =
    remaining match
      case Nil          => acc
      case head :: tail => loop(tail, head :: acc)

  loop(list, Nil)
```

@:@

---

**Links:**

- [Link to repository with exercises][sample]
- [Recursive Types. Part 1/5. Recursion](https://habr.com/ru/articles/863304/)
- [Recursive Types. Part 2/5. Fixed Points of Type Constructors](https://habr.com/ru/articles/863324/)
- [Recursive Types. Part 3/5. Free Containers](https://habr.com/ru/articles/863334/)
- [Recursive Types. Part 4/5. Recursion Schemes](https://habr.com/ru/articles/863362/)
- [Recursive Types. Part 5/5. Fun Calculus](https://habr.com/ru/articles/863376/)
- [Harrison - Introduction to Functional Programming](https://funprog-ru.github.io/)
- [Deriving Tail Recursive Fibonacci](https://systemfw.org/posts/tailrec.html)
- [Functional Programming by Alexander Alvin](https://alvinalexander.com/photos/functional-programming-simplied-free-pdf-preview/)
- [Functional Programming in Scala, Second Edition, Chapter 1-2 by Michael Pilquist, Rúnar Bjarnason, and Paul Chiusano](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Introduction to Functional Programming - Bird, Wadler](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Scala3 book](https://docs.scala-lang.org/scala3/book/fp-what-is-fp.html)
- [Tail Recursive Functions in Scala](https://alexn.org/blog/2021/01/26/tail-recursive-functions-in-scala/)
- [Tail Recursion in Scala - Baeldung](https://www.baeldung.com/scala/tail-recursion)

[sample]: https://gitverse.ru/scalabook/fp
