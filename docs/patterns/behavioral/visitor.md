# Посетитель

#### Назначение

Представление операции, которая должна быть выполнена над элементами структуры объекта. 
Visitor позволяет определить новую операцию без изменения классов элементов, над которыми она работает.

#### Диаграмма

![Visitor](https://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/VisitorDiagram.svg/515px-VisitorDiagram.svg.png?uselang=ru)

#### Пример

```scala
trait Expr
case class Num(n: Int) extends Expr
case class Sum(l: Expr, r: Expr) extends Expr

def prettyPrint(e: Expr): Unit =
  e match
    case Num(n) => print(n)
    case Sum(l, r) =>
      prettyPrint(l)
      print(" + ")
      prettyPrint(r)

def eval(e: Expr): Int =
  e match
    case Num(n)    => n
    case Sum(l, r) => eval(l) + eval(r)
```

```scala
val e1 = Sum(Sum(Num(1), Num(2)), Num(3))
prettyPrint(e1)
// 1 + 2 + 3
print(eval(e1))
// 6
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Visitor_pattern)
