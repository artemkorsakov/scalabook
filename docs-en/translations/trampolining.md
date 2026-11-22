# How to avoid stack overflow in Scala using free monads

Translation of the article by Runar Oli Bjarnason ["Stackless Scala With Free Monads"][Trampolines] with minor digressions.


## Prerequisites

Tail call elimination (TCE) in the Scala compiler
is limited to self-recursive methods, but other tail calls are not eliminated.
This makes functions composed of many smaller functions prone to stack overflow.
Having a general TCE mechanism would be a major asset in Scala,
especially for functional programming.
Trampolining is a popular technique that can be used for TCE in languages
that do not support it natively.
This article gives an introduction to "trampolines" in Scala and extends this solution
to eliminate any method call, even a call that is not in tail position at all.
This completely eliminates the use of the call stack in Scala programs.


## 1. Introduction

Since the call stack is a limited resource of the virtual machine,
most programmers with some Scala experience have encountered the problem
when a seemingly reasonable function ran off the end of the stack
and crashed the program with a `StackOverflowError`.
As a practical example, consider traversing a list while retaining some state.
We will use the `State` data type, which represents a transition function in a simple state machine.

```dotty
case class State[S, +A](runS: S => (A, S)):
  def map[B](f: A => B): State[S, B] =
    State[S, B] { s =>
      val (a, s1) = runS(s)
      (f(a), s1)
    }

  def flatMap[B](f: A => State[S, B]): State[S, B] =
    State[S, B] { s =>
      val (a, s1) = runS(s)
      f(a).runS(s1)
    }
```

The `runS` function takes some incoming state of type `S` and produces a value of type `A` together with a new state.
The `map` and `flatMap` methods allow us to thread the state through a for-comprehension,
to write seemingly imperative programs using `State` combinators, such as:

```dotty
def getState[S]: State[S, S] =
  State(s => (s, s))

def setState[S](s: S): State[S, Unit] =
  State(_ => ((), s))

def pureState[S, A](a: A): State[S, A] =
  State(s => (a, s))
```

Note that `pureState` and `flatMap` together [form the `State` monad][Category Theory].

As a simple demonstration, let us write a function that uses `State` to number all elements in a list.
Not because this is a compelling use case for `State`, but because it is simple and demonstrates stack overflow.

```dotty
def zipIndex[A](as: List[A]): List[(Int, A)] =
  as.foldLeft(pureState[Int, List[(Int, A)]](List())) { (acc, a) =>
    for {
      xs <- acc
      n  <- getState
      _  <- setState(n + 1)
    } yield (n, a) :: xs
  }.runS(0)
    ._1
    .reverse
```

```dotty
@main def runStateZipIndex(): Unit =
  import State.*
  val list = zipIndex[String](List.fill(10000)("el"))
  println("zipped list = " + list) // java.lang.StackOverflowError
```

We use fold left to emphasize that traversing the list is tail recursion.
Fold builds up a state action that starts with an empty list
and prepends successive elements, reversing the original list.
The state is an integer that is incremented at each step,
and the whole compound state action is run starting from zero before returning the reversed result.

But when `zipIndex` is called in `State.flatMap`, it fails with a `StackOverflowError`
if the number of elements in the list exceeds the size of the virtual machine's call stack.
The reason is that the state action itself is a function
composed of several smaller functions proportional to the length of the list.
Even though we represent it as a sequence of discrete steps,
each step calls the next step in a way that the compiler cannot optimize.

This would seem to severely limit the usefulness of functional programming in Scala.
This article explores the solution space for this problem:

- In section 3 we will discuss the well-known "trampolining" technique.
  In trampolining, instead of each step calling the next,
  functions pass the next step to a single control loop, known as a "trampoline".
  This allows us to programmatically trade the stack for the heap.
- We will then extend this technique by adding an operational monad (section 4),
  allowing any call to be turned into a tail call that can subsequently be eliminated.
  This will be the main contribution of this article.
