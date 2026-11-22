# Local Effects and Mutable State

## Introduction to the Concept

Functional programming is based on the principle of **referential transparency**, 
which states that an expression can be replaced with its result without changing the program's behavior. 
This leads us to the concept of **pure functions** — functions that 
have no side effects and depend only on their arguments.

However, in real programs it is sometimes necessary to work with mutable state. 
Let's consider how to do this while preserving function purity at the external level.

## Deeper Understanding of Referential Transparency

### Definitions

1. **Referential transparency**:
    - An expression `e` is referentially transparent if, throughout the entire program `p`, every occurrence of `e` 
      can be replaced with the result of its evaluation without changing the meaning of `p`.
    - *In simpler terms*: wherever you see `someExpression`, 
      you can replace it with its result, and the program will behave the same way.

2. **Function purity**:
    - A function `f` is pure if, for all referentially transparent `x`, the expression `f(x)` is also referentially transparent.
    - *In simpler terms*: a pure function always returns the same result 
      for the same arguments and has no side effects.

### Important Clarification

These definitions **do not prohibit** the use of mutation inside a function, if it is not visible from the outside. 
This is a key point to understand.

## Example: Pure Quicksort with Internal Mutation

Let's consider an implementation of quicksort in Scala:

```dotty
def quicksort(xs: List[Int]): List[Int] =
  if xs.isEmpty then xs
  else
    // 1. Create a mutable array from the input list
    val arr = xs.toArray

    // 2. Helper function for swapping elements
    def swap(x: Int, y: Int) =
      val tmp = arr(x)
      arr(x) = arr(y)
      arr(y) = tmp

    // 3. Array partitioning function
    def partition(l: Int, r: Int, pivot: Int) =
      val pivotVal = arr(pivot)
      swap(pivot, r)
      var j = l
      for i <- l until r if arr(i) < pivotVal do
        swap(i, j)
        j += 1
      swap(j, r)
      j

    // 4. Main recursive sorting function
    def qs(l: Int, r: Int): Unit =
      if l < r then
        val pi = partition(l, r, l + (r - l) / 2)
        qs(l, pi - 1)
        qs(pi + 1, r)

    // 5. Start sorting
    qs(0, arr.length - 1)
    // 6. Return the result as an immutable list
    arr.toList
```

### Why is this function pure?

1. **Locality of mutation**:
    - All changes occur inside the `arr` array, which is created inside the function
    - No external part of the program has access to `arr`

2. **Determinism**:
    - For the same input list, the function will always return the same result
    - No dependency on external state

3. **No side effects**:
    - The function doesn't change anything outside its scope
    - Doesn't interact with the outside world (files, network, etc.)

### Component Breakdown

1. **`swap`**:
    - Has a side effect (changes array elements)
    - But is private to `quicksort`
    - Its effects are not visible from the outside

2. **`partition`**:
    - Uses a mutable variable `j`
    - But this variable is visible only inside `partition`
    - All changes remain local

3. **`qs`**:
    - Recursively mutates the array
    - But again — changes are limited to the scope of `quicksort`

## Principles of Working with Local Effects

1. **Encapsulation**:
    - All mutable objects must be created inside the function
    - No references to them should "leak" to the outside

2. **Restricted scope**:
    - Mutation should be visible only within a limited scope
    - Helper functions with effects should be private

3. **Pure interface**:
    - Input parameters must not be modified
    - The result must be a new, immutable value

### When is this useful?

1. **Algorithms requiring mutation**:
    - Sorting (as in the example above)
    - Graph algorithms
    - Performance optimizations

2. **Temporary data structures**:
    - When you need to temporarily use a mutable structure for efficiency
    - But the final result is converted to an immutable form

3. **Gradual migration to functional style**:
    - You can start by encapsulating effects
    - Then gradually move to cleaner solutions

### Conclusions

1. **Pure functions can contain local effects**, if they are properly encapsulated.
2. **The key criterion** — the inability to observe effects from outside the function.
3. **This is a powerful tool** that combines the advantages of the functional approach with practical efficiency.

Thus, functional programming does not require a complete abandonment of mutations, 
but only their strict control and encapsulation. 
This makes FP a more practical and flexible approach to software development.

## Data Type for Safe Work with Mutable State

### The Problem of Restricting the Scope of Effects

When working with mutable state in functional programming, it's important to guarantee that:

1. State changes are not visible from outside a certain scope
2. It's impossible to accidentally "leak" mutable data to the outside

