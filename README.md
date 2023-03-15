# Fork of [Flyway-9.15.2](https://flywaydb.org)

See original [README](README)

## Why should you use this library?

Support Oracle 10, 11g

## How to you use this library as a replacement for org.coolbeevip.flywaydb:flyway-core

Make sure, that you only have one flyway dependency on your classpath. For example you can check the output of `mvn dependency:tree`.

#### by replacing a direct maven dependency

replace
```xml
<dependency>
    <groupId>org.flyway</groupId>
    <artifactId>flyway-core</artifactId>
    <version>9.15.2</version>
</dependency>
```
with
```xml
<dependency>
    <groupId>io.github.coolbeevip</groupId>
    <artifactId>flyway-core</artifactId>
    <version>9.15.2</version>
</dependency>
```

### by replacing jsch as a transitive maven dependency

```xml
<dependency>
    <groupId>io.github.coolbeevip</groupId>
    <artifactId>flyway-core</artifactId>
    <version>9.15.2</version>
</dependency>
```

## FAQ
