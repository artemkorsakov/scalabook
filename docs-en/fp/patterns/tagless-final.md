# Tagless Final

## Introduction

The Tagless Final pattern (tagless interpreters or "F with a hole") is an architectural pattern for building programs.
Its main goal is to separate the description of _what_ a program does from the description of _how_ it does it.

- **What the program does (DSL):** You describe business logic in a language you define
  (domain-specific language, DSL), using abstract interfaces (algebras).
  At this stage, your code says: "I need to increment a counter" or "get a list of products",
  but it does not say how exactly this will happen.
- **How the program does it (Interpretation):** Separately, you write implementations (interpreters)
  that explain how these abstract commands translate into concrete actions: "increment the counter in Redis"
  or "get products from PostgreSQL".

**Key benefits:**

- **Solves the [Expression Problem](https://en.wikipedia.org/wiki/Expression_problem):**
  Easy to add new operations (new functions in an interface) and new execution modes (new interpreters).
- **Testability:** For tests, you can replace "production" interpreters (working with a database)
  with "test" ones (working with in-memory data) without changing the business logic itself.
- **Composability:** Small algebras (for a counter, for products) are easily combined into more complex programs.

**Comparison with [Free Monad](../../typeclass/monad/free-monads.md):** Both approaches solve similar problems.
Tagless Final is considered more efficient at runtime (less overhead)
and often easier to understand in Scala due to direct use of language capabilities (traits).

## Algebras

The **algebra** is the heart of Tagless Final. It is, in essence, an **abstract API** for a certain domain.

```dotty
trait Counter[F[_]]:
  def incr: F[Unit] // Command: "Increment the counter"
  def get: F[Int]   // Query: "Give me the current counter value"
```

**Key points**:

- **`F[_]` (Type Constructor):** This is the "hole" for the effect. `F[_]` is not a concrete type (like `Int`),
  but a "shape" of a type that takes one type argument. For example, `IO[Int]`, `Future[Int]`, `Option[Int]` fit the shape `F[_]`.
  This allows the algebra to be independent of a concrete effect library (`cats-effect IO`, `ZIO`, `Monix Task`)
  or context (`Future`, `Id` for simulation).
- **This is not a Type Class:** Although it looks similar outwardly (a trait with a type parameter `F[_]`), it is just an interface.
    - **Type class** (e.g., `trait Monad[F[_]]`) describes the _capabilities_ of type `F` (how to work with it: `flatMap`, `pure`).
      It is usually one per project for a given `F`.
    - **Algebra** describes _business operations_ in the domain.
      For one algebra (`Counter`), there can be many implementations (interpreters).
- **Why without bounds (`F[_]: Monad`)?**
  The algebra should describe only _what_ needs to be done, not _what capabilities_ are needed for it.
  Bounds (such as `Monad`) are requirements on how operations can be combined.
  These requirements relate to the _usage_ stage of the algebra (in programs), not its definition.
  Adding `Monad` to the algebra implicitly says: "All possible implementations of this algebra can only work in the context of `Monad`",
  which unduly restricts its applicability.

**Summary:** The algebra is a maximally abstract, pure contract for your business domain.

## Interpreters

An **interpreter** is the bridge between the abstract world of algebras and the concrete world of implementation.
If the algebra is _what_, then the interpreter is _how_.

### Two Types of Interpreters: Production vs Test

#### Production Interpreter

```dotty
object Counter:
  case class RedisKey(value: String)

  def make[F[_]: Functor](
      key: RedisKey,
      cmd: RedisCommands[F, String, Int]
  ): Counter[F] =
    new Counter[F]:
      def incr: F[Unit] =
        cmd.incr(key.value).void  // Call to real Redis
      def get: F[Int] =
        cmd.get(key.value).map(_.getOrElse(0))  // Read from real Redis
```

**What is happening here**:

- The interpreter takes a dependency `RedisCommands` — a client for working with Redis
- Each method of the algebra (`incr`, `get`) is mapped to a specific Redis operation
- `key.value` is a concrete key in Redis where our counter is stored
- `map(_.getOrElse(0))` converts the `Option[Int]` from Redis into an `Int` with a default value

**Why the `Functor` bound?**
It is needed only for the `map` operation in the `get` method. We use the [minimally required bound](../../typeclass/monad.md).

#### Test Interpreter

```dotty
def testCounter[F[_]](ref: Ref[F, Int]): Counter[F] =
  new Counter[F]:
    def incr: F[Unit] = ref.update(_ + 1)  // Update value in memory
    def get: F[Int]   = ref.get  // Read from memory
```

**Key differences**:

- **No external dependencies** (Redis, database) — only `Ref[F, Int]`
- **Works entirely in memory** — ideal for tests
- **Isolated state** — each test can create its own `Ref`
- **Fast** — no network calls

**What is `Ref[F, Int]`?**
It is a thread-safe mutable memory cell that:

- Stores a value of type `Int`
- Provides atomic operations (`update`, `get`)
- Exists only within the scope of a single program execution

### Polymorphism of Interpreters

**Why are both interpreters polymorphic over `F[_]`?**

```dotty
// Both work with any F[_]
def make[F[_]: Functor](...): Counter[F]
def testCounter[F[_]](...): Counter[F]
```

**Advantages**:

1. **Testing flexibility**:

    ```dotty
    // In tests we can use IO
    Ref[IO].of(0).map: ref =>
      testCounter[IO](ref)
    ```

2. **Runtimes independence**:
    - The same algebra can work with `cats-effect IO`, `ZIO`, `Monix Task`
    - Switching between effect systems does not require changing business logic

### Practical Recommendations

**For production interpreters**:

1. Always encapsulate external resources (databases, HTTP clients)
2. Use `Resource` or `Managed` for managing lifecycles —
   see the ["Resource Safety"](../stream-processing/resource.md) section
3. Log errors, but not inside the interpreter (use a logging algebra)

**For test interpreters**:

1. Store state in memory (`Ref`, `AtomicRef`, collections)
2. Make interpreters as simple as possible
3. Add inspection methods (only in test builds):
    ```dotty
    // In tests only!
    trait TestCounter[F[_]] extends Counter[F]:
        def getInternalState: F[Int] // For checks in tests
    ```

**Summary:** Interpreters are where the abstraction meets reality.
A properly designed interpreter hides all technical details,
providing a clean, testable, and reusable API for your business logic.

## Programs

The **program** layer is where buildings of business logic are constructed from bricks — algebras.
This is where abstractions start to _work together_ to solve real problems.

```dotty
class ItemsCounter[F[_]: Apply](counter: Counter[F], items: Items[F]):
  def addItem(item: Item): F[Unit] =
    // First add the item, then increment the counter
    items.add(item) *> counter.incr 
```

**Characteristics of programs**:

1.  **They are composite:** They use one or more algebras.
2.  **They contain business rules:** The rule "increment the counter when adding an item" lives here.
3.  **They introduce type class bounds:** Since programs _combine_ effects
    (e.g., use `*>` for sequential execution), they need to specify
    what capabilities from `F[_]` they require ( [`Apply`, `Bind`, `Monad`](../../typeclass/monad.md)).
4.  **Principle of least power:** This is the golden rule. Use the weakest bound that allows the work to get done.
    - Just execute two actions one after another? [`Apply`](../../typeclass/monad/apply.md) (the `*>` method) is enough.
    - Need the result of the first action to decide what to do second? You will need [`Bind`](../../typeclass/monad/bind.md) (the `flatMap` method).
    - By using the minimally required bound, you make the program maximally flexible and reusable in different contexts.

**Purity of business logic:** What does "pure" mean?

- There should be no direct calls to databases, the file system, or external APIs. Everything is hidden behind algebras.
- Even logging should go through an algebra (e.g., `Logger[F]`).
  This allows you to plug in a "silent" interpreter in tests that does not log anything.
- The program describes the _sequence of steps_ in terms of the domain, not technical details.

**Ways to write programs**:

1. **Classes**: Good for logic that has its own state (dependencies as algebras) and multiple methods.
2. **Functions**: Ideal for one-off or small scenarios.
3. **Program composition**: Large programs can be assembled from smaller ones, creating a hierarchy.

Tagless Final provides a clear separation of responsibilities:

```dotty
trait Counter[F[_]]:
  def incr: F[Unit]
  def get: F[Int]
  
object Counter:
  def make[F[_]: Functor](key: RedisKey, cmd: RedisCommands[F, String, Int]): Counter[F] = ???
  
class ItemsCounter[F[_]: Apply](counter: Counter[F], items: Items[F]):
  def addItem(item: Item): F[Unit] = ??? 
```

1.  **Algebra (`trait Counter`):** **_defines_** _what_ can be done. (Specification)
2.  **Interpreter (`object Counter.make`):** **_implements_** _how_ to do it. (Executor)
3.  **Program (`class ItemsCounter`):** **_uses_** algebras to describe the _business process_. (Director/Screenwriter)

This separation gives incredible flexibility, testability, and maintainability, especially in large and complex projects.

## Why "Tagless Final"?

> "Constraints liberate, freedom restrains." — [Runar Bjarnason](https://www.youtube.com/watch?v=GqmsQeSzMdw)

**Parametricity** is the concept that shows how **type bounds make code more predictable**.

#### Example 1: Specific Function (Few Bounds)

```dotty
def specific(a: Int): Int
```

How many correct implementations can this function have? Infinitely many! Here are just a few:

```dotty
// Variant 1
def specific(a: Int): Int = 0

// Variant 2
def specific(a: Int): Int = a * 5

...
```

**Problem:** The type `Int → Int` is too broad. The function can do anything with the number, and the compiler won't help us understand what it actually does.

#### Example 2: Parametric Function (Strong Bounds)

```dotty
def parametric[A](a: A): A
```

How many correct implementations can this function have? Only one! (ignoring infinite recursion)

```dotty
def parametric[A](a: A): A = a
```

**Why does this happen?**

- The compiler knows nothing about type `A`
- You cannot create a value of type `A` from nothing (you don't know the constructor)
- You cannot convert `A` to another type (you don't know the operations)
- All you can do is return what you received

### Parametricity in Tagless Final

Now let's see how this works in Tagless Final:

#### Example without Parametricity (Direct `IO`):

```dotty
class UserService:
  def createUser(name: String): IO[User] =
    // Can do anything!
    for 
      _ <- logToFile(s"Creating user: $name") // Side effect #1
      user = User(name)
      _ <- saveToDatabase(user)         // Side effect #2
      _ <- sendWelcomeEmail(user.email) // Side effect #3
      _ <- updateAnalytics()            // Side effect #4 (who added this?!)
    yield user
```

**Problems**:

1. Unclear what the function does
2. Hard to test (need to mock everything)
3. Easy to add hidden side effects

#### Example with Parametricity (Tagless Final):

```dotty
class UserService[F[_]: Monad: Logger](
    dbService: Database,
    emailService: EmailService
):
  def createUser(name: String): F[User] =
    for
      _ <- Logger[F].info(s"Creating user: $name") // Only logging
      user = User(name)
      _ <- dbService.save(user)                 // Only saving to DB
      _ <- emailService.sendWelcome(user.email) // Only sending email
    // Cannot add updateAnalytics() — no service for analytics
    yield user
```

**Advantages**:

1. **Explicit dependencies:** All side effects are visible
2. **Limited power:** Cannot add "hidden" effects
3. **Easy to test:** Can substitute implementations

Parametricity is connected to an important mathematical concept —
[Theorems for Free](https://people.mpi-sws.org/~dreyer/tor/papers/wadler.pdf).
Roughly speaking:

> If a function has a polymorphic type, its behavior is limited by that type.

**Practical example**:

```dotty
// Polymorphic function
def mystery[F[_], A](fa: F[A]): F[A]

// What can we say about mystery, knowing only its type?
// The theorem says: it cannot:
// 1. Look inside F[A] (does not know about A)
// 2. Create a new value of type A (does not know the constructor)
// 3. Change the structure F (does not know about F)
// All it can do is return fa
```

In Tagless Final this means that if we have:

```dotty
def program[F[_]: Monad: Counter]: F[Unit]
```

We know that `program`:

- Can only use operations from `Monad` and `Counter
- Cannot do unexpected things (HTTP requests, file reads, etc.)

### Comparison with Alternatives

| Approach           | Parametricity | Testability           | Safety                          |
|--------------------|---------------|-----------------------|---------------------------------|
| **Direct `IO`**    | No            | Hard (many mocks)     | Low (can do anything)           |
| **Free Monad**     | Yes           | Good                  | High (but complex syntax)       |
| **Tagless Final**  | Yes           | Excellent             | High (simple syntax)            |

### Real Example: Code Evolution

**Version 1 (naive)**:

```dotty
def checkout(userId: UserId): IO[Order] =
  // 100 lines of code with mixed effects
  // Hard to test, easy to break
```

**Version 2 (with dependencies)**:

```dotty
class CheckoutService(
  db: Database,
  payment: PaymentGateway,
  email: EmailService,
  analytics: Analytics
):
  def checkout(userId: UserId): IO[Order] =
    // Better, but still direct IO
```

**Version 3 (Tagless Final)**:

```dotty
class CheckoutService[F[_]: Monad](
    dbService: Database,
    paymentService: PaymentService,
    emailService: EmailService
):
  def checkout(userId: UserId): F[Order] =
    // Pure business logic
    // All effects explicitly declared
    // Easy to test
    for
      user  <- dbService.getUser(userId)
      order <- createOrder(user)
      _     <- paymentService.process(order)
      _     <- emailService.sendReceipt(user.email, order)
    yield order
```

### Why All This?

1. **Safety through bounds:** The compiler won't let you do anything implicit
2. **Testability:** Easy to substitute implementations
3. **Modularity:** Components are loosely coupled
4. **Composability:** Small parts assemble into large systems
5. **Maintainability:** A function's capabilities are visible from its type

**Philosophical conclusion:** Tagless Final is not just a programming technique.
It is a **thinking discipline** that teaches us:

- To decompose problems into small parts
- To declare intentions explicitly
- To trust composition over inheritance
- To value the bounds that protect us from errors

## Summary

**Tagless Final** is an architectural pattern that separates _what the program does_ (business logic) from _how it does it_ (implementation).
It is based on three components: **algebras** (abstract domain interfaces, polymorphic over the effect type `F[_]`),
**programs** (pure business logic using algebras),
and **interpreters** (concrete implementations of algebras for production and tests).

Key advantages — **testability** (logic remains unchanged, only interpreters are swapped),
**parametricity** (type bounds prevent hidden effects) and **modularity** (clear boundaries between components).
The pattern is especially effective for complex applications with high maintainability requirements,
providing predictability and safety through type bounds.

---

**Links:**

- [Link to repository with exercises][sample]
- [Final tagless encodings have little to do with typeclasses](https://www.foxhound.systems/blog/final-tagless/)
- [Tagless Final for Humans by Noel Welsh | Scalar Conference 2025](https://www.youtube.com/watch?v=nyMwp7--rY4)
- [Tagless Final style][kiselyov]
- [Tagless Final - Abdelrahman Ibrahim](https://medium.com/@aibrahimblog/tagless-final-e2407c20a0f8)
- [Tagless Final - DevInsideYou](https://www.youtube.com/playlist?list=PLJGDHERh23x-3_T3Dua6Fwp4KlG0J25DI)
- [Tagless Final - Rock the JVM](https://www.youtube.com/watch?v=m3Qh-MmWpbM)
- [Volpe Gabriel - Practical FP in Scala](https://leanpub.com/pfp-scala)

[sample]: https://gitverse.ru/scalabook/fp
[kiselyov]: http://okmij.org/ftp/tagless-final/index.html
