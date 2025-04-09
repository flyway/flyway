---
subtitle: flyway.detectEncoding
---

## Description

Whether Flyway should attempt to auto-detect the file encoding of each migration. <br/>
**Note:** We recommend using a consistent file encoding to minimize the issues you encounter and specifying it to the [
`encoding`](<Configuration/Flyway Namespace/Flyway encoding Setting>)parameter. See [Troubleshooting](<Configuration/Flyway Namespace/Flyway Encoding Setting#troubleshooting>) for known problems and solutions.

Flyway can detect each of the following formats:

* `UTF-8`
* `ISO-8859-1`
* `UTF-16 BOMless`
* `UTF-16 LE`
* `UTF-16 BE`

When Flyway fails to auto-detect, it will default to the configured encoding if set, UTF-8 if not.

If a script configuration file defines an encoding, auto detection will be skipped on that file.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This is not supported by Flyway Desktop.

### Command-line

```powershell
. Namespace/Flyway encoding#troubleshooting) for known problems and solutions.

Flyway can detect each of the following formats:

* `UTF-8`
* `ISO-8859-1`
* `UTF-16 BOMless`
* `UTF-16 LE`
* `UTF-16 BE`

When Flyway fails to auto-detect, it will default to the configured encoding if set, UTF-8 if not.

If a script configuration file defines an encoding, auto detection will be skipped on that file.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This is not supported by Flyway Desktop.

### Command-line

```powershell
. flyway -detectEncoding="true" migrate
```

### TOML Configuration File

```toml
[flyway]
detectEncoding = true
```

### Configuration File

```properties
flyway.detectEncoding=true
```

### Environment Variable

```properties
FLYWAY_DETECT_ENCODING=true
```

### API

```java
Flyway.configure()
    .detectEncoding(true)
    .load()
```

### Gradle

```groovy
flyway {
    detectEncoding = true
}
```

### Maven

```xml
<configuration>
  <detectEncoding>true</detectEncoding>
</configuration>
```
