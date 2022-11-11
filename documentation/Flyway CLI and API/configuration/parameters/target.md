---
layout: documentation
menu: configuration
pill: target
subtitle: flyway.target
redirect_from: /documentation/configuration/target/
---

# Target

## Description

The target version up to which Flyway should consider migrations. If set to a value other than `current` or `latest`,
this must be a valid migration version (e.g. `2.1`).

### Targeting migrate

When migrating forwards, Flyway will apply all migrations up to and including the target version. Migrations with a
higher version number will be ignored. If the target is `current`, then no versioned migrations will be
applied but repeatable migrations will be, together with any callbacks.

### Targeting undo

When undoing migrations, Flyway works its way up the schema history table (i.e. in reverse applied order), undoing versioned migrations until it gets to one meeting one of these conditions:
 - The migration's version number is below the target version.
 - The migration doesn't have a corresponding undo migration.

Specifying a target version should be done with care, as undo scripts typically destroy database objects.

### Special values

- `current`: designates the current version of the schema
- `latest`: the latest version of the schema, as defined by the migration with the highest version
- `next`: the next version of the schema, as defined by the first pending migration
- `<version>?`: {% include teams.html %} Instructs Flyway to not fail if the target version doesn't exist. In this case, Flyway will go up to but not beyond the specified target (default: fail if the target version doesn't exist) (e.g.) `target=2.1?`

## Default

`latest` for versioned migrations, `current` for undo migrations.

## Usage

### Commandline
```powershell
./flyway -target="2.0" migrate
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