---
subtitle: Upgrading to Teams
redirect_from: /documentation/upgradingToTeams/
---
# Flyway Editions
- The Flyway Community edition contains all the core (free) features as well as Teams or Enterprise features which are made available based on your license.

- The Flyway Open Source edition just contains the open source components so upgrading requires a different installation of Flyway.
# Upgrading to Flyway Teams or Enterprise

This article assumes you are a proud owner of a Flyway Teams / Enterprise license. 

If you are not, head over to [Redgate Products Trial](https://www.red-gate.com/products/flyway/enterprise/trial) to start a free trial. Alternatively, you can
start a free trial using the [auth](Commands/Auth) command.
## Command Line
### Upgrading from Community
If you are currently using Flyway Community Edition then all you need do is authenticate with your Redgate credentials (see [Auth](Commands/Auth)) or provide a [License Key](Configuration/Parameters/Flyway/License Key) to unlock the advanced capabilities Flyway offers.

### Upgrading from Open Source


If you have been using the Open Source version of Flyway then you will need to download the [Flyway Community Edition](Usage/Command-Line) first.

## API
See details for Flyway Community in [Java(API)](Usage/API-Java)

For example:
```groovy
// gradle
dependencies {
    implementation 'com.redgate.flyway:flyway-core:{{ site.flywayVersion }}'
}

// code
Flyway flyway = Flyway.configure()
    .licenseKey("FL01...")
    .load();
flyway.info();
```

## Gradle
See details for Flyway Community in [Gradle Task](Usage/Gradle Task).

For example:
```groovy
plugins {
    id "com.redgate.flyway.flyway" version "{{ site.flywayVersion }}"
}

flyway {
    licenseKey = 'FL01...'
}
```

## Maven
See details for Flyway Community in [Maven Goal](Usage/Maven Goal).

For example:
```xml
<plugin>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <version>{{ site.flywayVersion }}</version>

    <configuration>
        <licenseKey>FL01...</licenseKey>
    </configuration>
</plugin>
```

# New Features

Now that you are using the Teams / Enterprise edition of Flyway, you can take advantage of all the powerful new features at your disposal:

- Begin creating [Undo migrations](Concepts/migrations#undo-migrations) to allow rollback of deployments.
- Begin storing your migrations in cloud storage such as [Amazon S3](Configuration/parameters/flyway/locations#amazon-s3) or [Google Cloud Storage](Configuration/parameters/flyway/locations#google-cloud-storage).
- Begin writing migrations in languages other than SQL and Java using [script migrations](Concepts/migrations#script-migrations).
- Preview your deployments, or execute them outside of Flyway using [Dry Runs](Concepts/Dry Runs).
- Optimise the execution of migrations using [batching](Configuration/parameters/flyway/batch) or [streaming](Configuration/parameters/flyway/stream).
- Gain more control over your deployments by [cherry picking](Configuration/Parameters/Flyway/Cherry Pick) which migrations to execute.
- Apply migrations manually outside of Flyway but update the schema history using [mark as applied](Configuration/Parameters/Flyway/Skip Executing Migrations).
- [Guaranteed support for databases](/https://flywaydb.org/download/faq#how-long-are-database-releases-supported-in-each-edition-of-flyway) up to 10 years old.
- Leverage the power of [Oracle SQL*Plus](Supported Databases/oracle database#sqlplus-commands) in your migrations.
- Promote database warnings to errors, or ignore errors thrown during execution with [error overrides](Concepts/Error Overrides).

... and much more. See [parameters](Configuration/parameters/) for all the Teams configuration parameters.
