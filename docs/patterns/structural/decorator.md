# Декоратор

#### Назначение

Динамически прикреплять дополнительные обязанности к объекту. 
Декораторы предоставляют гибкую альтернативу подклассам для расширения функциональности.

#### Диаграмма

![Decorator](https://upload.wikimedia.org/wikipedia/ru/0/00/Decorator_template.png)

#### Пример

```scala
trait Component:
  def draw(): Unit

class TextView(var s: String) extends Component:
  def draw(): Unit = println(s"Drawing..$s")

trait EncapsulateTextView(c: TextView) extends Component:
  def draw(): Unit = c.draw()

trait BorderDecorator extends Component:
  abstract override def draw(): Unit =
    super.draw()
    drawBorder()
  def drawBorder(): Unit =
    println("Drawing border")

trait ScrollDecorator extends Component:
  abstract override def draw(): Unit =
    scrollTo()
    super.draw()
  def scrollTo(): Unit = println("Scrolling..")
```

```scala
val tw = new TextView("foo")
val etw1 = new EncapsulateTextView(tw) with BorderDecorator with ScrollDecorator
// etw1: EncapsulateTextView & BorderDecorator & ScrollDecorator = repl.MdocSession$$anon$2@7d6a5d30
etw1.draw()
// Scrolling..
// Drawing..foo
// Drawing border
tw.s = "bar"
val etw2 = new EncapsulateTextView(tw) with ScrollDecorator with BorderDecorator
// etw2: EncapsulateTextView & ScrollDecorator & BorderDecorator = repl.MdocSession$$anon$6@88da5b
etw2.draw()
// Scrolling..
// Drawing..bar
// Drawing border
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Decorator_pattern)
