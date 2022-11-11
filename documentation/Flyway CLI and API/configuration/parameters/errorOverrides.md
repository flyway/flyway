---
layout: documentation
menu: configuration
pill: errorOverrides
subtitle: flyway.errorOverrides
redirect_from: /documentation/configuration/errorOverrides/
---

# Error Overrides
{% include teams.html %}

## Description
Rules for the built-in error handler that let you override specific SQL states and errors codes in order to force specific errors or warnings to be treated as debug messages, info messages, warnings or errors.

Each error override has the following format: <code>STATE:12345:W</code>. It is a 5 character SQL state (or <code>*</code> to match all SQL states), a colon, the SQL error code (or <code>*</code> to match all SQL error codes), a colon and finally the desired behavior that should override the initial one.

The following behaviors are accepted:
<ul>
    <li><code>D</code> to force a debug message</li>
    <li><code>D-</code> to force a debug message, but do not show the original sql state and error code</li>
    <li><code>D+</code> to force a debug message, but <b>only</b> show the original sql state and error code</li>
    <li><code>I</code> to force an info message</li>
    <li><code>I-</code> to force an info message, but do not show the original sql state and error code</li>
    <li><code>I+</code> to force an info message, but <b>only</b> show the original sql state and error code</li>
    <li><code>W</code> to force a warning</li>
    <li><code>W-</code> to force a warning, but do not show the original sql state and error code</li>
    <li><code>W+</code> to force a warning, but <b>only</b> show the original sql state and error code</li>
    <li><code>E</code> to force an error</li>
    <li><code>E-</code> to force an error, but do not show the original sql state and error code</li>
    <li><code>E+</code> to force an error, but <b>only</b> show the original sql state and error code</li>
</ul>

Example 1: to force Oracle stored procedure compilation issues to produce errors instead of warnings, the following errorOverride can be used: <code>99999:17110:E</code>

Example 2: to force SQL Server PRINT messages to be displayed as info messages (without SQL state and error code details) instead of warnings, the following errorOverride can be used: <code>S0001:0:I-</code>

Example 3: to force all errors with SQL error code 123 to be treated as warnings instead, the following errorOverride can be used: <code>*:123:W</code>

Example 4: Use this errorOverride to raise an error for any error or warning, showing only the SQL state and error code without the rest of the message (this is useful for cases where the full error message contains a large SQL statement which would take up the entire commandline output): <code>\*:\*:E+</code>

See [errorOverrides](/documentation/concepts/erroroverrides) for more details.

## Usage

### Commandline
```powershell
./flyway -errorOverrides="STATE:12345:W" clean
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

## Use Cases

See our [error overrides examples](/documentation/concepts/erroroverrides#examples).
