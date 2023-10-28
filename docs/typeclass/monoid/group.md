# Группа

Группа **G** — это [моноид](monoid.md) 
с обратным \\(g^{-1}\\) для каждого элемента g. 
Таким образом, **G** — категория с одним объектом, в которой каждая стрелка — изоморфизм.

Группа, помимо законов моноида:

- _Замыкание_ (_closure_): для `∀ x, y ∈ S` выполняется `x + y ∈ M`
- _Ассоциативность_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x + y) + z = x + (y + z)`
- _Тождественность_ (_identity_): существует `∃ e ∈ M` такой, что для `∀ x ∈ M` выполняется `e + x = x + e = x`

должна удовлетворять закону:

- _Обратимость_: для каждого `x ∈ G` существует \\(x^{-1}\\) такой, 
  что x * \\(x^{-1}\\) == \\(x^{-1}\\) * x == G.empty

## Описание 

```dotty
trait Group[A] extends Monoid[A]:
  extension (a: A) 
    def inverse: A
```

## Примеры

### Числа относительно сложения с 0

```dotty
given Group[Int] with
  override val empty                           = 0
  override def combine(x: Int, y: Int): Int    = x + y
  extension (a: Int) override def inverse: Int = -a
```

- Натуральные числа не являются группой по сложению (данное число **x** в **N**, но **-x** не находится в **N**).
- Ни целые числа, ни натуральные числа не являются группой по умножению, 
  но множество ненулевых рациональных чисел (**n/d** для любого **n, d ∈ N, n ≠ 0, d ≠ 0**) 
  является ([абелевой](abgroup.md)) группой по умножению с единичным элементом **1**.

В примере ниже, конечно же числитель и знаменатель надо сокращать на НОД и "нормализовывать" знак
с проверкой на **n ≠ 0, d ≠ 0**,
но в упрощенном варианте группа для рациональных чисел будет выглядеть так:

```dotty
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

```dotty
import cats.*, cats.syntax.all.*

1.inverse() // -1

assert((1 |+| 1.inverse()) === Monoid[Int].empty)
```

### Реализация в Spire

```dotty
import spire.algebra.Group
import spire.syntax.group.*

1.inverse       // -1
1 |+| 1.inverse // 0
```

---

**Ссылки:**

- [Algebird](https://twitter.github.io/algebird/typeclasses/group.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Grp.html)
- [Spire home page](https://typelevel.org/spire)
