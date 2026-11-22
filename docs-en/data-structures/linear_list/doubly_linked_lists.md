# Doubly Linked Lists

_Doubly linked lists_ contain references not only to the next node, but also to the previous one.

In doubly linked lists, there are pointers to the beginning and end of the list — in this case, adding an element
to the end of the list has constant time.

A doubly linked list can be implemented as follows:

```dotty
enum DoublyList[+A]:
  case Nil
  case DoublyListCons(left: DoublyList[A], value: A, right: DoublyList[A])

  lazy val length: Int = this match
    case Nil                            => 0
    case DoublyListCons(left, _, right) => left.length + 1 + right.length

object DoublyList:
  def prepend[A](list: DoublyList[A], a: A): DoublyList[A] = list match
    case Nil => DoublyListCons(Nil, a, Nil)
    case DoublyListCons(left, value, right) =>
      DoublyListCons(prepend(left, a), value, right)

  def append[A](list: DoublyList[A], a: A): DoublyList[A] = list match
    case Nil => DoublyListCons(Nil, a, Nil)
    case DoublyListCons(left, value, right) =>
      DoublyListCons(left, value, append(right, a))

val list = DoublyListCons(DoublyListCons(Nil, 3, Nil), 2, DoublyListCons(Nil, 1, Nil))

list.length                 
// 3
DoublyList.prepend(list, 0)
// DoublyListCons(DoublyListCons(DoublyListCons(Nil,0,Nil),3,Nil),2,DoublyListCons(Nil,1,Nil))
DoublyList.append(list, 4)  
// DoublyListCons(DoublyListCons(Nil,3,Nil),2,DoublyListCons(Nil,1,DoublyListCons(Nil,4,Nil)))
```

---

**Links:**

- [Source code][source]
- [Donald E. Knuth - The Art of Computer Programming, section 2.2.5](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)

[source]: https://gitverse.ru/scalabook/algorithms
