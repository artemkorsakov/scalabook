# Free Monad

## Introduction

If we use `TailRec` as our `IO` type, we avoid the stack overflow problem, but two other problems remain:

1. `TailRec` **does not explicitly indicate** what kinds of effects can occur.
2. `TailRec` **lacks a parallelism mechanism** or means to perform I/O without blocking the current thread.

Let's examine in more detail how `TailRec` works.

### Interpretation Problems

When executing the `run` method, the interpreter analyzes the program represented as `TailRec[A]`.
For example, if the program looks like this:

```dotty
FlatMap(Suspend(s), k)
```

the interpreter performs the following steps:

1. Calls `s()`, which represents a delayed computation (`Function0[A]`: `() => A`).
2. Receives the result and passes it to continuation `k`.
3. The result of `k` may again be `FlatMap`, and the process repeats.

However, the interpreter **knows nothing** about what `s()` actually does —
it could be reading a file, making a network request, printing to the console, etc.
This behavior makes the program **opaque** to the interpreter.
The only thing it can do is call `s()` and wait for the result.

Moreover, the interpreter **cannot handle asynchronous effects**, because `s()` is an ordinary synchronous call.
If we wanted our program to support asynchrony, we would need a different approach.

## Asynchronous Computations

To solve the asynchrony problem, we can replace `Function0` with the `Par[A]` type, representing parallel computation.
We will call this type `Async[A]`.

### Defining `Async`

```dotty
enum Async[A]:
  case Return(a: A)
  case Suspend(resume: Par[A])
  case FlatMap[A, B](sub: Async[A], k: A => Async[B]) extends Async[B]

  def flatMap[B](f: A => Async[B]): Async[B] =
    FlatMap(this, f)

  def map[B](f: A => B): Async[B] =
    flatMap(a => Return(f(a)))
```

Here `Suspend` takes a value of type `Par[A]`, which can be executed asynchronously.
This allows the interpreter to run computations in parallel.

### Implementing `run`

The `run` method now needs to return `Par[A]` instead of `A`.
We also add a `step` function that optimizes the program structure before execution:

```dotty
@annotation.tailrec
final def step: Async[A] = this match
  case FlatMap(FlatMap(x, f), g) => x.flatMap(a => f(a).flatMap(g)).step
  case FlatMap(Return(x), f)     => f(x).step
  case _                         => this

def run: Par[A] = step match
  case Return(a)  => Par.unit(a)
  case Suspend(r) => r
  case FlatMap(x, f) => x match
      case Suspend(r) => r.flatMap(a => f(a).run)
      case _ => sys.error("Impossible, since `step` eliminates these cases")
```

- **`step`**: simplifies the program structure by flattening nested `FlatMap`s.
- **`run`**: returns the program result as `Par[A]`, allowing asynchronous execution.

Thus, `Async` supports **asynchronous execution** and **stack safety**.

## Generalizing to the Free Monad

Although `Async` is already a good solution, we can abstract further.
Instead of fixing on a specific type (e.g., `Function0` or `Par`),
we generalize our construction to any effect constructor `F`.

### Definition of `Free`

```dotty
enum Free[F[_], A]:
  case Return(a: A)
  case Suspend(s: F[A])
  case FlatMap[F[_], A, B](s: Free[F, A], f: A => Free[F, B])
      extends Free[F, B]
  
  def flatMap[B](f: A => Free[F, B]): Free[F, B] =
    FlatMap(this, f)
  
  def map[B](f: A => B): Free[F, B] =
    flatMap(a => Return(f(a)))      
```

- **`Return`**: completed computation with result `a`.
- **`Suspend`**: suspension containing an effect `F[A]`.
- **`FlatMap`**: sequential composition of two computations.

This structure is called a [**free monad**][free] (Free Monad),
because it allows creating programs that describe sequences of actions,
without binding to a specific implementation of those actions.

### Type Aliases

Now `TailRec` and `Async` can be expressed through `Free`:

```dotty
type TailRec[A] = Free[Function0, A]
type Async[A]   = Free[Par, A]
```

- **`TailRec`** uses `Function0` to describe effects.
- **`Async`** uses `Par` for asynchrony support.

## Advantages of the Free Monad

1. **Flexibility**:
   - You can use any effect implementation (`Function0`, `Par`, `IO`, etc.).
   - Not dependent on a specific way of executing effects.

