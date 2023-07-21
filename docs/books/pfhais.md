# Pure functional HTTP APIs in Scala

В этом разделе приведен пример реализации на Scala 3 чистой имплементации http-сервиса 
из книги [Grassel Jens - Pure functional HTTP APIs in Scala][book].

Для реализации будем использовать следующие библиотеки:

- http4s
- Doobie (в качестве базы данных)
- Flyway для миграции БД (или запуска скриптов)
- Circe для JSON кодеков
- Iron для [использования уточняющих типов][refined] (вместо библиотеки refined, как в примере книги)
- PostgreSQL JDBC драйвер
- pureconfig (для загрузки файлов конфигурации)

> В примерах используются Scala `3.3.0` и последние версии библиотек на июль 2023.

## Чистая обработка конфигурации

К сожалению, на данный момент (июль 2023) для библиотеки iron 
[нет модуля взаимодействия с pureconfig](https://iltotore.github.io/iron/docs/modules/index.html),
поэтому дополнительно определяем given ...


---

**Ссылки:**

- Grassel Jens - Pure functional HTTP APIs in Scala:
    - [Книга][book]
    - [Исходный код][source]

[book]: https://leanpub.com/pfhais
[refined]: ../libs/refined.md
[source]: https://github.com/jan0sch/pfhais