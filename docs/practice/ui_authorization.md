{%
showPrev = true
prevPage = main.html
showNext = true
nextPage = ui_authorization/ui_welcome.html
%}

# Главная страница

Начнём с главной страницы приложения. Это точка входа, куда попадает пользователь при открытии сайта.
Главная страница должна решать две задачи:

1. **Если пользователь уже авторизован** (в куках есть валидный JWT-токен) — показать его рабочий стол
   с задачами, разбитыми по вкладкам.
2. **Если пользователь не авторизован** — показать форму входа.

Такой подход избавляет от лишних кликов: пользователю не нужно каждый раз логиниться заново,
если он уже вошёл в систему.

## Описание маршрута главной страницы

Добавим в `Routes` новый маршрут для главной страницы. Его ключевое отличие от `/health_check` —
**извлечение HTTP-заголовка Cookie**: именно в куках хранится JWT-токен, по которому мы определяем,
залогирован ли пользователь.

```dotty
val index =
  Route(
    Request.get(Path.root).extractHeader[Cookie],
    Response.ok(Entity.html)
  )
```

**Разбор маршрута:**

- **`Request.get(Path.root)`** — GET-запрос на корневой путь `/`.
- **`.extractHeader[Cookie]`** — извлекает из запроса заголовок `Cookie`. Тип `Cookie`
  определён в http4s и содержит список всех кук, отправленных браузером. Krop автоматически
  преобразует заголовок в этот тип и передаёт его в хэндлер.
- **`Response.ok(Entity.html)`** — успешный ответ (200 OK) с HTML-телом.

## Создание `InitialHandler`

`InitialHandler` — это хэндлер, который решает, что показать пользователю: рабочий стол или форму входа.

```dotty
final case class InitialHandler(
    middleware: JwtAuthMiddleware[IO, UserInfo],
    taskLifecycleService: TaskLifecycleService[IO]
)(using Logger[IO]):
  private val defaultPage =
    View.base("План развития", View.login(none)).toString

  val handler: Handler =
    Routes.index.handleIO: (cookie: Cookie) =>
      cookie.getToken match
        case Some(token) =>
          middleware.fromToken.run(token)
            .flatMap: (user, jwtToken) =>
              getViewWelcomeHtml(
                taskLifecycleService = taskLifecycleService,
                user = user,
                jwtToken = jwtToken,
                tab = DefaultTab
              ).map: welcome =>
                View.base(
                  "План развития",
                  welcome
                ).toString
            .recoverWith:
              case ex: AuthorizationException =>
                Logger[IO].error(ex)(
                  s"Ошибка при парсинге токена: ${ex.getMessage}"
                )
                  .as(defaultPage)
              case ex =>
                Logger[IO].error(ex)(s"Ошибка: ${ex.getMessage}")
                  .as(defaultPage)
        case None =>
          defaultPage.pure[IO]
```

**Пошаговый разбор:**

1. **Зависимости хэндлера:**
    - `middleware: JwtAuthMiddleware[IO, UserInfo]` — middleware для проверки JWT-токенов.
      Умеет превращать строку токена в объект `UserInfo` (информация о пользователе: id, username).
    - `taskLifecycleService: TaskLifecycleService[IO]` — сервис для загрузки задач пользователя.
    - `using Logger[IO]` — логгер для записи ошибок.

2. **`defaultPage`** — заранее сформированная страница логина. Это «запасной вариант»:
   если пользователь не авторизован или что-то пошло не так, он увидит именно её.

3. **`Routes.index.handleIO`** — регистрируем обработчик, принимающий `Cookie`. Метод `handleIO`
   используется вместо `handle`, потому что логика требует эффекта `IO` (проверка токена, загрузка задач).

4. **`cookie.getToken`** — пытаемся извлечь токен из куки (см. следующий раздел).
   Возвращается `Option[String]`.

