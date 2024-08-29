---
pill: placeholders
subtitle: flyway.placeholders
redirect_from: Configuration/Placeholderss/
---

# Placeholders

## Description
[Placeholders](Configuration/Placeholders Configuration) to replace in SQL migrations. 

For example to replace a placeholder named `key1` with the value `value1`, you can set `flyway.placeholders.key1=value1`. 
Flyway will take the `key1` part, along with the [placeholder prefix](Configuration/Parameters/Flyway/Placeholder Prefix) and the [placeholder suffix](Configuration/Parameters/Flyway/Placeholder Suffix) construct a placeholder replacement, which by default would look like `${key1}`. Then in your SQL migrations and instances of this will be replaced with `value1`. 

Placeholder matching is case insensitive, so a placeholder of `flyway.placeholders.key1` will match `${key1}` and `${KEY1}`.

For default placeholders that flyway automatically provides, see [placeholders configuration](Configuration/Placeholders Configuration).

## Usage

### Commandline
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
