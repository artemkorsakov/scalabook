# Коммутативное полукольцо

## Формальное определение

Коммутативное полукольцо - это [полукольцо](semiring.md), с коммутативной операцией умножения:
для \\(\forall x, y \in S\\) выполняется \\(x * y = y * x\\).

Для полукольца с единицей должны соблюдаться все законы полукольца:

- _Замыкание сложения_ (_closure_): для \\(\forall x, y \in S\\) выполняется \\(x + y \in S\\).
- _Ассоциативность сложения_ (_associativity_): для \\(\forall x, y, z \in S\\) выполняется \\((x + y) + z = x + (y + z)\\).
- _Существование нулевого элемента_: существует \\(\exists 0 \in S\\) такой,
  что для \\(\forall x \in S\\) выполняется \\(0 + x = x + 0 = x\\)
- _Коммутативность сложения_ (_commutative_): для \\(\forall x, y \in S\\) выполняется \\(x + y = y + x\\).
- _Замыкание умножения_ (_closure_): для \\(\forall x, y \in S\\) выполняется \\(x \* y \in S\\).
- _Ассоциативность умножения_ (_associativity_): для \\(\forall x, y, z \in S\\) выполняется \\((x \* y) \* z = x \* (y \* z)\\).
- _Дистрибутивность_ (_distributivus_, _распределительный закон_):
  для \\(\forall x, y, z \in S\\) выполняется \\(x \* (y + z) = x \* y + x \* z\\) и \\((x + y) \* z = x \* z + y \* z\\).
- _Мультипликативное свойство нуля_: \\(a \* 0 = 0 \* a = 0\\)

, а также закон коммутативности умножения:

- _Коммутативность умножения_ (_commutative_): для \\(\forall x, y \in S\\) выполняется \\(x \* y = y \* x\\).

## Определение в виде кода на Scala

```dotty
trait CSemiring[A] extends Semiring[A]
```

## Законы в виде кода на Scala

```dotty
trait CSemiringLaw extends SemiringLaw:
  def checkCSemiringLaw[A: CSemiring](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    checkSemiringLaw(x, y, z) combine
      check(
        CSemiring[A].times(x, y) == CSemiring[A].times(y, x),
        "commutative: x * y = y * x"
      )
```

## Примеры

### Числа относительно сложения с 0 и умножения с 1

`(Z, +, *)`

```dotty
given CSemiring[Int] with
  val empty: Int                   = 0
  def combine(x: Int, y: Int): Int = x + y
  def times(x: Int, y: Int): Int   = x * y
```

## Реализация

---

**Ссылки:**

- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D0%BB%D1%83%D0%BA%D0%BE%D0%BB%D1%8C%D1%86%D0%BE)
