---
layout: fp
title: "Функциональное состояние"
section: fp
prev: laziness
next: monoids
---

## {{page.title}}

Базовый шаблон, как сделать любой API с отслеживанием состояния чисто функциональным, выглядит так:

```scala
opaque type State[S, +A] = S => (A, S)

object State:
  extension [S, A](underlying: State[S, A])
    def run(s: S): (A, S) = underlying(s)

  def apply[S, A](f: S => (A, S)): State[S, A] = f
```

Здесь `State` — это сокращение от вычисления, которое переносит какое-то состояние, действие состояния, 
переход состояния или даже оператор. 

Помимо определения [непрозрачного типа](@DOC@type-system/types-opaque), 
предоставляется метод расширения `run`, позволяющий вызывать базовую функцию, 
а также метод `apply` для сопутствующего объекта, позволяющий создавать значение `State` из функции. 
В обоих случаях известен тот факт, что `State[S, A]` эквивалентно `S ⇒ (A, S)`, 
что делает эти две операции простыми и кажущимися избыточными. 
Однако за пределами определяющей области видимости — например, в другом пакете — эта эквивалентность неизвестна, 
и, следовательно, нужны такие преобразования. 

### Функции общего назначения, описывающие шаблоны программ с отслеживанием состояния

```scala
object State:
  extension [S, A](underlying: State[S, A])
    def map[B](f: A => B): State[S, B] =
      s1 =>
        val (a, s2) = underlying(s1)
        (f(a), s2)

    def map2[B, C](sb: State[S, B])(f: (A, B) => C): State[S, C] =
      for
        a <- underlying
        b <- sb
      yield f(a, b)

    def flatMap[B](f: A => State[S, B]): State[S, B] =
      s1 =>
        val (a, s2) = underlying(s1)
        f(a)(s2)

  def unit[S, A](a: A): State[S, A] = s => (a, s)

  def sequence[S, A](actions: List[State[S, A]]): State[S, List[A]] =
    actions.foldRight(unit[S, List[A]](Nil))((f, acc) => f.map2(acc)(_ :: _))
```

Пример:

```scala mdoc:invisible
object StateSpace:
  opaque type State[S, +A] = S => (A, S)

  object State:
    extension [S, A](underlying: State[S, A])
      def run(s: S): (A, S) = underlying(s)

      def map[B](f: A => B): State[S, B] =
        s1 =>
          val (a, s2) = underlying(s1)
          (f(a), s2)

      def map2[B, C](sb: State[S, B])(f: (A, B) => C): State[S, C] =
        for
          a <- underlying
          b <- sb
        yield f(a, b)

      def flatMap[B](f: A => State[S, B]): State[S, B] =
        s1 =>
          val (a, s2) = underlying(s1)
          f(a)(s2)

    def apply[S, A](f: S => (A, S)): State[S, A] = f

    def unit[S, A](a: A): State[S, A] = s => (a, s)

    def sequence[S, A](actions: List[State[S, A]]): State[S, List[A]] =
      actions.foldRight(unit[S, List[A]](Nil))((f, acc) => f.map2(acc)(_ :: _))

import StateSpace.State
import StateSpace.State.*
```
```scala mdoc
unit[String, Int](42).run("state")
val state = State[Int, String](i => (i.toString, i + 1))
state.map(str => s"Number: $str").run(5)
```

### Резюме

- API с отслеживанием состояния можно смоделировать как чистые функции, 
которые преобразуют входное состояние в выходное при вычислении значения.
- Тип данных `State` упрощает работу с API с отслеживанием состояния, 
устраняя необходимость вручную передавать состояния ввода и вывода во время вычислений.


---

**References:**
- [Functional Programming in Scala, Second Edition, Chapter 6](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)