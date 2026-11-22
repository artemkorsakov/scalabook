# Functional State

## Description and Concept

The functional approach to state management is based on a fundamental pattern 
that allows modeling any stateful API while preserving function purity. 
The basic pattern is implemented as follows:

```dotty
opaque type State[S, +A] = S => (A, S)

object State:
  extension [S, A](underlying: State[S, A])
    // Executes the state computation, returning the result and new state
    def run(s: S): (A, S) = underlying(s)

  // Constructor to create State from a function
  def apply[S, A](f: S => (A, S)): State[S, A] = f
```

### Analysis of the Implementation

The `State[S, A]` type represents:

1. **A computation with state** — an operation that depends on and can modify some state of type `S`
2. **A pure function** — transforming input state into a pair (result, new state)
3. **An abstraction** — hidden behind `opaque type` for type safety

Key components:

- `run` — the only way to "extract" a value from `State`, by executing the computation
- `apply` — a factory method for creating `State` computations

Using `opaque type` instead of a simple type alias provides important advantages:

1. **Encapsulation** — outside the `State` module, it's impossible to know that `State` is just a function
2. **Safety** — prevents accidental mixing with ordinary functions
3. **Flexibility** — allows changing the internal implementation without changing the API

```dotty
// Example usage:
val counter: State[Int, String] = State { s => 
  ("Result", s + 1) 
}
counter.run(0)  // ("Result", 1)
```

### Advantages of the Approach

1. **Purity** — all state changes are explicit and controlled
2. **Testability** — states can be easily mocked
3. **Composability** — complex behavior is assembled from simple components
4. **Thread safety** — natural immutability

### Summary

- **Functional state** is a computation that transforms input state into a result and new state
- **State** provides:
    - A pure way to work with mutable state
    - The ability to compose state transformations
    - Explicit tracking of state flow

### Exercise 6.1.1: Generating Fibonacci Sequence with State

> [Repository with exercises and instructions for completion][sample]

Implement a Fibonacci number generator using `State`. 
The generator should take the current state and return the next Fibonacci number with updated state.

```dotty   
// Generating Fibonacci sequence  
val fibState: State[???, ???] =
  State(???)
```  

**Examples:**

```dotty  
fibState.run((5, 8)) // (5, (8, 13)) 
// Returns the current number (5), new state — (8, 13)
```  

@:note(🔍 Show Solution)

A Fibonacci generator can be implemented with `State[(Long, Long), Long]`, 
where the state is the current pair of numbers `(a, b)`, 
and the output value is the next number in the sequence (`a`). 
At each call, the state updates: `a` is replaced by `b`, and `b` — by `a + b`.

```dotty  
val fibState: State[(Long, Long), Long] =
  State((a, b) => (a, (b, a + b)))
```  

How it works:

1. Input state: `(a, b)` (e.g., `(5, 8)`).
2. Returned value: `a` (in the example — `5`).
3. New state: `(b, a + b)` (in the example — `(8, 13)`).

Thus, each call to `fibState.run` generates the next Fibonacci number and advances the state forward.

@:@

## Example of Functional State

Let's consider a simplified example of a candy machine.

```dotty
case class SimpleMachine(candies: Int, coins: Int)
```

If the machine has candies remaining and you pay a coin, it will dispense a candy.
The number of candies decreases by 1, and the number of coins increases by 1.
In all other cases, the machine's state doesn't change.

Let's describe this with a function that computes the next state of the machine:

```dotty
case class SimpleMachine(candies: Int, coins: Int):
  self =>

  lazy val next: SimpleMachine =
    if candies <= 0 then self
    else SimpleMachine(candies - 1, coins + 1)
```

Let's define a `State` that computes the value — the number of earned coins:

```dotty
val state: State[SimpleMachine, Int] = State(sm => (sm.coins + 1, sm.next))
```

Here the `apply` method is used, creating a `State` value from a function.

To execute the computation of the value and the next state, call `run`:

```dotty
val initial = SimpleMachine(100, 0)
state.run(initial)
// (1, SimpleMachine(99, 1))
```

## General-Purpose Functions

General-purpose functions that describe patterns for programs tracking state. 
These functions form the basic set of operations for working with `State`, allowing combining 
and transforming state computations.

### Basic Combinators

#### map

The `map` function allows transforming a value inside the `State` context without affecting the state itself. 
Given a basic template `State[S, A]` and a transformation function `A => B`, a new `State` is created 
that on execution:

1. Accepts the initial state of type `S`
2. Runs the original `State`, getting the pair `(A, S1)`
3. Applies function `f` to value `A`, getting `B`
4. Returns the pair `(B, S1)`

```dotty
object State:
  extension [S, A](underlying: State[S, A])
    def map[B](f: A => B): State[S, B] =
      s =>
        val (a, s1) = run(s)
        (f(a), s1)
```

