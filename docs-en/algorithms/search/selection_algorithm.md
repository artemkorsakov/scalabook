# Selection Algorithm

**Algorithm:**

[Linear (on average) algorithm for finding the k-th order statistic](https://en.wikipedia.org/wiki/Selection_algorithm)

An algorithm for finding the k-th order statistic, based on quicksort and running in O(n) on average.

The idea of the algorithm is that the array is divided into two parts relative to a randomly (equally likely)
selected element — elements smaller than the selected one go into one part,
and the rest into the other (this operation runs in O(n)),
upon its completion, the selected element is at position `j`.
If the first part contains exactly `k-1` elements (`j = k`), the selected element is the sought one.
If `j > k`, the algorithm runs recursively on the first part of the array,
otherwise — on the second part (in the latter case, subtract `j` from `k` for the next iteration).

**Estimation:**

- **Time**:
    - Worst - \\(O(n^{2})\\)
    - Best - \\(O(1)\\)
    - Average - \\(O(n)\\)
- **Memory** - \\(O(1)\\)

**Code:**

```dotty
def selectionSearch[A: Ordering](list: List[A], n: Int): Option[A] =
  @tailrec
  def partitionAndSearch(as: List[A], m: Int): Option[A] = as match
    case Nil => None
    case h :: t =>
      val (l, p, g) = partition(h, t)
      val len       = l.length
      if len == m - 1 then Some(p)
      else if len >= m then partitionAndSearch(l, m)
      else partitionAndSearch(g, m - len - 1)

  def partition(head: A, tail: List[A]): (List[A], A, List[A]) =
    @tailrec
    def loop(
        rest: List[A],
        less: List[A],
        p: A,
        more: List[A]
    ): (List[A], A, List[A]) =
      rest match
        case Nil             => (less, p, more)
        case h :: t if h < p => loop(t, h :: less, p, more)
        case h :: t          => loop(t, less, p, h :: more)

    loop(tail, Nil, head, Nil)

  if n <= 0 then None
  else partitionAndSearch(list, n)
end selectionSearch

selectionSearch((1 to 100).toList, 45)
// Some(45)
```

---

**Links:**

- [Source code][sample]
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Scalacaster - Search algorithms](https://github.com/vkostyukov/scalacaster/tree/master/src/search)

[sample]: https://gitverse.ru/scalabook/algorithms
