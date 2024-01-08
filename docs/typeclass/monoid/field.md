# Поле

## Формальное определение

Поля расширяют [Евклидово кольцо](euclidean-ring.md), добавляя операцию обратную умножению для всех элементов,
за исключением нуля по сложению.

`Field[A]` поддерживает следующие операции:

- `reciprocal` (`a.reciprocal`): обратная операция умножению. Мультипликативный обратный `a`, т.е. `one/a`.
- `div` (`/`, `a / b`): деление `a` на `b`.

Формально, _поле_ — алгебра над множеством \\(\mathbf{F}\\), 
образующая [коммутативную группу](abgroup.md) по сложению \\(\mathbf{+}\\) над \\(\mathbf{F}\\) с нейтральным элементом \\(\mathbf{0}\\) 
и коммутативную группу по умножению \\(\mathbf{\*}\\) над ненулевыми элементами \\(\mathbf{F} \backslash \begin{Bmatrix} \mathbf{0} \end{Bmatrix}\\), 
при выполняющемся свойстве дистрибутивности умножения относительно сложения. 
Подразумевается также применимость операции \\(\mathbf{\*}\\) к нулевому элементу по сложению 
с сохранением свойства дистрибутивности на всём множестве \\(\mathbf{F}\\).

Если раскрыть определение, то множество \\(\mathbf{F}\\) с введёнными на нём алгебраическими операциями 
сложения \\(\mathbf{+}\\) и умножения \\(\mathbf{\*}\\)
(\\(\mathbf{+}\colon \mathbf{F} \times \mathbf{F} \to \mathbf{F},\mathbf{\*}\colon \mathbf{F} \times \mathbf{F} \to \mathbf{F}\\), 
то есть \\(\forall a, b \in F \quad (a + b) \in F, (a \* b) \in F\\) 
называется _полем_ \\(\left< F, +, \* \right>\\), если выполнены следующие аксиомы:

- _Ассоциативность сложения_: для \\(\forall x, y, z \in F\\) выполняется \\((x + y) + z = x + (y + z)\\).
- _Существование нулевого элемента_: существует \\(\exists 0 \in F\\) такой,
  что для \\(\forall x \in F\\) выполняется \\(0 + x = x + 0 = x\\)
- _Обратимость сложения_: для \\(\forall x \in F\\) существует \\((-x)\\) такой,
  что \\(x + (-x) = (-x) + x = 0\\)
- _Коммутативность сложения_: для \\(\forall x, y \in F\\) выполняется \\(x + y = y + x\\).
- _Ассоциативность умножения_: для \\(\forall x, y, z \in F\\) выполняется \\((x \* y) \* z = x \* (y \* z)\\).
- _Существование единичного элемента_: \\(\exists 1 \in F \quad \forall x \in F \quad x \* 1 = 1 \* x = x\\)
- _Коммутативность умножения_: для \\(\forall x, y \in F\\) выполняется \\(x \* y = y \* x\\).
- _Существование обратного элемента для ненулевых элементов_: \\((\forall x \in F: x \neq 0) \quad \exists x^{-1} \in F: \quad x \* x^{-1} = 1\\).
- _Дистрибутивность умножения относительно сложения_:
  для \\(\forall x, y, z \in F\\) выполняется \\(x \* (y + z) = x \* y + x \* z\\) и \\((x + y) \* z = x \* z + y \* z\\).

## Определение в виде кода на Scala

```dotty
trait Field[A] extends EuclideanRing[A]:
  extension (a: A) def reciprocal: A

  def div(x: A, y: A): A = times(x, y.reciprocal)
```

## Законы в виде кода на Scala

```dotty
trait FieldLaw extends EuclideanRingLaw:
  def checkFieldLaw[A: Field](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkEuclideanRingLaw(x, y, z) combine
      check(
        x == Field[A].empty || Field[A].times(x, x.reciprocal) == Field[A].one,
        "x * x^{-1} = 1"
      )
```

## Примеры

### Рациональные числа со сложением и умножением

```dotty
import spire.math.Rational

given Field[Rational] with
  val empty: Rational = Rational(0)
  val one: Rational   = Rational(1)
  def combine(x: Rational, y: Rational): Rational = x + y
  def times(x: Rational, y: Rational): Rational = x * y
  def gcd(x: Rational, y: Rational): Rational = one
  def lcm(x: Rational, y: Rational): Rational = times(x, y)
  def quot(x: Rational, y: Rational): Rational = x / y
  def mod(x: Rational, y: Rational): Rational = empty

  extension (a: Rational)
    def inverse: Rational    = Rational(-a.numerator, a.denominator)
    def reciprocal: Rational = Rational(a.denominator, a.numerator)
end given
```

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
- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D0%BB%D0%B5_(%D0%B0%D0%BB%D0%B3%D0%B5%D0%B1%D1%80%D0%B0))
