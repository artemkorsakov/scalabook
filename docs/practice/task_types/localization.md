{%
showPrev = true
prevPage = statuses.html
showNext = true
nextPage = ../db.html
%}

# Локализация

В данном разделе мы разберём, как реализуется **локализация** 
в приложении управления индивидуальным планом развития (ИПР), 
чтобы оно могло поддерживать несколько языков. Например, русский и английский. 
Мы будем использовать библиотеку [pureconfig](https://pureconfig.github.io/docs/) для чтения конфигураций.

Локализация позволяет:

- Предоставлять интерфейс на разных языках.
- Позволять пользователям добавлять новые языки.
- Гарантировать корректность данных.
- Упрощать обслуживание текстовых сообщений в приложении.

## Добавление зависимостей

Для работы с конфигурациями и уточнёнными типами данных, нам нужны следующие зависимости:

```dotty
object Dependencies {
  ...

  object iron {
    private val version = "3.2.1"
    val core            = "io.github.iltotore" %% "iron"            % version
    val pureconfig      = "io.github.iltotore" %% "iron-pureconfig" % version
  }

  object pureconfig {
    private val version = "0.17.9"
    val core            = "com.github.pureconfig" %% "pureconfig-core" % version
  }

  ...

  val pdpLibs: Seq[ModuleID] =
    Seq(
      cats.catsEffect,
      iron.core,
      iron.pureconfig,
      pureconfig.core
    )

  ...
}
```

## Конфигурационный файл

Создадим файл `application.conf` в директории `pdp/src/main/resources`:

```conf
// pdp/src/main/resources/application.conf
localization = {
  languages = [
    {
      code = "ru"
      name = "Русский"
      status = {
        "in-progress": "В работе"
        "planned": "Запланировано"
        "knowledge-base": "База знаний"
        "completed": "Завершено"
      }
      types = {
        "book" = "Книга"
        "article" = "Статья"
        "podcast" = "Подкаст"
        "video" = "Видео"
        "course" = "Курс"
      }
    },
    {
      code = "en"
      name = "English"
      status = {
        "in-progress": "In progress"
        "planned": "Planned"
        "knowledge-base": "Knowledge base"
        "completed": "Completed"
      }
      types = {
        "book" = "Book"
        "article" = "Article"
        "podcast" = "Podcast"
        "video" = "Video"
        "course" = "Course"
      }
    }
  ]
}
```

## Модели конфигурации

Создадим файл `Config.scala` в пакете `ru.scalabook.pdp.localization`:

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/localization/Config.scala
package ru.scalabook.pdp.localization

import io.github.iltotore.iron.pureconfig.given
import pureconfig.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

/** Тип `NonEmptyString` — это непустая строка без начальных и конечных
  * пробелов.
  */
type NonEmptyString = NonEmptyString.T
object NonEmptyString extends RefinedType[String, Not[Empty] & Trimmed]

/** Тип `LanguageCode` — это код языка от 2 до 3 символов. */
type LanguageCode = LanguageCode.T
object LanguageCode extends RefinedType[String, MinLength[2] & MaxLength[3]]

/** Тип `LanguageName` — это имя языка от 2 до 20 символов. */
type LanguageName = LanguageName.T
object LanguageName extends RefinedType[String, MinLength[2] & MaxLength[20]]

/** `StatusConfig` содержит локализацию статусов задач. */
final case class StatusConfig(
    inProgress: NonEmptyString,
    planned: NonEmptyString,
    knowledgeBase: NonEmptyString,
    completed: NonEmptyString
)

/** `TypesConfig` содержит локализацию типов задач. */
final case class TypesConfig(
    book: NonEmptyString,
    article: NonEmptyString,
    podcast: NonEmptyString,
    video: NonEmptyString,
    course: NonEmptyString
)

/** `LanguageConfig` содержит информацию о языке и его локализации:
  *   - `code` — код языка (например, "ru", "en")
  *   - `name` — имя языка (например, "Русский", "English")
  *   - `status` — локализация статусов
  *   - `types` — локализация типов задач
  */
final case class LanguageConfig(
    code: LanguageCode,
    name: LanguageName,
    status: StatusConfig,
    types: TypesConfig
) derives ConfigReader

/** `Languages` — список языков, поддерживающих как минимум 2 языка. */
type Languages = List[LanguageConfig] :| MinLength[2]

/** `LocalizationConfig` содержит все локализационные настройки. */
final case class LocalizationConfig(languages: Languages)
    derives ConfigReader

/** `Config` — корневой объект конфигурации. */
final case class Config(localization: LocalizationConfig) derives ConfigReader
```

## Тестирование конфигурации

Кроме требования того, чтобы список языков содержал минимум два языка,
потребуем, чтобы этот список содержал языки по-умолчанию: русский и английский.

Создадим файл `ConfigSuite.scala` в аналогичном пакете, но в директории тестов, 
где проверим корректность загрузки конфигурации:

```dotty
// pdp/src/main/test/ru/scalabook/pdp/localization/ConfigSuite.scala
package ru.scalabook.pdp.localization

import pureconfig.*
import weaver.*

object ConfigSuite extends SimpleIOSuite:
  private val configSource = ConfigSource.default.load[Config]

  pureTest("check read config"):
    expect(configSource.isRight)

  pureTest("check Russian language config"):
    matches(configSource):
      case Right(config) =>
        expect(
          config.localization.languages.exists(_.code == LanguageCode("ru"))
        )

  pureTest("check English language config"):
    matches(configSource):
      case Right(config) =>
        expect(
          config.localization.languages.exists(_.code == LanguageCode("en"))
        )
end ConfigSuite
```

### Что проверяет тест?

- Проверяет, что конфиг прочитался корректно.
- Проверяет, что обязательный языки, русский и английский, присутствуют в конфиге.
- Гарантирует, что данные соответствуют ограничениям.

## Расширяемость системы

Одной из важных особенностей разработанной системы локализации является её **гибкость и расширяемость**. 
Система спроектирована так, чтобы добавление новых языков было максимально простым, не требуя изменений в коде приложения.

### Как добавить новый язык?

Чтобы добавить поддержку нового языка, например, испанского, 
достаточно **изменить конфигурационный файл** `application.conf`, 
добавив в секцию `languages` новый блок:

```conf
{
  code = "es"
  name = "Español"
  status = {
    "in-progress": "En progreso"
    "planned": "Planificado"
    "knowledge-base": "Base de conocimientos"
    "completed": "Terminado"
  }
  types = {
    "book" = "Libro"
    "article" = "Artículo"
    "podcast" = "Podcast"
    "video" = "Video"
    "course" = "Curso"
  }
}
```

> ✅ Все остальные части системы будут работать без изменений, 
> потому что модель `LanguageConfig` ожидает список произвольной длины.

### Автоматическая проверка данных

Поскольку мы используем уточненные типы, система автоматически проверяет:

- Что код языка имеет длину от 2 до 3 символов.
- Что имя языка содержит от 2 до 20 символов.
- Что все строки (статусы, типы задач) не пустые и не содержат лишних пробелов.
- Что список языков содержит минимум два элемента (по умолчанию — русский и английский).

Если какая-либо запись в конфиге не соответствует этим ограничениям, 
загрузка конфигурации завершится с ошибкой в момент прогонки тестов. 
Это позволяет **рано выявлять проблемы** и **гарантировать стабильность** системы.

### Преимущества архитектуры

#### Не требуется изменять код приложения

При добавлении нового языка **не нужно менять ни одну строку кода**. 
Все изменения происходят только в конфигурационном файле. 
Это делает систему очень удобной для администраторов и локализаторов.

#### Гарантии качества данных

С помощью уточненных типов, мы можем быть уверены, что:

- Нет пустых или некорректных строк.
- Коды языков соблюдены.
- Все необходимые поля присутствуют.

Это снижает вероятность ошибок в будущем и упрощает обслуживание.

#### Поддержка нескольких языков из коробки

Система уже настроена на работу с несколькими языками. 
Её можно использовать как основу для международной версии продукта 
или для внутреннего использования в мультиязычной команде.

## Итоги

- **Локализация** реализована через конфигурационные файлы, что делает её **удобной и гибкой**.
- Благодаря **уточнённым типам**, данные проходят автоматическую проверку на корректность.
- **Библиотека `pureconfig`** позволяет легко загружать конфигурации из `application.conf`.
- Система **легко расширяема**: новые языки добавляются без изменений в коде.
- **Тесты** гарантируют, что конфигурация читается правильно и содержит все необходимые данные,
  в том числе после обновления конфигурационного файла.
- Архитектура спроектирована так, чтобы поддерживать **долгосрочную устойчивость** и **лёгкое масштабирование**.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
