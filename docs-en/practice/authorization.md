# Authorization

The authorization system is a key component of the application, allowing users to create
personal workspaces and manage their tasks in a secure and isolated environment.

## Functional Purpose

Implementing a registration and authentication mechanism gives users the following opportunities:

- **Creating a personal account** with a unique identifier, 
  which allows the system to segregate data between different users
- **Secure login** with credential verification
- **Isolation of user data** - each authorized user works exclusively with their own tasks, 
  goals, and progress, without access to others' data
- **Multi-user mode** - the application can serve multiple users simultaneously, 
  each using it as a personal tool for self-improvement

## User Data Model

The accounts storage system is designed with security requirements in mind and includes the following key aspects:

### Secure Password Storage

The database stores only **password hashes**, not the passwords themselves in plain text. 
This means that even in the event of a database breach, attackers will not gain access to users' real passwords. 
During authentication, the system verifies the entered password by hashing it using the same algorithm and comparing it with the stored value.

### Data Classes

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/models/UserInfo.scala
// Public user information (without sensitive data)
final case class UserInfo(
    id: UserId,
    username: UserName
) derives Decoder, Encoder

// pdp/src/main/scala/ru/scalabook/pdp/models/User.scala
// Full user model in the system
// Stores all data, including password hash
final case class User(
    id: UserId,
    username: UserName,
    passwordHash: EncryptedPassword
)
```  

### Data Validation with Refined Types

To ensure type-level data integrity, refined types with clear constraints are used:

- `UserId`: opaque refined type
- `UserName`: string of 2 to 20 characters, trimmed
- `Password`: raw password of 8 to 100 characters, trimmed
- `EncryptedPassword`: encrypted password (hash)

```dotty
object User:
  type UserId = UserId.T
  object UserId extends PdpUUID

  type UserName = UserName.T
  object UserName
      extends RefinedType[String, MinLength[2] & MaxLength[20] & Trimmed]

  type Password = Password.T
  object Password
      extends RefinedType[String, MinLength[8] & MaxLength[100] & Trimmed]

  type EncryptedPassword = EncryptedPassword.T
  object EncryptedPassword
      extends RefinedType[String, Not[Empty] & Trimmed]
``` 

## User Data Store Service

### Abstract Interface

Following the services for working with tasks and sections, we create an abstract service for managing user accounts:

```dotty
trait UsersService[F[_]]:
  /** Creating a new user */
  def create(username: UserName, passwordHash: EncryptedPassword): F[UserId]

  /** Getting a user by ID */
  def get(id: UserId): F[Option[User]]

  /** Finding a user by name */
  def find(username: UserName): F[Option[User]]

  /** Updating user data */
  def update(user: User): F[Boolean]

  /** Deleting a user */
  def delete(id: UserId): F[Boolean]
end UsersService
```

### Mock Implementation for Testing

To verify functionality, we create a mock implementation:

```dotty
// pdp/src/main/scala/ru/scalabook/pdp/services/mock/MockUsersService.scala
private[services] class MockUsersService[F[_]: Sync](
    users: Ref[F, Map[UserId, User]]
) extends UsersService[F]:
  def create(username: UserName, passwordHash: EncryptedPassword): F[UserId] =
    val id   = UserId(UUID.randomUUID())
    val user = User(id, username, passwordHash)
    users.update(_ + (id -> user)).as(id)

  def get(id: UserId): F[Option[User]] =
    users.get.map(_.values.find(_.id == id))

  def find(username: UserName): F[Option[User]] =
    users.get.map(_.values.find(_.username == username))

  def update(user: User): F[Boolean] =
    users.update(_ + (user.id -> user)).as(true)

  def delete(id: UserId): F[Boolean] =
    users.update(_ - id).as(true)
end MockUsersService

object MockUsersService:
  def make[F[_]: Sync]: Resource[F, UsersService[F]] =
    Resource.eval:
      Ref[F].of(Map.empty[UserId, User]).map: users =>
        new MockUsersService(users)
