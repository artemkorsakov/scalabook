# Applications

## What Is Stream Processing?

Imagine you need to transfer water from a lake to a pool. You can:

1. **Drain the entire lake at once** (load the whole file into memory) — slow, requires huge containers (memory), 
   and can lead to disaster if the container bursts (out of memory).
2. **Use a hose** (stream processing) — water is transferred in small, continuous portions. 
   This is efficient, safe, and you can start filling the pool while the lake is not yet fully "processed".

**Stream processing** is exactly this kind of "hose" for data. 
You process information not all at once, but in pieces ("chunks"), as they arrive. 
This is a key abstraction for working with large, infinite, or slow data sources.

## Where Is Stream Processing Used?

The ideas from the "Stream Processing" chapter are not dry theory, but a powerful tool 
that you have already seen, perhaps without realizing it.

- **File I/O**
    - **What it is:** The most classic example. Reading and writing files.
    - **What it looks like in code:**
        - **Without streams:** `val entireFile: String = readFile("huge_log.txt")` → Wait until a 100 GB file 
          loads entirely into RAM (likely the program will crash).
        - **With streams:** `Stream[IO, String] = readFileLineByLine("huge_log.txt")` → The file is read line by line. 
          Each line is processed and immediately discarded, freeing memory. Any file size can be processed.
    - **Details:** It works not only with text! 
      Audio, video, images — any binary data can be processed with streams.
- **Message Processing, Finite State Machines, and Actors**
    - **What it is:** The architecture of large systems (e.g., microservices), 
      where components communicate by sending messages to each other (e.g., via Kafka, RabbitMQ).
    - **What it looks like:** Each component (e.g., "Order Processing Service") can be represented 
      as `Stream[IO, Order]` — a stream of incoming order messages. 
      It transforms them into another stream — `Stream[IO, Invoice]` (an invoice stream). 
      This is a high-level description of complex system behavior through composition of simple streams.
- **Servers and Web Applications**
    - **What it is:** Any web server.
    - **What it looks like:** A server is a huge `Pipe[IO, Request, Response]` (a pipe that transforms requests into responses).
        - **Input stream:** An infinite stream of HTTP requests from users: `Stream[IO, Request]`.
        - **Output stream:** A stream of generated responses: `Stream[IO, Response]`.
    - **Example (http4s):** The [http4s](https://github.com/http4s/http4s) framework, built on [FS2][fs2], works exactly like this.
- **User Interface (UI) Programming**
    - **What it is:** Reactive interfaces where user actions instantly affect the display 
      (like in modern SPA frameworks such as React, but at a conceptual level).
    - **What it looks like:**
        - **Event stream:** `val clickStream: Stream[IO, MouseClick]` — a stream of mouse clicks.
        - **Data stream:** `val apiDataStream: Stream[IO, Data]` — a stream of data coming from the server.
    - **UI as a function:** The interface is a function that combines these streams 
      and determines how the screen should change in response to each new event or data.
- **Big Data and Distributed Systems**
    - **What it is:** Processing petabytes of data (e.g., in Apache Spark, Flink).
    - **Details:** Streaming graph nodes (processing stages) can be distributed across different computers in a cluster. 
      One computer filters data, another aggregates it, a third saves it to a database. Data flows between them. 
      Libraries like [FS2][fs2] provide abstractions that allow describing such logic and then "scaling" it to a cluster.

## FS2 Library: Industrial Implementation

**[FS2][fs2] (Functional Streams for Scala)** is not a learning example, but a full-fledged, battle-tested library 
that implements all the ideas described above for the Scala language.

- **What it provides:** `Stream`, `Pull`, `Pipe` — these are the main types you work with.
- **Key capabilities:**
    - **Concurrency:** Easily run parts of a streaming graph in parallel.
    - **Broadcasting:** Route data through different "channels" for processing.
    - **Safe resource handling:** Guaranteed closing of files, sockets, etc., even if an error occurs in the middle of the stream.
    - **Working with effects:** Deep integration with modern functional programming libraries 
      ([Cats Effect](https://github.com/typelevel/cats-effect), [ZIO](https://zio.dev/)), making code predictable and reliable.
- **Ecosystem:** [Many popular libraries](https://fs2.io/#/ecosystem) are built on FS2, 
  proving its power and versatility. Examples:
    - **http4s:** Web framework where both server and client are streams.
    - **doobie:** Library for working with databases. Your SQL queries and their results can be represented as streams.

## Summary (Key Concepts in Simple Words)

- **Imperative I/O vs. Streaming I/O:**
    - **Imperative:** "Read the whole file, then do something with it" — monolithic and unsafe.
    - **Streaming:** "Start transferring data from the file, and as each chunk arrives, apply operations to it" — 
      incremental, compositional, and safe.
- **Main Types:**
    - **`Stream[F, O]`:** This is the "hose" with data. It emits values of type `O`, 
      performing side effects (like reading from a file) in the context of `F` (e.g., `IO`).
    - **`Pull[F, O, R]`:** This is an "instruction" or "plan" for building a stream. 
      It can emit values `O` and at the end return a final _result_ `R` 
      (e.g., the number of processed bytes).
    - **Relationship:** `Stream` is a ready-made data stream. `Pull` is a tool for its flexible construction. 
      They can be easily converted to each other.
- **`Pipe[F, I, O]`:** This is a **transformer** of streams. An alias for the function `Stream[F, I] => Stream[F, O]`. 
  Imagine a segment of pipe that takes a stream of one type of data (input `I`) and returns a stream of another (output `O`).
    - **Examples of `Pipe`:**
        - `filter(_ > 0)`: Filters numbers, keeping only positive ones.
        - `map(_.toUpperCase)`: Converts each string to uppercase.
    - **Composition:** "Pipes" can be connected. `cleanData andThen validate andThen saveToDB` — a classic processing chain.
- **Resource Safety:** One of the main advantages. 
  FS2 guarantees that if you open a file via `Stream.resource`, it will be **definitely closed**, 
  even if an error occurred during processing or you took only the first 10 lines (`take(10)`).
- **Working with State:** `Ref` is simply a "safe variable" whose access occurs within effect `F`. 
  Useful when different elements of a stream need access to shared mutable state (e.g., a counter).

## Summary

Stream processing is a **mindset** about how data moves and transforms in your program. 
Instead of working with entire arrays of data, you work with a continuous stream, applying a series of small transformations to it. 
This leads to code that is:

- **Efficient:** Low memory consumption.
- **Reliable:** Safe resource management.
- **Composable:** Complex tasks assembled like Lego from simple pieces (`map`, `filter`, `flatMap`).
- **Versatile:** Applicable from reading files to building distributed systems.

The [FS2][fs2] library provides all the necessary tools for applying this approach in Scala at an industrial level.

---

**Links:**

- [Link to the repository with exercises][sample]
- [Functional Programming in Scala, Second Edition, Chapter 15](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)

[sample]: https://gitverse.ru/scalabook/fp
[fs2]: https://fs2.io/#/