5. **Ветвление:**
    - **`Some(token)`** — токен найден. Передаём его в `middleware.fromToken.run(token)`,
      который возвращает пару `(UserInfo, JwtToken)`. Если токен валиден — формируем
      приветственную страницу через `getViewWelcomeHtml` и оборачиваем её в `View.base`.
      Если токен просрочен, подделан или содержит несуществующего пользователя —
      ловим `AuthorizationException` и отдаём `defaultPage`.
    - **`None`** — токена нет. Просто отдаём `defaultPage`.

6. **`.recoverWith`** — критически важная часть обработки ошибок. Любая проблема с токеном
   (истёк, повреждён, неверная подпись) **не должна ронять приложение**. Пользователь просто
   увидит форму входа, как будто он не авторизован. Ошибка при этом логируется —
   это помогает диагностировать проблемы.

## Что такое `JwtAuthMiddleware`

`JwtAuthMiddleware` — это типобезопасная обёртка над проверкой JWT-токена.
Он построен на `Kleisli` — специальном типе из Cats, который представляет функцию,
возвращающую значение в контексте эффекта `F`.

```dotty
type JwtAuthMiddleware[F[_], A] = Kleisli[F, Authorization, (A, JwtToken)]
```

**Расшифровка типа:**

- **`F[_]`** — эффект (в нашем случае `IO`).
- **`A`** — тип пользователя (у нас `UserInfo`).
- **`Authorization`** — заголовок HTTP-запроса, содержащий токен.
- **`(A, JwtToken)`** — результат: пользователь + сам токен (нужен для дальнейших запросов).

Проще говоря, `JwtAuthMiddleware[IO, UserInfo]` — это функция вида
`Authorization → IO[(UserInfo, JwtToken)]`.

```dotty
object JwtAuthMiddleware:
  def apply[F[_]: MonadThrow as MT, A](
      jwtAuth: JwtAuth,
      authenticate: JwtToken => JwtClaim => F[Option[A]]
  ): JwtAuthMiddleware[F, A] =
    Kleisli {
      case Authorization(Token(AuthScheme.Bearer, token)) =>
        val jwtToken = JwtToken(token)

        jwtDecode[F](jwtToken, jwtAuth)
          .flatMap(authenticate(jwtToken))
          .flatMap(opt =>
            MT.fromOption(opt, AuthorizationException("Not found"))
          )
          .adaptErr { case _: JwtException =>
            AuthorizationException("Invalid access token")
          }
          .map(a => (a, jwtToken))
      case _ =>
        MT.raiseError(AuthorizationException("Bearer token not found"))
    }

  extension [F[_], A](middleware: JwtAuthMiddleware[F, A])
    def fromToken: Kleisli[F, String, (A, JwtToken)] =
      middleware
        .local[String]((token: String) =>
          Authorization(Token(AuthScheme.Bearer, token))
        )
```

**Что делает `apply`:**

1. **Сопоставление с образцом заголовка `Authorization`:** ожидается схема `Bearer <token>`.
   Если заголовок не соответствует этому формату — `AuthorizationException("Bearer token not found")`.

2. **Декодирование JWT:** `jwtDecode` проверяет подпись, срок действия и целостность токена.
   Если что-то не так — выбрасывается `JwtException`, которая затем преобразуется в
   `AuthorizationException("Invalid access token")` через `.adaptErr`.

3. **Аутентификация пользователя:** функция `authenticate(jwtToken)(jwtClaim)`
   получает `JwtClaim` (полезную нагрузку токена) и должна вернуть `Option[A]`
   (например, найти пользователя в БД по `userId` из токена). Если вернётся `None` —
   выбрасывается `AuthorizationException("Not found")`.

4. **Возврат пары `(A, JwtToken)`** — аутентифицированный пользователь и сам токен.

**Расширение `fromToken`** упрощает вызов middleware, когда у нас есть только строка токена
(например, из куки). Оно «заворачивает» строку в правильный заголовок `Authorization: Bearer <token>`
и вызывает middleware.

