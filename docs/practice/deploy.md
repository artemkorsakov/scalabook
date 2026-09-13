{%
showPrev = true
prevPage = index.html
showNext = true
nextPage = task_types.html
%}

# Создание нового проекта на Scala 3

Итак, начнем с разворачивания нового проекта.

## Предварительные требования

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
Мы добавим семь важных плагинов.

1. В папке вашего проекта создайте файл по пути `project/plugins.sbt`.
   Если папка `project` уже существует, просто создайте в ней файл.
2. Добавьте в этот файл следующие строки:
   ```dotty
   addSbtPlugin("ch.epfl.scala"           % "sbt-scalafix"              % "0.14.8")
   addSbtPlugin("com.github.cb372"        % "sbt-explicit-dependencies" % "0.3.1")
   addSbtPlugin("com.github.reibitto"     % "sbt-welcome"               % "0.6.0")
   addSbtPlugin("com.github.sbt"          % "sbt-native-packager"       % "1.11.7")
   addSbtPlugin("org.jmotor.sbt"          % "sbt-dependency-updates"    % "1.2.9")
   addSbtPlugin("org.playframework.twirl" % "sbt-twirl"                 % "2.0.9")
   addSbtPlugin("org.scalameta"           % "sbt-scalafmt"              % "2.6.1")
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
  version = "3.11.5"

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

#### Приветствие sbt-welcome

- [**Репозиторий**](https://github.com/reibitto/sbt-welcome)
- **Что делает?** Показывает настраиваемое приветственное сообщение при каждом запуске SBT.
  Это может быть полезно для напоминания о полезных командах, ссылках на документацию или
  отображения ASCII-арта проекта. Делает работу с консолью SBT более дружелюбной и информативной.

  ```text
  Welcome to
  
    _____    _____    _____  
   |  __ \  |  __ \  |  __ \
   | |__) | | |  | | | |__) |
   |  ___/  | |  | | |  ___/
   | |      | |__| | | |    
   |_|      |_____/  |_|    
                                                                  
  Version 0.0.1-SNAPSHOT
     
  Useful sbt tasks:
  a. pdp/run - Start backend server
  b. allCompile - Compile main and test sources
  c. prePush - Full pre-commit quality gate: compile, test, format, check dependencies
  d. fullCheck - Comprehensive project validation: prePush + dependency updates check
  ```

#### Упаковка sbt-native-packager

- [**Репозиторий**](https://github.com/sbt/sbt-native-packager)
- **Что делает?** Позволяет собирать приложение в различные форматы для распространения:
  Docker-образы, deb/rpm-пакеты для Linux, MSI для Windows, .exe, .app и многие другие.
  Это критически важно для деплоя приложения в production.

#### Компиляция Twirl-шаблонов sbt-twirl

- [**Репозиторий**](https://github.com/playframework/twirl)
- [**Документация**](https://www.playframework.com/documentation/latest/ScalaTemplates)
- **Что делает?** Компилирует Twirl-шаблоны (файлы `.scala.html`, `.scala.txt`, `.scala.xml`) в обычные Scala-классы.
  Twirl — это шаблонизатор от Play Framework, который позволяет создавать HTML-страницы,
  используя типобезопасный Scala-код. Все шаблоны проверяются компилятором, что исключает ошибки
  в разметке на этапе компиляции.

## Шаг 3: Создание псевдонимов (алиасов) для SBT команд

Чтобы не запоминать длинные последовательности команд, удобно создать для них короткие псевдонимы (алиасы).

1.  Создайте файл `aliases.sbt` в корне проекта (на уровне `build.sbt`). SBT автоматически подхватит его.
2.  Добавьте в файл следующие алиасы:

    ```dotty
    // "allCompile" - Компиляция основного и тестового кода
    addCommandAlias("allCompile", "compile; Test / compile")
    // "fix" - Применение всех исправлений и форматирования
    addCommandAlias("fix", "scalafmtSbt; scalafmtAll; scalafixAll")
    // "prePush" - Полная проверка перед тем, как закоммитить код
    addCommandAlias(
      "prePush",
      "clean; allCompile; test; fix; undeclaredCompileDependencies; unusedCompileDependencies"
    )
    // "fullCheck" - Полная проверка + поиск обновлений для библиотек
    addCommandAlias("fullCheck", "prePush; dependencyUpdates")
    ```

**Как использовать:**

- Перед тем как сделать `git push`, выполните `sbt prePush`. Эта команда:
    1. `clean` — очистит предыдущие артефакты сборки.
    2. `allCompile` — скомпилирует основной и тестовый код.
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
      val Scala = "3.9.0" // Укажите актуальную

      object cats {
        private val version       = "2.13.0"
        private val effectVersion = "3.7.1"
      
        val core         = "org.typelevel" %% "cats-core"          % version
        val effect       = "org.typelevel" %% "cats-effect"        % effectVersion
        val effectKernel = "org.typelevel" %% "cats-effect-kernel" % effectVersion
      }

      object weaver {
        private val version = "0.13.0"
      
        val cats       = "org.typelevel" %% "weaver-cats"       % version
        val scalacheck = "org.typelevel" %% "weaver-scalacheck" % version
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
      // Плагины для генерации фронта и docker image
      .enablePlugins(
        DockerPlugin,
        JavaAppPackaging,
        SbtTwirl
      )
      .settings(
        name := "pdp-backend",
        publish / skip := true,
        libraryDependencies ++= pdpLibs,
        libraryDependencies ++= testLibs
      )
      // Настройки docker image
      .settings(
        Docker / packageName := "personal-development-plan",
        dockerExposedPorts ++= Seq(8080),
        dockerUpdateLatest := true
      )
      .settings(
        run / fork := true
      )
    
    lazy val root = (project in file("."))
      .aggregate(pdp)
      .settings(
        name := "pdp-root",
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
    // Игнорирует ложноположительные сообщения об ошибка в шаблонах Twirl
    "-Wconf:src=.*/views/html/.*:silent",
    // Предупреждает о местах, где компилятор делал предположения о типах
    "-unchecked",
    // Не даст скомпилировать код с предупреждениями.
    "-Werror",
    // Предупреждает об использовании экспериментальных features языка
    "-feature",
    // Предупреждает, если значимый результат выражения (не `Unit`) игнорируется
    "-Wnonunit-statement"
  )
  
ThisBuild / scalaVersion      := Scala
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
```

