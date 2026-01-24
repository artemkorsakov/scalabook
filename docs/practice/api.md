{%
showPrev = true
prevPage = lifecycle.html
showNext = true
nextPage = sources.html
%}

# Определение клиентского API и создание сервиса


## Авторизация

Предоставим возможность пользователям регистрироваться и войти в систему.
Это позволит им работать с приложением над своими задачами.
Видеть только свои задачи, свои цели и прогресс по задачам.

Это позволит использовать приложение в качестве инструмента для личного развития для каждого пользователя одновременно.

Наша система должна иметь возможность хранить основную информацию о пользователях,
такую как имена пользователей и зашифрованные пароли.

```dotty
final case class User(  
    id: UUID,  
    username: UserName,  
    password: EncryptedPassword  
)

private[services] trait AuthService[F[_]]:  
  
  def findUser(token: JwtToken): F[Option[User]]  
  
  def newUser(username: UserName, password: Password): F[JwtToken]  
  
  def login(username: UserName, password: Password): F[JwtToken]  
  
  def logout(token: JwtToken, username: UserName): F[Unit]
```

Для быстрого доступа мы будем хранить корзину покупок в Redis, которая не должна сохраняться в течение одной сессии. Кроме того, токены аутентификации также будут храниться в нашем кэше, поскольку они должны быть аннулированы через определенное время.

#### Проверка работоспособности

Нашему приложению необходимо сообщать о своем состоянии работоспособности, что обычно включает в себя, помимо прочего, проверку подключения к базе данных. Мы можем смоделировать это следующим образом.

```dotty
import ru.scalabook.pdp.model.AppStatus.*  
  
final case class AppStatus(redis: RedisStatus, postgres: PostgresStatus)  
  
object AppStatus:  
  opaque type RedisStatus    = Status  
  opaque type PostgresStatus = Status  
  
  enum Status(isOk: Boolean):  
    case Okay()        extends Status(true)  
    case Unreachable() extends Status(false)
```

API:

```dotty
import ru.scalabook.pdp.model.AppStatus  
  
trait HealthCheck[F[_]]:  
  def status: F[AppStatus]
```



---

**Ссылки:**

- [Персональный план развития][plugin]
- [Репозиторий с исходниками][repo]

[repo]: https://gitverse.ru/scalabook/pdp
[plugin]: https://artemkorsakov.github.io/personal-development-plan/ru/
