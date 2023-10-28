# Идемпотентная полугруппа

_Band_ — это [полугруппа](semigroup.md), которая также является идемпотентной, 
т.е. добавление значения к самому себе приводит к тому же значению.

`Band` должна удовлетворять следующим законам:

Законы, наследуемые от полугруппы:

- _Замыкание_ (_closure_): для `∀ x, y ∈ S` выполняется `x + y ∈ M`
- _Ассоциативность_ (_associativity_): для `∀ x, y, z ∈ S` выполняется `(x + y) + z = x + (y + z)`

Законы идемпотентной полугруппы:

- _Идемпотентность_ (_idempotency_): для любых `a ∈ M` выполняется `a + a = a`

## Описание

```dotty
trait Semigroup[A]:
  def combine(x: A, y: A): A

  def combineN(x: A, n: Int): Option[A] =
    if n <= 0 then None
    else if n == 1 then Some(x)
    else combineN(x, n - 1).map(y => combine(x, y))

trait Band[A] extends Semigroup[A]:
  override def combineN(x: A, n: Int): Option[A] =
    if n <= 0 then None
    else Some(x)
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
