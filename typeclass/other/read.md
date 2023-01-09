# Read

`Read` — это класс типов, противоположный [`Show`](show). 
Функция `read` принимает строку и возвращает тип, который является членом `Read`.

## Описание

```scala
trait Read[F]:
  def read(s: String): F
```


---

## Ссылки

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Read.html)
