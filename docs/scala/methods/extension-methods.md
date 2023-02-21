# Методы расширения

Основная цель методов расширения (_extension methods_) -
позволить добавлять новые функциональные возможности в закрытые классы.
Представим, что у нас есть класс `Circle`, но мы не можем изменить его исходный код.
Например, он может быть определен следующим образом в сторонней библиотеке:

```scala
case class Circle(x: Double, y: Double, radius: Double)
```

Если необходимо добавить методы в этот класс, можно их определить как методы расширения, например:

```scala
extension (c: Circle)
  def circumference: Double = c.radius * math.Pi * 2
  def diameter: Double = c.radius * 2
  def area: Double = math.Pi * c.radius * c.radius
```

Ключевое слово `extension` объявляет о намерении определить один или несколько методов расширения для параметра,
заключенного в круглые скобки.
Как показано в примере выше, параметры типа `Circle` затем могут быть использованы в теле методов расширения.

Теперь, если есть экземпляр `Circle` с именем `aCircle`, можно вызвать эти методы следующим образом:

```scala
val aCircle = Circle(0.0, 0.0, 1.0)
// aCircle: Circle = Circle(x = 0.0, y = 0.0, radius = 1.0)
aCircle.circumference
// res0: Double = 6.283185307179586
aCircle.diameter
// res1: Double = 2.0
aCircle.area
// res2: Double = 3.141592653589793
```

Более подробно методы расширения раскрыты 
[в соответствующей главе раздела "Абстракции"](../abstractions/ca-extension-methods).


---

**Ссылки:**

- [Scala3 book, Method Features](https://docs.scala-lang.org/scala3/book/methods-most.html)
