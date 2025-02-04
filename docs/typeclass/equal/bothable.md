# Predecessible и Successible

## Формальное определение

`Predecessible` — это класс типов для представления множеств, которые можно считать в обратном порядке,
т.е. у каждого элемента есть _предыдущий_ элемент, меньший его. 

`Successible` — это класс типов для представления множеств, которые увеличиваются,
т.е. у каждого элемента есть _следующий_ элемент, больший его.

## Определение в виде кода на Scala

```dotty
trait Predecessible[A]:
  def prev(a: A): A

trait Successible[A]:
  def next(a: A): A

trait Bothable[A] extends Predecessible[A], Successible[A]
```

### Числа

```dotty
given Bothable[Int] with
  override def prev(a: Int): Int = a - 1
  override def next(a: Int): Int = a + 1
```

## Схема

```mermaid
classDiagram
    class Predecessible~A~{
        +pred(x: A) A
    }
    class Successible~A~{
        +succ(x: A) A
    }
    class Bothable~A~
    Predecessible <|-- Bothable 
    Successible <|-- Bothable     
```

---

**Ссылки:**

- [Algebird, Predecessible](https://twitter.github.io/algebird/typeclasses/predecessible.html)
- [Algebird, Successible](https://twitter.github.io/algebird/typeclasses/successible.html)
