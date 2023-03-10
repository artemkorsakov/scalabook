# Типы данных

В этом разделе представлен обзор переменных и типов данных Scala.

#### Два вида переменных

Переменная может быть неизменяемой или изменяемой:

| Тип           | Описание                                                                                                                                                                       |
|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `val`           | Создает неизменяемую переменную, подобную `final` в Java. Согласно стандартам Scala и функционального программирования желательно всегда создавать переменную с помощью `val`. |
| `var`           | Создает изменяемую переменную. Практически не используется в Scala, т.к. изменяемая переменная противоречит принципам функционального программирования.                        |

В примере показано, как создавать `val` и `var` переменные:

```scala
val a = 0
// a: Int = 0
var b = 1
// b: Int = 1
```

Значение `val` не может быть переназначено. 
Если попытаться переназначить, то будет получена ошибка компиляции:

```scala
val msg = "Hello, world"
msg = "Aloha"
// error:
// Reassignment to val msg
// msg = "Aloha"
// ^^^^^^^^^^^^^
```

И наоборот, `var` может быть переназначен:

```scala
var msg = "Hello, world"
// msg: String = "Hello, world"
msg = "Aloha"
msg
// res1: String = "Aloha"
```

#### Объявление типов переменных

Когда создается переменная, можно явно объявить ее тип или позволить определить тип компилятору:

```scala
val x: Int = 1
val x = 1
```

Вторая форма известна как вывод типа (_type inference_), и это отличный способ помочь сохранить код кратким. 
Компилятор Scala обычно может определить тип данных, как показано в выходных данных этих примеров:

```scala
val x = 1
// x: Int = 1
val s = "a string"
// s: String = "a string"
val nums = List(1, 2, 3)
// nums: List[Int] = List(1, 2, 3)
```

Всегда можно явно объявить тип переменной, но в простых примерах, подобных этим, в этом нет необходимости:

```scala
val x: Int = 1
val s: String = "a string"
val p: Person = Person("Richard")
```

В Scala все значения имеют тип, включая числовые значения и функции.

#### Иерархия типов в Scala

Приведенная ниже диаграмма иллюстрирует подмножество иерархии типов.

