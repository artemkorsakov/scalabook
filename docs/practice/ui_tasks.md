{%
showPrev = true
prevPage = authorization/create.html
showNext = true
nextPage = api/users_routes.html
%}

# Страница задачи



## Возврат на домашнюю страницу

Когда пользователь уже имеет аккаунт и хочет войти, он нажимает на ссылку «Войти».
Это отправляет GET-запрос на `/home/{tab}`, где `{tab}` — желаемая вкладка (по умолчанию `"in-progress"`).

### Маршрут

```dotty
val home =
  Route(
    Request.get(Path.root / "home" / Param.string).extractHeader[Cookie],
    Response.ok(Entity.html)
  )
```

**Разбор маршрута:**

- **`Request.get(Path.root / "home" / Param.string)`** — GET-запрос на `/home/{tab}`,
  где `{tab}` — строковый параметр пути. Например, `/home/in-progress` или `/home/planned`.
- **`.extractHeader[Cookie]`** — извлекает куки, чтобы проверить, авторизован ли пользователь.
- **`Response.ok(Entity.html)`** — успешный ответ с HTML.

### Хэндлер `HomeHandler`

```dotty
final case class HomeHandler(
    middleware: JwtAuthMiddleware[IO, UserInfo],
    taskLifecycleService: TaskLifecycleService[IO]
)(using Logger[IO]):
  private val defaultPage = View.login(None).toString

  val handler: Handler =
    Routes.home.handleIO: (tab: String, cookie: Cookie) =>
      cookie.getToken match
        case Some(token) =>
          middleware.fromToken.run(token)
            .flatMap: (user, token) =>
              getViewWelcome(
                taskLifecycleService = taskLifecycleService,
                user = user,
                jwtToken = token,
                tab = tab
              )
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

**Почему нужен отдельный хэндлер?**

Обычный `InitialHandler` обрабатывает только корневой путь `/` и всегда открывает вкладку `DefaultTab`.
Но когда пользователь нажимает на ссылку «Войти» со страницы регистрации
или после успешного создания задачи (будет разобрано позднее), необходимо вернуться на конкретную вкладку
(например, если задача создается на вкладке «Запланировано» и надо вернуться именно туда).
`HomeHandler` принимает параметр `tab` из URL и открывает именно эту вкладку.

Кроме того, этот хэндлер возвращает **только контейнер `#app`**, а не всю страницу
(потому что `hx-target="#app"` и `hx-swap="outerHTML"`). Это важно: мы не перезагружаем
всю страницу, а только заменяем её центральную часть.

**Логика:**

1. **`cookie.getToken`** — пытаемся извлечь токен из куки.
2. Если токен найден — проверяем его через middleware и формируем страницу welcome с указанной вкладкой.
3. Если токена нет или он невалиден — показываем форму входа (`defaultPage`).



---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
