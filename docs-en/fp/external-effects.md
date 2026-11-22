# External Effects and I/O

## Introduction

In functional programming, there is an important rule: functions must be pure.
This means they always return the same result for identical inputs
and produce no hidden changes (side effects) -- for example, they do not read files,
send network requests, or modify global state.

But if a program does not interact with the outside world at all, it is useless. We need to:

- read data from files and databases,
- output results to a console or UI,
- send HTTP requests.

How do we reconcile function purity with the need to work with the outside world?

### Describing Actions Instead of Executing Them

Instead of executing I/O operations directly,
functional languages offer to **describe** them as values.
These values can be combined, passed to functions, and even used to build complex scenarios --
but the actions themselves do not execute until the very last moment.

For example:

- `readFile("data.txt")` -- does not immediately read the file, but returns a **description** of the read operation.
- `print("Hello")` -- does not output text to the screen, but creates an **object** describing that action.

Only when the entire program is assembled from such descriptions, a special mechanism
(e.g., the language runtime) executes them in the correct order.

### Why Is This Convenient?

1. **Pure functions stay pure**
    The program logic works with descriptions of actions, not with real effects, so predictability is preserved.

2. **Control over execution**
    Simple operations can be combined into complex scenarios without losing control over when and how they execute.

3. **Safety**
    I/O operations are not scattered across the code, but explicitly extracted into separate data structures.

### How Does This Work Technically?

Functional languages use a special type (often called `IO`) that:

- **Represents an action**, not a result.
- **Allows chaining operations** (e.g., "read file -> process data -> write result").
- **Defers execution** until the program explicitly triggers it.

Thus, instead of mixing logic and side effects, we first build a **computation plan**, and then execute it.

This approach allows writing code that retains the advantages of functional programming,
while remaining practical for real-world tasks.

## Effect Factorization: From Imperative to Functional Style

### Step 1: Original Imperative Code

Consider the following example:

```dotty
case class Player(name: String, score: Int)

def contest(p1: Player, p2: Player): Unit =
  if p1.score > p2.score then
    println(s"${p1.name} is the winner!")
  else if p2.score > p1.score then
    println(s"${p2.name} is the winner!")
  else
    println("It's a draw.")
```

**Problems:**

- Business logic for determining the winner and outputting the result are mixed
- Cannot test the logic without actual console output
- Code cannot be reused for other purposes (e.g., GUI output)

### Step 2: Separating Pure Logic

```dotty
// Pure function with no side effects
def winner(p1: Player, p2: Player): Option[Player] =
  if p1.score > p2.score then Some(p1)
  else if p1.score < p2.score then Some(p2)
  else None

// Effects moved to the program boundary
def contest(p1: Player, p2: Player): Unit =
  winner(p1, p2) match
    case Some(Player(name, _)) => println(s"$name is the winner!")
    case None                  => println("It's a draw.")
```

**Improvements:**

1. `winner` becomes a pure function:
    - Always returns the same result for the same inputs
    - Easy to test
    - Usable in other contexts

2. `contest` now:
    - Only handles the output effect
    - Contains minimal logic

> It is always possible to decompose an impure procedure into a pure core function and two effect-handling procedures:
>
> - one, supplying input data to the pure function,
> - and another, doing something with the output data of the pure function.

### Step 3: Further Separation of Responsibilities

We can refactor this even further.
The `contest` function still has two responsibilities:

- computing which message to display,
- and then printing that message to the console.

```dotty
// Pure transformation of result to message
def winnerMsg(p: Option[Player]): String =
  p match
    case Some(p) => s"${p.name} is the winner!"
    case None    => "It's a draw."

// Effect function becomes trivial
def contest(p1: Player, p2: Player): Unit =
  println(winnerMsg(winner(p1, p2)))
```

**The resulting program architecture:**

1. `winner` (pure function) - determines the winner
2. `winnerMsg` (pure function) - formats the result
3. `contest` (effects) - console output

### General Refactoring Principle