- There is one subtle detail in the implementation of this monad: if it is implemented incorrectly,
  in some cases it will continue to overflow the stack.
  In section 4.3 we will look at what those cases are and how to defuse them.
- Trampoline programs can be interleaved, providing a model of cooperative coroutines. This will be covered in section 5.
- In section 6 we generalize trampolines to the free monad - an extremely versatile recursive data structure.
  We will look at some functions that work with all such structures (6.1),
  and discover that the work done for trampolines gives the same benefits for all free monads.


## 2. Background: tail call elimination in Scala

The Scala compiler can optimize a specific kind of tail call, known as a self-recursive call.
For example, the following definition of folding a list to the left is optimized by the compiler
to use a constant amount of stack space:

```dotty
def foldl[A, B](as: List[A], b: B, f: (B, A) => B): B =
  as match
    case Nil     => b
    case x :: xs => foldl(xs, f(b, x), f)
```

When the compiler detects that a method calls itself in tail position,
and if the method cannot be overridden (for example because it is declared private or final),
the recursive call is replaced by a simple jump in the compiled code.
This is equivalent to rewriting the tail recursion as a loop:

```dotty
def foldl[A, B](as: List[A], b: B, f: (B, A) => B): B =
  var z  = b
  var az = as
  while (true)
    az match
      case Nil => return z
      case x :: xs =>
        z = f(z, x)
        az = xs
  z
```

This kind of optimization has two benefits: the jump is much faster than a method call, and it requires no stack space.

But while optimizing self-recursive calls is easy,
replacing tail calls with jumps in general is a more difficult problem.
At present (the article was written in 2012) the Java Virtual Machine (JVM) only allows local jumps,
so there is no way to directly implement a tail call to another method as a jump.
For example, this mutual recursion cannot be optimized by the compiler,
even though the calls are in tail position:

```dotty
def even[A](ns: List[A]): Boolean =
  ns match
    case Nil     => true
    case _ :: xs => odd(xs)

def odd[A](ns: List[A]): Boolean =
  ns match
    case Nil     => false
    case _ :: xs => even(xs)
```

These functions will overflow the call stack if the size of the `List` is larger than the size of the stack.

Although a future JVM may implement explicit support for tail calls in bytecode,
this is not without obstacles and may not be as useful as it seems.
For example, the JVM execution model requires that the state of each thread of execution is kept in the thread stack.
In addition, exception handling is implemented by passing the exception up the call stack,
and the JVM provides the stack to the programmer for inspection.
In fact, its security model is implemented by looking at the permissions
granted to each stack frame individually.
This, combined with subclassing, dynamic dispatch, and just-in-time compilation,
makes optimizing tail calls within the Scala compiler itself difficult to implement.

Fortunately, we can sidestep all of these problems.
There is a way to mechanically replace the stack with the heap using a simple data structure.


## 3. Trampolines: trading the stack for the heap

We start with a very simple `Trampoline` data type.
It is identical in spirit, but differs in implementation, from [scala.util.control.TailCalls.TailRec][API].

```dotty
sealed trait Trampoline[+A]: 
  self =>
  
  @annotation.tailrec
  final def runT: A =
    self match 
      case More(k) => k().runT
      case Done(v) => v

case class More[+A](k: () => Trampoline[A]) extends Trampoline[A]
case class Done[+A](result: A)              extends Trampoline[A]
```

`Trampoline` represents a stepwise computation, and each step can take only one of two forms.
A `Done(v)` step returns the value `v`, in which case there are no more steps.
A `More(k)` step has more work: `k` is a closure that does some work and returns the next step.
The `runT` method is a simple tail-recursive method that executes all the steps.
It is declared `final` so that Scala can eliminate the tail call.

This solves the mutual recursion problem shown earlier.
All that needs to be done is to mechanically replace any return type `T` with `Trampoline[T]`.
Here are `odd` and `even`, modified in this way:

