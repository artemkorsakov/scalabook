# match expressions

Сопоставление с образцом (pattern matching) является основой функциональных языков программирования.
Scala включает в себя pattern matching, обладающий множеством возможностей.

В самом простом случае можно использовать выражение `match`, подобное оператору Java `switch`,
сопоставляя на основе целочисленного значения.
Как и предыдущие структуры, pattern matching - это действительно выражение, поскольку оно вычисляет результат:

```dotty
import scala.annotation.switch
val i = 6
// i: Int = 6
val day = (i: @switch) match
case 0 => "Sunday"
case 1 => "Monday"
case 2 => "Tuesday"
case 3 => "Wednesday"
case 4 => "Thursday"
case 5 => "Friday"
case 6 => "Saturday"
case _ => "invalid day"
// day: String = "Saturday"
```

В примере выше переменная `i` сопоставляется с числом и если равна от 0 до 6, то в `day` возвращается день недели.
Иной случай обозначается символом `_` и если `i` не равен от 0 до 6, то возвращается значение `invalid day`.

> При написании простых выражений соответствия, подобных этому, рекомендуется использовать аннотацию `@switch` для переменной `i`.
> Эта аннотация содержит предупреждение во время компиляции, если _switch_ не может быть скомпилирован в _tableswitch_
> или _lookupswitch_, которые лучше подходят с точки зрения производительности.

#### Значение по умолчанию

Когда нужно получить доступ к универсальному значению по умолчанию в pattern matching,
достаточно указать имя переменной в левой части оператора `case`,
а затем использовать это имя в правой части оператора:

```dotty
i match
  case 0 => println("1")
  case 1 => println("2")
  case what => println(s"Получено значение: $what")
// Получено значение: 6  
```

Переменной можно дать любое допустимое имя. Можно также использовать `_` в качестве имени, чтобы игнорировать значение.

#### Обработка нескольких возможных значений в одной строке

В этом примере показано, как использовать несколько возможных совпадений с образцом в каждом операторе `case`:

```dotty
val evenOrOdd = i match
  case 1 | 3 | 5 | 7 | 9 => println("odd")
  case 2 | 4 | 6 | 8 | 10 => println("even")
  case _ => println("some other number")
// even  
```

#### Использование if в pattern matching

В pattern matching можно использовать условия:

```dotty
i match
  case 1 => println("one, a lonely number")
  case x if x == 2 || x == 3 => println("two’s company, three’s a crowd")
  case x if x > 3 => println("4+, that’s a party")
  case _ => println("i’m guessing your number is zero or less")
// 4+, that’s a party  
```

Ещё пример:

```dotty
i match
  case a if 0 to 9 contains a => println(s"0-9 range: $a")
  case b if 10 to 19 contains b => println(s"10-19 range: $b")
  case c if 20 to 29 contains c => println(s"20-29 range: $c")
  case _ => println("Hmmm...")
// 0-9 range: 6  
```

#### case classes и выражение match

Также можно извлекать поля из `case class`-ов — и классов, которые имеют правильно написанные методы `apply`/`unapply` —
и использовать их в pattern matching.
Вот пример использования простого `case class Person`

```dotty
case class Person(name: String)
def speak(p: Person) = p match
case Person(name) if name == "Fred" => println(s"$name says, Yubba dubba doo")
case Person(name) if name == "Bam Bam" => println(s"$name says, Bam bam!")
case _ => println("Watch the Flintstones!")
speak(Person("Fred"))
// Fred says, Yubba dubba doo
speak(Person("Bam Bam"))
// Bam Bam says, Bam bam!
speak(Person("Wilma"))
// Watch the Flintstones!
```

#### Использование выражения match в теле метода

Поскольку выражения match возвращают значение, их можно использовать в теле метода.
Этот метод принимает значение `Matchable` в качестве входного параметра
и возвращает логическое значение на основе результата выражения соответствия:

```dotty
def isTruthy(a: Matchable) = a match
  case 0 | "" | false => false
  case _              => true
```

Входной параметр `a` определяется как тип `Matchable`, который является родителем всех типов Scala.
Для `Matchable` может выполняться сопоставление с образцом.
Метод реализуется путем сопоставления входных данных, обеспечивая два случая:
первый проверяет, является ли заданное значение целым числом `0`, пустой строкой или `false`,
и в этом случае возвращает `false`.
Для иных случаев возвращается значение `true`.

