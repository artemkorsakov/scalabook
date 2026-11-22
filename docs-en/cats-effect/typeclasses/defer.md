# Defer — Lazy Effect Creation

## Introduction

In Scala (as in most JVM languages) function arguments are **evaluated strictly** (call-by-value) — i.e., before the function is called.
This creates problems for recursive definitions and lazy effects.

```dotty
// Without Defer - evaluates immediately
def withoutDefer: IO[Boolean] =
  val marker =
    println("⚠ Will execute at definition time!")
    true
  IO.pure(marker)

val bad = withoutDefer
// ⚠ Will execute at definition time!
// val bad: IO[Boolean] = IO(true)

// With Defer - executes at run time
def withDefer(using D: Defer[IO]): IO[Boolean] =
  D.defer:
    val marker =
      println("✅ Will execute only at .unsafeRunSync()")
      true
    IO.pure(marker)

val good = withDefer
// val good: IO[Boolean] = IO(...)

good.unsafeRunSync()
// ✅ Will execute only at .unsafeRunSync()
// val res0: Boolean = true
```

## Definition

`Defer[F[_]]` provides the ability to **postpone effect creation** until it is actually used:

```dotty
trait Defer[F[_]]:
  def defer[A](fa: => F[A]): F[A]
```

**What `defer` does:**

- Accepts a **by-name parameter** (`=> F[A]`) — the expression is not evaluated at call time
- Returns an **effect** that, when executed, will evaluate the passed expression
- Allows "deferring" the creation of an inner effect

## Advanced Capabilities of Defer

### `fix` — Creating Recursive Effects

The `fix` method allows defining a **recursive value** in terms of itself:

```dotty
def fix[A](fn: F[A] => F[A]): F[A]
```

**How it works:**

```dotty
// fix(fn) = fn(fix(fn))  — recursive equation
```

**Example: Web Server with Recursive Routes**

```dotty
sealed trait Route[F[_]]
case class Leaf[F[_]](handler: F[Unit])         extends Route[F]
case class Branch[F[_]](routes: List[Route[F]]) extends Route[F]

def buildRouter[F[_]: {Defer, Monad}](routes: List[Route[F]]): F[Unit] =
  Defer[F].fix[Unit]: self =>
    routes match
      case Nil                   => Monad[F].unit
      case Leaf(handler) :: rest =>
        handler >> Defer[F].defer(buildRouter(rest))
      case Branch(branches) :: rest =>
        buildRouter(branches) >> Defer[F].defer(buildRouter(rest))
```

### `recursiveFn` — Recursive Functions That Return Effects

Creates a recursive function where each step is lazily wrapped in `defer`:

```dotty
def recursiveFn[A, B](fn: (A => F[B]) => (A => F[B])): A => F[B]
```

## Summary

### ✅ **Use `Defer`:**

- Building a **DSL with deferred interpretation**
- Want to **prevent immediate execution** of an effect when passing to a function
- Implementing **lazy resource initialization**
- Writing **parsers with recursive grammars**

### ❌ **Don't use `Defer`:**

- Need **result caching** (use `lazy val` or `memoize`)
- Simply wrapping a side effect (use `Sync[F].delay`)
- Working with an already-lazy effect (e.g., `IO` is already lazy)
- Need **pure laziness without effects** (use `cats.Eval`)

### 💡 **Golden Rule:**

```dotty
// Without Defer — strict evaluation
def strict[F[_]: Monad](fa: F[A]): F[A] = fa

// With Defer — lazy evaluation
def lazy_[F[_]: Defer: Monad](fa: => F[A]): F[A] = Defer[F].defer(fa)
```

`Defer` is a bridge between the strict world of Scala and the lazy world of effects.
It allows building recursive data structures and functions
that would otherwise lead to stack overflow or premature execution of side effects.

---

**Links:**

- [Cats effect documentation][doc]
- [Baeldung: Difference Between delay, defer, and deferred in Cats Effect](https://www.baeldung.com/scala/cats-effect-delay-defer-vs-deferred)

[doc]: https://typelevel.org/cats-effect/docs/concepts
