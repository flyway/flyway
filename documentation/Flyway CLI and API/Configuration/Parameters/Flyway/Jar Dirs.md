---
pill: jarDirs
subtitle: flyway.jarDirs
redirect_from: Configuration/jarDirs/
---

# Jar Dirs

## Description
Comma-separated list of directories containing JDBC drivers and Java-based migrations.

Like with SQL migrations, we recommend storing Java-based migrations in a separate folder to your Flyway installation.
This allows upgrading the commandline package by simply replacing the whole installation folder.

## Default
<nobr><i>&lt;install-dir&gt;</i>/jars</nobr>

*Note: We do not recommend relying on the default value. It may be changed in a future release.*

## Usage

This configuration parameter will only be used in the command line version of Flyway.

### Commandline
```powershell
./flyway -jarDirs="/my/jar/dir1,/my/jar/dir2" info
```

### TOML Configuration File
```toml
[environments.default]
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
