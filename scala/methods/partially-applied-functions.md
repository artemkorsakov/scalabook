# Каррирование

В методе можно указывать несколько групп параметров. При указании только части групп параметров возвращается
частично определенная функция. Пример:

```scala
def sum(a: Int)(b: Int): Int =
  a + b

def add2(b: Int): Int = sum(2)(b)

sum(42)(42)
// res0: Int = 84
add2(42)
// res1: Int = 44
```


---

**Ссылки:**
- [Scala3 book, Method Features](https://docs.scala-lang.org/scala3/book/methods-most.html)
