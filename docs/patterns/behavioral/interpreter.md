# Интерпретатор

#### Назначение

Для заданного языка определить представление для его грамматики 
вместе с интерпретатором, который использует представление для интерпретации предложений на языке.

#### Диаграмма

![Interpreter](https://upload.wikimedia.org/wikipedia/commons/thumb/b/bc/Interpreter_UML_class_diagram.svg/804px-Interpreter_UML_class_diagram.svg.png)

#### Пример

```scala
class Context:
  import scala.collection.mutable
  val result: mutable.Stack[String] = mutable.Stack.empty[String]

trait Expression:
  def interpret(context: Context): Unit

trait OperatorExpression extends Expression:
  val left: Expression
  val right: Expression

  def interpret(context: Context): Unit =
    left.interpret(context)
    val leftValue = context.result.pop()

    right.interpret(context)
    val rightValue = context.result.pop()

    doInterpret(context, leftValue, rightValue)

  protected def doInterpret(context: Context, leftValue: String, rightValue: String): Unit

end OperatorExpression
```

```scala
trait EqualsExpression extends OperatorExpression:
  protected override def doInterpret(context: Context, leftValue: String, rightValue: String): Unit =
    context.result.push(if leftValue == rightValue then "true" else "false")

trait OrExpression extends OperatorExpression:
  protected override def doInterpret(context: Context, leftValue: String, rightValue: String): Unit =
    context.result.push(if leftValue == "true" || rightValue == "true" then "true" else "false")

trait MyExpression extends Expression:
  var value: String

  def interpret(context: Context): Unit =
    context.result.push(value)
```

```scala
val context = Context()
val input = new MyExpression() { var value = "" }

var expression = new OrExpression {
  val left: Expression = new EqualsExpression {
    val left = input
    val right = new MyExpression { var value = "4" }
  }
  val right: Expression = new EqualsExpression {
    val left = input
    val right = new MyExpression { var value = "четыре" }
  }
}

input.value = "четыре"
expression.interpret(context)
context.result.pop()
// res0: String = "true"

input.value = "44"
expression.interpret(context)
context.result.pop()
// res1: String = "false"
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Interpreter_pattern)
