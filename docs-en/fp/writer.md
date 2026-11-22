# Functional Logger

## Description

`Writer` is for results to which another value is attached, acting as a kind of log.
One common use for `Writer` is recording sequences of steps in concurrent computations,
where standard imperative logging methods can lead to interleaved messages from different contexts.
The log for `Writer` computations is attached to the result,
so parallel computations can be launched without mixing logs.

Key characteristics:

- Combines a computation result (`A`) with an accumulating log (`W`)
- The log must provide a combining operation
- Guarantees execution order and log integrity even in concurrent computations
- An alternative to traditional impure logging

```dotty
case class Writer[W, A](run: () => (W, A))
```

Type parameters:

- `W` — log type (usually `String`, `List[String]`, or a dedicated log type)
- `A` — result type of the computation

## General-Purpose Functions

### map

Analogous to functional state, `map` allows, given a base journal `Writer[S, A]`
and a function to transform a value from type `A` to type `B`, to obtain `Writer[S, B]` —
a functional journal with computation of a value of type `B`.

Mathematical basis:

```math
map : (A → B) → Writer[W, A] → Writer[W, B]
```

Implementation:

```dotty
object Writer:
  extension [W, A](underlying: Writer[W, A])
    def map[B](f: A => B): Writer[W, B] =
      val (w, a) = underlying.run()
      Writer[W, B](() => (w, f(a)))
```

Usage example:

```dotty
val initial = Writer[String, Int](() => ("Initial value:", 1))

// Convert Int to Double, preserving the log
val writer = initial.map(x => x.toDouble * 1.5)
writer.run()
// ("Initial value:", 1.5)
```

### flatMap

The primary operation for sequentially combining computations while preserving and merging logs. 
Requires a log combining function.

Mathematical basis:

```math
flatMap : (A → Writer[W, B]) → Writer[W, A] → Writer[W, B]
```

Implementation:

```dotty
object Writer:
  extension [W, A](underlying: Writer[W, A])
    def flatMap[B](f: A => Writer[W, B])(using
        combine: (W, W) => W
    ): Writer[W, B] =
      Writer[W, B] { () =>
        val (w1, a) = underlying.run()
        val (w2, b) = f(a).run()
        (combine(w1, w2), b)
      }
```

Usage example:

```dotty
val initial = Writer[String, Int](() => ("Start:", 10))

def process(x: Int): Writer[String, Int] =
  if x > 5 then Writer(() => ("Greater than five,", x * 2))
  else Writer(() => ("Less than or equal to five,", x))

given combine: ((String, String) => String) = _ + _

val result = initial.flatMap(process)
result.run()
// ("Start:Greater than five,", 20)
```

### unit (pure)

A constructor that creates a `Writer` with a given value and an empty log. 

Mathematical basis:

```math
unit : A → Writer[W, A]
```

Implementation:

```dotty
object Writer:
  def unit[W, A](a: => A)(empty: W): Writer[W, A] =
    Writer[W, A](() => (empty, a))
```

Usage examples:

```dotty
// For String log
val w1 = Writer.unit(42)("")
w1.run()  // ("", 42)

// For List[String] log
val w2 = Writer.unit("result")(Nil)
w2.run()  // (List(), "result")
```

### Additional Useful Operations

#### tell

Adds a record to the log without changing the result:

```dotty
def tell[W](w: W): Writer[W, Unit] =
  Writer(() => (w, ()))
```

Example:

```dotty
given combine: ((String, String) => String) = _ + _

val computation =
  for
    _ <- Writer.tell("Computation start")
    x <- Writer.unit(10)("")
    _ <- Writer.tell(s"Received value: $x")
  yield x * 2

computation.run()
// ("Computation startReceived value: 10", 20)
```

#### listen

Returns not only the result, but also the current log:

```dotty
case class Writer[W, A](run: () => (W, A)):
  def listen: Writer[W, (A, W)] =
    Writer { () =>
      val (w, a) = run()
      (w, (a, w))
    }
```

## Practical Application

**Logging in pure functions:**

