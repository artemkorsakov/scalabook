# Env — Reading Environment Variables

`Env[F[_]]` is a **type class** providing a functional
and safe way to read process **environment variables**.
It abstracts access to environment variables, hiding platform-specific details and potential errors.

```dotty
trait Env[F[_]]:
  /** Returns the value of an environment variable, if it is defined. */
  def get(name: String): F[Option[String]]

  /** Returns all environment variables as an iterable collection of (name, value) pairs. */
  def entries: F[Iterable[(String, String)]]
```

## Why `F[Option[String]]`, Not Just `Option[String]`?

At first glance, reading an environment variable may seem like a pure operation
that shouldn't require an `F` wrapper.
However, there are several reasons why Cats Effect places it in an effect:

### 1. Environment Variables Can Change at Runtime (POSIX)

On POSIX-compliant systems (Linux, macOS), a process can change
its own environment variables via `setenv` and `unsetenv`.
Even if the process doesn't do this, **another thread** can change the environment (though it is very poor practice).

```c
// C code that can run in parallel with your Scala code
setenv("MY_VAR", "new_value", 1);
```

Cats Effect cannot guarantee that the value of an environment variable will remain unchanged
between consecutive `get` calls.
Since `F` can sequentially execute effects, placing `get` in an effect explicitly signals that
the operation **may yield different results on each call**.

### 2. Reading the Environment Can Fail

In some environments (e.g., Windows with insufficient permissions),
reading an environment variable can throw a `SecurityException`.
The `F` wrapper allows correctly lifting this error into the effect context:

```dotty
env.get("PATH").handleErrorWith { case _: SecurityException =>
  Console[F].errorln("No permissions to read variable PATH")
}
```

### 3. Cross-Platform Compatibility (JVM, Scala.js, Scala Native)

The environment reading implementation differs across platforms:

| Platform          | Characteristics                                                                                    |
|:------------------|:---------------------------------------------------------------------------------------------------|
| **JVM**           | Uses `System.getenv()`, which can return `null` or throw `SecurityException`.                      |
| **Scala Native**  | Calls libc (`getenv`), which requires interaction with native code.                                 |
| **Scala.js**      | There are no environment variables in the browser (only available via Node.js API).               |

The `F` wrapper hides these differences and provides a unified interface.

### 4. Environment Variables Are Not Pure

From the perspective of referential transparency, `Env.get("PATH")` doesn't have to return the same value on each call.
Placing the operation in `F` explicitly signals its **non-determinism** and **dependency on the outside world**.

## Usage Examples

### Reading a Single Variable

```dotty
import cats.effect.{IO, IOApp}
import cats.effect.std.Env

object ReadEnv extends IOApp.Simple:
  val run: IO[Unit] =
    for
      maybeHome <- Env[IO].get("HOME")
      _         <- maybeHome match
        case Some(home) => IO.println(s"Home directory: $home")
        case None       => IO.println("HOME variable is not set")
    yield ()
```

### Reading All Variables

```dotty
val program = Env[IO].entries.flatMap { vars =>
  vars.toList.traverse_ { case (k, v) =>
    IO.println(s"$k = $v")
  }
}
```

### Error Handling

```dotty
import cats.effect.IO
import cats.effect.std.Env

val safeGet = Env[IO].get("SECRET_KEY").attempt.flatMap {
  case Right(Some(value)) => IO.println(s"Key found: ${value.take(3)}...")
  case Right(None)        => IO.println("Key not set")
  case Left(e)            => IO.println(s"Access error: ${e.getMessage}")
}
```

### Validating Required Variables

```dotty
def requireEnv(name: String): IO[String] =
  Env[IO].get(name).flatMap {
    case Some(value) => IO.pure(value)
    case None        => IO.raiseError(
        new Exception(s"Missing required variable: $name")
      )
  }

val config =
  for
    dbUrl  <- requireEnv("DATABASE_URL")
    apiKey <- requireEnv("API_KEY")
    mode   <- requireEnv("MODE").map(_.toLowerCase)
  yield AppConfig(dbUrl, apiKey, mode)
```

## Platform-Specific Features

| Platform               | Availability    | `get` Characteristics                                                                                                |
|:-----------------------|:----------------|:---------------------------------------------------------------------------------------------------------------------|
| **JVM**                | ✅ Fully         | Can throw `SecurityException`. Returns `null` for missing variables (converted to `None`).                           |
| **Scala Native**       | ✅ Fully         | Uses `getenv` from libc. Works similarly to JVM.                                                                     |
| **Scala.js (Node.js)** | ⚠️ Partially    | Access via `process.env`. The `entries` method is supported.                                                         |
| **Scala.js (browser)** | ❌ Not available | No access to environment variables. `get` always returns `None`, `entries` returns empty `Iterable`.                |

## Comparison with Other Approaches

| Approach                                    | Result Type                   | Error Handling                     | Cross-Platform |
|:--------------------------------------------|:------------------------------|:-----------------------------------|:---------------|
| **`Env[F].get`**                            | `F[Option[String]]`           | ✅ Via `F` error mechanism          | ✅ Yes          |
| `System.getenv` (JVM)                       | `String` (may be `null`)      | ❌ `SecurityException` (synchronous)| ❌ JVM only     |
| `scala.scalanative.posix.stdlib.getenv`     | `CString`                     | ❌ Platform-dependent               | ❌ Native only  |
| `process.env` (Node.js)                     | `string \| undefined`         | ❌ Synchronous                       | ❌ JS only      |

## Typical Use Cases

| Scenario                              | Example                                         |
|:--------------------------------------|:------------------------------------------------|
| **Reading configuration**             | `DATABASE_URL`, `REDIS_HOST`, `LOG_LEVEL`      |
| **Feature toggles**                   | `ENABLE_FEATURE_X=true`                        |
| **Environment (dev/staging/prod)**    | `APP_ENV=production`                           |
| **API keys**                          | `SERVICE_API_KEY=abc123` (store in secrets!)   |
| **Deployment parameters**             | `KUBERNETES_NAMESPACE=default`                 |

## Summary

✅ **Use `Env` when you need to:**

- Read several environment variables in a cross-platform application.
- Elegantly handle a missing variable or access error.
- Preserve referential transparency and code uniformity.

❌ **Don't use `Env` if:**

- You need complex configuration with types and validation (use `pureconfig`).
- You are writing an app only for JVM and prefer `System.getenv` (but `Env` is still more convenient in tests).

**Key idea:** `Env` is a functional, safe, and cross-platform wrapper around the impure world of environment variables.
It turns potential global mutations and exceptions into honest `F[Option[String]]` effects.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Env](https://typelevel.org/cats-effect/docs/std/env)

[doc]: https://typelevel.org/cats-effect/docs/concepts
