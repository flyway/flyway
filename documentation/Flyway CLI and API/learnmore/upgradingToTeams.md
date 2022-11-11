---
layout: documentation
menu: upgradingToTeams
subtitle: Upgrading to Teams
redirect_from: /documentation/upgradingToTeams/
---

# Upgrading to Flyway Teams / Enterprise

This article assumes you are a proud owner of a Flyway Teams / Enterprise license. If you are not, head over to the [download/pricing page](/download) to purchase a license or start a free trial.

## Upgrading from Community

If you are currently using Flyway Community and have obtained a license, first you will need to download Flyway Teams / Enterprise. You can find a link in the relevant usage section below. Please see [license key](/documentation/configuration/parameters/licenseKey) to read about where to configure your license key.

### Command Line

If you are using the command line, first you need to download Flyway Teams / Enterprise [here](/documentation/usage/commandline/#tab-teams). Then, simply provide your license key through any of the [supported methods](/documentation/configuration/parameters/licenseKey).

For example:
```powershell
./flyway -licenseKey=FL01... info
```

### API

If you are using the API, simply swap your dependency from `org.flywaydb` to `org.flywaydb.enterprise`. Then provide your license key through any of the [supported methods](/documentation/configuration/parameters/licenseKey).

For example:
```groovy
// gradle
dependencies {
    compile 'org.flywaydb.enterprise:flyway-core:{{ site.flywayVersion }}'
}

// code
Flyway flyway = Flyway.configure()
    .licenseKey("FL01...")
    .load();
flyway.info();
```

### Gradle

If you are using the Gradle plugin, swap the plugin dependency from `id "org.flywaydb.flyway" version "{{ site.flywayVersion }}"` to `id "org.flywaydb.enterprise.flyway" version "{{ site.flywayVersion }}"`. Then provide your license key through any of the [supported methods](/documentation/configuration/parameters/licenseKey).

For example:
```groovy
plugins {
    id "org.flywaydb.enterprise.flyway" version "{{ site.flywayVersion }}"
}

flyway {
    licenseKey = 'FL01...'
}
```

### Maven

If you are using the Maven plugin, swap the plugin dependency from `<groupId>org.flywaydb</groupId>` to `<groupId>org.flywaydb.enterprise</groupId>`. Then provide your license key through any of the [supported methods](/documentation/configuration/parameters/licenseKey).

For example:
```xml
<plugin>
    <groupId>org.flywaydb.enterprise</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <version>{{ site.flywayVersion }}</version>

    <configuration>
        <licenseKey>FL01...</licenseKey>
    </configuration>
</plugin>
```

## New Features

Now that you are using the Teams / Enterprise edition of Flyway, you can take advantage of all the powerful new features at your disposal:

- Begin creating [Undo migrations](/documentation/concepts/migrations#undo-migrations) to allow rollback of deployments.
- Begin storing your migrations in cloud storage such as [Amazon S3](/documentation/configuration/parameters/locations#amazon-s3) or [Google Cloud Storage](/documentation/configuration/parameters/locations#google-cloud-storage).
- Begin writing migrations in languages other than SQL and Java using [script migrations](/documentation/concepts/migrations#script-migrations).
- Preview your deployments, or execute them outside of Flyway using [Dry Runs](/documentation/concepts/dryruns).
- Optimise the execution of migrations using [batching](/documentation/configuration/parameters/batch) or [streaming](/documentation/configuration/parameters/stream).
- Gain more control over your deployments by [cherry picking](/documentation/configuration/parameters/cherryPick) which migrations to execute.
- Apply migrations manually outside of Flyway but update the schema history using [mark as applied](/documentation/configuration/parameters/skipExecutingMigrations).
- [Guaranteed support for databases](/download/faq#how-long-are-database-releases-supported-in-each-edition-of-flyway) up to 10 years old.
- Leverage the power of [Oracle SQL*Plus](/documentation/database/oracle#sqlplus-commands) in your migrations.
- Promote database warnings to errors, or ignore errors thrown during execution with [error overrides](/documentation/concepts/erroroverrides).

... and much more. See [parameters](/documentation/configuration/parameters/) for all the Teams configuration parameters.