```dotty
def even[A](ns: List[A]): Trampoline[Boolean] =
  ns match
    case Nil     => Done(true)
    case _ :: xs => More(() => odd(xs))

def odd[A](ns: List[A]): Trampoline[Boolean] =
  ns match
    case Nil     => Done(false)
    case _ :: xs => More(() => even(xs))
```

Instead of direct recursion, the functions now return the next step as a `Trampoline`,
which can be executed recursively by calling the `runT` method.
The stack no longer overflows, regardless of how large the argument lists are.

```dotty
val list = List.fill(100000)(0)
even(list).runT
// true
```

## 4. Turning every call into a tail call

Let us see if we can apply the `Trampoline` solution to the list traversal problem with `State`.
We need to change the `State` runner
to return a `Trampoline` which we can run with tail recursion:

```dotty
case class State[S, +A](runS: S => Trampoline[(A, S)])
```

How do we now implement the `flatMap` method to compose `State` actions?
We could try this:

```dotty
case class State[S, +A](runS: S => Trampoline[(A, S)]):
  def flatMap[B](f: A => State[S, B]): State[S, B] =
    State[S, B] { s =>
      More { () =>
        val (a, s1) = runS(s).runT
        More(() => f(a) runS s1)
      }
    }
```

But this turns out to be insufficient.
The `zipIndex` example from section 1 will still overflow the stack for large lists, this time for even smaller lists.
The problem is that the call to `runT` is not in tail position,
so it cannot be optimized or wrapped in a `Trampoline`.

### 4.1 A Trampoline monad?

Let us try to solve this problem by making `Trampoline` monadic.
It already has a monadic `unit` which is the `Done` constructor
(`unit` for a monad `M` is a function of type `A => M[A]` for all `A`.
This is `unit` in the sense that passing it to `flatMap` is the identity: `fa.flatMap(unit) == fa`).
All it needs is a monadic bind, that is, a `flatMap`.
Let us add the `flatMap` method directly to `Trampoline`,
so that we can do the following in `State.flatMap`:

```dotty
def flatMap[B](f: A => State[S, B]): State[S, B] =
  State[S, B] { s =>
    More { () =>
      runS(s).flatMap { (a, s1) => More(() => f(a).runS(s1)) }
    }
  }
```

This is a definite improvement.
The problem moves to the `flatMap` method for `Trampoline`,
which one might be tempted to implement as follows:

```dotty
def flatMap[B](f: A => Trampoline[B]): More[B] =
  More[B](() => f(runT))
```

But that is not what we want. The call to `runT` here is also not in the tail.
It seems that no matter what we try, it is simply impossible to implement the `flatMap` method for `Trampoline`
without requiring any additional stack.

### 4.2 Building the monad correctly

The way to work around this limitation is to add a constructor to the `Trampoline` data type,
changing `flatMap` from a method call to a constructor call:

```dotty
case class FlatMap[A, +B](sub: Trampoline[A], k: A => Trampoline[B])
    extends Trampoline[B]
```

A trampoline of this form can be viewed as a subroutine call,
the result of which is returned to `k`.

The `runT` method of the `Trampoline` trait must now account for this new constructor.
For simplicity, let us separate the task of advancing to the next step from the task of running all steps:

```dotty
@annotation.tailrec
final def resume: Either[() => Trampoline[A], A] =
  self match
    case Done(v) => Right(v)
    case More(k) => Left(k)
    case FlatMap(a, f) =>
      a match
        case Done(v) => f(v).resume
        case More(k) => Left(() => FlatMap(k(), f))
        case FlatMap(b, g) =>
          FlatMap(b, x => FlatMap(g(x), f)).resume

@annotation.tailrec
final def runT: A =
  resume match
    case Right(a) => a
    case Left(k)  => k().runT
```

