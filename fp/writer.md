# Функциональный журнал

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

Помимо определения [непрозрачного типа](../scala/type-system/types-opaque), 
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

```scala
unit[String, Int](42).run("state")
// res0: Tuple2[Int, String] = (42, "state")
val state = State[Int, String](i => (i.toString, i + 1))
state.map(str => s"Number: $str").run(5)
// res1: Tuple2[String, Int] = ("Number: 5", 6)
```

### Резюме

- API с отслеживанием состояния можно смоделировать как чистые функции, 
которые преобразуют входное состояние в выходное при вычислении значения.
- Тип данных `State` упрощает работу с API с отслеживанием состояния, 
устраняя необходимость вручную передавать состояния ввода и вывода во время вычислений.


---

**References:**
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Writer.html)
