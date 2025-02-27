---
pill: errorOverrides
subtitle: flyway.errorOverrides
redirect_from: Configuration/errorOverrides/
---

{% include teams.html %}

## Description

Rules for the built-in error handler that let you override specific SQL states and errors codes in order to force specific errors or warnings to be treated as debug messages, info messages, warnings or errors.

Each error override has the following format: <code>STATE:12345:W</code>. It is a 5 character SQL state (or <code>
*</code> to match all SQL states), a colon, the SQL error code (
or <code>*</code> to match all SQL error codes), a colon and finally the desired behavior that should override the initial one.

The following behaviors are accepted:</p>
<ul>
    <li><code>D</code> to force a debug message</li>
    <li><code>D-</code> to force a debug message, but do not show the original sql state and error code</li>
    <li><code>I</code> to force an info message</li>
    <li><code>I-</code> to force an info message, but do not show the original sql state and error code</li>
    <li><code>W</code> to force a warning</li>
    <li><code>W-</code> to force a warning, but do not show the original sql state and error code</li>
    <li><code>E</code> to force an error</li>
    <li><code>E-</code> to force an error, but do not show the original sql state and error code</li>
</ul>

Example 1: to force Oracle stored procedure compilation issues to produce errors instead of warnings, the following errorOverride can be used: <code>99999:17110:E</code>

Example 2: to force SQL Server PRINT messages to be displayed as info messages (without SQL state and error code details) instead of warnings, the following errorOverride can be used: <code>S0001:0:I-</code>

Example 3: to force all errors with SQL error code 123 to be treated as warnings instead, the following errorOverride can be used: <code>*:123:W</code>

See [Migration Error and Logging handling](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/migration-error-and-logging-handling) for more details.

## Type

String array

## Default

`[]`

## Usage

### Flyway Desktop

This can't be set via Flyway Desktop.

### Command-line

```powershell
./flyway -errorOverrides="STATE:12345:W" clean
```

### TOML Configuration File

```toml
[flyway]
errorOverrides = ["STATE:12345:W"]
```

### Configuration File

```properties
flyway.errorOverrides=STATE:12345:W
```

### Environment Variable

```properties
FLYWAY_ERROR_OVERRIDES=STATE:12345:W
```

### API

```java
Flyway.configure()
    .errorOverrides("STATE:12345:W")
    .load()
```

### Gradle

```groovy
flyway {
    errorOverrides = 'STATE:12345:W'
}
```

### Maven

```xml
<configuration>
  <errorOverrides>STATE:12345:W</errorOverrides>
</configuration>
```
