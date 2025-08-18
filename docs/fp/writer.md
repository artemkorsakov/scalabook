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

Ключевые характеристики:

- Сочетает результат вычисления (`A`) с накапливаемым логом (`W`)
- Лог должен предоставлять операцию комбинирования
- Гарантирует порядок выполнения и целостность логов даже в параллельных вычислениях
- Альтернатива традиционному логгированию с побочными эффектами

```dotty
case class Writer[W, A](run: () => (W, A))
```

Параметры типа:

- `W` - тип лога (обычно `String`, `List[String]` или специальный тип логов)
- `A` - тип результата вычисления

## Функции общего назначения

### map

Аналогично функциональному состоянию, `map` позволяет при наличии базового журнала `Writer[S, A]`
и функции преобразования значения из типа `A` в тип `B` получать `Writer[S, B]` -
функциональный журнал с вычислением значения типа `B`.

Математическая основа:

```math
map : (A → B) → Writer[W, A] → Writer[W, B]
```

Реализация:

```dotty
object Writer:
  extension [W, A](underlying: Writer[W, A])
    def map[B](f: A => B): Writer[W, B] =
      val (w, a) = underlying.run()
      Writer[W, B](() => (w, f(a)))
```

Пример использования:

```dotty
val initial = Writer[String, Int](() => ("Начальное значение:", 1))

// Преобразуем Int в Double, сохраняя лог
val writer = initial.map(x => x.toDouble * 1.5)
writer.run()
// ("Начальное значение:", 1.5)
```

### flatMap

Основная операция, позволяющая последовательно комбинировать вычисления с сохранением и объединением логов. 
Требует функцию комбинирования логов.

Математическая основа:

```math
flatMap : (A → Writer[W, B]) → Writer[W, A] → Writer[W, B]
```

Реализация:

```dotty
object Writer:
  extension [W, A](underlying: Writer[W, A])
    def flatMap[B](f: A => Writer[W, B])(using
        combine: (W, W) => W
    ): Writer[W, B] =
      Writer[W, B] { () =>
        val (w1, a) = underlying.run()
        val (w2, b) = f(a).run()
        (combine(w1, w2), b)
      }
```

Пример использования:

```dotty
val initial = Writer[String, Int](() => ("Начало:", 10))

def process(x: Int): Writer[String, Int] =
  if x > 5 then Writer(() => ("Больше пяти,", x * 2))
  else Writer(() => ("Меньше или равно пяти,", x))

given combine: ((String, String) => String) = _ + _

val result = initial.flatMap(process)
result.run()
// ("Начало:Больше пяти,", 20)
```

### unit (pure)

Конструктор, создающий `Writer` с заданным значением и пустым логом. 

Математическая основа:

```math
unit : A → Writer[W, A]
```

Реализация:

```dotty
object Writer:
  def unit[W, A](a: => A)(empty: W): Writer[W, A] =
    Writer[W, A](() => (empty, a))
```

Примеры использования:

```dotty
// Для String лога
val w1 = Writer.unit(42)("")
w1.run()  // ("", 42)

// Для List[String] лога
val w2 = Writer.unit("result")(Nil)
w2.run()  // (List(), "result")
```

### Дополнительные полезные операции

#### tell

Добавляет запись в лог без изменения результата:

```dotty
def tell[W](w: W): Writer[W, Unit] =
  Writer(() => (w, ()))
```

Пример:

```dotty
given combine: ((String, String) => String) = _ + _

val computation =
  for
    _ <- Writer.tell("Начало вычисления")
    x <- Writer.unit(10)("")
    _ <- Writer.tell(s"Получено значение: $x")
  yield x * 2

computation.run()
// ("Начало вычисленияПолучено значение: 10", 20)
```

#### listen

Возвращает не только результат, но и текущий лог:

```dotty
case class Writer[W, A](run: () => (W, A)):
  def listen: Writer[W, (A, W)] =
    Writer { () =>
      val (w, a) = run()
      (w, (a, w))
    }
```

## Практическое применение

**Логгирование в чистых функциях**:

```dotty
def parseNumber(s: String): Writer[List[String], Int] =
  try
    val num = s.toInt
    Writer(() => (List(s"Успешно разобрано: $num"), num))
  catch
    case _: NumberFormatException =>
      Writer(() => (List(s"Ошибка разбора: $s"), 0))

parseNumber("42").listen.run()
// (List(Успешно разобрано: 42),(42,List(Успешно разобрано: 42)))
parseNumber("42abc").listen.run()
// (List(Ошибка разбора: 42abc),(0,List(Ошибка разбора: 42abc)))
```

