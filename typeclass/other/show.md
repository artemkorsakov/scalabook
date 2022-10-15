# Show

Элементы `Show` могут быть представлены в виде строк.

## Описание

```scala
trait Show[F]:
  def show(f: F): String
```

## Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._
1.0.shows // "1.0"
```


---

## References

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Show.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Show.html)
