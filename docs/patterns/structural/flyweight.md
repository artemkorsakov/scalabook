# Приспособленец

#### Назначение

Использование совместного использования для эффективной поддержки большого количества мелких объектов.
Оптимизация работы с памятью путём предотвращения создания экземпляров элементов, имеющих общую сущность.

#### Диаграмма

![Flyweight](https://upload.wikimedia.org/wikipedia/commons/e/ee/Flyweight.gif)

#### Пример

Flyweight используется для уменьшения затрат при работе с большим количеством мелких объектов. 
При проектировании Flyweight необходимо разделить его свойства на внешние и внутренние. 
Внутренние свойства всегда неизменны, тогда как внешние могут отличаться в зависимости от места 
и контекста применения и должны быть вынесены за пределы приспособленца.

Flyweight дополняет шаблон Factory Method таким образом, 
что при обращении клиента к Factory Method для создания нового объекта 
ищет уже созданный объект с такими же параметрами, что и у требуемого, и возвращает его клиенту. 
Если такого объекта нет, то фабрика создаст новый.

```scala
trait FlyWeightFactory[T1, T2] extends Function[T1, T2]:
  import scala.collection.mutable
  private val pool = mutable.Map.empty[T1, T2]

  def createFlyWeight(intrinsic: T1): T2

  def apply(index: T1): T2 =
    pool.getOrElseUpdate(index, createFlyWeight(index))

  def update(index: T1, elem: T2): Unit =
    pool(index) = elem

end FlyWeightFactory
```

```scala
class Character(char: Char):
  import scala.util.Random
  private lazy val state = Random.nextInt()
  def draw(): Unit =
    println(s"drawing character - $char, state - $state")

object CharacterFactory extends FlyWeightFactory[Char, Character]:
  def createFlyWeight(c: Char) = new Character(c)
```

```scala
val f1 = CharacterFactory('a')
val f2 = CharacterFactory('b')
val f3 = CharacterFactory('a')
val f4 = new Character('a')
f1.draw()
// drawing character - a, state - -273066214
f2.draw()
// drawing character - b, state - -9042767
f3.draw()
// drawing character - a, state - -273066214
f4.draw()
// drawing character - a, state - 139024107
```

Обратите внимание, что `f1` и `f3` указывают на один и тот же общий объект-приспособленец.


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Flyweight_pattern)
