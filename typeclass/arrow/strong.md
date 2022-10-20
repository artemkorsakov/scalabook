# Strong

`Strong` - сила в объединении. `Strong` расширяет `Profunctor` и добавляет операции `first` и `second`,
добавляющие заданное значение слева или справа от функции 
`A =>: B => (A, C) =>: (B, C)` или `A =>: B => (C, A) =>: (C, B)`.

`Strong` должен [удовлетворять следующим законам](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Farrow%2FStrongLaw.scala):
- при отображении функции swap слева и справа на `second` получим `first`: `dimap(second)(_.swap)(_.swap) == first`
- верно и обратное: `dimap(first)(_.swap)(_.swap) == second`
- `lmap fst == rmap fst . first'`
- и `lmap snd == rmap snd . second'`
- `lmap (second f) . first == rmap (second f) . first`
- и `lmap (first f) . second == rmap (first f) . second`
- `first' . first' == dimap assoc unassoc . first'`, где `assoc ((a,b),c) = (a,(b,c))` и `unassoc (a,(b,c)) = ((a,b),c)`
- и `second' . second' == dimap unassoc assoc . second'`


## Описание

```scala
trait Strong[=>:[_, _]] extends Profunctor[=>:]:
  def first[A, B, C](fa: A =>: B): (A, C) =>: (B, C)

  def second[A, B, C](fa: A =>: B): (C, A) =>: (C, B) =
    dimap[(A, C), (B, C), (C, A), (C, B)](first(fa))(_.swap)(_.swap)
```

## Примеры

### Функция от одной переменной

```scala
given Strong[Function1] with
  override def mapfst[A, B, C](fab: A => B)(f: C => A): C => B = f andThen fab

  override def mapsnd[A, B, C](fab: A => B)(f: B => C): A => C = fab andThen f

  override def first[A, B, C](fa: A => B): ((A, C)) => (B, C) = (a, c) => (fa(a), c)

  override def second[A, B, C](fa: A => B): ((C, A)) => (C, B) = (c, a) => (c, fa(a))
```

## Исходный код

[Исходный код](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Fmain%2Fscala%2Ftypeclass%2Farrow%2FStrong.scala&plain=1)

[Тесты](https://gitflic.ru/project/artemkorsakov/scalabook/blob?file=examples%2Fsrc%2Ftest%2Fscala%2Ftypeclass%2Farrow%2FStrongSuite.scala)


## Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._

val plus1 = (_: Int) + 1

plus1.first apply (7 -> "abc")    // (8,abc)
plus1.second apply ("def" -> 14)  // (def,15)
```


---

## References

- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Strong.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Arrow.html)
