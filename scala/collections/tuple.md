# Tuple (кортежи)

Scala `tuple` - это тип, который позволяет помещать коллекцию разных типов в один и тот же контейнер.
Например, учитывая `case class Person`:

```scala
case class Person(name: String)
```

можно построить кортеж, содержащий `Int`, `String` и `Person`:

```scala
val t = (11, "eleven", Person("Eleven"))
// t: Tuple3[Int, String, Person] = (11, "eleven", Person(name = "Eleven"))
```

Доступ к значениям кортежа осуществляется через индекс (начиная с 0):

```scala
t(0)
// res0: Int = 11
t(1)
// res1: String = "eleven"
t(2)
// res2: Person = Person(name = "Eleven")
```

либо через методы вида `._i`, где `i` - порядковый номер (начиная с 1, в отличие от индекса)

```scala
t._1
// res3: Int = 11
t._2
// res4: String = "eleven"
t._3
// res5: Person = Person(name = "Eleven")
```

Также можно использовать `extractor` для присвоения переменным значений полей кортежа:

```scala
val (num, str, person) = t
// num: Int = 11
// str: String = "eleven"
// person: Person = Person(name = "Eleven")
```

Кортежи хороши для случаев, когда необходимо поместить коллекцию разнородных типов
в небольшую структуру, похожую на коллекцию.


---

**References:**
- [Scala3 book](https://docs.scala-lang.org/scala3/book/taste-collections.html)
- [Scala3 book, Collections Types](https://docs.scala-lang.org/scala3/book/collections-classes.html)
- [Scala, Immutable collections](https://docs.scala-lang.org/ru/overviews/collections-2.13/concrete-immutable-collection-classes.html)
- [Scala, Mutable collections](https://docs.scala-lang.org/ru/overviews/collections-2.13/concrete-mutable-collection-classes.html)
