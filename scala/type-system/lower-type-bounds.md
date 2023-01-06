# Нижнее ограничение типа

В то время как верхнее ограничение типа ограничивает тип до подтипа стороннего типа,
нижнее ограничение типа объявляют тип супертипом стороннего типа.
Термин `B >: A` выражает то, что параметр типа `B` или абстрактный тип `B` относится к супертипу типа `A`.
В большинстве случаев `A` будет задавать тип класса, а `B` - тип метода.

Вот пример, где это полезно:

```scala
trait Node[+B]:
  def prepend(elem: B): Node[B]

case class ListNode[+B](h: B, t: Node[B]) extends Node[B]:
  def prepend(elem: B): ListNode[B] = ListNode(elem, this)
  def head: B = h
  def tail: Node[B] = t

case class Nil[+B]() extends Node[B]:
  def prepend(elem: B): ListNode[B] = ListNode(elem, this)
```

В данной программе реализован связанный список.
`Nil` представляет пустой список. Класс `ListNode` - это узел,
который содержит элемент типа `B` (`head`) и ссылку на остальную часть списка (`tail`).
Класс `Node` и его подтипы ковариантны, потому что указанно `+B`.

Однако эта программа не скомпилируется,
потому что параметр `elem` в `prepend` имеет тип `B`, который объявлен ковариантным.
Так это не работает, потому что функции контрвариантны в типах своих параметров
и ковариантны в типах своих результатов.

Чтобы исправить это, необходимо перевернуть вариантность типа параметра `elem` в `prepend`.
Для этого вводится новый тип для параметра `U`, у которого тип `B` указан в качестве нижней границы типа.

```scala
trait Node[+B]:
  def prepend[U >: B](elem: U): Node[U]
case class ListNode[+B](h: B, t: Node[B]) extends Node[B]:
  def prepend[U >: B](elem: U): ListNode[U] = ListNode(elem, this)
  def head: B = h
  def tail: Node[B] = t
case class Nil[+B]() extends Node[B]:
  def prepend[U >: B](elem: U): ListNode[U] = ListNode(elem, this)
```

Теперь можно сделать следующее:

```scala
trait Bird
case class AfricanSwallow() extends Bird
case class EuropeanSwallow() extends Bird
val africanSwallowList = ListNode[AfricanSwallow](AfricanSwallow(), Nil())
// africanSwallowList: ListNode[AfricanSwallow] = ListNode(
//   h = AfricanSwallow(),
//   t = Nil()
// )
val birdList: Node[Bird] = africanSwallowList
// birdList: Node[Bird] = ListNode(h = AfricanSwallow(), t = Nil())
birdList.prepend(EuropeanSwallow())
// res7: Node[Bird] = ListNode(
//   h = EuropeanSwallow(),
//   t = ListNode(h = AfricanSwallow(), t = Nil())
// )
```

Переменной с типом `Node[Bird]` можно присвоить значение `africanSwallowList`, а затем добавить и `EuropeanSwallow`.


---

**References:**
- [Scala tour, Lower type bounds](https://docs.scala-lang.org/ru/tour/lower-type-bounds.html)
