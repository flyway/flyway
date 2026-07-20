---
subtitle: Flyway Docker
---

## Flyway Docker Image
In addition to the base and Alpine Flyway images, optional Azure and Mongo variants are provided.
The Azure images include additional tooling for running Flyway on hosted Azure DevOps agents, whilst the Mongo variant bundles `mongosh` for native connectors.

You can find the [Dockerfiles](https://github.com/flyway/flyway/tree/main/flyway-docker/dockerfiles) used for our official Flyway images in the open-source repository.
### Redgate Edition
The official Flyway docker image for community, teams, and enterprise editions can be found [here](https://hub.docker.com/r/redgate/flyway).

### Open Source Edition
The open source Flyway image can be found [here](https://hub.docker.com/r/flyway/flyway).

## Usage

The examples below use the `redgate/flyway` image. Open Source Edition users should use `flyway/flyway` instead, and can omit the licensing parameters (see [Licensing](#licensing) below).

### The Flyway project folder

Mount a single folder that follows the [standard Flyway project structure](https://documentation.red-gate.com/fd/flyway-projects-271585108.html) and point Flyway at it with `-workingDirectory`. Flyway then finds `flyway.toml` inside it automatically, so connection details, `locations`, licensing, and driver/jar directories can all live in one config file instead of being repeated as command-line arguments.

```
my-flyway-project/
├── flyway.toml
├── migrations/
│   └── V1__Initial.sql
├── drivers/    (JDBC drivers not already bundled with Flyway)
└── jars/       (Java-based migrations and callbacks)
```

```
docker run --rm -v /absolute/path/to/my-flyway-project:/flyway/project redgate/flyway -workingDirectory=project migrate
```

### Getting started

The easiest way to get started is simply to test the default image by running

`docker run --rm redgate/flyway`

This will give you Flyway Command-line's usage instructions.

To do anything useful however, mount your project folder and point Flyway at it. Create a `flyway.toml`:

```
[environments.development]
url = "jdbc:sqlite:/flyway/project/dev.db"

[flyway]
environment = "development"
locations = ["filesystem:migrations"]
```

and a migration in `migrations/V1__Initial.sql`:

```sql
CREATE TABLE MyTable (
    MyColumn VARCHAR(100) NOT NULL
);
```

then run:

`docker run --rm -v /absolute/path/to/my-flyway-project:/flyway/project redgate/flyway -workingDirectory=project migrate`

Note that the syntax for **redgate/flyway:\*-azure** is slightly different in order to be compatible with Azure Pipelines
agent job requirements. As it does not define an entrypoint, you need to explicitly add the `flyway` command. For example:

`docker run --rm redgate/flyway:latest-azure flyway`

### Licensing

Community edition does not require a license. To unlock Teams and Enterprise features, add a Redgate personal access token to `flyway.toml`:

```
[flyway]
email = "foo.bar@red-gate.com"
token = "1234ABCD"
```

See the [Personal Access Tokens tutorial](https://documentation.red-gate.com/fd/tutorial-personal-access-tokens-277579351.html) for details on generating a token.

### Adding a JDBC driver

If your database driver [isn't shipped by default](https://documentation.red-gate.com/fd/reference), add it to a `drivers` folder in your project and reference it with `jarDirs`:

```
[flyway]
jarDirs = ["drivers"]
```

Apache Derby's driver isn't bundled with Flyway. Download the required jars into `drivers/` and Flyway will pick them up automatically:

`docker run --rm -v /absolute/path/to/my-flyway-project:/flyway/project redgate/flyway -workingDirectory=project -url="jdbc:derby:memory:mydb;create=true" info`

### Adding Java-based migrations and callbacks

Java-based migrations and callbacks work the same way, via a `jars` folder and `locations` including `classpath:db/migration`:

```
[flyway]
jarDirs = ["drivers", "jars"]
locations = ["filesystem:migrations", "classpath:db/migration"]
```

### Docker Compose

To run both Flyway and the database that will be migrated in containers, mount your project folder into the Flyway service and set `-workingDirectory` in its command.

#### Example

```
services:
  flyway:
    image: redgate/flyway
    command: -workingDirectory=project migrate
    volumes:
      - ./project:/flyway/project
    depends_on:
      db:
        condition: service_healthy
  db:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=P@ssw0rd
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    ports:
      - 3306:3306
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "-uroot", "-pP@ssw0rd"]
      interval: 5s
      timeout: 5s
      retries: 10
```

with `project/flyway.toml`:

```
[environments.development]
url = "jdbc:mysql://db?allowPublicKeyRetrieval=true&useSSL=false"
schemas = ["myschema"]
user = "root"
password = "P@ssw0rd"

[flyway]
environment = "development"
locations = ["filesystem:migrations"]
```

`allowPublicKeyRetrieval=true` is needed because recent `mysql` images default to `caching_sha2_password` authentication, which the bundled JDBC driver can't use without it (or TLS). The health check uses `127.0.0.1` rather than `localhost` because the MySQL client resolves `localhost` to a local socket connection, which stays reachable even while the container's TCP listener is still starting up. Using `127.0.0.1` forces a real network check.

`depends_on.condition: service_healthy` makes Compose wait for that health check to pass before starting Flyway, so the migration only runs once MySQL is actually ready to accept connections. Run `docker compose up` to start both services.

## Custom Docker Image
If you are building your own docker image, you will need to ensure the following environmental dependencies are met.

Redgate's comparison engines are built with .NET hence require additional dependencies which may or may not be bundled in your base image.
These are necessary if you wish to use Flyway's advanced capabilities.

### Base
```
libicu74
libgcc1
libstdc++6
libssl3
zlib1g
liblttng-ust1
libunwind8

libgssapi-krb5-2

python3-pip
```
### Alpine
```
icu-libs
libgcc
libstdc++
libssl3
zlib
libintl

krb5-libs

py3-pip

bash
```