```dotty
def parseNumber(s: String): Writer[List[String], Int] =
  try
    val num = s.toInt
    Writer(() => (List(s"Successfully parsed: $num"), num))
  catch
    case _: NumberFormatException =>
      Writer(() => (List(s"Parse error: $s"), 0))

parseNumber("42").listen.run()
// (List(Successfully parsed: 42),(42,List(Successfully parsed: 42)))
parseNumber("42abc").listen.run()
// (List(Parse error: 42abc),(0,List(Parse error: 42abc)))
```

**Collecting metrics:**

```dotty
def processRequest(req: Request): Writer[Stats, Response] = 
  val start = System.currentTimeMillis()
  // ... processing ...
  val end = System.currentTimeMillis()
  val stats = Stats(1, end - start)
  Writer(() => (stats, response))
```

**Transactional operations:**

```dotty
def transfer(from: Account, to: Account, amount: Money): Writer[TransactionLog, Unit] =
  for
    _ <- Writer.unit(from.withdraw(amount))(TransactionLog.empty)
    _ <- Writer.unit(to.deposit(amount))(TransactionLog.empty)
    _ <- tell(TransactionRecord(from, to, amount, "Completed"))
  yield ()
```

## Exercises

### Exercise 7.1.1: Basic Operations

> [Repository with exercises and instructions for completion][sample]

**Task:** Implement a computation that:

1. Starts with the number 5
2. Adds a log message "Initial value: 5"
3. Multiplies the number by 2
4. Adds a log message "Multiply by 2"
5. Subtracts 3
6. Adds a log message "Subtract 3"

```dotty   
val computation: Writer[List[String], Int] = 
  ???
```  

**Examples:**

```dotty  
val result = computation.run()
// (List("Initial value: 5", "Multiply by 2", "Subtract 3"), 7)
```  

**Hint:** Use:

- `Writer.unit` for the initial value
- `flatMap` for sequential operations
- `List[String]` as the log type
- The `++` operation for combining lists

@:note(🔍 Show Solution)

```dotty
import Writer.*

given combine: ((List[String], List[String]) => List[String]) = _ ++ _

def computation: Writer[List[String], Int] =
  // Create initial `Writer` with number `5` and first message:
  Writer.unit(5)(List("Initial value: 5"))
    // Multiply by `2` and add a message:
    .flatMap(x => Writer(() => (List("Multiply by 2"), x * 2)))
    // Subtract `3` and add a message:
    .flatMap(x => Writer(() => (List("Subtract 3"), x - 3)))

val (log, result) = computation.run()
println(s"Log: $log")
println(s"Result: $result")
// Log: List(Initial value: 5, Multiply by 2, Subtract 3)
// Result: 7
``` 

**Alternative solution with for-comprehension:**

```dotty
def computationFor: Writer[List[String], Int] = 
  for
    x <- Writer.unit(5)(List("Initial value: 5"))
    y <- Writer(() => (List("Multiply by 2"), x * 2))
    z <- Writer(() => (List("Subtract 3"), y - 3))
  yield z
```

@:@

### Exercise 7.1.2: Composition of Operations

**Task:** Create a function `sqrtWithLog` that:

1. Takes a `Double`
2. Checks that the number is non-negative (if not — adds "Error: negative number" to the log)
3. Computes the square root (for negative numbers returns `Double.NaN`)
4. Adds "Square root computed" to the log (for successful computations only)
5. Returns `Writer[List[String], Double]`

```dotty
def sqrtWithLog(x: Double): Writer[List[String], Double] =
  ???
```  

**Examples:**

```dotty  
sqrtWithLog(9.0).run()
// (List("Square root computed"), 3.0)

sqrtWithLog(-4.0).run()
// (List("Error: negative number"), Double.NaN)
```  

**Hint:**

- Use `tell` for adding error messages
- For the successful case, use `unit` with the result and message
- Note that the square root computation message is added only for positive numbers

@:note(🔍 Show Solution)

The function `sqrtWithLog` computes the square root of a number `x` of type `Double`, 
with logs recorded as a list of strings: for negative `x` it logs an error and returns `NaN`, 
and for non-negative values — returns the root and a success message.

```dotty
def sqrtWithLog(x: Double): Writer[List[String], Double] =
  if x < 0 then
    tell(List("Error: negative number")).map(_ => Double.NaN)
  else
    val result = math.sqrt(x)
    unit(result)(List("Square root computed"))
```

