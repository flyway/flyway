---
subtitle: Baseline
---
# Baseline

Baselines an existing database, excluding all migrations up to and including baselineVersion.

![Baseline](assets/command-baseline.png)

Baseline is for introducing Flyway to [existing databases](Learn More/existing-database-setup) by baselining them
at a specific version. This will cause [Migrate](Commands/migrate) to ignore all migrations
up to and including the baseline version. Newer migrations will then be applied as usual.

## Resetting the baseline

When you have many migrations, it might be desirable to reset your baseline migration. This will allow you to reduce the overhead of dealing with lots of scripts, many of which might be old and irrelevant.

Learn more about the concept of [Baseline Migrations](Concepts/Baseline Migrations) and a [Baseline Migration tutorial](Tutorials/Tutorial Baseline Migrations)

## Usage
See [configuration](Configuration/parameters/flyway/#baseline) for baseline specific configuration parameters.
{% include commandUsage.html command="Baseline" %}