2. **Transparency**:
   - The program explicitly describes its effects through `Suspend`.
   - The interpreter can understand and process them.

3. **Stack Safety**:
   - All recursive calls are in tail position.
   - No risk of stack overflow.

4. **Asynchrony Support**:
   - With `Free[Par, A]` you can build programs that run in parallel.

5. **Interpretability**:
   - The program can be interpreted differently depending on context (testing, logging, real execution).

## The Free Monad

The free monad `Free[F[_], A]` provides a universal mechanism for modeling effects in functional programs.
It combines:

- **Stack safety** (through tail recursion),
- **Asynchrony** (through choosing the right effect constructor),
- **Flexibility** (support for various effect implementations).

This makes `Free` a powerful tool for building complex and reliable functional applications.

### Exercise 9.3.1: Implementing the Monad Interface for `Free`

> [Repository with exercises and instructions][sample]

**Task:**

`Free` is a [monad][monad] for any choice of `F`.
Implement the `unit` and `flatMap` methods on the `Free` enum and provide a `Monad` instance for `Free[F, _]`.

```dotty
given freeMonad[F[_]]: Monad[[x] =>> Free[F, x]] with
  def unit[A](a: => A): Free[F, A] = ???
  extension [A](fa: Free[F, A])
    def flatMap[B](f: A => Free[F, B]): Free[F, B] = ???
```

This instance makes `Free[F, _]` a full-fledged monad, allowing its use in functional chains,
for-comprehension, and other constructs requiring monadic behavior.

@:note(🔍 Show solution)

The `unit` method should take a value `A` and return `Free[F, A]`,
which completes immediately without invoking any effects.
The `Return` constructor is used for this purpose.

The `flatMap` method should take `Free[F, A]` and a function `A => Free[F, B]`,
and return a new `Free[F, B]` representing the sequential execution of two steps.
The `FlatMap` constructor is used for this purpose:

```dotty
object Free:
  given freeMonad[F[_]]: Monad[[x] =>> Free[F, x]] with
    def unit[A](a: => A): Free[F, A] = Return(a)

    extension [A](fa: Free[F, A])
      def flatMap[B](f: A => Free[F, B]): Free[F, B] = fa.flatMap(f)
```

@:@

### Exercise 9.3.2: Implementing the Tail-Recursive Interpreter `runTrampoline`

**Task:**

Implement a specialized tail-recursive interpreter `runTrampoline` for running programs of type `Free[Function0, A]`.
This interpreter should safely execute the program without causing a stack overflow.

```dotty
extension [A](fa: Free[Function0, A])
  @annotation.tailrec
  def runTrampoline: A = ???
```

@:note(🔍 Show solution)

The `runTrampoline` method is implemented using recursive pattern matching.
Depending on the current program state (`Return`, `Suspend`, or `FlatMap`),
we perform the corresponding actions:

- If the current step is `Return(a)`, return the result `a`.
- If the current step is `Suspend(ta)`, call the function `ta()`.
- If the current step is `FlatMap(fx, f)`, analyze `fx`:
  - If `fx` is `Return(x)`, apply `f(x)` and recursively call `runTrampoline`.
  - If `fx` is `Suspend(tx)`, call `tx()`, then apply `f` to the result.
  - If `fx` is another `FlatMap(fy, g)`, right-associate `flatMap` to avoid nesting and continue execution.

```dotty
extension [A](fa: Free[Function0, A])
  @annotation.tailrec
  def runTrampoline: A = fa match
    case Free.Return(a)   => a
    case Free.Suspend(ta) => ta()
    case Free.FlatMap(fx, f) => fx match
        case Free.Return(x)   => f(x).runTrampoline
        case Free.Suspend(tx) => f(tx()).runTrampoline
        case Free.FlatMap(fy, g) =>
          fy.flatMap(y => g(y).flatMap(f)).runTrampoline
```

@:@

### Exercise 9.3.3: Implementing a Universal Interpreter `run` for `Free[F, A]`

**Task:**

Implement a universal interpreter for the `Free[F, A]` type that uses a `Monad[F]` instance.
This interpreter should transform a `Free[F, A]` program into a computation of type `F[A]`.
Use the tail-recursive `step` function to simplify the program structure before execution.

```dotty
enum Free[F[_], A]:
  ...
 
  def run(using F: Monad[F]): F[A] = ???
 
  @annotation.tailrec
  final def step: Free[F, A] = ???
```