The `resume` method works by pattern matching on the `Trampoline`,
returning either a result (`Right`) or the next step as a `Function0` (`Left`).
How we handle the `FlatMap(a,f)` case here is very important.
We match on the subroutine call `a`, and if it is `Done`, we just run the continuation.
If it is wrapped in the `More` constructor, we advance one step and wrap a `FlatMap` around it.
If the subroutine call itself contains a subroutine call,
we have a left-associative nesting of `FlatMap`s in an expression like: `FlatMap(FlatMap(b, g), f)`

It is crucial to resolve this case in a way that remains productive without introducing new stack frames.
The trick is to rebind the expression to the right: `FlatMap(b, x => FlatMap(g(x), f))`.
When we do this, the next iteration will match the pattern `b`,
and thus we can make an efficient tail call to invoke `resume` again.
Also note that when we peek inside nested `FlatMap` constructors,
we find that some type information has been lost.
In a pattern like `FlatMap(FlatMap(b, g), f)`, the type of `b` cannot be known,
so we must accept `Any` when building the right-associated nesting.
This is perfectly safe, since we can assume
that the left-associated nesting was well-typed when it was constructed.
This reassociation uses the monad laws.
`Trampoline` is a monad, and monads are associative by definition.
Therefore [right-associated continuations are always exactly equivalent to left-associated ones](../typeclass/monad/monad.md).

### 4.3 An easy way to get it wrong

There is one more edge case to consider here.
Now `resume` can overflow the stack if the left "matryoshka" of `FlatMap`s is higher than the call stack.
The call `f(v)` will make a call to `g(x)`, which will make yet another internal call and so on.
Let us avoid this by preventing the construction of deeply nested left-associated binds in the first place.
We make the `FlatMap` constructor private, exposing instead the `flatMap` method on `Trampoline`,
which we rewrite so that it always creates right-associated binds:

```dotty
def flatMap[B](f: A => Trampoline[B]): Trampoline[B] =
  self match
    case FlatMap(a, g) => FlatMap(a, x => g(x).flatMap(f))
    case x             => FlatMap(x, f)
```

To fill the gap, we must also prevent the `resume` method from building such a "matryoshka",
by replacing `FlatMap` constructor calls with `flatMap` method calls:

```dotty
case FlatMap(a, f) =>
  a match
    case Done(v) => f(v).resume
    case More(k) => Left(() => k().flatMap(f))
    case FlatMap(b, g) =>
      b.flatMap(x => g(x).flatMap(f)).resume
```

Finally, in order to use our `Trampoline` monad with Scala's for-comprehension,
we also need to implement the `map` method, which is, of course, simply defined in terms of `flatMap`:

```dotty
def map[B](f: A => B): Trampoline[B] =
  flatMap(a => Done(f(a)))
```

### 4.4 Stackless Scala

The previous `zipIndex` method can now be run without a `StackOverflowError`
for any input list size using the `State` monad.

The `Trampoline` presented here is a general solution for eliminating stack frames in Scala.
Now we can rewrite any program so that it does not use stack space at all.
Consider a program of this form:

```dotty
val x = f()
val y = g(x)
h(y)
```

It can be easily rewritten in the following form:

```dotty
for 
  x <- f()
  y <- g(x)
  z <- h(y)
yield z
```

if we take into account the following implicit conversion:

```dotty
given conv[A]: Conversion[A, Trampoline[A]] with
  def apply(a: A): Trampoline[A] = More(() => Done(a))
```

The only type of call for which the stepwise conversion is not suitable (and not by accident) is the self-recursive call.
They are easy to detect, and in those cases we could call the `More` constructor explicitly,
as in this recursive function for finding the n-th Fibonacci number:

```dotty
def fib(n: Int): Trampoline[Int] =
  if n <= 1 then Done(n)
  else
    for
      x <- More(() => fib(n - 1))
      y <- More(() => fib(n - 2))
    yield x + y
```

Since this conversion is completely mechanical, we can imagine
that one could write a compiler plugin
or otherwise extend the Scala compiler to transform all programs in this way.


