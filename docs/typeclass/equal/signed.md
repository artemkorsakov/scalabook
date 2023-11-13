# Signed

## Формальное определение

Общее упорядочивание, инвариантное к сдвигу, фиксируется классом типов `Signed[A]`,
который расширяет [`Order`](order.md), - типы, имеющие знак (отрицательный, ноль, положительный).
В общем случае тип `A` содержит нулевой элемент `0`.

`Signed` должен удовлетворять следующим законам (помимо законов `Order` и `Equal`):

- `signum(x) = -1` если `x < 0`, `signum(x) = 1` если `x > 0`, иначе - `signum(x) = 0`.

Если тип `A` оснащен отрицательными элементами `-x`, то:

- `abs(x) = -x` если `x < 0`, иначе - `x`,

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

enum Sign:
  case Positive
  case Zero
  case Negative

trait Signed[A] extends Order[A]:
  def sign(x: A): Sign
  def isSignPositive(x: A): Boolean = sign(x) == Sign.Positive
  def isSignZero(x: A): Boolean = sign(x) == Sign.Zero
  def isSignNegative(x: A): Boolean = sign(x) == Sign.Negative
  def isSignNonPositive(x: A): Boolean = sign(x) != Sign.Positive
  def isSignNonZero(x: A): Boolean = sign(x) != Sign.Zero
  def isSignNonNegative(x: A): Boolean = sign(x) != Sign.Negative
```

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.Signed.*
import spire.std.int.*

sign(10)               // Positive
sign(0)                // Zero
sign(-10)              // Negative
signum(10)             // 1
signum(0)              // 0
signum(-10)            // -1
abs(10)                // 10
abs(0)                 // 0 
abs(-10)               // 10
isSignPositive(10)     // true
isSignZero(0)          // true
isSignNegative(-10)    // true
isSignNonPositive(10)  // false
isSignNonZero(0)       // false
isSignNonNegative(-10) // false
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)
