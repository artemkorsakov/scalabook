{%
showPrev = true
prevPage = io-local.html
showNext = true
nextPage = scala-native.html
%}

# GraalVM Native Image и Cats Effect

## Что такое GraalVM Native Image?

**GraalVM Native Image** — это технология, позволяющая компилировать Java-приложения 
(и приложения на других JVM-языках, включая Scala) в **нативные исполняемые файлы**. 
В отличие от традиционной JVM-модели (код компилируется в байт-код, который затем интерпретируется или JIT-компилируется), 
нативный образ заранее компилируется в машинный код конкретной платформы.

**Ключевые преимущества нативных образов:**

| Характеристика                  | Традиционная JVM                    | GraalVM Native Image                            |
|:--------------------------------|:------------------------------------|:------------------------------------------------|
| **Время запуска**               | От сотен миллисекунд до секунд      | Миллисекунды (обычно 5-50 мс)                   |
| **Потребление памяти**          | Высокое (особенно в первые минуты)  | Значительно ниже                                |
| **Размер исполняемого файла**   | JAR-файл + JVM                      | Один исполняемый файл (~10-50 МБ)               |
| **Пиковая производительность**  | Высокая (после прогрева JIT)        | Ниже, чем у прогретой JVM                       |
| **Кроссплатформенность**        | Да (благодаря JVM)                  | Нет (нужно компилировать под каждую платформу)  |

**Когда использовать нативные образы:**

- **CLI-утилиты** — важен быстрый запуск.
- **Serverless / FaaS (AWS Lambda, Cloud Functions)** — холодный старт критичен.
- **Микросервисы** с низкими требованиями к памяти.
- **Встраиваемые системы** с ограниченными ресурсами.

**Когда НЕ стоит использовать:**

- Долгоживущие серверы с высокими требованиями к пиковой производительности (JVM после прогрева будет быстрее).
- Приложения, интенсивно использующие динамические возможности (рефлексию, прокси, динамическую загрузку классов).
- Когда важна кроссплатформенность (нужно компилировать образ под каждую ОС/архитектуру отдельно).

## Cats Effect и Native Image: что работает

Начиная с **Cats Effect 3.3.0**, была добавлена поддержка ключевых функций для работы в нативных образах:

1. **Дампы волокон (Fiber Dumps):** Функциональность, использующая `java.lang.reflect.Proxy` 
   для обработки сигналов POSIX (`SIGINFO`/`SIGUSR1`), полностью совместима с Native Image. 
   JAR-файлы Cats Effect содержат **дополнительные метаданные** для конфигурации отражения (reflection configuration), необходимые GraalVM.
2. **Минимальная версия GraalVM:** Поддержка Native Image гарантируется для **GraalVM 21.0 и выше**. 
   Более ранние версии могут работать, но официально не поддерживаются.
