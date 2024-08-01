# Каррирование

В методе можно указывать несколько групп параметров. При указании только части групп параметров возвращается
частично определенная функция. Пример:

```dotty
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
- Category Theory for Programmers - Bartosz Milewski:
    - [Теория категорий для программистов](https://henrychern.files.wordpress.com/2023/05/mil-ct3.pdf#page=149)
    - [Category Theory for Programmers, Function Types](https://bartoszmilewski.com/2015/03/13/function-types/)
- [SKB – Scala Curry](https://leobenkel.com/2020/09/skb-scala-curry/)
