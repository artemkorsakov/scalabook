{%
showPrev = true
prevPage = index.html
%}

# Создание нового проекта на Scala 3

Это руководство шаг за шагом проведет вас через процесс настройки нового проекта на Scala 3 
с использованием современных инструментов и лучших практик для обеспечения чистоты, надежности и поддерживаемости кода.

## Предварительные требования

Перед началом убедитесь, что на вашем компьютере установлены следующие инструменты:

**Установите Scala и sbt**: SBT (Simple Build Tool) — это стандартный инструмент для сборки проектов на Scala. 
Он управляет зависимостями, компиляцией и запуском вашего кода.

**Официальная инструкция по установке**: [Установка Scala и sbt](https://docs.scala-lang.org/ru/getting-started/install-scala.html)

## Шаг 1: Генерация шаблона проекта

Самый быстрый способ начать новый проект — использовать готовый шаблон.

1. Откройте терминал в папке, где вы хотите создать проект.
2. Выполните команду:
   ```bash
   sbt new scala/scala3.g8
   ```
   Эта команда сделает следующее:
   - `sbt new` — команда SBT для создания проекта из шаблона (Giter8).
   - `scala/scala3.g8` — указывает на официальный шаблон для Scala 3.
   - SBT запросит название вашего проекта (например, `my-scala3-app`). После ввода будет создана папка с этим именем, содержащая базовую структуру проекта.

**Полезные ссылки:**

- [Сборка файлов с помощью sbt](https://docs.scala-lang.org/ru/scala3/book/tools-sbt.html) — общее руководство по работе с SBT.
- [Что такое sbt new](https://www.scala-sbt.org/1.x/docs/sbt-new-and-Templates.html) — документация по шаблонам.

## Шаг 2: Подключение полезных плагинов SBT

Плагины расширяют функциональность SBT, автоматизируя рутинные задачи и помогая поддерживать качество кода. 
Мы добавим четыре важных плагина.

1. В папке вашего проекта создайте файл по пути `project/plugins.sbt`. 
   Если папка `project` уже существует, просто создайте в ней файл.
2. Добавьте в этот файл следующие строки:
   ```dotty
   // Плагин для проверки доступных обновлений библиотек
   addSbtPlugin("org.jmotor.sbt"   % "sbt-dependency-updates"    % "1.2.9")
   // Плагин для проверки явного объявления всех зависимостей
   addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.3.1")
   // Плагин для рефакторинга и статического анализа кода (Scalafix)
   addSbtPlugin("ch.epfl.scala"    % "sbt-scalafix"              % "0.14.3")
   // Плагин для автоматического форматирования кода (Scalafmt)
   addSbtPlugin("org.scalameta"    % "sbt-scalafmt"              % "2.5.5")
   ```

### Назначение каждого плагина

#### sbt-scalafix

- **Что делает?** Выполняет статический анализ кода и применяет правила рефакторинга. 
  Помогает находить и исправлять проблемы, устаревшие конструкции (deprecations) и приводит код к единому стилю.
- **Настройка:** Создайте файл `.scalafix.conf` в корне проекта.

  ```text
  # Включаем набор правил
  rules = [
    DisableSyntax,        # Запрещает использование некоторых "опасных" конструкций
    LeakingImplicitClassVal,
    RedundantSyntax       # Удаляет избыточный синтаксис
  ]

  # Конкретные настройки для правила DisableSyntax
  DisableSyntax.noVars = true          # Запрещает объявление `var` (иммутабельность)
  DisableSyntax.noThrows = true        # Запрещает `throw` (используйте типы вроде Either, Try)
  DisableSyntax.noNulls = true         # Запрещает `null` (основной источник ошибок)
  DisableSyntax.noReturns = true       # Запрещает ключевое слово `return`
  DisableSyntax.noWhileLoops = true    # Запрещает циклы `while` (используйте рекурсию или функции высшего порядка)
  DisableSyntax.noAsInstanceOf = true  # Запрещает небезопасные приведения типов
  DisableSyntax.noIsInstanceOf = true  # Запрещает небезопасные проверки типов
  DisableSyntax.noXml = true           # Запрещает устаревший XML-литерал в Scala
  DisableSyntax.noDefaultArgs = true   # Запрещает аргументы по умолчанию в методах
  DisableSyntax.noFinalVal = true      # (Опционально) Запрещает `final val`
  DisableSyntax.noFinalize = true      # Запрещает переопределение метода `finalize`
  ```

**Репозиторий:** [scalacenter/sbt-scalafix](https://github.com/scalacenter/sbt-scalafix)

#### sbt-scalafmt

- **Что делает?** Автоматически форматирует ваш исходный код согласно заданным правилам. 
  Гарантирует, что весь код в проекте выглядит одинаково, что упрощает чтение и совместную работу.
- **Настройка:** Создайте файл `.scalafmt.conf` в корне проекта.

  ```text
  # Версия scalafmt
  version = "3.9.10"

  # Стиль выравнивания
  align.preset = more

  # Используемый диалект Scala
  runner.dialect = scala3

  # Настройки переносов строк
  newlines.source = keep
  # Автоматически конвертирует синтаксис Scala 2 в Scala 3 (где это возможно)
  rewrite.scala3.convertToNewSyntax = true
  # Добавляет "end" маркеры для конструкций (например, `end if`) после 20 строк
  rewrite.scala3.insertEndMarkerMinLines = 20
  ```

**Репозиторий:** [scalameta/scalafmt](https://github.com/scalameta/scalafmt/)

#### sbt-dependency-updates

- **Что делает?** По sbt-команде проверяет репозитории Maven/Ivy и показывает список зависимостей 
  и самих плагинов SBT, для которых вышла новая версия. Помогает поддерживать проект в актуальном состоянии.
- **Использование:** Запустите в SBT команду `dependencyUpdates`.
- **Репозиторий:** [aiyanbo/sbt-dependency-updates](https://github.com/aiyanbo/sbt-dependency-updates)

#### sbt-explicit-dependencies

- **Что делает?** Проверяет, что в файле `build.sbt` явно объявлены ВСЕ библиотеки, которые использует ваш код для компиляции. 
  Иногда SBT может неявно подтянуть зависимости, что приводит к "скрытым" зависимостям и проблемам при их изменении. 
  Этот плагин устраняет такую неявность.
- **Использование:** Запустите в SBT команды `undeclaredCompileDependencies` и `unusedCompileDependencies`.
- **Репозиторий:** [cb372/sbt-explicit-dependencies](https://github.com/cb372/sbt-explicit-dependencies)

## Шаг 3: Создание псевдонимов (алиасов) для SBT команд

Чтобы не запоминать длинные последовательности команд, удобно создать для них короткие псевдонимы (алиасы).

1.  Создайте файл `aliases.sbt` в корне проекта (или в папке `project/`). SBT автоматически подхватит его.
2.  Добавьте в файл следующие алиасы:

    ```dotty
    // "com" - Компиляция основного и тестового кода
    addCommandAlias("com", "compile; Test / compile")

    // "fix" - Применение всех исправлений и форматирования
    addCommandAlias("fix", "scalafmtSbt; scalafmtAll; scalafixAll")

    // "prePush" - Полная проверка перед тем, как закоммитить код в репозиторий (например, в Git)
    addCommandAlias(
      "prePush",
      "clean; com; test; fix; undeclaredCompileDependencies; unusedCompileDependencies"
    )

    // "fullCheck" - Самая полная проверка, включая поиск обновлений для библиотек
    addCommandAlias("fullCheck", "prePush; dependencyUpdates")
    ```

**Как использовать:**

- Вместо ручного запуска компиляции просто введите в SBT: `sbt com`.
- Перед тем как сделать `git push`, выполните `sbt prePush`. Эта команда:
    1. `clean` — очистит предыдущие артефакты сборки.
    2. `com` — скомпилирует основной и тестовый код.
    3. `test` — запустит все тесты.
    4. `fix` — отформатирует код и применит исправления Scalafix.
    5. `undeclaredCompileDependencies` и `unusedCompileDependencies` — проверит корректность объявленных зависимостей.
- Для глубокой проверки используйте `sbt fullCheck`, которая дополнительно покажет доступные обновления библиотек.

## Шаг 4: Управление зависимостями

Чтобы аккуратно управлять библиотеками, особенно в больших проектах, зависимости выносятся в отдельный объект.

1.  Создайте файл `project/Dependencies.scala`.
2.  Добавьте в него код, который объявляет зависимости в структурированном виде:

    ```dotty
    import sbt.*

    object Dependencies {
      // Версия Scala для проекта
      val Scala = "3.7.3"

      object cats {
        private val catsEffectVersion = "3.7-4972921"

        // Библиотека Cats Effect для функционального асинхронного программирования
        val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
      }

      object weaver {
        private val version = "0.8.4"

        // Фреймворк для тестирования, хорошо интегрирующийся с Cats Effect
        val cats = "com.disneystreaming" %% "weaver-cats" % version
      }

      // Список библиотек для основного кода
      val rootLibs: Seq[ModuleID] =
        Seq(
          cats.catsEffect
        )

      // Список библиотек для тестов (указывается с модификатором `% Test`)
      val testLibs: Seq[ModuleID] =
        Seq(
          weaver.cats
        ).map(_ % Test)
    }
    ```

3.  Теперь обновите файл `build.sbt` в корне проекта, чтобы он использовал эти зависимости:

    ```dotty
    // Импортируем наши зависимости
    import Dependencies.*

    lazy val root = project
      .in(file("."))
      .settings(
        name         := "PersonalDevelopmentPlan", // Имя вашего проекта
        version      := "0.1.0-SNAPSHOT",          // Версия проекта
        scalaVersion := Scala,                     // Версия Scala из Dependencies.scala
        // Добавляем зависимости из нашего объекта
        libraryDependencies ++= rootLibs,
        libraryDependencies ++= testLibs
      )
    ```

**Используемый стек технологий (Typelevel Stack):**

- **[Cats Effect](https://typelevel.org/cats-effect/)** — библиотека для асинхронного 
  и параллельного программирования на основе функциональных принципов.
- **[Weaver Test](https://typelevel.org/weaver-test/overview/installation.html)** — простой 
  и мощный фреймворк для тестирования, идеально подходящий для проектов на Cats Effect.

## Шаг 5: Настройка параметров компилятора

Строгие настройки компилятора помогают писать более качественный код, 
превращая потенциальные ошибки времени выполнения в ошибки компиляции.

Добавьте в ваш `build.sbt` следующий блок (обычно в начало файла, так как он применяется ко всем подпроектам `ThisBuild`):

```dotty
ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",        // Предупреждает об использовании устаревших конструкций
    "-explain",            // Подробно объясняет ошибки компиляции
    "-Wunused:imports",    // Превращает неиспользуемые импорты в ошибки
    "-unchecked",          // Предупреждает о местах, где компилятор делал предположения о типах
    "-Xfatal-warnings",    // Превращает все предупреждения (warnings) в ошибки! Не даст скомпилировать код с предупреждениями.
    "-feature",            // Предупреждает об использовании экспериментальных features языка
    "-Wnonunit-statement"  // Предупреждает, если значимый результат выражения (не `Unit`) игнорируется
  ) 
```

## Шаг 6: Финальная проверка

- Очищаем папки `src/main/scala` и `src/test/scala`
- Создаем в них пакет `ru.scalabook.pdp`
- В папке `src/main/scala` создаем файл `Main.scala`:
  ```dotty
  package ru.scalabook.pdp
  
  import cats.effect.IO
  
  @main def hello(): Unit =
      println("Hello world!")
  
  def msgIO: IO[String] = IO("I was compiled by Scala 3. :)")
  ```
- В папке `src/test/scala` создаем файл `MainSuite.scala`:
  ```dotty
  package ru.scalabook.pdp

  import weaver.*
  
  object MainSuite extends SimpleIOSuite:
  
    test("hello side-effects"):
      for
        x <- msgIO
      yield expect.same("I was compiled by Scala 3. :)", x)
  ```
  
Все настроено! Пришло время убедиться, что проект собирается и проходит все проверки.



В терминале, в корневой папке проекта, выполните команду:

```dotty
sbt fullCheck
```

Эта команда, которую мы определили ранее, выполнит полный цикл проверки: 
очистку, компиляцию, тесты, форматирование, анализ зависимостей и проверку на обновления. 
Если все этапы прошли успешно, ваш проект полностью готов к началу разработки.

## Заключение

Поздравляем! Вы успешно настроили современный проект на Scala 3 с использованием лучших практик:

- **Автоматизация:** Форматирование и рефакторинг кода происходят по команде.
- **Качество код:** Строгие настройки компилятора и линтеры помогают избегать ошибок.
- **Структура:** Зависимости и настройки аккуратно организованы.
- **Функциональный стек:** В основе проекта лежат мощные и популярные библиотеки Cats Effect и Weaver.

Теперь вы можете сосредоточиться на написании кода, а инструменты позаботятся о его качестве.

---

**Ссылки:**

- [Документация к плагину "Персональный план развития"][plugin]
- [Исходный код этого проекта на GitVerse][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
