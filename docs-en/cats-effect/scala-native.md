# Scala Native

[Scala Native](https://github.com/scala-native/scala-native) is a technology that
allows compiling Scala code directly into **native machine code** (Native code),
bypassing the Java Virtual Machine (JVM). Unlike the traditional JVM model (bytecode → interpretation / JIT compilation),
Scala Native uses a compiler based on [LLVM](https://llvm.org/) to produce standalone executables.

**Key advantages of Scala Native:**

| Feature                        | JVM Compilation                                    | Scala Native                                        |
|:-------------------------------|:---------------------------------------------------|:----------------------------------------------------|
| **Startup time**               | Hundreds of milliseconds to seconds                | **Microseconds — milliseconds**                     |
| **Memory consumption**         | High (including JVM)                               | **Significantly lower**                              |
| **Final artifact size**        | JAR (from tens to hundreds of MB) + JVM            | Single executable (units or tens of MB)             |
| **Peak performance**           | High (after JIT warmup)                            | Lower than a warmed-up JVM                          |
| **Multithreading support**     | Full (multiple threads)                            | **Single-threaded** (currently)                     |
| **Direct native code calls**   | Via JNI (with significant overhead)                | Yes, directly (no overhead)                          |

**When to use Scala Native:**

- **CLI utilities** — fast startup is critical.
- **Scripts** — you need instant startup, comparable to Bash or Python.
- **Serverless / FaaS (AWS Lambda, Cloud Functions)** — cold start time is critical.
- **Embedded systems** with strict memory constraints.
- **High-performance computing** with native code calls (BLAS, LAPACK, CUDA).

**When to prefer JVM/Cats Effect:**

- Long-running servers with high peak throughput requirements (JVM is faster after warmup).
- Applications heavily using concurrency (Scala Native is currently single-threaded).
- When maximum portability matters ("compiled one JAR — runs everywhere").

## Cats Effect Support

Starting with version **Cats Effect 3.3.14**, the library **fully supports Scala Native**.
This is the result of significant work porting `IO`, work-stealing pools,
`Resource`, `Ref`, `Deferred`, `Queue`, `Semaphore`, and other primitives to the Native platform.

> **Important:** At the moment, the Scala Native runtime is **single-threaded**
> (but can switch between fibers). Libraries requiring multithreading (e.g., thread-pool caches)
> will run in single-threaded mode.
> This is not a significant limitation for CLI, parsers, workers, scripts, and many other applications.

## Complete Hello World Example

### 1. Plugin Configuration (`project/plugins.sbt`)

```dotty
// Check for the current version
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.17")
```

### 2. Project Configuration (`build.sbt`)

```dotty
ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "3.8.3"

lazy val root = project.in(file("."))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    name := "cats-effect-native-demo",
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.7.0",
    Compile / mainClass := Some("com.example.Main")
  )
```

### 3. Application Code (`src/main/scala/com/example/Main.scala`)

```dotty
package com.example

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  def run: IO[Unit] = IO.println("Hello Cats Effect! I am a native binary.")
```

### 4. Compilation

```bash
sbt nativeLink
```

The native executable will be created at:

```
target/scala-3.8.3/cats-effect-native-demo-out
```

### 5. Running and Benchmarking

```bash
# Simple run
$ ./target/scala-3.8.3/cats-effect-native-demo-out
Hello Cats Effect! I am a native binary.

# Measure startup time using hyperfine
$ hyperfine ./target/scala-3.8.3/cats-effect-native-demo-out
Benchmark 1: ./target/scala-3.8.3/cats-effect-native-demo-out
  Time (mean ± σ):       7.5 ms ±   1.2 ms    [User: 3.6 ms, System: 2.3 ms]
  Range (min … max):     6.0 ms …  17.9 ms    141 runs
```

**Result:** Startup in 7.5 ms — comparable to Go or C++ CLI utilities and significantly faster (10–100x) than JVM startup.

## Scala Native Limitations Important for Cats Effect

### 1. No Multithreading (Currently)

```dotty
// On JVM this would create multiple threads.
// On Native everything runs in a single thread, but Cats Effect fibers
// can still cooperate with each other.
val fib1 = longComputation.start
val fib2 = anotherLongComputation.start
```

**Consequences:**

- **`IO#unsafeRunSync` is unavailable** — the thread-blocking method that waits for completion. It doesn't make sense in a single-threaded environment.
- **`IO.blocking(...)` behaves like `IO.delay(...)`** — there's no dedicated blocking pool.
  If you use `blocking` on Native, it simply blocks the single execution thread.
- **Parallel collections (`parTraverse`)** work, but fiber switching happens within a single thread.

### 2. No `evalOn` (Thread Pool Switching)

The `IO.executionContext` and `IO.evalOn` methods don't make sense in a single-threaded environment.
On call, they simply ignore the provided `ExecutionContext` and continue executing on the main thread.

### 3. No Fiber Dumps via POSIX Signals

Scala Native does not support `sun.misc.Signal`. Fiber dumps (`SIGUSR1` / `SIGINFO`) are unavailable in Native applications.

### 4. Execution Time and Behavior

Everything that doesn't require system calls (pure computations, `flatMap`, `map`) runs very fast.
At the same time, you can safely call C native libraries (e.g., for GPU work, vectorization, system API).

## Alternative Compilation Approaches: `scala-cli`

If you don't use sbt, you can compile a native image using `scala-cli`:

```bash
scala-cli package --native Main.scala -o hello-native
./hello-native
```

This may be simpler for small scripts and utilities.

## Real-World Example: `http4s` HTTP Server on Native

The [`scala-native-ember-example`](https://github.com/ChristopherDavenport/scala-native-ember-example) project
demonstrates a full HTTP server based on Http4s and Ember, compiled to a native binary.
This proves that even despite single-threadedness, you can build efficient network applications on Scala Native.

### Architecture of such a server:

- **One thread** handles all incoming connections.
- Cats Effect fibers switch between sockets and requests cooperatively.
- **Non-blocking I/O** (epoll / kqueue) is used for socket operations.
- All of this within a single OS thread can handle thousands of connections.

## Conclusion and Recommendations

| If your scenario...                                       | Recommended Approach                                       |
|:----------------------------------------------------------|:-----------------------------------------------------------|
| CLI utility, script, worker                               | ✅ **Scala Native** (instant startup, small size)           |
| Server with intensive I/O (http4s, FS2)                   | ⚠️ Possible on Native, but with limitations                |
| Highly concurrent app on a multi-core CPU                 | 🟡 **JVM** (Native is currently single-threaded)           |
| Serverless / Lambda                                       | ✅ **Scala Native** (cold start in 5-10 ms)                |
| App heavily using `IO.blocking`                           | ❌ **JVM** (`blocking` is useless in Native)               |

**Future:** The lack of multithreading in Scala Native is a temporary limitation.
The Scala Native team is actively working on multithreading support.
Once this functionality becomes stable, Cats Effect on Native will get full JVM-level parallelism
(but with all the benefits of a native binary).

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Scala Native](https://typelevel.org/cats-effect/docs/core/scala-native)

[doc]: https://typelevel.org/cats-effect/docs/concepts
