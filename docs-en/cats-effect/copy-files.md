# Copying Files

Let's write a program that copies files. First, we'll develop a pure function that performs the copy,
then we'll embed it in a command-line executable application.

```dotty
// What we want in the end
def copy[F[_]](origin: File, destination: File): F[Long]
```

The function takes the source and destination files, and returns a **description** of the effect,
which, when executed, will copy the data and return the number of bytes copied.

## Problem: Side Effects and Purity

In imperative programming, we would write something like this:

```dotty
// Imperative approach (NOT functional!)
def copyImperative(src: File, dst: File): Long =
  val in  = new FileInputStream(src)  // side effect!
  val out = new FileOutputStream(dst) // side effect!
  try
    val buffer     = new Array[Byte](8192)
    var bytesRead  = 0
    var totalBytes = 0L
    while {
      bytesRead = in.read(buffer)
      bytesRead != -1
    } do
      out.write(buffer, 0, bytesRead)
      totalBytes += bytesRead
    totalBytes
  finally
    in.close()  // side effect!
    out.close() // side effect!
```

**Problems with this approach for functional programming:**

1. **Side effects everywhere** — reading, writing, and closing mutate the outside world.
2. **Exceptions** — `try/catch` violates [referential transparency](../fp/fp/pure_function.md).
3. **Unmanaged cancellation** — if the copy process needs to be interrupted, there's no safe mechanism.
4. **Thread blocking** — when copying large files, the OS thread is blocked.

In Cats Effect we solve these problems by elevating side effects into a **description** (`F[_]`),
which preserves purity and gives control over execution.

## Type Classes: Defining Required Capabilities

The `copy` function will be parameterized by the effect `F[_]`. But what capabilities must `F` have? Let's enumerate the requirements:

| Requirement                   | Why it matters                                                    | Required type class                                      |
|:------------------------------|:------------------------------------------------------------------|:---------------------------------------------------------|
| **Error handling**            | File may not exist, disk may be full, no access rights             | `MonadError[F, Throwable]`                               |
| **Cancellation**              | User pressed Ctrl+C, copying takes too long                        | [`MonadCancel[F, Throwable]`](typeclasses/monadcancel.md)|
| **Blocking operations**       | `read`/`write` at the OS level block the thread                    | [`Sync[F] (provides `blocking`)](typeclasses/sync.md)   |
| **Resource management**       | Guaranteed closure of streams on error or cancellation             | [`Resource (built on `MonadCancel`)](std/resource.md)   |

### Why Not `IO` Everywhere?

You might be tempted to immediately fix `F = IO` and skip type classes:

```dotty
// ❌ Bad: too concrete a type
def copy(origin: File, destination: File): IO[Long]
```

**Problem:** A function with `IO` says: "I can do everything: launch fibers, do timeouts, race conditions."
But for copying files, only a small subset of capabilities is needed. Narrowing the type to `Sync[F]`:

- **Documents intent** — the reader immediately sees: "Only synchronous effects here, no fiber magic."
- **Makes testing easier** — you can substitute a fake effect.
- **Protects against errors** — a new developer won't accidentally add a `.start` or `.race` call inside `copy`, breaking the copy logic.

**Least Power Principle:** Use the weakest type class that solves the problem.

We choose **`Sync[F]`**. It provides:

- `F.delay` — wrapper around a side effect.
- `F.blocking` — special wrapper for blocking operations (instructs the scheduler to allocate a separate thread).
- Inherits `MonadCancel` and `MonadError`, so cancellation and error handling are already included.

```dotty
import cats.effect.Sync
import java.io.File

def copy[F[_]: Sync](origin: File, destination: File): F[Long] = ???
```

## Resource Management: Introducing `Resource`

To open file streams and guarantee closing them (even on error or cancellation), we use `Resource[F, A]`.

Let's create separate functions for opening `InputStream` and `OutputStream`:

```dotty
import cats.effect.{Resource, Sync}
import cats.syntax.all.*
import java.io.{File, FileInputStream, FileOutputStream}

def inputStream[F[_]: {Sync, Logger}](
    f: File
): Resource[F, FileInputStream] =
  Resource.make {
    Sync[F].blocking(new FileInputStream(f)) // acquire
  } { inStream =>
    // log error, but don't let it fail
    Sync[F].blocking(inStream.close())
      .handleErrorWith { error =>
        Logger[F].error(error)("Failed to close source file")
      } // release
  }

def outputStream[F[_]: {Sync, Logger}](
    f: File
): Resource[F, FileOutputStream] =
  Resource.make {
    Sync[F].blocking(new FileOutputStream(f))
  } { outStream =>
    Sync[F].blocking(outStream.close())
      .handleErrorWith { error =>
        Logger[F].error(error)("Failed to close destination file")
      }
  }  
```

**Key points:**

