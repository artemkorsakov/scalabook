# Шаблонный метод

#### Назначение

Определить скелет алгоритма в операции, отложив некоторые шаги на подклассы. 
Шаблонный метод позволяет подклассам переопределять определенные шаги алгоритма без изменения структуры алгоритма.

#### Диаграмма

![Template Method](https://upload.wikimedia.org/wikipedia/commons/5/52/Template_Method_UML.svg?uselang=ru)

#### Пример

```scala
trait Template extends (Unit => Int):
  def subStepA(): Unit
  def subStepB: Int
  def apply: Int =
    subStepA()
    subStepB
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Template_method_pattern)
