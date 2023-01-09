# while and try

### while loops

Цикл `while` имеет следующий синтаксис:

```scala
var x = 1
while
  x < 3
do
  println(x)
  x += 1
// 1
// 2
```

> В Scala не приветствуется использование изменяемых переменных `var`, поэтому следует избегать `while`.
> Аналогичный результат можно достигнуть используя вспомогательный метод:
> ```scala
> def loop(x: Int): Unit =
>   if x < 3 then
>     println(x)
>     loop(x + 1)  
> loop(1)
> ```


### try/catch/finally

Как и в Java, в Scala есть конструкция `try`/`catch`/`finally`, позволяющая перехватывать исключения и управлять ими.
Для обеспечения согласованности Scala использует тот же синтаксис, что и выражения `match`,
и поддерживает pattern matching для различных возможных исключений.

В следующем примере `openAndReadAFile` - это метод, который выполняет то, что следует из его названия:
он открывает файл и считывает текст в нем, присваивая результат изменяемой переменной `text`:

```scala
var text = ""
try
  text = openAndReadAFile(filename)
catch
  case fnf: FileNotFoundException => fnf.printStackTrace()
  case ioe: IOException => ioe.printStackTrace()
finally
  println("Здесь необходимо закрыть ресурсы.")
```

Предполагая, что метод `openAndReadAFile` использует Java `java.io.*` классы для чтения файла
и не перехватывает его исключения, попытка открыть и прочитать файл может привести как к `FileNotFoundException`,
так и к `IOException`, и эти два исключения перехватываются в блоке `catch` этого примера.


---

**Ссылки:**
- [Scala3 book, taste Control Structures](https://docs.scala-lang.org/scala3/book/taste-control-structures.html)
- [Scala3 book, Control Structures](https://docs.scala-lang.org/scala3/book/control-structures.html)
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/changed-features/match-syntax.html)
