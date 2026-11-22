# Linear Search

> [Linear search](https://en.wikipedia.org/wiki/Linear_search), also known as sequential search —
> an algorithm for finding a given value of an arbitrary function on some interval.

**Algorithm:**

- **Step 1** (Start). Input data — a list `list` and a `key` to find.
- **Step 2** (Check for emptiness). If the list `list` is empty, return `None`, as the element is not found.
- **Step 3** (Comparison with the first element). If the first element of the list equals the sought key, return `Some(key)` — this means the element has been found.
- **Step 4** (Recursive call) If the first element does not equal the key, recursively call the function with the rest of the list.
- **Step 5** (Termination). The process repeats until an element is found or the end of the list is reached.

**Example:**

Consider an example where we have a list of numbers and we are looking for the number `3`:

- First call: `list = List(1, 2, 3, 4, 5), key = 3`. The list is not empty, the first element `1` does not equal `3`.
- Second call: `list = List(2, 3, 4, 5), key = 3`. The list is not empty, the first element `2` does not equal `3`.
- Third call: `list = List(3, 4, 5), key = 3`. The list is not empty, the first element `3` equals `3`.
- Return `Some(3)`.

Thus, the result of the function execution will be `Some(3)`, meaning the element has been found.
If we were looking for, say, the number `6`, the function would continue calling itself until it reached an empty list and would ultimately return `None`.

**Estimation:**

- **Time**:
    - Worst - \\(O(n)\\)
    - Best - \\(O(1)\\)
    - Average - \\(O(n)\\)
- **Memory** - \\(O(1)\\)

**Code:**

```dotty
@tailrec
def linearSearch[A](list: List[A], key: A): Option[A] =
  list match
    case Nil                      => None
    case head :: _ if head == key => Some(key)
    case _ :: tail                => linearSearch(tail, key)

val text   = "This is a functional implementation."
linearSearch(text.toList, 'a')
// Some(a)
```

---

**Links:**

- [Source code][sample]
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Scalacaster - Search algorithms](https://github.com/vkostyukov/scalacaster/tree/master/src/search)

[sample]: https://gitverse.ru/scalabook/algorithms
