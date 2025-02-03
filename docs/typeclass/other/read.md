# Read

## Формальное определение

`Read` — это класс типов, противоположный [`Show`](show.md). 
Функция `read` принимает строку и возвращает тип, который является членом `Read`.

## Определение в виде кода на Scala

```dotty
trait Read[F]:
  def read(s: String): F
```

---

**Ссылки:**

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/Read.html)
