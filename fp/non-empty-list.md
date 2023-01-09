# Непустой связанный список

`NonEmptyList` - это односвязный список, который гарантированно непустой.

```scala
final case class NonEmptyList[+A](head: A, tail: List[A])
```

Поскольку в списке есть хотя бы один элемент, всегда работает `head`.

Может использоваться, например, для коллекционирования сообщений об ошибках в `Either`.


---

## Ссылки

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Validation.html#NonEmptyList)
- [Scalaz API](https://javadoc.io/doc/org.scalaz/scalaz-core_3/7.3.6/scalaz/NonEmptyList.html)
