# Implementing the Authorization Service

## Creating the Authorization Service

The `make` factory method is the central assembly point for all components of the authorization system.
It demonstrates how various services and dependencies are combined to create a cohesive authentication mechanism.

### Method Signature

```dotty
def make[F[_]: MonadThrow](
    tokenExpiration: FiniteDuration,         // Token lifetime (e.g., 1 hour)
    tokens: Tokens[F],                       // JWT token generator
    usersService: UsersService[F],           // Database users service
    redis: RedisCommands[F, String, String], // Redis store for sessions
    crypto: Crypto                           // Cryptographic service for passwords
): AuthService[F]
```

**Context parameters:**

- `MonadThrow[F]` — requirement for the effect type `F` to handle errors (raising them into the effect context)

## Detailed Method Implementation

### 1. New user registration (`newUser`)

This method implements the full account creation flow:

```dotty
def newUser(username: UserName, password: Password): F[(SignedJwt, UserId)] =
  // Step 1: Check username uniqueness
  usersService.find(username).flatMap:
    case Some(_) =>
      // If user already exists — signal an error
      UserNameInUse(username).raiseError[F, (SignedJwt, UserId)]

    case None =>
      // Step 2: Encrypt password with error handling
      val encryptedPasswordF =
        MonadThrow[F].fromEither(
          crypto.encrypt(password)
            .leftMap(new IllegalArgumentException(_))
        )

      // Step 3: Sequential execution of operations
      for
        // 3.1 Get the encrypted password
        encryptedPassword <- encryptedPasswordF

        // 3.2 Save user to DB and get the ID
        userId <- usersService.create(username, encryptedPassword)

        // 3.3 Generate JWT token for the new session
        token <- tokens.create

        // 3.4 Prepare public user data for Redis
        user = UserInfo(userId, username).asJson.noSpaces

        // 3.5 Save the token -> user data binding
        _ <- redis.setEx(token.show, user, tokenExpiration)

        // 3.6 Save the reverse binding username -> current token
        _ <- redis.setEx(username.show, token.show, tokenExpiration)
      yield (token, userId)
```

**Key registration points:**

1. **Early validation** — check user existence before any other operations
2. **Safe encryption handling** — conversion of `Either` to `F` while preserving error context
3. **Operation atomicity** — using for-comprehension for sequential execution
4. **Double binding in Redis**:
   - Direct binding: `token.show -> UserInfo` for fast lookup by token
   - Reverse binding: `username -> token.show` for quick lookup of the user's current token

Note: the `SignedJwt` type from the Jots library uses
the `Show` interface (`.show`) instead of the `.value` field, so the token ends up in Redis as a plain string.

### 2. User login (`login`)

Authentication method with credential verification:

```dotty
def login(username: UserName, password: Password): F[(SignedJwt, UserId)] =
  // Step 1: Find user in DB
  usersService.find(username).flatMap:
    case None =>
      // User not found — authentication error
      UserNotFound(username).raiseError[F, (SignedJwt, UserId)]

    case Some(user) if !crypto.check(password, user.passwordHash) =>
      // Password doesn't match hash — authentication error
      InvalidPassword(user.username).raiseError[F, (SignedJwt, UserId)]

    case Some(user) =>
      // Step 2: Check for active session
      redis.get(username.show).flatMap:
        case Some(t) =>
          // User already has an active session — return existing token
          // This avoids multiple sessions for one user
          SignedJwt.fromString(t).liftTo[F].map((_, user.id))

        case None =>
          // Step 3: Create a new session
          // flatTap allows executing side effects while preserving the result
          tokens.create.flatTap: token =>
            redis.setEx(
              token.show,
              user.asJson.noSpaces,
              tokenExpiration
            ) *>
              redis.setEx(username.show, token.show, tokenExpiration)
          .map(token => (token, user.id))
```

**Authentication logic:**

1. **Step-by-step verification** — first check user existence, then password
2. **Single session** — "one active session per user" policy
3. **Session recovery** — if session already exists in Redis, return its token
4. **Side effects** — using `flatTap` to save to Redis without losing the token

### 3. Logout (`logout`)

```dotty
def logout(token: SignedJwt, username: UserName): F[Boolean] =
  for
    deltoc  <- redis.del(token.show)   // Delete token -> user binding
    detuser <- redis.del(username.show) // Delete user -> token binding
  yield deltoc > 0 && detuser > 0
```

**Implementation specifics:**

- Using for-comprehension for sequential execution of operations
- Operation atomicity guarantees state consistency

### Users Authentication Service

A separate component for middleware that verifies tokens in requests:

```dotty
object UsersAuthService:
  def make[F[_]: Functor](
      redis: RedisCommands[F, String, String]
  ): UsersAuthService[F, UserInfo] =
    new UsersAuthService[F, UserInfo]:
      def findUser(token: SignedJwt): F[Option[UserInfo]] =
        redis
          .get(token.show)      // Get JSON from Redis by token
          .map(_.flatMap: u =>  // Parse JSON into Option[UserInfo]
            decode[UserInfo](u).toOption
          )
```

