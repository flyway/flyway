---
subtitle: flyway.jarDirs
redirect_from: Configuration/jarDirs/
---

## Description

Array of directories containing JDBC drivers and Java-based migrations.
Relative paths will be resolved against your [working directory](<Command-line Parameters/Working Directory Parameter>).

Like with SQL migrations, we recommend storing Java-based migrations in a separate folder to your Flyway installation.
This allows upgrading the commandline package by simply replacing the whole installation folder.

## Type

String array

## Default

<nobr><i>&lt;install-dir&gt;</i>/jars</nobr>

*Note: We do not recommend relying on the default value. It may be changed in a future release.*

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -jarDirs="/my/jar/dir1,/my/jar/dir2" info
```

### TOML Configuration File

```toml
[flyway]
jarDirs = ["/my/jar/dir1", "/my/jar/dir2"]
```

### Configuration File

```properties
flyway.jarDirs=my/jar/dir1,my/jar/dir2
```

### Environment Variable

```properties
FLYWAY_JAR_DIRS=/my/jar/dir1,my/jar/dir2
```

### API

Not available

### Gradle

Not available

### Maven

Not available