**Сбор метрик**:

```dotty
def processRequest(req: Request): Writer[Stats, Response] = 
  val start = System.currentTimeMillis()
  // ... обработка ...
  val end = System.currentTimeMillis()
  val stats = Stats(1, end - start)
  Writer(() => (stats, response))
```

**Транзакционные операции**:

```dotty
def transfer(from: Account, to: Account, amount: Money): Writer[TransactionLog, Unit] =
  for
    _ <- Writer.unit(from.withdraw(amount))(TransactionLog.empty)
    _ <- Writer.unit(to.deposit(amount))(TransactionLog.empty)
    _ <- tell(TransactionRecord(from, to, amount, "Completed"))
  yield ()
```

## Упражнения

### Упражнение 7.1.1: Базовые операции

> [Репозиторий с упражнениями и инструкцией по выполнению][sample]

**Задача**: Реализуйте вычисление, которое:

1. Начинает с числа 5
2. Добавляет в лог сообщение "Начальное значение: 5"
3. Умножает число на 2
4. Добавляет в лог сообщение "Умножение на 2"
5. Вычитает 3
6. Добавляет в лог сообщение "Вычитание 3"

```dotty   
val computation: Writer[List[String], Int] = 
  ???
```  

**Примеры:**

```dotty  
val result = computation.run()
// (List("Начальное значение: 5", "Умножение на 2", "Вычитание 3"), 7)
```  

**Подсказка**: Используйте:

- `Writer.unit` для начального значения
- `flatMap` для последовательных операций
- `List[String]` как тип лога
- Операцию `++` для комбинирования списков

@:note(🔍 Показать решение)

```dotty
import Writer.*

given combine: ((List[String], List[String]) => List[String]) = _ ++ _

def computation: Writer[List[String], Int] =
  // Создаем начальный `Writer` с числом `5` и первым сообщением:
  Writer.unit(5)(List("Начальное значение: 5"))
    // Умножаем на `2` и добавляем сообщение:
    .flatMap(x => Writer(() => (List("Умножение на 2"), x * 2)))
    // Вычитаем `3` и добавляем сообщение:
    .flatMap(x => Writer(() => (List("Вычитание 3"), x - 3)))

val (log, result) = computation.run()
println(s"Лог: $log")
println(s"Результат: $result")
// Лог: List(Начальное значение: 5, Умножение на 2, Вычитание 3)
// Результат: 7
``` 

**Альтернативное решение с for-comprehension**:

```dotty
def computationFor: Writer[List[String], Int] = 
  for
    x <- Writer.unit(5)(List("Начальное значение: 5"))
    y <- Writer(() => (List("Умножение на 2"), x * 2))
    z <- Writer(() => (List("Вычитание 3"), y - 3))
  yield z
```

@:@

### Упражнение 7.1.2: Композиция операций

**Задача**: Создайте функцию `sqrtWithLog`, которая:

1. Принимает `Double`
2. Проверяет, что число неотрицательное (если нет - добавляет в лог "Ошибка: отрицательное число")
3. Вычисляет квадратный корень (для отрицательных чисел возвращает `Double.NaN`)
4. Добавляет в лог "Вычислен квадратный корень" (только для успешных вычислений)
5. Возвращает `Writer[List[String], Double]`

```dotty
def sqrtWithLog(x: Double): Writer[List[String], Double] =
  ???
```  

**Примеры:**

```dotty  
sqrtWithLog(9.0).run()
// (List("Вычислен квадратный корень"), 3.0)

sqrtWithLog(-4.0).run()
// (List("Ошибка: отрицательное число"), Double.NaN)
```  

**Подсказка**:

- Используйте `tell` для добавления сообщений об ошибках
- Для успешного случая используйте `unit` с результатом и сообщением
- Обратите внимание, что сообщение о вычислении корня добавляется только для положительных чисел

@:note(🔍 Показать решение)

Функция `sqrtWithLog` вычисляет квадратный корень числа `x` типа `Double`, 
сопровождая процесс логами в виде списка строк: при отрицательном `x` записывает ошибку и возвращает `NaN`, 
а для неотрицательного — возвращает корень и сообщение об успешном вычислении.

```dotty
def sqrtWithLog(x: Double): Writer[List[String], Double] =
  if x < 0 then
    tell(List("Ошибка: отрицательное число")).map(_ => Double.NaN)
  else
    val result = math.sqrt(x)
    unit(result)(List("Вычислен квадратный корень"))
```

