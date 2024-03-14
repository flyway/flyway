---
pill: workingDirectory
subtitle: flyway.workingDirectory
redirect_from: Configuration/workingDirectory/
---

# Working Directory

## Description
The working directory to consider when dealing with relative paths. If configured, this parameter will affect the below areas:
- Looking for the default [sql](Configuration/Parameters/Flyway/Locations) folder.
- Looking for the default [jars](Configuration/Parameters/Flyway/Jar Dirs) folder.
- Looking for default configuration files.
- The [Locations](Configuration/Parameters/Flyway/Locations) parameter
- The [Report Filename](Configuration/Parameters/Flyway/Report Filename) parameter

## Default
<i>empty</i>

## Usage

### Commandline
```powershell
./flyway -workingDirectory="sql" info
```

### TOML Configuration File
Not available

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

## Notes
If this parameter is left empty, Flyway will use the directory that Flyway is executed in.  