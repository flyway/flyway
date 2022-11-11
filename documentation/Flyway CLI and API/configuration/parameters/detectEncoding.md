---
layout: documentation
menu: configuration
pill: detectEncoding
subtitle: flyway.detectEncoding
---

# Detect Encoding
{% include teams.html %}

## Description
Whether Flyway should attempt to auto-detect the file encoding of each migration. <br/>
**NOTE:** We recommend using a consistent file encoding to minimize the issues you encounter and specifying it to the [`encoding`](/documentation/configuration/parameters/encoding) parameter. See [Troubleshooting](/documentation/configuration/parameters/encoding#troubleshooting) for known problems and solutions.

Flyway can detect each of the following formats:

* `UTF-8`
* `ISO-8859-1`
* `UTF-16 BOMless`
* `UTF-16 LE`
* `UTF-16 BE`

When Flyway fails to auto-detect, it will default to the configured encoding if set, UTF-8 if not.

If a script configuration file defines an encoding, auto detection will be skipped on that file.

## Default
false

## Usage

### Commandline
```powershell
./flyway -detectEncoding="true" migrate
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