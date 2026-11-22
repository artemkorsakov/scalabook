# I/O

## Networking

The `fs2-io` library supports both TCP and UDP through the `fs2.io.net` package. 
This package provides purely functional abstractions over Java NIO (Non-blocking I/O) networking support. 
The package fully leverages the resource safety guarantees provided by cats-effect and `fs2.Stream`.

### TCP

The `fs2.io.net.Socket` trait provides mechanisms for reading and writing data — both as individual actions 
and as part of streaming programs.

#### Clients

To start, let's write a client program that connects to a server, sends a message, and reads a response.

```dotty
import fs2.Chunk
import fs2.io.net.Network
import cats.effect.MonadCancelThrow
import cats.effect.std.Console
import cats.syntax.all.*
import com.comcast.ip4s.*

def client[F[_]: {MonadCancelThrow, Console, Network}]: F[Unit] =
  Network[F].connect(SocketAddress(host"localhost", port"5555")).use { socket =>
    socket.write(Chunk.array("Hello, world!".getBytes)) >>
      socket.read(8192).flatMap { response =>
        Console[F].println(s"Response: $response")
      }
  }
```

**Explanation:**

- To open a socket connected to `localhost:5555`, we use the `connect` method on the `Network` instance 
  (this is a type class providing the runtime for the sockets being created).
- `Network[F].connect` returns a `Resource[F, Socket[F]]`, which automatically closes the socket after use. 
  This ensures that even in case of an error, the socket will be properly released.
- To write data to the socket, `socket.write` is called, which takes a `Chunk[Byte]` and returns `F[Unit]`.
- After writing, we read once from the socket via `socket.read`, 
  passing the maximum number of bytes we want to read. The method returns `F[Option[Chunk[Byte]]]`:
  - `None` — if the socket has reached the end of input (connection closed).
  - `Some` — if the read returned a chunk of data (possibly smaller than requested).
- Finally, we print the response to the console.

**Important note:** in this example, we do not use any framing or message-packetizing protocol. 
Therefore, it is quite possible that a single read will receive only part of the original message — for example, only bytes for `"Hello, w"`. 
This happens because TCP is a streaming protocol and does not preserve message boundaries.

To simplify this task, we can use FS2 streams. The `Socket` trait defines streaming operations — `writes` and `reads`. 
Let's rewrite the example using these operations:

```dotty
import fs2.{Stream, text}
import fs2.io.net.Network
import cats.effect.MonadCancelThrow
import cats.effect.std.Console
import com.comcast.ip4s.*

def client[F[_]: {MonadCancelThrow, Console, Network}]: Stream[F, Unit] =
  Stream.resource(Network[F].connect(SocketAddress(
    host"localhost",
    port"5555"
  ))).flatMap { socket =>
    Stream("Hello, world!")
      .through(text.utf8.encode)
      .through(socket.writes) ++
      socket.reads
        .through(text.utf8.decode)
        .foreach { response =>
          Console[F].println(s"Response: $response")
        }
  }
```

**Structural changes:**

1. The socket resource is immediately lifted into a stream with `Stream.resource`.
2. We create a stream from a single string `"Hello, world!"`, convert it via `text.utf8.encode` to `Stream[Pure, Byte]`, 
   and then convert again via `socket.writes`, which gives us `Stream[F, Unit]`. 
   The `socket.writes` method returns a `Pipe` that writes each chunk of the input stream to the socket.
3. We then append a stream that reads the response — `socket.reads`, which yields `Stream[F, Byte]`, 
   terminating when the socket is closed or an end-of-input indicator is received. 
   We convert this stream via `text.utf8.decode`, yielding `Stream[F, String]`, and print each received response to the console.

**Problem:** this program will not terminate until the server side closes the socket or indicates no more data. 
To fix this, we need a protocol agreed upon by both the client and the server. 
Since we are working with text, let's use a simple protocol 
where each frame (or "packet" or "message") is terminated by the `\n` character. 
Let's update both writing and reading:

```dotty
def client[F[_]: {MonadCancelThrow, Console, Network}]: Stream[F, Unit] =
  Stream.resource(Network[F].connect(SocketAddress(
    host"localhost",
    port"5555"
  ))).flatMap { socket =>
    Stream("Hello, world!")
      .interleave(Stream.constant("\n"))
      .through(text.utf8.encode)
      .through(socket.writes) ++
      socket.reads
        .through(text.utf8.decode)
        .through(text.lines)
        .head
        .foreach { response =>
          Console[F].println(s"Response: $response")
        }
  }
```

