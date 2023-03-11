# Шаблон Строитель в Scala 3

Как известно, шаблон [Строитель (Builder)][Wiki] отделяет конструирование сложного объекта от его представления,
что особенно хорошо, когда нужно провести валидацию параметров перед получением итогового экземпляра.
Особенно удобно комбинировать шаблон Строитель с [уточняющими типами][habr].

Рассмотрим использование Строителя в Scala на версии `"3.2.2"`.

Представим, что у нас есть конфиг:

```scala
final case class ConnectionConfig (
    host: String,
    port: Int,
    timeout: Int,
    connectionRetry: Int,
    user: String,
    password: String
)
```

И мы хотим предоставить пользователю возможность создавать конфиг различными способами, но 
при этом валидировать значения перед формированием итогового конфига.
Например, по следующим правилам:

- `host` - строка от 4 символов
- `port` - число от 1024 до 65535
- `timeout` - неотрицательное число
- `connectionRetry` - от 1 до 10
- `user` - строка, содержащая только буквы и цифры
- `password` - строка без пробелов длиной от 8 до 16 символов

Весьма удобно использовать для этого уточняющие типы:

```scala
final case class ConnectionConfig(
    host: Host,
    port: Port,
    timeout: Timeout,
    connectionRetry: Retry,
    user: User,
    password: Password
)

object ConnectionConfig:
  opaque type Host     = String :| MinLength[4]
  opaque type Port     = Int :| GreaterEqual[1024] & LessEqual[65535]
  opaque type Timeout  = Int :| Positive
  opaque type Retry    = Int :| GreaterEqual[1024] & LessEqual[65535]
  opaque type User     = String :| Alphanumeric
  opaque type Password = String :| Match["\\S{8, 16}"]
```

У case class-а `ConnectionConfig` конструктор можно определить как приватный, 
чтобы ограничить создание конфига только через Builder. 




---

**Ссылки:**

- [Wikipedia][Wiki]

[Wiki]: https://ru.wikipedia.org/wiki/%D0%A1%D1%82%D1%80%D0%BE%D0%B8%D1%82%D0%B5%D0%BB%D1%8C_(%D1%88%D0%B0%D0%B1%D0%BB%D0%BE%D0%BD_%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F)
[habr]: https://habr.com/ru/company/kryptonite/blog/719488/
