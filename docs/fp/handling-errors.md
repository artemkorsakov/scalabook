# Обработка ошибок

## Основные принципы функционального возникновения и обработки ошибок

Создание исключений является побочным эффектом, поэтому не используется в функциональном коде. 
Вместо этого можно представлять сбои и исключения с помощью обычных значений, 
и можно писать функции высшего порядка, которые абстрагируются от общих шаблонов обработки ошибок и восстановления. 
Функциональное решение, возвращающее ошибки в виде значений, 
является более безопасным и сохраняет ссылочную прозрачность, 
а благодаря использованию функций высшего порядка можно сохранить основное преимущество исключений — 
консолидацию логики обработки ошибок. 

Есть две основные проблемы с исключениями: 

- исключения нарушают **ссылочную прозрачность** (RT - _referentially transparent_) **и вводят зависимость от контекста**, 
уводя от простых рассуждений модели подстановки и позволяя писать запутанный код на основе исключений. 
Исключения следует использовать только для обработки ошибок, а не для потока управления. 
- **исключения не являются типобезопасными**. Тип `failingFn`, `Int => Int`, ничего не говорит о том, 
что могут возникать исключения, и компилятор, конечно же, 
не будет заставлять вызывающую функцию `failingFn` принимать решение о том, как обрабатывать эти исключения. 
Если забыть проверить наличие исключения в `failingFn`, это не будет обнаружено до времени выполнения.

### Возможные альтернативы исключениям 

Рассмотрим ситуацию, когда можно было бы использовать исключение, 
и рассмотрим различные подходы, которые можно было бы использовать вместо него. 
Вот реализация функции, вычисляющей среднее значение списка, которое не определено, если список пуст:

```scala
def mean(xs: Seq[Double]): Double =
  if xs.isEmpty then
    throw new ArithmeticException("mean of empty list!")
  else xs.sum / xs.length
```

Функция среднего — это пример так называемой _частичной функции_: она не определена для некоторых входных данных. 
Функция обычно является частичной, потому что она делает некоторые предположения о своих входных данных, 
которые не подразумеваются типами входных данных.

Рассмотрим несколько альтернатив исключениям. 

Первая возможность — вернуть какое-то поддельное значение типа `Double`. 
Можно было бы просто вернуть `xs.sum / xs.length` во всех случаях и получить результат `0.0/0.0`, 
когда ввод пуст, что равно `Double.NaN`; 
или можно было бы вернуть какое-то другое сигнальное значение. 
В других ситуациях можно было бы вернуть `null` вместо значения нужного типа. 
Это решение следует отвергнуть по нескольким причинам:

- Это позволяет ошибкам распространяться незаметно — вызывающая сторона может просто забыть проверить это условие 
и не будет предупреждена компилятором, что может привести к тому, что последующий код не будет работать должным образом. 
Часто ошибка будет обнаружена в коде намного позже. 
- Помимо того, что метод подвержен ошибкам, он приводит к изрядному количеству шаблонного кода на сайтах вызовов 
с явными операторами `if` для проверки того, получил ли вызывающий «настоящий» результат. 
Этот шаблон увеличивается, если вызвать несколько функций, каждая из которых использует коды ошибок,
которые должны быть проверены и каким-то образом агрегированы.
- Это не применимо к полиморфному коду. 
Для некоторых типов вывода может даже не быть сигнального значения нужного типа! 
Рассмотрим такую функцию, как `max`, которая находит максимальное значение в последовательности 
в соответствии с пользовательской функцией сравнения: `def max[A](xs: Seq[A])(greater: (A,A) => Boolean): A`. 
Если ввод пуст, мы не можем изобрести значение типа `A`. 
Здесь также нельзя использовать `null`, поскольку `null` допустим только для непримитивных типов, 
а `A` на самом деле может быть примитивом, таким как `Double` или `Int`.
- Для этого требуется особая политика или соглашение о вызовах вызывающих объектов — 
правильное использование функции `mean` потребует, чтобы вызывающие выполняли что-то иное, 
кроме вызова `mean` и использования результата. 
Предоставление функциям специальных политик, подобных этой, затрудняет их передачу функциям высшего порядка, 
которые должны обрабатывать все аргументы единообразно.

Вторая возможность — заставить вызывающую программу предоставить аргумент, 
который говорит, что делать, если мы не знаем, как обрабатывать ввод:

```scala
def mean(xs: Seq[Double], onEmpty: Double): Double =
  if xs.isEmpty then onEmpty
  else xs.sum / xs.length
```

Это превращает `mean` в полную функцию, но у нее есть недостатки — 
она требует, чтобы вызывающие функции имели непосредственное представление о том, 
как обрабатывать неопределенный случай, и ограничивает их возвратом значения `Double`. 
Что, если среднее значение вызывается как часть более крупного вычисления, 
и хотелось бы прервать это вычисление, если среднее значение не определено? 
Или, может быть, в этом случае мы хотели бы выбрать совершенно другую ветвь вычислений? 
Простая передача параметра `onEmpty` не дает этой свободы. 
Нам нужен способ отложить решение о том, как обрабатывать неопределенные случаи, 
чтобы их можно было обрабатывать на наиболее подходящем уровне.

## Тип данных Option

Решение состоит в том, чтобы явно указать в возвращаемом типе, что функция не всегда может иметь ответ. 
Можно думать об этом как о передаче вызывающей стороне стратегии обработки ошибок. 

```scala
enum Option[+A]:
  case Some(get: A)
  case None
```

