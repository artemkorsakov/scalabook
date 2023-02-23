# Group

Группа **G** — это [моноид](monoid) с обратным **g<sup>−1</sup>** для каждого элемента **g**. 
Таким образом, **G** — категория с одним объектом, в которой каждая стрелка — изоморфизм.

Группа, помимо законов моноида, должна удовлетворять закону:
- **g * g<sup>−1</sup> == G.empty**

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


## Реализация в Cats

```scala
import cats.*, cats.syntax.all.*

1.inverse() // -1

assert((1 |+| 1.inverse()) === Monoid[Int].empty)
```


---

**Ссылки:**

- [Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Fmonoid%2FGroup.scala&plain=1)
- [Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Fmonoid%2FGroupSuite.scala)
- [Herding Cats](http://eed3si9n.com/herding-cats/Grp.html)
