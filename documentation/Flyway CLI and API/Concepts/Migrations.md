---
subtitle: Migrations
redirect_from: /documentation/migrations/
---
# Migrations

<div id="toc"></div>

## Overview

With Flyway all changes to the database are called **migrations**. Migrations can be either *versioned* or
*repeatable*. Versioned migrations come in 2 forms: regular and *undo*.

**Versioned migrations** have a *version*, a *description* and a *checksum*. The version must be unique. The description is purely
informative for you to be able to remember what each migration does. The checksum is there to detect accidental changes.
Versioned migrations are the most common type of migration. They are applied in order exactly once.

Optionally their effect can be undone by supplying an **undo migration** with the same version.

**Repeatable migrations** have a description and a checksum, but no version. Instead of being run just once, they are
(re-)applied every time their checksum changes.

Within a single migration run, repeatable migrations are always applied last, after all pending versioned migrations
have been executed. Repeatable migrations are applied in the order of their description.

By default both versioned and repeatable migrations can be written either in **[SQL](Concepts/migrations#sql-based-migrations)**
or in **[Java](Concepts/migrations#java-based-migrations)** and can consist of multiple statements.

Flyway automatically discovers migrations on the *filesystem* and on the Java *classpath*.

To keep track of which migrations have already been applied when and by whom, Flyway adds a *schema history table*
to your schema.

## Versioned Migrations

The most common type of migration is a **versioned migration**. Each versioned migration has a *version*, a *description*
and a *checksum*. The version must be unique. The description is purely
informative for you to be able to remember what each migration does. The checksum is there to detect accidental changes.
Versioned migrations are applied in order exactly once.

Versioned migrations are typically used for:
- Creating/altering/dropping tables/indexes/foreign keys/enums/UDTs/...
- Reference data updates
- User data corrections

Here is a small example:

```sql
CREATE TABLE car (
    id INT NOT NULL PRIMARY KEY,
    license_plate VARCHAR NOT NULL,
    color VARCHAR NOT NULL
);

ALTER TABLE owner ADD driver_license_id VARCHAR;

INSERT INTO brand (name) VALUES ('DeLorean');
```

Each versioned migration must be assigned a **unique version**. Any version is valid as long as it conforms to the usual
dotted notation. For most cases a simple increasing integer should be all you need. However Flyway is quite flexible and
all these versions are valid versioned migration versions:
- 1
- 001
- 5.2
- 1.2.3.4.5.6.7.8.9
- 205.68
- 20130115113556
- 2013.1.15.11.35.56
- 2013.01.15.11.35.56

Versioned migrations are applied in the order of their versions. Versions are sorted numerically as you would normally expect.

## Undo Migrations
{% include teams.html %}

**Undo migrations** are the opposite of regular versioned migrations. An undo migration is responsible for undoing the effects
of the versioned migration with the same version. Undo migrations are optional and not required to run regular versioned migrations.

For the example above, this is how the undo migration would look like:
```sql
DELETE FROM brand WHERE name='DeLorean';

ALTER TABLE owner DROP driver_license_id;

DROP TABLE car;
```

### Important Notes

While the idea of undo migrations is nice, unfortunately it sometimes breaks down in practice. As soon as
you have destructive changes (drop, delete, truncate, ...), you start getting into trouble. And even if you don't,
you end up creating home-made alternatives for restoring backups, which need to be properly tested as well.

Undo migrations assume the whole migration succeeded and should now be undone. This does not help with failed versioned
migrations on databases without DDL transactions. Why? A migration can fail at any point. If you have 10 statements,
it is possible for the 1st, the 5th, the 7th or the 10th to fail. There is
simply no way to know in advance. In contrast, undo migrations are written to undo an entire versioned migration and will not
help under such conditions.

An alternative approach which we find preferable is to **maintain backwards compatibility
between the DB and all versions of the code currently deployed in production**. This way a
failed migration is not a disaster. The old version of the application is still compatible with the DB, so you
can simply roll back the application code, investigate, and take corrective measures.

This should be complemented with a **proper, well tested, backup and restore strategy**. It is independent
of the database structure, and once it is tested and proven to work, no migration script can break it. For
optimal performance, and if your infrastructure supports this, we recommend using the snapshot
technology of your underlying storage solution. Especially for larger data volumes, this can be
several orders of magnitude faster than traditional backups and restores.

## Repeatable Migrations

**Repeatable migrations** have a description and a checksum, but no version. Instead of being run just once, they are
(re-)applied every time their checksum changes.

This is very useful for managing database objects whose definition can then simply be maintained in a single file in
version control. They are typically used for
- (Re-)creating views/procedures/functions/packages/...
- Bulk reference data reinserts

Within a single migration run, repeatable migrations are always applied last, after all pending versioned migrations have been executed. Repeatable migrations are applied in the order of their description.

It is your responsibility to ensure the same repeatable migration can be applied multiple times. This usually
involves making use of `CREATE OR REPLACE` clauses in your DDL statements.

Here is an example of what a repeatable migration looks like:

```sql
CREATE OR REPLACE VIEW blue_cars AS
    SELECT id, license_plate FROM cars WHERE color='blue';
```

## SQL-based migrations

Migrations are most commonly written in **SQL**. This makes it easy to get started and leverage any existing scripts,
tools and skills. It gives you access to the full set of capabilities of your database and eliminates the need to
understand any intermediate translation layer.

SQL-based migrations are typically used for
- DDL changes (CREATE/ALTER/DROP statements for TABLES,VIEWS,TRIGGERS,SEQUENCES,...)
- Simple reference data changes (CRUD in reference data tables)
- Simple bulk data changes (CRUD in regular data tables)

### Naming

In order to be picked up by Flyway, SQL migrations must comply with the following naming pattern:

<div class="row">
    <div class="col-md-4">
        <h4>Versioned Migrations</h4>
        <pre>Prefix  Separator       Suffix
   <i class="fa fa-long-arrow-down" style="padding: 4px"></i>   <i class="fa fa-long-arrow-down" style="padding: 4px"></i>                <i class="fa fa-long-arrow-down" style="padding: 4px"></i>
   <span style="color: white; font-weight: bold"><span style="background-color: #0000AA; padding: 4px">V</span><span style="background-color: #AA0000; padding: 4px">2</span><span style="background-color: #00AA00; padding: 4px">__</span><span style="background-color: #AAAA00; padding: 4px">Add_new_table</span><span style="background-color: #00AAAA; padding: 4px">.sql</span></span>
     <i class="fa fa-long-arrow-up" style="padding: 4px"></i>         <i class="fa fa-long-arrow-up" style="padding: 4px"></i>
 Version    Description</pre>
    </div>
    <div class="col-md-4">
        <h4>Undo Migrations</h4>
        <pre>Prefix  Separator       Suffix
   <i class="fa fa-long-arrow-down" style="padding: 4px"></i>   <i class="fa fa-long-arrow-down" style="padding: 4px"></i>                <i class="fa fa-long-arrow-down" style="padding: 4px"></i>
   <span style="color: white; font-weight: bold"><span style="background-color: #0000AA; padding: 4px">U</span><span style="background-color: #AA0000; padding: 4px">2</span><span style="background-color: #00AA00; padding: 4px">__</span><span style="background-color: #AAAA00; padding: 4px">Add_new_table</span><span style="background-color: #00AAAA; padding: 4px">.sql</span></span>
     <i class="fa fa-long-arrow-up" style="padding: 4px"></i>         <i class="fa fa-long-arrow-up" style="padding: 4px"></i>
 Version    Description</pre>
    </div>
    <div class="col-md-4">
        <h4>Repeatable Migrations</h4>
        <pre>Prefix Separator       Suffix
    <i class="fa fa-long-arrow-down" style="padding: 4px"></i> <i class="fa fa-long-arrow-down" style="padding: 4px"></i>                <i class="fa fa-long-arrow-down" style="padding: 4px"></i>
    <span style="color: white; font-weight: bold"><span style="background-color: #0000AA; padding: 4px">R</span><span style="background-color: #00AA00; padding: 4px">__</span><span style="background-color: #AAAA00; padding: 4px">Add_new_table</span><span style="background-color: #00AAAA; padding: 4px">.sql</span></span>
               <i class="fa fa-long-arrow-up" style="padding: 4px"></i>
           Description</pre>
    </div>
</div>

The file name consists of the following parts:
- **Prefix**: `V` for versioned ([configurable](Configuration/Parameters/SQL Migration Prefix)),
`U` for undo ([configurable](Configuration/Parameters/Undo SQL Migration Prefix)) and
`R` for repeatable migrations ([configurable](Configuration/Parameters/Repeatable SQL Migration Prefix))
- **Version**: Version with dots or underscores separate as many parts as you like (Not for repeatable migrations)
- **Separator**: `__` (two underscores) ([configurable](Configuration/Parameters/SQL Migration Separator))
- **Description**: Underscores or spaces separate the words
- **Suffix**: `.sql` ([configurable](Configuration/Parameters/SQL Migration Suffixes))

Optionally versioned SQL migrations can also omit both the separator and the description.

The configuration option <code>validateMigrationNaming</code> determines how Flyway handles files that do not correspond with
the naming pattern when carrying out a migration: if false then Flyway will simply ignore all such files, if true then
Flyway will fail fast and list all files which need to be corrected.

### Discovery

Flyway discovers SQL-based migrations from one or more directories referenced by the **[`locations`](Configuration/parameters/locations)**
property.
- Unprefixed locations or locations with the `classpath:` prefix target the Java classpath.
- Locations with the `filesystem:` prefix search the file system.
- Locations with the `s3:` prefix search AWS S3 buckets. To use AWS S3, the [AWS SDK v2](https://mvnrepository.com/artifact/software.amazon.awssdk/services) and dependencies must be included, and [configured](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html) for your S3 account.
- Locations with the `gcs:` prefix search Google Cloud Storage buckets. To use GCS, the [GCS library](https://cloud.google.com/storage/docs/reference/libraries#auth-cloud-implicit-java) must be included, and the GCS environment variable <code>GOOGLE_APPLICATION_CREDENTIALS</code> must be set to the credentials file for the service account that has access to the bucket.

<pre class="filetree"><i class="fa fa-folder-open"></i> my-project
  <i class="fa fa-folder-open"></i> src
    <i class="fa fa-folder-open"></i> main
      <i class="fa fa-folder-open"></i> resources
        <span><i class="fa fa-folder-open"></i> db
  <i class="fa fa-folder-open"></i> migration</span>                <i class="fa fa-long-arrow-left"></i> <code>classpath:db/migration</code>
            <i class="fa fa-file-text"></i> R__My_view.sql
            <i class="fa fa-file-text"></i> U1.1__Fix_indexes.sql
            <i class="fa fa-file-text"></i> U2__Add a new table.sql
            <i class="fa fa-file-text"></i> V1__Initial_version.sql
            <i class="fa fa-file-text"></i> V1.1__Fix_indexes.sql
            <i class="fa fa-file-text"></i> V2__Add a new table.sql
  <span><i class="fa fa-folder-open"></i> my-other-folder</span>                  <i class="fa fa-long-arrow-left"></i> <code>filesystem:/my-project/my-other-folder</code>
    <i class="fa fa-file-text"></i> U1.2__Add_constraints.sql
    <i class="fa fa-file-text"></i> V1.2__Add_constraints.sql</pre>

New SQL-based migrations are **discovered automatically** through filesystem and Java classpath scanning at runtime.
Once you have configured the [`locations`](Configuration/parameters/locations) you want to use, Flyway will
automatically pick up any new SQL migrations as long as they conform to the configured *naming convention*.

This scanning is recursive. All migrations in non-hidden directories below the specified ones are also picked up.

### Syntax

Flyway supports all regular SQL syntax elements including:
- Single- or multi-line statements
- Single- (--) or Multi-line (/* */) comments spanning complete lines
- Database-specific SQL syntax extensions (PL/SQL, T-SQL, ...) typically used to define stored procedures, packages, ...

Additionally in the case of Oracle, Flyway also supports [SQL*Plus commands](Supported Databases/oracle#sqlplus-commands).

### Placeholder Replacement
In addition to regular SQL syntax, Flyway also supports placeholder replacement with configurable pre- and suffixes.
By default it looks for Ant-style placeholders like `${myplaceholder}`. This can be very useful to abstract differences between environments.

See [Placeholders](Configuration/Placeholders Configuration).

## Java-based migrations

Java-based migrations are a great fit for all changes that can not easily be expressed using SQL.

These would typically be things like
- BLOB &amp; CLOB changes
- Advanced bulk data changes (Recalculations, advanced format changes, ...)

### Naming

In order to be picked up by Flyway, Java-based Migrations must implement the
[`JavaMigration`](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/api/migration/JavaMigration.html) interface. Most users
however should inherit from the convenience class [`BaseJavaMigration`](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/api/migration/BaseJavaMigration.html)
instead as it encourages Flyway's default naming convention, enabling Flyway to automatically extract the version and
the description from the class name. To be able to do so, the class name must comply with the following naming pattern:

<div class="row">
    <div class="col-md-4">
        <h4>Versioned Migrations</h4>
        <pre>Prefix  Separator
   <i class="fa fa-long-arrow-down" style="padding: 4px"></i>   <i class="fa fa-long-arrow-down" style="padding: 4px"></i>
   <span style="color: white; font-weight: bold"><span style="background-color: #0000AA; padding: 4px">V</span><span style="background-color: #AA0000; padding: 4px">2</span><span style="background-color: #00AA00; padding: 4px">__</span><span style="background-color: #AAAA00; padding: 4px">Add_new_table</span></span>
     <i class="fa fa-long-arrow-up" style="padding: 4px"></i>         <i class="fa fa-long-arrow-up" style="padding: 4px"></i>
 Version    Description</pre>
    </div>
    <div class="col-md-4">
        <h4>Undo Migrations</h4>
        <pre>Prefix  Separator
   <i class="fa fa-long-arrow-down" style="padding: 4px"></i>   <i class="fa fa-long-arrow-down" style="padding: 4px"></i>
   <span style="color: white; font-weight: bold"><span style="background-color: #0000AA; padding: 4px">U</span><span style="background-color: #AA0000; padding: 4px">2</span><span style="background-color: #00AA00; padding: 4px">__</span><span style="background-color: #AAAA00; padding: 4px">Add_new_table</span></span>
     <i class="fa fa-long-arrow-up" style="padding: 4px"></i>         <i class="fa fa-long-arrow-up" style="padding: 4px"></i>
 Version    Description</pre>
    </div>
    <div class="col-md-4">
        <h4>Repeatable Migrations</h4>
        <pre>Prefix Separator
    <i class="fa fa-long-arrow-down" style="padding: 4px"></i> <i class="fa fa-long-arrow-down" style="padding: 4px"></i>
    <span style="color: white; font-weight: bold"><span style="background-color: #0000AA; padding: 4px">R</span><span style="background-color: #00AA00; padding: 4px">__</span><span style="background-color: #AAAA00; padding: 4px">Add_new_table</span></span>
               <i class="fa fa-long-arrow-up" style="padding: 4px"></i>
           Description</pre>
    </div>
</div>

The class name consists of the following parts:
- **Prefix**: `V` for versioned migrations, `U` for undo migrations, `R` for repeatable migrations
- **Version**: Underscores (automatically replaced by dots at runtime) separate as many parts as you like (Not for repeatable migrations)
- **Separator**: `__` (two underscores)
- **Description**: Underscores (automatically replaced by spaces at runtime) separate the words

If you need more control over the class name, you can override the default convention by implementing the
[`JavaMigration`](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/api/migration/JavaMigration.html) interface directly.

This will allow you to name your class as you wish. Version, description and migration category are provided by
implementing the respective methods.

### Discovery

Flyway discovers Java-based migrations on the Java classpath in the packages referenced by the
[`locations`](Configuration/parameters/locations) property.

<pre class="filetree"><i class="fa fa-folder-open"></i> my-project
  <i class="fa fa-folder-open"></i> src
    <i class="fa fa-folder-open"></i> main
      <i class="fa fa-folder-open"></i> java
        <span><i class="fa fa-folder-open"></i> db
  <i class="fa fa-folder-open"></i> migration</span>            <i class="fa fa-long-arrow-left"></i> <code>classpath:db/migration</code>
            <i class="fa fa-file-text"></i> R__My_view
            <i class="fa fa-file-text"></i> U1_1__Fix_indexes
            <i class="fa fa-file-text"></i> V1__Initial_version
            <i class="fa fa-file-text"></i> V1_1__Fix_indexes
  <i class="fa fa-file-text"></i> pom.xml</pre>

New java migrations are **discovered automatically** through classpath scanning at runtime. The
scanning is recursive. Java migrations in subpackages of the specified ones are also picked up.

### Checksums and Validation

Unlike SQL migrations, Java migrations by default do not have a checksum and therefore do not participate in the
change detection of Flyway's validation. This can be remedied by implementing the
`getChecksum()` method, which you can then use to provide your own checksum, which will then be
stored and validated for changes.

### Sample Class
```java
package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import java.sql.PreparedStatement;

/**
 * Example of a Java-based migration.
 */
public class V1_2__Another_user extends BaseJavaMigration {
    public void migrate(Context context) throws Exception {
        try (PreparedStatement statement =
                 context
                     .getConnection()
                     .prepareStatement("INSERT INTO test_user (name) VALUES ('Obelix')")) {
            statement.execute();
        }
    }
}
```

Take care that your Java migration does not close the database connection, either explicitly or as a
result of a try-with-resources statement.

### Spring

If your application already uses Spring and you do not want to use JDBC directly you can easily use Spring JDBC's
`JdbcTemplate` instead:

```java
package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * Example of a Java-based migration using Spring JDBC.
 */
public class V1_2__Another_user extends BaseJavaMigration {
    public void migrate(Context context) {
        new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true))
                .execute("INSERT INTO test_user (name) VALUES ('Obelix')");
    }
}
```

## Script migrations
{% include teams.html %}

Sometimes it may be more desirable to use a scripting language for migrations. Flyway Teams currently supports the `.ps1`, `.bat`, `.cmd`, `.sh`, `.bash`, `.py` file extensions as migrations, and on non-windows platforms it also supports migrations without extensions (assuming a valid [shebang](https://en.wikipedia.org/wiki/Shebang_(Unix))).

These migrations follow the same [naming scheme](Concepts/migrations#naming) as SQL migrations, with only the file extension being different. For example `V1__execute_batch_tool.ps1` is a valid migration.

Script migrations can be used for a number of tasks such as:
- Triggering execution of a 3rd party application as part of the migrations (such as a batch upload tool)
- Cleaning up local files (such as those created by SQL*Plus SPOOL)

### Script Placeholder Replacement
Flyway also supports script placeholder replacement with configurable pre- and suffixes. Script placeholders can be read through environment variables in your chosen scripting language.

See [Script Placeholders](Configuration/Placeholders Configuration#script-migration-placeholders).

### Important notes
On linux, if executing an extensionless migration that is not set to be executable, Flyway will attempt to set the file to be executable before running it.

The migration checksum is only calculated for the migration itself, not for any files it references or loads.

## Transactions
Flyway wraps the execution of each migration script in a single transaction and applies them in order. For example, if I have 3 pending migrations on my target, calling `flyway migrate` looks like:

```
- Execute V001
  If success, commit and continue; else rollback (if possible) and stop - do not process any further pending migrations
- Execute V002
  If success, commit and continue; else rollback (if possible) and stop - do not process any further pending migrations
- Execute V003
  If success, commit and continue; else rollback (if possible) and stop - do not process any further pending migrations
```

Alternatively, for certain databases, for each `migrate`, you can configure Flyway to wrap the execution of all pending migrations in a single transaction by setting the [`group`](Configuration/parameters/group) property to `true`. This would look like:

```
Begin a transaction
  Execute V001
  Execute V002
  Execute V003
End transaction
If there are errors at any point, rollback to the starting point and stop processing.
```

If Flyway detects that a specific statement cannot be run within a transaction due to technical limitations of your
database, it won't run that migration within a transaction. Instead, it will be marked as *non-transactional*.

If the `group` property is set to true, then transactional and non-transactional statements cannot be mixed within a
migration run. You can allow this by setting the [`mixed`](Configuration/parameters/mixed) property to `true`. Note that
this is only applicable for PostgreSQL, Aurora PostgreSQL, SQL Server and SQLite which all have statements that do not
run at all within a transaction. This is not to be confused with implicit transactions, as they occur in MySQL or Oracle,
where even though a DDL statement was run within a transaction, the database will issue an implicit commit before and after
its execution.

### Manual override

If necessary, you can manually determine whether or not to execute a migration in a transaction. This is useful for
databases like PostgreSQL and SQL Server where certain statements can only execute outside a transaction.

For Java migrations, the `JavaMigration` interface has a method `canExecuteInTransaction`. This determines whether the execution
should take place inside a transaction. You can rely on `BaseJavaMigration`'s default behavior to return `true` or override
`canExecuteInTransaction` to execute certain migrations outside a transaction by returning `false`.

For SQL migrations, you can specify the script configuration property `executeInTransaction`. See [Script config](Configuration/Script Config Files).
### Important Note

If your database cleanly supports DDL statements within a transaction, failed migrations will always be rolled back
(unless they were marked as non-transactional).

If on the other hand your database does NOT cleanly supports DDL statements within a transaction (by for example
issuing an implicit commit before and after every DDL statement), Flyway won't be able to perform a clean rollback in
case of failure and will instead mark the migration as failed, indicating that some manual cleanup may be required.
You may also need to run [repair](Commands/repair) to remove the failed migration entry from the [schema
history table](#schema-history-table).

## Query Results

Migrations are primarily meant to be executed as part of release and deployment automation processes and there is rarely
the need to visually inspect the result of SQL queries.

There are however some scenarios where such manual inspection makes sense, and therefore Flyway will display query results in the usual tabular form when a `SELECT` statement (or any other statement that returns results) is executed.

### Toggling query results
{% include teams.html %}

To prevent Flyway from displaying query results, set the configuration option [`outputQueryResults`](Configuration/Parameters/Output Query Results) to `false`.

## Schema History Table

To keep track of which migrations have already been applied when and by whom, Flyway adds a special
**schema history table** to your schema. You can think of this table as a complete audit trail of all changes
performed against the schema. It also tracks migration checksums and whether or not the migrations were successful.

Read more about this in our getting started guide on [how Flyway works](Getting Started/how).

## Schema creation

By default, Flyway will attempt to create the schemas provided by the `schemas` and `defaultSchema` configuration options. This behavior can be toggled with the `createSchemas` configuration option.

This might be useful when you want complete control over how schemas are created.

### The `createSchemas` option and the Schema History Table

Flyway requires a schema for the schema history table to reside in before running a migration. When `createSchemas` is `false`, it will be impossible for the schema history table to be created, unless a schema already exists for it to reside in.

So, given a configuration like this:

```
flyway.createSchemas=false
flyway.schemas=my_schema
```

The following can happen if `createSchemas` is `false`:

- Run migrate
- `my_schema` *is not* created by Flyway
- Because `my_schema` is the default schema, Flyway attempts to create the schema history table in `my_schema`
- `my_schema` does not exist, so the operation fails

Therefore, when toggling `createSchemas` to `false`, the following setup is recommended:

- Set the default schema to `flyway_history_schema`
  - Either by setting `defaultSchema`, or placing it first in the `schemas` configuration option
- Set `initSql` to create `flyway_history_schema` if it doesn't exist
- Place your other schemas in the `schemas` property

So, given a configuration like this:

```
flyway.createSchemas=false
flyway.initSql=CREATE IF NOT EXISTS flyway_history_schema
flyway.schemas=flyway_history_schema,my_schema
```

The following will happen:

- Run migrate
- `initSql` is executed, so `flyway_history_schema` is created
- Because `flyway_history_schema` is the default schema, Flyway attempts to create the schema history table in `flyway_history_schema`
- `my_schema` *is not* created by Flyway
- Migrations run as normal
- Migrations are free to control creation of `my_schema`

## Migration States

| State              | Description                                                                                                                                               |
|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Pending`          | This migration has not been applied yet                                                                                                                   |
| `Success`          | This migration succeeded                                                                                                                                  |
| `Ignored`          | This migration will not be considered when running [`migrate`](Commands/migrate)                                                            |
| `Deleted`          | This is a migration that has been marked as deleted by [`repair`](Commands/undo)                                                            |
| `Available`        | This [`undo`](Commands/undo) migration is ready to be applied if desired                                                                    |
| `Undone`           | This versioned migration succeeded but has since been undone                                                                                              |
| `Above Target`     | This migration has not been applied yet and won't be applied because [`target`](Configuration/parameters/target) is set to a lower version |
| `Baseline`         | This migration has [`baselined`](Commands/baseline) this DB                                                                                 |
| `Below Baseline`   | This migration was not applied against this DB because the schema history table was [`baselined`](Commands/baseline) with a higher version  |
| `Missing`          | This migration succeeded and could not be resolved                                                                                                        |
| `Failed (Missing)` | This migration failed and could not be resolved                                                                                                           |
| `Failed`           | This migration failed                                                                                                                                     |
| `Failed (Future)`  | This migration failed and its version is higher than the schema history table's current version                                                           |
| `Future`           | This migration succeeded and its version is higher than the schema history table's current version                                                        |
| `Out of Order`     | This migration succeeded but it was applied out of order. Rerunning the entire migration history might produce different results!                         |
| `Outdated`         | This is a [`repeatable`](Tutorials/Tutorial Repeatable Migrations) migration that is outdated and should be re-applied                               |
| `Superseded`       | This is a [`repeatable`](Tutorials/Tutorial Repeatable Migrations) migration that is outdated and has already been superseded by a newer one         |