## Считывание токена из куки

Токен хранится в куке с именем `token`. Для удобного извлечения используется
метод расширения (`extension`) на типе `Cookie`:

```dotty
object Parser:
  extension (cookie: Cookie)
    def getToken: Option[String] =
      cookie.values.collectFirst:
        case rq if rq.name == "token" => rq.content
```

**Как это работает:**

- **`cookie.values`** — список всех кук (`RequestCookie`), отправленных браузером.
- **`collectFirst`** — ищет первый элемент, удовлетворяющий предикату.
- **`case rq if rq.name == "token"`** — если имя куки равно `"token"`, возвращаем её содержимое
  (`rq.content`) — это строка JWT-токена.
- Если куки с таким именем нет — возвращается `None`.

## Формирование приветственной страницы

Если токен валиден, формируем приветственную страницу с задачами пользователя.
Логика вынесена в объект `Views`:

```dotty
object Views:
  val DefaultTab: String = "in-progress"

  def getViewWelcomeHtml(
      taskLifecycleService: TaskLifecycleService[IO],
      user: UserInfo,
      jwtToken: JwtToken,
      tab: String
  ): IO[HtmlFormat.Appendable] =
    taskLifecycleService
      .getAll(
        userId = user.id,
        maybeStatus = none,
        maybeTaskType = none,
        maybeSectionId = none
      ).map: (tasks, types, sections) =>
        val inProgressTasks =
          tasks
            .filter(_.status == Status.InProgress)
            .sortBy(t => (t.endDate, t.ordinal))
        val plannedTasks =
          tasks
            .filter(_.status == Status.Planned)
            .sortBy(_.ordinal.value)
        val knowledgeBaseTasks =
          tasks
            .filter(_.status == Status.KnowledgeBase)
            .sortBy(_.name.value)

        val plannedTaskTypeNames       = plannedTasks.map(_.taskType).toSet
        val plannedSectionNames        = plannedTasks.map(_.section).toSet
        val knowledgeBaseTaskTypeNames =
          knowledgeBaseTasks.map(_.taskType).toSet
        val knowledgeBaseSectionNames = knowledgeBaseTasks.map(_.section).toSet

        val plannedTaskTypes =
          types
            .sortBy(_.ordinal)
            .collect {
              case t if plannedTaskTypeNames(t.name.value) => t.name.value
            }
        val knowledgeBaseTaskTypes =
          types
            .sortBy(_.ordinal)
            .collect {
              case t if knowledgeBaseTaskTypeNames(t.name.value) => t.name.value
            }
        val plannedSections =
          sections
            .sortBy(_.ordinal)
            .collect {
              case t if plannedSectionNames(t.name.value) => t.name.value
            }
        val knowledgeBaseSections =
          sections
            .sortBy(_.ordinal)
            .collect {
              case t if knowledgeBaseSectionNames(t.name.value) => t.name.value
            }

        View.welcome(
          username = user.username.value,
          token = jwtToken.value,
          tab = tab,
          inProgressTasks = inProgressTasks,
          plannedTasks = plannedTasks,
          knowledgeBaseTasks = knowledgeBaseTasks,
          plannedTaskTypes = plannedTaskTypes,
          plannedSections = plannedSections,
          knowledgeBaseTaskTypes = knowledgeBaseTaskTypes,
          knowledgeBaseSections = knowledgeBaseSections
        )
```

**Что происходит пошагово:**

1. **`DefaultTab`** — вкладка, открытая по умолчанию (`"in-progress"`).

2. **Загрузка данных:** `taskLifecycleService.getAll(userId, none, none, none)`
   возвращает кортеж `(tasks, types, sections)`:
    - `tasks` — все задачи пользователя (без фильтров).
    - `types` — все типы задач пользователя.
    - `sections` — все разделы пользователя.

