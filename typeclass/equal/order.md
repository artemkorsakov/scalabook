# Order

`Order` предназначен для типов, которые имеют порядок. Он расширяет тип [`Equal`](equal).
`Order` охватывает все стандартные функции сравнения, такие как `>`, `<`, `>=` и `<=`, 
а также с его помощью можно реализовать операции нахождения максимума, минимума, сортировку двух элементов и реверсивный `Order`.

`Order` должен удовлетворять следующим законам (помимо законов `Equal`):

- Антисимметричность: 
  - если `f1 < f2`, то `f2 > f1`, 
  - если `f1 <= f2`, то `f2 >= f1`,
  - и т.п.
- Транзитивность:
  - если `f1 < f2` и `f2 <= f3`, то `f1 < f3`,
  - если `f1 == f2` и `f2 <= f3`, то `f1 <= f3`,
  - если `f1 == f2` и `f2 == f3`, то `f1 == f3`,
  - и т.п.
- Соответствие `Order` и `Equal`:
  - `equal(f1, f2)` равно `true` тогда и только тогда, когда `order(f1, f2)` равно `Ordering.EQ`


## Описание

```scala
trait Equal[F]: 
  def equal(a1: F, a2: F): Boolean

enum Ordering:
  case LT
  case EQ
  case GT

trait Order[F] extends Equal[F]:
  self =>

  def order(x: F, y: F): Ordering

  def equal(x: F, y: F): Boolean = order(x, y) == Ordering.EQ

  def lessThan(x: F, y: F): Boolean = order(x, y) == Ordering.LT

  def lessThanOrEqual(x: F, y: F): Boolean = order(x, y) != Ordering.GT

  def greaterThan(x: F, y: F): Boolean = order(x, y) == Ordering.GT

  def greaterThanOrEqual(x: F, y: F): Boolean = order(x, y) != Ordering.LT

  def max(x: F, y: F): F = if greaterThanOrEqual(x, y) then x else y

  def min(x: F, y: F): F = if lessThan(x, y) then x else y

  def sort(x: F, y: F): (F, F) = if lessThanOrEqual(x, y) then (x, y) else (y, x)

  def reverseOrder: Order[F] = new Order[F]:
    def order(x: F, y: F): Ordering = self.order(y, x)
    override def equal(x: F, y: F) = self.equal(x, y)
    override def reverseOrder = self
```

## Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._
1.0 ?|? 2.0 // Ordering.LT
1.0 lt 2.0 // true
2.0 gt 1.0 // true
1.0 lte 2.0 // true
2.0 gte 1.0 // true
1 max 2 // 2
1 min 2 // 1
```

Метод `?|?` в ScalaZ возвращает результат сравнения `Ordering: LT, GT или EQ`. 


---

## References

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Order.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Order.html)
