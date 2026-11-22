# Hotswap — Safe Live Resource Replacement

## Motivation: The Problem with Nested `Resource`s

Imagine you are writing a logger that needs to **rotate (switch) log files** every `n` bytes. How is this usually done?

The naive solution: open another one inside the `use` of one resource.

```dotty
// ❌ PROBLEMATIC CODE: resource leak!
def badRotatingLogger(n: Int): Resource[IO, Logger[IO]] =
  file("0.log").flatMap { file0 =>
    Resource.eval {
      Ref[IO].of(0).map { bytesWritten =>
        new Logger[IO]:
          def log(msg: String): IO[Unit] =
            bytesWritten.get.flatMap { current =>
              if msg.length + current <= n then
                // Still writing to the current file
                write(file0, msg) *> bytesWritten.update(_ + msg.length)
              else
                // ❌ BAD: opening a new file inside use!
                file("1.log").use { file1 =>
                  write(file1, msg) *> bytesWritten.set(msg.length)
                }
            }
      }
    }
  }
```

**What's wrong here?**

| Problem                       | Explanation                                                                                                                                                     |
|:------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Memory leak**               | The outer file (`file0`) won't be closed until the inner resource (`file1`) is released. And the inner resource is only released after `use` completes.           |
| **Nested finalization**       | `Resource` guarantees reverse-order release (first `file1`, then `file0`), but `file0` continues to exist for as long as `file1` lives.                          |
| **Growing memory usage**      | With repeated log rotation, all previous files remain open, waiting for the last one to complete.                                                                 |

**Root cause:** When you call `Resource#use` inside another `Resource`,
the inner resource becomes **dependent** on the outer one.
The outer resource will be finalized **only after** the inner one is finalized.
This leads to accumulating "dangling" resources.

## `NonEmptyHotswap`

`NonEmptyHotswap` is a primitive that provides a **linear sequence of resources** as a single `Resource`. It allows:

1.  **Starting** with some initial resource.
2.  **Replacing** the current resource with a new one (`swap`), at which point the old resource is **immediately finalized**.
3.  **Getting** the current resource (`get`) for use.

```dotty
sealed trait NonEmptyHotswap[F[_], R]:

  /** Replaces the current resource with a new one. The previous resource is finalized. */
  def swap(next: Resource[F, R]): F[R]

  /** Returns the current resource (for use inside `use`). */
  def get: Resource[F, R]
```

**Key guarantees:**

- `get` always returns the **current** resource.
- On `swap`, the old resource is **immediately** finalized, and the new one becomes current.
- When the outer `Resource` (created via `NonEmptyHotswap`) is finalized, the **current** resource is also finalized.
- **Safety:** it is impossible to use the old resource after `swap` — the type system won't allow it
  (the old `Resource` becomes inaccessible).

## Example: Log Rotation

Back to the logger task. Here is how it is solved correctly:

```dotty
import cats.effect.{IO, Ref, Resource}
import cats.effect.std.NonEmptyHotswap

def rotatingLogger(n: Int): Resource[IO, Logger[IO]] =

  // Helper function: creates a Log Resource for a file
  def file(name: String): Resource[IO, File] =
    Resource.make(openFile(name))(file => close(file))

  // Function to write to a file
  def write(file: File, msg: String): IO[Unit] = ???

  // Create NonEmptyHotswap with initial file "0.log"
  NonEmptyHotswap[IO, File](file("0.log")).flatMap { hotswap =>
    // State: current file index and number of bytes written
    Resource.eval {
      for
        index <- Ref[IO].of(0) // current file number
        bytes <- Ref[IO].of(0) // how many bytes written to current file
      yield new Logger[IO]:
        def log(msg: String): IO[Unit] =
          bytes.get.flatMap { currentBytes =>
            val msgLen = msg.length

            // If message fits in the current file
            if msgLen <= n - currentBytes then
              hotswap.get.use { currentFile =>
                write(currentFile, msg) *>
                  bytes.update(_ + msgLen)
              }
            // Otherwise — rotate the file
            else
              for
                // Reset byte counter
                _ <- bytes.set(msgLen)
                // Increase file index
                newIdx <- index.updateAndGet(_ + 1)
                // Close old file and open new one
                _ <- hotswap.swap(file(s"$newIdx.log"))
                // Write message to new file
                _ <- hotswap.get.use(write(_, msg))
              yield ()
          }
    }
  }
end rotatingLogger
```

### Step-by-Step Explanation

1.  **Initialization:** `NonEmptyHotswap[IO, File](file("0.log"))` creates a `Hotswap` with file `0.log` already active inside.
2.  **Logging (normal case):** If the message fits in the current file, we get the current file via `hotswap.get.use` and write to it.
3.  **Rotation (critical case):** When the message doesn't fit:
    - Reset the byte counter (`bytes.set(msgLen)`).
    - Increase the file index (`index.updateAndGet(_ + 1)`).
    - Call `hotswap.swap(file(s"$newIdx.log"))`:
        - The **old file** (e.g., `0.log`) is **immediately closed**.
        - The **new file** (`1.log`) is opened and becomes current.
    - Write the message to the new file.
