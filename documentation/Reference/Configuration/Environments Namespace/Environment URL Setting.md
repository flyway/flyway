---
subtitle: flyway.url
redirect_from: Configuration/url/
---

## Description

The jdbc url to use to connect to the database.

Note: Only certain jdbc drivers are packaged with flyway.
If your driver is not packaged, then you need to ensure it is available on the classpath (see [Adding to the classpath](<Usage/Adding to the classpath>)).

## Type

String

## Default

<i>none - this is required</i>

## Usage

### Flyway Desktop

This can be set from the connection dialog.

### Command-line

```powershell
./flyway -url=jdbc:h2:mem:flyway_db info
```

To configure a named environment via command line when using a TOML configuration, prefix `url` with
`environments.{environment name}.` for example:

```powershell
./flyway -environments.sample.url=jdbc:h2:mem:flyway_db info
```

### TOML Configuration File

```toml
[environments.default]
url = "jdbc:h2:mem:flyway_db"
```

### Configuration File

```properties
flyway.url=jdbc:h2:mem:flyway_db
```

### Environment Variable

```properties
FLYWAY_URL=jdbc:h2:mem:flyway_db
```

### API

When using the Java API, you configure your JDBC URL, User and Password via a datasource.

```java
Flyway.configure()
        .datasource("jdbc:h2:mem:flyway_db", "myuser", "mysecretpassword")
        .load()
```

### Gradle

```groovy
flyway {
    url = 'jdbc:h2:mem:flyway_db'
}
```

### Maven

```xml
<configuration>
    <url>jdbc:h2:file:./target/foobar</url>
</configuration>
```
