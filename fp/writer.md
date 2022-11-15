# Функциональный журнал

`Writer` предназначен для результатов, к которым присоединено другое значение, действующее как своего рода журнал.
Одним из распространенных применений для `Writers` является запись последовательностей шагов в многопоточных вычислениях, 
где стандартные методы императивной регистрации могут привести к чередованию сообщений из разных контекстов. 
При этом лог для вычислений `Writer` привязан к результату, 
поэтому можно запускать параллельные вычисления без смешивания логов.


```scala
final case class Writer[W, A](run: () => (W, A))
```

## Функции общего назначения, описывающие шаблоны программ с ведением журнала

```scala
object Writer:
  extension [W, A](underlying: Writer[W, A])
    def map[B](f: A => B): Writer[W, B] =
      val (w, a) = underlying.run()
      Writer[W, B](() => (w, f(a)))

    def flatMap[B](f: A => Writer[W, B])(using monoid: Monoid[W]): Writer[W, B] =
      Writer[W, B] { () =>
        val (w1, a) = underlying.run()
        val (w2, b) = f(a).run()
        (monoid.combine(w1, w2), b)
      }

  def unit[W, A](a: => A)(using monoid: Monoid[W]): Writer[W, A] =
    Writer[W, A](() => (monoid.empty, a))
```

Пример:

```scala
unit[String, Int](42).run()
// res0: Tuple2[String, Int] = ("", 42)
```


## Реализация в Cats

```scala
import cats.data.Writer
import cats.instances.vector.*

val a = Writer(Vector(
  "It was the best of times",
  "it was the worst of times"
), 1859)
val (log, result) = a.run
// val log: Vector[String] = Vector(It was the best of times, it was the worst of times)
// val result: Int = 1859
```


---

## References

- [Cats](https://typelevel.org/cats/datatypes/writer.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Writer.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Writer.html)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#writer-monad)
  