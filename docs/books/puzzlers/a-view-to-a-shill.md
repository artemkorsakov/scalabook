# A View to a Shill

```scala mdoc:silent
val ints = Map(1 -> List(1, 2, 3, 4, 5))
val bits = ints.map { case (k, v) => (k, v.iterator) }
val nits = ints.view.mapValues(_.iterator)

s"${bits(1).next}${bits(1).next}"
s"${nits(1).next}${nits(1).next}"
```
```scala mdoc
s"${bits(1).next}${bits(1).next}"
s"${nits(1).next}${nits(1).next}"
```

Как объясняется [в этом тикете](https://github.com/scala/bug/issues/4776) и в Скаладоке, 
`mapValues` возвращает представление карты, которое сопоставляет каждый ключ этой карты с `f(this(key))`. 
Полученная карта оборачивает исходную карту без копирования каких-либо элементов. 
Каждое извлечение из обернутой карты приводит к новому вычислению функции отображения 
и, в данном случае, к новому итератору. 
Предписанное использование, когда требуется строгая коллекция: `(myMap mapValues (_.toIterator)).view.force`


---

**Ссылки:**

- [Scala Puzzlers](https://scalapuzzlers.com/index.html#pzzlr-037)
