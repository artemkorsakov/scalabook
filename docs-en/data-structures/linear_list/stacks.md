# Stacks

_A stack_ is a linear list in which all insertion and deletion operations (and, typically, data access operations)
are performed only at one end of the list.

This type of structure defines a set of rules:

- The last element inserted is the first one removed - _Last In First Out (LIFO)_.
- The first element inserted leaves the stack last.

A stack is a simple data structure, convenient for many operations requiring ordering or forced execution.
The space complexity for n `push` operations is _O(n)_, while the average case is _O(1)_.
Similarly, `pop` and `peek` have _O(1)_ complexity, as do `isEmpty` and `isFull`.

## Stack Definition

**Code:**

```dotty
enum Stack[+A]:
  case EmptyStack
  case NonEmptyStack(value: A, tail: Stack[A])

val emptyStack: Stack[Int]    = EmptyStack
val nonEmptyStack: Stack[Int] = NonEmptyStack(2, NonEmptyStack(1, EmptyStack))
```

## Head element

**Complexity:**

- **Time** - O(1)
- **Space** - O(1)

**Code:**

```dotty
enum Stack[+A]:
  lazy val headOption: Option[A] = this match
    case EmptyStack          => None
    case NonEmptyStack(h, _) => Some(h) 

emptyStack.headOption         // None
nonEmptyStack.headOption      // Some(2)
```

## Tail of the stack without the head element

**Complexity:**

- **Time** - O(1)
- **Space** - O(1)

**Code:**

```dotty
enum Stack[+A]:
  lazy val tailOption: Option[Stack[A]] = this match
    case EmptyStack             => None
    case NonEmptyStack(_, tail) => Some(tail)

emptyStack.tailOption         // None
nonEmptyStack.tailOption      // Some(NonEmptyStack(1,EmptyStack))
```

## Check if the stack is empty

**Complexity:**

- **Time** - O(1)
- **Space** - O(1)

**Code:**

```dotty
enum Stack[+A]:
  lazy val isEmpty: Boolean = this match
    case EmptyStack          => true
    case NonEmptyStack(_, _) => false

emptyStack.isEmpty            // true
nonEmptyStack.isEmpty         // false
```

## Computing the length of the stack

**Complexity:**

- **Time** - O(n)
- **Space** - O(n)

**Code:**

```dotty
enum Stack[+A]:
  lazy val length: Int = this match
    case EmptyStack             => 0
    case NonEmptyStack(_, tail) => 1 + tail.length

emptyStack.length             // 0
nonEmptyStack.length          // 2
```

## Push element onto stack

**Complexity:**

- **Time** - O(1)
- **Space** - O(1)

**Code:**

```dotty
enum Stack[+A]:
  def push[B >: A](value: B): Stack[B] = NonEmptyStack(value, this)

emptyStack.push(5)            
// NonEmptyStack(5,EmptyStack)
nonEmptyStack.push(5)
// NonEmptyStack(5,NonEmptyStack(2,NonEmptyStack(1,EmptyStack)))
```

## Pop element from stack

**Complexity:**

- **Time** - O(1)
- **Space** - O(1)

**Code:**

```dotty
enum Stack[+A]:
  def pop(): Option[(A, Stack[A])] = this match
    case EmptyStack                 => None
    case NonEmptyStack(value, tail) => Some((value, tail))

emptyStack.pop()
// None
nonEmptyStack.pop()
// Some((2,NonEmptyStack(1,EmptyStack)))
```

## Peek element from stack

**Complexity:**

- **Time** - O(1)
- **Space** - O(1)

**Code:**

```dotty
enum Stack[+A]:
  def peek(): Option[(A, Stack[A])] = this match
    case EmptyStack              => None
    case NonEmptyStack(value, _) => Some((value, this))

emptyStack.peek()
// None
nonEmptyStack.peek()
// Some((2,NonEmptyStack(2,NonEmptyStack(1,EmptyStack))))
```

---

**Links:**

- [Source code][source]
- [Donald E. Knuth - The Art of Computer Programming, section 2.2.1](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&amp;client_id=springerlink&amp;redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Scalacaster - Stack](https://github.com/vkostyukov/scalacaster/blob/master/src/collection/Stack.scala)

[source]: https://gitverse.ru/scalabook/algorithms
