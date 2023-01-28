# A Case of Strings

```scala
def objFromJava: Object = "string"
def stringFromJava: String = null

def printLengthIfString(a: AnyRef): Unit =
  a match
    case s: String => println("String of length " + s.length)
    case _ => println("Not a string")

printLengthIfString(objFromJava)
// String of length 6
printLengthIfString(stringFromJava)
// Not a string
```

Scala наследует от Java следующее поведение:

```scala
val s: String = null  // допустимо
println(s.isInstanceOf[String]) // печатает "false", потому что, как и в Java, null.instanceof[String] == false
```

Затем это приводит к разрешению сопоставления с образцом. 
Поэтому, если сопоставляется шаблон со значением, которое может быть `null`, 
необходимо явно проверять значение `null`:

```scala
a match
  case s: String => println("String of length " + s.length)
  case null      => println("Got null!")
  case _         => println("Something else...")
```

Разрешение сопоставления шаблонов основано на типе времени выполнения, 
поэтому первый пример соответствует случаю `s: String`, даже если тип времени компиляции — `java.lang.Object`. 
Хорошая идиома Scala состоит в том, чтобы преобразовать «может быть-нуль» из Java API в параметр:

```scala
val str: Option[String] = Option(stringFromJava)
str match
  case Some(s: String) => println("String of length " + s.length)
  case None => println("stringFromJava was null")
// stringFromJava was null  
```


---

**Ссылки:**
- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-035)
