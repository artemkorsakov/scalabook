# Category

## Формальное определение

`Category` - это [`Compose`](compose.md) с операцией `identity`.
`Category` можно трансформировать в [Monoid](../monoid/monoid.md) 
и [PlusEmpty](../monad/plus-empty.md).

`Category` должен удовлетворять следующим законам (помимо законов родительских классов типов):

- Identity (тождественность): существует `e ∈ M` такое, что `e + x = x + e = x`

#### Тождественность

Каждый объект имеет специальную стрелку, называемую _тождественной_ (или _тождественностью_),
которая оставляет объект неизменным.
Это означает, что когда вы компонуете тождественную стрелку с любой другой стрелкой, входящей или исходящей,
то получаете ту же стрелку. Тождественная стрелка ничего не делает с другой стрелкой.

Тождественная стрелка на объекте a обозначается как \\(id\_{a}\\).
Так что, если имеется стрелка \\(f : a \to b\\),
то можно скомпоновать ее с тождественностями с обеих сторон: \\(id\_{b} \circ f = f = f \circ id\_{a}\\)

Возьмем элемент \\(x : 1 \to a\\) и скомпонуем его с \\(id_{a}\\).
Результат \\(id\_{a} \circ x = x\\) означает, что тождественность оставляет элементы неизменными.

## Определение в виде кода на Scala

```dotty
trait Category[=>:[_, _]] extends Compose[=>:]:
  def id[A]: A =>: A

  def empty: PlusEmpty[[A] =>> A =>: A] = new PlusEmpty[[A] =>> A =>: A] with ComposePlus:
    def empty[A]: A =>: A = id

  def monoid[A]: Monoid[A =>: A] = new Monoid[A =>: A] with ComposeSemigroup[A]:
    def empty: A =>: A = id
```

## Примеры

### Функция от одной переменной

```dotty
given Category[Function1] with
  override def compose[A, B, C](f: B => C, g: A => B): A => C = g andThen f

  override def id[A]: A => A = a => a
```

## Реализация

### Реализация в ScalaZ

```dotty
import scalaz.*
import Scalaz.*

// ... Все операции родителей
```

---

**Ссылки:**

- [Herding Cats](http://eed3si9n.com/herding-cats/Arrow.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Arrow.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Category.html)
