# Enum

Члены типа `Enum`, расширяющего тип `Order`, представляют собой последовательно упорядоченные типы — их можно перечислить.

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


##### Описание

```scala
trait Equal[F]:
  def equal(a1: F, a2: F): Boolean

enum Ordering:
  case LT
  case EQ
  case GT

trait Order[F] extends Equal[F]:
  def order(x: F, y: F): Ordering

trait Enum[F] extends Order[F]:
  def succ(a: F): F
  def pred(a: F): F
  def min: Option[F]
  def max: Option[F]
```

### Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._
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

**References:**

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Enum.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Enum.html)