@:@

### Exercise 7.1.3: Data Validation

**Task:** Create a function `validatePerson` that checks:

1. Age >= 18 (otherwise "Age is less than 18")
2. Name is not empty (otherwise "Name cannot be empty")
3. The log must contain all errors (don't stop at the first one)

**Signature:**

```dotty
case class Person(name: String, age: Int)

def validatePerson(p: Person): Writer[List[String], Person] = ???
```

**Example:**

```dotty
validatePerson(Person("", 17)).run()
// (List("Name cannot be empty", "Age is less than 18"), Person("", 17))

validatePerson(Person("Alice", 25)).run()
// Returns Writer(Nil, Person("Alice", 25))
```

@:note(🔍 Show Solution)

The function `validatePerson` validates a `Person` using `Writer` to accumulate validation errors: 
if the age is less than 18, the message `"Age is less than 18"` is added, and if the name is empty — `"Name cannot be empty"`. 
The result is a `Writer` containing either a list of errors, or a valid `Person` object if no errors. 

```dotty  
def validatePerson(p: Person): Writer[List[String], Person] =
  val ageCheck: Writer[List[String], Unit] =
    if p.age < 18 then
      Writer.tell[List[String]](List("Age is less than 18"))
    else Writer.unit[List[String], Unit](())(Nil)
  val nameCheck: Writer[List[String], Unit] =
    if p.name.isEmpty then
      Writer.tell[List[String]](List("Name cannot be empty"))
    else Writer.unit[List[String], Unit](())(Nil)

  for
    _ <- ageCheck
    _ <- nameCheck
  yield p
```  

@:@

### Exercise 7.1.4: Log Transformation

**Task:** Create a `mapLog` method for `Writer` that allows transforming values in the log, 
while keeping the computation result unchanged.

**Requirements:**

1. The method must accept a log transformation function `W => W2`
2. Return a new `Writer` with the transformed log, but the same value `A`

**Signature:**

```dotty
def mapLog[W2](f: W => W2): Writer[W2, A] = ???
```

**Example:**

```dotty
Writer(() => (List("a", "b"), 42))
  .mapLog(_.mkString(","))
  .run()
// Result: ("a,b", 42)
```  

@:note(🔍 Show Solution)

The `mapLog` method transforms the log of `Writer[W, A]`, 
applying function `f` to the log value `W`, leaving the result `A` unchanged; 
at the same time, the computation remains lazy — the original `Writer` is executed only upon calling `run()`, 
returning a new tuple `(f(w), a)`. 
This allows changing the format or content of the log (e.g., converting a list to a string), 
without affecting the main computation.

```dotty
def mapLog[W2](f: W => W2): Writer[W2, A] =
  Writer { () =>
    val (w, a) = underlying.run()
    (f(w), a)
  }
```

@:@

---

**References:**

- [Link to the exercises repository][sample]
- [Three Useful Monads](https://habr.com/ru/articles/184722/)
- Category Theory for Programmers - Bartosz Milewski:
    - [Category Theory for Programmers](https://henrychern.wordpress.com/2022/09/15/%d1%82%d0%b5%d0%be%d1%80%d0%b8%d1%8f-%d0%ba%d0%b0%d1%82%d0%b5%d0%b3%d0%be%d1%80%d0%b8%d0%b9-%d0%b4%d0%bb%d1%8f-%d0%bf%d1%80%d0%be%d0%b3%d1%80%d0%b0%d0%bc%d0%bc%d0%b8%d1%81%d1%82%d0%be%d0%b2/)
    - [Category Theory for Programmers, Functoriality](https://bartoszmilewski.com/2015/02/03/functoriality/)
    - [Category Theory for Programmers, Kleisli Categories](https://bartoszmilewski.com/2014/12/23/kleisli-categories/)
- [Cats](https://typelevel.org/cats/datatypes/writer.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Writer.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Writer.html)
- [Scala with Cats](https://scalawithcats.com/dist/scala-with-cats.html#writer-monad)

[sample]: https://gitverse.ru/scalabook/fp