@:note(🔍 Show solution for step)

The `step` method is intended to simplify the structure of `Free[F, A]` so it contains only basic constructors.

```dotty
@annotation.tailrec
final def step: Free[F, A] = this match
  case FlatMap(FlatMap(fx, f), g) => fx.flatMap(x => f(x).flatMap(g)).step
  case FlatMap(Return(x), f)      => f(x).step
  case _                          => this
```

- If we encounter nested `FlatMap`s, we right-associate them and call `step` again.
- If we encounter `FlatMap(Return(x), f)`, we apply `f(x)` and continue simplifying.
- In all other cases, we return the current value, as it is already simplified.

@:@

@:note(🔍 Show solution for run)

The `run` method uses the result of `step` to obtain a simplified version of the program and executes it with `Monad[F]`.

```dotty
def run(using F: Monad[F]): F[A] = step match
  case Return(a) => F.unit(a)
  case Suspend(fa) => fa
  case FlatMap(Suspend(fa), f) => fa.flatMap(a => f(a).run)
  case FlatMap(_, _) =>
    sys.error("Impossible, `step` eliminates these cases")
```

- **`Return(a)`**: Return `F.unit(a)`, completing the computation.
- **`Suspend(fa)`**: Return `fa` itself, as it is already an `F[A]` value.
- **`FlatMap(Suspend(fa), f)`**: Execute `fa` and bind the result to continuation `f`.
- **`FlatMap(_, _)`**: Impossible branch, since `step` eliminates such cases.
  The exception is added for exhaustiveness of pattern matching.

This interpreter:

1. Uses `step` to simplify the program structure.
2. Supports executing programs through the `F` monad.
3. Guarantees the program will be correctly interpreted, regardless of its initial form.

@:@

## Summary: What Is `Free[F, A]`?

`Free[F, A]` is a universal data structure in functional programming
that allows modeling computations as a sequence of operations.
It represents a **recursive tree**, where:

- **Root** — the current computation step.
- **Branches** — describe possible actions or effects (via type `F`).
- **Leaves** — contain computation results of type `A`.

Thus, `Free[F, A]` can be thought of as an **abstract syntax tree (AST)** of a program,
written in a language where each instruction is defined by type `F`, and variables — by type `A`.

### Structure of `Free[F, A]`

The `Free[F, A]` type consists of three main constructors:

1. **`Return(a: A)`**  
   This is the final computation step — a "leaf of the tree".  
   The value `a` is the result we want to obtain.

2. **`Suspend(s: F[A])`**  
   Represents a suspended computation.  
   Here `s` is a value of type `F[A]`, which can be executed later.  
   For example, this could be a database query, file read, etc.

3. **`FlatMap(s: Free[F, A], f: A => Free[F, B])`**  
   Describes the binding of two steps: first `s` is executed, then its result is passed to `f` to continue computation.  
   This allows creating chains of actions.

### Why Is `Free[F, A]` Called a "Free Monad"?

`Free[F, A]` is a **monad** because it implements two key operations:

- **`unit`** — creates the initial value (`Return(a)`).
- **`flatMap`** — binds two computation steps (`FlatMap`).

It is precisely through these operations that `Free[F, A]` supports program composition
and can be used in functional constructs such as `for`-comprehension, pure computation chains, etc.

The word **"free"** here means that `Free[F, A]` imposes no restrictions on the type `F`.
This makes it very flexible: you can use any structure to describe effects,
whether it is `Function0`, `Par`, `IO`, or even a custom type such as `Console`.

### How Does the Interpreter Work?

To obtain a result from `Free[F, A]`, you need an **interpreter** — a program that traverses the tree and executes each step.
It must be able to process all layers of `F` that can appear in the structure.

For example, if `F` is `Console`, then the interpreter will execute `ReadLine` and `PrintLine` commands.
If `F` is `Par`, then it will launch parallel computations.

### Interaction Protocol

The type `F` defines an **interaction protocol** between the program and its environment. For example:

- If `F = Function0`, computations happen synchronously.
- If `F = Par`, computations happen asynchronously.

By choosing different `F`, you can precisely control what kinds of interactions are allowed in your program.
This makes `Free[F, A]` a powerful tool for building pure, testable, and extensible systems.

### Conclusion

Thus, `Free[F, A]` is:

- A recursive computation tree.
- A flexible monad that works with any type `F`.
- A means of modeling programs as abstract syntax trees.
- A platform for creating interpretable, pure, and stack-safe programs.

