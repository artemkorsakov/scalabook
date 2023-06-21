# Band

_Band_ — это [полугруппа](semigroup.md), которая также является идемпотентной, 
т.е. добавление значения к самому себе приводит к тому же значению.

`Band` должна удовлетворять следующим законам (+ закон `Semigroup`):

- Associativity (ассоциативность): `(x + y) + z = x + (y + z)`
- Idempotency (идемпотентность): для любых `a ∈ M` такое, что `a + a = a`

## Описание

```dotty
trait Semigroup[A]:
  def combine(x: A, y: A): A

trait Band[A] extends Semigroup[A]
```

## Примеры

### Множества

Некоторые операции над множествами являются идемпотентными: объединение, пересечение и т.п.
Например, объединение множеств образуют `Band`:

```dotty
given setBandInstance[A]: Band[Set[A]] =
  (x: Set[A], y: Set[A]) => x ++ y
```


---

**Ссылки:**

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Band.html)
