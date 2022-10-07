# Read

`Read` — это класс типов, противоположный `Show`. 
Функция `read` принимает строку и возвращает тип, который является членом `Read`.

##### Описание

```scala
trait Read[F]:
  def read(s: String): F
```


---

**References:**
