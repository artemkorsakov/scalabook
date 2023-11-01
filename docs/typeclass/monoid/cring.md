# Коммутативное кольцо

Коммутативное кольцо - [кольцо](ring.md), коммутативное относительно умножения.

Для коммутативного кольца должны соблюдаться все законы кольца:

- _Замыкание по сложению_ (_closure_): для `∀ x, y ∈ S` выполняется `x + y ∈ S`
- _Ассоциативность по сложению_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x + y) + z = x + (y + z)`
- _Тождественность по сложению_ (_identity_): существует `∃ e ∈ S` такой, что для `∀ x ∈ S` выполняется `e + x = x + e = x`
- _Обратимость по сложению_: для каждого `x ∈ S` существует \\(x^{-1}\\) такой,
  что x * \\(x^{-1}\\) == \\(x^{-1}\\) * x == e
- _Коммутативность по сложению_ (_commutative_): для `∀ x, y ∈ S` выполняется `x + y = y + x`
- _Замыкание по умножению_ (_closure_): для `∀ x, y ∈ S` выполняется `x * y ∈ S`
- _Ассоциативность по умножению_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x * y) * z = x * (y * z)`
- _Дистрибутивность_ (_distributivus_, _распределительный закон_):
  Для любых `∀ x, y, z ∈ S` выполняется `x * (y + z) = x * y + x * z` и `(x + y) * z = x * z + y * z`.

, а также закон коммутативности по умножению:

- _Коммутативность по умножению_ (_commutative_): для `∀ x, y ∈ S` выполняется `x * y = y * x`

## Описание

```dotty
trait CRing[A] extends Ring[A]
```

## Примеры

### Числа относительно сложения с 0 и умножения

`(Z, +, *)`

```dotty
given CRing[Int] with
  val empty                                    = 0
  def combine(x: Int, y: Int): Int             = x + y
  def times(x: Int, y: Int): Int               = x * y
  extension (a: Int) override def inverse: Int = -a
```

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.CRing
import spire.math.Rational

CRing.plus(Rational(1, 2), Rational(1, 3))
// val res0: spire.math.Rational = 5/6
CRing.times(Rational(1, 2), Rational(1, 3))
// val res1: spire.math.Rational = 1/6
CRing.pow(Rational(1, 2), 3)
// val res2: spire.math.Rational = 1/8
CRing.negate(Rational(1, 2))
// val res3: spire.math.Rational = -1/2
CRing.minus(Rational(1, 2), Rational(1, 3))
// val res4: spire.math.Rational = 1/6
CRing.zero[Rational]
// val res5: spire.math.Rational = 0
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)
