# Stackless Scala

Перевод статьи Runar Oli Bjarnason ["Stackless Scala With Free Monads"][trampolines] с небольшими отступлениями.
Добавлено несколько примеров из [стандартной Scala библиотеки][API].

## Для понимания

Исключение абстрактного хвостового вызова (Tail call elimination - TCE) в компиляторе Scala
ограничено саморекурсивными методами, но в остальном хвостовые вызовы не исключаются.
Это делает функции, состоящие из множества более мелких функций, склонными к переполнению стека.
Наличие общего механизма TCE было бы большим преимуществом в Scala,
особенно для функционального программирования.
Трамплининг — популярный метод, который можно использовать для TCE в языках,
изначально его не поддерживающих.
В этой статье дается введение в "батуты" в Scala и расширяется это решение,
позволяющее устранить любой вызов метода, даже вызов, который вообще не находится в хвостовой позиции.
Это полностью исключает использование стека вызовов в программах Scala.

## Введение

Поскольку стек вызовов является ограниченным ресурсом виртуальной машины,
большинство программистов, имеющих некоторый опыт работы со Scala, сталкивались с проблемой,
когда кажущаяся разумной функция выходила за пределы стека
и приводила к сбою программы с ошибкой `StackOverflowError`.
В качестве практического примера рассмотрим обход списка с сохранением некоторого состояния.
Мы будем использовать тип данных `State`, который представляет функцию перехода в простом автомате состояний.

```scala
case class State[S, +A](runS: S => (A, S)):
  def map[B](f: A => B): State[S, B] =
    State[S, B] { s =>
      val (a, s1) = runS(s)
      (f(a), s1)
    }

  def flatMap[B](f: A => State[S, B]): State[S, B] =
    State[S, B] { s =>
      val (a, s1) = runS(s)
      f(a).runS(s1)
    }
```

---

**Ссылки:**

- [Functional Programming in Scala, Second Edition, Chapter 13](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Runar Oli Bjarnason - Stackless Scala With Free Monads][trampolines]
- [Scala API][API]

[API]: https://www.scala-lang.org/api/3.2.2/scala/util/control/TailCalls$.html

[trampolines]: https://blog.higher-order.com/assets/trampolines.pdf
