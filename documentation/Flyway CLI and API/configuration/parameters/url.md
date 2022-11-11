---
layout: documentation
menu: configuration
pill: url
subtitle: flyway.url
redirect_from: /documentation/configuration/url/
---

# URL

## Description
The jdbc url to use to connect to the database.

Note: Only certain jdbc drivers are packaged with flyway. If your driver is not packaged, then you need to ensure it is available on the classpath (see [Adding to the classpath](/documentation/addingToTheClasspath)).

## Usage

### Commandline
```powershell
./flyway -url=jdbc:h2:mem:flyway_db info
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
```java
Flyway.configure()
    .url("jdbc:h2:mem:flyway_db")
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