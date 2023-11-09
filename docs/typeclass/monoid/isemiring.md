#  Идемпотентное полукольцо

## Формальное определение

_IdempotentMonoid_ — это [моноид](monoid.md), 
который также является [идемпотентным](band.md),
т.е. добавление значения к самому себе приводит к тому же значению.

`IdempotentMonoid` должен удовлетворять законам своих "родителей":

- _Замыкание_ (_closure_): для \\(\forall x, y \in IM\\) выполняется \\(x + y \in IM\\).
- _Ассоциативность_ (_associativity_): для \\(\forall x, y, z \in IM\\) выполняется \\((x + y) + z = x + (y + z)\\).
- _Тождественность_ или _существования нейтрального элемента_ (_identity_):
  существует \\(\exists e \in IM\\) такой, что для \\(\forall x \in IM\\) выполняется \\(e + x = x + e = x\\)
- _Идемпотентность_ (_idempotency_): для \\(\forall x \in IM\\) выполняется \\(x + x = x\\).

## Определение в виде кода на Scala

```dotty
trait IdempotentMonoid[A] extends Monoid[A], Band[A]
```

## Законы в виде кода на Scala

```dotty
trait IdempotentMonoidLaw extends MonoidLaw, BandLaw:
  def checkIdempotentMonoidLaw[A: IdempotentMonoid](
      x: A,
      y: A,
      z: A
  ): ValidatedNel[String, Unit] =
    (checkMonoidLaw(x, y, z), checkBandLaw(x, y, z)).mapN { case (_, _) => () }
```

## Примеры

### Множества

Некоторые операции над множествами являются идемпотентными: объединение, пересечение и т.п.
Например, объединение множеств образуют `IdempotentMonoid`:

```dotty
given [A]: IdempotentMonoid[Set[A]] with
  override def empty: Set[A] = Set.empty[A]
  override def combine(x: Set[A], y: Set[A]): Set[A] = x ++ y
```

---

**Ссылки:**

- [Wikipedia](https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D0%BB%D1%83%D0%BA%D0%BE%D0%BB%D1%8C%D1%86%D0%BE)
