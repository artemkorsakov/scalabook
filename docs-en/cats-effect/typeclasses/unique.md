# Unique — Source of Unique Tokens

## Introduction

In functional programming with effects, there is often a need
to **distinguish different instances of the same value**
or to **generate unique identifiers** for tracking effect execution.
The `Unique[F[_]]` typeclass provides a minimalistic,
but powerful way to create unique tokens in the effect context `F[_]`.

## The Problem `Unique` Solves

Imagine you are writing a logging system where you need to track call chains:

```dotty
def processRequest[F[_]: Monad](request: Request): F[Response] =
  for
    _      <- log("Processing started")
    result <- handleRequest(request)
    _      <- log("Processing finished")
  yield result
```

How do you distinguish different requests in logs? You can use:

- **Random numbers** — but no guarantee of uniqueness
- **UUID** — overkill for local tracking (128 bits, global uniqueness)
- **Atomics** — requires `Sync`, but doesn't express in types

`Unique` provides a simple solution: **a guaranteed unique local identifier** with minimal overhead.

## Definition and Key Concepts

### Basic Contract

```dotty
trait Unique[F[_]]:
  def unique: F[Unique.Token]
```

**What this means:**

- `unique` is an **effect** that, when executed, generates a new unique token
- Each call to `unique` returns a **different** `Token` instance
- Tokens are compared **by reference only** (reference equality)

### Companion Object and Syntax

```dotty
object Unique:
  def apply[F[_]: Unique as UN]: UN.type = UN

  final class Token extends Serializable

  object Token:
    given tokenHash: Hash[Token] =
      Hash.fromUniversalHashCode[Token]
```

**Important details:**

- `Token` is a final class, inheritance is forbidden
- Serializable (`Serializable`) — but with caveats (see below)
- Has a `Hash` instance (from Cats), based on `hashCode` (which can collide!)

## Uniqueness Guarantees

### What Is Guaranteed

```dotty
val token1: F[Unique.Token] = Unique[F].unique
val token2: F[Unique.Token] = Unique[F].unique

(token1, token2).mapN { (x, y) => x === y } === Monad[F].pure(false)
```

Two different calls to `unique` **always** yield unequal tokens.

### What Is NOT Guaranteed

1. **Global uniqueness between processes** — tokens are unique only within a single JVM/JavaScript VM
2. **Uniqueness preservation during serialization** — after deserialization, tokens may coincide
3. **`hashCode` uniqueness** — collisions are possible (though unlikely)
4. **Immutability after serialization** — the token loses its "uniqueness"

```dotty
// WRONG: passing over the network
val token = Unique[F].unique
sendOverNetwork(token)  // On the other side, the token may coincide with another!

// WRONG: storing in a database
val token = Unique[F].unique
db.insert(token)  // On the next run, the token may be regenerated
```

### Example of Incorrect Usage

```dotty
case class User(id: Unique.Token, name: String)

def createUser[F[_]: {Unique, Monad}](name: String): F[User] =
  Unique[F].unique.map(token => User(token, name))

// PROBLEM: tokens can't be stored in a database!
```

**Why this is bad:**

- Tokens may repeat on application restart
- Two different JVMs may generate identical tokens
- After deserialization, `equals` may work unpredictably

### Correct Alternative

```dotty
import java.util.UUID

case class User(id: UUID, name: String)

def createUser[F[_]: Sync](name: String): F[User] =
  Sync[F].delay(UUID.randomUUID()).map(uuid => User(uuid, name))
```

**Advantages of UUID:**

- Global uniqueness (collision probability ~10^-15)
- Safe serialization
- Can be passed over the network and stored in a database
- Human-readable representation (optional)

## Where and When to Use `Unique`?

### **Local Execution Tracking**

```dotty
def traced[F[_]: {Unique, Monad, Logger as log}, A](fa: F[A]): F[A] =
  for
    traceId <- Unique[F].unique
    _       <- logger.debug(s"Starting operation with traceId: $traceId")
    result  <- fa
    _       <- logger.debug(s"Finished operation with traceId: $traceId")
  yield result
```

### **Cache With Identification**

```dotty
class Cache[F[_]: {Unique, Ref.Make}, K, V]:
  private val storage: F[Ref[F, Map[Unique.Token, V]]] =
    Ref.of[F, Map[Unique.Token, V]](Map.empty)

  def store(value: V): F[Unique.Token] =
    for
      token <- Unique[F].unique
      cache <- storage
      _     <- cache.update(_ + (token -> value))
    yield token
```

### **Debugging Race Conditions**

```dotty
def debugRace[F[_]: {Unique, Monad}, A, B](
    fa: F[A],
    fb: F[B]
): F[Either[A, B]] =
  for
    raceId <- Unique[F].unique
    _      <- log(s"Starting race $raceId")
    result <- Spawn[F].race(fa, fb)
    _      <- log(s"Race $raceId finished with $result")
  yield result
```

### Unsuitable Scenarios

- **User/order identifiers** → use `UUID` or auto-increment in a database
- **API references** → need stable identifiers
- **Long-term storage** → tokens don't survive restarts
- **Distributed systems** → no guarantees between nodes

## `Unique` Implementations for Various Effects

### Sync Implementation (Via Reference Equality)

```dotty
given syncUnique[F[_]: Sync]: Unique[F] with
  def unique: F[Unique.Token] =
    Sync[F].delay(new Unique.Token()) 
```

**How it works:** In the JVM, each `new` creates a new object on the heap, and reference equality (`eq`) guarantees uniqueness.

## Summary: When and Why to Use `Unique`?

### ✅ **Use when you need:**

- A locally unique identifier within a single effect
- Tracking execution without the overhead of `UUID`
- Implementing low-level primitives (caches, tracing)
- Testing concurrent code

### ❌ **Don't use if:**

- You need global uniqueness (between processes/servers)
- Tokens need to be serialized/stored
- Identifiers are visible to users
- You need a uniqueness guarantee after restart

### 💡 **Golden Rule:**

`Unique.Token` is a **runtime ephemeral identifier**, not a **persistent business key**.
Use it as a local marker, but not as a replacement for `UUID` or auto-increment in a database.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Unique](https://typelevel.org/cats-effect/docs/typeclasses/unique)

[doc]: https://typelevel.org/cats-effect/docs/concepts