Let's consider the example with quicksort (`quicksort`) from the previous section. 
There we used a mutable array inside the function, but ensured purity by the fact that:

- The array was created inside the function
- No references to it were passed to the outside

However, this approach relies only on programmer discipline. 
We'd like the compiler to help verify these conditions.

### Solution: The ST Type for Controlled State

We will create a special type `ST[S, A]` that will:

- Represent computation with internal mutable state
- Guarantee that the state doesn't "leak" to the outside
- Allow composing such computations

#### Basic Definition

```dotty
// Special type hiding implementation details
opaque type ST[S, A] = S => (A, S)

object ST:
  // Transform the result of computation
  extension [S, A](self: ST[S, A])
    def map[B](f: A => B): ST[S, B] =
      s =>
        val (a, s1) = self(s)
        (f(a), s1)

    // Sequential execution of two computations
    def flatMap[B](f: A => ST[S, B]): ST[S, B] =
      s =>
        val (a, s1) = self(s)
        f(a)(s1)

  // Create a simple computation without effects
  def apply[S, A](a: => A): ST[S, A] =
    lazy val memo = a
    s => (memo, s)

  // Create a computation with an effect
  def lift[S, A](f: S => (A, S)): ST[S, A] = f
end ST
```

### How This Works

1. **Type parameter `S`**:
    - This is a "label" for a particular kind of mutable state
    - Doesn't contain actual data, only types
    - Helps the compiler track scope

2. **Restrictions**:
    - No way to get the actual state directly
    - All operations with state occur within `ST`

3. **Composition**:
    - `map` and `flatMap` methods allow sequential execution of operations
    - The result of one computation can be passed to the next

### Why is This Safe?

1. **State isolation**:
    - Each computation works only with its own state
    - It's impossible to pass state between different `ST` computations

2. **Scope control**:
    - The compiler verifies that the state isn't used outside the permitted scope
    - If you try to "steal" the state, the code won't compile

3. **Pure interface**:
    - From the outside, `ST` looks like a normal functional computation
    - All effects are hidden inside

### Advantages over Other Approaches

1. **Compared with manual encapsulation**:
    - The compiler helps find errors
    - No risk of accidentally breaking encapsulation

2. **Compared with `IO`**:
    - More precise capability limitation
    - Explicitly shows that we work only with local state
    - Better performance

## Working with Mutable References in ST

### Basic Operations with Mutable Cells

For safe work with mutable state in a functional style, we need three basic operations:

1. **Creating** a new mutable cell
2. **Writing** a value into the cell
3. **Reading** a value from the cell

Let's implement these operations using the `ST` type, 
which guarantees that all changes will remain local.

### Implementing a Mutable Reference (STRef)

```dotty
// Class for working with a mutable cell
final class STRef[S, A] private (private var cell: A):
  // Read value from the cell
  def read: ST[S, A] = ST(cell)

  // Write a new value into the cell
  def write(a: => A): ST[S, Unit] =
    ST.lift[S, Unit]: s =>
      cell = a
      ((), s)

object STRef:
  // Create a new mutable cell
  def apply[S, A](a: A): ST[S, STRef[S, A]] =
    ST(new STRef[S, A](a))
```

#### How This Works:

1. **STRef** — a wrapper around an ordinary variable (`var`)
2. **read** returns a computation that, when executed, will return the current value
3. **write** returns a computation that, when executed, will update the value
4. **apply** — the only way to create a new cell

The type `S` plays an important role here:

- It's a "label" that associates the cell with a particular scope
- Doesn't contain actual data, only ensures type safety
- Guarantees that the cell won't be used outside the permitted context

### Example Program with Mutable Cells

Consider a simple program that:

1. Creates two cells with initial value 1
2. Reads their values
3. Writes into each cell the value of the other cell + 1
4. Returns the final values

```dotty
for
  r1 <- STRef[Nothing, Int](1)  // Create the first cell
  r2 <- STRef[Nothing, Int](1)  // Create the second cell
  x  <- r1.read                 // Read from the first cell
  y  <- r2.read                 // Read from the second cell
  _  <- r1.write(y + 1)         // Write into the first cell (y+1)
  _  <- r2.write(x + 1)         // Write into the second cell (x+1)
  a  <- r1.read                 // Read the new value from the first
  b  <- r2.read                 // Read the new value from the second
yield (a, b)
```

#### Features of the Example:

1. We use `Nothing` as the label type, since it's suitable for simple examples
2. All operations are executed sequentially thanks to `for`-notation
3. The program can't be executed yet — a special launch mechanism is needed

