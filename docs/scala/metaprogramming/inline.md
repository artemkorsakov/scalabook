# Inline

Встраивание (`inline`) — это распространенный метод метапрограммирования во время компиляции, 
обычно используемый для оптимизации производительности. 
Концепция встраивания предоставляет собой точку входа в программирование с помощью макросов.

1. Вводится `inline` как [мягкий модификатор](https://scalabook.gitflic.space/docs/scala/soft-keywords).
2. Гарантируется, что встраивание происходит на самом деле, а не с "максимальной эффективностью".
3. Вводятся операции, которые гарантированно оцениваются во время компиляции.

### Inline Constants

Простейшей формой встраивания является встраивание констант в программы:

```scala
inline val pi = 3.141592653589793
inline val pie = "🥧"
```

Использование ключевого слова `inline` в определениях значений гарантирует, 
что все ссылки на `pi` и `pie` являются встроенными:

```scala
val pi2 = pi + pi
// pi2: Double = 6.283185307179586
val pie2 = pie + pie
// pie2: String = "🥧🥧"
```

В приведенном выше коде ссылки `pi` и `pie` встроены. 
Затем компилятор применяет оптимизацию под названием "свертывание констант", 
которая вычисляет результирующее значение `pi2` и `pie2` во время компиляции.

> Inline (Scala 3) vs. final (Scala 2)
> 
> В Scala 2 использовался бы модификатор `final` в определении без возвращаемого типа:
> 
> ```scala
> final val pi = 3.141592653589793
> final val pie = "🥧"
> ```
> 
> Модификатор `final` обеспечит, что `pi` и `pie` примет литеральный тип. 
> Затем оптимизация распространения констант в компиляторе может выполнить встраивание для таких определений. 
> Однако эта форма постоянного распространения не гарантируется. 
> Scala 3.0 также поддерживает `final val` - inlining как встраивание с максимальной эффективностью для целей миграции.

В настоящее время только константные выражения могут появляться в правой части определения встроенного значения. 
Таким образом, следующий код недействителен, хотя компилятор знает, 
что правая часть является постоянным значением времени компиляции:

```scala
val pi = 3.141592653589793
inline val pi2 = pi + pi // error
```

Обратите внимание, что при определении `inline val pi` добавление может быть вычислено во время компиляции. 
Это устраняет указанную выше ошибку и `pi2` получает литеральный тип `6.283185307179586d`.


### Inline Methods

Также можно использовать модификатор `inline` для определения метода, который должен быть встроен в точку вызова:

```scala
inline def logged[T](level: Int, message: => String)(inline op: T): T =
  println(s"[$level]Computing $message")
  val res = op
  println(s"[$level]Result of $message: $res")
  res
```

Когда вызывается такой встроенный метод `logged`, его тело будет развернуто на месте вызова во время компиляции! 
То есть вызов `logged` будет заменен телом метода. 
Предоставленные аргументы статически заменяются параметрами `logged`, соответственно. 
Поэтому компилятор встраивает следующий вызов

```scala
logged(logLevel, getMessage()) {
  computeSomething()
}
```

и переписывает его на:

```scala
val level   = logLevel
def message = getMessage()

println(s"[$level]Computing $message")
val res = computeSomething()
println(s"[$level]Result of $message: $res")
res
```

Встроенные методы всегда должны применяться полностью. Например, вызов

```
logged[String](1, "some message")
```

будет неправильно сформирован, и компилятор будет жаловаться на отсутствие аргументов.
Однако можно передавать аргументы с подстановочными знаками.
Например,

```
logged[String](1, "some message")(_)
```

##### Семантика встроенных методов

Пример метода `logged` использует три разных типа параметров, иллюстрируя, как встраивание обрабатывает эти параметры:

1. **Параметры по значению**. Компилятор создает `val` привязку для параметров по значению. 
Таким образом, выражение аргумента оценивается только один раз перед сокращением тела метода.
Это видно по параметру `level` из примера. 
В некоторых случаях, когда аргументы являются чистыми постоянными значениями, 
привязка опускается и значение встраивается напрямую.
2. **Параметры по имени**. Компилятор создает `def` привязку для параметров по имени. 
Таким образом, выражение аргумента оценивается каждый раз, когда оно используется, но код является общим.
Это видно по методу `message` из примера.
3. **Встроенные параметры**. Встроенные параметры не создают привязок и просто встраиваются. 
Таким образом, их код дублируется везде, где они используются.
Это видно по параметру `op` из примера.

Способ преобразования различных параметров гарантирует, что встраивание вызова не изменит его семантику. 
Это означает, что первоначальная обработка (разрешение перегрузки, неявный поиск и т. д.), 
выполняемая при вводе тела встроенного метода, не изменится при встроенном методе.

Например, рассмотрим следующий код:

```scala
class Logger:
  def log(x: Any): Unit = println(x)

class RefinedLogger extends Logger:
  override def log(x: Any): Unit = println("Any: " + x)
  def log(x: String): Unit = println("String: " + x)

inline def logged[T](logger: Logger, x: T): Unit =
  logger.log(x)
```

Отдельная проверка типа `logger.log(x)` разрешает вызов метода `Logger.log`, который принимает аргумент типа `Any`. 

Теперь, учитывая следующий код:

```scala
logged(new RefinedLogger, "✔")
// Any: ✔
```

Он расширяется до:

```scala
val logger = new RefinedLogger
val x = "✔"
logger.log(x)
// String: ✔
```

Несмотря на то, что теперь известно, что `x` - это `String`, 
вызов `logger.log(x)` по-прежнему разрешается в метод `Logger.log`, который принимает аргумент типа `Any`. 
Обратите внимание, что из-за позднего связывания фактический метод, вызываемый во время выполнения, 
будет переопределенным методом `RefinedLogger.log`.

> Встраивание сохраняет семантику.
> Независимо от того, определен ли `logged` как `def` или `inline def`, 
> он выполняет одни и те же операции с некоторыми отличиями в производительности.

#### Встроенные параметры

Одним из важных применений встраивания является обеспечение постоянной оптимизации свертывания за пределами методов. 
Встроенные параметры не создают привязок, и их код дублируется везде, где они используются.

```scala
inline def perimeter(inline radius: Double): Double =
  2.0 * pi * radius
```

В приведенном выше примере ожидается, что если `radius` известен статически, 
то все вычисления могут быть выполнены во время компиляции. 
Следующий вызов:

```scala
perimeter(5.0)
```

переписывается на:

```scala
2.0 * pi * 5.0
```

Затем встраивается `pi` (вначале принимаются `inline val` определения - `radius`):

```scala
2.0 * 3.141592653589793 * 5.0
```

Наконец, постоянно свернут до

```scala
31.4159265359
```

> **Встроенные параметры следует использовать только один раз.**
> Нужно быть осторожным при использовании встроенного параметра **более одного раза**. Рассмотрим следующий код:
> 
> ```scala
> inline def printPerimeter(inline radius: Double): Double =
>   println(s"Perimeter (r = $radius) = ${perimeter(radius)}")
> ```
> 
> Он отлично работает, когда передается константа или ссылка на `val`.
> 
> ```scala
> printPerimeter(5.0)
> // встраивается как
> println(s"Perimeter (r = ${5.0}) = ${31.4159265359}")
> ```
> 
> Но если передается большее выражение (возможно, с побочными эффектами), можно случайно дублировать работу.
> 
> ```scala
> printPerimeter(longComputation())
> // встраивается как
> println(s"Perimeter (r = ${longComputation()}) = ${6.283185307179586 * longComputation()}")
> ```

Полезным применением встроенных параметров является предотвращение создания замыканий, 
вызванных использованием параметров по имени.

```scala
inline def assert1(cond: Boolean, msg: => String) =
  if !cond then
    throw new Exception(msg)

assert1(x, "error1")
// is inlined as
val cond = x
def msg = "error1"
if !cond then
    throw new Exception(msg)
```

В приведенном выше примере видно, что использование параметра по имени приводит к локальному определению `msg`, 
которое выделяет замыкание перед проверкой условия.

Если вместо этого использовать встроенный параметр, можно гарантировать, 
что условие будет проверено до того, как будет достигнут любой код, обрабатывающий исключение. 
В случае утверждения этот код никогда не должен быть достигнут.

```scala
inline def assert2(cond: Boolean, inline msg: String) =
  if !cond then
    throw new Exception(msg)

assert2(x, "error2")
// is inlined as
val cond = x
if !cond then
    throw new Exception("error2")
```

В следующем примере показана разница в переводе между by-value, by-name и `inline` параметрами:

```scala
inline def funkyAssertEquals(actual: Double, expected: => Double, inline delta: Double): Unit =
  if (actual - expected).abs > delta then
    throw new AssertionError(s"difference between ${expected} and ${actual} was larger than ${delta}")

funkyAssertEquals(computeActual(), computeExpected(), computeDelta())
// translates to
//
//   val actual = computeActual()
//   def expected = computeExpected()
//   if (actual - expected).abs > computeDelta() then
//     throw new AssertionError(s"difference between ${expected} and ${actual} was larger than ${computeDelta()}")
```

Обычно встроенные параметры используются, когда необходимо распространить постоянные значения,
чтобы обеспечить дальнейшую оптимизацию/сокращение.

### Рекурсивные встроенные методы

Встроенные методы могут быть рекурсивными. 
Например, при вызове с постоянным `n` следующий метод `power` 
будет реализован прямым встроенным кодом без какого-либо цикла или рекурсии.

```scala
inline def power(x: Double, n: Int): Double =
  if n == 0 then 1.0
  else if n == 1 then x
  else
    val y = power(x, n / 2)
    if n % 2 == 0 then y * y else y * y * x

power(expr, 10)
// translates to
//
//   val x = expr
//   val y1 = x * x   // ^2
//   val y2 = y1 * y1 // ^4
//   val y3 = y2 * x  // ^5
//   y3 * y3          // ^10
```

### Встроенные условия

Если условием `if` является известная константа (`true` или `false`), то возможно, 
что после встраивания и сворачивания констант, условное выражение частично вычислится и сохранится только одна ветвь.

Например, следующий метод `power` содержит некоторые условия `if`, 
которые потенциально могут развернуть рекурсию и удалить все вызовы методов.

```scala
inline def power(x: Double, inline n: Int): Double =
  if (n == 0) 1.0
  else if (n % 2 == 1) x * power(x, n - 1)
  else power(x * x, n / 2)
```

Вызов `power` со статически известными константами приводит к следующему коду:

```scala
power(2, 2)
// первое встраивание
val x = 2
if (2 == 0) 1.0 // мертвая ветка
else if (2 % 2 == 1) x * power(x, 2 - 1) // мертвая ветка
else power(x * x, 2 / 2)
// частично свернулось до
val x = 2
power(x * x, 1)
```

Дальнейшие шаги встраивания:

```scala
// дальнейшее встраивание
val x = 2
val x2 = x * x
if (1 == 0) 1.0 // мертвая ветка
else if (1 % 2 == 1) x2 * power(x2, 1 - 1)
else power(x2 * x2, 1 / 2) // мертвая ветка
// частично свернулось до
val x = 2
val x2 = x * x
x2 * power(x2, 0)
// дальнейшее встраивание
val x = 2
val x2 = x * x
x2 * {
  if (0 == 0) 1.0
  else if (0 % 2 == 1) x * power(x, 0 - 1) // мертвая ветка
  else power(x * x, 0 / 2) // мертвая ветка
}
// частично свернулось до
val x = 2
val x2 = x * x
x2 * 1.0
```

Напротив, представим, что значение `n` неизвестно:

```scala
power(2, unknownNumber)
```

Руководствуясь встроенной аннотацией параметра, компилятор попытается развернуть рекурсию. 
Но безуспешно, так как параметр не известен статически.

```scala
// первое встраивание
val x = 2
if (unknownNumber == 0) 1.0
else if (unknownNumber % 2 == 1) x * power(x, unknownNumber - 1)
else power(x * x, unknownNumber / 2)
// дальнейшее встраивание
val x = 2
if (unknownNumber == 0) 1.0
else if (unknownNumber % 2 == 1) x * {
  if (unknownNumber - 1 == 0) 1.0
  else if ((unknownNumber - 1) % 2 == 1) x2 * power(x2, unknownNumber - 1 - 1)
  else power(x2 * x2, (unknownNumber - 1) / 2)
}
else {
  val x2 = x * x
  if (unknownNumber / 2 == 0) 1.0
  else if ((unknownNumber / 2) % 2 == 1) x2 * power(x2, unknownNumber / 2 - 1)
  else power(x2 * x2, unknownNumber / 2 / 2)
}
// компиляция никогда не закончится
...
```

Чтобы гарантировать, что ветвление действительно может быть выполнено во время компиляции, 
можно использовать `inline if` вариант `if`. 
Аннотирование условного выражения с помощью `inline` гарантирует, 
что условное выражение может быть уменьшено во время компиляции, 
и выдает ошибку, если условие не является статически известной константой.

```scala
inline def power(x: Double, inline n: Int): Double =
  inline if (n == 0) 1.0
  else inline if (n % 2 == 1) x * power(x, n - 1)
  else power(x * x, n / 2)
```

```scala
power(2, 2) // Ok
val unknownNumber = 2
power(2, unknownNumber) // error  
-- Error: ----------------------------------------------------------------------
|power(2, unknownNumber)
|^^^^^^^^^^^^^^^^^^^^^^^
|Cannot reduce `inline if` because its condition is not a constant value: unknownNumber.==(0)
| This location contains code that was inlined from rs$line$1:2
```

В прозрачном встроенном объекте `inline if` принудительно встраивает
любое встроенное определение в его условие во время проверки типа.

### Переопределение встроенного метода

Чтобы обеспечить правильное поведение при объединении статической функции `inline def` 
с динамической функцией интерфейсов и переопределении, необходимо наложить некоторые ограничения.

##### Эффективно final

Во-первых, все встроенные методы _фактически являются final_. 
Это гарантирует, что разрешение перегрузки во время компиляции будет вести себя так же, как во время выполнения.

##### Сохранение подписи

Во-вторых, переопределения должны иметь _точно такую же сигнатуру_, 
как и переопределенный метод, включая встроенные параметры. 
Это гарантирует, что семантика вызова одинакова для обоих методов.

##### Сохраненные встроенные методы

Можно реализовать или переопределить обычный метод с помощью встроенного метода.

Рассмотрим следующий пример:

```scala
trait Logger:
  def log(x: Any): Unit

class PrintLogger extends Logger:
  inline def log(x: Any): Unit = println(x)
```

Однако вызов метода `log` напрямую у `PrintLogger` приведет к встроенному коду, а его вызов на `Logger` — нет. 
Чтобы также допустить последнее, код `log` должен существовать во время выполнения. 
Это называется сохраненным встроенным методом.

Например:

```scala
val pl: PrintLogger = new PrintLogger
val l: Logger = pl
```

```scala
pl.log("msg")
// msg
l.log("msg")
// msg
```

Встроенные вызовы и динамически отправленные вызовы дают одинаковые результаты.


Любой несохраненный `inline` `def` или `val` код всегда можно полностью инлайнить во всех местах вызовов. 
Следовательно, эти методы не понадобятся во время выполнения и могут быть удалены из байт-кода. 
Однако сохраненные встроенные методы должны быть совместимы со случаем, когда они не являются встроенными. 
В частности, сохраненные встроенные методы не могут принимать никаких встроенных параметров. 
Кроме того, `inline if`(как в примере `power`) не будет работать, 
так как `if` не может быть свёрнут в константу в сохраненном случае. 
Другие примеры включают конструкции метапрограммирования, которые имеют смысл только при встраивании.

##### Абстрактные встроенные методы

Также можно создавать абстрактные встроенные определения.

```scala
trait InlineLogger:
  inline def log(inline x: Any): Unit

class PrintLogger extends InlineLogger:
  inline def log(inline x: Any): Unit = println(x)
```

Это заставляет реализацию `log` быть встроенным методом, а также позволяет использовать `inline` параметры. 

Парадоксально, но `log` на интерфейсе `InlineLogger` нельзя вызвать напрямую. 
Реализация метода неизвестна статически, и поэтому мы не знаем, что встраивать. 
Таким образом, вызов абстрактного встроенного метода приводит к ошибке. 

Пример:

```scala
val pl: PrintLogger = new PrintLogger
pl.log("msg")
val il: InlineLogger = pl
il.log("msg")
|^^^^^^^^^^^^^
|Deferred inline method log in trait InlineLogger cannot be invoked
```

Полезность абстрактных встроенных методов становится очевидной при использовании в другом встроенном методе:

```scala
inline def logged(logger: InlineLogger, x: Any) =
  logger.log(x)
```

Предположим, вызов для `logged` конкретного экземпляра `PrintLogger`:

```scala
logged(new PrintLogger, "🥧")
// inlined as
val logger: PrintLogger = new PrintLogger
logger.log(x)
```

После встраивания вызов `log` девиртуализируется и становится известно, что он находится на `PrintLogger`. 
Поэтому и код `log` может быть встроен.

#### Резюме встроенных методов

- Все `inline` методы являются `final`.
- Абстрактные `inline` методы могут быть реализованы только `inline` методами.
- Если `inline` метод переопределяет/реализует обычный метод, он должен быть сохранен, 
а сохраненные методы не могут иметь встроенных параметров.
- Абстрактные `inline` методы нельзя вызывать напрямую (за исключением встроенного кода).


### Отношение к `@inline`

Scala 2 также определяет `@inline` аннотацию, 
которая используется в качестве подсказки для встроенного кода. 
Модификатор `inline` является более мощным вариантом, чем аннотация:

- расширение гарантирует лучшую эффективность,
- расширение происходит во внешнем интерфейсе, а не в бэкэнде, и
- расширение также применяется к рекурсивным методам.

#### Определение константного выражения

Правые части встроенных значений и аргументов для встроенных параметров должны быть константными выражениями в смысле, 
определенном [SLS §6.24](https://www.scala-lang.org/files/archive/spec/2.13/06-expressions.html#constant-expressions), 
включая специфичные для платформы расширения, такие как свертывание констант чисто числовых вычислений.

Встроенное значение должно иметь литеральный тип, например `1` или `true`.

```scala
inline val four = 4
// equivalent to
inline val four: 4 = 4
```

Также возможно иметь встроенные значения типов, которые не имеют синтаксиса, например `Short(4)`.

```scala
trait InlineConstants:
  inline val myShort: Short

object Constants extends InlineConstants:
  inline val myShort/*: Short(4)*/ = 4
```

### Прозрачные встроенные методы

Прозрачные встроенные строки (`transparent inline`) — это простое, но мощное расширение `inline` методов, 
открывающее множество вариантов использования метапрограммирования. 
Вызовы прозрачности позволяют встроенному фрагменту кода уточнять тип возвращаемого значения 
на основе точного типа встроенного выражения. 
Говоря языком Scala 2, прозрачность отражает суть "макросов белого ящика".

```scala
transparent inline def default(inline name: String): Any =
  inline if name == "Int" then 0
  else inline if name == "String" then ""
  else ???
```

```scala
val n0: Int = default("Int")
// n0: Int = 0
val s0: String = default("String")
// s0: String = ""
```

Обратите внимание, что даже если возвращаемый тип метода `default` — `Any`, 
первый вызов печатается как `Int`, а второй — как `String`. 
Тип возвращаемого значения представляет собой верхнюю границу типа внутри встроенного термина. 
Также можно было бы быть более точным и написать:

```scala
transparent inline def default(inline name: String): 0 | "" = ...
```

Хотя в этом примере кажется, что возвращаемый тип не нужен, он важен, когда встроенный метод является рекурсивным. 
Тип должен быть достаточно точным для рекурсии.

Ещё пример:

```scala
class A
class B extends A:
  def m = true

transparent inline def choose(b: Boolean): A =
  if b then new A else new B

val obj1 = choose(true)  // static type is A
val obj2 = choose(false) // static type is B

// obj1.m // compile-time error: `m` is not defined on `A`
obj2.m    // OK
```

Здесь встроенный метод `choose` возвращает экземпляр любого из двух типов `A` или `B`.
Если бы `choose` не был объявлен как `transparent`, результат его раскрытия всегда был бы типа `A`,
даже если вычисляемое значение могло бы иметь подтип `B`.
Встроенный метод является "черным ящиком" в том смысле, что детали его реализации не просачиваются.
Но если указан модификатор `transparent`, расширение является типом расширенного тела.
Если аргумент `b` равен `true`, то этот тип равен `A`, иначе — `B`.
Следовательно, вызов `m` на `obj2` пройдет проверку типов,
поскольку `obj2` имеет тот же тип, что и расширение `choose(false)`, т.е. `B`.
Прозрачные встроенные методы являются "белыми ящиками" в том смысле,
что тип приложения такого метода может быть более специализированным,
чем его объявленный возвращаемый тип, в зависимости от того, как расширяется метод.

В следующем примере мы видим, как тип возвращаемого значения `zero` специализирован для одноэлементного типа `0`,
что позволяет приписать дополнению правильный тип `1`.

```scala
transparent inline def zero: Int = 0

val one: 1 = zero + 1
```

> **Прозрачные элементы влияют на двоичную совместимость**
> Важно отметить, что изменение тела метода `transparent inline def` изменит способ его вызова. 
> Это означает, что тело играет важную роль в совместимости двоичного кода и исходного кода этого интерфейса.

### Прозрачный и непрозрачный inline

Как уже обсуждалось, прозрачные встроенные методы могут влиять на проверку типов в месте вызова. 
Технически это означает, что прозрачные встроенные методы должны быть расширены во время проверки типов программы. 
Другие встроенные методы встраиваются позже, когда программа полностью типизирована.

Например, следующие две функции будут типизированы одинаково, но будут встроены в разное время.

```scala
inline def f1: T = ...
transparent inline def f2: T = (...): T
```

Примечательным отличием является поведение `transparent inline given`. 
Если при встраивании такого определения сообщается об ошибке, 
это будет рассматриваться как неявное несоответствие поиска, и поиск будет продолжен. 
A `transparent inline given` может добавить описание типа в свой RHS (как в `f2` предыдущем примере), 
чтобы избежать точного типа, но сохранить поведение поиска. 
С другой стороны, `inline given` принимается как неявное значение, а затем встраивается после ввода. 
Любая ошибка будет выдаваться как обычно.

### Встроенные match

`match` выражение в теле определения метода `inline` может иметь префикс модификатора `inline`.
Как и встроенные `if`, встроенные `match` гарантируют, 
что сопоставление с образцом может быть статически сокращено во время компиляции и сохраняется только одна ветвь.
Если статической информации достаточно для однозначного выбора ветви,
выражение сокращается до этой ветви и берется тип результата.
Если нет, возникает ошибка времени компиляции, которая сообщает, что совпадение не может быть уменьшено.

В приведенном ниже примере определяется встроенный метод с одним встроенным выражением соответствия,
которое выбирает case на основе его статического типа:

```scala
transparent inline def g(x: Any): Any =
  inline x match
    case x: String => (x, x) // Tuple2[String, String](x, x)
    case x: Double => x

g(1.0d)   // Has type 1.0d which is a subtype of Double
// res5: Double = 1.0 
g("test") // Has type (String, String)
// res6: Tuple2[String, String] = ("test", "test")
```

`x` проверяется статически, и встроенное совпадение сокращается,
возвращая соответствующее значение (со специализированным типом, потому что `g` объявлен `transparent`).

Встроенные `match` предоставляют способ сопоставления статического типа некоторого выражения. 
Поскольку сопоставляется статический тип выражения, следующий код не будет компилироваться.

```scala
val x: Any = "test"
g(x)
// error:
// cannot reduce inline match with
//  scrutinee:  this.x : (App0.this.x : Any)
//  patterns :  case x @ _:String
//              case x @ _:Double
//   inline x match
//          ^
```

Значение `x` не помечено как `inline` и, как следствие, 
во время компиляции недостаточно информации о проверке, чтобы решить, какую ветвь выбрать.

В примерах выше выполняется простой тест типа над объектом проверки.
Тип может иметь более богатую структуру, как простой ADT ниже.
`toInt` соответствует структуре числа в [Чёрч-кодировке](https://en.wikipedia.org/wiki/Church_encoding)
и вычисляет соответствующее целое число.

```scala
trait Nat
case object Zero extends Nat
case class Succ[N <: Nat](n: N) extends Nat

transparent inline def toInt(n: Nat): Int =
  inline n match
    case Zero     => 0
    case Succ(n1) => toInt(n1) + 1

inline val natTwo = toInt(Succ(Succ(Zero)))
val intTwo: 2 = natTwo
```

Предполагается, что `natTwo` имеет одноэлементный тип `2`.


### scala.compiletime

[Пакет scala.compiletime](https://scalabook.gitflic.space/docs/scala/metaprogramming/compile-time-ops) 
предоставляет полезные абстракции метапрограммирования, 
которые можно использовать в `inline` методах для обеспечения пользовательской семантики.


### Макросы

Встраивание также является основным механизмом, используемым для написания макросов. 
[Макросы](https://scalabook.gitflic.space/docs/scala/metaprogramming/macros) позволяют управлять генерацией и анализом кода после встроенного вызова.

```scala
inline def power(x: Double, inline n: Int) =
  ${ powerCode('x, 'n)  }

def powerCode(x: Expr[Double], n: Expr[Int])(using Quotes): Expr[Double] = ...
```


### Детали

Дополнительные сведения о семантике `inline` [см. в документе](https://dl.acm.org/doi/10.1145/3426426.3428486)


---

**Ссылки:**

- [Scala 3 Macros](https://docs.scala-lang.org/scala3/guides/macros/inline.html)
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/metaprogramming/inline.html)
