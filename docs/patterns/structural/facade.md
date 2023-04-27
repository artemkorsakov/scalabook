# Фасад

#### Назначение

Предоставление унифицированного интерфейса набору интерфейсов в подсистеме. 
`Facade` определяет высокоуровневый интерфейс, упрощающий использование подсистемы

#### Диаграмма

![Facade](https://upload.wikimedia.org/wikipedia/commons/5/56/UML_DP_Fa%C3%A7ade.png?uselang=ru)

#### Пример

```scala
trait Facade:
  type A <: SubSystemA
  type B <: SubSystemB
  protected val subA: A
  protected val subB: B
  def foo(): Unit = subB.foo(subA)
  protected class SubSystemA
  protected class SubSystemB:
    def foo(sub: SubSystemA): Unit = println("Calling foo")
end Facade

object FacadeA extends Facade:
  type A = SubSystemA
  type B = SubSystemB
  val subA: A = new SubSystemA
  val subB: B = new SubSystemB
end FacadeA
```

```scala
FacadeA.foo()
// Calling foo
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Facade_pattern)
