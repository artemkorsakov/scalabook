{%
showPrev = true
prevPage = state.html
showNext = true
nextPage = parallelism.html
%}

# Функциональный журнал

## Описание 

`Writer` предназначен для результатов, к которым присоединено другое значение, действующее как своего рода журнал.
Одним из распространенных применений для `Writer` является запись последовательностей шагов в многопоточных вычислениях,
где стандартные методы императивной регистрации могут привести к чередованию сообщений из разных контекстов.
При этом лог для вычислений `Writer` привязан к результату,
поэтому можно запускать параллельные вычисления без смешивания логов.

```dotty
final case class Writer[W, A](run: () => (W, A))
```

## Функции общего назначения

#### map

Аналогично функциональному состоянию, `map` позволяет при наличии базового журнала `Writer[S, A]` 
и функции преобразования значения из типа `A` в тип `B` получать `Writer[S, B]` - 
функциональный журнал с вычислением значения типа `B`.

```dotty
object Writer:
  extension [W, A](underlying: Writer[W, A])
    def map[B](f: A => B): Writer[W, B] =
      val (w, a) = underlying.run()
      Writer[W, B](() => (w, f(a)))
```

Пример:

```dotty
val initial = Writer[String, Int](() => ("Сообщение:", 1))

val writer = initial.map(_ + 1)
writer.run()
// (Сообщение:,2)
```

#### flatMap

`flatMap` позволяет при наличии функции преобразования значения в новый `Writer` получать этот `Writer`:

```dotty
object Writer:
  extension [W, A](underlying: Writer[W, A])
    def flatMap[B](f: A => Writer[W, B])(combine: (W, W) => W): Writer[W, B] =
      Writer[W, B] { () =>
        val (w1, a) = underlying.run()
        val (w2, b) = f(a).run()
        (combine(w1, w2), b)
      }
```

Пример:

```dotty
val initial = Writer[String, Int](() => ("Сообщение:", 1))

val f: Int => Writer[String, Int] = number =>
  if number <= 0 then Writer(() => ("Конфеты кончились", number))
  else Writer(() =>  ("Конфеты ещё есть", number))
  
val writer = initial.flatMap(f)(_ + _)
writer.run()
// (Сообщение:Конфеты ещё есть,1)
```

#### unit

`unit` оборачивает любое значение в очень простой `Writer` с заданным постоянным "сообщением":

```dotty
object Writer:
  def unit[W, A](a: => A)(empty: W): Writer[W, A] =
    Writer[W, A](() => (empty, a))
```

Пример:

```dotty
unit[String, Int](42)("").run()
// ("", 42)
```

## Реализация в библиотеках

### Реализация в Cats

```dotty
import cats.data.Writer

val a = Writer(Vector(
  "It was the best of times",
  "it was the worst of times"
), 1859)
val (log, result) = a.run
// val log: Vector[String] = Vector(It was the best of times, it was the worst of times)
// val result: Int = 1859
```

---

**Ссылки:**

- [Тройка полезных монад](https://habr.com/ru/articles/184722/)
- Category Theory for Programmers - Bartosz Milewski:
    - [Теория категорий для программистов](https://henrychern.files.wordpress.com/2023/05/mil-ct3.pdf#page=134)
    - [Category Theory for Programmers, Functoriality](https://bartoszmilewski.com/2015/02/03/functoriality/)
    - [Category Theory for Programmers, Kleisli Categories](https://bartoszmilewski.com/2014/12/23/kleisli-categories/)
- [Cats](https://typelevel.org/cats/datatypes/writer.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Writer.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Writer.html)
- [Scala with Cats](https://scalawithcats.com/dist/scala-with-cats.html#writer-monad)
