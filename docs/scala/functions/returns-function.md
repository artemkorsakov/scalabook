# Возврат функции в методе

Благодаря согласованности Scala написание метода, возвращающего функцию, 
похоже на то, что было описано в предыдущих разделах. 

Еще раз начнем с постановки проблемы:

_Необходимо создать метод `greet`, возвращающий функцию. 
Эта функция должна принимать строковый параметр и печатать его с помощью `println`._

Начнем с простого шага: `greet` не принимает никаких входных параметров, 
а просто создает функцию и возвращает её.

Учитывая это утверждение, можно начать создавать `greet`. Известно, что это будет метод:

```scala
def greet()
```

Также известно, что этот метод должен возвращать функцию, которая:

- принимает параметр `String` и 
- печатает эту строку с помощью `println`. 

Следовательно, эта функция имеет тип `String => Unit`:

```scala
def greet(): String => Unit = ???
             --------------
```

Теперь нужно создать тело метода. 
Возвращаемая функция соответствует следующему описанию:

```scala
(name: String) => println(s"Hello, $name")
```

Добавляем эту функцию в тело метода:

```scala
def greet(): String => Unit = 
  (name: String) => println(s"Hello, $name")
```

Поскольку метод возвращает функцию, мы получаем ее, вызывая `greet()`. 

```scala
val greetFunction = greet()
```

Теперь можно вызвать `greetFunction`:

```scala
greetFunction("Joe")
// Hello, Joe
```

### Доработка метода

Метод `greet()` был бы более полезным, если бы была возможность задавать приветствие. 
Например, передать его в качестве параметра методу `greet()` и использовать внутри `println`:

```scala
def greet(theGreeting: String): String => Unit = 
  (name: String) => println(s"$theGreeting, $name")
```

Теперь, при вызове этого метода, процесс становится более гибким, потому что приветствие можно изменить. 
Вот как это выглядит, когда создается функция из этого метода:

```scala
val sayHello = greet("Hello")
```

Выходные данные подписи типа показывают, что `sayHello` — это функция, 
которая принимает входной параметр `String` и возвращает `Unit`. 
Так что теперь, при передаче `sayHello` строки, печатается приветствие:

```scala
sayHello("Joe")
// Hello, Joe
```

Приветствие можно менять для создания новых функций:

```scala
val sayCiao = greet("Ciao")
sayCiao("Isabella")
// Ciao, Isabella

val sayHola = greet("Hola")
sayHola("Carlos")
// Hola, Carlos
```

### Более реалистичный пример

Этот метод может быть еще более полезным, когда возвращает одну из многих возможных функций, 
например, фабрику пользовательских функций.

Например, представим, что необходимо написать метод, который возвращает функции, 
приветствующие людей на разных языках. 
Ограничим это функциями, которые приветствуют на английском или французском языках, 
в зависимости от параметра, переданного в метод.

Созданный метод должен:

- принимать "желаемый язык" в качестве входных данных и 
- возвращать функцию в качестве результата. 

Кроме того, поскольку эта функция печатает заданную строку, известно, что она имеет тип `String => Unit`. 
С помощью этой информации сигнатура метода должна выглядеть так:

```scala
def createGreetingFunction(desiredLanguage: String): String => Unit = ???
```

Далее, поскольку возвращаемые функции, берут строку и печатают ее, 
можно прикинуть две анонимные функции для английского и французского языков:

```scala
(name: String) => println(s"Hello, $name")
(name: String) => println(s"Bonjour, $name")
```

Для большей читабельности дадим этим анонимным функциям имена и назначим двум переменным:

```scala
val englishGreeting = (name: String) => println(s"Hello, $name")
val frenchGreeting = (name: String) => println(s"Bonjour, $name")
```

Теперь все, что осталось, это вернуть `englishGreeting`, если `desiredLanguage` — английский, 
и вернуть `frenchGreeting`, если `desiredLanguage` — французский. 
Один из способов сделать это - pattern matching:

```scala
def createGreetingFunction(desiredLanguage: String): String => Unit =
  val englishGreeting = (name: String) => println(s"Hello, $name")
  val frenchGreeting = (name: String) => println(s"Bonjour, $name")
  desiredLanguage match
    case "english" => englishGreeting
    case "french" => frenchGreeting
```

Обратите внимание, что возврат значения функции из метода ничем не отличается 
от возврата строкового или целочисленного значения.

Вот как `createGreetingFunction` создает функцию приветствия на французском языке:

```scala
val greetInFrench = createGreetingFunction("french")
greetInFrench("Jonathan")
// Bonjour, Jonathan
```

И вот как - на английском:

```scala
val greetInEnglish = createGreetingFunction("english")
greetInEnglish("Joe")
// Hello, Joe
```


---

**Ссылки:**

- [Scala3 book](https://docs.scala-lang.org/scala3/book/fun-write-method-returns-function.html)