1. **Identifying the core**:
    - Find the parts of the code that can be expressed without effects
    - Extract them into pure functions

2. **Isolating effects**:
    - Leave the minimum number of side-effecting operations
    - Push them to the boundaries of the program

3. **Composition**:
    - Build the program as a composition of pure transformations
    - Effects execute only at the end

The same principle applies to larger, more complex programs.
We do not change what the program **does**,
only the internal details of how it is broken down into smaller functions.
The point here is that inside each function with side effects, there is a pure function waiting for the result.

We can formalize this understanding a bit.
Given an impure function `f` of type `A => B`, we can decompose `f` into two functions:

- A pure function of type `A => D`, where `D` is some description of the result of `f`.
- An impure function of type `D => B`, which can be thought of as an interpreter of those descriptions.

Let us consider the repeated application of this strategy to a program.
Each time we apply it, we make more functions pure and push side effects outward.
We could call these impure functions the "imperative shell" around the pure core of the program.
Eventually, we reach functions that seemingly require side effects,
such as the built-in `println` which has type `String => Unit`.

### Testing Example

```dotty
// Test pure parts
val alice = Player("Alice", 10)
val bob   = Player("Bob", 5)

assert(winner(alice, bob).contains(alice))
assert(winnerMsg(Some(alice)) == "Alice is the winner!")
```

### Extending the Architecture

If the output method needs to change (e.g., to a file instead of the console), only `contest` needs to be modified:

```dotty
def fileContest(p1: Player, p2: Player): Unit =
  Files.write(
    Paths.get("result.txt"),
    winnerMsg(winner(p1, p2)).getBytes
  )
```

Meanwhile, the pure logic remains unchanged.

**Conclusion**:

Gradually extracting pure functions makes code:

- More understandable
- Easy to test
- Flexible to change
- Reusable

### Exercise 9.1.1: User Registration

> [Repository with exercises and instructions][sample]

**Original code:**

```dotty
def registerUser(username: String, password: String): Boolean =
  if username.length < 3 then
    println("Username too short")
    false
  else if password.length < 8 then
    println("Password too weak")
    false
  else
    // Save to database
    DB.save(User(username, passwordHash(password)))
    println("User registered successfully")
    true
```

**Task:**

1. Extract a pure data validation function
2. Extract a pure password validation function
3. Extract a pure user validation function
4. Leave the effects (console output and DB write) in a separate function

```dotty
def validateUsername(username: String): Either[String, String] =
  ???

def validatePassword(password: String): Either[String, String] =
  ???

def validateUser(username: String, password: String): Either[String, User] =
  ???

def registerUser(username: String, password: String): Boolean =
  validateUser(username, password) match
    case Right(user) =>
      DB.save(user)
      println("User registered successfully")
      true
    case Left(error) =>
      println(error)
      false
```


@:note(T🔍 Show Solution)

This solution separates user validation (pure functions `validateUsername`, `validatePassword`, and `validateUser`)
from side effects (function `registerUser`).
The pure functions check data and return `Either` with an error or a valid `User`, isolating the business logic,
while `registerUser` processes the result, printing messages and saving data to the DB,
making the code testable, reusable, and easily modifiable.

```dotty
def validateUsername(username: String): Either[String, String] =
  if username.length < 3 then
    Left("Username too short")
  else
    Right(username)

def validatePassword(password: String): Either[String, String] =
  if password.length < 8 then
    Left("Password too weak")
  else
    Right(password)

def validateUser(username: String, password: String): Either[String, User] =
  (validateUsername(username), validatePassword(password)) match
    case (Right(validUser), Right(validPass)) =>
      Right(User(validUser, passwordHash(validPass)))
    case (Left(error1), Left(error2)) =>
      Left(s"$error1, $error2")
    case (Left(error), _) =>
      Left(error)
    case (_, Left(error)) =>
      Left(error)
```

@:@

### Exercise 9.1.2: Order Processing

**Original code:**

