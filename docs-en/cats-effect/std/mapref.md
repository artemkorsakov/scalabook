# MapRef — Atomic Dictionary by Key

`MapRef[F[_], K, V]` is a **specialized data structure** that can be thought of
as a map (dictionary) of key-value pairs, where each value (type `V`) for each key (type `K`)
is stored in its own, independent atomic reference — `Ref[F, V]`.

Access to each element is by its key,
and operations on different keys **can run concurrently**, without interfering with each other.

### Simplest Interface

```dotty
trait MapRef[F[_], K, V]:
  /** Returns the Ref for a specific key. */
  def apply(k: K): Ref[F, V]
```

You take a `MapRef`, pass a key `k` to it, and it returns a regular `Ref[F, V]`.
Then you work with this `Ref` as usual: `get`, `set`, `update`, `modify`.

## `MapRef` vs `Ref[F, Map[K, V]]`

The main question: why do we need `MapRef` if we can just put an entire immutable map into a single `Ref`?

| Operation                                     | `Ref[F, Map[K, V]]`                                                                                                                                                                                       | `MapRef[F, K, V]`                                                                                                      |
|:----------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------|
| **Update a single key**                       | 1. Read the entire map (`map.get`). 2. Change one entry (`map.updated`). 3. Write the entire map back (`ref.set`). **The entire process is atomic and blocks access to the map for other fibers.**   | Get `Ref[F, V]` by key and update it independently. **Doesn't block access to other keys.**                            |
| **Concurrent updates of different keys**      | ❌ Not possible. Even two changes to different keys will be serialized.                                                                                                                                   | ✅ Possible. Updates of keys `A` and `B` run fully simultaneously.                                                     |
| **Atomicity of complex operations**            | Atomic for the **entire map** as a whole.                                                                                                                                                                 | Atomic **only within a single key**. Cannot do atomic move of a value from key `A` to key `B`.                         |

**Simple metaphor:**

- `Ref[F, Map[K, V]]` is a **safe with one door**, inside which sits a large folder.
  To change one page, you lock the entire safe, take out the folder, edit the page, put the folder back, and unlock the safe.
- `MapRef[F, K, V]` is a **wall with many small cells**. Each cell has its own door and its own key.
  You can open multiple cells simultaneously and change their contents without interfering with each other.

## Key Advantages of `MapRef`

### 1. Reduced Contention

This is the most important advantage. In the traditional `Ref[F, Map]` model, on any update of any key,
the **entire map** is blocked (or enters a CAS race). This becomes a bottleneck under high load.

`MapRef` solves this problem by splitting data into **independent segments (shards)**.
Updates to different keys that end up in different shards can run **fully in parallel**.

### 2. Better Ergonomics

Working with `MapRef` is more intuitive and concise than repeatedly doing `ref.update(map => map.updated(key, value))`.

**Compare:**

```dotty
// With Ref[F, Map[K, V]]
def updateWithRef[F[_], K, V](ref: Ref[F, Map[K, V]])(key: K, value: V): F[Unit] =
  ref.update(_.updated(key, value))

// With MapRef
def updateWithMapRef[F[_], K, V](mapRef: MapRef[F, K, V])(key: K, value: V): F[Unit] =
  mapRef(key).set(value)
```

Code with `MapRef` is shorter and reads like working with an ordinary, but concurrent, map.

### 3. Flexibility: Working with `Option` and Default Values

Since `MapRef` is a **total map**, constructors usually provide two variants:

- **`MapRef.ofImmutableMap[F, K, V]`**: Returns `MapRef[F, K, Option[V]]`.
  This means that for a request to a non-existent key, you will get a `Ref` with `None`.
  This is convenient when keys are created dynamically, and their existence is not guaranteed. This is the most common and flexible variant.
- **`MapRef.ofHashMapWithDefault[F, K, V]`**: A constructor that accepts a default value (`default: V`).
  It will return `MapRef[F, K, V]`, where every key already has a default value.

## Real Example: In-Memory Database

Consider an example — creating a simple in-memory database. This is a classic use case for `MapRef`.

