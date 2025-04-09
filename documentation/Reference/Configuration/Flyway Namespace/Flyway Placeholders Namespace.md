---
subtitle: flyway.placeholders
redirect_from: Configuration/Placeholderss/
---

## Description

[Placeholders](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/migration-placeholders) to replace in SQL migrations.

For example to replace a placeholder named `key1` with the value `value1`, you can set `flyway.placeholders.key1="value1"`.
Flyway will take the `key1` part, along with the [placeholder prefix](<Configuration/Flyway Namespace/Flyway Placeholder Prefix Setting>) and the [placeholder suffix](<Configuration/Flyway Namespace/Flyway Placeholder Suffix Setting>) construct a placeholder replacement, which by default would look like `${key1}`. Then in your SQL migrations and instances of this will be replaced with `value1`.

Placeholder matching is case insensitive, so a placeholder of `flyway.placeholders.key1` will match `${key1}` and `${KEY1}`.

### Default placeholders

Flyway also provides default placeholders, whose values are automatically populated:

- `${flyway:defaultSchema}` = The default schema for Flyway
- `${flyway:user}` = The user Flyway will use to connect to the database
- `${flyway:database}` = The name of the database from the connection url
- `${flyway:timestamp}` = The time that Flyway parsed the script, formatted as 'yyyy-MM-dd HH:mm:ss'
- `${flyway:filename}` = The filename of the current script
- `${flyway:workingDirectory}` = The user working directory as defined by the ['user.dir'](https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html) System Property
- `${flyway:table}` = The name of the Flyway schema history table
- `${flyway:environment}` = The name of the [environment](<Configuration/Flyway Namespace/Flyway environment Setting>) configured for this script execution

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -placeholders.key1=value1 -placeholders.key2=value2 info
```

### TOML Configuration File

```toml
[flyway.placeholders]
key1 = "value1"
key2 = "value2"
```

### Configuration File

```properties
flyway.placeholders.key1=value1
flyway.placeholders.key2=value2
```

### Environment Variable

```properties
FLYWAY_PLACEHOLDERS_KEY1=value1
FLYWAY_PLACEHOLDERS_KEY2=value2
```

### API

```java
Map<String, String> placeholders = new HashMap<>();
placeholders.put("key1", "value1");
placeholders.put("key2", "value2");

Flyway.configure()
    .placeholders(placeholders)
    .load()
```

### Gradle

```groovy
flyway {
    placeholders = ['key1' : 'value1', 'key2' : 'value2']
}
```

### Maven

```xml
<configuration>
    <placeholders>
        <key1>value1</key1>
        <key2>value2</key2>
    </placeholders>
</configuration>
```
