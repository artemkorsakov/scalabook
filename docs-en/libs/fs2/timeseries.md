# Time Series

The `fs2.timeseries` package provides various utilities for working with timestamped values. 
This is particularly useful in areas such as network traffic monitoring, telemetry, timestamped logging, 
performance analysis, and any other scenarios where tracking value changes over time is important.

## TimeStamped Type

The `TimeStamped` type associates a timestamp, represented as a Unix epoch (time elapsed since the epoch start), with a value:

```dotty
case class TimeStamped[+A](time: FiniteDuration, value: A)
```

Where:

- `time: FiniteDuration` — the timestamp, usually measured in nanoseconds or milliseconds from the Unix epoch.
- `value: A` — the value itself, which can be of any type.

This simple but powerful data type serves as the foundation for all time series operations in FS2.

## Computing Bitrate

There are many interesting operations you can perform on streams of timestamped values. 
For example, we can compute the bitrate (bits per second) for binary streams:

```dotty
import fs2.Stream
import fs2.timeseries.TimeStamped
import scodec.bits.ByteVector

def withBitrate[F[_]](input: Stream[F, TimeStamped[ByteVector]]): Stream[F, TimeStamped[Either[Long, ByteVector]]] =
  TimeStamped.withPerSecondRate[ByteVector, Long](_.size * 8).toPipe(input)
```

**Detailed explanation:**

The `TimeStamped.withPerSecondRate` function allows us to aggregate a certain "feature" of a sequence of values, 
collected over each one-second interval. 
In this case, our feature is the number of bits (data size in bytes multiplied by 8), giving us the bitrate.

- The parameter `_.size * 8` is a function that extracts the feature from each `ByteVector` element. 
  Here we take the size in bytes and multiply by 8 to get the bit count.
- `withPerSecondRate` groups incoming values by timestamps corresponding to each second, 
  sums the feature for each group, and emits the result.
- The computed bitrates are emitted into the output stream.
- The result type is `TimeStamped[Either[Long, ByteVector]]`:
    - `Left(bitrate)` — the computed bitrate (bits per second) for the current interval.
    - `Right(ByteVector)` — the original data, passed through the stream unchanged (allowing to keep the original stream for further processing).

## Adding timestamps to a byte stream

Our `withBitrate` combinator requires an argument of type `Stream[F, TimeStamped[ByteVector]]`, 
whereas when working with binary data (e.g., reading from network sockets), we usually have `Stream[F, Byte]`. 
We need to convert a byte stream into a timestamped byte stream:

```dotty
def withReceivedBitrate[F[_]](input: Stream[F, Byte]): Stream[F, TimeStamped[Either[Long, ByteVector]]] =
  input.chunks.map(c => TimeStamped.unsafeMonotonic(c.toByteVector)).through(withBitrate)
```

**Explanation:**

