# Типы коллекций

В этом разделе продемонстрированы наиболее распространенные типы коллекций и их методы. 
В конце этого раздела для получения более подробной информации представлены дополнительные ссылки 
для более глубокого изучения коллекций.

### Три основные категории коллекций

Для коллекций Scala можно выделить три основные категории:
- Последовательности (**Sequences**/**Seq**) представляют собой последовательный набор элементов 
и могут быть индексированными (как массив) или линейными (как связанный список)
- Карты (**Maps**) содержат набор пар ключ/значение, например Java `Map`, Python dictionary или Ruby `Hash`
- Множества (**Sets**) — это неупорядоченный набор уникальных элементов

Все они являются базовыми типами и имеют подтипы для конкретных целей, 
таких как параллелизм (_concurrency_), кэширование (_caching_) и потоковая передача (_streaming_). 
В дополнение к этим трем основным категориям существуют и другие полезные типы коллекций, 
включая диапазоны (_ranges_), стеки (_stacks_) и очереди (_queues_).

#### Иерархия коллекций

В качестве краткого обзора следующие три рисунка показывают иерархию классов и трейтов в коллекциях Scala.

На первом рисунке показаны типы коллекций в пакете `scala.collection`. 
Все это высокоуровневые абстрактные классы или трейты, которые обычно имеют неизменяемые и изменяемые реализации.

![General collection hierarchy](https://docs.scala-lang.org/resources/images/tour/collections-diagram-213.svg)

На этом рисунке показаны все коллекции в пакете `scala.collection.immutable`:

![Immutable collection hierarchy](https://docs.scala-lang.org/resources/images/tour/collections-immutable-diagram-213.svg)

А на этом рисунке показаны все коллекции в пакете `scala.collection.mutable`:

![Mutable collection hierarchy](https://docs.scala-lang.org/resources/images/tour/collections-mutable-diagram-213.svg)

В следующих разделах представлены некоторые из распространенных типов.


### Общие коллекции

Основные коллекции, используемые чаще всего:

| Тип коллекции | Неизменяемая | Изменяемая | Описание                                                                                                                                                           |
|---------------|--------------|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `List`        | &#10003;     |            | Линейная неизменяемая последовательность (связный список)                                                                                                          |
| `Vector`      | &#10003;     |            | Индексированная неизменяемая последовательность                                                                                                                    |
| `LazyList`    | &#10003;     |            | Ленивый неизменяемый связанный список, элементы которого вычисляются только тогда, когда они необходимы; подходит для больших или бесконечных последовательностей. |
| `ArrayBuffer` |              | &#10003;   | Подходящий тип для изменяемой индексированной последовательности                                                                                                   |
| `ListBuffer`  |              | &#10003;   | Используется, когда вам нужен изменяемый список; обычно преобразуется в `List`                                                                                     |
| `Map`         | &#10003;     | &#10003;   | Итерируемая коллекция, состоящая из пар ключей и значений                                                                                                          |
| `Set`         | &#10003;     | &#10003;   | Итерируемая коллекция без повторяющихся элементов                                                                                                                  |

Как показано, `Map` и `Set` бывают как неизменяемыми, так и изменяемыми.

Основы каждого типа демонстрируются в следующих разделах.

> В Scala буфер, такой как `ArrayBuffer` или `ListBuffer`, представляет собой последовательность, 
> которая может увеличиваться и уменьшаться.

##### Примечание о неизменяемых коллекциях

В последующих разделах всякий раз, когда используется слово _immutable_, 
можно с уверенностью сказать, что тип предназначен для использования в стиле функционального программирования (ФП). 
С помощью таких типов коллекция не меняется, 
а при вызове функциональных методов возвращается новый результат - новая коллекция.


### Выбор последовательности

При выборе последовательности нужно руководствоваться двумя основными вопросами:
- должна ли последовательность индексироваться (как массив), обеспечивая быстрый доступ к любому элементу, 
или она должна быть реализована как линейный связанный список?
- необходима изменяемая или неизменяемая коллекция?

Рекомендуемые универсальные последовательности:

| Тип \ Категория           | Неизменяемая | Изменяемая    |
|---------------------------|--------------|---------------|
| индексируемая             | `Vector`     | `ArrayBuffer` |
| линейный связанный список | `List`       | `ListBuffer`  |

Например, если нужна неизменяемая индексированная коллекция, в общем случае следует использовать `Vector`. 
И наоборот, если нужна изменяемая индексированная коллекция, используйте `ArrayBuffer`.

> `List` и `Vector` часто используются при написании кода в функциональном стиле. 
> `ArrayBuffer` обычно используется при написании кода в императивном стиле. 
> `ListBuffer` используется, когда стили смешиваются, например, при создании списка.

Следующие несколько разделов кратко демонстрируют типы `List`, `Vector` и `ArrayBuffer`.

### Подробнее

Для дополнительной информации о коллекциях, см. следующие ресурсы:
- [Как устроены коллекции? Какую из них выбирать?](https://docs.scala-lang.org/ru/overviews/collections-2.13/introduction.html)


---

**References:**
- [Scala3 book](https://docs.scala-lang.org/scala3/book/taste-collections.html)
- [Scala3 book, Collections Types](https://docs.scala-lang.org/scala3/book/collections-classes.html)