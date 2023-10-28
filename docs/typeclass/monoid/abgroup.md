# Абелева группа

`AbGroup[A]` - "абелева группа", [группа](group.md), которая является коммутативной.

Помимо законов моноида должен соблюдаться закон:

- _Коммутативность_ (_commutative_): для `∀ x, y ∈ S` выполняется `x + y = y + x`

## Описание

```dotty
trait AbGroup[A] extends Group[A], CMonoid[A]
```

## Примеры

### Целые числа **Z** являются абелевой группой по сложению

```dotty
given AbGroup[Int] with
  override val empty                           = 0
  override def combine(x: Int, y: Int): Int    = x + y
  extension (a: Int) override def inverse: Int = -a
```

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.AbGroup

AbGroup[Int].inverse(1) // -1
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)

