---
subtitle: Should Execute Concept
---

The [`shouldExecute`](<Configuration/Script Configuration/Should Execute>) option in [Script Configuration](<Configuration/Script Configuration>) gives you control over whether a specific migration is executed 
when Flyway Migrate runs.

## Examples

### Executing migrations in a specific schema

Flyway comes with [default placeholders](<Configuration/placeholders configuration>) that are automatically populated with useful values, such as the user connecting to the database and the default schema used by Flyway, and these can be used in the expression provided to `shouldExecute`.

To control the execution of migrations based on the schema, we can use the `${flyway:defaultSchema}` default placeholder.

Let's say we have the following migrations:

```
V1__A_migration_1.sql
V2__B_migration_1.sql
```

Migrations `V1` should only be executed against schema `A`, and `V2` against schema `B`. 

We can achieve this by creating script configuration files:
- `V1__A_migration_1.sql.conf` for `V1` which contains the line `shouldExecute=${flyway:defaultSchema}==A`
- `V2__B_migration_1.sql.conf` for `V2` which contains the line `shouldExecute=${flyway:defaultSchema}==B`

With this, if we run `flyway migrate -defaultSchema="A"` then only `V1` will be executed, and `V2` will be ignored.

### The benefit of setting `shouldExecute=false`

Initially it may not be obvious how defining `shouldExecute` as `false` would be useful, since this just has the same effect as using [`cherryPick`](<Configuration/parameters/flyway/cherry pick>) with all the migrations you do want to execute.

However, we quite often want to defer the execution of a long duration migration so that it can run, for example, overnight. Using `cherryPick` can become unwieldy when the number of migrations you do want to execute is large. 
`cherryPick` also requires changing how you execute Flyway, which makes it less desirable in fully automated pipelines. 
Let's see how you can achieve this with `shouldExecute`

Assume we have the following migrations:

```
V1__shrt_migration_1.sql
V2__long_migration_1.sql
```

In this example, let's also assume that we have a basic automated pipeline running `flyway migrate` on each commit to version control.

Since migration `V2` takes too long to execute during a work day, we decide to execute it overnight, but don't want to delay executing `V1`.
We can still execute migration `V1` but first need to create a script configuration file:

```
V2__long_migration_1.sql.conf
shouldExecute=false
```

When we commit this to version control, migrations `V1` will automatically be applied, ignoring `V2`.

When we reach the end of the day and decide it's time to apply `V2`, we can modify the script configuration file and remove the line containing `shouldExecute=false` since the default value is `true`. 

We also need to modify the Flyway [outOfOrder](<Configuration/Parameters/Flyway/Out Of Order>) configuration file and set `outOfOrder=true`. 

Once we commit this, migration `V2` will automatically begin execution in our pipeline.
