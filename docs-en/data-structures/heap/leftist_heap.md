# Leftist Heap

A leftist heap is a kind of heap introduced by Clark Crane and subsequently studied in depth
by Chris Okasaki in his book "Purely Functional Data Structures". It is specifically designed so that
**merging two heaps** is fast — O(log n), unlike the O(n) of a plain binary heap.

## The problem a leftist heap solves

Recall that a binary heap cannot quickly merge two heaps into one. However, in many problems
(computing a running median, graph algorithms, or merging priority queues)
the `merge` operation is critical. The leftist heap gives up a rigid depth balance
but gains an efficient merge in return.

The key idea is to keep the tree structure **skewed to the left**: the short branch is always on the right.
When merging we always recursively merge the right branches, while the left branch stays untouched —
so the amount of work is proportional to the length of the right path, which we bound from below.

## The two properties

Okasaki's implementation of a leftist heap satisfies two properties:

- **Heap-ordered property:** `value(node) <= value(node.left)`, and also `value(node) <= value(node.right)`.
  This is the standard min-heap invariant: a vertex's key does not exceed the keys of its children.
- **Leftist property:** `rank(node.left) >= rank(node.right)`.

, where `rank` is the length of the shortest path from the node to an empty node (the rightmost element of the heap —
the extreme right path from the root to a null node).
Formally, `rank(Leaf) = 0`, and `rank(Branch(_, l, r, _)) = rank(r) + 1`,
since by the leftist property the right path is always the shortest.

Combining these properties together, we can guarantee execution time of at most O(log n)
for insert/remove operations in a leftist heap.
Why this works: the leftist invariant guarantees that the right path from any node to a leaf is short
(no longer than O(log n)), and merging — and consequently insertion and deletion — proceed along the right paths.

## Relation to insert and remove

Although the leftist heap only publicly exposes `insert` and `remove`, both are easily expressed via `merge`:

- **Insertion** is the merge of the current heap with a single-element heap.
- **Removal of the extreme** extracts the root and merges its left and right subtrees.

Thus all non-trivial logic is concentrated in a single `merge` function, which makes
the implementation compact and easy to understand. After every merge the helper `swap`
restores the leftist invariant, swapping the children if necessary.

### Complexity

- _Minimum element of heap_ (`min`)
    - **Time** - O(1)
    - **Space** - O(1)
- _Insertion into heap_ (`insert`)
    - **Time** - O(log n)
    - **Space** - O(log n)
- _Removal from heap_ (`remove`)
    - **Time** - O(log n)
    - **Space** - O(log n)
- _Union of two heaps_ (`merge`)
    - **Time** - O(log n)
    - **Space** - O(log n)

### Code

```dotty
enum LeftistHeap[+A]:
  import LeftistHeap.*

  case Leaf

  private case Branch(
      min: A,
      left: LeftistHeap[A],
      right: LeftistHeap[A],
      override val rank: Int
  )

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false

  val rank: Int = this match
    case neh: Branch[A] => neh.rank
    case _              => 0

  lazy val minOption: Option[A] = this match
    case neh: Branch[A] => Some(neh.min)
    case _              => None

  def insert[B >: A: Ordering](x: B): LeftistHeap[B] = this match
    case Branch(min, left, right, _) =>
      if left.rank > right.rank then bubble(min, left, right.insert(x))
      else bubble(min, left.insert(x), right)
    case _ => LeftistHeap(x)

  def remove[B >: A: Ordering]: Option[LeftistHeap[B]] = this match
    case Branch(_, left, right, _) => Some(merge(left, right))
    case _                         => None
end LeftistHeap

object LeftistHeap:
  def empty[A]: LeftistHeap[A] = Leaf

  def apply[A: Ordering](x: A): LeftistHeap[A] =
    LeftistHeap(x, Leaf, Leaf)

  private def apply[A: Ordering](
      x: A,
      l: LeftistHeap[A],
      r: LeftistHeap[A]
  ): LeftistHeap[A] =
    Branch(x, l, r, r.rank + 1)

  private def bubble[A: Ordering](
      x: A,
      l: LeftistHeap[A],
      r: LeftistHeap[A]
  ): LeftistHeap[A] =
    (l, r) match
      case (Branch(y, lt, rt, _), _) if x > y =>
        LeftistHeap(y, LeftistHeap(x, lt, rt), r)
      case (_, Branch(z, lt, rt, _)) if x > z =>
        LeftistHeap(z, l, LeftistHeap(x, lt, rt))
      case (_, _) => LeftistHeap(x, l, r)

  def merge[A: Ordering](x: LeftistHeap[A], y: LeftistHeap[A]): LeftistHeap[A] =
    (x, y) match
      case (_, Leaf) => x
      case (Leaf, _) => y
      case (
            Branch(xx, xl, xr, _),
            Branch(yy, yl, yr, _)
          ) =>
        if xx < yy then swap(xx, xl, merge(xr, y))
        else swap(yy, yl, merge(yr, x))

  private def swap[A: Ordering](
      x: A,
      l: LeftistHeap[A],
      r: LeftistHeap[A]
  ): LeftistHeap[A] =
    if l.rank < r.rank then LeftistHeap(x, r, l)
    else LeftistHeap(x, l, r)

end LeftistHeap
```

