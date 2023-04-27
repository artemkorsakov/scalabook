# Компоновщик

#### Назначение

Комбинировать объекты в древовидные структуры для представления иерархий часть-целое. 
Composite позволяет клиентам единообразно обрабатывать отдельные объекты и композиции объектов.

#### Диаграмма

![Composite](https://upload.wikimedia.org/wikipedia/commons/5/5a/Composite_UML_class_diagram_%28fixed%29.svg?uselang=ru)

#### Пример

В листинге ниже абстрактный класс `Component` представляет интерфейс для общих операций, 
которые мы хотим выполнять с листьями или композитами, в данном случае только с одним методом с именем `display`.
`case class`-ы `Text` и `Picture` — это два листовых объекта, оба они реализуют `display`.
`case class Composite` содержит любое количество дочерних компонентов, то есть листьев или других составных частей.
В зависимости от того, насколько тяжелыми являются наши листовые объекты, 
или выполняют ли они другие роли в нашем дизайне или нет, 
мы можем сделать дочерние переменные как неизменяемыми, так и изменяемыми.

```scala
trait Component:
  def display(): Unit

case class Text(text: String) extends Component:
  def display(): Unit = println(text)

case class Picture(picture: String) extends Component:
  def display(): Unit = println(picture)

case class Composite(children: List[Component]) extends Component:
  def display(): Unit = children.foreach(_.display())
```

```scala
val tree = Composite(List(Composite(List(Text("text1"), Picture("picture1"))), Text("text2")))
tree.display()
// text1
// picture1
// text2
tree.children(1).display()
// text2
```

Ниже показан пример метода, который обходит составную структуру и изменяет все узлы `Text` на месте.

```scala
def changeAllText(c: Component, s: String): Component =
  c match
    case _: Text    => Text(s)
    case p: Picture => p
    case Composite(children) =>
      val newChildren = children.map(changeAllText(_, s))
      Composite(newChildren)
changeAllText(tree, "text3").display()
// text3
// picture1
// text3
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Composite_pattern)
