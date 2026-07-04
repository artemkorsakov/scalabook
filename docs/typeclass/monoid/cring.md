{%
showPrev = true
prevPage = crig.html
showNext = true
nextPage = gcdring.html
%}

# Коммутативное кольцо

Коммутативное кольцо с единицей объединяет в себе [коммутативное кольцо без единицы](crng.md) и [кольцо с единицей](ring.md).

Для коммутативного кольца с единицей должны соблюдаться следующие законы:

- _Замыкание сложения_ (_closure_): для \\(\forall x, y \in R\\) выполняется \\(x + y \in R\\).
- _Ассоциативность сложения_ (_associativity_): для \\(\forall x, y, z \in R\\) выполняется \\((x + y) + z = x + (y + z)\\).
- _Существование нулевого элемента_: существует \\(\exists 0 \in R\\) такой,
  что для \\(\forall x \in R\\) выполняется \\(0 + x = x + 0 = x\\)
- _Обратимость сложения_: для \\(\forall x \in R\\) существует \\((-x)\\) такой,
  что \\(x + (-x) = (-x) + x = 0\\)
- _Коммутативность сложения_ (_commutative_): для \\(\forall x, y \in R\\) выполняется \\(x + y = y + x\\).
- _Замыкание умножения_ (_closure_): для \\(\forall x, y \in R\\) выполняется \\(x \* y \in R\\).
- _Ассоциативность умножения_ (_associativity_): для \\(\forall x, y, z \in R\\) выполняется \\((x \* y) \* z = x \* (y \* z)\\).
- _Существование единичного элемента_: \\(\exists 1 \in R \quad \forall x \in R \quad x \* 1 = 1 \* x = x\\)
- _Коммутативность умножения_: для \\(\forall x, y \in R\\) выполняется \\(x \* y = y \* x\\).
- _Дистрибутивность_ (_distributivus_, _распределительный закон_):
  для \\(\forall x, y, z \in R\\) выполняется \\(x \* (y + z) = x \* y + x \* z\\) и \\((x + y) \* z = x \* z + y \* z\\).

## Код

```dotty
trait CommutativeRing[A] extends Ring[A], CommutativeRng[A], CommutativeRig[A]
```

### Законы

Законы наследуются от [законов коммутативного кольца](crng.md) и [кольца с единицей](ring.md).

## Упражнения

### Упражнение 1.6.1: Реализация коммутативного кольца с единицей для целых чисел.

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

Заполните пропущенные части (`???`):

```dotty
given CommutativeRing[Int] with
  val empty: Int = ???

  val one: Int = ???

  def combine(x: Int, y: Int): Int = ???

  def times(x: Int, y: Int): Int = ???

  extension (a: Int)
    def inverse: Int = ???
```

@:note(🔍 Показать решение)

```dotty
given CommutativeRing[Int] with
  val empty: Int = 0

  val one: Int = 1

  def combine(x: Int, y: Int): Int = x + y

  def times(x: Int, y: Int): Int = x * y

  extension (a: Int)
    def inverse: Int = -a
```

**Примеры вызовов**:

```dotty
CommutativeRing[Int].combine(5, 3)
// 8
CommutativeRing[Int].empty
// 0
CommutativeRing[Int].inverse(5)
// -5
CommutativeRing[Int].times(5, 3)
// 15
CommutativeRing[Int].one
// 1
```

@:@

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
    class MultiplicativeSemigroup~A~{
        +times(x: A, y: A) A
    }
    MultiplicativeSemigroup <|-- Semiring
    Semigroup .. MultiplicativeSemigroup
    class Rng~A~
    AbelianGroup <|-- Rng
    Semiring <|-- Rng
    class CommutativeSemiring~A~
    Semiring <|-- CommutativeSemiring
    class Rig~A~{
        +one() A
    }
    Semiring <|-- Rig
    class CommutativeRng~A~
    Rng <|-- CommutativeRng
    CommutativeSemiring <|-- CommutativeRng
    class CommutativeRig~A~
    Rig <|-- CommutativeRig
    CommutativeSemiring <|-- CommutativeRig
    class Ring~A~
    Rng <|-- Ring
    Rig <|-- Ring
    class CommutativeRing~A~
    Ring <|-- CommutativeRing
    CommutativeRig <|-- CommutativeRing
    CommutativeRng <|-- CommutativeRing
```

## Реализация в библиотеках

### [Реализация в Spire](../../libs/spire.md)

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

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Spire home page](https://typelevel.org/spire/)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)

[sample]: https://gitverse.ru/scalabook/typeclasses
