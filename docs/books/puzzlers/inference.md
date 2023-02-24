# Inference Hindrance

```scala
case object Completed
given Conversion[Any, Completed.type] = _ => Completed
def log[T](t: T): T =
  println(s"Log: $t")
  t
def duplicate1(s: String): Completed.type =
  val res = log(s + s)
  res
def duplicate2(s: String): Completed.type =
  log(s + s)
duplicate1("Hello")
// Log: HelloHello
// res0: Completed = Completed
duplicate2("world")
// Log: Completed
// res1: Completed = Completed
```

В методе `duplicate1` тип значения `res` не указан и подразумевается как `String`. 
Этот тип не соответствует ожидаемому возвращаемому типу метода `Completed.type`, 
поэтому компилятор ищет применимое неявное преобразование (SLS §7.3). 
Он находит `given Conversion[Any, Completed.type]` 
и превращает последний оператор метода `duplicate1` в `(_ => Completed)(res)`. 
Поэтому печатается `Log: HelloHello`. 

В случае метода `duplicate2` компилятору сначала необходимо определить тип последнего оператора `log(s + s)`. 
Поскольку возвращаемый тип журнала метода определяется его параметром типа, 
то это означает, что компилятору необходимо выяснить значение параметра типа `T` 
для этого конкретного вызова метода `log`. 
Чтобы определить значение параметра типа, который не указан явно, 
компилятор выполняет локальный вывод типа (SLS §6.26.4). 
Эта процедура учитывает, среди прочего, ожидаемый тип оператора. 
В этом случае ожидаемый тип — это возвращаемый тип метода `duplicate2`, т.е. `Completed.type`. 
Определив, что значение параметра типа `T` для этого вызова метода `log` должно быть `Completed.type`, 
компилятор затем должен выяснить, как поступить с аргументом `s + s`, переданным в `log`. 
Это `String`, но, согласно сигнатуре типа `log`, он также должен быть `Completed.type`. 
Компилятор снова ищет применимое неявное преобразование 
и еще раз находит представление `given Conversion[Any, Completed.type]`. 
Это представление вставляется вокруг аргумента, передаваемого в метод `log`, 
поэтому последний оператор метода `duplicate2` становится `log[Completed.type]((_ => Completed)(s + s))`. 
В результате вызов `log` из метода `duplicate2` выводит `Log: Completed`, 
строковое представление объекта `Completed`.


---

**Ссылки:**

- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-066)
