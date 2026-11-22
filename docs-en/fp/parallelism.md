# Parallelism

## Parallelism with Pure Functions

Pure functions without side effects are ideal for parallel computations, 
since they can be freely combined and executed in any order 
without risk of race conditions or other parallelism issues.

Let's consider the operation of summing numbers:

```dotty
def sum(ints: Seq[Int]): Int =
  ints.foldLeft(0)((a, b) => a + b)
```

This implementation:

1. Uses strictly sequential computation via `foldLeft`
2. Traverses the collection strictly from left to right
3. At each step, performs an addition operation with the accumulator
4. Has time complexity O(n), where n is the collection size

The main drawback is the inability to parallelize due to the strictly sequential nature of `foldLeft`.

An alternative approach is the "divide and conquer" algorithm:

```dotty
def sum(ints: IndexedSeq[Int]): Int =
  if ints.length <= 1 then
    ints.headOption.getOrElse(0)
  else
    val (l, r) = ints.splitAt(ints.length / 2)
    sum(l) + sum(r)

sum(IndexedSeq(1, 2, 3, 4, 5))
// 15
```

Key features of this approach:

1. The collection is split into two parts via `splitAt`
2. Each part is processed recursively and independently
3. Results are combined via simple addition
4. `IndexedSeq` is used for efficient splitting
5. Recursion depth is log₂(n)

The main advantage is the natural ability to parallelize recursive calls `sum(l)` and `sum(r)`, since they:

1. Are independent of each other
2. Have no shared mutable state
3. Can be executed in different threads

## Data Type for Parallel Computations

For safe parallel computations in a functional style, we define a special type `Par[A]`, 
representing a deferred parallel computation that returns a value of type `A`.

Basic API:

```dotty
opaque type Par[A] = ???

object Par:
  def unit[A](a: A): Par[A] =
    ???
    
  def fork[A](pa: => Par[A]): Par[A] =
    ???
    
  def lazyUnit[A](a: => A): Par[A] =
    fork(unit(a))

  extension [A](pa: Par[A])
    def run: A =
      ???
      
    def map2[B, C](pb: Par[B])(f: (A, B) => C): Par[C] =
      ???
```

Detailed description of operations:

1. `unit(a: A): Par[A]`  
    - Converts a value into the parallel context
    - Doesn't perform actual computations until `run` is called
    - Guarantees the value will be available when needed

2. `fork(a: => Par[A]): Par[A]`  
    - Marks a computation for parallel execution
    - Accepts a call-by-name parameter
    - Doesn't start computation immediately
    - Schedules execution in a separate thread (or thread pool)

3. `lazyUnit(a: => A): Par[A]`  
    - Combination of `unit` and `fork`
    - Allows deferring computation and executing it in parallel
    - Useful for wrapping side effects

4. `run: A`  
    - Starts the parallel computation
    - Blocks the current thread until completion
    - Returns the result of the computation
    - Manages the thread pool and synchronization

5. `map2[B, C](pb: Par[B])(f: (A, B) => C): Par[C]`  
    - Combines two parallel computations
    - Applies function `f` to the results
    - Computations can run in parallel
    - Preserves the order of applying the function

Example implementation of parallel sum:

```dotty
def sum(ints: IndexedSeq[Int]): Par[Int] =
  if ints.size <= 1 then
    Par.unit(ints.headOption.getOrElse(0))
  else
    val (l, r) = ints.splitAt(ints.size / 2)
    Par.fork(sum(l)).map2(Par.fork(sum(r)))(_ + _)
```

Key points of the implementation:

1. The base case handles empty and single-element collections
2. Splits the collection into roughly equal parts
3. Recursively computes sums for the parts
4. Explicitly marks parallelism with `fork`
5. Combines results via `map2`

## Laws of Parallel Computations

For correct operation of parallel computations, certain laws must be satisfied.

The main law (conservation law):

```dotty
unit(x).map(f) == unit(f(x))
```

This means that:

1. Lifting a value and subsequently applying a function is equivalent to lifting the result of applying the function
2. Guarantees preservation of semantics during parallel execution

## Exercises

### Exercise 8.1.1: Parallel Computation of Product of Numbers

> [Repository with exercises and instructions for completion][sample]

**Task:**

Implement parallel computation of the product of numbers in a collection, using the "divide and conquer" approach. 

```dotty
def product(ints: IndexedSeq[Int]): Par[Int] =
  ???
```

**Examples**:

```dotty
product(IndexedSeq(1, 2, 3, 4, 5)).run(service).get()
// Result: 120

product(IndexedSeq.empty[Int]).run(service).get()
// Result: 1 (identity element for multiplication)
```  

@:note(🔍 Show Solution)

The solution implements parallel computation of the product of numbers in a collection by "divide and conquer" strategy: 
for an empty sequence, the identity element `1` is returned; for a single element — the element itself; 
and for larger collections, the array is recursively split in half, 
after which the products of the halves are computed in parallel (via `fork`) and combined via multiplication (via `map2`). 

```dotty
def product(doubles: IndexedSeq[Int]): Par[Int] =
  if doubles.size <= 1 then
    Par.unit(doubles.headOption.getOrElse(1))
  else
    val (l, r) = doubles.splitAt(doubles.size / 2)
    Par.fork(product(l)).map2(Par.fork(product(r)))(_ * _)
```

@:@

### Exercise 8.1.2: Parallel Search for Maximum Element

**Task:**  

Implement a function for parallel search for the maximum element in a collection of integers. 
The function should return `Option[Int]` to correctly handle empty collections.

```dotty
def max(ints: IndexedSeq[Int]): Par[Option[Int]] =
  ???
```

