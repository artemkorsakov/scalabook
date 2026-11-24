# Heap

A [heap][heap wiki] is a specialized tree-based data structure that satisfies the heap property:
if B is a child node of node A, then key(A) ≥ key(B).
This implies that the element with the largest key is always the root node of the heap,
so such heaps are sometimes called _max-heaps_
(as an alternative, if the comparison is inverted,
then the smallest element will always be the root node; such heaps are called _min-heaps_).

## Why heaps are useful

Heaps solve the most in-demand problem of priority queues: maintaining a set of elements with the ability
to quickly obtain (and/or extract) the **extreme** element — the maximum or the minimum.
Unlike a plain queue, where order is determined by arrival time,
a priority queue always returns the element with the highest (or lowest) priority.

Such a structure underpins many algorithms and systems:

- **Sorting** — the sorting tree (see heapsort) sorts an array in O(n log n).
- **Greedy algorithms** — e.g., Dijkstra's shortest-path algorithm and Prim's minimum spanning tree algorithm rely directly on a min-heap.
- **Task schedulers** — operating systems and thread pools use priority queues to pick the next task.
- **Merging streams** — merging k sorted arrays into one is done in O(n log k).

## The heap property

Formally, the heap is a binary tree (or, more generally, a tree) for which every vertex satisfies
the **ordering invariant**: a vertex's key is not less than the keys of all its descendants (for a max-heap)
or not greater (for a min-heap).
By transitivity of the ordering relation this invariant guarantees that the greatest (smallest) key
is always in the root. Hence reading the extreme element takes O(1).

It is important to understand that the heap property does **not** impose a total order on elements:
trees containing the same set of keys can have different shapes. This is unlike search trees,
where the ordering strictly dictates the relative placement of keys and enables searching for an arbitrary element.
A heap cannot quickly search for an arbitrary element by value — its strength lies in fast access to the extreme.

## Standard Binary Heap

A [binary heap, or sorting tree][bin heap wiki] is a binary tree that satisfies three conditions:

- **Heap ordering.** The value at any vertex is not less (not greater for min-heaps) than the values of its descendants.
  This is the basic invariant described above.
- **Almost-complete filling.** The depth of all leaves (distance to the root) differs by at most 1 level,
  meaning the tree is always balanced and has height O(log n). It is this balance that gives
  logarithmic guarantees for insertion and deletion.
- **Sequential filling.** The last level is filled left to right without gaps.
  Together, the last two properties mean the tree can be represented as a dense array without holes,
  where the children of index `i` are at `2i + 1` and `2i + 2`.

### How insertion and deletion work

The **functional** Scala implementation below uses immutable data structures.
Each operation returns a new heap without modifying the original.
Thanks to structural sharing, nodes unaffected by an operation are reused between the old and new versions
of the tree, preserving efficiency even in a purely functional style.

The implementation additionally maintains `size` and `height` fields, which allow the "almost-complete"
invariant to be enforced with no visible holes and route insertion into whichever subtree
is not yet fully filled.

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
    - **Time** - O(n)
    - **Space** - O(n)

### Code

