# Стеки

_Стек_ — это линейный список, в котором все операции вставки и удаления (и, как правило, операции доступа к данным) 
выполняются только на одном из концов списка.

Структура такого типа определяет набор правил:

- Элемент, который вставляется последним, удаляется первым - _Last In First Out (LIFO)_.
- Элемент, вставленный первым, покидает стек последним.

Стек — это простая структура данных, удобная для многих операций, требующих упорядочивания или принудительного исполнения. 
Сложность пространства для n операций `push` (_протолкнуть_) составляет _O(n)_, тогда как средний случай _O(1)_. 
Точно так же `pop` (_вытолкнуть_) и `peek` (_заглянуть_) имеют сложность _O(1)_, что верно для `isEmpty` и `isFull`.

Возможная реализация стека:

```dotty
enum Stack[+A]:
  case EmptyStack
  case NonEmptyStack(value: A, tail: Stack[A])

  lazy val isEmpty: Boolean = this match
    case EmptyStack          => true
    case NonEmptyStack(_, _) => false

object Stack:
  def empty[A]: Stack[A] = EmptyStack

  extension [A](stack: Stack[A])
    def push[B <: A](value: B): Stack[A] = NonEmptyStack(value, stack)

    def pop(): Option[(A, Stack[A])] = stack match
      case EmptyStack                 => None
      case NonEmptyStack(value, tail) => Some((value, tail))
      
    def peek(): Option[(A, Stack[A])] = stack match
      case EmptyStack              => None
      case NonEmptyStack(value, _) => Some((value, stack))
```

---

**Ссылки:**

- [Donald E. Knuth - The Art of Computer Programming, section 2.2.1](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
