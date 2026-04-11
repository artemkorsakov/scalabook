{%
showPrev = true
prevPage = ../cats-effect.html
showNext = true
nextPage = copy-files.html
%}

# Начало

Добавляем в зависимости котов:

```sbt
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.6.3"
```

Создаем простое приложение:

```dotty
import cats.effect.{IO, IOApp}

object HelloWorld extends IOApp.Simple:
  val run: IO[Unit] = IO.println("Hello, World!")
```

В результате при прогонке `sbt run` в консоль будет выведено:

```text
Hello, World!
```

---

**Ссылки:**

- [Ссылка на репозиторий с упражнениями][sample]
- [Cats effect documentation][doc]

[doc]: https://typelevel.org/cats-effect/docs/getting-started
[sample]: https://gitverse.ru/scalabook/cats