```dotty
enum BinaryHeap[+A]:
  import BinaryHeap.*

  case Leaf

  private case Branch(
      min: A,
      left: BinaryHeap[A],
      right: BinaryHeap[A],
      override val size: Int,
      override val height: Int
  )

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false

  val size: Int = this match
    case neh: Branch[A] => neh.size
    case _              => 0

  val height: Int = this match
    case neh: Branch[A] => neh.height
    case _              => 0

  lazy val minOption: Option[A] = this match
    case neh: Branch[A] => Some(neh.min)
    case _              => None

  def insert[B >: A: Ordering](x: B): BinaryHeap[B] = this match
    case Leaf => BinaryHeap(x)
    case Branch(min, left, right, size, height) =>
      if left.size < left.height * left.height - 1 then
        bubbleUp(min, left.insert(x), right)
      else if right.size < right.height * right.height - 1 then
        bubbleUp(min, left, right.insert(x))
      else if right.height < left.height then
        bubbleUp(min, left, right.insert(x))
      else
        bubbleUp(min, left.insert(x), right)

  def remove[B >: A: Ordering]: Option[BinaryHeap[B]] =
    def floatLeft(x: A, l: BinaryHeap[A], r: BinaryHeap[A]): BinaryHeap[A] =
      l match
        case Branch(y, lt, rt, _, _) => BinaryHeap(y, BinaryHeap(x, lt, rt), r)
        case _                       => BinaryHeap(x, l, r)

    def floatRight(x: A, l: BinaryHeap[A], r: BinaryHeap[A]): BinaryHeap[A] =
      r match
        case Branch(y, lt, rt, _, _) => BinaryHeap(y, l, BinaryHeap(x, lt, rt))
        case _                       => BinaryHeap(x, l, r)

    def mergeChildren(l: BinaryHeap[A], r: BinaryHeap[A]): BinaryHeap[A] =
      (l, r) match
        case (Leaf, Leaf) => Leaf
        case (Leaf, Branch(rmin, rleft, rright, rsize, rheight)) =>
          floatRight(rmin, l, mergeChildren(rleft, rright))
        case (Branch(lmin, lleft, lright, lsize, lheight), Leaf) =>
          floatLeft(lmin, mergeChildren(lleft, lright), r)
        case (
              Branch(lmin, lleft, lright, lsize, lheight),
              Branch(rmin, rleft, rright, rsize, rheight)
            ) =>
          if lsize < lheight * lheight - 1 then
            floatLeft(lmin, mergeChildren(lleft, lright), r)
          else if rsize < rheight * rheight - 1 then
            floatRight(rmin, l, mergeChildren(rleft, rright))
          else if rheight < lheight then
            floatLeft(lmin, mergeChildren(lleft, lright), r)
          else floatRight(rmin, l, mergeChildren(rleft, rright))

    def bubbleRootDown(h: BinaryHeap[A]): BinaryHeap[A] = h match
      case Branch(min, left, right, _, _) =>
        bubbleDown(min, left, right)
      case _ => Leaf

    this match
      case Branch(_, left, right, _, _) =>
        Some(bubbleRootDown(mergeChildren(left, right)))
      case _ => None
  end remove
end BinaryHeap

object BinaryHeap:
  def empty[A]: BinaryHeap[A] = Leaf

  def apply[A: Ordering](x: A): BinaryHeap[A] =
    BinaryHeap(x, Leaf, Leaf)

  private def apply[A: Ordering](
      x: A,
      l: BinaryHeap[A],
      r: BinaryHeap[A]
  ): BinaryHeap[A] =
    Branch(x, l, r, l.size + r.size + 1, math.max(l.height, r.height) + 1)

  private def bubbleUp[A: Ordering](
      x: A,
      l: BinaryHeap[A],
      r: BinaryHeap[A]
  ): BinaryHeap[A] = (l, r) match
    case (Branch(y, lt, rt, _, _), _) if x > y =>
      BinaryHeap(y, BinaryHeap(x, lt, rt), r)
    case (_, Branch(z, lt, rt, _, _)) if x > z =>
      BinaryHeap(z, l, BinaryHeap(x, lt, rt))
    case (_, _) => BinaryHeap(x, l, r)

  private def bubbleDown[A: Ordering](
      x: A,
      l: BinaryHeap[A],
      r: BinaryHeap[A]
  ): BinaryHeap[A] = (l, r) match
    case (Branch(y, _, _, _, _), Branch(z, lt, rt, _, _))
        if z < y && z < x =>
      BinaryHeap(z, l, bubbleDown(x, lt, rt))
    case (Branch(y, lt, rt, _, _), _) if x > y =>
      BinaryHeap(y, bubbleDown(x, lt, rt), r)
    case (_, _) => BinaryHeap(x, l, r)
end BinaryHeap
```

### Understanding the Scala implementation

Let us look at the key points of the code:

- **Recursive structure.** The heap is described by an algebraic data type: `Leaf` is an empty heap,
  `Branch(min, left, right, size, height)` is a non-empty node where `min` holds the minimum of the whole subtree.
  The `size` and `height` fields are cached in the node, so they are computed in O(1) without re-traversing.