- **`Resource.make(acquire)(release)`** — creates a resource. `acquire` runs on creation, `release` on finalization.
- **`Sync[F].blocking`** — marks the operation as blocking, so Cats Effect can move it to a dedicated thread pool (without blocking the main ones).
- **`.handleErrorWith` in `release`** — an error while closing should not "kill" the finalization of other resources
  (e.g., if `InputStream` cannot be closed, `OutputStream` still needs to be closed).

### Resource Composition

Combine the two resources into one tuple:

```dotty
def inputOutputStreams[F[_]: {Sync, Logger}](
    in: File,
    out: File
): Resource[F, (InputStream, OutputStream)] =
  for
    inStream  <- inputStream(in)
    outStream <- outputStream(out)
  yield (inStream, outStream)  
```

**Guarantees:** If opening `outStream` fails (e.g., no write permissions), then `inStream` will be closed immediately.

## Implementing Copy: Resource and Logic Composition

Now that we've defined safe stream acquisition through `Resource`, the next step is implementing the `copy` function.
Its job is to bind the resources (streams) to the copy logic, ensuring resources are released regardless of the outcome.

```dotty
def copy[F[_]: {Sync, Logger}](
    origin: File,
    destination: File
): F[Long] =
  inputOutputStreams(origin, destination).use { (in, out) =>
    transfer(in, out)   // (*)
  }
```

**Anatomy of the call:**

1. `inputOutputStreams(...)` creates a `Resource[F, (InputStream, OutputStream)]`.
   At this stage, no files are opened yet — this is just a description.
2. The `.use` call — is the critical point. This is where Cats Effect performs `acquire` (opens the streams)
   and passes them to the function you provide: `{ (in, out) => ... }`.
3. Inside the `use` block, we call `transfer(in, out)`, which performs the actual copy.
4. **Guarantee:** When `use` completes (successfully, with an error, or by cancellation),
   Cats Effect will **definitely** perform `release` for all resources, in reverse order: first close `out`, then `in`.

Important consequences of [`Resource`](std/resource.md) semantics:

- If opening `in` fails (file not found), `out` won't even start opening.
- If `in` opens but `out` cannot be opened (no write permissions), `in` will be **immediately closed**.
  (`release` for `in` fires automatically).
- No file descriptor leaks occur — even in a multithreaded concurrent environment.

> `transfer` is a new function that will perform the actual data copying after acquiring the resources (streams).
> When they are no longer needed, regardless of the outcome of `transfer` (success or failure), both streams will be closed.
> If either stream fails to be acquired, `transfer` will not run.

## Copying Data: Writing `transfer`

The `transfer` function must:

1. Read data from `InputStream` into a buffer.
2. Write data from the buffer into `OutputStream`.
3. Repeat until the file ends (`read()` returns `-1`).
4. Count the number of bytes transferred.
5. Do all this **without stack overflow** and with the ability to **cancel** at safe points.

The base loop is recursive. This is the functional replacement for the imperative `while`.

```dotty
def transfer[F[_]: Sync](
    origin: InputStream,
    destination: OutputStream
): F[Long] =
  val buffer = new Array[Byte](1024 * 10) // 10 KB buffer

  def loop(acc: Long): F[Long] = ???

  loop(0L)
```

Detailing the implementation of `loop`:

```dotty
def loop(acc: Long): F[Long] =
  for
    bytesRead <- Sync[F].blocking(origin.read(buffer, 0, buffer.length))
    // If -1 is read (end of file), return the accumulated sum
    result <- if bytesRead == -1 then
      Sync[F].pure(acc) // end of stream, return result
    else
      // Write the read block
      Sync[F].blocking(destination.write(buffer, 0, bytesRead)) >>
        loop(acc + bytesRead)
  yield result
```

**Key implementation details:**

- **`Sync[F].blocking`** is used for all I/O operations.
  This is a signal to the Cats Effect scheduler: "This operation may block the OS thread."
  The scheduler [allocates separate threads for such operations](typeclasses/sync.md), so as not to block the main threads.
- **Recursion** — since `F` is built on [deferred operations](../fp/external-effects/stack_overflow_error.md),
  recursion is safe and does not cause stack overflow.
- **`>>`** — the sequencing operator from Cats. `a >> b` is equivalent to `a.flatMap(_ => b)`,
  i.e., executes `a`, then `b`, ignoring the result of `a`. Here: write data → continue recursion.
- **Counter `acc`** — increments at each iteration by the number of bytes read (`bytesRead`).

If any exception occurs during `transfer` (e.g., disk is full or the network drops),
**the streams will be automatically closed by `Resource`.**
You don't even need to think about it — Cats Effect guarantees `release` blocks execute regardless of the outcome.

## Working with Cancellation: Why This Is Safe

[Cancellation](typeclasses/monadcancel.md) is a powerful but non-trivial cats-effect feature.
Some `F` instances may be cancellable (e.g., if launched in parallel via `.start` or within `.race`).
Cancellation means execution will be interrupted. But Cats Effect provides clear guarantees.

**Key guarantee:**

