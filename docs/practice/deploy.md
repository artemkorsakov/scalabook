{%
showPrev = true
prevPage = index.html
showNext = true
nextPage = task_types.html
%}

# Создание нового проекта на Scala 3

Итак, начнем с разворачивания нового проекта.

## Предварительные требования

Перед началом убедитесь, что на вашем компьютере установлены следующие инструменты:

[**Установите Scala и sbt**](https://docs.scala-lang.org/ru/getting-started/install-scala.html): 
SBT (Scala Build Tool) — это стандартный инструмент для сборки проектов на Scala. 
Он управляет зависимостями, компиляцией и запуском вашего кода.

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
   - SBT запросит название вашего проекта (например, `PersonalDevelopmentPlan`). 
     После ввода будет создана папка с этим именем, содержащая базовую структуру проекта.

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

#### Статический анализ кода sbt-scalafix

- [**Репозиторий**](https://github.com/scalacenter/sbt-scalafix)
- [**Описание правил**](https://scalacenter.github.io/scalafix/docs/users/installation.html)
- **Что делает?** Выполняет статический анализ кода и применяет правила рефакторинга. 
  Помогает находить и исправлять проблемы, устаревшие конструкции (deprecations) и приводит код к единому стилю.
  Особенно эффективен в больших командах, помогая обеспечить единый code style.
- **Настройка:** Создайте файл `.scalafix.conf` в корне проекта со следующим содержимым:

  ```text
  # Включаем набор правил
  rules = [
      DisableSyntax         # Запрещает использование некоторых "опасных" конструкций
      LeakingImplicitClassVal
      OrganizeImports       # Удаляет неиспользуемые импорты
      RedundantSyntax       # Удаляет избыточный синтаксис
  ]

  # Конкретные настройки для правила DisableSyntax
  DisableSyntax.noVars = true          # Запрещает `var` - изменяемые переменные
  DisableSyntax.noThrows = true        # Запрещает `throw` - исключения
  DisableSyntax.noNulls = true         # Запрещает `null`
  DisableSyntax.noReturns = true       # Запрещает ключевое слово `return`
  DisableSyntax.noWhileLoops = true    # Запрещает циклы `while`
  DisableSyntax.noAsInstanceOf = true  # Запрещает небезопасные приведения типов
  DisableSyntax.noIsInstanceOf = true  # Запрещает небезопасные проверки типов
  DisableSyntax.noXml = true           # Запрещает устаревший XML-литерал в Scala
  DisableSyntax.noDefaultArgs = true   # Запрещает аргументы по умолчанию в методах
  DisableSyntax.noFinalVal = true      # Запрещает `final val`
  DisableSyntax.noFinalize = true      # Запрещает переопределение метода `finalize`
  
  # Правила для стандартизации импортов
  OrganizeImports {
      removeUnused = false
      targetDialect = Scala3
    
      groupedImports = Keep
      coalesceToWildcardImportThreshold = 5
  }
  ```

#### Форматирование sbt-scalafmt

- [**Репозиторий**](https://github.com/scalameta/scalafmt/)
- **Что делает?** Автоматически форматирует ваш исходный код согласно заданным правилам. 
  Гарантирует, что весь код в проекте выглядит одинаково, что упрощает чтение и совместную работу.
- **Настройка:** Создайте файл `.scalafmt.conf` в корне проекта со следующим содержимым:

  ```text
  # Версия scalafmt
  version = "3.10.6"

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

#### Обновление зависимостей sbt-dependency-updates

- [**Репозиторий**](https://github.com/aiyanbo/sbt-dependency-updates)
- **Что делает?** По sbt-команде проверяет репозитории Maven/Ivy и показывает список зависимостей 
  и самих плагинов SBT, для которых вышла новая версия. Помогает поддерживать проект в актуальном состоянии.
- **Использование:** Запустите в SBT команду `dependencyUpdates`.

#### Скрытые зависимости sbt-explicit-dependencies

- [**Репозиторий**](https://github.com/cb372/sbt-explicit-dependencies)
- **Что делает?** Проверяет, что в файле `build.sbt` явно объявлены все библиотеки, которые использует ваш код для компиляции. 
  Иногда SBT может неявно подтянуть зависимости, что приводит к "скрытым" зависимостям и проблемам при их изменении. 
  Этот плагин устраняет такую неявность.
- **Использование:** Запустите в SBT команды `undeclaredCompileDependencies` и `unusedCompileDependencies`.

## Шаг 3: Создание псевдонимов (алиасов) для SBT команд

Чтобы не запоминать длинные последовательности команд, удобно создать для них короткие псевдонимы (алиасы).

1.  Создайте файл `aliases.sbt` в корне проекта (на уровне `build.sbt`). SBT автоматически подхватит его.
2.  Добавьте в файл следующие алиасы:

    ```dotty
    // "com" - Компиляция основного и тестового кода
    addCommandAlias("com", "compile; Test / compile")

    // "fix" - Применение всех исправлений и форматирования
    addCommandAlias("fix", "scalafmtSbt; scalafmtAll; scalafixAll")

    // "prePush" - Полная проверка перед тем, как закоммитить код
    addCommandAlias(
      "prePush",
      "clean; com; test; fix; undeclaredCompileDependencies; unusedCompileDependencies"
    )

    // "fullCheck" - Полная проверка + поиск обновлений для библиотек
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

Чтобы аккуратно управлять библиотеками, особенно в больших проектах, 
зависимости желательно выносить в отдельный объект.

1.  Создайте файл `project/Dependencies.scala`.
2.  Добавьте в него код, который объявляет зависимости в структурированном виде:

    ```dotty
    import sbt.*

    object Dependencies {
      // Версия Scala для проекта
      val Scala = "3.7.3" // Укажите актуальную

      object cats {
        private val version       = "2.13.0"
        private val effectVersion = "3.7-4972921"
      
        val core         = "org.typelevel" %% "cats-core"          % version
        val effect       = "org.typelevel" %% "cats-effect"        % effectVersion
        val effectKernel = "org.typelevel" %% "cats-effect-kernel" % effectVersion
      }

      object weaver {
        private val version = "0.8.4"

        // Фреймворк для тестирования, хорошо интегрирующийся с Cats Effect
        val cats = "com.disneystreaming" %% "weaver-cats" % version
      }

      // Список библиотек для основного кода
      val pdpLibs: Seq[ModuleID] =
        Seq(
          cats.core,
          cats.effect,
          cats.effectKernel,
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
    // Импортируем зависимости
    import Dependencies.*

    lazy val pdp = (project in file("pdp"))
      // Имя проекта
      .settings(name := "PersonalDevelopmentPlan") 
      .settings(
        // Добавляем зависимости из нашего объекта
        libraryDependencies ++= pdpLibs,
        libraryDependencies ++= testLibs
      )
    
    lazy val root = (project in file("."))
      // Сборка всех модулей
      .aggregate(pdp)
      .settings(
        name := "root",
        publish / skip := true
      )
    ```

    В сложных проектах рекомендуется использовать модульную структуру, когда функциональность разбита на отдельные куски.
    Например, `pdp` — это модуль для разработки личного плана, а `root` — это корневой модуль.
    В этом случае `root` не является самостоятельным проектом, а просто объединяет все модули.
    Это позволяет легко собирать и тестировать отдельные части проекта.

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
    // Предупреждает об использовании устаревших конструкций
    "-deprecation",
    // Подробно объясняет ошибки компиляции
    "-explain",
    // Превращает неиспользуемые импорты в ошибки
    "-Wunused:imports",
    // Предупреждает о местах, где компилятор делал предположения о типах
    "-unchecked",
    // Превращает все предупреждения (warnings) в ошибки!
    // Не даст скомпилировать код с предупреждениями.
    "-Xfatal-warnings",
    // Предупреждает об использовании экспериментальных features языка
    "-feature",
    // Предупреждает, если значимый результат выражения (не `Unit`) игнорируется
    "-Wnonunit-statement"
  )
  
// Версия Scala берется из Dependencies.scala  
ThisBuild / scalaVersion := Scala

// Версия проекта (чаще всего выносят в отдельный файл version.sbt)
ThisBuild / version := "0.1.0-SNAPSHOT"
```

## Шаг 6: Финальная проверка работоспособности

После настройки всех инструментов и конфигураций необходимо убедиться, 
что проект корректно собирается и проходит все проверки.

### Подготовка структуры проекта

1. **Очистите стандартные папки:** Удалите папки `src/main/scala` и `src/test/scala`
2. В модуле `pdp` создайте папки `src/main/scala` и `src/test/scala`
3. **Создайте пакет:** В обеих папках создайте пакет `ru.scalabook.pdp`

### Запуск полной проверки

В терминале, в корневой папке проекта, выполните команду:

```dotty
sbt fullCheck
```

Эта команда, которую мы определили ранее, выполнит полный цикл проверки:

1. **`clean`** — очистка предыдущих артефактов сборки
2. **`com`** — компиляция основного и тестового кода
3. **`test`** — запуск всех тестов
4. **`fix`** — форматирование кода Scalafmt и применение исправлений Scalafix
5. **`undeclaredCompileDependencies`** — проверка на скрытые зависимости
6. **`unusedCompileDependencies`** — поиск неиспользуемых зависимостей 
   (пока игнорируем предупреждение о неиспользуемых библиотеках, т.к. ещё не написан код)
7. **`dependencyUpdates`** — проверка доступных обновлений библиотек

Проект полностью готов к началу разработки.

## Заключение

Отлично! Проект на Scala 3 успешно настроен:

- **Автоматизация:** Форматирование и рефакторинг кода происходят по команде
- **Качество кода:** Строгие настройки компилятора и линтеры помогают избегать ошибок
- **Структура:** Зависимости и настройки аккуратно организованы
- **Функциональный стек:** В основе проекта лежат мощные и популярные библиотеки, такие как Cats Effect

Теперь можно сосредоточиться на написании кода, а инструменты позаботятся о его качестве.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