![иерархия типов](https://docs.scala-lang.org/resources/images/scala3-book/hierarchy.svg)

`Any` - это супертип всех типов, также называемый _the top type_. 
Он определяет универсальные методы, такие как `equals`, `hashCode` и `toString`.

У верхнего типа `Any` есть подтип `Matchable`, который используется для обозначения всех типов, 
для которых возможно выполнить pattern matching. 
Важно гарантировать вызов свойства "параметричность", что вкратце означает, 
что мы не можем сопоставлять шаблоны для значений типа `Any`, 
а только для значений, которые являются подтипом `Matchable`.
[Справочная документация](https://docs.scala-lang.org/scala3/reference/other-new-features/matchable.html) содержит более подробную информацию о Matchable.

`Matchable` имеет два важных подтипа: `AnyVal` и `AnyRef`.

`AnyVal` представляет типы значений. 
Существует несколько предопределенных типов значений, 
и они _non-nullable_: `Double`, `Float`, `Long`, `Int`, `Short`, `Byte`, `Char`, `Unit` и `Boolean`. 
`Unit` - это тип значения, который не несет никакой значимой информации. 
Существует ровно один экземпляр `Unit` - `()`.

`AnyRef` представляет ссылочные типы. 
Все типы, не являющиеся значениями, определяются как ссылочные типы. 
Каждый пользовательский тип в Scala является подтипом `AnyRef`. 
Если Scala используется в контексте среды выполнения Java, `AnyRef` соответствует `java.lang.Object`.

В языках, основанных на операторах, `void` используется для методов, которые ничего не возвращают. 
В Scala для методов, которые не имеют возвращаемого значения, такие как следующий метод, 
для той же цели используется `Unit`:

```scala
def printIt(a: Any): Unit = println(a)
```

Вот пример, демонстрирующий, что строки, целые числа, символы, логические значения и функции являются экземплярами `Any` 
и могут обрабатываться так же, как и любой другой объект:

```scala
val list: List[Any] = List(
  "a string",
  732,  
  'c',  
  true, 
  () => "an anonymous function returning a string"
)
// list: List[Any] = List(
//   "a string",
//   732,
//   'c',
//   true,
//   <function>
// )
```

Код определяет список значений типа `List[Any]`. 
Список инициализируется элементами различных типов, но каждый из них является экземпляром `scala.Any`, 
поэтому мы можем добавить их в список.

#### Типы значений в Scala

Как показано выше, числовые типы Scala расширяют `AnyVal`, и все они являются полноценными объектами. 
В этих примерах показано, как объявлять переменные этих числовых типов:

```scala
val b: Byte = 1
val i: Int = 1
val l: Long = 1
val s: Short = 1
val d: Double = 2.0
val f: Float = 3.0
```

В первых четырех примерах, если явно не указать тип, то число `1` по умолчанию будет равно `Int`, 
поэтому, если нужен один из других типов данных — `Byte`, `Long` или `Short` — необходимо явно объявить эти типы. 
Числа с десятичной дробью (например, `2.0`) по умолчанию будут иметь значение `Double`, 
поэтому, если необходим `Float`, нужно объявить `Float` явно, как показано в последнем примере.

Поскольку `Int` и `Double` являются числовыми типами по умолчанию, их можно создавать без явного объявления типа данных:

```scala
val i = 123
// i: Int = 123
val j = 1.0
// j: Double = 1.0
```

Также можно добавить символы `L`, `D`, and `F` (или их эквивалент в нижнем регистре) для того, 
чтобы задать `Long`, `Double`, или `Float` значения:

```scala
val x = 1_000L
// x: Long = 1000L
val y = 2.2D 
// y: Double = 2.2 
val z = 3.3F
// z: Float = 3.3F
```

В Scala также есть типы `String` (значение заключается в двойные кавычки или три двойных) 
и `Char` (значение заключается в одинарные кавычки):

```scala
val name = "Bill"
// name: String = "Bill"
val c = 'a'    
// c: Char = 'a'  
```

#### BigInt и BigDecimal

Для действительно больших чисел можно использовать типы `BigInt` и `BigDecimal`:

```scala
var a = BigInt(1_234_567_890_987_654_321L)
// a: BigInt = 1234567890987654321
var b = BigDecimal(123_456.789)
// b: BigDecimal = 123456.789
```

Где `Double` и `Float` являются приблизительными десятичными числами, 
а `BigDecimal` используется для точной арифметики, например, при работе с валютой.

`BigInt` и `BigDecimal` поддерживают все привычные числовые операторы:

```scala
val b = BigInt(1234567890)
// b: BigInt = 1234567890
val c = b + b             
// c: BigInt = 2469135780             
val d = b * b            
// d: BigInt = 1524157875019052100           
```

#### Строки

Строки Scala похожи на строки Java, но у них есть две замечательные дополнительные функции:
- они поддерживают интерполяцию строк
- создавать многострочные строки очень просто

##### String interpolation

Интерполяция строк обеспечивает очень удобный способ использования переменных внутри строк. 
Например, учитывая эти три переменные:

```scala
val firstName = "John"
val mi = 'C'
val lastName = "Doe"
```

их комбинацию можно получить так:

```scala
s"Name: $firstName $mi $lastName"
// res4: String = "Name: John C Doe"
```

Достаточно поставить перед строкой букву `s`, а затем - символ `$` перед именами переменных внутри строки. 

Чтобы вставить произвольные выражения в строку, они заключаются в фигурные скобки:

```scala
s"2 + 2 = ${2 + 2}"
// res6: String = "2 + 2 = 4"
val x = -1
// x: Int = -1
s"x.abs = ${x.abs}"
// res7: String = "x.abs = 1"
```

Символ `s`, помещенный перед строкой, является лишь одним из возможных интерполяторов. 
Если использовать `f` вместо `s`, можно использовать синтаксис форматирования в стиле `printf` в строке. 
Кроме того, интерполятор строк - это всего лишь специальный метод, и его можно определить самостоятельно. 
Например, некоторые библиотеки баз данных определяют очень мощный интерполятор `sql`.

Для экранирования символа `"` в интерполяции используется символ `$`:

```scala
val inventor = "Thomas Edison"
// inventor: String = "Thomas Edison"
val interpolation = s"as $inventor said: $"The three great essentials to achieve anything worth while are: Hard work, Stick-to-itiveness, and Common sense.$""
// interpolation: String = "as Thomas Edison said: \"The three great essentials to achieve anything worth while are: Hard work, Stick-to-itiveness, and Common sense.\""
println(interpolation)
// as Thomas Edison said: "The three great essentials to achieve anything worth while are: Hard work, Stick-to-itiveness, and Common sense."
```

##### Multiline strings

Многострочные строки создаются путем включения строки в три двойные кавычки:

```scala
println("""The essence of Scala:
               Fusion of functional and object-oriented
               programming in a typed setting.""")              
// The essence of Scala:
//                      Fusion of functional and object-oriented
//                      programming in a typed setting.             
```

Одним из недостатков базового подхода является то, что строки после первой имеют отступ.

Если важно исключить отступ, можно поставить символ `|` перед всеми строками после первой и вызвать метод `stripMargin` после строки:

```scala
println("""The essence of Scala:
               |Fusion of functional and object-oriented
               |programming in a typed setting.""".stripMargin)               
// The essence of Scala:
// Fusion of functional and object-oriented
// programming in a typed setting.           
```

Теперь все строки выравниваются по левому краю. 

Здесь также можно использовать переменные внутри строки, добавив `s` перед первыми `"""`.


#### Приведение типов

Типы значений могут быть приведены следующим образом:

![приведение типов](https://docs.scala-lang.org/resources/images/tour/type-casting-diagram.svg)

Например:

```scala
val x: Int = 987654321
// x: Int = 987654321
val y: Long = x
// y: Long = 987654321L
val face: Char = '☺'
// face: Char = '☺'
val number: Int = face
// number: Int = 9786
```

Приведение типов однонаправленное, следующий код не будет компилиться:

```scala
val a: Long = 987654321
val b: Float = a 
val c: Long = b
// val c: Long = b
//             ^
//             Found:    (b : Float)
//             Required: Long
```

Неявное приведение типов в некоторых случаях помечено как `deprecated` и может быть запрещено в будущих версиях:

```scala
val x: Long = 987654321
val y: Float = x
// method long2float in object Long is deprecated since 2.13.1: Implicit conversion from Long to Float is dangerous because it loses precision. Write `.toFloat` instead.
// val y: Float = x
//                ^
```

#### Nothing и null

`Nothing` является подтипом всех типов, также называемым _the bottom type_. 
Нет значения, которое имело бы тип `Nothing`. 
Он обычно сигнализирует о прекращении, таком как `thrown exception`, выходе из программы или бесконечном цикле - 
т.е. это тип выражения, который не вычисляется до определенного значения, 
или метод, который нормально не возвращается.

`Null` - это подтип всех ссылочных типов (т.е. любой подтип `AnyRef`). 
Он имеет единственное значение, определяемое ключевым словом `null`. 
В настоящее время применение `null` считается плохой практикой. 
Его следует использовать в основном для взаимодействия с другими языками JVM. 
Опция _opt-in compiler_ изменяет статус Null, делая все ссылочные типы _non-nullable_. 
Этот параметр может стать значением по умолчанию в будущей версии Scala.

В то же время `null` почти никогда не следует использовать в коде Scala. 
Альтернативы `null` обсуждаются в главе о функциональном программировании и в [документации API](https://scala-lang.org/api/3.x/scala/Option.html).

---

**Ссылки:**

- [Scala3 book, Variables and Data Types](https://docs.scala-lang.org/scala3/book/taste-vars-data-types.html)
- [Scala3 book, A First Look at Types](https://docs.scala-lang.org/scala3/book/first-look-at-types.html)