# Идемпотентная полугруппа

## Формальное определение

_Band_ — это [полугруппа](semigroup.md), которая также является идемпотентной, 
т.е. добавление значения к самому себе приводит к тому же значению.

`Band` должна удовлетворять следующим законам:

- _Замыкание_ (_closure_): для \\(\forall x, y \in B\\) выполняется \\(x + y \in B\\).
- _Ассоциативность_ (_associativity_): для \\(\forall x, y, z \in B\\) выполняется \\((x + y) + z = x + (y + z)\\).
- _Идемпотентность_ (_idempotency_): для \\(\forall x \in B\\) выполняется \\(x + x = x\\).

> Первые два закона наследуются от полугруппы.

## Определение в виде кода на Scala

```dotty
trait Band[A] extends Semigroup[A]:
  override def combineN(x: A, n: Int :| Greater[0]): A = x
```

## Законы в виде кода на Scala

```dotty
trait BandLaw extends SemigroupLaw:
  def checkBandLaw[A: Band](x: A, y: A, z: A): ValidatedNel[String, Unit] =
    checkSemigroupLaw(x, y, z) combine
      check(Band[A].combine(x, x) == x, "idempotency: x + x = x")
```

## Примеры

### Множества

Некоторые операции над множествами являются идемпотентными: объединение, пересечение и т.п.
Например, объединение множеств образуют `Band`:

```dotty
given [A]: Band[Set[A]] =
  (x: Set[A], y: Set[A]) => x ++ y
```

---

**Ссылки:**

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Band.html)