```

### PostgreSQL Implementation

The production implementation uses Doobie for database access:

```dotty
private[pdp] class PostgreSQLUsersService[F[_]: MonadCancelThrow](
    xa: Transactor[F]
) extends UsersService[F]:

  def create(username: UserName, passwordHash: EncryptedPassword): F[UserId] =
    sql"""INSERT INTO users (name, password) 
          VALUES ($username, $passwordHash) 
          RETURNING id"""
      .query[UserId]
      .unique
      .transact(xa)

  def get(id: UserId): F[Option[User]] =
    sql"""SELECT id, name, password FROM users WHERE id = $id"""
      .query[User].option.transact(xa)

  def find(username: UserName): F[Option[User]] =
    fr"SELECT id, name, password FROM users WHERE name = $username LIMIT 1"
      .query[User]
      .option
      .transact(xa)

  def update(user: User): F[Boolean] =
    sql"""UPDATE users SET name = ${user.username}, password = ${user.passwordHash} WHERE id = ${user.id}"""
      .update.run.transact(xa).map(_ > 0)

  def delete(id: UserId): F[Boolean] =
    sql"""DELETE FROM users WHERE id = $id"""
      .update.run.transact(xa).map(_ > 0)
```

## Key Architectural Decisions

1. **Type-level safety** - using refined types ensures data validity at compile time
2. **Separation of concerns** - separate classes for different operations (registration, login, public data)
3. **Password hashing** - only hashes are stored in the database, minimizing damage from potential data breaches
4. **Effect abstraction** - using `F[_]` allows easy changing of the effect context
5. **Declarative mapping** - table schema description is separate from data access logic

This architecture provides a secure and scalable authorization system, 
ready for multi-user mode.

## Authorization Service

The authorization service is the central component of the security system, 
responsible for user authentication and session management. 
Let's examine its architecture, internal mechanisms, and interaction with other components.

### Authorization Service Interface

```dotty
trait AuthService[F[_]]:
  /** Registering a new user */
  def newUser(username: UserName, password: Password): F[(SignedJwt, UserId)]

  /** Logging in an existing user */
  def login(username: UserName, password: Password): F[(SignedJwt, UserId)]

  /** Logging out (token invalidation) */
  def logout(token: SignedJwt, username: UserName): F[Boolean]

/** Service for validating users by JWT tokens. Used in middleware
  * for request authentication
  */
trait UsersAuthService[F[_], A]:
  /** Finding a user by JWT token */
  def findUser(token: SignedJwt): F[Option[A]]
```

The `SignedJwt` type comes from the [Jots][jots] library — a purely functional JWT module
built on Cats Effect.

### Token Management

JWT tokens are used to maintain user sessions. 
They are stored in Redis with a limited lifetime for the ability to force session termination.

For working with Redis we will use the [Redis4Cats][redis4cats] library — a purely functional 
and asynchronous Redis client built on Cats Effect, Fs2, and the Java library Lettuce.

Adding dependencies:

```dotty
object redis4cats {
  private val version = "2.0.6"
  val effects         = "dev.profunktor" %% "redis4cats-effects"  % version
  val log4cats        = "dev.profunktor" %% "redis4cats-log4cats" % version
}

val pdpLibs: Seq[ModuleID] =
  Seq(
    // ...
    redis4cats.effects,
    redis4cats.log4cats,
    // ...
  )
```

#### Token Generation

The `Tokens` component is responsible for creating JWT tokens. The [Jots][jots] library 
provides a type-safe API: claims are described as a regular case class, 
and signing is performed by the `JwtSigning[F]` service.

```dotty
// Token claims (payload)
final case class TokenClaims(
    userId: UUID,
    issuedAt: Long,
    expiresAt: Long
)

object TokenClaims:
  given Encoder.AsObject[TokenClaims] =
    Encoder.forProduct3("userId", "iat", "exp")(claims =>
      (claims.userId.toString, claims.issuedAt, claims.expiresAt)
    )

  given JwtEncoder[TokenClaims] =
    JwtEncoder.encodeClaims
end TokenClaims

trait Tokens[F[_]]:
  /** Creating a new JWT token
    *
    * @return
    *   the generated JWT token
    */
  def create: F[SignedJwt]

object Tokens:
  def make[F[_]: {GenUUID, Sync}](
      jwtSigning: JwtSigning[F], // Token signing service
      expiration: FiniteDuration // Token lifetime
  ): Tokens[F] =
    new Tokens[F]:
      def create: F[SignedJwt] =
        for
          uuid <- GenUUID[F].make
          now  <- Sync[F].delay(Instant.now().getEpochSecond)
          claims = TokenClaims(uuid, now, now + expiration.toSeconds)
          token <- claims.asJwt.signWith(jwtSigning)
        yield token
  end make
end Tokens
```

#### Configuration

Configuration constants are moved to the external file `application.conf`:

```conf
// pdp/src/main/resources/application.conf

