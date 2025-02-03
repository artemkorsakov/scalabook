# Enum

## Формальное определение

Члены типа `Enum`, расширяющего тип [`Order`](order.md) и [`Bothable`](bothable.md), 
представляют собой последовательно упорядоченные типы — их можно перечислить.

Основное преимущество класса типов `Enum` заключается в том, что мы можем использовать его типы в диапазонах списков. 
У них также есть определенные преемники (_successors_) и предшественники (_predecessors_), 
которые вы можете получить с помощью функций `succ` и `pred`.
И возможно существование минимального и максимального элементов.

`Enum` должен удовлетворять следующим законам (помимо законов `Order` и `Equal`):

- следующий предыдущего элемента `x` является `x`: `succ(pred(x)) == x`
- предыдущий следующего элемента `x` является `x`: `pred(succ(x)) == x`
- если определены и `min`, и `max`, то предыдущий от `min` - это `max`
- если определены и `min`, и `max`, то следующий для `max` - это `min`
- для любого x, не равного `max`, его следующий элемент больше или равен `x`: `x == max || succ(x) >= x`
- для любого x, не равного `min`, его предыдущий элемент меньше или равен `x`: `x == min || pred(x) <= x`

## Определение в виде кода на Scala

```dotty
trait Equal[A]:
  def equal(a1: A, a2: A): Boolean

enum Ordering:
  case LT
  case EQ
  case GT

trait Order[A] extends Equal[A]:
  def order(x: A, y: A): Ordering

trait Predecessible[A]:
  def pred(a: A): A

trait Successible[A]:
  def succ(a: A): A

trait Bothable[A] extends Predecessible[A], Successible[A]

trait Enum[A] extends Order[A], Bothable[A]:
  def min: Option[A]
  def max: Option[A]
```

## Реализация в библиотеках

### Реализация в ScalaZ

```dotty
import scalaz.*
import Scalaz.*
1 |-> 2        // [1,2]
1 |--> (2, 5)  // [1,3,5]
1.pred         // 0
1.predx        // Some(0)
1.succ         // 2
1.succx        // Some(2)
Enum[Int].min  // Some(-2147483648)
Enum[Int].max  // Some(2147483647)
```

---

**Ссылки:**

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Enum.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Enum.html)
