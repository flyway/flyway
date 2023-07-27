---
pill: workingDirectory
subtitle: flyway.workingDirectory
redirect_from: Configuration/workingDirectory/
---

# Working Directory

## Description
The working directory to consider when dealing with relative paths for both config files and locations.

## Default
<i>default for client</i>

## Usage

### Commandline
```powershell
./flyway -workingDirectory="sql" info
```

### Configuration File
Not available

### Environment Variable
Not available

### API
```java
Flyway.configure()
        .workingDirectory("working_Directory")
        .load()
```

### Gradle
Not available

### Maven
```xml
<configuration>
    <workingDirectory>sql</workingDirectory>
</configuration>
```