## Шаг 6: Настройка приветственного экрана sbt-welcome

Добавьте в ваш build.sbt следующую конфигурацию:

```dotty
import sbtwelcome.*

logo :=
  raw"""
       |Welcome to
       |
       |  _____   _____    _____
       | |  __ \ |  __ \  |  __ \
       | | |__) || |  | | | |__) |
       | |  ___/ | |  | | |  ___/
       | | |     | |__| | | |
       | |_|     |_____/  |_|
       |
       |Version ${version.value}
   """.stripMargin

logoColor := scala.Console.MAGENTA

usefulTasks := Seq(
  UsefulTask("pdp/run", "Start backend server"),
  UsefulTask("allCompile", "Compile main and test sources"),
  UsefulTask(
    "prePush",
    "Full pre-commit quality gate: compile, test, format, check dependencies"
  ),
  UsefulTask(
    "fullCheck",
    "Comprehensive project validation: prePush + dependency updates check"
  )
)
```

Как это работает:

- `logo` — задаёт ASCII-арт или текстовый логотип, который будет отображаться при старте SBT. 
- `logoColor` — задаёт цвет логотипа в консоли. Доступные цвета: `scala.Console.RED`, `GREEN`, `YELLOW`, `BLUE`, `MAGENTA`, `CYAN`, `WHITE` и другие.
- `usefulTasks` — список команд, которые отображаются на приветственном экране с описанием. 
  Это помогает новым разработчикам быстро узнать, какие команды доступны в проекте.

## Шаг 7: Финальная проверка работоспособности

После настройки всех инструментов и конфигураций необходимо убедиться, 
что проект корректно собирается и проходит все проверки.

### Подготовка структуры проекта

1. **Очистите стандартные папки:** Удалите папки `src/main/scala` и `src/test/scala`
2. В модуле `pdp` создайте папки `src/main/scala` и `src/test/scala`
3. **Создайте пакет:** В обеих папках создайте пакет `ru.scalabook.pdp`

### Запуск полной проверки

В терминале, в корневой папке проекта, выполните команду:

```dotty
sbt prePush
```

Эта команда, которую мы определили ранее, выполнит полный цикл проверки:

1. **`clean`** — очистка предыдущих артефактов сборки
2. **`com`** — компиляция основного и тестового кода
3. **`test`** — запуск всех тестов
4. **`fix`** — форматирование кода Scalafmt и применение исправлений Scalafix
5. **`undeclaredCompileDependencies`** — проверка на скрытые зависимости
6. **`unusedCompileDependencies`** — поиск неиспользуемых зависимостей 
   (пока игнорируем предупреждение о неиспользуемых библиотеках, т.к. ещё не написан код)

> Предупреждения о неиспользуемых библиотеках пока можно игнорировать, 
> потому что ещё не было написано ни строчки кода, поэтому нет используемых библиотек.

Проект полностью готов к началу разработки.

## Заключение

Отлично! Проект на Scala 3 успешно настроен:

- **Автоматизация:** Форматирование и рефакторинг кода происходят по команде
- **Качество кода:** Строгие настройки компилятора и линтеры помогают избегать ошибок
- **Структура:** Зависимости и настройки аккуратно организованы
- **Функциональный стек:** В основе проекта лежат такие библиотеки, как Cats Effect

Теперь можно сосредоточиться на написании кода, а инструменты позаботятся о его качестве.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
