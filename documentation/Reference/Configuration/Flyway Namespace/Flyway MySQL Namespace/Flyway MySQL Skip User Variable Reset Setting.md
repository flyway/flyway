---
subtitle: flyway.mysql.skipUserVariableReset
redirect_from: Configuration/mysqlSkipUserVariableReset/
---

## Description

Skip the user variable reset capability check during MySQL connection initialization. 

When Flyway connects to a MySQL database, it attempts to determine if the database supports user variable reset by querying the `performance_schema.user_variables_by_thread` system table. This check requires `SELECT` permission on `performance_schema` tables.

If your MySQL user account does not have permission to access `performance_schema`, you can enable this setting to skip the check. This will prevent `SQLSyntaxErrorException` errors during Flyway initialization.
This exception can cause critical issues when using connection pools like Druid, which treat this error as fatal and disable the affected connection, potentially triggering cascading failures in dependent components.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -mysql.skipUserVariableReset="true" migrate
```

### TOML Configuration File

```toml
[flyway.mysql]
skipUserVariableReset = true
```

### Configuration File

```properties
flyway.mysql.skipUserVariableReset=true
```

### Environment Variable

```properties
FLYWAY_MYSQL_SKIP_USER_VARIABLE_RESET=true
```

### API

```java
MysqlConfigurationExtension mysqlConfigurationExtension = configuration.getConfigurationExtension(MysqlConfigurationExtension.class);
mysqlConfigurationExtension.setSkipUserVariableReset(true);
```

### Gradle

```groovy
flyway {
    mysqlSkipUserVariableReset = true
}
```

### Maven

```xml
<configuration>
    <mysqlSkipUserVariableReset>true</mysqlSkipUserVariableReset>
</configuration>
```

### Spring Boot

When using Spring Boot with Flyway, you can configure this setting in your `application.yml` or `application.properties`:
#### Configuration customizer
```java

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.database.mysql.MysqlConfigurationExtension;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConditionalOnClass(name = "org.flywaydb.database.mysql.MysqlConfigurationExtension")
@ConfigurationProperties("spring.flyway.mysql")
@Order(1)
@Data
public class MysqlFlywayConfigurationCustomizer implements FlywayConfigurationCustomizer {

    private Boolean skipUserVariableReset;

    @Override
    public void customize(FluentConfiguration configuration) {
        if (skipUserVariableReset != null) {
            MysqlConfigurationExtension mysqlConfig = configuration.getPluginRegister().getExact(MysqlConfigurationExtension.class);
            if(mysqlConfig != null){
                mysqlConfig.setSkipUserVariableReset(skipUserVariableReset);
            }
        }
    }
}
```
#### application.yml

```yaml
spring:
  flyway:
    mysql:
      skip-user-variable-reset: true
```

or using kebab-case:

```yaml
spring:
  flyway:
    mysql:
      skipUserVariableReset: true
```

#### application.properties

```properties
spring.flyway.properties.flyway.mysql.skip-user-variable-reset=true
```

## Use Cases

### Database user with restricted permissions

When your MySQL database user is restricted and does not have `SELECT` permission on `performance_schema.user_variables_by_thread`, you will encounter the following error:

```
java.sql.SQLSyntaxErrorException: SELECT command denied to user 'your_user'@'your_host' for table 'user_variables_by_thread'
```

In this case, enable `skipUserVariableReset` to bypass the capability check:

```properties
flyway.mysql.skipUserVariableReset=true
```

This allows Flyway to initialize successfully without requiring additional database permissions.

### Cloud-managed databases with restricted system table access

Some cloud database providers (e.g., AWS RDS, Azure Database for MySQL) may restrict access to certain `performance_schema` tables for security reasons. Setting this option to `true` allows Flyway to work with these restricted database instances.

## Related Configuration

- [Flyway MySQL Namespace](<Configuration/Flyway Namespace/Flyway MySQL Namespace>) - Overview of all MySQL-specific configurations