3. **Важное ограничение:** Необходимо использовать флаг `--no-fallback` при компиляции. 
   В противном случае `native-image` попытается найти статическую конфигурацию отражения 
   для методов перечисления (`scala.Enumeration`), что приведёт к сбою. 
   Если в вашем коде используется `scala.Enumeration`, этот подход не сработает (см. [issue #3051](https://github.com/typelevel/cats-effect/issues/3051)).

## Полный пример: Hello World с sbt-native-image

### 1. Настройка плагина (`project/plugins.sbt`)

Добавьте плагин `sbt-native-image`:

```dotty
// project/plugins.sbt
addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.1")
```

### 2. Конфигурация проекта (`build.sbt`)

```dotty
// build.sbt
ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "3.8.3"

lazy val root = (project in file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "cats-effect-3-hello-world",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.7.0",
    Compile / mainClass := Some("com.example.Main"),
    nativeImageOptions += "--no-fallback",      // КЛЮЧЕВОЙ ФЛАГ!
    nativeImageVersion := "22.1.0"              // Минимум 21.0.0
  )
```

### 3. Код приложения (`src/main/scala/com/example/Main.scala`)

```dotty
package com.example

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  def run: IO[Unit] = IO.println("Hello Cats Effect!")
```

### 4. Компиляция

```bash
sbt nativeImage
```

Успешно скомпилированный нативный образ будет находиться по адресу:

```
target/native-image/cats-effect-3-hello-world
```

### 5. Запуск и измерение производительности

```bash
# Простой запуск
$ ./target/native-image/cats-effect-3-hello-world
Hello Cats Effect!

# Измерение времени запуска с помощью hyperfine
$ hyperfine ./target/native-image/cats-effect-3-hello-world
Benchmark 1: ./target/native-image/cats-effect-3-hello-world
  Time (mean ± σ):      11.2 ms ±   0.6 ms    [User: 6.7 ms, System: 4.8 ms]
  Range (min … max):     9.4 ms …  12.9 ms    182 runs
```

**Результат:** Время запуска около **11 миллисекунд** — это на 1-2 порядка быстрее, 
чем запуск того же приложения на JVM (обычно 100-500 мс). 
Это критически важно для serverless-сред и CLI-утилит.

## Альтернативный способ: Scala CLI

Если вы не используете sbt, вы можете скомпилировать нативный образ с помощью команды `package` из **scala-cli**:

```bash
scala-cli package --native-image --graalvm-jvm-id 22.1.0 --no-fallback Main.scala
```

(Подразумевается, что у вас уже установлен `scala-cli` и нужная версия GraalVM.)

## Устранение неполадок и «подводные камни»

### 1. Флаг `--no-fallback` обязателен

Без этого флага `native-image` попытается создать образ, который в случае ошибки может «откатиться» к запуску на JVM. 
Это приводит к попыткам анализа рефлексии для `scala.Enumeration`, которые не имеют статической конфигурации и вызовут ошибку.

**Решение:** Всегда добавляйте `nativeImageOptions += "--no-fallback"`.

### 2. Использование `scala.Enumeration`

Если в вашем коде или зависимостях используется `scala.Enumeration`, 
прямое использование `--no-fallback` может быть небезопасным (см. [комментарий в issue #3051](https://github.com/typelevel/cats-effect/issues/3051#issuecomment-1167026949)). 
Рекомендуется избегать перечислений в коде, который будет компилироваться в нативный образ. 
Альтернативы: `sealed trait + case object`, `enumeratum` (с осторожностью) или Java-перечисления.

### 3. Динамические возможности

Код, интенсивно использующий рефлексию, динамические прокси, `Class.forName` и т.д., 
может потребовать дополнительной конфигурации (JSON-файлы с описанием рефлексии). 
Cats Effect предоставляет метаданные для своих внутренних нужд, но ваш код — нет.

**Решение:** Добавляйте `nativeImageOptions ++= Seq("-H:ReflectionConfigurationFiles=path/to/reflect-config.json")` при необходимости.

### 4. Версия GraalVM

Убедитесь, что ваша версия GraalVM **22.1.0** или новее (подойдет и 21.0.0, но 22.x имеет улучшения). Проверить версию можно командой:

```bash
gu --version
native-image --version
```

### 5. Память при компиляции

Компиляция нативных образов требует значительного количества памяти (несколько гигабайт). 
Убедитесь, что вы выделили достаточно памяти для процесса `native-image`:

```bash
export JAVA_OPTS="-Xmx6g"
sbt nativeImage
```

## Итоговый чек-лист для успешной компиляции

- GraalVM версии ≥ 21.0.0 (рекомендуется 22.1+)
- Плагин `sbt-native-image` версии ≥ 0.3.1
- В `build.sbt`: `nativeImageOptions += "--no-fallback"`
- В `build.sbt`: `nativeImageVersion := "22.1.0"`
- Код не использует `scala.Enumeration` (или вы готовы к дополнительным настройкам)
- Достаточно памяти для компиляции (рекомендуется 4-6 ГБ)

**Будущее:** Поддержка Native Image в Cats Effect активно развивается. 
Следите за обновлениями и [официальной документацией](https://typelevel.org/cats-effect/) для новых версий.

---

**Ссылки:**

- [Cats effect documentation][doc]
- [Cats effect GraalVM Native Image](https://typelevel.org/cats-effect/docs/core/native-image)

[doc]: https://typelevel.org/cats-effect/docs/concepts