#### Why is This Safe?

1. **Encapsulation**:
    - Direct access to the `cell` variable is impossible from the outside
    - All operations go through `ST` computations

2. **Scope control**:
    - The cell is bound to a particular context (type `S`)
    - It's impossible to accidentally pass the cell to another context

3. **Pure interface**:
    - From the outside, working with `STRef` looks like a pure computation
    - All effects are hidden inside `ST` actions

## Safe Execution of Operations with Mutable State

### How to Safely Work with Mutable State

When using `ST` for working with mutable state, it's important to follow two key principles:

1. All state changes must be temporary and local
2. No references to mutable objects should "leak" to the outside

Let's consider the difference between these two types of operations:

```dotty
ST[S, STRef[S, Int]]  // Dangerous operation — returns a mutable reference
ST[S, Int]            // Safe operation — simply returns a value
```

### Safety Guarantees

The Scala type system helps us ensure safety through the following mechanisms:

1. **Binding mutable objects to context**:
    - Each `STRef` is labeled with the same type `S` as the enclosing `ST` action
    - This prevents passing references between different contexts

2. **Checking return values**:
    - It's forbidden to return mutable objects from `ST` actions
    - You can only return simple values (`Int`, `String`, etc.)

### Running ST Actions

To safely execute `ST` actions, we use a special `run` function:

```dotty
object ST:
  def run[A](action: [s] => () => ST[s, A]): A =
    val computation: ST[Unit, A] = action[Unit]()
    computation(())(0)  // Execute the computation with a dummy state
```

### How This Works:

1. **Polymorphic function**:
    - The action must be polymorphic over type `s`
    - This guarantees that it doesn't depend on a specific state type

2. **Dummy state**:
    - `Unit` is passed as a dummy state during execution
    - Since the action is polymorphic, it can't actually use this state

3. **Extracting the result**:
    - Only the final value of type `A` is returned
    - All intermediate mutable objects remain inside

### Example of Safe Execution

```dotty
// Create a safe action
val safeAction = [s] =>
  () =>
    for
      cell <- STRef[s, Int](10) // Create a cell
      _    <- cell.write(20)    // Change the value
      x    <- cell.read         // Read the value
    yield x * 2 // Return the result

// Execute the action
val result = ST.run(safeAction) // Result: 40
```

### Why is This Safe?

**Impossible to return a mutable object**:

```dotty
// This code won't compile:
val p: [s] => () => ST[s, STRef[Nothing, Int]] =
[s] => () => STRef(1)

// -- [E007] Type Mismatch Error: -------------------------------------------------
// 2 |  [s] => () => STRef(1)
//   |               ^^^^^^^^
//   |               Found:    Example.ST[Any, Example.STRef[Any, Int]]
//   |               Required: Example.ST[s, Example.STRef[Nothing, Int]]
```

Note: the type `s` is bound at the moment of calling the polymorphic function
and can even be called multiple times with different types.
We cannot know this type until the function is called.

As a consequence, the fact that we cannot get `STRef` as output from an `ST` action
guarantees that if we have an `STRef`, we are inside the `ST` action
that created it, so we can always safely mutate the reference.

**All changes are isolated**:

- Each action works with its own state
- It's impossible to pass state between different calls

**Pure result**:

- The `run` function returns an ordinary immutable value
- All side effects remain inside the execution

### Advantages of This Approach

1. **Safety**:
    - The compiler checks the correctness of state operations
    - Impossible to accidentally break the rules

2. **Performance**:
    - Real mutation can be used internally
    - A purely functional interface is preserved externally

3. **Convenience**:
    - Allows writing imperative code inside a functional wrapper
    - Preserves all the advantages of the functional approach

This mechanism gives us the ability to use mutable state where needed, 
while preserving all the guarantees of functional programming.

## Working with Mutable Arrays

### Creating a Safe Interface for Arrays

For practical use of mutable state, arrays are often required. 
Let's implement a safe interface for working with them:

```dotty
import scala.reflect.ClassTag

// Wrapper class for a mutable array
final class STArray[S, A] private (private var value: Array[A]):
  
  // Get the size of the array
  def size: ST[S, Int] = ST(value.size)

  // Write an element by index
  def write(i: Int, a: A): ST[S, Unit] =
    ST.lift[S, Unit]: s =>
      value(i) = a
      ((), s)

  // Read an element by index
  def read(i: Int): ST[S, A] = ST(value(i))

  // Convert to an immutable list
  def freeze: ST[S, List[A]] = ST(value.toList)

object STArray:
  // Create a new array of a given size
  def apply[S, A: ClassTag](
      sz: Int,
      v: A
  ): ST[S, STArray[S, A]] =
    ST(new STArray[S, A](Array.fill(sz)(v)))
```

