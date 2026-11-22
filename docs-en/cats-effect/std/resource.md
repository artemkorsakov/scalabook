# Resource — Safe Resource Management

## Motivation: The Problem of Resource Management

In any application, one pattern arises frequently:

1. **Acquire** a resource (open a file, establish a network connection, start a thread).
2. **Use** the resource to do useful work.
3. **Release** the resource (close the file, disconnect the connection, stop the thread),
   **regardless** of whether the work completed successfully, with an error, or was cancelled.

### Traditional Approach: `bracket`

Cats Effect provides the `bracket` method (from `MonadCancel`) specifically for this purpose:

```dotty
def bracket[A, B](acquire: F[A])(use: A => F[B])(release: A => F[Unit]): F[B]
```

**How it works:**

1.  `acquire` — acquire the resource (e.g., open a file).
2.  `use` — use the resource for computation.
3.  `release` — **guaranteed** release of the resource after `use`, even if `use` failed with an error or was cancelled.

### Problem: Nested "Hell" with Multiple Resources

When there is only one resource, the code looks fine. But what if we need to open **three files** simultaneously?
The code becomes deeply nested and hard to read:

```dotty
val concat: IO[Unit] =
  IO.bracket(openFile("file1")) { file1 =>
    IO.bracket(openFile("file2")) { file2 =>
      IO.bracket(openFile("file3")) { file3 =>
        for
          bytes1 <- read(file1)
          bytes2 <- read(file2)
          _      <- write(file3, bytes1 ++ bytes2)
        yield ()
      }(file3 => close(file3))
    }(file2 => close(file2))
  }(file1 => close(file1))
```

**Drawbacks of this approach:**

| Problem                             | Description                                                                           |
|:------------------------------------|:--------------------------------------------------------------------------------------|
| **Deep nesting**                    | Each new resource adds an indentation level (the so-called "pyramid of doom").       |
| **Mixed responsibility**           | Resource acquisition logic is tightly coupled with its usage logic.                   |
| **Error-prone composition**         | Easy to mix up the closure order or forget to release.                               |
| **Low reusability**                 | Can't easily create a "resource" that will then be used in different places.         |

## What is `Resource`?

`Resource[F[_], A]` is an **abstraction** that encapsulates **two things**:

1. How to **acquire** (`acquire`) a value of type `A`.
2. How to **release** (`release`) that value.

`Resource` forms a **monad** (it has `map` and `flatMap`),
which allows us to **combine several resources** into a single composite resource, **avoiding nesting**.

### Creating and Using

```dotty
object Resource:
  // Creates a Resource from an acquisition effect and a release function
  def make[F[_], A](acquire: F[A])(release: A => F[Unit]): Resource[F, A]

  // Converts any effect into a Resource (empty releaser)
  def eval[F[_], A](fa: F[A]): Resource[F, A]

abstract class Resource[F[_], A]:
  // Uses the resource: acquires it, passes it to the function, and guarantees release
  def use[B](f: A => F[B]): F[B]
```

### How Does It Work?

- **`Resource.make(acquire)(release)`** creates a "recipe" for acquiring and releasing a resource.
- **`res.flatMap`** or **for-comprehension** combines several such recipes into one.
- **`res.use(f)`** — the only moment when the resource is **actually acquired**, passed to `f`,
  and **after its completion (or error) — immediately released** in reverse order.

**Most important rule:** `Resource` **does not manage the resource constantly.**
The resource lives only inside `use`. As soon as `use` completes — the resource is destroyed.

## Example #1: Safe File Work

Back to our three-file example. Here is how `Resource` solves the nesting problem:

```dotty
def file(name: String): Resource[IO, File] =
  Resource.make(openFile(name))(file => close(file))

val concat: IO[Unit] = (
  for
    in1 <- file("file1") // These lines don't open files!
    in2 <- file("file2") // They just create "recipes"
    out <- file("file3") // Composite Resource[IO, (File, File, File)]
  yield (in1, in2, out)
).use {
  case (f1, f2, f3) => // And here files are opened
    for
      bytes1 <- read(f1)
      bytes2 <- read(f2)
      _      <- write(f3, bytes1 ++ bytes2)
    yield ()
} // And here (after use) they are guaranteed to be closed (in reverse order)
```

**What happened here?**

1.  **`file("file1")`** doesn't open the file. It creates a `Resource[IO, File]` — a "promise" to open the file in the future and close it after use.
2.  **for-comprehension** combines three such promises into one: `Resource[IO, (File, File, File)]`.
3.  **`.use`** — this is the entry point. It:
     - **Acquires all three** files (if one doesn't open, the others won't even start opening!).
     - Passes them to the use function.
     - After the function completes (success, error, cancellation) **closes the files in reverse order**: first `f3`, then `f2`, then `f1`.

**Key advantages:**

- **No nesting:** code is linear and readable.
- **Composition:** `file` is a reusable building block.
- **Safety:** closure order is guaranteed, even on cancellation or error.

## Example #2: Combination with `eval` and Working with Connections

`Resource.eval` is useful when you have an effect that doesn't need to be released but is part of the "resource" logic.

