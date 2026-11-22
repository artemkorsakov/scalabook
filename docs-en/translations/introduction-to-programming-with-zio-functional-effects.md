---
layout: default
title: Introduction to ZIO Functional Effects
parent: Translations
summary: Getting started with ZIO, a functional effect system for Scala.
nav_order: 8
---

# Introduction to ZIO Functional Effects
{: .no_toc }

{% include toc.html %}

# Introduction to ZIO Functional Effects

ZIO is a functional effect system for Scala that provides a purely functional approach
to handling side effects, concurrency, and error handling.

## What is ZIO?

`ZIO[-R, +E, +A]` represents a computation that:

- May require a environment of type `R`
- May fail with an error of type `E`
- Succeeds with a value of type `A`

## Basic Effects

```scala
import zio._

// A simple effect that succeeds with a value
val helloWorld: ZIO[Any, Nothing, String] = ZIO.succeed("Hello, World!")

// An effect that may fail
val failingEffect: ZIO[Any, String, Nothing] = ZIO.fail("Something went wrong!")

// An effect that requires an environment
val configEffect: ZIO[Config, Nothing, String]  = ZIO.service[Config].getEnv
```

## Running Effects

```scala
import zio.Runtime

val runtime = Runtime.default

// Blocking run
runtime.unsafeRun(helloWorld)

// Async run
runtime.unsafeRun(helloWorld fork)
```

## Combinators

```scala
// Mapping over success
val mapped: ZIO[R, E, B] = effect.map(a => b)

// Chaining effects
val chained: ZIO[R, E, C] = effect.flatMap(a => effect2)

// Error handling
val recovered: ZIO[R, E2, A] = effect.catchAll(e => recoveryEffect)

// Combining effects
val combined: ZIO[R, E, (A, B)] = effect1.zip(effect2)
```

## Concurrency

```scala
// Run two effects in parallel
val parResult: ZIO[R, E, (A, B)] = effect1.zipPar(effect2)

// Race
val raceWinner: ZIO[R, E, Either[A, B]] = effect1.race(effect2)

// Fork and join
val fiber: Fiber[E, A] = effect.fork
val result: Result[E, A] = fiber.join
```

## ZIO Streams

```scala
import zio.stream._

val stream: ZStream[Any, Nothing, Int] =
  ZStream.fromIterable(1 to 100)

val streamedResult: ZStream[Any, Nothing, String] =
  stream.map(n => s"Number: $n")
```

## Example: HTTP Client

```scala
import zio.http._

val clientEffect: ZIO[HttpClient, Exception, Response] =
  Http.get(url).flatMap { response =>
    if response.status.isSuccess then
      ZIO.succeed(response.body)
    else
      ZIO.fail(new RuntimeException("HTTP error"))
  }
```

## ZIO Test

```scala
import zio.test._

object MySpec extends ZIOSpecDefault:
  def spec = suite("My Suite")(
    test("simple success") {
      assertTrue(1 + 1 == 2)
    },
    test("effectful test") {
      for
        result <- ZIO.succeed(42)
      yield assertTrue(result == 42)
    }
  )
```

## Further Reading

- [ZIO Documentation](https://zio.dev/docs/)
- [ZIO GitHub](https://github.com/zio/zio)
- [ZIO Streams](https://zio.dev/docs/datatypes/ziostreams)