```dotty
import cats.effect.IO
import cats.effect.std.MapRef

// Imagine this is our "table" in the DB.
trait DatabaseClient[F[_], Id, Data]:
  def getDataById(id: Id): F[Option[Data]]
  def upsertData(id: Id, data: Data): F[Unit]

object DatabaseClient:
  // Factory method for creating an in-memory implementation.
  def inMemory[Id, Data]: IO[DatabaseClient[IO, Id, Data]] =

    // 1. Create MapRef[IO, Id, Option[Data]].
    //    shardCount = 5: Data will be split into 5 independent "pockets" to reduce contention.
    //    Empty None values for new keys.
    MapRef.ofShardedImmutableMap[IO, Id, Data](shardCount = 5).map { mapRef =>
      // 2. Implement the database interface using our mapRef.
      new DatabaseClient[IO, Id, Data]:

        override def getDataById(id: Id): IO[Option[Data]] =
          // Get the Ref for a specific Id and read its value.
          // This is an instant, non-blocking operation.
          mapRef(id).get

        override def upsertData(id: Id, data: Data): IO[Unit] =
          // For upsert: get the Ref for Id and set it to Some(data).
          // This is an atomic operation for that Id.
          mapRef(id).update(_ => Some(data))
    }
end DatabaseClient
```

### Why is this a good example?

1. **Live sharding example:** `shardCount = 5` is not magic. It tells `MapRef` to create 5 "cells",
   keys will be distributed among them, and thus can be updated in parallel.
2. **Efficiency:** `get` and `upsert` for different `Id`s (e.g., `"user1"` and `"user2"`)
   will run simultaneously and won't block each other.
3. **Simplicity:** All the complexity of concurrent map operations is encapsulated inside `MapRef`.

## Other Use Cases

### 1. Cache with Per-Key Locking

You need to cache the results of expensive operations. Loading is expensive, and you want
it to run only once per key, even with concurrent access.
`MapRef` is ideal for this task.

```dotty
import cats.effect.{IO, Ref}
import cats.effect.std.MapRef

class ExpensiveCache[K, V](mapRef: MapRef[IO, K, Option[V]]):
  def getOrLoad(key: K, load: K => IO[V]): IO[V] =
    mapRef(key).modify {
      case Some(v) => (Some(v), v.pure[IO]) // Already in cache
      case None    => (None, load(key))     // Not in cache, need to load
    }.flatten // flatten, since modify returns F[F[V]]
```

**Important:** In this example, the `load(key)` operation **is executed inside** `modify`, i.e., under this key's lock.
This guarantees that for the same `key`, the `load` function will be called **exactly once**. Other fibers will wait.

### 2. Fine-Grained Counter or State Management

You have a million users, and for each you need to store an independent action counter.
With `Ref[Map]`, updating the counter for one user would block the entire map. With `MapRef` it is simple:

```dotty
def incrementUserActionCount(userId: String): IO[Unit] =
  mapRef(userId).update(_.orElse(Some(0)).map(_ + 1))
```

## Important Characteristics and Limitations

### ✅ Advantages of `MapRef`:

- **Reduced contention:** High performance when working with a large number of unique keys thanks to sharding.
- **Better ergonomics:** Code becomes cleaner and more understandable.
- **Flexibility:** Work with existing and non-existing keys via `Option`.

### ❌ Limitations and When `MapRef` Is Not Suitable:

- **No atomic operations spanning multiple keys:** You **cannot** atomically move a value
  from key `A` to key `B` or atomically read values of two keys. For this, you still need `Ref[F, Map[K, V]]`.
- **No full iteration or `keys`.** You cannot efficiently get all keys or all values. `MapRef` is not iterable.
- **`shardCount` is a compromise.** Too few shards won't give a parallelism benefit.
  Too many — adds overhead for hashing and management.
  It needs to be tuned to your load (usually around the number of CPU cores).

## `MapRef` Compared to Alternatives

| Scenario                                                              | Best Choice                    | Why                                                            |
|:----------------------------------------------------------------------|:-------------------------------|:---------------------------------------------------------------|
| You need atomicity and consistency for all keys at once               | `Ref[F, Map[K, V]]`            | The only way to atomically read and change the entire map.    |
| You need to update different keys in parallel, under high load        | **`MapRef`**                   | Best performance and scalability.                              |
| Keys and values are created and deleted dynamically                   | **`MapRef`**                   | Use `MapRef[F, K, Option[V]]`.                                |
| You also need to efficiently iterate over keys or values              | `Ref[F, Map]` or external store | `MapRef` doesn't provide an iteration API.                    |

## Summary

`MapRef` is a specialized, powerful primitive for high-concurrency scenarios
where you need to work with a large number of keys, sharing state among them,
but without the need for atomic operations spanning multiple keys at once.

It implements the **sharding** pattern out of the box, offering a simple and ergonomic API.
If you spot `Ref[F, Map[K, V]]` in your code that is frequently updated for different keys
and becomes a bottleneck — most likely, you need to replace it with `MapRef`.

---

**Links:**

- [Cats effect documentation][doc]
- [Cats effect MapRef](https://typelevel.org/cats-effect/docs/std/mapref)

[doc]: https://typelevel.org/cats-effect/docs/concepts
