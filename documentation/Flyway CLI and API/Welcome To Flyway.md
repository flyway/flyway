---
title: Welcome To Flyway
---

Flyway is an database migration tool that strongly favors simplicity and convention over configuration.

It is based around just several core commands:
[Migrate](Commands/migrate), [Clean](Commands/clean), [Info](Commands/info), [Validate](Commands/validate), [Undo](Commands/undo), [Baseline](Commands/baseline) and [Repair](Commands/repair).

Plus an extended set of commands for more advanced use-cases:
[Auth](Commands/Auth), [Check](<Commands/Check command>) and [Snapshot](Commands/Snapshot)

Migrations can be written in:
- [SQL](Concepts/migrations#sql-based-migrations) (database-specific syntax (such as PL/SQL, T-SQL, \...) is supported)
- [Java](Concepts/migrations#java-based-migrations) (for advanced data
transformations or dealing with LOBs).

It has a [Command-line client](<Usage/Command Line>).
It also allows [Java API](Usage/api-java) integration for migrating the database on your application startup.
Alternatively, you can also use the [Maven plugin](<Usage/Maven Goal>) or [Gradle plugin](<Usage/Gradle Task>).

And if that's not enough, there are [plugins](Usage/Community%20Plugins%20and%20Integrations) available for
Spring Boot, Dropwizard, Grails, Play, SBT, Ant, Griffon, Grunt, Ninja and more!

## Next Steps
* If you haven't checked out the [Getting Started](https://documentation.red-gate.com/fd/getting-started-with-flyway-184127223.html) section yet, do it now. You'll be up and running in no time!
* If you are looking for a GUI experience with more comprehensive authoring tools then take a look at [Flyway Desktop](https://documentation.red-gate.com/fd/about-flyway-desktop-138346954.html)

## Supported databases

{% include supported-databases.html %}

