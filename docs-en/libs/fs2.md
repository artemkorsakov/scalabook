# FS2

[**FS2**][home] is a library for working with functional, efficient, and parallel streams in Scala.

It provides powerful abstractions for processing data in a streaming fashion,
allowing you to work with input/output (e.g., network connections and files) using constant memory consumption,
perform stateful transformations,
ensure safe resource management, and control side effects.

The library is built on top of [**Cats Effect**](../cats-effect/index.md)
(a well-known framework for asynchronous and concurrent programming in Scala)
and serves as the foundation for several popular Typelevel ecosystem projects, including:

- [**http4s**](https://http4s.org) — a functional HTTP library,
- [**Skunk**](https://typelevel.org/skunk/) — a purely functional PostgreSQL client,
- [**Doobie**](https://tpolecat.github.io/doobie/) — a functional JDBC library for working with databases.

## Dependencies (SBT)

To start using FS2, add the following dependencies to your `build.sbt` ([current version](https://mvnrepository.com/artifact/co.fs2)):

```sbt
// Core module — required for working with streams
libraryDependencies += "co.fs2" %% "fs2-core" % "3.13.0"

// Additional module for I/O operations (files, network)
libraryDependencies += "co.fs2" %% "fs2-io" % "3.13.0"

// Module for Reactive Streams integration (asynchronous stream standard)
libraryDependencies += "co.fs2" %% "fs2-reactive-streams" % "3.13.0"

// Module for working with binary formats via the scodec library
libraryDependencies += "co.fs2" %% "fs2-scodec" % "3.13.0"
```

## Usage Example

FS2 is a streaming I/O library.
Its core principles are composability, expressiveness, resource safety, and high performance.

Consider a simple but illustrative example:

```dotty
import cats.effect.{IO, IOApp}
import fs2.{Stream, text}
import fs2.io.file.{Files, Path}

object Converter extends IOApp.Simple:

  private val converter: Stream[IO, Unit] =
    def fahrenheitToCelsius(f: Double): Double = (f - 32.0) * (5.0 / 9.0)

    Files[IO].readUtf8Lines(Path("testdata/fahrenheit.txt"))
      .withFilter(s => s.trim.nonEmpty && !s.startsWith("//"))
      .map(line => fahrenheitToCelsius(line.toDouble).toString)
      .intersperse("\n")
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(Path("testdata/celsius.txt")))

  // Program entry point — executing the stream and collecting final results
  def run: IO[Unit] =
    converter.compile.drain
end Converter
```

### What does this code do?

The program reads the file `testdata/fahrenheit.txt` **line by line**, _without loading the entire file into memory_.  
It skips empty lines and lines starting with `//` (comments).  
Each non-empty line is interpreted as a temperature in Fahrenheit, converted to Celsius,
and written as a number to the output file `testdata/celsius.txt`.

> Writing is also performed **incrementally as data arrives**,
> which ensures **constant memory consumption** —  
> the amount of memory used does not depend on the size of the input and output files.

Additionally, the library automatically closes file descriptors both on successful completion
and in case of exceptions — an example of **resource safety**.

Note that for simplicity, the example is specialized for the `IO` effect (from Cats Effect).
However, the `Stream[F, A]` type is fully polymorphic with respect to the effect `F[_]` — it can work with any effect
that supports the required Cats Effect type classes (e.g., `IO`, `Task`, `SyncIO`, and others).

## Additional Capabilities

The FS2 library goes far beyond simple file I/O and provides many advanced usage scenarios:

### 1. Zipping and merging streams

You can combine multiple data sources into a single stream, for example, zipping
or merging elements from different streams using an arbitrary transformation function.
The flexibility of stream composition is virtually limitless thanks to a rich algebra of combinators.

### 2. Dynamic resource allocation

A stream can allocate resources dynamically — for example, reading a list of files obtained via a network connection,
and processing each one.
The library guarantees correct release of all acquired resources upon completion (both normal and abnormal).

### 3. Nondeterministic and concurrent processing

A stream can concurrently read from multiple input channels and process results as they arrive (whichever returns first).
Thanks to built-in concurrency combinators and data structures,
streams can serve as lightweight declarative execution flows (threads),
enabling the construction of complex concurrent behavior compositionally, without explicit low-level synchronization management.

## FS2

FS2 is not just a streaming I/O library,
but a universal, declarative, and composable tool for describing arbitrary control flows,
data processing, and concurrent computations in a functional style.
It fits perfectly into the [Typelevel ecosystem](https://typelevel.org/projects/) and is the standard choice for many projects
requiring high performance, safety, and expressiveness when working with asynchronous and streaming data.

## FS2 Ecosystem

Libraries using FS2 and integrations with data storage.

### Database Clients & Storage

Libraries for working with databases, key-value stores, and file systems:

- [doobie](https://github.com/tpolecat/doobie): A purely functional JDBC client built on top of fs2.
- [ldbc](https://github.com/takapi327/ldbc): A purely functional JDBC layer with Cats Effect 3 and Scala 3.
- [mongo4cats](https://github.com/Kirill5k/mongo4cats): A Mongo DB client wrapper for Cats Effect and FS2.
- [neotypes](https://github.com/neotypes/neotypes): A lightweight, type-safe, and asynchronous driver for neo4j in Scala. Supports streaming queries using fs2.
- [redis4cats](https://redis4cats.profunktor.dev/): A Redis client built on top of fs2 and cats-effect.
- [fs2-blobstore](https://github.com/fs2-blobstore/fs2-blobstore): A minimal, idiomatic, stream-based Scala interface for S3, GCS, SFTP, and other key-value store implementations.

### Message Queues & Messaging

Libraries for working with message brokers and queue systems:

- [fs2-rabbit](https://fs2-rabbit.profunktor.dev/): A stream-based RabbitMQ client built on top of fs2 and cats-effect.
- [fs2-google-pubsub](https://github.com/permutive/fs2-google-pubsub): A [Google Cloud Pub/Sub](https://cloud.google.com/pubsub/) implementation using fs2 and cats-effect.
- [kafka4s](https://github.com/Banno/kafka4s): Functional programming with Kafka and Scala.
- [fd4s/fs2-kafka](https://github.com/fd4s/fs2-kafka): Functional Kafka streams for Scala.

### Networking & Protocols

Libraries for working with various network protocols:

- [http4s](http://http4s.org/): A minimal, idiomatic Scala interface for HTTP services using fs2.
- [fs2-grpc](https://github.com/typelevel/fs2-grpc): A gRPC implementation for FS2 / Cats Effect.
- [fs2-ftp](https://github.com/regis-leray/fs2-ftp): A simple Ftp/Ftps/Sftp client using fs2 and cats-effect.
- [fs2-ssh](https://github.com/slamdata/fs2-ssh): An Apache SSHD wrapper focused on cats-effect and fs2.

### Cloud Providers

Libraries for integration with cloud services:

- [fs2-aws](https://github.com/saksdirect/fs2-aws): FS2 streams for interacting with AWS services.


### Data Processing & Formats

Libraries for parsing, serialization, and processing various data formats:

- [fs2-data](https://github.com/satabin/fs2-data): A collection of data parsers and libraries for manipulating various formats for fs2.
- [spata](https://github.com/fingo/spata): A CSV parser built on FS2.
- [mysql-binlog-stream](https://github.com/laserdisc-io/mysql-binlog-stream): Streaming MySQL binary log (binlog) events using FS2.

### Scheduling & Time

Libraries for working with time, schedules, and rate limiting:

- [fs2-cron](https://github.com/fthomas/fs2-cron): FS2 streams based on cron expressions.
- [fs2-throttler](https://github.com/kovstas/fs2-throttler): A throttling mechanism for fs2, based on the "token bucket" algorithm.
- [upperbound](https://github.com/SystemFw/upperbound): A purely functional interval-based rate limiter with backpressure support.

### Security & Encryption

- [fs2-aes](https://github.com/jwojnowski/fs2-aes): AES encryption for fs2.

### System Processes

- [prox](https://vigoo.github.io/prox/): A library for working with system processes.

### Domain-Specific

Libraries for solving specific tasks:

- [vinyldns](https://github.com/vinyldns/vinyldns): A DNS management system that uses fs2 to rate-limit updates to DNS backends.

---

**References:**

- [Fs2 home page][home]
- [Aquascape project](https://zainab-ali.github.io/aquascape/reference/)
- [A sky full of streams by Jakub Kozłowski](https://www.youtube.com/watch?v=oluPEFlXumw)
- [A streaming library with a superpower: FS2 and functional programming](https://medium.freecodecamp.org/a-streaming-library-with-a-superpower-fs2-and-functional-programming-6f602079f70a)
- [An introduction to FS2 Streams by Michael Pilquist](https://www.youtube.com/watch?v=B1wb4fIdtn4)
- [Basic streams and combinators in fs2 by Jakub Kozłowski](https://www.youtube.com/watch?v=TmhIabcu6Fs)
- [Build a Stream to fetch everything behind an offset paged API by David Francoeur](https://davidfrancoeur.com/post/paged-api-fs2/)
- [Compose your program flow with Stream by Fabio Labella](https://www.youtube.com/watch?v=x3GLwl1FxcA)
- [Compositional Streaming with FS2 by Michael Pilquist](https://www.youtube.com/watch?v=oFk8-a1FSP0)
- [Declarative Control Flow with fs2 Stream by Fabio Labella](https://www.youtube.com/watch?v=YSN__0VEsaw)
- [FS2 - Crash course by Łukasz Byczyński](https://www.youtube.com/watch?v=tbShO8eIXbI)
- [FS2 Internals: Performance by Michael Pilquist](https://www.youtube.com/watch?v=TXxzMF14pxU)
- [How slow is your tram? by Michał Pawlik](https://www.youtube.com/watch?v=E-gnY0weqsk)
- [Inference Driven Design by Michael Pilquist](https://mpilquist.github.io/blog/2018/07/04/fs2/)
- [Introduction to FS2: Functional Streams for Scala](https://www.baeldung.com/scala/fs2-functional-streams)
- [Leverage FS2 API to write to a file by David Francoeur](https://davidfrancoeur.com/post/file-writing-fs2/)
- [My fs2 (was scalaz-stream) User Notes](https://devon-miller.gitbook.io/test_private_book)
- [No leftovers: Working with pulls in fs2 by Zainab Ali](https://blog.kebab-ca.se/chapters/fs2/overview.html)
- [Scala FS2 — handle broken CSV lines by Alexey Novakov](https://medium.com/se-notes-by-alexey-novakov/scala-fs2-handle-broken-csv-lines-8d8c4defcd88)
- [Sharding a stream of values using Fs2 by Gabriel Volpe](https://www.youtube.com/watch?v=FWYXqYQWAc0)
- [Tips for working with FS2 by Pere Villega](https://underscore.io/blog/posts/2018/03/20/fs2.html)
- [Writing a simple Telegram bot with tagless final, http4s and fs2 by Vladimir Pavkin](https://pavkin.ru/writing-a-simple-telegram-bot-with-tagless-final-http4s-and-fs2/)

[home]: https://fs2.io/#/
