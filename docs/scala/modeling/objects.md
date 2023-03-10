# Объекты

В Scala ключевое слово `object` создает одноэлементный объект (singleton).
Другими словами, объект определяет класс, который имеет ровно один экземпляр.
Он инициализируется лениво, когда ссылаются на его элементы, аналогично `lazy val`.
Объекты в Scala позволяют группировать методы и поля в одном пространстве имен,
аналогично тому, как используются статические члены класса в Java, Javascript (ES6) или `@staticmethod` в Python.

Объекты имеют несколько применений:

- Они используются для создания коллекций служебных методов.
- _companion object_ - это объект, имеющий то же имя, что и класс, с которым он совместно использует файл.
  В этой ситуации класс называется _companion class_.
- Они используются для имплементации `traits` для создания модулей.

Объявление объекта аналогично объявлению класса. 
Вот пример объекта `StringUtils`, который содержит набор методов для работы со строками:

```scala
object StringUtils:
  def truncate(s: String, length: Int): String = s.take(length)
  def containsWhitespace(s: String): Boolean = s.matches(".*\\s.*")
  def isNullOrEmpty(s: String): Boolean = s == null || s.trim.isEmpty
```

Поскольку `StringUtils` является одноэлементным, его методы могут вызываться непосредственно в объекте:

```scala
StringUtils.truncate("Chuck Bartowski", 5)
// res0: String = "Chuck"
```

Импорт в Scala очень гибкий и позволяет импортировать все члены объекта:

```scala
import StringUtils.*
truncate("Chuck Bartowski", 5)
// res1: String = "Chuck"
containsWhitespace("Sarah Walker")
// res2: Boolean = true
isNullOrEmpty("John Casey")
// res3: Boolean = false
```

Можно импортировать только часть методов:

```scala
import StringUtils.{truncate, containsWhitespace}
truncate("Charles Carmichael", 7)    
containsWhitespace("Captain Awesome") 
isNullOrEmpty("Morgan Grimes")  // Not found: isNullOrEmpty (error)
```

Объекты также могут иметь поля, к которым можно обратиться, как к статистическим методам:

```scala
object MathConstants:
  val PI = 3.14159
  val E = 2.71828
println(MathConstants.PI)
// 3.14159
```


---

**Ссылки:**

- [Scala3 book, domain modeling tools](https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html)
- [Scala3 book, taste modeling](https://docs.scala-lang.org/scala3/book/taste-modeling.html)
- [Scala3 book, taste objects](https://docs.scala-lang.org/scala3/book/taste-objects.html)
