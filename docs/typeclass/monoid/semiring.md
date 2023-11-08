# Полукольцо

## Формальное определение

В то время как [группа](group.md), [моноид](monoid.md) и [полугруппа](semigroup.md) 
определяются множеством и одной операцией, полукольцо определяется множеством и двумя операциями. 
Учитывая множество `R` и операции `+` и `*`, 
мы говорим, что `(R, +, *)` - это полукольцо, если оно удовлетворяет следующим свойствам:

- `(R, +)` является [коммутативным моноидом](cmonoid.md)
- `(R, *)` является [полугруппой](semigroup.md)

Для полугруппы должны соблюдаться законы коммутативного моноида по сложению:

- _Замыкание_ (_closure_): для `∀ x, y ∈ S` выполняется `x + y ∈ S`
- _Ассоциативность_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x + y) + z = x + (y + z)`
- _Тождественность_ (_identity_): существует `∃ e ∈ S` такой, что для `∀ x ∈ S` выполняется `e + x = x + e = x`
- _Коммутативность_ (_commutative_): для `∀ x, y ∈ S` выполняется `x + y = y + x`

и законы полугруппы по умножению:

- _Замыкание_ (_closure_): для `∀ x, y ∈ S` выполняется `x * y ∈ S`
- _Ассоциативность_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x * y) * z = x * (y * z)`

, а также дистрибутивность:

- _Дистрибутивность_ (_distributivus_, _распределительный закон_): 
  Для любых `∀ x, y, z ∈ S` выполняется `x * (y + z) = x * y + x * z` и `(x + y) * z = x * z + y * z`.

## Определение в виде кода на Scala

```dotty
trait MultiplicativeSemigroup[A]:
  def times(x: A, y: A): A

trait Semiring[A] extends CMonoid[A], MultiplicativeSemigroup[A]    
```

## Законы в виде кода на Scala

```dotty
```

## Примеры

### Числа относительно сложения с 0 и умножения

`(Z, +, *)`

```dotty
given Semiring[Int] with
  val empty                        = 0
  def combine(x: Int, y: Int): Int = x + y
  def times(x: Int, y: Int): Int   = x * y
```

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.Semiring
import spire.math.Rational

Semiring.plus(Rational(1, 2), Rational(1, 3))
// val res0: spire.math.Rational = 5/6
Semiring.times(Rational(1, 2), Rational(1, 3))
// val res1: spire.math.Rational = 1/6
Semiring.pow(Rational(1, 2), 3)
// val res2: spire.math.Rational = 1/8
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)
