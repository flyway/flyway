---
subtitle: flyway.workingDirectory
redirect_from: Configuration/workingDirectory/
---

## Description

The working directory to consider when dealing with relative paths. If configured, this parameter will affect the below areas:

- Looking for the default [migration locations](<Configuration/Flyway Namespace/Flyway Locations Setting>) folder.
- Looking for the default [jars](<Configuration/Flyway Namespace/Flyway Jar Dirs Setting>) folder.
- Looking for the default [Rules Location](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Rules Location Setting>) folder.
- Looking for default [Configuration Files](https://documentation.red-gate.com/flyway/flyway-concepts/flyway-projects).
- The [`locations`](<Configuration/Flyway Namespace/Flyway Locations Setting>) parameter
- The [`jarDirs`](<Configuration/Flyway Namespace/Flyway Jar Dirs Setting>) parameter
- The [`configFiles`](<Command-line Parameters/Config Files Parameter>) parameter
- The [`reportFilename`](<Configuration/Flyway Namespace/Flyway Report Filename Setting>) parameter
- The [`snapshotFilename`](<Configuration/Flyway Namespace/Flyway Snapshot Namespace/Flyway Snapshot Filename Setting>) parameter
- The [`dryRunOutput`](<Configuration/Flyway Namespace/Flyway Dry Run Output Setting>) parameter
- The [`deployedSnapshot`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Deployed Snapshot Setting>) parameter
- The [`nextSnapshot`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Next Snapshot Setting>) parameter
- The [`filterFile`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Filter File Setting>) parameter
- The [`rulesLocation`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Rules Location Setting>) parameter 
- The [`rulesConfig`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Rules Config Setting>) parameter

## Type

String

## Default

<i>none</i>

## Usage

### Command-line

```powershell
./flyway -workingDirectory="my_project" info
```

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