# Стеки

Стек можно рассматривать как контейнер с одним входом, т.е. элементы можно вставлять и извлекать только с этого входа. 

Операции стека: 

- вставить элемент
- удалить элемент 
- проверить стек на пустоту
- проверить стек на заполненность

Стек — это простая структура данных, удобная для многих операций, требующих упорядочивания или принудительного исполнения. 
Сложность пространства для n операций `push` составляет _O(n)_, тогда как средний случай _O(1)_. 
Точно так же `pop` и `peek` имеют сложность _O(1)_, что верно для `isEmpty` и `isFull`.

Возможная реализация стека заданной длины в императивном стиле:

```scala
class ImperativeStack[A: ClassTag](maxSize: Int):
  private val stackBox = new Array[A](maxSize)
  private var top = -1

  def push(data: A): Unit =
    if isFull then throw new IllegalArgumentException("Can't add element to full stack")
    else
      top += 1
      stackBox(top) = data

  def pop(): A =
    val popData = peek()
    top -= 1
    popData

  def peek(): A =
    if isEmpty then throw new IllegalArgumentException("Can't get element to empty stack")
    else stackBox(top)

  def isEmpty: Boolean =
    top == -1

  def isFull: Boolean =
    top == maxSize - 1
```

Возможная реализация стека переменной длины в функциональном стиле:

```scala
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

    def peek(): Option[(A, Stack[A])] = stack match
      case EmptyStack              => None
      case NonEmptyStack(value, _) => Some((value, stack))

    def pop(): Option[(A, Stack[A])] = stack match
      case EmptyStack                 => None
      case NonEmptyStack(value, tail) => Some((value, tail))
```


---

**Ссылки:**

- [Исходный код ImperativeStack](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Falgorithms%2Fstructures%2FImperativeStack.scala&plain=1)
- [Исходный код Stack](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Falgorithms%2Fstructures%2FStack.scala&plain=1)
- [Тесты ImperativeStack](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Falgorithms%2Fstructures%2FImperativeStackSuite.scala&plain=1)
- [Тесты Stack](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Falgorithms%2Fstructures%2FStackSuite.scala&plain=1)
- [Bhim P. Upadhyaya - Data Structures and Algorithms with Scala](https://link.springer.com/book/10.1007/978-3-030-12561-5)
