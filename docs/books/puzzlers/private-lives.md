# Private Lives

```scala
object Lives:
  class Private:
    def foo1: Any = new Private.C1
    def foo2: Any = new Private.C2

  object Private:
    class C1 private {}
    private class C2 {}
// error:
// constructor C1 cannot be accessed as a member of repl.MdocSession.App.Lives.Private.C1 from class Private.
//     def foo1: Any = new Private.C1
//                         ^^^^^^^^^^
```

Сопутствующий объект `Private` определяет два класса `C1` и `C2`. 
`C2` — это приватный класс, который доступен только в объекте `Private` и его сопутствующем классе, 
тогда как `C1` — это общедоступный класс с приватным конструктором по умолчанию, 
поэтому этот класс может быть создан только в классе `C1`. 
Как следствие, метод `foo2` компилируется нормально (поскольку приватный класс `C2` виден в классе `Private`), 
тогда как реализация метода `foo1` сообщает об ошибке компилятора.
Обратите внимание, что тип результата метода `foo2` должен быть установлен на базовый тип `Private.C2`, 
иначе тип результата этого метода будет невидимым для любого вызывающего объекта 
за пределами класса/объекта `Private` 
(компилятор сообщит что закрытый класс `C2` выходит за рамки своей определяющей области). 
Для метода `foo1` в этом нет необходимости.


---

**Ссылки:**

- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-015)
