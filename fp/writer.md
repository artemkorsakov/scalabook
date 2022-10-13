# Функциональный журнал

`Writer` предназначена для значений, к которым присоединено другое значение, действующее как своего рода значение журнала.

```scala
final case class Writer[W, A](run: () => (W, A))
```

### Функции общего назначения, описывающие шаблоны программ с ведением журнала

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


---

**References:**
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Writer.html)
