# Blocking Tasks

The simplest and most obvious source of CPU resource starvation is **executing blocking tasks in the compute thread pool**.
The reason is straightforward: the compute pool has a fixed size (typically equal to the number of CPU cores).
If even one of its threads is blocked (e.g., on an I/O operation), its computational power is irrecoverably lost.
If _all_ threads block, your application comes to a complete halt.

In real applications, the problem hides deep within third-party libraries,
especially those written for Java in the era when the blocking I/O model dominated.

- **Typical suspects:**
    - JDBC calls (databases).
    - File read/write (`FileInputStream`, `FileOutputStream`, `RandomAccessFile`, `nio.FileChannel` in synchronous mode).
    - Network calls (older `HttpURLConnection`, `Socket`).
    - DNS operations (`java.net.URL`, `InetAddress.getByName`).
    - Any `Thread.sleep()`, `Object.wait()`, or `synchronized` blocks.
    - Many `java.nio.*` operations in synchronous (non-`Selector`) mode.

**As soon as the problematic code is found, the fix is simple:** replace `IO(...)` with `IO.blocking(...)` or `IO.interruptible(...)`.
`IO.blocking` redirects the task's execution to a special, unlimited blocking thread pool,
without affecting the compute pool.
`IO.interruptible` does the same, but also allows Cats Effect to interrupt (via `Thread.interrupt()`)
a blocked operation upon fiber cancellation.

## Blocked Thread Detection

If your application is an `IOApp`, Cats Effect provides a **built-in,
but disabled by default tool for detecting blocked threads**.
This is the simplest way to diagnose such problems.

**How to enable it:**

```dotty
import cats.effect.IOApp

object Example extends IOApp.Simple:
  // Enable blocked thread detection
  override protected def blockedThreadDetectionEnabled = true

  val run: IO[Unit] = ??? // Your program
```

**How it works:**

1. Thanks to the work-stealing pool, several `io-compute-*` threads work together.
2. The detector creates a background watcher thread.
3. This watcher periodically (every few seconds) **randomly** selects one of the live `io-compute-*` threads.
4. It checks that thread's state. If the thread is in `BLOCKED` or `WAITING` state
   (indicating it is blocked at the JVM/OS level),
   the detector **prints its stack trace** to standard error (`stderr`).

**Analyzing the results:**

Suppose you see something like this:

```
[WARNING] A compute thread has been blocked for at least N ms (possibly due to a blocking call).

"io-compute-5" #21 prio=5 os_prio=0 cpu=2.31ms elapsed=41.71s tid=0x00007fa7a4d6c000 nid=0xc6e runnable
  java.lang.Thread.State: RUNNABLE
  at java.io.FileInputStream.readBytes(Native Method)
  at java.io.FileInputStream.read(FileInputStream.java:...)
  at myapp.LegacyFileReader.readNextBlock(LegacyFileReader.scala:42)
  at myapp.DataProcessor.process(DataProcessor.scala:...)
  ...
```

The stack trace directly points to code that should be wrapped in `IO.blocking`.
In this case, it's the `readBytes` call inside `LegacyFileReader`.
You go to your application, find where `LegacyFileReader.readNextBlock` is called,
and wrap it in `IO.blocking`.

```dotty
// Before (bad)
def processFile: IO[Unit] = IO {
  legacyReader.readNextBlock()
}

// After (good)
def processFile: IO[Unit] = IO.blocking {
  legacyReader.readNextBlock()
}
```

**Important notes about the detector:**

- **It is random, not deterministic.** The thread selection is random.
  The detector does not guarantee detecting _all_ blocking calls, but it will likely find frequently occurring ones.
- **It is expensive.** The thread inspection procedure is non-trivial and consumes resources. This is why it is **disabled by default**.
- **Use it in test/staging environments, not in production.**
  Enable it locally or on staging for debugging, but disable it on production servers.

## Alternative Method: Thread Dumps

If you are not using `IOApp` or for some reason cannot enable the detector, **thread dumps** are a powerful manual diagnostic method.

**How to take a dump:**

