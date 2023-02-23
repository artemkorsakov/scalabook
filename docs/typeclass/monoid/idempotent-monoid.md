# Idempotent Monoid

_IdempotentMonoid_ — это [моноид](https://scalabook.gitflic.space/docs/typeclass/monoid/monoid), 
который также является [идемпотентным](https://scalabook.gitflic.space/docs/typeclass/monoid/band),
т.е. добавление значения к самому себе приводит к тому же значению.

`IdempotentMonoid` должен удовлетворять законам своих "родителей": `Monoid`, `Band`, `Semigroup`.

## Описание
```scala
trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Monoid[A] extends Semigroup[A]:
  def empty: A

trait Band[A] extends Semigroup[A]

trait IdempotentMonoid[A] extends Monoid[A], Band[A]
```

## Примеры

### Множества

Некоторые операции над множествами являются идемпотентными: объединение, пересечение и т.п.
Например, объединение множеств образуют `IdempotentMonoid`:

```scala
given setIdempotentMonoidInstance[A]: IdempotentMonoid[Set[A]] with
  override def empty: Set[A] = Set.empty[A]
  override def combine(x: Set[A], y: Set[A]): Set[A] = x ++ y
```


---

**Ссылки:**
- [Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonoid%2FIdempotentMonoid.scala&plain=1)
- [Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonoid%2FIdempotentMonoidSuite.scala)