Using `Free[F, A]`, you get the ability to build programs that are easy to test,
modify, and execute in different environments, without losing purity and predictability.

## A Monad Supporting Only Console I/O

Previously, we considered using `Free[Function0, A]` for building pure programs with side effects.
However, the `Function0` type is too general and does not allow explicitly modeling specific kinds of effects —
such as reading from the console or printing to the screen.

To gain more control over effects, we can use an **algebraic data type**
that describes only the operations we want to support.
In this case, we will limit ourselves to **console I/O**.

### Defining `Console[A]`

We define the type `Console[A]`, which represents a computation
returning a value of type `A`, but limited to two possible actions:

1. `ReadLine`: an operation that reads a line from the console.
2. `PrintLine(line: String)`: an operation that prints a line to the console.

```dotty
enum Console[A]:
  case ReadLine                extends Console[Option[String]]
  case PrintLine(line: String) extends Console[Unit]

  def toPar: Par[A] = this match
    case ReadLine        => Par.lazyUnit(Try(readLine()).toOption)
    case PrintLine(line) => Par.lazyUnit(println(line))

  def toThunk: () => A = this match
    case ReadLine        => () => Try(readLine()).toOption
    case PrintLine(line) => () => println(line)
```

- `ReadLine` models an input operation — it returns `Option[String]`,
  because input may be empty or terminated by the user.
- `PrintLine(line)` models an output operation — it returns nothing (`Unit`), since printing is a side effect.
- `toPar` and `toThunk` are interpreters that transform `Console[A]`
  into an async computation (`Par`) or a delayed function (`Function0`).

### Embedding `Console` in `Free`

Now, to use `Console` within the free monad `Free[F, A]`, we can create programs using the `Suspend` constructor.

```dotty
object Console:
  def readLn: Free[Console, Option[String]] =
    Suspend(ReadLine)
 
  def printLn(line: String): Free[Console, Unit] =
    Suspend(PrintLine(line))
```

- `readLn` creates a program `Free[Console, Option[String]]` that describes reading a line from the console.
- `printLn` creates a program `Free[Console, Unit]` that describes printing a line to the console.

These programs **do not execute immediately** — they simply describe a sequence of actions.
Execution will happen later when we pass them to an interpreter.

### Example Program with Console I/O

Using `Free[Console, A]`, we can write programs that interact with the console:

```dotty
val f1: Free[Console, Option[String]] =
  for
    _  <- printLn("I can only interact with the console.")
    ln <- readLn
  yield ln
```

This program does the following:

1. Prints the message `"I can only interact with the console."`.
2. Reads a line from the console.
3. Returns this line as the result.

The program does not contain any other side effects — only those described in `Console`.

> ⚠️ Scala does not prohibit the programmer from using other effects (e.g., file or network operations),
> but if we adhere to discipline and use only `Console`, then the program remains pure and easily testable.

### Advantages of Using `Console` over `Function0`

- **Clarity**: We know exactly what effects can occur in the program.
- **Testability**: We can replace `Console` with a "pure" interpreter that does not perform real I/O, but saves data to a buffer.
- **Reusability**: `Console` can be interpreted differently:
  through `Par`, `Function0`, or even through `Reader`, where input is predefined.

## How to Actually Run a `Free[Console, A]`?

Previously, we described `Free[F, A]` as a universal data structure
that allows building pure programs with side effects.
However, to actually **run** such a program and obtain a result, we need an interpreter —
a function that can "execute" all the steps described in the `Free` tree.

In the case of `Free[Console, A]`, where `Console[A]` is a type modeling console operations (e.g., `ReadLine` and `PrintLine`),
a problem arises: `Console` itself **is not a monad**,
and therefore we cannot directly use the `run` method, which requires a `Monad[F]` instance.

### Why `Console` Cannot Be a Monad

Let's look at the definition of `Console`:

```dotty
enum Console[A]:
  case ReadLine                extends Console[Option[String]]
  case PrintLine(line: String) extends Console[Unit]
```

This is a simple algebraic type that describes possible actions:
reading a line from the console or printing a line to the screen.
But it has no `flatMap` implementation, making it **unsuitable for use as a monad**.

If we try to add `flatMap`, we get something like:

```dotty
def flatMap[B](f: A => Console[B]): Console[B] = this match
  case ReadLine => ???
  case PrintLine(s) => ???
```

