# Scodec

The `fs2-scodec` library provides streaming encoding and decoding of binary data, 
based on the [scodec](https://github.com/scodec/scodec) library. 
Initially, this library was called [scodec-stream](https://github.com/scodec/scodec-stream) 
and evolved independently for a long time before being integrated into FS2.

**What is Scodec?**

Scodec is a library for working with binary data in Scala, 
allowing you to describe binary protocols and data formats in a composable, type-safe, and purely functional style. 
Instead of manually managing offsets in byte buffers, 
you describe the data structure using encoding/decoding combinators, 
and the library handles all the low-level work.

**Why use fs2-scodec?**

- **Streaming** — you can decode data as it arrives, 
  without loading the entire file or network message into memory at once. 
  This is critical for working with large data volumes or infinite streams.
- **Resource safety** — integration with FS2 ensures proper resource management (closing files, sockets, etc.).
- **Composability** — you can build complex protocols from simple, well-tested building blocks.
- **Performance** — streaming with chunks 
  ensures high performance with low memory consumption.

## Basic decoding example

Consider a program that reads a file and decodes a stream of frames from it. Each frame has the following format:

1. The first 4 bytes are a 32-bit signed integer indicating the length of the frame payload.
2. This is followed by the specified number of bytes — the actual frame payload.

```dotty
import cats.effect.{IO, IOApp}
import scodec.bits.*
import scodec.codecs.*
import fs2.Stream
import fs2.interop.scodec.*
import fs2.io.file.{Files, Path}

object Decode extends IOApp.Simple:

  def run: IO[Unit] =
    // Create a decoder that reads a stream of frames
    val frames: StreamDecoder[ByteVector] =
      StreamDecoder.many(int32).flatMap { numBytes =>
        // For each frame: read length (int32), then read numBytes of payload
        StreamDecoder.once(bytes(numBytes))
      }

    // File path
    val filePath = Path("largefile.bin")

    // Read the file in a stream and decode it with our decoder
    val s: Stream[IO, ByteVector] =
      Files[IO].readAll(filePath).through(frames.toPipeByte)

    // Count the number of successfully decoded frames
    s.compile.count.flatMap(cnt => IO.println(s"Read $cnt frames."))
```

When executed, this program will incrementally read data from the "largefile.bin" file 
and decode a stream of frames according to the described format.

## Detailed example breakdown

### Creating a decoder

```dotty
val frames: StreamDecoder[ByteVector] =
  StreamDecoder.many(int32).flatMap { numBytes =>
    StreamDecoder.once(bytes(numBytes))
  }
```

Here, decomposition into two parts is used:

1. **`StreamDecoder.many(int32)`** — creates a decoder that repeatedly applies the `int32` codec to the input stream, 
    extracting 32-bit integers one after another. Each extracted value represents the length of the next frame.
2. **`.flatMap { numBytes => StreamDecoder.once(bytes(numBytes)) }`** — for each extracted `numBytes` value, 
    a new decoder is created that applies the `bytes(numBytes)` codec exactly once. 
    This codec reads exactly `numBytes` bytes from the input stream and returns them as a `ByteVector`. 
    The process then repeats: we read `int32` again (using `StreamDecoder.many`), then bytes again, 
    and so on until the input stream is exhausted.

> `flatMap` at the `StreamDecoder` level works such that each time the decoder extracts a value, 
> it can use that value to dynamically choose the next decoder. 
> This allows describing protocols with variable field lengths, where the size of the next field depends on the value of the previous one.

An alternative way to write the same decoder using the `decodeMany` method:

```dotty
val frames: StreamDecoder[ByteVector] =
  StreamDecoder.decodeMany(int32.flatMap { numBytes =>
    bytes(numBytes)
  })
```

### Reading a file and applying the decoder

```dotty
val s: Stream[IO, ByteVector] =
  Files[IO].readAll(filePath).through(frames.toPipeByte)
```

- **`Files[IO].readAll(filePath)`** — creates a `Stream[IO, Byte]` stream that reads the file incrementally, 
  using a fixed-size buffer. This ensures that even very large files are not loaded into memory at once.
- **`.through(frames.toPipeByte)`** — applies the decoder to the byte stream. 
  `frames.toPipeByte` converts a `StreamDecoder[ByteVector]` into a `Pipe[F, Byte, ByteVector]` — a function that 
  takes a byte stream and emits a stream of decoded `ByteVector`s. 
  The result: a stream of frames, where each frame is a sequence of bytes read according to the protocol description.

### Counting results

```dotty
s.compile.count.flatMap(cnt => IO.println(s"Read $cnt frames."))
```

- **`s.compile.count`** — compiles the stream and counts the number of elements 
  (in our case, the number of successfully decoded frames). This executes within the `IO` effect.
- **`flatMap(cnt => IO.println(...))`** — after counting, prints the result to the console.

## Core types: StreamDecoder and StreamEncoder

The `fs2-scodec` library provides two core types:

### `StreamDecoder[A]`

`StreamDecoder[A]` represents a decoder that transforms a byte stream into a stream of values of type `A`. 
It is a purely functional description of how to extract structured data from a byte stream.

**Constructors and combinators of StreamDecoder:**

- `StreamDecoder.once(decoder: Decoder[A])` — creates a decoder that applies the passed `Decoder` 
  exactly once to the beginning of the stream. After decoding one value, the decoder terminates 
  (even if there are remaining data in the stream, they will not be read).
- `StreamDecoder.many(decoder: Decoder[A])` — creates a decoder that repeatedly applies the passed `Decoder` to the stream, 
  extracting a sequence of values of type `A`. Decoding continues until 
  the input data is exhausted or an error occurs.
- `StreamDecoder.decodeMany[A](decoder: Decoder[A])` — alternative syntax for `many`.
- `flatMap[B](f: A => StreamDecoder[B])` — allows dynamically choosing the next decoder 
  based on the just-decoded value. This is a powerful mechanism for describing protocols with variable structure.
- `map[B](f: A => B)` — transforms the decoder's output values.
- `filter(p: A => Boolean)` — filters out values that do not satisfy the predicate.
- `evalMap[F[_], B](f: A => F[B])` — allows performing effects on each decoded value 
  (e.g., logging, validation with external system calls).
- `handleErrorWith(f: Throwable => StreamDecoder[A])` — handles decoding errors, 
  allowing recovery or skipping of corrupted data.
- `repeat` — repeats the decoder infinitely (or until error/end of data).
- `stop` — forcefully terminates decoding.
- `toPipeByte` — converts a `StreamDecoder[A]` into a `Pipe[F, Byte, A]` for use in FS2 pipelines.

### `StreamEncoder[A]`

`StreamEncoder[A]` represents an encoder that transforms a stream of values of type `A` into a byte stream. 
It is the mirror image of `StreamDecoder`.

**Main constructors:**

- `StreamEncoder.once(encoder: Encoder[A])` — encodes a single value.
- `StreamEncoder.many(encoder: Encoder[A])` — encodes a sequence of values.
- `StreamEncoder.encodeMany[A](encoder: Encoder[A])` — alternative syntax.
- `contramap[B](f: B => A)` — transforms input values before encoding.
- `andThen` — composition of encoders (sequential encoding).

## Error handling and incomplete data

Scodec distinguishes two types of errors:

1. **Decoding error** (`Err`) — occurs when the data structure does not match expectations (e.g., invalid format, buffer overread).
2. **Insufficient data** — occurs when there are not enough bytes in the buffer to complete decoding.

Both of these situations are handled transparently in `StreamDecoder`:

- When data is insufficient, the decoder pauses and waits for new bytes to arrive.
- On a decoding error, the decoder terminates with an error (unless `handleErrorWith` is used).

---

**References:**

- [Fs2 home page][home]

[home]: https://fs2.io/#/