- **Insertion.** The method finds a subtree that is not yet fully filled
  (`left.size < left.height * left.height - 1`) and recursively places the element there,
  then restores the invariant via `bubbleUp`.
  The condition using the square of the height checks whether the node has reached its maximum possible size
  for its height: in a complete tree of height `h` all levels are filled, so its size is close to `(h + 1)² - 1`.
- **Deletion.** The root is removed; its children are then combined via `mergeChildren`,
  after which the root is pushed downward through `bubbleDown` so the heap invariant is preserved.
- **Sifting.** `bubbleUp` and `bubbleDown` are the classic operations that restore the order:
  the first lifts an offending element toward the root, the second pushes it down, swapping
  with the larger of the children.

### Example

```dotty
val emptyBinaryHeap: BinaryHeap[Int]    = BinaryHeap.empty
val nonEmptyBinaryHeap: BinaryHeap[Int] = BinaryHeap.empty.insert(1).insert(2)
 
emptyBinaryHeap.isEmpty        // true
nonEmptyBinaryHeap.isEmpty     // false

emptyBinaryHeap.size           // 0
nonEmptyBinaryHeap.size        // 2
  
emptyBinaryHeap.height         // 0
nonEmptyBinaryHeap.height      // 2
  
emptyBinaryHeap.minOption      // None
nonEmptyBinaryHeap.minOption   // Some(1)

emptyBinaryHeap.insert(5)    
// Branch(5,Leaf,Leaf,1,1) 
nonEmptyBinaryHeap.insert(5)
// Branch(1,Branch(2,Leaf,Leaf,1,1),Branch(5,Leaf,Leaf,1,1),3,2)

emptyBinaryHeap.remove         
// None
nonEmptyBinaryHeap.remove
// Some(Branch(2,Leaf,Leaf,1,1))
```

The example clearly demonstrates the basic behavior: an empty heap reports `isEmpty = true`,
`minOption = None`, and deletion from it returns `None`. After inserting elements the root always
holds the minimum value, and removing the root again restores the invariant, leaving the next-smallest
value at the root.

## Heap variants

A plain binary heap handles insertion and extraction of the extreme in O(log n),
but its merge operation costs a whole O(n). Many algorithms need to efficiently merge two priority queues.
For this purpose, special heap variants were invented:

- **[Leftist heap]** — restores balance through a strict rank invariant,
  guaranteeing O(log n) per merge. Described in its own section.
- **[Skew heap](heap/skew_heap.md)** — a self-adjusting variant of the leftist heap that unconditionally
  swaps the children at every merge. Gives amortized O(log n).
- **[Pairing heap](heap/pairing_heap.md)** — a multi-way tree, a simplified Fibonacci heap.
  In practice shows excellent performance with a very simple implementation.

## Summary

The heap is one of the fundamental data structures for working with priority queues.
It provides O(1) for reading the extreme and O(log n) for insertion and deletion,
while being implementable in just a few dozen lines. Choosing a specific heap variant comes down to
which operations are critical for the task: if you need to merge heaps often —
choose a leftist, skew, or pairing heap.

---

**Links:**

- [Source code][source]
- [Heap - Wiki][heap wiki]
- [Binary heap - Wiki][bin heap wiki]
- [Leftist heap][leftist heap]
- [Pairing heap - Wiki][pairing heap wiki]
- [Skew heap - Wiki][skew heap wiki]
- [Алгоритмика - Куча](https://ru.algorithmica.org/cs/basic-structures/heap/)
- [Heap - Scalacaster](https://github.com/vkostyukov/scalacaster/blob/master/src/heap)
- [A Functional Approach to Standard Binary Heaps - Vladimir Kostyukov](http://arxiv.org/pdf/1312.4666v1)

[heap wiki]: https://en.wikipedia.org/wiki/Heap_(data_structure)
[bin heap wiki]: https://en.wikipedia.org/wiki/Binary_heap
[leftist heap]: heap/leftist_heap.md
[pairing heap wiki]: https://en.wikipedia.org/wiki/Pairing_heap
[skew heap wiki]: https://en.wikipedia.org/wiki/Skew_heap
[source]: https://gitverse.ru/scalabook/algorithms