# Полукольцо

## Формальное определение

В то время как [группа](group.md), [моноид](monoid.md) и [полугруппа](semigroup.md) 
определяются множеством и одной операцией, полукольцо определяется множеством и двумя операциями. 
Учитывая множество `R` и операции `+` и `*`, 
мы говорим, что `(R, +, *)` - это полукольцо, если оно удовлетворяет следующим свойствам:

- `(R, +)` является [коммутативным моноидом](cmonoid.md)
- `(R, *)` является [полугруппой](semigroup.md)

Для полугруппы должны соблюдаться законы коммутативного моноида по сложению:

- _Замыкание_ (_closure_): для \\(\forall x, y \in S\\) выполняется \\(x + y \in S\\).
- _Ассоциативность_ (_associativity_): для \\(\forall x, y, z \in S\\) выполняется \\((x + y) + z = x + (y + z)\\).
- _Тождественность_ или _существования нейтрального элемента_ (_identity_): существует \\(\exists e \in S\\) такой,
  что для \\(\forall x \in S\\) выполняется \\(e + x = x + e = x\\)
- _Коммутативность_ (_commutative_): для \\(\forall x, y \in S\\) выполняется \\(x + y = y + x\\).

и законы полугруппы по умножению:

- _Замыкание_ (_closure_): для \\(\forall x, y \in S\\) выполняется \\(x * y \in S\\).
- _Ассоциативность_ (_associativity_): для \\(\forall x, y, z \in S\\) выполняется \\((x * y) * z = x * (y * z)\\).

, а также дистрибутивность и мультипликативное свойство нуля:

- _Дистрибутивность_ (_distributivus_, _распределительный закон_):
  для \\(\forall x, y, z \in S\\) выполняется \\(x * (y + z) = x * y + x * z\\) и \\((x + y) * z = x * z + y * z\\).
- _Мультипликативное свойство нуля_: \\(a * 0 = 0 * a = 0\\)

### Связь с кольцом

Полукольцо — общеалгебраическая структура, похожая на [кольцо](ring.md), 
но без требования существования противоположного по сложению элемента.

Для [кольца](ring.md) последнее соотношение (_мультипликативное свойство нуля_) не требуется, 
поскольку оно следует из других, для полукольца оно необходимо.
Отличие полукольца от кольца состоит только в том, что по сложению полукольцо образует не коммутативную группу,
а только коммутативный моноид.

## Определение в виде кода на Scala

```dotty
trait MultiplicativeSemigroup[A]:
  def times(x: A, y: A): A

trait Semiring[A] extends CMonoid[A], MultiplicativeSemigroup[A]    
```

## Законы в виде кода на Scala

```dotty
trait MultiplicativeSemigroupLaw extends Laws:
  def checkMultiplicativeSemigroupLaw[A: MultiplicativeSemigroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    check(
      MultiplicativeSemigroup[A]
        .times(MultiplicativeSemigroup[A].times(x, y), z) ==
        MultiplicativeSemigroup[A]
          .times(x, MultiplicativeSemigroup[A].times(y, z)),
      "associative: (x * y) * z == x * (y * z)"
    )
    
trait SemiringLaw extends CMonoidLaw, MultiplicativeSemigroupLaw:
  def checkSemiringLaw[A: Semiring](x: A, y: A, z: A): Unit =
    (checkCMonoidLaw(x, y, z), checkMultiplicativeSemigroupLaw(x, y, z)).mapN {
      case (_, _) => ()
    }    
```

## Примеры

### Числа относительно сложения с 0 и умножения

`(Z, +, *)`

```dotty
given Semiring[Int] with
  val empty                        = 0
  def combine(x: Int, y: Int): Int = x + y
  def times(x: Int, y: Int): Int   = x * y
```

## Реализация

### Реализация в Spire

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

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
- [Spire User's Guide](https://typelevel.org/spire/guide.html)
- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D0%BB%D1%83%D0%BA%D0%BE%D0%BB%D1%8C%D1%86%D0%BE)
