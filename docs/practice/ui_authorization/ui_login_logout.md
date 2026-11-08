# Страницы входа и выхода

После того как мы настроили главную страницу с проверкой авторизации, нужно реализовать два ключевых
эндпоинта: **вход в систему** (`/auth/login`) и **выход из системы** (`/auth/logout`).
Логин проверяет учётные данные пользователя и выдаёт JWT-токен, а logout инвалидирует токен
(в нашем случае — удаляет его из Redis), чтобы им нельзя было воспользоваться повторно.

## Вход в систему (Login)

### Описание маршрута

Маршруты теперь объявляются непосредственно в хэндлерах. Для логина маршрут объявлен как приватная переменная `route` в `LoginHandler`:

```dotty
private val route = Route(
  Request.post(Path.root / "auth" / "login")
    .withEntity(Entity.formOf[LoginRequest]),
  Response.ok(Entity.html)
)
```

**Разбор маршрута:**

- **`Request.post(Path.root / "auth" / "login")`** — POST-запрос на путь `/auth/login`.
  Логин всегда POST, потому что мы передаём чувствительные данные (пароль) в теле запроса.
- **`.withEntity(Entity.formOf[LoginRequest])`** — тело запроса имеет тип `application/x-www-form-urlencoded`
  и автоматически десериализуется в `LoginRequest`. Это стандартный формат для HTML-форм,
  который отправляет htmx по умолчанию. Krop с помощью `FormCodec[LoginRequest]`
  (кодек, который мы описывали в разделе про модели) превращает пары `username=...&password=...`
  в типизированный объект.
- **`Response.ok(Entity.html)`** — успешный ответ (200 OK) с HTML-телом. Все варианты ответа
  (успех или форма с ошибкой) генерируются внутри хэндлера, поэтому `.orNotFound` здесь не нужен.

### Хэндлер `LoginHandler`

```dotty
final case class LoginHandler(
    authService: AuthService[IO],
    taskLifecycleService: TaskLifecycleService[IO]
)(using Logger[IO]):
  private val route = Route(
    Request.post(Path.root / "auth" / "login")
      .withEntity(Entity.formOf[LoginRequest]),
    Response.ok(Entity.html)
  )

  // Нельзя раскрывать пользователю ошибку валидации.
  private val fallbackPage: String =
    View.login("Некорректный логин или пароль".some).toString

  val handler: Handler =
    route.handleIO: (request: LoginRequest) =>
      UserRegistration.parse(request)
        .map { registration =>
          authService
            .login(registration.username, registration.password)
            .flatMap: (token, id) =>
              getViewWelcome(
                taskLifecycleService = taskLifecycleService,
                user = UserInfo(id, registration.username),
                jwtToken = token,
                tab = DefaultTab
              )
            .recoverWith:
              case UserNotFound(_) | InvalidPassword(_) =>
                fallbackPage.pure[IO]
              case ex =>
                Logger[IO].error(ex)(s"Ошибка: ${ex.getMessage}")
                  .as(fallbackPage)
        }.valueOr(_ => fallbackPage.pure[IO])
end LoginHandler
```

**Пошаговый разбор:**

1. **Зависимости:**
    - `authService: AuthService[IO]` — сервис аутентификации. Умеет проверять логин/пароль
      и генерировать JWT-токен.
    - `taskLifecycleService: TaskLifecycleService[IO]` — сервис для загрузки задач пользователя.
      Нужен, чтобы сразу после логина показать приветственную страницу с задачами.
    - `using Logger[IO]` — логгер для записи ошибок.

2. **`private val route`** — маршрут объявлен внутри хэндлера.

3. **`fallbackPage`** — приватная `val` с HTML-страницей логина и сообщением
   «Некорректный логин или пароль». Это **единственная** страница-заглушка, которая
   возвращается при любой ошибке (неверный пароль, неизвестный логин, ошибка валидации,
   внутренняя ошибка). Сообщение одинаковое — чтобы не раскрывать, существует ли пользователь.

