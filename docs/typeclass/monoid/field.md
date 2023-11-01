# Поле

Поля расширяют [Евклидово кольцо](euclidean-ring.md), добавляя операцию обратную умножению.

`Field[A]` поддерживает следующие операции:

- `reciprocal` (`a.reciprocal`): обратная операция умножению. Мультипликативный обратный `a`, т.е. `one/a`.
- `div` (`/`, `a / b`): деление `a` на `b`.

Помимо законов [Евклидова кольца](euclidean-ring.md):

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
- `b * (a /~ b) + (a % b) = a`

должен соблюдаться закон:

- _Обратимость по умножению_: для каждого `x ∈ S` существует \\(x^{-1}\\) такой,
  что x * \\(x^{-1}\\) == \\(x^{-1}\\) * x == one. Или `a * a.reciprocal = one`

## Описание

```dotty
trait Field[A] extends EuclideanRing[A]:
  extension (a: A) def reciprocal: A

  def div(x: A, y: A): A = times(x, y.reciprocal)
```

## Примеры

### Рациональные числа со сложением и умножением

## Реализация

### Реализация в Spire

```dotty
import spire.math.Real.apply

15.reciprocal
// val res0: spire.math.Real = 1/15
15 / 3
// val res1: Int = 5
15.24.ceil
// val res2: spire.math.Real = 16
15.24.floor
// val res3: spire.math.Real = 15
15.24.round
// val res4: spire.math.Real = 15
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)
