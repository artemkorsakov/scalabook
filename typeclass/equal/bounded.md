# Bounded 

`Bounded` имеют верхнюю и нижнюю границы, а следовательно расширяет тип `Order`, 
позволяющий сравнивать элементы множества.

`Enum` может расширять `Bounded` в некоторых случаях.

`Bounded` должен удовлетворять следующим законам (помимо законов `Order` и `Equal`):

- любой `x` из множества не превышает `max`: `x <= max`
- любой `x` из множества не меньше `min`: `x >= min`

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

trait Bounded[F] extends Order[F]:
  def min: F
  def max: F
```


---

**References:**
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Bounded.html)
