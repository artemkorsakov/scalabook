# Adding a Docker Image

Once the application is fully implemented and tested locally, the delivery phase begins —
packaging into a Docker image and launching together with infrastructure services
(PostgreSQL and Redis) via `docker-compose`. This allows running the application
on any machine with a single command, without manually installing JDK, Scala, and PostgreSQL.

## Configuring Docker Image Build in `build.sbt`

For building a Docker image, [sbt-native-packager](https://github.com/sbt/sbt-native-packager) is used — a plugin that
can package a Scala application into a Docker image along with a JRE, all dependencies,
and a startup script.

### Plugin Dependencies

```dotty
lazy val pdp = (project in file("pdp"))
  .enablePlugins(
    DockerPlugin,       // Building a Docker image
    JavaAppPackaging    // Packaging a JVM application (JRE + all jars + startup script)
  )
```

**Plugin Breakdown:**

- **`JavaAppPackaging`** — the base plugin. Packages the application into a "distribution":
  JRE + all jar files + a bash startup script. This distribution is then
  placed into the Docker image.
- **`DockerPlugin`** — extends `JavaAppPackaging`, adding the ability to
  build a Docker image from the distribution.

Without the `JavaAppPackaging` plugin, the `DockerPlugin` won't work — it depends on it.

### Docker Settings

```dotty
  .settings(
    Docker / packageName := "personal-development-plan",
    dockerExposedPorts ++= Seq(8080),
    dockerUpdateLatest := true
  )
```

**Settings Breakdown:**

| Setting                  | Value                       | What it does                                                                                                                |
| -------------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| **`Docker / packageName`** | `"personal-development-plan"` | Docker image name. On build, you'll get `personal-development-plan:0.1.0` and the `latest` tag.                                 |
| **`dockerExposedPorts`**   | `Seq(8080)`                   | The `EXPOSE 8080` directive in the Dockerfile. The port the application listens on.                                                |
| **`dockerUpdateLatest`**   | `true`                        | After building, updates the `latest` tag — `personal-development-plan:latest`. Without this, you'd have to specify the version manually. |

### How It Works Under the Hood

Running `sbt Docker/publishLocal` does the following:

1. **`stage`** — `JavaAppPackaging` builds the distribution in `pdp/target/docker/stage/opt/docker/`:

    ```
    opt/docker/
    ├── bin/
    │   └── pdp-backend          # startup script
    ├── lib/
    │   └── *.jar                # all dependencies
    ├── assets/                  # our static files
    └── ...
    ```

2. **Dockerfile** — `DockerPlugin` generates a Dockerfile:

    ```dockerfile
    FROM eclipse-temurin:21-jre
    WORKDIR /opt/docker
    COPY --chown=... opt /opt/docker
    USER 1001:0
    EXPOSE 8080
    ENTRYPOINT ["/opt/docker/bin/pdp-backend"]
    ```

3. **`docker build`** — Docker builds the image based on this Dockerfile.

4. **`latest` tag** — is updated if `dockerUpdateLatest := true`.

## Working with Static Resources

### Problem

By default, Krop expects static files at the path `src/main/resources/assets`.
This works when running via `sbt run` or from an IDE, but **breaks in a Docker container**:
the image has no `src/` directory, and the working directory is `/opt/docker`.

On application startup, an error will occur:

```
Cannot create an asset route for src/main/resources/assets as this path is not a directory.
```

### Solution: `src/universal`

`JavaAppPackaging` automatically copies the contents of `src/universal/` to `/opt/docker/`
inside the image. So we move the assets there:

```
pdp/
└── src/
    └── universal/
        └── assets/
            ├── css/
            ├── js/
            └── icons/
```

**After the image is built**, this structure will be inside the container:

```
/opt/docker/
├── bin/
├── lib/
└── assets/
    ├── css/
    ├── js/
    └── icons/
```

### Changing `AssetRoute`

Update the code so the path is **relative** to the working directory `/opt/docker`:

```dotty
// Before:
val assets: AssetRoute =
  AssetRoute(Path.root / "assets", "src/main/resources/assets")

// After:
val assets: AssetRoute =
  AssetRoute(Path.root / "assets", "assets")
```

**Breakdown:**

- **`Path.root / "assets"`** — URL at which assets are available: `/assets/...`
- **`"assets"`** — path on the filesystem relative to the working directory.
  In Docker, the working directory is `/opt/docker`, so Krop looks for `/opt/docker/assets`.
  The files ended up there thanks to `src/universal`.

**How to verify the files are in place:**

```bash
docker run --rm --entrypoint ls personal-development-plan:latest -la /opt/docker/assets
```

You should see `css/`, `js/`, `icons/`.

## Docker Compose

`docker-compose.yml` describes three services: PostgreSQL, Redis, and our application.

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

### Service Breakdown

**`postgres` service:**

- **`image: postgres:18`** — official PostgreSQL version 18 image.
- **`ports: "5432:5432"`** — exposes the port to the host. This is convenient for debugging:
  you can connect to the database via `psql` from the host.
- **`environment`** — variables for database initialization:
  - `POSTGRES_DB=pdp` — creates the `pdp` database.
  - `POSTGRES_USER=postgres` — superuser.
  - `POSTGRES_PASSWORD=my-password` — password (in production use secrets).
- **`healthcheck`** — checks database readiness via the `pg_isready` command.
  Dependent services won't start until the database is ready.
- **`restart: always`** — automatically restarts the container on failure
  or host restart.

**`redis` service:**

- **`image: redis:latest`** — official Redis image.
- **`ports: "6379:6379"`** — exposes the port.
- **`healthcheck`** — checks Redis readiness via the `redis-cli ping` command.
  Expects response `PONG`.

**`pdp` service:**

- **`image: personal-development-plan:latest`** — our image, built via
  `sbt Docker/publishLocal`.
- **`ports: "8080:8080"`** — exposes the application port.
- **`depends_on`** — starts the service **only after** `postgres` and `redis`
  are healthy. This is critical: without `healthcheck`, the application could start
  before the database and crash with a connection error.

### What's Important to Remember

**1. The host in the application must be `0.0.0.0`**

By default, Krop listens on `localhost` — only inside the container. For the application
to be accessible from the host machine, `application.conf` must have:

```hocon
http-server-config {
    host = "0.0.0.0"
    port = 8080
}
```

Otherwise, accessing `http://localhost:8080/` from the host will result in
`Connection refused`.

**2. The application must connect to services by their names**

In `docker-compose`, all services are on the same network and see each other
by their names. In `application.conf` the following must be:

```hocon
postgres {
    host = "postgres"     # not localhost!
    port = 5432
    database = "pdp"
    user = "postgres"
    password = "my-password"
}

redis {
    host = "redis"        # not localhost!
    port = 6379
}
```

**3. Passwords go in secrets**

In production, don't store passwords in `docker-compose.yml`. Use
`secrets` or environment variables from CI/CD:

```yml
environment:
  - POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
```

And run:

```bash
POSTGRES_PASSWORD=secret docker-compose up
```

## Running the Project

### 1. Local Docker Image Build

```bash
sbt Docker/publishLocal
```

What happens:

- SBT compiles the project.
- `JavaAppPackaging` builds the distribution.
- `DockerPlugin` generates the Dockerfile and builds the image.
- The image gets the tag `personal-development-plan:latest` (and version).

**Verify the image was built:**

```bash
docker images | grep personal-development-plan
```

### 2. Launch via `docker-compose`

```bash
docker-compose up
```

What happens:

1. A default network is created (if it doesn't exist).
2. `postgres` is started, awaiting its `healthy` state.
3. `redis` is started, awaiting its `healthy` state.
4. `pdp` — our application — is started.
5. Logs from all services are output to the terminal.

**The application is available at:** `http://localhost:8080/`

**Verification:**

```bash
curl http://localhost:8080/health_check
```

Expected response:

```json
{
  "redis": "Okay",
  "postgres": "Okay"
}
```

### 3. Shutdown

```bash
docker-compose down
```

What happens:

1. All containers are stopped.
2. Containers and the network are deleted.

**If you also want to delete the data:**

```bash
docker-compose down -v
```

The `-v` flag removes volumes. Use with caution: all database data will be lost.

### 4. Rebuild After Changes

If you've changed the code:

```bash
sbt Docker/publishLocal
docker-compose up --force-recreate pdp
```

**Why is `--force-recreate` needed?**

`docker-compose up` might reuse the old container if the image hasn't changed
by hash. `--force-recreate` guarantees the container will be recreated with the new image.

## Useful Commands

| Command                      | What it does                               |
| ---------------------------- | ---------------------------------------- |
| `sbt Docker/publishLocal`    | Builds a Docker image locally           |
| `sbt Docker/stage`           | Only builds the distribution (without image) |
| `docker-compose up`          | Starts all services                    |
| `docker-compose up -d`       | Starts in the background                         |
| `docker-compose down`        | Stops and deletes containers       |
| `docker-compose down -v`     | Same + deletes volumes                  |
| `docker-compose logs -f pdp` | Application logs in real time       |
| `docker-compose exec pdp sh` | Enters the container                      |
| `docker-compose ps`          | Shows service status               |

## Summary

We've set up the full delivery pipeline for the application:

1. **Docker image build** via sbt-native-packager.
2. **Static assets** in `src/universal/assets` — they end up inside the image.
3. **Docker Compose** — starts the application together with PostgreSQL and Redis
    with a single command.
4. **Healthchecks** — ensure the correct startup order: database first,
    then the application.
5. **Verification** — `curl /health_check` confirms everything works.

**Key architectural decisions:**

- **`src/universal`** — the standard location for files that should end up
  in the distribution.
- **Relative path `"assets"`** in `AssetRoute` — works both locally
  and in the container.
- **`depends_on` with `condition: service_healthy`** — prevents race
  conditions at startup.
- **`dockerUpdateLatest`** — simplifies tagging during development.

The application is now deployment-ready: on any machine with Docker it's sufficient
to run `sbt Docker/publishLocal && docker-compose up` — and it will work.

---

**Links:**

- [Source code repository][repo]

[repo]: https://gitverse.ru/scalabook/pdp
