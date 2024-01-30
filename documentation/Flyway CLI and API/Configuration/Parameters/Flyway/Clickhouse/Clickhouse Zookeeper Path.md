---
pill: clickhouseZookeeperPath
subtitle: flyway.clickhouse.zookeeperPath
---

# Clickhouse Zookeeper Path

## Description
The path to the Zookeeper node that contains the Clickhouse cluster configuration.

## Usage

### Commandline
```powershell
./flyway -clickhouse.zookeeperPath="/clickhouse/tables/{shard}/{database}/{table}" info
```

### TOML Configuration File
```toml
[flyway]
clickhouse.zookeeperPath = "/clickhouse/tables/{shard}/{database}/{table}"
```

### Configuration File
```properties
flyway.clickhouse.zookeeperPath=/clickhouse/tables/{shard}/{database}/{table}
```

### Environment Variable
```properties
FLYWAY_CLICKHOUSE_ZOOKEEPER_PATH=/clickhouse/tables/{shard}/{database}/{table}
```

### API
```java
ClickHouseConfigurationExtension clickHouseConfigurationExtension = configuration.getPluginRegister().getPlugin(ClickHouseConfigurationExtension.class);
clickHouseConfigurationExtension.setZookeeperPath("/clickhouse/tables/{shard}/{database}/{table}");
```

### Gradle
```groovy
flyway {
    pluginConfiguration = [
        clickhouseZookeeperPath: '/clickhouse/tables/{shard}/{database}/{table}'
    ]
}
```

### Maven
```xml
<configuration>
    <pluginConfiguration>
        <clickhouseZookeeperPath>/clickhouse/tables/{shard}/{database}/{table}</clickhouseZookeeperPath>
    </pluginConfiguration>
</configuration>
```
