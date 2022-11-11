---
layout: documentation
menu: configuration
pill: oracleSqlPlus
subtitle: flyway.oracleSqlPlus
redirect_from: /documentation/configuration/oracleSqlPlus/
---

# Oracle SQL*Plus
{% include teams.html %}

## Description
Enable Flyway's support for [Oracle SQL*Plus commands](/documentation/database/oracle#sqlplus-commands).

## Default
false

## Usage

### Commandline
```powershell
./flyway -oracle.sqlplus="true" info
```

### Configuration File
```properties
flyway.oracle.sqlplus=true
```

### Environment Variable
```properties
FLYWAY_ORACLE_SQLPLUS=true
```

### API
```java
Flyway.configure()
    .oracleSqlplus(true)
    .load()
```

### Gradle
```groovy
flyway {
    oracleSqlplus = true
}
```

### Maven
```xml
<configuration>
    <oracleSqlplus>true</oracleSqlplus>
</configuration>
```

## Use Cases

### Configuring consistent sessions for your migrations

See our list of [supported SQL\*Plus commands](/documentation/database/oracle#sqlplus-commands) and how you can utilize them with [site and user profiles](/documentation/database/oracle#site-profiles-gloginsql--user-profiles-loginsql) once SQL\*Plus is enable to achieved this.