**How it works:**

1. Middleware intercepts HTTP request and extracts token from header
2. Calls `findUser` with the received token
3. If token is valid (exists in Redis) — returns `UserInfo`
4. If token is not in Redis or is invalid — returns `None`, request is rejected

## Component Interaction Diagram

```
HTTP Request (with token)
        ↓
[Auth Middleware] → UsersAuthService.findUser(token)
        ↓                    ↓
   Request allowed   Redis lookup token.show
        ↓                    ↓
  Business logic    ←    UserInfo (if found)
        ↓
HTTP Response

Registration/Login:
Client → AuthService (newUser/login)
        ↓
   UsersService (DB)
        ↓
   Token Generation
        ↓
   Redis Storage (sessions)
        ↓
   JWT Token → Client
```

### Advantages of this Architecture

1. **Separation of concerns** — each component is responsible for its specific task
2. **Testability** — all dependencies can be mocked
3. **Scalability** — Redis as centralized session storage enables horizontal scaling
4. **Security** — passwords are never transmitted in plain text, sessions are managed centrally
5. **Performance** — Redis provides fast access to session data
6. **Fault tolerance** — if one application instance fails, sessions are preserved in Redis

### Error Handling

The service defines several error types for different scenarios:

- **UserNameInUse(username)** — attempt to register with an existing username
- **UserNotFound(username)** — attempt to login by a non-existent user
- **InvalidPassword(username)** — incorrect password at login
- **IllegalArgumentException** — error during password encryption (invalid input)

All errors are raised into the `F` context via `raiseError`, allowing centralized handling in the HTTP layer.

## Testing the Authorization Service

Testing is an important stage of development, especially for sensitive components such as the authorization system.
Let's examine in detail how testing is organized using TestContainers
for isolated environment and specific asserters for checking various scenarios.

### Test Environment Setup

To ensure isolated and reproducible testing, the approach uses shared resources via TestContainers:

```dotty
object SharedResources extends GlobalResource:
  // Key for accessing Redis container in global context
  val RedisKey: Option[String] = "redis".some

  /** Initialize shared resources for all tests
    * @param global global context for storing shared resources
    */
  def sharedResources(global: GlobalWrite): Resource[IO, Unit] =
    for
      // Start Redis container
      redisContainer <- redisResource()
      // Store container in global context for access from tests
      _              <- global.putR(redisContainer, RedisKey)
    yield ()

  /** Create Redis container resource
    * @return Resource with running Redis container
    */
  private def redisResource(): Resource[IO, RedisContainer] =
    Resource.make {
      IO {
        val container = RedisContainer() // Create Redis container
        container.start()                // Start the container
        container
      }
    }(container => IO(container.stop())) // Guaranteed stop after tests
```

**Why this approach?**

1. **Isolation** — each test run works with a clean database
2. **Reproducibility** — tests don't depend on external services
3. **Concurrency** — shared resources optimize container usage
4. **Automatic cleanup** — `Resource` guarantees container shutdown

### Test Class Structure

```dotty
class AuthServiceSuite(global: GlobalRead) extends IOSuite:
  // Token for "user not found" test: correctly formatted but not in Redis
  private val notFoundToken: SignedJwt =
    SignedJwt.fromString(
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3ODk3NzQ2NjEsImlhdCI6MTc4NjE3NDY2MSwidXNlcklkIjoiYWM1ZWM4M2YtNWQwZi00MzZmLThjM2ItOWEwNTI2M2RhZGUzIn0.AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    ).toOption.get

  type Res = (AuthService[IO], UsersAuthService[IO, UserInfo])

  override def sharedResource: Resource[IO, Res] =
    for
      authServices <- global.getOrFailR[AuthServices](AuthServicesKey)
    yield (authServices.authService, authServices.usersAuthService)
  end for
end sharedResource
```

Shared resources `AuthServices` (final `AuthService` and `UsersAuthService`) are created once in `SharedResources` for all tests:

## Detailed Test Descriptions

### 1. Searching for a non-existent token

```dotty
test("UsersAuthService.findUser should return None if the JWT is not found"):
  resource =>
    val (_, usersAuthService) = resource

    for
      notFoundUser <- usersAuthService.findUser(notFoundToken)
    yield expect.all(notFoundUser.isEmpty)
```

**What it checks:**

- ✓ Accessing Redis with a non-existent token
- ✓ Correct handling of missing data
- ✓ Returning `None` instead of an error

**Why this matters:**
Middleware should not crash on invalid tokens, but should correctly reject requests.

### 2. Full user lifecycle