4. **`UserRegistration.parse(request)`** — валидируем поля запроса. Раньше для этого был
   отдельный расширяющий объект `Parser` с методом `request.parse`, но в `f186beb` его **удалили**,
   а логика переехала в метод компаньона `UserRegistration.parse`. Возвращает
   `ValidatedNec[String, UserRegistration]`:
   ```dotty
   object UserRegistration:
     def parse(request: LoginRequest): ValidatedNec[String, UserRegistration] =
       (
         UserName.either(request.username).left.map(er =>
           s"Логин: $er"
         ).toValidatedNec,
         Password.either(request.password).left.map(er =>
           s"Пароль: $er"
         ).toValidatedNec
       ).mapN((username, password) =>
         UserRegistration(username = username, password = password)
       )
   ```
    - Если поля пустые или не соответствуют ограничениям — возвращается ошибка.
    - Если всё в порядке — возвращается `UserRegistration(username, password)`.

5. **`.map { registration => ... }`** — если валидация прошла успешно, получаем
   `UserRegistration` и вызываем `authService.login(registration.username, registration.password)`.

6. **`authService.login`** — проверяет пользователя в БД:
    - Если пользователь найден и пароль верный — возвращает `(JwtToken, UserId)`.
    - Если пользователь не найден — выбрасывает `UserNotFound`.
    - Если пароль неверный — выбрасывает `InvalidPassword`.

7. **`.flatMap: (token, id) => ...`** — при успехе формируем приветственную страницу
   через `getViewWelcome` (та же функция, что использовалась в `InitialHandler`).
   Результат — это уже готовая HTML-строка.

8. **`.recoverWith`** — обработка ошибок:
    - **`UserNotFound` / `InvalidPassword`** — возвращаем `fallbackPage` (форму логина
      с сообщением «Некорректный логин или пароль»).
    - **Любое другое исключение** — логируем и тоже возвращаем `fallbackPage`.

9. **`.valueOr(_ => fallbackPage.pure[IO])`** — обрабатывает случай, когда `UserRegistration.parse`
   вернул ошибку. Например, если пользователь отправил пустую форму. В этом случае тоже
   показываем форму логина с сообщением об ошибке.

**Возвращаемый тип `IO[String]`:**

Хэндлер возвращает `IO[String]` — всегда готовая HTML-строка. Раньше тип был
`IO[Option[String]]` с `None` для 404, но теперь все ошибки намеренно возвращают форму
логина с сообщением, поэтому `Option` больше не нужен.

### Обработка ошибок

Если пользователь ввёл неверный логин или пароль, он увидит форму входа с сообщением об ошибке:

![Error](../images/login_error.png)

Сообщение формируется в `fallbackPage`:

```dotty
private val fallbackPage: String =
  View.login("Некорректный логин или пароль".some).toString
```

Обратите внимание: сообщение **одинаково** и для несуществующего пользователя, и для
неверного пароля. Это осознанное решение для безопасности: злоумышленник не может
перебором логинов определить, какие пользователи зарегистрированы в системе.

## Выход из системы (Logout)

### Описание маршрута

Маршрут объявлен внутри `LogoutHandler`:

```dotty
private val route = Route(
  Request.post(Path.root / "auth" / "logout").extractHeader[Authorization],
  Response.ok(Entity.html).orNotFound
)
```

**Разбор маршрута:**

- **`Request.post(Path.root / "auth" / "logout")`** — POST-запрос на `/auth/logout`.
  Выход должен быть POST, а не GET, потому что это изменяющая операция (инвалидация токена).
  GET-запросы должны быть идемпотентными и не менять состояние.
- **`.extractHeader[Authorization]`** — извлекает HTTP-заголовок `Authorization` из запроса.
  Именно там передаётся JWT-токен: `Authorization: Bearer <token>`.
- **`.orNotFound`** — запасной вариант: 404, если запрос не совпал.

### Хэндлер `LogoutHandler`

