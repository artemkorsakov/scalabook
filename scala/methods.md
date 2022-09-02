# Методы

Scala `classes`, `case classes`, `case objects`, `traits`, `enums`, и `objects` могут содержать методы.
Кроме того, они могут быть определены вне любой из перечисленных конструкций.
Методы являются определениями "верхнего уровня", поскольку не вложены в другое определение. 
Проще говоря, методы теперь могут быть определены где угодно.

В Scala методы обладают множеством особенностей, в том числе:
- [Несколько групп параметров (partially-applied functions)](methods/partially-applied-functions)
- [Методы с неопределенным количеством параметров (vararg parameters)](methods/vararg-parameters)
- [Параметры по имени (by-name parameters)](methods/by-name-parameter)
- [Функция в качестве параметра](functions)
- [Generic параметры](methods/generic-parameter)
- [Значения параметров по умолчанию](methods/default-parameters)
- [Контекстные параметры](abstractions/ca-using)
- [inline методы](metaprogramming/inline)
- и многое другое

---

**References:**
- [Scala3 book](https://docs.scala-lang.org/scala3/book/methods-intro.html)
