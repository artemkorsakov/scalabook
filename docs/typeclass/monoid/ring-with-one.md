# Кольцо с единицей

**Кольцо с единицей** — это кольцо, в котором существует нейтральный элемент относительно операции умножения. Этот элемент называется **единицей** и обозначается обычно как \\(1\\). Давайте разберём это подробнее.

---

### Определение кольца с единицей
Кольцо с единицей — это множество \\(R\\), на котором определены две бинарные операции:
1. **Сложение** (\\(+\\)): коммутативная и ассоциативная операция.
2. **Умножение** (\\(\cdot\\)): ассоциативная операция.

Эти операции должны удовлетворять следующим аксиомам:
1. **Коммутативность сложения**: \\(a + b = b + a\\) для любых \\(a, b \in R\\).
2. **Ассоциативность сложения**: \\((a + b) + c = a + (b + c)\\) для любых \\(a, b, c \in R\\).
3. **Наличие нулевого элемента**: существует элемент \\(0 \in R\\) такой, что \\(a + 0 = a\\) для любого \\(a \in R\\).
4. **Наличие противоположного элемента**: для любого \\(a \in R\\) существует элемент \\(-a \in R\\) такой, что \\(a + (-a) = 0\\).
5. **Ассоциативность умножения**: \\((a \cdot b) \cdot c = a \cdot (b \cdot c)\\) для любых \\(a, b, c \in R\\).
6. **Дистрибутивность умножения относительно сложения**:
  - \\(a \cdot (b + c) = a \cdot b + a \cdot c\\) (левая дистрибутивность),
  - \\((a + b) \cdot c = a \cdot c + b \cdot c\\) (правая дистрибутивность).
7. **Наличие единичного элемента**: существует элемент \\(1 \in R\\) такой, что \\(a \cdot 1 = 1 \cdot a = a\\) для любого \\(a \in R\\).

---

### Примеры колец с единицей
1. **Кольцо целых чисел \\(\mathbb{Z}\\)**:
  - Операции: сложение и умножение.
  - Нулевой элемент: \\(0\\).
  - Единичный элемент: \\(1\\).

2. **Кольцо действительных чисел \\(\mathbb{R}\\)**:
  - Операции: сложение и умножение.
  - Нулевой элемент: \\(0\\).
  - Единичный элемент: \\(1\\).

3. **Кольцо многочленов \\(\mathbb{R}\[x\]\\)**:
  - Операции: сложение и умножение многочленов.
  - Нулевой элемент: нулевой многочлен.
  - Единичный элемент: многочлен \\(1\\).

4. **Кольцо матриц \\(M_n(\mathbb{R})\\)**:
  - Операции: сложение и умножение матриц.
  - Нулевой элемент: нулевая матрица.
  - Единичный элемент: единичная матрица.

5. **Кольцо вычетов \\(\mathbb{Z}/n\mathbb{Z}\\)**:
  - Операции: сложение и умножение по модулю \\(n\\).
  - Нулевой элемент: \\(0\\).
  - Единичный элемент: \\(1\\).

---

### Зачем нужны кольца с единицей?
Кольца с единицей находят применение в различных областях:
1. **Алгебра**: изучаются как базовые алгебраические структуры.
2. **Теория чисел**: используются для анализа свойств чисел и их операций.
3. **Геометрия**: кольца многочленов применяются в алгебраической геометрии.
4. **Физика**: используются для описания симметрий в физических системах.
5. **Криптография**: кольца вычетов применяются в алгоритмах шифрования.

---

### Отличие кольца с единицей от кольца без единицы
Основное отличие заключается в наличии единичного элемента относительно умножения. В кольце с единицей существует элемент \\(1\\), который является нейтральным для умножения, то есть \\(a \cdot 1 = 1 \cdot a = a\\) для любого \\(a \in R\\). В кольце без единицы такого элемента может не быть.

---

### Итог
Кольцо с единицей — это кольцо, в котором существует нейтральный элемент относительно умножения. Оно широко используется в математике и её приложениях для моделирования и анализа различных систем.

## Формальное определение

Кольцо с единицей - это [кольцо](ring.md), которое имеет [мультипликативную единицу](semiring-with-one.md).

Для кольца с единицей должны соблюдаться все законы кольца:

- _Замыкание сложения_ (_closure_): для \\(\forall x, y \in R\\) выполняется \\(x + y \in R\\).
- _Ассоциативность сложения_ (_associativity_): для \\(\forall x, y, z \in R\\) выполняется \\((x + y) + z = x + (y + z)\\).
- _Существование нулевого элемента_: существует \\(\exists 0 \in R\\) такой,
  что для \\(\forall x \in R\\) выполняется \\(0 + x = x + 0 = x\\)
- _Обратимость сложения_: для \\(\forall x \in R\\) существует \\((-x)\\) такой,
  что \\(x + (-x) = (-x) + x = 0\\)
- _Коммутативность сложения_ (_commutative_): для \\(\forall x, y \in R\\) выполняется \\(x + y = y + x\\).
- _Замыкание умножения_ (_closure_): для \\(\forall x, y \in R\\) выполняется \\(x \* y \in R\\).
- _Ассоциативность умножения_ (_associativity_): для \\(\forall x, y, z \in R\\) выполняется \\((x \* y) \* z = x \* (y \* z)\\).
- _Дистрибутивность_ (_distributivus_, _распределительный закон_):
  для \\(\forall x, y, z \in R\\) выполняется \\(x \* (y + z) = x \* y + x \* z\\) и \\((x + y) \* z = x \* z + y \* z\\).

, а также закон тождественности по умножению:

- _Существование единичного элемента_: \\(\exists 1 \in R \quad \forall x \in R \quad x \* 1 = 1 \* x = x\\)

## Код

```dotty
trait RingWithUnity[A] extends Ring[A], SemiringWithUnity[A]
```

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

### Законы

```dotty
trait RingWithUnityLaw extends RingLaw, SemiringWithUnityLaw:
  def checkRingWithUnityLaw[A: RingWithUnity](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkRingLaw(x, y, z) combine checkSemiringWithUnityLaw(x, y, z)
```

## Схема

```mermaid
classDiagram
    class Semigroup~A~{
        +combine(x: A, y: A) A
    }  
    class Monoid~A~{
        +empty() A
    }
    Semigroup <|-- Monoid
    class CommutativeSemigroup~A~
    Semigroup <|-- CommutativeSemigroup  
    class Group~A~{
      +inverse(x: A) A
    }  
    Monoid <|-- Group 
    class CommutativeMonoid~A~
    Monoid <|-- CommutativeMonoid
    CommutativeSemigroup <|-- CommutativeMonoid
    class AbelianGroup~A~
    Group <|-- AbelianGroup
    CommutativeMonoid <|-- AbelianGroup
    class Semiring~A~
    CommutativeMonoid <|-- Semiring
    class MultiplicativeSemigroup~A~
    MultiplicativeSemigroup <|-- Semiring
    Semigroup .. MultiplicativeSemigroup
    class Ring~A~
    AbelianGroup <|-- Ring
    Semiring <|-- Ring
    class SemiringWithUnity~A~{
        +one() A
    }
    Semiring <|-- SemiringWithUnity
    class RingWithUnity~A~
    Ring <|-- RingWithUnity
    SemiringWithUnity <|-- RingWithUnity
```

## Реализация в библиотеках

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
