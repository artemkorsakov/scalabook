# Теория категорий

В данном разделе будут собраны основные [классы типов](scala/abstractions/type-classes.md) из теории категорий,
а также примеры реализации этих классов типов на Scala,
в том числе из таких семейств библиотек, как
[Scalaz](https://scalaz.github.io/7/),
[Algebird](https://twitter.github.io/algebird/),
[Cats](http://typelevel.org/cats/).

[**Теория категорий**](https://ru.wikipedia.org/wiki/%D0%A2%D0%B5%D0%BE%D1%80%D0%B8%D1%8F_%D0%BA%D0%B0%D1%82%D0%B5%D0%B3%D0%BE%D1%80%D0%B8%D0%B9) — 
раздел математики, изучающий свойства отношений между математическими объектами, не зависящие от внутренней структуры объектов.

Категория **C** — это:

- класс объектов **Ob{C}**
- для каждой пары объектов **A**, **B** задано множество морфизмов (или стрелок) **Hom{C}(A,B)**, 
  причём каждому морфизму соответствуют единственные **A** и **B**
- для пары морфизмов **f ∈ Hom(A,B)** и **g ∈ Hom(B,C)** определена композиция **g∘f ∈ Hom(A,C)**
- для каждого объекта **A** задан тождественный морфизм **id ∈ Hom(A,A)** (стрелка от объекта к самому себе)

![Commutative diagram for morphism](https://upload.wikimedia.org/wikipedia/commons/e/ef/Commutative_diagram_for_morphism.svg)

причём выполняются две аксиомы:

- операция композиции ассоциативна: если есть три морфизма, **f**, **g** и **h**, 
  которые могут быть скомпонованы (то есть, их типы согласованы друг с другом), 
  то не имеет значения порядок компоновки: **h∘(g∘f)=(h∘g)∘f=h∘g∘f**
- тождественный морфизм действует тривиально: **f∘id=id∘f=f** для **f ∈ Hom(A,B)**

![Category laws](https://upload.wikimedia.org/wikipedia/commons/b/bd/Diag_ax_cat.gif)

В Scala это определение можно выразить так:

```scala
def compose[A, B, C](f: A => B, g: B => C): A => C = f andThen g
def id[A]: A => A = a => a
```

А соответствующие законы вот так 
(если задана операция `assert`, способная проверять функции на равенство, например, на какой-то выборке):

```scala
trait Laws[A, B, C, D]:
  def associativityLaw(f: A => B, g: B => C, h: C => D): Unit =
    assert(compose(compose(f, g), h), compose(f, compose(g, h)))

  def identityLaw(f: A => B): Unit =
    assert(compose(f, id), f)
    assert(compose(id, f), f)
```

#### Краткий итог 

Категория состоит из объектов и стрелок (морфизмов).
Стрелки могут быть скомпонованы, их композиция ассоциативна. 
Каждый объект имеет единичную стрелку, которая служит в качестве единицы композиции.


---

**Ссылки:**

- Bartosz Milewski - Category Theory for Programmers:
    - [Теория категорий для программистов](https://henrychern.wordpress.com/2022/09/15/%d1%82%d0%b5%d0%be%d1%80%d0%b8%d1%8f-%d0%ba%d0%b0%d1%82%d0%b5%d0%b3%d0%be%d1%80%d0%b8%d0%b9-%d0%b4%d0%bb%d1%8f-%d0%bf%d1%80%d0%be%d0%b3%d1%80%d0%b0%d0%bc%d0%bc%d0%b8%d1%81%d1%82%d0%be%d0%b2/)
    - [Category Theory for Programmers](https://bartoszmilewski.com/2014/11/04/category-the-essence-of-composition)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#anatomy-of-a-type-class)
- [Type classes in Algebird](https://twitter.github.io/algebird/typeclasses.html)
- [Type classes in Cats](https://github.com/tpolecat/cats-infographic)
- [Type classes in Scalaz](https://scalaz.github.io/7/typeclass/index.html)