The problem here is that for `ReadLine` we need a value of type `Option[String]` to pass to `f`.
But we don't have it yet — it will only appear when someone performs real keyboard input.
That is, we **cannot implement `flatMap` without access to the real effect** that has not yet occurred.

### Generalizing `run` through `runFree`

Since `Console` does not form a monad, but we want to be able to run `Free[Console, A]` programs,
we need to find a way to **transform** `Console` into a type that is already a monad.
For example, we can use `Function0` or `Par`, which support `flatMap`.

For this, we generalize the `run` method by introducing a new `runFree` signature:

```dotty
enum Free[F[_], A]:
  ...
  def runFree[G[_]](t: [x] => F[x] => G[x])(using G: Monad[G]): G[A] =
    step match
      case Return(a)              => G.unit(a)
      case Suspend(r)             => t(r)
      case FlatMap(Suspend(r), f) => t(r).flatMap(a => f(a).runFree(t))
      case FlatMap(_, _) =>
        sys.error("Impossible, `step` eliminates these cases")
```

This function takes:

- `t`: a **polymorphic function** that transforms any `F[x]` into `G[x]`.
- `G`: a **`Monad[G]` instance** that allows working with `flatMap`.

Thus, `runFree` allows interpreting `Free[F, A]` in any other monadic context `G`,
if we know how to translate `F[x]` into `G[x]`.

### The Polymorphic Function: `[x] => F[x] => G[x]`

