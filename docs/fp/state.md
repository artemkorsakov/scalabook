{%
showPrev = true
prevPage = laziness.html
showNext = true
nextPage = writer.html
%}

# Функциональное состояние

## Описание 

Базовый шаблон, как сделать любой API с отслеживанием состояния чисто функциональным, выглядит так:

```dotty
opaque type State[S, +A] = S => (A, S)

object State:
  extension [S, A](underlying: State[S, A])
    def run(s: S): (A, S) = underlying(s)

  def apply[S, A](f: S => (A, S)): State[S, A] = f
```

Здесь `State` — это сокращение от вычисления, которое переносит какое-то состояние, действие состояния, 
переход состояния или даже оператор. 

Помимо определения непрозрачного типа, 
предоставляется метод расширения `run`, позволяющий вызывать базовую функцию, 
а также метод `apply` для сопутствующего объекта, позволяющий создавать значение `State` из функции. 
В обоих случаях известен тот факт, что `State[S, A]` эквивалентно `S ⇒ (A, S)`, 
что делает эти две операции простыми и кажущимися избыточными. 
Однако за пределами определяющей области видимости — например, в другом пакете — эта эквивалентность неизвестна, 
и, следовательно, нужны такие преобразования. 

**Резюме**

- API с отслеживанием состояния можно смоделировать как чистые функции,
  которые преобразуют входное состояние в выходное при вычислении значения.
- Тип данных `State` упрощает работу с API с отслеживанием состояния,
  устраняя необходимость вручную передавать состояния ввода и вывода во время вычислений.

## Пример функционального состояния

Рассмотрим упрощенный пример машины по раздаче конфет.

```dotty
case class SimpleMachine(candies: Int, coins: Int)
```

Если в машине остались конфеты и ей заплатить монету, то она выдаст конфету.
Количество конфет уменьшиться на 1, а количество монет увеличится на 1.
В остальных случаях состояние машины не изменится.

Опишем это функцией вычисления следующего состояния машины:

```dotty
case class SimpleMachine(candies: Int, coins: Int):
  self =>

  lazy val next: SimpleMachine =
    if candies <= 0 then self
    else SimpleMachine(candies - 1, coins + 1)
```

Определим `State`, вычисляющий значение - количество заработанных монет:

```dotty
val state: State[SimpleMachine, Int] = State(sm => (sm.coins + 1, sm.next))
```

Здесь используется метод `apply`, создающий значение `State` из функции.

Запустить вычисление значения и следующего состояния можно, вызвав `run`:

```dotty
val initial = SimpleMachine(100, 0)
state.run(initial)
// (1, SimpleMachine(99, 1))
```

## Функции общего назначения

Функции общего назначения, описывающие шаблоны программ с отслеживанием состояния.

#### map

`map` позволяет при наличии базового шаблона `State[S, A]` и функции преобразования значения из типа `A` в тип `B`
получать `State[S, B]` - преобразование входного состояния в выходное с вычислением значения типа `B`.

```dotty
object State:
  extension [S, A](underlying: State[S, A])
    def map[B](f: A => B): State[S, B] =
      s =>
        val (a, s1) = run(s)
        (f(a), s1)
```

Пример:

```dotty
val stateB = state.map(coins => s"Заработано $coins монет")
stateB.run(initial)
// (Заработано 1 монет, SimpleMachine(99, 1))
```

#### map2

`map2` позволяет объединять два `State` в один при наличии функции объединения входящих значений.

```dotty
object State:
  extension [S, A](underlying: State[S, A])
    def map2[B, C](sb: State[S, B])(f: (A, B) => C): State[S, C] =
      s0 =>
        val (a, s1) = run(s0)
        val (b, s2) = sb.run(s1)
        (f(a, b), s2)
```

Пример:

```dotty
val stateA =
  State[SimpleMachine, String](sm => ("Первое изменение", sm.next))
val stateB =
  State[SimpleMachine, String](sm => (", Второе изменение", sm.next))
val stateC = stateA.map2(stateB)(_ + _)

stateC.run(initial)
// (Первое изменение, Второе изменение, SimpleMachine(98, 2))
```

#### flatMap

`flatMap` позволяет при наличии функции преобразования значения в новый `State` получать этот `State`:

```dotty
object State:
  extension [S, A](underlying: State[S, A])
    def flatMap[B](f: A => State[S, B]): State[S, B] =
      s0 =>
        val (a, s1) = run(s0)
        f(a)(s1)
```

Пример:

```dotty
val stateA = State[SimpleMachine, Int](sm => (sm.candies - 1, sm.next))
val f: Int => State[SimpleMachine, String] = candies =>
  if candies <= 0 then State(sm => ("Конфеты кончились", sm))
  else State(sm => ("Конфеты ещё есть", sm))

val stateB = stateA.flatMap(f)

stateB.run(initial)
// (Конфеты ещё есть, SimpleMachine(99, 1))
```

#### for comprehension

При наличии у `State` методов `map` и `flatMap` можно использовать синтаксический сахар:

```dotty
val stateB =
  for
    candies <- stateA
    message <- f(candies)
  yield message

stateB.run(initial)
// (Конфеты ещё есть, SimpleMachine(99, 1))
```

#### unit

`unit` оборачивает любое значение в очень простой `State`, не изменяющий состояние:

```dotty
object State:
  def unit[S, A](a: A): State[S, A] =
    s => (a, s)
```

Пример:

```dotty
val stateA = State.unit[SimpleMachine, String]("Сообщение")
stateA.run(initial)
// (Сообщение, SimpleMachine(100, 0))
```

#### get, set и modify

- `get` нужен для получения текущего состояния
- `set` - для замены старого состояния новым
- а `modify` - для изменения состояния (основан на двух предыдущих)