```dotty
case class Customer(name: String, isPremium: Boolean, email: String)
case class Item(name: String, price: Double, isOutOfStock: Boolean)
case class Order(customer: Customer, items: List[Item])

object DB:
  def save(order: Order): Unit =
    println(s"Saving order for ${order.customer.name} to database")

object Inventory:
  def update(items: List[Item]): Unit =
    println(s"Updating inventory for ${items.map(_.name).mkString(", ")}")

object Email:
  def send(email: String, message: String): Unit =
    println(s"Sending email to $email: $message")

def processOrder(order: Order): Unit =
  val total    = order.items.map(_.price).sum
  val discount = if order.customer.isPremium then total * 0.1 else 0

  println(s"Processing order for ${order.customer.name}")
  println(s"Total: $total, Discount: $discount")

  if order.items.exists(_.isOutOfStock) then
    println("Some items are out of stock!")
  else
    DB.save(order)
    Inventory.update(order.items)
    Email.send(order.customer.email, "Your order is processed")
```

**Task:**

1. Extract a pure function to compute the total
2. Extract a pure function to check item availability
3. Separate info printing from DB/inventory operations

```dotty
def calculateTotal(items: List[Item]): Double =
  ???

def calculateDiscount(total: Double, isPremium: Boolean): Double =
  ???

def checkAvailability(items: List[Item]): Boolean =
  ???

def printOrderInfo(order: Order, total: Double, discount: Double): Unit =
  println(s"Processing order for ${order.customer.name}")
  println(s"Total: $total, Discount: $discount")

def processOrder(order: Order): Unit =
  val total    = calculateTotal(order.items)
  val discount = calculateDiscount(total, order.customer.isPremium)

  printOrderInfo(order, total, discount)

  if checkAvailability(order.items) then
    DB.save(order)
    Inventory.update(order.items)
    Email.send(order.customer.email, "Your order is processed")
  else
    println("Some items are out of stock!")
```


@:note(T🔍 Show Solution)

The following solution separates order processing into pure functions for business logic
(`calculateTotal`, `calculateDiscount`, `checkAvailability`)
and effect-handling functions (`printOrderInfo`, `processOrder`),
isolating computations from I/O operations (console output, DB and inventory work).
The pure functions handle calculations and checks, returning results without changing external state,
while `processOrder` coordinates effect execution using these results for saving data and sending notifications.

```dotty
def calculateTotal(items: List[Item]): Double =
  items.map(_.price).sum

def calculateDiscount(total: Double, isPremium: Boolean): Double =
  if isPremium then total * 0.1 else 0

def checkAvailability(items: List[Item]): Boolean =
  !items.exists(_.isOutOfStock)
```

@:@

## The IO Concept: Formalizing Approach to Managing Side Effects

### Basic IO Abstraction Implementation

Traditional I/O procedures, such as `println`,
combine two distinct responsibilities:

1. Describing what needs to be done (intent)
2. Actually performing the action (effect)

We can separate these aspects by introducing a special data type `IO`:

```dotty
trait IO:
  /** Executes the described action. The method is marked "unsafe" to
    * emphasize that this is the entry point for real side effects.
    */
  def unsafeRun(): Unit

object IO:
  /** Creates an IO action to print a string to the console.
    * @param msg
    *   The string to print
    * @return
    *   An IO action describing the print operation
    */
  def PrintLine(msg: String): IO = new IO:
    def unsafeRun(): Unit = println(msg)
```

### Usage Example

```dotty
// Creating an action description
val hello: IO = PrintLine("Hello, World!")

// Actual execution (in a real application, this is done once at the program "root")
hello.unsafeRun() // Prints "Hello, World!"
```

### Key Characteristics of the Approach

1. **Description over execution**:
    - `IO` is an immutable description of an action
    - The action itself is not executed until `unsafeRun()` is explicitly called

2. **Function purity**:
    - Functions returning `IO` stay pure, since they merely create descriptions
    - All side effects are isolated in `unsafeRun()`

3. **Composability**:
    - Action descriptions can be combined into more complex programs