The parameter `t` in `runFree` has type `[x] => F[x] => G[x]`.
This is a [**polymorphic function**](https://docs.scala-lang.org/scala3/reference/new-types/polymorphic-function-types.html) that works for any type `x`.
It guarantees that we can translate `F[Int]` into `G[Int]`, `F[String]` into `G[String]`, and so on,
but **not** `F[Int]` into `G[Boolean]`.

Example:

```dotty
val toThunk: [x] => Console[x] => () => x = [x] => (c: Console[x]) => c.toThunk
```

Here `toThunk` is a function that takes any `Console[x]` and returns a delayed computation `() => x`.
This approach allows us to preserve types and correctly chain values in monadic chains.

#### Why Isn't a Simple `F[A] => G[A]` Enough?

Let's figure out **why we specifically need a polymorphic function**, and not just `F[A] => G[A]`.

Suppose we have a program that reads a number from the console:

```dotty
def readInt: Free[Console, Option[Int]] =
  for
    ln  <- readLn
  yield ln.flatMap(_.toIntOption)
```

This program has type `Free[Console, Option[Int]]`, but its internal structure may contain intermediate steps
that work with other types.
For example, `readLn` returns `Option[String]`, and only inside `FlatMap` do we transform it into `Option[Int]`.

Thus, when interpreting the `readInt` program, we may encounter `Free[Console, String]`,
`Free[Console, Int]`, and even `Free[Console, Unit]` if there is a `PrintLine` call somewhere.

If we try to use a simple function `t: F[A] => G[A]`,
it would work **only for one specific type `A`**.
But such a function **will not work** if the program contains other types internally.

Suppose we wanted to change the `runFree` signature to:

```dotty
def runFree[G[_], B](t: F[B] => G[B])(using G: Monad[G]): G[A]
```

This signature says: "I have a function `t` that knows how to translate `F[B]` into `G[B]`, and I want to apply it to a `Free[F, A]` program."

But this **will not work**, because `runFree` must handle **any** type `x`
that may appear in a `Free[F, A]` program.
That is, it must accept a function that works **for any `x`**, not just for a specific `B`.

### Dependence on `Monad` Instances

To use `runFree`, the target monad `G` must have implementations of `unit` and `flatMap`.
Therefore, we must define the corresponding `Monad` instances:

```dotty
given Monad[Function0] with
  def unit[A](a: => A): () => A =
    () => a

  extension [A](fa: () => A)
    def flatMap[B](f: A => () => B): () => B =
      f(fa())

given parMonad: Monad[Par] with
  def unit[A](a: => A): Par[A] = Par.unit(a)

  extension [A](fa: Par[A])
    def flatMap[B](f: A => Par[B]): Par[B] =
      Par.flatMap(fa)(f)      
```

### Example: Interpreting `Free[Console, A]` into `Function0`

With `runFree`, we can easily create an interpreter
that transforms our `Free[Console, A]` program into a delayed function `() => A`, using `Function0`:

```dotty
extension [A](fa: Free[Console, A])
  def toThunk: () => A =
    fa.runFree([x] => (c: Console[x]) => c.toThunk)
```

Now, to execute the program, we just call `toThunk()`:

```dotty
val program: Free[Console, Option[String]] =
  for
    _  <- printLn("Enter text:")
    ln <- readLn
  yield ln

val result: Option[String] = program.toThunk()
```

### Asynchronous Variant: Interpreting into `Par`

Similarly, we can interpret the program in an asynchronous context, using `Par`:

```dotty
extension [A](fa: Free[Console, A])
  def toPar: Par[A] =
    fa.runFree([x] => (c: Console[x]) => c.toPar)
```

Now `toPar` returns `Par[A]`, which allows running the program in parallel.

### Exercise 9.3.4: Implementing `translate`

**Task:**

We have already implemented the `toThunk` function, which allows running a `Free[Console, A]` program
by transforming it into a delayed function `() => A`. However, it turns out that `toThunk` **is not stack-safe**.

The reason is that the `flatMap` method for `Function0` is not optimized for tail recursion.
This leads to the fact that when executing a program with many sequential steps
(e.g., chains of dozens of `FlatMap`s), a **stack overflow** may occur,
similar to the problem we encountered with naive `IO`.

To solve this problem, we need to find a way to **stack-safely** execute a `Free[Console, A]` program.

The `translate` function is a generalized mechanism that allows **translating a `Free[F, A]` program
into a `Free[G, A]` program**, using some transformation between `F[x]` and `G[x]`.

Unlike `runFree`, which transforms `Free[F, A]` into a value of type `G[A]`,
`translate` preserves the structure as `Free[G, A]`.

Implement `translate`:

```dotty
enum Free[F[_], A]:
  def translate[G[_]](fToG: [x] => F[x] => G[x]): Free[G, A] = ???
```

@:note(🔍 Show solution)

We implement `translate` through `runFree`.
We can use `runFree` to get `Free[G, A]` if we define `H[x] = Free[G, x]`.
Then `runFree` will work as follows:

1. Takes `Free[F, A]`.
2. Transforms each `Suspend(f)` into `Free[G, A]` using `Suspend(fToG(f))`.
3. Returns a new `Free[G, A]` program.

```dotty
enum Free[F[_], A]:
  ...
  def translate[G[_]](fToG: [x] => F[x] => G[x]): Free[G, A] =
    runFree([x] => (fx: F[x]) => Suspend(fToG(fx)))
```

Thus, `translate` transforms our program into a new program where all effects `F[x]` are replaced with `G[x]`.

@:@

### Exercise 9.3.5: Implementing a Stack-Safe `unsafeRunConsole`

Now that we have `translate`, we can use it together with `runTrampoline`
to execute a `Free[Console, A]` program **stack-safely**.

Implement `runTrampoline`:

```dotty
extension [A](fa: Free[Console, A])
  def unsafeRunConsole: A = ???
```

@:note(🔍 Show solution)

1. We call `translate`, passing in a function that transforms `Console[x]` into `Function0[x]` (a delayed computation).
2. The result is a `Free[Function0, A]` program.
3. This program can be safely executed using `runTrampoline`.

```dotty
extension [A](fa: Free[Console, A])
  def unsafeRunConsole: A =
    fa.translate([x] => (c: Console[x]) => c.toThunk).runTrampoline
```

- `translate([x] => (c: Console[x]) => c.toThunk)` transforms the `Free[Console, A]` program into `Free[Function0, A]`.
- `.runTrampoline` executes the program safely, using tail recursion.

@:@

### Summary

To run a program of type `Free[Console, A]`, three key steps are needed:

1. **Define the interpretation of each step**:
   Each element of the program, such as `ReadLine` or `PrintLine`, must be translated into the corresponding action.
   This is achieved with a translation function such as `toThunk`, `toPar`, `toState`, etc.

2. **Transform effects**:
   We translate the type `Console[x]` into another monadic type that supports the desired execution mode —
   whether it is synchronous computation (`Function0`), async processing (`Par`), or something else.

3. **Use `runFree` for interpretation**:
   The `runFree` method applies the translator to all program steps,
   transforming the program into a value of the target monad.
   Thus, we can execute the program in any context that supports `Monad`.

#### The Power of `Free[F, A]`

The type `Free[F, A]` represents a program written in a language where each instruction is defined via `F`.
For example, if `F = Console`, then the language contains two instructions: `ReadLine` and `PrintLine`.

`Free` provides:

- **Recursive structure** (`Suspend`, `Return`, `FlatMap`),
- **A mechanism for composing actions**, allowing the construction of complex programs from simple steps,
- **Flexibility** in choosing the implementation of effects (`F`).

This makes `Free` a universal means of modeling effects:
you can use it not only for console I/O,
but also for working with the file system, network, databases, and much more, simply by changing the type `F`.

#### Application Examples

- **Synchronous execution**: `Free[Function0, A]` → `() => A`
- **Asynchronous execution**: `Free[Par, A]` → `Par[A]`
- **Pure tests**: `Free[Console, A]` → `ConsoleReader[A]` or `ConsoleState[A]`

Each of these options allows preserving purity and predictability, even in the presence of side effects.

#### Advantages of Using `Free`

- **Purity**: The program is separated from the implementation of effects.
- **Testability**: We can replace real effects with pure simulations.
- **Extensibility**: Adding new kinds of effects does not require modifying existing code.
- **Stack Safety**: Tail recursion prevents stack overflow.

#### Conclusion

`Free[F, A]` is a powerful abstract mechanism that allows writing programs with side effects
that remain pure, testable, and flexible.
It provides full control over when and how these effects are executed,
which makes it a valuable tool in functional programming.

## Pure Interpreters

One of the key advantages of free monads (`Free[F, A]`) is their **purity and separation of the program from its execution**.
A program described as `Free[Console, A]` is not something that "executes" automatically.
It is simply a **data structure** that describes a sequence of actions.
How exactly it is executed depends on the **interpreter**.

An interpreter is a function that transforms actions of type `F` into concrete computations. For example:

- An interpreter can run the program on a real console.
- It can simulate console interaction for testing.
- It can log all actions or save them for later analysis.
- Or it may not perform **any side effects at all**, using pure values instead.

### Why Can an Interpreter Be Pure?

The type `Free[Console, A]` is an abstraction that does **not require** us to use any side effects.
There is **no keyboard reading or screen printing built into the type itself**.
Everything is done by the interpreter.

If we want to test a `Free[Console, A]` program, we do not need to run it in a real environment.
We can use a **pure interpreter** that will:

- Return pre-defined input data instead of `ReadLine`.
- Record all `PrintLine` commands in a list for verification.
- Not use any side effects.

### Example 1: Interpreter with Fixed Input — `ConsoleReader`

Suppose we want to write an interpreter that **always returns the same string** on every `ReadLine` call.
This is useful for testing.

We can create a type `ConsoleReader[A]` that represents a function `String => A`,
where `String` is the input string (e.g., user input), and `A` is the program result.

```dotty
case class ConsoleReader[A](run: String => A):
  def map[B](f: A => B): ConsoleReader[B] =
    ConsoleReader(r => f(run(r)))
 
  def flatMap[B](f: A => ConsoleReader[B]): ConsoleReader[B] =
    ConsoleReader(r => f(run(r)).run(r))
```

This type supports `map` and `flatMap`, so it can be a monad:

```dotty
object ConsoleReader:
  given monad: Monad[ConsoleReader] with
    def unit[A](a: => A) = ConsoleReader(_ => a)
    
    extension [A](fa: ConsoleReader[A])
      def flatMap[B](f: A => ConsoleReader[B]) = fa.flatMap(f)
```

Now let's add a `toReader` method to `Console` that allows transforming each program step into a pure value:

```dotty
enum Console[A]:
  ...
  def toReader: ConsoleReader[A] = this match
    case ReadLine     => ConsoleReader(in => Some(in))
    case PrintLine(_) => ConsoleReader(_ => ())
```

Then, to interpret the entire program, we use `runFree`, passing `toReader` as the translator:

```dotty
extension [A](fa: Free[Console, A])
  def toReader: ConsoleReader[A] =
    fa.runFree([x] => (c: Console[x]) => c.toReader)
```

Example usage:

```dotty
val program: Free[Console, Option[String]] =
  for
    _  <- printLn("Enter text:")
    ln <- readLn
  yield ln

val testInput = "Hello, world!"
val result: Option[String] = program.toReader.run(testInput)
// val result: Option[String] = Some(Hello, world!)
```

The result will be `"Hello, world!"`, since `readLn` will return this string, and `printLn` will be ignored.

### Example 2: I/O Simulation with Buffers — `ConsoleState`

If we need a more **realistic simulation** of console interaction,
we can use **state** — two lists: one for input data, another for output.

```dotty
case class Buffers(in: List[String], out: Vector[String])

case class ConsoleState[A](
  run: Buffers => (A, Buffers)
)

object ConsoleState:
  given monad: Monad[ConsoleState] with
    def unit[A](a: => A) = ConsoleState(b => (a, b))
    extension [A](fa: ConsoleState[A])
      def flatMap[B](f: A=> ConsoleState[B]) =
        ConsoleState { buffers =>
          val (a, newBuffers) = fa.run(buffers)
          f(a).run(newBuffers)
        }
```

Now let's implement `toState` for `Console`:

```dotty
enum Console[A]:
  ...
  def toState: ConsoleState[A] = this match
    case ReadLine =>
      ConsoleState: bufs =>
        bufs.in match
          case Nil    => (None, bufs)
          case h :: t => (Some(h), bufs.copy(in = t))
    case PrintLine(line) =>
      ConsoleState(bufs => ((), bufs.copy(out = bufs.out :+ line)))
```

Interpreter for `Free[Console, A]`:

```dotty
extension [A](fa: Free[Console, A])
  def toState: ConsoleState[A] =
    fa.runFree([x] => (c: Console[x]) => c.toState)
```

Example usage:

```dotty
val program: Free[Console, Option[String]] =
  for
    _  <- printLn("Enter text:")
    ln <- readLn
  yield ln

val initialState         = Buffers(List("Hello"), Vector())
val (result, finalState) = program.toState.run(initialState)
// val result: Option[String] = Some(Hello)
// val finalState: FreeExercises.Buffers = Buffers(List(),Vector(Enter text:))
```

### Why Is This Important?

#### ✅ Testability

With such interpreters, you can:

- Verify that the program returns the correct result.
- Ensure that `PrintLine` is called the correct number of times.
- Ensure that `ReadLine` uses the correct data.

#### ✅ Purity and Predictability

Pure interpreters allow:

- Avoiding side effects in tests.
- Simplifying debugging.
- Guaranteeing deterministic behavior.

#### ✅ Extensibility

You can create many interpreters:

- For testing.
- For logging.
- For debugging.
- For state preservation.
- For asynchronous execution.

### Core Idea

`Free[Console, A]` is a **pure program** that **does not depend on how it is executed**.
It merely describes a sequence of actions. How exactly they will be executed is decided by the interpreter.

Thanks to `runFree`, we can easily **translate** our program into various monadic contexts:

- `Function0` → synchronous execution.
- `Par` → asynchronous execution.
- `ConsoleReader` → testing with fixed input.
- `ConsoleState` → I/O simulation.
- `IO` → real execution with side effects.

### Conclusion

Using **pure interpreters** demonstrates the flexibility and power of free monads:

- You can write programs that remain pure but model complex behavior.
- You can easily test such programs without running real side effects.
- You can use the same programs in a wide variety of environments: testing, logging, production, etc.

This makes `Free[F, A]` a universal tool for building pure, testable, and extensible applications.

---

**Links:**

- [Repository with exercises][sample]
- [Functional Programming in Scala, Second Edition, Chapter 13](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
[free]: ../../typeclass/monad/free-monads.md
[monad]: ../../typeclass/monad/monad.md

### Example Program with Console I/O

Using `Free[Console, A]`, we can write programs that interact with the console:

```dotty
val f1: Free[Console, Option[String]] =
  for
    _  <- printLn("I can only interact with the console.")
    ln <- readLn
  yield ln
```

This program does the following:

1. Prints the message `"I can only interact with the console."`.
2. Reads a line from the console.
3. Returns this line as the result.

The program does not contain any other side effects — only those described in `Console`.

> ⚠️ Scala does not prohibit the programmer from using other effects (e.g., file or network operations),
> but if we adhere to discipline and use only `Console`, then the program remains pure and easily testable.

### Advantages of Using `Console` over `Function0`

- **Clarity**: We know exactly what effects can occur in the program.
- **Testability**: We can replace `Console` with a "pure" interpreter that does not perform real I/O, but saves data to a buffer.
- **Reusability**: `Console` can be interpreted differently:
  through `Par`, `Function0`, or even through `Reader`, where input is predefined.