4.  **Completion:** When the outer `Resource` (logger) is finalized, `NonEmptyHotswap` automatically closes the last active file.

**Advantages over the "naive" approach:**

- No leaks: old files are closed immediately upon rotation.
- Linear memory usage: only **one** file is open at any given time.
- Safe composition: `NonEmptyHotswap` handles the complex lifecycle itself.

## When to Use `NonEmptyHotswap` vs `Resource`?

| Task                                             | Solution                  | Why                                                         |
|:-------------------------------------------------|:--------------------------|:------------------------------------------------------------|
| Simple work with a single resource               | `Resource`                | Explicit `acquire`/`release` management.                    |
| Resource chain (A → B → C), where B depends on A | `Resource` + `flatMap`    | Reverse-order release.                                      |
| **Infinite or dynamic resource rotation**         | **`NonEmptyHotswap`**     | Old resources are closed immediately, not accumulated.      |
| Connection pool, where connections are reused    | `Resource` + `Semaphore`  | A different task.                                           |

## Practical Usage Examples

### Example 1: Cyclic File Rotation (Bounded Number)

```dotty
def rotatingFileLogger(
    maxFiles: Int,
    maxBytesPerFile: Int
): Resource[IO, Logger[IO]] =
  NonEmptyHotswap[IO, File](file("0.log")).flatMap { hotswap =>
    Resource.eval {
      Ref[IO].of(0).flatMap { fileIdx =>
        Ref[IO].of(0).flatMap { bytesWritten =>
          def nextFile: IO[Unit] =
            for
              idx <- fileIdx.updateAndGet(i => (i + 1) % maxFiles)
              _   <- hotswap.swap(file(s"$idx.log"))
              _   <- bytesWritten.set(0)
            yield ()

          IO.pure(new Logger[IO]:
            def log(msg: String): IO[Unit] =
              bytesWritten.get.flatMap { written =>
                if written + msg.length <= maxBytesPerFile then
                  hotswap.get.use(write(_, msg)) *>
                    bytesWritten.update(_ + msg.length)
                else
                  nextFile >> log(msg) // recursively write to new file
              })
        }
      }
    }
  }
```

### Example 2: Switching Between Configurations at Runtime

```dotty
case class Config(host: String, port: Int)

def resourceForConfig(config: Config): Resource[IO, HttpClient] = ???

def configurableClient(initialConfig: Config)
    : Resource[IO, (Config => IO[Unit], HttpClient)] =
  NonEmptyHotswap[IO, HttpClient](resourceForConfig(initialConfig)).flatMap {
    hotswap =>
      Resource.eval(Ref[IO].of(initialConfig)).flatMap { currentConfig =>
        val updater = (newConfig: Config) =>
          for
            _ <- currentConfig.set(newConfig)
            _ <- hotswap.swap(resourceForConfig(newConfig))
          yield ()

        hotswap.get.map { client =>
          (updater, client)
        }
      }
  }

// Usage:
configurableClient(Config("localhost", 8080)).use { case (update, client) =>
  for
    _ <- client.send(request1) // use localhost:8080
    _ <- update(Config("remote.com", 9090))
    _ <- client.send(request2) // use remote.com:9090
  yield ()
}
```

## Important Nuances

### 1. `NonEmptyHotswap` Is Exactly "Non-Empty"

The name emphasizes that there is **always** a current resource. You cannot create a `Hotswap` without an initial resource.
This guarantees that `get` always returns a valid `Resource`.

### 2. `swap` Does Not Return the Old Resource

Unlike some other FFI, `swap` **does not return** the old resource for subsequent use.
This is intentional: the old resource is immediately finalized, and you must not have access to it.
If you need to do something with the old resource before closing, use `Resource#onFinalize`.

### 3. Is `swap` Idempotent?

No, each `swap` opens a new resource and closes the current one.
Don't call `swap` without necessity — it is an expensive operation (open/close).

### 4. Cancellability

`NonEmptyHotswap` works correctly with cancellation:

- If a fiber is cancelled during `swap`, the new resource won't be activated,
  and the old one remains (or is closed, depending on implementation).
- Resource finalization on cancellation is guaranteed.

## Summary: When to Use `NonEmptyHotswap`?

✅ **Use `NonEmptyHotswap` if:**

- You need to **dynamically replace** one resource with another at runtime.
- The old resource should be **immediately freed** (not wait for the entire `Resource` to complete).
- Typical examples: log rotation, configuration switching, live connection switching, live queues.

❌ **Don't use `NonEmptyHotswap` if:**

- You have a simple linear resource chain (use `Resource` + `flatMap`).
- You need to atomically replace several resources simultaneously (this primitive is for one).
- You work with a resource pool where resources are reused (you need `Semaphore` + `Resource`).

**Key idea:** `NonEmptyHotswap` is a "leak-killer" for scenarios
where resources must replace each other in a long-lived process.
It takes on all the complexity of finalization, leaving you with a clean and clear API for rotation.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Hotswap](https://typelevel.org/cats-effect/docs/std/hotswap)

[doc]: https://typelevel.org/cats-effect/docs/concepts
