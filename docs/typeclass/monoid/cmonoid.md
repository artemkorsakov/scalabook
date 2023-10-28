# Коммутативный моноид

`CMonoid[A]` - моноид, который коммутативен.

## Описание

```dotty
trait Semigroup[A]:
  def combine(x: A, y: A): A
  
  def combineN(x: A, n: Int): Option[A] =
    if n <= 0 then None
    else if n == 1 then Some(x)
    else combineN(x, n - 1).map(y => combine(x, y))
```

## Примеры

### Натуральные числа N образуют полугруппу относительно сложения

```dotty
given sumSemigroupInstance: Semigroup[Int] = (x: Int, y: Int) => x + y
```

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.Semigroup
import spire.syntax.semigroup.*

1 |+| 2                           // 3
("hello", 123) |+| ("world", 321) // ("helloworld",444)

Semigroup.combineN(3, 5)          // 15
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)

