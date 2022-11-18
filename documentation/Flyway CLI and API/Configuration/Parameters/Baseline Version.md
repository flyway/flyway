---
pill: baselineVersion
subtitle: flyway.baselineVersion
redirect_from: Configuration/baselineVersion/
---

# Baseline Version

## Description
The version to tag an existing schema with when executing [baseline](Commands/baseline).

## Default
1

## Usage

### Commandline
```powershell
./flyway -baselineVersion="0.0" baseline
```

### Configuration File
```properties
flyway.baselineVersion=0.0
```

### Environment Variable
```properties
FLYWAY_BASELINE_VERSION=0.0
```

### API
```java
Flyway.configure()
    .baselineVersion("0.0")
    .load()
```

### Gradle
```groovy
flyway {
    baselineVersion = '0.0'
}
```

### Maven
```xml
<configuration>
    <baselineVersion>0.0</baselineVersion>
</configuration>
```
