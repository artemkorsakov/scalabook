# IORuntime Configuration

`IORuntime` is the heart of Cats Effect, containing all the necessary thread pools,
schedulers, and configurations for executing `IO` programs.
The ability to configure it externally allows adapting Cats Effect behavior
to the specific requirements of your application and runtime environment, whether JVM, Node.js, or browser.
You can configure `IORuntime` through both **JVM arguments** (on the JVM platform)
and **environment variables** (for JavaScript platforms, e.g., Node.js or the browser).
In the browser, you can also define global variables.

## JVM

On the JVM platform, configuration parameters are passed to the application as Java **system properties**, using the command-line argument `-D`:

```bash
java -Dcats.effect.propName=propValue -jar my-app.jar
```

## JavaScript (Node.js and Browser)

In the JavaScript environment (Node.js), configuration uses **environment variables**:

```bash
export CATS_EFFECT_PROPNAME=propValue
node my-app.js
```

In a **browser** (and, typically, in any other JavaScript runtime), there is no direct access to environment variables.
Instead, you can define a global variable in your bundle using build tools like Webpack.
For example, via `EnvironmentPlugin` or by defining a variable in the global `process.env` object (if your environment supports it):

```javascript
// Node.js or environment with process.env support
process.env.CATS_EFFECT_PROPNAME = propValue;

// Alias for Create React App compatibility
process.env.REACT_APP_CATS_EFFECT_PROPNAME = propValue;
```

