# Команда

#### Назначение

Инкапсулировать запрос как объект, тем самым позволяя параметризовать клиентов с различными запросами, 
ставить в очередь или регистрировать запросы и поддерживать операции, которые можно отменить.

#### Диаграмма

![Command](https://upload.wikimedia.org/wikipedia/ru/0/0c/Command.gif)

#### Пример

Класс `Button` ожидает функцию обратного вызова, которую он будет выполнять при вызове метода `click`.

```scala
class Button(val click: () => Unit)
val button = new Button(() => println("click!"))
button.click()
// click!
```


---

**Ссылки:**

- [Scala & Design Patterns by Frederik Skeel Løkke](https://www.scala-lang.org/old/sites/default/files/FrederikThesis.pdf)
- [Wikipedia](https://en.wikipedia.org/wiki/Command_pattern)