### Implementation Features:

1. **Safe constructor**:
    - The array is created only through the `apply` method of the companion object
    - Returns not the array itself, but an `ST` action that creates it

2. **Restricted access**:
    - All operations with the array return `ST` actions
    - Direct access to the array from the outside is impossible

3. **Conversion to immutable form**:
    - The `freeze` method allows getting a list of elements
    - Guarantees that the array won't be modified after conversion

### Requirements for Element Types

Scala requires special information for creating arrays of arbitrary types:

- Parameter `A: ClassTag` ensures the ability to create an array for type `A`
- The standard library provides the necessary `ClassTag`s for primitive types

### Advantages of the Approach:

1. **Safety**:
    - The compiler prevents leaking references to the array
    - All changes are isolated within `ST` actions

2. **Performance**:
    - A real mutable array is used internally
    - A purely functional interface is preserved externally

3. **Composability**:
    - Individual operations can be combined into more complex ones
    - The sequence of execution is preserved

### Exercise 10.1.1: Implementing the fill Method for STArray

> [Repository with exercises and instructions for completion][sample]

**Task:**

Add a `fill` method to the `STArray` class that fills the array with values from a given `Map`,
where keys represent indices and values are the elements to write.

```dotty
final class STArray[S, A] private (private var value: Array[A]):
  // ... other methods ...
  
  def fill(xs: Map[Int, A]): ST[S, Unit] = ???
```

**Examples:**

```dotty
// Program to fill the array
val program = [s] =>
  () =>
    for
      // Create an array of size 3 with initial value 0
      arr    <- STArray[s, Int](3, 0)
      // Fill indices 0 and 2
      _      <- arr.fill(Map(0 -> 10, 2 -> 30))
      result <- arr.freeze
    yield result

// Execute the program
val result = ST.run(program) // Result: List(10, 0, 30)
```

@:note(🔍 Show Solution)

The `fill` method sequentially writes values from the given `Map` into the array, 
using `foldLeft` to combine write operations: 
starting with an empty `ST[S, Unit](())`, for each (index, value) pair 
it adds a `write` operation via `flatMap`, 
guaranteeing type safety thanks to the `ST` system.

```dotty
def fill(xs: Map[Int, A]): ST[S, Unit] =
  xs.foldLeft(ST[S, Unit](())):
    case (st, (k, v)) => st.flatMap(_ => write(k, v))
```

@:@

### Purely Functional In-Place Quicksort

The components for `quicksort` are now easy to write in `ST`.
For example, the `swap` function, which swaps two elements of the array,
can be defined as a method in `STArray`:

```dotty
def swap(i: Int, j: Int): ST[S, Unit] =
  for
    x <- read(i)     // Read value at index i
    y <- read(j)     // Read value at index j
    _ <- write(i, y) // Write y at position i
    _ <- write(j, x) // Write x at position j
  yield ()
```

### Exercise 10.1.2: Implementing partition

Write a purely functional version of `partition`:

```dotty
def partition[S](a: STArray[S, Int], l: Int, r: Int, pivot: Int): ST[S, Int] = ???
```

@:note(🔍 Show Solution)

The `partition` function implements the partitioning step in quicksort: 
first the pivot element is moved to the end of the array, 
then elements are scanned from `l` to `r-1` — if an element is less than the pivot, 
it is swapped with the element at the current boundary (`j`), which is incremented after each swap. 
At the end, the pivot element is returned to its final position (between smaller and larger elements), 
which is returned as the result. 
All work with mutable state (reading/writing to the array, boundary counter) 
is encapsulated in `ST` actions, preserving referential transparency.

```dotty
def partition[S](
    a: STArray[S, Int], // Working array
    l: Int,             // Left boundary
    r: Int,             // Right boundary
    pivot: Int          // Pivot element index
): ST[S, Int] = // Returns the new pivot position
  for
    vp <- a.read(pivot)    // Read the pivot element value
    _  <- a.swap(pivot, r) // Move pivot to the end
    j  <- STRef(l)         // Create a mutable reference for the boundary
    _ <- (l until r).foldLeft(ST[S, Unit](()))((s, i) =>
      for
        _  <- s
        vi <- a.read(i) // Read the current element
        _ <- if vi < vp then // If the element is less than the pivot
          for
            vj <- j.read          // Read the current boundary
            _  <- a.swap(i, vj)   // Swap them
            _  <- j.write(vj + 1) // Increment the boundary
          yield ()
        else ST[S, Unit](()) // Otherwise do nothing
      yield ()
    )
    x <- j.read       // Read the final position
    _ <- a.swap(x, r) // Return pivot to its place
  yield x // Return the new pivot position
```