3. **Группировка задач по статусам:**
    - **`inProgressTasks`** — задачи «В работе», отсортированные по `(endDate, ordinal)`.
      Сначала задачи с ближайшим дедлайном, а при равенстве — по порядку.
    - **`plannedTasks`** — задачи «Запланировано», отсортированные по `ordinal`.
      Порядок задаётся пользователем вручную.
    - **`knowledgeBaseTasks`** — задачи «База знаний», отсортированные по имени.
      Алфавитный порядок для удобства поиска.

4. **Подготовка опций для фильтров.** Здесь важный момент: **фильтры должны показывать
   только те типы и разделы, которые реально встречаются в задачах данной вкладки.**
   Иначе пользователь увидит в выпадающем списке типы, которые нигде не используются,
   и это создаст путаницу.

    - **`plannedTaskTypeNames`** — `Set` имён типов, встречающихся в запланированных задачах.
    - **`plannedSectionNames`** — аналогично для разделов.
    - **`knowledgeBaseTaskTypeNames`**, **`knowledgeBaseSectionNames`** — то же для базы знаний.

5. **Формирование отсортированных списков опций:**
    - Проходим по всем `types` в порядке `ordinal` и оставляем только те, чьи имена
      встречаются в `Set`. Так фильтр показывает типы в правильном порядке (не алфавитном,
      а по `ordinal`).
    - Аналогично для разделов.

6. **`View.welcome(...)`** — рендерим Twirl-шаблон, передавая все подготовленные данные.

## Внешний вид страницы

Если всё прошло успешно, пользователь видит приветственную страницу с тремя вкладками:

![In progress tab](images/in-progress.png)

Если что-то пошло не так — пользователь видит страницу логина:

```dotty
View.base("План развития", View.login(none)).toString
```

![Login page](images/login.png)

## Шаблонизатор Twirl