The usage example shows transforming a numeric value into a string:

```dotty
val stateB = state.map(coins => s"Earned $coins coins")
stateB.run(initial)
// Result: (Earned 1 coins, SimpleMachine(99, 1))
```

#### map2

The `map2` combinator allows executing two `State` computations sequentially and combining their results with a function. 

Features:

- Computations are executed sequentially, with state passed from one to the next
- Allows combining results without explicitly extracting values

```dotty
object State:
  extension [S, A](underlying: State[S, A])
    def map2[B, C](sb: State[S, B])(f: (A, B) => C): State[S, C] =
      s0 =>
        val (a, s1) = run(s0)
        val (b, s2) = sb.run(s1)
        (f(a, b), s2)
```

The example demonstrates string concatenation from two `State` computations:

```dotty
val stateA = State[SimpleMachine, String](sm => ("First change", sm.next))
val stateB = State[SimpleMachine, String](sm => (", Second change", sm.next))
val stateC = stateA.map2(stateB)(_ + _)

stateC.run(initial)
// Result: (First change, Second change, SimpleMachine(98, 2))
```

#### flatMap

The `flatMap` function (also known as `bind`) allows creating sequential computations, 
where each next one depends on the result of the previous one. 
This is a key operation for `State`.

```dotty
object State:
  extension [S, A](underlying: State[S, A])
    def flatMap[B](f: A => State[S, B]): State[S, B] =
      s0 =>
        val (a, s1) = run(s0)
        f(a)(s1)
```

The example shows branching logic depending on an intermediate result:

```dotty
val stateA = State[SimpleMachine, Int](sm => (sm.candies - 1, sm.next))
val f: Int => State[SimpleMachine, String] = candies =>
  if candies <= 0 then State(sm => ("No candies left", sm))
  else State(sm => ("Candies still available", sm))

val stateB = stateA.flatMap(f)

stateB.run(initial)
// Result: (Candies still available, SimpleMachine(99, 1))
```

### Syntactic Sugar

#### for-comprehension

Thanks to the presence of `map` and `flatMap`, `State` can be used in for-notation, 
which significantly improves readability of sequential operations:

```dotty
val stateB =
  for
    candies <- stateA      // extract candies value
    message <- f(candies)  // pass it to the next computation
  yield message            // return the result

stateB.run(initial)
// Result: (Candies still available, SimpleMachine(99, 1))
```

### Basic Constructors

#### unit

The `unit` function (also called `pure`) creates a `State` that:

- Doesn't change the state
- Contains the passed value
- Is a neutral element for `flatMap`

```dotty
object State:
  def unit[S, A](a: A): State[S, A] =
    s => (a, s)
```

Example of creating a trivial `State`:

```dotty
val stateA = State.unit[SimpleMachine, String]("Message")
stateA.run(initial)
// Result: (Message, SimpleMachine(100, 0))
```

### Working with State

#### get, set, and modify

A trio of functions for directly working with state:

1. `get` — gets the current state, making it available as a value
2. `set` — completely replaces the current state with a new one
3. `modify` — transforms the state with a function

```dotty
object State:
  def get[S]: State[S, S] = s => (s, s)  // returns the current state

  def set[S](s: S): State[S, Unit] = _ => ((), s)  // ignores the old state

  def modify[S](f: S => S): State[S, Unit] =
    for
      s <- get       // get the current state
      _ <- set(f(s)) // set the transformed state
    yield ()
```

Usage examples:

```dotty
val initial = SimpleMachine(100, 0)
val other   = SimpleMachine(0, 100)

// Reading state
State.get[SimpleMachine].run(initial)
// (SimpleMachine(100, 0), SimpleMachine(100, 0))

// Writing state
State.set(other).run(initial)
// ((), SimpleMachine(0, 100))

// Modifying state
State.modify[SimpleMachine](_.next).run(initial)
// ((), SimpleMachine(99, 1))
```

### Working with Collections

#### traverse and sequence

Functions for processing collections of `State` computations:

- `traverse` — transforms a list of values into `State` computations via a function and collects results
- `sequence` — a special case of `traverse` where the input is already a list of `State` computations

```dotty
object State:
  def sequence[S, A](list: List[State[S, A]]): State[S, List[A]] =
    traverse(list)(identity)  // applies identity to each element

  def traverse[S, A, B](as: List[A])(f: A => State[S, B]): State[S, List[B]] =
    as.foldRight(unit[S, List[B]](Nil))((a, acc) => f(a).map2(acc)(_ :: _))
    // Right-associative fold with accumulating results
```

Key features:

- Computations are executed sequentially
- State is passed from one computation to the next
- Results are collected in a list in the same order

## Candy Machine: Simulating a Candy Dispenser

