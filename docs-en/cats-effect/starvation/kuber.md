# Resource Overcommitment Competition

> This scenario is specific to **Kubernetes**, **Amazon ECS**, and similar container deployments with resource management.

In the world of container orchestration, **resource overcommitment** is a common practice.
A cluster is allocated a certain number of physical CPUs, but containers are "promised" (in limits) more cores
than actually exist, based on the assumption that they won't all use them simultaneously.

Suppose you have a physical server with 32 cores. You configure the container orchestrator to
allocate a total of 64 virtual cores to containers, hoping that not all applications will simultaneously load the CPU to 100%.
The overcommit factor here is 2 (or 100% overcommit).
This is a standard strategy _for most applications_, because they rarely use the CPU to its full capacity.

**The problem:** Cats Effect is **not** "most applications."
Cats Effect applications, especially those written following all recommendations,
**utilize all available CPUs very well**.
The work-stealing compute pool is constantly active. If you give a Cats Effect application a limit of 4 CPU,
it will attempt to use **all 4**, constantly.

Now imagine that on a single physical host with 32 cores, you placed 16 containers,
each allocated 4 CPU (limit). But 16 × 4 = 64 cores, while only 32 are physically available.
The overcommit factor is 2. If all 16 Cats Effect applications simultaneously attempt to utilize
their cores at 100% (e.g., during peak hours), a **symmetric peak load** will occur.
An airline sold 64 tickets for a plane with 32 seats. All passengers showed up.
This is **resource overcommitment competition**.
The OS kernel is forced to constantly switch context between 64 threads that "want" to work, but only 32 can physically work.
The result is a catastrophic performance drop and guaranteed starvation.

**Solution:** **Eliminate resource overcommitment.**
If a container is promised `n` CPUs, those CPUs must be physically reserved for it for the entire duration of its execution.

This advice pairs well with the previous one: **Cats Effect applications benefit from "taller"
(more CPU cores) and "narrower" (fewer replicas) deployments.**

**Example:**

- **Cluster:** 16 hosts, each with 64 CPU. Total available: 1024 CPU.
- **Bad configuration (with overcommit):** Each container gets a 4 CPU limit, you run 256 containers.
  Overcommit factor = 1 (maximum). Under peak load, all 256 containers will compete for 1024 CPU. This **does not scale**.
- **Good configuration (no overcommit):** Each container gets a 16 CPU limit. You run 64 containers.
  No competition. Due to better cache locality and absence of context switches, 64 powerful containers
  will likely handle more traffic than 256 weaker ones.

## CPU Pinning in Kubernetes

Even if you avoid overcommit, the Linux kernel scheduler may still actively "interfere."
It may move Cats Effect worker threads between different physical CPU cores,
violating **thread affinity**, which is critical for keeping L1/L2 caches hot.

In Kubernetes, this can be prevented by configuring **CPU Pinning** for your pod:

1. **Configure CPU management policy in kubelet:** Set the policy to `static`.
2. **Ensure `Guaranteed` QoS class for the pod:** All containers in the pod must have `requests` and `limits` for CPU, equal to each other.
3. **Request a whole number of CPUs:** In `resources.limits.cpu` and `resources.requests.cpu`, specify a whole number (e.g., `8`, not `8500m`).

After this, you will see CPU management messages in the kubelet logs (e.g., `cpumanager updated ...`).
This will reserve isolated physical cores for your container,
which will not be accessible to other containers (or even system processes).

## Process Contention

All the previous advice assumed a **single significantly active process** on the instance — your `java` Cats Effect process.
If this is not the case (e.g., your Java application shares a server with Postgres and Nginx),
the situation becomes **much worse**.
CPU competition arises not between threads of one process, but between different processes.
This can lead to **quadratic performance degradation**,
since each process has its own scheduler (Cats Effect inside the JVM and the OS scheduler for Postgres),
which are unaware of each other and actively contend.

**Diagnosis:** Use `top`, `htop`, or `pidstat`. If you see that **any** other process
(other than your application and kernel system processes) consistently consumes more than 5-10% CPU, this is a clear sign of process contention.

**Solution:** Assign a **separate instance (virtual machine, cluster node)** to each latency-critical application.
Do not mix them. In the Kubernetes context, this means using `taints` and `tolerations`
to isolate workloads or dedicating separate node pools for different application types.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect Starvation and Tuning](https://typelevel.org/cats-effect/docs/core/starvation-and-tuning)

[doc]: https://typelevel.org/cats-effect/docs/concepts
