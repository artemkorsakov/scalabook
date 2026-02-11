{%
showPrev = true
prevPage = complete.html
showNext = true
nextPage = ../api.html
%}

# Авторизация

Предоставим возможность пользователям регистрироваться и войти в систему.  
Это позволит им работать с приложением над своими задачами.  
Видеть только свои задачи, свои цели и прогресс по задачам.

Это позволит использовать приложение в качестве инструмента для личного развития для каждого пользователя одновременно.

## Пользователи системы

Наша система должна иметь возможность хранить основную информацию о пользователях,  
такую как имена пользователей и зашифрованные пароли.

```dotty  
// pdp/src/main/scala/ru/scalabook/pdp/model/User.scala
package ru.scalabook.pdp.model

import java.util.UUID
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import ru.scalabook.pdp.model.User.*
import ru.scalabook.sql.*
import ru.scalabook.sql.ColumnDefinition.{ColumnDefault, ColumnName, ColumnType}
import ru.scalabook.sql.TableSchema.{Columns, TableName}

/** Пользователь системы */
final case class User(
    id: UUID,
    username: UserName,
    password: EncryptedPassword
)

object User:
  type UserName = UserName.T
  object UserName
      extends RefinedType[String, MinLength[2] & MaxLength[20] & Trimmed]

  type Password = Password.T
  object Password
      extends RefinedType[String, MinLength[8] & MaxLength[100] & Trimmed]

  type EncryptedPassword = EncryptedPassword.T
  object EncryptedPassword
      extends RefinedType[String, MinLength[8] & MaxLength[100] & Trimmed]

  /** Неявная реализация TableSchema для типа User. Описывает, как сущность User
    * должна быть отображена в таблицу БД.
    */
  given TableSchema[UserType] with
    def tableName: TableName = TableName("users")

    def columns: Columns =
      Columns(
        ColumnDefinition(
          ColumnName.Id,
          ColumnType.UUID
        ).asPK().default(ColumnDefault.UUID),
        ColumnDefinition(
          ColumnName("name"),
          ColumnType.Varchar
        ).asNotNull(),
        ColumnDefinition(
          ColumnName("password"),
          ColumnType.Varchar
        ).asNotNull()
      )
end User 
```  

### Сервис авторизации
  
```dotty 
// pdp/src/main/scala/ru/scalabook/pdp/services/AuthService.scala 
package ru.scalabook.pdp.services

import dev.profunktor.auth.jwt.JwtToken
import ru.scalabook.pdp.model.User
import ru.scalabook.pdp.model.User.*

trait AuthService[F[_]]:

  def findUser(token: JwtToken): F[Option[User]]

  def newUser(username: UserName, password: Password): F[JwtToken]

  def login(username: UserName, password: Password): F[JwtToken]

  def logout(token: JwtToken, username: UserName): F[Unit]
```  

Токены аутентификации также будут храниться в Redis, 
поскольку они должны быть аннулированы через определенное время.

### Тестирование сервисов






## Проверка работоспособности

Нашему приложению необходимо сообщать о своем состоянии работоспособности, 
что обычно включает в себя, помимо прочего, проверку подключения к базе данных. 
Мы можем смоделировать это следующим образом.

```dotty  
// pdp/src/main/scala/ru/scalabook/pdp/model/AppStatus.scala
package ru.scalabook.pdp.model

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
// pdp/src/main/scala/ru/scalabook/pdp/services/HealthCheck.scala
package ru.scalabook.pdp.services

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
