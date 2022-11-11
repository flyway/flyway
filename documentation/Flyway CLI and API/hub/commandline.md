---
layout: documentation
menu: hub_commandline
subtitle: Using the `flywayhub` command line
redirect_from: /documentation/hub-commandline
---

# Using the `flywayhub` command line

In addition to the <a href="hub.flywaydb.org">Flyway Hub UI</a>, Flyway Hub also offers a command line based workflow for running migration script tests against our ephemeral database instances.

To get started with the `flywayhub` command line, download and install the right version for your platform following the <a href="https://hub.flywaydb.org/projects/new/integration">instructions on Flyway Hub</a>.

## Authentication

Before running tests users first need to authenticate with Flyway Hub using the `flywayhub auth` command:

```
flywayhub auth
```

This will open a browser window with a code displayed. Hit 'Submit' and return to the terminal.

## Running tests

The `flywayhub test` command allows you to run your migration scripts from the command line against an automatically provisioned, ephemeral database instance. This functionality mirrors what you can do in the Flyway Hub UI with the 'Run checks' button on the project view.

For example:

```
flywayhub test --project myproject --engine 'PostgreSQL (v13.2)' ./sql
```

will do the following steps in order:

1. Provision a fresh Postgres v13.2 instance in the cloud with an empty database.
2. Use `flyway migrate` to run the migration scripts in `./sql` against the newly provisioned database.
3. Optionally, run <a href="https://www.sqlfluff.com/">sqlfluff</a> against the migrations in `./sql` to produce a linting report. This step is skipped if `sqlfluff` is not installed on your system.
4. Upload the results of the migration (and linting) to Flyway Hub.
5. Tear down the database instance.

## Hard coded database names

While not recommended, it is possible for Flyway migration scripts to contain references to specific database names. In this case, the migrations will only run against a database of that name. To support this use case, provide the `--database` flag to the `flywayhub` command. For example:

```
flywayhub test --project myproject --engine 'SQL Server (v2017)' --database mydatabase ./sql
```

This will ensure that your migrations run against a database called 'mydatabase' by creating an empty database of that name in the automatically provisioned instance.

## Flyway config files

Using the `--flywayconf` flag, it is possible to supply the `flyway.conf` file that your project uses to the `flywayhub` command. This might be necessary if you have defined placeholder values, for example. Running:

```
flywayhub test --project myproject --engine 'SQL Server (v2017)' --flywayconf path/to/flyway.conf ./sql
```

Ensures that your `flyway.conf` file will be used for the test run.

## Supported engines

Flyway Hub supports the following database engines and versions. These strings are the valid arguments for the `--engine` flag:

* 'SQL Server (v2017)'
* 'SQL Server (v2019)'
* 'PostgreSQL (v11.0)'
* 'PostgreSQL (v12.0)'
* 'PostgreSQL (v13.2)'
* 'MySQL (v5.7)'
* 'MySQL (v8.0)'
* 'MariaDB (v10.6)'

## Automation

The `flywayhub` CLI is a key component when using Flyway Hub in automation settings like CI pipelines. The next section describes how to set up automation using Flyway Hub to test your migrations on every commit.

<a href="/documentation/hub/automation"
        class="btn btn-primary">Automation <i class="fa fa-arrow-right"></i></a>

