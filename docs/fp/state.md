# Функциональное состояние

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

Помимо определения [непрозрачного типа](https://scalabook.gitflic.space/docs/scala/type-system/types-opaque), 
предоставляется метод расширения `run`, позволяющий вызывать базовую функцию, 
а также метод `apply` для сопутствующего объекта, позволяющий создавать значение `State` из функции. 
В обоих случаях известен тот факт, что `State[S, A]` эквивалентно `S ⇒ (A, S)`, 
что делает эти две операции простыми и кажущимися избыточными. 
Однако за пределами определяющей области видимости — например, в другом пакете — эта эквивалентность неизвестна, 
и, следовательно, нужны такие преобразования. 

## Функции общего назначения, описывающие шаблоны программ с отслеживанием состояния

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

```scala
unit[String, Int](42).run("state")
// res0: Tuple2[Int, String] = (42, "state")
val state = State[Int, String](i => (i.toString, i + 1))
state.map(str => s"Number: $str").run(5)
// res1: Tuple2[String, Int] = ("Number: 5", 6)
```

## Резюме

- API с отслеживанием состояния можно смоделировать как чистые функции, 
которые преобразуют входное состояние в выходное при вычислении значения.
- Тип данных `State` упрощает работу с API с отслеживанием состояния, 
устраняя необходимость вручную передавать состояния ввода и вывода во время вычислений.


## Реализация в Cats

```scala
import cats.data.State

val step1 = State[Int, String]{ num =>
  val ans = num + 1
  (ans, s"Result of step1: $ans")
}

val step2 = State[Int, String]{ num =>
  val ans = num * 2
  (ans, s"Result of step2: $ans")
}

val both = for {
  a <- step1
  b <- step2
} yield (a, b)

val (state, result) = both.run(20).value
// val state: Int = 42
// val result: (String, String) = (Result of step1: 21,Result of step2: 42)
```


---

**Ссылки:**

- [Cats](https://typelevel.org/cats/datatypes/state.html)
- [Functional Programming in Scala, Second Edition, Chapter 6](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Herding Cats](http://eed3si9n.com/herding-cats/State.html)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#the-state-monad)
