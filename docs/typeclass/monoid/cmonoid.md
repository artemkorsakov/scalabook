# Коммутативный моноид

`CMonoid[A]` - [моноид](monoid.md), который [коммутативен](csemigroup.md).

Помимо законов моноида:

- _Замыкание_ (_closure_): для `∀ x, y ∈ S` выполняется `x + y ∈ M`
- _Ассоциативность_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x + y) + z = x + (y + z)`
- _Тождественность_ (_identity_): существует `∃ e ∈ M` такой, что для `∀ x ∈ M` выполняется `e + x = x + e = x`

должен соблюдаться закон:

- _Коммутативность_ (_commutative_): для `∀ x, y ∈ S` выполняется `x + y = y + x`

## Описание

```dotty
trait CMonoid[A] extends Monoid[A], CSemigroup[A]
```

## Примеры

### Натуральные числа N образуют коммутативный моноид относительно сложения и 0

```dotty
given CMonoid[Int] with
  val empty                        = 0
  def combine(x: Int, y: Int): Int = x + y
```

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.CMonoid

CMonoid.combine(100, CMonoid.empty[Int]) // 100
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)

