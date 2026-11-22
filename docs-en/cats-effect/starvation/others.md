# Configuration and False Positives

## Timer Resolution — False Positives

> **This is the only root cause that is NOT legitimate starvation, and leads to FALSE POSITIVE detections.**

The starvation detector works by comparing expected `IO.sleep` duration with actual duration.
If the actual duration frequently exceeds the expected, the detector thinks the task scheduler is performing poorly.

But there are two reasons why the timer itself may be inaccurate:

1. **Browser timer limits (on Scala.js):** Browsers, especially Firefox,
   may limit the minimum `setTimeout` resolution to 4 or even 10 ms.
   This does not affect the 100 ms threshold, but makes testing less precise.
2. **Timer contention:** This occurs when your application creates an **enormous number**
   of very short timers (e.g., millions of `IO.sleep(1.milli)`).

**In these scenarios:** the detector fires, but the problem is not CPU starvation, but timer inaccuracy.
**The correct solution is to disable the detector or increase its sensitivity threshold**, not to try to "optimize" the scheduler.

## Configuring the Starvation Detector

### Disabling (Disable)

**In most cases, this is NOT recommended!** Disable the check only if:

1. You have **no SLAs (latency requirements whatsoever)**
2. Or you have definitively confirmed the problem is timer inaccuracy (see section above).

```dotty
import scala.concurrent.duration.*

object MyMain extends IOApp:
  // Set infinite delay before first check
  override def runtimeConfig =
    super.runtimeConfig.copy(cpuStarvationCheckInitialDelay = Duration.Inf)

  def run(args: List[String]) = ???
```

### Sensitivity Tuning

By default, the detector warns at delays > **100 ms**. This value is computed as:
`cpuStarvationCheckInterval` (default: `1.second`) × `cpuStarvationCheckThreshold` (default: `0.1`).

- **Increasing sensitivity (fewer false positives):** Increase the check interval.
- **Decreasing sensitivity (earlier problem detection):** Decrease the interval.

```dotty
object MyOtherMain extends IOApp:
  // Increase threshold to 500 ms
  override def runtimeConfig =
    super.runtimeConfig.copy(cpuStarvationCheckInterval = 5.seconds)

  def run(args: List[String]) = ???
```

**Main advice:** Tune sensitivity according to your **SLAs**.
If your API must respond within 200 ms, then a 100 ms threshold is perfectly reasonable.
If your backend processes night batch jobs where second-long delays are acceptable,
you can increase the threshold or disable the check entirely.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Starvation and Tuning](https://typelevel.org/cats-effect/docs/core/starvation-and-tuning)

[doc]: https://typelevel.org/cats-effect/docs/concepts
