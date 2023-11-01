# Евклидово кольцо

Евклидово кольцо — это [кольцо НОД](gcdring.md) (расширяет `GCDRing[A]`),
которое также поддерживает евклидово деление (например, поэтапное или целочисленное деление).

Формально евклидовы кольца определяют _евклидову функцию_ `f` такую, что для любого `x` и `y` в `A`,
если `y` ненулевое, то существуют `q` и `r` (частное и остаток) такие, что `a = b*q + r` и `r = 0` или `f(r) < f(b)`.
Для целых чисел обычно `f` - это функция абсолютного значения.

`EuclideanRing[A]` добавляет следующие операции:

- `quot` (`/~`, `a /~ b`): частное (евклидово деление)
- `mod` (`%`, `a % b`): остаток

В целых числах евклидово частное и остаток соответствуют делению с остатком;
однако знак результата является вопросом соглашения.
Для рациональных чисел (или с плавающей запятой) `a /~ b = a / b` и `a % b = 0` по определению.

Помимо законов кольца НОД:

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
- `d * m === a * b` для `d = gcd(a, b)` и `m = lcm(a, b)`
- `gcd` ассоциативен и коммутативен
- `lcm` ассоциативен и коммутативен

должен соблюдаться закон:

- `b * (a /~ b) + (a % b) = a`

## Описание

```dotty
trait EuclideanRing[A] extends GCDRing[A]:
  def quot(x: A, y: A): A

  def mod(x: A, y: A): A
```

## Примеры

### Числа относительно сложения с 0 и умножения с 1

`(Z, +, *)`

```dotty
given EuclideanRing[Int] with
  val empty: Int                   = 0
  val one: Int                     = 1
  def combine(x: Int, y: Int): Int = x + y
  def times(x: Int, y: Int): Int   = x * y
  def gcd(x: Int, y: Int): Int = if y == 0 then x else gcd(y, x % y)
  def lcm(x: Int, y: Int): Int                 = times(x, y) / gcd(x, y)
  def quot(x: Int, y: Int): Int                = x / y
  def mod(x: Int, y: Int): Int                 = x % y
  extension (a: Int) override def inverse: Int = -a
```

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.EuclideanRing
import spire.std.int.*

EuclideanRing.plus(15, 10)
// val res0: Int = 25
EuclideanRing.times(15, 10)
// val res1: Int = 150
EuclideanRing.pow(15, 2)
// val res2: Int = 225
EuclideanRing.negate(15)
// val res3: Int = -15
EuclideanRing.minus(15, 10)
// val res4: Int = 5
EuclideanRing.zero[Int]
// val res5: Int = 0
EuclideanRing.one[Int]
// val res6: Int = 1
EuclideanRing.gcd(15, 10)
// val res7: Int = 5
EuclideanRing.lcm(15, 10)
// val res8: Int = 30
EuclideanRing.equot(15, 10)
// val res9: Int = 1
EuclideanRing.emod(15, 10)
// val res10: Int = 5
EuclideanRing.equotmod(15, 10)
// val res11: (Int, Int) = (1,5)
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)