### Understanding the Scala implementation

- **Rank.** Unlike the binary heap, the node stores no size or height but precisely `rank` —
  the length of the shortest (right) path to an empty node. When constructing a node via `apply(x, l, r)`,
  the rank is computed as `r.rank + 1`, because the left child is never shorter than the right one.
- **Merge.** The function picks the root with the smaller key and recursively merges its right subtree
  with the other heap. Then `swap` checks the leftist invariant and, if violated, swaps the children.
- **Insertion.** If the heap is non-empty, the element is inserted into whichever subtree preserves
  the leftist shape, and `bubble` restores heap ordering.
- **Functional nature.** Like the binary heap, the implementation is fully immutable
  and shares unchanged subtrees between versions through structural sharing.

### Example

```dotty
val emptyLeftistHeap: LeftistHeap[Int] = LeftistHeap.empty
val nonEmptyLeftistHeap: LeftistHeap[Int] = LeftistHeap.empty.insert(1).insert(2)
 
emptyLeftistHeap.isEmpty       // true
nonEmptyLeftistHeap.isEmpty    // false
  
emptyLeftistHeap.rank          // 0
nonEmptyLeftistHeap.rank       // 1
  
emptyLeftistHeap.minOption     // None
nonEmptyLeftistHeap.minOption  // Some(1)
  
emptyLeftistHeap.insert(5)
// Branch(5,Leaf,Leaf,1)
nonEmptyLeftistHeap.insert(5)
// Branch(1,Branch(2,Leaf,Leaf,1),Branch(5,Leaf,Leaf,1),2)

emptyLeftistHeap.remove
// None
nonEmptyLeftistHeap.remove
// Some(Branch(2,Leaf,Leaf,1))

LeftistHeap.merge(emptyLeftistHeap, emptyLeftistHeap)
// Leaf
LeftistHeap.merge(emptyLeftistHeap, nonEmptyLeftistHeap)
// Branch(1,Branch(2,Leaf,Leaf,1),Leaf,1)
LeftistHeap.merge(nonEmptyLeftistHeap, nonEmptyLeftistHeap)
// Branch(1,Branch(2,Leaf,Leaf,1),Branch(1,Branch(2,Leaf,Leaf,1),Leaf,1),2)
```

The example shows that the root always remains the minimum element, and the `merge` operation
correctly combines two heaps into a new leftist heap while maintaining both invariants.

---

**Links:**

- [Source code][source]
- [Heap - Wiki][heap wiki]
- [Binary heap - Wiki][bin heap wiki]
- [Leftist heap in the "Heap" section][heap]
- [Pairing heap - Wiki][pairing heap wiki]
- [Skew heap - Wiki][skew heap wiki]
- [Алгоритмика - Куча](https://ru.algorithmica.org/cs/basic-structures/heap/)
- [Heap - Scalacaster](https://github.com/vkostyukov/scalacaster/blob/master/src/heap)
- [A Functional Approach to Standard Binary Heaps - Vladimir Kostyukov](http://arxiv.org/pdf/1312.4666v1)

[heap wiki]: https://en.wikipedia.org/wiki/Heap_(data_structure)
[bin heap wiki]: https://en.wikipedia.org/wiki/Binary_heap
[heap]: ../heap.md
[pairing heap wiki]: https://en.wikipedia.org/wiki/Pairing_heap
[skew heap wiki]: https://en.wikipedia.org/wiki/Skew_heap
[source]: https://gitverse.ru/scalabook/algorithms