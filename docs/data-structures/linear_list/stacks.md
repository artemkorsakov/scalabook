{%
showPrev = true
prevPage = doubly_linked_lists.html
showNext = true
nextPage = queues.html
%}

# Стеки

_Стек_ — это линейный список, в котором все операции вставки и удаления (и, как правило, операции доступа к данным) 
выполняются только на одном из концов списка.

Структура такого типа определяет набор правил:

- Элемент, который вставляется последним, удаляется первым - _Last In First Out (LIFO)_.
- Элемент, вставленный первым, покидает стек последним.

Стек — это простая структура данных, удобная для многих операций, требующих упорядочивания или принудительного исполнения. 
Сложность пространства для n операций `push` (_протолкнуть_) составляет _O(n)_, тогда как средний случай _O(1)_. 
Точно так же `pop` (_вытолкнуть_) и `peek` (_заглянуть_) имеют сложность _O(1)_, что верно для `isEmpty` и `isFull`.

## Определение стека

**Код:**

```dotty
enum Stack[+A]:
  case EmptyStack
  case NonEmptyStack(value: A, tail: Stack[A])

val emptyStack: Stack[Int]    = EmptyStack
val nonEmptyStack: Stack[Int] = NonEmptyStack(2, NonEmptyStack(1, EmptyStack))
```

## Заглавный элемент

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
enum Stack[+A]:
  lazy val headOption: Option[A] = this match
    case EmptyStack          => None
    case NonEmptyStack(h, _) => Some(h) 

emptyStack.headOption         // None
nonEmptyStack.headOption      // Some(2)
```

## Остаток стека без начального элемента

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
enum Stack[+A]:
  lazy val tailOption: Option[Stack[A]] = this match
    case EmptyStack             => None
    case NonEmptyStack(_, tail) => Some(tail)

emptyStack.tailOption         // None
nonEmptyStack.tailOption      // Some(NonEmptyStack(1,EmptyStack))
```

## Проверка стека на пустоту

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
enum Stack[+A]:
  lazy val isEmpty: Boolean = this match
    case EmptyStack          => true
    case NonEmptyStack(_, _) => false

emptyStack.isEmpty            // true
nonEmptyStack.isEmpty         // false
```

## Вычисление длины стека

**Оценка:**

- **Время** - O(n)
- **Память** - O(n)

**Код:**

```dotty
enum Stack[+A]:
  lazy val length: Int = this match
    case EmptyStack             => 0
    case NonEmptyStack(_, tail) => 1 + tail.length

emptyStack.length             // 0
nonEmptyStack.length          // 2
```

## Добавление элемента в стек

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

```dotty
enum Stack[+A]:
  def push[B >: A](value: B): Stack[B] = NonEmptyStack(value, this)

emptyStack.push(5)            
// NonEmptyStack(5,EmptyStack)
nonEmptyStack.push(5)
// NonEmptyStack(5,NonEmptyStack(2,NonEmptyStack(1,EmptyStack)))
```

## Выталкивание элемента из стека

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

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

## Получение элемента из стека

**Оценка:**

- **Время** - O(1)
- **Память** - O(1)

**Код:**

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

**Ссылки:**

- [Исходный код][source]
- [Donald E. Knuth - The Art of Computer Programming, section 2.2.1](https://www-cs-faculty.stanford.edu/~knuth/taocp.html)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://idp.springer.com/authorize?response_type=cookie&client_id=springerlink&redirect_uri=https%3A%2F%2Flink.springer.com%2Fbook%2F10.1007%2F978-3-030-12561-5)
- [Scalacaster - Stack](https://github.com/vkostyukov/scalacaster/blob/master/src/collection/Stack.scala)

[source]: https://gitverse.ru/scalabook/algorithms
