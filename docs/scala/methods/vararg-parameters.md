# Методы с неопределенным количеством параметров

Метод может иметь неопределенное количество параметров одного типа.
Они указываются с помощью синтаксиса `T*`. Пример:

```scala
def printAll(args: String*): Unit =
  args.foreach(println)

printAll("Adam")
// Adam
printAll("Adam", "Bob")
// Adam
// Bob
printAll("Adam", "Bob", "Celin")
// Adam
// Bob
// Celin
printAll("Adam", "Bob", "Celin", "David")
// Adam
// Bob
// Celin
// David
```

Ещё пример:

```scala
val arr = Array(0, 1, 2, 3)
// arr: Array[Int] = Array(0, 1, 2, 3)
val lst = List(arr*)
// lst: List[Int] = List(0, 1, 2, 3)

def printList(lst: List[Int]): Unit =
  lst match
    case List(0, 1, xs*) => println(xs)
    case List(1, _*) => println("Starts with 1")
    case _ => println("Error")

printList(lst)
// List(2, 3)
printList(lst.tail)
// Starts with 1
```


---

**Ссылки:**

- [Scala3 book, Method Features](https://docs.scala-lang.org/scala3/book/methods-most.html)
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/changed-features/vararg-splices.html)