- **In a terminal (Unix/Linux/Mac):** Press `Ctrl + \` (backslash) in the console where the JVM is running.
- **Via OS signal:** Use the `kill -3 <pid>` command (where `<pid>` is your application's process ID).
- **On Windows:** Press `Ctrl + Break`.

In any case, the JVM will print a full dump of all threads and their stack traces to standard error (`stderr`).

**Analyzing the results:**

Take **several dumps** with a few seconds between them. Then look for threads named `io-compute-*`.
If in any of the dumps **at least one** such thread is in `BLOCKED` or `WAITING` state, that is a red flag.
Examine its stack trace to find the problematic code.

_Output example (excerpt):_

```
"io-compute-2" #21 daemon prio=5 os_prio=0 cpu=3.21ms elapsed=10.31s tid=0x00007f9a3c011000 nid=0x3a4e waiting for monitor entry [0x00007f9a38a7e000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:...)
	- waiting to lock <0x00000000deadbeef> (a java.lang.Object)
	at com.example.LegacyLibrary.initialize(LegacyLibrary.java:42)
```

Here we can see that the thread is blocked on a synchronized method inside `LegacyLibrary.initialize`.
This is a classic blocking call that should be wrapped in `IO.blocking`.

**Limitations of thread dumps:**

- Unlike the built-in detector, the stack trace **will not be augmented** with Cats Effect fiber information.
  You will only see code that was wrapped in `IO.delay` or `IO(...)`. However, this is enough to find the source of the problem.
- This is a manual and non-automated process.

## File I/O and DNS: Special Warnings

It is important to understand which operations are blocking by default on the target platform.

- **File I/O:**
    - **macOS and Linux:** **All** file system operations (read, write, open, close)
      are blocking at the kernel level. Even `FileChannel` in synchronous mode will block.
    - **Windows:** Most local file system (NTFS) operations are **non-blocking**,
      but operations on network drives (SMB) are blocking. For portability, treat any file I/O as blocking.
- **Network and DNS:**
    - Socket operations (`Socket`) are blocking.
    - **`java.net.URL` and `InetAddress`:** When calling `equals`, `hashCode`, or even the `new URL(...)` constructor,
      a **blocking DNS query may be executed** to resolve the host name. This is an extremely insidious source of blocking!

This is why the starvation warning specifically mentions "file system operations or network calls."
These are the most common and hardest-to-catch causes of problems.

**Recommendation:** If your code interacts with the file system, DNS, JDBC, or any other potentially blocking API,
default to using `IO.blocking`.
It is better to lose a bit of performance due to context switching than to completely halt the application.

## Misusing `IO.blocking` and Other Kinds of Starvation

While wrapping blocking calls in `IO.blocking` solves the compute pool blocking problem, it is not a panacea.

1. **Pressure on the blocking pool:** `IO.blocking` moves the task to a separate, cached thread pool,
   which can dynamically expand. If an excessive number of blocking tasks are created,
   this pool may consume significant memory (each thread ~1 MB of stack) and place a heavy load on the OS scheduler.
   This, in turn, can also lead to starvation, just of a different kind.
2. **Misuse of `interruptible`:** `IO.interruptible` can cause `Thread.interrupt()`
   in a blocked thread if the fiber was cancelled. However, not all blocking methods handle interrupts correctly.
   Some may ignore them or even corrupt state. Use `interruptible` with caution and after thorough testing.
3. **CPU-intensive tasks:** Even non-blocking but very long CPU tasks can monopo
ize a thread and cause starvation.
   `blocking` won't help here — you need to use `IO.cede` for cooperative yielding.

**Summary:** Start by enabling `blockedThreadDetectionEnabled` in your `IOApp` (or by manual thread dumps).
Upon finding blocking code, wrap it in `IO.blocking`. If the problem persists,
proceed to [analyzing CPU-intensive tasks](intensive-tasks.md) and the overall concurrency architecture.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Starvation and Tuning](https://typelevel.org/cats-effect/docs/core/starvation-and-tuning)

[doc]: https://typelevel.org/cats-effect/docs/concepts
