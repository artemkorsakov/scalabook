# Абелева группа

`AbGroup[A]` - "абелева группа", группа, которая является коммутативной.

Группа, помимо законов моноида, должна удовлетворять закону:

- _Обратимость_: для каждого **g ∈ G** существует \\(g^{-1}\\) такой,
  что g * \\(g^{-1}\\) == \\(g^{-1}\\) * g == G.empty

Более того, это _абелева группа_, если она удовлетворяет свойству:

- _Коммутативность_: **x * y = y * x** для всех **x** и **y ∈ G**.

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

- Целые числа **Z** являются абелевой группой по сложению


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