**What changed:**

- On the writing side, we added `.interleave(Stream.constant("\n"))` before UTF-8 encoding. 
  This results in `"\n"` being appended after each input string.
- On the reading side, we convert the output of `utf8.decode` via `text.lines`, 
  which emits strings between newline characters.
- Finally, we call `head` to take the first complete line of output. 
  Note that the rest of the `reads` stream is discarded after processing the first complete line. 
  This leads to proper socket closure and cleanup.

#### Handling Connection Errors

If a TCP connection cannot be established, `connect` fails with `fs2.io.net.ConnectException`. 
To automatically retry connections, we can catch `ConnectException` and attempt to connect again.

```dotty
import scala.concurrent.duration.*
import cats.effect.Temporal
import fs2.io.net.{ConnectException, Socket}

def retryingConnect[F[_]: {Temporal, Network}](address: SocketAddress[Host])
    : Stream[F, Socket[F]] =
  Stream.resource(Network[F].connect(address))
    .handleErrorWith {
      case _: ConnectException =>
        retryingConnect(address).delayBy(5.seconds)
      case other => Stream.raiseError(other)
    }

def client[F[_]: {Temporal, Console, Network}]: Stream[F, Unit] =
  retryingConnect(SocketAddress(host"localhost", port"5555")).flatMap {
    socket =>
      Stream("Hello, world!")
        .interleave(Stream.constant("\n"))
        .through(text.utf8.encode)
        .through(socket.writes) ++
        socket.reads
          .through(text.utf8.decode)
          .through(text.lines)
          .head
          .foreach { response =>
            Console[F].println(s"Response: $response")
          }
  }
```

**Explanation:**

- We extracted the `Network[F].connect` call into a separate method `retryingConnect`.
- The method attempts to create a client and handles `ConnectException`. 
  Upon catching the exception, we recursively call `retryingConnect` with a 5-second delay (via `delayBy`).
- To use `delayBy`, we needed to add the `Temporal` constraint to `F`.
- This same pattern can be used for more sophisticated retry strategies — 
  for example, exponential backoff and termination after a fixed number of attempts.
- Streams calling methods on `Socket` can also fail with exceptions due to loss of the underlying TCP connection. 
  Such exceptions can be handled similarly.

#### Servers

Now let's implement a server application interacting with the client we just created. 
The server will be a simple echo server — for each line of text received, it responds with the same line.

```dotty
import cats.effect.Concurrent

def echoServer[F[_]: {Concurrent, Network}]: F[Unit] =
  Stream.resource(Network[F].bind(SocketAddress.port(port"5555"))).map {
    serverSocket =>
      serverSocket.accept.map { clientSocket =>
        clientSocket.reads
          .through(text.utf8.decode)
          .through(text.lines)
          .interleave(Stream.constant("\n"))
          .through(text.utf8.encode)
          .through(clientSocket.writes)
          // handle client socket errors
          .handleErrorWith(_ => Stream.empty) 
      }
  }.parJoin(100).compile.drain
```

**Step-by-step explanation:**

1. We start by calling `Network[F].bind`, which returns a `Resource[F, ServerSocket[F]]`.
2. `ServerSocket` defines an `accept` method of type `Stream[F, Socket[F]]`. This is an infinite stream of client sockets — 
    each time a client connects to the server, a `Socket[F]` is produced, enabling interaction with that client.
3. The lifetime of a client socket is managed by the overarching stream: for example, a `flatMap` over the socket will keep it open 
    until the inner stream completes, after which the client socket is closed and all underlying resources are returned to the runtime.
4. We apply `map` to this infinite stream of clients and provide logic for handling an individual client. 
    In this case, we read from the client socket, decode received bytes to UTF-8, extract individual lines 
    and write each line back to the client. This logic is implemented as a single `Stream[F, Unit]`.
5. Since we applied `map` to the infinite client stream, we get a `Stream[F, Stream[F, Unit]]`. 
    We convert this to a single `Stream[F, Unit]` via `parJoin(100)`, which executes up to 100 inner streams concurrently. 
    As inner streams complete, new inner streams are pulled from the source. 
    Thus, `parJoin` controls the maximum number of simultaneous client requests processed by the server.
6. It is important to handle errors in client streams when merging all streams together.

**Typical pattern:**

```
Network[F].bind(address).map(ss => handleClient(ss.accept)).parJoin(maxConcurrentClients)
```

