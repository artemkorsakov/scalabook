# Linear List

A linear list is a sequence of \\(n \geq 0\\) nodes \\(X\_{1}, X\_{2}, ... , X\_{n}\\),
whose key structural feature is such an arrangement of list elements relative to each other
as if they were placed on a single line.
In other words, the following condition must be met in such a structure:
if \\(n > 0\\) and \\(X\_{1}\\) is the first node and \\(X\_{n}\\) is the last,
then the k-th node \\(X\_{k}\\) follows \\(X\_{k − 1}\\) and precedes node \\(X\_{k + 1}\\) for all \\(1 < k < n\\).

The following operations can be performed on linear lists.

1. Accessing the k-th node of the list to check and/or modify the contents of its fields.
2. Inserting a new node immediately after or before the k-th node.
3. Deleting the k-th node.
4. Merging two (or more) linear lists into a single list.
5. Splitting a linear list into two (or more) lists.
6. Creating a copy of a linear list.
7. Determining the number of nodes in the list.
8. Sorting nodes in ascending order of values in specific fields of these nodes.
9. Searching for a node with a given value in a particular field.

---

**Links:**

- [Source code][source]
- [Cats: Chain](https://typelevel.org/cats/datatypes/chain.html)
- [Cats: NonEmptyList](https://typelevel.org/cats/datatypes/nel.html)
- [Cats: OneAnd](https://typelevel.org/cats/datatypes/oneand.html)
- [Donald E. Knuth - The Art of Computer Programming, section 2.2.1](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)

[source]: https://gitverse.ru/scalabook/algorithms
