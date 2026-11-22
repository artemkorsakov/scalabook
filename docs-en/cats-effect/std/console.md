# Console — Simple Console I/O

## What is `Console`?

`Console[F[_]]` is a **type class** providing standard methods for reading
and writing data through the **console** (standard input/output).
It is designed for **simple interactive programs** (REPL, educational examples, small utilities),
but it is **not suitable** for logging or serious production code.

```dotty
trait Console[F[_]]:
  // Output (stdout)
  def print[A](a: A)(using S: Show[A] = Show.fromToString[A]): F[Unit]
  def println[A](a: A)(using S: Show[A] = Show.fromToString[A]): F[Unit]

  // Error output (stderr)
  def error[A](a: A)(using S: Show[A] = Show.fromToString[A]): F[Unit]
  def errorln[A](a: A)(using S: Show[A] = Show.fromToString[A]): F[Unit]

  // Print stack trace
  def printStackTrace(t: Throwable): F[Unit]

  // Read a line (stdin)
  def readLine: F[String]

  // Other methods (readLineWithCharset, putStr, ...) — platform-dependent
```

**Key feature:** output methods use `cats.Show` to convert values to strings.
If you don't have a custom `Show` instance, the standard `toString` is used.

## When to Use `Console`, and When Not To?

| Scenario                                                            | Tool                                           | Why                                                      |
|:--------------------------------------------------------------------|:-----------------------------------------------|:---------------------------------------------------------|
| **Interactive program** (user prompts, CLI game)                     | `Console`                                      | Simplicity, conciseness.                                 |
| **REPL** (read-eval-print loop)                                      | `Console`                                      | Perfect fit.                                             |
| **Logging (debug info)**                                            | `IO.println` or `Sync[F].delay(println(...))` | `Console` may be overkill.                               |
| **Production logging**                                              | `slf4j`, `log4cats`, `logback`                 | `Console` provides no log levels, formatting, rotation.  |
| **Complex text interface**                                          | `fs2.io.stdin`, `fs2.io.stdout`                | `readLine` is not always cancelable (see below).         |

## Example: Greeting the User

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.Console

object HelloUser extends IOApp.Simple:
  val run: IO[Unit] =
    for
      _    <- Console[IO].println("Please enter your name:")
      name <- Console[IO].readLine
      _    <- if name.nonEmpty then
        Console[IO].println(s"Hello, $name!")
      else
        Console[IO].errorln("Name cannot be empty!")
    yield ()
```

**How it works:**

1. `println` outputs a string to **stdout**.
2. `readLine` blocks the fiber (semantically) until the user enters a line and presses Enter.
3. `errorln` outputs a message to **stderr** (error stream).

## Platform-Dependent Limitations

`Console` works on all three target platforms of Cats Effect (JVM, Scala.js, Scala Native), but **not equally everywhere**.

### JVM: The `readLine` Cancellation Problem

On JVM, `readLine` is based on `System.in.read()`, which **does not support interruption** via standard Java mechanisms.
This leads to a fatal problem:

```dotty
import scala.concurrent.duration.*

// ❌ THIS WILL DEADLOCK ON JVM!
IO.readLine.timeoutTo(3.seconds, IO.pure("timeout"))
```

**Why?**

- `System.in.read()` blocks the thread at the OS level.
- Cats Effect cannot "wake up" this thread on cancellation.
- The only ways to interrupt `readLine` are: the user enters a line, or `System.in` is closed.

**Solution:** Use asynchronous alternatives (e.g., `fs2.io.stdin` combined with `fs2`).

### Scala Native: Similar Problem

`readLine` on Scala Native is also based on blocking input and cannot be cancelled by timeout.

### Scala.js: `readLine` Is Not Implemented!

On the JavaScript platform (Node.js or browser), `readLine` is **not available**. Console input is not a standard operation.

**Alternative:** Use `fs2.io.stdin` from `fs2-io` library (Node.js) or implement input via HTML (browser).

## Advanced Features

### Custom Formatting via `Show`

`Console` methods accept a hidden `Show[A]`. This allows controlling how values are converted to strings.

```dotty
import cats.Show

case class User(name: String, age: Int)

given Show[User] = Show.show { u =>
  s"User: ${u.name}, age: ${u.age}"
}

val program = Console[IO].println(User("Anna", 25))
// Output: "User: Anna, age: 25"
```

### Printing Stack Trace

```dotty
def safeDiv(a: Int, b: Int): IO[Int] =
  IO(a / b).handleErrorWith { t =>
    Console[IO].errorln("Division error!") >>
      Console[IO].printStackTrace(t) >>
      IO.raiseError(t)
  }
```

## Alternatives for Production Logging

If you need logging in production, don't use `Console`. Instead:

| Library                | Purpose                                             |
|:-----------------------|:----------------------------------------------------|
| **`log4cats`**         | Functional logging with levels (debug, info, error) |
| **`slf4j` + `logback`**| Standard JVM logging infrastructure                   |
| **`natchez`**          | Distributed tracing                                   |

Example with `log4cats`:

```dotty
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

logger.info("Application started") >>
logger.error(throwable)("Critical error")
```

## Summary: When to Use `Console`?

✅ **Use `Console` if:**

- You are writing a **simple interactive application** (CLI game, educational example, one-off script).
- You need a **quick way to output** information to stdout/stderr.
- You are working on **JVM or Scala Native** and are OK with `readLine` being non-cancelable.

❌ **Don't use `Console` if:**

- You need **production logging** (levels, rotation, formatting).
- You are writing on **Scala.js** and need console input (use `fs2.io.stdin`).
- You need **cancelable input** or **timeouts** on JVM (also switch to `fs2`).
- The project already uses `log4cats` or similar libraries.

**Key idea:** `Console` is a simple and convenient bridge to the console for small programs,
but it has serious limitations.
For larger applications, choose specialized logging and asynchronous I/O tools.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Console](https://typelevel.org/cats-effect/docs/std/console)

[doc]: https://typelevel.org/cats-effect/docs/concepts
