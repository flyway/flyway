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

<a style="text-decoration: none; background: rgb(204,0,0); padding: 6px 40px; border-radius: 10px; color: white; font-weight: bold;" href="https://flywaydb.org/reset-the-baseline-migration">Learn more about resetting the baseline migration</a>

## Usage
See [configuration](Configuration/parameters/#baseline) for baseline specific configuration parameters.
{% include commandUsage.html command="Baseline" %}
