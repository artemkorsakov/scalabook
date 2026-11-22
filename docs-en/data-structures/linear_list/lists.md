# Linked Lists

_Linked lists_ are implemented in Scala as the `List` class.

Singly linked lists are used when you need to efficiently add an element
at the beginning of a list, as well as separate a list into a head element and a "tail" — the time complexity of these
operations is constant. The complexity of most other operations is _O(N)_, including computing the length of the list.

To compute the length of a list or add an element to the end of the list, you must traverse the entire list.
The time complexity of adding or removing an element at a given index can be up to _O(N)_ when the index
is close to _N_.

## List Definition

> A linked list is a data structure consisting of nodes,
> where each node contains data and a reference to the next node in the sequence.
> Unlike arrays, linked lists do not have a fixed size and can dynamically change their length.

**Code:**

In the following definition:

- `LinkedList` is a generic type that can hold elements of any type `A`.
- `Nil` represents an empty list.
- `Cons(head: A, tail: LinkedList[A])` represents a list node, where `head` is the value of the current node,
  and `tail` is a reference to the next node (or an empty list if this is the last node).
- Thus, a linked list can be recursively constructed from nodes, each pointing to the next.

```dotty
enum LinkedList[+A]:
  case Nil
  case Cons(head: A, tail: LinkedList[A])

val emptyList: LinkedList[Int] = LinkedList.Nil
// Empty list
val nonEmptyList: LinkedList[Int] = LinkedList.Cons(3, LinkedList.Cons(2, LinkedList.Cons(1, LinkedList.Nil)))
// Non-empty list: (3, 2, 1)
```

## Head Element

**Complexity:**

- **Time** - O(1)
- **Space** - O(1)

**Code:**

```dotty
enum LinkedList[+A]:
  lazy val headOption: Option[A] = this match
    case Nil        => None
    case Cons(h, _) => Some(h)

emptyList.headOption    // None
nonEmptyList.headOption // Some(3)
```

## Tail of the list without the head element

**Complexity:**

- **Time** - O(1)
- **Space** - O(1)

**Code:**

```dotty
enum LinkedList[+A]:
  lazy val tailOption: Option[LinkedList[A]] = this match
    case Nil           => None
    case Cons(_, tail) => Some(tail)

emptyList.tailOption    // None
nonEmptyList.tailOption // Some(Cons(2,Cons(1,Nil)))
```

## Last element

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  lazy val lastOption: Option[A] = this match
    case Nil          => None
    case Cons(h, Nil) => Some(h)
    case Cons(h, t)   => t.lastOption

emptyList.lastOption    // None
nonEmptyList.lastOption // Some(1)
```

## Tail of the list without the last element

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  lazy val initOption: Option[LinkedList[A]] = this match
    case Nil          => None
    case Cons(h, Nil) => Some(Nil)
    case Cons(h, t)   => t.initOption.map(Cons(h, _))

emptyList.initOption    // None
nonEmptyList.initOption // Some(Cons(3,Cons(2,Nil)))
```

## Check if the list is empty

**Complexity:**

- **Time** - O(1)
- **Space** - O(1)

**Code:**

```dotty
enum LinkedList[+A]:
  lazy val isEmpty: Boolean = this match
    case Nil => true
    case _   => false

emptyList.isEmpty       // true
nonEmptyList.isEmpty    // false
```

## Computing the length of the list

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  lazy val length: Int = this match
    case Nil           => 0
    case Cons(_, tail) => 1 + tail.length

emptyList.length        // 0
nonEmptyList.length     // 3
```

## Prepend to the list

**Complexity:**

- **Time** - O(1)
- **Space** - O(1)

**Code:**

```dotty
enum LinkedList[+A]:
  def prepend[B >: A](b: B): LinkedList[B] = Cons(b, this)

emptyList.prepend(5)    // Cons(5,Nil)
nonEmptyList.prepend(5) // Cons(5,Cons(3,Cons(2,Cons(1,Nil))))
```

## Append to the end of the list

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  def append[B >: A](b: B): LinkedList[B] = this match
    case Nil              => Cons(b, Nil)
    case Cons(head, tail) => Cons(head, tail.append(b))

emptyList.append(5)     // Cons(5,Nil)
nonEmptyList.append(5)  // Cons(3,Cons(2,Cons(1,Cons(5,Nil))))
```

