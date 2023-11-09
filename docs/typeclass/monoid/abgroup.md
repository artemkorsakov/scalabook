# Абелева группа

## Формальное определение

`AbGroup[A]` - "абелева группа", [группа](group.md), которая является [коммутативной](cmonoid.md).

Помимо законов группы:

- _Замыкание_ (_closure_): для \\(\forall x, y \in G\\) выполняется \\(x + y \in G\\).
- _Ассоциативность_ (_associativity_): для \\(\forall x, y, z \in G\\) выполняется \\((x + y) + z = x + (y + z)\\).
- _Тождественность_ или _существования нейтрального элемента_ (_identity_): существует \\(\exists e \in G\\) такой, 
  что для \\(\forall x \in G\\) выполняется \\(e + x = x + e = x\\)
- _Обратимость_: для \\(\forall x \in G\\) существует \\(x^{-1} \in G\\) такой, что \\(x + x^{-1} = x^{-1} + x = e\\)

должен соблюдаться закон:

- _Коммутативность_ (_commutative_): для \\(\forall x, y \in G\\) выполняется \\(x + y = y + x\\).

## Определение в виде кода на Scala

```dotty
trait AbGroup[A] extends Group[A], CMonoid[A]
```

## Законы в виде кода на Scala

```dotty
trait AbGroupLaw extends GroupLaw, CMonoidLaw:
  def checkAbGroupLaw[A: AbGroup](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    (checkGroupLaw(x, y, z), checkCMonoidLaw(x, y, z))
      .mapN { case (_, _) =>
        ()
      }
```

## Примеры

### Целые числа **Z** являются абелевой группой по сложению

```dotty
given AbGroup[Int] with
  override val empty                           = 0
  override def combine(x: Int, y: Int): Int    = x + y
  extension (a: Int) override def inverse: Int = -a
```

## Реализация

### Реализация в Spire

```dotty
import spire.algebra.AbGroup

AbGroup[Int].inverse(1) // -1
```

---

**Ссылки:**

- [Spire home page](https://typelevel.org/spire)
- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%90%D0%B1%D0%B5%D0%BB%D0%B5%D0%B2%D0%B0_%D0%B3%D1%80%D1%83%D0%BF%D0%BF%D0%B0)

