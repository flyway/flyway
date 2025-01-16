---
pill: outputQueryResults
subtitle: flyway.outputQueryResults
redirect_from: Configuration/outputQueryResults/
---

## Description

Migrations are primarily meant to be executed as part of release and deployment automation processes and there is rarely the need to visually inspect the result of SQL queries.
There are however some scenarios where such manual inspection makes sense, and therefore Flyway will display query results in the usual tabular form when a `SELECT` statement (or any other statement that returns results) is executed.

To prevent Flyway from displaying query results, set this option to `false`.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop.

### Command-line

```powershell
./flyway -outputQueryResults="false" info
```

### TOML Configuration File

```toml
[flyway]
outputQueryResults = false
```

### Configuration File

```properties
flyway.outputQueryResults=false
```

### Environment Variable

```properties
FLYWAY_OUTPUT_QUERY_RESULTS=false
```

### API

```java
Flyway.configure()
    .outputQueryResults(false)
    .load()
```

### Gradle

```groovy
flyway {
    outputQueryResults = false
}
```

### Maven

```xml
<configuration>
    <outputQueryResults>false</outputQueryResults>
</configuration>
```

## Use Cases

### Checking the result of your migrations

When developing and testing migrations, you often want to do a sanity check to ensure that they behave and return expected values. For example, you may have applied some migrations that insert data. You could then also execute a select query such as:

```
SELECT * FROM my_table
```

With `outputQueryResults` enabled the result of this
`SELECT` will be printed for you to inspect and verify before you continue.