### Applying to the Example Above

```dotty
def contest(p1: Player, p2: Player): IO =
  PrintLine(winnerMsg(winner(p1, p2)))
```

The `contest` function is now pure; it returns an `IO` value that merely describes the action
that should take place, but does not actually perform it.
We say that `contest` has (or produces) an _effect_ or is _effectful_,
but it is only the `unsafeRun` method of the `IO` interpreter that actually has the side effect.

Advantages of the new version:

1. **Single responsibility**:
    - `contest` now only assembles the program from components
    - `winner` determines the winner (pure logic)
    - `winnerMsg` formats the message (pure transformation)
    - `PrintLine` describes the output (effect)

2. **Execution control**:
    - The program is described declaratively
    - The execution point (`unsafeRun`) is explicitly controlled

3. **Testability**:
    - `contest` can be tested without actual console output
    - Only the structure of the returned `IO` value can be verified

### Architectural Consequences

This approach leads to the following application architecture:

1. **Core**:
    - Pure business logic functions
    - Data transformations
    - Generation of effect descriptions

2. **Periphery**:
    - `unsafeRun()` interpreter
    - Implementation of specific effects
    - Adapters to external systems

Separation allows:

- Easy testing of the program core
- Swapping effect implementations (e.g., for tests)
- Controlling execution of side effects
- Creating predictable programs

## Handling Input Effects

### The Problem of Input Effects

In previous examples, the `IO` type only allowed describing output operations (e.g., console output).
However, many programs need to interact with external data sources.
Suppose we want to write a program that asks the user for a temperature in Fahrenheit,
then converts that value to Celsius and displays it to the user.
A typical imperative program might look roughly like the following listing.

```dotty
def fahrenheitToCelsius(f: Double): Double =
  (f - 32) * 5.0 / 9.0

def converter(): Unit =
  println("Enter a temperature in degrees Fahrenheit: ")
  val d = readLine.toDouble
  println(fahrenheitToCelsius(d))
```

### Limitations of Simple IO

Trying to rewrite this in a functional style runs into a problem:

```dotty
def converter(): IO =
  val prompt: IO =
    PrintLine("Enter a temperature in degrees Fahrenheit: ")
  // Cannot obtain the entered value in a pure manner
```

### Extended IO Version

The solution is to parameterize the `IO` type so it can represent computations that return values.
Here, `map` and `flatMap` functions are added, so `IO` can be used in for-comprehensions.

```dotty
trait IO[A]:
  self =>

  /**
   * Executes the described action and returns a result of type A
   * @return The result of executing the effect
   */
  def unsafeRun(): A

  /**
   * Transforms the result of an IO action
   * @param f The transformation function
   * @return A new IO action
   */
  def map[B](f: A => B): IO[B] = new:
    def unsafeRun(): B = f(self.unsafeRun())

  /**
   * Sequentially composes IO actions
   * @param f A function that returns a new IO action
   * @return The composed action
   */
  def flatMap[B](f: A => IO[B]): IO[B] = new:
    def unsafeRun(): B = f(self.unsafeRun()).unsafeRun()

object IO:
  /**
   * Creates an IO action from an expression
   * @param a The expression, which may contain effects
   * @return An IO action
   */
  def apply[A](a: => A): IO[A] = new:
    def unsafeRun(): A = a
```

### Temperature Converter Implementation

Now we can fully describe the temperature conversion program:

```dotty
def ReadLine: IO[String]             = IO(readLine())
def PrintLine(msg: String): IO[Unit] = IO(println(msg))

def converter: IO[Unit] =
  for
    _     <- PrintLine("Enter a temperature in degrees Fahrenheit: ")
    input <- ReadLine
    temp = input.toDouble
    _ <- PrintLine(s"${fahrenheitToCelsius(temp)} Celsius")
  yield ()
```

Our `converter` definition no longer has side effects;
it is a referentially transparent description of an effectful computation,
and `converter.unsafeRun` is the interpreter that will actually execute those effects.

