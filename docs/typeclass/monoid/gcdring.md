# Кольца НОД

## Формальное определение

Кольца НОД - [коммутативное кольцо с единицей](cring-with-one.md)
с добавлением наибольшего общего делителя и наименьшего общего кратного.

Кольца НОД поддерживают следующие операции:

- `gcd`: наибольший общий делитель
- `lcm`: наименьшее общее кратное

Для Кольца НОД должны соблюдаться законы родителей:

- _Замыкание по сложению_ (_closure_): для `∀ x, y ∈ S` выполняется `x + y ∈ S`
- _Ассоциативность по сложению_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x + y) + z = x + (y + z)`
- _Тождественность по сложению_ (_identity_): существует `∃ e ∈ S` такой, что для `∀ x ∈ S` выполняется `e + x = x + e = x`
- _Обратимость по сложению_: для каждого `x ∈ S` существует \\(x^{-1}\\) такой,
  что x + \\(x^{-1}\\) == \\(x^{-1}\\) + x == e
- _Коммутативность по сложению_ (_commutative_): для `∀ x, y ∈ S` выполняется `x + y = y + x`
- _Замыкание по умножению_ (_closure_): для `∀ x, y ∈ S` выполняется `x * y ∈ S`
- _Ассоциативность по умножению_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x * y) * z = x * (y * z)`
- _Тождественность по умножению_ (_identity_): существует `∃ one ∈ S` такой, что для `∀ x ∈ S` выполняется `one * x = x * one = x`
- _Коммутативность по умножению_ (_commutative_): для `∀ x, y ∈ S` выполняется `x * y = y * x`
- _Дистрибутивность_ (_distributivus_, _распределительный закон_):
  Для любых `∀ x, y, z ∈ S` выполняется `x * (y + z) = x * y + x * z` и `(x + y) * z = x * z + y * z`.

, а также следующие законы:

- `d * m === a * b` для `d = gcd(a, b)` и `m = lcm(a, b)`
- `gcd` ассоциативен и коммутативен
- `lcm` ассоциативен и коммутативен

## Определение в виде кода на Scala

```dotty
trait GCDRing[A] extends CRingWithUnity[A]:
  def gcd(x: A, y: A): A

  def lcm(x: A, y: A): A
```

## Законы в виде кода на Scala

```dotty
```

## Примеры

### Числа относительно сложения с 0 и умножения с 1

`(Z, +, *)`

```dotty
given GCDRing[Int] with
  val empty: Int                   = 0
  val one: Int                     = 1
  def combine(x: Int, y: Int): Int = x + y
  def times(x: Int, y: Int): Int   = x * y
  def gcd(x: Int, y: Int): Int =
    if y == 0 then x else gcd(y, x % y)
  def lcm(x: Int, y: Int): Int = times(x, y) / gcd(x, y)

  extension (a: Int) override def inverse: Int = -a
```

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.GCDRing
import spire.math.Rational
import spire.std.int.*

GCDRing.plus(Rational(1, 2), Rational(1, 3))
// val res0: spire.math.Rational = 5/6
GCDRing.times(Rational(1, 2), Rational(1, 3))
// val res1: spire.math.Rational = 1/6
GCDRing.pow(Rational(1, 2), 3)
// val res2: spire.math.Rational = 1/8
GCDRing.negate(Rational(1, 2))
// val res3: spire.math.Rational = -1/2
GCDRing.minus(Rational(1, 2), Rational(1, 3))
// val res4: spire.math.Rational = 1/6
GCDRing.zero[Rational]
// val res5: spire.math.Rational = 0
GCDRing.one[Rational]
// val res6: spire.math.Rational = 1
GCDRing.gcd(15, 10)
// val res7: Int = 5
GCDRing.lcm(15, 10)
// val res8: Int = 30
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)