```dotty
def openConnection(host: String, port: Int): Resource[IO, Socket] =
  Resource.make(IO.println(s"Connecting to $host:$port") >> Socket(host, port))(
    s =>
      IO.println(s"Closing connection to $host:$port") >> s.close
  )

def getConfig(conn: Socket): IO[String] =
  conn.send("GET /config") >> conn.receive

val program: IO[Unit] = (
  for
    _    <- Resource.eval(IO.println("Starting work"))    // Just an effect
    conn <- openConnection("localhost", 8080)               // Resource
    _    <- Resource.eval(IO.println("Connection opened")) // Another effect
  yield conn
).use { conn => // Here the connection is already open and ready for use
  for
    config <- getConfig(conn)
    _      <- IO.println(s"Received config: $config")
  yield ()
} // Here the connection will be automatically closed
```

## Important Guarantees and Behavior Details

### 1. Guaranteed Reverse-Order Release

```dotty
val res =
  for
    a <- Resource.make(acquireA)(releaseA)
    b <- Resource.make(acquireB)(releaseB)
    c <- Resource.make(acquireC)(releaseC)
  yield (a, b, c)

res.use(???)
// Order of calls:
// 1. acquireA
// 2. acquireB
// 3. acquireC
// 4. use(...)
// 5. releaseC
// 6. releaseB
// 7. releaseA
```

This is critically important because resources often depend on each other (e.g., `b` may depend on `a`).

### 2. `acquire` and `release` Are Uncancellable

**`acquire` and `release` are `uncancellable` (uncancelable).** This means:

- **During `acquire`**: the resource will be fully acquired, even if a cancellation occurred. This prevents the "half-acquired resource" state.
- **During `release`**: the release will be fully completed, without interruption. This guarantees that the resource won't "leak".
- **Cancellation is possible only during `use`** (between `acquire` and `release`).

```dotty
Resource.make(IO.println("I can't be interrupted!"))( _ => IO.println("Neither can this!"))
```

### 3. `Resource` Is a "One-Shot" Use

Each `use` call leads to a **new acquisition** of the resource.

```dotty
val fileResource: Resource[IO, File] = Resource.make(openFile("a.txt"))(close)

// ❌ WRONG: file will be opened and closed twice, but for different operations!
val program = fileResource.use(read) >> fileResource.use(read)

// ✅ CORRECT: file is opened once, and both read operations are performed with it
val program = fileResource.use { file => read(file) >> read(file) }
```

**Consequence:** Don't hold `Resource` as a long-lived object that you plan to read from multiple times.
Instead, create it once for a single "session" of work.

## Advanced Features and Patterns

### `allocated` — "Extract" a Resource from `Resource`

```dotty
def allocated: F[(A, F[Unit])]
```

Executes `acquire`, but **doesn't release** the resource automatically. Returns a tuple: (value, finalizer).
**This function should be used with caution**, as it breaks the safety guarantees of `Resource`.

```dotty
val (socket, finalize) =
  Resource.make(openSocket)(close).allocated.unsafeRunSync()
try
  socket.send("hello")
finally
  finalize.unsafeRunSync() // don't forget to call!
```

### `onFinalize` — Add an Action to Release

```dotty
def onFinalize(f: F[Unit]): Resource[F, A]
```

Adds an additional effect to the release.

```dotty
val res = Resource.make(openFile)(close).onFinalize(IO.println("File will be closed"))
```

### `Resource.fromAutoCloseable` — For Java Resources

A utility for `AutoCloseable` resources (files, sockets, JDBC connections).

```dotty
val file = Resource.fromAutoCloseable(IO(new FileInputStream("data.bin")))
```

## Summary: When to Use `Resource`?

| Task                                                                    | Solution                                                         |
|:------------------------------------------------------------------------|:-----------------------------------------------------------------|
| **Working with one file/socket/connection**                             | `Resource.make` + `use`                                          |
| **Combining several dependent resources**                               | `for` inside `Resource` (as in the file example)                 |
| **Safe work with Java resources (AutoCloseable)**                       | `Resource.fromAutoCloseable`                                     |
| **Simple effect without release (part of a chain)**                     | `Resource.eval`                                                  |
| **Need access to the resource from a long-lived server (don't close)** | Consider `Resource` inside a loop, recreating it as needed       |

`Resource` is the standard, idiomatic way to work with resources in Cats Effect.
It replaces chains of nested `bracket` with clean, composable, and safe code.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Resource](https://typelevel.org/cats-effect/docs/std/resource)
- [Composable resource management in Scala](https://bszwej.medium.com/composable-resource-management-in-scala-ce902bda48b2)
- [Baeldung: Resource Handling in Cats Effect](https://www.baeldung.com/scala/cats-effect-resource-handling)
- [Essential Effects by Adam Rosien](https://essentialeffects.dev/)
- [Resource Handling in Cats Effect 3 Part 5](https://yadukrishnan.live/resource-handling-in-cats-effect-3-part-5)

[doc]: https://typelevel.org/cats-effect/docs/concepts
