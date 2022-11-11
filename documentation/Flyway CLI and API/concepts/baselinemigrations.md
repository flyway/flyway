---
layout: documentation
menu: baselinemigrations
subtitle: Baseline Migrations
redirect_from: /documentation/concepts/statescripts
---
# Baseline Migrations
{% include teams.html %}

Over the lifetime of a project, many database objects may be created and destroyed across many migrations which leaves behind a lengthy history of migrations that need to be applied in order to bring a new environment up to speed.

Instead, you might wish to add a single, cumulative migration that represents the state of your database after all of those migrations have been applied without disrupting existing environments.

[Flyway Teams Edition](/try-flyway-teams-edition) gives you a way to achieve this using **Baseline Migrations**.

## How it works

Baseline migrations are prefixed with `B` followed by the version of your database they represent. For example: `B5__my_database.sql` represents the state of your database after applying all versioned migrations up to and including `V5`.

Baseline migrations are only used when deploying to new environments. If used in an environment where some Flyway migrations have already been applied, baseline migrations will be ignored. New environments will choose the latest baseline migration as the starting point when you run `migrate`. Every migration with a version below the latest baseline migration's version is marked as `ignored`. <br/>
Note that:
- repeatable migrations are executed as normal
- baseline migrations do not replace versioned migrations - you can have both a baseline migration and a versioned migration at the same version
- baseline migrations are not affiliated with the `baseline` command and are executed during the `migrate` process

This mechanism is fully automated and requires no modification in your pipeline to begin using. Simply add your baseline migrations when you need them and they will be utilized.

## Configuration

The `B` prefix is configurable with the [baselineMigrationPrefix](/documentation/configuration/parameters/baselineMigrationPrefix) parameter.

## Compatibility Note

Baseline migrations were formally known as State Scripts. These can be used as baseline migrations without modification, by either changing the prefix of your state scripts to be `B` or setting the value of the [baselineMigrationPrefix](/documentation/configuration/parameters/baselineMigrationPrefix) parameter to `S`.

## Tutorial

Click [here](/documentation/tutorials/baselineMigrations) to see a tutorial on using baseline migrations.

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/command/migrate">Migrate<i class="fa fa-arrow-right"></i></a>
</p>