Для формирования фронта используется **Twirl** — [типобезопасный шаблонизатор от Play Framework](https://www.playframework.com/documentation/3.0.x/ScalaTemplates).
Его ключевое преимущество: шаблоны компилируются в обычные Scala-функции, 
поэтому ошибки в разметке обнаруживаются **на этапе компиляции**, а не в рантайме. 
Это особенно ценно в больших проектах, где HTML-страницы разрастаются.

### Базовый шаблон `base.scala.html`

Это «обёртка» для всех страниц приложения. Он содержит общие элементы: `<head>`,
подключение CSS и JS, верхнюю панель с заголовком.

```html
@(title: String, content: Html)
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>@title</title>

    <link rel="stylesheet" href="/assets/css/pdp.css" />
    <link rel="icon" href="/assets/icons/favicon.ico" type="image/x-icon" />
    <script src="/assets/js/pdp.js"></script>
    <script src="@{"https://cdn.jsdelivr.net/npm/htmx.org@2.0.10/dist/htmx.js"}" integrity="sha384-Q+Dky3iHVJOr6wUjQ4ulh6uQ76an/t+ak1+PjMVaxRjbZamFLAG+u9InkfjbsEQf" crossorigin="anonymous"></script>
</head>
<body>
<header class="app-header">
    <div class="header-container">
        <a href="/">@title</a>
    </div>
    <div class="header-divider"></div>
</header>

<main class="main-content">
    @content
</main>
</body>
</html>
```

**Параметры:**

- **`title: String`** — заголовок страницы, отображается в браузерной вкладке и в шапке.
  Клик по нему ведёт на главную страницу.
- **`content: Html`** — основное содержимое страницы, вставляется в `<main>`.

**Важно:** здесь подключается **htmx** — библиотека для обновления частей страницы без перезагрузки.
Именно она используется на welcome-странице для переключения вкладок, отправки форм и обновления
списка задач. Без неё все действия требовали бы полной перезагрузки страницы.

### Шаблон `login.scala.html`

Форма входа. Показывается, если пользователь не авторизован.

```html
@(errorMessage: Option[String])
<div id="app" class="app-container-narrow">
   <h2>Вход в систему</h2>

   <form id="loginForm">
      <div class="form-group">
         <label for="loginUsername">Имя пользователя:</label>
         <input id="loginUsername" name="username" type="text" required/>
      </div>
      <div class="form-group">
         <label for="loginPassword">Пароль:</label>
         <input id="loginPassword" name="password" type="password" required/>
      </div>
      <button
              type="button"
              hx-post="/auth/login"
              hx-target="#app"
              hx-swap="outerHTML"
      >Войти
      </button>
   </form>

   <div class="messageBlock">
      @errorMessage.map { msg =>
      <div class="error">@msg</div>
      }
   </div>

   <p>
      Нет аккаунта?
      <a
              onclick="htmx.process(this); this.click();"
              hx-get="/register"
              hx-target="#app"
              hx-swap="outerHTML"
      >Зарегистрироваться</a>
   </p>
</div>
```

**Особенности:**

- **`errorMessage: Option[String]`** — опциональное сообщение об ошибке (например,
  «Неверный логин или пароль»). Если оно есть — отображается под формой в блоке `.error`.
- **`hx-post="/auth/login"`** — форма отправляется через htmx без перезагрузки страницы.
- **`hx-target="#app"`** — ответ сервера заменяет содержимое блока `#app`. Если логин
  успешен, сервер вернёт welcome-страницу; если нет — ту же форму с сообщением об ошибке.
- **`hx-swap="outerHTML"`** — заменяет весь блок `#app`, а не только его содержимое.
- **Ссылка на регистрацию** — тоже использует htmx: `hx-get="/register"` подгружает
  форму регистрации вместо формы входа.

## Регистрация хэндлера в Main

В конце добавляем `initialHandler` в цепочку обработчиков приложения:

```dotty
    val initialHandler =
      InitialHandler(middleware, services.taskLifecycleService)

    initialHandler.handler
      .orElse(healthHandler.handler)
      .orElse(assets)
      .orElse(Application.notFound)
```

**Порядок важен:**

1. **`initialHandler.handler`** — обрабатывает `/` (главную страницу).
2. **`healthHandler.handler`** — обрабатывает `/health_check`.
3. **`assets`** — отдаёт статические файлы (`/assets/css/pdp.css`, `/assets/js/pdp.js`,
   `/assets/icons/favicon.ico`).
4. **`Application.notFound`** — запасной вариант: если ничего не подошло, возвращаем 404.

Krop пробует обработчики по очереди через `orElse` и останавливается на первом,
который «подошёл» к запросу. Поэтому `notFound` всегда должен быть последним,
а ассеты — до него, чтобы статика отдавалась корректно.

## Итог

Мы настроили главную страницу приложения с проверкой авторизации через JWT-токен в куках.
Если пользователь авторизован — он видит свой рабочий стол с задачами, разбитыми по вкладкам.
Если нет — форму входа с возможностью перейти к регистрации. Вся логика обработки ошибок
изолирована: проблемы с токеном не роняют приложение, а мягко приводят пользователя к форме логина.

Ключевые идеи:

- **JWT в куках** — токен хранится в HTTP-куке `token`, что позволяет автоматически
  подставлять его при каждом запросе к тому же домену.
- **Middleware для проверки** — `JwtAuthMiddleware` инкапсулирует всю логику проверки
  токена: декодирование, валидацию подписи, поиск пользователя.
- **Формирование контекстных фильтров** — фильтры показывают только те типы и разделы,
  которые реально встречаются в задачах вкладки.
- **htmx для интерактивности** — переключение вкладок, открытие модальных окон и отправка
  форм происходят без перезагрузки страницы.
- **Twirl для шаблонов** — типобезопасные HTML-шаблоны, проверяемые компилятором.

---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
