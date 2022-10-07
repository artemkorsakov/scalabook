# Equal

Класс типов `Equal` описывает тип, который можно сравнивать на наблюдаемое равенство.

`Equal` должен следовать [следующим законам](https://ru.wikipedia.org/wiki/%D0%A2%D0%BE%D0%B6%D0%B4%D0%B5%D1%81%D1%82%D0%B2%D0%BE_%D0%BD%D0%B5%D1%80%D0%B0%D0%B7%D0%BB%D0%B8%D1%87%D0%B8%D0%BC%D1%8B%D1%85):

- Reflexivity(рефлексивность): `x == x` для любого `x`
- Неразличимость тождеств: для любых `x` и `y`, если `x == y`, то `x` и `y` неразличимы.
- Личность неразличимых: для любых `x` и `y`, если `x` и `y` неразличимы, то `x == y`

Неразличимость формализует интуитивное представление о двух объектах, обладающих точно такими же свойствами. 
Два значения `x, y: A` неразличимы, если не существует такой функции `f: A => Boolean`, что `f(x)` - `true`, а `f(y)` - `false`. 

Эти законы влекут за собой симметрию и транзитивность, которые должно быть легче проверить, 
поскольку они не дают универсальной количественной оценки для всех возможных предикатов в языке:

- Симметричность: `x == y` тогда и только тогда, когда `y == x`
- Транзитивность: если `x == y` и `y == z`, тогда `x == z`

Если есть преобразование `f: G => F`, то из `Equal[F]` можно получить `Equal[G]`.

##### Описание

```scala
trait Equal[F]:
  self =>
  
  def equal(a1: F, a2: F): Boolean

  def contramap[G](f: G => F): Equal[G] =
    (a1: G, a2: G) => self.equal(f(a1), f(a2))
```

### Реализация в ScalaZ

```scala
import scalaz._
import Scalaz._
List(1, 2, 3) === List(1, 2, 3) // true
List(1, 2, 3) =/= List(1, 2, 4) // true
```


---

**References:**

- [Scalaz](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Equal.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Equal.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Equal.html)
