---
subtitle: Docker Provisioner
---

- **Status:** Preview

{% include enterprise.html %}

This [provisioner](https://documentation.red-gate.com/flyway/flyway-concepts/environments/provisioning) automatically provisions a database in a Docker container with minimal configuration. It detects the database engine and version to use, selects a matching Docker image, and spins up a container using raw Docker commands.

This makes it ideal for quickly standing up a [build environment](https://documentation.red-gate.com/flyway/flyway-concepts/environments/shadow-and-build-environments) - for example in a CI pipeline - with little or no configuration.

Prerequisites:
* Docker must be installed and available on the machine running Flyway

To configure this provisioner:
1. Set the value of the [provisioner parameter](<Configuration/Environments Namespace/Environment Provisioner Setting>) to `docker`
2. Optionally populate any of the following resolver properties:
    - `sourceEnvironment` - (Optional) The name of another environment to probe for the database engine type and version. When specified, the provisioner connects to this environment to detect the database configuration to replicate. If not specified, the provisioner attempts to detect from the target environment context (this will fail if the target is not a direct connection, i.e. it has a provisioner of its own).
    - `databaseEngine` - (Optional) The database engine type to use (e.g. `sqlserver`, `postgresql`, `mysql`, `oracle`). When specified alongside `databaseEngineVersion`, the provisioner skips auto-detection and uses these values directly. Useful when there is no existing database to probe.
    - `databaseEngineVersion` - (Optional) The version of the database engine to use (e.g. `16.2`, `2022`). Must be specified together with `databaseEngine`.
    - `waitTimeout` - (Optional) The time to wait until the container is running/healthy. Defaults to `2m`. This takes the form of a number optionally followed by a time unit, `s`, `m`, `h`, or `d`. If no time unit is specified, seconds are assumed.
    - `keepAlive` - (Optional) When set to `true`, the container is kept running after Flyway exits so that subsequent Flyway commands can reuse it. Defaults to `true`.
    - `iAgreeToTheDBVendorsEula` - (Optional) Must be set to `true` to provision database engines that require acceptance of the vendor's EULA (SQL Server and Oracle). See [EULA acceptance](#eula-acceptance) below.

> **Note:** The `composeFile` and `services` keys belong to the [`docker-compose` provisioner](<Configuration/Environments Namespace/Environment Provisioner Setting/Docker Compose Provisioner>). For backward compatibility, a `docker` configuration that sets only those keys is forwarded to `docker-compose` with a deprecation warning, but combining them with any of the keys above is not supported and results in an error. Use `docker-compose` for compose-file based provisioning and `docker` for the auto-detecting behavior described here.

## Engine and version resolution

The provisioner resolves the database engine and version using the following precedence:

1. If `databaseEngine` and `databaseEngineVersion` are explicitly set, use those values directly.
2. If `sourceEnvironment` is set, probe that environment to detect the values.
3. Otherwise, attempt to detect from the target environment context.

## Examples

### Zero-config (auto-detection from the target environment)

When the target environment is a direct connection, no resolver configuration is required. Flyway probes the target to detect the database engine and version, provisions a matching Docker container, and uses it as the environment.

```toml
[environments.build]
provisioner = "docker"

[flyway]
environment = "build"
```

### Probing a source environment

Specify `sourceEnvironment` to detect the engine and version from another environment. In this example, the provisioner connects to the `production` environment, detects that it is running SQL Server 2022, and spins up a matching Docker container for the build environment.

```toml
[environments.production]
url = "jdbc:sqlserver://prod-server:1433;databaseName=MyDatabase;trustServerCertificate=true"
user = "${localSecret.ProdUser}"
password = "${localSecret.ProdPassword}"

[environments.build]
provisioner = "docker"

[environments.build.resolvers.docker]
sourceEnvironment = "production"
```

### Explicit engine and version

When there is no existing database to probe (for example, a greenfield project), set `databaseEngine` and `databaseEngineVersion` explicitly to skip auto-detection.

```toml
[environments.build]
provisioner = "docker"

[environments.build.resolvers.docker]
databaseEngine = "postgresql"
databaseEngineVersion = "16.2"
```

## Container lifecycle and naming

By default, `keepAlive` is `true`, so the container is kept running after Flyway exits. This is important for CI pipelines where multiple Flyway commands run in sequence against the same build database - repeatedly starting and stopping containers between commands would be inefficient, so a kept-alive container is reused by subsequent commands.

Because containers are kept alive by default, the provisioner names them in a logical and predictable manner. This means that on a developer machine you can identify and clean up provisioned containers manually (for example with `docker ps` and `docker rm`) if you wish.

Set `keepAlive = false` to have the container removed when Flyway exits:

```toml
[environments.build]
provisioner = "docker"

[environments.build.resolvers.docker]
sourceEnvironment = "production"
waitTimeout = "3m"
keepAlive = false
```

## EULA acceptance

Some database engines require you to accept the vendor's End User License Agreement (EULA) before a container can be provisioned. This applies to **SQL Server** and **Oracle**. PostgreSQL and MySQL are not affected.

To provision one of these engines, set `iAgreeToTheDBVendorsEula` to `true`, which confirms your agreement to the relevant vendor's EULA:

```toml
[environments.build]
provisioner = "docker"

[environments.build.resolvers.docker]
databaseEngine = "sqlserver"
databaseEngineVersion = "2022"
iAgreeToTheDBVendorsEula = true
```

If this property is not set to `true` for a SQL Server or Oracle engine, provisioning will fail.
