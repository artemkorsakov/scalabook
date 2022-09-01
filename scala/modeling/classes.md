# Классы

Как и в других языках, класс в Scala — это шаблон для создания экземпляров объекта. 
Вот несколько примеров классов:

```scala
class Person(var name: String, var vocation: String)
class Book(var title: String, var author: String, var year: Int)
class Movie(var name: String, var director: String, var year: Int)
```

Эти примеры показывают, как в Scala объявляются классы.

В примере выше все параметры классов определены как поля `var`, что означает, что они изменяемы. 
Если необходимо, чтобы они были неизменяемыми, можно определить их как `val` или использовать case class.

Новый экземпляр класса создается следующим образом 
(без ключевого слова `new`, благодаря универсальным `apply` методам, см. ниже):

```scala
val p = Person("Robert Allen Zimmerman", "Harmonica Player")
```

Если есть экземпляр класса, такого как `p`, то можно получить доступ к его полям, 
которые в этом примере являются параметрами конструктора:

```scala
p.name
// res0: String = "Robert Allen Zimmerman"
p.vocation
// res1: String = "Harmonica Player"
```

Как уже упоминалось, все эти параметры были созданы как поля `var`, поэтому их можно изменять:

```scala
p.name = "Bob Dylan"
p.vocation = "Musician"
```

#### Поля и методы

Классы также могут иметь методы и дополнительные поля, не являющиеся частью конструкторов. 
Они определены в теле класса. Тело инициализируется как часть конструктора по умолчанию:

```scala
class Person(var firstName: String, var lastName: String):
  println("initialization begins")
  val fullName = s"$firstName $lastName"

  def printFullName: Unit =
    println(fullName)

  printFullName
  println("initialization ends")
```

Пример демонстрирует, как происходит инициализация класса:

```scala
val john = Person("John", "Doe")
// initialization begins
// John Doe
// initialization ends
// john: Person = repl.MdocSession$App4$Person@7058cf27
john.printFullName
// John Doe
```

Классы также могут расширять `trait`-ы и абстрактные классы, которые будут рассмотрены в специальных разделах ниже.

#### Параметры по умолчанию

Параметры конструктора класса также могут иметь значения по умолчанию:

```scala
class Socket(val timeout: Int = 5_000, val linger: Int = 5_000):
  override def toString = s"timeout: $timeout, linger: $linger"
```

Отличительной особенностью этой функции является то, что она позволяет пользователям кода создавать классы 
различными способами, как если бы у класса были альтернативные конструкторы:

```scala
Socket()                
// res6: Socket = timeout: 5000, linger: 5000                
Socket(2_500)           
// res7: Socket = timeout: 2500, linger: 5000           
Socket(10_000, 10_000)  
// res8: Socket = timeout: 10000, linger: 10000  
Socket(timeout = 10_000)
// res9: Socket = timeout: 10000, linger: 5000
Socket(linger = 10_000)
// res10: Socket = timeout: 5000, linger: 10000
```

При создании нового экземпляра класса также можно использовать именованные параметры. 
Это приветствуется и особенно полезно, когда параметры имеют одинаковый тип:

```scala
Socket(10_000, 10_001)
// res11: Socket = timeout: 10000, linger: 10001
Socket(timeout = 10_000, linger = 10_001)
// res12: Socket = timeout: 10000, linger: 10001
Socket(linger = 10_000, timeout = 10_001)
// res13: Socket = timeout: 10001, linger: 10000
```

#### Вспомогательные конструкторы

В классе можно определить несколько конструкторов. 
Например, предположим, что нужно определить три конструктора класса `Student`:
- с именем и государственным ID (1)
- с именем, государственным ID и датой подачи заявления (2)
- с именем, государственным ID и студенческим ID (3)

Пример описания класса с тремя этими конструкторами:

```scala
import java.time.*
class Student(var name: String, var govtId: String): // [1] основной конструктор
  private var _applicationDate: Option[LocalDate] = None
  private var _studentId: Int = 0

  def this(name: String, govtId: String, applicationDate: LocalDate) =   // [2] конструктор с датой подачи заявления
    this(name, govtId)
    _applicationDate = Some(applicationDate)

  def this(name: String, govtId: String, studentId: Int) =   // [3] конструктор со студенческим id
    this(name, govtId)
    _studentId = studentId
```

Эти конструкторы могут быть вызваны следующим образом:

```scala
Student("Mary", "123")
Student("Mary", "123", LocalDate.now)
Student("Mary", "123", 456)
```

Для возможности создания классов несколькими способами можно использовать как параметры по умолчанию, 
так и несколько конструкторов, как в примере выше.


### Универсальные apply методы

Scala 3 обобщает схему генерации `apply` методов на все конкретные классы. 
Т.е., как упоминалось выше, можно создавать экземпляры класса без ключевого слова `new`.
Пример:

```scala
class StringBuilder(s: String):
  def this() = this("")

StringBuilder("abc")  // устаревший вариант: new StringBuilder("abc")
StringBuilder()       // устаревший вариант: new StringBuilder()
```

Это работает, поскольку вместе с классом создается сопутствующий объект с двумя `apply` методами. 
Объект выглядит так:

```scala
object StringBuilder:
  inline def apply(s: String): StringBuilder = new StringBuilder(s)
  inline def apply(): StringBuilder = new StringBuilder()
```

Синтетический объект `StringBuilder` и его `apply` методы называются _прокси-конструкторами_. 
Прокси-конструкторы генерируются даже для классов Java и классов из Scala 2. 
Точные правила следующие:
- прокси-конструктор сопутствующего объекта `object C` создается для конкретного класса `C` при условии, 
что класс еще не имеет сопутствующего объекта, а также нет другого значения или метода с именем `C`, 
определенным или унаследованным в области, в которой он определен.
- прокси-конструкторы `apply` методов генерируются для предоставленного конкретного класса если:
  - у класса есть объект-компаньон (который мог быть сгенерирован на шаге 1) и
  - этот сопутствующий объект еще не определяет элемент с именем `apply`.
- каждый сгенерированный `apply` метод пересылается одному конструктору класса. 
Он имеет те же параметры типа и значения, что и конструктор.

Прокси-конструкторы сопутствующего объекта не могут использоваться в качестве значений сами по себе. 
Они должны быть выбраны с помощью `apply` (или применены к аргументам, и в этом случае `apply` неявно вставляется).

Прокси-конструкторы также не могут затенять обычные определения. 
То есть, если идентификатор разрешается в прокси конструктор, 
и тот же идентификатор также определен или импортирован в какой-либо другой области, сообщается о неоднозначности.


---

**References:**
- [Scala3 book, domain modeling tools](https://docs.scala-lang.org/scala3/book/domain-modeling-tools.html)
- [Scala3 book, taste modeling](https://docs.scala-lang.org/scala3/book/taste-modeling.html)
- [Scala3 book, taste objects](https://docs.scala-lang.org/scala3/book/taste-objects.html)
- [Scala 3 Reference, Universal Apply Methods](https://docs.scala-lang.org/scala3/reference/other-new-features/creator-applications.html)
