# Абелева группа

## Формальное определение

`AbGroup[A]` - "абелева группа", [группа](group.md), которая является [коммутативной](cmonoid.md).

Помимо законов группы:

- _Замыкание_ (_closure_): для `∀ x, y ∈ S` выполняется `x + y ∈ S`
- _Ассоциативность_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x + y) + z = x + (y + z)`
- _Тождественность_ (_identity_): существует `∃ e ∈ S` такой, что для `∀ x ∈ S` выполняется `e + x = x + e = x`
- _Обратимость_: для каждого `x ∈ S` существует \\(x^{-1}\\) такой,
  что x + \\(x^{-1}\\) == \\(x^{-1}\\) + x == e

должен соблюдаться закон:

- _Коммутативность_ (_commutative_): для `∀ x, y ∈ S` выполняется `x + y = y + x`

## Описание

```dotty
trait AbGroup[A] extends Group[A], CMonoid[A]
```

## Определение в виде кода на Scala

### Целые числа **Z** являются абелевой группой по сложению

```dotty
given AbGroup[Int] with
  override val empty                           = 0
  override def combine(x: Int, y: Int): Int    = x + y
  extension (a: Int) override def inverse: Int = -a
```

## Законы в виде кода на Scala

```dotty
```

## Примеры

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.AbGroup

AbGroup[Int].inverse(1) // -1
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)

