---
pill: target
subtitle: flyway.target
redirect_from: Configuration/target/
---

## Description

The target version up to which Flyway should consider migrations.
This must be a valid migration version, or one of the special values detailed later.

### Migrate

When migrating forwards, Flyway will apply all migrations up to and including the target version. Migrations with a
higher version number will be ignored. If the target is `current`, then no versioned migrations will be
applied but repeatable migrations will be, together with any callbacks.

#### Special values

- `current`: designates the current version of the schema
- `latest`: the latest version of the schema, as defined by the migration with the highest version
- `next`: the next version of the schema, as defined by the first pending migration
- `<version>?`: instructs Flyway to not fail if the target version doesn't exist. In this case, Flyway will go up to but not beyond the specified target (default: fail if the target version doesn't exist) (e.g.) `target=2.1?`

### Undo

When undoing migrations, Flyway works its way up the schema history table (i.e. in reverse applied order), undoing versioned migrations until it gets to one meeting one of these conditions:

- The migration's version number is below the target version.
- The migration doesn't have a corresponding undo migration.

Specifying a target version should be done with care, as undo scripts typically destroy database objects.

#### Special values

- `current`, `latest`, `next`: designates the current version of the schema. Flyway will undo the latest applied migration.
- `<version>?`: instructs Flyway to not fail if the target version doesn't exist. In this case, Flyway will go down to but not beyond the specified target (default: fail if the target version doesn't exist) (e.g.) `target=2.1?`

## Type

String

## Default

`"latest"` for versioned migrations
`"current"` for undo migrations.

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -target="2.0" migrate
```

### TOML Configuration File

```toml
[flyway]
target = "2.0"
```

### Configuration File

```properties
flyway.target=2.0
```

### Environment Variable

```properties
FLYWAY_TARGET=2.0
```

### API

```java
Flyway.configure()
    .target("2.0")
    .load()
```

### Gradle

```groovy
flyway {
    target = '2.0'
}
```

### Maven

```xml
<configuration>
    <target>2.0</target>
</configuration>
```
