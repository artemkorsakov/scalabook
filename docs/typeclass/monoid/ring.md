# Кольцо

Кольцо (Ring) - это [полукольцо](semiring.md), являющееся [абелевой группой](abgroup.md) по сложению.

Учитывая множество `R` и операции `+` и `*`, 
мы говорим, что `(R, +, *)` - это кольцо, если оно удовлетворяет следующим свойствам:

- `(R, +)` является [абелевой группой](abgroup.md)
- Для любых `x` и `y ∈ R`: `x * y ∈ R`.
- Для любых `x`, `y` и `z ∈ R`: `(x * y) * z = x * (y * z)`.
- Для любых `x`, `y` и `z ∈ R`: `x * (y + z) = x * y + x * z` и `(x + y) * z = x * z + y * z`.

Законы по сложению:

- _Замыкание_ (_closure_): для `∀ x, y ∈ R` выполняется `x + y ∈ R`
- _Ассоциативность_ (_associativity_): для `∀ x, y, z ∈ R` выполняется `(x + y) + z = x + (y + z)`
- _Тождественность_ (_identity_): существует `∃ e ∈ R` такой, что для `∀ x ∈ R` выполняется `e + x = x + e = x`
- _Обратимость_: для каждого `x ∈ R` существует \\(x^{-1}\\) такой,
  что x * \\(x^{-1}\\) == \\(x^{-1}\\) * x == e
- _Коммутативность_ (_commutative_): для `∀ x, y ∈ R` выполняется `x + y = y + x`

Законы по умножению:

- _Замыкание_ (_closure_): для `∀ x, y ∈ R` выполняется `x * y ∈ R`
- _Ассоциативность_ (_associativity_): для `∀ x, y, z ∈ R` выполняется `(x * y) * z = x * (y * z)`

Законы по сложению и умножению:

- _Дистрибутивность_ (_distributivus_, _распределительный закон_):
  Для любых `∀ x, y, z ∈ R` выполняется `x * (y + z) = x * y + x * z` и `(x + y) * z = x * z + y * z`.

## Описание

```dotty
trait Ring[A] extends AbGroup[A], Semiring[A]
```

## Примеры

### Числа относительно сложения с 0 и умножения

`(Z, +, *)`

```dotty
given Ring[Int] with
  val empty                                    = 0
  def combine(x: Int, y: Int): Int             = x + y
  def times(x: Int, y: Int): Int               = x * y
  extension (a: Int) override def inverse: Int = -a
```

### Матрицы

Набор квадратных матриц заданного размера представляет собой кольцо.

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.Rng
import spire.math.Rational

Rng.plus(Rational(1, 2), Rational(1, 3))
// val res0: spire.math.Rational = 5/6
Rng.times(Rational(1, 2), Rational(1, 3))
// val res1: spire.math.Rational = 1/6
Rng.pow(Rational(1, 2), 3)
// val res2: spire.math.Rational = 1/8
Rng.negate(Rational(1, 2))
// val res3: spire.math.Rational = -1/2
Rng.minus(Rational(1, 2), Rational(1, 3))
// val res4: spire.math.Rational = 1/6
Rng.zero[Rational]
// val res5: spire.math.Rational = 0
```

---

**Ссылки:**

- [Algebird](https://twitter.github.io/algebird/typeclasses/ring.html)
- [Spire home page](https://typelevel.org/spire)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)
