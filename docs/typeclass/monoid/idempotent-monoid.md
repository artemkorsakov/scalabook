#  Идемпотентная моноид

_IdempotentMonoid_ — это [моноид](monoid.md), 
который также является [идемпотентным](band.md),
т.е. добавление значения к самому себе приводит к тому же значению.

`IdempotentMonoid` должен удовлетворять законам своих "родителей": `Monoid`, `Band`, `Semigroup`.

## Описание

```dotty
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

```dotty
given setIdempotentMonoidInstance[A]: IdempotentMonoid[Set[A]] with
  override def empty: Set[A] = Set.empty[A]
  override def combine(x: Set[A], y: Set[A]): Set[A] = x ++ y
```


---

**Ссылки:**
