# GraalVM Native Image and Cats Effect

## What Is GraalVM Native Image?

**GraalVM Native Image** is a technology that allows compiling Java applications
(and applications in other JVM languages, including Scala) into **native executable files**.
Unlike the traditional JVM model (code compiled to bytecode, which is then interpreted or JIT-compiled),
the native image is AOT-compiled into machine code for a specific platform.

**Key advantages of native images:**

| Feature                    | Traditional JVM                          | GraalVM Native Image                                    |
|:---------------------------|:-----------------------------------------|:--------------------------------------------------------|
| **Startup time**           | Hundreds of milliseconds to seconds      | Milliseconds (typically 5-50 ms)                        |
| **Memory consumption**     | High (especially in the first minutes)   | Significantly lower                                     |
| **Executable file size**   | JAR file + JVM                           | Single executable file (~10-50 MB)                      |
| **Peak performance**       | High (after JIT warmup)                  | Lower than a warmed-up JVM                              |
| **Cross-platform**         | Yes (thanks to JVM)                      | No (must compile for each platform separately)          |

**When to use native images:**

- **CLI utilities** — fast startup is important.
- **Serverless / FaaS (AWS Lambda, Cloud Functions)** — cold start is critical.
- **Microservices** with low memory requirements.
- **Embedded systems** with limited resources.

**When NOT to use native images:**

- Long-running servers with high peak performance requirements (the warmed-up JVM will be faster).
- Applications heavily using dynamic features (reflection, proxies, dynamic class loading).
- When cross-platform portability matters (must compile the image for each OS/architecture separately).

## Cats Effect and Native Image: What Works

Starting with **Cats Effect 3.3.0**, support was added for key features for working in native images:

1. **Fiber Dumps:** The functionality using `java.lang.reflect.Proxy`
   for handling POSIX signals (`SIGINFO`/`SIGUSR1`) is fully compatible with Native Image.
   Cats Effect JARs contain **additional metadata** for reflection configuration needed by GraalVM.
2. **Minimum GraalVM version:** Native Image support is guaranteed for **GraalVM 21.0 and above**.
   Earlier versions may work but are not officially supported.
3. **Important limitation:** You must use the `--no-fallback` flag during compilation.
   Otherwise, `native-image` will attempt to find static reflection configuration
   for `scala.Enumeration` methods, which will cause a failure.
   If your code uses `scala.Enumeration`, this approach won't work (see [issue #3051](https://github.com/typelevel/cats-effect/issues/3051)).

## Complete Example: Hello World with sbt-native-image

### 1. Plugin Configuration (`project/plugins.sbt`)

Add the `sbt-native-image` plugin:

```dotty
// project/plugins.sbt
addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.1")
```

### 2. Project Configuration (`build.sbt`)

```dotty
// build.sbt
ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "3.8.3"

lazy val root = (project in file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "cats-effect-3-hello-world",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.7.0",
    Compile / mainClass := Some("com.example.Main"),
    nativeImageOptions += "--no-fallback",      // CRITICAL FLAG!
    nativeImageVersion := "22.1.0"              // Minimum 21.0.0
  )
```

### 3. Application Code (`src/main/scala/com/example/Main.scala`)

```dotty
package com.example

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  def run: IO[Unit] = IO.println("Hello Cats Effect!")
```

### 4. Compilation

```bash
sbt nativeImage
```

The successfully compiled native image will be located at:

```
target/native-image/cats-effect-3-hello-world
```

### 5. Running and Benchmarking

```bash
# Simple run
$ ./target/native-image/cats-effect-3-hello-world
Hello Cats Effect!

# Measure startup time using hyperfine
$ hyperfine ./target/native-image/cats-effect-3-hello-world
Benchmark 1: ./target/native-image/cats-effect-3-hello-world
  Time (mean ± σ):      11.2 ms ±   0.6 ms    [User: 6.7 ms, System: 4.8 ms]
  Range (min … max):     9.4 ms …  12.9 ms    182 runs
```

**Result:** Startup time of about **11 milliseconds** — that's 1-2 orders of magnitude faster
than running the same application on the JVM (typically 100-500 ms).
This is critical for serverless environments and CLI utilities.

## Alternative Approach: Scala CLI

If you don't use sbt, you can compile a native image using the `package` command from **scala-cli**:

```bash
scala-cli package --native-image --graalvm-jvm-id 22.1.0 --no-fallback Main.scala
```

(This assumes you already have `scala-cli` and the required GraalVM version installed.)

## Troubleshooting and Pitfalls

### 1. The `--no-fallback` Flag Is Mandatory

Without this flag, `native-image` may attempt to create an image that on error can "fall back" to running on the JVM.
This leads to reflection analysis attempts for `scala.Enumeration`, which lack static configuration and will fail.

**Solution:** Always add `nativeImageOptions += "--no-fallback"`.

### 2. Using `scala.Enumeration`

If your code or dependencies use `scala.Enumeration`,
directly using `--no-fallback` may be unsafe (see [comment in issue #3051](https://github.com/typelevel/cats-effect/issues/3051#issuecomment-1167026949)).
It is recommended to avoid enums in code that will be compiled to a native image.
Alternatives: `sealed trait + case object`, `enumeratum` (with caution), or Java enums.

### 3. Dynamic Features

Code heavily using reflection, dynamic proxies, `Class.forName`, etc.,
may require additional configuration (JSON files describing reflection).
Cats Effect provides metadata for its internal needs, but your code does not.

**Solution:** Add `nativeImageOptions ++= Seq("-H:ReflectionConfigurationFiles=path/to/reflect-config.json")` as needed.

### 4. GraalVM Version

Ensure your GraalVM version is **22.1.0** or newer (21.0.0 works, but 22.x has improvements). Check the version with:

```bash
gu --version
native-image --version
```

### 5. Compilation Memory

Compiling native images requires a significant amount of memory (several gigabytes).
Ensure you allocate enough memory for the `native-image` process:

```bash
export JAVA_OPTS="-Xmx6g"
sbt nativeImage
```

## Final Checklist for Successful Compilation

- GraalVM version ≥ 21.0.0 (22.1+ recommended)
- `sbt-native-image` plugin version ≥ 0.3.1
- In `build.sbt`: `nativeImageOptions += "--no-fallback"`
- In `build.sbt`: `nativeImageVersion := "22.1.0"`
- Code does not use `scala.Enumeration` (or you're prepared for additional configuration)
- Enough memory for compilation (4-6 GB recommended)

**Future:** Native Image support in Cats Effect is actively being developed.
Stay tuned to updates in the [official documentation](https://typelevel.org/cats-effect/) for new versions.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect GraalVM Native Image](https://typelevel.org/cats-effect/docs/core/native-image)

[doc]: https://typelevel.org/cats-effect/docs/concepts