### Key Features of the Solution

1. **Full interaction**:
    - Now we can describe both input and output
    - `ReadLine` returns an `IO[String]` with the expected input

2. **Operation composition**:
    - `map` allows transforming results
    - `flatMap` allows chaining operations sequentially
    - For-comprehension provides convenient syntax

3. **Referential transparency**:
    - `converter` remains a pure function
    - All effects are deferred until `unsafeRun()` is called

4. **Type safety**:
    - The `IO[A]` type explicitly indicates the returned value
    - Data processing errors are detected at compile time

### Additional Examples

1. **Echo program**:
    ```dotty
    val echo: IO[Unit] = ReadLine.flatMap(PrintLine)
    ```

2. **Reading a number**:
    ```dotty
    val readInt: IO[Int] = ReadLine.map(_.toInt)
    ```

3. **Interactive dialogue**:
    ```dotty
    val questionnaire: IO[Unit] =
      for
        _    <- PrintLine("What's your name?")
        name <- ReadLine
        _    <- PrintLine(s"Hello, $name! How old are you?")
        age  <- ReadLine.map(_.toInt)
        _    <- PrintLine(s"$age is a great age!")
      yield ()
    ```

### Architectural Advantages

1. **Clear separation**:
    - Program description (pure code)
    - Effect execution (single `unsafeRun()` call)

2. **Testability**:
    - Real effects can be replaced with test doubles
    - Logic can be verified without actual I/O

3. **Modifiability**:
    - Easy to change input sources (file, network, console)
    - Simple to add new data transformations

This approach allows creating complex interactive programs,
while retaining all the advantages of functional programming.

### Exercise 9.1.3: Sum and Product

**Task:**

Implement a program that:

1. Asks the user for two numbers
2. Repeats the prompt on invalid input, using recursion in the `IO` context
3. Displays their sum and product

```dotty
def readInput(): IO[String] = ???

def readNumber(): IO[Int] = ???

val sumAndProduct: IO[Unit] = ???
```

@:note(T🔍 Show Solution)

The following solution implements an interactive program for reading numbers
with input validation and computing their sum and product, using a functional approach with `IO`:

- **`readInput`** requests input and returns a string in the `IO` context;
- **`readNumber`** recursively handles invalid input, converting the string to a number via `toIntOption`;
- **`sumAndProduct`** composes two safe number reads via for-comprehension and prints the computed results,
  preserving referential transparency and deferring effect execution until `unsafeRun()` is called.

The code demonstrates **pure effect composition**, **exception-free error handling**,
and **interactivity** while preserving immutability.

```dotty
def readInput(): IO[String] =
  for
    _     <- PrintLine("Enter a number: ")
    input <- ReadLine
  yield input

def readNumber(): IO[Int] =
  readInput().flatMap: input =>
    input.toIntOption match
      case Some(num) => IO(num)
      case None =>
        PrintLine("Invalid number entered! Please retry.").flatMap: _ =>
          readNumber()

val sumAndProduct: IO[Unit] =
  for
    a <- readNumber()
    b <- readNumber()
    _ <- PrintLine(s"Sum: ${a + b}")
    _ <- PrintLine(s"Product: ${a * b}")
  yield ()
```

@:@

## Advantages and Limitations of the `IO` Approach

The `IO` (input/output) concept is a fundamental mechanism for working with effects in functional programming.
Its current implementation serves as a basic level of abstraction, providing interaction with the outside world
while preserving the purity of the main part of the program.

### Key Characteristics of the Approach

1. **Separation of pure code from code with side effects**
    - Explicit extraction of all side-effecting operations into a special `IO` context
    - Guarantee that functions without `IO` remain pure (deterministic and side-effect-free)
    - Forced indication of where external interaction occurs in the function type

2. **Reification of computations**
    - `IO` expressions are first-class language values
    - Capabilities:
        - Storing in collections
        - Passing as arguments
        - Dynamic construction
        - Composition into more complex operations

