---
pill: configuration
subtitle: configuration
redirect_from: Configuration/
---

# Configuration

Flyway has many different parameters that can be set to configure its behavior. These parameters can be set through a variety of different means, depending on how you are using Flyway.

## Usage

### Command Line
If using the command line, config parameters can be set via command line arguments (e.g. `./flyway -url=jdbc:h2:mem:flyway info`), [configuration files](Configuration/Configuration Files), or environment variables (e.g. `FLYWAY_URL=jdbc:h2:mem:flyway`).

### Api
If using the api, config parameters can be set via calling methods on the configuration object returned by `Flyway.configure()` (e.g. `Flyway.configure().url("jdbc:h2:mem:flyway").load()`), [configuration files](Configuration/Configuration Files), or environment variables if the `.envVars()` method is called on the configuration object.

### Maven
If using maven, config parameters can be set on the configuration xml block in the maven config, [configuration files](Configuration/Configuration Files), or environment variables (e.g. `FLYWAY_URL=jdbc:h2:mem:flyway`).

### Gradle
If using gradle, config parameters can be set in the plugin configuration block, [configuration files](Configuration/Configuration Files), or environment variables (e.g. `FLYWAY_URL=jdbc:h2:mem:flyway`).

## Resolvers

Values beginning with `$` are treated as values to be resolved by *resolvers*. *Resolvers* are integrations with other value-storage systems such as [Vault](Tutorials/Tutorial Integrating Vault).

If you have a value starting with `$` which is not something to be resolved, it can be escaped by starting it with `$$`.

<div id="children">
{% include childPages.html %}
</div>