## 5. Cooperative multitasking

We have seen how to compose `Trampoline` computations sequentially with `flatMap`.
But it is also possible to compose them in parallel by interleaving the computations:

```dotty
infix def zip[B](b: Trampoline[B]): Trampoline[(A, B)] =
  (self.resume, b.resume) match
    case (Right(a), Right(b)) =>
      Done((a, b))
    case (Left(a), Left(b)) =>
      More(() => a() zip b())
    case (Left(a), Right(b)) =>
      More(() => a() zip Done(b))
    case (Right(a), Left(b)) =>
      More(() => Done(a) zip b())
```

To see all this in action, one can introduce side effects for printing to the console:

```dotty
val hello: Trampoline[Unit] =
  for
    _ <- print(" Hello , ")
    _ <- println(" World !")
  yield ()
```

One can see what happens if the `hello` computation is interleaved with itself:

```dotty
(hello zip hello).runT
// Hello ,  Hello ,  World !
// World !
```

Although this is single-threaded concurrency, it is easy to imagine distributing the work across many threads.
For a set of running trampolines, any trampoline that is not in the `Done` state
can be resumed by any available thread.

It turns out that this method can be generalized to a model of full symmetric coroutines.


## 6. Free monads: generalizing the trampoline

We can think of `Trampoline` as a coroutine that can be suspended in `Function0` and then resumed.
But it is not the only type constructor we could use for suspension.
If we abstract over the type constructor, we get the following data type:

```dotty
sealed trait Free[S[+_], A]

object Free:
  private infix case class FlatMap[S[+_], A, B](
      a: Free[S, A],
      f: A => Free[S, B]
  ) extends Free[S, B]

case class Done[S[+_], A](a: A)             extends Free[S, A]
case class More[S[+_], A](k: S[Free[S, A]]) extends Free[S, A]
```

Now `Trampoline` can be defined as:

```dotty
type Trampoline[A] = Free[Function0, A]
```

As can be seen from the `Done` and `FlatMap` data constructors above,
`Free[S,A]` is a monad for any covariant functor `S`.
In category theory terms this is exactly the [free monad generated by the functor `S`](../typeclass/monad/free-monads.md).

When we say that `S` must be a functor, we mean that a `Functor[S]` instance must exist:

```dotty
trait Functor[F[_]]:
  def map[A, B](fa: F[A])(f: A => B): F[B]
```

For `Function0`, the functor is defined simply:

```dotty
given Functor[Function0] with
  def map[A, B](a: () => A)(f: A => B): () => B = () => f(a())
```

### 6.1 Functions defined for all free monads

To make concrete the claim that all free monads can benefit from the work already done above,
we can generalize the methods defined earlier for `Trampoline`.
For example, here is the general form of `resume`:

```dotty
final def resume(using S: Functor[S]): Either[S[Free[S, A]], A] =
  this match
    case Done(a) => Right(a)
    case More(k) => Left(k)
    case a FlatMap f =>
      a match
        case Done(a)     => f(a).resume
        case More(k)     => Left(S.map(k)(_ flatMap f))
        case b FlatMap g => b.flatMap(x => g(x).flatMap(f)).resume
```

Note that this definition is essentially the same as for `Trampoline`.
The only differences are the type signature, the additional implicit `Functor` argument,
and the fact that we replaced the explicit `Function0` construction with calls to `map` for our functor.
This is also true for the generalized implementations of `zip`, `map`, and `flatMap`.
Here is `zip`:

```dotty
infix def zip[B](b: Free[S, B])(using S: Functor[S]): Free[S, (A, B)] =
  (resume, b.resume) match
    case (Left(a), Left(b)) =>
      More(S.map(a)(x => More(S.map(b)(y => x zip y))))
    case (Left(a), Right(b)) =>
      More(S.map(a)(x => x zip Done(b)))
    case (Right(a), Left(b)) =>
      More(S.map(b)(y => Done(a) zip y))
    case (Right(a), Right(b)) =>
      Done((a, b))
```