3. **Interpreted model**
    - Separation of computation description from execution
    - Ability to swap the interpreter without modifying client code
      (`converter` remains identical regardless of `IO` implementation)
    - Hiding the internal representation of `IO` from the user

### Detailed Advantages

1. **Clear code separation**
    - *Example*: Pure function `validateInput` returns `Either`, while `readInput` returns `IO[String]`
    - *Advantage*: Simplifies code analysis and refactoring

2. **Testability**
    - Capabilities:
        - Replacing real effects with test interpreters
        - Verifying logic without launching actual I/O operations
    - *Test example*:
      ```dotty
      val testIO = IO.pure("test") // Emulate input without actual reading
      assert(runTest(testIO) == expected)
      ```

3. **Composability**
    - Base operations:
        - `map` - result transformation
        - `flatMap` - sequential composition
    - *Example*:
      ```dotty
      val combined = readFile("data.txt").flatMap(parseContent)
      ```

4. **Execution flexibility**
    - Interpreter options:
        - Synchronous (blocking)
        - Asynchronous (callback-based)
        - Lazy (deferred execution)
        - Test (with mocked effects)

### Limitations of the Current Implementation

1. **Stack overflow issues**
    - *Cause*: Recursive nature of `flatMap` chains
    - *Manifestation*: Deep sequences of operations cause stack overflow
    - *Example of dangerous code*:
      ```dotty
      def recursiveIO(n: Int): IO[Unit] =
          IO.unit.flatMap(_ => if n > 0 then recursiveIO(n - 1) else IO.unit)
      ```
    - *Solution*: Requires a "lazy" execution mechanism or trampolines

2. **Opaque effects**
    - *Problems*:
        - No static analysis of possible effects
        - No information about the nature of the operation (read, write, network call)
        - Risk of infinite execution or hanging
    - *Example*:
      ```dotty
      val riskyIO: IO[String] = ??? // Unclear what exactly this effect does
      ```

3. **Synchronous execution model**
    - *Limitations*:
        - Thread blocking during I/O operations
        - Inefficient resource usage in highly loaded systems
        - Complexity of implementing concurrent scenarios
    - *Example*:
      ```dotty
      // Sequential blocking execution
      val syncOps = writeFile("a.txt").flatMap(readFile("b.txt"))
      ```

### Examples and Consequences of Limitations

**Case 1: Stack Issues**

```dotty
def deepChain(depth: Int): IO[Int] =
  (0 until depth).foldLeft(IO(0)): (acc, _) =>
    acc.flatMap(n => IO(n + 1))

deepChain(100000).unsafeRun()
// At depth > 100000, this will cause java.lang.StackOverflowError
```

**Case 2: Opaque Effects**

```dotty
val ambiguousProgram: IO[Unit] =
  for
    _ <- fetchURL("http://example.com")
    _ <- logToFile("debug.log")
    _ <- launchMissiles() // Effect hidden in the operation chain
  yield ()
```

### Ways to Address `IO` Limitations

To solve the stack overflow problem, *trampolining* is employed,
or a switch to *lazy execution* via containers like `TailRec`.
Effect opacity is addressed through *typed effects* (e.g., `IO[FileOperation, A]`)
or a *capabilities* system.
Asynchrony is achieved by integrating with `Future`/`Promise`
or using *green threads* (as in ZIO/Cats-Effect).
Resource control employs the *bracket pattern*,
and effect analysis uses *HKD (Higher-Kinded Data)* or *effect-tracking* at the type level.
These approaches preserve the advantages of `IO` while addressing its weaknesses.

## Conclusion

The current implementation of `IO` provides a solid foundation for working with effects, offering:

- Clear separation between pure and effectful code
- Rich program composition capabilities
- Flexibility in choosing execution strategies

However, there is room for improvement in the areas of:

- Performance (asynchrony, stack management)
- Expressiveness (describing the nature of effects)
- Safety (resource and error control)

---

**Links:**

- [Repository with exercises][sample]
- [Functional Programming in Scala, Second Edition, Chapter 13](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
