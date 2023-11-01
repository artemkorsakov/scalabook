# Кольца и поля

Семейство классов типов `Ring` представляет собой совокупность двух двоичных операций (аддитивной и мультипликативной). 
Spire определяет их, расширяя соответствующие аддитивные и мультипликативные трейты групп.
Кольца также предоставляют метод `pow` (`**`) для выполнения повторного умножения.
В следующем списке описаны классы кольцевых типов, предоставляемые Spire:

## Semiring

`Semiring[A]` обеспечивает `+`, `zero` и `*`. 
`Semiring[A]` расширяет `AdditiveCommutativeMonoid[A]` и `MultiplicativeSemigroup[A]`.

- `plus` (`+`): сложение
- `times` (`*`): умножение
- `pow` (`**`): возведения в степень (интегральная экспонента)

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

## Rng

`Rng[A]` обеспечивает коммутативные `+`, `zero`, `-`, и `*`.
`Rng[A]` расширяет `Semiring[A]` и `AdditiveAbGroup[A]`.

- `negate` (`-`): обратная операция сложению
- `minus` (`-`): вычитание
- `zero`: аддитивное тождество (такой элемент, что для любого `x` из множества `x + zero = x`)

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

## Rig

`Rig[A]` обеспечивает `+`, `zero`, `*` и `one`.
`Rig[A]` расширяет `Semiring[A]` и `MultiplicativeMonoid[A]`.

- `zero`: аддитивное тождество
- `one`: мультипликативное тождество (такой элемент, что для любого `x` из множества `x * one = x`)

```dotty
import spire.algebra.Rig
import spire.math.Rational

Rig.plus(Rational(1, 2), Rational(1, 3))
// val res0: spire.math.Rational = 5/6
Rig.times(Rational(1, 2), Rational(1, 3))
// val res1: spire.math.Rational = 1/6
Rig.pow(Rational(1, 2), 3)
// val res2: spire.math.Rational = 1/8
Rig.zero[Rational]
// val res3: spire.math.Rational = 0
Rig.one[Rational]
// val res4: spire.math.Rational = 1
```

## Ring

`Ring[A]` обеспечивает коммутативные `+`, `zero`, `-`, `*` и `one`.
`Ring[A]` расширяет `Rig[A]` и `Rng[A]`.

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

## CRing

`CRing[A]` обеспечивает коммутативный `+`, `zero`, `-`, коммутативный `*` и `one`.
`CRing[A]` расширяет `Ring[A]` и `MultiplicativeCMonoid[A]`.

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
CRing.one[Rational]
// val res6: spire.math.Rational = 1
```

## GCDRing (кольца НОД)

Коммутативные кольца (в литературе их также называют _доменами_ (_domains_)) имеют богатую структуру.

`GCDRing[A]` — это коммутативные кольца (расширяет `CRing[A]`) 
с добавлением наибольшего общего делителя и наименьшего общего кратного.

`GCDRing[A]` поддерживает следующие операции:

- `gcd` (`a gcd b`): наибольший общий делитель
- `lcm` (`a lcm b`): наименьшее общее кратное

И подчиняется следующим законам:

- `d * m === a * b` для `d = gcd(a, b)` и `m = lcm(a, b)`,
- `gcd` ассоциативен и коммутативен,
- `lcm` ассоциативен и коммутативен.

Обратите внимание, что НОД определяется с точностью до делимого элемента (единицы); 
в частности, его знак является вопросом соглашения.

Spire требует, чтобы эти операции были коммутативными. 
Обратите внимание, что поля имеют свободу действий для определения операции НОД. 
На практике экземпляры `Field[A]` предоставляют либо тривиальную реализацию `gcd(x != 0, y != 0) == 1`, 
либо определение, расширяющее определение, 
используемое для целочисленного кольца (`gcd(a / b, c / d) == gcd(a, c) / lcm(b, d)`).

## EuclideanRing

Spire поддерживает евклидовы кольца (называемые `EuclideanRing[A]`). 
Евклидово кольцо — это кольцо НОД (расширяет `GCDRing[A]`), 
которое также поддерживает евклидово деление (например, поэтапное или целочисленное деление). 
Эта структура обобщает многие полезные свойства целых чисел (например, частное и остаток, а также наибольший общий делитель).

Формально евклидовы кольца определяют _евклидову функцию_ `f` такую, что для любого `x` и `y` в `A`, 
если `y` ненулевое, то существуют `q` и `r` (частное и остаток) такие, что `a = b*q + r` и `r = 0` или `f(r) < f(b)`. 
Для целых чисел обычно `f` - это функция абсолютного значения.

`EuclideanRing[A]` поддерживает следующие операции:

- `quot` (`/~`, `a /~ b`): частное (евклидово деление)
- `mod` (`%`, `a % b`): остаток
- `quotmod` (`/%`, `a /% b`): частное и остаток, объединяет `quot` и `mod` в одну операцию.

Spire требует, чтобы `b * (a /~ b) + (a % b)` было эквивалентно `a`.

В целых числах евклидово частное и остаток соответствуют делению с остатком; 
однако знак результата является вопросом соглашения. 
Для рациональных чисел (или с плавающей запятой) `a /~ b = a / b` и `a % b = 0` по определению.

## Field

Поля представляют собой коммутативные кольца с коммутативным умножением и мультипликативными обратными для всех ненулевых элементов.
`spire.Field[A]` расширяет `algebra.Field[A]` и `EuclideanRing[A]`.
Поля обобщают то, что большинство людей думает о рациональных числах.

`Field[A]` поддерживает следующие операции:

- `reciprocal` (`a.reciprocal`): обратная операция умножению. Мультипликативный обратный `a`, т.е. `one/a`.
- `div` (`/`, `a / b`): деление `a` на `b`.
- `ceil`: округление вверх
- `floor`: округление вниз
- `round`: округление до ближайшего

Несмотря на то, что поля находятся на вершине кольцевой иерархии, 
существует множество операций, которые поля не предоставляют:

- равенство и упорядоченность (обеспечиваемые `Eq[A]` и `Order[A]`).
- квадратный корень и другие корни (предоставленные `NRoot[A]`).
- синус, косинус и тригонометрические функции (предоставлены `Trig[A]`).

## Иррациональные и трансцендентальные классы типов

Spire поддерживает квадратные корни и дробные степени через `NRoot[A]`. 
Доступны следующие методы:

- `nroot` (`a nroot k`): корень k-й степени из `a` (тип k - `Int`)
- `sqrt` (`a.sqrt`): квадратный корень из `a`
- `fpow` (`**`, `a fpow b`): возведение в степень (принимает дробный показатель степени)

В Spire нет дробных типов, которые могли бы точно представлять иррациональные корни. 
Это означает, что многие законы, которые можно было бы написать о корнях, будут слабее, чем хотелось бы:

- `a.sqrt` = `(a nroot 2)` = `(a fpow 2.reciprocal)`
- если `A` может точно представить `1/k`, то `(a nroot k)` = `(a fpow k.reciprocal)`
- если `(a nroot k)` рационально, то `(a nroot k).pow(k)` = `a`

Типы приближенных значений, например `Double` и `BigDecimal`, содержат встроенную точность, 
с которой Spire может находить корни. 
Точные типы, например, `Rational` не имеют экземпляров `NRoot`, определенных по умолчанию, 
но экземпляры могут быть созданы с точностью, заданной пользователем.

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)