```dotty
object State:
  def get[S]: State[S, S] = s => (s, s)

  def set[S](s: S): State[S, Unit] = _ => ((), s)

  def modify[S](f: S => S): State[S, Unit] =
    for
      s <- get
      _ <- set(f(s))
    yield ()
```

Пример:

```dotty
val initial = SimpleMachine(100, 0)
val other   = SimpleMachine(0, 100)

State.get[SimpleMachine].run(initial)
// (SimpleMachine(100, 0), SimpleMachine(100, 0))

State.set(other).run(initial)
// ((), SimpleMachine(0, 100))

State.modify[SimpleMachine](_.next).run(initial)
// ((), SimpleMachine(99, 1))
```

#### traverse и sequence

`traverse` и `sequence` необходимы для обработки коллекции операций с состоянием.

```dotty
object State:
  def sequence[S, A](list: List[State[S, A]]): State[S, List[A]] =
    traverse(list)(identity)

  def traverse[S, A, B](as: List[A])(f: A => State[S, B]): State[S, List[B]] =
    as.foldRight(unit[S, List[B]](Nil))((a, acc) => f(a).map2(acc)(_ :: _))
```

## Автомат, моделирующий раздачу конфет

Давайте реализуем более сложный автомат, моделирующий простой раздатчик конфет. 
Автомат имеет два типа входа: можно вставить монету или повернуть ручку, чтобы выдать конфету. 
Он может находиться в одном из двух состояний: заблокировано или разблокировано. 
Он также отслеживает, сколько конфет осталось и сколько монет в нем содержится:

```dotty
enum Input:
  case Coin, Turn
 
case class Machine(locked: Boolean, candies: Int, coins: Int)
```

Правила работы машины следующие:

- (а) Если вставить монету в заблокированный автомат, он разблокируется, если в нем остались конфеты.
- (б) Если повернуть ручку на разблокированном автомате, он выдаст конфеты и заблокируется.
- (в) Поворот ручки на заблокированном автомате или вставка монеты в разблокированный автомат ничего не дает.
- (г) Машина, в которой закончились конфеты, игнорирует все входные данные.

Для начала создадим функцию, которая реализует правила выше и
по переданному `Input`-у возвращает функцию изменения состояния машины:

```dotty
object Candy:
  private val updateMachine: Input => Machine => Machine =
    (i: Input) =>
      (s: Machine) =>
        (i, s) match
          case (_, Machine(_, 0, _))              => s      // (г)
          case (Input.Coin, Machine(false, _, _)) => s      // (в)
          case (Input.Turn, Machine(true, _, _))  => s      // (в)
          case (Input.Coin, Machine(true, candy, coin)) =>  // (а)
            Machine(false, candy, coin + 1)
          case (Input.Turn, Machine(false, candy, coin)) => // (б)
            Machine(true, candy - 1, coin)
```

Теперь создадим метод `simulateMachine`, который управляет машиной на основе списка входных данных 
и возвращает количество монет и конфет, оставшихся в машине в конце:

```dotty
object Candy:
  def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] =
    for
      _ <- State.traverse(inputs)(input => State.modify(updateMachine(input)))
      s <- State.get
    yield (s.coins, s.candies)
```

Первая строка `State.traverse(inputs)(input => State.modify(updateMachine(input)))` возвращает `State`, 
преобразующий входное состояние машины в выходное с вычислением значения типа `List[Unit]`, 
которое нас не интересует.
Вторая строка возвращает выходное состояние в качестве значения.
Из выходного состояния мы можем получить итоговое количество монет и конфет после всех преобразований.

#### Итого

Метод `simulateMachine` по списку операций (вставить монету или повернуть ручку) возвращает `State[Machine, (Int, Int)]` - 
по своей сути функцию с типом `Machine => ((Int, Int), Machine)`.
Осталось только её запустить.

Например, если входные данные `Machine` содержат 10 монет и 5 конфет
и всего успешно куплено 4 конфеты, то получим следующие выходные данные `(14, 1)`:

```dotty
val initialMachine = Machine(true, 5, 10)

val inputs =
  List(Coin, Turn, Coin, Coin, Coin, Turn, Turn, Turn, Coin, Turn, Coin, Turn)

val runner = Candy.simulateMachine(inputs)
runner.run(initialMachine)
// ((14, 1), Machine(true, 1, 14))
```

4 и 5 операции - повторная вставка монеты, а также 7 и 8 - поворачивание ручки на заблокированном автомате,
не изменяют состояния, поэтому на выходе получаем результат успешной покупки 4 конфет.

## Реализация в библиотеках

### Реализация в Cats

```dotty
import cats.data.State

val addOne = State[Int, String] { num =>
  val ans = num + 1
  (ans, s"Результат инкремента: $ans")
}

val double = State[Int, String] { num =>
  val ans = num * 2
  (ans, s"Результат удваивания: $ans")
}

val both =
  for
    a <- addOne
    b <- double
  yield (a, b)

val (state, result) = both.run(20).value


val (state, result) = both.run(20).value
// val state: Int = 42
// val result: (String, String) = (Результат инкремента: 21,Результат удваивания: 42)
```

---

**Ссылки:**

- [Тройка полезных монад](https://habr.com/ru/articles/184722/)
- [Cats](https://typelevel.org/cats/datatypes/state.html)
- [Functional Programming in Scala, Second Edition, Chapter 6](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
- [Herding Cats](http://eed3si9n.com/herding-cats/State.html)
- [Memorisation with State using Scala](https://blog.tmorris.net/posts/memoisation-with-state-using-scala/index.html)
- [Scala with Cats](https://www.scalawithcats.com/dist/scala-with-cats.html#the-state-monad)