token = {
  jwt-access-token-key = "5h0pp1ng_k4rt"
  expiration = 60.minutes
}
```

The following new parameters are defined in the config file:

```dotty
type JwtAccessTokenKeyConfig = JwtAccessTokenKeyConfig.T

object JwtAccessTokenKeyConfig extends RefinedType[String, Not[Empty] & Trimmed]

final case class TokenConfig(
    jwtAccessTokenKey: Secret[JwtAccessTokenKeyConfig],
    expiration: FiniteDuration
) derives ConfigReader

object TokenConfig:
  given ConfigReader[Secret[JwtAccessTokenKeyConfig]] =
    ConfigReader[JwtAccessTokenKeyConfig].map(Secret.apply)

/** `Config` — root configuration object. */
final case class Config(
    httpServerConfig: HttpServerConfig,
    postgresql: PostgreSQLConfig,
    redis: RedisConfig,
    token: TokenConfig
) derives ConfigReader
```

#### Setting Up Signing and Verification

The [Jots][jots] library manages token timestamps itself, so two builders handle signing 
and verification: `JwtSigningBuilder` and `JwtVerificationBuilder`.

```dotty
for
  crypto <- Crypto.make[F]
  secretKeyValue = cfg.token.jwtAccessTokenKey.value.value
  secretKey      <- SecretKey(secretKeyValue).liftTo[F]

  // Token signing service (used in Tokens.create)
  userJwtSigning <- JwtSigningBuilder
    .default[F]
    .hmac(HS256, secretKey)
    .build

  // Signature verification service (used in JwtAuthMiddleware)
  userJwtVerification <- JwtVerificationBuilder
    .default[F]
    .hmac(HS256, secretKey)
    .build

  val tokens = Tokens.make[F](userJwtSigning, cfg.token.expiration)
  ...
yield new Security[F](userJwtVerification, auth, usersAuth) {}
```

- **`SecretKey(secretKeyValue)`** — utility to lift a string key into the safe type `SecretKey`.
- **`JwtSigningBuilder.default[F].hmac(HS256, secretKey).build`** — builds `JwtSigning[F]`
  for signing tokens using HMAC-SHA256.
- **`JwtVerificationBuilder...build`** — builds `JwtVerification[F]` for signature verification.
- Both builders use **the same secret key** — important for consistency 
  between signing and verification.

#### Unique ID Generation

A UUID generator is used to create unique token IDs:

```dotty
trait GenUUID[F[_]]:
  def make: F[UUID]

  def read(str: String): F[UUID]

object GenUUID:
  def apply[F[_]: GenUUID]: GenUUID[F] = summon

  given forSync[F[_]: Sync]: GenUUID[F] =
    new GenUUID[F]:
      def make: F[UUID] = Sync[F].delay(UUID.randomUUID())

      def read(str: String): F[UUID] =
        ApplicativeThrow[F].catchNonFatal(UUID.fromString(str))
```

### Cryptographic Operations

Password security is ensured by the `Crypto` component, which uses the BCrypt algorithm:

```dotty
trait Crypto:
  def encrypt(value: Password): Either[String, EncryptedPassword]

  def check(password: Password, encrypted: EncryptedPassword): Boolean
end Crypto

object Crypto:
  def make[F[_]: Sync]: F[Crypto] =
    Sync[F].delay:
      new Crypto:
        def encrypt(password: Password): Either[String, EncryptedPassword] =
          val hashed = BCrypt.hashpw(password.value, BCrypt.gensalt)
          EncryptedPassword.either(hashed)

        def check(password: Password, encrypted: EncryptedPassword): Boolean =
          BCrypt.checkpw(password.value, encrypted.value)
end Crypto
```

**How BCrypt works:**

- Automatically generates a random salt for each password
- Embeds the salt into the resulting hash
- Resistant to dictionary and rainbow table attacks
- Configurable number of iterations (work factor) to slow down brute force

### Security

1. **Passwords are not stored in plain text** — only BCrypt hashes
2. **Tokens have a limited lifetime** — automatic invalidation
3. **Ability to force-terminate sessions** — via removal from Redis
4. **Unique token identifiers** — prevents reuse
5. **JWT signature** — guarantees token integrity

---

**Links:**

- [Source code repository][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]
- [Jots — JWT for Scala and Cats Effect][jots]

[repo]: https://gitverse.ru/scalabook/pdp
[pfp]: https://leanpub.com/pfp-scala
[redis4cats]: https://github.com/profunktor/redis4cats
[jots]: https://github.com/vlovgr/jots
