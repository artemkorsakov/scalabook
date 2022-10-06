# Order

`Order` предназначен для типов, которые имеют порядок. 
`Order` охватывает все стандартные функции сравнения, такие как `>`, `<`, `>=` и `<=`, 
а также с его помощью можно реализовать операции нахождения максимума, минимума, сортировку двух элементов и реверсивный `Order`.

`Order` должен удовлетворять следующим законам:



##### Описание

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

### Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._
List(1, 2, 3) === List(1, 2, 3) // true
List(1, 2, 3) =/= List(1, 2, 4) // true
```

Order enables ?|? syntax which returns Ordering: LT, GT, and EQ. 
It also enables lt, gt, lte, gte, min, and max operators by declaring order method. 
Similar to Equal, comparing Int and Doubl fails compilation.




---

**References:**

- [Scalaz](https://scalaz.github.io/7/typeclass/index.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Order.html)
