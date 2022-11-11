---
layout: documentation
menu: configuration
pill: baselineDescription
subtitle: flyway.baselineDescription
redirect_from: /documentation/configuration/baselineDescription/
---

# Baseline Description

## Description
The Description to tag an existing schema with when executing [baseline](/documentation/command/baseline).

## Default
<nobr>&lt;&lt; Flyway Baseline &gt;&gt;</nobr>

## Usage

### Commandline
```powershell
./flyway -baselineDescription="Baseline" baseline
```

### Configuration File
```properties
flyway.baselineDescription=Baseline
```

### Environment Variable
```properties
FLYWAY_BASELINE_DESCRIPTION=Baseline
```

### API
```java
Flyway.configure()
    .baselineDescription("Baseline")
    .load()
```

### Gradle
```groovy
flyway {
    baselineDescription = 'Baseline'
}
```

### Maven
```xml
<configuration>
    <baselineDescription>Baseline</baselineDescription>
</configuration>
```