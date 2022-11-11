---
layout: documentation
menu: configuration
pill: ignoreMigrationPatterns
subtitle: flyway.ignoreMigrationPatterns
---

# Ignore Migration Patterns

## Description
Ignore migrations during `validate` and `repair` according to a given list of [patterns](https://flywaydb.org/documentation/configuration/parameters/ignoreMigrationPatterns#patterns).

Only `Missing` migrations are ignored during `repair`.

### Patterns
Patterns are of the form `type`:`status` with `*` matching `type` or `status`.

`type` must be one of (*case insensitive*):

* `repeatable` {% include teams.html %}
* `versioned` {% include teams.html %}
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

Patterns are comma seperated. For example, to ignore missing repeatables and pending versioned migrations:
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

## Default
`*:future`

## Usage

### Commandline
```powershell
./flyway -ignoreMigrationPatterns="repeatable:missing" validate
```

### Configuration File
```properties
flyway.ignoreMigrationPatterns="repeatable:missing"
```

### Environment Variable
```properties
FLYWAY_IGNORE_MIGRATION_PATTERNS="repeatable:missing"
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

## Unsetting the value

By default, `future` migrations are ignored. You can unset this by assigning an empty string to `ignoreMigrationPatterns`

For example, in your configuration file you would add:

```properties
flyway.ignoreMigrationPatterns=
```