`Option` имеет два случая: он может быть определен, и в этом случае он равен `Some`, 
или не определен, и в этом случае - `None`. 
Можно использовать `Option` для определения среднего значения следующим образом:

```scala
import Option.{Some, None}

def mean(xs: Seq[Double]): Option[Double] =
  if xs.isEmpty then None
  else Some(xs.sum / xs.length)
```

Тип возвращаемого значения теперь отражает возможность того, что результат не всегда может быть определен. 
Функция по-прежнему всегда возвращает результат объявленного типа (теперь `Option[Double]`), 
т.е. это общая функция. 
Каждое значение входного типа принимает ровно одно значение выходного типа.

### Сценарии использования основных функций Option

Основными операциями `Option` являются:

```scala
def map[B](f: A => B): Option[B]

def getOrElse[B >: A](default: => B): B

def flatMap[B](f: A => Option[B]): Option[B]

def orElse[B >: A](ob: => Option[B]): Option[B]

def filter(f: A => Boolean): Option[A]
```

Ниже даны некоторые рекомендации о том, когда использовать каждую из них.

Функцию `map` можно использовать для преобразования результата внутри параметра, если он существует. 
Можно думать об этом как о продолжении вычислений в предположении, что ошибки не произошло; 
это также способ отложить обработку ошибок на более поздний код:

`flatMap` аналогичен, за исключением того, 
что функция, предоставляемая для преобразования результата, сама по себе может дать сбой.

Пример:

```scala
case class Employee(name: String, department: String, manager: Option[Employee])
def lookupByName(name: String): Option[Employee] =
  name match
    case "Jack" => Some(Employee("Jack", "department1", None))
    case "Joe"  => Some(Employee("Joe", "department2", lookupByName("Jack")))
    case _      => None
lookupByName("Joe").map(_.department)
// res1: Option[String] = Some(value = "department2")
lookupByName("Joe").flatMap(_.manager)
// res2: Option[Employee] = Some(
//   value = Employee(name = "Jack", department = "department1", manager = None)
// )
```

`filter` можно использовать для преобразования успехов в неудачи, 
если успешные значения не соответствуют заданному предикату. 
Распространенным шаблоном является преобразование `Option` через вызовы `map`, `flatMap` и/или `filter`, 
а затем использование `getOrElse` для обработки ошибок в конце:

```scala
lookupByName("Joe")
  .map(_.department)
  .filter(_ != "department2")
  .getOrElse("Default Dept")
// res3: String = "Default Dept"  
```

`getOrElse` используется для преобразования из `Option[String]` в `String`, 
предоставляя значение по умолчанию на случай ошибки. 
`orElse` похож на `getOrElse`, за исключением того, что возвращается другой `Option`, если первый не определен. 
Это часто бывает полезно, когда нужно вместе связать возможные неудачные вычисления, 
пробуя второе, если первое не удалось. 
Обычная идиома состоит в том, чтобы сделать `o.getOrElse(throw new Exception("FAIL"))` 
для преобразования варианта `None` обратно в исключение. 

Общее эмпирическое правило состоит в том, что исключения используются только в том случае, 
если никакая разумная программа никогда не перехватит исключение; 
если для некоторых вызывающих объектов исключение может быть исправимой ошибкой,
используется `Option` (или `Either`, обсуждается позже), чтобы предоставить гибкость.

## Тип данных Either

Основная идея заключается в том, что сбои и исключения можно представлять с помощью обычных значений 
и писать функции, которые абстрагируются от общих шаблонов обработки ошибок и восстановления.
`Option` — не единственный тип данных, который можно использовать для этой цели, 
и хотя он используется часто, он довольно прост. 
`Option` ничего не говорит о том, что пошло не так в случае падения. 
Все, что он может сделать, это дать `None`, указывая на отсутствие значения. 
Но иногда необходимо узнать больше. 
Например, может понадобиться строка, которая дает больше информации, 
или, если возникло исключение, хотелось бы узнать, что это за ошибка на самом деле. 

Можно создать тип данных, который кодирует любую необходимую информацию о сбоях. 
Иногда достаточно просто знать, произошел ли сбой, и в этом случае можно использовать `Option`; 
в других случаях нужна дополнительная информация. 
В этом разделе будет рассмотрено простое расширение `Option` - 
тип данных `Either`, который позволяет отслеживать причину сбоя. 

Его определение:

```scala
enum Either[+E, +A]:
  case Left(value: E)
  case Right(value: A)
```

`Either` имеет только два варианта (_case_), как и `Option`. 
Существенная разница состоит в том, что оба варианта содержат значение. 
Тип данных `Either` представляет в очень общем виде значения, которые могут быть одним из двух. 
Можно сказать, что это дизъюнктное объединение двух типов. 
По соглашению конструктор `Right` зарезервирован для случая успеха, а конструктор `Left` - для неудачи.

Пример `mean`, возвращающий `String` в случае ошибки:

```scala
def mean(xs: Seq[Double]): Either[String, Double] =
  if xs.isEmpty then
    Left("Пустой список!")
  else
    Right(xs.sum / xs.length)
```

`Left` можно использовать и для возврата самого исключения:

```scala
import scala.util.control.NonFatal

def catchNonFatal[A](a: => A): Either[Throwable, A] =
  try Right(a)
  catch case NonFatal(t) => Left(t)
```


---

**Ссылки:**

- [Functional Programming in Scala, Second Edition, Chapter 4](https://www.manning.com/books/functional-programming-in-scala-second-edition?query=Functional%20Programming%20in%20Scala,%20Second%20Edition)