1. `input.chunks` — splits the byte stream into chunks. Chunks are [FS2's internal data storage units](guide.md), 
    and working with them is more efficient than with individual bytes.
2. `.map(c => TimeStamped.unsafeMonotonic(c.toByteVector))` — for each chunk:
     - Converts it to a `ByteVector` (an efficient data structure for working with bytes from the scodec library).
     - Adds a timestamp using `TimeStamped.unsafeMonotonic`. 
       This function uses a monotonic clock to obtain the current time. 
       The name includes `unsafe` because it performs a side effect (reading the system time) outside the effect context `F`. 
       This is acceptable since reading time is a relatively safe operation, 
       but in strictly functional code, effect-based alternatives are preferred.
3. `.through(withBitrate)` — applies the previously defined bitrate computation combinator.

## Computing a moving average

Each emitted sample is the sum of bits received during each one-second pair. 
Now let's compute the average bitrate over the last 10 seconds. 
This can be done with `mapAccumulate` together with `scala.collection.immutable.Queue` 
(a queue is a data structure supporting O(1) enqueue and dequeue operations):

```dotty
import scala.collection.immutable.Queue

def withAverageBitrate[F[_]](input: Stream[F, Byte])
    : Stream[F, TimeStamped[Either[Long, ByteVector]]] =
  withReceivedBitrate(input).mapAccumulate(Queue.empty[Long]) {
    case (q, tsv @ TimeStamped(_, Right(_))) => (q, tsv)
    case (q, TimeStamped(t, Left(sample)))   =>
      val q2      = (sample +: q).take(10)
      val average = q2.sum / q2.size
      (q, TimeStamped(t, Left(average)))
  }.map(_._2)
```

**Step-by-step explanation:**

1. `withReceivedBitrate(input)` — first, we compute the bitrate for each one-second interval.
2. `.mapAccumulate(Queue.empty[Long])` — performs accumulation with state. Initial state is an empty queue.
3. Processing each element:
     - If the element is original data (`Right(_)`), we simply pass it through unchanged, keeping the queue unchanged. 
       This is important because we do not want data to affect the average computation — the average is calculated only from bitrates.
     - If the element is a computed bitrate (`Left(sample)`):
         - `val q2 = (sample +: q).take(10)` — adds the new sample to the front of the queue and takes only the last 10 elements. 
           If the queue had 10 elements, the oldest (last in the queue) will be dropped.
         - `val average = q2.sum / q2.size` — computes the arithmetic mean over the 10 most recent values.
         - Returns the updated queue and a new element with the computed average bitrate.
4. `.map(_._2)` — extracts only the second part of the tuple (updated elements), discarding the queue state since we no longer need it.

## Storing results in a Ref

Then we can store the computed bitrates in a `Ref` for later inspection, logging, etc. 
A `Ref` is a [thread-safe reference](../../cats-effect/typeclasses/concurrent/ref.md) from the cats-effect library 
that allows atomically reading and mutating a value:

```dotty
import cats.effect.Ref
import fs2.Chunk

def measureAverageBitrate[F[_]](
    store: Ref[F, Long],
    input: Stream[F, Byte]
): Stream[F, Byte] =
  withAverageBitrate(input).flatMap {
    case TimeStamped(_, Left(bitrate)) => Stream.exec(store.set(bitrate))
    case TimeStamped(_, Right(bytes))  => Stream.chunk(Chunk.byteVector(bytes))
  }
```

**Explanation:**

1. `withAverageBitrate(input)` — applies the average bitrate computation.
2. `.flatMap` — for each stream element:
     - If it is the average bitrate (`Left(bitrate)`): performs the store effect in `Ref` via `Stream.exec(store.set(bitrate))`. 
       `Stream.exec` creates a stream that performs an effect and emits nothing.
     - If it is original data (`Right(bytes)`): passes it through via `Stream.chunk(Chunk.byteVector(bytes))`. 
       This allows keeping the original byte stream for further processing (e.g., passing to another system component).

## The problem: frozen state when data stops

All well and good, but what happens if we stop receiving data for a while? 
In this case, no data arrives at `TimeStamped.withPerSecondRate`, and therefore no output either. 
This means our `Ref` will not be updated — it will remain frozen with the last value! 
We need `Ref` to accurately reflect data loss, meaning we need a way to allow `withPerSecondRate` 
to emit zero values as time passes, even when no data is arriving.

**Problem example:**

- In second 1, we received 8000 bits. Bitrate = 8000 bit/s, stored in `Ref`.
- In second 2, we received 6000 bits. Bitrate = 6000 bit/s, stored in `Ref`.
- In seconds 3-10, we received no bytes. No output from `withPerSecondRate`, `Ref` remains at 6000, although the actual bitrate is 0.
- This misleads monitoring systems and can lead to incorrect decisions.

## Solution: Time Series

To solve this problem, we can change the bitrate computation to work with values of type `TimeStamped[Option[ByteVector]]`, 
where `TimeStamped(None)` represents a clock tick — a signal that one time interval has passed, 
even if no data was received.

A stream of values of type `Option`, where `None` is used as a time marker, is called a **time series**. 
The `fs2.timeseries.TimeSeries` object defines various ways to build such streams.

### Step 1: Adapting withBitrate for Option

First, we adapt `withBitrate` to work with `TimeStamped[Option[ByteVector]]` values:

```dotty
import fs2.Stream
import fs2.timeseries.{TimeStamped, TimeSeries}
import scodec.bits.ByteVector

def withBitrate[F[_]](input: Stream[F, TimeStamped[Option[ByteVector]]])
    : Stream[F, TimeStamped[Either[Long, Option[ByteVector]]]] =
  TimeStamped.withPerSecondRate[
    Option[ByteVector],
    Long
  ](_.map(_.size).getOrElse(0L) * 8).toPipe(input)
```

**Changes:**

- Input type is now `TimeStamped[Option[ByteVector]]`.
- The feature extraction function now handles `Option`:
     - If data exists (`Some(bytes)`), we take its size and multiply by 8.
     - If no data (`None`), the feature is 0.
- Output type is now `TimeStamped[Either[Long, Option[ByteVector]]]`:
     - `Left(bitrate)` — bitrate (can be 0 if there was no data).
     - `Right(Some(bytes))` — data.
     - `Right(None)` — a time tick when there was no data.

### Step 2: Converting to a time series

Now we need to adapt `withReceivedBitrate` to convert our original stream into a time series:

```dotty
import scala.concurrent.duration.*
import cats.effect.Temporal

def withReceivedBitrate[F[_]: Temporal](input: Stream[F, Byte])
    : Stream[F, TimeStamped[Either[Long, Option[ByteVector]]]] =
  TimeSeries.timePulled(
    input.chunks.map(_.toByteVector),
    1.second,
    1.second
  ).through(withBitrate)
```

**What is happening here:**

- We used the `timePulled` operation to build a time series from a regular stream.
- As the name suggests, `timePulled` adds a timestamp to each received value, 
  using the wall clock time at the moment the value was pulled from the original stream.
- `timePulled` parameters:
     - The first `1.second` — tick period. Every second, even if no data is received, a `TimeStamped(None)` is generated. 
       This ensures that `withPerSecondRate` receives input even during periods without traffic.
     - The second `1.second` — sampling period (or aggregation "bucket"). 
       Data received during each second is grouped together.
- These two parameters can differ. For example, we might receive ticks every 100ms but aggregate data per second. 
  This provides finer timestamp precision.
- The result is passed to `withBitrate`, which now correctly handles both data and empty intervals.

### Step 3: Remaining parts of the computation

The remaining parts of the bitrate computation are almost independent of this time series conversion 
(except for small changes in type signatures). 
Now `Ref` will be updated even during periods without data — during these periods, the bitrate will be 0, 
which matches reality.

## Scans

Several stream transformations were written above, 
which gradually built a (pipe) pipeline for monitoring the bitrate passing through a stream. 
Each of these transformations, up to the final storage of the computed bitrate in a `Ref`, 
was a **pure transformation** — no effects were computed and no resources were opened. 
This type of processing is very common when working with time series.

The `fs2.Scan` type allows us to express such stateful pure computations in a way 
that provides significantly greater composability than `Pipe`. Let's rewrite our example above using `Scan`:

```dotty
import fs2.{Stream, Chunk, Scan}
import fs2.timeseries.{TimeStamped, TimeSeries}
import cats.effect.{Ref, Temporal}
import scodec.bits.ByteVector
import scala.collection.immutable.Queue
import scala.concurrent.duration.*

// Define a Scan for computing bitrate
def bitrate =
  TimeStamped.withPerSecondRate[
    Option[ByteVector],
    Long
  ](_.map(_.size).getOrElse(0L) * 8)

// Define a Scan for computing the moving average bitrate
def averageBitrate =
  bitrate.andThen(Scan.stateful1(Queue.empty[Long]) {
    case (q, tsv @ TimeStamped(_, Right(_))) => (q, tsv)
    case (q, TimeStamped(t, Left(sample)))   =>
      val q2      = (sample +: q).take(10)
      val average = q2.sum / q2.size
      (q, TimeStamped(t, Left(average)))
  })

// Function to measure average bitrate using Scan
def measureAverageBitrate[F[_]: Temporal](
    store: Ref[F, Long],
    input: Stream[F, Byte]
): Stream[F, Byte] =
  TimeSeries.timePulled(input.chunks.map(_.toByteVector), 1.second, 1.second)
    .through(averageBitrate.toPipe)
    .flatMap {
      case TimeStamped(_, Left(bitrate))      => Stream.exec(store.set(bitrate))
      case TimeStamped(_, Right(Some(bytes))) =>
        Stream.chunk(Chunk.byteVector(bytes))
      case TimeStamped(_, Right(None)) => Stream.empty
    }
```

**What changed and why it is better:**

1. **Composition via `andThen`**:
     - In this example, both `bitrate` and `averageBitrate` are `Scan` instances.
     - The `averageBitrate` function directly uses `bitrate` via `andThen` to build a "larger" scan. 
       This is like function composition: we say "first apply bitrate, then apply moving average."
     - This composition is much more readable and easier to understand than nested `through` calls.

2. **Separation of logic from execution**:
     - `Scan` is a pure description of a transformation. It has no effects and is independent of the specific effect type `F`.
     - Once we finish composing scans, we convert the scan to a `Pipe` via `.toPipe` 
       and apply it to the input stream. This separation allows testing scan logic independently of effects.

3. **Reusability**:
     - Scans can be easily combined, reused, and tested individually.
     - For example, we could define other scans (e.g., for computing maximum bitrate, median, etc.) 
       and combine them together.

4. **Handling `None`**:
     - In the final processing, we explicitly handle the `Right(None)` case (empty tick), skipping it. 
       This allows avoiding writing empty values to the output stream.

## TimeStamped and TimeSeries combinator overview

The `fs2.timeseries` package contains many combinators on `TimeStamped` and `TimeSeries` 
that provide various scans. Here are some of them:

### TimeStamped combinators:

- `withPerSecondRate` — computes the rate of change of a feature per second. Used in the examples above for bitrate.
- `withRate` — a more general version that allows specifying an arbitrary interval.
- `withCountPerSecond` — counts the number of elements per second.
- `map`, `filter`, `flatMap` — standard operations on timestamped values.

### TimeSeries combinators:

- `timePulled` — creates a time series from a regular stream, adding timestamps at pull time.
- `timeArrival` — creates a time series using the arrival time of elements (may differ from pull time in asynchronous systems).
- `tick` — generates periodic ticks (`None` values) at a specified interval.
- `sample` — samples a stream at a given frequency.
- `filter` / `collect` — filters values in a time series.
- `map` — transforms values while preserving timestamps.

### Scan combinators:

- `stateful1` — creates a scan with a single state (as in the queue example).
- `stateful` — creates a scan with arbitrary state and the ability to return multiple output values.
- `andThen` — composition of scans (sequential application).
- `zipWith` — combines two scans in parallel.
- `toPipe` — converts a scan to a `Pipe` for use in a stream.

## Advantages of the scan approach

Using `Scan` instead of direct stream manipulation provides several important advantages:

1. **Purity** — scans are pure functions with no effects. This simplifies reasoning about the program and its testing.
2. **Composability** — scans are easily combined via `andThen`, `zipWith`, and other combinators.
3. **Reusability** — the same scan can be used in different parts of the program with different input streams.
4. **Separation of logic from infrastructure** — data processing logic is separated from code 
    responsible for executing effects (e.g., reading from a socket, writing to a `Ref`).
5. **Performance** — scans operate at the chunk level, 
    minimizing overhead per individual element processed.

---

**References:**

- [Fs2 home page][home]

[home]: https://fs2.io/#/