### 6.2 Generic data types as free monads

Informally, we can think of `Free[S, A]` as the type of any computation
that can branch along some functor `S` and end with some data of type `A` at the leaves.
To understand this, consider an ordinary binary decision tree.
It is a free monad whose functor "splits" the computation into two parts at each branch:

```dotty
type Pair[+A]    = (A, A)
type BinTree[+A] = Free[Pair, A]
```

The `Done` case for `BinTree[A]` is a leaf containing a value of type `A`,
and the `More` case is a branch containing two values of type `BinTree[A]`.
Our free monad (in the `FlatMap` case) allows us to take each leaf of the tree,
apply a tree-building function to it, and graft the resulting tree in place of that leaf.
And since this is a `Free` instance, the work we already did on `Trampoline`
allows us to do this in constant time and constant stack space.

To obtain a tree with any number of possible branches at each node instead of two,
we must branch with the `List` functor:

```dotty
type Tree[+A] = Free[List, A]
```

Indeed, `List` itself can be expressed as an application of `Free`:

```dotty
type List[A] = Free[[X] =>> (A, X), Unit]
```

In this representation, `List[A]` is a coroutine
that produces a value of type `A` on each resumption, or `Unit` if it is an empty list.
The free monad action here is not the action of the "list monad" per se
(whose monadic bind replaces each list element with a new list),
but a monad that allows us to append one list to another.
Again, since this is an application of `Free`,
we can perform this operation in constant time and constant memory.

### 6.3 A free monad for State

Although the free monad examples presented here are very simple,
the suspension functor for a free monad can be arbitrarily complex.
It can produce output and await input in any conceivable combination.
We could, for example, model `State` as a small language where we can get and set state:

```dotty
sealed trait StateF[S, +A]
case class Get[S, A](f: S => A)  extends StateF[S, A]
case class Put[S, A](s: S, a: A) extends StateF[S, A]
```

In the `Get` constructor, `f` is a function that expects the current state value as input.
In the `Put` constructor, `s` is the new state,
and `a` is the rest of the computation (which presumably may do something with that state).
We will need proofs that our data type is in fact a functor:

```dotty
given statefFun[S]: Functor[[X] =>> StateF[S, X]] = new:
  def map[A, B](m: StateF[S, A])(f: A => B): StateF[S, B] =
    m match
      case Get(g)    => Get(s => f(g(s)))
      case Put(s, a) => Put(s, f(a))
```

Then we can encode a `State`-like monad
directly as the free monad generated by our `StateF` functor:

```dotty
type FreeState[S, A] = Free[[X] =>> StateF[S, X], A]
```

The `pureState` combinator from section 1 comes "for free" with the `Done` constructor of our free monad.

```dotty
def pureState[S, A](a: A): FreeState[S, A] = Done(a)
```

And the other two, for getting and setting state, are easy to define:

```dotty
def getState[S]: FreeState[S, S] = More(Get(s => Done(s)))

def setState[S](s: S): FreeState[S, Unit] = More(Put(s, Done(())))
```

A simple loop is used to run the action with a given initial state:

```dotty
def evalS[S, A](s: S, t: FreeState[S, A]): A =
  t.resume match
    case Left(Get(f))    => evalS(s, f(s))
    case Left(Put(n, a)) => evalS(n, a)
    case Right(a)        => a
```

Now we can write pure functions that keep some state in this monad.
For example, here is `zipIndex` from section 1, this time using our `FreeState` monad:

```dotty
def zipIndex[A](as: List[A]): List[(Int, A)] =
  evalS(
    0,
    as.foldLeft(pureState[Int, List[(Int, A)]](List())) { (acc, a) =>
      for
        xs <- acc
        n  <- getState
        _  <- setState(n + 1)
      yield (n, a) :: xs
    }
  ).reverse
```

