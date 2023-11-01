---
pill: configuration
subtitle: configuration
redirect_from: Configuration/
---

# Configuration

Flyway has many different parameters that can be set to configure its behavior. These parameters can be set through a variety of different means, depending on how you are using Flyway.

## Usage

### Command Line
If using the command line, config parameters can be set via:
- Command line arguments 
  - e.g. `./flyway -url=jdbc:h2:mem:flyway info`
- [Configuration files](Configuration/Configuration Files) 
- Environment variables 
  - e.g. `FLYWAY_URL=jdbc:h2:mem:flyway`

You can find out about the evaluation order of these mechanisms in [CLI configuration order](Configuration/CLI Configuration Order)
### API
If [using the API](Usage/API Java), config parameters can be set via:
- Calling methods on the configuration object returned by `Flyway.configure()` 
  - e.g. `Flyway.configure().url("jdbc:h2:mem:flyway").load()`
- [Configuration files](Configuration/Configuration Files)
- Environment variables if the `.envVars()` method is called on the configuration object.

### Maven
If [using Maven](Usage/Maven Goal), config parameters can be set:
- On the configuration XML block in the maven config
- [Configuration files](Configuration/Configuration Files)
- Environment variables 
  - e.g. `FLYWAY_URL=jdbc:h2:mem:flyway`

### Gradle
If [using Gradle](Usage/Gradle Task), config parameters can be set:
- In the plugin configuration block
- [Configuration files](Configuration/Configuration Files)
- Environment variables 
  - e.g. `FLYWAY_URL=jdbc:h2:mem:flyway`

## Resolvers

Values beginning with `$` are treated as values to be resolved by *resolvers*. *Resolvers* are integrations with other value-storage systems such as [Vault](Tutorials/Tutorial Integrating Vault).

If you have a value starting with `$` which is not something to be resolved, it can be escaped by starting it with `$$`.

<div id="children">
{% include childPages.html %}
</div>