# Permutation

_A _permutation_ of `n` objects is a way of sequentially arranging `n` distinct objects in order.
For example, for three objects \\({a, b, c}\\) there are six permutations: \\(abc, acb, bac, bca, cab, cba\\).

Denoting by \\(p\_{nk}\\) the number of ways to choose \\(k\\) objects from \\(n\\) taking order into account,
we get \\(p\_{nk} = n(n − 1)...(n − k + 1)\\).
It follows from this that the total number of permutations is expressed by the formula \\(p\_{nn} = n(n − 1) ... (1) = n!\\).

## Constructing permutations

There are two main methods for transitioning from permutations of n − 1 objects to permutations of n objects.

#### Method 1

**Algorithm:**

For each permutation \\(a\_{1} a\_{2} ... a\_{n−1}\\) of the objects \\({1, 2,...,n−1}\\), construct n more permutations
by placing the number n in every possible position, yielding
\\(n a\_{1} a\_{2} ... a\_{n−1},\\) \\(a\_{1} n a\_{2} ... a\_{n−1},\\) ..., \\(a\_{1} a\_{2} ... n a\_{n−1}, a\_{1} a\_{2} ... a\_{n−1} n\\).

Clearly, these are all possible permutations of n objects, and none of them repeats.

**Example:**

Consider the case `(n = 3)`. We have the set `({1, 2})` and all its permutations: `(1, 2)`, `(2, 1)`.

Now insert the number `3` into every possible position in these permutations:

- For permutation `(1, 2)`: `(3, 1, 2)`, `(1, 3, 2)`, `(1, 2, 3)`.
- For permutation `(2, 1)`: `(3, 2, 1)`, `(2, 3, 1)`, `(2, 1, 3)`.

Thus, all possible permutations of `({1, 2, 3})` are:
`(1, 2, 3)`, `(1, 3, 2)`, `(2, 1, 3)`, `(2, 3, 1)`, `(3, 1, 2)`, `(3, 2, 1)`.

**Estimation:**

For generating permutations, the algorithm works as follows:

- First, generate all permutations of `(n - 1)` objects, which requires `(n - 1)!` operations.
- Then, for each of these permutations, insert the number `n` in `n` different positions, which requires `(O(n))` operations per permutation.
- Thus, the total number of operations is: \\((n-1)! \cdot n = n!\\)
- Therefore, the asymptotic complexity of the algorithm is O(n!).

- **Time** - \\(O(n!)\\)
- **Memory** - \\(O(n^2)\\)

**Code:**

```dotty
def constructByMethod1[A](source: Vector[A], a: A): Vector[Vector[A]] =
  (0 to source.length).map { i =>
    val (before, after) = source.splitAt(i)
    before ++ Vector(a) ++ after
  }.toVector

constructByMethod1(Vector(2, 3, 1), 4)
// Vector(Vector(4, 2, 3, 1), Vector(2, 4, 3, 1), Vector(2, 3, 4, 1), Vector(2, 3, 1, 4))
```

#### Method 2.

**Algorithm:**

For each permutation \\(a\_{1} a\_{2} ... a\_{n−1}\\) of the objects \\({1, 2,...,n−1}\\),
construct yet another n permutations as follows.
First, construct the set
\\(a\_{1} a\_{2} ... a\_{n−1}\frac{1}{2}\\),
\\(a\_{1} a\_{2} ... a\_{n−1}\frac{3}{2}\\),
...
\\(a\_{1} a\_{2} ... a\_{n−1}\frac{n-1}{2}\\).
Then replace the elements of each permutation with the digits \\({1, 2,...,n}\\), _preserving the order_.
For example, for the permutation \\(2 3 1\\) the set equals
\\(231\frac{1}{2}\\),
\\(231\frac{3}{2}\\),
\\(231\frac{5}{2}\\),
\\(231\frac{7}{2}\\)
and after replacement we get \\(3421, 3412, 2413, 2314\\).

