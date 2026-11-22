# Hash Tables

A data structure that uses a method of mapping large ranges to smaller ones is called a hash table.
It stores keys and their values, using a hash function for mapping.
A hash table is one of the most efficient data structures with _O(1)_ runtime for search, insertion, and deletion.
In the worst case, especially when linked lists are stored as values, the runtime can be _O(n)_.

From a structural perspective, a hash table is more similar to an array; however, the way of accessing elements differs.
In an array, elements are addressed using direct addressing.
With direct addressing, access to an element at position k is done using the index value k.
In hash tables, keys are computed using a hash function.
The essence of a hash table lies in the ability to design a hash function
that effectively maps a real-world range to a range available (or allocated) in a computer.
A good hash function can distribute real-world keys evenly in computer memory without any collisions.
A collision occurs when two or more real-world keys are mapped to one hash table key.
When keys from the problem space have a very large range,
it may be impossible to have unique corresponding keys in the hash table.
In such scenarios, linked lists are used to provide additional identification.
In addition to the key, values of objects can be compared.
In such implementations, in the worst case, searching for an element in a hash table may take _O(n)_ time.
This is primarily because linked lists are involved.


---

**Links:**

- [Source code][source]
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)

[source]: https://gitverse.ru/scalabook/algorithms
