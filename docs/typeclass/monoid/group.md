# Group

Группа **G** — это [моноид](monoid.md) 
с обратным **g^(−1)** для каждого элемента **g**. 
Таким образом, **G** — категория с одним объектом, в которой каждая стрелка — изоморфизм.

Группа, помимо законов моноида, должна удовлетворять закону:
- **g * g^(−1) == G.empty**

## Описание 

```scala
trait Group[A] extends Monoid[A]:
  extension (a: A) 
    def inverse: A
```

## Примеры

### Числа относительно сложения с 0

```scala
given Group[Int] with
  override val empty                           = 0
  override def combine(x: Int, y: Int): Int    = x + y
  extension (a: Int) override def inverse: Int = -a
```

## Реализация

### Реализация в Cats

```scala
import cats.*, cats.syntax.all.*

1.inverse() // -1

assert((1 |+| 1.inverse()) === Monoid[Int].empty)
```

---

**Ссылки:**

- [Herding Cats](http://eed3si9n.com/herding-cats/Grp.html)
