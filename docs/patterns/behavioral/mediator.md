# Посредник

#### Назначение

Определение объекта, который инкапсулирует способ взаимодействия набора объектов. 
Медиатор способствует слабой связи, не позволяя объектам явно ссылаться друг на друга, 
и позволяет независимо изменять их взаимодействие.

#### Диаграмма

![Mediator](https://upload.wikimedia.org/wikipedia/commons/e/e4/Mediator_design_pattern.png?uselang=ru)

#### Пример

Классы `ListBox` и `EntryField` — наши классы-коллеги, оба — `Widget`-ы. 
`DialogDirector` содержит вложенный трейт `ListBoxDir`, который перехватывает каждый раз, когда щелкают наш список. 
При нажатии на него вызывается `listBoxChanged`, что приводит к установке текста в нашем поле ввода с текущим выбором списка. 
Это простой пример взаимодействия объектов. 
Обратите внимание, что коллеги совершенно не знают о посреднике и, следовательно, о самой схеме.


```scala
// Widgets
class ListBox:
  def getSelection: String = "selected"
  def click(): Unit = ()

class EntryField:
  def setText(s: String): Unit = println(s)

class DialogDirector:
  protected trait ListBoxDir extends ListBox:
    abstract override def click(): Unit =
      super.click()
      listBoxChanged()

  // Colleagues
  val listBox: ListBox = new ListBox with ListBoxDir
  val entryField: EntryField = new EntryField

  // Directing methods
  def showDialog(): Unit = ()

  // called when listBox is clicked via advice
  def listBoxChanged(): Unit = entryField.setText(listBox.getSelection)

end DialogDirector
```

```scala
val dialog = new DialogDirector
val listBox = dialog.listBox
val entryField = dialog.entryField
listBox.click()
// selected
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Mediator_pattern)
