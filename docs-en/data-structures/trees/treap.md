# Cartesian Tree

**Definition:**

The [cartesian tree](https://en.wikipedia.org/wiki/Cartesian_tree) (_treap_, from _tree_ + _heap_),
also called a deramida or a "pineap" (_pyramid_ + _tree_), is a binary search tree
in which each vertex stores a pair of a _key_ and a _priority_.
The structure simultaneously combines two properties:

1. **By key** — the tree is a binary search tree: at the root node the key is greater than
   all keys in the left subtree and less than all keys in the right subtree.
2. **By priority** — the tree is a heap: the priority of each node is greater than or equal to
   the priorities of all its descendants (max-heap by priority).

Thanks to this property the tree is easy to construct geometrically: put `n` points on a plane,
where the abscissa (`x`) is the key and the ordinate (`y`) is the priority.
Pick the topmost point (with the largest `y`) — it becomes the root.
The points to its left recursively form the left subtree, the points to its right form the right subtree.
If all keys and priorities are distinct, such a tree is built **uniquely**.

The name "cartesian" comes not from the author of the algorithm, but from the cartesian coordinate system,
in which it is convenient to represent the structure: the axes `x` and `y` describe at the same time
the order of the search tree (by `x`) and the heap (by `y`).

**Complexity:**

If priorities are chosen randomly and independently, the expected height of the tree
is logarithmic, which is why the cartesian tree is called a _randomized_ search tree.
All operations run in time proportional to the height:

- _Search for an element_ (`search`)
    - **Time** - O(log n) (expected)
    - **Space** - O(log n)
- _Insertion of an element_ (`insert`)
    - **Time** - O(log n) (expected)
    - **Space** - O(log n) 
- _Removal of an element_ (`remove`)
    - **Time** - O(log n) (expected)
    - **Space** - O(log n)
- _Partitioning_ (`split`)
    - **Time** - O(log n) (expected)
    - **Space** - O(log n)
- _Merging_ (`merge`)
    - **Time** - O(log n) (expected)
    - **Space** - O(log n)

## Structure

Unlike an ordinary [binary search tree](binary-tree.md),
each node of a cartesian tree stores not only a key but also a priority.
In the examples below the priority is generated randomly, which is what provides
the expected logarithmic running time of the operations.

**Code:**

```dotty
enum Treap[+K, +P]:
  case Leaf
  case Branch(key: K, priority: P, left: Treap[K, P], right: Treap[K, P])

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false

  lazy val size: Int = this match
    case Leaf                  => 0
    case Branch(_, _, l, r)    => 1 + l.size + r.size

  def keys: List[K] = this match
    case Leaf              => Nil
    case Branch(k, _, l, r) => l.keys ::: k :: r.keys
```

## Reasoning About the Complexity

The intuition behind why random priorities yield a logarithmic height is as follows:
consider a vertex `y` and an arbitrary other vertex `x` (`x < y`).
Vertex `x` is an ancestor of `y` exactly when the priority of `x` is the largest
among all vertices on the key interval `[x, y]`. Since priorities are distributed
uniformly and independently, the probability of this is `1 / (|x - y| + 1)`.

Summing these probabilities over all possible ancestors, we obtain the expected depth:

\\(\mathbb{E}\[d\_{i}\] = \sum\_{j \neq i} \frac{1}{|i - j| + 1} 
\leq 2 \sum\_{k=2}^{n} \frac{1}{k} = O(\log n)\\)

where the last sum is the harmonic series. Thus the expected depth of a vertex
(and therefore the height of the tree) is bounded by a logarithmic function.

## Implementation Based on split and merge

The basic operations of a cartesian tree are conveniently expressed through two helper functions:
`merge`, which joins two trees, and `split`, which partitions a single tree into two.
Through them it is easy to implement insertion, deletion, and search.

### Merge

`merge` takes two trees `A` and `B`, for which it is known that **all keys of `A` are less than all keys of `B`**,
and returns a correct cartesian tree consisting of all vertices.

The node with the larger priority becomes the root of the resulting tree.
If that is the root of `A`, its left subtree remains unchanged, and the right subtree
is recursively merged with tree `B`. We proceed symmetrically if the root of `B` has the larger priority.

**Code:**

```dotty
def merge[K, P](a: Treap[K, P], b: Treap[K, P])(using Ordering[P]): Treap[K, P] =
  (a, b) match
    case (Leaf, _)            => b
    case (_, Leaf)            => a
    case (Branch(_, pa, _, _), Branch(_, pb, _, _)) =>
      if summon[Ordering[P]].gteq(pa, pb) then
        a match
          case Branch(ka, _, la, ra) => Branch(ka, pa, la, merge(ra, b))
          case Leaf                  => b
      else
        b match
          case Branch(kb, _, lb, rb) => Branch(kb, pb, merge(a, lb), rb)
          case Leaf                  => a
```

If the roots have equal priorities, the larger of the two subtrees is taken
as the new root — the `gteq` check ensures determinism. The `Leaf` branch inside
the inner pattern match is unreachable, since the outer `match` already handles empty
trees, but is retained for type completeness.

### Split

`split` takes a tree `t` and a key `x` and returns a pair of trees `(l, r)` such
that all keys in `l` are not greater than `x`, and all keys in `r` are strictly greater than `x`.

First we determine which part the root should belong to.
If the root key is `<= x`, the root stays in the left part, and its right subtree
is recursively partitioned; otherwise — symmetrically.

**Code:**

```dotty
def split[K: Ordering, P](t: Treap[K, P], x: K): (Treap[K, P], Treap[K, P]) =
  t match
    case Leaf => (Leaf, Leaf)
    case Branch(k, p, l, r) =>
      if summon[Ordering[K]].lteq(k, x) then
        val (ll, rr) = split(r, x)
        (Branch(k, p, l, ll), rr)
      else
        val (ll, rr) = split(l, x)
        (ll, Branch(k, p, rr, r))
```

### Insertion

To insert a key `x`, it suffices to split the tree by `x` into `l` and `r`,
create a node with key `x` and a random priority, and join the three trees via `merge`.

**Code:**

```dotty
def insert[K: Ordering, P](t: Treap[K, P], key: K, priority: P): Treap[K, P] =
  val (l, r) = split(t, key)
  merge(merge(l, Branch(key, priority, Leaf, Leaf)), r)
```

### Search

Search by key is analogous to search in an ordinary [binary search tree](binary-tree.md)
and does not use priorities: at each step we compare the key with the root key
and descend into the left or right subtree.

**Code:**

```dotty
def search[K: Ordering, A](t: Treap[K, A], key: K): Option[A] =
  t match
    case Leaf => None
    case Branch(k, pr, l, r) =>
      val ord = summon[Ordering[K]]
      if ord.lt(key, k) then search(l, key)
      else if ord.gt(key, k) then search(r, key)
      else Some(pr)
```

### Deletion

Deletion of a key `x` can be expressed through a pair of `split` calls, however it is
more illustrative (and not tied to a particular way of finding a predecessor)
to define it directly recursively: we descend to the target vertex by key,
and once found, we replace it by merging its left and right subtrees.

**Code:**

```dotty
def remove[K, P](t: Treap[K, P], key: K)(using Ordering[K]): Treap[K, P] =
  t match
    case Leaf => Leaf
    case Branch(k, p, l, r) =>
      val ord = summon[Ordering[K]]
      if ord.lt(key, k) then Branch(k, p, remove(l, key), r)
      else if ord.gt(key, k) then Branch(k, p, l, remove(r, key))
      else merge(l, r)
```

If the key is not found, the tree remains unchanged. If the vertex is found,
it is "discarded", and its left and right subtrees, which already satisfy
the condition for a correct `merge` (all keys on the left are less than all keys on the right),
are joined into a single tree.

## Usage Example

Let us demonstrate the basic operations on a simple example.
Because the priorities in the example are given explicitly (not randomly), the guarantee
of a logarithmic height does not apply here — this is merely an illustration of the algorithm.

```dotty
val t0: Treap[Int, Int] = Leaf

val t1 = insert(t0, 5, 10)
val t2 = insert(t1, 3, 20)
val t3 = insert(t2, 8, 5)
val t4 = insert(t3, 1, 15)

t4.keys        // List(1, 3, 5, 8)  (inorder traversal, keys sorted)
search(t4, 3)  // Some(20)          (priority of the vertex with key 3)
search(t4, 9)  // None

val (l, r) = split(t4, 4)
l.keys         // List(1, 3)        (keys <= 4)
r.keys         // List(5, 8)        (keys > 4)
```

Even though `keys` returns a sorted sequence
(which is a property of the binary search tree by key), the internal shape of the tree depends
on the priorities: in the example above the root is the vertex `(3, 20)` — it has the largest
priority among all vertices.

## Applications

The cartesian tree combines implementation simplicity with logarithmic guarantees,
which is why it is often used where a dynamic ordered set is needed:

- Implementation of ordered associative containers (dictionaries and sets)
  with an expected complexity of O(log n) per operation.
- The `lower_bound` operation — finding the first element not smaller than the given one.
- Counting the number of elements in a range `[l, r]` using a pair of `split` calls.
- **Implicit treap**: when the priority acts as the position,
  the structure allows working with a sequence as if it were an array, supporting
  insertion, deletion, reversal, and merging of substrings in O(log n).
- **Persistent** data structures: since the operations do not destroy the original subtrees,
  versions of the tree easily reuse nodes, providing efficient persistent structures.

---

**Links:**

- [Source code][source]
- [Donald E. Knuth - The Art of Computer Programming, section 2.3.1](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Алгоритмика - Cartesian tree](https://ru.algorithmica.org/cs/tree-structures/treap/)
- [Алгоритмика - Implicit key](https://ru.algorithmica.org/cs/tree-structures/)

[source]: https://gitverse.ru/scalabook/algorithms