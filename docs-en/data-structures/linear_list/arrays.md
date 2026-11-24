# Arrays

_An array_ is an ordered, indexed collection of related elements.

An array can be represented as a sequence: \\(a\_{0}, a\_{1}, ..., a\_{n}\\),
where \\(n\\) is a positive integer greater than \\(1\\). The lower subscripts are indices.
One instance of this general sequence is `<a,e,i,o,u>`. In this case, \\(n = 5\\).
We can access the third vowel using the corresponding index.
If we name this sequence as \\(V\\), then \\(V\_{3}\\) = `i` provided that indexing starts from \\(1\\).
We could use notations such as `V[3]`, `V(3)` or `Third(V)`.
If indexing starts from 0, then the representation is \\(V\_{2}\\) = `i`.

Different programming languages use different styles for representing arrays.
Reading and modifying an element at a given index i takes time _O(1)_.
Inserting an element at the i-th position requires `(n−i)` shifts and is therefore _O(n−i)_;
likewise, deleting an element at the i-th index takes _O(n−i)_ time.
Inserting an element into a full array requires creating a new array with greater capacity,
copying all elements from the old array, and inserting the new element.

```dotty
val vowels = Array('a', 'e', 'i', 'o', 'u')
// vowels: Array[Char] = Array('a', 'e', 'i', 'o', 'u')
vowels(2)
// res0: Char = 'i'
val vowelsReversed = vowels.reverse
// vowelsReversed: Array[Char] = Array('u', 'o', 'i', 'e', 'a')
vowelsReversed(2)
// res1: Char = 'i'
vowels.length
// res2: Int = 5
```

---

**Links:**

- [Source code][source]
- [Rod Stephens - Algorithms: Theory and Practical Applications. Chapter 4. Arrays](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854/)
- [Donald E. Knuth - The Art of Computer Programming, section 2.2.6](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Алгоритмика - Массивы и кортежи](https://ru.algorithmica.org/cs/basic-structures/array/)

[source]: https://gitverse.ru/scalabook/algorithms