This is a very common pattern when working with server sockets.

A simpler echo server could be implemented with such basic logic:

```dotty
client.reads.through(client.writes)
```

However, this implementation would send bytes back to the client as they are received, not only after complete text lines are received.

**Example application:** [fs2-chat](https://github.com/typelevel/fs2-chat) is an example multi-user chat server 
and client, using TCP support in FS2 and [scodec](https://scodec.org) for binary data processing.

### UDP

UDP support works much like TCP. 
The `fs2.io.net.DatagramSocket` trait provides mechanisms for reading and writing UDP datagrams. 
UDP sockets are created via the `bindDatagramSocket` method on `fs2.io.net.Network`. 
Unlike TCP, there is no distinction between client and server sockets.

Adapting the TCP client example for UDP:

```dotty
import fs2.{Stream, text}
import fs2.io.net.Network
import cats.effect.Concurrent
import cats.effect.std.Console
import com.comcast.ip4s.*

def client[F[_]: {Concurrent, Console, Network}]: F[Unit] =
  val address = SocketAddress(ip"127.0.0.1", port"5555")
  Stream.resource(Network[F].bindDatagramSocket()).flatMap { socket =>
    Stream("Hello, world!")
      .through(text.utf8.encode)
      .chunks
      .evalTap(data => socket.write(data, address)) ++
      socket.reads
        .flatMap(datagram => Stream.chunk(datagram.bytes))
        .through(text.utf8.decode)
        .foreach { response =>
          Console[F].println(s"Response: $response")
        }
  }.compile.drain
```

**Differences from TCP:**

- We call `socket.write`, passing a byte chunk and a destination address.
- On reading, we convert `Stream[F, Datagram]` to `Stream[F, Byte]` via `flatMap(datagram => Stream.chunk(datagram.bytes))`. 
  Each datagram contains not only data but also the sender's address.
- Otherwise, the example is unchanged.

The server implementation for UDP is significantly simpler than for TCP:

```dotty
def echoServer[F[_]: {Concurrent, Network}]: F[Unit] =
  Stream.resource(
    Network[F].bindDatagramSocket(SocketAddress.port(port"5555"))
  ).flatMap { socket =>
    socket.reads.through(socket.writes)
  }.compile.drain
```

**How it works:**

- We open a socket bound to port `5555`.
- We then read datagrams from this socket and write each datagram back.
- Since each received datagram contains the remote sender address, we can use the same datagram for writing.

### TLS (Transport Layer Security)

The `fs2.io.net.tls` package provides TLS support over TCP and DTLS over UDP, 
built on `javax.net.ssl` on the JVM, [s2n-tls][s2n-tls] on Scala Native, and the [`node:tls` module] on Node.js.

TLS over TCP is provided by the `TLSSocket` trait, created via `Network.tlsContext`. 
`TLSContext` provides the cryptographic material used when establishing a TLS session — 
for example, a set of trusted certificates (sometimes called a trust store) 
and, optionally, a set of certificates identifying the application (sometimes called a key store).

The `TLSContext.Builder` trait provides many ways to create a `TLSContext`:

- `system` — uses the default system trust store (delegates to `javax.net.ssl.SSLContext.getDefault` on JVM).
- `fromKeyStoreFile(pathToFile, storePassword, keyPassword)` — loads a Java Key Store file (JVM only).
- `insecure` — trusts all certificates (extremely unsafe! Use only for quick tests).

`TLSContext` is typically created at application startup via `Network[F].tlsContext` 
and used for all sockets throughout the application's lifetime. 
After creating a `TLSContext`, the `client` and `server` methods are used to instantiate `TLSSocket` 
(and `dtlsClient` / `dtlsServer` methods for `DTLSSocket`). 
In each case, a regular socket must be provided, which `TLSSocket` will use for performing the TLS handshake 
as well as transmitting and receiving encrypted data. 
`TLSSocket` extends `fs2.io.net.Socket`, making adding TLS support a simple replacement in a program using `fs2-io`.

Adapting the TCP echo client for TLS:

```dotty
import fs2.{Chunk, Stream, text}
import fs2.io.net.Network
import fs2.io.net.tls.TLSContext
import cats.effect.MonadCancelThrow
import cats.effect.std.Console
import cats.syntax.all.*
import com.comcast.ip4s.*

def client[F[_]: {MonadCancelThrow, Console, Network}](
    tlsContext: TLSContext[F]
): Stream[F, Unit] =
  Stream.resource(Network[F].connect(SocketAddress(
    host"localhost",
    port"5555"
  ))).flatMap { underlyingSocket =>
    Stream.resource(tlsContext.client(underlyingSocket)).flatMap { socket =>
      Stream("Hello, world!")
        .interleave(Stream.constant("\n"))
        .through(text.utf8.encode)
        .through(socket.writes) ++
        socket.reads
          .through(text.utf8.decode)
          .through(text.lines)
          .head
          .foreach { response =>
            Console[F].println(s"Response: $response")
          }
    }
  }
```

**The only difference:** we wrap the underlying socket in a `TLSSocket`, ensuring all traffic is encrypted.

#### Configuring TLS session parameters

The various methods on `TLSContext` that create `TLSSocket` and `DTLSSocket` take a `TLSParameters` argument, 
allowing customization of session parameters. This allows configuring things like client authentication, 
supported protocols and cipher suites, and SNI extensions.

For example:

```dotty
import fs2.io.net.tls.{TLSParameters, TLSSocket}
import cats.effect.Resource
import javax.net.ssl.SNIHostName

def tlsClientWithSni[F[_]: Network](
    tlsContext: TLSContext[F],
    address: SocketAddress[Host]
): Resource[F, TLSSocket[F]] =
  Network[F].connect(address).flatMap { underlyingSocket =>
    tlsContext.clientBuilder(
      underlyingSocket
    ).withParameters(
      TLSParameters(
        protocols = Some(List("TLSv1.3")),
        serverNames = Some(List(new SNIHostName(address.host.toString)))
      )
    ).build
  }
```

In this example, we configured the TLS session to use only TLS 1.3 and added an SNI extension with the target server's host name. 
This is particularly important when working with virtual hosting, where a single IP address serves multiple domains.

#### Accessing TLS session information

`TLSSocket` extends `Socket` and provides several additional methods. 
Of particular interest is the `session` method, which returns `F[javax.net.ssl.SSLSession]`, 
containing information about the established TLS session. 
This allows us to query data such as the peer certificate or the negotiated cipher suite.

In the following example, we extract various session information to help debug TLS connections:

```dotty
def debug[F[_]: {MonadCancelThrow, Network}](
    tlsContext: TLSContext[F],
    address: SocketAddress[Host]
): F[String] =
  Network[F].connect(address).use { underlyingSocket =>
    tlsContext
      .clientBuilder(underlyingSocket)
      .withParameters(
        TLSParameters(serverNames =
          Some(List(new SNIHostName(address.host.toString)))
        )
      )
      .build
      .use { tlsSocket =>
        tlsSocket.write(Chunk.empty) >>
          tlsSocket.session.map { session =>
            s"Cipher suite: ${session.getCipherSuite}\r\n" +
              "Peer certificate chain:\r\n" +
              session.getPeerCertificates.zipWithIndex
                .map { case (cert, idx) => s"Certificate $idx: $cert" }
                .mkString("\r\n")
          }
      }
  }
```

This code:

1. Establishes a TLS connection to the specified address.
2. Sends an empty chunk (just to initiate the handshake).
3. Extracts the `SSLSession` object, which contains detailed information about the connection.
4. Returns a string with the cipher suite and the peer certificate chain.

## Files

The `fs2.io.file` package provides support for working with files. 
[An example from the beginning of the section](../fs2.md) demonstrates two of the simplest use cases — incremental reading from a file 
via `fs2.io.file.readAll` and incremental writing to a file via `fs2.io.file.writeAll`.

Another example — computing the SHA-256 digest of a file and writing it in hexadecimal to an adjacent file:

```dotty
import cats.effect.Concurrent
import cats.syntax.functor.*
import fs2.text
import fs2.Stream
import fs2.hashing.{Hashing, HashAlgorithm}
import fs2.io.file.{Files, Path}

def writeDigest[F[_]: {Files, Hashing, Concurrent}](path: Path): F[Path] =
  val target = Path(path.toString + ".sha256")
  Files[F].readAll(path)
    .through(Hashing[F].hash(HashAlgorithm.SHA256))
    .flatMap(h => Stream.chunk(h.bytes))
    .through(text.hex.encode)
    .through(text.utf8.encode)
    .through(Files[F].writeAll(target))
    .compile
    .drain
    .as(target)
```

**Explanation:**

1. `Files[F].readAll(path)` — streams the file, emitting bytes.
2. `Hashing[F].hash(HashAlgorithm.SHA256)` — computes the SHA-256 hash of the input stream. Returns `Stream[F, Hash]`.
3. `.flatMap(h => Stream.chunk(h.bytes))` — converts a `Hash` to a stream of bytes.
4. `text.hex.encode` — encodes bytes to hexadecimal text.
5. `text.utf8.encode` — encodes text to UTF-8 bytes.
6. `Files[F].writeAll(target)` — writes the result to the target file.

### Advanced file operations

For more complex use cases, several types are available:

- `FileHandle[F]` — represents an open file and provides various methods for interacting with the file: 
  reading data, writing data, querying size, etc. — all within the effect `F`. 
  Created by calling `FileHandle.fromPath(path, blocker)`.
- `ReadCursor` — combines a `FileHandle[F]` with an offset in the file in bytes. 
  Methods on `ReadCursor` perform read operations starting from the current offset, 
  and return an updated cursor along with the read data.
- `WriteCursor` — similarly, combines a `FileHandle[F]` with an offset. 
  Methods use the offset as the position for writing the next byte chunk, returning an updated cursor.

These types are useful when you need to perform random access file operations 
or require finer control over the read/write position.

### Filesystem operations

The `fs2.io.file` package object also provides many ways to interact with the filesystem: 
moving files, creating directories, traversing all paths in a directory tree, monitoring changes in a directory, etc.

For example, counting the total number of bytes in a directory tree can be done in a single line:

```dotty
def totalBytes[F[_]: {Files, Concurrent}](path: Path): F[Long] =
  Files[F].walk(path).evalMap(p => Files[F].size(p).handleError(_ => 0L)).compile.foldMonoid
```

**How it works:**

- `Files[F].walk(path)` — traverses all paths in the directory tree starting from `path`, emitting each path.
- `.evalMap(p => Files[F].size(p).handleError(_ => 0L))` — for each path, gets the file size. 
  If the file is unavailable (e.g., access error), returns 0.
- `.compile.foldMonoid` — sums all sizes using the monoid for `Long` (addition).

### More complex example: counting Scala source code lines

Combining `walk`, `readAll`, and various parsing operations, we can count the number of Scala source code lines:

```dotty
def scalaLineCount[F[_]: {Files, Concurrent}](path: Path): F[Long] =
  Files[F].walk(path).filter(_.extName == ".scala").flatMap { p =>
    Files[F].readAll(p).through(text.utf8.decode).through(text.lines).as(1L)
  }.compile.foldMonoid
```

**Explanation:**

1. `Files[F].walk(path)` — traverses the directory tree.
2. `.filter(_.extName == ".scala")` — keeps only files with the `.scala` extension.
3. For each file:
     - `Files[F].readAll(p)` — reads the file in a stream.
     - `.through(text.utf8.decode)` — decodes to strings.
     - `.through(text.lines)` — splits into lines.
     - `.as(1L)` — turns each line into the number 1.
4. `.compile.foldMonoid` — sums all the ones, obtaining the total number of lines.

### Filesystems and custom paths

The `Files` object is not tied to a specific filesystem. 
You can use a custom filesystem, for example, one mounted inside a JAR file, to access resources. 
If a `java.nio.file.FileSystem` has already been created, file interaction can be achieved by calling `Path.fromFsPath` to obtain a `Path` object.

## Processes

The `fs2.io.process` package provides an API for launching and interacting with native processes.

Often you are interested in the output of a process. Here is how to obtain the output of the `echo` command:

```dotty
import cats.effect.{Concurrent, MonadCancelThrow}
import fs2.io.process.{Processes, ProcessBuilder}
import fs2.text

def helloProcess[F[_]: {Concurrent, Processes}]: F[String] =
  ProcessBuilder("echo", "Hello, process!").spawn.use { process =>
    process.stdout.through(text.utf8.decode).compile.string
  }
```

**Explanation:**

- `ProcessBuilder("echo", "Hello, process!")` — creates a process description.
- `.spawn` — starts the process, returning `Resource[F, Process[F]]`.
- `process.stdout` — a byte stream from the process's standard output.
- `.through(text.utf8.decode)` — decodes bytes to strings.
- `.compile.string` — collects all strings into a single one.

### Feeding data to a process

You can also pass data to a process. Example with `wc --words` (word count):

```dotty
def wordCountProcess[F[_]: {Concurrent, Processes}](words: String)
    : F[Option[Int]] =
  ProcessBuilder("wc", "--words").spawn.use { process =>
    val in = Stream.emit(words).through(text.utf8.encode).through(process.stdin)
    val out = process.stdout.through(text.utf8.decode)
    out.concurrently(in).compile.string.map(_.strip.toIntOption)
  }
```

**Explanation:**

- `process.stdin` is a `Pipe[F, Byte, Nothing]` for writing data to the process's standard input.
- `out.concurrently(in)` — runs the write stream `in` concurrently with the read stream `out`. 
  The stream `out` will read output while `in` is writing data. 
  When `in` completes, `out` will continue reading until the process's stdout is closed.
- `.compile.string` — collects the output into a string.
- `.map(_.strip.toIntOption)` — strips whitespace and converts to `Int`.

### Waiting for process completion

Or simply wait for the process to complete and get the exit code:

```dotty
def sleepProcess[F[_]: {MonadCancelThrow, Processes}]: F[Int] =
  ProcessBuilder("sleep", "1s").spawn.use(_.exitValue)
```

`exitValue` returns `F[Int]` with the process exit code.

### Managing process lifetime

To terminate a running process, simply exit the `use` block:

```dotty
import cats.effect.Temporal
import scala.concurrent.duration.*

def oneSecondServer[F[_]: {Temporal, Processes}]: F[Unit] =
  ProcessBuilder("python", "-m", "http.server").spawn.surround {
    Temporal[F].sleep(1.second)
  }
```

**Explanation:**

- `.spawn` starts the process and returns `Resource[F, Process[F]]`.
- `.surround` ensures the resource (the process) is released after the passed effect completes.
- `Temporal[F].sleep(1.second)` — waits 1 second, after which the resource is released and the process terminates (SIGTERM is sent).

## Console Operations

Console writing often comes down to `s.evalMap(o => IO.println(o))`. 
This works well for quick tests but can be problematic in large applications. 
Calling `println` blocks until data is written to the standard output stream. 
This can cause fairness issues with other non-blocking operations 
running in the main thread pool.

For such situations, `fs2-io` provides several utilities:

```dotty
def stdoutLines[F[_]: Sync, O: Show](
    charset: Charset = utf8Charset
): Pipe[F, O, Nothing]

def stdout[F[_]: Sync]: Pipe[F, Byte, Nothing]
```

Both of these `Pipe`s are provided in the `fs2.io` package object. 
The `stdoutLines` method uses a `Show[O]` instance to convert stream elements to strings. 
Unlike `println`, these operations write to the console using a dedicated thread pool 
for blocking operations, preventing issues with the main pool.

The `fs2.io` package object also provides several utilities for reading values from the console:

```dotty
def stdin[F[_]: Sync](bufSize: Int): Stream[F, Byte]

def stdinUtf8[F[_]: Sync](bufSize: Int): Stream[F, String]
```

Like their output counterparts, these operations perform blocking reads on the blocking thread pool.

## Java Stream Interop

The `fs2.io` package object provides interoperability with `java.io.InputStream` and `java.io.OutputStream`.

- `fs2.io.readInputStream` (and `unsafeReadInputStream` — see ScalaDoc for differences) creates a `Stream[F, Byte]` from an `InputStream`.
- `fs2.io.writeOutputStream` provides a `Pipe` that writes bytes emitted by `Stream[F, Byte]` to an `OutputStream`.
- `fs2.io.readOutputStream` creates a `Stream[F, Byte]` from a function that writes data to an `OutputStream`.

**Usage examples:**

```dotty
import cats.effect.IO
import fs2.*
import fs2.io.{readInputStream, readOutputStream, writeOutputStream}
import java.io.{InputStream, OutputStream}

// Reading from InputStream
val inputStream: InputStream      = ???
val bytesStream: Stream[IO, Byte] = readInputStream[IO](IO(inputStream), 8192)

// Writing to OutputStream
val outputStream: OutputStream      = ???
val writePipe: Pipe[IO, Byte, Unit] = writeOutputStream[IO](IO(outputStream))

// Creating a stream from a function that writes to OutputStream
val generatedStream: Stream[IO, Byte] =
  readOutputStream[IO](8192) { out =>
    IO {
      out.write("Hello".getBytes)
      out.flush()
    }
  }
```

These functions are particularly useful when integrating with existing Java code or libraries 
working with traditional I/O streams.

---

**References:**

- [Fs2 home page][home]

[home]: https://fs2.io/#/
[s2n-tls]: https://github.com/aws/s2n-tls
[`node:tls` module]: https://nodejs.org/api/tls.html
