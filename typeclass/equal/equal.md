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

## Описание

```scala
trait Equal[F]:
  self =>

  def equal(a1: F, a2: F): Boolean

  def notEqual(a1: F, a2: F): Boolean = !equal(a1, a2)

  def contramap[G](f: G => F): Equal[G] =
    (a1: G, a2: G) => self.equal(f(a1), f(a2))
```

## Реализация в ScalaZ

```scala
import scalaz.*
import Scalaz.*
List(1, 2, 3) === List(1, 2, 3) // true
List(1, 2, 3) =/= List(1, 2, 4) // true
```

## Реализация в Cats

```scala
import cats.*
import cats.implicits.*

1 === 1              // true
"Hello" =!= "World"  // true
123 === "123"        // Не скомпилируется: Type Mismatch Error
1.some =!= none[Int] // true

final case class Cat(name: String, age: Int, color: String)

given Eq[Cat] =
  Eq.instance[Cat] { (cat1, cat2) =>
    (cat1.name  === cat2.name ) &&
      (cat1.age   === cat2.age  ) &&
      (cat1.color === cat2.color)
  }

val cat1 = Cat("Garfield",   38, "orange and black")
val cat2 = Cat("Heathcliff", 32, "orange and black")
cat1 === cat2         // false
```


---

**Ссылки:**

- [Cats](https://typelevel.org/cats/typeclasses/eq.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Eq.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Equal.html)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/Equal.html)