Let's implement a more complex machine that simulates a candy dispenser as a finite state machine. 
This machine demonstrates a classic example of state management 
with clearly defined transition rules between states.

### Data Model

The machine has:

Two types of input (user interactions):

```dotty
enum Input:
  case Coin  // Coin (payment)
  case Turn  // Turning the knob (receiving product)
```

State described by three parameters:

```dotty
case class Machine(
  locked: Boolean,  // Current lock status
  candies: Int,     // Remaining candies
  coins: Int        // Number of collected coins
)
```

### Machine Rules

The machine implements strict business logic via the following rules:

(a) **Unlocking upon payment**  
   
If a coin is inserted (`Coin`) into a locked machine (`locked = true`), it unlocks (if there are candies), increasing the coin counter.

(b) **Dispensing product**  
   
Turning the knob (`Turn`) on an unlocked machine (`locked = false`):

- Decreases the number of candies
- Automatically locks the machine

(c) **Ignoring invalid actions**  

The system ignores:

- Turning the knob on a locked machine
- Inserting a coin into an already unlocked machine

(d) **"No product" mode**

A machine with zero candies (`candies = 0`) completely ignores all inputs.

### State Logic Implementation

The `updateMachine` function implements a deterministic finite state machine, 
where each combination of (`Input`, `Machine`) uniquely determines the next state:

```dotty
object Candy:
  private val updateMachine: Input => Machine => Machine =
    (i: Input) =>
      (s: Machine) =>
        (i, s) match
          // (d) No product mode
          case (_, Machine(_, 0, _)) => s
          
          // (c) Invalid actions
          case (Input.Coin, Machine(false, _, _)) => s
          case (Input.Turn, Machine(true, _, _))  => s
          
          // (a) Successful payment
          case (Input.Coin, Machine(true, candy, coin)) =>
            Machine(false, candy, coin + 1)
            
          // (b) Successful dispensing
          case (Input.Turn, Machine(false, candy, coin)) =>
            Machine(true, candy - 1, coin)
```

### Machine Simulator

The `simulateMachine` method processes a sequence of actions, applying state modifications:

```dotty
object Candy:
  def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] =
    for
      // Apply all input actions as State transformations
      _ <- State.traverse(inputs)(input => 
        State.modify(updateMachine(input)))
      
      // Get the final state
      s <- State.get
    yield (s.coins, s.candies)
```

Key points of the implementation:

1. `State.traverse` sequentially applies each action from the `inputs` list
2. `State.modify` transforms the machine according to `updateMachine` rules
3. The result of intermediate steps is ignored (`List[Unit]`)
4. The final state is extracted via `State.get`
5. A tuple `(coins, candies)` is returned as the result

### Example Walkthrough

Let's consider a detailed example with state tracing:

```dotty
val initialMachine = Machine(
  locked = true,  // Machine is locked
  candies = 5,    // 5 candies available
  coins = 10      // 10 coins already in the machine
)

val inputs = List(
  Coin, // 1. Unlock (+1 coin)
  Turn, // 2. Dispense a candy (-1 candy)
  Coin, // 3. Unlock (+1 coin)
  Coin, // 4. Repeat payment (ignored)
  Coin, // 5. Repeat payment (ignored)
  Turn, // 6. Dispense a candy (-1 candy)
  Turn, // 7. Won't work (locked)
  Turn, // 8. Won't work (locked)
  Coin, // 9. Unlock (+1 coin)
  Turn  // 10. Final dispensing (-1 candy)
)

val ((totalCoins, remainingCandies), finalState) =
  Candy.simulateMachine(inputs).run(initialMachine)

// Result: ((13, 2), Machine(true, 2, 13))
```

Step-by-step semantics:

1. Initial state: (10 coins, 5 candies)
2. Successful operations: 1, 2, 3, 6, 9, 10
3. Ignored operations: 4, 5, 7, 8
4. Final: +3 coins (13), -3 candies (2)

### Architectural Features

1. **Purity of computations** — All state transformations are immutable
2. **Composability** — Logic is assembled from small components
3. **Explicit modeling** — All states and transitions are clearly defined
4. **Testability** — Easy to verify any sequence of actions

This approach demonstrates effective use of functional state 
for modeling real processes while maintaining control over complexity.

---

**References:**

- [Link to the exercises repository][sample]
- [Three Useful Monads](https://habr.com/ru/articles/184722/)
- [Cats](https://typelevel.org/cats/datatypes/state.html)
- [Functional Programming in Scala, Second Edition, Chapter 6](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Herding Cats](http://eed3si9n.com/herding-cats/State.html)
- [Memorisation with State using Scala](https://blog.tmorris.net/posts/memoisation-with-state-using-scala/index.html)
- [Scala with Cats](https://scalawithcats.com/dist/scala-with-cats.html#the-state-monad)

[sample]: https://gitverse.ru/scalabook/fp