The implementation is almost identical, and it runs in constant stack without needing to go through `Trampoline`.
The conclusion is that it is not always necessary or desirable to modify an existing data structure.
We can invent new free monads of our own design a la carte (see 7.4) and reap the same benefits.


## 7. Existing work

Nothing presented here is particularly original, although parts are taken from various sources
and, to my knowledge, have not previously been assembled in this way, especially in Scala (2012 - at the time of writing).

### 7.1 Trampolines

Using "trampoline" functions to implement tail calls in stack-oriented languages is
a [well-known technique][Trampolined Style], implemented in many languages as libraries or compilers.

The Scala standard library (version `3.2.2`) includes the [TailCalls][API] object,
providing the `TailRec` data structure.

### 7.2 Operational monads

The implementation of free monads in this article has a monadic bind
that is reified on the heap as a data constructor, rather than as a method call on the stack.
This allowed binds to be manipulated as data and reassociated.
One could call this technique the use of an operational monad, based on the work of [Heinrich Apfelmus][Apfelmus].
He discusses monads representing programs with an explicitly reified stack,
but not the free monads or trampolines presented here.

Our `FreeState` monad is also an operational monad, explicitly reifying the two operations `Get` and `Put`.

### 7.3 Free monads and coroutines

The idea of generalizing trampolines came from Blazewicz's _Coroutine Pipelines_ in the October issue of ["The Monad Reader" for 2011][The monad reader].
Although he does not make explicit references to free structures, it is clear
that his `Coroutine` type is a transformation of a Free monad.
In the current implementation in Scala, one must give up parametrization by the additional monad
in order to preserve tail call elimination.
Instead of being written as a monad transformer,
`Free` can be transformed by a monad transformer for the same effect.
For example, `Coroutine[A, [X] =>> State[S, X], B]` becomes `StateT[S, [X] =>> Free[A, X], B]`,
given: `case class StateT[S, M[_], A](run: S => M[(A, S)])`

Blazewicz's discussion describes in much more detail
the "kinds of interacting coroutines that can be expressed as free monads
with producers, consumers, and transducers between them".

### 7.4 The expression problem

The idea of the `FreeState` monad modeling state transitions with the coproduct of `Get` and `Put`
comes from Wouter Swierstra's 2008 article ["Data types a la carte"][Swierstra].
Swierstra uses the free monad to build custom recursive data types
from the coproduct of arbitrary functors and Haskell type classes
to handle dispatch in the various cases of the coproduct.

### 7.5 Codensity

The act of associating all monadic binds to the right can be understood
as an [application of codensity][Category Theory].
This key insight came from a discussion with Ed Kmett about [his work][Free Monads for Less]
with an I/O data structure expressed as a free monad.

Janis Voigtlander discusses the codensity of free monads in his article
["Asymptotic Improvement of Computations over Free Monads"][Asymptotic Improvement of Computations over Free Monads],
although he does not name the notion "codensity" explicitly.
The goal of his article is not the conservation of stack space per se, but performance improvement.

Voigtlander explains how descending into left-associated binds in a free monad
has quadratic overhead,
and he proceeds to eliminate that overhead using the codensity monad.

### 7.6 Iteratees

Safe and modular incremental I/O has long been unattainable in purely functional languages such as Haskell.
[Oleg Kiselyov][Kiselyov, Incremental] and [John Lato][Lato, Iteratee] discuss the idea of the iteratee,
a pure automaton that consumes input,
produces effects, and can maintain some internal state.

`Iteratees` are not exactly free monads,
but they represent a composition of a free monad with another functor,
so they can be expressed as an application of `Free`.
The `IterV` type in Lato's article can be expressed very roughly as follows:

```dotty
type IterV[I, O] = Free[[X] =>> I => X, (I, O)]
```

