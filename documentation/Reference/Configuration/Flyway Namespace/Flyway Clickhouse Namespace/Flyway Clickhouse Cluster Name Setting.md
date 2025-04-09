---
subtitle: Flyway Clickhouse ClusterName
---

## Description

The name of the Clickhouse cluster.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

```powershell
./flyway -clickhouse.clusterName="example_cluster" info
```

### TOML Configuration File

```toml
[flyway.clickhouse]
clusterName="example_cluster"
```

### Configuration File

```properties
flyway.clickhouse.clusterName=example_cluster
```

### Environment Variable

```properties
FLYWAY_CLICKHOUSE_CLUSTER_NAME=example_cluster
```

### API

```java
ClickHouseConfigurationExtension clickHouseConfigurationExtension = configuration.getPluginRegister().getPlugin(ClickHouseConfigurationExtension.class);
clickHouseConfigurationExtension.setClusterName("example_cluster");
```

### Gradle

```groovy
flyway {
    pluginConfiguration = [
        clickhouseClusterName: 'example_cluster'
    ]
}
```

### Maven

```xml
<configuration>
    <pluginConfiguration>
        <clickhouseClusterName>example_cluster</clickhouseClusterName>
    </pluginConfiguration>
</configuration>
```
