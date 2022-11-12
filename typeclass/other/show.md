# Show

Элементы `Show` могут быть представлены в виде строк.

## Описание

```scala
trait Show[A]:
  def show(a: A): String
```

## Реализация в ScalaZ

```scala
import scalaz.*
import Scalaz.*
1.0.shows // "1.0"
```

## Реализация в Cats

```scala
import cats.*
import cats.implicits.*
123.show     // 123
"abc".show   // abc
```


---

## References

- [Cats](https://typelevel.org/cats/typeclasses/show.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Show.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Show.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Show.html)
