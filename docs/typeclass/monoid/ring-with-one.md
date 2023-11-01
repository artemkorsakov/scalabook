# Кольцо с единицей

Кольцо с единицей - это [кольцо](ring.md), которое имеет [мультипликативную единицу](semiring-with-one.md).

Для кольца с единицей должны соблюдаться все законы кольца:

- _Замыкание по сложению_ (_closure_): для `∀ x, y ∈ S` выполняется `x + y ∈ S`
- _Ассоциативность по сложению_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x + y) + z = x + (y + z)`
- _Тождественность по сложению_ (_identity_): существует `∃ e ∈ S` такой, что для `∀ x ∈ S` выполняется `e + x = x + e = x`
- _Обратимость по сложению_: для каждого `x ∈ S` существует \\(x^{-1}\\) такой,
  что x + \\(x^{-1}\\) == \\(x^{-1}\\) + x == e
- _Коммутативность по сложению_ (_commutative_): для `∀ x, y ∈ S` выполняется `x + y = y + x`
- _Замыкание по умножению_ (_closure_): для `∀ x, y ∈ S` выполняется `x * y ∈ S`
- _Ассоциативность по умножению_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x * y) * z = x * (y * z)`
- _Дистрибутивность_ (_distributivus_, _распределительный закон_):
  Для любых `∀ x, y, z ∈ S` выполняется `x * (y + z) = x * y + x * z` и `(x + y) * z = x * z + y * z`.

, а также закон тождественности по умножению:

- _Тождественность по умножению_ (_identity_): существует `∃ one ∈ S` такой, что для `∀ x ∈ S` выполняется `one * x = x * one = x`

## Описание

```dotty
trait RingWithUnity[A] extends Ring[A], SemiringWithUnity[A]
```

## Примеры

### Числа относительно сложения с 0 и умножения с 1

`(Z, +, *)`

```dotty
given RingWithUnity[Int] with
  val empty: Int                               = 0
  val one: Int                                 = 1
  def combine(x: Int, y: Int): Int             = x + y
  def times(x: Int, y: Int): Int               = x * y
  extension (a: Int) override def inverse: Int = -a
```

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.Ring
import spire.math.Rational

Ring.plus(Rational(1, 2), Rational(1, 3))
// val res0: spire.math.Rational = 5/6
Ring.times(Rational(1, 2), Rational(1, 3))
// val res1: spire.math.Rational = 1/6
Ring.pow(Rational(1, 2), 3)
// val res2: spire.math.Rational = 1/8
Ring.negate(Rational(1, 2))
// val res3: spire.math.Rational = -1/2
Ring.minus(Rational(1, 2), Rational(1, 3))
// val res4: spire.math.Rational = 1/6
Ring.zero[Rational]
// val res5: spire.math.Rational = 0
Ring.one[Rational]
// val res6: spire.math.Rational = 1
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)