## Getting an element by a given index

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  def get(i: Int): Option[A] =
    if i < 0 then None
    else if i == 0 then headOption
    else tailOption.flatMap(_.get(i - 1))

emptyList.get(0)        // None
nonEmptyList.get(0)     // Some(3)
```

## Check if the list contains a given element

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  def contains[B >: A](x: B): Boolean = this match
    case Nil                        => false
    case Cons(head, _) if head == x => true
    case Cons(_, tail)              => tail.contains(x)  

emptyList.contains(0)    // false
nonEmptyList.contains(2) // true
```

## Concatenation of two lists

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  def concat[B >: A](list: LinkedList[B]): LinkedList[B] = this match
    case Nil              => list
    case Cons(head, tail) => Cons(head, tail.concat(list))   

emptyList.concat(nonEmptyList)    // Cons(3,Cons(2,Cons(1,Nil)))
nonEmptyList.concat(nonEmptyList) // Cons(3,Cons(2,Cons(1,Cons(3,Cons(2,Cons(1,Nil))))))
```

## Filtering list elements

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  def filter(p: A => Boolean): LinkedList[A] = this match
    case Nil                         => Nil
    case Cons(head, tail) if p(head) => Cons(head, tail.filter(p))
    case Cons(_, tail)               => tail.filter(p)  

emptyList.filter(_ % 2 == 0)    // Nil
nonEmptyList.filter(_ % 2 == 0) // Cons(2,Nil)
```

## Transforming list elements

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  def map[B](f: A => B): LinkedList[B] = this match
    case Nil              => Nil
    case Cons(head, tail) => Cons(f(head), tail.map(f))

emptyList.map(_ + 1)            // Nil
nonEmptyList.map(_ + 1)         // Cons(4,Cons(3,Cons(2,Nil)))
```

## Folding list elements

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  def fold[B](init: B)(op: (B, A) => B): B = this match
    case Nil              => init
    case Cons(head, tail) => tail.fold(op(init, head))(op)

emptyList.fold(100)(_ + _)      // 100
nonEmptyList.fold(100)(_ + _)   // 106
```

## List of first n elements

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  def take(n: Int): LinkedList[A] = this match
    case Cons(head, tail) if n > 0 => Cons(head, tail.take(n - 1))
    case _ => Nil

emptyList.take(2)    // Nil 
nonEmptyList.take(2) // Cons(3,Cons(2,Nil))
```

## List except for the first n elements

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  def drop(n: Int): LinkedList[A] = this match
    case list if n <= 0            => list
    case Cons(head, tail) if n > 0 => tail.drop(n - 1)
    case _                         => Nil

emptyList.drop(1)    // Nil
nonEmptyList.drop(1) // Cons(2,Cons(1,Nil)) 
```

## List of first elements satisfying the predicate

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  def takeWhile(p: A => Boolean): LinkedList[A] = this match
    case Cons(head, tail) if p(head) => Cons(head, tail.takeWhile(p))
    case _ => Nil

emptyList.takeWhile(_ > 1)    // Nil
nonEmptyList.takeWhile(_ > 1) // Cons(3,Cons(2,Nil))
```

## List except for first elements satisfying the predicate

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum LinkedList[+A]:
  def dropWhile(p: A => Boolean): LinkedList[A] = this match
    case Cons(head, tail) if p(head) => tail.dropWhile(p)
    case _ => this  

emptyList.dropWhile(_ > 1)    // Nil
nonEmptyList.dropWhile(_ > 1) // Cons(1,Nil)
```

---

**Links:**

- [Source code][source]
- [Rod Stephens - Algorithms: Theory and Practical Applications. Chapter 3. Linked Lists](https://eksmo.ru/book/algoritmy-teoriya-i-prakticheskoe-primenenie-2-e-izdanie-ITD1210854/)
- [Linked list - Wikipedia](https://ru.wikipedia.org/wiki/%D0%A1%D0%B2%D1%8F%D0%B7%D0%BD%D1%8B%D0%B9_%D1%81%D0%BF%D0%B8%D1%81%D0%BE%D0%BA)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Introduction to Functional Programming - Bird, Wadler, Chapter 3](https://archive.org/details/introductiontofu0000bird/mode/2up)
- [Scalacaster - List](https://github.com/vkostyukov/scalacaster/blob/master/src/collection/List.scala)

[source]: https://gitverse.ru/scalabook/algorithms
