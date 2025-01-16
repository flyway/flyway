---
pill: ignoreMigrationPatterns
subtitle: flyway.ignoreMigrationPatterns
---

## Description

Ignore migrations during `validate` and `repair` according to a given list of patterns.

Only `Missing` migrations are ignored during `repair`.

### Patterns

Patterns are of the form `type`:`status` with `*` matching `type` or `status`.

`type` must be one of (*case insensitive*):

* `repeatable`
* `versioned`
* `*` *(will match any of the above)*

`status` must be one of (*case insensitive*):

* `Missing`
* `Pending`
* `Ignored`
* `Future`
* `*` *(will match any of the above)*

For example, the pattern to ignore missing repeatables is:

```
repeatable:missing
```

Patterns are comma separated. For example, to ignore missing repeatable migrations and pending versioned migrations:

```
repeatable:missing,versioned:pending
```

The `*` wild card is also supported, thus:

```
*:missing
```

will ignore missing migrations no matter their type and:

```
repeatable:*
```

will ignore repeatables regardless of their state.

## Type

String array

## Default

`"*:future"`

## Usage

### Command-line

```powershell
./flyway -ignoreMigrationPatterns="repeatable:missing" validate
```

### TOML Configuration File

```toml
[flyway]
ignoreMigrationPatterns = ["repeatable:missing"]
```

### Configuration File

```properties
flyway.ignoreMigrationPatterns=repeatable:missing
```

### Environment Variable

```properties
FLYWAY_IGNORE_MIGRATION_PATTERNS=repeatable:missing
```

### API

```java
Flyway.configure()
    .ignoreMigrationPatterns("repeatable:missing")
    .load()
```

### Gradle

```groovy
flyway {
    ignoreMigrationPatterns = ['repeatable:missing']
}
```

### Maven

```xml
<configuration>
    <ignoreMigrationPatterns>
        <ignoreMigrationPattern>repeatable:missing</ignoreMigrationPattern>
    </ignoreMigrationPatterns>
</configuration>
```

## Clearing the value

By default, `future` migrations are ignored. You can unset this by assigning an empty string to
`ignoreMigrationPatterns`

For example, in your configuration file you would add:

```properties
flyway.ignoreMigrationPatterns=
```
