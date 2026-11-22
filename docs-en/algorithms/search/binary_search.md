# Binary Search

> [Binary search](https://en.wikipedia.org/wiki/Binary_search)
> (also known as **halving method** or **bisection**) is
> a classic algorithm for searching for an element in a sorted array (vector), using division of the array into halves.

**Algorithm and Explanation:**

In functional programming, halving a linked list presents a challenge.
To compute the length of a linked list, one must traverse all elements, which takes linear time.
Then one must find the middle element, which also takes linear time.
Altogether, if the list length is n, computing half of the list will take \\(\frac{3n}{2}\\) operations.

Let's try to compute half of the list in \\(\frac{n}{2}\\) operations.
We define a "race" algorithm that computes half of the list in \\(\frac{n}{2}\\) operations.

- **Step 1** (Initialization). We define two lists: `list` and `subList`, where the second is always a sublist of the first.
  The initial values: the original list and an empty list.
- **Step 2** (Determining the cut). We define two lists: `oneStep` and `twoStep`, where in the first list we remove one element at a time from `list`,
  and in the second list we remove two elements at a time from `list`. The initial values: the original list `list`.
- **Step 3** (Condition check). If `twoStep` or `twoStep` without its head equals `subList`, then we return `oneStep` as the half.
  Otherwise, we remove one element from `oneStep` and two from `twoStep`.

`oneStep` will contain exactly half of the elements between `list` and `subList`, because by the time `twoStep` equals `subList`,
`2k` elements will have been removed from it, standing between `list` and `subList`, so `k` elements will have been removed from `oneStep`, i.e., half.

We then use the standard binary search algorithm:

- **Step 1** (Base conditions). In the `search` method we "reduce" two lists: the original list and an empty list.
- **Step 2** (Finding the middle). We find `middle` — the middle of these two lists — a list containing half the elements of the original list, using the algorithm above.
- **Step 3** (Condition check). Comparison of the key with the head element of `middle`.
    - If the original key is less than the head element of `middle`, we continue searching in the left half ("reduce" the original list and `middle`).
    - If the original key is greater than the head element of `middle`, we continue searching in the right half ("reduce" `middle` without its head and an empty list).
    - If the original key equals the head element of `middle`, we return that element.
- **Step 4** (Termination). If during the "reduction" the two lists become equal, the sought element is not found.

**Example:**

Let's consider an example of computing binary search for the list `List(1, 3, 5, 7, 9)` and key `key = 3`.

Computation example:

- Initialization: `list = List(1, 3, 5, 7, 9), subList = List()`, `key = 3`
- Finding the half:
    - `oneStep = List(1, 3, 5, 7, 9), twoSteps = List(1, 3, 5, 7, 9) =>`
    - `oneStep = List(3, 5, 7, 9), twoSteps = List(5, 7, 9) =>`
    - `oneStep = List(5, 7, 9), twoSteps = List(9)`, recursion ends: `twoSteps` without its head equals `subList`
    - The half is `List(5, 7, 9)`
- Condition check: `3` is less than `5`, so we continue for lists `list = List(1, 3, 5, 7, 9), subList = List(5, 7, 9)`.
- Finding the half:
    - `oneStep = List(1, 3, 5, 7, 9), twoSteps = List(1, 3, 5, 7, 9)`
    - `oneStep = List(3, 5, 7, 9), twoSteps = List(5, 7, 9)`, recursion ends: `twoSteps` equals `subList`.
    - The half is `List(3, 5, 7, 9)`
- The original key equals the head element of the "half", so we return the answer: `3`.

**Estimation:**

- **Time**:
    - Worst - \\(O(\log n)\\)
    - Best - \\(O(1)\\)
    - Average - \\(O(\log n)\\)
- **Memory** - \\(O(1)\\)

**Code:**

```dotty
import scala.annotation.tailrec
import scala.math.Ordered.orderingToOrdered

def binarySearch[A: Ordering](list: List[A], key: A): Option[A] =
  @tailrec
  def search(lst: List[A], sub: List[A]): Option[A] =
    if lst == sub then None
    else
      middle(lst, sub) match
        case Nil => None
        case head :: next =>
          if key < head then search(lst, head :: next)
          else if key > head then search(next, sub)
          else Some(head)

  def middle(lst: List[A], sub: List[A]): List[A] =
    @tailrec
    def race(oneStep: List[A], twoSteps: List[A]): List[A] =
      if twoSteps.nonEmpty && twoSteps != sub && twoSteps.tail != sub then
        race(oneStep.drop(1), twoSteps.drop(2))
      else oneStep

    race(lst, lst)

  search(list, Nil)
end binarySearch

binarySearch((1 to 100).toList, 45)
// Some(45)
```

---

**Links:**

- [Source code][sample]
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Scalacaster - Search algorithms](https://github.com/vkostyukov/scalacaster/tree/master/src/search)
- [Algorithmica - Binary Search](https://ru.algorithmica.org/cs/interactive/binary-search/)
- [Algorithmica - Binary Search on Answer](https://ru.algorithmica.org/cs/interactive/answer-search/)

[sample]: https://gitverse.ru/scalabook/algorithms
