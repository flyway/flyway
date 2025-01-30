---
menu: faq
subtitle: FAQ
---

# Frequently Asked Questions

- [I found a defect. Where should I report it?](#defect)
- [I have a feature request. Where should I submit it?](#feature-request)
- [I have a question. Where can I ask it?](#question)
- [Does Flyway support undo/downgrade/downward migrations?](#downgrade)
- [What is the best strategy for dealing with hot fixes?](#hot-fixes)
- [Can multiple nodes migrate in parallel?](#parallel)
- [Does Flyway perform a rollback if a migration fails?](#rollback)
- [Does Flyway support multiple schemas?](#multiple-schemas)
- [Does Flyway work with OSGi?](#osgi?)
- [Does Flyway support placeholder replacement?](#placeholders)
- [Does Flyway depend on Spring?](#spring)
- [Can I make structure changes to the DB outside of Flyway?](#outside-changes)
- [How do you repair the database after a failed migration?](#repair)
- [Why does clean drop individual objects instead of the schema itself?](#clean-objects)
- [What is the best strategy for handling database-specific sql?](#db-specific-sql)
- [How do I rebuild a Schema history table ?](#rebuild-sht)
- [Why is the flyway_schema_history table case-sensitive?](#case-sensitive)
- [How can I integrate Flyway with Hibernate in a CDI environment?](#hibernate-cdi)
- [How can I get process Flyway's output programmatically?](#programmatic)

## I found a defect. Where should I report it?

<a name="defect"></a>
Check the [issue tracker](https://github.com/flyway/flyway/issues?state=open) if someone else already reported it. If not, [raise a new issue](https://github.com/flyway/flyway/issues?state=open).

## I have a feature request. Where should I submit it?

<a name="feature"></a>
Check the [issue
tracker](https://github.com/flyway/flyway/issues?state=open) if someone else already suggested it.

If not, [raise a new issue](https://github.com/flyway/flyway/issues?state=open).

## I have a question. Where can I ask it?

<a name="question"></a>
There are a number of areas that Redgate and the community monitor where you might be able to find the answer already.

- [StackOverflow](http://stackoverflow.com) under the [flyway](http://stackoverflow.com/questions/tagged/flyway) tag.

- [Redgate Hub - Flyway forum](https://forum.red-gate.com/categories/flyway-general)

## What about undo/downgrade/downward migrations?

<a name="downgrade"></a>

Yes, Flyway does support [undo migrations](Commands/undo).

## What is the best strategy for dealing with hot fixes?

<a name="hot-fixes"></a>

You have a regular release schedule, say once per sprint. Version 7 is live and you are developing version 8. Version 8 contains DB Schema Changes. Suddenly hot fix is required for version 7, and it also needs a schema change.

**How can you deal with this?**

Even though the code may be branched, the database schema won\'t. It
will have a linear evolution.

This means that the emergency fix, say version 7.1 needs to be deployed as part of the hot fix AND the new version 8.

By the time version 8 will be deployed, Flyway will recognize that the migration version 7.1 has already been applied. It will ignore it and migrate to version 8.

When recreating the database, everything with be cleanly installed in order: version 7, 7.1 and 8.

**If this isn\'t an option** you can activate the outOfOrder property to allow Flyway to run the migrations out of order and fill the gaps.

## Can multiple nodes migrate in parallel?

<a name="parallel"></a>
Yes! Flyway uses the locking technology of your database to coordinate multiple nodes. This ensures that even if multiple instances of your application attempt to migrate the database at the same time, it still works. Cluster configurations are fully supported.

## Does Flyway perform a rollback if a migration fails?

<a name="rollback"></a>
Flyway runs each migration in a separate transaction. In case of failure this transaction is rolled back.

Unfortunately, today only DB2, PostgreSQL, Derby, EnterpriseDB and to a certain extent SQL Server support DDL statements inside a transaction. Other databases such as Oracle will implicitly sneak in a commit before and after each DDL statement, drastically reducing the effectiveness of this roll back.

One alternative if you want to work around this, is to include only a single DDL statement per migration. This solution however has the drawback of being quite cumbersome.

## Does Flyway support multiple schemas?

<a name="multiple-schemas"></a>
Yes! These are the recommended strategies for dealing with them

### Multiple identical schemas

If you have multiple identical schemas, say one per tenant, invoke Flyway in a loop and change `flyway.schemas` to match the name of the schema of the current tenant.

### The schemas are distinct, but have the same life-cycle:

Use a single Flyway instance. Flyway has support for this built-in.

 Fill the `flyway.schemas` property with the comma-separated list of schemas you wish to manage. All schemas will be tracked using a single schema history table that will be placed in the first schema of the list. 

Make sure the user of the datasource has the necessary grants for all schemas, and prefix the objects (tables, views, \...) you reference.

### The schemas have a distinct life-cycle or must be autonomous and cleanly separated:

Use multiple Flyway instances. Each instance manages its own schema and references its own schema history table. Place migrations for each schema in a distinct location.

Schema foo:

``` prettyprint
locations = /sql/foo
schemas = foo
table = flyway_schema_history
```

Schema bar:

``` prettyprint
locations = /sql/bar
schemas = bar
table = flyway_schema_history
```

## Does Flyway work with OSGi?

<a name="osgi"></a>
Yes! Flyway runs on Equinox and is well suited for OSGi and Eclipse RCP applications.

## Does Flyway support placeholder replacement?

<a name="placeholders"></a>
Yes! Flyway can replace placeholders in Sql migrations. The default
pattern is `${placeholder}`. This can be configured using the
`placeholderPrefix` and `placeholderSuffix` properties.

See [Placeholders](<Configuration/Flyway Namespace/Flyway Placeholders Namespace>) for more details.

## Does Flyway depend on Spring?
<a name="spring"></a>
Flyway does not depend on Spring.

If you have Spring Jdbc on the classpath, Flyway will be able to load Java migrations making use of Spring\'s convenient JdbcTemplate class.

## Can I make structure changes to the DB outside of Flyway?

<a name="outside-changes"></a>
No. One of the prerequisites for being able to rely on the metadata in the database and having reliable migrations is that ALL database changes are made by Flyway. No exceptions.

The price for this reliability is discipline. Uncontrolled changes have no room here as they will literally sabotage your confidence. Even simple things like adding an index can trip over a migration if it has already been added manually before.

## How do you repair the database after a failed migration?

<a name="repair"></a>
If your database supports DDL transactions, Flyway does the work for
you.

If your database doesn\'t then these are the steps to follow:

1. Manually undo the changes of the migration
2. Invoke the repair command
3. Fix the failed migration
4. Try again

## Why does `clean` drop individual objects instead of the schema itself?

<a name="clean-objects"></a>
`clean` will remove what Flyway created. If Flyway also created the schema itself, `clean` will drop it. Otherwise, it will only drop the objects within the schema.

## What is the best strategy for handling database-specific sql?

<a name="db-specific-sql"></a>
Assuming you use Derby in TEST and Oracle in PROD.

You can use the `flyway.locations` property. It would look like this:

TEST (Derby): `flyway.locations=sql/common,sql/derby`

PROD (Oracle): `flyway.locations=sql/common,sql/oracle`

You could then have the common statements (V1\_\_Create_table.sql) in
common and different copies of the DB-specific statements
(V2\_\_Alter_table.sql) in the db-specific locations.

An even better solution, in my opinion, is to have the same DB in prod and test. Yes, you do lose a bit of performance, but on the other hand you also eliminate another difference (and potential source of errors) between the environments.

## How do I rebuild a Schema history table

<a name="rebuild-sht"></a>
You may want to do this if your Schema history table has been accidentally dropped or if you have re-baselined your migrations and the previous deployment information is no longer applicable.

Steps:

1. Run the [Baseline command](commands/baseline) to provision the table
2. Populate the table without re-executing your migrations by running a Migrate command with the [Skip Executing Migrations](<Configuration/Flyway Namespace/Flyway Skip Executing Migrations Setting>) switch enabled.

## Why is the flyway_schema_history table case-sensitive?

<a name="case-sensitive"></a>
The flyway_schema_history is case-sensitive due to the quotes used in its creation script. This allows for characters not supported in identifiers otherwise.

The name (and case) can be configured through the `flyway.table`
property.

The table is an internal Flyway implementation detail and not part of the public API. It can therefore change from time to time.

## How can I integrate Flyway with Hibernate in a CDI environment?

<a name="hibernate-cdi"></a>
For Hibernate 4.X see this [StackOverflow answer](http://stackoverflow.com/questions/11071821/cdi-extension-for-flyway).

For Hibernate 5.X see [this issue](https://github.com/flyway/flyway/issues/1981).

## How can I get process Flyway's output programmatically?

<a name="programmatic"></a>
Most of Flyway's output defaults to human-readable. By using the [`outputType`](<Command-line Parameters/Output Type Parameter>) switch, you can enable JSON format instead which will make programmatically interacting with the output much easier.