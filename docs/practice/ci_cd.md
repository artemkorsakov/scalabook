{%
showPrev = true
prevPage = ui_tasks/ui_task_timer.html
showNext = true
nextPage = ordinal.html
%}

# Добавление Docker-образа

После того как приложение полностью реализовано и протестировано локально, наступает этап
доставки — упаковки в Docker-образ и запуска вместе с инфраструктурными сервисами
(PostgreSQL и Redis) через `docker-compose`. Это позволяет запустить приложение
на любой машине одной командой, не устанавливая JDK, Scala и PostgreSQL вручную.

## Настройка сборки Docker-образа в `build.sbt`

Для сборки Docker-образа используется [sbt-native-packager](https://github.com/sbt/sbt-native-packager) — плагин, который
умеет упаковывать Scala-приложение в Docker-образ вместе с JRE, всеми зависимостями
и стартовым скриптом.

### Подключение плагинов

```dotty
lazy val pdp = (project in file("pdp"))
  .enablePlugins(
    DockerPlugin,        // Сборка Docker-образа
    JavaAppPackaging,    // Упаковка JVM-приложения (JRE + все jar'ы + скрипт запуска)
    SbtTwirl             // Компиляция Twirl-шаблонов
  )
```

**Разбор плагинов:**

- **`JavaAppPackaging`** — базовый плагин. Собирает приложение в «дистрибутив»:
  JRE + все jar-файлы + bash-скрипт для запуска. Именно этот дистрибутив потом
  кладётся в Docker-образ.
- **`DockerPlugin`** — расширяет `JavaAppPackaging`, добавляя возможность
  собрать Docker-образ из дистрибутива.
- **`SbtTwirl`** — нужен, потому что у нас HTML-шаблоны на Twirl.

Без `JavaAppPackaging` плагин `DockerPlugin` не заработает — он зависит от него.

### Настройки Docker

```dotty
  .settings(
    Docker / packageName := "personal-development-plan",
    dockerExposedPorts ++= Seq(8080),
    dockerUpdateLatest := true
  )
```

**Разбор настроек:**

| Настройка                  | Значение                      | Что делает                                                                                                                |
| -------------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| **`Docker / packageName`** | `"personal-development-plan"` | Имя Docker-образа. При сборке получится `personal-development-plan:0.1.0` и тег `latest`.                                 |
| **`dockerExposedPorts`**   | `Seq(8080)`                   | Директива `EXPOSE 8080` в Dockerfile. Порт, на котором слушает приложение.                                                |
| **`dockerUpdateLatest`**   | `true`                        | После сборки обновляет тег `latest` — `personal-development-plan:latest`. Без этого пришлось бы указывать версию вручную. |

### Как это работает под капотом

При выполнении `sbt Docker/publishLocal` происходит следующее:

1. **`stage`** — `JavaAppPackaging` собирает дистрибутив в `pdp/target/docker/stage/opt/docker/`:

   ```
   opt/docker/
   ├── bin/
   │   └── pdp-backend          # стартовый скрипт
   ├── lib/
   │   └── *.jar                # все зависимости
   ├── assets/                  # наши статические файлы
   └── ...
   ```

2. **Dockerfile** — `DockerPlugin` генерирует Dockerfile:

   ```dockerfile
   FROM eclipse-temurin:21-jre
   WORKDIR /opt/docker
   COPY --chown=... opt /opt/docker
   USER 1001:0
   EXPOSE 8080
   ENTRYPOINT ["/opt/docker/bin/pdp-backend"]
   ```

3. **`docker build`** — Docker собирает образ на основе этого Dockerfile.

4. **Тег `latest`** — обновляется, если `dockerUpdateLatest := true`.

## Работа со статическими ресурсами

### Проблема

По умолчанию Krop ожидает статические файлы по пути `src/main/resources/assets`.
Это работает при запуске через `sbt run` или из IDE, но **ломается в Docker-контейнере**:
внутри образа нет директории `src/`, а рабочая директория — `/opt/docker`.

При старте приложения возникнет ошибка:

```
Cannot create an asset route for src/main/resources/assets as this path is not a directory.
```

### Решение: `src/universal`

`JavaAppPackaging` автоматически копирует содержимое `src/universal/` в `/opt/docker/`
внутри образа. Поэтому мы переносим ассеты туда:

```
pdp/
└── src/
    └── universal/
        └── assets/
            ├── css/
            ├── js/
            └── icons/
```

**После сборки образа** эта структура окажется внутри контейнера:

```
/opt/docker/
├── bin/
├── lib/
└── assets/
    ├── css/
    ├── js/
    └── icons/
```

### Изменение `AssetRoute`

Обновляем код так, чтобы путь был **относительным** к рабочей директории `/opt/docker`:

```dotty
// Было:
val assets: AssetRoute =
  AssetRoute(Path.root / "assets", "src/main/resources/assets")

// Стало:
val assets: AssetRoute =
  AssetRoute(Path.root / "assets", "assets")
```

**Разбор:**

- **`Path.root / "assets"`** — URL, по которому ассеты доступны: `/assets/...`
- **`"assets"`** — путь на файловой системе относительно рабочей директории.
  В Docker рабочая директория — `/opt/docker`, поэтому Krop ищет `/opt/docker/assets`.
  Именно там оказались файлы благодаря `src/universal`.

**Как проверить, что файлы на месте:**

```bash
docker run --rm --entrypoint ls personal-development-plan:latest -la /opt/docker/assets
```

Вы должны увидеть `css/`, `js/`, `icons/`.

## Docker Compose

`docker-compose.yml` описывает три сервиса: PostgreSQL, Redis и наше приложение.

```yml
services:
  postgres:
    restart: always
    image: postgres:18
    ports:
      - "5432:5432"
    environment:
      - DEBUG=false
      - POSTGRES_DB=pdp
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=my-password
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  redis:
    restart: always
    image: redis:latest
    ports:
      - "6379:6379"
    environment:
      - DEBUG=false
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 1s
      timeout: 3s
      retries: 30

  pdp:
    image: personal-development-plan:latest
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
```

### Разбор сервисов

**Сервис `postgres`:**

- **`image: postgres:18`** — официальный образ PostgreSQL версии 18.
- **`ports: "5432:5432"`** — пробрасывает порт на хост. Это удобно для отладки:
  можно подключиться к БД через `psql` с хоста.
- **`environment`** — переменные для инициализации БД:
  - `POSTGRES_DB=pdp` — создаётся база данных `pdp`.
  - `POSTGRES_USER=postgres` — суперпользователь.
  - `POSTGRES_PASSWORD=my-password` — пароль (в production используйте секреты).
- **`healthcheck`** — проверяет готовность БД командой `pg_isready`.
  Пока БД не готова, зависимые сервисы не запустятся.
- **`restart: always`** — автоматически перезапускает контейнер при падении
  или перезагрузке хоста.

**Сервис `redis`:**

- **`image: redis:latest`** — официальный образ Redis.
- **`ports: "6379:6379"`** — пробрасывает порт.
- **`healthcheck`** — проверяет готовность Redis командой `redis-cli ping`.
  Ожидается ответ `PONG`.

**Сервис `pdp`:**

- **`image: personal-development-plan:latest`** — наш образ, собранный через
  `sbt Docker/publishLocal`.
- **`ports: "8080:8080"`** — пробрасывает порт приложения.
- **`depends_on`** — запускает сервис **только после** того, как `postgres` и `redis`
  станут healthy. Это критично: без `healthcheck` приложение могло бы стартовать
  раньше БД и упасть с ошибкой подключения.

### Что важно помнить

**1. Хост в приложении должен быть `0.0.0.0`**

По умолчанию Krop слушает `localhost` — только внутри контейнера. Чтобы приложение
было доступно с хост-машины, в `application.conf` должно быть:

```hocon
http-server-config {
    host = "0.0.0.0"
    port = 8080
}
```

Иначе при обращении к `http://localhost:8080/` с хост-машины вы получите
`Connection refused`.

**2. Приложение должно подключаться к сервисам по их именам**

В `docker-compose` все сервисы находятся в одной сети и видят друг друга
по именам. В `application.conf` должны быть:

```hocon
postgres {
    host = "postgres"     # не localhost!
    port = 5432
    database = "pdp"
    user = "postgres"
    password = "my-password"
}

redis {
    host = "redis"        # не localhost!
    port = 6379
}
```

**3. Пароли — в секретах**

В production не храните пароли в `docker-compose.yml`. Используйте
`secrets` или переменные окружения из CI/CD:

```yml
environment:
  - POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
```

И запускайте:

```bash
POSTGRES_PASSWORD=secret docker-compose up
```

## Запуск проекта

### 1. Локальная сборка Docker-образа

```bash
sbt Docker/publishLocal
```

Что происходит:

- SBT компилирует проект.
- `JavaAppPackaging` собирает дистрибутив.
- `DockerPlugin` генерирует Dockerfile и собирает образ.
- Образ получает тег `personal-development-plan:latest` (и версию).

**Проверить, что образ собрался:**

```bash
docker images | grep personal-development-plan
```

### 2. Запуск через `docker-compose`

```bash
docker-compose up
```

Что происходит:

1. Создаётся сеть по умолчанию (если её нет).
2. Запускается `postgres`, ждётся его `healthy`-состояние.
3. Запускается `redis`, ждётся его `healthy`-состояние.
4. Запускается `pdp` — наше приложение.
5. Логи всех сервисов выводятся в терминал.

**Приложение доступно по адресу:** `http://localhost:8080/`

**Проверка работы:**

```bash
curl http://localhost:8080/health_check
```

Ожидаемый ответ:

```json
{
  "redis": "Okay",
  "postgres": "Okay"
}
```

### 3. Остановка

```bash
docker-compose down
```

Что происходит:

1. Останавливаются все контейнеры.
2. Удаляются контейнеры и сеть.

**Если нужно удалить и данные:**

```bash
docker-compose down -v
```

Флаг `-v` удаляет volumes. Используйте осторожно: все данные БД будут потеряны.

### 4. Пересборка после изменений

Если изменили код:

```bash
sbt Docker/publishLocal
docker-compose up --force-recreate pdp
```

**Почему нужен `--force-recreate`?**

`docker-compose up` может использовать старый контейнер, если образ не изменился
по хешу. `--force-recreate` гарантирует, что контейнер будет пересоздан с новым образом.

## Полезные команды

| Команда                      | Что делает                               |
| ---------------------------- | ---------------------------------------- |
| `sbt Docker/publishLocal`    | Собирает Docker-образ локально           |
| `sbt Docker/stage`           | Только собирает дистрибутив (без образа) |
| `docker-compose up`          | Запускает все сервисы                    |
| `docker-compose up -d`       | Запускает в фоне                         |
| `docker-compose down`        | Останавливает и удаляет контейнеры       |
| `docker-compose down -v`     | То же + удаляет volumes                  |
| `docker-compose logs -f pdp` | Логи приложения в реальном времени       |
| `docker-compose exec pdp sh` | Заходит в контейнер                      |
| `docker-compose ps`          | Показывает статус сервисов               |

## Итог

Мы настроили полный цикл доставки приложения:

1. **Сборка Docker-образа** через sbt-native-packager.
2. **Статические ассеты** в `src/universal/assets` — попадают внутрь образа.
3. **Docker Compose** — запускает приложение вместе с PostgreSQL и Redis
   одной командой.
4. **Healthchecks** — гарантируют правильный порядок запуска: сначала БД,
   потом приложение.
5. **Проверка** — `curl /health_check` подтверждает, что всё работает.

**Ключевые архитектурные решения:**

- **`src/universal`** — стандартное место для файлов, которые должны попасть
  в дистрибутив.
- **Относительный путь `"assets"`** в `AssetRoute` — работает и локально,
  и в контейнере.
- **`depends_on` с `condition: service_healthy`** — предотвращает гонку
  при старте.
- **`dockerUpdateLatest`** — упрощает тегирование при разработке.

Теперь приложение готово к развёртыванию: на любой машине с Docker достаточно
выполнить `sbt Docker/publishLocal && docker-compose up` — и оно заработает.

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