Since `IterV` tracks the remaining input (much like a parser),
it is not technically free.
But the article also discusses an "inner iterator" which they call an enumeratee,
transforming one stream of input into a stream of output,
and this type is easier to express as a free monad:

```dotty
type Enumeratee[I, O, A] = Free[[X] =>> Either[I => X, (O, X)], A]
```

This coroutine type is a stream transducer that can either request input of type `I`
or emit output of type `O` each time it is resumed,
and terminate with a value of type `A`.


## 8. Conclusions and future work

We have seen how to use trampolines to perform recursive calls on the heap instead of the stack in Scala.
The technique is simple, but there are some details that we must implement in a certain way,
given the capabilities and limitations of the Scala language and the JVM, otherwise the solution will not work.

We have seen how trampolines relate to the general idea of free monads over a functor
and how all our concerns about trampolines, as well as all their benefits, carry over easily to any such monad.

Further research is needed to implement these ideas in the Scala compiler -
perhaps as a plugin -
to enable stackless programming without any additional effort on the part of the programmer.

Free monads have many more applications than we envisioned here.
With growing industrial pressure toward pure functional programming,
there is a need for a programming model that allows composing modular computations,
efficient in time and memory.
Promising work is being done on iteratees and monadic streams,
but Scala does not yet have a very clean API for them.
Perhaps a library based on free monads could change everything.


---

**References:**

- [Effects, trampolines and why we need them - Alexey Shuksto, Scala Meetup 20.04.23](https://www.youtube.com/watch?v=4jSi6RRIaCk)
- [Asymptotic Improvement of Computations over Free Monads - Janis Voigtlander][Asymptotic Improvement of Computations over Free Monads]
- [Category Theory - S. Awodey][Category Theory]
- [Data types a la carte - Wouter Swierstra][Swierstra]
- [Free Monads for Less - E. A. Kmett][Free Monads for Less]
- [Functional Programming in Scala, Second Edition, Chapter 13](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Incremental multi-level input processing and collection enumeration - O. Kiselyov][Kiselyov, Incremental]
- [Iteratee: Teaching an Old Fold New Tricks - J. W. Lato][Lato, Iteratee]
- [Scala API][API]
- [Stackless Scala With Free Monads - Runar Oli Bjarnason][Trampolines]
- [Stackless Scala with Free Monads - hearding cats](http://eed3si9n.com/herding-cats/stackless-scala-with-free-monads.html)
- [Stackless Scala with Free Monads - learning ScalaZ](http://eed3si9n.com/learning-scalaz/Stackless+Scala+with+Free+Monads.html)
- [The Monad.Reader Issue 19][The monad reader]
- [The Operational Monad Tutorial - Heinrich Apfelmus][Apfelmus]
- [Trampolined Style - Steven E. Ganz and Daniel P. Friedman and Mitchell Wand][Trampolined Style]

[API]: https://www.scala-lang.org/api/3.2.2/scala/util/control/TailCalls$.html
[Apfelmus]: https://apfelmus.nfshost.com/articles/operational-monad.html
[Asymptotic Improvement of Computations over Free Monads]: https://janis-voigtlaender.eu/papers/AsymptoticImprovementOfComputationsOverFreeMonads.pdf
[Category Theory]: https://books.google.ru/books/about/Category_Theory.html?hl=ru&amp;id=AIhUtQAACAAJ&amp;redir_esc=y
[Kiselyov, Incremental]: https://okmij.org/ftp/Streams.html
[Lato, Iteratee]: https://themonadreader.files.wordpress.com/2010/05/issue16.pdf
[Free Monads for Less]: http://comonad.com/reader/2011/free-monads-for-less/
[Swierstra]: https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.101.4131&amp;rep=rep1&amp;type=pdf
[The monad reader]: https://themonadreader.files.wordpress.com/2011/10/issue19.pdf
[Trampolined Style]: https://dl.acm.org/doi/abs/10.1145/317636.317779
[Trampolines]: https://blog.higher-order.com/assets/trampolines.pdf