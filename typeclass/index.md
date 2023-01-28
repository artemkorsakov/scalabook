# Теория категорий

[**Теория категорий**](https://ru.wikipedia.org/wiki/%D0%A2%D0%B5%D0%BE%D1%80%D0%B8%D1%8F_%D0%BA%D0%B0%D1%82%D0%B5%D0%B3%D0%BE%D1%80%D0%B8%D0%B9) — 
раздел математики, изучающий свойства отношений между математическими объектами, не зависящие от внутренней структуры объектов.

**Класс типов** (или **типовой класс**, _type class_) — это абстрактный параметризованный тип, 
который позволяет добавлять новое поведение к любому закрытому типу данных без использования подтипов.
Подробнее о классах типов [описано в основной документации](../scala/abstractions/type-classes), 
а также в главе [1.1 Anatomy of a Type Class][Scala with Cats]
книги _"Scala with Cats by Noel Welsh and Dave Gurnell"_.

В данном разделе будут собраны основные классы типов, а также примеры реализации этих классов типов на Scala, 
в том числе из таких семейств библиотек, как 
[Scalaz](https://scalaz.github.io/7/), 
[Algebird](https://twitter.github.io/algebird/), 
[Cats](http://typelevel.org/cats/).

Классы типов сгруппированы по "семействам":

- [Группа Equal](equal)
- [Группа Monoid](monoid)
- [Группа Monad](monad)
- [Группа Arrow](arrow)
- [Группа Bifunctor](bifunctor)
- [Вне группы](other)

---

**Ссылки:**

- [Scala with Cats][Scala with Cats]
- [Type classes in Algebird](https://twitter.github.io/algebird/typeclasses.html)
- [Type classes in Cats](https://github.com/tpolecat/cats-infographic)
- [Type classes in Scalaz](https://scalaz.github.io/7/typeclass/index.html)

[Scala with Cats]: https://www.scalawithcats.com/dist/scala-with-cats.html#anatomy-of-a-type-class
