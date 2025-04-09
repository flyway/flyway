---
subtitle: flyway.encoding
redirect_from: Configuration/encoding/
---

## Description

The encoding of SQL migrations.

The encodings that Flyway supports are:

- `US-ASCII`
- `ISO-8859-1`
- `UTF-8`
- `UTF-16BE`
- `UTF-16LE`
- `UTF-16`

We recommend using a consistent file encoding across all of your scripts to minimize the issues you encounter.
See the Troubleshooting section below for known problems and solutions.

Note that this setting can be set from [Script Configuration](<Script Configuration>) in addition to project configuration.

## Type

String

## Default

`"UTF-8"`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.
It will be honoured by Flyway Desktop when viewing or generating migration scripts.

### Command-line

```powershell
./flyway -encoding="UTF-16" info
```

### TOML Configuration File

```toml
[flyway]
encoding = "UTF-16"
```

### Configuration File

```properties
flyway.encoding=UTF-16
```

### Script Configuration File

```properties
flyway.encoding=UTF-16
```

### Environment Variable

```properties
FLYWAY_ENCODING=UTF-16
```

### API

```java
Flyway.configure()
    .encoding("UTF-16")
    .load()
```

### Gradle

```groovy
flyway {
    encoding = 'UTF-16'
}
```

### Maven

```xml
<configuration>
  <encoding>UTF-16</encoding>
</configuration>
```

## Troubleshooting

### My text editor doesn't support any of Flyway's supported encodings

This is because some encoding names are synonyms for others. For instance, an editor which specifies `ANSI` is actually
`ISO-8859-1`.

### I'm getting a MalformedInputException

This exception is due to inconsistent encoding configurations.
`ISO-8859-1` is the most compatible supported encoding, so using this encoding could fix your configuration. However, we recommend that all of your scripts have the same encoding.
