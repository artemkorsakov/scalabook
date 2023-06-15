# Group

Группа **G** — это [моноид](monoid.md) 
с обратным **g^(−1)** для каждого элемента **g**. 
Таким образом, **G** — категория с одним объектом, в которой каждая стрелка — изоморфизм.

Группа, помимо законов моноида, должна удовлетворять закону:

- _Обратимость_: для каждого **g ∈ G** существует **g^(−1)** такой, что **g * g^(−1) == g^(−1) * g == G.empty**

Более того, это _абелева группа_, если она удовлетворяет свойству:

- _Коммутативность_: **x * y = y * x** для всех **x** и **y ∈ G**.

## Описание 

```scala
trait Group[A] extends Monoid[A]:
  extension (a: A) 
    def inverse: A
```

## Примеры

### Числа относительно сложения с 0

```scala
given Group[Int] with
  override val empty                           = 0
  override def combine(x: Int, y: Int): Int    = x + y
  extension (a: Int) override def inverse: Int = -a
```

- Целые числа **Z** являются абелевой группой по сложению
- Натуральные числа не являются группой при сложении (данное число **x** в **N**, но **-x** не находится в **N**).
- Ни целые числа, ни натуральные числа не являются группой по умножению, 
  но множество ненулевых рациональных чисел (**n/d** для любого **n, d ∈ N, n ≠ 0, d ≠ 0**) 
  является (абелевой) группой по умножению с единичным элементом **1**.

В примере ниже, конечно же числитель и знаменатель надо сокращать на НОД и "нормализовывать" знак
с проверкой на **n ≠ 0, d ≠ 0**,
но в упрощенном варианте группа для рациональных чисел будет выглядеть так:

```scala
final case class Rational(n: Int, d: Int):
  def *(that: Rational): Rational =
    Rational(n * that.n, d * that.d)

given Group[Rational] with
  override val empty: Rational                             = Rational(1, 1)
  override def combine(x: Rational, y: Rational): Rational = x * y
  extension (a: Rational) override def inverse: Rational   = Rational(a.d, a.n)

```

## Реализация

### Реализация в Cats

```scala
import cats.*, cats.syntax.all.*

1.inverse() // -1

assert((1 |+| 1.inverse()) === Monoid[Int].empty)
```

---

**Ссылки:**

- [Algebird](https://twitter.github.io/algebird/typeclasses/group.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Grp.html)