```dotty
final case class LogoutHandler(
    authService: AuthService[IO],
    middleware: JwtAuthMiddleware[IO, UserInfo]
)(using Logger[IO]) extends AuthorizationHandler(middleware):
  private val route = Route(
    Request.post(Path.root / "auth" / "logout").extractHeader[Authorization],
    Response.ok(Entity.html).orNotFound
  )

  val handler: Handler =
    route.handleIO: (authorization: Authorization) =>
      withUserFromHeader(authorization): (user: UserInfo, token: JwtToken) =>
        authService.logout(token, user.username)
          .as(View.login(none).toString)
```

**Пошаговый разбор:**

1. **`extends AuthorizationHandler(middleware)`** — наследуемся от базового класса
   `AuthorizationHandler`, который инкапсулирует логику проверки токена. Это избавляет
   от необходимости вручную извлекать токен из заголовка и валидировать его в каждом хэндлере.

2. **`withUserFromHeader(authorization): (user: UserInfo, token: JwtToken) => ...`** — специальный
   метод, который принимает **два** аргумента:
    - Первый — `authorization` из заголовка запроса (то, что извлекла `handleIO`).
    - Второй (каррированная лямбда) — `(user, token)`.
   
   Метод:
    - Проверяет токен через middleware.
    - Если токен валиден — передаёт в лямбду `user` (информация о пользователе)
      и `token` (сам JWT-токен).
    - Если невалиден — автоматически возвращает ошибку `AuthorizationException`,
      которую обрабатывает `AuthorizationHandler`.

3. **`authService.logout(token, user.username)`** — инвалидирует токен. В нашей реализации
   это означает удаление токена из Redis (хранилища активных сессий).
   После этого токен нельзя будет использовать повторно.

4. **`.as(View.login(none).toString)`** — после успешного выхода возвращаем страницу логина.
   `none` означает, что сообщения об ошибке нет.

**Возвращаемый тип `IO[String]`:**

Здесь хэндлер возвращает `IO[String]` — просто HTML-страницу логина. После выхода
пользователь видит форму входа, как будто он только что зашёл на сайт.

### Базовый класс `AuthorizationHandler`

`AuthorizationHandler` — это вспомогательный трейт, который инкапсулирует логику
проверки токена. Он используется всеми защищёнными хэндлерами (настройки, задачи, таймер и т.д.).

Типичная реализация выглядит так:

```dotty
trait AuthorizationHandler(middleware: JwtAuthMiddleware[IO, UserInfo])(using
    Logger[IO]
):
  def withUserFromHeader(authorization: Authorization)(
      f: ((UserInfo, JwtToken)) => IO[String]
  ): IO[Option[String]] =
    withUserFromHeaderOpt(authorization)(f(_).map(_.some))

  def withUserFromHeaderOpt(authorization: Authorization)(
      f: ((UserInfo, JwtToken)) => IO[Option[String]]
  ): IO[Option[String]] =
    middleware
      .run(authorization)
      .flatMap(f)
      .recoverWith:
        case ex: AuthorizationException =>
          Logger[IO].error(ex)(
            s"Ошибка при парсинге токена: ${ex.getMessage}"
          ).as(View.error().toString.some)
        case ex =>
          Logger[IO].error(ex)(s"Ошибка: ${ex.getMessage}").as(none)

  def withUserFromCookieOpt(cookie: Cookie)(
      f: ((UserInfo, JwtToken)) => IO[Option[String]],
      fallbackPage: => String
  ): IO[String] =
    withUserFromCookie(cookie: Cookie)(
      f(_).map(_.getOrElse(fallbackPage)),
      fallbackPage
    )

  def withUserFromCookie(cookie: Cookie)(
      f: ((UserInfo, JwtToken)) => IO[String],
      fallbackPage: => String
  ): IO[String] =
    cookie.values
      .collectFirst {
        case rq if rq.name == "token" => rq.content
      } match
      case Some(token) =>
        middleware.fromToken.run(token)
          .flatMap(f)
          .recoverWith:
            case ex: AuthorizationException =>
              Logger[IO].error(ex)(
                s"Ошибка при парсинге токена: ${ex.getMessage}"
              )
                .as(fallbackPage)
            case ex =>
              Logger[IO].error(ex)(s"Ошибка: ${ex.getMessage}")
                .as(fallbackPage)
      case None =>
        Logger[IO].debug("В cookies не найден токен пользователя")
          .as(fallbackPage)
end AuthorizationHandler
```

