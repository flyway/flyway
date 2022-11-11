---
layout: documentation
menu: configuration
pill: workingDirectory
subtitle: flyway.workingDirectory
redirect_from: /documentation/configuration/workingDirectory/
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
Not available

### Gradle
Not available

### Maven
```xml
<configuration>
    <workingDirectory>sql</workingDirectory>
</configuration>
```