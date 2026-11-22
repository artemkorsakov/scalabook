# Arrangement

_An _arrangement_ (or permutation of k elements) is an ordered set of `k` elements chosen from `n` distinct elements, where the order of selection matters.

**Formal definition:**

Let there be a set of `n` distinct elements. An **arrangement of `n` elements taken `k` at a time** is any ordered set of `k` elements selected from the given set.

**Important:**

- The order of elements in an arrangement matters.
- The same element cannot be repeated in an arrangement (arrangements without repetition).

**Examples:**

- **Arrangements of 3 elements taken 2 at a time:**
    - Set: `{A, B, C}`
    - Arrangements: `{AB, BA, AC, CA, BC, CB}`
- **Arrangements of 4 elements taken 3 at a time:**
    - Set: `{1, 2, 3, 4}`
    - Arrangements: `{123, 124, 132, 134, 142, 143, 213, 214, 231, 234, 241, 243,`
                  `312, 314, 321, 324, 341, 342, 412, 413, 421, 423, 431, 432}`

**Formula for counting the number of arrangements:**

The number of arrangements of `n` elements taken `k` at a time is denoted `A(n, k)` and is calculated by the formula:

\\(A(n, k) = \frac{n!}{(n - k)!}\\)

**Example calculation:**

- **Number of arrangements of 5 elements taken 3 at a time:**

\\(A(5, 3) = \frac{5!}{(5 - 3)!} = \frac{5 \cdot 4 \cdot 3 \cdot 2 \cdot 1}{2 \cdot 1} = 5 \cdot 4 \cdot 3 = 60\\)

### Arrangements of a given length

> Given a collection of elements. Generate all arrangements of a given length.
>
> Taken from [the article "Combinatorial Algorithms in Scala"][kostyukov]

**Algorithm:**

- **Step 1** (Check length).
    - If `n` is greater than the list length, return an empty list.
    - If the list is empty, return an empty list.
    - If `n` equals 1, return a list consisting of sublists, each containing one element from the original list.
- **Step 2** (Recursive case).
    - Split the list into head (`hd`) and tail (`tl`).
    - Compute all arrangements containing the head.
        - Recursively call the algorithm on the tail `tl` to obtain all arrangements of length `n - 1`.
        - Use `hd` to create new arrangements by inserting it into each variation from the previous step via mixing (**Step 3**).
    - Compute all arrangements not containing the head.
        - Recursively call the algorithm on the tail `tl` to obtain all arrangements of length `n`.
    - Combine arrangements containing the head element with arrangements not containing the head element.
- **Step 3** (Mixing): inserting a given element into a given list of arrangements at all possible positions.
    - If the list of arrangements is empty, return an empty list — no arrangements exist.
    - Split the list of arrangements into head (`hdA`) and tail (`tlA`).
        - "Fold" the given element into the arrangement `hdA` (**Step 4**).
        - Compute the list of arrangements resulting from mixing (**Step 3**) the given element with `tlA`.
        - Combine the arrangements obtained in the previous two steps.
- **Step 4** (Fold):
    - Insert the given element at position `i` in the given arrangement, where `i` can be any value from `0` to the length of the arrangement.

**Example:**

Consider the example with `list = List(1, 2)` and `n = 2`:

- Check: `n` is not greater than the list length.
- Split: `hd = 1`, `tl = List(2)`.
- Recursive call:
    - the algorithm for `List(2)` — a list of one element — returns `List(List(2))`.
- Mixing:
    - For arrangement `List(2)`:
    - Insert `1` at position `0`: `List(1, 2)`.
    - Insert `1` at position `1`: `List(2, 1)`.
- Result: `List(List(1, 2), List(2, 1))`.
- Recursive call for the tail `tl` and length `2`:
    - the algorithm for `List(2)` returns an empty list, since `n = 2` is greater than the list length.
- Combining results:
    - Combine `List(List(1, 2), List(2, 1))` with the empty list, resulting in `List(List(1, 2), List(2, 1))`.

**Estimation:**

- **Time** - \\(A\_{n}^{k}\\)
- **Memory** - \\(A\_{n}^{k}\\)

**Code:**

```dotty
extension [A](list: List[A])
  def xvariations(n: Int): List[List[A]] =
    def mixmany(a: A, lists: List[List[A]]): List[List[A]] =
      lists match
        case Nil      => Nil
        case hd :: tl => foldone(a, hd) ::: mixmany(a, tl)

    def foldone(a: A, lists: List[A]): List[List[A]] =
      (1 to lists.length).foldLeft(List(a :: lists)): (acc, i) =>
        mixone(i, a, lists) :: acc

    def mixone(i: Int, a: A, lists: List[A]): List[A] =
      lists.slice(0, i) ::: (a :: lists.slice(i, lists.length))

    if n > list.length then Nil
    else
      list match
        case Nil         => Nil
        case _ if n == 1 => list.map(List(_))
        case hd :: tl =>
          mixmany(hd, tl.xvariations(n - 1)) ::: tl.xvariations(n)
  end xvariations
end extension

List("a", "b", "c").xvariations(2)
// List(List("b", "a"), List("a", "b"),
//      List("c", "a"), List("a", "c"),
//      List("c", "b"), List("b", "c"))
```

### All arrangements

> Given a collection of elements. Generate all possible arrangements from this collection of all possible lengths.
>
> The total number of arrangements in a set of length n can be calculated as follows:
>
> \\(P\_{n}^{} = A\_{n}^{n} = n!\\)
>
> Taken from [the article "Combinatorial Algorithms in Scala"][kostyukov]

**Algorithm:**

- **Step 1**. Iterate `i` over all possible arrangement lengths from `1` to `n`.
- **Step 2**. For each length `i`, use the algorithm for generating arrangements of a given length.

**Estimation:**

- **Time** - \\(O(n!)\\)
- **Memory** - \\(O(n!)\\)

**Code:**

```dotty
def xpermutations: List[List[A]] = xvariations(l.length)

List("a", "b", "c").xpermutations
// List(
//   List("c", "b", "a"),
//   List("c", "a", "b"),
//   List("a", "c", "b"),
//   List("b", "c", "a"),
//   List("b", "a", "c"),
//   List("a", "b", "c")
// )
```

---

**Links:**

- [Source code][sample]
- [Combinatorial Algorithms in Scala - kostyukov.net][kostyukov]

[kostyukov]: https://kostyukov.net/posts/combinatorial-algorithms-in-scala/
[sample]: https://gitverse.ru/scalabook/algorithms
