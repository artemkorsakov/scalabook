# Trees

Formally, a _tree_ is defined as a finite set \\(T\\) of one or more nodes with the following properties:

a) there is one designated node, namely the _root_ of the given tree \\(T\\);

b) the remaining nodes (except the root) are distributed among \\(m \geq 0\\) disjoint sets
\\(T\_{1},... , T\_{m}\\), and each of these sets is itself a tree;
the trees \\(T\_{1},... , T\_{m}\\) are called _subtrees_ of this root.

From this definition it follows that each node in a tree is the root of some subtree of the given tree.
The number of subtrees of a node is called the _degree_ of that node.
A node with zero degree is called a _terminal node_ or a _leaf_.
A non-terminal node is called a _branch node_.
The _level_ of a node with respect to a tree T is defined recursively as follows.
The level of the root of tree T is zero, and the level of any other node is one greater
than the level of the root of the nearest subtree of tree T that contains the given node.

If in item (b) of the above definition the relative order of subtrees \\(T\_{1},... , T\_{m}\\) matters,
then the tree is an _ordered tree_.
If in an ordered tree \\(m \geq 2\\), it makes sense to call subtree \\(T\_{2}\\) the second subtree of this root, and so on.
Ordered trees are sometimes also called _plane trees_,
since in their ordering the way the tree is laid out on a plane matters.
If we do not distinguish between two trees that differ only in the relative order of subtrees of nodes,
then the tree is called _unordered_ (or _unordered tree_),
since only the relative orientation of nodes matters, not their order.

A _forest_ is a set (usually ordered) that contains either no disjoint trees
or contains several disjoint trees.
Then another formulation of item (b) in the above tree definition could read:
_nodes of a tree, excluding the root, form a forest_.

Standard terminology for tree structures comes from genealogical "family tree" diagrams (a person's descendants).
Each node is called a _parent_ of the roots of its subtrees,
and the roots themselves are called _siblings_ or _children_ of their parent.
The root of the entire tree has no parent.

To denote kinship that may span several levels of a tree,
the terms _"ancestor"_ and _"descendant"_ will be used.

> One of the advantages of tree-based information representation is the convenience of analysis.
> When we place information in a tree structure, certain relationships can be seen much faster
> than in any text representation.
> In addition, trees possess certain properties that allow for efficient synthesis of information.

---

**Links:**

- [Source code][source]
- [Donald E. Knuth - The Art of Computer Programming, section 2.3](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Scalacaster - trees](https://github.com/vkostyukov/scalacaster/tree/master/src/tree)

[source]: https://gitverse.ru/scalabook/algorithms
