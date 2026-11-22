# CPU Starvation

One of the most insidious trends in modern software deployed on servers is
the strategy of shrinking application resources to the minimum, packaging these applications into containers,
and attempting to recover the lost power by replicating a large number of instances.
While there are valid reasons for containerizing applications and designing them for a replicated topology,
abstracted from the underlying host systems, it is quite possible to overdo a good thing.
This practice, known as **"wide" deployment**,
fundamentally contradicts the way Cats Effect works.

## Why Few CPUs Are Bad for Cats Effect?

Imagine a typical application deployed in a container,
where the administrator has allocated **just 2 virtual CPUs (vCPU)**.
Which threads will compete for these two cores?

Even for a simple Cats Effect application, the number of threads can be significant:

1. **`compute-worker` (compute pool):** The number of threads is usually equal to the number of vCPU. In our case — **2 threads**.
   These are the only threads that perform useful, compute-intensive work for your application.
2. **`timer` (timer dispatch thread):** **1 thread.** Handles timers (`IO.sleep`, etc.).
3. **`async-io` (async I/O dispatch thread):** **1 thread.**
4. **`blocking` (blocking pool):** By default, creates threads as needed, but at least **2 threads** may be active.
5. **`JIT compiler` (JIT compilation thread):** **1 thread.** Indispensable for JVM performance.
6. **`GC` (garbage collection thread):** **1 thread** (or several, depending on the chosen collector).
7. **`main` (main thread):** **1 thread**, which starts the `IOApp`.
8. **Third-party library threads:** For example, AWS SDK v2 creates its own thread pools.

**Total:** In the worst case, about **9 active threads** compete for just **2 virtual CPU cores**!

The overcommit coefficient for compute resources is `1 - 2/9 = 78%`.
This means that nearly 80% of the time, the OS kernel spends _switching context_ between these threads,
rather than executing useful work for your application.
The result is chronic **CPU starvation** and warnings from Cats Effect.

## Comparison: 2 CPUs vs 8 CPUs

Let's look at how the situation changes if we allocate **8 virtual CPUs** to the container
(keeping the total cluster power the same, proportionally reducing the number of instances).

| Thread type                | With 2 vCPU (9 threads) | With 8 vCPU (9 threads) |
|:---------------------------|:------------------------|:------------------------|
| **Compute (compute)**      | 2                       | 8                       |
| **All others (rest)**      | 7                       | 7                       |
| **Overcommit coefficient** | `1 - 2/9 ≈ 78%`         | `1 - 8/15 ≈ 47%`        |

Even moving from 2 to 8 cores reduces the overcommit coefficient from 78% to 47%.
This is already a working, though not ideal, configuration.
With **16 vCPU** it drops to `1 - 16/23 ≈ 30%`, and with **32 vCPU** — to `1 - 32/39 ≈ 18%`,
where competition can be considered practically non-impactful on performance.

## Axiom for Cats Effect Deployment

From this, we can formulate a golden rule:

> **«Replicated Cats Effect deployments generally benefit from using slightly "taller" and less "wide" resources.»**

In other words: **it is better to have a few powerful instances (with 8, 16, 32 vCPU) than many weak ones (with 2, 1, 0.5 vCPU).**

This is the opposite of standard recommendations for many Java HTTP servers
based on blocking I/O (e.g., older Tomcat configurations), where "narrow" pods (1-2 vCPU) are the norm.
Cats Effect, with its user-space work-stealing scheduler, needs room to maneuver.

### Why Does This Work?

The essence of Cats Effect is to move the bulk of scheduling logic to the _application_ level (user space),
where it can be more aggressively optimized for async workloads.
The OS kernel scheduler is "heavy artillery" — it is good for general cases, but creates significant overhead.
When you have extremely few vCPU, the user-space scheduler simply has no room to maneuver.
You end up fighting not for performance, but just to switch context.

> **"Going wide limits resources available in user space
> (hindering the substantial impact of the user-space scheduler),
> while shifting the overall scheduling problem to the kernel (which is far less efficient)."**

This strategy makes sense when the user-space scheduler is very limited in capabilities.
But it is simply counterproductive when the user-space scheduler is _more_ optimized
than the one in the kernel.
This is exactly the case with Cats Effect.

## Practical Implications and Tuning

Of course, it is not as simple as doubling vCPU and halving the number of instances.
Scaling is a complex process, and you will likely need to configure other resources,
such as memory, connection limits, file descriptor limits, and autoscaling signals.
However, 8 vCPU is a **good empirical rule for the minimum number of processors**
that should be available to a Cats Effect application.
16 or even 32 vCPU would likely further improve performance, and experimenting with such settings is worth it.

### Special Case: Kubernetes and `cpu_quota`

One of the most critical and frequent causes of "CPU starvation" in modern deployments is
**running inside Kubernetes without a configured CPU quota (`cpu_quota`)**.

When you create a pod in Kubernetes, you can specify `limits.cpu`.
Based on this value, kubelet configures a cgroup for the container.
However, **the JVM cannot automatically read cgroup settings from older versions (cgroup v1)**.
In older Java versions (before Java 10) and in some default configurations,
the JVM determines the number of available processors as `1`.
This is **fatal** for Cats Effect, which by default will create only 1 compute thread!

**Diagnosing the problem:**

- You deployed an application in Kubernetes.
- The application works, but has very low performance and consistently emits starvation warnings.
- You checked thread dumps (`jstack`) and saw only 1 thread in the `io-compute-*` pool.

**Solution:**

You have two paths:

1. **Configure `cpu_quota` (usually set via `limits.cpu` in the pod).**
   After this, the JVM will be able to correctly determine the number of available cores,
   starting from Java 10 (by default) and in Java 8 with update 191 (if `-XX:+UseContainerSupport` is enabled).

2. **Explicitly specify the JVM processor count** using the `-XX:ActiveProcessorCount=<n>` argument. This is the most reliable and controlled approach.
     ```dockerfile
     ENTRYPOINT ["java", "-XX:ActiveProcessorCount=8", "-jar", "app.jar"]
     ```
     You should set this value equal to the `limits.cpu` in your Kubernetes pod
     (or slightly less, leaving some for other processes in the container).
     This is a critical setting for any Cats Effect application running under Kubernetes.

A more detailed guide on [containerizing Java applications for Kubernetes](https://learn.microsoft.com/en-us/azure/developer/java/containers/kubernetes#understand-jvm-available-processors) describes the mechanism in detail.

> **Key takeaway:** All Cats Effect applications running in Kubernetes
> must either have `cpu_quota` configured (allowing the JVM to automatically detect the core count),
> **or** use the `-XX:ActiveProcessorCount` argument to explicitly tell the JVM how many cores to use.
> Without this, your application will run on one thread and likely suffer from starvation.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Starvation and Tuning](https://typelevel.org/cats-effect/docs/core/starvation-and-tuning)
- [Threads at Scale - Daniel Spiewak](https://www.youtube.com/watch?v=PLApcas04V0)

[doc]: https://typelevel.org/cats-effect/docs/concepts