There is another way to describe this process.
Take a permutation \\(a\_{1}a\_{2}...a\_{n−1}\\) and a number \\(k, 1 \leq k \leq n\\);
add 1 to every \\(a\_{j}\\) whose value is \\(\geq k\\).
As a result, we get a permutation \\(b\_{1}b\_{2}...b\_{n−1}\\)
of the elements \\({1, ..., k − 1, k + 1, ..., n}\\);
thus, \\(b\_{1}b\_{2}...b\_{n−1}k\\) is a permutation of the numbers \\({1,...,n}\\).
With this construction method it is also obvious that each permutation of \\(n\\) elements appears exactly once.
Similar constructions can be done by placing \\(k\\) not on the right, but on the left, or at any other fixed position.

**Example:**

To illustrate the algorithm, let us consider an example with \\(n = 4\\).
In this case, we have permutations of \\(1, 2, 3\\),
which we will use to generate permutations of \\(1, 2, 3, 4\\).

Permutations of \\({1, 2, 3}\\):
\\((1, 2, 3), (1, 3, 2), (2, 1, 3), (2, 3, 1), (3, 1, 2), (3, 2, 1)\\).

Generating new permutations: For each permutation \\(a\_{1} a\_{2} a\_{3}\\), create 4 new permutations by adding \\(\frac{1}{2}\\), \\(\frac{3}{2}\\), \\(\frac{5}{2}\\) and \\(\frac{7}{2}\\) at the end.

For \\(1, 2, 3\\):

- \\(1, 2, 3, \frac{1}{2}\\) → \\(1, 2, 3, 4\\)
- \\(1, 2, 3, \frac{3}{2}\\) → \\(1, 2, 3, 4\\)
- \\(1, 2, 3, \frac{5}{2}\\) → \\(1, 2, 3, 4\\)
- \\(1, 2, 3, \frac{7}{2}\\) → \\(1, 2, 3, 4\\)

For \\(1, 3, 2\\):

- \\(1, 3, 2, \frac{1}{2}\\) → \\(1, 3, 2, 4\\)
- \\(1, 3, 2, \frac{3}{2}\\) → \\(1, 3, 2, 4\\)
- \\(1, 3, 2, \frac{5}{2}\\) → \\(1, 3, 2, 4\\)
- \\(1, 3, 2, \frac{7}{2}\\) → \\(1, 3, 2, 4\\)

Similarly for the remaining permutations.

Result: After replacing elements, we get all permutations of \\(1, 2, 3, 4\\):

\\(1234\\),
\\(1243\\),
\\(1324\\),
\\(1342\\),
\\(1423\\),
\\(1432\\),
\\(2134\\),
\\(2143\\),
\\(2314\\),
\\(2341\\),
\\(2413\\),
\\(2431\\),
\\(3124\\),
\\(3142\\),
\\(3214\\),
\\(3241\\),
\\(3412\\),
\\(3421\\),
\\(4123\\),
\\(4132\\),
\\(4213\\),
\\(4231\\),
\\(4312\\),
\\(4321\\)

**Estimation:**

- **Time** - \\(O(n!)\\)
- **Memory** - \\(O(n^2)\\)

**Code:**

```dotty
def constructByMethod2(source: Vector[Int]): Vector[Vector[Int]] =
  (1 to source.length + 1).map { k =>
    source.map { v => if v >= k then v + 1 else v } :+ k
  }.toVector

constructByMethod2(Vector(2, 3, 1))
// Vector(Vector(3, 4, 2, 1), Vector(3, 4, 1, 2), Vector(2, 4, 1, 3), Vector(2, 3, 1, 4))
```

---

**Links:**

- [Source code][sample]
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.5](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [The Art of Computer Programming - Donald E. Knuth, section 1.2.11.2](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)

[sample]: https://gitverse.ru/scalabook/algorithms