```dotty
test("UsersAuthService.findUser should return an user if it exists"):
  resource =>
    val (authService, usersAuthService) = resource

    val username = UserName("first_user")
    val password = Password("first_password")

    for
      // Register new user
      (newUserToken, userId1) <- authService.newUser(username, password)
      maybeNewUser <- usersAuthService.findUser(newUserToken)

      // Login existing user
      (loggedUserToken, userId2) <- authService.login(username, password)
      maybeLoggedUser <- usersAuthService.findUser(loggedUserToken)

      // Logout
      status          <- authService.logout(loggedUserToken, username)

      // Check token invalidation after logout
      maybeLoggedOutNewUser <-
        usersAuthService.findUser(newUserToken)
    yield whenSuccess(maybeNewUser) { user =>
      expect.same(username, user.username) &&
        expect.same(userId1, user.id) &&
        expect.same(userId2, user.id)
    } &&
      whenSuccess(maybeLoggedUser) { user =>
        expect.same(username, user.username)
      } &&
      expect(status) &&
      expect(maybeLoggedOutNewUser.isEmpty)
    end for
```

**Scenario checks:**

- ✓ Successful registration and token receipt
- ✓ Correct data for found user
- ✓ Successful login and token receipt
- ✓ Logout works (token invalidation)
- ✓ Inability to use old token after logout

### 3. Checking username uniqueness

```dotty
test("UsersAuthService.newUser should not create an user with existed username"):
  resource =>
    val (authService, usersAuthService) = resource

    val username  = UserName("second_user")
    val password  = Password("second_password")
    val password2 = Password("second_password2")

    for
      // First registration - successful
      (newUserToken, _) <- authService.newUser(username, password)
      maybeNewUser      <- usersAuthService.findUser(newUserToken)

      // Attempt to register again with same name
      existedUser       <- authService.newUser(username, password2).attempt

      // Check that old user hasn't changed
      maybeNewUser2     <- usersAuthService.findUser(newUserToken)
    yield matches((maybeNewUser, maybeNewUser2)) {
      case (Some(user), Some(user2)) =>
        expect.same(user2.id, user.id) &&          // ID hasn't changed
          expect.same(user2.username, user.username) && // Name hasn't changed
          expect.same(existedUser, Left(UserNameInUse(username))) // Correct error
    }
```

**Key checks:**

- ✓ Protection against duplicate usernames
- ✓ Preservation of existing data integrity
- ✓ Correct error type

### 4. Checking login for non-existent user

```dotty
test("UsersAuthService.login should not login non-existent or invalid credentials"):
  resource =>
    val (authService, _) = resource

    val username = UserName("non-existent_user")
    val password = Password("non-existent_password")

    for
      nonExistent <- authService.login(username, password).attempt
    yield matches(nonExistent) { case Left(error) =>
      expect.same(UserNotFound(username), error)
    }
```

**Checks:**

- ✓ Cannot login with non-existent username
- ✓ Correct error for non-existent user
- ✓ No information leakage (error doesn't indicate what exactly was wrong)

### 5. Checking wrong password

```dotty
test("UsersAuthService.login should not login if a passport is invalid"):
  resource =>
    val (authService, _) = resource

    val username  = UserName("third_user")
    val password  = Password("third_password")
    val password2 = Password("third_password2")

    for
      _               <- authService.newUser(username, password)
      invalidPassport <- authService.login(username, password2).attempt
    yield matches(invalidPassport) { case Left(error) =>
      expect.same(InvalidPassword(username), error)
    }
```

**Important aspects:**

- ✓ User exists, but password is incorrect
- ✓ Receiving a specific error about wrong password

### 6. Login after logout

```dotty
test("UsersAuthService.login should login after logout"):
  resource =>
    val (authService, usersAuthService) = resource

    val username = UserName("forth_user")
    val password = Password("forth_password")

    for
      (token, userId1)    <- authService.newUser(username, password)
      _                   <- authService.login(username, password)
      status              <- authService.logout(token, username)
      (newToken, userId2) <- authService.login(username, password)
      maybeUser           <- usersAuthService.findUser(newToken)
    yield whenSuccess(maybeUser): user =>
      expect.same(username, user.username) &&
        expect(status) &&
        expect.same(userId1, user.id) &&
        expect.same(userId2, user.id)
```

**Checks:**

- ✓ Ability to re-login after logout
- ✓ Generating a new token on re-login
- ✓ Correct data in new token

## Scenario Coverage

Tests cover all main user scenarios:

| Scenario                        | Checks                          |
|---------------------------------|----------------------------------|
| New user registration           | Creation, username uniqueness    |
| Existing user login             | Authentication, token generation |
| Logout                          | Token invalidation               |
| Re-login                        | New session creation             |
| Invalid credentials             | Error handling                   |
| Non-existent user               | Security (no information leaks)  |

### Testing Principles

1. **Isolation** — each test works with clean data
2. **Determinism** — same inputs produce same results
3. **Happy path coverage** — main successful scenarios
4. **Error coverage** — all possible error situations
5. **Invariant checking** — data remains consistent

### Quality Metrics

- ✓ All public service methods are tested
- ✓ Positive and negative scenarios
- ✓ Edge cases
- ✓ Integration with real DB (via TestContainers)
- ✓ Isolated environment for each test

---

**References:**

- [Source repository][repo]
- [Gabriel Volpe - Practical FP in Scala][pfp]

[repo]: https://gitverse.ru/scalabook/pdp
[pfp]: https://leanpub.com/pfp-scala
[redis4cats]: https://github.com/profunktor/redis4cats
