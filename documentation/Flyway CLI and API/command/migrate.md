---
layout: documentation
menu: migrate
subtitle: Migrate
---
# Migrate

Migrates the schema to the latest version. Flyway will create the schema history table automatically if it doesn't exist.

![Migrate](/assets/balsamiq/command-migrate.png)

Migrate is the centerpiece of the Flyway workflow. It will scan the filesystem or your classpath for available migrations.
It will compare them to the migrations that have been applied to the database. If any difference is found, it will
migrate the database to close the gap.

Migrate should preferably be executed on application startup to avoid any incompatibilities between the database
    and the expectations of the code.

## Behavior

Executing migrate is idempotent and can be done safely regardless of the current version of the schema.

#### Example 1: We have migrations available up to version 9, and the database is at version 5.

Migrate will apply the migrations 6, 7, 8 and 9 in order.

#### Example 2: We have migrations available up to version 9, and the database is at version 9.

Migrate does nothing.

## Usage
{% include commandUsage.html command="migrate" %}

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/command/clean">Clean <i class="fa fa-arrow-right"></i></a>
</p>