---
subtitle: Dry Runs
redirect_from: /documentation/dryruns/
---
# Dry Runs
{% include teams.html %}

When Flyway migrates a database, it looks for migrations that need to be applied, sorts them and applies them in order
directly against the database.

This default behavior is great for the vast majority of the cases.

There are however situations where you may want to
- preview the changes Flyway will make to the database
- submit the SQL statements for review to a DBA before applying them
- use Flyway to determine what needs updating, yet use a different tool to apply the actual database changes

[Flyway Teams Edition](https://www.red-gate.com/products/flyway/teams/trial/) gives you a way to achieve all these scenarios using **Dry Runs**.

## Usage

API/Maven/Gradle users will need to include `flyway-proprietary` as a dependency in order to use Dry run. See [API (Java)](Usage/API Java)


## How it works

When doing a Dry Run, Flyway sets up a read-only connection to the database. It assesses what migrations need to run and
generates a single SQL file containing all statements it would have executed in case of a regular migration
run. This SQL file can then be reviewed. If satisfactory, Flyway can then be instructed to migrate the database and
all changes will be applied. Alternatively a separate tool of your choice can also be used to apply the dry run SQL file
directly to the database without using Flyway. This SQL file also contains the necessary statements to create and update Flyway's
[schema history table](Concepts/migrations#schema-history-table), ensuring that all schema changes are tracked the usual way.

It is not advised to change a dry run script after it's been generated. Instead, any changes should be made to the migrations and a new dry run script generated. This is to ensure the changes executed match what's in your migrations.

### Intercepted in Dry Run

These changes are intercepted and written into a file as explained above.

- SQL versioned migrations
- SQL repeatable migrations
- SQL callbacks

#### New in V9:

These are no longer executed during a dry run. Instead, their file names are logged in the dry run output.

- [Arbitrary script migrations](Concepts/migrations#script-migrations)
- [Arbitrary script callbacks](Concepts/Callback concept#script-callbacks)
- [Java migrations](Concepts/migrations#java-based-migrations)
- [Java callbacks](Concepts/Callback concept#java-callbacks)

## Configuration

When using the Flyway [command-line tool](Usage/Command Line), [Maven plugin](Usage/Maven Goal) or
[Gradle plugin](usage/Gradle Task), a SQL file contained the output of the dry run can be configured using the
[`flyway.dryRunOutput`](Configuration/Parameters/Flyway/Dry Run Output) property. This can be on the local file
system, or in AWS S3 / Google Cloud Storage.

When using the API directly, the dry run output can be configured using a `java.io.OutputStream`, giving you additional
flexibility.

As soon as this property is set, Flyway kicks in dry run mode. The database is no longer modified and all SQL statements
that would have been applied are sent to the dry run output instead.

## Tutorial

Click [here](Tutorials/Tutorial Dry Runs) to see a tutorial on using dry runs.
