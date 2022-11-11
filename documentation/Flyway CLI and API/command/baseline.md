---
layout: documentation
menu: baseline
subtitle: Baseline
---
# Baseline

Baselines an existing database, excluding all migrations up to and including baselineVersion.

![Baseline](/assets/balsamiq/command-baseline.png)

Baseline is for introducing Flyway to [existing databases](/documentation/learnmore/existing) by baselining them
at a specific version. This will cause [Migrate](/documentation/command/migrate) to ignore all migrations
up to and including the baseline version. Newer migrations will then be applied as usual.

## Resetting the baseline

When you have many migrations, it might be desirable to reset your baseline migration. This will allow you to reduce the overhead of dealing with lots of scripts, many of which might be old and irrelevant.

<a class="btn btn-primary" href="https://flywaydb.org/reset-the-baseline-migration">Learn more about resetting the baseline migration</a>

## Usage
See [configuration](/documentation/configuration/parameters/#baseline) for baseline specific configuration parameters.
{% include commandUsage.html command="baseline" %}

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/command/repair">Repair <i class="fa fa-arrow-right"></i></a>
</p>
