# Pairing Heap

[Pairing heaps][pairing heap wiki] are multi-way tree structures, heap-ordered,
and can be considered as simplified Fibonacci heaps.
They were proposed by Fredman, Sedgewick, Sleator, and Tarjan in 1986
and attracted attention due to their unusually simple implementation combined with excellent
asymptotic and practical properties.

## How a pairing heap differs from a binary heap

- **Multi-way branching.** Nodes may have an arbitrary number of children, stored as a list of subtrees.
  This fundamentally distinguishes a pairing heap from binary variants
  (binary, leftist, skew), which have exactly two children.
- **Heap ordering.** A node's key does not exceed the keys of all its children —
  the root always contains the minimum element.
- **Simple structure.** Storing a value and a list of sub-heaps in each node is sufficient.
  No ranks, heights, or additional balancing fields are required.

## The idea of the pairing operation

The secret of the pairing heap's efficiency lies in how subtrees are processed after the root is removed.
The `pairing` method merges the subtrees in pairs: first the first and second trees in the list are merged,
then the third and fourth, and so on, after which the result is merged into a single heap.
This two-phase scheme ("first pairwise, then accumulate") yields amortized logarithmic complexity
for `insert` and `remove` without the complex logic characteristic of the Fibonacci heap.

Although a rigorous proof of the amortized bounds of pairing heaps turned out to be non-trivial
and remained an open question for a long time, in practice pairing heaps perform excellently
and are often the fastest among the easily implementable ones.

## Operations

All operations are expressed through the auxiliary `merge` of two heaps:

- **Insertion (`insert`)** — merging a single-element heap with the current heap.
- **Removal of the extreme (`remove`)** — extracting the root and applying `pairing` to its list of subtrees.
- **Minimum (`min`)** — simply reading the root, O(1).

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
enum PairingHeap[+A]:

  import PairingHeap.*

  case Leaf
  private case Branch(min: A, subtrees: List[PairingHeap[A]])

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false

  def insert[B >: A: Ordering](x: B): PairingHeap[B] =
    merge(PairingHeap(x), this)

  def remove[B >: A: Ordering]: Option[PairingHeap[B]] = this match
    case Branch(_, subtrees) => Some(pairing(subtrees))
    case _                   => None

end PairingHeap

object PairingHeap:
  def empty[A]: PairingHeap[A] = Leaf

  def apply[A: Ordering](x: A): PairingHeap[A] =
    Branch(x, List.empty)

  private def merge[A: Ordering](
      x: PairingHeap[A],
      y: PairingHeap[A]
  ): PairingHeap[A] =
    (x, y) match
      case (_, Leaf) => x
      case (Leaf, _) => y
      case (Branch(x1, subs1), Branch(x2, subs2)) =>
        if x1 < x2 then
          Branch(x1, Branch(x2, subs2) :: subs1)
        else
          Branch(x2, Branch(x1, subs1) :: subs2)

  @tailrec
  private def pairing[A: Ordering](subtrees: List[PairingHeap[A]])
      : PairingHeap[A] =
    subtrees match
      case Nil              => Leaf
      case hd :: Nil        => hd
      case h1 :: h2 :: tail => pairing(merge(h1, h2) :: tail)

end PairingHeap
```

### Understanding the Scala implementation

- **List of sub-heaps.** Each node `Branch(min, subtrees)` stores a value and a list of child heaps.
  Thanks to `List`, adding a child during a merge takes O(1) by prepending to the list head.
- **Merge.** The smaller of the two roots is chosen; the winner's root stays on top,
  and the losing heap is added to its list of sub-heaps. Complexity — O(1).
- **Pairing.** A recursive tail-recursive function processes the list: empty gives an empty heap,
  a single element is the finished result, and for two or more leading heaps the pair is merged,
  after which the result is placed at the head of the list and the process repeats.
  The `@tailrec` annotation guarantees that the recursion is optimized into a loop
  and will not overflow the stack even on large lists.

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