**Requirements:**

- Handle empty collections (return `None`)
- For non-empty collections, apply recursive splitting
- Use `Math.max` for comparing elements
- Preserve `Option` semantics when combining results

**Examples:**

```dotty
max(IndexedSeq(3, 1, 4, 2)).run(service).get() // Some(4)
max(IndexedSeq.empty[Int]).run(service).get()  // None
max(IndexedSeq(42)).run(service).get()         // Some(42)
```

@:note(🔍 Show Solution)

The solution implements parallel search for the maximum element in a collection: 
for collections of size ≤ 1, `headOption` returns the element, 
and for larger arrays, the sequence is recursively split in half, 
after which the maximums in each half are computed in parallel (via `fork`), 
and results are combined via `map2` with handling all `Option` variants 
(comparing values via `Math.max`, preserving non-empty values when `None` is present). 

```dotty
def max(ints: IndexedSeq[Int]): Par[Option[Int]] =
  if ints.size <= 1 then
    Par.unit(ints.headOption)
  else
    val (l, r) = ints.splitAt(ints.size / 2)
    Par.fork(max(l)).map2(Par.fork(max(r))) {
      case (Some(a), Some(b)) => Some(Math.max(a, b))
      case (a, None)          => a
      case (None, b)          => b
    }    
```

@:@

### Exercise 8.1.3: Parallel Condition Check for All Elements

**Task:**  

Implement a function for parallel checking that all elements of a collection satisfy a given predicate.

```dotty
def all[A](xs: IndexedSeq[A])(p: A => Boolean): Par[Boolean] =
  ???
```

**Examples:**

```dotty
val isEven = (x: Int) => x % 2 == 0

all(IndexedSeq(2, 4, 6))(isEven).run(service).get()   // true
all(IndexedSeq(2, 3, 4))(isEven).run(service).get()   // false
all(IndexedSeq.empty[Int])(isEven).run(service).get() // true
```

@:note(🔍 Show Solution)

The `all` function checks that all elements of collection `xs` satisfy predicate `p`, using parallel computations: 
if the collection is small (size ≤ 1), the result is computed synchronously via `forall`; otherwise, the collection is split in half, 
and the check is performed recursively for each half in parallel threads using `fork`, 
after which results are combined via `map2` with logical AND (`&&`).

```dotty
def all[A](xs: IndexedSeq[A])(p: A => Boolean): Par[Boolean] =
  if xs.size <= 1 then
    Par.unit(xs.headOption.forall(p))
  else
    val (l, r) = xs.splitAt(xs.size / 2)
    Par.fork(all(l)(p)).map2(Par.fork(all(r)(p)))(_ && _) 
```

@:@

### Exercise 8.1.4: Parallel Collection Transformation (map)

**Task:**  

Implement function `parMap` — a parallel analog of the standard `map` operation, 
which applies a transformation function to each element of a collection.

```dotty
def parMap[A, B](xs: IndexedSeq[A])(f: A => B): Par[IndexedSeq[B]] =
  ???
```

**Examples:**

```dotty
val increment = (x: Int) => x + 1

parMap(IndexedSeq(1, 2, 3))(increment).run(service).get()   // IndexedSeq(2, 3, 4)
parMap(IndexedSeq.empty[Int])(increment).run(service).get() // IndexedSeq()
parMap(IndexedSeq(10))(increment).run(service).get()        // IndexedSeq(11)
```

@:note(🔍 Show Solution)

```dotty
def parMap[A, B](xs: IndexedSeq[A])(f: A => B): Par[IndexedSeq[B]] =
  if xs.size <= 1 then
    Par.unit(xs.map(f))
  else
    val (l, r) = xs.splitAt(xs.size / 2)
    Par.fork(parMap(l)(f)).map2(Par.fork(parMap(r)(f)))(_ ++ _)
```

@:@

### Exercise 8.1.5: Parallel reduce

**Task:**  

Implement function `reduce` — a parallel analog of the standard fold operation, 
which combines elements of a collection using an associative binary operation.

```dotty
def reduce[A](xs: IndexedSeq[A])(op: (A, A) => A): Par[Option[A]] =
  ???
```

**Examples:**

```dotty
val sumOp = (a: Int, b: Int) => a + b

reduce(IndexedSeq(1, 2, 3, 4))(sumOp).run(service).get() // Some(10)
reduce(IndexedSeq.empty[Int])(sumOp).run(service).get()  // None
reduce(IndexedSeq(5))(sumOp).run(service).get()          // Some(5)
```

@:note(🔍 Show Solution)

The `reduce` function parallelly folds collection `xs` with operation `op`: 
if the collection is empty, `None` is returned; if it contains one element — `Some(xs.head)`; 
otherwise, the collection is split in half, and each part is processed recursively in parallel threads via `fork`, 
after which results are combined via `map2`, 
applying `op` to values inside `Option` or preserving the remaining value 
if one of the variants is `None`.

```dotty
def reduce[A](xs: IndexedSeq[A])(op: (A, A) => A): Par[Option[A]] =
  if xs.isEmpty then Par.unit(None)
  else if xs.size == 1 then Par.unit(Some(xs.head))
  else
    val (l, r) = xs.splitAt(xs.size / 2)
    Par.fork(reduce(l)(op)).map2(Par.fork(reduce(r)(op))) {
      case (Some(a), Some(b)) => Some(op(a, b))
      case (a, None)          => a
      case (None, b)          => b
    }
```

@:@

---

**References:**

- [Link to the exercises repository][sample]
- [Functional Programming in Scala, Second Edition, Chapter 7](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