**Что делает:**

1. Все методы принимают **два** аргумента: исходные данные (`authorization` или `cookie`)
   **и** каррированную функцию `f`, которой передаётся `(user, token)`.
2. Метод вызывает middleware для проверки токена.
3. Если токен валиден — передаёт `(user, token)` в пользовательскую функцию `f`.
4. Если заголовка/куки нет или токен невалиден — возвращается `fallbackPage`.

**Два способа передать токен:**

- **`withUserFromHeader` / `withUserFromHeaderOpt` с `Authorization`** — токен приходит
  в заголовке `Authorization: Bearer <token>` (формы `/auth/login`, `/auth/logout`,
  действия задачи). Возвращают `IO[Option[String]]`.
- **`withUserFromCookie` / `withUserFromCookieOpt` с `Cookie`** — токен хранится в куки
  `token` (открытие страниц: `/home`, `/task/{uuid}`, `/note/{uuid}` и т.д.). Трейт сам
  извлекает токен из куки и возвращает `IO[String]`, подставляя `fallbackPage` при
  отсутствии/невалидности токена. `fallbackPage` — **по имени** (`=> String`), то есть
  вычисляется только при необходимости. Для мягкой версии (`withUserFromCookieOpt`) лямбда
  возвращает `Option`, и при `None` тоже подставляется `fallbackPage`.

**Преимущества такого подхода:**

- **Единообразие**: все защищённые хэндлеры используют один и тот же механизм проверки.
- **Безопасность по умолчанию**: если разработчик забыл проверить токен — компилятор
  заставит его использовать `withUserFromHeader`, потому что обычный `handleIO` не даст доступ
  к `UserInfo`.
- **Единая точка для токена из куки**: извлечение `token` из куки и подстановка
  `fallbackPage` реализованы один раз в `AuthorizationHandler`, а не в каждом хэндлере.

## Регистрация хэндлеров в Main

После создания `LoginHandler` и `LogoutHandler` добавляем их в цепочку обработчиков:

```dotty
val loginHandler  = LoginHandler(authService, services.taskLifecycleService)
val logoutHandler = LogoutHandler(authService, middleware)

initialHandler.handler
  .orElse(loginHandler.handler)
  .orElse(logoutHandler.handler)
  .orElse(healthHandler.handler)
  .orElse(assets)
  .orElse(Application.notFound)
```

## Итог

Мы реализовали два ключевых эндпоинта для аутентификации:

1. **`/auth/login`** — принимает логин и пароль в формате `application/x-www-form-urlencoded`,
   проверяет их через `AuthService`, генерирует JWT-токен и показывает приветственную
   страницу с задачами. При ошибке показывает форму логина с сообщением.

2. **`/auth/logout`** — принимает JWT-токен в заголовке `Authorization`, инвалидирует его
   через `AuthService` (удаляет из Redis), после чего возвращает страницу логина.

Оба эндпоинта используют htmx для замены содержимого страницы без перезагрузки:
клиент отправляет запрос, сервер возвращает HTML, htmx подставляет его в `#app`.

**Ключевые архитектурные решения:**

- **Форма логина** не различает «пользователь не найден» и «неверный пароль» — это защита
  от перебора логинов.
- **`AuthorizationHandler`** инкапсулирует проверку токена и делает код защищённых
  хэндлеров лаконичным.
- **Единый интерфейс `Handler`** — упрощает их композицию через `orElse`.

---

**Ссылки:**

- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
