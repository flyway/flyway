---
layout: documentation
menu: contributingDatabaseSupport
subtitle: Contributing Database Compatibility
---
# Contributing Database Compatibility to Flyway

Flyway follows an Open Source model for the Community edition. We welcome code contributions through Pull Requests on the [Flyway GitHub page](https://github.com/flyway/flyway). This article will provide help with contributing code to make Flyway compatible with a new database platform. 

Flyway supports migrations for a large number of database platforms in a unified and consistent way. It does this by abstracting away the details of each database into a set of classes for each platform, plus factory classes that construct the appropriate objects for the database at hand; all communication with the database is done through a JDBC connection. The advantage of this approach is that JDBC is a widely adopted standard; with little more than a JDBC driver and knowledge of the SQL dialect used by a database it is possible to make Flyway compatible with your database of choice.

## You will need...

*   A JDBC driver for your database.
*   A Java IDE that builds with Java 8 or higher. We use and recommend IntelliJ

**Note for organizations:** let us know whether you will allow us to ship your JDBC driver with Flyway. This helps us deliver a more frictionless out-of-the-box experience for end users. If you follow step 2 below, this will happen automatically.

## Getting started

Fork the [Flyway](https://github.com/flyway/flyway) repo. If you’re using IntelliJ, you should be able to open the Flyway top level folder and see a number of projects. Copy the file `/flyway-commandline/src/main/assembly/flyway.conf` to an accessible location on your machine. This location will be a temporary 'scratch' area for testing. Use this copy to set up the following properties:

*   `flyway.url` - the JDBC URL of your development database
*   `flyway.user` - the user account
*   `flyway.password` - the password to the database
*   `flyway.locations` - to point to an accessible folder where you can put test migrations.

You can now set up a run configuration in your IDE that will compile Flyway and run using your newly created configuration:

*   Main class: `org.flywaydb.commandline.Main`
*   Program arguments: `info -X -configFiles=<scratch location>\flyway.conf`
*   Classpath of module: `flyway-commandline`

Flyway itself should start. Since Flyway doesn’t yet support your database you should see a message like:

`org.flywaydb.core.api.FlywayException: Unable to autodetect JDBC driver for url: jdbc:mydatabase://<host>:<port>/<databasename>`

You’re now ready to start adding that database support. We’re going to assume your database platform is called **FooDb**. Change the obvious naming conventions to suit your database.

## Project structure

First of all, where should we put the changes? In versions of Flyway prior to 7.10.0, we would make changes to the Flyway core. However, 
we've more recently separated out database-specific code into separate modules, and in particular, community contributions should
go into `flyway-community-db-support`. This means there won't be any changes affecting critical areas of the product code for 
existing users.

## Let's code!

Here are all the changes and additions you'll need to make:

1.  Add a dependency to your JDBC driver to the `flyway-community-db-support` `pom.xml`
1.  Document the format of the JDBC connection url for your database. This is not necessary to make Flyway work but it will help adoption of your database!
1.  Create a new folder `foo` in `org.flywaydb.community.database` to contain your new classes.
1.  In the folder create classes `FooDatabase` (subclassed from Database), `FooSchema` (subclassed from Schema), and `FooTable` (subclassed from Table), using the canonical signatures. These classes make up Flyway’s internal representation of the parts of your database that it works on.
1.  Create class `FooParser` (subclassed from Parser) using the canonical signature. This represents a simplified version of a parser for your database’s dialect of SQL. When finished it will be able to decompose a migration script into separate statements and report on serious errors, but it does not need to fully understand them.
1.  Create a class `FooDatabaseType` subclassed from `DatabaseType` in the folder your created. This class acts as the collation class that brings together all the classes you created before. Implement the required methods. There are also some optional methods you can override to customize the behavior.
    *   `createSqlScriptFactory` - To use a custom SqlScriptFactory
    *   `createSqlScriptExecutorFactory` - To use a custom SqlScriptExecutorFactory
    *   `createExecutionStrategy` - To use a custom DatabaseExecutionStrategy
    *   `createTransactionalExecutionTemplate` - To use a custom ExecutionTemplate
    *   `setDefaultConnectionProps` - To set custom default connection properties
    *   `shutdownDatabase` - To run any necessary code to cleanup the database on shutdown
    *   `detectUserRequiredByUrl` - To skip prompting for user if the URL contains user information (e.g. user property, login file)
    *   `detectPasswordRequiredByUrl` - To skip prompting for password if the URL contains password information (e.g. key file, or password property)
1.  In `DatabaseTypeRegister.registerForDatabaseTypes` add a `registeredDatabaseTypes.add(new FooDatabaseType(classLoader));` line.
1.  Create class `FooConnection` subclassed from `Connection<FooDatabase>` using the canonical signature. This represents a JDBC connection to your database. You probably won’t use it in isolation but it is an important component of a `JdbcTemplate`, which provides numerous convenience methods for running queries on your database.  
    In the constructor of `FooConnection`, you can use the `jdbcTemplate` field of `Connection` to query for any database properties that you need to acquire immediately and maintain as part of the state of the connection. You will need to override the following methods as a minimum:
    *   `doRestoreOriginalState()` – to reset anything that a migration may have changed
    *   `getCurrentSchemaNameOrSearchPath()` – to return the current database schema for the connection, if this is a concept in your database, or the default schema name if not.
    *   `doChangeCurrentSchemaOrSearchPath()` – to change the current database schema, if this is a concept in your database. If not, use the default which is a no-op.
    *   `getSchema()` – to return a constructed `FooSchema` object.
1.  Add overrides for `FooDatabase` to customize it to fit the SQL conventions of your database:
    *   `doGetConnection()` - to return a new `FooConnection`
    *   `ensureSupported()` - to determine which versions of your database will be supported by Flyway. During development, you can leave this as a no-op.
    *   `getRawCreateScript()` - to return SQL appropriate for your database to create the schema history table. Refer to an existing database type to see the column types needed. The table name will be provided by the table argument. If the baseline argument is true, this method should also insert a row for the baseline migration.
    *   `getSelectStatement()` – to return SQL appropriate for your database to select all rows from the history table with installed\_rank greater than a parameter value.
    *   `getInsertStatement()` – to return SQL appropriate to insert a row into the history table with nine parameter values (corresponding to the table columns in order).
    *   `supportsDdlTransactions()` – to return whether the database can support executing DDL statements inside a transaction or not.
    *   `supportsChangingCurrentSchema()` – to return whether the database can support the concept of a current schema attached to a connection, which can be changed via SQL.
    *   `supportsEmptyMigrationDescription()` - if your database can't support an empty string in the description column of the history table verbatim (eg. Oracle implicitly converts it to NULL), override this to return false.
    *   `getBooleanTrue()` and `getBooleanFalse()` – to return string representations of the Boolean values as used in your database’s dialect of SQL. Typically these are "true" and "false", but could be, for example, "1" and "0"
    *   `doQuote()` - to return an escaped version of an identifier for use in SQL. Typically this is the provided value with a double-quote added either side, but could be, for example, square brackets either side as in SQL Server.
    *   `catalogIsSchema()` – to return true if the database uses a catalog to represent a single schema (eg. MySQL, SQLite); false if a catalog is a collection of schemas.
1.  Add overrides for `FooParser` to customize it to fit the SQL dialect your database uses:
    *   The constructor should call the superclass constructor with a peek depth. This determines how far in advance the parser looks to determine the nature of various symbols. 2 is a reasonable start, unless you know your database has two-character entities (like Snowflake DB’s `$$` for javascript delimiters) in which case start at 3.
    *   Override `getDefaultDelimiter()` if your database uses something other than a semicolon to delimit separate statements
    *   Override `getIdentifierQuote()` if your database uses something other than a double-quote to escape identifiers (eg. MySQL uses backticks)
    *   Override `getAlternativeIdentifierQuote()` if your database has a second way to escape identifiers in addition to double-quotes.
    *   Override `getAlternativeStringLiteralQuote()` if your database has a second way to mark string literals in addition to single-quotes (eg. MySql allows double-quotes)
    *   Override `getValidKeywords()` if your database has a different set of valid keywords to the standard ones. It's not strictly necessary to include keywords that cannot be found in migration scripts.
    *   There are other overrides available for handling more complex SQL; contact us for advice in these cases as it is beyond the scope of this guide.
1.  Add overrides for `FooSchema` to customize it to fit the SQL dialect your database uses:
    *   `doExists()` – to query whether the schema described exists in the database
    *   `doEmpty()` – to query whether the schema contains any sub-objects eg. tables, views, procedures.
    *   `getObjectCount()` – to query the number of objects of a given type that exist in the schema
    *   `doCreate()` – to create the schema in the database
    *   `doDrop()` – to drop the schema in the database
    *   `doClean()` – to drop all the objects that exist in the schema
    *   `doAllTables()` – to query for all the tables in the schema and return a populated array of `FooTable` objects
    *   `getTable()` – to return a `FooTable` object for the given name
1.  Add overrides for `FooTable` to customize it to fit the SQL dialect your database uses:
    *   `doDrop()` – to drop the table
    *   `doExists()` – to query whether the table described exists in the database
    *   `doLock()` – to lock the table with a read/write pessimistic lock until the end of the current transaction. This is used to prevent concurrent reads and writes to the schema history while a migration is underway. If your database doesn’t support table-level locks, do nothing.
1. Finally, expose your database support to the Flyway engine by adding an entry to `src/main/resources/META-INF/services/org.flywaydb.core.internal.database.DatabaseType`

## Try it!

You should at this point be able to drop your `flyway-community-db-support` jar into Flyway (the `lib` folder is preferable) and
the necessary driver jars into `drivers`, run the `flyway info` build configuration and see an empty version history. 
Congratulations! You have got a basic implementation up and running. You can now start creating migration scripts and running 
`flyway migrate` on them.

Basic SQL scripts should run with few problems, but you may find more edge cases, particularly in `Parser`. Look at the existing overrides for existing platforms for examples of how to deal with them. If you find you need to make more invasive changes in the core of Flyway, please do contact us for advice. We will need to test bigger changes ourselves against all our test instances before we can accept them.

## What's next?

Now that you've proved that Flyway can work with your database, you may wish to submit a request for your database to be listed as Compatible on our support pages. 

In this case you will need to:

+ Have completed every section of this tutorial
+ Submitted your code as a [Pull Request](https://github.com/flyway/flyway/pulls) for our review, remembering to include supporting material (e.g. test code, results, screenshots etc.) to prove compatibility
+ Completed any requested code changes
+ Signed the [Flyway CLA](https://cla-assistant.io/flyway/flyway) for your PR

Finally, adding compatibility for your database to Flyway is just the first step. If you want your database to become officially supported, you should learn more about [Flyway's Support Levels](/documentation/learnmore/database-support), and [process for obtaining Certification](/documentation/learnmore/getting-certified).
