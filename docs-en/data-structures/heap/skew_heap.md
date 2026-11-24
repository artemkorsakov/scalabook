# Skew Heap

A [skew heap] is a self-adjusting form of a left-leaning (leftist) heap
that attempts to maintain balance by unconditionally swapping all nodes on the merge path when merging two heaps.
(The merge operation is also used when inserting and removing values.)

## The idea of self-adjustment

In a leftist heap, after a merge we check the ranks of the children and, if the invariant is violated,
swap them (the `swap` function). The skew heap goes further: it does this **unconditionally**.
At every merge we always recursively merge the root's right subtree with the other tree,
and then simply swap the left and right children without looking at any balance metric.

Through such "always swap" behavior the skew heap gradually adapts its shape
to the sequence of operations. Although a single operation may in the worst case be expensive
(up to O(n)), the total cost of a sequence of operations is amortized to O(log n) per operation.
This is a classic example of **amortized analysis** and a self-regulating data structure.

## Comparison with the leftist heap

- **Leftist heap** uses a rank and restores balance only when necessary,
  guaranteeing O(log n) in the worst case.
- **Skew heap** drops ranks entirely, making the implementation even simpler
  (no `rank`, and no `swap` with a condition check). Instead of a worst-case guarantee
  it relies on amortized complexity.

## Operations

As with other heaps, the public operations `insert` and `remove` are expressed through `merge`:

- **Insertion** — merging with a single-element heap.
- **Removal of the extreme** — extracting the root and merging its left and right subtrees.
- **Minimum** — reading the root, O(1).

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
enum SkewHeap[+A]:
  import SkewHeap.*

  case Leaf
  private case Branch(min: A, left: SkewHeap[A], right: SkewHeap[A])

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false

  def insert[B >: A: Ordering](x: B): SkewHeap[B] =
    merge(apply(x), this)

  def remove[B >: A: Ordering]: Option[SkewHeap[B]] = this match
    case Branch(_, left, right) => Some(merge(left, right))
    case _                      => None

end SkewHeap

object SkewHeap:
  def empty[A]: SkewHeap[A] = Leaf

  def apply[A: Ordering](x: A): SkewHeap[A] =
    Branch(x, Leaf, Leaf)

  def merge[A: Ordering](x: SkewHeap[A], y: SkewHeap[A]): SkewHeap[A] =
    (x, y) match
      case (_, Leaf) => x
      case (Leaf, _) => y
      case (Branch(x1, l1, r1), Branch(x2, l2, r2)) =>
        if x1 < x2 then
          Branch(x1, merge(Branch(x2, l2, r2), r1), l1)
        else Branch(x2, merge(Branch(x1, l1, r1), r2), l2)

end SkewHeap
```

### Understanding the Scala implementation

- **No ranks.** Each node stores only a value and two children — no auxiliary fields for balancing.
  This is the most compact structure among the heaps discussed here.
- **Merge with rotation.** The smaller root's right subtree is recursively merged with the other tree,
  and the result plus the left subtree become the children: the right child gets the merge result,
  the left child gets the former left subtree. Thanks to this rotation the tree "tilts" toward the side
  opposite the last operations, providing self-regulation.
- **Minimal code.** Since `insert` and `remove` reduce to `merge`, all the logic
  fits in a single recursive pattern match.

### Example

```dotty
val emptySkewHeap: SkewHeap[Int]     = SkewHeap.empty
val nonEmptySkewHeap: SkewHeap[Int]  = SkewHeap.empty.insert(1).insert(2)

emptySkewHeap.isEmpty       // true
nonEmptySkewHeap.isEmpty    // false

emptySkewHeap.minOption     // None
nonEmptySkewHeap.minOption  // Some(1)

nonEmptySkewHeap.remove
// Some(Branch(2,Leaf,Leaf))
```

The example confirms that the root is always minimal, and after removing the root the new heap
is again arranged correctly.

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