Эти примеры показывают, как работает метод:

```dotty
isTruthy(0)
// res6: Boolean = false
isTruthy(false)
// res7: Boolean = false
isTruthy("")
// res8: Boolean = false
isTruthy(1)
// res9: Boolean = true
isTruthy(" ")
// res10: Boolean = true
isTruthy(2F)
// res11: Boolean = true
```

Использование pattern matching в качестве тела метода очень распространено.

#### Использование различных шаблонов в pattern matching

Для выражения `match` можно использовать множество различных шаблонов. Например:
- Сравнение с константой (`case 3 =>`)
- Сравнение с последовательностями (`case List(els : _*) =>`)
- Сравнение с кортежами (`case (x, y) =>`)
- Сравнение с конструктором класса (`case Person(first, last) =>`)
- Сравнение по типу (`case p: Person =>`)

Все эти виды шаблонов показаны в следующем примере:

```dotty
def pattern(x: Matchable): String = x match

  // Сравнение с константой
  case 0 => "ноль"
  case true => "true"
  case "hello" => "строка 'hello'"
  case Nil => "пустой List"

  // Сравнение с последовательностями
  case List(0, _, _) => "список из 3 элементов с 0 в качестве первого элемента"
  case List(1, _*) => "Непустой список, начинающийся с 1, и имеющий любой размер > 0"
  case Vector(1, _*) => "Vector, начинающийся с 1, и имеющий любой размер > 0"

  // Сравнение с кортежами
  case (a, b) => s"получено $a и $b"
  case (a, b, c) => s"получено $a, $b и $c"

  // Сравнение с конструктором класса
  case Person(first, "Alexander") => s"Alexander, first name = $first"
  case Dog("Zeus") => "Собака с именем Zeus"

  // Сравнение по типу
  case s: String => s"получена строка: $s"
  case i: Int => s"получено число: $i"
  case f: Float => s"получено число с плавающей точкой: $f"
  case a: Array[Int] => s"массив чисел: ${a.mkString(",")}"
  case as: Array[String] => s"массив строк: ${as.mkString(",")}"
  case d: Dog => s"Экземпляр класса Dog: ${d.name}"
  case list: List[?] => s"получен List: $list"
  case m: Map[?, ?] => m.toString

  // Сравнение по умолчанию
  case _ => "Unknown"
```

#### Дополнительные возможности выражений match

`match` выражения могут быть объединены в цепочку:

```dotty
def chain(xs: List[Int]) =
  xs match
    case Nil => "empty"
    case _   => "nonempty"
  match
    case "empty"    => 0
    case "nonempty" => 1

chain(List.empty[Int])
// res12: Int = 0
chain(List(1, 2, 3))
// res13: Int = 1
```


---

**Ссылки:**

- [8 Scala Pattern Matching Tricks](https://blog.rockthejvm.com/8-pm-tricks)
- [Custom Pattern Matching in Scala](https://www.youtube.com/watch?v=js2IgkNz1H0&list=PLmtsMNDRU0Bzj7INIrLugi3a_WClwQuiS)
- [Pattern Matching Tricks in Scala - Rock the JVM](https://www.youtube.com/watch?v=qe85507fSc8&list=PLmtsMNDRU0Bzj7INIrLugi3a_WClwQuiS&index=2)
- [Scala3 book, taste Control Structures](https://docs.scala-lang.org/scala3/book/taste-control-structures.html)
- [Scala3 book, Control Structures](https://docs.scala-lang.org/scala3/book/control-structures.html)
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/changed-features/match-syntax.html)
- [SKB – Scala pattern matching](https://leobenkel.com/2020/09/skb-scala-pattern-matching/)
- [SKB – Scala pattern matching @](https://leobenkel.com/2020/11/skb-scala-pattern-matching-at/)
- [SKB – Scala pattern matching for case class](https://leobenkel.com/2020/11/skb-scala-pattern-matching-for-case-class/)
- [SKB – Scala pattern matching OR](https://leobenkel.com/2020/11/skb-scala-pattern-matching-or/)
- [Top 10 pattern matching tips in Scala](https://scalajobs.com/blog/top-10-pattern-matching-tips-in-scala)
