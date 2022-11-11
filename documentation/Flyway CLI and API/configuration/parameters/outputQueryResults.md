---
layout: documentation
menu: configuration
pill: outputQueryResults
subtitle: flyway.outputQueryResults
redirect_from: /documentation/configuration/outputQueryResults/
---

# Output Query Results
{% include teams.html %}

## Description
Controls whether Flyway should output a table with the results of queries when executing migrations. 

## Default
true

## Usage

### Commandline
```powershell
./flyway -outputQueryResults="false" info
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

With `outputQueryResults` enabled the result of this `SELECT` will be printed for you to inspect and verify before you continue.