Code wrapped in `Sync[F].blocking` **cannot be cancelled mid-way**.
Cats Effect waits for the blocking operation (read or write) to complete.
Cancellation can only occur **between** two `blocking` calls.

For example:

```dotty
for
  a <- Sync[F].blocking(origin.read(buffer))  // Cancellation point: BEFORE and AFTER
  b <- Sync[F].blocking(destination.write(...)) // Cancellation point: BEFORE and AFTER
yield ...
```

If cancellation arrives **during the execution** of `origin.read(buffer)`,
Cats Effect will wait for `read()` to complete (return control),
and only then interrupt execution, proceeding to `Resource` finalizers.

**What happens to resources on cancellation?**

Fortunately, `Resource` simplifies cancellation. If the `F` inside `Resource.use` is cancelled,
Cats Effect **automatically launchs the release of all involved resources**.
In our example, this means:

1. The current `transmit` call may be cancelled after completing the current iteration.
2. Control transfers to `.use`.
3. `.use` performs `release` for `out` (closes the file), then `release` for `in`.
4. All descriptors are closed, data is not corrupted.

The program's user (e.g., who pressed Ctrl+C) gets an immediate response, and the system remains in a consistent state.
Without `Resource`, you would have to manually track a "cancelled" flag and close streams in `finally` — which is error-prone.

## Final Program: `IOApp`

Now that we have the pure `copy[F[_]]` function, we can create a concrete command-line program.
For this, we use `IOApp` — the "functional" equivalent of Scala's traditional `App`.

`IOApp` allows:

- Defining a pure `run: IO[ExitCode]` function.
- Automatically handling OS signals (Ctrl+C) as `IO` cancellation.
- Ensuring `IO` execution with the correct scheduler and thread pool.

### Implementing `run`

```dotty
import cats.effect.*
import cats.syntax.all.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.io.*

object CopyFiles extends IOApp:

  // Logger for IO (transmitted automatically as implicit)
  private given Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    for
      // 1. Argument validation
      _ <- if args.length < 2 then
              IO.raiseError(
                new IllegalArgumentException("Source and destination files not specified")
              )
            else IO.unit

      // 2. Prepare File objects
      orig = new File(args.head)
      dest = new File(args(1))

      // 3. Call the pure copy function
      count <- copy[IO](orig, dest)

      // 4. Log the result
      _ <- Logger[IO].info(
        s"$count bytes copied from ${orig.getPath} to ${dest.getPath}"
      )
    yield ExitCode.Success
```

**What's important here:**

- **`args.length`** — we check the input parameters. `IO.raiseError` lifts the error inside `IO`.
  Since `IO` implements `MonadError`, subsequent steps (`orig = ...`, `copy`) will not be executed, and the error will be caught.
- **`copy[IO](orig, dest)`** — we call our polymorphic function, specializing `F = IO`.
  Since `IO` implements the `Sync` instance, everything compiles.
- **`Logger[IO].info`** — we log the result. Note that logging itself is an effect, and it's built into the chain.

### Why Is This Better Than `java.nio.file.Files.copy`?

One might argue that using `IO { java.nio.file.Files.copy(src, dst, options) }` would give `IO` the same purity characteristics as our function.

**But there's a critical difference:**

- `IO { Files.copy(...) }` — is a black box that cannot be cancelled. It will execute to completion, blocking the thread.
  Pressing Ctrl+C will have no effect on the copy.
- Our `copy` is built on `Resource` and `Sync[F].blocking`. It can be **cancelled** at any safe point (between reads).
  On cancellation, file descriptors will be closed, and the system won't be left in a half-finished state.

This cancellability becomes critical if `copy[F]` is used as part of a larger system,
where timeouts or race conditions require interrupting long-running operations.

## Summary: Not Fearing Cancellation

1. **Use `Resource` for everything that can be "opened and closed".** It handles cancellation automatically.
2. **Wrap I/O operations in `Sync[F].blocking`.** This tells the scheduler not to interrupt the operation until it naturally completes.
3. **Write recursive loops via `flatMap` (or for-comprehension).** Cats Effect optimizes these.
4. **Use `IOApp` in the final program.** It binds pure descriptions to real execution, signal handling, and the thread pool.

Final program structure:

```dotty
object CopyFiles extends IOApp:
  def transfer[F[_]: Sync](in: InputStream, out: OutputStream): F[Long] = // recursive loop
  def copy[F[_]: Sync](orig: File, dest: File): F[Long] = // Resource.use
  def run(args: List[String]): IO[ExitCode] = // IOApp entry point
```

Now you have not just a copy function, but a **concurrency-safe, cancellable, and pure** abstraction
that can be used with any `F` implementing `Sync`.

---

**Links:**

- [Source code](https://gitverse.ru/artemkorsakov/scalabook/content/master/examples/src/main/scala/ru/scalabook/libs/cats/effect/CopyFiles.scala)
- [Cats effect documentation](https://typelevel.org/cats-effect/docs/concepts)
- [Cats effect File Copying](https://typelevel.org/cats-effect/docs/tutorial)