In Webpack, this can be configured via [EnvironmentPlugin](https://webpack.js.org/plugins/environment-plugin/),
which allows passing environment variables into the build.

## Configuration Parameters Table

Below is a table of `IORuntime` configuration parameters, with their names for JVM (system property)
and JavaScript (environment variable), their default type and value, and a detailed description.

**Parameter:** `cats.effect.detectBlockedThreads`

- **JS env:** N/A
- **Default type:** `Boolean` (`false`)
- **Description:** **Enables the blocked thread detector.** If `true`, the scheduler will periodically check the state of random compute threads and log a warning if they are found to be in a `BLOCKED` state. Extremely useful for debugging, but adds overhead, so typically disabled in production.

**Parameter:** `cats.effect.logNonDaemonThreadsOnExit`

- **JS env:** N/A
- **Default type:** `Boolean` (`true`)
- **Description:** On JVM shutdown (in `IOApp`), checks if any non-daemon threads are still alive (besides the main one). If so, logs their list to help detect thread leaks.

**Parameter:** `cats.effect.logNonDaemonThreads.sleepIntervalMillis`

- **JS env:** N/A
- **Default type:** `Long` (`10000L`)
- **Description:** Interval (in milliseconds) between checks for non-daemon threads on exit.

**Parameter:** `cats.effect.warnOnNonMainThreadDetected`

- **JS env:** N/A
- **Default type:** `Boolean` (`true`)
- **Description:** If `true`, `IOApp` will log a warning if its `main` method was not started on the JVM's main thread (which may indicate scheduling issues).

**Parameter:** `cats.effect.cancelation.check.threshold`

- **JS env:** `CATS_EFFECT_CANCELATION_CHECK_THRESHOLD`
- **Default type:** `Int` (`512`)
- **Description:** **Cancellation check frequency.** Every `N` iterations of the fiber execution loop, the fiber checks if it has been cancelled. A lower value makes cancellation more responsive but adds overhead.

**Parameter:** `cats.effect.auto.yield.threshold.multiplier`

- **JS env:** `CATS_EFFECT_AUTO_YIELD_THRESHOLD_MULTIPLIER`
- **Default type:** `Int` (`2`)
- **Description:** **Auto-yield multiplier.** Auto-yield occurs every `cancelationCheck.threshold * multiplier` iterations. This helps prevent a fiber with a long synchronous loop from monopolizing the CPU.

**Parameter:** `cats.effect.tracing.exceptions.enhanced`

- **JS env:** `CATS_EFFECT_TRACING_EXCEPTIONS_ENHANCED`
- **Default type:** `Boolean` (`true`)
- **Description:** **Enhanced exception traces.** When enabled, Cats Effect enriches exception stack traces with async frames (fiber frames), significantly simplifying debugging at the cost of small overhead.

**Parameter:** `cats.effect.tracing.buffer.size`

- **JS env:** `CATS_EFFECT_TRACING_BUFFER_SIZE`
- **Default type:** `Int` (`16`)
- **Description:** **Tracing buffer size.** Number of stack frames to retain for async tracing. Will be rounded up to the next power of two. Affects stack trace granularity.

**Parameter:** `cats.effect.shutdown.hook.timeout`

- **JS env:** `CATS_EFFECT_SHUTDOWN_HOOK_TIMEOUT`
- **Default type:** `Duration` (`Inf`)
- **Description:** **Shutdown hook timeout.** If the application receives a signal (e.g., `Ctrl+C`), Cats Effect initiates a cancellation process for all fibers. This parameter determines how long the runtime waits for them to complete before forcibly shutting down the JVM. Set a specific value to avoid infinite waiting.

**Parameter:** `cats.effect.cpu.starvation.check.interval`

- **JS env:** `CATS_EFFECT_CPU_STARVATION_CHECK_INTERVAL`
- **Default type:** `FiniteDuration` (`1.second`)
- **Description:** **CPU starvation check interval.** At this interval, the built-in detector wakes up and checks whether the scheduler delay has exceeded the threshold.

**Parameter:** `cats.effect.cpu.starvation.check.initialDelay`

- **JS env:** `CATS_EFFECT_CPU_STARVATION_CHECK_INITIAL_DELAY`
- **Default type:** `Duration` (`10.seconds`)
- **Description:** **Initial starvation check delay.** To avoid false positives during "warming up" of the JVM (JIT compilation), the check does not start immediately. Set to `Duration.Inf` to disable starvation checks entirely.

**Parameter:** `cats.effect.cpu.starvation.check.threshold`

- **JS env:** `CATS_EFFECT_CPU_STARVATION_CHECK_THRESHOLD`
- **Default type:** `Double` (`0.1`)
- **Description:** **Starvation threshold.** The detector logs a warning if `(actual_time - expected_time) / expected_time > threshold`. A value of `0.1` means a warning will appear if the actual sleep time exceeded the expected time by more than 10%.

**Parameter:** `cats.effect.trackFiberContext`

- **JS env:** N/A
- **Default type:** `Boolean` (`false`)
- **Description:** **Fiber context tracking.** Enables the mechanism required for `IOLocal` to work correctly in multithreaded environments, similar to `ThreadLocal`. Enable only when needed, as it adds overhead.

## Configuration Recommendations

1.  **Development and Debugging:**
    - Set `cats.effect.detectBlockedThreads=true` to catch accidental blocking calls.
    - Ensure `cats.effect.tracing.exceptions.enhanced=true` (enabled by default) for readable stack traces.

2.  **Production (High Performance):**
    - Leave `cats.effect.detectBlockedThreads=false` (default).
    - If you do not use `IOLocal`, leave `cats.effect.trackFiberContext=false`.
    - Configure `shutdown.hook.timeout` according to your SLA (e.g., `30.seconds`) so applications don't hang on shutdown.

3.  **Combating Starvation (CPU Starvation):**
    - If you get false starvation warnings due to environment specifics (e.g., CI or slow machines), increase `cpu.starvation.check.interval` or set `cpu.starvation.check.initialDelay = Duration.Inf`.
    - If you are intentionally migrating to wider (more CPU) instances, you may want to lower the threshold for earlier problem detection.

4.  **Trading / Fintech (Ultra-Low Latency):**
    - Increase `cancelation.check.threshold` (e.g., to `4096`) to reduce cancellation check overhead.
    - Accordingly increase `auto.yield.threshold.multiplier` to defer auto-yield.
    - Disable enhanced exception traces entirely by setting `tracing.exceptions.enhanced=false`.
    - Disable starvation checks (`initialDelay = Inf`) as they add noise to measurements.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect IORuntime Configuration](https://typelevel.org/cats-effect/docs/core/io-runtime-config)

[doc]: https://typelevel.org/cats-effect/docs/concepts
