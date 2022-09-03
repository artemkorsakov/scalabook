# I Can Has Padding?

```scala
import scala.StringBuilder

extension (sb: StringBuilder)
  def pad2(width: Int) =
    1 to width - sb.length foreach { sb append '*' }
    sb

// greeting.length == 14
val greeting = new StringBuilder("Hello, kitteh!")
// farewell.length == 9
val farewell = new StringBuilder("U go now.")  // I hate long bye-bye.
```

```scala
println(greeting pad2 20)
// Hello, kitteh!*
```

```scala
println(farewell pad2 20)
// java.lang.StringIndexOutOfBoundsException: index 10,length 10
```

Вспомните, что в Scala есть `StringBuilder`, который скрывает `java.lang.StringBuilder`, и у него есть метод `apply`. 
`foreach` принимает функцию с параметром `Int`. 
`StringBuilder.append` возвращает `StringBuilder`, 
а `StringBuilder` расширяет `Function1`, 
поэтому компилятор может просто использовать возвращенное значение в качестве аргумента для вызова `foreach`. 
Каждая итерация вызывает `StringBuilder.apply`, который является псевдонимом для `StringBuilder.charAt`. 
Код, сгенерированный компилятором, эквивалентен следующему:

```scala
extension (sb: StringBuilder)
  def pad2(width: Int) =
    // the StringBuilder returned by append is a function!
    val sbAsFunction: Function1[Int, Char] = sb.append('*') // the same sb that was 
                                                            // passed to Padder
    (1 to (width - sb.length)).foreach(sbAsFunction)
    sb
```

Если развернуть цикл, причина исключения становится понятной. 
В случае более короткой строки `StringBuilder` содержит только десять символов (`U go now.*` после вызова `append()`). 
Таким образом, вызов `apply` - `charAt` - терпит неудачу на десятой итерации:

```scala
def pad2(width: Int) =
  val sbAsFunction: Function1[Int, Char] = sb.append('*')
  sbAsFunction.apply(1)
  ...
  sbAsFunction.apply(9)
  sbAsFunction.apply(10) // fails here
  sbAsFunction.apply(11)
  sb
```

Можно получить ожидаемое поведение, явно указав литерал функции, который будет использоваться:

```scala
import scala.StringBuilder

extension (sb: StringBuilder)
  def pad2(width: Int) =
    1 to (width - sb.length) foreach (_ => sb append '*')
    sb

// greeting.length == 14
val greeting = new StringBuilder("Hello, kitteh!")

// farewell.length == 9
val farewell = new StringBuilder("U go now.")  // I hate long bye-bye.
```

```scala
println(greeting pad2 20)
// Hello, kitteh!******
println(farewell pad2 20)
// U go now.***********
```


---

**References:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-027)
