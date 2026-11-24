# Binary Tree

[A binary tree](https://en.wikipedia.org/wiki/Binary_tree) is
a special form of tree that has at most two children.
Information represented as binary trees is much easier to process,
as binary trees possess certain properties.

## Structure

Formally, a binary tree can be represented as a triple `T = (x, L, R)`,
where `x` represents a node, and `L` and `R` are the left and right branches respectively.
`L` and `R` are disjoint binary trees and do not contain `x`.
The term "binary" comes from the two children,
i.e. each node in a binary tree can have at most two children.
This is also called the degree of the binary tree, which is 2.

For a binary tree to be full (a full binary tree), it must satisfy two conditions:

1. All leaves are at the same level and
2. Every internal node has two children

Several important definitions related to the structure of binary trees or trees in general:

- _path_: defined as a sequence of nodes (\\(x\_{0}, x\_{1}, x\_{2}, ... , x\_{n}\\)),
  where nodes with adjacent subscripts are adjacent nodes.
  Since trees are acyclic, a path cannot contain the same node more than once.
- _path length_: defined as the number \\(n\\) of adjacent pairs.
- _root path_: for a node \\(x\_{0}\\), its root path is defined as the path (\\(x\_{0}, x\_{1}, x\_{2}, ... , x\_{n}\\)) where \\(x\_{n}\\) is the root of the tree.
- _depth_: defined as the length of the root path
- _height_: defined as the greatest depth among all its nodes
- _level_: this is the set of all nodes at a given depth
- _size_: defined as the number of non-terminal nodes

**Code:**

```dotty
enum BinaryTree[+A]:
  case Leaf
  case Branch(value: A, left: BinaryTree[A], right: BinaryTree[A])

  lazy val isEmpty: Boolean = this match
    case Leaf => true
    case _    => false
    
  lazy val size: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + l.size + r.size

  lazy val height: Int = this match
    case Leaf            => 0
    case Branch(_, l, r) => 1 + math.max(l.height, r.height)
    
  def rootPath[B >: A](b: B): Option[NonEmptyList[B]] = this match
    case Leaf                              => None
    case Branch(value, _, _) if value == b => Some(NonEmptyList.one(b))
    case Branch(value, left, right) =>
      left.rootPath(b).orElse(right.rootPath(b)).map: nel =>
        value :: nel
  
  def level(level: Int): List[A] =
    @tailrec
    def loop(level: Int, trees: List[BinaryTree[A]]): List[A] =
      if level < 0 then List.empty
      else if level == 0 then
        trees.foldLeft(List.empty[A]) { case (acc, tree) =>
          tree match
            case Leaf            => acc
            case Branch(v, _, _) => v :: acc
        }
      else
        loop(
          level - 1,
          trees.flatMap:
            case Branch(_, left, right) if !left.isEmpty && !right.isEmpty =>
              List(left, right)
            case Branch(_, left, _) if !left.isEmpty   => List(left)
            case Branch(_, _, right) if !right.isEmpty => List(right)
            case _ => List.empty[BinaryTree[A]]
        )

    loop(level, List(this))
  end level      
```

**Example:**

```dotty
val exampleBinaryTree: BinaryTree[Char] = Branch(
  'A',
  Branch('B', Branch('D', Leaf, Leaf), Leaf),
  Branch(
    'C',
    Branch('E', Leaf, Branch('G', Leaf, Leaf)),
    Branch('F', Branch('H', Leaf, Leaf), Branch('J', Leaf, Leaf))
  )
)

exampleBinaryTree.size          // 9
exampleBinaryTree.height        // 4
exampleBinaryTree.rootPath('G') // Some(NonEmptyList(A, C, E, G))
exampleBinaryTree.level(3)      // List(J, H, G)
```

## Tree construction

**Algorithm:**

In the `BinaryTree` object, the `apply` methods
create binary trees from various types of data collections:

- The `apply` method with one argument returns a tree node with that value and two empty subtrees.
- The `apply` method that takes a collection builds a binary tree from the given values.
  If the collection is empty, an empty leaf is returned.
  Otherwise, the collection without the first element is divided in half,
  a node is created with the first element and two subtrees, created from the left and right split parts.

**Code:**

```dotty
object BinaryTree:
  def apply[A](a: A): BinaryTree[A] = Branch(a, Leaf, Leaf)

  def apply[A](values: List[A]): BinaryTree[A] =
    values match
      case Nil => Leaf
      case x :: xs =>
        val (left, right) = xs.splitAt(xs.length / 2)
        Branch(x, BinaryTree(left), BinaryTree(right))

  def apply[A](values: Vector[A]): BinaryTree[A] =
    if values.isEmpty then Leaf
    else
      val (left, right) = values.tail.splitAt(values.length / 2)
      Branch(values.head, BinaryTree(left), BinaryTree(right))
```

## Tree traversal algorithms

There are quite a few algorithms for working with tree structures,
in which the most common concept is _traversing_ a tree or _walking_ through it.
With this method of exploring a tree, each node is visited exactly once,
and a complete traversal of the tree defines a linear ordering of nodes, which simplifies the algorithm,
since in this case one can use the concept of "next" node,
i.e. a node that is located before or after the given node in such an ordering.

To traverse a binary tree, one of three fundamentally different methods can be used:
_preorder_, _inorder_, or _postorder_.

### Preorder

1. Visit the root
2. Perform a preorder traversal of the left subtree, if it is not empty
3. Perform a preorder traversal of the right subtree, if it is not empty

### Inorder

1. Perform an inorder traversal of the left subtree, if it is not empty
2. Visit the root
3. Perform an inorder traversal of the right subtree, if it is not empty

### Postorder

1. Perform a postorder traversal of the left subtree, if it is not empty
2. Perform a postorder traversal of the right subtree, if it is not empty
3. Visit the root

**Code:**

```dotty
lazy val preorder: IndexedSeq[A] = this match
  case Leaf                       => IndexedSeq.empty
  case Branch(value, left, right) => IndexedSeq(value) ++ left.preorder ++ right.preorder

lazy val inorder: IndexedSeq[A] = this match
  case Leaf                       => IndexedSeq.empty
  case Branch(value, left, right) => left.inorder ++ IndexedSeq(value) ++ right.inorder

lazy val postorder: IndexedSeq[A] = this match
  case Leaf                       => IndexedSeq.empty
  case Branch(value, left, right) => left.postorder ++ right.postorder ++ IndexedSeq(value)
```

**Example:**

If we apply these definitions to the binary tree
`('A', ('B', ('D', -, -), -), ('C', ('E', -, ('G', -, -)), ('F', ('H', -, -), ('J', -, -))))`,
then in preorder traversal we get the sequence `A B D C E G F H J`.
(first comes the root `A`, then the left subtree in preorder, and finally the right subtree in preorder.)

In inorder traversal, the root is visited after traversing the nodes of one of the trees,
just as if the nodes were "projected" onto a horizontal line to form the sequence `D B A E G C H F J`.

Similarly, postorder traversal yields the sequence `D B G E H J F C A`.

```dotty
val binaryTree: BinaryTree[Int] = Branch(
  'A',
  Branch('B', Branch('D', Leaf, Leaf), Leaf),
  Branch(
    'C',
    Branch('E', Leaf, Branch('G', Leaf, Leaf)),
    Branch('F', Branch('H', Leaf, Leaf), Branch('J', Leaf, Leaf))
  )
)

binaryTree.preorder
// Vector('A', 'B', 'D', 'C', 'E', 'G', 'F', 'H', 'J')

binaryTree.inorder
// Vector('D', 'B', 'A', 'E', 'G', 'C', 'H', 'F', 'J')

binaryTree.postorder
// Vector('D', 'B', 'G', 'E', 'H', 'J', 'F', 'C', 'A')
```

## Similarity and Equivalence

Let \\(u\_{1}, u\_{2}, ... , u\_{n}\\) and \\(\acute{u}\_{1}, \acute{u}\_{2}, ... , \acute{u}\_{\acute{n}}\\)
be nodes of binary trees \\(T\\) and \\(\acute{T}\\) respectively in preorder traversal.

For any node u, let

- \\(l(u)=1\\) if \\(u\\) has a non-empty left subtree, otherwise \\(l(u) = 0\\);
- \\(r(u)=1\\) if \\(u\\) has a non-empty right subtree, otherwise \\(r(u) = 0\\);

\\(T\\) and \\(\acute{T}\\) are **similar** if and only if \\(n = \acute{n}\\) and
\\(l(u\_{j}) = l(\acute{u}\_{j})\\), \\(r(u\_{j}) = r(\acute{u}\_{j})\\) for \\(1 \leq j \leq n\\).

Furthermore, similar trees \\(T\\) and \\(\acute{T}\\) are **equivalent** if and only if
\\(info(u\_{j}) = info(\acute{u}\_{j})\\) for \\(1 \leq j \leq n\\).

**Code:**

```dotty
def areSimilar[A, B](treeA: BinaryTree[A], treeB: BinaryTree[B]): Boolean =
  (treeA, treeB) match
    case (Leaf, Leaf) => true
    case (Branch(_, leftA, rightA), Branch(_, leftB, rightB)) =>
      areSimilar(leftA, leftB) && areSimilar(rightA, rightB)
    case _ => false

def areEqual[A](treeA: BinaryTree[A], treeB: BinaryTree[A]): Boolean =
  (treeA, treeB) match
    case (Leaf, Leaf) => true
    case (Branch(valueA, leftA, rightA), Branch(valueB, leftB, rightB)) =>
      valueA == valueB && areEqual(leftA, leftB) && areEqual(rightA, rightB)
    case _ => false
```

## Natural Correspondence

Let \\(F = (T\_{1}, T\_{2}, ... , T\_{n})\\) be some forest of trees.
Then the binary tree \\(B(F)\\) corresponding to \\(F\\) can be strictly defined as follows.

- a) If \\(n = 0\\), then \\(B(F)\\) is empty.
- b) If \\(n > 0\\), then the root of \\(B(F)\\) is the root of \\((T\_{1})\\);
  \\(B(T\_{11}, T\_{12}, ... , T\_{1m})\\) is the left subtree of \\(B(F)\\),
  where \\(T\_{11}, T\_{12}, ... , T\_{1m}\\) are the subtrees of root \\((T\_{1})\\);
  \\(B(T\_{2}, ... , T\_{n})\\) is the right subtree of \\(B(F)\\).

This mapping is called _natural correspondence_ between a forest and binary trees.

## Applications

Binary trees have many applications.

One specialization of binary trees is binary search trees, which accelerate search applications
by supporting search, insertion, and deletion with average time complexity _O(lg n)_.

Binary trees are also used in compilers to process expressions using expression trees.
They can also be used for data compression, such as Huffman coding trees.

---

**Links:**

- [Source code][source]
- [Donald E. Knuth - The Art of Computer Programming, section 2.3.1](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Алгоритмика - Деревья поиска](https://ru.algorithmica.org/cs/tree-structures/)

[source]: https://gitverse.ru/scalabook/algorithms
