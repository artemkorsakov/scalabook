# Группа

## Формальное определение

Группа **G** — это [моноид](monoid.md) с обратным \\(g^{-1}\\) для каждого элемента g.

Множество \\(G\\) с заданной на нём бинарной операцией +: \\(G \times G \to G\\) называется _группой_
\\((G, +)\\), если выполнены следующие аксиомы:

- _Замыкание_ (_closure_): для \\(\forall x, y \in G\\) выполняется \\(x + y \in G\\).
- _Ассоциативность_ (_associativity_): для \\(\forall x, y, z \in G\\) выполняется \\((x + y) + z = x + (y + z)\\).
- _Тождественность_ или _существования нейтрального элемента_ (_identity_):
  существует \\(\exists e \in G\\) такой, что для \\(\forall x \in G\\) выполняется \\(e + x = x + e = x\\)
- _Обратимость_: для \\(\forall x \in G\\) существует \\(x^{-1} \in G\\) такой,
  что \\(x + x^{-1} = x^{-1} + x = e\\)

> Первые три закона наследуются от моноида.

## Определение в виде кода на Scala

```dotty
trait Group[A] extends Monoid[A]:
  extension (a: A) 
    def inverse: A
```

## Законы в виде кода на Scala

```dotty
trait GroupLaw extends MonoidLaw:
  def checkGroupLaw[A: Group](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    (
      checkMonoidLaw(x, y, z),
      check(
        Group[A].combine(x, Group[A].inverse(x)) == Group[A].empty,
        "inverse law 1: x + (-x) = 0"
      ),
      check(
        Group[A].combine(Group[A].inverse(x), x) == Group[A].empty,
        "inverse law 2: (-x) + x = 0"
      )
    ).mapN { case (_, _, _) => () }
```

## Примеры

### Числа относительно сложения с 0

```dotty
given Group[Int] with
  val empty                           = 0
  def combine(x: Int, y: Int): Int    = x + y
  extension (a: Int) def inverse: Int = -a
```

- Натуральные числа не являются группой по сложению (данное число **x** в **N**, но **-x** не находится в **N**).
- Ни целые числа, ни натуральные числа не являются группой по умножению, 
  но множество ненулевых рациональных чисел (**n/d** для любого **n, d ∈ N, n ≠ 0, d ≠ 0**) 
  является ([абелевой](abgroup.md)) группой по умножению с единичным элементом **1**.

В примере ниже числитель и знаменатель надо сокращать на НОД и "нормализовывать" знак
с проверкой на **n ≠ 0, d ≠ 0**,
но в упрощенном варианте группа для рациональных чисел будет выглядеть так:

```dotty
final case class Rational(n: Int, d: Int):
  def *(that: Rational): Rational =
    Rational(n * that.n, d * that.d)

given Group[Rational] with
  val empty: Rational                             = Rational(1, 1)
  def combine(x: Rational, y: Rational): Rational = x * y
  extension (a: Rational) def inverse: Rational   = Rational(a.d, a.n)
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
- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%93%D1%80%D1%83%D0%BF%D0%BF%D0%B0_(%D0%BC%D0%B0%D1%82%D0%B5%D0%BC%D0%B0%D1%82%D0%B8%D0%BA%D0%B0))