@:@

### Упражнение 7.1.3: Валидация данных

**Задача**: Создайте функцию `validatePerson`, которая проверяет:

1. Возраст >= 18 (иначе "Возраст меньше 18")
2. Имя не пустое (иначе "Имя не может быть пустым")
3. Лог должен содержать все ошибки (не останавливаться на первой)

**Сигнатура**:

```dotty
case class Person(name: String, age: Int)

def validatePerson(p: Person): Writer[List[String], Person] = ???
```

**Пример**:

```dotty
validatePerson(Person("", 17)).run()
// (List("Имя не может быть пустым", "Возраст меньше 18"), Person("", 17))

validatePerson(Person("Alice", 25)).run()
// вернет Writer(Nil, Person("Alice", 25))
```

@:note(🔍 Показать решение)

Функция `validatePerson` проверяет `Person`, используя `Writer` для накопления ошибок валидации: 
если возраст меньше 18, добавляется сообщение `"Возраст меньше 18"`, а если имя пустое — `"Имя не может быть пустым"`. 
В результате возвращается `Writer`, содержащий либо список ошибок, либо валидный объект `Person`, если ошибок нет. 

```dotty  
def validatePerson(p: Person): Writer[List[String], Person] =
  val ageCheck: Writer[List[String], Unit] =
    if p.age < 18 then
      Writer.tell[List[String]](List("Возраст меньше 18"))
    else Writer.unit[List[String], Unit](())(Nil)
  val nameCheck: Writer[List[String], Unit] =
    if p.name.isEmpty then
      Writer.tell[List[String]](List("Имя не может быть пустым"))
    else Writer.unit[List[String], Unit](())(Nil)

  for
    _ <- ageCheck
    _ <- nameCheck
  yield p
```  

@:@

### Упражнение 7.1.4: Трансформация лога

**Задача**: Создайте метод `mapLog` для `Writer`, который позволяет преобразовывать значения в логе, 
сохраняя при этом результат вычислений неизменным.

**Требования**:

1. Метод должен принимать функцию преобразования лога `W => W2`
2. Возвращать новый `Writer` с преобразованным логом, но тем же значением `A`

**Сигнатура**:

```dotty
def mapLog[W2](f: W => W2): Writer[W2, A] = ???
```

**Пример**:

```dotty
Writer(() => (List("a", "b"), 42))
  .mapLog(_.mkString(","))
  .run()
// Результат: ("a,b", 42)
```  

@:note(🔍 Показать решение)

Метод `mapLog` преобразует лог `Writer[W, A]`, 
применяя функцию `f` к значению лога `W`, оставляя результат `A` без изменений; 
при этом вычисление остаётся ленивым — оригинальный `Writer` выполняется только при вызове `run()`, 
возвращая новый кортеж `(f(w), a)`. 
Это позволяет изменять формат или содержимое лога (например, конвертировать список в строку), 
не затрагивая основное вычисление.

```dotty
def mapLog[W2](f: W => W2): Writer[W2, A] =
  Writer { () =>
    val (w, a) = underlying.run()
    (f(w), a)
  }
```

@:@

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Тройка полезных монад](https://habr.com/ru/articles/184722/)
- Category Theory for Programmers - Bartosz Milewski:
    - [Теория категорий для программистов](https://henrychern.wordpress.com/2022/09/15/%d1%82%d0%b5%d0%be%d1%80%d0%b8%d1%8f-%d0%ba%d0%b0%d1%82%d0%b5%d0%b3%d0%be%d1%80%d0%b8%d0%b9-%d0%b4%d0%bb%d1%8f-%d0%bf%d1%80%d0%be%d0%b3%d1%80%d0%b0%d0%bc%d0%bc%d0%b8%d1%81%d1%82%d0%be%d0%b2/)
    - [Category Theory for Programmers, Functoriality](https://bartoszmilewski.com/2015/02/03/functoriality/)
    - [Category Theory for Programmers, Kleisli Categories](https://bartoszmilewski.com/2014/12/23/kleisli-categories/)
- [Cats](https://typelevel.org/cats/datatypes/writer.html)
- [Herding Cats](http://eed3si9n.com/herding-cats/Writer.html)
- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Writer.html)
- [Scala with Cats](https://scalawithcats.com/dist/scala-with-cats.html#writer-monad)

[sample]: https://gitverse.ru/scalabook/fp
