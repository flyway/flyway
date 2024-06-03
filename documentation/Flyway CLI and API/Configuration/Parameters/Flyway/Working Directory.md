---
pill: workingDirectory
subtitle: flyway.workingDirectory
redirect_from: Configuration/workingDirectory/
---

# Working Directory

## Description
The working directory to consider when dealing with relative paths. If configured, this parameter will affect the below areas:
- Looking for the default [migration locations](Configuration/Parameters/Flyway/Locations) folder.
- Looking for the default [jars](<Configuration/Parameters/Flyway/Jar Dirs>) folder.
- Looking for the default [Rules Location](<Configuration/Parameters/Flyway/Check/Rules Location>) folder.
- Looking for default [Configuration Files](<Configuration/Configuration Files>).
- The [`locations`](Configuration/Parameters/Flyway/Locations) parameter
- The [`jarDirs`](<Configuration/Parameters/Flyway/Jar Dirs>) parameter
- The [`configFiles`](<Configuration/Parameters/Flyway/Config Files>) parameter
- The [`reportFilename`](<Configuration/Parameters/Flyway/Report Filename>) parameter
- The [`snapshotFilename`](<Configuration/Parameters/Flyway/Snapshot Filename>) parameter
- The [`dryRunOutput`](<Configuration/Parameters/Flyway/Dry Run Output>) parameter 
- The [`deployedSnapshot`](<Configuration/Parameters/Flyway/Check/Deployed Snapshot>) parameter
- The [`nextSnapshot`](<Configuration/Parameters/Flyway/Check/Next Snapshot>) parameter
- The [`filterFile`](<Configuration/Parameters/Flyway/Check/Filter File>) parameter
- The [`rulesLocation`](<Configuration/Parameters/Flyway/Check/Rules Location>) parameter

## Default
<i>empty</i>

## Usage

### Commandline
```powershell
./flyway -workingDirectory="my_project" info
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
        .workingDirectory("my_project")
        .load()
```

### Gradle
Not available

### Maven
```xml
<configuration>
    <workingDirectory>my_project</workingDirectory>
</configuration>
```

## Notes
If this parameter is left empty, Flyway will use the directory that Flyway is executed in.  