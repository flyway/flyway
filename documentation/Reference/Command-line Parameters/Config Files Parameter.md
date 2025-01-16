---
pill: configFiles
subtitle: flyway.configFiles
redirect_from: Configuration/Configuration Filess/
---

## Description

The [Flyway configuration](https://documentation.red-gate.com/flyway/flyway-concepts/flyway-projects) files to load (in addition to those loaded automatically).

Relative paths will be resolved against the [Working Directory](<Command-line Parameters/Working Directory Parameter>).

_Note:_ It is possible to have Flyway read from the standard input using the special option `-configFiles=-`.
It is simpler to use environment variables and/or variable substitution if you need more dynamic configuration.

## Type

String array

## Default

`[]`

## Usage

### Command-line

```powershell
./flyway -configFiles="my.toml" info
```

To pass in multiple files, separate their names with commas:

```powershell
./flyway -configFiles=path/to/myAlternativeConfig.toml,other.toml migrate
```

This will also work with `.conf` config files.

### Command-line via standard input

Read a single option from `echo`:
```powershell
echo $'flyway.url=jdbc:h2:mem:mydb' | flyway info -configFiles=-
```

Read multiple options from `echo`, delimited by newlines:
```powershell
echo $'flyway.url=jdbc:h2:mem:mydb\nflyway.user=sa' | flyway info -configFiles=-
```

Use `cat` to read a config file and pipe it directly into Flyway:
```powershell
cat flyway.conf | flyway migrate -configFiles=-
```

Use `gpg` to encrypt a config file, then pipe it into Flyway.

Encrypt the config file:
```powershell
gpg -e -r "Your Name" flyway.conf
```

Decrypt the file and pipe it to Flyway:
```powershell
gpg -d -q flyway.conf.gpg | flyway info -configFiles=-
```

### Environment Variable

```properties
FLYWAY_CONFIG_FILES=my.toml
```

### API

Not available

### Gradle

```groovy
flyway {
    configFiles = ['my.conf']
}
```

### Maven

```xml
<configuration>
  <configFiles>
    <configFile>my.conf</configFile>
  </configFiles>
</configuration>
```
