# Коммутативная полугруппа

`CSemigroup[A]` - [полугруппа](semigroup.md), которая является коммутативной.

Помимо законов полугруппы:

- _Замыкание_ (_closure_): для `∀ x, y ∈ S` выполняется `x + y ∈ S`
- _Ассоциативность_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x + y) + z = x + (y + z)`

должен соблюдаться закон:

- _Коммутативность_ (_commutative_): для `∀ x, y ∈ S` выполняется `x + y = y + x`

## Описание

```dotty
trait Semigroup[A]:
  def combine(x: A, y: A): A
  
trait CSemigroup[A] extends Semigroup[A]
```

## Примеры

### Натуральные числа N образуют коммутативную полугруппу относительно сложения

```dotty
given CSemigroup[Int] = (x: Int, y: Int) => x + y
```

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.CSemigroup

CSemigroup.combine(1, 2) == CSemigroup.combine(2, 1)
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)