@:@

### Exercise 10.1.3: Implementing qs

Write a purely functional version of `qs`:

```dotty
def qs[S](a: STArray[S, Int], l: Int, r: Int): ST[S, Unit] = ???
```

@:note(🔍 Show Solution)

The `qs` function implements recursive quicksort within `ST`:
if the boundaries are valid (`l < r`), it selects the middle element as the pivot
via `partition`, recursively sorts the left and right parts relative to the pivot,
and in the base case (when there's nothing to sort) returns an "empty" action.
All operations with the mutable array are encapsulated in `ST` actions, which preserves function purity,
allowing efficient in-place array modification.

```dotty
def qs[S](a: STArray[S, Int], l: Int, r: Int): ST[S, Unit] =
  if l < r then // If there's something to sort
    for
      pi <- partition(a, l, r, l + (r - l) / 2) // Partition the array
      _  <- qs(a, l, pi - 1)                    // Sort the left part
      _  <- qs(a, pi + 1, r)                    // Sort the right part
    yield ()
  else ST[S, Unit](()) // Base case of recursion
```

@:@

## Purity in Programming is Context-Dependent

### 1. Unobservable Effects and Locality

In the previous sections, we discussed how some effects (such as data modification) 
can be "invisible" to the program if they are confined to a local scope.  

**Example**: If a function modifies a temporary variable but doesn't return or pass it to the outside, 
external code won't be able to notice these changes.

### 2. Referential Transparency Under the Microscope

Referential transparency is a property of an expression 
that allows replacing it with the result of its evaluation without changing the program's behavior.  
But Scala has nuances:

#### Example with Class `Foo`:

```dotty
case class Foo(s: String)

// Comparison by value
val b = Foo("hello") == Foo("hello")  // true 
// Comparison by reference (as in Java)
val c = Foo("hello") eq Foo("hello")  // false
```

**Problem**:  

Each call to `Foo("hello")` creates a new object in memory. 
This is technically a side effect (memory allocation), 
but most programs don't notice it, since they don't use `eq`.

#### Key Takeaway:

- **Constructors in Scala are not fully transparent** due to the creation of new objects.
- But if a program doesn't check references (via `eq`), this effect is "invisible".

### 3. Refined Definition of Referential Transparency

> Expression `e` is transparent for program `p` if all its uses in `p` 
> can be replaced with the result of evaluating `e` without changing the meaning of `p`.

**What does this mean?**  

Purity depends on:

1. **The context of the program** (what effects it can observe).
2. **The criteria for equivalence** (what we consider "identical" behavior).

### 4. What Counts as a Side Effect?

#### Example with Method `timesTwo`:

```dotty
def timesTwo(x: Int): Int =
  if x < 0 then println("Negative!")  // Side effect
  x * 2
```

- Replacing `timesTwo(2)` with `4` will preserve the result, but "hide" the console output.
- **Important**: We need to decide whether to consider console output as part of the program's behavior.

## Conclusion

**Key Ideas**

1. **Local mutations are acceptable** if they don't affect external code.
2. **Purity is a convention**:
    - What effects do we consider important?
    - How do we define that two programs behave "the same"?
3. **Scala's tools help manage complexity**:
    - `ST` — for isolated mutations.
    - `IO` — for explicit management of I/O operations.
    - Custom mechanisms can be created for specific tasks.

**Summary**

- **Functional code is not about forbidding changes**, but about predictability.
- **The decision about purity is made based on the program's requirements**.
- **Scala's type system** is a powerful tool for expressing these decisions.

---

**References:**

- [Link to the exercises repository][sample]
- [Functional Programming in Scala, Second Edition, Chapter 14](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Towards an Effect System in Scala, Part 1: ST Monad](https://apocalisp.wordpress.com/2011/03/20/towards-an-effect-system-in-scala-part-1/)
- [Towards an Effect System in Scala, Part 2: IO Monad](https://apocalisp.wordpress.com/2011/12/19/towards-an-effect-system-in-scala-part-2-io-monad/)

[sample]: https://gitverse.ru/scalabook/fp
